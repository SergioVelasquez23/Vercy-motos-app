package com.prog3.security.Controllers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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

import com.prog3.security.Models.Pedido;
import com.prog3.security.Models.TotalVentasResponse;
import com.prog3.security.Repositories.PedidoRepository;
import com.prog3.security.Services.ResponseService;
import com.prog3.security.Utils.ApiResponse;

@CrossOrigin
@RestController
@RequestMapping("api/pedidos")
public class PedidosController {

    @Autowired
    PedidoRepository thePedidoRepository;

    @Autowired
    private ResponseService responseService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<Pedido>>> find() {
        try {
            List<Pedido> pedidos = this.thePedidoRepository.findAll();
            return responseService.success(pedidos, "Pedidos obtenidos exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener pedidos: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Pedido>> findById(@PathVariable String id) {
        try {
            Pedido pedido = this.thePedidoRepository.findById(id).orElse(null);
            if (pedido == null) {
                return responseService.notFound("Pedido no encontrado con ID: " + id);
            }
            return responseService.success(pedido, "Pedido encontrado");
        } catch (Exception e) {
            return responseService.internalError("Error al buscar pedido: " + e.getMessage());
        }
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<ApiResponse<List<Pedido>>> findByTipo(@PathVariable String tipo) {
        try {
            List<Pedido> pedidos = this.thePedidoRepository.findByTipo(tipo);
            return responseService.success(pedidos, "Pedidos filtrados por tipo obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al filtrar pedidos por tipo: " + e.getMessage());
        }
    }

    @GetMapping("/mesa/{mesa}")
    public ResponseEntity<ApiResponse<List<Pedido>>> findByMesa(@PathVariable String mesa) {
        try {
            List<Pedido> pedidos = this.thePedidoRepository.findByMesa(mesa);
            return responseService.success(pedidos, "Pedidos de la mesa obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener pedidos de la mesa: " + e.getMessage());
        }
    }

    @GetMapping("/cliente/{cliente}")
    public ResponseEntity<ApiResponse<List<Pedido>>> findByCliente(@PathVariable String cliente) {
        try {
            List<Pedido> pedidos = this.thePedidoRepository.findByCliente(cliente);
            return responseService.success(pedidos, "Pedidos del cliente obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener pedidos del cliente: " + e.getMessage());
        }
    }

    @GetMapping("/mesero/{mesero}")
    public ResponseEntity<ApiResponse<List<Pedido>>> findByMesero(@PathVariable String mesero) {
        try {
            List<Pedido> pedidos = this.thePedidoRepository.findByMesero(mesero);
            return responseService.success(pedidos, "Pedidos del mesero obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener pedidos del mesero: " + e.getMessage());
        }
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<ApiResponse<List<Pedido>>> findByEstado(@PathVariable String estado) {
        try {
            List<Pedido> pedidos = this.thePedidoRepository.findByEstado(estado);
            return responseService.success(pedidos, "Pedidos filtrados por estado obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al filtrar pedidos por estado: " + e.getMessage());
        }
    }

    @GetMapping("/fechas")
    public ResponseEntity<ApiResponse<List<Pedido>>> findByFechaRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        try {
            List<Pedido> pedidos = this.thePedidoRepository.findByFechaBetween(fechaInicio, fechaFin);
            return responseService.success(pedidos, "Pedidos filtrados por fecha obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al filtrar pedidos por fecha: " + e.getMessage());
        }
    }

    @GetMapping("/hoy")
    public ResponseEntity<ApiResponse<List<Pedido>>> findHoy() {
        try {
            LocalDateTime inicioHoy = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            List<Pedido> pedidos = this.thePedidoRepository.findByFechaGreaterThanEqual(inicioHoy);
            return responseService.success(pedidos, "Pedidos de hoy obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener pedidos de hoy: " + e.getMessage());
        }
    }

    @GetMapping("/plataforma/{plataforma}")
    public ResponseEntity<ApiResponse<List<Pedido>>> findByPlataforma(@PathVariable String plataforma) {
        try {
            List<Pedido> pedidos = this.thePedidoRepository.findByPlataforma(plataforma);
            return responseService.success(pedidos, "Pedidos filtrados por plataforma obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al filtrar pedidos por plataforma: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Pedido>> create(@RequestBody Pedido newPedido) {
        try {
            // Validaciones básicas
            if (newPedido.getTipo() == null || newPedido.getTipo().isEmpty()) {
                return responseService.badRequest("El tipo de pedido es requerido");
            }

            if (newPedido.getItems() == null || newPedido.getItems().isEmpty()) {
                return responseService.badRequest("El pedido debe tener al menos un item");
            }

            // Asegurar que la fecha esté establecida
            if (newPedido.getFecha() == null) {
                newPedido.setFecha(LocalDateTime.now());
            }

            // Asegurar que el estado esté establecido
            if (newPedido.getEstado() == null || newPedido.getEstado().isEmpty()) {
                newPedido.setEstado("pendiente");
            }

            // Si es un pedido de mesa, validar que la mesa no sea null
            if ("normal".equals(newPedido.getTipo()) && (newPedido.getMesa() == null || newPedido.getMesa().isEmpty())) {
                return responseService.badRequest("Para pedidos normales, la mesa es requerida");
            }

            // Calcular el total del pedido si no viene establecido
            if (newPedido.getTotal() == 0.0 && !newPedido.getItems().isEmpty()) {
                double total = newPedido.getItems().stream()
                        .mapToDouble(item -> item.getPrecio() * item.getCantidad())
                        .sum();
                newPedido.setTotal(total);
            }

            Pedido pedidoCreado = this.thePedidoRepository.save(newPedido);

            // Log para debug
            System.out.println("Pedido creado con ID: " + pedidoCreado.get_id());

            // Verificar que el ID no sea null antes de retornar
            if (pedidoCreado.get_id() == null || pedidoCreado.get_id().isEmpty()) {
                System.out.println("ERROR: El pedido se guardó pero el ID está vacío");
                // Intentar recuperar el pedido recién creado de la base de datos
                List<Pedido> ultimosPedidos = this.thePedidoRepository.findByFechaGreaterThanEqual(LocalDateTime.now().minusMinutes(1));
                if (!ultimosPedidos.isEmpty()) {
                    pedidoCreado = ultimosPedidos.get(ultimosPedidos.size() - 1);
                    System.out.println("Pedido recuperado con ID: " + pedidoCreado.get_id());
                }
            }

            if (pedidoCreado.get_id() == null) {
                return responseService.internalError("Error al generar el ID del pedido");
            }

            return responseService.created(pedidoCreado, "Pedido creado exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al crear pedido: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Pedido>> update(@PathVariable String id, @RequestBody Pedido newPedido) {
        try {
            Pedido actualPedido = this.thePedidoRepository.findById(id).orElse(null);
            if (actualPedido == null) {
                return responseService.notFound("Pedido no encontrado con ID: " + id);
            }

            actualPedido.setTipo(newPedido.getTipo());
            actualPedido.setMesa(newPedido.getMesa());
            actualPedido.setCliente(newPedido.getCliente());
            actualPedido.setMesero(newPedido.getMesero());
            actualPedido.setItems(newPedido.getItems());
            actualPedido.setNotas(newPedido.getNotas());
            actualPedido.setPlataforma(newPedido.getPlataforma());
            actualPedido.setPedidoPor(newPedido.getPedidoPor());
            actualPedido.setGuardadoPor(newPedido.getGuardadoPor());
            actualPedido.setFechaCortesia(newPedido.getFechaCortesia());
            actualPedido.setEstado(newPedido.getEstado());

            Pedido pedidoActualizado = this.thePedidoRepository.save(actualPedido);
            return responseService.success(pedidoActualizado, "Pedido actualizado exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al actualizar pedido: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/estado/{estado}")
    public ResponseEntity<ApiResponse<Pedido>> cambiarEstado(@PathVariable String id, @PathVariable String estado) {
        try {
            Pedido pedido = this.thePedidoRepository.findById(id).orElse(null);
            if (pedido == null) {
                return responseService.notFound("Pedido no encontrado con ID: " + id);
            }

            pedido.setEstado(estado);
            Pedido pedidoActualizado = this.thePedidoRepository.save(pedido);
            return responseService.success(pedidoActualizado, "Estado del pedido actualizado exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al cambiar estado del pedido: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        try {
            Pedido pedido = this.thePedidoRepository.findById(id).orElse(null);
            if (pedido == null) {
                return responseService.notFound("Pedido no encontrado con ID: " + id);
            }

            // Verificar que el pedido se pueda eliminar (ej: no esté en proceso)
            if ("enProceso".equals(pedido.getEstado()) || "completado".equals(pedido.getEstado())) {
                return responseService.conflict("No se puede eliminar un pedido que está en proceso o completado");
            }

            this.thePedidoRepository.delete(pedido);
            return responseService.success(null, "Pedido eliminado exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al eliminar pedido: " + e.getMessage());
        }
    }

    @GetMapping("/total-ventas")
    public ResponseEntity<ApiResponse<TotalVentasResponse>> getTotalVentas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {

        try {
            System.out.println("Calculando total de ventas desde " + fechaInicio + " hasta " + fechaFin);

            List<Pedido> pedidos = this.thePedidoRepository.findPedidosForTotalVentas(fechaInicio, fechaFin);
            System.out.println("Pedidos encontrados: " + pedidos.size());

            // Calculate total from completed (canceled) orders
            double total = pedidos.stream()
                    .filter(pedido -> pedido != null)
                    .mapToDouble(pedido -> {
                        Double orderTotal = pedido.getTotal();
                        return orderTotal != null ? orderTotal : 0.0;
                    })
                    .sum();

            System.out.println("Total calculado: " + total);

            TotalVentasResponse response = new TotalVentasResponse(total);
            return responseService.success(response, "Total de ventas calculado exitosamente");
        } catch (Exception e) {
            System.err.println("Error calculando total de ventas: " + e.getMessage());
            // Log full exception details for debugging
            System.err.println("Detalles completos del error:");
            for (StackTraceElement element : e.getStackTrace()) {
                System.err.println("\t" + element.toString());
            }
            return responseService.internalError("Error al calcular total de ventas: " + e.getMessage());
        }
    }

    @DeleteMapping("/eliminar-todo")
    public ResponseEntity<?> eliminarTodosPedidos() {
        try {
            long cantidadPedidos = thePedidoRepository.count();
            thePedidoRepository.deleteAll();

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Todos los pedidos han sido eliminados exitosamente");
            response.put("cantidadEliminada", cantidadPedidos);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar los pedidos: " + e.getMessage());
        }
    }

    @DeleteMapping("/eliminar-por-estado/{estado}")
    public ResponseEntity<?> eliminarPedidosPorEstado(@PathVariable String estado) {
        try {
            List<Pedido> pedidos = thePedidoRepository.findByEstado(estado);
            thePedidoRepository.deleteAllByEstado(estado);

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Pedidos con estado '" + estado + "' han sido eliminados exitosamente");
            response.put("cantidadEliminada", pedidos.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar los pedidos: " + e.getMessage());
        }
    }

    // Endpoints específicos para pedidos internos y cortesía
    @GetMapping("/internos")
    public ResponseEntity<ApiResponse<List<Pedido>>> findPedidosInternos() {
        try {
            List<Pedido> pedidosInternos = this.thePedidoRepository.findByTipo("interno");
            return responseService.success(pedidosInternos, "Pedidos internos obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener pedidos internos: " + e.getMessage());
        }
    }

    @GetMapping("/cortesia")
    public ResponseEntity<ApiResponse<List<Pedido>>> findPedidosCortesia() {
        try {
            List<Pedido> pedidosCortesia = this.thePedidoRepository.findByTipo("cortesia");
            return responseService.success(pedidosCortesia, "Pedidos de cortesía obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener pedidos de cortesía: " + e.getMessage());
        }
    }

    @GetMapping("/guardado-por/{guardadoPor}")
    public ResponseEntity<ApiResponse<List<Pedido>>> findByGuardadoPor(@PathVariable String guardadoPor) {
        try {
            List<Pedido> pedidos = this.thePedidoRepository.findByGuardadoPor(guardadoPor);
            return responseService.success(pedidos, "Pedidos guardados por usuario obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener pedidos por guardadoPor: " + e.getMessage());
        }
    }

    @GetMapping("/pedido-por/{pedidoPor}")
    public ResponseEntity<ApiResponse<List<Pedido>>> findByPedidoPor(@PathVariable String pedidoPor) {
        try {
            List<Pedido> pedidos = this.thePedidoRepository.findByPedidoPor(pedidoPor);
            return responseService.success(pedidos, "Pedidos solicitados por usuario obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener pedidos por pedidoPor: " + e.getMessage());
        }
    }

    @GetMapping("/pendientes-internos")
    public ResponseEntity<ApiResponse<List<Pedido>>> findPendientesInternos() {
        try {
            LocalDateTime inicioHoy = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            // Buscar pedidos internos pendientes de hoy
            List<Pedido> pedidos = this.thePedidoRepository.findByTipoAndEstadoAndFechaGreaterThanEqual("interno", "pendiente", inicioHoy);
            return responseService.success(pedidos, "Pedidos internos pendientes obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener pedidos internos pendientes: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/completar")
    public ResponseEntity<ApiResponse<Pedido>> completarPedido(@PathVariable String id) {
        try {
            Pedido pedido = this.thePedidoRepository.findById(id).orElse(null);
            if (pedido == null) {
                return responseService.notFound("Pedido no encontrado con ID: " + id);
            }

            pedido.setEstado("completado");
            Pedido pedidoActualizado = this.thePedidoRepository.save(pedido);
            return responseService.success(pedidoActualizado, "Pedido completado exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al completar pedido: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/pagar")
    public ResponseEntity<ApiResponse<Pedido>> pagarPedido(@PathVariable String id, @RequestBody Map<String, Object> datosPago) {
        try {
            Pedido pedido = this.thePedidoRepository.findById(id).orElse(null);
            if (pedido == null) {
                return responseService.notFound("Pedido no encontrado con ID: " + id);
            }

            // Validar que el pedido se pueda pagar
            if ("pagado".equals(pedido.getEstado()) || "cancelado".equals(pedido.getEstado())) {
                return responseService.conflict("El pedido ya está pagado o cancelado");
            }

            // Extraer datos del request con manejo de errores mejorado
            String formaPago = datosPago.getOrDefault("formaPago", "efectivo").toString();

            double propina = 0.0;
            try {
                Object propinaObj = datosPago.getOrDefault("propina", "0");
                if (propinaObj instanceof Number) {
                    propina = ((Number) propinaObj).doubleValue();
                } else {
                    propina = Double.parseDouble(propinaObj.toString());
                }
            } catch (NumberFormatException e) {
                return responseService.badRequest("El valor de propina debe ser un número válido");
            }

            String pagadoPor = datosPago.getOrDefault("pagadoPor", "").toString();

            // Usar el método de utilidad del modelo
            pedido.pagar(formaPago, propina, pagadoPor);

            Pedido pedidoActualizado = this.thePedidoRepository.save(pedido);
            return responseService.success(pedidoActualizado, "Pedido pagado exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al pagar pedido: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<ApiResponse<Pedido>> cancelarPedido(@PathVariable String id, @RequestBody Map<String, Object> datosCancelacion) {
        try {
            Pedido pedido = this.thePedidoRepository.findById(id).orElse(null);
            if (pedido == null) {
                return responseService.notFound("Pedido no encontrado con ID: " + id);
            }

            // Validar que el pedido se pueda cancelar
            if ("pagado".equals(pedido.getEstado()) || "cancelado".equals(pedido.getEstado())) {
                return responseService.conflict("No se puede cancelar un pedido que ya está pagado o cancelado");
            }

            // Extraer datos del request
            String motivo = datosCancelacion.getOrDefault("motivo", "Sin motivo especificado").toString();
            String canceladoPor = datosCancelacion.getOrDefault("canceladoPor", "").toString();

            // Usar el método de utilidad del modelo
            pedido.cancelar(motivo, canceladoPor);

            Pedido pedidoActualizado = this.thePedidoRepository.save(pedido);
            return responseService.success(pedidoActualizado, "Pedido cancelado exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al cancelar pedido: " + e.getMessage());
        }
    }

    @GetMapping("/pagados")
    public ResponseEntity<ApiResponse<List<Pedido>>> findPedidosPagados() {
        try {
            List<Pedido> pedidosPagados = this.thePedidoRepository.findByEstado("pagado");
            return responseService.success(pedidosPagados, "Pedidos pagados obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener pedidos pagados: " + e.getMessage());
        }
    }

    @GetMapping("/cancelados")
    public ResponseEntity<ApiResponse<List<Pedido>>> findPedidosCancelados() {
        try {
            List<Pedido> pedidosCancelados = this.thePedidoRepository.findByEstado("cancelado");
            return responseService.success(pedidosCancelados, "Pedidos cancelados obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener pedidos cancelados: " + e.getMessage());
        }
    }
}
