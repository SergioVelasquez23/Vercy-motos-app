package com.prog3.security.DTOs;

public class CrearPedidoMesaEspecialRequest {

    private String mesa;
    private String nombrePedido;
    private String cliente;
    private String mesero;
    private String notas;
    private String tipo; // Por defecto será "normal"

    public CrearPedidoMesaEspecialRequest() {
    }

    public CrearPedidoMesaEspecialRequest(String mesa, String nombrePedido, String cliente, String mesero) {
        this.mesa = mesa;
        this.nombrePedido = nombrePedido;
        this.cliente = cliente;
        this.mesero = mesero;
        this.tipo = "normal";
    }

    // Getters y Setters
    public String getMesa() {
        return mesa;
    }

    public void setMesa(String mesa) {
        this.mesa = mesa;
    }

    public String getNombrePedido() {
        return nombrePedido;
    }

    public void setNombrePedido(String nombrePedido) {
        this.nombrePedido = nombrePedido;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getMesero() {
        return mesero;
    }

    public void setMesero(String mesero) {
        this.mesero = mesero;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
