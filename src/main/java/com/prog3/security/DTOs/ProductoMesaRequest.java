package com.prog3.security.DTOs;

import java.util.List;
import java.util.ArrayList;

public class ProductoMesaRequest {

    private String productoId;
    private int cantidad;
    private double precio;
    private String notas;
    private List<String> ingredientesSeleccionados; // IDs de los ingredientes seleccionados (carnes, etc.)

    public ProductoMesaRequest() {
        this.ingredientesSeleccionados = new ArrayList<>();
    }

    public ProductoMesaRequest(String productoId, int cantidad, double precio, String notas) {
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.precio = precio;
        this.notas = notas;
        this.ingredientesSeleccionados = new ArrayList<>();
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

    public List<String> getIngredientesSeleccionados() {
        return ingredientesSeleccionados;
    }

    public void setIngredientesSeleccionados(List<String> ingredientesSeleccionados) {
        this.ingredientesSeleccionados = ingredientesSeleccionados;
    }
}
