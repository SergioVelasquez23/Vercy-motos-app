package com.prog3.security.Controllers;

import java.time.LocalDateTime;
import java.util.List;

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
import com.prog3.security.Services.InventarioService;
import com.prog3.security.Services.ResponseService;
import com.prog3.security.Utils.ApiResponse;
import com.prog3.security.DTOs.PagarPedidoRequest;

@CrossOrigin
@RestController
@RequestMapping("api/pedidos")
public class PedidosController {

    @Autowired
    PedidoRepository thePedidoRepository;

    @Autowired
    private ResponseService responseService;

    @Autowired
    private InventarioService inventarioService;

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
            // Asegurar que la fecha esté establecida
            if (newPedido.getFecha() == null) {
                newPedido.setFecha(LocalDateTime.now());
            }

            // Asegurar que el estado esté establecido
            if (newPedido.getEstado() == null || newPedido.getEstado().isEmpty()) {
                newPedido.setEstado("pendiente");
            }

            // Limpiar ID antes de guardar para que MongoDB genere uno nuevo
            newPedido.set_id(null);

            Pedido pedidoCreado = this.thePedidoRepository.save(newPedido);

            // Verificar que el pedido se guardó correctamente con un ID válido
            if (pedidoCreado.get_id() == null || pedidoCreado.get_id().isEmpty()) {
                // Intentar obtener el pedido recién creado por otros campos únicos
                List<Pedido> pedidosRecientes = this.thePedidoRepository.findByMesaAndFechaBetween(
                        newPedido.getMesa(),
                        LocalDateTime.now().minusMinutes(1),
                        LocalDateTime.now().plusMinutes(1)
                );

                if (!pedidosRecientes.isEmpty()) {
                    pedidoCreado = pedidosRecientes.get(pedidosRecientes.size() - 1); // El más reciente
                } else {
                    throw new RuntimeException("Error: El pedido se guardó pero no se pudo recuperar el ID");
                }
            }

            // Procesar el pedido para descontar del inventario
            try {
                System.out.println("Procesando pedido para inventario: " + pedidoCreado.get_id());
                inventarioService.procesarPedidoParaInventario(pedidoCreado);
                System.out.println("Inventario actualizado correctamente");
            } catch (Exception e) {
                System.err.println("Error al procesar inventario: " + e.getMessage());
                // No interrumpimos la creación del pedido si hay error en inventario
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

            // Procesar el pedido actualizado para ajustar el inventario
            try {
                System.out.println("Procesando pedido actualizado para inventario: " + pedidoActualizado.get_id());
                inventarioService.procesarPedidoParaInventario(pedidoActualizado);
                System.out.println("Inventario actualizado correctamente");
            } catch (Exception e) {
                System.err.println("Error al procesar inventario: " + e.getMessage());
                // No interrumpimos la actualización del pedido si hay error en inventario
            }

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
                    .mapToDouble(Pedido::getTotal)
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

    @PutMapping("/{id}/pagar")
    public ResponseEntity<ApiResponse<Pedido>> pagarPedido(@PathVariable String id, @RequestBody PagarPedidoRequest pagarRequest) {
        try {
            Pedido pedido = this.thePedidoRepository.findById(id).orElse(null);
            if (pedido == null) {
                return responseService.notFound("Pedido no encontrado con ID: " + id);
            }

            // Validar que el tipo de pago esté especificado
            if (pagarRequest.getTipoPago() == null || pagarRequest.getTipoPago().isEmpty()) {
                return responseService.badRequest("El tipo de pago es requerido");
            }

            // Actualizar información según el tipo de pago
            String nuevoEstado = pagarRequest.getTipoPago(); // "pagado", "cortesia", "consumo_interno", "cancelado"
            pedido.setEstado(nuevoEstado);

            // Actualizar el campo pagadoPor (que ya existe en el modelo)
            pedido.setPagadoPor(pagarRequest.getProcesadoPor());

            // Agregar información adicional en las notas
            String notasAdicionales = pagarRequest.getNotas() != null ? pagarRequest.getNotas() : "";

            // Solo para pagos normales
            if (pagarRequest.esPagado()) {
                pedido.setFormaPago(pagarRequest.getFormaPago());
                pedido.setPropina(pagarRequest.getPropina());

                // Calcular total pagado incluyendo propina
                double totalConPropina = pedido.getTotal() + pagarRequest.getPropina();
                pedido.setTotalPagado(totalConPropina);
                pedido.setIncluyePropina(pagarRequest.getPropina() > 0);

                // Establecer fecha de pago solo para pagos reales
                pedido.setFechaPago(LocalDateTime.now());

                pedido.setNotas(notasAdicionales);

            } else if (pagarRequest.esCortesia()) {
                // Para cortesías
                pedido.setFechaCortesia(LocalDateTime.now());
                pedido.setPropina(0);
                pedido.setTotalPagado(0);

                // Agregar motivo de cortesía a las notas
                String motivoCortesia = pagarRequest.getMotivoCortesia() != null ? pagarRequest.getMotivoCortesia() : "";
                pedido.setNotas("CORTESÍA - " + motivoCortesia + " - " + notasAdicionales);

            } else if (pagarRequest.esConsumoInterno()) {
                // Para consumo interno
                pedido.setPropina(0);
                pedido.setTotalPagado(0);

                // Agregar tipo de consumo interno a las notas
                String tipoConsumo = pagarRequest.getTipoConsumoInterno() != null ? pagarRequest.getTipoConsumoInterno() : "";
                pedido.setNotas("CONSUMO INTERNO - " + tipoConsumo + " - " + notasAdicionales);

            } else if (pagarRequest.esCancelado()) {
                // Para cancelados
                pedido.setPropina(0);
                pedido.setTotalPagado(0);
                pedido.setFechaCancelacion(LocalDateTime.now());
                pedido.setCanceladoPor(pagarRequest.getProcesadoPor());
                pedido.setMotivoCancelacion(notasAdicionales);
            }

            Pedido pedidoProcesado = this.thePedidoRepository.save(pedido);

            String mensaje = switch (pagarRequest.getTipoPago()) {
                case "pagado" ->
                    "Pedido pagado exitosamente";
                case "cortesia" ->
                    "Pedido marcado como cortesía exitosamente";
                case "consumo_interno" ->
                    "Pedido marcado como consumo interno exitosamente";
                case "cancelado" ->
                    "Pedido cancelado exitosamente";
                default ->
                    "Pedido procesado exitosamente";
            };

            return responseService.success(pedidoProcesado, mensaje);
        } catch (Exception e) {
            return responseService.internalError("Error al procesar pedido: " + e.getMessage());
        }
    }
}
