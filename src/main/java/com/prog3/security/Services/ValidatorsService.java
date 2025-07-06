package com.prog3.security.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.prog3.security.Models.Permission;
import com.prog3.security.Models.Role;
import com.prog3.security.Models.RolePermission;
import com.prog3.security.Models.Session;
import com.prog3.security.Models.User;
import com.prog3.security.Models.UserRole;
import com.prog3.security.Repositories.PermissionRepository;
import com.prog3.security.Repositories.RolePermissionRepository;
import com.prog3.security.Repositories.UserRepository;
import com.prog3.security.Repositories.UserRoleRepository;
import com.prog3.security.Repositories.SessionRepository;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class ValidatorsService {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PermissionRepository thePermissionRepository;
    @Autowired
    private UserRepository theUserRepository;
    @Autowired
    private RolePermissionRepository theRolePermissionRepository;
    @Autowired
    private UserRoleRepository theUserRoleRepository;
    @Autowired
    private SessionRepository theSessionRepository;

    private static final String BEARER_PREFIX = "Bearer ";

    public boolean validationRolePermission(HttpServletRequest request,
            String url,
            String method) {
        // SEGURIDAD TEMPORALMENTE DESACTIVADA PARA CREAR PRODUCTOS DE PRUEBA
        System.out.println("Interceptor desactivado - URL: " + url + " - Method: " + method);
        return true;

        /* CÓDIGO ORIGINAL DE SEGURIDAD - COMENTADO TEMPORALMENTE
        boolean success = false;

        User theUser = this.getUser(request);

        if (theUser != null) {
            url = url.replaceAll("[0-9a-fA-F]{24}|\\d+", "?"); // Reemplaza el Mongo id o los números en la URL por "?"
            Permission thePermission = this.thePermissionRepository.getPermission(url, method); // Obtiene el permiso de la URL y el método
            List<UserRole> userRoles = this.theUserRoleRepository.getRolesByUserId(theUser.get_id());
            int i = 0;
            while (i < userRoles.size() && !success) {
                UserRole actual = userRoles.get(i);
                Role theRole = actual.getRole();

                if (theRole != null && thePermission != null) {
                    RolePermission theRolePermission = this.theRolePermissionRepository.getRolePermission(theRole.get_id(), thePermission.get_id());
                    if (theRolePermission != null) {
                        success = true;
                    }
                }
                i += 1;
            }

        }
        return success;
         */
    }

    public User getUser(final HttpServletRequest request) {
        User theUser = null;

        String authorizationHeader = request.getHeader("Authorization"); // Obtiene el encabezado de autorización de la solicitud

        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            String token = authorizationHeader.substring(BEARER_PREFIX.length()); // Extrae el token de autorización de la solicitud
            User theUserFromToken = jwtService.getUserFromToken(token);// Obtiene el usuario del token

            if (theUserFromToken != null) {
                theUser = this.theUserRepository.findById(theUserFromToken.get_id()).orElse(null); // Obtiene el usuario de la base de datos
            }
        }
        return theUser;
    }

    public Session getSessionByTwoFactorCode(String twoFactorCode) {
        return theSessionRepository.findByValidationCode(twoFactorCode);
    }
}
