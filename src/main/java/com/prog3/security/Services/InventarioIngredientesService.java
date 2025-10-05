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
            System.out.println("🔍 DEBUG - Tipo de producto: " + producto.getTipoProducto());
            System.out.println("🔍 DEBUG - esCombo(): " + producto.esCombo());
            System.out.println("🔍 DEBUG - esIndividual(): " + producto.esIndividual());
            System.out.println("🔍 DEBUG - Ingredientes requeridos: " + (producto.getIngredientesRequeridos() != null ? producto.getIngredientesRequeridos().size() : "null"));
            System.out.println("🔍 DEBUG - Ingredientes opcionales: " + (producto.getIngredientesOpcionales() != null ? producto.getIngredientesOpcionales().size() : "null"));

            // Descontar ingredientes requeridos (siempre se consumen independientemente del tipo)
            if (producto.getIngredientesRequeridos() != null) {
                System.out.println("📋 Procesando " + producto.getIngredientesRequeridos().size() + " ingredientes requeridos");
                for (IngredienteProducto ingredienteReq : producto.getIngredientesRequeridos()) {
                    double cantidadTotal = ingredienteReq.getCantidadNecesaria() * cantidadProducto;
                    descontarIngrediente(ingredienteReq.getIngredienteId(), cantidadTotal,
                            "Consumo automático - " + producto.getNombre(), procesadoPor);
                }
            } else {
                System.out.println("ℹ️ No hay ingredientes requeridos configurados");
            }

            // Manejar ingredientes opcionales: solo descontar si hay selección
            if (producto.getIngredientesOpcionales() != null && ingredientesSeleccionados != null && !ingredientesSeleccionados.isEmpty()) {
                System.out.println("🔸 Procesando selección de ingredientes opcionales para: " + ingredientesSeleccionados.size() + " ingredientes");
                for (IngredienteProducto ingredienteOpc : producto.getIngredientesOpcionales()) {
                    if (ingredientesSeleccionados.contains(ingredienteOpc.getIngredienteId())) {
                        double cantidadTotal = ingredienteOpc.getCantidadNecesaria() * cantidadProducto;
                        descontarIngrediente(ingredienteOpc.getIngredienteId(), cantidadTotal,
                                "Selección opcional - " + producto.getNombre(), procesadoPor);
                        System.out.println("✅ Descontado ingrediente opcional: " + ingredienteOpc.getIngredienteId() + ", cantidad: " + cantidadTotal);
                    }
                }
            } else {
                System.out.println("⚠️ Producto sin ingredientes opcionales seleccionados - no se descuenta nada de los opcionales");
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
            }            // ✅ CORRECCIÓN: Verificación mejorada de stock
            double stockActual = inventario.getCantidadActual();
            double stockAnterior = stockActual; // Guardamos el stock anterior para el registro

            // Validar que la cantidad sea positiva
            if (cantidad <= 0) {
                System.out.println("⚠️ Cantidad inválida para ingrediente " + inventario.getProductoNombre()
                        + ": " + cantidad + " - Omitiendo descuento");
                return; // No procesar movimientos de 0 o negativos
            }

            // Verificar stock suficiente
            if (stockActual < cantidad) {
                System.err.println("❌ STOCK INSUFICIENTE para ingrediente " + inventario.getProductoNombre()
                        + ". Stock actual: " + stockActual
                        + ", Cantidad requerida: " + cantidad
                        + " - NO SE REALIZARÁ EL DESCUENTO");

                // ✅ NUEVO: No continuar si no hay stock suficiente
                // Crear movimiento de advertencia pero sin descontar
                MovimientoInventario movimientoFallido = new MovimientoInventario();
                movimientoFallido.setInventarioId(inventario.get_id());
                movimientoFallido.setProductoId(ingredienteId);
                movimientoFallido.setProductoNombre(inventario.getProductoNombre());
                movimientoFallido.setTipoMovimiento("error");
                movimientoFallido.setCantidadAnterior(stockAnterior);
                movimientoFallido.setCantidadMovimiento(0.0); // Sin movimiento
                movimientoFallido.setCantidadNueva(stockActual); // Stock sin cambios
                movimientoFallido.setMotivo("ERROR: " + motivo + " - Stock insuficiente (Requerido: " + cantidad + ", Disponible: " + stockActual + ")");
                movimientoFallido.setResponsable(procesadoPor);
                movimientoFallido.setFecha(LocalDateTime.now());

                movimientoInventarioRepository.save(movimientoFallido);
                return; // Salir sin realizar descuento
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

            // ✅ CORRECCIÓN: Crear movimiento de inventario con validaciones
            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setInventarioId(inventario.get_id());
            movimiento.setProductoId(ingredienteId);
            movimiento.setProductoNombre(inventario.getProductoNombre());
            movimiento.setTipoMovimiento("salida");
            movimiento.setCantidadAnterior(stockAnterior);
            movimiento.setCantidadMovimiento(-cantidad); // Negativo para salidas
            movimiento.setCantidadNueva(inventario.getCantidadActual());
            movimiento.setResponsable(procesadoPor);
            movimiento.setFecha(LocalDateTime.now());

            // ✅ CORRECCIÓN: Motivo descriptivo y correcto
            String motivoCorregido = motivo;
            if (motivo.toLowerCase().contains("entrada") && cantidad > 0) {
                // Corregir motivos contradictorios
                motivoCorregido = motivo.replace("Entrada de", "Consumo de").replace("entrada", "consumo");
            }
            movimiento.setMotivo(motivoCorregido);
            movimiento.setResponsable(procesadoPor);
            movimiento.setFecha(LocalDateTime.now());

            movimientoInventarioRepository.save(movimiento);

            System.out.println("📝 Movimiento registrado: " + motivoCorregido + " - Cantidad: " + cantidad);

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

    /**
     * Descuenta ingredientes del inventario agrupando por producto e
     * ingrediente
     *
     * @param productoId ID del producto
     * @param cantidad Cantidad de productos
     * @param ingredientesSeleccionados IDs de ingredientes seleccionados (para
     * combos)
     * @param motivo Razón del descuento
     * @param referencia Referencia para agrupar movimientos (ej: ID del pedido)
     * @param responsable Quien realizó la operación
     * @return Mapa con resultados de la operación
     */
    public java.util.Map<String, Object> descontarIngredientesDelInventarioConAgrupacion(
            String productoId,
            int cantidad,
            List<String> ingredientesSeleccionados,
            String motivo,
            String referencia,
            String responsable) {

        java.util.Map<String, Object> resultado = new java.util.HashMap<>();
        java.util.List<java.util.Map<String, Object>> ingredientesDescontados = new java.util.ArrayList<>();

        try {
            System.out.println("🔄 Iniciando descuento con agrupación para producto: " + productoId);

            // Obtener el producto
            Optional<Producto> productoOpt = productoRepository.findById(productoId);
            if (!productoOpt.isPresent()) {
                resultado.put("error", "Producto no encontrado: " + productoId);
                System.err.println("❌ Producto no encontrado: " + productoId);
                return resultado;
            }

            Producto producto = productoOpt.get();
            resultado.put("producto", producto.getNombre());
            resultado.put("tipoProducto", producto.getTipoProducto());

            // Procesar ingredientes requeridos (siempre se descuentan)
            if (producto.getIngredientesRequeridos() != null) {
                for (IngredienteProducto ingredienteProducto : producto.getIngredientesRequeridos()) {
                    java.util.Map<String, Object> infoDescuento = descontarIngredienteIndividual(
                            ingredienteProducto.getIngredienteId(),
                            ingredienteProducto.getCantidadNecesaria() * cantidad,
                            motivo,
                            "Requerido: " + ingredienteProducto.getNombre(),
                            referencia,
                            responsable
                    );
                    ingredientesDescontados.add(infoDescuento);
                }
            }

            // Procesar ingredientes opcionales según el tipo de producto
            if (producto.getIngredientesOpcionales() != null) {
                for (IngredienteProducto ingredienteProducto : producto.getIngredientesOpcionales()) {
                    // Para productos individuales, descontar todos los opcionales
                    // Para combos, solo los seleccionados
                    boolean debeDescontar = producto.esCombo()
                            ? (ingredientesSeleccionados != null && ingredientesSeleccionados.contains(ingredienteProducto.getIngredienteId()))
                            : producto.esIndividual();

                    if (debeDescontar) {
                        java.util.Map<String, Object> infoDescuento = descontarIngredienteIndividual(
                                ingredienteProducto.getIngredienteId(),
                                ingredienteProducto.getCantidadNecesaria() * cantidad,
                                motivo,
                                "Opcional: " + ingredienteProducto.getNombre(),
                                referencia,
                                responsable
                        );
                        ingredientesDescontados.add(infoDescuento);
                    }
                }
            }

            resultado.put("ingredientesDescontados", ingredientesDescontados);
            resultado.put("totalIngredientes", ingredientesDescontados.size());
            resultado.put("exito", true);

            System.out.println("✅ Descuento con agrupación completado. Total ingredientes procesados: " + ingredientesDescontados.size());

        } catch (Exception e) {
            resultado.put("error", e.getMessage());
            resultado.put("exito", false);
            System.err.println("❌ Error en descuento con agrupación: " + e.getMessage());
            e.printStackTrace();
        }

        return resultado;
    }

    /**
     * Descuenta un ingrediente específico del inventario
     */
    private java.util.Map<String, Object> descontarIngredienteIndividual(
            String ingredienteId,
            double cantidad,
            String motivo,
            String observacion,
            String referencia,
            String responsable) {

        java.util.Map<String, Object> resultado = new java.util.HashMap<>();
        resultado.put("ingredienteId", ingredienteId);

        try {
            Optional<Ingrediente> ingredienteOpt = ingredienteRepository.findById(ingredienteId);
            if (!ingredienteOpt.isPresent()) {
                resultado.put("error", "Ingrediente no encontrado");
                System.err.println("❌ Ingrediente no encontrado: " + ingredienteId);
                return resultado;
            }

            Ingrediente ingrediente = ingredienteOpt.get();
            resultado.put("nombre", ingrediente.getNombre());
            resultado.put("unidad", ingrediente.getUnidad());

            // Verificar si es descontable
            if (!ingrediente.isDescontable()) {
                resultado.put("mensaje", "Ingrediente no descontable");
                resultado.put("descontado", false);
                System.out.println("⚠️ Ingrediente no descontable: " + ingrediente.getNombre());
                return resultado;
            }

            // Registrar cantidades para el movimiento
            double stockAnterior = ingrediente.getStockActual();
            double cantidadMovimiento = -cantidad; // Negativo porque es salida
            double stockNuevo = stockAnterior + cantidadMovimiento;

            // Actualizar el stock
            ingrediente.setStockActual(stockNuevo);
            ingredienteRepository.save(ingrediente);

            // Registrar el movimiento
            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setProductoId(ingredienteId);
            movimiento.setProductoNombre(ingrediente.getNombre());
            movimiento.setTipoMovimiento("salida");
            movimiento.setMotivo(motivo);
            movimiento.setCantidadAnterior(stockAnterior);
            movimiento.setCantidadMovimiento(cantidadMovimiento);
            movimiento.setCantidadNueva(stockNuevo);
            movimiento.setResponsable(responsable);
            movimiento.setReferencia(referencia);
            movimiento.setObservaciones(observacion);
            movimiento.setFecha(LocalDateTime.now());

            movimientoInventarioRepository.save(movimiento);

            // Información de resultado
            resultado.put("stockAnterior", stockAnterior);
            resultado.put("cantidadDescontada", cantidad);
            resultado.put("stockNuevo", stockNuevo);
            resultado.put("descontado", true);
            resultado.put("movimientoId", movimiento.get_id());

            System.out.println("✅ Ingrediente descontado: " + ingrediente.getNombre() + " - Cantidad: " + cantidad + " - Stock nuevo: " + stockNuevo);

        } catch (Exception e) {
            resultado.put("error", e.getMessage());
            resultado.put("descontado", false);
            System.err.println("❌ Error descontando ingrediente " + ingredienteId + ": " + e.getMessage());
        }

        return resultado;
    }

    /**
     * ✅ NUEVO: Valida si hay stock suficiente para un producto antes de
     * procesarlo
     *
     * @param productoId ID del producto
     * @param cantidad Cantidad de productos
     * @param ingredientesSeleccionados Ingredientes seleccionados
     * @return Map con resultado de validación
     */
    public java.util.Map<String, Object> validarStockDisponible(
            String productoId,
            int cantidad,
            List<String> ingredientesSeleccionados) {

        java.util.Map<String, Object> resultado = new java.util.HashMap<>();
        java.util.List<java.util.Map<String, Object>> ingredientesFaltantes = new java.util.ArrayList<>();
        java.util.List<java.util.Map<String, Object>> alertasBajo = new java.util.ArrayList<>();

        try {
            System.out.println("🔍 Validando stock para producto: " + productoId + " cantidad: " + cantidad);

            Optional<Producto> productoOpt = productoRepository.findById(productoId);
            if (!productoOpt.isPresent()) {
                resultado.put("stockSuficiente", false);
                resultado.put("error", "Producto no encontrado");
                return resultado;
            }

            Producto producto = productoOpt.get();
            resultado.put("producto", producto.getNombre());
            resultado.put("tipoProducto", producto.getTipoProducto());

            if (!producto.isTieneIngredientes()) {
                resultado.put("stockSuficiente", true);
                resultado.put("mensaje", "Producto sin ingredientes - No requiere validación");
                return resultado;
            }

            if (ingredientesSeleccionados == null) {
                ingredientesSeleccionados = java.util.List.of();
            }

            // Validar ingredientes requeridos
            if (producto.getIngredientesRequeridos() != null) {
                System.out.println("📋 Validando " + producto.getIngredientesRequeridos().size() + " ingredientes requeridos");
                for (IngredienteProducto ingredienteReq : producto.getIngredientesRequeridos()) {
                    validarIngredienteIndividual(
                            ingredienteReq, cantidad, "requerido", producto.getNombre(),
                            ingredientesFaltantes, alertasBajo
                    );
                }
            }

            // Validar ingredientes opcionales seleccionados
            if (producto.getIngredientesOpcionales() != null) {
                System.out.println("📋 Validando ingredientes opcionales seleccionados de " + producto.getIngredientesOpcionales().size() + " disponibles");
                for (IngredienteProducto ingredienteOpc : producto.getIngredientesOpcionales()) {
                    if (ingredientesSeleccionados.contains(ingredienteOpc.getIngredienteId())) {
                        System.out.println("   ✓ Validando opcional seleccionado: " + ingredienteOpc.getNombre());
                        validarIngredienteIndividual(
                                ingredienteOpc, cantidad, "opcional", producto.getNombre(),
                                ingredientesFaltantes, alertasBajo
                        );
                    }
                }
            }

            boolean stockSuficiente = ingredientesFaltantes.isEmpty();

            resultado.put("stockSuficiente", stockSuficiente);
            resultado.put("ingredientesFaltantes", ingredientesFaltantes);
            resultado.put("alertas", alertasBajo);
            resultado.put("totalIngredientesValidados",
                    (producto.getIngredientesRequeridos() != null ? producto.getIngredientesRequeridos().size() : 0)
                    + ingredientesSeleccionados.size());

            System.out.println("✅ Validación completada - Stock suficiente: " + stockSuficiente
                    + ", Faltantes: " + ingredientesFaltantes.size()
                    + ", Alertas: " + alertasBajo.size());

        } catch (Exception e) {
            resultado.put("stockSuficiente", false);
            resultado.put("error", "Error validando stock: " + e.getMessage());
            System.err.println("❌ Error en validación de stock: " + e.getMessage());
            e.printStackTrace();
        }

        return resultado;
    }

    /**
     * ✅ MÉTODO AUXILIAR: Valida un ingrediente individual
     */
    private void validarIngredienteIndividual(
            IngredienteProducto ingredienteProducto,
            int cantidad,
            String tipo,
            String nombreProducto,
            java.util.List<java.util.Map<String, Object>> ingredientesFaltantes,
            java.util.List<java.util.Map<String, Object>> alertasBajo) {

        try {
            Optional<Ingrediente> ingredienteOpt = ingredienteRepository.findById(ingredienteProducto.getIngredienteId());
            if (!ingredienteOpt.isPresent()) {
                System.err.println("❌ Ingrediente no encontrado: " + ingredienteProducto.getIngredienteId());
                return;
            }

            Ingrediente ingrediente = ingredienteOpt.get();
            double cantidadNecesaria = ingredienteProducto.getCantidadNecesaria() * cantidad;
            double stockActual = ingrediente.getStockActual();

            System.out.println("   🔍 " + ingrediente.getNombre()
                    + " - Stock: " + stockActual
                    + ", Necesario: " + cantidadNecesaria
                    + " (" + tipo + ")");

            if (stockActual < cantidadNecesaria) {
                // Stock insuficiente
                java.util.Map<String, Object> faltante = new java.util.HashMap<>();
                faltante.put("ingredienteId", ingrediente.get_id());
                faltante.put("nombre", ingrediente.getNombre());
                faltante.put("stockActual", stockActual);
                faltante.put("cantidadNecesaria", cantidadNecesaria);
                faltante.put("unidad", ingrediente.getUnidad());
                faltante.put("tipo", tipo);
                faltante.put("producto", nombreProducto);
                faltante.put("faltante", cantidadNecesaria - stockActual);
                ingredientesFaltantes.add(faltante);

                System.out.println("   ❌ STOCK INSUFICIENTE: " + ingrediente.getNombre()
                        + " (Faltante: " + (cantidadNecesaria - stockActual) + " " + ingrediente.getUnidad() + ")");
            } else if (stockActual - cantidadNecesaria <= (ingrediente.getStockMinimo() != null ? ingrediente.getStockMinimo() : 0)) {
                // Stock bajo pero suficiente
                java.util.Map<String, Object> alerta = new java.util.HashMap<>();
                alerta.put("ingrediente", ingrediente.getNombre());
                alerta.put("stockActual", stockActual);
                alerta.put("stockMinimo", ingrediente.getStockMinimo());
                alerta.put("stockDespues", stockActual - cantidadNecesaria);
                alerta.put("unidad", ingrediente.getUnidad());
                alertasBajo.add(alerta);

                System.out.println("   ⚠️ STOCK BAJO: " + ingrediente.getNombre()
                        + " (Después: " + (stockActual - cantidadNecesaria) + " " + ingrediente.getUnidad() + ")");
            } else {
                System.out.println("   ✅ Stock suficiente: " + ingrediente.getNombre());
            }
        } catch (Exception e) {
            System.err.println("❌ Error validando ingrediente: " + e.getMessage());
        }
    }
}
