package com.prog3.security.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * Modelo para el manejo de deudas
 */
@Document(collection = "deudas")
public class Deuda {

    @Id
    private String _id;
    
    // Información del pedido/mesa
    private String pedidoId;                    // ID del pedido que generó la deuda
    private String mesaId;                      // ID de la mesa
    private String mesaNombre;                  // Nombre de la mesa
    
    // Información financiera
    private double montoTotal;                  // Monto total de la deuda
    private double montoPagado;                 // Monto ya pagado
    private double montoDeuda;                  // Monto pendiente por pagar
    
    // Fechas
    private LocalDateTime fechaCreacion;        // Fecha de creación de la deuda
    private LocalDateTime fechaVencimiento;     // Fecha de vencimiento (opcional)
    private LocalDateTime fechaUltimoPago;      // Fecha del último pago
    
    // Información del cliente
    private String clienteInfo;                 // Información del cliente
    private String descripcion;                 // Descripción de la deuda
    
    // Estado y pagos
    private List<PagoDeuda> pagos;              // Lista de pagos realizados
    private boolean activa;                     // Si la deuda está activa
    
    // Metadata
    private String creadaPor;                   // Usuario que creó la deuda
    private String ultimoModificadoPor;         // Usuario que realizó la última modificación

    // Constructores
    public Deuda() {
        this.fechaCreacion = LocalDateTime.now();
        this.pagos = new ArrayList<>();
        this.activa = true;
        this.montoPagado = 0.0;
    }

    public Deuda(String pedidoId, String mesaId, String mesaNombre, 
                 double montoTotal, String clienteInfo, String creadaPor) {
        this();
        this.pedidoId = pedidoId;
        this.mesaId = mesaId;
        this.mesaNombre = mesaNombre;
        this.montoTotal = montoTotal;
        this.montoDeuda = montoTotal;
        this.clienteInfo = clienteInfo;
        this.creadaPor = creadaPor;
    }

    // Getters y Setters
    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(String pedidoId) {
        this.pedidoId = pedidoId;
    }

    public String getMesaId() {
        return mesaId;
    }

    public void setMesaId(String mesaId) {
        this.mesaId = mesaId;
    }

    public String getMesaNombre() {
        return mesaNombre;
    }

    public void setMesaNombre(String mesaNombre) {
        this.mesaNombre = mesaNombre;
    }

    public double getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(double montoTotal) {
        this.montoTotal = montoTotal;
        this.recalcularMontoDeuda();
    }

    public double getMontoPagado() {
        return montoPagado;
    }

    public void setMontoPagado(double montoPagado) {
        this.montoPagado = montoPagado;
        this.recalcularMontoDeuda();
    }

    public double getMontoDeuda() {
        return montoDeuda;
    }

    public void setMontoDeuda(double montoDeuda) {
        this.montoDeuda = montoDeuda;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDateTime fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public LocalDateTime getFechaUltimoPago() {
        return fechaUltimoPago;
    }

    public void setFechaUltimoPago(LocalDateTime fechaUltimoPago) {
        this.fechaUltimoPago = fechaUltimoPago;
    }

    public String getClienteInfo() {
        return clienteInfo;
    }

    public void setClienteInfo(String clienteInfo) {
        this.clienteInfo = clienteInfo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<PagoDeuda> getPagos() {
        return pagos;
    }

    public void setPagos(List<PagoDeuda> pagos) {
        this.pagos = pagos;
        this.recalcularMontoPagado();
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    public String getCreadaPor() {
        return creadaPor;
    }

    public void setCreadaPor(String creadaPor) {
        this.creadaPor = creadaPor;
    }

    public String getUltimoModificadoPor() {
        return ultimoModificadoPor;
    }

    public void setUltimoModificadoPor(String ultimoModificadoPor) {
        this.ultimoModificadoPor = ultimoModificadoPor;
    }

    // Métodos utilitarios
    public void agregarPago(PagoDeuda pago) {
        if (this.pagos == null) {
            this.pagos = new ArrayList<>();
        }
        this.pagos.add(pago);
        this.recalcularMontoPagado();
        this.fechaUltimoPago = LocalDateTime.now();
        
        // Si ya está completamente pagada, desactivar la deuda
        if (this.montoDeuda <= 0) {
            this.activa = false;
        }
    }

    public void recalcularMontoPagado() {
        if (this.pagos != null) {
            this.montoPagado = this.pagos.stream()
                    .mapToDouble(PagoDeuda::getMonto)
                    .sum();
        }
        this.recalcularMontoDeuda();
    }

    public void recalcularMontoDeuda() {
        this.montoDeuda = this.montoTotal - this.montoPagado;
        if (this.montoDeuda < 0) {
            this.montoDeuda = 0;
        }
    }

    public boolean estaVencida() {
        return this.fechaVencimiento != null && 
               LocalDateTime.now().isAfter(this.fechaVencimiento) && 
               this.activa;
    }

    public boolean estaCompletamentePagada() {
        return this.montoDeuda <= 0;
    }

    public double getPorcentajePagado() {
        if (this.montoTotal <= 0) {
            return 0.0;
        }
        return (this.montoPagado / this.montoTotal) * 100.0;
    }
}