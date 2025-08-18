package com.prog3.security.Controllers;

import com.prog3.security.Models.AuditLog;
import com.prog3.security.Repositories.AuditLogRepository;
import com.prog3.security.Services.ResponseService;
import com.prog3.security.Utils.ApiResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("api/auditoria")
public class AuditoriaController {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private ResponseService responseService;

    /**
     * Obtiene todos los registros de auditoría (con límite)
     */
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<AuditLog>>> findAll() {
        try {
            List<AuditLog> logs = auditLogRepository.findTop50ByOrderByFechaDesc();
            return responseService.success(logs, "Registros de auditoría obtenidos exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener registros de auditoría: " + e.getMessage());
        }
    }

    /**
     * Obtiene un registro de auditoría por su ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AuditLog>> findById(@PathVariable String id) {
        try {
            return auditLogRepository.findById(id)
                    .map(log -> responseService.success(log, "Registro de auditoría obtenido exitosamente"))
                    .orElse(responseService.notFound("No se encontró el registro de auditoría con ID: " + id));
        } catch (Exception e) {
            return responseService.internalError("Error al obtener registro de auditoría: " + e.getMessage());
        }
    }

    /**
     * Busca registros por módulo del sistema
     */
    @GetMapping("/modulo/{modulo}")
    public ResponseEntity<ApiResponse<List<AuditLog>>> findByModulo(@PathVariable String modulo) {
        try {
            List<AuditLog> logs = auditLogRepository.findByModuloSistemaOrderByFechaDesc(modulo);
            return responseService.success(logs, "Registros de auditoría obtenidos por módulo: " + modulo);
        } catch (Exception e) {
            return responseService.internalError("Error al obtener registros de auditoría: " + e.getMessage());
        }
    }

    /**
     * Busca registros por usuario
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<ApiResponse<List<AuditLog>>> findByUsuario(@PathVariable String usuarioId) {
        try {
            List<AuditLog> logs = auditLogRepository.findByUsuarioIdOrderByFechaDesc(usuarioId);
            return responseService.success(logs, "Registros de auditoría obtenidos por usuario: " + usuarioId);
        } catch (Exception e) {
            return responseService.internalError("Error al obtener registros de auditoría: " + e.getMessage());
        }
    }

    /**
     * Busca registros por entidad afectada
     */
    @GetMapping("/entidad/{entidadId}")
    public ResponseEntity<ApiResponse<List<AuditLog>>> findByEntidad(@PathVariable String entidadId) {
        try {
            List<AuditLog> logs = auditLogRepository.findByEntidadIdOrderByFechaDesc(entidadId);
            return responseService.success(logs, "Registros de auditoría obtenidos por entidad: " + entidadId);
        } catch (Exception e) {
            return responseService.internalError("Error al obtener registros de auditoría: " + e.getMessage());
        }
    }

    /**
     * Busca registros por rango de fechas
     */
    @GetMapping("/fecha")
    public ResponseEntity<ApiResponse<List<AuditLog>>> findByFechaRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        try {
            List<AuditLog> logs = auditLogRepository.findByFechaBetweenOrderByFechaDesc(fechaInicio, fechaFin);
            return responseService.success(logs, "Registros de auditoría obtenidos por rango de fecha");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener registros de auditoría: " + e.getMessage());
        }
    }

    /**
     * Busca registros por módulo y tipo de acción
     */
    @GetMapping("/modulo/{modulo}/accion/{accion}")
    public ResponseEntity<ApiResponse<List<AuditLog>>> findByModuloAndAccion(
            @PathVariable String modulo,
            @PathVariable String accion) {
        try {
            List<AuditLog> logs = auditLogRepository.findByModuloSistemaAndTipoAccionOrderByFechaDesc(modulo, accion);
            return responseService.success(logs, "Registros de auditoría obtenidos por módulo y acción");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener registros de auditoría: " + e.getMessage());
        }
    }

    /**
     * Obtiene registros de auditoría paginados Ideal para Flutter para mostrar
     * listas largas con paginación
     */
    @GetMapping("/paginado")
    public ResponseEntity<ApiResponse<List<AuditLog>>> findPaginated(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanio) {
        try {
            // Implementación básica de paginación basada en skip
            List<AuditLog> logs = auditLogRepository.findAll();

            int inicio = pagina * tamanio;
            int fin = Math.min(inicio + tamanio, logs.size());

            if (inicio >= logs.size()) {
                return responseService.success(List.of(), "No hay más registros");
            }

            List<AuditLog> paginatedLogs = logs.subList(inicio, fin);
            return responseService.success(paginatedLogs, "Registros de auditoría paginados obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener registros de auditoría paginados: " + e.getMessage());
        }
    }

    /**
     * Obtiene un resumen de auditoría por módulo Útil para dashboards en
     * Flutter
     */
    @GetMapping("/resumen")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getResumen() {
        try {
            Map<String, Object> resumen = new java.util.HashMap<>();

            // Contar por módulo
            Map<String, Long> porModulo = new java.util.HashMap<>();
            List<AuditLog> todos = auditLogRepository.findAll();

            todos.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            AuditLog::getModuloSistema,
                            java.util.stream.Collectors.counting()
                    ))
                    .forEach((modulo, cantidad) -> porModulo.put(modulo, cantidad));

            // Últimas 10 actividades
            List<AuditLog> ultimas = auditLogRepository.findTop50ByOrderByFechaDesc().stream()
                    .limit(10)
                    .collect(java.util.stream.Collectors.toList());

            resumen.put("conteoModulos", porModulo);
            resumen.put("ultimasActividades", ultimas);
            resumen.put("total", todos.size());

            return responseService.success(resumen, "Resumen de auditoría obtenido exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener resumen de auditoría: " + e.getMessage());
        }
    }
}
