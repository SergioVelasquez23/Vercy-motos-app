package com.prog3.security.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "ingreso_caja")
public class IngresoCaja {
    @Id
    private String id;
    private String cuadreCajaId; // ID de la caja a la que pertenece el ingreso
    private String concepto;
    private double monto;
    private String formaPago; // efectivo, transferencia, etc.
    private LocalDateTime fechaIngreso;
    private String responsable;
    private String observaciones;

    public IngresoCaja() {}


    public IngresoCaja(String cuadreCajaId, String concepto, double monto, String formaPago, LocalDateTime fechaIngreso, String responsable, String observaciones) {
        this.cuadreCajaId = cuadreCajaId;
        this.concepto = concepto;
        this.monto = monto;
        this.formaPago = formaPago;
        this.fechaIngreso = fechaIngreso;
        this.responsable = responsable;
        this.observaciones = observaciones;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCuadreCajaId() { return cuadreCajaId; }
    public void setCuadreCajaId(String cuadreCajaId) { this.cuadreCajaId = cuadreCajaId; }
    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }
    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }
    public String getFormaPago() { return formaPago; }
    public void setFormaPago(String formaPago) { this.formaPago = formaPago; }
    public LocalDateTime getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDateTime fechaIngreso) { this.fechaIngreso = fechaIngreso; }
    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
