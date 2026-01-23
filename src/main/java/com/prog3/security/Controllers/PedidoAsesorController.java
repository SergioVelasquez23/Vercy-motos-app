package com.prog3.security.Controllers;

import com.prog3.security.Models.PedidoAsesor;
import com.prog3.security.Models.PedidoAsesor.EstadoPedidoAsesor;
import com.prog3.security.Services.PedidoAsesorService;
import com.prog3.security.Services.ResponseService;
import com.prog3.security.Exception.BusinessException;
import com.prog3.security.Exception.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("api/pedidos-asesor")
@Tag(name = "Pedidos Asesor", description = "Gestión de pedidos creados por asesores comerciales")
public class PedidoAsesorController {

    @Autowired
    private PedidoAsesorService pedidoAsesorService;

    @Autowired
    private ResponseService responseService;

    /**
     * 📝 Crear un nuevo pedido de asesor POST /api/pedidos-asesor
     */
    @PostMapping
    @Operation(summary = "Crear pedido de asesor",
            description = "Crea un nuevo pedido con los datos del cliente y los items solicitados")
    @ApiResponses(
            value = {@ApiResponse(responseCode = "200", description = "Pedido creado exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")})
    public ResponseEntity<?> crearPedido(@Valid @RequestBody PedidoAsesor pedido) {
        try {
            PedidoAsesor pedidoCreado = pedidoAsesorService.crearPedido(pedido);
            return ResponseEntity.ok(pedidoCreado);
        } catch (BusinessException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message",
                    "Error al crear el pedido: " + e.getMessage()));
        }
    }

    /**
     * 📋 Listar todos los pedidos GET /api/pedidos-asesor
     */
    @GetMapping
    @Operation(summary = "Listar pedidos de asesor",
            description = "Obtiene lista de pedidos. Puede filtrar por estado o asesorId usando query params")
    public ResponseEntity<?> listarPedidos(@RequestParam(required = false) @Parameter(
            description = "Filtrar por estado (PENDIENTE, FACTURADO, CANCELADO)") String estado,
            @RequestParam(required = false) @Parameter(
                    description = "Filtrar por ID del asesor") String asesorId) {
        try {
            List<PedidoAsesor> pedidos;

            // Filtrar por asesor y estado
            if (asesorId != null && estado != null) {
                EstadoPedidoAsesor estadoEnum = EstadoPedidoAsesor.valueOf(estado.toUpperCase());
                pedidos = pedidoAsesorService.listarPorAsesorYEstado(asesorId, estadoEnum);
            }
            // Filtrar solo por asesor
            else if (asesorId != null) {
                pedidos = pedidoAsesorService.listarPorAsesor(asesorId);
            }
            // Filtrar solo por estado
            else if (estado != null) {
                EstadoPedidoAsesor estadoEnum = EstadoPedidoAsesor.valueOf(estado.toUpperCase());
                pedidos = pedidoAsesorService.listarPorEstado(estadoEnum);
            }
            // Sin filtros - todos
            else {
                pedidos = pedidoAsesorService.listarTodos();
            }

            return ResponseEntity.ok(pedidos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message",
                    "Estado inválido. Use: PENDIENTE, FACTURADO o CANCELADO"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message",
                    "Error al listar pedidos: " + e.getMessage()));
        }
    }

    /**
     * 🔍 Obtener un pedido por ID GET /api/pedidos-asesor/:id
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener pedido por ID",
            description = "Obtiene los detalles completos de un pedido específico")
    public ResponseEntity<?> obtenerPedido(@PathVariable String id) {
        try {
            PedidoAsesor pedido = pedidoAsesorService.obtenerPedido(id);
            return ResponseEntity.ok(pedido);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message",
                    "Error al obtener el pedido: " + e.getMessage()));
        }
    }

    /**
     * ✅ Marcar pedido como facturado PUT /api/pedidos-asesor/:id/facturar
     */
    @PutMapping("/{id}/facturar")
    @Operation(summary = "Facturar pedido",
            description = "Marca un pedido como facturado y asocia el ID de la factura")
    public ResponseEntity<?> facturarPedido(@PathVariable String id,
            @RequestBody Map<String, String> body) {
        try {
            String facturaId = body.get("facturaId");
            String facturadoPor = body.get("facturadoPor");

            if (facturaId == null || facturaId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        Map.of("success", false, "message", "El ID de factura es obligatorio"));
            }

            if (facturadoPor == null || facturadoPor.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message",
                        "El nombre del facturador es obligatorio"));
            }

            PedidoAsesor pedido = pedidoAsesorService.facturarPedido(id, facturaId, facturadoPor);
            return ResponseEntity.ok(pedido);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message",
                    "Error al facturar el pedido: " + e.getMessage()));
        }
    }

    /**
     * ❌ Cancelar pedido PUT /api/pedidos-asesor/:id/cancelar
     */
    @PutMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar pedido", description = "Cancela un pedido pendiente")
    public ResponseEntity<?> cancelarPedido(@PathVariable String id,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            String usuario = body != null ? body.get("usuario") : "Sistema";
            String motivo = body != null ? body.get("motivo") : "No especificado";

            PedidoAsesor pedido = pedidoAsesorService.cancelarPedido(id, usuario, motivo);
            return ResponseEntity.ok(pedido);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message",
                    "Error al cancelar el pedido: " + e.getMessage()));
        }
    }

    /**
     * ✏️ Actualizar pedido PUT /api/pedidos-asesor/:id
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar pedido",
            description = "Actualiza un pedido pendiente (solo cliente, items y observaciones)")
    public ResponseEntity<?> actualizarPedido(@PathVariable String id,
            @RequestBody PedidoAsesor pedidoActualizado,
            @RequestParam(required = false, defaultValue = "Sistema") String usuario) {
        try {
            PedidoAsesor pedido =
                    pedidoAsesorService.actualizarPedido(id, pedidoActualizado, usuario);
            return ResponseEntity.ok(pedido);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message",
                    "Error al actualizar el pedido: " + e.getMessage()));
        }
    }

    /**
     * 🗑️ Eliminar pedido DELETE /api/pedidos-asesor/:id
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar pedido",
            description = "Elimina un pedido (solo si NO está facturado)")
    public ResponseEntity<?> eliminarPedido(@PathVariable String id) {
        try {
            pedidoAsesorService.eliminarPedido(id);
            return ResponseEntity
                    .ok(Map.of("success", true, "message", "Pedido eliminado correctamente"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message",
                    "Error al eliminar el pedido: " + e.getMessage()));
        }
    }

    /**
     * 📊 Obtener estadísticas de pedidos GET /api/pedidos-asesor/estadisticas
     */
    @GetMapping("/estadisticas")
    @Operation(summary = "Estadísticas de pedidos",
            description = "Obtiene estadísticas generales de pedidos de asesor")
    public ResponseEntity<?> obtenerEstadisticas() {
        try {
            PedidoAsesorService.EstadisticasPedidos stats =
                    pedidoAsesorService.obtenerEstadisticas();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message",
                    "Error al obtener estadísticas: " + e.getMessage()));
        }
    }
}
