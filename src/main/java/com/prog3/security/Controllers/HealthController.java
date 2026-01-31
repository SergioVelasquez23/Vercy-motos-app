package com.prog3.security.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.prog3.security.Utils.ApiResponse;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(originPatterns = {
        "https://vercy-motos-app.web.app",
        "https://vercy-motos-app-048m.onrender.com",
        "http://localhost:*",
        "http://127.0.0.1:*",
        "http://192.168.*.*:*"
}, allowCredentials = "true")
@RestController
public class HealthController {
    
    // Endpoint principal con prefijo /api
    @GetMapping("api/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("message", "Server is running");
        
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .message("Health check successful")
                .data(health)
                .build());
    }
    
    // Ping con prefijo /api - soporta GET y HEAD
    @RequestMapping(value = "api/health/ping", method = { RequestMethod.GET, RequestMethod.HEAD })
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    // ============================================
    // 🏥 ENDPOINTS SIN PREFIJO /api PARA UPTIMEROBOT
    // ============================================

    // Health check sin prefijo /api
    @GetMapping("health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheckNoPrefix() {
        return healthCheck();
    }

    // Ping sin prefijo /api - soporta GET y HEAD (para UptimeRobot)
    @RequestMapping(value = "health/ping", method = { RequestMethod.GET, RequestMethod.HEAD })
    public ResponseEntity<String> pingNoPrefix() {
        return ResponseEntity.ok("pong");
    }
}
