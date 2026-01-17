package com.prog3.security.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "lotes")
public class Lote {

    @Id
    private String _id;

    private String codigo; // Código del lote (ej: LOTE-2024-001)
    private String itemId; // producto o ingrediente
    private String tipoItem; // "producto" o "ingrediente"
    private String bodegaId;
    private double cantidadInicial;
    private double cantidadActual;
    private LocalDate fechaIngreso;
    private LocalDate fechaFabricacion;
    private LocalDate fechaVencimiento;
    private String proveedor;
    private String factura;
    private double costoUnitario;
    private String estado; // ACTIVO, AGOTADO, VENCIDO, RETIRADO
    private String observaciones;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    public Lote() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        this.fechaIngreso = LocalDate.now();
        this.estado = "ACTIVO";
    }

    @JsonProperty("_id")
    public String get_id() {
        return _id;
    }

    @JsonProperty("_id")
    public void set_id(String _id) {
        this._id = _id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

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

    public String getBodegaId() {
        return bodegaId;
    }

    public void setBodegaId(String bodegaId) {
        this.bodegaId = bodegaId;
    }

    public double getCantidadInicial() {
        return cantidadInicial;
    }

    public void setCantidadInicial(double cantidadInicial) {
        this.cantidadInicial = cantidadInicial;
    }

    public double getCantidadActual() {
        return cantidadActual;
    }

    public void setCantidadActual(double cantidadActual) {
        this.cantidadActual = cantidadActual;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public LocalDate getFechaFabricacion() {
        return fechaFabricacion;
    }

    public void setFechaFabricacion(LocalDate fechaFabricacion) {
        this.fechaFabricacion = fechaFabricacion;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    public String getFactura() {
        return factura;
    }

    public void setFactura(String factura) {
        this.factura = factura;
    }

    public double getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(double costoUnitario) {
        this.costoUnitario = costoUnitario;
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
     * Verifica si el lote está vencido
     */
    public boolean estaVencido() {
        return fechaVencimiento != null && LocalDate.now().isAfter(fechaVencimiento);
    }

    /**
     * Verifica si el lote está próximo a vencer (30 días o menos)
     */
    public boolean estaPorVencer() {
        if (fechaVencimiento == null)
            return false;
        return LocalDate.now().plusDays(30).isAfter(fechaVencimiento)
                && !LocalDate.now().isAfter(fechaVencimiento);
    }

    /**
     * Obtiene días hasta el vencimiento
     */
    public long diasParaVencimiento() {
        if (fechaVencimiento == null)
            return Long.MAX_VALUE;
        return LocalDate.now().until(fechaVencimiento).getDays();
    }

    @Override
    public String toString() {
        return "Lote{" + "_id='" + _id + '\'' + ", codigo='" + codigo + '\'' + ", itemId='" + itemId
                + '\'' + ", cantidadActual=" + cantidadActual + ", fechaVencimiento="
                + fechaVencimiento + ", estado='" + estado + '\'' + '}';
    }
}
