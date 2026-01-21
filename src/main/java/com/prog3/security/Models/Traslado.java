package com.prog3.security.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

@Document(collection = "traslados")
public class Traslado {

    @Id
    private String _id;

    private String numero; // T1856, T1857, etc
    private String productoId;
    private String productoNombre;
    private String origenBodegaId;
    private String origenBodegaNombre;
    private String destinoBodegaId;
    private String destinoBodegaNombre;
    private double cantidad;
    private String unidad;
    private String estado; // PENDIENTE, ACEPTADO, RECHAZADO, EN_TRANSITO
    private String solicitante; // Usuario que solicita el traslado
    private String aprobador; // Usuario que aprueba/rechaza
    private String observaciones;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaAprobacion;
    private LocalDateTime fechaCompletado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    public Traslado() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        this.estado = "PENDIENTE";
    }

    @JsonProperty("_id")
    public String get_id() {
        return _id;
    }

    @JsonProperty("_id")
    public void set_id(String _id) {
        this._id = _id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getProductoId() {
        return productoId;
    }

    public void setProductoId(String productoId) {
        this.productoId = productoId;
    }

    public String getProductoNombre() {
        return productoNombre;
    }

    public void setProductoNombre(String productoNombre) {
        this.productoNombre = productoNombre;
    }

    public String getOrigenBodegaId() {
        return origenBodegaId;
    }

    public void setOrigenBodegaId(String origenBodegaId) {
        this.origenBodegaId = origenBodegaId;
    }

    public String getOrigenBodegaNombre() {
        return origenBodegaNombre;
    }

    public void setOrigenBodegaNombre(String origenBodegaNombre) {
        this.origenBodegaNombre = origenBodegaNombre;
    }

    public String getDestinoBodegaId() {
        return destinoBodegaId;
    }

    public void setDestinoBodegaId(String destinoBodegaId) {
        this.destinoBodegaId = destinoBodegaId;
    }

    public String getDestinoBodegaNombre() {
        return destinoBodegaNombre;
    }

    public void setDestinoBodegaNombre(String destinoBodegaNombre) {
        this.destinoBodegaNombre = destinoBodegaNombre;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
        this.fechaActualizacion = LocalDateTime.now();
    }

    public String getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(String solicitante) {
        this.solicitante = solicitante;
    }

    public String getAprobador() {
        return aprobador;
    }

    public void setAprobador(String aprobador) {
        this.aprobador = aprobador;
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

    public LocalDateTime getFechaAprobacion() {
        return fechaAprobacion;
    }

    public void setFechaAprobacion(LocalDateTime fechaAprobacion) {
        this.fechaAprobacion = fechaAprobacion;
    }

    public LocalDateTime getFechaCompletado() {
        return fechaCompletado;
    }

    public void setFechaCompletado(LocalDateTime fechaCompletado) {
        this.fechaCompletado = fechaCompletado;
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
}
