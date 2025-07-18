package com.prog3.security.DTOs;

public class ProductoMesaRequest {

    private String productoId;
    private int cantidad;
    private double precio;
    private String notas;

    public ProductoMesaRequest() {
    }

    public ProductoMesaRequest(String productoId, int cantidad, double precio, String notas) {
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.precio = precio;
        this.notas = notas;
    }

    // Getters y Setters
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
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }
}
