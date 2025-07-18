package com.prog3.security.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Document
public class Factura {

    @Id
    private String _id;

    // Información básica de la factura
    private String numero;
    private LocalDateTime fecha;

    // Información del consumidor final
    private String nit;  // Puede ser "22222222222" para consumidor final
    private String clienteTelefono;
    private String clienteDireccion;

    // Items de la factura
    private List<ItemFactura> items;

    // Información de pago
    private String medioPago;  // "Efectivo"
    private String formaPago;  // "Contado"

    // Totales
    private double total;

    // Información adicional
    private String atendidoPor;

    public Factura() {
        this.fecha = LocalDateTime.now();
        this.items = new ArrayList<>();
        this.medioPago = "Efectivo";
        this.formaPago = "Contado";
        this.total = 0.0;
    }

    // Getters y Setters básicos
    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getClienteTelefono() {
        return clienteTelefono;
    }

    public void setClienteTelefono(String clienteTelefono) {
        this.clienteTelefono = clienteTelefono;
    }

    public String getClienteDireccion() {
        return clienteDireccion;
    }

    public void setClienteDireccion(String clienteDireccion) {
        this.clienteDireccion = clienteDireccion;
    }

    public List<ItemFactura> getItems() {
        return items;
    }

    public void setItems(List<ItemFactura> items) {
        this.items = items;
        calcularTotal();
    }

    public String getMedioPago() {
        return medioPago;
    }

    public void setMedioPago(String medioPago) {
        this.medioPago = medioPago;
    }

    public String getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(String formaPago) {
        this.formaPago = formaPago;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getAtendidoPor() {
        return atendidoPor;
    }

    public void setAtendidoPor(String atendidoPor) {
        this.atendidoPor = atendidoPor;
    }

    // Métodos de utilidad
    public void calcularTotal() {
        this.total = this.items.stream()
                .mapToDouble(item -> item.getCantidad() * item.getPrecioUnitario())
                .sum();
    }

    public void agregarItem(ItemFactura item) {
        this.items.add(item);
        calcularTotal();
    }

    public void removerItem(int indice) {
        if (indice >= 0 && indice < this.items.size()) {
            this.items.remove(indice);
            calcularTotal();
        }
    }
}
