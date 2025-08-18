package com.prog3.security.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Modelo para registros de auditoría del sistema.
 */
@Document(collection = "audit_logs")
public class AuditLog {

    @Id
    private String _id;
    private String moduloSistema;  // Módulo del sistema (inventario, pedidos, caja, etc.)
    private String tipoAccion;     // Tipo de acción (crear, actualizar, eliminar, etc.)
    private String entidadId;      // ID de la entidad afectada
    private String descripcion;    // Descripción de la acción
    private String usuarioId;      // ID del usuario que realizó la acción
    private String valoresAnteriores; // Valores anteriores (formato JSON o texto)
    private String valoresNuevos;    // Valores nuevos (formato JSON o texto)
    private LocalDateTime fecha;   // Fecha y hora de la acción

    public AuditLog() {
        this.fecha = LocalDateTime.now();
    }

    // Getters y setters
    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getModuloSistema() {
        return moduloSistema;
    }

    public void setModuloSistema(String moduloSistema) {
        this.moduloSistema = moduloSistema;
    }

    public String getTipoAccion() {
        return tipoAccion;
    }

    public void setTipoAccion(String tipoAccion) {
        this.tipoAccion = tipoAccion;
    }

    public String getEntidadId() {
        return entidadId;
    }

    public void setEntidadId(String entidadId) {
        this.entidadId = entidadId;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getValoresAnteriores() {
        return valoresAnteriores;
    }

    public void setValoresAnteriores(String valoresAnteriores) {
        this.valoresAnteriores = valoresAnteriores;
    }

    public String getValoresNuevos() {
        return valoresNuevos;
    }

    public void setValoresNuevos(String valoresNuevos) {
        this.valoresNuevos = valoresNuevos;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    @Override
    public String toString() {
        return "AuditLog{"
                + "_id='" + _id + '\''
                + ", moduloSistema='" + moduloSistema + '\''
                + ", tipoAccion='" + tipoAccion + '\''
                + ", entidadId='" + entidadId + '\''
                + ", descripcion='" + descripcion + '\''
                + ", usuarioId='" + usuarioId + '\''
                + ", fecha=" + fecha
                + '}';
    }
}
