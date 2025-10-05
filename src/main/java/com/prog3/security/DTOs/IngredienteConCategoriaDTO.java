package com.prog3.security.DTOs;

public class IngredienteConCategoriaDTO {

    private String _id;
    private String categoriaId;
    private String categoriaNombre;
    private String nombre;
    private String unidadId;
    private String unidadNombre;
    private String unidadAbreviatura;
    private Double stockActual;
    private Double stockMinimo;

    public IngredienteConCategoriaDTO() {
    }

    public IngredienteConCategoriaDTO(String _id, String categoriaId, String categoriaNombre, String nombre,
            String unidadId, String unidadNombre, String unidadAbreviatura, Double stockActual, Double stockMinimo) {
        this._id = _id;
        this.categoriaId = categoriaId;
        this.categoriaNombre = categoriaNombre;
        this.nombre = nombre;
        this.unidadId = unidadId;
        this.unidadNombre = unidadNombre;
        this.unidadAbreviatura = unidadAbreviatura;
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

    public String getCategoriaNombre() {
        return categoriaNombre;
    }

    public void setCategoriaNombre(String categoriaNombre) {
        this.categoriaNombre = categoriaNombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUnidad() {
    return unidadNombre;
    }

    public void setUnidad(String unidad) {
        this.unidadNombre = unidad;
    }
    public String getUnidadId() {
        return unidadId;
    }
    public void setUnidadId(String unidadId) {
        this.unidadId = unidadId;
    }
    public String getUnidadAbreviatura() {
        return unidadAbreviatura;
    }
    public void setUnidadAbreviatura(String unidadAbreviatura) {
        this.unidadAbreviatura = unidadAbreviatura;
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
}
