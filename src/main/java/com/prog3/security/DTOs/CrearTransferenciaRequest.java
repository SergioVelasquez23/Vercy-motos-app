package com.prog3.security.DTOs;

import java.util.List;

public class CrearTransferenciaRequest {
    private String bodegaOrigenId;
    private String bodegaDestinoId;
    private String usuarioId;
    private List<ItemTransferencia> items;
    private String observaciones;

    public static class ItemTransferencia {
        private String itemId;
        private String tipoItem;
        private double cantidad;
        private String observaciones;

        public ItemTransferencia() {}

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

        public double getCantidad() {
            return cantidad;
        }

        public void setCantidad(double cantidad) {
            this.cantidad = cantidad;
        }

        public String getObservaciones() {
            return observaciones;
        }

        public void setObservaciones(String observaciones) {
            this.observaciones = observaciones;
        }
    }

    public CrearTransferenciaRequest() {}

    public String getBodegaOrigenId() {
        return bodegaOrigenId;
    }

    public void setBodegaOrigenId(String bodegaOrigenId) {
        this.bodegaOrigenId = bodegaOrigenId;
    }

    public String getBodegaDestinoId() {
        return bodegaDestinoId;
    }

    public void setBodegaDestinoId(String bodegaDestinoId) {
        this.bodegaDestinoId = bodegaDestinoId;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public List<ItemTransferencia> getItems() {
        return items;
    }

    public void setItems(List<ItemTransferencia> items) {
        this.items = items;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
