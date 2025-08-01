package com.prog3.security.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ingredientes")
public class Ingrediente {

    @Id
    private String _id;
    private String categoria;
    private String codigo;
    private String nombre;
    private String unidad;
    private Double precioCompra;
    private Double stockActual;
    private Double stockMinimo;
    private String estado;

    public Ingrediente() {
        // Constructor vacío - MongoDB generará automáticamente el _id
    }

    public Ingrediente(String categoria, String codigo, String nombre, String unidad,
            Double precioCompra, Double stockActual, Double stockMinimo, String estado) {
        // No establecemos _id aquí, MongoDB lo generará automáticamente
        this.categoria = categoria;
        this.codigo = codigo;
        this.nombre = nombre;
        this.unidad = unidad;
        this.precioCompra = precioCompra;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.estado = estado;
    }

    // Getters y Setters
    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
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

    public Double getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(Double precioCompra) {
        this.precioCompra = precioCompra;
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Ingrediente{"
                + "_id='" + _id + '\''
                + ", categoria='" + categoria + '\''
                + ", codigo='" + codigo + '\''
                + ", nombre='" + nombre + '\''
                + ", unidad='" + unidad + '\''
                + ", precioCompra=" + precioCompra
                + ", stockActual=" + stockActual
                + ", stockMinimo=" + stockMinimo
                + ", estado='" + estado + '\''
                + '}';
    }
}
