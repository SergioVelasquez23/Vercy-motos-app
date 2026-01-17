package com.prog3.security.DTOs;

public class StockBodegaDTO {
    private String bodegaId;
    private String bodegaNombre;
    private String itemId;
    private String itemNombre;
    private String tipoItem;
    private double stockActual;
    private double stockMinimo;
    private double stockMaximo;
    private String ubicacionFisica;
    private boolean stockBajo;

    public StockBodegaDTO() {}

    public String getBodegaId() {
        return bodegaId;
    }

    public void setBodegaId(String bodegaId) {
        this.bodegaId = bodegaId;
    }

    public String getBodegaNombre() {
        return bodegaNombre;
    }

    public void setBodegaNombre(String bodegaNombre) {
        this.bodegaNombre = bodegaNombre;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemNombre() {
        return itemNombre;
    }

    public void setItemNombre(String itemNombre) {
        this.itemNombre = itemNombre;
    }

    public String getTipoItem() {
        return tipoItem;
    }

    public void setTipoItem(String tipoItem) {
        this.tipoItem = tipoItem;
    }

    public double getStockActual() {
        return stockActual;
    }

    public void setStockActual(double stockActual) {
        this.stockActual = stockActual;
    }

    public double getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(double stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public double getStockMaximo() {
        return stockMaximo;
    }

    public void setStockMaximo(double stockMaximo) {
        this.stockMaximo = stockMaximo;
    }

    public String getUbicacionFisica() {
        return ubicacionFisica;
    }

    public void setUbicacionFisica(String ubicacionFisica) {
        this.ubicacionFisica = ubicacionFisica;
    }

    public boolean isStockBajo() {
        return stockBajo;
    }

    public void setStockBajo(boolean stockBajo) {
        this.stockBajo = stockBajo;
    }
}
