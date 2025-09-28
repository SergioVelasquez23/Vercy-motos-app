package com.prog3.security.Controllers;

import com.prog3.security.Models.Deuda;
import com.prog3.security.Models.PagoDeuda;
import com.prog3.security.Services.DeudaService;
import com.prog3.security.Services.ResponseService;
import com.prog3.security.Utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("api/deudas")
public class DeudaController {

    @Autowired
    private DeudaService deudaService;

    @Autowired
    private ResponseService responseService;

    /**
     * POST /api/deudas - Crear deuda
     */
    @PostMapping("")
    public ResponseEntity<ApiResponse<Deuda>> crearDeuda(@RequestBody Deuda deuda) {
        try {
            Deuda nuevaDeuda = deudaService.crearDeuda(deuda);
            return responseService.success(nuevaDeuda, "Deuda creada exitosamente");
        } catch (IllegalArgumentException e) {
            return responseService.badRequest("Datos inválidos: " + e.getMessage());
        } catch (RuntimeException e) {
            return responseService.badRequest("Error: " + e.getMessage());
        } catch (Exception e) {
            return responseService.internalError("Error al crear deuda: " + e.getMessage());
        }
    }

    /**
     * GET /api/deudas - Obtener deudas
     */
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<Deuda>>> obtenerDeudas(
            @RequestParam(required = false, defaultValue = "false") boolean soloActivas,
            @RequestParam(required = false) String mesaId,
            @RequestParam(required = false) String clienteInfo,
            @RequestParam(required = false) String pedidoId) {
        try {
            List<Deuda> deudas;

            if (pedidoId != null) {
                Optional<Deuda> deuda = deudaService.obtenerDeudaPorPedido(pedidoId);
                deudas = deuda.isPresent() ? List.of(deuda.get()) : List.of();
            } else if (mesaId != null) {
                deudas = soloActivas ? 
                    deudaService.obtenerDeudasActivasPorMesa(mesaId) : 
                    deudaService.obtenerDeudasPorMesa(mesaId);
            } else if (clienteInfo != null) {
                deudas = deudaService.buscarDeudasPorCliente(clienteInfo);
            } else if (soloActivas) {
                deudas = deudaService.obtenerDeudasActivas();
            } else {
                deudas = deudaService.obtenerDeudasRecientes();
            }

            return responseService.success(deudas, "Deudas obtenidas exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener deudas: " + e.getMessage());
        }
    }

    /**
     * GET /api/deudas/{id} - Obtener deuda específica
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Deuda>> obtenerDeudaPorId(@PathVariable String id) {
        try {
            Optional<Deuda> deuda = deudaService.obtenerDeudaPorId(id);
            if (deuda.isPresent()) {
                return responseService.success(deuda.get(), "Deuda encontrada");
            } else {
                return responseService.notFound("Deuda no encontrada");
            }
        } catch (Exception e) {
            return responseService.internalError("Error al obtener deuda: " + e.getMessage());
        }
    }

    /**
     * POST /api/deudas/{id}/pagar - Registrar pago de deuda
     */
    @PostMapping("/{id}/pagar")
    public ResponseEntity<ApiResponse<Deuda>> registrarPago(@PathVariable String id, @RequestBody PagoDeuda pago) {
        try {
            Deuda deudaActualizada = deudaService.registrarPago(id, pago);
            return responseService.success(deudaActualizada, "Pago registrado exitosamente");
        } catch (IllegalArgumentException e) {
            return responseService.badRequest("Datos inválidos: " + e.getMessage());
        } catch (RuntimeException e) {
            return responseService.badRequest("Error: " + e.getMessage());
        } catch (Exception e) {
            return responseService.internalError("Error al registrar pago: " + e.getMessage());
        }
    }

    /**
     * GET /api/deudas/{id}/pagos - Obtener pagos de una deuda
     */
    @GetMapping("/{id}/pagos")
    public ResponseEntity<ApiResponse<List<PagoDeuda>>> obtenerPagosDeuda(@PathVariable String id) {
        try {
            List<PagoDeuda> pagos = deudaService.obtenerPagosDeuda(id);
            return responseService.success(pagos, "Pagos obtenidos exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener pagos: " + e.getMessage());
        }
    }

    /**
     * GET /api/deudas/estadisticas - Estadísticas de deudas
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<ApiResponse<Map<String, Object>>> obtenerEstadisticas() {
        try {
            Map<String, Object> estadisticas = deudaService.obtenerEstadisticasDeudas();
            return responseService.success(estadisticas, "Estadísticas obtenidas exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener estadísticas: " + e.getMessage());
        }
    }

    /**
     * GET /api/deudas/activas - Obtener solo deudas activas
     */
    @GetMapping("/activas")
    public ResponseEntity<ApiResponse<List<Deuda>>> obtenerDeudasActivas() {
        try {
            List<Deuda> deudasActivas = deudaService.obtenerDeudasActivas();
            return responseService.success(deudasActivas, "Deudas activas obtenidas exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener deudas activas: " + e.getMessage());
        }
    }

    /**
     * GET /api/deudas/vencidas - Obtener deudas vencidas
     */
    @GetMapping("/vencidas")
    public ResponseEntity<ApiResponse<List<Deuda>>> obtenerDeudasVencidas() {
        try {
            List<Deuda> deudasVencidas = deudaService.obtenerDeudasVencidas();
            return responseService.success(deudasVencidas, "Deudas vencidas obtenidas exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener deudas vencidas: " + e.getMessage());
        }
    }

    /**
     * GET /api/deudas/fecha - Obtener deudas por rango de fechas
     */
    @GetMapping("/fecha")
    public ResponseEntity<ApiResponse<List<Deuda>>> obtenerDeudasPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        try {
            List<Deuda> deudas = deudaService.obtenerDeudasPorFecha(fechaInicio, fechaFin);
            return responseService.success(deudas, "Deudas obtenidas exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener deudas por fecha: " + e.getMessage());
        }
    }

    /**
     * PUT /api/deudas/{id} - Actualizar deuda
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Deuda>> actualizarDeuda(@PathVariable String id, @RequestBody Deuda deudaActualizada) {
        try {
            Deuda deuda = deudaService.actualizarDeuda(id, deudaActualizada);
            return responseService.success(deuda, "Deuda actualizada exitosamente");
        } catch (RuntimeException e) {
            return responseService.badRequest("Error al actualizar: " + e.getMessage());
        } catch (Exception e) {
            return responseService.internalError("Error al actualizar deuda: " + e.getMessage());
        }
    }

    /**
     * PUT /api/deudas/{id}/estado - Cambiar estado de deuda (activar/desactivar)
     */
    @PutMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<Deuda>> cambiarEstadoDeuda(
            @PathVariable String id, 
            @RequestParam boolean activa,
            @RequestParam String modificadoPor) {
        try {
            Deuda deuda = deudaService.cambiarEstadoDeuda(id, activa, modificadoPor);
            String mensaje = activa ? "Deuda activada exitosamente" : "Deuda desactivada exitosamente";
            return responseService.success(deuda, mensaje);
        } catch (RuntimeException e) {
            return responseService.badRequest("Error al cambiar estado: " + e.getMessage());
        } catch (Exception e) {
            return responseService.internalError("Error al cambiar estado de deuda: " + e.getMessage());
        }
    }

    /**
     * POST /api/deudas/{id}/recalcular - Recalcular monto de deuda
     */
    @PostMapping("/{id}/recalcular")
    public ResponseEntity<ApiResponse<Deuda>> recalcularDeuda(@PathVariable String id) {
        try {
            Deuda deuda = deudaService.recalcularDeuda(id);
            return responseService.success(deuda, "Deuda recalculada exitosamente");
        } catch (RuntimeException e) {
            return responseService.badRequest("Error al recalcular: " + e.getMessage());
        } catch (Exception e) {
            return responseService.internalError("Error al recalcular deuda: " + e.getMessage());
        }
    }

    /**
     * DELETE /api/deudas/{id} - Eliminar deuda (solo si no tiene pagos)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> eliminarDeuda(@PathVariable String id) {
        try {
            boolean eliminado = deudaService.eliminarDeuda(id);
            if (eliminado) {
                return responseService.success("Deuda eliminada", "Deuda eliminada exitosamente");
            } else {
                return responseService.badRequest("No se pudo eliminar la deuda");
            }
        } catch (Exception e) {
            return responseService.internalError("Error al eliminar deuda: " + e.getMessage());
        }
    }
}