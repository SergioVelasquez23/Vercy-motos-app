package com.prog3.security.DTOs;

public class CancelarPedidoRequest {

    private String motivo; // Razón de la cancelación
    private String canceladoPor; // Quien cancela el pedido
    private String notas; // Notas adicionales

    public CancelarPedidoRequest() {
    }

    public CancelarPedidoRequest(String motivo, String canceladoPor, String notas) {
        this.motivo = motivo;
        this.canceladoPor = canceladoPor;
        this.notas = notas;
    }

    // Getters y Setters
    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getCanceladoPor() {
        return canceladoPor;
    }

    public void setCanceladoPor(String canceladoPor) {
        this.canceladoPor = canceladoPor;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }
}
