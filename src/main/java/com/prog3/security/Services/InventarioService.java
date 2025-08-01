package com.prog3.security.Services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.prog3.security.Models.Inventario;
import com.prog3.security.Models.Ingrediente;
import com.prog3.security.Models.Producto;
import com.prog3.security.Models.Pedido;
import com.prog3.security.Models.ItemPedido;
import com.prog3.security.Models.MovimientoInventario;
import com.prog3.security.Repositories.InventarioRepository;
import com.prog3.security.Repositories.IngredienteRepository;
import com.prog3.security.Repositories.ProductoRepository;
import com.prog3.security.Repositories.MovimientoInventarioRepository;

@Service
public class InventarioService {

    @Autowired
    private InventarioRepository theInventarioRepository;

    @Autowired
    private IngredienteRepository theIngredienteRepository;

    @Autowired
    private ProductoRepository theProductoRepository;

    @Autowired
    private MovimientoInventarioRepository movimientoRepository;

    /**
     * SISTEMA ACTUALIZADO - SIN DATOS ESTÁTICOS
     * 
     * Procesa un pedido para descontar del inventario usando el nuevo sistema de ingredientes
     * dinámico basado en base de datos. Ya no utiliza datos hardcodeados.
     */
    public void procesarPedidoParaInventario(Pedido pedido) {
        // Solo procesar pedidos activos o pagados (no cancelados ni cortesías)
        if ("cancelado".equals(pedido.getEstado()) || "cortesia".equals(pedido.getTipo())) {
            System.out.println("Pedido no procesado para inventario: Estado=" + pedido.getEstado() + ", Tipo=" + pedido.getTipo());
            return;
        }

        System.out.println("======== PROCESANDO PEDIDO PARA INVENTARIO =========");
        System.out.println("Pedido ID: " + pedido.get_id());
        System.out.println("Mesa: " + pedido.getMesa());
        System.out.println("Estado: " + pedido.getEstado());
        System.out.println("Tipo: " + pedido.getTipo());
        System.out.println("Fecha: " + pedido.getFecha());

        // Procesar items del pedido si existen (sistema legacy)
        if (pedido.getItems() != null && !pedido.getItems().isEmpty()) {
            procesarItemsPedidoLegacy(pedido.getItems(), pedido);
        }

        System.out.println("======== FIN PROCESAMIENTO INVENTARIO =========");
    }

    /**
     * Procesa items del pedido (sistema legacy actualizado)
     */
    private void procesarItemsPedidoLegacy(List<ItemPedido> items, Pedido pedido) {
        System.out.println("Procesando " + items.size() + " items del pedido");
        
        for (ItemPedido item : items) {
            System.out.println("------------------------------------------");
            System.out.println("Procesando item: " + item.getProductoNombre() + ", ProductoID=" + item.getProductoId());
            System.out.println("Cantidad: " + item.getCantidad());

            // Buscar el producto para ver si tiene ingredientes configurados
            Optional<Producto> productoOpt = theProductoRepository.findById(item.getProductoId());
            if (productoOpt.isPresent()) {
                Producto producto = productoOpt.get();
                
                // Si el producto tiene ingredientes configurados, usar el nuevo sistema
                if (producto.getIngredientesDisponibles() != null && !producto.getIngredientesDisponibles().isEmpty()) {
                    System.out.println("Producto tiene ingredientes configurados, procesando ingredientes");
                    procesarIngredientesProducto(producto, item.getCantidad());
                } else {
                    System.out.println("Producto sin ingredientes configurados - procesamiento legacy");
                    // Mantener lógica legacy para productos sin ingredientes
                    descontarInventarioDirecto(item.getProductoId(), item.getCantidad(), pedido);
                }
            } else {
                System.out.println("Producto no encontrado: " + item.getProductoId());
                // Intentar descuento directo por ID
                descontarInventarioDirecto(item.getProductoId(), item.getCantidad(), pedido);
            }
        }
    }

    /**
     * Procesa ingredientes de un producto (nuevo sistema)
     */
    private void procesarIngredientesProducto(Producto producto, int cantidad) {
        System.out.println("Procesando ingredientes para producto: " + producto.getNombre());
        
        for (String ingredienteId : producto.getIngredientesDisponibles()) {
            try {
                Optional<Ingrediente> ingredienteOpt = theIngredienteRepository.findById(ingredienteId);
                if (ingredienteOpt.isPresent()) {
                    Ingrediente ingrediente = ingredienteOpt.get();
                    
                    // Cantidad por defecto: 1 unidad de ingrediente por producto
                    double cantidadADescontar = 1.0 * cantidad;
                    
                    if (ingrediente.getStockActual() >= cantidadADescontar) {
                        ingrediente.setStockActual(ingrediente.getStockActual() - cantidadADescontar);
                        theIngredienteRepository.save(ingrediente);
                        
                        System.out.println("Descontado ingrediente: " + ingrediente.getNombre() + 
                                         ", cantidad: " + cantidadADescontar + 
                                         ", stock restante: " + ingrediente.getStockActual());
                    } else {
                        System.err.println("ADVERTENCIA: Stock insuficiente para ingrediente: " + 
                                         ingrediente.getNombre() + 
                                         ". Requerido: " + cantidadADescontar + 
                                         ", Disponible: " + ingrediente.getStockActual());
                    }
                } else {
                    System.err.println("ADVERTENCIA: Ingrediente no encontrado con ID: " + ingredienteId);
                }
            } catch (Exception e) {
                System.err.println("Error al procesar ingrediente " + ingredienteId + ": " + e.getMessage());
            }
        }
    }

    /**
     * Método legacy para descontar directamente del inventario
     */
    private void descontarInventarioDirecto(String productoId, int cantidad, Pedido pedido) {
        System.out.println("LEGACY: Descuento directo del inventario para producto: " + productoId);
        
        try {
            // Buscar en inventario por producto ID
            Inventario inventario = theInventarioRepository.findByProductoId(productoId);
            if (inventario != null) {
                double cantidadActual = inventario.getCantidadActual();
                double cantidadDescontar = cantidad;

                if (cantidadActual >= cantidadDescontar) {
                    registrarSalidaInventario(inventario, cantidadDescontar, pedido);
                    System.out.println("Descuento exitoso del inventario legacy");
                } else {
                    System.err.println("ADVERTENCIA: Stock insuficiente en inventario legacy para producto " + productoId);
                    System.err.println("Requerido: " + cantidadDescontar + ", Disponible: " + cantidadActual);
                }
            } else {
                System.out.println("No se encontró inventario legacy para producto: " + productoId);
            }
        } catch (Exception e) {
            System.err.println("Error en descuento directo de inventario: " + e.getMessage());
        }
    }

    /**
     * Registra una salida en el inventario legacy
     */
    private void registrarSalidaInventario(Inventario inventario, double cantidad, Pedido pedido) {
        double cantidadAnterior = inventario.getCantidadActual();
        inventario.setCantidadActual(cantidadAnterior - cantidad);
        inventario.setFechaUltimaActualizacion(LocalDateTime.now());
        theInventarioRepository.save(inventario);

        // Crear movimiento de inventario
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setInventarioId(inventario.get_id());
        movimiento.setProductoId(inventario.getProductoId());
        movimiento.setProductoNombre(inventario.getProductoNombre());
        movimiento.setTipoMovimiento("salida");
        movimiento.setMotivo("venta");
        movimiento.setCantidadAnterior(cantidadAnterior);
        movimiento.setCantidadMovimiento(-cantidad);
        movimiento.setCantidadNueva(inventario.getCantidadActual());
        movimiento.setResponsable(pedido.getMesero() != null ? pedido.getMesero() : "sistema");
        movimiento.setReferencia(pedido.get_id());
        movimiento.setObservaciones("Pedido en mesa: " + pedido.getMesa());
        movimiento.setCostoUnitario(inventario.getCostoUnitario());
        movimiento.setFecha(LocalDateTime.now());

        movimientoRepository.save(movimiento);

        System.out.println("Movimiento de inventario registrado: " + inventario.getProductoNombre() + 
                         " - Cantidad: " + cantidad + " - Stock nuevo: " + inventario.getCantidadActual());
    }

    /**
     * Obtiene el stock actual de un ingrediente
     */
    public Double getStockIngrediente(String ingredienteId) {
        Optional<Ingrediente> ingrediente = theIngredienteRepository.findById(ingredienteId);
        return ingrediente.map(Ingrediente::getStockActual).orElse(0.0);
    }

    /**
     * Verifica si hay suficiente stock de un ingrediente
     */
    public boolean hayStockSuficiente(String ingredienteId, double cantidadRequerida) {
        Double stockActual = getStockIngrediente(ingredienteId);
        return stockActual >= cantidadRequerida;
    }

    /**
     * Obtiene todos los ingredientes con stock bajo
     */
    public List<Ingrediente> getIngredientesStockBajo() {
        return theIngredienteRepository.findByStockBajo();
    }

    /**
     * Actualiza el stock de un ingrediente manualmente
     */
    public void actualizarStockIngrediente(String ingredienteId, double nuevoStock) {
        Optional<Ingrediente> ingredienteOpt = theIngredienteRepository.findById(ingredienteId);
        if (ingredienteOpt.isPresent()) {
            Ingrediente ingrediente = ingredienteOpt.get();
            ingrediente.setStockActual(nuevoStock);
            theIngredienteRepository.save(ingrediente);
            System.out.println("Stock actualizado para " + ingrediente.getNombre() + ": " + nuevoStock);
        } else {
            System.err.println("No se encontró el ingrediente con ID: " + ingredienteId);
        }
    }
}
