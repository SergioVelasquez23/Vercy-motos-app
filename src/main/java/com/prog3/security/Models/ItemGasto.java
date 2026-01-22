package com.prog3.security.Models;

/**
 * Representa un item/línea individual de un gasto
 * Permite gastos con múltiples conceptos
 */
public class ItemGasto {

    private String concepto; // Descripción del concepto
    private double valor; // Valor base del item
    private double porcentajeDescuento; // % de descuento
    private double valorDescuento; // Valor calculado del descuento
    private String tipoImpuesto; // Tipo de impuesto (IVA, etc.)
    private double porcentajeImpuesto; // % de impuesto (tasa)
    private double valorImpuesto; // Valor calculado del impuesto
    private double total; // Total del item

    public ItemGasto() {
        this.valor = 0.0;
        this.porcentajeDescuento = 0.0;
        this.valorDescuento = 0.0;
        this.tipoImpuesto = "IVA";
        this.porcentajeImpuesto = 0.0;
        this.valorImpuesto = 0.0;
        this.total = 0.0;
    }

    public ItemGasto(String concepto, double valor, double porcentajeDescuento,
            String tipoImpuesto, double porcentajeImpuesto) {
        this.concepto = concepto;
        this.valor = valor;
        this.porcentajeDescuento = porcentajeDescuento;
        this.tipoImpuesto = tipoImpuesto != null ? tipoImpuesto : "IVA";
        this.porcentajeImpuesto = porcentajeImpuesto;
        calcularTotales();
    }

    /**
     * Calcula los valores de descuento, impuesto y total
     * Fórmula:
     * - valorDescuento = valor * (porcentajeDescuento / 100)
     * - baseImponible = valor - valorDescuento
     * - valorImpuesto = baseImponible * (porcentajeImpuesto / 100)
     * - total = baseImponible + valorImpuesto
     */
    public void calcularTotales() {
        // Calcular descuento
        this.valorDescuento = this.valor * (this.porcentajeDescuento / 100.0);

        // Base imponible (después del descuento)
        double baseImponible = this.valor - this.valorDescuento;

        // Calcular impuesto
        this.valorImpuesto = baseImponible * (this.porcentajeImpuesto / 100.0);

        // Calcular total
        this.total = baseImponible + this.valorImpuesto;
    }

    // Getters y Setters
    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
        calcularTotales();
    }

    public double getPorcentajeDescuento() {
        return porcentajeDescuento;
    }

    public void setPorcentajeDescuento(double porcentajeDescuento) {
        this.porcentajeDescuento = porcentajeDescuento;
        calcularTotales();
    }

    public double getValorDescuento() {
        return valorDescuento;
    }

    public void setValorDescuento(double valorDescuento) {
        this.valorDescuento = valorDescuento;
    }

    public String getTipoImpuesto() {
        return tipoImpuesto;
    }

    public void setTipoImpuesto(String tipoImpuesto) {
        this.tipoImpuesto = tipoImpuesto;
    }

    public double getPorcentajeImpuesto() {
        return porcentajeImpuesto;
    }

    public void setPorcentajeImpuesto(double porcentajeImpuesto) {
        this.porcentajeImpuesto = porcentajeImpuesto;
        calcularTotales();
    }

    public double getValorImpuesto() {
        return valorImpuesto;
    }

    public void setValorImpuesto(double valorImpuesto) {
        this.valorImpuesto = valorImpuesto;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "ItemGasto{" +
                "concepto='" + concepto + '\'' +
                ", valor=" + valor +
                ", porcentajeDescuento=" + porcentajeDescuento +
                ", valorDescuento=" + valorDescuento +
                ", tipoImpuesto='" + tipoImpuesto + '\'' +
                ", porcentajeImpuesto=" + porcentajeImpuesto +
                ", valorImpuesto=" + valorImpuesto +
                ", total=" + total +
                '}';
    }
}
