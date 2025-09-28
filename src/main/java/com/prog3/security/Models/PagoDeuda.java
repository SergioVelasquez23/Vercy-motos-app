package com.prog3.security.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

/**
 * Modelo para registrar pagos de deudas
 */
@Document(collection = "pagos_deuda")
public class PagoDeuda {

    @Id
    private String _id;
    
    private String deudaId;                     // ID de la deuda asociada
    private double monto;                       // Monto del pago
    private String metodoPago;                  // Método de pago utilizado
    private LocalDateTime fechaPago;            // Fecha del pago
    private String recibidoPor;                 // Usuario que recibió el pago
    private String observaciones;               // Observaciones del pago

    // Constructores
    public PagoDeuda() {
        this.fechaPago = LocalDateTime.now();
    }

    public PagoDeuda(String deudaId, double monto, String metodoPago, String recibidoPor) {
        this();
        this.deudaId = deudaId;
        this.monto = monto;
        this.metodoPago = metodoPago;
        this.recibidoPor = recibidoPor;
    }

    // Getters y Setters
    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getDeudaId() {
        return deudaId;
    }

    public void setDeudaId(String deudaId) {
        this.deudaId = deudaId;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }

    public String getRecibidoPor() {
        return recibidoPor;
    }

    public void setRecibidoPor(String recibidoPor) {
        this.recibidoPor = recibidoPor;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}