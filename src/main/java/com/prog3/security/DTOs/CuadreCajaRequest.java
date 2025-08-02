package com.prog3.security.DTOs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class CuadreCajaRequest {

    private String nombre;          // Nombre de la caja (ej: "Caja Principal")
    private String responsable;
    private String identificacionMaquina; // Para registrar en qué computadora se hizo el cierre
    private List<String> cajeros = new ArrayList<>(); // Lista de cajeros que trabajaron durante el turno
    private double fondoInicial;    // Dinero inicial en caja (fondo)
    private Map<String, Double> fondoInicialDesglosado = new HashMap<>(); // Efectivo, transferencia
    private double efectivoDeclarado;
    private double tolerancia = 1000.0; // Tolerancia por defecto de $1000
    private String observaciones;
    private boolean cerrarCaja;     // Indica si se debe cerrar la caja

    // Ventas
    private double totalVentas;
    private Map<String, Double> ventasDesglosadas = new HashMap<>();
    private double totalPropinas;

    // Gastos
    private double totalGastos;
    private Map<String, Double> gastosDesglosados = new HashMap<>();
    private double totalPagosFacturas;

    // Domicilios
    private double totalDomicilios;

    public CuadreCajaRequest() {
    }

    public CuadreCajaRequest(String nombre, String responsable, double fondoInicial,
            double efectivoDeclarado, double tolerancia,
            String observaciones, boolean cerrarCaja) {
        this.nombre = nombre;
        this.responsable = responsable;
        this.fondoInicial = fondoInicial;
        this.efectivoDeclarado = efectivoDeclarado;
        this.tolerancia = tolerancia;
        this.observaciones = observaciones;
        this.cerrarCaja = cerrarCaja;
    }

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }

    public double getFondoInicial() {
        return fondoInicial;
    }

    public void setFondoInicial(double fondoInicial) {
        this.fondoInicial = fondoInicial;
    }

    public boolean isCerrarCaja() {
        return cerrarCaja;
    }

    public void setCerrarCaja(boolean cerrarCaja) {
        this.cerrarCaja = cerrarCaja;
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

    public String getIdentificacionMaquina() {
        return identificacionMaquina;
    }

    public void setIdentificacionMaquina(String identificacionMaquina) {
        this.identificacionMaquina = identificacionMaquina;
    }

    public List<String> getCajeros() {
        return cajeros;
    }

    public void setCajeros(List<String> cajeros) {
        this.cajeros = cajeros;
    }

    public Map<String, Double> getFondoInicialDesglosado() {
        return fondoInicialDesglosado;
    }

    public void setFondoInicialDesglosado(Map<String, Double> fondoInicialDesglosado) {
        this.fondoInicialDesglosado = fondoInicialDesglosado;
    }

    public double getTotalVentas() {
        return totalVentas;
    }

    public void setTotalVentas(double totalVentas) {
        this.totalVentas = totalVentas;
    }

    public Map<String, Double> getVentasDesglosadas() {
        return ventasDesglosadas;
    }

    public void setVentasDesglosadas(Map<String, Double> ventasDesglosadas) {
        this.ventasDesglosadas = ventasDesglosadas;
    }

    public double getTotalPropinas() {
        return totalPropinas;
    }

    public void setTotalPropinas(double totalPropinas) {
        this.totalPropinas = totalPropinas;
    }

    public double getTotalGastos() {
        return totalGastos;
    }

    public void setTotalGastos(double totalGastos) {
        this.totalGastos = totalGastos;
    }

    public Map<String, Double> getGastosDesglosados() {
        return gastosDesglosados;
    }

    public void setGastosDesglosados(Map<String, Double> gastosDesglosados) {
        this.gastosDesglosados = gastosDesglosados;
    }

    public double getTotalPagosFacturas() {
        return totalPagosFacturas;
    }

    public void setTotalPagosFacturas(double totalPagosFacturas) {
        this.totalPagosFacturas = totalPagosFacturas;
    }

    public double getTotalDomicilios() {
        return totalDomicilios;
    }

    public void setTotalDomicilios(double totalDomicilios) {
        this.totalDomicilios = totalDomicilios;
    }
}
