package com.prog3.security.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.prog3.security.Models.User;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.List;

@Service
public class RoleValidatorService {

    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ValidatorsService validatorsService;

    /**
     * Validates if the user has any of the required roles
     */
    public User validateRoles(HttpServletRequest request, String... roles) {
        try {
            String authorizationHeader = request.getHeader("Authorization");

            if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
                String token = authorizationHeader.substring(BEARER_PREFIX.length());
                User user = validatorsService.getUser(request);

                if (user != null) {
                    List<String> userRoles = jwtService.getRolesFromToken(token);

                    // Check if the user has any of the required roles
                    boolean hasRole = false;
                    if (roles.length > 0) {
                        for (String role : roles) {
                            if (userRoles.contains(role)) {
                                hasRole = true;
                                break;
                            }
                        }
                    } else {
                        // If no roles specified, just check if authenticated
                        hasRole = true;
                    }

                    if (hasRole) {
                        return user;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error en validación de roles: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al validar roles: " + e.getMessage());
        }

        // If we get here, the user is not authorized
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para acceder a este recurso");
    }

    /**
     * Check if the user has all the specified roles
     */
    public boolean hasAllRoles(HttpServletRequest request, String... roles) {
        try {
            String authorizationHeader = request.getHeader("Authorization");

            if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
                String token = authorizationHeader.substring(BEARER_PREFIX.length());
                List<String> userRoles = jwtService.getRolesFromToken(token);

                return userRoles.containsAll(Arrays.asList(roles));
            }
        } catch (Exception e) {
            System.out.println("Error verificando roles (hasAllRoles): " + e.getMessage());
        }

        return false;
    }

    /**
     * Check if the user has any of the specified roles
     */
    public boolean hasAnyRole(HttpServletRequest request, String... roles) {
        try {
            String authorizationHeader = request.getHeader("Authorization");

            if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
                String token = authorizationHeader.substring(BEARER_PREFIX.length());
                List<String> userRoles = jwtService.getRolesFromToken(token);

                for (String role : roles) {
                    if (userRoles.contains(role)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error verificando roles (hasAnyRole): " + e.getMessage());
        }

        return false;
    }
}
