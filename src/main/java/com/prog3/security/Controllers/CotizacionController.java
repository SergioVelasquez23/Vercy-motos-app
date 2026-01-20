package com.prog3.security.Controllers;

import com.prog3.security.Models.Cotizacion;
import com.prog3.security.Services.CotizacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

/**
 * Controlador REST para gestionar Cotizaciones
 * Endpoints para crear, consultar, actualizar y gestionar cotizaciones
 */
@RestController
@RequestMapping("/api/cotizaciones")
public class CotizacionController {

    @Autowired
    private CotizacionService cotizacionService;

    /**
     * Obtener todas las cotizaciones
     * GET /api/cotizaciones
     */
    @GetMapping
    public ResponseEntity<?> obtenerTodasLasCotizaciones() {
        try {
            List<Cotizacion> cotizaciones = cotizacionService.obtenerTodasLasCotizaciones();
            return ResponseEntity.ok(cotizaciones);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener cotizaciones: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Obtener cotización por ID
     * GET /api/cotizaciones/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerCotizacionPorId(@PathVariable String id) {
        try {
            Optional<Cotizacion> cotizacion = cotizacionService.obtenerCotizacionPorId(id);
            
            if (cotizacion.isPresent()) {
                return ResponseEntity.ok(cotizacion.get());
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Cotización no encontrada");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener cotización: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Obtener cotización por número
     * GET /api/cotizaciones/numero/{numeroCotizacion}
     */
    @GetMapping("/numero/{numeroCotizacion}")
    public ResponseEntity<?> obtenerCotizacionPorNumero(@PathVariable String numeroCotizacion) {
        try {
            Optional<Cotizacion> cotizacion = cotizacionService.obtenerCotizacionPorNumero(numeroCotizacion);
            
            if (cotizacion.isPresent()) {
                return ResponseEntity.ok(cotizacion.get());
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Cotización no encontrada");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener cotización: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Crear nueva cotización
     * POST /api/cotizaciones
     * Body: { "clienteId": "123", "clienteNombre": "Juan", "items": [...], ... }
     */
    @PostMapping
    public ResponseEntity<?> crearCotizacion(
            @RequestBody Cotizacion cotizacion,
            @RequestHeader(value = "X-Usuario-Id", required = false, defaultValue = "sistema") String usuarioId) {
        try {
            // Validar items
            if (cotizacion.getItems() == null || cotizacion.getItems().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "La cotización debe tener al menos un item");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Validar cliente
            if (cotizacion.getClienteId() == null || cotizacion.getClienteId().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "El ID del cliente es requerido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            Cotizacion nuevaCotizacion = cotizacionService.crearCotizacion(cotizacion, usuarioId);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaCotizacion);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al crear cotización: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Actualizar cotización existente
     * PUT /api/cotizaciones/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarCotizacion(
            @PathVariable String id,
            @RequestBody Cotizacion cotizacion,
            @RequestHeader(value = "X-Usuario-Id", required = false, defaultValue = "sistema") String usuarioId) {
        try {
            Cotizacion cotizacionActualizada = cotizacionService.actualizarCotizacion(id, cotizacion, usuarioId);
            return ResponseEntity.ok(cotizacionActualizada);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al actualizar cotización: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Calcular totales sin guardar (para preview)
     * POST /api/cotizaciones/calcular-totales
     */
    @PostMapping("/calcular-totales")
    public ResponseEntity<?> calcularTotales(@RequestBody Cotizacion cotizacion) {
        try {
            Map<String, Object> totales = cotizacionService.calcularTotalesSinGuardar(cotizacion);
            return ResponseEntity.ok(totales);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al calcular totales: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Eliminar cotización
     * DELETE /api/cotizaciones/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarCotizacion(@PathVariable String id) {
        try {
            boolean eliminada = cotizacionService.eliminarCotizacion(id);
            
            if (eliminada) {
                Map<String, String> respuesta = new HashMap<>();
                respuesta.put("mensaje", "Cotización eliminada exitosamente");
                return ResponseEntity.ok(respuesta);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Cotización no encontrada");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al eliminar cotización: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Obtener cotizaciones por cliente
     * GET /api/cotizaciones/cliente/{clienteId}
     */
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<?> obtenerCotizacionesPorCliente(@PathVariable String clienteId) {
        try {
            List<Cotizacion> cotizaciones = cotizacionService.obtenerCotizacionesPorCliente(clienteId);
            return ResponseEntity.ok(cotizaciones);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener cotizaciones: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Obtener cotizaciones por estado
     * GET /api/cotizaciones/estado/{estado}
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<?> obtenerCotizacionesPorEstado(@PathVariable String estado) {
        try {
            List<Cotizacion> cotizaciones = cotizacionService.obtenerCotizacionesPorEstado(estado);
            return ResponseEntity.ok(cotizaciones);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener cotizaciones: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Aceptar cotización
     * PUT /api/cotizaciones/{id}/aceptar
     */
    @PutMapping("/{id}/aceptar")
    public ResponseEntity<?> aceptarCotizacion(
            @PathVariable String id,
            @RequestHeader(value = "X-Usuario-Id", required = false, defaultValue = "sistema") String usuarioId) {
        try {
            Cotizacion cotizacion = cotizacionService.aceptarCotizacion(id, usuarioId);
            return ResponseEntity.ok(cotizacion);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al aceptar cotización: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Rechazar cotización
     * PUT /api/cotizaciones/{id}/rechazar
     */
    @PutMapping("/{id}/rechazar")
    public ResponseEntity<?> rechazarCotizacion(
            @PathVariable String id,
            @RequestHeader(value = "X-Usuario-Id", required = false, defaultValue = "sistema") String usuarioId) {
        try {
            Cotizacion cotizacion = cotizacionService.rechazarCotizacion(id, usuarioId);
            return ResponseEntity.ok(cotizacion);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al rechazar cotización: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Convertir cotización a factura
     * POST /api/cotizaciones/{id}/convertir-factura
     * Body: { "facturaId": "FAC-123" }
     */
    @PostMapping("/{id}/convertir-factura")
    public ResponseEntity<?> convertirAFactura(
            @PathVariable String id,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-Usuario-Id", required = false, defaultValue = "sistema") String usuarioId) {
        try {
            String facturaId = body.get("facturaId");
            
            if (facturaId == null || facturaId.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "El ID de la factura es requerido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            Cotizacion cotizacion = cotizacionService.convertirAFactura(id, facturaId, usuarioId);
            return ResponseEntity.ok(cotizacion);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al convertir cotización: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Actualizar cotizaciones vencidas
     * POST /api/cotizaciones/actualizar-vencidas
     */
    @PostMapping("/actualizar-vencidas")
    public ResponseEntity<?> actualizarCotizacionesVencidas() {
        try {
            int cotizacionesActualizadas = cotizacionService.marcarCotizacionesVencidas();
            
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Cotizaciones vencidas actualizadas");
            respuesta.put("cantidad", cotizacionesActualizadas);
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al actualizar cotizaciones vencidas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Obtener estadísticas de cotizaciones
     * GET /api/cotizaciones/estadisticas
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<?> obtenerEstadisticas() {
        try {
            Map<String, Object> estadisticas = cotizacionService.obtenerEstadisticas();
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener estadísticas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Buscar cotizaciones por rango de fechas
     * GET /api/cotizaciones/buscar-por-fechas?inicio=2024-01-01&fin=2024-12-31
     */
    @GetMapping("/buscar-por-fechas")
    public ResponseEntity<?> buscarPorRangoFechas(
            @RequestParam String inicio,
            @RequestParam String fin) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LocalDateTime fechaInicio = LocalDateTime.parse(inicio + "T00:00:00", formatter);
            LocalDateTime fechaFin = LocalDateTime.parse(fin + "T23:59:59", formatter);
            
            List<Cotizacion> cotizaciones = cotizacionService.buscarPorRangoFechas(fechaInicio, fechaFin);
            return ResponseEntity.ok(cotizaciones);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al buscar cotizaciones: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
