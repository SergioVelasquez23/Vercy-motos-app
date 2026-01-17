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
            .allowedOrigins(
                "http://10.112.216.36:5300",
                "http://192.168.20.24:5300",
                "http://192.168.20.24:8081",
                "https://sopa-y-carbon.netlify.app",
                "https://zingy-kitsune-66762f.netlify.app",
                        "https://sopa-y-carbon-app.web.app", // 🔥 Firebase frontend
                        "https://vercy-motos.web.app" // 🏍️ Vercy Motos frontend
            )
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true);
    }
}