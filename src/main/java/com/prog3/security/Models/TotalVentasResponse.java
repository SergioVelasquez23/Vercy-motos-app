package com.prog3.security.Models;

public class TotalVentasResponse {

    private double total;
    private double totalEfectivo;
    private double totalTransferencia;
    private double totalTarjeta;
    private double totalOtros;

    public TotalVentasResponse(double total) {
        this.total = total;
        this.totalEfectivo = 0.0;
        this.totalTransferencia = 0.0;
        this.totalTarjeta = 0.0;
        this.totalOtros = 0.0;
    }

    public TotalVentasResponse(double total, double totalEfectivo, double totalTransferencia, double totalTarjeta, double totalOtros) {
        this.total = total;
        this.totalEfectivo = totalEfectivo;
        this.totalTransferencia = totalTransferencia;
        this.totalTarjeta = totalTarjeta;
        this.totalOtros = totalOtros;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getTotalEfectivo() {
        return totalEfectivo;
    }

    public void setTotalEfectivo(double totalEfectivo) {
        this.totalEfectivo = totalEfectivo;
    }

    public double getTotalTransferencia() {
        return totalTransferencia;
    }

    public void setTotalTransferencia(double totalTransferencia) {
        this.totalTransferencia = totalTransferencia;
    }

    public double getTotalTarjeta() {
        return totalTarjeta;
    }

    public void setTotalTarjeta(double totalTarjeta) {
        this.totalTarjeta = totalTarjeta;
    }

    public double getTotalOtros() {
        return totalOtros;
    }

    public void setTotalOtros(double totalOtros) {
        this.totalOtros = totalOtros;
    }
}
