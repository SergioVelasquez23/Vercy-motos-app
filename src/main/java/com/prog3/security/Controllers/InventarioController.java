package com.prog3.security.Controllers;

import java.util.Set;
import com.prog3.security.Models.ItemPedido;

import java.util.Map;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.prog3.security.DTOs.DevolverIngredientesDTO;
import com.prog3.security.DTOs.DevolverIngredientesDTO.IngredienteDevueltoDTO;
import com.prog3.security.Models.Ingrediente;
import com.prog3.security.Models.IngredienteProducto;
import com.prog3.security.Models.Inventario;
import com.prog3.security.Services.InventarioIngredientesService;
import com.prog3.security.Models.MovimientoInventario;
import com.prog3.security.Models.Pedido;
import com.prog3.security.Models.Producto;
import com.prog3.security.Repositories.IngredienteRepository;
import com.prog3.security.Repositories.InventarioRepository;
import com.prog3.security.Repositories.MovimientoInventarioRepository;
import com.prog3.security.Repositories.PedidoRepository;
import com.prog3.security.Services.InventarioService;
import com.prog3.security.Services.ResponseService;
import com.prog3.security.Utils.ApiResponse;

@CrossOrigin
@RestController
@RequestMapping("/api/inventario")
public class InventarioController {
    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private MovimientoInventarioRepository movimientoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private IngredienteRepository ingredienteRepository;

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private ResponseService responseService;

    @Autowired
    private InventarioIngredientesService inventarioIngredientesService;

    @Autowired
    private com.prog3.security.Repositories.ProductoRepository productoRepository;

    // Endpoint de diagnóstico - ELIMINAR EN PRODUCCIÓN
    @PostMapping("/diagnostico-descuento")
    public ResponseEntity<ApiResponse<Map<String, Object>>> diagnosticoDescuentoIngredientes(
            @RequestParam String productoId,
            @RequestParam int cantidad,
            @RequestBody List<String> ingredientesSeleccionados) {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("productoId", productoId);
        resultado.put("cantidad", cantidad);
        resultado.put("ingredientesSeleccionados", ingredientesSeleccionados);
        try {
            Optional<com.prog3.security.Models.Producto> productoOpt = productoRepository.findById(productoId);
            if (productoOpt.isPresent()) {
                com.prog3.security.Models.Producto producto = productoOpt.get();
                resultado.put("nombreProducto", producto.getNombre());
                resultado.put("tipoProducto", producto.getTipoProducto());
                resultado.put("tieneIngredientes", producto.isTieneIngredientes());
                // Verificar si el ingrediente está en la lista de opcionales
                if (producto.getIngredientesOpcionales() != null) {
                    List<Map<String, Object>> coincidencias = new java.util.ArrayList<>();
                    for (com.prog3.security.Models.IngredienteProducto ingOpc : producto.getIngredientesOpcionales()) {
                        Map<String, Object> match = new HashMap<>();
                        match.put("ingredienteId", ingOpc.getIngredienteId());
                        match.put("nombre", ingOpc.getNombre());
                        match.put("coincide", ingredientesSeleccionados.contains(ingOpc.getIngredienteId()));
                        coincidencias.add(match);
                    }
                    resultado.put("coincidencias", coincidencias);
                }
                // Llamar al método real
                inventarioIngredientesService.descontarIngredientesDelInventario(
                        productoId, cantidad, ingredientesSeleccionados, "Diagnóstico");
                resultado.put("procesado", true);
            } else {
                resultado.put("error", "Producto no encontrado");
            }
        } catch (Exception e) {
            resultado.put("error", e.getMessage());
            e.printStackTrace();
        }
        return responseService.success(resultado, "Diagnóstico completado");
    }

    /**
     * ✅ NUEVO: Endpoint para validar stock disponible antes de procesar un pedido
     * Permite al frontend verificar si hay suficiente stock antes de agregar al carrito
     */
    @PostMapping("/validar-stock")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validarStock(
            @RequestParam String productoId,
            @RequestParam int cantidad,
            @RequestBody(required = false) List<String> ingredientesSeleccionados) {
        try {
            System.out.println("🔍 Validando stock para producto: " + productoId + ", cantidad: " + cantidad);
            
            if (ingredientesSeleccionados == null) {
                ingredientesSeleccionados = new ArrayList<>();
            }
            
            Map<String, Object> resultado = inventarioIngredientesService.validarStockDisponible(
                productoId, cantidad, ingredientesSeleccionados
            );
            
            boolean stockSuficiente = (Boolean) resultado.get("stockSuficiente");
            String mensaje = stockSuficiente 
                ? "Stock suficiente para procesar el pedido"
                : "Stock insuficiente para algunos ingredientes";
            
            // Agregar información adicional para el frontend
            resultado.put("timestamp", LocalDateTime.now());
            resultado.put("validacionExitosa", true);
            
            return responseService.success(resultado, mensaje);
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("stockSuficiente", false);
            errorResult.put("error", e.getMessage());
            errorResult.put("validacionExitosa", false);
            errorResult.put("timestamp", LocalDateTime.now());
            
            System.err.println("❌ Error validando stock: " + e.getMessage());
            return responseService.internalError("Error al validar stock: " + e.getMessage());
        }
    }

    /**
     * ✅ NUEVO: Endpoint para obtener información detallada de stock de un ingrediente
     * Útil para mostrar información detallada en el frontend
     */
    @GetMapping("/ingrediente/{ingredienteId}/info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getInfoIngrediente(@PathVariable String ingredienteId) {
        try {
            Optional<Ingrediente> ingredienteOpt = ingredienteRepository.findById(ingredienteId);
            if (!ingredienteOpt.isPresent()) {
                return responseService.notFound("Ingrediente no encontrado con ID: " + ingredienteId);
            }
            
            Ingrediente ingrediente = ingredienteOpt.get();
            Map<String, Object> info = new HashMap<>();
            
            // Información básica
            info.put("id", ingrediente.get_id());
            info.put("nombre", ingrediente.getNombre());
            info.put("stockActual", ingrediente.getStockActual());
            info.put("stockMinimo", ingrediente.getStockMinimo());
            info.put("unidad", ingrediente.getUnidad());
            info.put("descontable", ingrediente.isDescontable());
            
            // Estado del stock
            boolean stockBajo = ingrediente.getStockActual() <= ingrediente.getStockMinimo();
            boolean stockCritico = ingrediente.getStockActual() <= 0;
            
            info.put("estadoStock", stockCritico ? "critico" : (stockBajo ? "bajo" : "normal"));
            info.put("stockBajo", stockBajo);
            info.put("stockCritico", stockCritico);
            
            // Información del inventario si existe
            Inventario inventario = inventarioRepository.findByProductoId(ingredienteId);
            if (inventario != null) {
                info.put("inventarioId", inventario.get_id());
                info.put("cantidadInventario", inventario.getCantidadActual());
                info.put("fechaUltimaActualizacion", inventario.getFechaUltimaActualizacion());
                
                // Verificar sincronización
                boolean sincronizado = Math.abs(ingrediente.getStockActual() - inventario.getCantidadActual()) < 0.0001;
                info.put("sincronizado", sincronizado);
            } else {
                info.put("inventarioId", null);
                info.put("sincronizado", false);
                info.put("mensaje", "Sin registro de inventario - Se creará automáticamente si es necesario");
            }
            
            return responseService.success(info, "Información del ingrediente obtenida exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener información del ingrediente: " + e.getMessage());
        }
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> findAll() {
        try {
            List<Inventario> inventarios = inventarioRepository.findAll();
            List<Map<String, Object>> resultado = new ArrayList<>();
            for (Inventario inv : inventarios) {
                Map<String, Object> map = new HashMap<>();
                map.put("inventario", inv);
                // Unidad ya no está disponible
                resultado.add(map);
            }
            return responseService.success(resultado, "Inventario obtenido exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener el inventario: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Inventario>> findById(@PathVariable String id) {
        try {
            Inventario inventario = inventarioRepository.findById(id).orElse(null);
            if (inventario == null) {
                return responseService.notFound("Inventario no encontrado con ID: " + id);
            }
            return responseService.success(inventario, "Inventario encontrado");
        } catch (Exception e) {
            return responseService.internalError("Error al buscar el inventario: " + e.getMessage());
        }
    }

    @GetMapping("/producto/{productoId}")
    public ResponseEntity<ApiResponse<Inventario>> findByProducto(@PathVariable String productoId) {
        try {
            Inventario inventario = inventarioRepository.findByProductoId(productoId);
            if (inventario == null) {
                return responseService.notFound("Inventario no encontrado para el producto ID: " + productoId);
            }
            return responseService.success(inventario, "Inventario encontrado");
        } catch (Exception e) {
            return responseService.internalError("Error al buscar el inventario por producto: " + e.getMessage());
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<List<Inventario>>> findByNombre(@RequestParam String nombre) {
        try {
            List<Inventario> inventarios = inventarioRepository.findByProductoNombreContainingIgnoreCase(nombre);
            return responseService.success(inventarios, "Búsqueda de inventario completada");
        } catch (Exception e) {
            return responseService.internalError("Error al buscar inventario por nombre: " + e.getMessage());
        }
    }

    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<ApiResponse<List<Inventario>>> findByCategoria(@PathVariable String categoria) {
        try {
            List<Inventario> inventarios = inventarioRepository.findByCategoria(categoria);
            return responseService.success(inventarios, "Inventario por categoría obtenido");
        } catch (Exception e) {
            return responseService.internalError("Error al buscar inventario por categoría: " + e.getMessage());
        }
    }

    @GetMapping("/ubicacion/{ubicacion}")
    public ResponseEntity<ApiResponse<List<Inventario>>> findByUbicacion(@PathVariable String ubicacion) {
        try {
            List<Inventario> inventarios = inventarioRepository.findByUbicacion(ubicacion);
            return responseService.success(inventarios, "Inventario por ubicación obtenido");
        } catch (Exception e) {
            return responseService.internalError("Error al buscar inventario por ubicación: " + e.getMessage());
        }
    }

    @GetMapping("/stock-bajo")
    public ResponseEntity<ApiResponse<List<Inventario>>> findStockBajo() {
        try {
            List<Inventario> inventarios = inventarioRepository.findProductosConStockBajo();
            return responseService.success(inventarios, "Productos con stock bajo obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener productos con stock bajo: " + e.getMessage());
        }
    }

    @GetMapping("/agotados")
    public ResponseEntity<ApiResponse<List<Inventario>>> findAgotados() {
        try {
            List<Inventario> inventarios = inventarioRepository.findProductosAgotados();
            return responseService.success(inventarios, "Productos agotados obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener productos agotados: " + e.getMessage());
        }
    }

    @GetMapping("/proximos-vencer")
    public ResponseEntity<ApiResponse<List<Inventario>>> findProximosVencer(@RequestParam(defaultValue = "30") int dias) {
        try {
            LocalDateTime fechaLimite = LocalDateTime.now().plusDays(dias);
            List<Inventario> inventarios = inventarioRepository.findProductosProximosAVencer(fechaLimite);
            return responseService.success(inventarios, "Productos próximos a vencer obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener productos próximos a vencer: " + e.getMessage());
        }
    }

    @GetMapping("/resumen")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getResumenInventario() {
        try {
            Map<String, Object> resumen = new HashMap<>();

            List<Inventario> todos = inventarioRepository.findAll();
            List<Inventario> stockBajo = inventarioRepository.findProductosConStockBajo();
            List<Inventario> agotados = inventarioRepository.findProductosAgotados();

            double valorTotal = todos.stream().mapToDouble(Inventario::getCostoTotal).sum();

            resumen.put("totalProductos", todos.size());
            resumen.put("stockBajo", stockBajo.size());
            resumen.put("agotados", agotados.size());
            resumen.put("valorTotalInventario", valorTotal);

            return responseService.success(resumen, "Resumen de inventario obtenido exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener resumen de inventario: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Inventario>> create(@RequestBody Inventario inventario) {
        try {
            // Verificar si ya existe inventario para este producto
            if (inventarioRepository.existsByProductoId(inventario.getProductoId())) {
                return responseService.conflict("Ya existe inventario para este producto");
            }

            Inventario nuevoInventario = inventarioRepository.save(inventario);

            // Crear movimiento inicial
            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setInventarioId(nuevoInventario.get_id());
            movimiento.setProductoId(nuevoInventario.getProductoId());
            movimiento.setProductoNombre(nuevoInventario.getProductoNombre());
            movimiento.setTipoMovimiento("entrada");
            movimiento.setMotivo("inventario_inicial");
            movimiento.setCantidadAnterior(0);
            movimiento.setCantidadMovimiento(inventario.getCantidadActual());
            movimiento.setCantidadNueva(inventario.getCantidadActual());
            movimiento.setResponsable("sistema");
            movimiento.setCostoUnitario(inventario.getCostoUnitario());

            movimientoRepository.save(movimiento);

            return responseService.created(nuevoInventario, "Inventario creado exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al crear inventario: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Inventario>> update(@PathVariable String id,
            @RequestBody Inventario inventarioActualizado) {
        try {
            Inventario inventarioExistente = inventarioRepository.findById(id).orElse(null);
            if (inventarioExistente == null) {
                return responseService.notFound("Inventario no encontrado con ID: " + id);
            }

            // Guardar cantidad anterior para el movimiento
            double cantidadAnterior = inventarioExistente.getCantidadActual();

            // Actualizar datos
            inventarioExistente.setProductoNombre(inventarioActualizado.getProductoNombre());
            inventarioExistente.setCantidadMinima(inventarioActualizado.getCantidadMinima());
            inventarioExistente.setCantidadMaxima(inventarioActualizado.getCantidadMaxima());
            inventarioExistente.setUnidadMedida(inventarioActualizado.getUnidadMedida());
            inventarioExistente.setCostoUnitario(inventarioActualizado.getCostoUnitario());
            inventarioExistente.setProveedor(inventarioActualizado.getProveedor());
            inventarioExistente.setUbicacion(inventarioActualizado.getUbicacion());
            inventarioExistente.setEstado(inventarioActualizado.getEstado());
            inventarioExistente.setCategoria(inventarioActualizado.getCategoria());
            inventarioExistente.setFechaVencimiento(inventarioActualizado.getFechaVencimiento());
            inventarioExistente.setLote(inventarioActualizado.getLote());
            inventarioExistente.setObservaciones(inventarioActualizado.getObservaciones());

            // Si hay cambio en cantidad, crear movimiento
            if (cantidadAnterior != inventarioActualizado.getCantidadActual()) {
                inventarioExistente.setCantidadActual(inventarioActualizado.getCantidadActual());

                MovimientoInventario movimiento = new MovimientoInventario();
                movimiento.setInventarioId(inventarioExistente.get_id());
                movimiento.setProductoId(inventarioExistente.getProductoId());
                movimiento.setProductoNombre(inventarioExistente.getProductoNombre());
                movimiento.setTipoMovimiento("ajuste");
                movimiento.setMotivo("ajuste_inventario");
                movimiento.setCantidadAnterior(cantidadAnterior);
                movimiento.setCantidadMovimiento(inventarioActualizado.getCantidadActual() - cantidadAnterior);
                movimiento.setCantidadNueva(inventarioActualizado.getCantidadActual());
                movimiento.setResponsable("usuario"); // Debería venir del JWT
                movimiento.setCostoUnitario(inventarioExistente.getCostoUnitario());

                movimientoRepository.save(movimiento);
            }

            Inventario inventarioGuardado = inventarioRepository.save(inventarioExistente);
            return responseService.success(inventarioGuardado, "Inventario actualizado exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al actualizar inventario: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/entrada")
    public ResponseEntity<ApiResponse<Inventario>> registrarEntrada(@PathVariable String id, @RequestBody Map<String, Object> datos) {
        try {
            Inventario inventario = inventarioRepository.findById(id).orElse(null);
            if (inventario == null) {
                return responseService.notFound("Inventario no encontrado con ID: " + id);
            }

            double cantidad = Double.parseDouble(datos.get("cantidad").toString());
            String motivo = datos.get("motivo").toString();
            String responsable = datos.get("responsable").toString();
            String proveedor = datos.getOrDefault("proveedor", "").toString();
            String observaciones = datos.getOrDefault("observaciones", "").toString();

            double cantidadAnterior = inventario.getCantidadActual();
            inventario.setCantidadActual(cantidadAnterior + cantidad);

            // Crear movimiento
            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setInventarioId(inventario.get_id());
            movimiento.setProductoId(inventario.getProductoId());
            movimiento.setProductoNombre(inventario.getProductoNombre());
            movimiento.setTipoMovimiento("entrada");
            movimiento.setMotivo(motivo);
            movimiento.setCantidadAnterior(cantidadAnterior);
            movimiento.setCantidadMovimiento(cantidad);
            movimiento.setCantidadNueva(inventario.getCantidadActual());
            movimiento.setResponsable(responsable);
            movimiento.setProveedor(proveedor);
            movimiento.setObservaciones(observaciones);
            movimiento.setCostoUnitario(inventario.getCostoUnitario());

            inventarioRepository.save(inventario);
            movimientoRepository.save(movimiento);

            return responseService.success(inventario, "Entrada de inventario registrada exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al registrar entrada de inventario: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/salida")
    public ResponseEntity<ApiResponse<Inventario>> registrarSalida(@PathVariable String id, @RequestBody Map<String, Object> datos) {
        try {
            Inventario inventario = inventarioRepository.findById(id).orElse(null);
            if (inventario == null) {
                return responseService.notFound("Inventario no encontrado con ID: " + id);
            }

            double cantidad = Double.parseDouble(datos.get("cantidad").toString());
            String motivo = datos.get("motivo").toString();
            String responsable = datos.get("responsable").toString();
            String referencia = datos.getOrDefault("referencia", "").toString();
            String observaciones = datos.getOrDefault("observaciones", "").toString();

            if (inventario.getCantidadActual() < cantidad) {
                return responseService.badRequest("Stock insuficiente. Disponible: " + inventario.getCantidadActual());
            }

            double cantidadAnterior = inventario.getCantidadActual();
            inventario.setCantidadActual(cantidadAnterior - cantidad);

            // Crear movimiento
            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setInventarioId(inventario.get_id());
            movimiento.setProductoId(inventario.getProductoId());
            movimiento.setProductoNombre(inventario.getProductoNombre());
            movimiento.setTipoMovimiento("salida");
            movimiento.setMotivo(motivo);
            movimiento.setCantidadAnterior(cantidadAnterior);
            movimiento.setCantidadMovimiento(-cantidad);
            movimiento.setCantidadNueva(inventario.getCantidadActual());
            movimiento.setResponsable(responsable);
            movimiento.setReferencia(referencia);
            movimiento.setObservaciones(observaciones);
            movimiento.setCostoUnitario(inventario.getCostoUnitario());

            inventarioRepository.save(inventario);
            movimientoRepository.save(movimiento);

            return responseService.success(inventario, "Salida de inventario registrada exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al registrar salida de inventario: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/movimientos")
    public ResponseEntity<ApiResponse<List<MovimientoInventario>>> getMovimientos(@PathVariable String id) {
        try {
            List<MovimientoInventario> movimientos = movimientoRepository.findByInventarioId(id);
            return responseService.success(movimientos, "Movimientos de inventario obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener movimientos: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        try {
            if (!inventarioRepository.existsById(id)) {
                return responseService.notFound("Inventario no encontrado con ID: " + id);
            }
            inventarioRepository.deleteById(id);
            return responseService.success(null, "Inventario eliminado exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al eliminar inventario: " + e.getMessage());
        }
    }

    @GetMapping("/debug/estado-stock")
    public ResponseEntity<Map<String, Object>> debugEstadoStock() {
        try {
            List<Inventario> inventarios = inventarioRepository.findAll();
            Map<String, Object> debug = new HashMap<>();

            debug.put("totalItems", inventarios.size());
            debug.put("timestamp", LocalDateTime.now());

            // Mostrar inventarios con stock bajo
            List<Map<String, Object>> stockBajo = inventarios.stream()
                    .filter(inv -> inv.getCantidadActual() <= inv.getCantidadMinima())
                    .map(inv -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", inv.get_id());
                        item.put("nombre", inv.getProductoNombre());
                        item.put("stockActual", inv.getCantidadActual());
                        item.put("stockMinimo", inv.getCantidadMinima());
                        item.put("unidad", inv.getUnidadMedida());
                        return item;
                    })
                    .toList();

            debug.put("stockBajo", stockBajo);

            // Mostrar últimos movimientos
            List<MovimientoInventario> ultimosMovimientos = movimientoRepository
                    .findTop10ByOrderByFechaDesc();
            debug.put("ultimosMovimientos", ultimosMovimientos);

            return ResponseEntity.ok(debug);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @GetMapping("/movimientos")
    public ResponseEntity<ApiResponse<List<MovimientoInventario>>> getAllMovimientos() {
        try {
            List<MovimientoInventario> movimientos = movimientoRepository.findAll();
            
            // ✅ CORRECCIÓN: Convertir fechas a zona horaria de Colombia (UTC-5)
            movimientos.forEach(mov -> {
                if (mov.getFecha() != null) {
                    try {
                        // Convertir UTC a zona horaria de Colombia
                        ZonedDateTime fechaLocal = mov.getFecha()
                            .atZone(ZoneId.of("UTC"))
                            .withZoneSameInstant(ZoneId.of("America/Bogota"));
                        mov.setFecha(fechaLocal.toLocalDateTime());
                    } catch (Exception e) {
                        System.err.println("⚠️ Error convertir zona horaria para movimiento: " + mov.get_id() + " - " + e.getMessage());
                    }
                }
            });
            
            return responseService.success(movimientos, "Movimientos de inventario obtenidos correctamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener movimientos de inventario: " + e.getMessage());
        }
    }

    /**
     * ✅ NUEVO: Endpoint para limpiar movimientos erróneos del historial
     */
    @DeleteMapping("/movimientos/limpiar-errores")
    public ResponseEntity<ApiResponse<Map<String, Object>>> limpiarMovimientosErroneos() {
        try {
            // Buscar movimientos con cantidad 0.0 y tipo salida
            List<MovimientoInventario> movimientosErroneos = movimientoRepository
                .findByTipoMovimientoAndCantidadMovimiento("salida", 0.0);
            
            // También buscar movimientos con motivos contradictorios
            List<MovimientoInventario> movimientosContradictorios = movimientoRepository.findAll()
                .stream()
                .filter(mov -> mov.getMotivo() != null && 
                    mov.getMotivo().toLowerCase().contains("entrada") && 
                    mov.getTipoMovimiento().equals("salida") &&
                    mov.getCantidadMovimiento() <= 0.0)
                .collect(Collectors.toList());
            
            int totalEliminados = movimientosErroneos.size() + movimientosContradictorios.size();
            
            // Eliminar movimientos erróneos
            if (!movimientosErroneos.isEmpty()) {
                movimientoRepository.deleteAll(movimientosErroneos);
                System.out.println("🗑️ Eliminados " + movimientosErroneos.size() + " movimientos con cantidad 0.0");
            }
            
            if (!movimientosContradictorios.isEmpty()) {
                movimientoRepository.deleteAll(movimientosContradictorios);
                System.out.println("🗑️ Eliminados " + movimientosContradictorios.size() + " movimientos contradictorios");
            }
            
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("movimientosEliminados", totalEliminados);
            resultado.put("movimientosCantidadCero", movimientosErroneos.size());
            resultado.put("movimientosContradictorios", movimientosContradictorios.size());
            
            return responseService.success(resultado, 
                "Limpieza completada. " + totalEliminados + " movimientos erróneos eliminados");
                
        } catch (Exception e) {
            return responseService.internalError("Error limpiando movimientos: " + e.getMessage());
        }
    }
    
    /**
     * ✅ NUEVO: Endpoint para sincronizar inventario con ingredientes
     */
    @PostMapping("/sincronizar-con-ingredientes")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sincronizarInventarioConIngredientes() {
        try {
            List<Ingrediente> ingredientes = ingredienteRepository.findAll();
            Map<String, Object> resultado = new HashMap<>();
            List<String> sincronizados = new ArrayList<>();
            List<String> creados = new ArrayList<>();
            int contadorSincronizados = 0;
            int contadorCreados = 0;

            for (Ingrediente ingrediente : ingredientes) {
                Inventario inventario = inventarioRepository.findByProductoId(ingrediente.get_id());
                
                if (inventario == null) {
                    // Crear registro de inventario para ingrediente
                    inventario = new Inventario();
                    inventario.setProductoId(ingrediente.get_id());
                    inventario.setProductoNombre(ingrediente.getNombre());
                    inventario.setCategoria("Ingrediente");
                    inventario.setCantidadActual(ingrediente.getStockActual());
                    inventario.setCantidadMinima(ingrediente.getStockMinimo());
                    inventario.setUnidadMedida(ingrediente.getUnidad());
                    inventario.setCostoUnitario(0.0);
                    inventario.setFechaUltimaActualizacion(LocalDateTime.now());
                    inventario.setEstado("activo");
                    
                    inventarioRepository.save(inventario);
                    creados.add(ingrediente.getNombre());
                    contadorCreados++;
                } else {
                    // Sincronizar stock si hay diferencias
                    if (Math.abs(inventario.getCantidadActual() - ingrediente.getStockActual()) > 0.0001) {
                        inventario.setCantidadActual(ingrediente.getStockActual());
                        inventario.setFechaUltimaActualizacion(LocalDateTime.now());
                        inventarioRepository.save(inventario);
                        sincronizados.add(ingrediente.getNombre() + " (" + inventario.getCantidadActual() + " -> " + ingrediente.getStockActual() + ")");
                        contadorSincronizados++;
                    }
                }
            }

            resultado.put("ingredientesCreados", contadorCreados);
            resultado.put("ingredientesSincronizados", contadorSincronizados);
            resultado.put("detallesCreados", creados);
            resultado.put("detallesSincronizados", sincronizados);

            return responseService.success(resultado, "Sincronización completada exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al sincronizar inventario: " + e.getMessage());
        }
    }
    
    // **ENDPOINTS FALTANTES QUE EL FRONTEND NECESITA**
    
    @PostMapping("/devolver-ingredientes")
    public ResponseEntity<ApiResponse<DevolverIngredientesDTO>> devolverIngredientesAlInventario(
            @RequestBody Map<String, Object> request) {
        try {
            String pedidoId = (String) request.get("pedidoId");
            String productoId = (String) request.get("productoId");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> ingredientes = (List<Map<String, Object>>) request.get("ingredientes");
            String motivo = (String) request.get("motivo");
            String responsable = (String) request.get("responsable");
            Integer cantidadDevuelta = (Integer) request.getOrDefault("cantidadDevuelta", 1);
            
            // Obtener nombre del producto para el DTO
            String productoNombre = "Producto desconocido";
            Optional<Producto> productoOpt = productoRepository.findById(productoId);
            if (productoOpt.isPresent()) {
                productoNombre = productoOpt.get().getNombre();
            }
            
            List<IngredienteDevueltoDTO> ingredientesDevueltos = new java.util.ArrayList<>();
            
            // Procesar cada ingrediente para devolverlo al inventario
            for (Map<String, Object> ingredienteData : ingredientes) {
                String ingredienteId = (String) ingredienteData.get("ingredienteId");
                Double cantidadADevolver = ((Number) ingredienteData.get("cantidadADevolver")).doubleValue();
                
                // Buscar el ingrediente y actualizar su stock
                Optional<Ingrediente> ingredienteOpt = ingredienteRepository.findById(ingredienteId);
                if (ingredienteOpt.isPresent()) {
                    Ingrediente ingrediente = ingredienteOpt.get();
                    double stockAnterior = ingrediente.getStockActual();
                    double nuevoStock = stockAnterior + cantidadADevolver;
                    
                    ingrediente.setStockActual(nuevoStock);
                    ingredienteRepository.save(ingrediente);
                    
                    // Usar directamente el campo unidad del ingrediente
                    String unidadNombre = ingrediente.getUnidad();
                    String unidadAbreviatura = ingrediente.getUnidad();
                    IngredienteDevueltoDTO ingredienteDevueltoDTO = new IngredienteDevueltoDTO(
                        ingredienteId,
                        ingrediente.getNombre(),
                        cantidadADevolver,
                        unidadNombre,
                        unidadAbreviatura,
                        stockAnterior,
                        nuevoStock
                    );
                    ingredientesDevueltos.add(ingredienteDevueltoDTO);
                    
                    // Registrar movimiento de devolución
                    MovimientoInventario movimiento = new MovimientoInventario();
                    movimiento.setProductoId(ingredienteId);
                    movimiento.setProductoNombre(ingrediente.getNombre());
                    movimiento.setTipoMovimiento("entrada");
                    movimiento.setMotivo(motivo);
                    movimiento.setCantidadAnterior(stockAnterior);
                    movimiento.setCantidadMovimiento(cantidadADevolver);
                    movimiento.setCantidadNueva(nuevoStock);
                    movimiento.setResponsable(responsable);
                    movimiento.setReferencia(pedidoId);
                    movimiento.setObservaciones("Devolución por cancelación de producto");
                    movimiento.setFecha(LocalDateTime.now());
                    
                    movimientoRepository.save(movimiento);
                }
            }
            
            // Crear DTO de respuesta
            DevolverIngredientesDTO respuesta = new DevolverIngredientesDTO(
                productoId,
                productoNombre,
                cantidadDevuelta,
                ingredientesDevueltos,
                "Ingredientes devueltos al inventario exitosamente"
            );
            
            return responseService.success(respuesta, "Devolución de ingredientes completada");
        } catch (Exception e) {
            return responseService.internalError("Error al devolver ingredientes al inventario: " + e.getMessage());
        }
    }
    
    @GetMapping("/ingredientes-descontados")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getIngredientesDescontados(
            @RequestParam String pedidoId,
            @RequestParam String productoId) {
        try {
            // Buscar el producto para obtener sus ingredientes
            Optional<Producto> productoOpt = productoRepository.findById(productoId);
            if (productoOpt.isEmpty()) {
                return responseService.notFound("Producto no encontrado con ID: " + productoId);
            }
            
            Producto producto = productoOpt.get();
            List<Map<String, Object>> ingredientesDescontados = new java.util.ArrayList<>();
            
            // Procesar ingredientes requeridos
            if (producto.getIngredientesRequeridos() != null) {
                for (IngredienteProducto ip : producto.getIngredientesRequeridos()) {
                    Map<String, Object> ingredienteInfo = new HashMap<>();
                    ingredienteInfo.put("ingredienteId", ip.getIngredienteId());
                    ingredienteInfo.put("nombre", ip.getNombre());
                    ingredienteInfo.put("cantidadDescontada", ip.getCantidadNecesaria());
                    ingredienteInfo.put("unidad", ip.getUnidad());
                    ingredienteInfo.put("tipo", "requerido");
                    ingredientesDescontados.add(ingredienteInfo);
                }
            }
            
            // TODO: Procesar ingredientes opcionales seleccionados
            // Esto requiere que el pedido guarde qué ingredientes opcionales fueron seleccionados
            
            return responseService.success(ingredientesDescontados, "Ingredientes descontados obtenidos exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener ingredientes descontados: " + e.getMessage());
        }
    }

    // Nuevo endpoint para procesar pedido y descontar inventario
    // Método auxiliar para asegurar que los ingredientes tengan registro en inventario
    private Inventario asegurarInventarioIngrediente(String ingredienteId) {
        Inventario inv = inventarioRepository.findByProductoId(ingredienteId);

        // Si no existe, creamos un registro básico
        if (inv == null) {
            Optional<Ingrediente> ingOpt = ingredienteRepository.findById(ingredienteId);
            if (ingOpt.isPresent()) {
                Ingrediente ing = ingOpt.get();
                inv = new Inventario();
                inv.setProductoId(ingredienteId);
                inv.setProductoNombre(ing.getNombre());
                inv.setCategoria("Ingrediente");
                inv.setCantidadActual(ing.getStockActual());
                inv.setCantidadMinima(ing.getStockMinimo());
                inv.setUnidadMedida(ing.getUnidad());
                inv.setCostoUnitario(0.0); // Valor por defecto
                inv.setFechaUltimaActualizacion(LocalDateTime.now());

                // Guardar el nuevo registro de inventario
                inv = inventarioRepository.save(inv);
                System.out.println("✅ Creado nuevo registro de inventario para ingrediente: " + ing.getNombre());
            } else {
                System.err.println("❌ No se pudo crear inventario para ingrediente no encontrado: " + ingredienteId);
            }
        }

        return inv;
    }

    @PostMapping("/procesar-pedido/{pedidoId}")
    public ResponseEntity<ApiResponse<List<Inventario>>> procesarPedido(
            @PathVariable String pedidoId,
            @RequestBody Map<String, List<String>> ingredientesPorItem) {
        try {
            Pedido pedido = pedidoRepository.findById(pedidoId).orElse(null);
            if (pedido == null) {
                return responseService.notFound("Pedido no encontrado con ID: " + pedidoId);
            }

            System.out.println("🔄 Procesando pedido: " + pedidoId);
            System.out.println("📋 Ingredientes recibidos: " + ingredientesPorItem);

            // Asignar ingredientes seleccionados a cada item según el body
            if (pedido.getItems() != null) {
                for (ItemPedido item : pedido.getItems()) {
                    String key = item.getProductoId();
                    if (ingredientesPorItem.containsKey(key)) {
                        List<String> ingredientesSeleccionados = ingredientesPorItem.get(key);
                        System.out.println("🍽️ Asignando ingredientes a producto: " + item.getProductoNombre()
                                + " (ID: " + key + "): " + ingredientesSeleccionados);
                        item.setIngredientesSeleccionados(ingredientesSeleccionados);
                    } else {
                        System.out.println("⚠️ No se recibieron ingredientes para: " + item.getProductoNombre()
                                + " (ID: " + key + ")");
                    }
                }
            }

            // Procesar el pedido para actualizar el inventario
            inventarioService.procesarPedidoParaInventario(pedido);
            // Devolver los ingredientes actualizados
            List<Inventario> inventariosIngredientes = new java.util.ArrayList<>();
            Set<String> ingredientesProcessados = new java.util.HashSet<>();

            if (pedido.getItems() != null) {
                for (ItemPedido item : pedido.getItems()) {
                    // Buscar el producto para obtener sus ingredientes
                    Optional<Producto> productoOpt = productoRepository.findById(item.getProductoId());
                    if (productoOpt.isPresent()) {
                        Producto producto = productoOpt.get();

                        // Añadir ingredientes requeridos
                        if (producto.getIngredientesRequeridos() != null) {
                            for (IngredienteProducto ip : producto.getIngredientesRequeridos()) {
                                ingredientesProcessados.add(ip.getIngredienteId());
                            }
                        }

                        // Añadir ingredientes seleccionados
                        if (item.getIngredientesSeleccionados() != null) {
                            for (String ingredienteId : item.getIngredientesSeleccionados()) {
                                ingredientesProcessados.add(ingredienteId);
                            }
                        }

                        // Si es producto individual, añadir todos los opcionales
                        if (producto.esIndividual() && producto.getIngredientesOpcionales() != null) {
                            for (IngredienteProducto ip : producto.getIngredientesOpcionales()) {
                                ingredientesProcessados.add(ip.getIngredienteId());
                            }
                        }
                    }
                }
            }

            // Logging para depuración
            System.out.println("🔍 Ingredientes procesados encontrados: " + ingredientesProcessados.size());

            // Si no hay ingredientes procesados, intentar obtener directamente de la solicitud
            if (ingredientesProcessados.isEmpty()) {
                System.out.println("⚠️ No se encontraron ingredientes procesados. Intentando usar directamente los ingredientes enviados.");
                for (Map.Entry<String, List<String>> entry : ingredientesPorItem.entrySet()) {
                    String productoId = entry.getKey();
                    List<String> ingredientes = entry.getValue();

                    System.out.println("📋 Producto ID: " + productoId + ", Ingredientes: " + ingredientes);

                    // Añadir ingredientes directamente de la solicitud
                    if (ingredientes != null) {
                        ingredientesProcessados.addAll(ingredientes);
                    }
                }
                System.out.println("🔄 Total ingredientes después de procesar directamente: " + ingredientesProcessados.size());
            }

            // Obtener todos los inventarios de ingredientes procesados
            for (String ingredienteId : ingredientesProcessados) {
                System.out.println("🔍 Buscando inventario para ingrediente ID: " + ingredienteId);

                // Asegurarnos que exista registro en inventario
                Inventario inv = asegurarInventarioIngrediente(ingredienteId);
                if (inv != null) {
                    inventariosIngredientes.add(inv);
                    System.out.println("✅ Inventario encontrado para ingrediente: " + inv.getProductoNombre()
                            + ", cantidad: " + inv.getCantidadActual());
                } else {
                    System.out.println("❌ No se pudo crear/encontrar inventario para ingrediente ID: " + ingredienteId);
                }

                // También añadir info del ingrediente mismo
                Optional<Ingrediente> ingrediente = ingredienteRepository.findById(ingredienteId);
                if (ingrediente.isPresent()) {
                    System.out.println("✅ Ingrediente encontrado: " + ingrediente.get().getNombre()
                            + " - Stock actual: " + ingrediente.get().getStockActual());
                } else {
                    System.out.println("❌ No se encontró información del ingrediente con ID: " + ingredienteId);
                }
            }

            // Si después de todo no tenemos ingredientes, usar directamente los de la solicitud
            if (inventariosIngredientes.isEmpty() && ingredientesPorItem != null && !ingredientesPorItem.isEmpty()) {
                System.out.println("⚠️ No se pudieron procesar ingredientes. Obteniendo directamente los ingredientes enviados.");
                for (Map.Entry<String, List<String>> entry : ingredientesPorItem.entrySet()) {
                    List<String> ingredientesIds = entry.getValue();
                    for (String ingredienteId : ingredientesIds) {
                        Inventario inv = asegurarInventarioIngrediente(ingredienteId);
                        if (inv != null) {
                            inventariosIngredientes.add(inv);
                        }
                    }
                }
            }

            System.out.println("📊 Total de inventarios a devolver: " + inventariosIngredientes.size());

            return responseService.success(inventariosIngredientes, "Ingredientes actualizados tras procesar el pedido");
        } catch (Exception e) {
            return responseService.internalError("Error al procesar el pedido para inventario: " + e.getMessage());
        }
    }

    // ==============================
    // DEBUG ENDPOINTS - DIAGNOSTICAR PRODUCTOS
    // ==============================
    
    @GetMapping("/debug/producto/{productoId}")
    public ResponseEntity<Map<String, Object>> debugProducto(@PathVariable String productoId) {
        try {
            Map<String, Object> debug = new HashMap<>();
            
            // Buscar el producto
            Optional<Producto> productoOpt = productoRepository.findById(productoId);
            if (!productoOpt.isPresent()) {
                debug.put("error", "Producto no encontrado");
                return ResponseEntity.notFound().build();
            }
            
            Producto producto = productoOpt.get();
            
            // Información básica del producto
            debug.put("id", producto.get_id());
            debug.put("nombre", producto.getNombre());
            debug.put("tipoProducto", producto.getTipoProducto());
            debug.put("tieneIngredientes", producto.isTieneIngredientes());
            debug.put("esCombo", producto.esCombo());
            debug.put("esIndividual", producto.esIndividual());
            
            // Información de ingredientes
            List<Map<String, Object>> ingredientesRequeridos = new ArrayList<>();
            if (producto.getIngredientesRequeridos() != null) {
                for (IngredienteProducto ing : producto.getIngredientesRequeridos()) {
                    Map<String, Object> ingInfo = new HashMap<>();
                    ingInfo.put("ingredienteId", ing.getIngredienteId());
                    ingInfo.put("cantidadNecesaria", ing.getCantidadNecesaria());
                    
                    // Verificar stock
                    Inventario inventario = inventarioRepository.findByProductoId(ing.getIngredienteId());
                    if (inventario != null) {
                        ingInfo.put("stockDisponible", inventario.getCantidadActual());
                        ingInfo.put("stockMinimo", inventario.getCantidadMinima());
                        ingInfo.put("nombreIngrediente", inventario.getProductoNombre());
                    } else {
                        ingInfo.put("stockDisponible", "NO ENCONTRADO EN INVENTARIO");
                    }
                    
                    ingredientesRequeridos.add(ingInfo);
                }
            }
            debug.put("ingredientesRequeridos", ingredientesRequeridos);
            
            List<Map<String, Object>> ingredientesOpcionales = new ArrayList<>();
            if (producto.getIngredientesOpcionales() != null) {
                for (IngredienteProducto ing : producto.getIngredientesOpcionales()) {
                    Map<String, Object> ingInfo = new HashMap<>();
                    ingInfo.put("ingredienteId", ing.getIngredienteId());
                    ingInfo.put("cantidadNecesaria", ing.getCantidadNecesaria());
                    
                    // Verificar stock
                    Inventario inventario = inventarioRepository.findByProductoId(ing.getIngredienteId());
                    if (inventario != null) {
                        ingInfo.put("stockDisponible", inventario.getCantidadActual());
                        ingInfo.put("stockMinimo", inventario.getCantidadMinima());
                        ingInfo.put("nombreIngrediente", inventario.getProductoNombre());
                    } else {
                        ingInfo.put("stockDisponible", "NO ENCONTRADO EN INVENTARIO");
                    }
                    
                    ingredientesOpcionales.add(ingInfo);
                }
            }
            debug.put("ingredientesOpcionales", ingredientesOpcionales);
            
            // Diagnóstico
            List<String> diagnosticos = new ArrayList<>();
            if (!producto.isTieneIngredientes()) {
                diagnosticos.add("❌ Producto no está configurado para manejar ingredientes (tieneIngredientes = false)");
            }
            if (producto.esIndividual() && (producto.getIngredientesOpcionales() == null || producto.getIngredientesOpcionales().isEmpty())) {
                diagnosticos.add("⚠️ Producto individual sin ingredientes opcionales - no se descuentan ingredientes");
            }
            if (producto.esCombo() && (producto.getIngredientesOpcionales() == null || producto.getIngredientesOpcionales().isEmpty())) {
                diagnosticos.add("⚠️ Producto combo sin ingredientes opcionales - solo se permiten personalizaciones");
            }
            if (!producto.esCombo() && !producto.esIndividual()) {
                diagnosticos.add("❌ Tipo de producto no reconocido: '" + producto.getTipoProducto() + "'");
            }
            
            debug.put("diagnosticos", diagnosticos);
            
            return ResponseEntity.ok(debug);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al debuggar producto: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @PostMapping("/debug/test-descuento/{productoId}")
    public ResponseEntity<Map<String, Object>> testDescuentoProducto(
            @PathVariable String productoId,
            @RequestParam(defaultValue = "1") int cantidad,
            @RequestBody(required = false) List<String> ingredientesSeleccionados) {
        
        try {
            Map<String, Object> resultado = new HashMap<>();
            
            System.out.println("🧪 ===========================================");
            System.out.println("🧪 TEST DE DESCUENTO - INICIO");
            System.out.println("🧪 Producto ID: " + productoId);
            System.out.println("🧪 Cantidad: " + cantidad);
            System.out.println("🧪 Ingredientes seleccionados: " + ingredientesSeleccionados);
            System.out.println("🧪 ===========================================");
            
            // Llamar al método de descuento
            inventarioIngredientesService.descontarIngredientesDelInventario(
                productoId, 
                cantidad, 
                ingredientesSeleccionados, 
                "TEST-USER"
            );
            
            resultado.put("status", "success");
            resultado.put("message", "Test de descuento completado. Revisa los logs para ver el resultado.");
            resultado.put("productoId", productoId);
            resultado.put("cantidad", cantidad);
            resultado.put("ingredientesSeleccionados", ingredientesSeleccionados);
            
            System.out.println("🧪 ===========================================");
            System.out.println("🧪 TEST DE DESCUENTO - FIN");
            System.out.println("🧪 ===========================================");
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error en test de descuento: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(error);
        }
    }
}
