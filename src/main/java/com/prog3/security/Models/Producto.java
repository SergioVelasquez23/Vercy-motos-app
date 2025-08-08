package com.prog3.security.Models;

import java.util.List;
import java.util.ArrayList;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Producto {

    @Id
    private String _id;
    private String nombre;
    private double precio;
    private double costo;
    private double impuestos;
    private double utilidad;
    private boolean tieneVariantes;
    private String estado;
    private String imagenUrl;
    private String categoriaId; // Referencia a Categoria
    private String descripcion;
    private int cantidad;
    private String nota;
    private List<String> ingredientesDisponibles; // IDs de ingredientes que pueden ser opciones (carnes, etc.)
    private boolean tieneIngredientes; // Indica si el producto maneja ingredientes
    private String tipoProducto; // "combo" o "individual" - determina si el cliente puede elegir ingredientes
    private List<IngredienteProducto> ingredientesRequeridos; // Ingredientes fijos que siempre se consumen
    private List<IngredienteProducto> ingredientesOpcionales; // Ingredientes opcionales que el cliente puede elegir (solo para combos)

    public Producto() {
        // Inicializar campos que pueden ser null con valores por defecto
        this.estado = "Activo";
        this.tieneVariantes = false;
        this.cantidad = 1;
        this.impuestos = 0.0;
        this.imagenUrl = "";
        this.categoriaId = "";
        this.descripcion = "";
        this.nota = "";
        this.ingredientesDisponibles = new ArrayList<>();
        this.tieneIngredientes = false;
        this.tipoProducto = "individual"; // Por defecto es individual
        this.ingredientesRequeridos = new ArrayList<>();
        this.ingredientesOpcionales = new ArrayList<>();
    }

    public Producto(String nombre, double precio, double costo, double utilidad) {
        this.nombre = nombre;
        this.precio = precio;
        this.costo = costo;
        this.utilidad = utilidad;
        this.impuestos = 0.0;
        this.tieneVariantes = false;
        this.estado = "Activo";
        this.cantidad = 1;
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

    public boolean esCombo() {
        return "combo".equals(this.tipoProducto);
    }

    public boolean esIndividual() {
        return "individual".equals(this.tipoProducto);
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
