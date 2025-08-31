package com.prog3.security.DTOs;

import java.util.List;

/**
 * DTO para estructurar la respuesta cuando se devuelven ingredientes al inventario
 */
public class DevolverIngredientesDTO {
    
    private String productoId;
    private String productoNombre;
    private int cantidadDevuelta;
    private List<IngredienteDevueltoDTO> ingredientesDevueltos;
    private String mensaje;

    // Constructores
    public DevolverIngredientesDTO() {}

    public DevolverIngredientesDTO(String productoId, String productoNombre, int cantidadDevuelta, 
                                 List<IngredienteDevueltoDTO> ingredientesDevueltos, String mensaje) {
        this.productoId = productoId;
        this.productoNombre = productoNombre;
        this.cantidadDevuelta = cantidadDevuelta;
        this.ingredientesDevueltos = ingredientesDevueltos;
        this.mensaje = mensaje;
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

    public int getCantidadDevuelta() {
        return cantidadDevuelta;
    }

    public void setCantidadDevuelta(int cantidadDevuelta) {
        this.cantidadDevuelta = cantidadDevuelta;
    }

    public List<IngredienteDevueltoDTO> getIngredientesDevueltos() {
        return ingredientesDevueltos;
    }

    public void setIngredientesDevueltos(List<IngredienteDevueltoDTO> ingredientesDevueltos) {
        this.ingredientesDevueltos = ingredientesDevueltos;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    /**
     * DTO interno para representar cada ingrediente devuelto
     */
    public static class IngredienteDevueltoDTO {
        private String ingredienteId;
        private String ingredienteNombre;
        private double cantidadDevuelta;
        private String unidad;
        private double stockAnterior;
        private double stockActual;

        // Constructores
        public IngredienteDevueltoDTO() {}

        public IngredienteDevueltoDTO(String ingredienteId, String ingredienteNombre, double cantidadDevuelta,
                                    String unidad, double stockAnterior, double stockActual) {
            this.ingredienteId = ingredienteId;
            this.ingredienteNombre = ingredienteNombre;
            this.cantidadDevuelta = cantidadDevuelta;
            this.unidad = unidad;
            this.stockAnterior = stockAnterior;
            this.stockActual = stockActual;
        }

        // Getters y Setters
        public String getIngredienteId() {
            return ingredienteId;
        }

        public void setIngredienteId(String ingredienteId) {
            this.ingredienteId = ingredienteId;
        }

        public String getIngredienteNombre() {
            return ingredienteNombre;
        }

        public void setIngredienteNombre(String ingredienteNombre) {
            this.ingredienteNombre = ingredienteNombre;
        }

        public double getCantidadDevuelta() {
            return cantidadDevuelta;
        }

        public void setCantidadDevuelta(double cantidadDevuelta) {
            this.cantidadDevuelta = cantidadDevuelta;
        }

        public String getUnidad() {
            return unidad;
        }

        public void setUnidad(String unidad) {
            this.unidad = unidad;
        }

        public double getStockAnterior() {
            return stockAnterior;
        }

        public void setStockAnterior(double stockAnterior) {
            this.stockAnterior = stockAnterior;
        }

        public double getStockActual() {
            return stockActual;
        }

        public void setStockActual(double stockActual) {
            this.stockActual = stockActual;
        }
    }
}
