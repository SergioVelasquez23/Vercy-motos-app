package com.prog3.security.DTOs;

import java.time.LocalDateTime;

public class TrasladoRequestDTO {

    private String productoId;
    private String origenBodegaId;
    private String destinoBodegaId;
    private double cantidad;
    private String solicitante;
    private String observaciones;

    public TrasladoRequestDTO() {
    }

    public String getProductoId() {
        return productoId;
    }

    public void setProductoId(String productoId) {
        this.productoId = productoId;
    }

    public String getOrigenBodegaId() {
        return origenBodegaId;
    }

    public void setOrigenBodegaId(String origenBodegaId) {
        this.origenBodegaId = origenBodegaId;
    }

    public String getDestinoBodegaId() {
        return destinoBodegaId;
    }

    public void setDestinoBodegaId(String destinoBodegaId) {
        this.destinoBodegaId = destinoBodegaId;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public String getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(String solicitante) {
        this.solicitante = solicitante;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
