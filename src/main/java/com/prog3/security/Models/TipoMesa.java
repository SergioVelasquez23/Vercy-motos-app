package com.prog3.security.Models;

/**
 * Enum para los tipos de mesa
 */
public enum TipoMesa {
    NORMAL("Mesa Normal", "Mesa estándar del restaurante"),
    ESPECIAL("Mesa Especial", "Mesa con tarifas o servicios especiales");

    private final String nombre;
    private final String descripcion;

    TipoMesa(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return nombre;
    }
}