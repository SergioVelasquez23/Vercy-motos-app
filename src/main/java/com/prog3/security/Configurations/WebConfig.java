package com.prog3.security.Configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Configurar manejo de archivos estáticos para imágenes
        registry.addResourceHandler("/images/platos/**")
                .addResourceLocations("file:src/main/resources/static/images/platos/");
        
        // Servir archivos estáticos (incluyendo nuestra página de test)
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
                
        // Permitir acceso directo a la página de test
        registry.addResourceHandler("/websocket-test.html")
                .addResourceLocations("classpath:/static/");
    }
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                        .allowedOriginPatterns(
                                        "https://vercy-motos-app.web.app",
                                        "https://vercy-motos-app-048m.onrender.com",
                                        "http://localhost:*",
                                        "http://127.0.0.1:*",
                                        "http://192.168.*.*:*") // ✅ Orígenes específicos en lugar de "*"
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                        .allowedHeaders("*")
                        .exposedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
    }
}