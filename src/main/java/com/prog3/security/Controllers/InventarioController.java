package com.prog3.security.Controllers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.prog3.security.Models.Inventario;
import com.prog3.security.Models.MovimientoInventario;
import com.prog3.security.Repositories.InventarioRepository;
import com.prog3.security.Repositories.MovimientoInventarioRepository;
import com.prog3.security.Repositories.PedidoRepository;
import com.prog3.security.Models.Pedido;
import com.prog3.security.Models.ItemPedido;
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
    private InventarioService inventarioService;
    @Autowired
    private ResponseService responseService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<Inventario>>> findAll() {
        try {
            List<Inventario> inventarios = inventarioRepository.findAll();
            return responseService.success(inventarios, "Inventario obtenido exitosamente");
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
    public ResponseEntity<ApiResponse<Inventario>> update(@PathVariable String id, @RequestBody Inventario inventarioActualizado) {
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
            return responseService.success(movimientos, "Movimientos de inventario obtenidos correctamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener movimientos de inventario: " + e.getMessage());
        }
    }

    // Nuevo endpoint para procesar pedido y descontar inventario
    @PostMapping("/procesar-pedido/{pedidoId}")
    public ResponseEntity<ApiResponse<List<Inventario>>> procesarPedido(
            @PathVariable String pedidoId,
            @RequestBody Map<String, List<String>> ingredientesPorItem) {
        try {
            Pedido pedido = pedidoRepository.findById(pedidoId).orElse(null);
            if (pedido == null) {
                return responseService.notFound("Pedido no encontrado con ID: " + pedidoId);
            }
            // Asignar ingredientes seleccionados a cada item según el body
            if (pedido.getItems() != null) {
                for (int i = 0; i < pedido.getItems().size(); i++) {
                    ItemPedido item = pedido.getItems().get(i);
                    String key = item.getProductoId();
                    if (ingredientesPorItem.containsKey(key)) {
                        item.setIngredientesSeleccionados(ingredientesPorItem.get(key));
                    }
                }
            }
            inventarioService.procesarPedidoParaInventario(pedido);
            // Devolver los ingredientes actualizados
            List<Inventario> inventariosIngredientes = new java.util.ArrayList<>();
            if (pedido.getItems() != null) {
                for (ItemPedido item : pedido.getItems()) {
                    if (item.getIngredientesSeleccionados() != null) {
                        for (String ingredienteId : item.getIngredientesSeleccionados()) {
                            Inventario inv = inventarioRepository.findByProductoId(ingredienteId);
                            if (inv != null) {
                                inventariosIngredientes.add(inv);
                            }
                        }
                    }
                }
            }
            return responseService.success(inventariosIngredientes, "Ingredientes actualizados tras procesar el pedido");
        } catch (Exception e) {
            return responseService.internalError("Error al procesar el pedido para inventario: " + e.getMessage());
        }
    }
}
