package com.prog3.security.DTOs;

public class PagarPedidoRequest {

    private String tipoPago; // "pagado", "cortesia", "consumo_interno", "cancelado"
    private String formaPago; // efectivo, transferencia, tarjeta, otro (solo para tipoPago = "pagado")
    private double propina; // Solo aplica para tipoPago = "pagado"
    private String procesadoPor; // Quien procesa (mesero, cajero, etc.)
    private String notas; // Notas adicionales
    private String motivoCortesia; // Solo para cortesía (ej: "cumpleaños cliente")
    private String tipoConsumoInterno; // Solo para consumo interno (ej: "empleado", "gerencia")

    public PagarPedidoRequest() {
    }

    public PagarPedidoRequest(String tipoPago, String formaPago, double propina, String procesadoPor, String notas) {
        this.tipoPago = tipoPago;
        this.formaPago = formaPago;
        this.propina = propina;
        this.procesadoPor = procesadoPor;
        this.notas = notas;
    }

    // Getters y Setters
    public String getTipoPago() {
        return tipoPago;
    }

    public void setTipoPago(String tipoPago) {
        this.tipoPago = tipoPago;
    }

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

    public String getProcesadoPor() {
        return procesadoPor;
    }

    public void setProcesadoPor(String procesadoPor) {
        this.procesadoPor = procesadoPor;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public String getMotivoCortesia() {
        return motivoCortesia;
    }

    public void setMotivoCortesia(String motivoCortesia) {
        this.motivoCortesia = motivoCortesia;
    }

    public String getTipoConsumoInterno() {
        return tipoConsumoInterno;
    }

    public void setTipoConsumoInterno(String tipoConsumoInterno) {
        this.tipoConsumoInterno = tipoConsumoInterno;
    }

    // Métodos de utilidad para validar el tipo
    public boolean esPagado() {
        return "pagado".equals(this.tipoPago);
    }

    public boolean esCortesia() {
        return "cortesia".equals(this.tipoPago);
    }

    public boolean esConsumoInterno() {
        return "consumo_interno".equals(this.tipoPago);
    }

    public boolean esCancelado() {
        return "cancelado".equals(this.tipoPago);
    }

    public boolean sumaAVentas() {
        return esPagado(); // Solo los pagados suman a ventas
    }
}
