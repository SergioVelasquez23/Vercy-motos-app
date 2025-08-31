package com.prog3.security.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document
public class User {

    @Id
    private String _id;
    private String name; // "nombre" en frontend
    private String email;
    private String password;
    private boolean activo = true; // Campo faltante que espera el frontend
    private int numeroDeSesiones = 0;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    public User() {
        this.activo = true;
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
    }

    public User(String name, String email, String password) {
        this();
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getNumeroDeSesiones() {
        return numeroDeSesiones;
    }

    public void setNumeroDeSesiones(int numeroDeSesiones) {
        this.numeroDeSesiones = numeroDeSesiones;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
        this.fechaActualizacion = LocalDateTime.now();
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    // Métodos de utilidad para compatibilidad con frontend
    public String getNombre() {
        return this.name; // Alias para compatibilidad
    }

    public void setNombre(String nombre) {
        this.name = nombre;
        this.fechaActualizacion = LocalDateTime.now();
    }
}
