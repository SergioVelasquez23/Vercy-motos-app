package com.prog3.security.Models;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

public class Reporte {

    private Map<String, Object> ventasHoy;
    private Map<String, Object> pedidosHoy;
    private Map<String, Object> inventario;
    private Map<String, Object> facturacion;
    private LocalDateTime fecha;

    public Reporte() {
        this.ventasHoy = new HashMap<>();
        this.pedidosHoy = new HashMap<>();
        this.inventario = new HashMap<>();
        this.facturacion = new HashMap<>();
        this.fecha = LocalDateTime.now();
    }

    // Getters y Setters
    public Map<String, Object> getVentasHoy() {
        return ventasHoy;
    }

    public void setVentasHoy(Map<String, Object> ventasHoy) {
        this.ventasHoy = ventasHoy;
    }

    public Map<String, Object> getPedidosHoy() {
        return pedidosHoy;
    }

    public void setPedidosHoy(Map<String, Object> pedidosHoy) {
        this.pedidosHoy = pedidosHoy;
    }

    public Map<String, Object> getInventario() {
        return inventario;
    }

    public void setInventario(Map<String, Object> inventario) {
        this.inventario = inventario;
    }

    public Map<String, Object> getFacturacion() {
        return facturacion;
    }

    public void setFacturacion(Map<String, Object> facturacion) {
        this.facturacion = facturacion;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
