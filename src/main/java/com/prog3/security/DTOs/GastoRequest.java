package com.prog3.security.DTOs;

import java.time.LocalDateTime;

public class GastoRequest {

    private String cuadreCajaId;
    private String tipoGastoId;
    private String tipoGastoNombre;
    private String concepto;
    private double monto;
    private String responsable;
    private LocalDateTime fechaGasto;
    private String numeroRecibo;
    private String numeroFactura;
    private String proveedor;
    private String formaPago;
    private boolean pagadoDesdeCaja;
    private double subtotal;
    private double impuestos;

    public GastoRequest() {
    }

    // Getters y setters
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

    public boolean isPagadoDesdeCaja() {
        return pagadoDesdeCaja;
    }

    public void setPagadoDesdeCaja(boolean pagadoDesdeCaja) {
        this.pagadoDesdeCaja = pagadoDesdeCaja;
    }
}
