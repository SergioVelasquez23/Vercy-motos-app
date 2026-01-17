package com.prog3.security.DTOs;

import java.util.List;

public class ImprimirEtiquetasRequest {
    private List<ItemEtiqueta> items;
    private ConfiguracionEtiqueta configuracion;

    public static class ItemEtiqueta {
        private String itemId;
        private String tipoItem; // "producto" o "ingrediente"
        private int cantidad; // Número de etiquetas a imprimir

        public ItemEtiqueta() {
            this.cantidad = 1;
        }

        public ItemEtiqueta(String itemId, String tipoItem, int cantidad) {
            this.itemId = itemId;
            this.tipoItem = tipoItem;
            this.cantidad = cantidad;
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

        public int getCantidad() {
            return cantidad;
        }

        public void setCantidad(int cantidad) {
            this.cantidad = cantidad;
        }
    }

    public static class ConfiguracionEtiqueta {
        private String tamano; // "PEQUENA", "MEDIANA", "GRANDE"
        private boolean incluirPrecio;
        private boolean incluirDescripcion;
        private boolean incluirLogo;

        public ConfiguracionEtiqueta() {
            this.tamano = "MEDIANA";
            this.incluirPrecio = true;
            this.incluirDescripcion = false;
            this.incluirLogo = true;
        }

        // Getters y Setters
        public String getTamano() {
            return tamano;
        }

        public void setTamano(String tamano) {
            this.tamano = tamano;
        }

        public boolean isIncluirPrecio() {
            return incluirPrecio;
        }

        public void setIncluirPrecio(boolean incluirPrecio) {
            this.incluirPrecio = incluirPrecio;
        }

        public boolean isIncluirDescripcion() {
            return incluirDescripcion;
        }

        public void setIncluirDescripcion(boolean incluirDescripcion) {
            this.incluirDescripcion = incluirDescripcion;
        }

        public boolean isIncluirLogo() {
            return incluirLogo;
        }

        public void setIncluirLogo(boolean incluirLogo) {
            this.incluirLogo = incluirLogo;
        }
    }

    public ImprimirEtiquetasRequest() {
        this.configuracion = new ConfiguracionEtiqueta();
    }

    // Getters y Setters
    public List<ItemEtiqueta> getItems() {
        return items;
    }

    public void setItems(List<ItemEtiqueta> items) {
        this.items = items;
    }

    public ConfiguracionEtiqueta getConfiguracion() {
        return configuracion;
    }

    public void setConfiguracion(ConfiguracionEtiqueta configuracion) {
        this.configuracion = configuracion;
    }
}
