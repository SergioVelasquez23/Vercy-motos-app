package com.prog3.security.DTOs;

public class EtiquetaCodigoBarrasDTO {
    private String itemId;
    private String codigo;
    private String nombre;
    private String descripcion;
    private double precio;
    private String tipoItem; // "producto" o "ingrediente"
    private byte[] imagenCodigoBarras; // Imagen del código de barras en bytes
    private String formatoImagen; // "PNG", "SVG", etc.

    public EtiquetaCodigoBarrasDTO() {}

    public EtiquetaCodigoBarrasDTO(String itemId, String codigo, String nombre, double precio,
            String tipoItem) {
        this.itemId = itemId;
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.tipoItem = tipoItem;
        this.formatoImagen = "PNG";
    }

    // Getters y Setters
    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getTipoItem() {
        return tipoItem;
    }

    public void setTipoItem(String tipoItem) {
        this.tipoItem = tipoItem;
    }

    public byte[] getImagenCodigoBarras() {
        return imagenCodigoBarras;
    }

    public void setImagenCodigoBarras(byte[] imagenCodigoBarras) {
        this.imagenCodigoBarras = imagenCodigoBarras;
    }

    public String getFormatoImagen() {
        return formatoImagen;
    }

    public void setFormatoImagen(String formatoImagen) {
        this.formatoImagen = formatoImagen;
    }
}
