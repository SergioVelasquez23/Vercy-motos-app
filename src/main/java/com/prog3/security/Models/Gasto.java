package com.prog3.security.Models;

import java.time.LocalDateTime;

public class Gasto {

    private String _id;
    private String cuadreCajaId;  // ID del cuadre de caja al que pertenece
    private String tipoGastoId;   // ID del tipo de gasto
    private String tipoGastoNombre; // Nombre del tipo de gasto (para no tener que hacer join)
    private String concepto;      // Descripción del gasto
    private double monto;         // Monto del gasto
    private String responsable;   // Responsable del gasto
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaGasto;
    private String numeroRecibo;
    private String numeroFactura;
    private String proveedor;
    private String formaPago;     // "efectivo", "transferencia", etc.
    private boolean pagadoDesdeCaja; // Indica si el gasto fue pagado desde la caja (descuenta del efectivo)
    private double subtotal;      // Valor antes de impuestos
    private double impuestos;     // Valor de impuestos
    private String estado;        // "pendiente", "aprobado", "rechazado"

    // Constructor vacío
    public Gasto() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaGasto = LocalDateTime.now();
        this.estado = "pendiente";
        this.formaPago = "efectivo"; // Por defecto
        this.pagadoDesdeCaja = false; // Por defecto no se paga desde caja
    }

    // Constructor con campos básicos
    public Gasto(String cuadreCajaId, String tipoGastoId, String tipoGastoNombre, String concepto, double monto,
            String responsable) {
        this();
        this.cuadreCajaId = cuadreCajaId;
        this.tipoGastoId = tipoGastoId;
        this.tipoGastoNombre = tipoGastoNombre;
        this.concepto = concepto;
        this.monto = monto;
        this.responsable = responsable;
        this.subtotal = monto; // Por defecto el subtotal es igual al monto
    }

    // Getters y setters
    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getCuadreCajaId() {
        return cuadreCajaId;
    }

    public void setCuadreCajaId(String cuadreCajaId) {
        this.cuadreCajaId = cuadreCajaId;
    }

    public String getTipoGastoId() {
        return tipoGastoId;
    }

    public void setTipoGastoId(String tipoGastoId) {
        this.tipoGastoId = tipoGastoId;
    }

    public String getTipoGastoNombre() {
        return tipoGastoNombre;
    }

    public void setTipoGastoNombre(String tipoGastoNombre) {
        this.tipoGastoNombre = tipoGastoNombre;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaGasto() {
        return fechaGasto;
    }

    public void setFechaGasto(LocalDateTime fechaGasto) {
        this.fechaGasto = fechaGasto;
    }

    public String getNumeroRecibo() {
        return numeroRecibo;
    }

    public void setNumeroRecibo(String numeroRecibo) {
        this.numeroRecibo = numeroRecibo;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(String numeroFactura) {
        this.numeroFactura = numeroFactura;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    public String getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(String formaPago) {
        this.formaPago = formaPago;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getImpuestos() {
        return impuestos;
    }

    public void setImpuestos(double impuestos) {
        this.impuestos = impuestos;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public boolean isPagadoDesdeCaja() {
        return pagadoDesdeCaja;
    }

    public void setPagadoDesdeCaja(boolean pagadoDesdeCaja) {
        this.pagadoDesdeCaja = pagadoDesdeCaja;
    }
}
