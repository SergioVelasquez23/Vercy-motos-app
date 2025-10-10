package com.prog3.security.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;

/**
 * DTO para representar un pago parcial en pagos mixtos.
 * Permite especificar una forma de pago y un monto específico.
 */
public class PagoMixto {

    @NotBlank(message = "La forma de pago es obligatoria")
    @Pattern(regexp = "^(efectivo|transferencia|tarjeta|otro)$", 
             message = "La forma de pago debe ser: efectivo, transferencia, tarjeta u otro")
    private String formaPago;

    @Positive(message = "El monto debe ser mayor que cero")
    private double monto;

    // Constructores
    public PagoMixto() {
    }

    public PagoMixto(String formaPago, double monto) {
        this.formaPago = formaPago;
        this.monto = monto;
    }

    // Getters y setters
    public String getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(String formaPago) {
        this.formaPago = formaPago;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    @Override
    public String toString() {
        return "PagoMixto{" +
                "formaPago='" + formaPago + '\'' +
                ", monto=" + monto +
                '}';
    }
}