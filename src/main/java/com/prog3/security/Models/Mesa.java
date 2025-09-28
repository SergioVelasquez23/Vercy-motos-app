package com.prog3.security.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;
import java.util.ArrayList;

@Document
public class Mesa {

    @Id
    private String _id;
    private String nombre;
    private boolean ocupada;
    private double total;
    private List<String> productosIds; // IDs de productos en la mesa
    private TipoMesa tipo;              // Tipo de mesa (NORMAL, ESPECIAL)

    public Mesa() {
        this.productosIds = new ArrayList<>();
        this.ocupada = false;
        this.total = 0.0;
        this.tipo = TipoMesa.NORMAL; // Valor por defecto
    }

    public Mesa(String nombre) {
        this.nombre = nombre;
        this.productosIds = new ArrayList<>();
        this.ocupada = false;
        this.total = 0.0;
        this.tipo = TipoMesa.NORMAL; // Valor por defecto
    }

    public Mesa(String nombre, TipoMesa tipo) {
        this.nombre = nombre;
        this.productosIds = new ArrayList<>();
        this.ocupada = false;
        this.total = 0.0;
        this.tipo = tipo != null ? tipo : TipoMesa.NORMAL;
    }

    // Getters y Setters
    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isOcupada() {
        return ocupada;
    }

    public void setOcupada(boolean ocupada) {
        this.ocupada = ocupada;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public List<String> getProductosIds() {
        return productosIds;
    }

    public void setProductosIds(List<String> productosIds) {
        this.productosIds = productosIds;
    }

    public TipoMesa getTipo() {
        return tipo;
    }

    public void setTipo(TipoMesa tipo) {
        this.tipo = tipo != null ? tipo : TipoMesa.NORMAL;
    }

    /**
     * Determina si esta mesa puede tener múltiples pedidos simultáneos
     */
    public boolean esEspecial() {
        // Primero verificar por el campo tipo
        if (this.tipo == TipoMesa.ESPECIAL) {
            return true;
        }
        
        // Mantener compatibilidad con la lógica anterior por nombre
        if (this.nombre == null) {
            return false;
        }
        String nombreUpper = this.nombre.toUpperCase();
        return nombreUpper.contains("DOMICILIO")
                || nombreUpper.contains("CAJA")
                || nombreUpper.contains("MESA AUXILIAR");
    }
}
