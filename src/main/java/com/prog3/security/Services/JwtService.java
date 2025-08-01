package com.prog3.security.Services;

import com.prog3.security.Models.User;
import com.prog3.security.Models.UserRole;
import com.prog3.security.Repositories.UserRoleRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret; // Esta es la clave secreta que se utiliza para firmar el token. Debe mantenerse segura.

    @Value("${jwt.expiration}")
    private Long expiration; // Tiempo de expiración del token en milisegundos.

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    @Autowired
    private UserRoleRepository userRoleRepository;

    public HashMap<String, Object> generateToken(User theUser) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        Map<String, Object> claims = new HashMap<>();
        claims.put("_id", theUser.get_id());
        claims.put("name", theUser.getName());
        claims.put("email", theUser.getEmail());

        // Add roles to the token
        List<String> roles;
        try {
            System.out.println("Buscando roles para usuario con ID: " + theUser.get_id());
            List<UserRole> userRoles = userRoleRepository.getRolesByUserId(theUser.get_id());
            System.out.println("Roles encontrados: " + userRoles.size());

            // Si no se encuentran roles, asignar roles por defecto para testing
            if (userRoles.isEmpty()) {
                System.out.println("No se encontraron roles para el usuario. Asignando SUPERADMIN para pruebas");
                roles = Collections.singletonList("SUPERADMIN");
            } else {
                roles = userRoles.stream()
                        .map(userRole -> {
                            String roleName = userRole.getRole().getName();
                            System.out.println("Rol encontrado: " + roleName);
                            return roleName;
                        })
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            System.out.println("Error obteniendo roles: " + e.getMessage());
            e.printStackTrace();
            // Si hay error, asignar SUPERADMIN para pruebas
            roles = Collections.singletonList("SUPERADMIN");
        }
        System.out.println("Roles finales asignados al token: " + roles);
        claims.put("roles", roles);

        HashMap<String, Object> theResponse = new HashMap<>();

        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();

        theResponse.put("token", token);
        theResponse.put("expiration", expiryDate);

        return theResponse;
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);

            // Verifica la expiración del token
            Date now = new Date();
            return !claimsJws.getBody().getExpiration().before(now);
        } catch (SignatureException ex) {
            // La firma del token es inválida
            return false;
        } catch (Exception e) {
            // Otra excepción
            return false;
        }
    }

    public User getUserFromToken(String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);

            Claims claims = claimsJws.getBody();

            System.out.println("Claims: " + claims);

            User user = new User();
            user.set_id((String) claims.get("_id"));
            user.setName((String) claims.get("name"));
            user.setEmail((String) claims.get("email"));

            return user;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("SecretKey: " + getSigningKey());
            return null;
        }
    }

    // Helper method to extract roles from token
    public List<String> getRolesFromToken(String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);

            Claims claims = claimsJws.getBody();

            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) claims.get("roles");
            return roles != null ? roles : Collections.emptyList();
        } catch (Exception e) {
            System.out.println("Error getting roles: " + e.getMessage());
            return Collections.emptyList();
        }
    }

}
