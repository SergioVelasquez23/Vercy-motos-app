package com.prog3.security.Models;

/**
 * Representa un ingrediente comprado en una factura de compras
 */
public class ItemFacturaIngrediente {

    private String ingredienteId;       // ID del ingrediente
    private String ingredienteNombre;   // Nombre del ingrediente
    private double cantidad;            // Cantidad comprada
    private String unidad;              // Unidad de medida (kg, g, litros, etc.)
    private double precioUnitario;      // Precio por unidad
    private double precioTotal;         // Total de este item
    private boolean descontable;        // Si se debe aumentar el stock o no
    private String observaciones;       // Observaciones adicionales

    public ItemFacturaIngrediente() {
        this.cantidad = 0.0;
        this.precioUnitario = 0.0;
        this.precioTotal = 0.0;
        this.descontable = true; // Por defecto es descontable
    }

    public ItemFacturaIngrediente(String ingredienteId, String ingredienteNombre,
            double cantidad, String unidad, double precioUnitario,
            boolean descontable) {
        this.ingredienteId = ingredienteId;
        this.ingredienteNombre = ingredienteNombre;
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.precioUnitario = precioUnitario;
        this.descontable = descontable;
        this.precioTotal = cantidad * precioUnitario;
    }

    /**
     * Calcula el precio total del item
     */
    public void calcularPrecioTotal() {
        this.precioTotal = this.cantidad * this.precioUnitario;
    }

    // Getters y Setters
    public String getIngredienteId() {
        return ingredienteId;
    }

    public void setIngredienteId(String ingredienteId) {
        this.ingredienteId = ingredienteId;
    }

    public String getIngredienteNombre() {
        return ingredienteNombre;
    }

    public void setIngredienteNombre(String ingredienteNombre) {
        this.ingredienteNombre = ingredienteNombre;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
        calcularPrecioTotal();
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
        calcularPrecioTotal();
    }

    public double getPrecioTotal() {
        return precioTotal;
    }

    public void setPrecioTotal(double precioTotal) {
        this.precioTotal = precioTotal;
    }

    public boolean isDescontable() {
        return descontable;
    }

    public void setDescontable(boolean descontable) {
        this.descontable = descontable;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    @Override
    public String toString() {
        return "ItemFacturaIngrediente{"
                + "ingredienteId='" + ingredienteId + '\''
                + ", ingredienteNombre='" + ingredienteNombre + '\''
                + ", cantidad=" + cantidad
                + ", unidad='" + unidad + '\''
                + ", precioUnitario=" + precioUnitario
                + ", precioTotal=" + precioTotal
                + ", descontable=" + descontable
                + ", observaciones='" + observaciones + '\''
                + '}';
    }
}
