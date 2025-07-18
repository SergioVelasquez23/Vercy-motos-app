package com.prog3.security.DTOs;

public class CuadreCajaRequest {

    private String responsable;
    private double efectivoDeclarado;
    private double tolerancia = 1000.0; // Tolerancia por defecto de $1000
    private String observaciones;

    public CuadreCajaRequest() {
    }

    public CuadreCajaRequest(String responsable, double efectivoDeclarado, double tolerancia, String observaciones) {
        this.responsable = responsable;
        this.efectivoDeclarado = efectivoDeclarado;
        this.tolerancia = tolerancia;
        this.observaciones = observaciones;
    }

    // Getters y Setters
    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }

    public double getEfectivoDeclarado() {
        return efectivoDeclarado;
    }

    public void setEfectivoDeclarado(double efectivoDeclarado) {
        this.efectivoDeclarado = efectivoDeclarado;
    }

    public double getTolerancia() {
        return tolerancia;
    }

    public void setTolerancia(double tolerancia) {
        this.tolerancia = tolerancia;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
