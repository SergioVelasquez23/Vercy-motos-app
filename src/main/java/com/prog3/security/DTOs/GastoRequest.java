package com.prog3.security.DTOs;

import java.time.LocalDateTime;
import java.util.List;
import com.prog3.security.Models.ItemGasto;

public class GastoRequest {

    private String cuadreCajaId;
    private String tipoGastoId;
    private String tipoGastoNombre;
    private String concepto; // Descripción general
    private double monto;
    private String responsable;
    private LocalDateTime fechaGasto;
    private LocalDateTime fechaVencimiento; // Nueva: Fecha de vencimiento
    private String numeroRecibo;
    private String numeroFactura;
    private String proveedor;
    private String proveedorId; // Nueva: ID del proveedor
    private String formaPago;
    private boolean pagadoDesdeCaja;
    private boolean documentoSoporte; // Nueva: Toggle documento soporte

    // Items del gasto (para gastos con múltiples líneas)
    private List<ItemGasto> items;

    // Campos de cálculo
    private double subtotal;
    private double totalDescuentos;
    private double impuestos; // Legacy
    private double totalImpuestos;

    // 💰 Campos de retenciones
    private double porcentajeRetencion;
    private double porcentajeReteIva;
    private double porcentajeReteIca;

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

    // Nuevos getters y setters

    public LocalDateTime getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDateTime fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public String getProveedorId() {
        return proveedorId;
    }

    public void setProveedorId(String proveedorId) {
        this.proveedorId = proveedorId;
    }

    public boolean isDocumentoSoporte() {
        return documentoSoporte;
    }

    public void setDocumentoSoporte(boolean documentoSoporte) {
        this.documentoSoporte = documentoSoporte;
    }

    public List<ItemGasto> getItems() {
        return items;
    }

    public void setItems(List<ItemGasto> items) {
        this.items = items;
    }

    public double getTotalDescuentos() {
        return totalDescuentos;
    }

    public void setTotalDescuentos(double totalDescuentos) {
        this.totalDescuentos = totalDescuentos;
    }

    public double getTotalImpuestos() {
        return totalImpuestos;
    }

    public void setTotalImpuestos(double totalImpuestos) {
        this.totalImpuestos = totalImpuestos;
    }

    public double getPorcentajeRetencion() {
        return porcentajeRetencion;
    }

    public void setPorcentajeRetencion(double porcentajeRetencion) {
        this.porcentajeRetencion = porcentajeRetencion;
    }

    public double getPorcentajeReteIva() {
        return porcentajeReteIva;
    }

    public void setPorcentajeReteIva(double porcentajeReteIva) {
        this.porcentajeReteIva = porcentajeReteIva;
    }

    public double getPorcentajeReteIca() {
        return porcentajeReteIca;
    }

    public void setPorcentajeReteIca(double porcentajeReteIca) {
        this.porcentajeReteIca = porcentajeReteIca;
    }
}
