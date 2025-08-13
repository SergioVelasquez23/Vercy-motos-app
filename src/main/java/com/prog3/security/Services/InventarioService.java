package com.prog3.security.Services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

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

    @Autowired
    private InventarioIngredientesService inventarioIngredientesService;

    /**
     * SISTEMA ACTUALIZADO - SIN DATOS ESTÁTICOS
     *
     * Procesa un pedido para descontar del inventario usando el nuevo sistema
     * de ingredientes dinámico basado en base de datos. Ya no utiliza datos
     * hardcodeados.
     */
    public void procesarPedidoParaInventario(Pedido pedido) {
        // Solo procesar pedidos activos o pagados (no cancelados ni cortesías)
        if ("cancelado".equals(pedido.getEstado()) || "cortesia".equals(pedido.getTipo())) {
            System.out.println("🚫 Pedido no procesado para inventario: Estado=" + pedido.getEstado() + ", Tipo=" + pedido.getTipo());
            return;
        }

        System.out.println("🔄======== PROCESANDO PEDIDO PARA INVENTARIO =========🔄");
        System.out.println("📋 Pedido ID: " + pedido.get_id());
        System.out.println("🍽️ Mesa: " + pedido.getMesa());
        System.out.println("📊 Estado: " + pedido.getEstado());
        System.out.println("🏷️ Tipo: " + pedido.getTipo());
        System.out.println("📅 Fecha: " + pedido.getFecha());

        // Procesar items del pedido si existen (sistema legacy)
        if (pedido.getItems() != null && !pedido.getItems().isEmpty()) {
            System.out.println("🛒 Cantidad de items a procesar: " + pedido.getItems().size());
            procesarItemsPedidoLegacy(pedido.getItems(), pedido);
        } else {
            System.out.println("⚠️ El pedido no tiene items para procesar");
        }

        System.out.println("✅======== FIN PROCESAMIENTO INVENTARIO =========✅");
    }

    /**
     * Procesa items del pedido (sistema actualizado)
     */
    private void procesarItemsPedidoLegacy(List<ItemPedido> items, Pedido pedido) {
        System.out.println("📦 Procesando " + items.size() + " items del pedido");

        for (ItemPedido item : items) {
            System.out.println("------------------------------------------");
            System.out.println("🍽️ Procesando item: " + item.getProductoNombre() + ", ProductoID=" + item.getProductoId());
            System.out.println("📊 Cantidad: " + item.getCantidad());

            // Buscar el producto para verificar si tiene ingredientes configurados
            Optional<Producto> productoOpt = theProductoRepository.findById(item.getProductoId());
            if (productoOpt.isPresent()) {
                Producto producto = productoOpt.get();
                System.out.println("✅ Producto encontrado: " + producto.getNombre());
                System.out.println("🔧 Tiene ingredientes: " + producto.isTieneIngredientes());

                // NUEVO SISTEMA: Verificar si el producto tiene ingredientes (combo o individual)
                if (producto.isTieneIngredientes()) {
                    System.out.println("🍽️ Producto con ingredientes detectado - usando NUEVO SISTEMA");
                    System.out.println("📋 Tipo de producto: " + producto.getTipoProducto());

                    try {
                        System.out.println("🔄 Llamando a inventarioIngredientesService.descontarIngredientesDelInventario...");
                        System.out.println("📋 Producto: " + producto.getNombre() + " (ID: " + producto.get_id() + ")");
                        System.out.println("📊 Cantidad: " + item.getCantidad());
                        System.out.println("🍽️ Ingredientes seleccionados: "
                                + (item.getIngredientesSeleccionados() != null ? item.getIngredientesSeleccionados() : "ninguno"));

                        if (item.getIngredientesSeleccionados() == null || item.getIngredientesSeleccionados().isEmpty()) {
                            System.out.println("⚠️ ADVERTENCIA: No hay ingredientes seleccionados para este item");
                            // Si el producto es un combo y no tiene ingredientes seleccionados, podría ser un error
                            if (producto.esCombo()) {
                                System.out.println("❌ ERROR: El producto es un combo pero no tiene ingredientes seleccionados");
                            }
                        }

                        // Usar el nuevo servicio de ingredientes
                        inventarioIngredientesService.descontarIngredientesDelInventario(
                                producto.get_id(),
                                item.getCantidad(),
                                item.getIngredientesSeleccionados(),
                                "Sistema" // procesadoPor
                        );
                        System.out.println("✅ Ingredientes descontados correctamente");
                    } catch (Exception e) {
                        System.err.println("❌ Error al descontar ingredientes: " + e.getMessage());
                        System.err.println("Error detallado: " + e.toString());
                        // Fallback al sistema legacy en caso de error
                        System.out.println("🔄 Intentando fallback al sistema legacy...");
                        descontarInventarioDirecto(item.getProductoId(), item.getCantidad(), pedido);
                    }
                } // SISTEMA LEGACY: Productos sin ingredientes configurados
                else if (producto.getIngredientesDisponibles() != null && !producto.getIngredientesDisponibles().isEmpty()) {
                    System.out.println("📋 Producto con ingredientes legacy - procesando ingredientes");
                    procesarIngredientesProducto(producto, item.getCantidad(), item.getIngredientesSeleccionados());
                } else {
                    System.out.println("🔄 Producto sin ingredientes configurados - procesamiento legacy");
                    // Mantener lógica legacy para productos sin ingredientes
                    descontarInventarioDirecto(item.getProductoId(), item.getCantidad(), pedido);
                }
            } else {
                System.out.println("❌ Producto no encontrado: " + item.getProductoId());
                // Intentar descuento directo por ID
                descontarInventarioDirecto(item.getProductoId(), item.getCantidad(), pedido);
            }
        }
    }

    /**
     * Procesa ingredientes de un producto (nuevo sistema)
     */
    // Adaptado: ahora acepta lista de IDs seleccionados
    private void procesarIngredientesProducto(Producto producto, int cantidad, List<String> ingredientesSeleccionados) {
        System.out.println("Procesando ingredientes seleccionados para producto: " + producto.getNombre());

        if (ingredientesSeleccionados == null || ingredientesSeleccionados.isEmpty()) {
            System.out.println("⚠️ No se recibieron ingredientes seleccionados, no se descuenta nada.");
            return;
        }

        for (String ingredienteId : ingredientesSeleccionados) {
            try {
                Optional<Ingrediente> ingredienteOpt = theIngredienteRepository.findById(ingredienteId);
                if (ingredienteOpt.isPresent()) {
                    Ingrediente ingrediente = ingredienteOpt.get();

                    // Cantidad por defecto: 1 unidad de ingrediente por producto
                    double cantidadADescontar = 1.0 * cantidad;

                    if (ingrediente.getStockActual() >= cantidadADescontar) {
                        ingrediente.setStockActual(ingrediente.getStockActual() - cantidadADescontar);
                        theIngredienteRepository.save(ingrediente);

                        System.out.println("Descontado ingrediente: " + ingrediente.getNombre()
                                + ", cantidad: " + cantidadADescontar
                                + ", stock restante: " + ingrediente.getStockActual());
                    } else {
                        System.err.println("ADVERTENCIA: Stock insuficiente para ingrediente: "
                                + ingrediente.getNombre()
                                + ". Requerido: " + cantidadADescontar
                                + ", Disponible: " + ingrediente.getStockActual());
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

        System.out.println("Movimiento de inventario registrado: " + inventario.getProductoNombre()
                + " - Cantidad: " + cantidad + " - Stock nuevo: " + inventario.getCantidadActual());
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

    /**
     * Devuelve ingredientes específicos al inventario (para cancelaciones
     * selectivas)
     */
    public void devolverIngredientesAlInventario(String pedidoId, String productoId,
            List<com.prog3.security.DTOs.CancelarProductoRequest.IngredienteADevolver> ingredientesADevolver,
            String procesadoPor) {
        System.out.println("======== DEVOLVIENDO INGREDIENTES AL INVENTARIO =========");
        System.out.println("Pedido ID: " + pedidoId);
        System.out.println("Producto ID: " + productoId);
        System.out.println("Procesado por: " + procesadoPor);

        for (com.prog3.security.DTOs.CancelarProductoRequest.IngredienteADevolver ingredienteDevolver : ingredientesADevolver) {
            if (ingredienteDevolver.isDevolver() && ingredienteDevolver.getCantidadADevolver() > 0) {
                try {
                    // Buscar el ingrediente
                    Optional<Ingrediente> ingredienteOpt = theIngredienteRepository.findById(ingredienteDevolver.getIngredienteId());

                    if (ingredienteOpt.isPresent()) {
                        Ingrediente ingrediente = ingredienteOpt.get();
                        double stockAnterior = ingrediente.getStockActual();
                        double nuevoStock = stockAnterior + ingredienteDevolver.getCantidadADevolver();

                        // Actualizar stock
                        ingrediente.setStockActual(nuevoStock);
                        theIngredienteRepository.save(ingrediente);

                        // Registrar movimiento de devolución
                        MovimientoInventario movimiento = new MovimientoInventario();
                        movimiento.setProductoId(ingredienteDevolver.getIngredienteId()); // Usar como referencia
                        movimiento.setProductoNombre(ingrediente.getNombre());
                        movimiento.setTipoMovimiento("ENTRADA");
                        movimiento.setCantidadMovimiento(ingredienteDevolver.getCantidadADevolver());
                        movimiento.setCantidadAnterior(stockAnterior);
                        movimiento.setCantidadNueva(nuevoStock);
                        movimiento.setMotivo("DEVOLUCIÓN POR CANCELACIÓN");
                        movimiento.setObservaciones("Devolución de " + ingrediente.getNombre()
                                + " por cancelación de producto en pedido " + pedidoId);
                        movimiento.setResponsable(procesadoPor);
                        movimiento.setFecha(LocalDateTime.now());
                        movimiento.setReferencia(pedidoId);

                        movimientoRepository.save(movimiento);

                        System.out.println("✅ Devuelto: " + ingrediente.getNombre()
                                + " - Cantidad: " + ingredienteDevolver.getCantidadADevolver()
                                + " - Stock anterior: " + stockAnterior
                                + " - Stock nuevo: " + nuevoStock);
                    } else {
                        System.err.println("❌ No se encontró ingrediente con ID: " + ingredienteDevolver.getIngredienteId());
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error devolviendo ingrediente "
                            + ingredienteDevolver.getNombreIngrediente() + ": " + e.getMessage());
                }
            } else {
                System.out.println("⏭️ Saltando devolución de: " + ingredienteDevolver.getNombreIngrediente()
                        + " - Motivo: " + ingredienteDevolver.getMotivoNoDevolucion());
            }
        }

        System.out.println("======== FIN DEVOLUCIÓN INGREDIENTES =========");
    }

    /**
     * Obtiene los ingredientes que fueron descontados para un producto
     * específico en un pedido Para mostrar en el frontend qué se puede devolver
     */
    public List<com.prog3.security.DTOs.CancelarProductoRequest.IngredienteADevolver>
            getIngredientesDescontadosParaProducto(String pedidoId, String productoId, int cantidadProducto) {

        List<com.prog3.security.DTOs.CancelarProductoRequest.IngredienteADevolver> ingredientesDescontados
                = new ArrayList<>();

        try {
            // Buscar el producto para obtener sus ingredientes
            Optional<Producto> productoOpt = theProductoRepository.findById(productoId);

            if (productoOpt.isPresent()) {
                Producto producto = productoOpt.get();

                if (producto.isTieneIngredientes()) {
                    // Procesar ingredientes requeridos
                    if (producto.getIngredientesRequeridos() != null) {
                        for (com.prog3.security.Models.IngredienteProducto ingredienteProducto : producto.getIngredientesRequeridos()) {
                            double cantidadDescontada = ingredienteProducto.getCantidadNecesaria() * cantidadProducto;

                            com.prog3.security.DTOs.CancelarProductoRequest.IngredienteADevolver ingredienteADevolver
                                    = new com.prog3.security.DTOs.CancelarProductoRequest.IngredienteADevolver(
                                            ingredienteProducto.getIngredienteId(),
                                            ingredienteProducto.getNombre(),
                                            cantidadDescontada,
                                            ingredienteProducto.getUnidad()
                                    );

                            ingredientesDescontados.add(ingredienteADevolver);
                        }
                    }

                    // TODO: También procesar ingredientes opcionales seleccionados en el pedido
                    // Esto requeriría modificar el modelo de pedido para guardar qué ingredientes fueron seleccionados
                }
            }
        } catch (Exception e) {
            System.err.println("Error obteniendo ingredientes descontados: " + e.getMessage());
        }

        return ingredientesDescontados;
    }

    // El método descontarIngrediente ha sido eliminado para evitar dependencia circular con InventarioIngredientesService
}
