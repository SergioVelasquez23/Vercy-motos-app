package com.prog3.security.Models;

import java.util.List;
import java.util.ArrayList;

/**
 * Modelo para items individuales de una cotización
 * Representa cada producto/servicio cotizado con sus impuestos y descuentos
 */
public class ItemCotizacion {

    // 🏷️ IDENTIFICACIÓN
    private String id;                              // ID del item
    
    // 🔗 REFERENCIA A PRODUCTO  
    private String productoId;                      // ID del producto
    private String productoNombre;                  // Nombre del producto
    
    // 📊 CANTIDADES Y PRECIOS
    private int cantidad;                           // Cantidad cotizada
    private double precioUnitario;                  // Precio unitario
    
    // 💰 FACTURACIÓN ELECTRÓNICA
    private String codigoProducto;                  // Código interno (ej: "PROD-001")
    private String codigoBarras;                    // EAN (ej: "7501234567890")
    private String tipoImpuesto;                    // "IVA", "INC", "Exento", "IVA+INC"
    private double porcentajeImpuesto = 0.0;        // Porcentaje (ej: 19.0)
    private double valorImpuesto = 0.0;             // Valor calculado
    private double porcentajeDescuento = 0.0;       // Porcentaje descuento (ej: 10.0)
    private double valorDescuento = 0.0;            // Valor calculado
    
    // 📝 INFORMACIÓN ADICIONAL
    private String notas;                           // Notas especiales
    
    // 🏗️ CONSTRUCTORES
    public ItemCotizacion() {
        this.cantidad = 1;
        this.precioUnitario = 0.0;
    }
    
    public ItemCotizacion(String productoId, int cantidad, double precioUnitario) {
        this();
        this.productoId = productoId;
        this.setCantidad(cantidad);
        this.setPrecioUnitario(precioUnitario);
    }
    
    public ItemCotizacion(String productoId, String productoNombre, int cantidad, double precioUnitario) {
        this(productoId, cantidad, precioUnitario);
        this.productoNombre = productoNombre;
    }

    // 📐 GETTERS Y SETTERS BÁSICOS
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) { this.productoId = productoId; }
    
    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }
    
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        this.cantidad = cantidad;
    }
    
    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) {
        if (precioUnitario < 0) {
            throw new IllegalArgumentException("El precio unitario no puede ser negativo");
        }
        this.precioUnitario = precioUnitario;
    }
    
    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
    
    // 💰 GETTERS Y SETTERS FACTURACIÓN
    
    public String getCodigoProducto() { return codigoProducto; }
    public void setCodigoProducto(String codigoProducto) { this.codigoProducto = codigoProducto; }
    
    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }
    
    public String getTipoImpuesto() { return tipoImpuesto; }
    public void setTipoImpuesto(String tipoImpuesto) { this.tipoImpuesto = tipoImpuesto; }
    
    public double getPorcentajeImpuesto() { return porcentajeImpuesto; }
    public void setPorcentajeImpuesto(double porcentajeImpuesto) {
        if (porcentajeImpuesto < 0 || porcentajeImpuesto > 100) {
            throw new IllegalArgumentException("El porcentaje de impuesto debe estar entre 0 y 100");
        }
        this.porcentajeImpuesto = porcentajeImpuesto;
        calcularValorImpuesto();
    }
    
    public double getValorImpuesto() { return valorImpuesto; }
    public void setValorImpuesto(double valorImpuesto) { this.valorImpuesto = valorImpuesto; }
    
    public double getPorcentajeDescuento() { return porcentajeDescuento; }
    public void setPorcentajeDescuento(double porcentajeDescuento) {
        if (porcentajeDescuento < 0 || porcentajeDescuento > 100) {
            throw new IllegalArgumentException("El porcentaje de descuento debe estar entre 0 y 100");
        }
        this.porcentajeDescuento = porcentajeDescuento;
        calcularValorDescuento();
    }
    
    public double getValorDescuento() { return valorDescuento; }
    public void setValorDescuento(double valorDescuento) { this.valorDescuento = valorDescuento; }
    
    // 🧮 MÉTODOS DE CÁLCULO
    
    /**
     * Subtotal del item (sin impuestos ni descuentos)
     * @return cantidad * precioUnitario
     */
    public double getSubtotal() {
        return cantidad * precioUnitario;
    }
    
    /**
     * Calcula el valor del impuesto basado en el subtotal y porcentaje
     */
    public void calcularValorImpuesto() {
        this.valorImpuesto = (getSubtotal() * porcentajeImpuesto) / 100.0;
    }
    
    /**
     * Calcula el valor del descuento basado en el subtotal y porcentaje
     */
    public void calcularValorDescuento() {
        this.valorDescuento = (getSubtotal() * porcentajeDescuento) / 100.0;
    }
    
    /**
     * Valor total del item (incluye impuesto y descuento)
     * @return subtotal + valorImpuesto - valorDescuento
     */
    public double getValorTotal() {
        return getSubtotal() + valorImpuesto - valorDescuento;
    }
    
    // 🧪 VALIDACIÓN
    
    public boolean isValid() {
        return productoId != null 
            && !productoId.trim().isEmpty() 
            && cantidad > 0 
            && precioUnitario >= 0;
    }
    
    public List<String> getValidationErrors() {
        List<String> errors = new ArrayList<>();
        if (productoId == null || productoId.trim().isEmpty()) {
            errors.add("ProductoId es requerido");
        }
        if (cantidad <= 0) {
            errors.add("Cantidad debe ser mayor a 0");
        }
        if (precioUnitario < 0) {
            errors.add("Precio unitario no puede ser negativo");
        }
        return errors;
    }
    
    @Override
    public String toString() {
        return String.format("ItemCotizacion{id='%s', productoId='%s', cantidad=%d, precioUnitario=%.2f, subtotal=%.2f}", 
                           id, productoId, cantidad, precioUnitario, getSubtotal());
    }
}
