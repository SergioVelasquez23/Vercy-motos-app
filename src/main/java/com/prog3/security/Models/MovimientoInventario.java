package com.prog3.security.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document
public class MovimientoInventario {

    @Id
    private String _id;
    private String inventarioId; // Referencia al inventario
    private String productoId; // Referencia al producto
    private String productoNombre; // Para consultas rápidas
    private String tipoMovimiento; // entrada, salida, ajuste, merma, transferencia
    private double cantidadAnterior;
    private double cantidadMovimiento; // positivo para entradas, negativo para salidas
    private double cantidadNueva;
    private String motivo; // compra, venta, ajuste_inventario, merma, transferencia, etc.
    private String referencia; // ID del pedido, factura de compra, etc.
    private LocalDateTime fecha;
    private String responsable; // Usuario que realizó el movimiento
    private String observaciones;
    private double costoUnitario;
    private double costoTotal;
    private String proveedor; // Para entradas
    private String destino; // Para transferencias o salidas

    public MovimientoInventario() {
        this.fecha = LocalDateTime.now();
    }

    public MovimientoInventario(String inventarioId, String tipoMovimiento, double cantidadMovimiento, String motivo, String responsable) {
        this();
        this.inventarioId = inventarioId;
        this.tipoMovimiento = tipoMovimiento;
        this.cantidadMovimiento = cantidadMovimiento;
        this.motivo = motivo;
        this.responsable = responsable;
    }

    // Getters y Setters
    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getInventarioId() {
        return inventarioId;
    }

    public void setInventarioId(String inventarioId) {
        this.inventarioId = inventarioId;
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

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public double getCantidadAnterior() {
        return cantidadAnterior;
    }

    public void setCantidadAnterior(double cantidadAnterior) {
        this.cantidadAnterior = cantidadAnterior;
    }

    public double getCantidadMovimiento() {
        return cantidadMovimiento;
    }

    public void setCantidadMovimiento(double cantidadMovimiento) {
        this.cantidadMovimiento = cantidadMovimiento;
        this.cantidadNueva = this.cantidadAnterior + this.cantidadMovimiento;
    }

    public double getCantidadNueva() {
        return cantidadNueva;
    }

    public void setCantidadNueva(double cantidadNueva) {
        this.cantidadNueva = cantidadNueva;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public double getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(double costoUnitario) {
        this.costoUnitario = costoUnitario;
        this.costoTotal = Math.abs(this.cantidadMovimiento) * this.costoUnitario;
    }

    public double getCostoTotal() {
        return costoTotal;
    }

    public void setCostoTotal(double costoTotal) {
        this.costoTotal = costoTotal;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }
}
