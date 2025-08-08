package com.prog3.security.Models;

import java.util.List;
import java.util.ArrayList;

public class ItemPedido {

    private String id;
    private String productoId;
    private String productoNombre; // Nuevo campo para el nombre del producto
    private int cantidad;
    private double precioUnitario;
    private String notas;
    private boolean pagado;
    private double subtotal;
    private double precio;
    private double total;
    private List<String> ingredientesSeleccionados; // Nuevo campo para ingredientes seleccionados

    public ItemPedido() {
        this.pagado = false;
        this.ingredientesSeleccionados = new ArrayList<>();
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

    public String getProductoNombre() {
        return productoNombre;
    }

    public void setProductoNombre(String productoNombre) {
        this.productoNombre = productoNombre;
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

    public List<String> getIngredientesSeleccionados() {
        return ingredientesSeleccionados;
    }

    public void setIngredientesSeleccionados(List<String> ingredientesSeleccionados) {
        this.ingredientesSeleccionados = ingredientesSeleccionados != null ? ingredientesSeleccionados : new ArrayList<>();
    }

    private void calcularSubtotal() {
        this.subtotal = this.cantidad * this.precioUnitario;
    }
}
