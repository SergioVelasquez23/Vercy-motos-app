package com.prog3.security.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Document
public class Pedido {

    // Historial de ediciones
    public static class HistorialEdicion {
        private String accion; // "creado", "producto_agregado", "producto_editado", "producto_eliminado", "total_actualizado"
        private String usuario;
        private LocalDateTime fecha;
        private String detalles; // Descripción de la edición
        private String productoAfectado; // ID o nombre del producto si aplica

        // Constructor sin argumentos (requerido para serialización/deserialización)
        public HistorialEdicion() {
            this.fecha = LocalDateTime.now();
        }

        public HistorialEdicion(String accion, String usuario, String detalles) {
            this.accion = accion;
            this.usuario = usuario;
            this.fecha = LocalDateTime.now();
            this.detalles = detalles;
        }

        public HistorialEdicion(String accion, String usuario, String detalles, String productoAfectado) {
            this(accion, usuario, detalles);
            this.productoAfectado = productoAfectado;
        }

        // Getters y setters
        public String getAccion() { return accion; }
        public void setAccion(String accion) { this.accion = accion; }
        public String getUsuario() { return usuario; }
        public void setUsuario(String usuario) { this.usuario = usuario; }
        public LocalDateTime getFecha() { return fecha; }
        public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
        public String getDetalles() { return detalles; }
        public void setDetalles(String detalles) { this.detalles = detalles; }
        public String getProductoAfectado() { return productoAfectado; }
        public void setProductoAfectado(String productoAfectado) { this.productoAfectado = productoAfectado; }
    }

    // Pagos parciales: cada pago tiene monto y forma
    public static class PagoParcial {

        private double monto;
        private String formaPago;
        private LocalDateTime fecha;
        private String procesadoPor;

        // Constructor sin argumentos (requerido para serialización/deserialización)
        public PagoParcial() {
            this.fecha = LocalDateTime.now();
        }

        public PagoParcial(double monto, String formaPago, String procesadoPor) {
            this.monto = monto;
            this.formaPago = formaPago;
            this.fecha = LocalDateTime.now();
            this.procesadoPor = procesadoPor;
        }

        public double getMonto() {
            return monto;
        }

        public String getFormaPago() {
            return formaPago;
        }

        public LocalDateTime getFecha() {
            return fecha;
        }

        public String getProcesadoPor() {
            return procesadoPor;
        }

        public void setMonto(double monto) {
            this.monto = monto;
        }

        public void setFormaPago(String formaPago) {
            this.formaPago = formaPago;
        }

        public void setFecha(LocalDateTime fecha) {
            this.fecha = fecha;
        }

        public void setProcesadoPor(String procesadoPor) {
            this.procesadoPor = procesadoPor;
        }
    }

    private List<PagoParcial> pagosParciales = new ArrayList<>();
    private List<HistorialEdicion> historialEdiciones = new ArrayList<>();

    public List<PagoParcial> getPagosParciales() {
        return pagosParciales;
    }

    public List<HistorialEdicion> getHistorialEdiciones() {
        return historialEdiciones;
    }

    public void agregarEdicion(String accion, String usuario, String detalles) {
        historialEdiciones.add(new HistorialEdicion(accion, usuario, detalles));
    }

    public void agregarEdicion(String accion, String usuario, String detalles, String productoAfectado) {
        historialEdiciones.add(new HistorialEdicion(accion, usuario, detalles, productoAfectado));
    }

    public void agregarPagoParcial(double monto, String formaPago, String procesadoPor) {
        pagosParciales.add(new PagoParcial(monto, formaPago, procesadoPor));
        this.totalPagado += monto;
    }

    @Id
    @JsonProperty("_id")
    private String _id;
    private LocalDateTime fecha;
    private String tipo; // normal, rt, interno, cancelado, cortesia
    private String mesa;
    private String cliente;
    private String nombrePedido; // Nombre identificativo del pedido (usado en mesas especiales)
    private String mesero;
    private List<ItemPedido> items;
    private double total;
    private double descuento;
    private boolean incluyePropina;
    private String notas;
    private String plataforma; // Para pedidos RT
    private String pedidoPor; // Para pedidos internos
    private String guardadoPor; // Para pedidos internos
    private LocalDateTime fechaCortesia;
    private String estado; // activo, pagado, cancelado, completado
    private String formaPago; // efectivo, transferencia, tarjeta, otro
    private List<ItemPedido> itemsPagados; // Items que ya han sido pagados

    // Campos para pago
    private double propina; // Propina añadida al pagar
    private double totalPagado; // Total incluyendo propina
    private LocalDateTime fechaPago; // Cuándo se pagó
    private String pagadoPor; // Quien procesó el pago

    // Campos para cancelación
    private String motivoCancelacion; // Por qué se canceló
    private String canceladoPor; // Quien canceló el pedido
    private LocalDateTime fechaCancelacion; // Cuándo se canceló

    // Campo para relacionar con el cuadre de caja
    private String cuadreCajaId; // ID del cuadre de caja al que pertenece este pedido

    // 📋 CAMPOS PARA FACTURACIÓN ELECTRÓNICA (DIAN)

    // --- INFORMACIÓN GENERAL DE FACTURA ---
    private String descripcionFactura; // Descripción general de la factura
    private List<String> archivosAdjuntos; // URLs de archivos adjuntos
    private String tipoFactura = "POS"; // Tipo: "POS", "Electrónica", "Computarizada", "Manual"
    private LocalDateTime fechaVencimiento; // Fecha de vencimiento (para crédito)
    private String numeroFactura; // Número único de factura (FAC-0001, FE-2024-001)
    private String codigoBarrasFactura; // CUFE para facturas electrónicas

    // --- RETENCIONES Y TRIBUTOS ---
    private double retencion = 0.0; // Porcentaje de retención en la fuente
    private double valorRetencion = 0.0; // Valor calculado de retención
    private double reteIVA = 0.0; // Porcentaje de ReteIVA
    private double valorReteIVA = 0.0; // Valor calculado de ReteIVA
    private double reteICA = 0.0; // Porcentaje de ReteICA
    private double valorReteICA = 0.0; // Valor calculado de ReteICA
    private java.util.Map<String, Object> aiu; // AIU para contratos de obra

    // --- DESCUENTOS DETALLADOS ---
    private String tipoDescuentoGeneral = "Valor"; // Tipo: "Valor" o "Porcentaje"
    private double descuentoGeneral = 0.0; // Valor del descuento general
    private double descuentoProductos = 0.0; // Suma de descuentos de items

    // --- TOTALES CALCULADOS ---
    private double subtotal = 0.0; // Suma de items sin impuestos ni descuentos
    private double totalImpuestos = 0.0; // Total de impuestos
    private double totalDescuentos = 0.0; // Total de descuentos (general + productos)
    private double totalRetenciones = 0.0; // Total de retenciones
    private double totalFinal = 0.0; // Total final a pagar

    public Pedido() {
        this.fecha = LocalDateTime.now();
        this.estado = "activo";
        this.total = 0.0;
        this.descuento = 0.0;
        this.incluyePropina = false;
        this.items = new ArrayList<>();
        this.itemsPagados = new ArrayList<>();
        this.archivosAdjuntos = new ArrayList<>();
    }

    public Pedido(String tipo, List<ItemPedido> items) {
        this.fecha = LocalDateTime.now();
        this.tipo = tipo;
        this.items = items;
        this.estado = "pendiente";
        this.total = 0.0;
        this.descuento = 0.0;
        this.incluyePropina = false;
        this.itemsPagados = new ArrayList<>();
    }

    // Getters y Setters
    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getMesa() {
        return mesa;
    }

    public void setMesa(String mesa) {
        this.mesa = mesa;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getNombrePedido() {
        return nombrePedido;
    }

    public void setNombrePedido(String nombrePedido) {
        this.nombrePedido = nombrePedido;
    }

    public String getMesero() {
        return mesero;
    }

    public void setMesero(String mesero) {
        this.mesero = mesero;
    }

    public List<ItemPedido> getItems() {
        return items;
    }

    public void setItems(List<ItemPedido> items) {
        this.items = items;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getDescuento() {
        return descuento;
    }

    public void setDescuento(double descuento) {
        this.descuento = descuento;
    }

    public boolean isIncluyePropina() {
        return incluyePropina;
    }

    public void setIncluyePropina(boolean incluyePropina) {
        this.incluyePropina = incluyePropina;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public String getPlataforma() {
        return plataforma;
    }

    public void setPlataforma(String plataforma) {
        this.plataforma = plataforma;
    }

    public String getPedidoPor() {
        return pedidoPor;
    }

    public void setPedidoPor(String pedidoPor) {
        this.pedidoPor = pedidoPor;
    }

    public String getGuardadoPor() {
        return guardadoPor;
    }

    public void setGuardadoPor(String guardadoPor) {
        this.guardadoPor = guardadoPor;
    }

    public LocalDateTime getFechaCortesia() {
        return fechaCortesia;
    }

    public void setFechaCortesia(LocalDateTime fechaCortesia) {
        this.fechaCortesia = fechaCortesia;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(String formaPago) {
        this.formaPago = formaPago;
    }

    public List<ItemPedido> getItemsPagados() {
        return itemsPagados;
    }

    public void setItemsPagados(List<ItemPedido> itemsPagados) {
        this.itemsPagados = itemsPagados;
    }

    public void marcarItemComoPagado(String itemId) {
        // Buscar el item en la lista de items
        for (ItemPedido item : items) {
            if (item.getId().equals(itemId)) {
                // Remover de items activos y agregar a pagados
                items.remove(item);
                itemsPagados.add(item);
                break;
            }
        }
        // Actualizar el estado si todos los items están pagados
        if (items.isEmpty() && !itemsPagados.isEmpty()) {
            this.estado = "completado";
        }
    }

    public double calcularTotalPendiente() {
        double totalPendiente = 0.0;
        for (ItemPedido item : items) {
            totalPendiente += item.getSubtotal();
        }
        return totalPendiente;
    }

    public double calcularTotalPagado() {
        double totalPagado = 0.0;
        for (ItemPedido item : itemsPagados) {
            totalPagado += item.getSubtotal();
        }
        return totalPagado;
    }

    // Método para calcular el total real del pedido basado en todos los items
    public double calcularTotalReal() {
        return calcularTotalPendiente() + calcularTotalPagado();
    }

    // Getters y Setters para los nuevos campos
    public double getPropina() {
        return propina;
    }

    public void setPropina(double propina) {
        this.propina = propina;
    }

    public double getTotalPagado() {
        return totalPagado;
    }

    public void setTotalPagado(double totalPagado) {
        this.totalPagado = totalPagado;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }

    public String getPagadoPor() {
        return pagadoPor;
    }

    public void setPagadoPor(String pagadoPor) {
        this.pagadoPor = pagadoPor;
    }

    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    public void setMotivoCancelacion(String motivoCancelacion) {
        this.motivoCancelacion = motivoCancelacion;
    }

    public String getCanceladoPor() {
        return canceladoPor;
    }

    public void setCanceladoPor(String canceladoPor) {
        this.canceladoPor = canceladoPor;
    }

    public LocalDateTime getFechaCancelacion() {
        return fechaCancelacion;
    }

    public void setFechaCancelacion(LocalDateTime fechaCancelacion) {
        this.fechaCancelacion = fechaCancelacion;
    }

    public String getCuadreCajaId() {
        return cuadreCajaId;
    }

    public void setCuadreCajaId(String cuadreCajaId) {
        this.cuadreCajaId = cuadreCajaId;
    }

    // 📋 GETTERS Y SETTERS PARA FACTURACIÓN ELECTRÓNICA

    public String getDescripcionFactura() {
        return descripcionFactura;
    }

    public void setDescripcionFactura(String descripcionFactura) {
        this.descripcionFactura = descripcionFactura;
    }

    public List<String> getArchivosAdjuntos() {
        return archivosAdjuntos;
    }

    public void setArchivosAdjuntos(List<String> archivosAdjuntos) {
        this.archivosAdjuntos = archivosAdjuntos;
    }

    public String getTipoFactura() {
        return tipoFactura;
    }

    public void setTipoFactura(String tipoFactura) {
        this.tipoFactura = tipoFactura;
    }

    public LocalDateTime getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDateTime fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(String numeroFactura) {
        this.numeroFactura = numeroFactura;
    }

    public String getCodigoBarrasFactura() {
        return codigoBarrasFactura;
    }

    public void setCodigoBarrasFactura(String codigoBarrasFactura) {
        this.codigoBarrasFactura = codigoBarrasFactura;
    }

    public double getRetencion() {
        return retencion;
    }

    public void setRetencion(double retencion) {
        this.retencion = retencion;
    }

    public double getValorRetencion() {
        return valorRetencion;
    }

    public void setValorRetencion(double valorRetencion) {
        this.valorRetencion = valorRetencion;
    }

    public double getReteIVA() {
        return reteIVA;
    }

    public void setReteIVA(double reteIVA) {
        this.reteIVA = reteIVA;
    }

    public double getValorReteIVA() {
        return valorReteIVA;
    }

    public void setValorReteIVA(double valorReteIVA) {
        this.valorReteIVA = valorReteIVA;
    }

    public double getReteICA() {
        return reteICA;
    }

    public void setReteICA(double reteICA) {
        this.reteICA = reteICA;
    }

    public double getValorReteICA() {
        return valorReteICA;
    }

    public void setValorReteICA(double valorReteICA) {
        this.valorReteICA = valorReteICA;
    }

    public java.util.Map<String, Object> getAiu() {
        return aiu;
    }

    public void setAiu(java.util.Map<String, Object> aiu) {
        this.aiu = aiu;
    }

    public String getTipoDescuentoGeneral() {
        return tipoDescuentoGeneral;
    }

    public void setTipoDescuentoGeneral(String tipoDescuentoGeneral) {
        this.tipoDescuentoGeneral = tipoDescuentoGeneral;
    }

    public double getDescuentoGeneral() {
        return descuentoGeneral;
    }

    public void setDescuentoGeneral(double descuentoGeneral) {
        this.descuentoGeneral = descuentoGeneral;
    }

    public double getDescuentoProductos() {
        return descuentoProductos;
    }

    public void setDescuentoProductos(double descuentoProductos) {
        this.descuentoProductos = descuentoProductos;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getTotalImpuestos() {
        return totalImpuestos;
    }

    public void setTotalImpuestos(double totalImpuestos) {
        this.totalImpuestos = totalImpuestos;
    }

    public double getTotalDescuentos() {
        return totalDescuentos;
    }

    public void setTotalDescuentos(double totalDescuentos) {
        this.totalDescuentos = totalDescuentos;
    }

    public double getTotalRetenciones() {
        return totalRetenciones;
    }

    public void setTotalRetenciones(double totalRetenciones) {
        this.totalRetenciones = totalRetenciones;
    }

    public double getTotalFinal() {
        return totalFinal;
    }

    public void setTotalFinal(double totalFinal) {
        this.totalFinal = totalFinal;
    }

    /**
     * 🧮 Calcula todos los totales de la factura Debe llamarse antes de guardar o actualizar el
     * pedido
     */
    public void calcularTotales() {
        // 1. Calcular valores de impuestos y descuentos de cada item
        for (ItemPedido item : items) {
            item.calcularValorImpuesto();
            item.calcularValorDescuento();
        }

        // 2. Calcular subtotal (suma de items sin impuestos ni descuentos)
        this.subtotal = items.stream().mapToDouble(ItemPedido::getSubtotal).sum();

        // 3. Calcular total de impuestos
        this.totalImpuestos = items.stream().mapToDouble(ItemPedido::getValorImpuesto).sum();

        // 4. Calcular descuento de productos
        this.descuentoProductos = items.stream().mapToDouble(ItemPedido::getValorDescuento).sum();

        // 5. Calcular descuento general (si es porcentaje, convertir a valor)
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
        this.totalFinal =
                this.subtotal + this.totalImpuestos - this.totalDescuentos - this.totalRetenciones;

        // 9. Sincronizar con campo 'total' para compatibilidad con código existente
        this.total = this.totalFinal;
    }

    // Métodos de utilidad
    public void pagar(String formaPago, double propina, String pagadoPor) {
        this.estado = "pagado";
        this.formaPago = formaPago;
        this.propina = propina;
        this.totalPagado = this.total + propina;
        this.fechaPago = LocalDateTime.now();
        this.pagadoPor = pagadoPor;
        // Nota: cuadreCajaId debe ser asignado en el servicio que maneja el pago
    }

    public void pagar(String formaPago, double propina, String pagadoPor, String cuadreCajaId) {
        this.estado = "pagado";
        this.formaPago = formaPago;
        this.propina = propina;
        this.totalPagado = this.total + propina;
        this.fechaPago = LocalDateTime.now();
        this.pagadoPor = pagadoPor;
        this.cuadreCajaId = cuadreCajaId;
    }

    public void cancelar(String motivo, String canceladoPor) {
        this.estado = "cancelado";
        this.motivoCancelacion = motivo;
        this.canceladoPor = canceladoPor;
        this.fechaCancelacion = LocalDateTime.now();
    }
}
