package com.prog3.security.Models;

public class ItemFactura {

    private String productoId;
    private String productoNombre;
    private String codigoProducto;
    private int cantidad;
    private double precioUnitario;
    private double descuentoItem;
    private double subtotalItem;
    private double impuestoItem;
    private double totalItem;
    private String observaciones;
    private String categoria;

    public ItemFactura() {
        this.cantidad = 1;
        this.precioUnitario = 0.0;
        this.descuentoItem = 0.0;
        this.subtotalItem = 0.0;
        this.impuestoItem = 0.0;
        this.totalItem = 0.0;
    }

    public ItemFactura(String productoId, String productoNombre, int cantidad, double precioUnitario) {
        this();
        this.productoId = productoId;
        this.productoNombre = productoNombre;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        calcularTotales();
    }

    // Getters y Setters
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

    public String getCodigoProducto() {
        return codigoProducto;
    }

    public void setCodigoProducto(String codigoProducto) {
        this.codigoProducto = codigoProducto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
        calcularTotales();
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
        calcularTotales();
    }

    public double getDescuentoItem() {
        return descuentoItem;
    }

    public void setDescuentoItem(double descuentoItem) {
        this.descuentoItem = descuentoItem;
        calcularTotales();
    }

    public double getSubtotalItem() {
        return subtotalItem;
    }

    public void setSubtotalItem(double subtotalItem) {
        this.subtotalItem = subtotalItem;
    }

    public double getImpuestoItem() {
        return impuestoItem;
    }

    public void setImpuestoItem(double impuestoItem) {
        this.impuestoItem = impuestoItem;
    }

    public double getTotalItem() {
        return totalItem;
    }

    public void setTotalItem(double totalItem) {
        this.totalItem = totalItem;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    // Método para calcular totales del item
    private void calcularTotales() {
        this.subtotalItem = this.cantidad * this.precioUnitario;
        double baseImponible = this.subtotalItem - this.descuentoItem;
        this.impuestoItem = baseImponible * 0.19; // IVA 19%
        this.totalItem = baseImponible + this.impuestoItem;
    }
}
