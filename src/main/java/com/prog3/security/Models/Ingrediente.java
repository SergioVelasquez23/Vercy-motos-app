package com.prog3.security.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonProperty;

@Document(collection = "ingredientes")
public class Ingrediente {

    @Id
    private String _id;
    private String categoriaId;
    private String nombre;
    private String unidad;
    private Double stockActual;
    private Double stockMinimo;
    @JsonProperty("costo")
    private double costo;
    private boolean descontable = true; // Por defecto true

    public Ingrediente() {
        // Constructor vacío - MongoDB generará automáticamente el _id
    }

    public Ingrediente(String categoriaId, String nombre, String unidad,
            Double stockActual, Double stockMinimo, double costo) {
        // No establecemos _id aquí, MongoDB lo generará automáticamente
        this.categoriaId = categoriaId;
        this.nombre = nombre;
        this.unidad = unidad;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.costo = costo;
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

    public boolean isDescontable() {
        return descontable;
    }

    public void setDescontable(boolean descontable) {
        this.descontable = descontable;
    }
    
    @JsonProperty("costo")
    public double getCosto() {
        return costo;
    }

    @JsonProperty("costo")
    public void setCosto(double costo) {
        this.costo = costo;
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
                + ", costo=" + costo
                + ", descontable=" + descontable
                + '}';
    }
}
