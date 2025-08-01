package com.prog3.security.Controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prog3.security.Models.User;
import com.prog3.security.Services.JwtService;
import com.prog3.security.Services.ResponseService;
import com.prog3.security.Services.ValidatorsService;
import com.prog3.security.Utils.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;

@CrossOrigin
@RestController
@RequestMapping("api/user-info")
public class UserInfoController {

    @Autowired
    private ValidatorsService validatorsService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ResponseService responseService;

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUserInfo(HttpServletRequest request) {
        try {
            String authorizationHeader = request.getHeader("Authorization");
            Map<String, Object> userInfo = new HashMap<>();

            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);

                // Validar token
                boolean isValidToken = jwtService.validateToken(token);
                userInfo.put("validToken", isValidToken);

                if (isValidToken) {
                    // Obtener datos del usuario
                    User user = jwtService.getUserFromToken(token);
                    if (user != null) {
                        userInfo.put("id", user.get_id());
                        userInfo.put("name", user.getName());
                        userInfo.put("email", user.getEmail());
                    }

                    // Obtener roles
                    List<String> roles = jwtService.getRolesFromToken(token);
                    userInfo.put("roles", roles);

                    // Verificar roles específicos
                    userInfo.put("isSuperAdmin", roles.contains("SUPERADMIN"));
                    userInfo.put("isAdmin", roles.contains("ADMIN"));
                    userInfo.put("isMesero", roles.contains("MESERO"));
                }
            } else {
                userInfo.put("error", "No se proporcionó token de autenticación");
            }

            return responseService.success(userInfo, "Información de usuario obtenida correctamente");
        } catch (Exception e) {
            e.printStackTrace();
            return responseService.internalError("Error obteniendo información del usuario: " + e.getMessage());
        }
    }
}
