package com.prog3.security.Models;

/**
 * Clase que representa la relación entre un producto y un ingrediente Incluye
 * la cantidad necesaria del ingrediente para el producto
 */
public class IngredienteProducto {

    private String ingredienteId; // ID del ingrediente en la base de datos
    private String nombre; // Nombre del ingrediente (para facilitar visualización)
    private double cantidadNecesaria; // Cantidad del ingrediente que se necesita
    private String unidad; // Unidad de medida (kg, gramos, litros, etc.)
    private boolean esOpcional; // Si es opcional, el cliente puede elegir no incluirlo

    // Constructor por defecto
    public IngredienteProducto() {
        this.cantidadNecesaria = 0.0;
        this.esOpcional = false;
        this.unidad = "unidad";
    }

    // Constructor con parámetros principales
    public IngredienteProducto(String ingredienteId, String nombre, double cantidadNecesaria, String unidad) {
        this.ingredienteId = ingredienteId;
        this.nombre = nombre;
        this.cantidadNecesaria = cantidadNecesaria;
        this.unidad = unidad;
        this.esOpcional = false;
    }

    // Constructor con parámetros completos  
    public IngredienteProducto(String ingredienteId, String nombre, double cantidadNecesaria,
            String unidad, boolean esOpcional) {
        this.ingredienteId = ingredienteId;
        this.nombre = nombre;
        this.cantidadNecesaria = cantidadNecesaria;
        this.unidad = unidad;
        this.esOpcional = esOpcional;
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

    public double getCantidadNecesaria() {
        return cantidadNecesaria;
    }

    public void setCantidadNecesaria(double cantidadNecesaria) {
        this.cantidadNecesaria = cantidadNecesaria;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public boolean isEsOpcional() {
        return esOpcional;
    }

    public void setEsOpcional(boolean esOpcional) {
        this.esOpcional = esOpcional;
    }

    @Override
    public String toString() {
        return "IngredienteProducto{"
                + "ingredienteId='" + ingredienteId + '\''
                + ", nombre='" + nombre + '\''
                + ", cantidadNecesaria=" + cantidadNecesaria
                + ", unidad='" + unidad + '\''
                + ", esOpcional=" + esOpcional
                + '}';
    }
}
