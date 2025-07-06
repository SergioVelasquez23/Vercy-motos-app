package com.prog3.security.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Document
public class Pedido {

    @Id
    private String _id;
    private LocalDateTime fecha;
    private String tipo; // normal, rt, interno, cancelado, cortesia
    private String mesa;
    private String cliente;
    private String mesero;
    private List<ItemPedido> items;
    private String notas;
    private String plataforma; // Para pedidos RT
    private String pedidoPor; // Para pedidos internos
    private String guardadoPor; // Para pedidos internos
    private LocalDateTime fechaCortesia;
    private String estado; // pendiente, enProceso, completado, entregado, cancelado

    public Pedido() {
        this.fecha = LocalDateTime.now();
        this.estado = "pendiente";
    }

    public Pedido(String tipo, List<ItemPedido> items) {
        this.fecha = LocalDateTime.now();
        this.tipo = tipo;
        this.items = items;
        this.estado = "pendiente";
    }

    // Getters y Setters
    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getMesa() {
        return mesa;
    }

    public void setMesa(String mesa) {
        this.mesa = mesa;
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

    public List<ItemPedido> getItems() {
        return items;
    }

    public void setItems(List<ItemPedido> items) {
        this.items = items;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public String getPlataforma() {
        return plataforma;
    }

    public void setPlataforma(String plataforma) {
        this.plataforma = plataforma;
    }

    public String getPedidoPor() {
        return pedidoPor;
    }

    public void setPedidoPor(String pedidoPor) {
        this.pedidoPor = pedidoPor;
    }

    public String getGuardadoPor() {
        return guardadoPor;
    }

    public void setGuardadoPor(String guardadoPor) {
        this.guardadoPor = guardadoPor;
    }

    public LocalDateTime getFechaCortesia() {
        return fechaCortesia;
    }

    public void setFechaCortesia(LocalDateTime fechaCortesia) {
        this.fechaCortesia = fechaCortesia;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
