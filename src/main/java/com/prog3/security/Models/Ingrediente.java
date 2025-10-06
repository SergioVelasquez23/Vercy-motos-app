package com.prog3.security.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ingredientes")
public class Ingrediente {

    @Id
    private String _id;
    private String categoriaId;
    private String nombre;
    private String unidad;
    private Double stockActual;
    private Double stockMinimo;

    public Ingrediente() {
        // Constructor vacío - MongoDB generará automáticamente el _id
    }

    public Ingrediente(String categoriaId, String nombre, String unidad,
            Double stockActual, Double stockMinimo) {
        // No establecemos _id aquí, MongoDB lo generará automáticamente
        this.categoriaId = categoriaId;
        this.nombre = nombre;
        this.unidad = unidad;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
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
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
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

    @Override
    public String toString() {
        return "Ingrediente{"
                + "_id='" + _id + '\''
                + ", categoriaId='" + categoriaId + '\''
                + ", nombre='" + nombre + '\''
                + ", unidad='" + unidad + '\''
                + ", stockActual=" + stockActual
                + ", stockMinimo=" + stockMinimo
                + '}';
    }
}
