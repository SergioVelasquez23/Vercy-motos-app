package com.prog3.security.Models;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Modelo ItemPedido unificado con frontend Flutter
 * 
 * Esta versión elimina la ambigüedad de múltiples campos de precio
 * y mantiene consistencia total con el modelo Dart.
 * 
 * CAMBIOS PRINCIPALES:
 * - Un solo campo precio: precioUnitario
 * - Subtotal siempre calculado, nunca almacenado
 * - Eliminados campos confusos: precio, total, pagado
 * - Validaciones en setters
 * - Mejor documentación
 */
public class ItemPedido {

    // 🏷️ IDENTIFICACIÓN
    private String id;                              // Opcional, generado por MongoDB
    
    // 🔗 REFERENCIA A PRODUCTO  
    private String productoId;                      // ID del producto (requerido)
    private String productoNombre;                  // Cache del nombre (opcional)
    
    // 📊 CANTIDADES Y PRECIOS
    private int cantidad;                           // Cantidad pedida (requerido)
    private double precioUnitario;                  // ÚNICO precio (requerido)
    
    // 📝 INFORMACIÓN ADICIONAL
    private String notas;                           // Notas especiales (opcional)
    private List<String> ingredientesSeleccionados; // Ingredientes customizados
    
    // 🏗️ CONSTRUCTORES
    public ItemPedido() {
        this.ingredientesSeleccionados = new ArrayList<>();
        this.cantidad = 1;
        this.precioUnitario = 0.0;
    }
    
    public ItemPedido(String productoId, int cantidad, double precioUnitario) {
        this();
        this.productoId = productoId;
        this.setCantidad(cantidad);  // Usar setter para validación
        this.setPrecioUnitario(precioUnitario);  // Usar setter para validación
    }
    
    public ItemPedido(String productoId, String productoNombre, int cantidad, double precioUnitario) {
        this(productoId, cantidad, precioUnitario);
        this.productoNombre = productoNombre;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductoId() {
        return productoId;
    }

    public void setProductoId(String productoId) {
        this.productoId = productoId;
    }

    public String getProductoNombre() {
        return productoNombre;
    }

    public void setProductoNombre(String productoNombre) {
        this.productoNombre = productoNombre;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        this.cantidad = cantidad;
        // Removed calcularSubtotal() call as subtotal is dynamically calculated
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        if (precioUnitario < 0) {
            throw new IllegalArgumentException("El precio unitario no puede ser negativo");
        }
        this.precioUnitario = precioUnitario;
        // Removed calcularSubtotal() call as subtotal is dynamically calculated
    }

    public double getPrecio() {
        return precioUnitario; // Updated to use precioUnitario
    }

    public void setPrecio(double precio) {
        this.precioUnitario = precio; // Updated to set precioUnitario
    }

    public double getTotal() {
        return getSubtotal(); // Updated to calculate total using subtotal
    }

    public void setTotal(double total) {
        // Removed as total is calculated dynamically
    }

    public boolean isPagado() {
        return false; // Placeholder as pagado is no longer part of the class
    }

    public void setPagado(boolean pagado) {
        // Removed as pagado is no longer part of the class
    }

    public double getSubtotal() {
        return cantidad * precioUnitario; // Ensure subtotal is calculated dynamically
    }

    public void setSubtotal(double subtotal) {
        // Removed as subtotal is calculated dynamically
    }

    public List<String> getIngredientesSeleccionados() {
        return ingredientesSeleccionados;
    }

    public void setIngredientesSeleccionados(List<String> ingredientesSeleccionados) {
        this.ingredientesSeleccionados = ingredientesSeleccionados != null ? ingredientesSeleccionados : new ArrayList<>();
    }

    public String getNotas() {
        return notas; // Reintroducing the getter for the notas field
    }
    // 🛠 MÉTODOS UTILITARIOS
    @Override
    public String toString() {
        return String.format("ItemPedido{id='%s', productoId='%s', cantidad=%d, precioUnitario=%.2f, subtotal=%.2f}", 
                           id, productoId, cantidad, precioUnitario, getSubtotal());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ItemPedido that = (ItemPedido) obj;
        return Objects.equals(id, that.id) && Objects.equals(productoId, that.productoId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, productoId);
    }
    
    // 🧪 MÉTODOS DE VALIDACIÓN
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
}
