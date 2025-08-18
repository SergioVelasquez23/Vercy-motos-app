package com.prog3.security.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.prog3.security.Models.AuditLog;
import com.prog3.security.Repositories.AuditLogRepository;

import java.time.LocalDateTime;

/**
 * Servicio para registrar acciones de auditoría en el sistema.
 */
@Service
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    /**
     * Registra una acción en el log de auditoría
     *
     * @param moduloSistema El módulo del sistema donde ocurrió la acción
     * (inventario, pedidos, caja, etc.)
     * @param tipoAccion El tipo de acción (crear, actualizar, eliminar, etc.)
     * @param entidadId El ID de la entidad afectada
     * @param descripcion Descripción detallada de la acción
     * @param usuarioId El ID del usuario que realizó la acción
     * @param valoresAnteriores Los valores anteriores (opcional)
     * @param valoresNuevos Los valores nuevos (opcional)
     */
    public AuditLog registrarAccion(String moduloSistema, String tipoAccion, String entidadId,
            String descripcion, String usuarioId, String valoresAnteriores, String valoresNuevos) {

        AuditLog auditLog = new AuditLog();
        auditLog.setModuloSistema(moduloSistema);
        auditLog.setTipoAccion(tipoAccion);
        auditLog.setEntidadId(entidadId);
        auditLog.setDescripcion(descripcion);
        auditLog.setUsuarioId(usuarioId);
        auditLog.setValoresAnteriores(valoresAnteriores);
        auditLog.setValoresNuevos(valoresNuevos);
        auditLog.setFecha(LocalDateTime.now());

        return auditLogRepository.save(auditLog);
    }

    /**
     * Registra una acción de auditoría simplificada
     */
    public AuditLog registrarAccion(String moduloSistema, String tipoAccion, String entidadId,
            String descripcion, String usuarioId) {
        return registrarAccion(moduloSistema, tipoAccion, entidadId, descripcion, usuarioId, null, null);
    }

    /**
     * Registra una acción en inventario
     */
    public AuditLog registrarAccionInventario(String tipoAccion, String productoId, String descripcion,
            String usuarioId, double stockAnterior, double stockNuevo) {
        String valoresAnteriores = "stock:" + stockAnterior;
        String valoresNuevos = "stock:" + stockNuevo;

        return registrarAccion("INVENTARIO", tipoAccion, productoId, descripcion, usuarioId,
                valoresAnteriores, valoresNuevos);
    }

    /**
     * Registra una acción en pedidos
     */
    public AuditLog registrarAccionPedidos(String tipoAccion, String pedidoId, String descripcion,
            String usuarioId, String estadoAnterior, String estadoNuevo) {
        String valoresAnteriores = estadoAnterior != null ? "estado:" + estadoAnterior : null;
        String valoresNuevos = estadoNuevo != null ? "estado:" + estadoNuevo : null;

        return registrarAccion("PEDIDOS", tipoAccion, pedidoId, descripcion, usuarioId,
                valoresAnteriores, valoresNuevos);
    }

    /**
     * Registra una acción en caja
     */
    public AuditLog registrarAccionCaja(String tipoAccion, String cajaId, String descripcion,
            String usuarioId, double montoAnterior, double montoNuevo) {
        String valoresAnteriores = "monto:" + montoAnterior;
        String valoresNuevos = "monto:" + montoNuevo;

        return registrarAccion("CAJA", tipoAccion, cajaId, descripcion, usuarioId,
                valoresAnteriores, valoresNuevos);
    }

    /**
     * Registra una acción en mesas
     */
    public AuditLog registrarAccionMesa(String tipoAccion, String mesaId, String nombreMesa,
            String descripcion, String usuarioId) {
        return registrarAccion("MESAS", tipoAccion, mesaId, descripcion, usuarioId, null, "mesa:" + nombreMesa);
    }

    /**
     * Método simplificado para registrar operación en mesa
     */
    public AuditLog logMesaOperation(String tipoAccion, String mesaId, String nombreMesa) {
        String descripcion = "Operación " + tipoAccion + " en mesa " + nombreMesa;
        String usuarioId = "sistema"; // Idealmente obtener el usuario del contexto de seguridad

        return registrarAccionMesa(tipoAccion, mesaId, nombreMesa, descripcion, usuarioId);
    }
}
