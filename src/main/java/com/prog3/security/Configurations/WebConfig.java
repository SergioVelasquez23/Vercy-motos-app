package com.prog3.security.Configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins(
                "http://10.112.216.36:5300",
                "http://192.168.20.24:5300",
                "http://192.168.20.24:8081",
                "https://sopa-y-carbon.netlify.app",
                "https://zingy-kitsune-66762f.netlify.app",
                "https://sopa-y-carbon-app.web.app" // 🔥 Firebase frontend
            )
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true);
    }
}