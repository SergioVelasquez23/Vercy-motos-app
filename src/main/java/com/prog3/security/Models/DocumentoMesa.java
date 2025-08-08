package com.prog3.security.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Document(collection = "documentos_mesa")
public class DocumentoMesa {

    @Id
    private String _id;
    private String numeroDocumento;
    private LocalDateTime fecha;
    private double total;
    private String vendedor;
    private String mesaNombre;
    private List<String> pedidosIds; // IDs de los pedidos asociados
    private boolean pagado;
    private LocalDateTime fechaPago;
    private String formaPago;
    private String pagadoPor;
    private double propina;

    public DocumentoMesa() {
        this.pedidosIds = new ArrayList<>();
        this.pagado = false;
        this.propina = 0.0;
    }

    public DocumentoMesa(String numeroDocumento, LocalDateTime fecha, double total,
            String vendedor, String mesaNombre, List<String> pedidosIds) {
        this.numeroDocumento = numeroDocumento;
        this.fecha = fecha;
        this.total = total;
        this.vendedor = vendedor;
        this.mesaNombre = mesaNombre;
        this.pedidosIds = pedidosIds != null ? pedidosIds : new ArrayList<>();
        this.pagado = false;
        this.propina = 0.0;
    }

    // Getters y Setters
    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getVendedor() {
        return vendedor;
    }

    public void setVendedor(String vendedor) {
        this.vendedor = vendedor;
    }

    public String getMesaNombre() {
        return mesaNombre;
    }

    public void setMesaNombre(String mesaNombre) {
        this.mesaNombre = mesaNombre;
    }

    public List<String> getPedidosIds() {
        return pedidosIds;
    }

    public void setPedidosIds(List<String> pedidosIds) {
        this.pedidosIds = pedidosIds;
    }

    public boolean isPagado() {
        return pagado;
    }

    public void setPagado(boolean pagado) {
        this.pagado = pagado;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }

    public String getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(String formaPago) {
        this.formaPago = formaPago;
    }

    public String getPagadoPor() {
        return pagadoPor;
    }

    public void setPagadoPor(String pagadoPor) {
        this.pagadoPor = pagadoPor;
    }

    public double getPropina() {
        return propina;
    }

    public void setPropina(double propina) {
        this.propina = propina;
    }

    // Métodos de utilidad
    public void agregarPedido(String pedidoId) {
        if (!this.pedidosIds.contains(pedidoId)) {
            this.pedidosIds.add(pedidoId);
        }
    }

    public void removerPedido(String pedidoId) {
        this.pedidosIds.remove(pedidoId);
    }

    public int getCantidadPedidos() {
        return this.pedidosIds.size();
    }

    public String getEstado() {
        return pagado ? "Pagado" : "Pendiente";
    }

    @Override
    public String toString() {
        return "DocumentoMesa{"
                + "_id='" + _id + '\''
                + ", numeroDocumento='" + numeroDocumento + '\''
                + ", fecha=" + fecha
                + ", total=" + total
                + ", vendedor='" + vendedor + '\''
                + ", mesaNombre='" + mesaNombre + '\''
                + ", pedidosIds=" + pedidosIds
                + ", pagado=" + pagado
                + ", fechaPago=" + fechaPago
                + ", formaPago='" + formaPago + '\''
                + ", pagadoPor='" + pagadoPor + '\''
                + ", propina=" + propina
                + '}';
    }
}
