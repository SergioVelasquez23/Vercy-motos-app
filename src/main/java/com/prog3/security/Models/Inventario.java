package com.prog3.security.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document
public class Inventario {

    // Para compatibilidad con frontend: exponer stockActual en el JSON
    public double getStockActual() {
        return this.cantidadActual;
    }

    @Id
    private String _id;
    private String productoId; // Referencia al producto
    private String productoNombre; // Nombre del producto para consultas rápidas
    private double cantidadActual;
    private double cantidadMinima; // Stock mínimo para alertas
    private double cantidadMaxima; // Stock máximo recomendado
    private String unidadMedida; // kg, litros, unidades, etc.
    private double costoUnitario;
    private double costoTotal;
    private String proveedor;
    private LocalDateTime fechaUltimaActualizacion;
    private String ubicacion; // bodega, cocina, bar, etc.
    private String estado; // activo, agotado, descontinuado
    private String categoria; // materia prima, producto terminado, insumo
    private LocalDateTime fechaVencimiento;
    private String lote;
    private String observaciones;

    public Inventario() {
        this.fechaUltimaActualizacion = LocalDateTime.now();
        this.estado = "activo";
        this.cantidadActual = 0.0;
        this.cantidadMinima = 0.0;
        this.cantidadMaxima = 0.0;
        this.costoUnitario = 0.0;
        this.costoTotal = 0.0;
    }

    public Inventario(String productoId, String productoNombre, double cantidadActual, String unidadMedida) {
        this();
        this.productoId = productoId;
        this.productoNombre = productoNombre;
        this.cantidadActual = cantidadActual;
        this.unidadMedida = unidadMedida;
    }

    // Getters y Setters
    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getProductoId() {
        return productoId;
    }

    public void setProductoId(String productoId) {
        this.productoId = productoId;
    }

    public String getProductoNombre() {
        return productoNombre;
    }

    public void setProductoNombre(String productoNombre) {
        this.productoNombre = productoNombre;
    }

    public double getCantidadActual() {
        return cantidadActual;
    }

    public void setCantidadActual(double cantidadActual) {
        this.cantidadActual = cantidadActual;
        this.costoTotal = this.cantidadActual * this.costoUnitario;
        this.fechaUltimaActualizacion = LocalDateTime.now();
    }

    public double getCantidadMinima() {
        return cantidadMinima;
    }

    public void setCantidadMinima(double cantidadMinima) {
        this.cantidadMinima = cantidadMinima;
    }

    public double getCantidadMaxima() {
        return cantidadMaxima;
    }

    public void setCantidadMaxima(double cantidadMaxima) {
        this.cantidadMaxima = cantidadMaxima;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public double getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(double costoUnitario) {
        this.costoUnitario = costoUnitario;
        this.costoTotal = this.cantidadActual * this.costoUnitario;
        this.fechaUltimaActualizacion = LocalDateTime.now();
    }

    public double getCostoTotal() {
        return costoTotal;
    }

    public void setCostoTotal(double costoTotal) {
        this.costoTotal = costoTotal;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    public LocalDateTime getFechaUltimaActualizacion() {
        return fechaUltimaActualizacion;
    }

    public void setFechaUltimaActualizacion(LocalDateTime fechaUltimaActualizacion) {
        this.fechaUltimaActualizacion = fechaUltimaActualizacion;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public LocalDateTime getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDateTime fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    // Métodos de utilidad
    public boolean isStockBajo() {
        return this.cantidadActual <= this.cantidadMinima;
    }

    public boolean isStockAlto() {
        return this.cantidadActual >= this.cantidadMaxima;
    }

    public boolean isProximoVencimiento(int diasAnticipacion) {
        if (this.fechaVencimiento == null) {
            return false;
        }
        LocalDateTime fechaLimite = LocalDateTime.now().plusDays(diasAnticipacion);
        return this.fechaVencimiento.isBefore(fechaLimite);
    }
}
