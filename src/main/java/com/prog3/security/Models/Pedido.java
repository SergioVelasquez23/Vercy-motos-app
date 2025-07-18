package com.prog3.security.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Document
public class Pedido {

    @Id
    @JsonProperty("_id")
    private String _id;
    private LocalDateTime fecha;
    private String tipo; // normal, rt, interno, cancelado, cortesia
    private String mesa;
    private String cliente;
    private String mesero;
    private List<ItemPedido> items;
    private double total;
    private double descuento;
    private boolean incluyePropina;
    private String notas;
    private String plataforma; // Para pedidos RT
    private String pedidoPor; // Para pedidos internos
    private String guardadoPor; // Para pedidos internos
    private LocalDateTime fechaCortesia;
    private String estado; // activo, pagado, cancelado, completado
    private String formaPago; // efectivo, transferencia, tarjeta, otro
    private List<ItemPedido> itemsPagados; // Items que ya han sido pagados

    // Campos para pago
    private double propina; // Propina añadida al pagar
    private double totalPagado; // Total incluyendo propina
    private LocalDateTime fechaPago; // Cuándo se pagó
    private String pagadoPor; // Quien procesó el pago

    // Campos para cancelación
    private String motivoCancelacion; // Por qué se canceló
    private String canceladoPor; // Quien canceló el pedido
    private LocalDateTime fechaCancelacion; // Cuándo se canceló

    public Pedido() {
        this.fecha = LocalDateTime.now();
        this.estado = "activo";
        this.total = 0.0;
        this.descuento = 0.0;
        this.incluyePropina = false;
        this.items = new ArrayList<>();
        this.itemsPagados = new ArrayList<>();
    }

    public Pedido(String tipo, List<ItemPedido> items) {
        this.fecha = LocalDateTime.now();
        this.tipo = tipo;
        this.items = items;
        this.estado = "pendiente";
        this.total = 0.0;
        this.descuento = 0.0;
        this.incluyePropina = false;
        this.itemsPagados = new ArrayList<>();
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

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getDescuento() {
        return descuento;
    }

    public void setDescuento(double descuento) {
        this.descuento = descuento;
    }

    public boolean isIncluyePropina() {
        return incluyePropina;
    }

    public void setIncluyePropina(boolean incluyePropina) {
        this.incluyePropina = incluyePropina;
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

    public String getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(String formaPago) {
        this.formaPago = formaPago;
    }

    public List<ItemPedido> getItemsPagados() {
        return itemsPagados;
    }

    public void setItemsPagados(List<ItemPedido> itemsPagados) {
        this.itemsPagados = itemsPagados;
    }

    public void marcarItemComoPagado(String itemId) {
        // Buscar el item en la lista de items
        for (ItemPedido item : items) {
            if (item.getId().equals(itemId)) {
                // Remover de items activos y agregar a pagados
                items.remove(item);
                itemsPagados.add(item);
                break;
            }
        }
        // Actualizar el estado si todos los items están pagados
        if (items.isEmpty() && !itemsPagados.isEmpty()) {
            this.estado = "completado";
        }
    }

    public double calcularTotalPendiente() {
        double totalPendiente = 0.0;
        for (ItemPedido item : items) {
            totalPendiente += item.getSubtotal();
        }
        return totalPendiente;
    }

    public double calcularTotalPagado() {
        double totalPagado = 0.0;
        for (ItemPedido item : itemsPagados) {
            totalPagado += item.getSubtotal();
        }
        return totalPagado;
    }

    // Getters y Setters para los nuevos campos
    public double getPropina() {
        return propina;
    }

    public void setPropina(double propina) {
        this.propina = propina;
    }

    public double getTotalPagado() {
        return totalPagado;
    }

    public void setTotalPagado(double totalPagado) {
        this.totalPagado = totalPagado;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }

    public String getPagadoPor() {
        return pagadoPor;
    }

    public void setPagadoPor(String pagadoPor) {
        this.pagadoPor = pagadoPor;
    }

    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    public void setMotivoCancelacion(String motivoCancelacion) {
        this.motivoCancelacion = motivoCancelacion;
    }

    public String getCanceladoPor() {
        return canceladoPor;
    }

    public void setCanceladoPor(String canceladoPor) {
        this.canceladoPor = canceladoPor;
    }

    public LocalDateTime getFechaCancelacion() {
        return fechaCancelacion;
    }

    public void setFechaCancelacion(LocalDateTime fechaCancelacion) {
        this.fechaCancelacion = fechaCancelacion;
    }

    // Métodos de utilidad
    public void pagar(String formaPago, double propina, String pagadoPor) {
        this.estado = "pagado";
        this.formaPago = formaPago;
        this.propina = propina;
        this.totalPagado = this.total + propina;
        this.fechaPago = LocalDateTime.now();
        this.pagadoPor = pagadoPor;
    }

    public void cancelar(String motivo, String canceladoPor) {
        this.estado = "cancelado";
        this.motivoCancelacion = motivo;
        this.canceladoPor = canceladoPor;
        this.fechaCancelacion = LocalDateTime.now();
    }
}
