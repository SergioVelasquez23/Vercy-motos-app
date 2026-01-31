package com.prog3.security.Configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
//indica el tipo de la clase (config)
public class SecurityConfig {

    @Bean
    //metodo que define las reglas de seguridad http
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                //habilita cors con la configuracion definida
                .csrf(csrf -> csrf.disable())
                //deshabilita proteccion csrf (no necesaria en APIs REST)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll());
                //permite todas las solicitudes sin autenticacions
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // ✅ SOLUCIONADO: Orígenes específicos para permitir credenciales
        config.setAllowedOriginPatterns(List.of(
                "https://vercy-motos-app.web.app",
                "https://vercy-motos-app-048m.onrender.com",
                "http://localhost:*",
                "http://127.0.0.1:*",
                "http://192.168.*.*:*"));
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setExposedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
//Este archivo configura la seguridad básica de la API:
// Permite todas las solicitudes (sin autenticación).
// Habilita CORS para cualquier origen, método y header.
// Desactiva CSRF.
// No permite credenciales con CORS global.
// Ideal para desarrollo o APIs públicas, pero no recomendado para
// producción sin restricciones adicionales.