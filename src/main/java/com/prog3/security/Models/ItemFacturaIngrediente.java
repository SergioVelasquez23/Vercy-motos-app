package com.prog3.security.Models;

/**
 * Representa un ingrediente/producto comprado en una factura de compras
 */
public class ItemFacturaIngrediente {

    private String ingredienteId; // ID del ingrediente/producto
    private String codigo; // Código interno del producto
    private String codigoBarras; // Código de barras del producto
    private String ingredienteNombre; // Nombre del ingrediente/producto
    private double cantidad;            // Cantidad comprada
    private String unidad;              // Unidad de medida (kg, g, litros, etc.)
    private double precioUnitario;      // Precio por unidad
    private double valorTotal; // Valor total sin impuestos ni descuentos
    private double precioTotal; // Total de este item (con impuestos y descuentos)

    // Campos de impuesto
    private double porcentajeImpuesto; // % de impuesto (IVA)
    private String tipoImpuesto; // Tipo de impuesto ("%" por defecto)
    private double valorImpuesto; // Valor calculado del impuesto

    // Campos de descuento
    private double porcentajeDescuento; // % de descuento por producto
    private double valorDescuento; // Valor calculado del descuento

    private boolean descontable;        // Si se debe aumentar el stock o no
    private String observaciones;       // Observaciones adicionales

    public ItemFacturaIngrediente() {
        this.cantidad = 0.0;
        this.precioUnitario = 0.0;
        this.valorTotal = 0.0;
        this.precioTotal = 0.0;
        this.porcentajeImpuesto = 0.0;
        this.tipoImpuesto = "%";
        this.valorImpuesto = 0.0;
        this.porcentajeDescuento = 0.0;
        this.valorDescuento = 0.0;
        this.descontable = true; // Por defecto es descontable
    }

    public ItemFacturaIngrediente(String ingredienteId, String codigo, String codigoBarras,
            String ingredienteNombre, double cantidad, String unidad,
            double precioUnitario, double porcentajeImpuesto, String tipoImpuesto,
            double porcentajeDescuento, boolean descontable) {
        this.ingredienteId = ingredienteId;
        this.codigo = codigo;
        this.codigoBarras = codigoBarras;
        this.ingredienteNombre = ingredienteNombre;
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.precioUnitario = precioUnitario;
        this.porcentajeImpuesto = porcentajeImpuesto;
        this.tipoImpuesto = tipoImpuesto != null ? tipoImpuesto : "%";
        this.porcentajeDescuento = porcentajeDescuento;
        this.descontable = descontable;
        calcularPrecioTotal();
    }

    /**
     * Calcula el precio total del item incluyendo impuestos y descuentos
     * Fórmula: valorTotal = cantidad * precioUnitario
     * valorDescuento = valorTotal * (porcentajeDescuento / 100)
     * valorImpuesto = (valorTotal - valorDescuento) * (porcentajeImpuesto / 100)
     * precioTotal = valorTotal - valorDescuento + valorImpuesto
     */
    public void calcularPrecioTotal() {
        // Calcular valor total sin impuestos ni descuentos
        this.valorTotal = this.cantidad * this.precioUnitario;

        // Calcular descuento
        this.valorDescuento = this.valorTotal * (this.porcentajeDescuento / 100.0);

        // Calcular impuesto sobre el valor después del descuento
        double baseParaImpuesto = this.valorTotal - this.valorDescuento;
        this.valorImpuesto = baseParaImpuesto * (this.porcentajeImpuesto / 100.0);

        // Calcular precio total final
        this.precioTotal = baseParaImpuesto + this.valorImpuesto;
    }

    // Getters y Setters
    public String getIngredienteId() {
        return ingredienteId;
    }

    public void setIngredienteId(String ingredienteId) {
        this.ingredienteId = ingredienteId;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
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

    public double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public double getPrecioTotal() {
        return precioTotal;
    }

    public void setPrecioTotal(double precioTotal) {
        this.precioTotal = precioTotal;
    }

    // Getters y setters para impuestos
    public double getPorcentajeImpuesto() {
        return porcentajeImpuesto;
    }

    public void setPorcentajeImpuesto(double porcentajeImpuesto) {
        this.porcentajeImpuesto = porcentajeImpuesto;
        calcularPrecioTotal();
    }

    public String getTipoImpuesto() {
        return tipoImpuesto;
    }

    public void setTipoImpuesto(String tipoImpuesto) {
        this.tipoImpuesto = tipoImpuesto;
    }

    public double getValorImpuesto() {
        return valorImpuesto;
    }

    public void setValorImpuesto(double valorImpuesto) {
        this.valorImpuesto = valorImpuesto;
    }

    // Getters y setters para descuentos
    public double getPorcentajeDescuento() {
        return porcentajeDescuento;
    }

    public void setPorcentajeDescuento(double porcentajeDescuento) {
        this.porcentajeDescuento = porcentajeDescuento;
        calcularPrecioTotal();
    }

    public double getValorDescuento() {
        return valorDescuento;
    }

    public void setValorDescuento(double valorDescuento) {
        this.valorDescuento = valorDescuento;
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
                + ", codigo='" + codigo + '\''
                + ", codigoBarras='" + codigoBarras + '\''
                + ", ingredienteNombre='" + ingredienteNombre + '\''
                + ", cantidad=" + cantidad
                + ", unidad='" + unidad + '\''
                + ", precioUnitario=" + precioUnitario
                + ", valorTotal=" + valorTotal
                + ", porcentajeImpuesto=" + porcentajeImpuesto
                + ", tipoImpuesto='" + tipoImpuesto + '\''
                + ", valorImpuesto=" + valorImpuesto
                + ", porcentajeDescuento=" + porcentajeDescuento
                + ", valorDescuento=" + valorDescuento
                + ", precioTotal=" + precioTotal
                + ", descontable=" + descontable
                + ", observaciones='" + observaciones + '\''
                + '}';
    }
}
