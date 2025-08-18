package com.prog3.security.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;

/**
 * Servicio para enviar notificaciones en tiempo real a través de WebSocket.
 */
@Service
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public WebSocketNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Envía una notificación de actualización de mesa a todos los clientes.
     */
    public void notificarActualizacionMesa(String mesaId, String nombreMesa, String accion) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("tipo", "MESA_ACTUALIZADA");
        payload.put("mesaId", mesaId);
        payload.put("nombreMesa", nombreMesa);
        payload.put("accion", accion);
        payload.put("timestamp", LocalDateTime.now().toString());

        messagingTemplate.convertAndSend("/topic/mesas", payload);
    }

    /**
     * Envía una notificación de actualización de pedido a todos los clientes.
     */
    public void notificarActualizacionPedido(String pedidoId, String mesaId, String estado) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("tipo", "PEDIDO_ACTUALIZADO");
        payload.put("pedidoId", pedidoId);
        payload.put("mesaId", mesaId);
        payload.put("estado", estado);
        payload.put("timestamp", LocalDateTime.now().toString());

        messagingTemplate.convertAndSend("/topic/pedidos", payload);
    }

    /**
     * Envía una notificación de actualización de mesa usando el objeto Mesa
     */
    public void notifyTableUpdate(Object mesa) {
        try {
            // Intentar obtener los datos mediante reflection para que sea más genérico
            String mesaId = getPropertyValue(mesa, "_id");
            String nombreMesa = getPropertyValue(mesa, "nombre");
            boolean ocupada = Boolean.parseBoolean(getPropertyValue(mesa, "ocupada"));

            // Crear y enviar la notificación
            Map<String, Object> payload = new HashMap<>();
            payload.put("tipo", "MESA_ACTUALIZADA");
            payload.put("mesaId", mesaId);
            payload.put("nombreMesa", nombreMesa);
            payload.put("ocupada", ocupada);
            payload.put("timestamp", LocalDateTime.now().toString());

            messagingTemplate.convertAndSend("/topic/mesas", payload);
        } catch (Exception e) {
            System.err.println("Error al enviar notificación de mesa: " + e.getMessage());
        }
    }

    /**
     * Helper method to get property values using reflection
     */
    private String getPropertyValue(Object obj, String fieldName) {
        try {
            String getterMethod = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            // Special case for _id field
            if (fieldName.equals("_id")) {
                getterMethod = "get_id";
            } // Special case for boolean fields
            else if (fieldName.equals("ocupada")) {
                getterMethod = "isOcupada";
            }

            return String.valueOf(obj.getClass().getMethod(getterMethod).invoke(obj));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Envía una notificación de inventario bajo a todos los clientes.
     */
    public void notificarInventarioBajo(String productoId, String nombreProducto, double stockActual, double stockMinimo) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("tipo", "ALERTA_INVENTARIO");
        payload.put("productoId", productoId);
        payload.put("nombreProducto", nombreProducto);
        payload.put("stockActual", stockActual);
        payload.put("stockMinimo", stockMinimo);
        payload.put("timestamp", LocalDateTime.now().toString());

        messagingTemplate.convertAndSend("/topic/inventario", payload);
    }

    /**
     * Envía una notificación de cambio en caja a todos los clientes.
     */
    public void notificarCambioCaja(String cuadreId, String accion, String responsable) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("tipo", "CAJA_ACTUALIZADA");
        payload.put("cuadreId", cuadreId);
        payload.put("accion", accion);
        payload.put("responsable", responsable);
        payload.put("timestamp", LocalDateTime.now().toString());

        messagingTemplate.convertAndSend("/topic/caja", payload);
    }

    /**
     * Envía una notificación a un usuario específico.
     */
    public void notificarAUsuario(String userId, String mensaje, String tipo) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("tipo", tipo);
        payload.put("mensaje", mensaje);
        payload.put("timestamp", LocalDateTime.now().toString());

        messagingTemplate.convertAndSendToUser(userId, "/queue/notificaciones", payload);
    }
}
