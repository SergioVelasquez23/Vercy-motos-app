package com.prog3.security.Models;

public class ItemPedido {

    private String id;
    private String productoId;
    private int cantidad;
    private double precioUnitario;
    private String notas;
    private boolean pagado;
    private double subtotal;
    private double precio;
    private double total;

    public ItemPedido() {
        this.pagado = false;
    }

    public ItemPedido(String productoId, int cantidad, double precioUnitario) {
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.pagado = false;
        this.calcularSubtotal();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductoId() {
        return productoId;
    }

    public void setProductoId(String productoId) {
        this.productoId = productoId;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
        this.calcularSubtotal();
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
        this.calcularSubtotal();
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
        this.total = precio * cantidad;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public boolean isPagado() {
        return pagado;
    }

    public void setPagado(boolean pagado) {
        this.pagado = pagado;
    }

    public double getSubtotal() {
        return subtotal;
    }

    private void calcularSubtotal() {
        this.subtotal = this.cantidad * this.precioUnitario;
    }
}
