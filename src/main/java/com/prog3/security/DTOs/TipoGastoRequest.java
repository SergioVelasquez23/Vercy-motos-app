package com.prog3.security.DTOs;

public class TipoGastoRequest {

    private String nombre;
    private String descripcion;
    private boolean activo;

    public TipoGastoRequest() {
        this.activo = true;
    }

    // Getters y setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
