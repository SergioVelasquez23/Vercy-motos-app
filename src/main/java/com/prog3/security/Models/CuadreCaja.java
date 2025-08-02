package com.prog3.security.Models;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class CuadreCaja {

    private String _id;
    private String nombre;        // Nombre de la caja (ej: "Caja Principal", "Caja 1")
    private String responsable;
    private String identificacionMaquina; // Para registrar en qué computadora se hizo el cierre
    private List<String> cajeros = new ArrayList<>(); // Lista de cajeros que trabajaron durante el turno
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;
    private double fondoInicial;  // El dinero con el que se inicia la caja
    private Map<String, Double> fondoInicialDesglosado = new HashMap<>(); // Fondo inicial desglosado por medio de pago (efectivo, transferencia)

    // Ventas
    private double totalVentas; // Total de ventas
    private Map<String, Double> ventasDesglosadas = new HashMap<>(); // Ventas desglosadas por medio de pago
    private double totalPropinas; // Total de propinas

    // Gastos
    private double totalGastos; // Total de gastos
    private Map<String, Double> gastosDesglosados = new HashMap<>(); // Gastos desglosados por tipo
    private double totalPagosFacturas; // Total pagos a facturas de compras

    // Resumen
    private double efectivoDeclarado;
    private double efectivoEsperado;
    private double diferencia;
    private boolean cuadrado;
    private boolean cerrada;      // Indica si la caja está cerrada o abierta
    private double tolerancia;
    private String observaciones;
    private String estado; // "pendiente", "aprobado", "rechazado"
    private String aprobadoPor;
    private LocalDateTime fechaAprobacion;
    private String urlComprobanteDiario; // URL al comprobante diario
    private String urlInventario;        // URL al inventario
    private double totalDomicilios; // Total de domicilios

    // Constructor vacío requerido por MongoDB
    public CuadreCaja() {
    }

    // Constructor con campos básicos
    public CuadreCaja(String nombre, String responsable, double fondoInicial, double efectivoDeclarado, double efectivoEsperado,
            double tolerancia, String observaciones) {
        this.nombre = nombre;
        this.responsable = responsable;
        this.fechaApertura = LocalDateTime.now();
        this.fondoInicial = fondoInicial;
        this.efectivoDeclarado = efectivoDeclarado;
        this.efectivoEsperado = efectivoEsperado;
        this.diferencia = efectivoDeclarado - efectivoEsperado;
        this.tolerancia = tolerancia;
        this.cuadrado = Math.abs(this.diferencia) <= tolerancia;
        this.observaciones = observaciones;
        this.estado = "pendiente";
        this.cerrada = false;

        // Inicializar los nuevos campos
        this.fondoInicialDesglosado = new HashMap<>();
        this.ventasDesglosadas = new HashMap<>();
        this.gastosDesglosados = new HashMap<>();
        this.totalVentas = 0.0;
        this.totalPropinas = 0.0;
        this.totalGastos = 0.0;
        this.totalPagosFacturas = 0.0;
        this.totalDomicilios = 0.0;
        this.cajeros = new ArrayList<>();

        // Por defecto, el fondo inicial va todo en efectivo
        this.fondoInicialDesglosado.put("Efectivo", fondoInicial);
    }

    // Getters y setters
    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDateTime getFechaApertura() {
        return fechaApertura;
    }

    public void setFechaApertura(LocalDateTime fechaApertura) {
        this.fechaApertura = fechaApertura;
    }

    public LocalDateTime getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDateTime fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public double getFondoInicial() {
        return fondoInicial;
    }

    public void setFondoInicial(double fondoInicial) {
        this.fondoInicial = fondoInicial;
    }

    public double getEfectivoDeclarado() {
        return efectivoDeclarado;
    }

    public void setEfectivoDeclarado(double efectivoDeclarado) {
        this.efectivoDeclarado = efectivoDeclarado;
        // Recalcular diferencia y estado de cuadre
        if (this.efectivoEsperado != 0) {
            this.diferencia = efectivoDeclarado - this.efectivoEsperado;
            this.cuadrado = Math.abs(this.diferencia) <= this.tolerancia;
        }
    }

    public double getEfectivoEsperado() {
        return efectivoEsperado;
    }

    public void setEfectivoEsperado(double efectivoEsperado) {
        this.efectivoEsperado = efectivoEsperado;
        // Recalcular diferencia y estado de cuadre
        this.diferencia = this.efectivoDeclarado - efectivoEsperado;
        this.cuadrado = Math.abs(this.diferencia) <= this.tolerancia;
    }

    public double getDiferencia() {
        return diferencia;
    }

    public void setDiferencia(double diferencia) {
        this.diferencia = diferencia;
    }

    public boolean isCuadrado() {
        return cuadrado;
    }

    public void setCuadrado(boolean cuadrado) {
        this.cuadrado = cuadrado;
    }

    public double getTolerancia() {
        return tolerancia;
    }

    public void setTolerancia(double tolerancia) {
        this.tolerancia = tolerancia;
        // Recalcular estado de cuadre con la nueva tolerancia
        this.cuadrado = Math.abs(this.diferencia) <= tolerancia;
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

    public String getAprobadoPor() {
        return aprobadoPor;
    }

    public void setAprobadoPor(String aprobadoPor) {
        this.aprobadoPor = aprobadoPor;
    }

    public LocalDateTime getFechaAprobacion() {
        return fechaAprobacion;
    }

    public void setFechaAprobacion(LocalDateTime fechaAprobacion) {
        this.fechaAprobacion = fechaAprobacion;
    }

    public boolean isCerrada() {
        return cerrada;
    }

    public void setCerrada(boolean cerrada) {
        this.cerrada = cerrada;
    }

    public String getUrlComprobanteDiario() {
        return urlComprobanteDiario;
    }

    public void setUrlComprobanteDiario(String urlComprobanteDiario) {
        this.urlComprobanteDiario = urlComprobanteDiario;
    }

    public String getUrlInventario() {
        return urlInventario;
    }

    public void setUrlInventario(String urlInventario) {
        this.urlInventario = urlInventario;
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

    public void addCajero(String cajero) {
        if (this.cajeros == null) {
            this.cajeros = new ArrayList<>();
        }
        this.cajeros.add(cajero);
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
