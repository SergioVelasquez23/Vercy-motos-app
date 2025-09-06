package com.prog3.security.Controllers;

import java.time.LocalDateTime;
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
import com.prog3.security.Models.CuadreCaja;
import com.prog3.security.Models.ItemPedido;
import com.prog3.security.Repositories.PedidoRepository;
import com.prog3.security.Repositories.CuadreCajaRepository;
import com.prog3.security.Services.InventarioService;
import com.prog3.security.Services.ResponseService;
import com.prog3.security.Services.CuadreCajaService;
import com.prog3.security.Services.WebSocketNotificationService;
// import com.prog3.security.Utils.ApiResponse; // Comentado para evitar conflicto con anotación Swagger
import com.prog3.security.DTOs.PagarPedidoRequest;
import com.prog3.security.DTOs.CancelarProductoRequest;
import com.prog3.security.Exception.BusinessException;
import com.prog3.security.Exception.ResourceNotFoundException;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;

@CrossOrigin
@RestController
@RequestMapping("api/pedidos")
@Tag(name = "Pedidos", description = "Gestión completa de pedidos del restaurante")
public class PedidosController {

    @Autowired
    PedidoRepository thePedidoRepository;

    @Autowired
    CuadreCajaRepository cuadreCajaRepository;

    @Autowired
    private ResponseService responseService;

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private CuadreCajaService cuadreCajaService;

    @Autowired
    private WebSocketNotificationService webSocketService;

    @Operation(
        summary = "Obtener todos los pedidos",
        description = "Retorna la lista completa de pedidos en el sistema"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de pedidos obtenida exitosamente"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<List<Pedido>>> find() {
        try {
            List<Pedido> pedidos = this.thePedidoRepository.findAll();
            return responseService.success(pedidos, "Pedidos obtenidos exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener pedidos: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<Pedido>> findById(@PathVariable String id) {
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
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<List<Pedido>>> findByTipo(@PathVariable String tipo) {
        try {
            List<Pedido> pedidos = this.thePedidoRepository.findByTipo(tipo);
            return responseService.success(pedidos, "Pedidos filtrados por tipo obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al filtrar pedidos por tipo: " + e.getMessage());
        }
    }

    @GetMapping("/mesa/{mesa}")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<List<Pedido>>> findByMesa(@PathVariable String mesa) {
        try {
            List<Pedido> pedidos = this.thePedidoRepository.findByMesa(mesa);
            return responseService.success(pedidos, "Pedidos de la mesa obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener pedidos de la mesa: " + e.getMessage());
        }
    }

    @GetMapping("/cliente/{cliente}")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<List<Pedido>>> findByCliente(@PathVariable String cliente) {
        try {
            List<Pedido> pedidos = this.thePedidoRepository.findByCliente(cliente);
            return responseService.success(pedidos, "Pedidos del cliente obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener pedidos del cliente: " + e.getMessage());
        }
    }

    @GetMapping("/mesero/{mesero}")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<List<Pedido>>> findByMesero(@PathVariable String mesero) {
        try {
            List<Pedido> pedidos = this.thePedidoRepository.findByMesero(mesero);
            return responseService.success(pedidos, "Pedidos del mesero obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener pedidos del mesero: " + e.getMessage());
        }
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<List<Pedido>>> findByEstado(@PathVariable String estado) {
        try {
            List<Pedido> pedidos = this.thePedidoRepository.findByEstado(estado);
            return responseService.success(pedidos, "Pedidos filtrados por estado obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al filtrar pedidos por estado: " + e.getMessage());
        }
    }

    @GetMapping("/fechas")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<List<Pedido>>> findByFechaRange(
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
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<List<Pedido>>> findHoy() {
        try {
            LocalDateTime inicioHoy = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            List<Pedido> pedidos = this.thePedidoRepository.findByFechaGreaterThanEqual(inicioHoy);
            return responseService.success(pedidos, "Pedidos de hoy obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener pedidos de hoy: " + e.getMessage());
        }
    }

    @GetMapping("/plataforma/{plataforma}")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<List<Pedido>>> findByPlataforma(@PathVariable String plataforma) {
        try {
            List<Pedido> pedidos = this.thePedidoRepository.findByPlataforma(plataforma);
            return responseService.success(pedidos, "Pedidos filtrados por plataforma obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al filtrar pedidos por plataforma: " + e.getMessage());
        }
    }

    @GetMapping("/cuadre/{cuadreId}")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<List<Pedido>>> findByCuadreId(@PathVariable String cuadreId) {
        try {
            List<Pedido> pedidos = this.thePedidoRepository.findByCuadreCajaId(cuadreId);
            return responseService.success(pedidos, "Pedidos del cuadre de caja obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener pedidos del cuadre: " + e.getMessage());
        }
    }

    @GetMapping("/cuadre/{cuadreId}/pagados")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<List<Pedido>>> findByCuadreIdPagados(@PathVariable String cuadreId) {
        try {
            List<Pedido> pedidos = this.thePedidoRepository.findByCuadreCajaIdAndEstado(cuadreId, "pagado");
            return responseService.success(pedidos, "Pedidos pagados del cuadre de caja obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener pedidos pagados del cuadre: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<Pedido>> create(@RequestBody Pedido newPedido) {
        try {
            // VALIDACIÓN: Verificar que hay una caja abierta antes de crear el pedido
            List<CuadreCaja> cajasAbiertas = cuadreCajaRepository.findByCerradaFalse();
            if (cajasAbiertas.isEmpty()) {
                System.out.println("❌ Intento de crear pedido sin caja abierta");
                return responseService.badRequest(
                    "No se puede crear un pedido sin una caja abierta. Debe abrir una caja antes de registrar pedidos.");
            }
            
            // Obtener la caja activa (la primera caja abierta)
            CuadreCaja cajaActiva = cajasAbiertas.get(0);
            System.out.println("✅ Caja activa encontrada: " + cajaActiva.get_id() + " - " + cajaActiva.getNombre());
            
            // Asignar el cuadreId al pedido
            newPedido.setCuadreCajaId(cajaActiva.get_id());
            System.out.println("🔗 Pedido vinculado a cuadre: " + cajaActiva.get_id());

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
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<Pedido>> update(@PathVariable String id, @RequestBody Pedido newPedido) {
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
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<Pedido>> cambiarEstado(@PathVariable String id, @PathVariable String estado) {
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
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<Void>> delete(@PathVariable String id) {
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
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<TotalVentasResponse>> getTotalVentas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {

        try {
            System.out.println("Calculando total de ventas desde " + fechaInicio + " hasta " + fechaFin);

            // Buscar la caja abierta actualmente (no cerrada)
            List<CuadreCaja> cajasAbiertas = cuadreCajaRepository.findByCerradaFalse();
            CuadreCaja cajaActiva = cajasAbiertas.isEmpty() ? null : cajasAbiertas.get(0);

            LocalDateTime fechaInicioVentas = fechaInicio;
            if (cajaActiva != null) {
                // Si hay una caja abierta, usar su fecha de apertura como inicio
                fechaInicioVentas = cajaActiva.getFechaApertura();
                System.out.println("Usando fecha de apertura de caja activa: " + fechaInicioVentas);
            } else {
                System.out.println("No se encontró caja activa, usando fecha proporcionada: " + fechaInicio);
            }

            List<Pedido> pedidos = this.thePedidoRepository.findPedidosForTotalVentas(fechaInicioVentas, fechaFin);
            System.out.println("Pedidos encontrados: " + pedidos.size());

            // Filtrar solo pedidos pagados (estado = "pagado")
            List<Pedido> pedidosPagados = pedidos.stream()
                    .filter(pedido -> pedido != null && "pagado".equals(pedido.getEstado()))
                    .toList();

            System.out.println("Pedidos pagados: " + pedidosPagados.size());

            // Calcular totales por forma de pago
            double totalEfectivo = pedidosPagados.stream()
                    .filter(p -> "efectivo".equalsIgnoreCase(p.getFormaPago()))
                    .mapToDouble(p -> p.getTotalPagado() > 0 ? p.getTotalPagado() : p.getTotal())
                    .sum();

            double totalTransferencia = pedidosPagados.stream()
                    .filter(p -> "transferencia".equalsIgnoreCase(p.getFormaPago()))
                    .mapToDouble(p -> p.getTotalPagado() > 0 ? p.getTotalPagado() : p.getTotal())
                    .sum();

            double totalTarjeta = pedidosPagados.stream()
                    .filter(p -> "tarjeta".equalsIgnoreCase(p.getFormaPago()))
                    .mapToDouble(p -> p.getTotalPagado() > 0 ? p.getTotalPagado() : p.getTotal())
                    .sum();

            double totalOtros = pedidosPagados.stream()
                    .filter(p -> p.getFormaPago() == null
                    || (!p.getFormaPago().equalsIgnoreCase("efectivo")
                    && !p.getFormaPago().equalsIgnoreCase("transferencia")
                    && !p.getFormaPago().equalsIgnoreCase("tarjeta")))
                    .mapToDouble(p -> p.getTotalPagado() > 0 ? p.getTotalPagado() : p.getTotal())
                    .sum();

            double totalGeneral = totalEfectivo + totalTransferencia + totalTarjeta + totalOtros;

            System.out.println("=== RESUMEN DE VENTAS POR FORMA DE PAGO ===");
            System.out.println("Total Efectivo: " + totalEfectivo);
            System.out.println("Total Transferencia: " + totalTransferencia);
            System.out.println("Total Tarjeta: " + totalTarjeta);
            System.out.println("Total Otros: " + totalOtros);
            System.out.println("Total General: " + totalGeneral);

            TotalVentasResponse response = new TotalVentasResponse(totalGeneral, totalEfectivo, totalTransferencia, totalTarjeta, totalOtros);
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

    @Operation(
        summary = "Procesar pago de pedido",
        description = """
            Procesa el pago de un pedido con diferentes tipos:
            - **pagado**: Pago normal con propina opcional
            - **cortesia**: Sin costo (cumpleaños, promociones)
            - **consumo_interno**: Para empleados/gerencia  
            - **cancelado**: Cancelación del pedido
            
            **Validaciones importantes:**
            - Debe haber una caja abierta
            - El pedido no debe estar ya procesado
            - Los campos requeridos según el tipo de pago
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Pedido procesado exitosamente",
            content = @Content(schema = @Schema(implementation = Pedido.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Datos inválidos o caja no abierta",
            content = @Content(examples = @ExampleObject(
                name = "Caja no abierta",
                value = "{\"success\": false, \"message\": \"No se puede procesar el pago sin una caja abierta\"}"
            ))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Pedido no encontrado"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409", 
            description = "El pedido ya está procesado"
        )
    })
    @PutMapping("/{id}/pagar")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<Pedido>> pagarPedido(
            @Parameter(description = "ID del pedido a procesar", required = true) 
            @PathVariable String id, 
            @Parameter(description = "Datos del pago", required = true)
            @RequestBody @Valid PagarPedidoRequest pagarRequest) {
        try {
            System.out.println("[PAGAR_PEDIDO] Iniciando pago para pedido ID: " + id);
            
            // Validar que el ID no sea nulo o vacío
            if (id == null || id.trim().isEmpty()) {
                throw new IllegalArgumentException("El ID del pedido es requerido");
            }
            
            Pedido pedido = this.thePedidoRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.pedido(id));
            
            // Validar que el pedido no esté ya pagado (evitar dobles pagos)
            if ("pagado".equals(pedido.getEstado()) || "cortesia".equals(pedido.getEstado()) || 
                "consumo_interno".equals(pedido.getEstado()) || "cancelado".equals(pedido.getEstado())) {
                throw BusinessException.pedidoYaPagado(id);
            }
            
            // VALIDACIÓN: Verificar que hay una caja abierta antes de procesar el pago
            List<CuadreCaja> cajasAbiertas = cuadreCajaRepository.findByCerradaFalse();
            if (cajasAbiertas.isEmpty()) {
                System.out.println("❌ Intento de pagar pedido sin caja abierta");
                throw BusinessException.cajaNoAbierta();
            }
            
            // Asignar cuadre de caja si el pedido no lo tiene
            if (pedido.getCuadreCajaId() == null || pedido.getCuadreCajaId().isEmpty()) {
                CuadreCaja cajaActiva = cajasAbiertas.get(0);
                pedido.setCuadreCajaId(cajaActiva.get_id());
                System.out.println("🔗 Asignando pedido a cuadre activo: " + cajaActiva.get_id());
            }

            // Validación personalizada adicional
            if (!pagarRequest.isValid()) {
                String error = pagarRequest.getValidationError();
                System.out.println("[PAGAR_PEDIDO] Validación personalizada fallida: " + error);
                throw new IllegalArgumentException(error);
            }

            String notasAdicionales = pagarRequest.getNotas() != null ? pagarRequest.getNotas() : "";

            if (pagarRequest.esPagado()) {
                System.out.println("[PAGAR_PEDIDO] Procesando pago real. FormaPago: " + pagarRequest.getFormaPago() + ", Propina: " + pagarRequest.getPropina() + ", ProcesadoPor: " + pagarRequest.getProcesadoPor());
                pedido.pagar(pagarRequest.getFormaPago(), pagarRequest.getPropina(), pagarRequest.getProcesadoPor());
                pedido.setIncluyePropina(pagarRequest.getPropina() > 0);
                pedido.setNotas(notasAdicionales);
            } else if (pagarRequest.esCortesia()) {
                System.out.println("[PAGAR_PEDIDO] Procesando cortesía");
                pedido.setEstado("cortesia");
                pedido.setFechaCortesia(LocalDateTime.now());
                pedido.setPropina(0);
                pedido.setTotalPagado(0);
                String motivoCortesia = pagarRequest.getMotivoCortesia() != null ? pagarRequest.getMotivoCortesia() : "";
                pedido.setNotas("CORTESÍA - " + motivoCortesia + " - " + notasAdicionales);
            } else if (pagarRequest.esConsumoInterno()) {
                System.out.println("[PAGAR_PEDIDO] Procesando consumo interno");
                pedido.setEstado("consumo_interno");
                pedido.setPropina(0);
                pedido.setTotalPagado(0);
                String tipoConsumo = pagarRequest.getTipoConsumoInterno() != null ? pagarRequest.getTipoConsumoInterno() : "";
                pedido.setNotas("CONSUMO INTERNO - " + tipoConsumo + " - " + notasAdicionales);
            } else if (pagarRequest.esCancelado()) {
                System.out.println("[PAGAR_PEDIDO] Procesando cancelación");
                pedido.setEstado("cancelado");
                pedido.setPropina(0);
                pedido.setTotalPagado(0);
                pedido.setFechaCancelacion(LocalDateTime.now());
                pedido.setCanceladoPor(pagarRequest.getProcesadoPor());
                pedido.setMotivoCancelacion(notasAdicionales);
            }

            System.out.println("[PAGAR_PEDIDO] Estado final del pedido: " + pedido.getEstado());
            System.out.println("[PAGAR_PEDIDO] Pedido antes de guardar: " + pedido);
            Pedido pedidoProcesado = this.thePedidoRepository.save(pedido);
            System.out.println("[PAGAR_PEDIDO] Pedido guardado correctamente. ID: " + pedidoProcesado.get_id());

            // Asignar al cuadre de caja activo siempre (no solo para pagos)
            if (pedidoProcesado != null) {
                boolean asignado = cuadreCajaService.asignarPedidoACuadreActivo(pedidoProcesado.get_id());
                if (!asignado) {
                    System.out.println("⚠️ Advertencia: Pedido procesado pero no se pudo asignar a ningún cuadre activo");
                } else {
                    System.out.println("[PAGAR_PEDIDO] Pedido asignado a cuadre activo correctamente");
                }
                
                // Notificar vía WebSocket que se pagó un pedido (para actualizar dashboard)
                try {
                    webSocketService.notificarPedidoPagado(
                        pedidoProcesado.get_id(),
                        pedidoProcesado.getMesa(),
                        pedidoProcesado.getTotalPagado() > 0 ? pedidoProcesado.getTotalPagado() : pedidoProcesado.getTotal(),
                        pedidoProcesado.getFormaPago()
                    );
                } catch (Exception wsError) {
                    System.err.println("⚠️ Error enviando notificación WebSocket: " + wsError.getMessage());
                }
            }

            String mensaje;
            switch (pagarRequest.getTipoPago()) {
                case "pagado":
                    mensaje = "Pedido pagado exitosamente";
                    break;
                case "cortesia":
                    mensaje = "Pedido marcado como cortesía exitosamente";
                    break;
                case "consumo_interno":
                    mensaje = "Pedido marcado como consumo interno exitosamente";
                    break;
                case "cancelado":
                    mensaje = "Pedido cancelado exitosamente";
                    break;
                default:
                    mensaje = "Pedido procesado exitosamente";
                    break;
            }

            return responseService.success(pedidoProcesado, mensaje);
        } catch (Exception e) {
            System.err.println("[PAGAR_PEDIDO] Error al procesar pedido: " + e.getMessage());
            e.printStackTrace();
            return responseService.internalError("Error al procesar pedido: " + e.getMessage());
        }
    }

    /**
     * Obtener todos los pedidos activos de una mesa especial
     */
    @GetMapping("/mesa/{mesa}/activos")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<List<Pedido>>> getPedidosActivosPorMesa(@PathVariable String mesa) {
        try {
            List<Pedido> pedidos = this.thePedidoRepository.findPedidosActivosByMesa(mesa);
            return responseService.success(pedidos, "Pedidos activos obtenidos exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener pedidos activos: " + e.getMessage());
        }
    }

    /**
     * Crear un pedido con nombre específico para mesa especial
     */
    @PostMapping("/mesa-especial")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<Pedido>> createPedidoMesaEspecial(@RequestBody Pedido pedido) {
        try {
            // Validar que se proporcione el nombre del pedido
            if (pedido.getNombrePedido() == null || pedido.getNombrePedido().trim().isEmpty()) {
                return responseService.badRequest("El nombre del pedido es obligatorio para mesas especiales");
            }

            // Validar que la mesa existe y es especial (esto se podría hacer con el repositorio de mesas)
            // Verificar si ya existe un pedido activo con ese nombre en esa mesa
            List<Pedido> pedidosExistentes = this.thePedidoRepository.findPedidoActivoByMesaAndNombre(
                    pedido.getMesa(),
                    pedido.getNombrePedido()
            );

            if (!pedidosExistentes.isEmpty()) {
                return responseService.conflict("Ya existe un pedido activo con el nombre '"
                        + pedido.getNombrePedido() + "' en la mesa " + pedido.getMesa());
            }

            // Establecer valores por defecto
            pedido.set_id(null); // MongoDB generará el ID
            pedido.setFecha(LocalDateTime.now());
            pedido.setEstado("activo");

            if (pedido.getItems() == null) {
                pedido.setItems(List.of());
            }

            Pedido nuevoPedido = this.thePedidoRepository.save(pedido);
            return responseService.created(nuevoPedido, "Pedido creado exitosamente para mesa especial");
        } catch (Exception e) {
            return responseService.internalError("Error al crear pedido para mesa especial: " + e.getMessage());
        }
    }

    /**
     * Obtener un pedido específico por mesa y nombre
     */
    @GetMapping("/mesa/{mesa}/pedido/{nombrePedido}")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<Pedido>> getPedidoPorMesaYNombre(
            @PathVariable String mesa,
            @PathVariable String nombrePedido) {
        try {
            List<Pedido> pedidos = this.thePedidoRepository.findPedidoActivoByMesaAndNombre(mesa, nombrePedido);

            if (pedidos.isEmpty()) {
                return responseService.notFound("No se encontró un pedido activo con el nombre '"
                        + nombrePedido + "' en la mesa " + mesa);
            }

            // Devolver el primer pedido encontrado (debería ser único)
            return responseService.success(pedidos.get(0), "Pedido encontrado");
        } catch (Exception e) {
            return responseService.internalError("Error al buscar pedido: " + e.getMessage());
        }
    }

    /**
     * Eliminar todos los pedidos activos de una mesa específica
     */
    @DeleteMapping("/mesa/{mesa}/vaciar")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<String>> vaciarPedidosMesa(@PathVariable String mesa) {
        try {
            // Obtener todos los pedidos activos de la mesa
            List<Pedido> pedidosActivos = this.thePedidoRepository.findPedidosActivosByMesa(mesa);

            if (pedidosActivos.isEmpty()) {
                return responseService.success("", "No hay pedidos activos en la mesa " + mesa);
            }

            int cantidadEliminada = pedidosActivos.size();

            // Eliminar todos los pedidos encontrados
            this.thePedidoRepository.deleteAll(pedidosActivos);

            String mensaje = "Se eliminaron " + cantidadEliminada + " pedidos activos de la mesa " + mesa;
            return responseService.success(mensaje, mensaje);
        } catch (Exception e) {
            return responseService.internalError("Error al vaciar pedidos de la mesa: " + e.getMessage());
        }
    }

    /**
     * Eliminar un pedido específico por mesa y nombre
     */
    @DeleteMapping("/mesa/{mesa}/pedido/{nombrePedido}")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<String>> eliminarPedidoPorNombre(
            @PathVariable String mesa,
            @PathVariable String nombrePedido) {
        try {
            List<Pedido> pedidos = this.thePedidoRepository.findPedidoActivoByMesaAndNombre(mesa, nombrePedido);

            if (pedidos.isEmpty()) {
                return responseService.notFound("No se encontró un pedido activo con el nombre '"
                        + nombrePedido + "' en la mesa " + mesa);
            }

            // Eliminar el pedido encontrado
            this.thePedidoRepository.delete(pedidos.get(0));

            String mensaje = "Pedido '" + nombrePedido + "' eliminado exitosamente de la mesa " + mesa;
            return responseService.success(mensaje, mensaje);
        } catch (Exception e) {
            return responseService.internalError("Error al eliminar pedido específico: " + e.getMessage());
        }
    }

    /**
     * Obtener los ingredientes que se pueden devolver al cancelar un producto
     */
    @GetMapping("/{pedidoId}/producto/{productoId}/ingredientes-devolucion")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<List<CancelarProductoRequest.IngredienteADevolver>>>
            getIngredientesParaDevolucion(@PathVariable String pedidoId,
                    @PathVariable String productoId,
                    @RequestParam int cantidad) {
        try {
            // Buscar el pedido
            Pedido pedido = this.thePedidoRepository.findById(pedidoId).orElse(null);
            if (pedido == null) {
                return responseService.notFound("Pedido no encontrado con ID: " + pedidoId);
            }

            // Obtener los ingredientes que fueron descontados
            List<CancelarProductoRequest.IngredienteADevolver> ingredientesDescontados
                    = inventarioService.getIngredientesDescontadosParaProducto(pedidoId, productoId, cantidad);

            return responseService.success(ingredientesDescontados, "Ingredientes para devolución obtenidos exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener ingredientes para devolución: " + e.getMessage());
        }
    }

    /**
     * Cancelar un producto del pedido con devolución selectiva de ingredientes
     */
    @PostMapping("/cancelar-producto")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<Pedido>> cancelarProducto(@RequestBody CancelarProductoRequest request) {
        try {
            // Buscar el pedido
            Pedido pedido = this.thePedidoRepository.findById(request.getPedidoId()).orElse(null);
            if (pedido == null) {
                return responseService.notFound("Pedido no encontrado con ID: " + request.getPedidoId());
            }

            // Buscar el item en el pedido
            boolean productoEncontrado = false;
            if (pedido.getItems() != null) {
                for (ItemPedido item : pedido.getItems()) {
                    if (item.getProductoId().equals(request.getProductoId())) {
                        productoEncontrado = true;

                        // Verificar si hay suficiente cantidad para cancelar
                        if (item.getCantidad() < request.getCantidadACancelar()) {
                            return responseService.badRequest("No se puede cancelar más cantidad de la que existe en el pedido");
                        }

                        // Reducir la cantidad o eliminar el item
                        if (item.getCantidad() == request.getCantidadACancelar()) {
                            // Eliminar completamente el item
                            pedido.getItems().remove(item);
                        } else {
                            // Reducir la cantidad
                            item.setCantidad(item.getCantidad() - request.getCantidadACancelar());
                            // El total se recalcula automáticamente en el setter de cantidad
                        }
                        break;
                    }
                }
            }

            if (!productoEncontrado) {
                return responseService.notFound("Producto no encontrado en el pedido");
            }

            // Devolver ingredientes seleccionados al inventario
            if (request.getIngredientesADevolver() != null && !request.getIngredientesADevolver().isEmpty()) {
                inventarioService.devolverIngredientesAlInventario(
                        request.getPedidoId(),
                        request.getProductoId(),
                        request.getIngredientesADevolver(),
                        request.getCanceladoPor()
                );
            }

            // Recalcular total del pedido
            double nuevoTotal = 0;
            if (pedido.getItems() != null) {
                for (ItemPedido item : pedido.getItems()) {
                    nuevoTotal += item.getTotal();
                }
            }
            pedido.setTotal(nuevoTotal);

            // Agregar nota de cancelación
            String notaCancelacion = "CANCELACIÓN: " + request.getMotivoCancelacion();
            if (request.getNotas() != null && !request.getNotas().isEmpty()) {
                notaCancelacion += " - " + request.getNotas();
            }

            String notasActuales = pedido.getNotas() != null ? pedido.getNotas() : "";
            pedido.setNotas(notasActuales.isEmpty() ? notaCancelacion : notasActuales + "\n" + notaCancelacion);

            // Guardar el pedido actualizado
            Pedido pedidoActualizado = this.thePedidoRepository.save(pedido);

            return responseService.success(pedidoActualizado, "Producto cancelado exitosamente con devolución selectiva de ingredientes");
        } catch (Exception e) {
            return responseService.internalError("Error al cancelar producto: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/test-inventario")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<String>> testProcesarInventario(@PathVariable String id) {
        try {
            Pedido pedido = this.thePedidoRepository.findById(id).orElse(null);
            if (pedido == null) {
                return responseService.notFound("Pedido no encontrado con ID: " + id);
            }

            System.out.println("🧪 ===== TEST: FORZANDO PROCESAMIENTO DE INVENTARIO =====");
            inventarioService.procesarPedidoParaInventario(pedido);
            System.out.println("🧪 ===== FIN TEST =====");

            return responseService.success("OK", "Procesamiento de inventario ejecutado. Ver logs del servidor.");
        } catch (Exception e) {
            return responseService.internalError("Error al procesar inventario: " + e.getMessage());
        }
    }

    /**
     * Eliminar TODOS los pedidos - USAR CON PRECAUCIÓN Endpoint para limpiar
     * completamente la base de datos de pedidos
     */
    @DeleteMapping("/admin/eliminar-todos")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<String>> eliminarTodosLosPedidos() {
        try {
            List<Pedido> todosLosPedidos = thePedidoRepository.findAll();
            int cantidadEliminada = todosLosPedidos.size();

            System.out.println("⚠️ ELIMINANDO TODOS LOS PEDIDOS: " + cantidadEliminada + " pedidos");

            thePedidoRepository.deleteAll();

            System.out.println("✅ Se eliminaron " + cantidadEliminada + " pedidos exitosamente");

            return responseService.success(
                    "TODOS_ELIMINADOS",
                    "Se eliminaron " + cantidadEliminada + " pedidos exitosamente"
            );
        } catch (Exception e) {
            System.err.println("❌ Error al eliminar todos los pedidos: " + e.getMessage());
            return responseService.internalError("Error al eliminar todos los pedidos: " + e.getMessage());
        }
    }

    /**
     * Eliminar pedidos por estado específico
     */
    @DeleteMapping("/admin/eliminar-por-estado/{estado}")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<String>> eliminarPedidosPorEstado(@PathVariable String estado) {
        try {
            List<Pedido> pedidosPorEstado = thePedidoRepository.findByEstado(estado);
            int cantidadEliminada = pedidosPorEstado.size();

            System.out.println("⚠️ ELIMINANDO PEDIDOS CON ESTADO '" + estado + "': " + cantidadEliminada + " pedidos");

            thePedidoRepository.deleteAll(pedidosPorEstado);

            System.out.println("✅ Se eliminaron " + cantidadEliminada + " pedidos con estado '" + estado + "'");

            return responseService.success(
                    "PEDIDOS_ELIMINADOS",
                    "Se eliminaron " + cantidadEliminada + " pedidos con estado '" + estado + "'"
            );
        } catch (Exception e) {
            System.err.println("❌ Error al eliminar pedidos por estado: " + e.getMessage());
            return responseService.internalError("Error al eliminar pedidos por estado: " + e.getMessage());
        }
    }

    /**
     * Eliminar pedidos en un rango de fechas
     */
    @DeleteMapping("/admin/eliminar-por-fechas")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<String>> eliminarPedidosPorFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        try {
            List<Pedido> pedidosEnRango = thePedidoRepository.findByFechaBetween(fechaInicio, fechaFin);
            int cantidadEliminada = pedidosEnRango.size();

            System.out.println("⚠️ ELIMINANDO PEDIDOS ENTRE " + fechaInicio + " Y " + fechaFin + ": " + cantidadEliminada + " pedidos");

            thePedidoRepository.deleteAll(pedidosEnRango);

            System.out.println("✅ Se eliminaron " + cantidadEliminada + " pedidos en el rango de fechas");

            return responseService.success(
                    "PEDIDOS_ELIMINADOS",
                    "Se eliminaron " + cantidadEliminada + " pedidos entre " + fechaInicio + " y " + fechaFin
            );
        } catch (Exception e) {
            System.err.println("❌ Error al eliminar pedidos por fechas: " + e.getMessage());
            return responseService.internalError("Error al eliminar pedidos por fechas: " + e.getMessage());
        }
    }

    /**
     * Contar todos los pedidos antes de eliminar (para confirmación)
     */
    @GetMapping("/admin/contar-todos")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<Integer>> contarTodosLosPedidos() {
        try {
            int totalPedidos = (int) thePedidoRepository.count();

            System.out.println("📊 Total de pedidos en la base de datos: " + totalPedidos);

            return responseService.success(totalPedidos, "Total de pedidos: " + totalPedidos);
        } catch (Exception e) {
            return responseService.internalError("Error al contar pedidos: " + e.getMessage());
        }
    }

    /**
     * Contar pedidos por estado
     */
    @GetMapping("/admin/contar-por-estado/{estado}")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<Integer>> contarPedidosPorEstado(@PathVariable String estado) {
        try {
            List<Pedido> pedidosPorEstado = thePedidoRepository.findByEstado(estado);
            int cantidad = pedidosPorEstado.size();

            System.out.println("📊 Pedidos con estado '" + estado + "': " + cantidad);

            return responseService.success(cantidad, "Pedidos con estado '" + estado + "': " + cantidad);
        } catch (Exception e) {
            return responseService.internalError("Error al contar pedidos por estado: " + e.getMessage());
        }
    }

    /**
     * Mover un pedido de una mesa a otra
     */
    @PutMapping("/{pedidoId}/mover-mesa")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<Pedido>> moverPedidoAMesa(
            @PathVariable String pedidoId,
            @RequestBody Map<String, String> request) {
        try {
            String nuevaMesa = request.get("nuevaMesa");
            String nombrePedido = request.get("nombrePedido"); // Para mesas especiales
            
            if (nuevaMesa == null || nuevaMesa.trim().isEmpty()) {
                return responseService.badRequest("El nombre de la mesa destino es requerido");
            }
            
            // Buscar el pedido
            Pedido pedido = this.thePedidoRepository.findById(pedidoId).orElse(null);
            if (pedido == null) {
                return responseService.notFound("Pedido no encontrado con ID: " + pedidoId);
            }
            
            // Verificar que el pedido esté en estado activo
            if (!"activo".equals(pedido.getEstado()) && !"pendiente".equals(pedido.getEstado())) {
                return responseService.badRequest("Solo se pueden mover pedidos en estado activo o pendiente");
            }
            
            String mesaAnterior = pedido.getMesa();
            
            // Actualizar la mesa del pedido
            pedido.setMesa(nuevaMesa);
            
            // Si es mesa especial y se proporciona nombre, actualizarlo
            if (nombrePedido != null && !nombrePedido.trim().isEmpty()) {
                pedido.setNombrePedido(nombrePedido);
            }
            
            // Guardar el pedido actualizado
            Pedido pedidoActualizado = this.thePedidoRepository.save(pedido);
            
            System.out.println("🚚 Pedido movido exitosamente:");
            System.out.println("  - Pedido ID: " + pedidoId);
            System.out.println("  - Mesa anterior: " + mesaAnterior);
            System.out.println("  - Mesa nueva: " + nuevaMesa);
            if (nombrePedido != null) {
                System.out.println("  - Nombre pedido: " + nombrePedido);
            }
            
            return responseService.success(pedidoActualizado, 
                "Pedido movido exitosamente de " + mesaAnterior + " a " + nuevaMesa);
            
        } catch (Exception e) {
            System.err.println("❌ Error al mover pedido: " + e.getMessage());
            return responseService.internalError("Error al mover pedido: " + e.getMessage());
        }
    }
}
