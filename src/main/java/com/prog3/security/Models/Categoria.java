package com.prog3.security.Models;

import org.springframework.data.annotation.Id;
//este se usa para marcar el campo que sera el identificador unico de la base de datos
import org.springframework.data.mongodb.core.mapping.Document;
//este indica que esta clase se guardara como un documento en mongodb

@Document //esta es la notacion que le dice a springboot que esto es un documento de mongodb
public class Categoria {

    // notacion id para el campo que sera el identificador unico de la base de datos
    @Id
    private String _id;
    private String nombre;
    private String descripcion;
    private String imagenUrl;

    //constructor por defecto sin valores
    public Categoria() {
    }

    //constructor parametrizado
    public Categoria(String nombre, String descripcion, String imagenUrl) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.imagenUrl = imagenUrl;
    }

    // Getters y Setters
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

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }
}
