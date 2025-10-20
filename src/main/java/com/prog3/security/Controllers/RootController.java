package com.prog3.security.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
// no RequestMapping required here
import org.springframework.web.bind.annotation.RestController;
import com.prog3.security.Utils.ApiResponse;
import com.prog3.security.Services.ResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.Instant;
import java.util.Map;
import java.util.HashMap;

/**
 * Controlador ligero para la raíz y endpoints de estado.
 * Evita que la raíz devuelva 500 en ausencia de recursos estáticos.
 */
@RestController
public class RootController {

    @Autowired
    private ResponseService responseService;

    @GetMapping("/")
    public ResponseEntity<ApiResponse<Map<String, Object>>> root() {
        Map<String, Object> body = new HashMap<>();
        body.put("message", "Sopa-y-Carbon API está arriba");
        body.put("timestamp", Instant.now().toString());
        return responseService.success(body, "API disponible");
    }

    @GetMapping("/api/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> status() {
        Map<String, Object> body = new HashMap<>();
        body.put("status", "ok");
        body.put("timestamp", Instant.now().toString());
        return responseService.success(body, "Status OK");
    }
}
