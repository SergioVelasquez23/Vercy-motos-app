package com.prog3.security.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "ajustes_inventario")
public class AjusteInventario {

    @Id
    private String _id;

    private String tipo; // AJUSTE_POSITIVO, AJUSTE_NEGATIVO, MERMA, PERDIDA, DAÑO, ROBO, CORRECCION
    private String bodegaId;
    private String usuarioId;
    private String motivo;
    private String justificacion; // Explicación detallada
    private List<ItemAjuste> items;
    private String estado; // PENDIENTE, APROBADO, RECHAZADO
    private String aprobadoPor;
    private LocalDateTime fechaAprobacion;
    private String motivoRechazo;
    private boolean requiereAprobacion;
    private LocalDateTime fecha;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    public AjusteInventario() {
        this.fecha = LocalDateTime.now();
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        this.estado = "PENDIENTE";
        this.requiereAprobacion = true;
    }

    public static class ItemAjuste {
        private String itemId;
        private String tipoItem; // producto o ingrediente
        private double cantidadAnterior;
        private double cantidadAjuste; // positivo o negativo
        private double cantidadNueva;
        private String loteId; // Opcional, si el ajuste es específico a un lote
        private double costoUnitario;
        private String observaciones;

        public ItemAjuste() {}

        public String getItemId() {
            return itemId;
        }

        public void setItemId(String itemId) {
            this.itemId = itemId;
        }

        public String getTipoItem() {
            return tipoItem;
        }

        public void setTipoItem(String tipoItem) {
            this.tipoItem = tipoItem;
        }

        public double getCantidadAnterior() {
            return cantidadAnterior;
        }

        public void setCantidadAnterior(double cantidadAnterior) {
            this.cantidadAnterior = cantidadAnterior;
        }

        public double getCantidadAjuste() {
            return cantidadAjuste;
        }

        public void setCantidadAjuste(double cantidadAjuste) {
            this.cantidadAjuste = cantidadAjuste;
        }

        public double getCantidadNueva() {
            return cantidadNueva;
        }

        public void setCantidadNueva(double cantidadNueva) {
            this.cantidadNueva = cantidadNueva;
        }

        public String getLoteId() {
            return loteId;
        }

        public void setLoteId(String loteId) {
            this.loteId = loteId;
        }

        public double getCostoUnitario() {
            return costoUnitario;
        }

        public void setCostoUnitario(double costoUnitario) {
            this.costoUnitario = costoUnitario;
        }

        public String getObservaciones() {
            return observaciones;
        }

        public void setObservaciones(String observaciones) {
            this.observaciones = observaciones;
        }

        public double getValorTotal() {
            return Math.abs(cantidadAjuste) * costoUnitario;
        }
    }

    @JsonProperty("_id")
    public String get_id() {
        return _id;
    }

    @JsonProperty("_id")
    public void set_id(String _id) {
        this._id = _id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getBodegaId() {
        return bodegaId;
    }

    public void setBodegaId(String bodegaId) {
        this.bodegaId = bodegaId;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getJustificacion() {
        return justificacion;
    }

    public void setJustificacion(String justificacion) {
        this.justificacion = justificacion;
    }

    public List<ItemAjuste> getItems() {
        return items;
    }

    public void setItems(List<ItemAjuste> items) {
        this.items = items;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getAprobadoPor() {
        return aprobadoPor;
    }

    public void setAprobadoPor(String aprobadoPor) {
        this.aprobadoPor = aprobadoPor;
    }

    public LocalDateTime getFechaAprobacion() {
        return fechaAprobacion;
    }

    public void setFechaAprobacion(LocalDateTime fechaAprobacion) {
        this.fechaAprobacion = fechaAprobacion;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public void setMotivoRechazo(String motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
    }

    public boolean isRequiereAprobacion() {
        return requiereAprobacion;
    }

    public void setRequiereAprobacion(boolean requiereAprobacion) {
        this.requiereAprobacion = requiereAprobacion;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    /**
     * Calcula el valor total del ajuste
     */
    public double getValorTotal() {
        if (items == null)
            return 0;
        return items.stream().mapToDouble(ItemAjuste::getValorTotal).sum();
    }

    @Override
    public String toString() {
        return "AjusteInventario{" + "_id='" + _id + '\'' + ", tipo='" + tipo + '\''
                + ", bodegaId='" + bodegaId + '\'' + ", estado='" + estado + '\'' + ", fecha="
                + fecha + '}';
    }
}
