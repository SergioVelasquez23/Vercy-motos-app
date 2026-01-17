package com.prog3.security.DTOs;

public class GenerarCodigoBarrasRequest {
    private String itemId; // ID del producto o ingrediente
    private String tipoItem; // "producto" o "ingrediente"
    private String codigoPersonalizado; // Opcional: código personalizado
    private TipoCodigoBarras tipoCodigo; // EAN13, EAN8, CODE128

    public enum TipoCodigoBarras {
        EAN13, EAN8, CODE128, QR
    }

    public GenerarCodigoBarrasRequest() {
        this.tipoCodigo = TipoCodigoBarras.EAN13; // Por defecto
    }

    public GenerarCodigoBarrasRequest(String itemId, String tipoItem) {
        this.itemId = itemId;
        this.tipoItem = tipoItem;
        this.tipoCodigo = TipoCodigoBarras.EAN13;
    }

    // Getters y Setters
    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getTipoItem() {
        return tipoItem;
    }

    public void setTipoItem(String tipoItem) {
        this.tipoItem = tipoItem;
    }

    public String getCodigoPersonalizado() {
        return codigoPersonalizado;
    }

    public void setCodigoPersonalizado(String codigoPersonalizado) {
        this.codigoPersonalizado = codigoPersonalizado;
    }

    public TipoCodigoBarras getTipoCodigo() {
        return tipoCodigo;
    }

    public void setTipoCodigo(TipoCodigoBarras tipoCodigo) {
        this.tipoCodigo = tipoCodigo;
    }
}
