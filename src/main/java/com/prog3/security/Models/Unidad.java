package com.prog3.security.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "unidades")
public class Unidad {
    @Id
    private String id;
    private String nombre;
    private String abreviatura;
    private boolean activo = true;

    public Unidad() {}
    public Unidad(String nombre, String abreviatura) {
        this.nombre = nombre;
        this.abreviatura = abreviatura;
        this.activo = true;
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getAbreviatura() { return abreviatura; }
    public void setAbreviatura(String abreviatura) { this.abreviatura = abreviatura; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
