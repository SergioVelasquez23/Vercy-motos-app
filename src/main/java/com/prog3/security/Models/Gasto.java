package com.prog3.security.Models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Gasto {

    private String _id;
    private String cuadreCajaId;  // ID del cuadre de caja al que pertenece
    private String tipoGastoId; // ID del tipo de gasto (Categoría)
    private String tipoGastoNombre; // Nombre del tipo de gasto (para no tener que join)
    private String concepto; // Descripción general del gasto
    private double monto; // Monto total del gasto
    private String responsable;   // Responsable del gasto
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaGasto; // Fecha del gasto
    private LocalDateTime fechaVencimiento; // Fecha de vencimiento
    private String numeroRecibo;
    private String numeroFactura;
    private String proveedor; // Proveedor del gasto
    private String proveedorId; // ID del proveedor (si existe en BD)
    private String formaPago; // "efectivo", "transferencia", etc.
    private boolean documentoSoporte; // Indica si tiene documento soporte

    @JsonProperty("pagadoDesdeCaja")
    private Boolean pagadoDesdeCaja; // Indica si el gasto fue pagado desde la caja

    // Items del gasto (para gastos con múltiples líneas/conceptos)
    private List<ItemGasto> items;

    // Campos de cálculos
    private double subtotal; // Suma de valores base de items
    private double totalDescuentos; // Total de descuentos aplicados
    private double totalImpuestos; // Total de impuestos (IVA, etc.)

    // 💰 Campos de retenciones (similar a Factura)
    private double baseGravable; // Base sobre la que se calculan retenciones
    private double porcentajeRetencion; // % Retención en la fuente
    private double valorRetencion; // Valor de retención en la fuente
    private double porcentajeReteIva; // % Retención de IVA
    private double valorReteIva; // Valor de retención de IVA
    private double porcentajeReteIca; // % Retención de ICA
    private double valorReteIca; // Valor de retención de ICA
    private double totalRetenciones; // Total de retenciones aplicadas

    // Campo legacy para compatibilidad
    private double impuestos; // Valor de impuestos (legacy)
    private String estado; // "pendiente", "aprobado", "rechazado"

    // Constructor vacío
    public Gasto() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaGasto = LocalDateTime.now();
        this.estado = "pendiente";
        this.formaPago = "efectivo"; // Por defecto
        this.pagadoDesdeCaja = Boolean.FALSE; // Por defecto no se paga desde caja
        this.documentoSoporte = false;
        this.items = new ArrayList<>();
        this.subtotal = 0.0;
        this.totalDescuentos = 0.0;
        this.totalImpuestos = 0.0;
        this.totalRetenciones = 0.0;
        this.monto = 0.0;
    }

    // Constructor con campos básicos
    public Gasto(String cuadreCajaId, String tipoGastoId, String tipoGastoNombre, String concepto, double monto,
            String responsable) {
        this();
        this.cuadreCajaId = cuadreCajaId;
        this.tipoGastoId = tipoGastoId;
        this.tipoGastoNombre = tipoGastoNombre;
        this.concepto = concepto;
        this.monto = monto;
        this.responsable = responsable;
        this.subtotal = monto; // Por defecto el subtotal es igual al monto
    }

    /**
     * Calcula los totales del gasto basándose en los items
     */
    public void calcularTotales() {
        if (this.items == null || this.items.isEmpty()) {
            // Si no hay items, usar valores directos
            this.baseGravable = this.subtotal - this.totalDescuentos + this.totalImpuestos;
        } else {
            // Calcular desde items
            double subTotal = 0.0;
            double descuentos = 0.0;
            double impuestosItems = 0.0;

            for (ItemGasto item : this.items) {
                item.calcularTotales();
                subTotal += item.getValor();
                descuentos += item.getValorDescuento();
                impuestosItems += item.getValorImpuesto();
            }

            this.subtotal = subTotal;
            this.totalDescuentos = descuentos;
            this.totalImpuestos = impuestosItems;
            this.baseGravable = subTotal - descuentos + impuestosItems;
        }

        // Calcular retenciones
        this.valorRetencion = this.baseGravable * (this.porcentajeRetencion / 100.0);
        this.valorReteIva = this.totalImpuestos * (this.porcentajeReteIva / 100.0);
        this.valorReteIca = this.baseGravable * (this.porcentajeReteIca / 100.0);
        this.totalRetenciones = this.valorRetencion + this.valorReteIva + this.valorReteIca;

        // Calcular monto total = baseGravable - retenciones
        this.monto = this.baseGravable - this.totalRetenciones;
    }

    /**
     * Agrega un item al gasto
     */
    public void agregarItem(ItemGasto item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
        calcularTotales();
    }

    /**
     * Remueve un item del gasto por índice
     */
    public void removerItem(int indice) {
        if (this.items != null && indice >= 0 && indice < this.items.size()) {
            this.items.remove(indice);
            calcularTotales();
        }
    }

    // Getters y setters
    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getCuadreCajaId() {
        return cuadreCajaId;
    }

    public void setCuadreCajaId(String cuadreCajaId) {
        this.cuadreCajaId = cuadreCajaId;
    }

    public String getTipoGastoId() {
        return tipoGastoId;
    }

    public void setTipoGastoId(String tipoGastoId) {
        this.tipoGastoId = tipoGastoId;
    }

    public String getTipoGastoNombre() {
        return tipoGastoNombre;
    }

    public void setTipoGastoNombre(String tipoGastoNombre) {
        this.tipoGastoNombre = tipoGastoNombre;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaGasto() {
        return fechaGasto;
    }

    public void setFechaGasto(LocalDateTime fechaGasto) {
        this.fechaGasto = fechaGasto;
    }

    public String getNumeroRecibo() {
        return numeroRecibo;
    }

    public void setNumeroRecibo(String numeroRecibo) {
        this.numeroRecibo = numeroRecibo;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(String numeroFactura) {
        this.numeroFactura = numeroFactura;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    public String getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(String formaPago) {
        this.formaPago = formaPago;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getImpuestos() {
        return impuestos;
    }

    public void setImpuestos(double impuestos) {
        this.impuestos = impuestos;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public boolean isPagadoDesdeCaja() {
        // ✅ Manejar valores nulos para gastos existentes sin este campo
        return pagadoDesdeCaja != null ? pagadoDesdeCaja : false;
    }

    public void setPagadoDesdeCaja(boolean pagadoDesdeCaja) {
        this.pagadoDesdeCaja = Boolean.valueOf(pagadoDesdeCaja);
    }

    // Getters y Setters para nuevos campos

    public LocalDateTime getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDateTime fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public String getProveedorId() {
        return proveedorId;
    }

    public void setProveedorId(String proveedorId) {
        this.proveedorId = proveedorId;
    }

    public boolean isDocumentoSoporte() {
        return documentoSoporte;
    }

    public void setDocumentoSoporte(boolean documentoSoporte) {
        this.documentoSoporte = documentoSoporte;
    }

    public List<ItemGasto> getItems() {
        return items;
    }

    public void setItems(List<ItemGasto> items) {
        this.items = items;
        calcularTotales();
    }

    public double getTotalDescuentos() {
        return totalDescuentos;
    }

    public void setTotalDescuentos(double totalDescuentos) {
        this.totalDescuentos = totalDescuentos;
    }

    public double getTotalImpuestos() {
        return totalImpuestos;
    }

    public void setTotalImpuestos(double totalImpuestos) {
        this.totalImpuestos = totalImpuestos;
    }

    // Getters y Setters para retenciones

    public double getBaseGravable() {
        return baseGravable;
    }

    public void setBaseGravable(double baseGravable) {
        this.baseGravable = baseGravable;
    }

    public double getPorcentajeRetencion() {
        return porcentajeRetencion;
    }

    public void setPorcentajeRetencion(double porcentajeRetencion) {
        this.porcentajeRetencion = porcentajeRetencion;
    }

    public double getValorRetencion() {
        return valorRetencion;
    }

    public void setValorRetencion(double valorRetencion) {
        this.valorRetencion = valorRetencion;
    }

    public double getPorcentajeReteIva() {
        return porcentajeReteIva;
    }

    public void setPorcentajeReteIva(double porcentajeReteIva) {
        this.porcentajeReteIva = porcentajeReteIva;
    }

    public double getValorReteIva() {
        return valorReteIva;
    }

    public void setValorReteIva(double valorReteIva) {
        this.valorReteIva = valorReteIva;
    }

    public double getPorcentajeReteIca() {
        return porcentajeReteIca;
    }

    public void setPorcentajeReteIca(double porcentajeReteIca) {
        this.porcentajeReteIca = porcentajeReteIca;
    }

    public double getValorReteIca() {
        return valorReteIca;
    }

    public void setValorReteIca(double valorReteIca) {
        this.valorReteIca = valorReteIca;
    }

    public double getTotalRetenciones() {
        return totalRetenciones;
    }

    public void setTotalRetenciones(double totalRetenciones) {
        this.totalRetenciones = totalRetenciones;
    }
}
