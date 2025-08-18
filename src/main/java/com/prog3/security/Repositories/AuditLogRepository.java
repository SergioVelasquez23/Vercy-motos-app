package com.prog3.security.Repositories;

import com.prog3.security.Models.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para el registro de auditoría.
 */
@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {

    /**
     * Buscar logs por módulo del sistema
     */
    List<AuditLog> findByModuloSistemaOrderByFechaDesc(String moduloSistema);

    /**
     * Buscar logs por tipo de acción
     */
    List<AuditLog> findByTipoAccionOrderByFechaDesc(String tipoAccion);

    /**
     * Buscar logs por usuario
     */
    List<AuditLog> findByUsuarioIdOrderByFechaDesc(String usuarioId);

    /**
     * Buscar logs por entidad afectada
     */
    List<AuditLog> findByEntidadIdOrderByFechaDesc(String entidadId);

    /**
     * Buscar logs por rango de fecha
     */
    List<AuditLog> findByFechaBetweenOrderByFechaDesc(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Buscar logs por módulo y tipo de acción
     */
    List<AuditLog> findByModuloSistemaAndTipoAccionOrderByFechaDesc(String moduloSistema, String tipoAccion);

    /**
     * Obtener los últimos logs
     */
    List<AuditLog> findTop50ByOrderByFechaDesc();
}
