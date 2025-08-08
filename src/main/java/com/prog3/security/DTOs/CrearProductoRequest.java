package com.prog3.security.DTOs;

import java.util.List;
import java.util.ArrayList;
import com.prog3.security.Models.IngredienteProducto;

public class CrearProductoRequest {

    private String nombre;
    private double precio;
    private double costo;
    private double impuestos;
    private double utilidad;
    private boolean tieneVariantes;
    private String estado;
    private String imagenUrl;
    private String categoriaId;
    private String descripcion;
    private int cantidad;
    private String nota;
    private List<String> ingredientesDisponibles; // IDs de ingredientes que pueden ser opciones

    // Campos nuevos para combo/individual
    private boolean tieneIngredientes;
    private String tipoProducto;
    private List<IngredienteProducto> ingredientesRequeridos;
    private List<IngredienteProducto> ingredientesOpcionales;

    public CrearProductoRequest() {
        this.ingredientesDisponibles = new ArrayList<>();
        this.estado = "Activo";
        this.tieneVariantes = false;
        this.cantidad = 1;
        this.impuestos = 0.0;
        this.tieneIngredientes = false;
        this.tipoProducto = "individual";
        this.ingredientesRequeridos = new ArrayList<>();
        this.ingredientesOpcionales = new ArrayList<>();
    }

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public double getCosto() {
        return costo;
    }

    public void setCosto(double costo) {
        this.costo = costo;
    }

    public double getImpuestos() {
        return impuestos;
    }

    public void setImpuestos(double impuestos) {
        this.impuestos = impuestos;
    }

    public double getUtilidad() {
        return utilidad;
    }

    public void setUtilidad(double utilidad) {
        this.utilidad = utilidad;
    }

    public boolean isTieneVariantes() {
        return tieneVariantes;
    }

    public void setTieneVariantes(boolean tieneVariantes) {
        this.tieneVariantes = tieneVariantes;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public String getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(String categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getNota() {
        return nota;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }

    public List<String> getIngredientesDisponibles() {
        return ingredientesDisponibles;
    }

    public void setIngredientesDisponibles(List<String> ingredientesDisponibles) {
        this.ingredientesDisponibles = ingredientesDisponibles;
    }

    // Getters y setters para campos nuevos
    public boolean isTieneIngredientes() {
        return tieneIngredientes;
    }

    public void setTieneIngredientes(boolean tieneIngredientes) {
        this.tieneIngredientes = tieneIngredientes;
    }

    public String getTipoProducto() {
        return tipoProducto;
    }

    public void setTipoProducto(String tipoProducto) {
        this.tipoProducto = tipoProducto;
    }

    public List<IngredienteProducto> getIngredientesRequeridos() {
        return ingredientesRequeridos;
    }

    public void setIngredientesRequeridos(List<IngredienteProducto> ingredientesRequeridos) {
        this.ingredientesRequeridos = ingredientesRequeridos;
    }

    public List<IngredienteProducto> getIngredientesOpcionales() {
        return ingredientesOpcionales;
    }

    public void setIngredientesOpcionales(List<IngredienteProducto> ingredientesOpcionales) {
        this.ingredientesOpcionales = ingredientesOpcionales;
    }
}
