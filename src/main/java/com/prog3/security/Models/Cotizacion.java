package com.prog3.security.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * Modelo de Cotización para el sistema de facturación
 * Permite crear presupuestos y cotizaciones antes de generar facturas definitivas
 */
@Document(collection = "cotizaciones")
public class Cotizacion {

    @Id
    @JsonProperty("_id")
    private String _id;
    
    // 📅 INFORMACIÓN BÁSICA
    private LocalDateTime fecha;                    // Fecha de creación
    private LocalDateTime fechaVencimiento;         // Fecha límite de validez
    private String estado;                          // "activa", "aceptada", "rechazada", "vencida", "convertida"
    
    // 👤 INFORMACIÓN DEL CLIENTE
    private String clienteId;                       // ID del cliente (documento/NIT)
    private String clienteNombre;                   // Nombre completo del cliente
    private String clienteTelefono;                 // Teléfono de contacto
    private String clienteEmail;                    // Email del cliente
    
    // 📋 ITEMS DE LA COTIZACIÓN
    private List<ItemCotizacion> items;             // Productos/servicios cotizados
    
    // 📝 INFORMACIÓN ADICIONAL
    private String descripcion;                     // Descripción general
    private List<String> archivosAdjuntos;          // URLs de archivos
    private List<String> soportesPago;              // URLs de soportes
    
    // 💰 RETENCIONES Y TRIBUTOS
    private double retencion = 0.0;                 // % Retención en la fuente
    private double valorRetencion = 0.0;            // Valor calculado
    private double reteIVA = 0.0;                   // % ReteIVA
    private double valorReteIVA = 0.0;              // Valor calculado
    private double reteICA = 0.0;                   // % ReteICA
    private double valorReteICA = 0.0;              // Valor calculado
    
    // 💳 DESCUENTOS
    private String tipoDescuentoGeneral = "Valor";  // "Valor" o "Porcentaje"
    private double descuentoGeneral = 0.0;          // Descuento general aplicado
    private double descuentoProductos = 0.0;        // Suma de descuentos por item
    
    // 💵 TOTALES CALCULADOS
    private double subtotal = 0.0;                  // Suma sin impuestos ni descuentos
    private double totalImpuestos = 0.0;            // Total de impuestos
    private double totalDescuentos = 0.0;           // Total de descuentos
    private double totalRetenciones = 0.0;          // Total de retenciones
    private double totalFinal = 0.0;                // Total a pagar
    
    // 🔍 TRACKING
    private String creadoPor;                       // Usuario que creó la cotización
    private String modificadoPor;                   // Último usuario que modificó
    private LocalDateTime fechaModificacion;        // Última modificación
    private String numeroCotizacion;                // Número único (COT-0001)
    private String facturaRelacionadaId;            // ID de factura si se convirtió
    
    // 🏗️ CONSTRUCTORES
    public Cotizacion() {
        this.fecha = LocalDateTime.now();
        this.estado = "activa";
        this.items = new ArrayList<>();
        this.archivosAdjuntos = new ArrayList<>();
        this.soportesPago = new ArrayList<>();
    }
    
    // 📐 GETTERS Y SETTERS
    
    public String get_id() { return _id; }
    public void set_id(String _id) { this._id = _id; }
    
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    
    public LocalDateTime getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDateTime fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }
    
    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
    
    public String getClienteTelefono() { return clienteTelefono; }
    public void setClienteTelefono(String clienteTelefono) { this.clienteTelefono = clienteTelefono; }
    
    public String getClienteEmail() { return clienteEmail; }
    public void setClienteEmail(String clienteEmail) { this.clienteEmail = clienteEmail; }
    
    public List<ItemCotizacion> getItems() { return items; }
    public void setItems(List<ItemCotizacion> items) { this.items = items; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public List<String> getArchivosAdjuntos() { return archivosAdjuntos; }
    public void setArchivosAdjuntos(List<String> archivosAdjuntos) { this.archivosAdjuntos = archivosAdjuntos; }
    
    public List<String> getSoportesPago() { return soportesPago; }
    public void setSoportesPago(List<String> soportesPago) { this.soportesPago = soportesPago; }
    
    public double getRetencion() { return retencion; }
    public void setRetencion(double retencion) { this.retencion = retencion; }
    
    public double getValorRetencion() { return valorRetencion; }
    public void setValorRetencion(double valorRetencion) { this.valorRetencion = valorRetencion; }
    
    public double getReteIVA() { return reteIVA; }
    public void setReteIVA(double reteIVA) { this.reteIVA = reteIVA; }
    
    public double getValorReteIVA() { return valorReteIVA; }
    public void setValorReteIVA(double valorReteIVA) { this.valorReteIVA = valorReteIVA; }
    
    public double getReteICA() { return reteICA; }
    public void setReteICA(double reteICA) { this.reteICA = reteICA; }
    
    public double getValorReteICA() { return valorReteICA; }
    public void setValorReteICA(double valorReteICA) { this.valorReteICA = valorReteICA; }
    
    public String getTipoDescuentoGeneral() { return tipoDescuentoGeneral; }
    public void setTipoDescuentoGeneral(String tipoDescuentoGeneral) { this.tipoDescuentoGeneral = tipoDescuentoGeneral; }
    
    public double getDescuentoGeneral() { return descuentoGeneral; }
    public void setDescuentoGeneral(double descuentoGeneral) { this.descuentoGeneral = descuentoGeneral; }
    
    public double getDescuentoProductos() { return descuentoProductos; }
    public void setDescuentoProductos(double descuentoProductos) { this.descuentoProductos = descuentoProductos; }
    
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    
    public double getTotalImpuestos() { return totalImpuestos; }
    public void setTotalImpuestos(double totalImpuestos) { this.totalImpuestos = totalImpuestos; }
    
    public double getTotalDescuentos() { return totalDescuentos; }
    public void setTotalDescuentos(double totalDescuentos) { this.totalDescuentos = totalDescuentos; }
    
    public double getTotalRetenciones() { return totalRetenciones; }
    public void setTotalRetenciones(double totalRetenciones) { this.totalRetenciones = totalRetenciones; }
    
    public double getTotalFinal() { return totalFinal; }
    public void setTotalFinal(double totalFinal) { this.totalFinal = totalFinal; }
    
    public String getCreadoPor() { return creadoPor; }
    public void setCreadoPor(String creadoPor) { this.creadoPor = creadoPor; }
    
    public String getModificadoPor() { return modificadoPor; }
    public void setModificadoPor(String modificadoPor) { this.modificadoPor = modificadoPor; }
    
    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDateTime fechaModificacion) { this.fechaModificacion = fechaModificacion; }
    
    public String getNumeroCotizacion() { return numeroCotizacion; }
    public void setNumeroCotizacion(String numeroCotizacion) { this.numeroCotizacion = numeroCotizacion; }
    
    public String getFacturaRelacionadaId() { return facturaRelacionadaId; }
    public void setFacturaRelacionadaId(String facturaRelacionadaId) { this.facturaRelacionadaId = facturaRelacionadaId; }
    
    // 🧮 MÉTODOS DE CÁLCULO
    
    /**
     * Calcula todos los totales de la cotización
     * Debe llamarse antes de guardar o actualizar
     */
    public void calcularTotales() {
        // 1. Calcular valores de impuestos y descuentos de cada item
        for (ItemCotizacion item : items) {
            item.calcularValorImpuesto();
            item.calcularValorDescuento();
        }
        
        // 2. Calcular subtotal
        this.subtotal = items.stream()
            .mapToDouble(ItemCotizacion::getSubtotal)
            .sum();
        
        // 3. Calcular total de impuestos
        this.totalImpuestos = items.stream()
            .mapToDouble(ItemCotizacion::getValorImpuesto)
            .sum();
        
        // 4. Calcular descuento de productos
        this.descuentoProductos = items.stream()
            .mapToDouble(ItemCotizacion::getValorDescuento)
            .sum();
        
        // 5. Calcular descuento general (si es porcentaje)
        if ("Porcentaje".equals(this.tipoDescuentoGeneral)) {
            this.descuentoGeneral = (this.subtotal * this.descuentoGeneral) / 100.0;
        }
        
        // 6. Calcular total de descuentos
        this.totalDescuentos = this.descuentoGeneral + this.descuentoProductos;
        
        // 7. Calcular retenciones
        this.valorRetencion = (this.subtotal * this.retencion) / 100.0;
        this.valorReteIVA = (this.totalImpuestos * this.reteIVA) / 100.0;
        this.valorReteICA = (this.subtotal * this.reteICA) / 100.0;
        this.totalRetenciones = this.valorRetencion + this.valorReteIVA + this.valorReteICA;
        
        // 8. Calcular total final
        this.totalFinal = this.subtotal + this.totalImpuestos - this.totalDescuentos - this.totalRetenciones;
    }
    
    /**
     * Verifica si la cotización está vencida
     */
    public boolean estaVencida() {
        return fechaVencimiento != null && LocalDateTime.now().isAfter(fechaVencimiento);
    }
    
    /**
     * Marca la cotización como aceptada
     */
    public void aceptar(String usuarioId) {
        this.estado = "aceptada";
        this.modificadoPor = usuarioId;
        this.fechaModificacion = LocalDateTime.now();
    }
    
    /**
     * Marca la cotización como rechazada
     */
    public void rechazar(String usuarioId) {
        this.estado = "rechazada";
        this.modificadoPor = usuarioId;
        this.fechaModificacion = LocalDateTime.now();
    }
    
    /**
     * Marca la cotización como convertida a factura
     */
    public void convertirAFactura(String facturaId, String usuarioId) {
        this.estado = "convertida";
        this.facturaRelacionadaId = facturaId;
        this.modificadoPor = usuarioId;
        this.fechaModificacion = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return String.format("Cotizacion{id='%s', numero='%s', cliente='%s', estado='%s', total=%.2f}", 
                           _id, numeroCotizacion, clienteNombre, estado, totalFinal);
    }
}
