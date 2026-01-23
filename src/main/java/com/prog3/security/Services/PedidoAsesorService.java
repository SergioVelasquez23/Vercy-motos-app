package com.prog3.security.Services;

import com.prog3.security.Models.PedidoAsesor;
import com.prog3.security.Models.PedidoAsesor.EstadoPedidoAsesor;
import com.prog3.security.Repositories.PedidoAsesorRepository;
import com.prog3.security.Exception.ResourceNotFoundException;
import com.prog3.security.Exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PedidoAsesorService {

    @Autowired
    private PedidoAsesorRepository pedidoAsesorRepository;

    @Autowired
    private WebSocketNotificationService webSocketService;

    /**
     * Crear un nuevo pedido de asesor
     */
    public PedidoAsesor crearPedido(PedidoAsesor pedido) {
        // Validaciones básicas
        if (pedido.getClienteNombre() == null || pedido.getClienteNombre().trim().isEmpty()) {
            throw new BusinessException("El nombre del cliente es obligatorio");
        }

        if (pedido.getAsesorNombre() == null || pedido.getAsesorNombre().trim().isEmpty()) {
            throw new BusinessException("El nombre del asesor es obligatorio");
        }

        if (pedido.getItems() == null || pedido.getItems().isEmpty()) {
            throw new BusinessException("El pedido debe tener al menos un item");
        }

        // Establecer valores por defecto
        pedido.setFechaCreacion(LocalDateTime.now());
        pedido.setFechaActualizacion(LocalDateTime.now());
        pedido.setEstado(EstadoPedidoAsesor.PENDIENTE);
        pedido.setFacturado(false);

        // Calcular totales
        pedido.calcularTotales();

        // Agregar al historial
        pedido.agregarHistorial("creado", pedido.getAsesorNombre(),
                String.format("Pedido creado con %d items por un total de $%.2f",
                        pedido.getItems().size(), pedido.getTotal()));

        // Guardar en la base de datos
        PedidoAsesor pedidoGuardado = pedidoAsesorRepository.save(pedido);

        // Notificar por WebSocket
        try {
            webSocketService.notificarActualizacionPedido(pedidoGuardado.getId(),
                    pedidoGuardado.getClienteNombre(), "PENDIENTE");
        } catch (Exception e) {
            // Log pero no fallar la operación
            System.err.println("Error enviando notificación WebSocket: " + e.getMessage());
        }

        return pedidoGuardado;
    }

    /**
     * Obtener un pedido por ID
     */
    public PedidoAsesor obtenerPedido(String id) {
        return pedidoAsesorRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Pedido no encontrado con ID: " + id));
    }

    /**
     * Listar todos los pedidos
     */
    public List<PedidoAsesor> listarTodos() {
        return pedidoAsesorRepository.findAllByOrderByFechaCreacionDesc();
    }

    /**
     * Listar pedidos por estado
     */
    public List<PedidoAsesor> listarPorEstado(EstadoPedidoAsesor estado) {
        return pedidoAsesorRepository.findByEstadoOrderByFechaCreacionDesc(estado);
    }

    /**
     * Listar pedidos por asesor
     */
    public List<PedidoAsesor> listarPorAsesor(String asesorId) {
        return pedidoAsesorRepository.findByAsesorId(asesorId);
    }

    /**
     * Listar pedidos por asesor y estado
     */
    public List<PedidoAsesor> listarPorAsesorYEstado(String asesorId, EstadoPedidoAsesor estado) {
        return pedidoAsesorRepository.findByAsesorIdAndEstado(asesorId, estado);
    }

    /**
     * Marcar pedido como facturado
     */
    public PedidoAsesor facturarPedido(String id, String facturaId, String facturadoPor) {
        PedidoAsesor pedido = obtenerPedido(id);

        // Validar estado
        if (pedido.getEstado() == EstadoPedidoAsesor.FACTURADO) {
            throw new BusinessException("El pedido ya ha sido facturado");
        }

        if (pedido.getEstado() == EstadoPedidoAsesor.CANCELADO) {
            throw new BusinessException("No se puede facturar un pedido cancelado");
        }

        // Actualizar datos de facturación
        pedido.setFacturado(true);
        pedido.setFacturaId(facturaId);
        pedido.setFacturadoPor(facturadoPor);
        pedido.setFechaFacturacion(LocalDateTime.now());
        pedido.setEstado(EstadoPedidoAsesor.FACTURADO);

        // Agregar al historial
        pedido.agregarHistorial("facturado", facturadoPor,
                String.format("Pedido facturado. ID Factura: %s", facturaId));

        // Guardar
        PedidoAsesor pedidoActualizado = pedidoAsesorRepository.save(pedido);

        // Notificar
        try {
            webSocketService.notificarActualizacionPedido(pedidoActualizado.getId(),
                    pedidoActualizado.getClienteNombre(), "FACTURADO");
        } catch (Exception e) {
            System.err.println("Error enviando notificación WebSocket: " + e.getMessage());
        }

        return pedidoActualizado;
    }

    /**
     * Cancelar pedido
     */
    public PedidoAsesor cancelarPedido(String id, String usuario, String motivo) {
        PedidoAsesor pedido = obtenerPedido(id);

        // Validar estado
        if (pedido.getEstado() == EstadoPedidoAsesor.FACTURADO) {
            throw new BusinessException("No se puede cancelar un pedido ya facturado");
        }

        if (pedido.getEstado() == EstadoPedidoAsesor.CANCELADO) {
            throw new BusinessException("El pedido ya está cancelado");
        }

        // Cancelar
        pedido.setEstado(EstadoPedidoAsesor.CANCELADO);

        // Agregar al historial
        pedido.agregarHistorial("cancelado", usuario, String.format("Pedido cancelado. Motivo: %s",
                motivo != null ? motivo : "No especificado"));

        // Guardar
        PedidoAsesor pedidoActualizado = pedidoAsesorRepository.save(pedido);

        // Notificar
        try {
            webSocketService.notificarActualizacionPedido(pedidoActualizado.getId(),
                    pedidoActualizado.getClienteNombre(), "CANCELADO");
        } catch (Exception e) {
            System.err.println("Error enviando notificación WebSocket: " + e.getMessage());
        }

        return pedidoActualizado;
    }

    /**
     * Actualizar pedido (solo si está pendiente)
     */
    public PedidoAsesor actualizarPedido(String id, PedidoAsesor pedidoActualizado,
            String usuario) {
        PedidoAsesor pedido = obtenerPedido(id);

        // Solo se pueden actualizar pedidos pendientes
        if (pedido.getEstado() != EstadoPedidoAsesor.PENDIENTE) {
            throw new BusinessException("Solo se pueden actualizar pedidos pendientes");
        }

        // Actualizar campos permitidos
        if (pedidoActualizado.getClienteNombre() != null) {
            pedido.setClienteNombre(pedidoActualizado.getClienteNombre());
        }

        if (pedidoActualizado.getClienteTelefono() != null) {
            pedido.setClienteTelefono(pedidoActualizado.getClienteTelefono());
        }

        if (pedidoActualizado.getItems() != null && !pedidoActualizado.getItems().isEmpty()) {
            pedido.setItems(pedidoActualizado.getItems());
            pedido.calcularTotales();
        }

        if (pedidoActualizado.getObservaciones() != null) {
            pedido.setObservaciones(pedidoActualizado.getObservaciones());
        }

        // Agregar al historial
        pedido.agregarHistorial("actualizado", usuario, "Pedido actualizado");

        return pedidoAsesorRepository.save(pedido);
    }

    /**
     * Eliminar pedido (solo si está cancelado o pendiente)
     */
    public void eliminarPedido(String id) {
        PedidoAsesor pedido = obtenerPedido(id);

        if (pedido.getEstado() == EstadoPedidoAsesor.FACTURADO) {
            throw new BusinessException("No se puede eliminar un pedido facturado");
        }

        pedidoAsesorRepository.deleteById(id);
    }

    /**
     * Obtener estadísticas de pedidos
     */
    public EstadisticasPedidos obtenerEstadisticas() {
        long pendientes = pedidoAsesorRepository.countByEstado(EstadoPedidoAsesor.PENDIENTE);
        long facturados = pedidoAsesorRepository.countByEstado(EstadoPedidoAsesor.FACTURADO);
        long cancelados = pedidoAsesorRepository.countByEstado(EstadoPedidoAsesor.CANCELADO);
        long total = pedidoAsesorRepository.count();

        return new EstadisticasPedidos(total, pendientes, facturados, cancelados);
    }

    // Clase auxiliar para estadísticas
    public static class EstadisticasPedidos {
        private long total;
        private long pendientes;
        private long facturados;
        private long cancelados;

        public EstadisticasPedidos(long total, long pendientes, long facturados, long cancelados) {
            this.total = total;
            this.pendientes = pendientes;
            this.facturados = facturados;
            this.cancelados = cancelados;
        }

        // Getters
        public long getTotal() {
            return total;
        }

        public long getPendientes() {
            return pendientes;
        }

        public long getFacturados() {
            return facturados;
        }

        public long getCancelados() {
            return cancelados;
        }
    }
}
