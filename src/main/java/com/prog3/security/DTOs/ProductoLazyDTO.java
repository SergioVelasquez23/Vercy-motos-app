package com.prog3.security.DTOs;

/**
 * DTO ligero para lazy loading de productos
 * Solo incluye campos esenciales para mejorar rendimiento
 */
public class ProductoLazyDTO {
    private String _id;
    private String nombre;
    private double precio;
    private String categoriaId;
    private String imagenUrl;
    private String estado;
    private boolean tieneVariantes;
    private boolean tieneIngredientes;
    private String tipoProducto;
    
    // Constructor vacío
    public ProductoLazyDTO() {}
    
    // Constructor con todos los campos
    public ProductoLazyDTO(String _id, String nombre, double precio, String categoriaId, 
                           String imagenUrl, String estado, boolean tieneVariantes,
                           boolean tieneIngredientes, String tipoProducto) {
        this._id = _id;
        this.nombre = nombre;
        this.precio = precio;
        this.categoriaId = categoriaId;
        this.imagenUrl = imagenUrl;
        this.estado = estado;
        this.tieneVariantes = tieneVariantes;
        this.tieneIngredientes = tieneIngredientes;
        this.tipoProducto = tipoProducto;
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

    public String getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(String categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public boolean isTieneVariantes() {
        return tieneVariantes;
    }

    public void setTieneVariantes(boolean tieneVariantes) {
        this.tieneVariantes = tieneVariantes;
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
}
