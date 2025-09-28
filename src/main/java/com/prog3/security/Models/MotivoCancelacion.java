package com.prog3.security.Models;

/**
 * Enum para los motivos de cancelación de productos
 */
public enum MotivoCancelacion {
    CLIENTE_SOLICITO("Cliente solicitó cancelación"),
    ERROR_PEDIDO("Error en el pedido"),
    NO_DISPONIBLE("Producto no disponible"),
    CAMBIO_MESA("Cambio de mesa"),
    ERROR_SISTEMA("Error del sistema"),
    OTRO("Otro motivo");

    private final String descripcion;

    MotivoCancelacion(String descripcion) {
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