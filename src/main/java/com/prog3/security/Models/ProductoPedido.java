package com.prog3.security.Models;

import java.util.List;
import java.util.ArrayList;

public class ProductoPedido {

    private String productoId;
    private String nombreProducto;
    private int cantidad;
    private double precioUnitario;
    private double precioTotal;
    private String notas;
    private List<String> ingredientesSeleccionados; // IDs de los ingredientes seleccionados
    private List<IngredienteSeleccionado> detalleIngredientes; // Información detallada de los ingredientes

    public ProductoPedido() {
        this.ingredientesSeleccionados = new ArrayList<>();
        this.detalleIngredientes = new ArrayList<>();
    }

    public ProductoPedido(String productoId, String nombreProducto, int cantidad, double precioUnitario) {
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.precioTotal = cantidad * precioUnitario;
        this.ingredientesSeleccionados = new ArrayList<>();
        this.detalleIngredientes = new ArrayList<>();
    }

    // Getters y Setters
    public String getProductoId() {
        return productoId;
    }

    public void setProductoId(String productoId) {
        this.productoId = productoId;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
        this.precioTotal = cantidad * precioUnitario; // Recalcular total
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
        this.precioTotal = cantidad * precioUnitario; // Recalcular total
    }

    public double getPrecioTotal() {
        return precioTotal;
    }

    public void setPrecioTotal(double precioTotal) {
        this.precioTotal = precioTotal;
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

    public List<IngredienteSeleccionado> getDetalleIngredientes() {
        return detalleIngredientes;
    }

    public void setDetalleIngredientes(List<IngredienteSeleccionado> detalleIngredientes) {
        this.detalleIngredientes = detalleIngredientes;
    }

    // Clase interna para manejar el detalle de ingredientes seleccionados
    public static class IngredienteSeleccionado {

        private String ingredienteId;
        private String nombre;
        private String categoria;
        private double cantidadUsada;
        private String unidad;

        public IngredienteSeleccionado() {
        }

        public IngredienteSeleccionado(String ingredienteId, String nombre, String categoria, double cantidadUsada, String unidad) {
            this.ingredienteId = ingredienteId;
            this.nombre = nombre;
            this.categoria = categoria;
            this.cantidadUsada = cantidadUsada;
            this.unidad = unidad;
        }

        // Getters y Setters
        public String getIngredienteId() {
            return ingredienteId;
        }

        public void setIngredienteId(String ingredienteId) {
            this.ingredienteId = ingredienteId;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getCategoria() {
            return categoria;
        }

        public void setCategoria(String categoria) {
            this.categoria = categoria;
        }

        public double getCantidadUsada() {
            return cantidadUsada;
        }

        public void setCantidadUsada(double cantidadUsada) {
            this.cantidadUsada = cantidadUsada;
        }

        public String getUnidad() {
            return unidad;
        }

        public void setUnidad(String unidad) {
            this.unidad = unidad;
        }
    }
}
