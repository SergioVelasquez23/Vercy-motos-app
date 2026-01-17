package com.prog3.security.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

@Document(collection = "inventario_bodegas")
public class InventarioBodega {

    @Id
    private String _id;

    private String bodegaId;
    private String itemId; // producto o ingrediente
    private String tipoItem; // "producto" o "ingrediente"
    private double stockActual;
    private double stockMinimo;
    private double stockMaximo;
    private String ubicacionFisica; // estante, pasillo, etc.
    private LocalDateTime ultimoMovimiento;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    public InventarioBodega() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        this.stockActual = 0;
        this.stockMinimo = 0;
        this.stockMaximo = 0;
    }

    @JsonProperty("_id")
    public String get_id() {
        return _id;
    }

    @JsonProperty("_id")
    public void set_id(String _id) {
        this._id = _id;
    }

    public String getBodegaId() {
        return bodegaId;
    }

    public void setBodegaId(String bodegaId) {
        this.bodegaId = bodegaId;
    }

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

    public LocalDateTime getUltimoMovimiento() {
        return ultimoMovimiento;
    }

    public void setUltimoMovimiento(LocalDateTime ultimoMovimiento) {
        this.ultimoMovimiento = ultimoMovimiento;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    @Override
    public String toString() {
        return "InventarioBodega{" + "_id='" + _id + '\'' + ", bodegaId='" + bodegaId + '\''
                + ", itemId='" + itemId + '\'' + ", tipoItem='" + tipoItem + '\'' + ", stockActual="
                + stockActual + ", stockMinimo=" + stockMinimo + '}';
    }
}
