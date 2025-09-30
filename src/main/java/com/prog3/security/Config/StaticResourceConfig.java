package com.prog3.security.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Sirve archivos de /uploads/images/platos bajo la URL /images/platos/**
        registry.addResourceHandler("/images/platos/**")
                .addResourceLocations("file:uploads/images/platos/");
    }
}
