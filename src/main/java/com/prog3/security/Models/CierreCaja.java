package com.prog3.security.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

@Document(collection = "cierres_caja")
public class CierreCaja {

    @Id
    private String _id;

    private LocalDateTime fechaCierre;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String responsable;

    // Montos iniciales declarados
    private double efectivoInicial;
    private double transferenciasIniciales;
    private double totalInicial;

    // Ventas del sistema
    private double ventasEfectivo;
    private double ventasTransferencias;
    private double ventasTarjetas;
    private double totalVentas;
    private double totalPropinas;

    // Gastos detallados
    private Map<String, Double> gastosPorTipo; // {"Compras": 194200, "Administrativos": 44500}
    private double totalGastos;

    // Cálculos finales
    private double debeTener; // Lo que debería tener en efectivo
    private double efectivoDeclarado; // Lo que realmente se cuenta
    private double diferencia; // Diferencia entre debe tener y declarado
    private boolean cuadreOk;

    // Información adicional
    private int cantidadFacturas;
    private int cantidadPedidos;
    private String observaciones;
    private String estado; // "abierto", "cerrado"

    // Detalle de ventas por método de pago
    private Map<String, Double> detalleVentas;

    // Constructores
    public CierreCaja() {
        this.gastosPorTipo = new HashMap<>();
        this.detalleVentas = new HashMap<>();
    }

    public CierreCaja(LocalDateTime fechaInicio, LocalDateTime fechaFin, String responsable) {
        this();
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.responsable = responsable;
        this.fechaCierre = LocalDateTime.now();
        this.estado = "abierto";
    }

    // Getters y Setters
    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public LocalDateTime getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDateTime fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }

    public double getEfectivoInicial() {
        return efectivoInicial;
    }

    public void setEfectivoInicial(double efectivoInicial) {
        this.efectivoInicial = efectivoInicial;
    }

    public double getTransferenciasIniciales() {
        return transferenciasIniciales;
    }

    public void setTransferenciasIniciales(double transferenciasIniciales) {
        this.transferenciasIniciales = transferenciasIniciales;
    }

    public double getTotalInicial() {
        return totalInicial;
    }

    public void setTotalInicial(double totalInicial) {
        this.totalInicial = totalInicial;
    }

    public double getVentasEfectivo() {
        return ventasEfectivo;
    }

    public void setVentasEfectivo(double ventasEfectivo) {
        this.ventasEfectivo = ventasEfectivo;
    }

    public double getVentasTransferencias() {
        return ventasTransferencias;
    }

    public void setVentasTransferencias(double ventasTransferencias) {
        this.ventasTransferencias = ventasTransferencias;
    }

    public double getVentasTarjetas() {
        return ventasTarjetas;
    }

    public void setVentasTarjetas(double ventasTarjetas) {
        this.ventasTarjetas = ventasTarjetas;
    }

    public double getTotalVentas() {
        return totalVentas;
    }

    public void setTotalVentas(double totalVentas) {
        this.totalVentas = totalVentas;
    }

    public double getTotalPropinas() {
        return totalPropinas;
    }

    public void setTotalPropinas(double totalPropinas) {
        this.totalPropinas = totalPropinas;
    }

    public Map<String, Double> getGastosPorTipo() {
        return gastosPorTipo;
    }

    public void setGastosPorTipo(Map<String, Double> gastosPorTipo) {
        this.gastosPorTipo = gastosPorTipo;
    }

    public double getTotalGastos() {
        return totalGastos;
    }

    public void setTotalGastos(double totalGastos) {
        this.totalGastos = totalGastos;
    }

    public double getDebeTener() {
        return debeTener;
    }

    public void setDebeTener(double debeTener) {
        this.debeTener = debeTener;
    }

    public double getEfectivoDeclarado() {
        return efectivoDeclarado;
    }

    public void setEfectivoDeclarado(double efectivoDeclarado) {
        this.efectivoDeclarado = efectivoDeclarado;
    }

    public double getDiferencia() {
        return diferencia;
    }

    public void setDiferencia(double diferencia) {
        this.diferencia = diferencia;
    }

    public boolean isCuadreOk() {
        return cuadreOk;
    }

    public void setCuadreOk(boolean cuadreOk) {
        this.cuadreOk = cuadreOk;
    }

    public int getCantidadFacturas() {
        return cantidadFacturas;
    }

    public void setCantidadFacturas(int cantidadFacturas) {
        this.cantidadFacturas = cantidadFacturas;
    }

    public int getCantidadPedidos() {
        return cantidadPedidos;
    }

    public void setCantidadPedidos(int cantidadPedidos) {
        this.cantidadPedidos = cantidadPedidos;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Map<String, Double> getDetalleVentas() {
        return detalleVentas;
    }

    public void setDetalleVentas(Map<String, Double> detalleVentas) {
        this.detalleVentas = detalleVentas;
    }
}
