package com.prog3.security.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "transferencias_bodegas")
public class TransferenciaBodega {

    @Id
    private String _id;

    private String bodegaOrigenId;
    private String bodegaDestinoId;
    private String usuarioId;
    private List<ItemTransferencia> items;
    private String estado; // PENDIENTE, EN_TRANSITO, COMPLETADA, RECHAZADA
    private String observaciones;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaEnvio;
    private LocalDateTime fechaRecepcion;
    private String motivoRechazo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    public TransferenciaBodega() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        this.fechaSolicitud = LocalDateTime.now();
        this.estado = "PENDIENTE";
    }

    public static class ItemTransferencia {
        private String itemId;
        private String tipoItem;
        private double cantidadSolicitada;
        private double cantidadEnviada;
        private double cantidadRecibida;
        private String observaciones;

        public ItemTransferencia() {}

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

        public double getCantidadSolicitada() {
            return cantidadSolicitada;
        }

        public void setCantidadSolicitada(double cantidadSolicitada) {
            this.cantidadSolicitada = cantidadSolicitada;
        }

        public double getCantidadEnviada() {
            return cantidadEnviada;
        }

        public void setCantidadEnviada(double cantidadEnviada) {
            this.cantidadEnviada = cantidadEnviada;
        }

        public double getCantidadRecibida() {
            return cantidadRecibida;
        }

        public void setCantidadRecibida(double cantidadRecibida) {
            this.cantidadRecibida = cantidadRecibida;
        }

        public String getObservaciones() {
            return observaciones;
        }

        public void setObservaciones(String observaciones) {
            this.observaciones = observaciones;
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

    public String getBodegaOrigenId() {
        return bodegaOrigenId;
    }

    public void setBodegaOrigenId(String bodegaOrigenId) {
        this.bodegaOrigenId = bodegaOrigenId;
    }

    public String getBodegaDestinoId() {
        return bodegaDestinoId;
    }

    public void setBodegaDestinoId(String bodegaDestinoId) {
        this.bodegaDestinoId = bodegaDestinoId;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public List<ItemTransferencia> getItems() {
        return items;
    }

    public void setItems(List<ItemTransferencia> items) {
        this.items = items;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public LocalDateTime getFechaRecepcion() {
        return fechaRecepcion;
    }

    public void setFechaRecepcion(LocalDateTime fechaRecepcion) {
        this.fechaRecepcion = fechaRecepcion;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public void setMotivoRechazo(String motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
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

    @Override
    public String toString() {
        return "TransferenciaBodega{" + "_id='" + _id + '\'' + ", bodegaOrigenId='" + bodegaOrigenId
                + '\'' + ", bodegaDestinoId='" + bodegaDestinoId + '\'' + ", estado='" + estado
                + '\'' + ", fechaSolicitud=" + fechaSolicitud + '}';
    }
}
