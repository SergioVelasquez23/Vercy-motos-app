package com.prog3.security.Services;

import com.prog3.security.Models.*;
import com.prog3.security.Repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class InventarioIngredientesService {

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private MovimientoInventarioRepository movimientoInventarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private IngredienteRepository ingredienteRepository;

    /**
     * Descuenta los ingredientes del inventario cuando se crea un pedido
     *
     * @param productoId ID del producto pedido
     * @param cantidadProducto Cantidad del producto en el pedido
     * @param ingredientesSeleccionados Lista de ingredientes opcionales
     * seleccionados
     * @param procesadoPor Usuario que procesa la orden
     */
    public void descontarIngredientesDelInventario(String productoId, int cantidadProducto,
            List<String> ingredientesSeleccionados, String procesadoPor) {
        try {
            System.out.println("🔍 INICIO descontarIngredientesDelInventario para producto: " + productoId);
            System.out.println("📋 Ingredientes seleccionados: " + (ingredientesSeleccionados != null ? ingredientesSeleccionados : "ninguno"));
            System.out.println("📊 Cantidad del producto: " + cantidadProducto);

            Optional<Producto> productoOpt = productoRepository.findById(productoId);
            if (!productoOpt.isPresent()) {
                System.err.println("❌ Producto no encontrado: " + productoId);
                return;
            }

            Producto producto = productoOpt.get();

            // Solo procesar si el producto tiene ingredientes
            if (!producto.isTieneIngredientes()) {
                System.out.println("✅ Producto " + producto.getNombre() + " no maneja ingredientes");
                return;
            }

            System.out.println("🥘 Descontando ingredientes para " + producto.getTipoProducto() + ": " + producto.getNombre()
                    + " (Cantidad: " + cantidadProducto + ")");

            // Descontar ingredientes requeridos (siempre se consumen independientemente del tipo)
            if (producto.getIngredientesRequeridos() != null) {
                for (IngredienteProducto ingredienteReq : producto.getIngredientesRequeridos()) {
                    double cantidadTotal = ingredienteReq.getCantidadNecesaria() * cantidadProducto;
                    descontarIngrediente(ingredienteReq.getIngredienteId(), cantidadTotal,
                            "Consumo automático - " + producto.getNombre(), procesadoPor);
                }
            }

            // Manejar ingredientes opcionales según el tipo de producto
            if (producto.esCombo()) {
                // PRODUCTO COMBO: Solo descontar ingredientes opcionales seleccionados por el cliente
                if (producto.getIngredientesOpcionales() != null && ingredientesSeleccionados != null) {
                    System.out.println("🔸 Procesando selección de combo para: " + ingredientesSeleccionados.size() + " ingredientes");
                    for (IngredienteProducto ingredienteOpc : producto.getIngredientesOpcionales()) {
                        if (ingredientesSeleccionados.contains(ingredienteOpc.getIngredienteId())) {
                            double cantidadTotal = ingredienteOpc.getCantidadNecesaria() * cantidadProducto;
                            descontarIngrediente(ingredienteOpc.getIngredienteId(), cantidadTotal,
                                    "Selección de combo - " + producto.getNombre(), procesadoPor);
                        }
                    }
                }
            } else if (producto.esIndividual()) {
                // PRODUCTO INDIVIDUAL: Descontar TODOS los ingredientes opcionales por defecto
                if (producto.getIngredientesOpcionales() != null) {
                    System.out.println("🔹 Procesando producto individual - descontando todos los ingredientes por defecto");
                    for (IngredienteProducto ingredienteOpc : producto.getIngredientesOpcionales()) {
                        double cantidadTotal = ingredienteOpc.getCantidadNecesaria() * cantidadProducto;
                        descontarIngrediente(ingredienteOpc.getIngredienteId(), cantidadTotal,
                                "Consumo por defecto - " + producto.getNombre(), procesadoPor);
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Error al descontar ingredientes del producto " + productoId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Descuenta un ingrediente específico del inventario
     */
    private void descontarIngrediente(String ingredienteId, double cantidad, String motivo, String procesadoPor) {
        try {
            System.out.println("🔍 INICIO descontarIngrediente: " + ingredienteId + ", cantidad: " + cantidad);

            // Obtener primero el ingrediente para asegurar que existe
            Optional<Ingrediente> ingredienteOpt = ingredienteRepository.findById(ingredienteId);
            if (!ingredienteOpt.isPresent()) {
                System.err.println("❌ Ingrediente no existe en base de datos: " + ingredienteId);
                return;
            }

            Ingrediente ingrediente = ingredienteOpt.get();
            System.out.println("✅ Ingrediente encontrado: " + ingrediente.getNombre() + ", stock actual: " + ingrediente.getStockActual());

            // Buscar o crear el registro de inventario para este ingrediente
            Inventario inventario = inventarioRepository.findByProductoId(ingredienteId);

            if (inventario == null) {
                System.out.println("⚠️ Ingrediente no encontrado en inventario: " + ingredienteId + " - Creando registro...");
                // Crear un nuevo registro de inventario para este ingrediente
                inventario = new Inventario();
                inventario.setProductoId(ingredienteId);
                inventario.setProductoNombre(ingrediente.getNombre());
                inventario.setCategoria("Ingrediente");
                inventario.setCantidadActual(ingrediente.getStockActual());
                inventario.setCantidadMinima(ingrediente.getStockMinimo());
                inventario.setUnidadMedida(ingrediente.getUnidad());
                inventario.setCostoUnitario(0.0); // Valor por defecto
                inventario.setFechaUltimaActualizacion(LocalDateTime.now());
                inventario.setEstado("activo");

                inventario = inventarioRepository.save(inventario);
                System.out.println("✅ Registro de inventario creado para: " + ingrediente.getNombre());
            }            // Verificar que hay suficiente stock
            double stockActual = inventario.getCantidadActual();
            double stockAnterior = stockActual; // Guardamos el stock anterior para el registro

            if (stockActual < cantidad) {
                System.err.println("⚠️ Stock insuficiente para ingrediente " + inventario.getProductoNombre()
                        + ". Stock actual: " + stockActual
                        + ", Cantidad requerida: " + cantidad);
                // Continuar con el descuento hasta donde sea posible
                cantidad = stockActual;
            }

            // Actualizar el ingrediente - USAR EL QUE YA TENEMOS EN MEMORIA (evitar consultar de nuevo)
            double stockIngredienteActual = ingrediente.getStockActual();
            double stockIngredienteNuevo = Math.max(0, stockIngredienteActual - cantidad); // Evitar stock negativo
            ingrediente.setStockActual(stockIngredienteNuevo);
            ingredienteRepository.save(ingrediente);
            System.out.println("⬇️ Stock actualizado para " + ingrediente.getNombre()
                    + ": " + stockIngredienteActual + " -> " + stockIngredienteNuevo + " ("
                    + (stockIngredienteActual - stockIngredienteNuevo) + " unidades descontadas)");

            // Actualizar el stock en inventario
            double nuevoStock = Math.max(0, stockActual - cantidad); // Evitar stock negativo
            inventario.setCantidadActual(nuevoStock);
            inventario = inventarioRepository.save(inventario); // Asegurar que tenemos la versión actualizada
            System.out.println("⬇️ Inventario actualizado para " + inventario.getProductoNombre()
                    + ": " + stockActual + " -> " + nuevoStock);

            // Crear movimiento de inventario
            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setInventarioId(inventario.get_id());
            movimiento.setProductoId(ingredienteId);
            movimiento.setProductoNombre(inventario.getProductoNombre());
            movimiento.setTipoMovimiento("salida");
            movimiento.setCantidadAnterior(stockAnterior);
            movimiento.setCantidadMovimiento(-cantidad); // Negativo para salidas
            movimiento.setCantidadNueva(inventario.getCantidadActual());
            movimiento.setMotivo(motivo);
            movimiento.setResponsable(procesadoPor);
            movimiento.setFecha(LocalDateTime.now());

            movimientoInventarioRepository.save(movimiento);

            System.out.println("✅ Descontado " + cantidad + " " + inventario.getUnidadMedida()
                    + " de " + inventario.getProductoNombre()
                    + ". Stock anterior: " + stockAnterior
                    + ", Stock actual: " + inventario.getCantidadActual());

            // Verificar stock mínimo
            if (inventario.getCantidadActual() <= inventario.getCantidadMinima()) {
                System.out.println("⚠️ ALERTA: Stock bajo para " + inventario.getProductoNombre()
                        + ". Stock actual: " + inventario.getCantidadActual()
                        + ", Stock mínimo: " + inventario.getCantidadMinima());
            }

            // Verificar sincronización entre Ingrediente e Inventario
            if (Math.abs(ingrediente.getStockActual() - inventario.getCantidadActual()) > 0.0001) {
                System.out.println("⚠️ ALERTA: Desincronización entre Ingrediente e Inventario para " + ingrediente.getNombre());
                System.out.println("   Stock en Ingrediente: " + ingrediente.getStockActual());
                System.out.println("   Stock en Inventario: " + inventario.getCantidadActual());
                System.out.println("   Sincronizando valores...");

                // Sincronizar el stock del ingrediente con el inventario
                inventario.setCantidadActual(ingrediente.getStockActual());
                inventarioRepository.save(inventario);
                System.out.println("✅ Sincronización completada: " + ingrediente.getStockActual());
            }

        } catch (Exception e) {
            System.err.println("❌ Error al descontar ingrediente " + ingredienteId + ": " + e.getMessage());
            // Log error details instead of printing stack trace
            System.err.println("Error detallado: " + e.toString());
        }
    }

    /**
     * Obtiene los ingredientes opcionales disponibles para un producto Solo
     * retorna ingredientes si el producto es tipo "combo"
     */
    public List<IngredienteProducto> getIngredientesOpcionalesDisponibles(String productoId) {
        try {
            Optional<Producto> productoOpt = productoRepository.findById(productoId);
            if (productoOpt.isPresent() && productoOpt.get().isTieneIngredientes()) {
                Producto producto = productoOpt.get();

                // Solo devolver ingredientes opcionales si es un combo
                // Los productos individuales no permiten selección de ingredientes
                if (producto.esCombo()) {
                    return producto.getIngredientesOpcionales();
                } else {
                    System.out.println("ℹ️ Producto " + producto.getNombre()
                            + " es individual - no permite selección de ingredientes");
                    return List.of(); // Lista vacía para productos individuales
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error al obtener ingredientes opcionales para producto " + productoId + ": " + e.getMessage());
        }
        return List.of();
    }

    /**
     * Método auxiliar para obtener TODOS los ingredientes opcionales sin
     * importar el tipo (útil para administración)
     */
    public List<IngredienteProducto> getAllIngredientesOpcionales(String productoId) {
        try {
            Optional<Producto> productoOpt = productoRepository.findById(productoId);
            if (productoOpt.isPresent() && productoOpt.get().isTieneIngredientes()) {
                return productoOpt.get().getIngredientesOpcionales();
            }
        } catch (Exception e) {
            System.err.println("❌ Error al obtener ingredientes opcionales para producto " + productoId + ": " + e.getMessage());
        }
        return List.of();
    }

    /**
     * Verifica si hay suficientes ingredientes para un producto antes de
     * permitir la orden
     */
    public boolean verificarDisponibilidadIngredientes(String productoId, int cantidadProducto,
            List<String> ingredientesSeleccionados) {
        try {
            Optional<Producto> productoOpt = productoRepository.findById(productoId);
            if (!productoOpt.isPresent() || !productoOpt.get().isTieneIngredientes()) {
                return true; // Si no tiene ingredientes, está disponible
            }

            Producto producto = productoOpt.get();

            // Verificar ingredientes requeridos
            if (producto.getIngredientesRequeridos() != null) {
                for (IngredienteProducto ingredienteReq : producto.getIngredientesRequeridos()) {
                    double cantidadNecesaria = ingredienteReq.getCantidadNecesaria() * cantidadProducto;
                    if (!hayStockSuficiente(ingredienteReq.getIngredienteId(), cantidadNecesaria)) {
                        return false;
                    }
                }
            }

            // Verificar ingredientes opcionales según el tipo de producto
            if (producto.esCombo()) {
                // COMBO: Solo verificar ingredientes seleccionados
                if (producto.getIngredientesOpcionales() != null && ingredientesSeleccionados != null) {
                    for (IngredienteProducto ingredienteOpc : producto.getIngredientesOpcionales()) {
                        if (ingredientesSeleccionados.contains(ingredienteOpc.getIngredienteId())) {
                            double cantidadNecesaria = ingredienteOpc.getCantidadNecesaria() * cantidadProducto;
                            if (!hayStockSuficiente(ingredienteOpc.getIngredienteId(), cantidadNecesaria)) {
                                return false;
                            }
                        }
                    }
                }
            } else if (producto.esIndividual()) {
                // INDIVIDUAL: Verificar TODOS los ingredientes opcionales (se descontarán por defecto)
                if (producto.getIngredientesOpcionales() != null) {
                    for (IngredienteProducto ingredienteOpc : producto.getIngredientesOpcionales()) {
                        double cantidadNecesaria = ingredienteOpc.getCantidadNecesaria() * cantidadProducto;
                        if (!hayStockSuficiente(ingredienteOpc.getIngredienteId(), cantidadNecesaria)) {
                            return false;
                        }
                    }
                }
            }

            return true;
        } catch (Exception e) {
            System.err.println("❌ Error al verificar disponibilidad de ingredientes: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifica si hay stock suficiente de un ingrediente
     */
    private boolean hayStockSuficiente(String ingredienteId, double cantidadNecesaria) {
        try {
            Inventario inventario = inventarioRepository.findByProductoId(ingredienteId);
            if (inventario != null) {
                return inventario.getCantidadActual() >= cantidadNecesaria;
            }
        } catch (Exception e) {
            System.err.println("❌ Error al verificar stock del ingrediente " + ingredienteId + ": " + e.getMessage());
        }
        return false;
    }
}
