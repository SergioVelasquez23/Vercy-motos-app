package com.prog3.security.DTOs;

public class PagarPedidoRequest {

    private String formaPago; // efectivo, transferencia, tarjeta, otro
    private double propina;
    private String pagadoPor; // Quien procesa el pago (mesero, cajero, etc.)
    private String notas; // Notas adicionales del pago

    public PagarPedidoRequest() {
    }

    public PagarPedidoRequest(String formaPago, double propina, String pagadoPor, String notas) {
        this.formaPago = formaPago;
        this.propina = propina;
        this.pagadoPor = pagadoPor;
        this.notas = notas;
    }

    // Getters y Setters
    public String getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(String formaPago) {
        this.formaPago = formaPago;
    }

    public double getPropina() {
        return propina;
    }

    public void setPropina(double propina) {
        this.propina = propina;
    }

    public String getPagadoPor() {
        return pagadoPor;
    }

    public void setPagadoPor(String pagadoPor) {
        this.pagadoPor = pagadoPor;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }
}
