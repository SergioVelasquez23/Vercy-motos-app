package com.prog3.security.Models;

public class TipoGasto {

    private String _id;
    private String nombre;
    private String descripcion;
    private boolean activo;

    // Constructor vacío
    public TipoGasto() {
        this.activo = true;  // Por defecto los tipos de gasto están activos
    }

    public TipoGasto(String nombre) {
        this();
        this.nombre = nombre;
    }

    public TipoGasto(String nombre, String descripcion) {
        this();
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    // Getters y setters
    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

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
