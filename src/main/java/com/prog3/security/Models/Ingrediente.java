package com.prog3.security.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ingredientes")
public class Ingrediente {
    public String getUnidadId() {
        return unidadId;
    }
    public void setUnidadId(String unidadId) {
        this.unidadId = unidadId;
    }

    @Id
    private String _id;
    private String categoriaId;
    private String nombre;
    private String unidadId; // Referencia al id de Unidad
    private Double stockActual;
    private Double stockMinimo;
    private boolean descontable; // Nuevo atributo: indica si se descuenta del stock

    public Ingrediente() {
        // Constructor vacío - MongoDB generará automáticamente el _id
        this.descontable = true; // Por defecto, los ingredientes son descontables
    }

    public Ingrediente(String categoriaId, String nombre, String unidad,
            Double stockActual, Double stockMinimo) {
        // No establecemos _id aquí, MongoDB lo generará automáticamente
    this.categoriaId = categoriaId;
    this.nombre = nombre;
    this.unidadId = unidad;
    this.stockActual = stockActual;
    this.stockMinimo = stockMinimo;
    this.descontable = true; // Por defecto, los ingredientes son descontables
    }

    public Ingrediente(String categoriaId, String nombre, String unidad,
            Double stockActual, Double stockMinimo, boolean descontable) {
    this.categoriaId = categoriaId;
    this.nombre = nombre;
    this.unidadId = unidad;
    this.stockActual = stockActual;
    this.stockMinimo = stockMinimo;
    this.descontable = descontable;
    }

    // Getters y Setters
    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(String categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUnidad() {
    return unidadId;
    }

    public void setUnidad(String unidad) {
    this.unidadId = unidad;
    }

    public Double getStockActual() {
        return stockActual;
    }

    public void setStockActual(Double stockActual) {
        this.stockActual = stockActual;
    }

    public Double getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Double stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public boolean isDescontable() {
        return descontable;
    }

    public void setDescontable(boolean descontable) {
        this.descontable = descontable;
    }

    @Override
    public String toString() {
        return "Ingrediente{"
                + "_id='" + _id + '\''
                + ", categoriaId='" + categoriaId + '\''
                + ", nombre='" + nombre + '\''
                + ", unidadId='" + unidadId + '\''
                + ", stockActual=" + stockActual
                + ", stockMinimo=" + stockMinimo
                + ", descontable=" + descontable
                + '}';
    }
}
