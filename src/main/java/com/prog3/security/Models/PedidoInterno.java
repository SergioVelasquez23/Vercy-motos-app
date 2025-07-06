package com.prog3.security.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document
public class PedidoInterno {

    @Id
    private String _id;
    private LocalDateTime fechaPedido;
    private LocalDateTime fechaCortesia;
    private String productoId;
    private int cantidad;
    private String nota;
    private String guardadoPor;
    private String pedidoPor;
    private String estado; // pendiente, enProceso, completado, cancelado

    public PedidoInterno() {
        this.fechaPedido = LocalDateTime.now();
        this.estado = "pendiente";
    }

    public PedidoInterno(String productoId, int cantidad, String guardadoPor, String pedidoPor) {
        this.fechaPedido = LocalDateTime.now();
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.guardadoPor = guardadoPor;
        this.pedidoPor = pedidoPor;
        this.estado = "pendiente";
    }

    // Getters y Setters
    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public LocalDateTime getFechaPedido() {
        return fechaPedido;
    }

    public void setFechaPedido(LocalDateTime fechaPedido) {
        this.fechaPedido = fechaPedido;
    }

    public LocalDateTime getFechaCortesia() {
        return fechaCortesia;
    }

    public void setFechaCortesia(LocalDateTime fechaCortesia) {
        this.fechaCortesia = fechaCortesia;
    }

    public String getProductoId() {
        return productoId;
    }

    public void setProductoId(String productoId) {
        this.productoId = productoId;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getNota() {
        return nota;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }

    public String getGuardadoPor() {
        return guardadoPor;
    }

    public void setGuardadoPor(String guardadoPor) {
        this.guardadoPor = guardadoPor;
    }

    public String getPedidoPor() {
        return pedidoPor;
    }

    public void setPedidoPor(String pedidoPor) {
        this.pedidoPor = pedidoPor;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
