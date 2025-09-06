package com.prog3.security.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.prog3.security.Services.ResponseService;
import com.prog3.security.Utils.ApiResponse;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("api/notificaciones")
public class NotificacionesController {

    @Autowired
    private ResponseService responseService;

    /**
     * Endpoint simple para que el frontend verifique si hay actualizaciones
     * Este es un enfoque más simple que WebSocket para el dashboard
     */
    @GetMapping("/dashboard/estado")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEstadoDashboard() {
        try {
            Map<String, Object> estado = new HashMap<>();
            estado.put("timestamp", LocalDateTime.now().toString());
            estado.put("activo", true);
            estado.put("ultimaActualizacion", LocalDateTime.now().toString());
            
            return responseService.success(estado, "Estado del dashboard obtenido");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener estado del dashboard: " + e.getMessage());
        }
    }

    /**
     * Endpoint para verificar si hay pedidos recientes pagados
     */
    @GetMapping("/pedidos-recientes")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPedidosRecientes(
            @RequestParam(required = false, defaultValue = "5") int minutos) {
        try {
            Map<String, Object> respuesta = new HashMap<>();
            
            // Calcular hace cuántos minutos
            LocalDateTime hace = LocalDateTime.now().minusMinutes(minutos);
            respuesta.put("desde", hace.toString());
            respuesta.put("timestamp", LocalDateTime.now().toString());
            respuesta.put("hayNuevos", false); // Por ahora siempre false
            
            return responseService.success(respuesta, "Estado de pedidos recientes obtenido");
        } catch (Exception e) {
            return responseService.internalError("Error al verificar pedidos recientes: " + e.getMessage());
        }
    }
}
