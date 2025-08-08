package com.prog3.security.DTOs;

import java.util.List;
import java.util.ArrayList;

/**
 * DTO para manejar la cancelación de productos con selección de ingredientes a
 * devolver
 */
public class CancelarProductoRequest {

    private String pedidoId;
    private String productoId;
    private int cantidadACancelar;
    private String motivoCancelacion;
    private String canceladoPor;
    private List<IngredienteADevolver> ingredientesADevolver;
    private String notas;

    public CancelarProductoRequest() {
        this.ingredientesADevolver = new ArrayList<>();
    }

    // Getters y Setters
    public String getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(String pedidoId) {
        this.pedidoId = pedidoId;
    }

    public String getProductoId() {
        return productoId;
    }

    public void setProductoId(String productoId) {
        this.productoId = productoId;
    }

    public int getCantidadACancelar() {
        return cantidadACancelar;
    }

    public void setCantidadACancelar(int cantidadACancelar) {
        this.cantidadACancelar = cantidadACancelar;
    }

    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    public void setMotivoCancelacion(String motivoCancelacion) {
        this.motivoCancelacion = motivoCancelacion;
    }

    public String getCanceladoPor() {
        return canceladoPor;
    }

    public void setCanceladoPor(String canceladoPor) {
        this.canceladoPor = canceladoPor;
    }

    public List<IngredienteADevolver> getIngredientesADevolver() {
        return ingredientesADevolver;
    }

    public void setIngredientesADevolver(List<IngredienteADevolver> ingredientesADevolver) {
        this.ingredientesADevolver = ingredientesADevolver;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    /**
     * Clase interna para representar un ingrediente que se puede devolver al
     * inventario
     */
    public static class IngredienteADevolver {

        private String ingredienteId;
        private String nombreIngrediente;
        private double cantidadOriginal; // Cantidad que se había descontado
        private double cantidadADevolver; // Cantidad que se va a devolver (puede ser menor)
        private String unidad;
        private boolean devolver; // Si el usuario decidió devolverlo o no
        private String motivoNoDevolucion; // Por qué no se devuelve (ej: "ya fue cocinado")

        public IngredienteADevolver() {
        }

        public IngredienteADevolver(String ingredienteId, String nombreIngrediente,
                double cantidadOriginal, String unidad) {
            this.ingredienteId = ingredienteId;
            this.nombreIngrediente = nombreIngrediente;
            this.cantidadOriginal = cantidadOriginal;
            this.cantidadADevolver = cantidadOriginal; // Por defecto, devolver todo
            this.unidad = unidad;
            this.devolver = true; // Por defecto, sí devolver
        }

        // Getters y Setters
        public String getIngredienteId() {
            return ingredienteId;
        }

        public void setIngredienteId(String ingredienteId) {
            this.ingredienteId = ingredienteId;
        }

        public String getNombreIngrediente() {
            return nombreIngrediente;
        }

        public void setNombreIngrediente(String nombreIngrediente) {
            this.nombreIngrediente = nombreIngrediente;
        }

        public double getCantidadOriginal() {
            return cantidadOriginal;
        }

        public void setCantidadOriginal(double cantidadOriginal) {
            this.cantidadOriginal = cantidadOriginal;
        }

        public double getCantidadADevolver() {
            return cantidadADevolver;
        }

        public void setCantidadADevolver(double cantidadADevolver) {
            this.cantidadADevolver = cantidadADevolver;
        }

        public String getUnidad() {
            return unidad;
        }

        public void setUnidad(String unidad) {
            this.unidad = unidad;
        }

        public boolean isDevolver() {
            return devolver;
        }

        public void setDevolver(boolean devolver) {
            this.devolver = devolver;
        }

        public String getMotivoNoDevolucion() {
            return motivoNoDevolucion;
        }

        public void setMotivoNoDevolucion(String motivoNoDevolucion) {
            this.motivoNoDevolucion = motivoNoDevolucion;
        }
    }
}
