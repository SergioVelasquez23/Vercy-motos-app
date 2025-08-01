package com.prog3.security.Controllers;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prog3.security.Models.Permission;
import com.prog3.security.Models.Session;
import com.prog3.security.Models.User;
import com.prog3.security.Repositories.SessionRepository;
import com.prog3.security.Repositories.UserRepository;
import com.prog3.security.Services.EncryptionService;
import com.prog3.security.Services.JwtService;
import com.prog3.security.Services.RequestURL;
import com.prog3.security.Services.ValidatorsService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@CrossOrigin
@RestController
@RequestMapping("/api/public/security")
public class SecurityController {

    @Autowired
    private UserRepository theUserRepository;

    @Autowired
    private SessionRepository theSessionRepository;

    @Autowired
    private EncryptionService theEncryptionService;

    @Autowired
    private JwtService theJwtService;

    @Autowired
    private RequestURL theRequestURL;

    @Autowired
    private ValidatorsService theValidatorsService;

    // Endpoint: /login
    @PostMapping("/login")
    public HashMap<String, Object> login(@RequestBody User theNewUser, final HttpServletResponse response) throws IOException {
        HashMap<String, Object> theResponse = new HashMap<>();
        User theActualUser = this.theUserRepository.getUserByEmail(theNewUser.getEmail());

        if (theActualUser != null
                && theActualUser.getPassword().equals(theEncryptionService.convertSHA256(theNewUser.getPassword()))) {
            // Generar un único código de dos factores
            String twoFactorCode = theEncryptionService.generateValidationCode();

            // Crear una nueva sesión y guardar el código
            Session newSession = new Session();
            newSession.setUser(theActualUser);
            newSession.setValidationCode(twoFactorCode);
            newSession.setExpirationDate(new Date(System.currentTimeMillis() + 3600000)); // 1 hora de expiración
            this.theSessionRepository.save(newSession);

            // Generar el token JWT
            HashMap<String, Object> tokenResponse = theJwtService.generateToken(theActualUser);
            String token = tokenResponse.get("token").toString();
            Date expirationDate = (Date) tokenResponse.get("expiration");

            // No enviar el código por correo ni devolverlo en la respuesta
            theActualUser.setPassword("");
            theActualUser.setNumeroDeSesiones(0);
            theResponse.put("user", theActualUser);
            theResponse.put("token", token);
            theResponse.put("expiration", expirationDate);
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
        return theResponse;
    }

    // Endpoint: /login/validate/{twoFactorCode}
    @PostMapping("/login/validate/{twoFactorCode}")
    public HashMap<String, Object> validateLogin(@PathVariable String twoFactorCode, final HttpServletResponse response) throws IOException {
        HashMap<String, Object> theResponse = new HashMap<>();

        // Buscar la sesión activa usando el código de dos factores
        Session validSession = theSessionRepository.findByValidationCode(twoFactorCode);

        if (validSession == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Código de dos factores incorrecto");
            return theResponse;
        }

        User theActualUser = validSession.getUser();
        if (theActualUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Usuario no encontrado");
            return theResponse;
        }

        // Generar el token JWT
        HashMap<String, Object> tokenResponse = theJwtService.generateToken(theActualUser);
        String token = tokenResponse.get("token").toString();
        Date expirationDate = (Date) tokenResponse.get("expiration");

        // Actualizar la sesión con el token y limpiar el código de validación
        validSession.setValidationCode("");
        validSession.setToken(token);
        validSession.setExpirationDate(expirationDate);
        theSessionRepository.save(validSession);

        // Incrementar el número de sesiones del usuario
        theActualUser.setNumeroDeSesiones(theActualUser.getNumeroDeSesiones() + 1);
        theUserRepository.save(theActualUser);

        // Preparar la respuesta
        theActualUser.setPassword(""); // No devolver la contraseña
        theResponse.put("user", theActualUser);
        theResponse.put("token", token);
        theResponse.put("expiration", expirationDate);

        return theResponse;
    }

    // Endpoint: /login-no-auth
    @PostMapping("/login-no-auth")
    public HashMap<String, Object> loginNoAuth(@RequestBody User theNewUser, final HttpServletResponse response) throws IOException {
        HashMap<String, Object> theResponse = new HashMap<>();
        User theActualUser = this.theUserRepository.getUserByEmail(theNewUser.getEmail());

        if (theActualUser != null
                && theActualUser.getPassword().equals(theEncryptionService.convertSHA256(theNewUser.getPassword()))) {

            String token = theJwtService.generateToken(theActualUser).get("token").toString();
            theActualUser.setPassword(""); // No devolver la contraseña
            theResponse.put("token", token);
            theResponse.put("user", theActualUser);
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
        return theResponse;
    }

    // Endpoint to get user roles
    @GetMapping("/me/roles")
    public HashMap<String, Object> getUserRoles(HttpServletRequest request) {
        HashMap<String, Object> response = new HashMap<>();

        try {
            // Get the token from the Authorization header
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                // Get roles from token
                List<String> roles = theJwtService.getRolesFromToken(token);
                response.put("roles", roles);
                response.put("success", true);
            } else {
                response.put("success", false);
                response.put("message", "No authorization token provided");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }

        return response;
    }
    // Método auxiliar: Validación de dos factores

    // private boolean twoFactorValidation(User theActualUser, String twoFactorCode) {
    //     List<Session> theSessions = theSessionRepository.getSessionByUser(theActualUser.get_id());
    //     for (Session session : theSessions) {
    //         if (session.getValidationCode().equals(twoFactorCode)) {
    //             return true;
    //         }
    //     }
    //     return false;
    // }
    // Endpoint: /permissions-validation
    @PostMapping("permissions-validation")
    public boolean permissionsValidation(final HttpServletRequest request,
            @RequestBody Permission thePermission) {
        boolean success = this.theValidatorsService.validationRolePermission(request, thePermission.getUrl(), thePermission.getMethod());
        return success;
    }

    // Endpoint: /{userId}/matchSession/{sessionId}
    @PutMapping("/{userId}/matchSession/{sessionId}")
    public String matchSession(@PathVariable String userId, @PathVariable String sessionId) {
        User theActualUser = this.theUserRepository.findById(userId).orElse(null);
        Session theActualSession = this.theSessionRepository.findById(sessionId).orElse(null);

        if (theActualUser != null && theActualSession != null) {
            theActualSession.setUser(theActualUser);
            this.theSessionRepository.save(theActualSession);
            return "Session matched to user successfully";
        } else {
            return "User or session not found";
        }
    }

    // Endpoint: /{userId}/unmatchSession/{sessionId}
    @PutMapping("/{userId}/unmatchSession/{sessionId}")
    public String unmatchSession(@PathVariable String userId, @PathVariable String sessionId) {
        Session theActualSession = this.theSessionRepository.findById(sessionId).orElse(null);

        if (theActualSession != null && theActualSession.getUser() != null && theActualSession.getUser().get_id().equals(userId)) {
            theActualSession.setUser(null);
            this.theSessionRepository.save(theActualSession);
            return "Session unmatched from user successfully";
        } else {
            return "User or session not found";
        }
    }

    // Endpoint: /user/{userId}/numeroDeSesiones
    @GetMapping("/user/{userId}/numeroDeSesiones")
    public int getNumeroDeSesiones(@PathVariable String userId) {
        User theActualUser = this.theUserRepository.findById(userId).orElse(null);

        if (theActualUser != null) {
            return theActualUser.getNumeroDeSesiones();
        } else {
            throw new RuntimeException("Usuario no encontrado");
        }
    }

    // Endpoint: /send-email
    @PostMapping("/send-email")
    public String sendEmail(@RequestBody Map<String, String> requestBody) {
        String message = requestBody.get("message");
        String recipient = requestBody.get("recipient");
        return theRequestURL.sendEmail(message, recipient);
    }

    // Endpoint: /send-reset-link
    @PostMapping("/send-reset-link")
    public String sendResetLink(@RequestBody Map<String, String> requestBody) {
        String message = requestBody.get("message");
        String recipient = requestBody.get("recipient");
        return theRequestURL.sendResetLink(message, recipient);
    }

    // Endpoint: /send-payment-notification
    @PostMapping("/send-payment-notification")
    public String sendPaymentNotification(@RequestBody Map<String, String> requestBody) {
        String message = requestBody.get("message");
        String recipient = requestBody.get("recipient");
        return theRequestURL.sendPaymentNotification(message, recipient);
    }
}
