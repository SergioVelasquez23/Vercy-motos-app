package com.prog3.security.Controllers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.Date;

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
import com.prog3.security.Services.PedidoService;
import com.prog3.security.Services.MesaService; // Import agregado
// import com.prog3.security.Utils.ApiResponse; // Comentado para evitar conflicto con anotación Swagger
import com.prog3.security.DTOs.PagarPedidoRequest;
import com.prog3.security.DTOs.CancelarProductoRequest;
import com.prog3.security.Exception.BusinessException;
import com.prog3.security.Exception.ResourceNotFoundException;
import com.prog3.security.Models.Mesa;
import com.prog3.security.Repositories.MesaRepository;
import com.prog3.security.DTOs.PagoMixto;

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

    @PutMapping("/{id}/pagos/{index}")
    public ResponseEntity<?> editarPagoParcial(@PathVariable String id, @PathVariable int index, @RequestBody Map<String, Object> nuevoPago) {
        try {
            Pedido pedido = this.thePedidoRepository.findById(id).orElse(null);
            if (pedido == null) {
                return ResponseEntity.status(404).body("Pedido no encontrado");
            }
            if (index < 0 || index >= pedido.getPagosParciales().size()) {
                return ResponseEntity.status(400).body("Índice de pago inválido");
            }
            Pedido.PagoParcial pagoAnterior = pedido.getPagosParciales().get(index);
            double montoAnterior = pagoAnterior.getMonto();
            String formaPagoAnterior = pagoAnterior.getFormaPago();
            // Leer nuevos datos
            double montoNuevo = Double.parseDouble(nuevoPago.getOrDefault("monto", montoAnterior).toString());
            String formaPagoNueva = nuevoPago.getOrDefault("formaPago", formaPagoAnterior).toString();
            Object fechaObj = nuevoPago.getOrDefault("fecha", pagoAnterior.getFecha());
            LocalDateTime fechaNueva;
            if (fechaObj instanceof String) {
                fechaNueva = LocalDateTime.parse((String) fechaObj);
            } else if (fechaObj instanceof LocalDateTime) {
                fechaNueva = (LocalDateTime) fechaObj;
            } else {
                fechaNueva = pagoAnterior.getFecha();
            }
            String procesadoPorNuevo = nuevoPago.getOrDefault("procesadoPor", pagoAnterior.getProcesadoPor()).toString();
            // Actualizar pago parcial
            pagoAnterior.setMonto(montoNuevo);
            pagoAnterior.setFormaPago(formaPagoNueva);
            pagoAnterior.setFecha(fechaNueva);
            pagoAnterior.setProcesadoPor(procesadoPorNuevo);
            // Recalcular total pagado
            pedido.setTotalPagado(pedido.getTotalPagado() - montoAnterior + montoNuevo);
            // Actualizar cuadre de caja (restar anterior, sumar nuevo)
            cuadreCajaService.sumarPagoACuadreActivo(-montoAnterior, formaPagoAnterior);
            cuadreCajaService.sumarPagoACuadreActivo(montoNuevo, formaPagoNueva);
            // ✅ ACTUALIZAR ESTADO DEL PEDIDO (CONSIDERANDO DESCUENTOS)
            // Calcular total final esperado: (total - descuento) + propina
            double totalItems = pedido.getTotal();
            double descuento = pedido.getDescuento();
            double propina = pedido.getPropina();
            double totalConDescuento = Math.max(totalItems - descuento, 0.0);
            double totalFinalEsperado = totalConDescuento + propina;

            if (pedido.getTotalPagado() < totalFinalEsperado) {
                pedido.setEstado("pendiente");
            } else {
                pedido.setEstado("pagado");
            }
            this.thePedidoRepository.save(pedido);
            return ResponseEntity.ok(pedido.getPagosParciales());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al editar pago: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}/pagos/{index}")
    public ResponseEntity<?> eliminarPagoParcial(@PathVariable String id, @PathVariable int index) {
        try {
            Pedido pedido = this.thePedidoRepository.findById(id).orElse(null);
            if (pedido == null) {
                return ResponseEntity.status(404).body("Pedido no encontrado");
            }
            if (index < 0 || index >= pedido.getPagosParciales().size()) {
                return ResponseEntity.status(400).body("Índice de pago inválido");
            }
            // Obtener y eliminar el pago
            Pedido.PagoParcial pago = pedido.getPagosParciales().get(index);
            pedido.getPagosParciales().remove(index);
            pedido.setTotalPagado(pedido.getTotalPagado() - pago.getMonto());
            // Actualizar cuadre de caja
            cuadreCajaService.sumarPagoACuadreActivo(-pago.getMonto(), pago.getFormaPago());

            // ✅ ACTUALIZAR ESTADO DEL PEDIDO (CONSIDERANDO DESCUENTOS)
            // Calcular total final esperado: (total - descuento) + propina
            double totalItems = pedido.getTotal();
            double descuento = pedido.getDescuento();
            double propina = pedido.getPropina();
            double totalConDescuento = Math.max(totalItems - descuento, 0.0);
            double totalFinalEsperado = totalConDescuento + propina;

            if (pedido.getTotalPagado() < totalFinalEsperado) {
                pedido.setEstado("pendiente");
            }
            this.thePedidoRepository.save(pedido);
            return ResponseEntity.ok(pedido.getPagosParciales());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al eliminar pago: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/pagos")
    public ResponseEntity<?> getPagosParciales(@PathVariable String id) {
        try {
            Pedido pedido = this.thePedidoRepository.findById(id).orElse(null);
            if (pedido == null) {
                return ResponseEntity.status(404).body("Pedido no encontrado");
            }
            return ResponseEntity.ok(pedido.getPagosParciales());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al obtener pagos: " + e.getMessage());
        }
    }

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

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private MesaService mesaService;

    @Operation(summary = "Obtener todos los pedidos", description = "Retorna la lista completa de pedidos en el sistema")
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
            // Validar que mesa no esté vacía
            if (mesa == null || mesa.trim().isEmpty()) {
                return responseService.badRequest("El nombre de la mesa es requerido");
            }
            List<Pedido> pedidos = this.thePedidoRepository.findByMesa(mesa);
            return responseService.success(pedidos, "Pedidos de la mesa obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener pedidos de la mesa: " + e.getMessage());
        }
    }

    @GetMapping("/cliente/{cliente}")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<List<Pedido>>> findByCliente(
            @PathVariable String cliente) {
        try {
            List<Pedido> pedidos = this.thePedidoRepository.findByCliente(cliente);
            return responseService.success(pedidos, "Pedidos del cliente obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener pedidos del cliente: " + e.getMessage());
        }
    }

    @GetMapping("/mesero/{mesero}")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<List<Pedido>>> findByMesero(
            @PathVariable String mesero) {
        try {
            List<Pedido> pedidos = this.thePedidoRepository.findByMesero(mesero);
            return responseService.success(pedidos, "Pedidos del mesero obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener pedidos del mesero: " + e.getMessage());
        }
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<List<Pedido>>> findByEstado(
            @PathVariable String estado) {
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

    @GetMapping("/por-fecha")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<List<Pedido>>> getPedidosPorFecha(@RequestParam String fecha) {
        try {
            LocalDate fechaDate = LocalDate.parse(fecha);
            LocalDateTime startOfDay = fechaDate.atStartOfDay();
            LocalDateTime endOfDay = fechaDate.atTime(23, 59, 59, 999999999);
            
            List<Pedido> pedidos = thePedidoRepository.findByFechaBetween(startOfDay, endOfDay);
            return responseService.success(pedidos, "Pedidos obtenidos exitosamente para la fecha: " + fecha);
        } catch (Exception e) {
            return responseService.internalError("Formato de fecha inválido. Use formato: YYYY-MM-DD");
        }
    }

    @GetMapping("/plataforma/{plataforma}")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<List<Pedido>>> findByPlataforma(
            @PathVariable String plataforma) {
        try {
            List<Pedido> pedidos = this.thePedidoRepository.findByPlataforma(plataforma);
            return responseService.success(pedidos, "Pedidos filtrados por plataforma obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al filtrar pedidos por plataforma: " + e.getMessage());
        }
    }

    @GetMapping("/cuadre/{cuadreId}")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<List<Pedido>>> findByCuadreId(
            @PathVariable String cuadreId) {
        try {
            List<Pedido> pedidos = this.thePedidoRepository.findByCuadreCajaId(cuadreId);
            return responseService.success(pedidos, "Pedidos del cuadre de caja obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener pedidos del cuadre: " + e.getMessage());
        }
    }

    @GetMapping("/cuadre/{cuadreId}/pagados")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<List<Pedido>>> findByCuadreIdPagados(
            @PathVariable String cuadreId) {
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

            // Asignar el usuario que agrega cada producto
            String usuarioActual = newPedido.getMesero() != null ? newPedido.getMesero() : newPedido.getPedidoPor();
            if (usuarioActual == null) {
                usuarioActual = "sistema";
            }
            if (newPedido.getItems() != null) {
                for (ItemPedido item : newPedido.getItems()) {
                    item.setAgregadoPor(usuarioActual);
                    item.setFechaAgregado(LocalDateTime.now());
                }
            }

            Pedido pedidoCreado = this.thePedidoRepository.save(newPedido);

            // Verificar que el pedido se guardó correctamente con un ID válido
            if (pedidoCreado.get_id() == null || pedidoCreado.get_id().isEmpty()) {
                // Intentar obtener el pedido recién creado por otros campos únicos
                List<Pedido> pedidosRecientes = this.thePedidoRepository.findByMesaAndFechaBetween(
                        newPedido.getMesa(),
                        LocalDateTime.now().minusMinutes(1),
                        LocalDateTime.now().plusMinutes(1));

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
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<Pedido>> update(@PathVariable String id,
            @RequestBody Pedido newPedido) {
        try {
            Pedido actualPedido = this.thePedidoRepository.findById(id).orElse(null);
            if (actualPedido == null) {
                return responseService.notFound("Pedido no encontrado con ID: " + id);
            }

            actualPedido.setTipo(newPedido.getTipo());
            actualPedido.setMesa(newPedido.getMesa());
            actualPedido.setCliente(newPedido.getCliente());
            actualPedido.setMesero(newPedido.getMesero());
            actualPedido.setNotas(newPedido.getNotas());
            actualPedido.setPlataforma(newPedido.getPlataforma());
            actualPedido.setPedidoPor(newPedido.getPedidoPor());
            actualPedido.setGuardadoPor(newPedido.getGuardadoPor());
            actualPedido.setFechaCortesia(newPedido.getFechaCortesia());
            actualPedido.setEstado(newPedido.getEstado());

            // Determinar el usuario que realiza la edición
            String usuarioActual = newPedido.getMesero() != null ? newPedido.getMesero() : newPedido.getPedidoPor();
            if (usuarioActual == null) {
                usuarioActual = "sistema";
            }

            // Registrar historial de ediciones
            if (newPedido.getItems() != null) {
                // Comparar items anteriores con nuevos para detectar cambios
                List<ItemPedido> itemsAnteriores = actualPedido.getItems() != null ? actualPedido.getItems() : new ArrayList<>();
                List<ItemPedido> itemsNuevos = newPedido.getItems();

                // Productos agregados (nuevos IDs)
                for (ItemPedido itemNuevo : itemsNuevos) {
                    boolean esNuevo = itemsAnteriores.stream()
                            .noneMatch(item -> item.getProductoId().equals(itemNuevo.getProductoId()));
                    if (esNuevo) {
                        actualPedido.agregarEdicion("producto_agregado", usuarioActual, 
                            "Agregó: " + itemNuevo.getProductoNombre() + " (Cant: " + itemNuevo.getCantidad() + ")",
                            itemNuevo.getProductoId());
                    } else {
                        // Verificar si cambió la cantidad
                        ItemPedido itemAnterior = itemsAnteriores.stream()
                                .filter(item -> item.getProductoId().equals(itemNuevo.getProductoId()))
                                .findFirst().orElse(null);
                        if (itemAnterior != null && itemAnterior.getCantidad() != itemNuevo.getCantidad()) {
                            actualPedido.agregarEdicion("producto_editado", usuarioActual,
                                "Editó cantidad: " + itemNuevo.getProductoNombre() + 
                                " (de " + itemAnterior.getCantidad() + " a " + itemNuevo.getCantidad() + ")",
                                itemNuevo.getProductoId());
                        }
                    }
                    
                    // Asignar usuario y fecha a cada item
                    itemNuevo.setAgregadoPor(usuarioActual);
                    itemNuevo.setFechaAgregado(LocalDateTime.now());
                }

                // Productos eliminados (IDs que ya no están)
                for (ItemPedido itemAnterior : itemsAnteriores) {
                    boolean fueEliminado = itemsNuevos.stream()
                            .noneMatch(item -> item.getProductoId().equals(itemAnterior.getProductoId()));
                    if (fueEliminado) {
                        actualPedido.agregarEdicion("producto_eliminado", usuarioActual,
                            "Eliminó: " + itemAnterior.getProductoNombre() + " (Cant: " + itemAnterior.getCantidad() + ")",
                            itemAnterior.getProductoId());
                    }
                }
            }

            actualPedido.setItems(newPedido.getItems());

            // 💰 ACTUALIZAR DESCUENTOS Y PROPINAS del frontend
            actualPedido.setDescuento(newPedido.getDescuento());
            actualPedido.setIncluyePropina(newPedido.isIncluyePropina());

            // 💰 RECALCULAR TOTAL DEL PEDIDO con descuentos aplicados
            recalcularTotalConDescuentos(actualPedido);

            // Registrar edición general si cambió información básica
            boolean cambioMesa = !Objects.equals(actualPedido.getMesa(), newPedido.getMesa());
            boolean cambioCliente = !Objects.equals(actualPedido.getCliente(), newPedido.getCliente());
            // Las notas pueden ser nulas, así que usamos Objects.equals para la comparación segura
            boolean cambioNotas = !Objects.equals(actualPedido.getNotas(), newPedido.getNotas());
            
            if (cambioMesa || cambioCliente || cambioNotas) {
                actualPedido.agregarEdicion("pedido_editado", usuarioActual, "Editó información del pedido");
            }

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
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<Pedido>> cambiarEstado(@PathVariable String id,
            @PathVariable String estado) {
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

            // Restar todos los pagos asociados del cuadre de caja
            if (pedido.getPagosParciales() != null && !pedido.getPagosParciales().isEmpty()) {
                System.out.println("🔄 Restando pagos del pedido eliminado de la caja...");
                for (Pedido.PagoParcial pago : pedido.getPagosParciales()) {
                    cuadreCajaService.restarPagoDelCuadreActivo(pago.getMonto(), pago.getFormaPago());
                    System.out.println("   - Restado: $" + pago.getMonto() + " (" + pago.getFormaPago() + ")");
                }
            }

            // Eliminar el pedido
            this.thePedidoRepository.delete(pedido);
            // Limpieza automática de la mesa
            mesaService.limpiarMesaSiNoTienePedidos(pedido.getMesa());
            
            return responseService.success(null, "Pedido eliminado exitosamente y dinero descontado de caja");
        } catch (Exception e) {
            return responseService.internalError("Error al eliminar pedido: " + e.getMessage());
        }
    }

    /**
     * Elimina un pedido pagado y revierte los efectos en ventas y caja
     */
    @DeleteMapping("/{id}/pagado")
    @Operation(summary = "Eliminar pedido pagado", description = "Elimina un pedido que ya fue pagado y revierte los efectos en las ventas y el efectivo de caja")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<Void>> eliminarPedidoPagado(@PathVariable String id) {
        try {
            boolean eliminado = pedidoService.eliminarPedidoPagado(id);

            if (eliminado) {
                return responseService.success(null,
                        "Pedido pagado eliminado exitosamente. Se han revertido los cambios en ventas y caja.");
            } else {
                return responseService.badRequest("No se pudo eliminar el pedido pagado");
            }
        } catch (Exception e) {
            return responseService.internalError("Error al eliminar pedido pagado: " + e.getMessage());
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

            // ✅ CALCULAR TOTALES APLICANDO DESCUENTOS Y PROPINAS CORRECTAMENTE
            double totalEfectivo = pedidosPagados.stream()
                    .filter(p -> "efectivo".equalsIgnoreCase(p.getFormaPago()))
                    .mapToDouble(p -> {
                        // Aplicar descuentos y sumar propinas
                        double totalItems = p.getTotal();
                        double descuento = p.getDescuento();
                        double propina = p.getPropina();
                        double totalConDescuento = Math.max(totalItems - descuento, 0.0);
                        double totalFinal = totalConDescuento + propina;

                        // Usar totalPagado si está disponible, sino calcular
                        return p.getTotalPagado() > 0 ? p.getTotalPagado() : totalFinal;
                    })
                    .sum();

            double totalTransferencia = pedidosPagados.stream()
                    .filter(p -> "transferencia".equalsIgnoreCase(p.getFormaPago()))
                    .mapToDouble(p -> {
                        // Aplicar descuentos y sumar propinas
                        double totalItems = p.getTotal();
                        double descuento = p.getDescuento();
                        double propina = p.getPropina();
                        double totalConDescuento = Math.max(totalItems - descuento, 0.0);
                        double totalFinal = totalConDescuento + propina;

                        // Usar totalPagado si está disponible, sino calcular
                        return p.getTotalPagado() > 0 ? p.getTotalPagado() : totalFinal;
                    })
                    .sum();

            double totalTarjeta = pedidosPagados.stream()
                    .filter(p -> "tarjeta".equalsIgnoreCase(p.getFormaPago()))
                    .mapToDouble(p -> {
                        // Aplicar descuentos y sumar propinas
                        double totalItems = p.getTotal();
                        double descuento = p.getDescuento();
                        double propina = p.getPropina();
                        double totalConDescuento = Math.max(totalItems - descuento, 0.0);
                        double totalFinal = totalConDescuento + propina;

                        // Usar totalPagado si está disponible, sino calcular
                        return p.getTotalPagado() > 0 ? p.getTotalPagado() : totalFinal;
                    })
                    .sum();

            double totalOtros = pedidosPagados.stream()
                    .filter(p -> p.getFormaPago() == null
                    || (!p.getFormaPago().equalsIgnoreCase("efectivo")
                    && !p.getFormaPago().equalsIgnoreCase("transferencia")
                    && !p.getFormaPago().equalsIgnoreCase("tarjeta")))
                    .mapToDouble(p -> {
                        // Aplicar descuentos y sumar propinas
                        double totalItems = p.getTotal();
                        double descuento = p.getDescuento();
                        double propina = p.getPropina();
                        double totalConDescuento = Math.max(totalItems - descuento, 0.0);
                        double totalFinal = totalConDescuento + propina;

                        // Usar totalPagado si está disponible, sino calcular
                        return p.getTotalPagado() > 0 ? p.getTotalPagado() : totalFinal;
                    })
                    .sum();

            double totalGeneral = totalEfectivo + totalTransferencia + totalTarjeta + totalOtros;

            System.out.println(
                    "=== RESUMEN DE VENTAS POR FORMA DE PAGO (💰 CON DESCUENTOS APLICADOS) ===");
            System.out.println("Total Efectivo: " + totalEfectivo);
            System.out.println("Total Transferencia: " + totalTransferencia);
            System.out.println("Total Tarjeta: " + totalTarjeta);
            System.out.println("Total Otros: " + totalOtros);
            System.out.println("Total General: " + totalGeneral);

            // ✅ DEBUG: Calcular totales sin descuentos para comparación
            double totalSinDescuentos =
                    pedidosPagados.stream().mapToDouble(p -> p.getTotal() + p.getPropina()).sum();
            double totalDescuentosAplicados =
                    pedidosPagados.stream().mapToDouble(p -> p.getDescuento()).sum();
            System.out.println("\n📊 COMPARACIÓN:");
            System.out.println("Total SIN descuentos: " + totalSinDescuentos);
            System.out.println("Total descuentos aplicados: " + totalDescuentosAplicados);
            System.out.println("Total CON descuentos: " + totalGeneral);
            System.out.println(
                    "Diferencia (ahorro clientes): " + (totalSinDescuentos - totalGeneral));

            TotalVentasResponse response = new TotalVentasResponse(totalGeneral, totalEfectivo, totalTransferencia,
                    totalTarjeta, totalOtros);
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

    @Operation(summary = "Procesar pago de pedido", description = """
            Procesa el pago de un pedido con diferentes tipos:
            - **pagado**: Pago normal con propina opcional
            - **cortesia**: Sin costo (cumpleaños, promociones)
            - **consumo_interno**: Para empleados/gerencia
            - **cancelado**: Cancelación del pedido

            **Validaciones importantes:**
            - Debe haber una caja abierta
            - El pedido no debe estar ya procesado
            - Los campos requeridos según el tipo de pago
            """)
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pedido procesado exitosamente", content = @Content(schema = @Schema(implementation = Pedido.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos o caja no abierta", content = @Content(examples = @ExampleObject(name = "Caja no abierta", value = "{\"success\": false, \"message\": \"No se puede procesar el pago sin una caja abierta\"}"))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pedido no encontrado"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "El pedido ya está procesado")
    })
    @PutMapping("/{id}/pagar")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<Pedido>> pagarPedido(
            @Parameter(description = "ID del pedido a procesar", required = true) @PathVariable String id,
            @Parameter(description = "Datos del pago", required = true) @RequestBody @Valid PagarPedidoRequest pagarRequest) {
        try {
            // 🔍 DEBUG COMPLETO: Mostrar TODOS los datos que llegan del frontend
            System.out.println("\n🔍 ===== DEBUG COMPLETO PAGO PEDIDO =====");
            System.out.println("📝 ID del pedido: " + id);
            System.out.println("💰 Datos completos del request:");
            System.out.println("  - FormaPago: " + pagarRequest.getFormaPago());
            System.out.println("  - Propina: " + pagarRequest.getPropina());
            System.out.println("  - Descuento: " + pagarRequest.getDescuento());
            System.out.println("  - ProcesadoPor: " + pagarRequest.getProcesadoPor());
            System.out.println("🔍 Raw object: " + pagarRequest.toString());
            System.out.println("===========================================\n");

            System.out.println("[PAGAR_PEDIDO] Iniciando pago para pedido ID: " + id);

            // Validar que el ID no sea nulo o vacío
            if (id == null || id.trim().isEmpty()) {
                throw new IllegalArgumentException("El ID del pedido es requerido");
            }

            Pedido pedido = this.thePedidoRepository.findById(id)
                    .orElseThrow(() -> ResourceNotFoundException.pedido(id));

            // Validar que el pedido no esté ya pagado (evitar dobles pagos)
            if ("pagado".equals(pedido.getEstado()) || "cortesia".equals(pedido.getEstado())
                    || "consumo_interno".equals(pedido.getEstado()) || "cancelado".equals(pedido.getEstado())) {
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
                System.out.println(
                        "[PAGAR_PEDIDO] Procesando pago real. FormaPago: " + pagarRequest.getFormaPago() + ", Propina: "
                                + pagarRequest.getPropina() + ", Descuento: "
                                + pagarRequest.getDescuento() + ", ProcesadoPor: "
                                + pagarRequest.getProcesadoPor());

                // ✅ APLICAR DESCUENTO AL PEDIDO
                if (pagarRequest.getDescuento() > 0) {
                    System.out.println("💰 Aplicando descuento: $" + pagarRequest.getDescuento());
                    pedido.setDescuento(pagarRequest.getDescuento());
                }
                // Pagos parciales y múltiples formas de pago
                // Sumar propina
                if (pagarRequest.getPropina() > 0) {
                    pedido.setPropina(pedido.getPropina() + pagarRequest.getPropina());
                }
                
                // Procesar el pago del pedido
                if (pagarRequest.getFormaPago() != null) {
                    // Verificar si es un pago mixto
                    boolean esPagoMixto = pagarRequest.getPagosMixtos() != null && !pagarRequest.getPagosMixtos().isEmpty();
                    
                    if (esPagoMixto) {
                        // ✅ CALCULAR TOTAL CON DESCUENTO APLICADO
                        double totalOriginal = pedido.getTotal();
                        double descuento = pagarRequest.getDescuento();
                        double totalConDescuento = Math.max(totalOriginal - descuento, 0.0);
                        double totalFinal = totalConDescuento + pagarRequest.getPropina();

                        System.out.println("💰 Cálculos de pago mixto:");
                        System.out.println("   - Total original: $" + totalOriginal);
                        System.out.println("   - Descuento: $" + descuento);
                        System.out.println("   - Total con descuento: $" + totalConDescuento);
                        System.out.println("   - Propina: $" + pagarRequest.getPropina());
                        System.out.println("   - Total final: $" + totalFinal);

                        // Si es un pago mixto, configuramos el estado y otros campos básicos
                        pedido.setEstado("pagado");
                        pedido.setFormaPago("mixto"); // Marcar específicamente como pago mixto
                        pedido.setPropina(pagarRequest.getPropina());
                        pedido.setDescuento(descuento); // ✅ GUARDAR DESCUENTO
                        pedido.setTotalPagado(totalFinal); // ✅ USAR TOTAL FINAL CON DESCUENTO
                        pedido.setFechaPago(LocalDateTime.now());
                        pedido.setPagadoPor(pagarRequest.getProcesadoPor());
                        
                        // Procesar cada forma de pago individual
                        System.out.println("💰 Procesando pago mixto:");
                        double totalRegistrado = 0.0;
                        
                        // Iterar sobre los pagos mixtos
                        for (PagoMixto pagoMixto : pagarRequest.getPagosMixtos()) {
                            String formaPago = pagoMixto.getFormaPago();
                            double monto = pagoMixto.getMonto();
                            
                            // Registrar pago parcial
                            pedido.agregarPagoParcial(monto, formaPago, pagarRequest.getProcesadoPor());
                            
                            // Sumar a la caja según forma de pago
                            // Esto asegura que cada monto se suma a la categoría correcta (efectivo, transferencia, etc.)
                            System.out.println("   - $" + monto + " por " + formaPago);
                            cuadreCajaService.sumarPagoACuadreActivo(monto, formaPago);
                            
                            totalRegistrado += monto;
                        }
                        
                        // Distribuir propina proporcionalmente según el monto de cada forma de pago
                        if (pagarRequest.getPropina() > 0 && !pagarRequest.getPagosMixtos().isEmpty()) {
                            // Calcular el porcentaje que representa cada forma de pago del total
                            double totalMixto = 0.0;
                            for (PagoMixto pago : pagarRequest.getPagosMixtos()) {
                                totalMixto += pago.getMonto();
                            }
                            
                            // Distribuir propina proporcionalmente
                            for (PagoMixto pagoMixto : pagarRequest.getPagosMixtos()) {
                                // Calcular propina proporcional al monto del pago
                                double porcentajePago = pagoMixto.getMonto() / totalMixto;
                                double propinaPorFormaPago = pagarRequest.getPropina() * porcentajePago;
                                
                                // Sumar la propina a la caja correspondiente
                                System.out.println("   - Propina: $" + propinaPorFormaPago + " por " + pagoMixto.getFormaPago() + 
                                                   " (" + (porcentajePago * 100) + "% del pago)");
                                cuadreCajaService.sumarPagoACuadreActivo(propinaPorFormaPago, pagoMixto.getFormaPago());
                            }
                        }
                        
                        System.out.println("💰 Total registrado en pagos mixtos: $" + totalRegistrado);
                        System.out.println(
                                "💰 Total con descuento del pedido: $" + totalConDescuento);
                        
                        // Verificar que los montos son correctos (comparar con total con descuento)
                        if (Math.abs(totalRegistrado - totalConDescuento) > 0.01) {
                            System.out.println("⚠️ ADVERTENCIA: El total de pagos mixtos ($" + totalRegistrado + 
                                    ") no coincide con el total con descuento del pedido ($"
                                    + totalConDescuento + ")");
                        }
                    } else {
                        // ✅ CALCULAR TOTAL CON DESCUENTO APLICADO PARA PAGO SIMPLE
                        double totalOriginal = pedido.getTotal();
                        double descuento = pagarRequest.getDescuento();
                        double totalConDescuento = Math.max(totalOriginal - descuento, 0.0);
                        double totalFinal = totalConDescuento + pagarRequest.getPropina();

                        System.out.println("💰 Cálculos de pago simple:");
                        System.out.println("   - Total original: $" + totalOriginal);
                        System.out.println("   - Descuento: $" + descuento);
                        System.out.println("   - Total con descuento: $" + totalConDescuento);
                        System.out.println("   - Propina: $" + pagarRequest.getPropina());
                        System.out.println("   - Total final: $" + totalFinal);

                        // Pago regular con una sola forma de pago
                        // ✅ APLICAR DESCUENTO ANTES DEL PAGO
                        pedido.setDescuento(descuento);

                        // ✅ CONFIGURAR PAGO MANUALMENTE (no usar pedido.pagar() que sobrescribe
                        // totalPagado)
                        // NOTA: pedido.pagar() usa this.total + propina ignorando descuentos
                        // Por eso configuramos cada campo manualmente para mantener el descuento
                        pedido.setEstado("pagado");
                        pedido.setFormaPago(pagarRequest.getFormaPago());
                        pedido.setPropina(pagarRequest.getPropina()); // Ya se había sumado arriba
                        pedido.setTotalPagado(totalFinal); // ✅ MANTENER EL TOTAL CORRECTO CON
                                                           // DESCUENTO
                        pedido.setFechaPago(LocalDateTime.now());
                        pedido.setPagadoPor(pagarRequest.getProcesadoPor());

                        // Log para debugging
                        System.out.println("💰 Pedido procesado como pagado simple:");
                        System.out.println("   - Total original: $" + totalOriginal);
                        System.out.println("   - Descuento aplicado: $" + descuento);
                        System.out.println("   - Total con descuento: $" + totalConDescuento);
                        System.out.println("   - Propina: $" + pedido.getPropina());
                        System.out.println("   - Total pagado final: $" + pedido.getTotalPagado());
                        System.out.println("   - Forma de pago: " + pedido.getFormaPago());
                        
                        // ✅ SUMAR SOLO EL TOTAL CON DESCUENTO A LA CAJA (NO EL TOTAL ORIGINAL)
                        System.out.println("💰 Sumando venta a caja: $" + totalConDescuento
                                + " por " + pagarRequest.getFormaPago());
                        cuadreCajaService.sumarPagoACuadreActivo(totalConDescuento,
                                pagarRequest.getFormaPago());
                        
                        // Sumar la propina a la caja si hay
                        if (pagarRequest.getPropina() > 0) {
                            System.out.println("💰 Sumando propina a caja: $" + pagarRequest.getPropina() + " por " + pagarRequest.getFormaPago());
                            cuadreCajaService.sumarPagoACuadreActivo(pagarRequest.getPropina(), pagarRequest.getFormaPago());
                        }
                    }
                }
                pedido.setNotas(notasAdicionales);
                
                // No es necesario configurar el estado o fecha porque 
                // el método pagar() ya lo hace correctamente
            } else if (pagarRequest.esCortesia()) {
                System.out.println("[PAGAR_PEDIDO] Procesando cortesía");

                // ✅ APLICAR DESCUENTO TAMBIÉN EN CORTESÍAS
                if (pagarRequest.getDescuento() > 0) {
                    System.out.println(
                            "💰 Aplicando descuento en cortesía: $" + pagarRequest.getDescuento());
                    pedido.setDescuento(pagarRequest.getDescuento());
                }

                pedido.setEstado("cortesia");
                pedido.setFechaCortesia(LocalDateTime.now());
                pedido.setPropina(0);
                pedido.setTotalPagado(0);
                String motivoCortesia = pagarRequest.getMotivoCortesia() != null ? pagarRequest.getMotivoCortesia()
                        : "";
                pedido.setNotas("CORTESÍA - " + motivoCortesia + " - " + notasAdicionales);
                // Liberar la mesa si existe
                if (pedido.getMesa() != null && !pedido.getMesa().isEmpty()) {
                    MesaRepository mesaRepository = (MesaRepository) org.springframework.beans.factory.BeanFactoryUtils.beanOfTypeIncludingAncestors(
                            org.springframework.web.context.ContextLoader.getCurrentWebApplicationContext(), MesaRepository.class);
                    Mesa mesa = mesaRepository.findByNombre(pedido.getMesa());
                    if (mesa != null) {
                        mesa.setOcupada(false);
                        mesaRepository.save(mesa);
                        System.out.println("[PAGAR_PEDIDO] Mesa liberada por cortesía: " + mesa.getNombre());
                    }
                }
            } else if (pagarRequest.esConsumoInterno()) {
                System.out.println("[PAGAR_PEDIDO] Procesando consumo interno");
                pedido.setEstado("consumo_interno");
                pedido.setPropina(0);
                pedido.setTotalPagado(0);
                String tipoConsumo = pagarRequest.getTipoConsumoInterno() != null ? pagarRequest.getTipoConsumoInterno()
                        : "";
                pedido.setNotas("CONSUMO INTERNO - " + tipoConsumo + " - " + notasAdicionales);
                // Liberar la mesa si existe
                if (pedido.getMesa() != null && !pedido.getMesa().isEmpty()) {
                    MesaRepository mesaRepository = (MesaRepository) org.springframework.beans.factory.BeanFactoryUtils.beanOfTypeIncludingAncestors(
                            org.springframework.web.context.ContextLoader.getCurrentWebApplicationContext(), MesaRepository.class);
                    Mesa mesa = mesaRepository.findByNombre(pedido.getMesa());
                    if (mesa != null) {
                        mesa.setOcupada(false);
                        mesaRepository.save(mesa);
                        System.out.println("[PAGAR_PEDIDO] Mesa liberada por consumo interno: " + mesa.getNombre());
                    }
                }
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

            // 🔍 DEBUG ESPECÍFICO ANTES DE GUARDAR
            System.out.println("\n💰 ===== DEBUG DESCUENTO ANTES DE GUARDAR =====");
            System.out.println("  - Total original: " + pedido.getTotal());
            System.out.println("  - Descuento aplicado: " + pedido.getDescuento());
            System.out.println("  - Propina aplicada: " + pedido.getPropina());
            System.out.println("  - Total pagado: " + pedido.getTotalPagado());
            System.out.println("================================================\n");

            Pedido pedidoProcesado = this.thePedidoRepository.save(pedido);

            // 🔍 DEBUG ESPECÍFICO DESPUÉS DE GUARDAR
            System.out.println("\n✅ ===== DEBUG DESCUENTO DESPUÉS DE GUARDAR =====");
            System.out.println("  - Total original: " + pedidoProcesado.getTotal());
            System.out.println("  - Descuento persistido: " + pedidoProcesado.getDescuento());
            System.out.println("  - Propina persistida: " + pedidoProcesado.getPropina());
            System.out.println("  - Total pagado persistido: " + pedidoProcesado.getTotalPagado());
            System.out.println("==================================================\n");

            System.out.println("[PAGAR_PEDIDO] Pedido guardado correctamente. ID: " + pedidoProcesado.get_id());

            // Asignar al cuadre de caja activo siempre (no solo para pagos)
            if (pedidoProcesado != null) {
                boolean asignado = cuadreCajaService.asignarPedidoACuadreActivo(pedidoProcesado.get_id());
                if (!asignado) {
                    System.out
                            .println("⚠️ Advertencia: Pedido procesado pero no se pudo asignar a ningún cuadre activo");
                } else {
                    System.out.println("[PAGAR_PEDIDO] Pedido asignado a cuadre activo correctamente");
                }

                // Notificar vía WebSocket que se pagó un pedido (para actualizar dashboard)
                try {
                    // ✅ CALCULAR TOTAL CORRECTO CON DESCUENTOS PARA WEBSOCKET
                    double totalItems = pedidoProcesado.getTotal();
                    double descuento = pedidoProcesado.getDescuento();
                    double propina = pedidoProcesado.getPropina();
                    double totalConDescuento = Math.max(totalItems - descuento, 0.0);
                    double totalFinal = totalConDescuento + propina;

                    double totalParaNotificacion =
                            pedidoProcesado.getTotalPagado() > 0 ? pedidoProcesado.getTotalPagado()
                                    : totalFinal;

                    webSocketService.notificarPedidoPagado(
                            pedidoProcesado.get_id(),
                            pedidoProcesado.getMesa(),
                            totalParaNotificacion,
                            pedidoProcesado.getFormaPago());
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
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<List<Pedido>>> getPedidosActivosPorMesa(
            @PathVariable String mesa) {
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
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<Pedido>> createPedidoMesaEspecial(
            @RequestBody Pedido pedido) {
        try {
            // Validar que se proporcione el nombre del pedido
            if (pedido.getNombrePedido() == null || pedido.getNombrePedido().trim().isEmpty()) {
                return responseService.badRequest("El nombre del pedido es obligatorio para mesas especiales");
            }

            // Validar que la mesa existe y es especial (esto se podría hacer con el repositorio de mesas)
            // Verificar si ya existe un pedido activo con ese nombre en esa mesa
            List<Pedido> pedidosExistentes = this.thePedidoRepository.findPedidoActivoByMesaAndNombre(
                    pedido.getMesa(),
                    pedido.getNombrePedido());

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
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<List<CancelarProductoRequest.IngredienteADevolver>>> getIngredientesParaDevolucion(
            @PathVariable String pedidoId,
            @PathVariable String productoId,
            @RequestParam int cantidad) {
        try {
            // Buscar el pedido
            Pedido pedido = this.thePedidoRepository.findById(pedidoId).orElse(null);
            if (pedido == null) {
                return responseService.notFound("Pedido no encontrado con ID: " + pedidoId);
            }

            // Obtener los ingredientes que fueron descontados
            List<CancelarProductoRequest.IngredienteADevolver> ingredientesDescontados = inventarioService
                    .getIngredientesDescontadosParaProducto(pedidoId, productoId, cantidad);

            return responseService.success(ingredientesDescontados,
                    "Ingredientes para devolución obtenidos exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener ingredientes para devolución: " + e.getMessage());
        }
    }

    /**
     * Cancelar un producto del pedido con devolución selectiva de ingredientes
     */
    @PostMapping("/cancelar-producto")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<Pedido>> cancelarProducto(
            @RequestBody CancelarProductoRequest request) {
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
                            return responseService
                                    .badRequest("No se puede cancelar más cantidad de la que existe en el pedido");
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

            // Validar que solo se devuelvan ingredientes realmente seleccionados en el producto
            if (request.getIngredientesADevolver() != null && !request.getIngredientesADevolver().isEmpty()) {
                // Buscar el item pedido correspondiente
                ItemPedido itemPedidoSeleccionado = null;
                if (pedido.getItems() != null) {
                    for (ItemPedido item : pedido.getItems()) {
                        if (item.getProductoId().equals(request.getProductoId())) {
                            itemPedidoSeleccionado = item;
                            break;
                        }
                    }
                }
                List<com.prog3.security.DTOs.CancelarProductoRequest.IngredienteADevolver> ingredientesFiltrados = new ArrayList<>();
                if (itemPedidoSeleccionado != null && itemPedidoSeleccionado.getIngredientesSeleccionados() != null) {
                    int cantidadOriginal = itemPedidoSeleccionado.getCantidad() + request.getCantidadACancelar(); // antes de cancelar
                    int cantidadCancelada = request.getCantidadACancelar();
                    double proporcionCancelada = cantidadOriginal > 0 ? ((double) cantidadCancelada / cantidadOriginal) : 0;
                    for (com.prog3.security.DTOs.CancelarProductoRequest.IngredienteADevolver ing : request.getIngredientesADevolver()) {
                        if (itemPedidoSeleccionado.getIngredientesSeleccionados().contains(ing.getIngredienteId()) && ing.isDevolver() && ing.getCantidadADevolver() > 0) {
                            // Ajustar cantidad a devolver proporcionalmente
                            double cantidadProporcional = ing.getCantidadOriginal() * proporcionCancelada;
                            ing.setCantidadADevolver(cantidadProporcional);
                            ingredientesFiltrados.add(ing);
                        }
                    }
                }
                if (!ingredientesFiltrados.isEmpty()) {
                    inventarioService.devolverIngredientesAlInventario(
                            request.getPedidoId(),
                            request.getProductoId(),
                            ingredientesFiltrados,
                            request.getCanceladoPor());
                }
            }

            // Recalcular total del pedido con descuentos aplicados
            recalcularTotalConDescuentos(pedido);

            // Agregar nota de cancelación con detalle de ingredientes devueltos y no devueltos
            StringBuilder detalleIngredientes = new StringBuilder();
            if (request.getIngredientesADevolver() != null && !request.getIngredientesADevolver().isEmpty()) {
                detalleIngredientes.append("\nIngredientes devueltos:");
                for (com.prog3.security.DTOs.CancelarProductoRequest.IngredienteADevolver ing : request.getIngredientesADevolver()) {
                    if (ing.isDevolver() && ing.getCantidadADevolver() > 0) {
                        detalleIngredientes.append("\n- ").append(ing.getNombreIngrediente()).append(": ").append(ing.getCantidadADevolver()).append(" ").append(ing.getUnidad());
                    }
                }
                detalleIngredientes.append("\nIngredientes NO devueltos:");
                for (com.prog3.security.DTOs.CancelarProductoRequest.IngredienteADevolver ing : request.getIngredientesADevolver()) {
                    if (!ing.isDevolver()) {
                        detalleIngredientes.append("\n- ").append(ing.getNombreIngrediente()).append(" (Motivo: ").append(ing.getMotivoNoDevolucion() != null ? ing.getMotivoNoDevolucion() : "No especificado").append(")");
                    }
                }
            }
            String notaCancelacion = "CANCELACIÓN: " + request.getMotivoCancelacion();
            if (request.getNotas() != null && !request.getNotas().isEmpty()) {
                notaCancelacion += " - " + request.getNotas();
            }
            if (detalleIngredientes.length() > 0) {
                notaCancelacion += detalleIngredientes.toString();
            }
            String notasActuales = pedido.getNotas() != null ? pedido.getNotas() : "";
            pedido.setNotas(notasActuales.isEmpty() ? notaCancelacion : notasActuales + "\n" + notaCancelacion);

            // Guardar el pedido actualizado
            Pedido pedidoActualizado = this.thePedidoRepository.save(pedido);

            return responseService.success(pedidoActualizado,
                    "Producto cancelado exitosamente con devolución selectiva de ingredientes");
        } catch (Exception e) {
            return responseService.internalError("Error al cancelar producto: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/test-inventario")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<String>> testProcesarInventario(
            @PathVariable String id) {
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
                    "Se eliminaron " + cantidadEliminada + " pedidos exitosamente");
        } catch (Exception e) {
            System.err.println("❌ Error al eliminar todos los pedidos: " + e.getMessage());
            return responseService.internalError("Error al eliminar todos los pedidos: " + e.getMessage());
        }
    }

    /**
     * Eliminar pedidos por estado específico
     */
    @DeleteMapping("/admin/eliminar-por-estado/{estado}")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<String>> eliminarPedidosPorEstado(
            @PathVariable String estado) {
        try {
            List<Pedido> pedidosPorEstado = thePedidoRepository.findByEstado(estado);
            int cantidadEliminada = pedidosPorEstado.size();

            System.out.println("⚠️ ELIMINANDO PEDIDOS CON ESTADO '" + estado + "': " + cantidadEliminada + " pedidos");

            thePedidoRepository.deleteAll(pedidosPorEstado);

            System.out.println("✅ Se eliminaron " + cantidadEliminada + " pedidos con estado '" + estado + "'");

            return responseService.success(
                    "PEDIDOS_ELIMINADOS",
                    "Se eliminaron " + cantidadEliminada + " pedidos con estado '" + estado + "'");
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

            System.out.println("⚠️ ELIMINANDO PEDIDOS ENTRE " + fechaInicio + " Y " + fechaFin + ": "
                    + cantidadEliminada + " pedidos");

            thePedidoRepository.deleteAll(pedidosEnRango);

            System.out.println("✅ Se eliminaron " + cantidadEliminada + " pedidos en el rango de fechas");

            return responseService.success(
                    "PEDIDOS_ELIMINADOS",
                    "Se eliminaron " + cantidadEliminada + " pedidos entre " + fechaInicio + " y " + fechaFin);
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
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<Integer>> contarPedidosPorEstado(
            @PathVariable String estado) {
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

    /**
     * 🔄 Mueve productos específicos de un pedido a otra mesa - Edita el pedido
     * actual eliminando los productos seleccionados - Crea un nuevo pedido en
     * la mesa destino con esos productos
     */
    @PostMapping("/mover-productos-especificos")
    public ResponseEntity<?> moverProductosEspecificos(@RequestBody Map<String, Object> request) {
        try {
            // 📋 Extraer datos del request
            String pedidoId = (String) request.get("pedidoId");
            String mesaDestino = (String) request.get("mesaDestino");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> productosAMover = (List<Map<String, Object>>) request.get("productos");

            System.out.println("🔄 Iniciando movimiento de productos específicos:");
            System.out.println("   📦 Pedido origen: " + pedidoId);
            System.out.println("   🪑 Mesa destino: " + mesaDestino);
            System.out.println("   📋 Productos a mover: " + productosAMover.size());

            // ✅ Validaciones básicas
            if (pedidoId == null || mesaDestino == null || productosAMover == null || productosAMover.isEmpty()) {
                return responseService.badRequest("Datos incompletos para mover productos");
            }

            // 🔍 Buscar pedido origen
            Optional<Pedido> pedidoOrigenOpt = thePedidoRepository.findById(pedidoId);
            if (!pedidoOrigenOpt.isPresent()) {
                return responseService.badRequest("Pedido origen no encontrado");
            }

            Pedido pedidoOrigen = pedidoOrigenOpt.get();
            System.out.println("✅ Pedido origen encontrado - Mesa: " + pedidoOrigen.getMesa());

            // 📦 Crear lista de productos para el nuevo pedido
            List<ItemPedido> productosNuevoPedido = new ArrayList<>();
            List<ItemPedido> productosOriginalesActualizados = new ArrayList<>();

            // 🔄 Procesar cada producto del pedido original
            for (ItemPedido productoOriginal : pedidoOrigen.getItems()) {
                boolean encontradoEnMover = false;
                int cantidadAMover = 0;

                // 🔍 Verificar si este producto está en la lista de productos a mover
                for (Map<String, Object> productoMover : productosAMover) {
                    String productoId = (String) productoMover.get("productoId");
                    Integer cantidad = (Integer) productoMover.get("cantidad");

                    if (productoOriginal.getProductoId().equals(productoId)) {
                        encontradoEnMover = true;
                        cantidadAMover = cantidad;
                        break;
                    }
                }

                if (encontradoEnMover) {
                    // ✂️ Producto se va a mover (parcial o totalmente)
                    if (cantidadAMover >= productoOriginal.getCantidad()) {
                        // 🚚 Mover todo el producto al nuevo pedido
                        ItemPedido productoNuevo = new ItemPedido();
                        productoNuevo.setProductoId(productoOriginal.getProductoId());
                        productoNuevo.setProductoNombre(productoOriginal.getProductoNombre());
                        productoNuevo.setCantidad(productoOriginal.getCantidad());
                        productoNuevo.setPrecioUnitario(productoOriginal.getPrecioUnitario());
                        productoNuevo.setNotas(productoOriginal.getNotas());
                        productosNuevoPedido.add(productoNuevo);

                        System.out
                                .println("   🚚 Producto movido completamente: " + productoOriginal.getProductoNombre()
                                        + " (Cantidad: " + productoOriginal.getCantidad() + ")");
                    } else {
                        // ✂️ Dividir producto: parte se queda, parte se va
                        // Crear producto para el nuevo pedido
                        ItemPedido productoNuevo = new ItemPedido();
                        productoNuevo.setProductoId(productoOriginal.getProductoId());
                        productoNuevo.setProductoNombre(productoOriginal.getProductoNombre());
                        productoNuevo.setCantidad(cantidadAMover);
                        productoNuevo.setPrecioUnitario(productoOriginal.getPrecioUnitario());
                        productoNuevo.setNotas(productoOriginal.getNotas());
                        productosNuevoPedido.add(productoNuevo);

                        // Actualizar producto original (reducir cantidad)
                        ItemPedido productoActualizado = new ItemPedido();
                        productoActualizado.setProductoId(productoOriginal.getProductoId());
                        productoActualizado.setProductoNombre(productoOriginal.getProductoNombre());
                        productoActualizado.setCantidad(productoOriginal.getCantidad() - cantidadAMover);
                        productoActualizado.setPrecioUnitario(productoOriginal.getPrecioUnitario());
                        productoActualizado.setNotas(productoOriginal.getNotas());
                        productosOriginalesActualizados.add(productoActualizado);

                        System.out.println("   ✂️ Producto dividido: " + productoOriginal.getProductoNombre()
                                + " (Original: " + productoOriginal.getCantidad()
                                + ", Movido: " + cantidadAMover
                                + ", Restante: " + (productoOriginal.getCantidad() - cantidadAMover) + ")");
                    }
                } else {
                    // 🏠 Producto se queda en el pedido original
                    productosOriginalesActualizados.add(productoOriginal);
                }
            }

            // 🔍 Buscar pedido existente en mesa destino o crear nuevo
            Pedido pedidoDestino = null;
            List<Pedido> pedidosActivos = thePedidoRepository.findPedidosActivosByMesa(mesaDestino);

            if (!pedidosActivos.isEmpty()) {
                // Tomar el primer pedido activo encontrado
                // ➕ Agregar productos al pedido existente
                pedidoDestino = pedidosActivos.get(0);
                pedidoDestino.getItems().addAll(productosNuevoPedido);

                // 💰 Recalcular total del pedido existente con descuentos
                recalcularTotalConDescuentos(pedidoDestino);
                pedidoDestino.setFecha(LocalDateTime.now());

                System.out.println("➕ Productos agregados al pedido existente - Mesa: " + mesaDestino);
            } else {
                // 🆕 Crear pedido nuevo solo si no hay uno activo
                pedidoDestino = new Pedido();
                pedidoDestino.setMesa(mesaDestino);
                pedidoDestino.setItems(productosNuevoPedido);
                pedidoDestino.setEstado("activo");
                pedidoDestino.setFecha(LocalDateTime.now());

                // 💰 Calcular totales del nuevo pedido
                double totalNuevo = productosNuevoPedido.stream()
                        .mapToDouble(ItemPedido::getSubtotal)
                        .sum();
                pedidoDestino.setTotal(totalNuevo);

                System.out.println("🆕 Nuevo pedido creado - Mesa: " + mesaDestino);
            }

            // 💾 Guardar pedido (nuevo o actualizado)
            Pedido pedidoGuardado = thePedidoRepository.save(pedidoDestino);
            System.out.println(
                    "✅ Pedido guardado - ID: " + pedidoGuardado.get_id() + ", Total: $" + pedidoGuardado.getTotal());

            // 🔄 Actualizar pedido original
            if (productosOriginalesActualizados.isEmpty()) {
                // 🗑️ Si no quedan productos, eliminar el pedido original
                thePedidoRepository.deleteById(pedidoId);
                System.out.println("🗑️ Pedido original eliminado (sin productos restantes)");
            } else {
                // ✏️ Actualizar pedido original con productos restantes
                pedidoOrigen.setItems(productosOriginalesActualizados);
                recalcularTotalConDescuentos(pedidoOrigen);
                pedidoOrigen.setFecha(LocalDateTime.now());
                thePedidoRepository.save(pedidoOrigen);
                System.out.println(
                        "✏️ Pedido original actualizado - Total: $" + pedidoOrigen.getTotal());
            }

            // 📊 Preparar respuesta
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("success", true);
            resultado.put("message", "Productos movidos exitosamente");
            resultado.put("nuevoPedidoId", pedidoGuardado.get_id());
            resultado.put("mesaDestino", mesaDestino);
            resultado.put("productosMovidos", productosNuevoPedido.size());
            resultado.put("pedidoOriginalEliminado", productosOriginalesActualizados.isEmpty());

            System.out.println("🎉 Movimiento de productos completado exitosamente");
            return responseService.success(resultado, "Productos movidos exitosamente");

        } catch (Exception e) {
            System.err.println("❌ Error al mover productos específicos: " + e.getMessage());
            e.printStackTrace();
            return responseService.internalError("Error al mover productos específicos: " + e.getMessage());
        }
    }

    /**
     * 💰 Procesa pago parcial de productos específicos de un pedido - Crea un
     * pedido pagado con los productos seleccionados - Crea un documento de
     * pago/factura - Mantiene un pedido activo con los productos restantes - ✅
     * ACTUALIZADO: Ahora incluye todas las funciones del pago normal: •
     * Validación de caja abierta • Asignación automática al cuadre de caja
     * activo • Notificaciones WebSocket • Campos de pago consistentes
     * (totalPagado, pagadoPor, etc.)
     */
    // 🎯 Endpoint con la ruta que espera el frontend
    @PutMapping("/{id}/pagar-parcial")
    public ResponseEntity<?> pagarPedidoParcial(@PathVariable String id, @RequestBody Map<String, Object> request) {
        try {
            System.out.println("🔍 DEBUG PAGO PARCIAL - ID del pedido: " + id);
            System.out.println("🔍 DEBUG PAGO PARCIAL - Request recibido: " + request);

            // 📋 Extraer datos del request (el ID viene por PathVariable)
            // Manejar formato del frontend Flutter
            @SuppressWarnings("unchecked")
            List<String> itemIds = (List<String>) request.get("itemIds");
            String metodoPago = (String) request.get("formaPago"); // Frontend usa "formaPago"
            String procesadoPor = (String) request.get("procesadoPor");
            String notas = (String) request.get("notas");
            Object totalCalculado = request.get("totalCalculado");
            String clienteNombre = (String) request.get("clienteNombre");
            String clienteDocumento = (String) request.get("clienteDocumento");

            // Para compatibilidad con formato anterior
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> productosAPagar = (List<Map<String, Object>>) request.get("productos");
            if (metodoPago == null) {
                metodoPago = (String) request.get("metodoPago");
            }

            // Si no hay datos de cliente, usar procesadoPor como referencia
            if (clienteNombre == null && procesadoPor != null) {
                clienteNombre = "Cliente de " + procesadoPor;
            }

            System.out.println("🔍 DEBUG - Datos extraídos:");
            System.out.println("   pedidoId: " + id);
            System.out.println("   formaPago: " + metodoPago);
            System.out.println("   itemIds: " + itemIds);
            System.out.println("   procesadoPor: " + procesadoPor);
            System.out.println("   productosAPagar (legacy): " + productosAPagar);

            System.out.println("💰 Iniciando pago parcial:");
            System.out.println("   📦 Pedido origen: " + id);
            System.out.println("   💳 Método pago: " + metodoPago);
            System.out.println("   👤 Procesado por: " + procesadoPor);
            System.out.println("   📋 Items a pagar: " + (itemIds != null ? itemIds.size() : 0));
            System.out.println("   📋 Productos (legacy): " + (productosAPagar != null ? productosAPagar.size() : 0));

            // ✅ Validaciones básicas - priorizar itemIds del frontend
            boolean tieneProductos = (itemIds != null && !itemIds.isEmpty())
                    || (productosAPagar != null && !productosAPagar.isEmpty());

            if (id == null || !tieneProductos) {
                return responseService.badRequest("Datos incompletos para el pago parcial");
            }

            // 🏦 VALIDACIÓN: Verificar que hay una caja abierta antes de procesar el pago parcial
            List<CuadreCaja> cajasAbiertas = cuadreCajaRepository.findByCerradaFalse();
            if (cajasAbiertas.isEmpty()) {
                System.out.println("❌ Intento de pago parcial sin caja abierta");
                return responseService.badRequest("No se puede procesar el pago parcial sin una caja abierta");
            }

            // 🔍 Buscar pedido origen
            Optional<Pedido> pedidoOrigenOpt = thePedidoRepository.findById(id);
            if (!pedidoOrigenOpt.isPresent()) {
                return responseService.badRequest("Pedido no encontrado");
            }

            Pedido pedidoOrigen = pedidoOrigenOpt.get();
            System.out.println("✅ Pedido origen encontrado - Mesa: " + pedidoOrigen.getMesa());

            // Si productosAPagar es null pero itemIds tiene datos, construir productosAPagar
            if ((productosAPagar == null || productosAPagar.isEmpty()) && itemIds != null && !itemIds.isEmpty()) {
                productosAPagar = new ArrayList<>();
                // Buscar el pedido original para obtener los productos
                Optional<Pedido> pedidoOrigenOpt2 = thePedidoRepository.findById(id);
                if (pedidoOrigenOpt2.isPresent()) {
                    Pedido pedidoOrigen2 = pedidoOrigenOpt2.get();
                    for (String itemId : itemIds) {
                        for (ItemPedido prod : pedidoOrigen2.getItems()) {
                            if (prod.getProductoId().equals(itemId)) {
                                Map<String, Object> prodMap = new java.util.HashMap<>();
                                prodMap.put("productoId", prod.getProductoId());
                                prodMap.put("cantidad", prod.getCantidad());
                                productosAPagar.add(prodMap);
                                break;
                            }
                        }
                    }
                }
            }

            // 📦 Crear listas para productos pagados y restantes
            List<ItemPedido> productosPagados = new ArrayList<>();
            List<ItemPedido> productosRestantes = new ArrayList<>();

            // 🔄 Procesar cada producto del pedido original
            for (ItemPedido productoOriginal : pedidoOrigen.getItems()) {
                boolean encontradoEnPago = false;
                int cantidadAPagar = 0;

                // 🔍 Verificar si este producto está en la lista de productos a pagar
                for (Map<String, Object> productoPagar : productosAPagar) {
                    String productoId = (String) productoPagar.get("productoId");
                    Integer cantidad = (Integer) productoPagar.get("cantidad");

                    if (productoOriginal.getProductoId().equals(productoId)) {
                        encontradoEnPago = true;
                        cantidadAPagar = cantidad;
                        break;
                    }
                }

                if (encontradoEnPago) {
                    // 💳 Producto se va a pagar (parcial o totalmente)
                    if (cantidadAPagar >= productoOriginal.getCantidad()) {
                        // 💰 Pagar todo el producto
                        ItemPedido productoPagado = new ItemPedido();
                        productoPagado.setProductoId(productoOriginal.getProductoId());
                        productoPagado.setProductoNombre(productoOriginal.getProductoNombre());
                        productoPagado.setCantidad(productoOriginal.getCantidad());
                        productoPagado.setPrecioUnitario(productoOriginal.getPrecioUnitario());
                        productoPagado.setNotas(productoOriginal.getNotas());
                        productosPagados.add(productoPagado);

                        System.out
                                .println("   💰 Producto pagado completamente: " + productoOriginal.getProductoNombre()
                                        + " (Cantidad: " + productoOriginal.getCantidad() + ")");
                    } else {
                        // ✂️ Dividir producto: parte se paga, parte queda pendiente
                        // Crear producto pagado
                        ItemPedido productoPagado = new ItemPedido();
                        productoPagado.setProductoId(productoOriginal.getProductoId());
                        productoPagado.setProductoNombre(productoOriginal.getProductoNombre());
                        productoPagado.setCantidad(cantidadAPagar);
                        productoPagado.setPrecioUnitario(productoOriginal.getPrecioUnitario());
                        productoPagado.setNotas(productoOriginal.getNotas());
                        productosPagados.add(productoPagado);

                        // Crear producto restante
                        ItemPedido productoRestante = new ItemPedido();
                        productoRestante.setProductoId(productoOriginal.getProductoId());
                        productoRestante.setProductoNombre(productoOriginal.getProductoNombre());
                        productoRestante.setCantidad(productoOriginal.getCantidad() - cantidadAPagar);
                        productoRestante.setPrecioUnitario(productoOriginal.getPrecioUnitario());
                        productoRestante.setNotas(productoOriginal.getNotas());
                        productosRestantes.add(productoRestante);

                        System.out.println("   ✂️ Producto dividido: " + productoOriginal.getProductoNombre()
                                + " (Original: " + productoOriginal.getCantidad()
                                + ", Pagado: " + cantidadAPagar
                                + ", Restante: " + (productoOriginal.getCantidad() - cantidadAPagar) + ")");
                    }
                } else {
                    // ⏳ Producto queda pendiente (no se paga)
                    productosRestantes.add(productoOriginal);
                }
            }

            // 💰 Calcular total pagado
            double totalPagado = productosPagados.stream()
                    .mapToDouble(ItemPedido::getSubtotal)
                    .sum();

            // 🧾 Crear pedido pagado
            Pedido pedidoPagado = new Pedido();
            pedidoPagado.setMesa(pedidoOrigen.getMesa());
            pedidoPagado.setItems(productosPagados);
            pedidoPagado.setTotal(totalPagado);
            pedidoPagado.setCliente(clienteNombre);
            pedidoPagado.setFecha(pedidoOrigen.getFecha());

            // 💰 Usar el método pagar para establecer todos los campos correctamente (igual que pagarPedido)
            pedidoPagado.pagar(metodoPago, 0.0, procesadoPor); // Sin propina en pagos parciales por defecto

            // 🏦 Asignar cuadre de caja desde la creación (igual que en pagarPedido)
            if (!cajasAbiertas.isEmpty()) {
                CuadreCaja cajaActiva = cajasAbiertas.get(0);
                pedidoPagado.setCuadreCajaId(cajaActiva.get_id());
                System.out.println("🔗 Pedido pagado parcial vinculado a cuadre: " + cajaActiva.get_id());
            }

            // 💾 Guardar pedido pagado
            Pedido pedidoPagadoGuardado = thePedidoRepository.save(pedidoPagado);
            System.out.println("✅ Pedido pagado creado - ID: " + pedidoPagadoGuardado.get_id()
                    + ", Total: $" + totalPagado);

            // 🏦 Asignar al cuadre de caja activo (igual que en pagarPedido)
            if (pedidoPagadoGuardado != null) {
                boolean asignado = cuadreCajaService.asignarPedidoACuadreActivo(pedidoPagadoGuardado.get_id());
                if (!asignado) {
                    System.out.println(
                            "⚠️ Advertencia: Pedido pagado parcialmente pero no se pudo asignar a ningún cuadre activo");
                } else {
                    System.out.println("[PAGO_PARCIAL] Pedido asignado a cuadre activo correctamente");
                }

                // Notificar vía WebSocket que se pagó un pedido (para actualizar dashboard)
                try {
                    // ✅ CALCULAR TOTAL CORRECTO CON DESCUENTOS PARA WEBSOCKET
                    double totalItems = pedidoPagadoGuardado.getTotal();
                    double descuento = pedidoPagadoGuardado.getDescuento();
                    double propina = pedidoPagadoGuardado.getPropina();
                    double totalConDescuento = Math.max(totalItems - descuento, 0.0);
                    double totalFinal = totalConDescuento + propina;

                    double totalParaNotificacion = pedidoPagadoGuardado.getTotalPagado() > 0
                            ? pedidoPagadoGuardado.getTotalPagado()
                            : totalFinal;

                    webSocketService.notificarPedidoPagado(
                            pedidoPagadoGuardado.get_id(),
                            pedidoPagadoGuardado.getMesa(),
                            totalParaNotificacion,
                            pedidoPagadoGuardado.getFormaPago());
                } catch (Exception wsError) {
                    System.err.println(
                            "⚠️ Error enviando notificación WebSocket en pago parcial: " + wsError.getMessage());
                }
            }

            // 🔄 Actualizar o eliminar pedido original
            String pedidoRestanteId = null;
            if (productosRestantes.isEmpty()) {
                // 🗑️ Si no quedan productos pendientes, eliminar el pedido original
                thePedidoRepository.deleteById(id);
                System.out.println("🗑️ Pedido original eliminado (todos los productos pagados)");
            } else {
                // ✏️ Actualizar pedido original con productos restantes
                pedidoOrigen.setItems(productosRestantes);
                double totalRestante = productosRestantes.stream()
                        .mapToDouble(ItemPedido::getSubtotal)
                        .sum();
                pedidoOrigen.setTotal(totalRestante);
                pedidoOrigen.setFecha(LocalDateTime.now());
                Pedido pedidoRestanteGuardado = thePedidoRepository.save(pedidoOrigen);
                pedidoRestanteId = pedidoRestanteGuardado.get_id();
                System.out.println("✏️ Pedido original actualizado - Total restante: $" + totalRestante);
            }

            // 📄 Crear documento de pago/factura
            Map<String, Object> documentoPago = new HashMap<>();
            documentoPago.put("id", UUID.randomUUID().toString());
            documentoPago.put("tipo", "pago_parcial");
            documentoPago.put("pedidoId", pedidoPagadoGuardado.get_id());
            documentoPago.put("pedidoOriginalId", id);
            documentoPago.put("mesa", pedidoOrigen.getMesa());
            documentoPago.put("productos", productosPagados);
            documentoPago.put("subtotal", totalPagado);
            documentoPago.put("impuestos", totalPagado * 0.19); // IVA 19%
            documentoPago.put("total", totalPagado * 1.19);
            documentoPago.put("metodoPago", metodoPago);
            documentoPago.put("cliente", Map.of(
                    "nombre", clienteNombre != null ? clienteNombre : "Cliente General",
                    "documento", clienteDocumento != null ? clienteDocumento : "N/A"));
            System.out.println("🔍 DEBUG - Antes de setear fechaPago...");
            LocalDateTime fechaPago = LocalDateTime.now();
            System.out.println("🔍 DEBUG - Fecha pago creada: " + fechaPago);
            documentoPago.put("fechaPago", fechaPago);
            documentoPago.put("numeroDocumento", "DOC-" + System.currentTimeMillis());

            // 📊 Preparar respuesta completa
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("success", true);
            resultado.put("message", "Pago parcial procesado exitosamente");
            resultado.put("pedidoPagadoId", pedidoPagadoGuardado.get_id());
            resultado.put("pedidoRestanteId", pedidoRestanteId);
            resultado.put("totalPagado", totalPagado);
            resultado.put("totalConImpuestos", totalPagado * 1.19);
            resultado.put("productosProcessados", productosPagados.size());
            resultado.put("productosRestantes", productosRestantes.size());
            resultado.put("documentoPago", documentoPago);
            resultado.put("pedidoOriginalEliminado", productosRestantes.isEmpty());

            System.out.println("🎉 Pago parcial completado exitosamente");
            System.out.println("   💰 Total pagado: $" + totalPagado);
            System.out.println("   📄 Documento: " + documentoPago.get("numeroDocumento"));
            System.out.println(
                    "   🏦 Asignado a cuadre: " + (pedidoPagadoGuardado.getCuadreCajaId() != null ? "Sí" : "No"));

            return responseService.success(resultado, "Pago parcial procesado exitosamente");

        } catch (Exception e) {
            System.err.println("❌ Error en pago parcial: " + e.getMessage());
            System.err.println("❌ Tipo de error: " + e.getClass().getSimpleName());
            System.err.println("❌ Stack trace completo:");
            e.printStackTrace();
            return responseService.internalError("Error en pago parcial: " + e.getMessage());
        }
    }

    // 🔄 Endpoint de compatibilidad (mantener por si acaso)
    @PostMapping("/pago-parcial")
    public ResponseEntity<?> pagarProductosParcialCompatibilidad(@RequestBody Map<String, Object> request) {
        try {
            String pedidoId = (String) request.get("pedidoId");
            if (pedidoId == null) {
                return responseService.badRequest("pedidoId es requerido");
            }

            // Redirigir al endpoint principal
            return pagarPedidoParcial(pedidoId, request);

        } catch (Exception e) {
            return responseService.internalError("Error en pago parcial: " + e.getMessage());
        }
    }

    // 💰 MÉTODOS UTILITARIOS PARA CÁLCULO DE TOTALES

    /**
     * 💰 Recalcula el total final de un pedido aplicando descuentos Este método asegura que todos
     * los cálculos de totales sean consistentes
     * 
     * @param pedido El pedido a recalcular
     */
    private void recalcularTotalConDescuentos(Pedido pedido) {
        // Calcular total de items
        double totalItems = 0;
        if (pedido.getItems() != null && !pedido.getItems().isEmpty()) {
            totalItems = pedido.getItems().stream().mapToDouble(ItemPedido::getSubtotal).sum();
        }

        // Aplicar descuentos
        double totalConDescuento = totalItems - pedido.getDescuento();

        // Asegurar que el total no sea negativo después del descuento
        if (totalConDescuento < 0) {
            totalConDescuento = 0;
        }

        pedido.setTotal(totalConDescuento);

        System.out.println("💰 Total recalculado para pedido " + pedido.get_id() + ":");
        System.out.println("   - Total items: $" + totalItems);
        System.out.println("   - Descuento aplicado: $" + pedido.getDescuento());
        System.out.println("   - Total final: $" + totalConDescuento);
    }

    /**
     * 💰 Calcula el total final que se debe pagar incluyendo propinas
     * 
     * @param pedido El pedido
     * @return Total final a pagar (total + propina)
     */
    private double calcularTotalAPagar(Pedido pedido) {
        double totalBase = pedido.getTotal(); // Ya incluye descuentos aplicados
        double propina = pedido.isIncluyePropina() ? pedido.getPropina() : 0.0;
        return totalBase + propina;
    }

    /**
     * ✅ Calcula el total final aplicando descuentos y propinas correctamente
     * 
     * @param pedido El pedido
     * @return Total final (total - descuento + propina)
     */
    private double calcularTotalConDescuentos(Pedido pedido) {
        double totalItems = pedido.getTotal(); // Total base de items
        double descuento = pedido.getDescuento(); // Descuento aplicado
        double propina = pedido.getPropina(); // Propina añadida

        double totalConDescuento = Math.max(totalItems - descuento, 0.0); // Nunca negativo
        return totalConDescuento + propina; // Total final
    }
}
