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
        // ✅ CORREGIDO: Usar orígenes específicos en lugar de "*" para permitir credenciales
        config.addAllowedOrigin("https://sopa-y-carbon-app.web.app"); // 🔥 Firebase frontend
        config.addAllowedOrigin("https://vercy-motos.web.app"); // 🏍️ Vercy Motos frontend
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true); // ✅ Ahora puede ser true con orígenes específicos

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