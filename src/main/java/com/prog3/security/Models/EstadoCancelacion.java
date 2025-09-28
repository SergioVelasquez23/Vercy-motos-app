package com.prog3.security.Models;

/**
 * Enum para el estado de cancelación de productos
 */
public enum EstadoCancelacion {
    PENDIENTE("Pendiente"),
    CONFIRMADA("Confirmada"),
    REVERTIDA("Revertida");

    private final String descripcion;

    EstadoCancelacion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}