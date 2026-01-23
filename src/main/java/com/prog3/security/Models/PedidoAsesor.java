package com.prog3.security.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Document(collection = "pedidos_asesor")
public class PedidoAsesor {

    @Id
    private String id;

    // Información del cliente
    private String clienteNombre;
    private String clienteId; // Opcional - ID si el cliente está registrado
    private String clienteTelefono;
    private String clienteDocumento;

    // Información del asesor
    private String asesorNombre;
    private String asesorId; // ID del usuario que creó el pedido

    // Items del pedido
    private List<ItemPedidoAsesor> items = new ArrayList<>();

    // Información financiera
    private double subtotal;
    private double impuestos;
    private double descuento;
    private double total;

    // Estado del pedido
    private EstadoPedidoAsesor estado = EstadoPedidoAsesor.PENDIENTE;

    // Información de facturación
    private boolean facturado = false;
    private String facturaId; // ID de la factura cuando se facture
    private String facturadoPor; // Nombre del usuario que facturó
    private LocalDateTime fechaFacturacion;

    // Fechas
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    // Observaciones
    private String observaciones;

    // Historial de cambios
    private List<HistorialCambio> historial = new ArrayList<>();

    // Constructor sin argumentos
    public PedidoAsesor() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
    }

    // Clase interna para items del pedido
    public static class ItemPedidoAsesor {
        private String productoId;
        private String productoNombre;
        private int cantidad;
        private double precioUnitario;
        private double subtotal; // cantidad * precioUnitario
        private String notas;

        // Ingredientes (si aplica)
        private List<String> ingredientesSeleccionados = new ArrayList<>();
        private List<IngredienteUsado> ingredientesUsados = new ArrayList<>();

        // Constructor sin argumentos
        public ItemPedidoAsesor() {}

        public ItemPedidoAsesor(String productoId, String productoNombre, int cantidad,
                double precioUnitario) {
            this.productoId = productoId;
            this.productoNombre = productoNombre;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
            this.subtotal = cantidad * precioUnitario;
        }

        // Getters y setters
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
            this.cantidad = cantidad;
            this.subtotal = cantidad * precioUnitario;
        }

        public double getPrecioUnitario() {
            return precioUnitario;
        }

        public void setPrecioUnitario(double precioUnitario) {
            this.precioUnitario = precioUnitario;
            this.subtotal = cantidad * precioUnitario;
        }

        public double getSubtotal() {
            return subtotal;
        }

        public void setSubtotal(double subtotal) {
            this.subtotal = subtotal;
        }

        public String getNotas() {
            return notas;
        }

        public void setNotas(String notas) {
            this.notas = notas;
        }

        public List<String> getIngredientesSeleccionados() {
            return ingredientesSeleccionados;
        }

        public void setIngredientesSeleccionados(List<String> ingredientesSeleccionados) {
            this.ingredientesSeleccionados = ingredientesSeleccionados;
        }

        public List<IngredienteUsado> getIngredientesUsados() {
            return ingredientesUsados;
        }

        public void setIngredientesUsados(List<IngredienteUsado> ingredientesUsados) {
            this.ingredientesUsados = ingredientesUsados;
        }
    }

    // Clase interna para ingredientes usados
    public static class IngredienteUsado {
        private String ingredienteId;
        private String nombre;
        private double cantidad;
        private String unidad;

        public IngredienteUsado() {}

        public IngredienteUsado(String ingredienteId, String nombre, double cantidad,
                String unidad) {
            this.ingredienteId = ingredienteId;
            this.nombre = nombre;
            this.cantidad = cantidad;
            this.unidad = unidad;
        }

        // Getters y setters
        public String getIngredienteId() {
            return ingredienteId;
        }

        public void setIngredienteId(String ingredienteId) {
            this.ingredienteId = ingredienteId;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public double getCantidad() {
            return cantidad;
        }

        public void setCantidad(double cantidad) {
            this.cantidad = cantidad;
        }

        public String getUnidad() {
            return unidad;
        }

        public void setUnidad(String unidad) {
            this.unidad = unidad;
        }
    }

    // Clase interna para historial de cambios
    public static class HistorialCambio {
        private String accion; // "creado", "actualizado", "facturado", "cancelado"
        private String usuario;
        private LocalDateTime fecha;
        private String detalles;

        public HistorialCambio() {
            this.fecha = LocalDateTime.now();
        }

        public HistorialCambio(String accion, String usuario, String detalles) {
            this.accion = accion;
            this.usuario = usuario;
            this.fecha = LocalDateTime.now();
            this.detalles = detalles;
        }

        // Getters y setters
        public String getAccion() {
            return accion;
        }

        public void setAccion(String accion) {
            this.accion = accion;
        }

        public String getUsuario() {
            return usuario;
        }

        public void setUsuario(String usuario) {
            this.usuario = usuario;
        }

        public LocalDateTime getFecha() {
            return fecha;
        }

        public void setFecha(LocalDateTime fecha) {
            this.fecha = fecha;
        }

        public String getDetalles() {
            return detalles;
        }

        public void setDetalles(String detalles) {
            this.detalles = detalles;
        }
    }

    // Enum para estado del pedido
    public enum EstadoPedidoAsesor {
        PENDIENTE, FACTURADO, CANCELADO
    }

    // Métodos de utilidad
    public void calcularTotales() {
        this.subtotal = items.stream().mapToDouble(ItemPedidoAsesor::getSubtotal).sum();
        this.total = this.subtotal + this.impuestos - this.descuento;
    }

    public void agregarHistorial(String accion, String usuario, String detalles) {
        if (this.historial == null) {
            this.historial = new ArrayList<>();
        }
        this.historial.add(new HistorialCambio(accion, usuario, detalles));
        this.fechaActualizacion = LocalDateTime.now();
    }

    // Getters y setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public String getClienteTelefono() {
        return clienteTelefono;
    }

    public void setClienteTelefono(String clienteTelefono) {
        this.clienteTelefono = clienteTelefono;
    }

    public String getClienteDocumento() {
        return clienteDocumento;
    }

    public void setClienteDocumento(String clienteDocumento) {
        this.clienteDocumento = clienteDocumento;
    }

    public String getAsesorNombre() {
        return asesorNombre;
    }

    public void setAsesorNombre(String asesorNombre) {
        this.asesorNombre = asesorNombre;
    }

    public String getAsesorId() {
        return asesorId;
    }

    public void setAsesorId(String asesorId) {
        this.asesorId = asesorId;
    }

    public List<ItemPedidoAsesor> getItems() {
        return items;
    }

    public void setItems(List<ItemPedidoAsesor> items) {
        this.items = items;
        calcularTotales();
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getImpuestos() {
        return impuestos;
    }

    public void setImpuestos(double impuestos) {
        this.impuestos = impuestos;
        calcularTotales();
    }

    public double getDescuento() {
        return descuento;
    }

    public void setDescuento(double descuento) {
        this.descuento = descuento;
        calcularTotales();
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public EstadoPedidoAsesor getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedidoAsesor estado) {
        this.estado = estado;
    }

    public boolean isFacturado() {
        return facturado;
    }

    public void setFacturado(boolean facturado) {
        this.facturado = facturado;
    }

    public String getFacturaId() {
        return facturaId;
    }

    public void setFacturaId(String facturaId) {
        this.facturaId = facturaId;
    }

    public String getFacturadoPor() {
        return facturadoPor;
    }

    public void setFacturadoPor(String facturadoPor) {
        this.facturadoPor = facturadoPor;
    }

    public LocalDateTime getFechaFacturacion() {
        return fechaFacturacion;
    }

    public void setFechaFacturacion(LocalDateTime fechaFacturacion) {
        this.fechaFacturacion = fechaFacturacion;
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

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public List<HistorialCambio> getHistorial() {
        return historial;
    }

    public void setHistorial(List<HistorialCambio> historial) {
        this.historial = historial;
    }
}
