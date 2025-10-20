package com.prog3.security.Controllers;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para testing de WebSocket
 */
@Controller
public class WebSocketTestController {

    /**
     * Endpoint para test de conexión WebSocket
     * Frontend puede enviar mensaje a /app/test
     */
    @MessageMapping("/test")
    @SendTo("/topic/test")
    public Map<String, Object> test(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("message", "WebSocket funcionando correctamente");
        response.put("received", message);
        response.put("timestamp", LocalDateTime.now().toString());
        return response;
    }

    /**
     * Echo simple para pruebas
     */
    @MessageMapping("/echo")
    @SendTo("/topic/echo")
    public Map<String, Object> echo(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("echo", message);
        response.put("timestamp", LocalDateTime.now().toString());
        return response;
    }
}