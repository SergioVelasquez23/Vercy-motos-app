package com.prog3.security.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Document
public class Factura {

    public boolean getPagadoDesdeCaja() {
        return pagadoDesdeCaja;
    }

    private String cuadreCajaId;

    public String getCuadreCajaId() {
        return cuadreCajaId;
    }

    public void setCuadreCajaId(String cuadreCajaId) {
        this.cuadreCajaId = cuadreCajaId;
    }

    @Id
    private String _id;

    // Información básica de la factura
    private String numero;
    private LocalDateTime fecha;
    private LocalDateTime fechaVencimiento; // Fecha de vencimiento de la factura
    private String tipoFactura; // "venta" o "compra"

    // Información del cliente (para facturas de venta)
    private String nit;
    private String clienteNombre; // Nombre completo del cliente
    private String clienteTelefono;
    private String clienteEmail; // Email del cliente
    private String clienteDireccion;
    private String atendidoPor;

    // Información del proveedor (para facturas de compra)
    private String proveedorNit;
    private String proveedorNombre;
    private String proveedorTelefono;
    private String proveedorDireccion;

    // Items de la factura
    private List<ItemFactura> items; // Para facturas de venta
    private List<ItemFacturaIngrediente> itemsIngredientes; // Para facturas de compra

    // Información de pago
    private String medioPago;  // "Efectivo", "Transferencia", etc.
    private String formaPago;  // "Contado", "Crédito"

    // Totales calculados
    private double subtotal = 0.0; // Suma sin impuestos ni descuentos
    private double totalImpuestos = 0.0; // Total de impuestos
    private double totalDescuentos = 0.0; // Total de descuentos aplicados
    private String tipoDescuento = "Valor"; // "Valor" o "Porcentaje"
    private double descuentoGeneral = 0.0; // Descuento general aplicado
    private double total;

    // Control de pago (especialmente para facturas de compras)
    private boolean pagadoDesdeCaja;    // Indica si el pago sale de la caja registradora

    // Información adicional
    private String registradoPor;  // Usuario que registra la factura
    private String observaciones;  // Observaciones generales

    public Factura() {
        this.fecha = LocalDateTime.now();
        this.items = new ArrayList<>();
        this.itemsIngredientes = new ArrayList<>();
        this.medioPago = "Efectivo";
        this.formaPago = "Contado";
        this.total = 0.0;
        this.tipoFactura = "venta"; // Por defecto es factura de venta
        this.pagadoDesdeCaja = false; // Por defecto no sale de caja
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

    public String getTipoFactura() {
        return tipoFactura;
    }

    public void setTipoFactura(String tipoFactura) {
        this.tipoFactura = tipoFactura;
    }

    public LocalDateTime getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDateTime fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    // Getters y setters para información del cliente
    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public String getClienteTelefono() {
        return clienteTelefono;
    }

    public void setClienteTelefono(String clienteTelefono) {
        this.clienteTelefono = clienteTelefono;
    }

    public String getClienteEmail() {
        return clienteEmail;
    }

    public void setClienteEmail(String clienteEmail) {
        this.clienteEmail = clienteEmail;
    }

    public String getClienteDireccion() {
        return clienteDireccion;
    }

    public void setClienteDireccion(String clienteDireccion) {
        this.clienteDireccion = clienteDireccion;
    }

    public String getAtendidoPor() {
        return atendidoPor;
    }

    public void setAtendidoPor(String atendidoPor) {
        this.atendidoPor = atendidoPor;
    }

    // Getters y setters para información del proveedor
    public String getProveedorNit() {
        return proveedorNit;
    }

    public void setProveedorNit(String proveedorNit) {
        this.proveedorNit = proveedorNit;
    }

    public String getProveedorNombre() {
        return proveedorNombre;
    }

    public void setProveedorNombre(String proveedorNombre) {
        this.proveedorNombre = proveedorNombre;
    }

    public String getProveedorTelefono() {
        return proveedorTelefono;
    }

    public void setProveedorTelefono(String proveedorTelefono) {
        this.proveedorTelefono = proveedorTelefono;
    }

    public String getProveedorDireccion() {
        return proveedorDireccion;
    }

    public void setProveedorDireccion(String proveedorDireccion) {
        this.proveedorDireccion = proveedorDireccion;
    }

    // Getters y setters para items de venta
    public List<ItemFactura> getItems() {
        return items;
    }

    public void setItems(List<ItemFactura> items) {
        this.items = items;
        calcularTotal();
    }

    public List<ItemFacturaIngrediente> getItemsIngredientes() {
        return itemsIngredientes;
    }

    public void setItemsIngredientes(List<ItemFacturaIngrediente> itemsIngredientes) {
        this.itemsIngredientes = itemsIngredientes;
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

    // Getters y setters para totales calculados
    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getTotalImpuestos() {
        return totalImpuestos;
    }

    public void setTotalImpuestos(double totalImpuestos) {
        this.totalImpuestos = totalImpuestos;
    }

    public double getTotalDescuentos() {
        return totalDescuentos;
    }

    public void setTotalDescuentos(double totalDescuentos) {
        this.totalDescuentos = totalDescuentos;
    }

    public String getTipoDescuento() {
        return tipoDescuento;
    }

    public void setTipoDescuento(String tipoDescuento) {
        this.tipoDescuento = tipoDescuento;
    }

    public double getDescuentoGeneral() {
        return descuentoGeneral;
    }

    public void setDescuentoGeneral(double descuentoGeneral) {
        this.descuentoGeneral = descuentoGeneral;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getRegistradoPor() {
        return registradoPor;
    }

    public void setRegistradoPor(String registradoPor) {
        this.registradoPor = registradoPor;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public boolean isPagadoDesdeCaja() {
        return pagadoDesdeCaja;
    }

    public void setPagadoDesdeCaja(boolean pagadoDesdeCaja) {
        this.pagadoDesdeCaja = pagadoDesdeCaja;
    }

    // Métodos de utilidad
    public void calcularTotal() {
        double totalVenta = 0.0;
        double totalCompra = 0.0;
        double impuestosCalculados = 0.0;

        // Calcular total de items de venta
        if (this.items != null) {
            totalVenta = this.items.stream()
                    .mapToDouble(item -> item.getCantidad() * item.getPrecioUnitario())
                    .sum();
        }

        // Calcular total de items de compra
        if (this.itemsIngredientes != null) {
            totalCompra = this.itemsIngredientes.stream()
                    .mapToDouble(ItemFacturaIngrediente::getPrecioTotal)
                    .sum();
        }

        // Calcular subtotal (antes de impuestos y descuentos)
        this.subtotal = totalVenta + totalCompra;

        // Calcular descuento
        double descuentoAplicado = 0.0;
        if ("Porcentaje".equals(this.tipoDescuento) && this.descuentoGeneral > 0) {
            descuentoAplicado = this.subtotal * (this.descuentoGeneral / 100);
        } else {
            descuentoAplicado = this.descuentoGeneral;
        }
        this.totalDescuentos = descuentoAplicado;

        // Total final = subtotal + impuestos - descuentos
        this.total = this.subtotal + this.totalImpuestos - this.totalDescuentos;
    }

    // Métodos para facturas de venta
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

    // Métodos para facturas de compra
    public void agregarItemIngrediente(ItemFacturaIngrediente item) {
        this.itemsIngredientes.add(item);
        calcularTotal();
    }

    public void removerItemIngrediente(int indice) {
        if (indice >= 0 && indice < this.itemsIngredientes.size()) {
            this.itemsIngredientes.remove(indice);
            calcularTotal();
        }
    }

    // Método adicional para compatibilidad con el controlador
    public void calcularTotales() {
        calcularTotal();
    }

    @Override
    public String toString() {
        return "Factura{"
                + "_id='" + _id + '\''
                + ", numero='" + numero + '\''
                + ", fecha=" + fecha
                + ", proveedorNombre='" + proveedorNombre + '\''
                + ", total=" + total
                + ", itemsIngredientes=" + (itemsIngredientes != null ? itemsIngredientes.size() : 0)
                + '}';
    }
}
