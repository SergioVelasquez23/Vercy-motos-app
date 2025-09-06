package com.prog3.security.Config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Configuración de caché para optimizar el rendimiento del sistema
 * Implementa cache en memoria para consultas frecuentes
 */
@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    /**
     * Configuración del administrador de caché
     * Utiliza cache en memoria para evitar dependencias externas como Redis
     */
    @Bean
    @Override
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        
        cacheManager.setCaches(Arrays.asList(
            // Cache para productos (consultados frecuentemente en pedidos)
            new ConcurrentMapCache("productos", true), 
            
            // Cache para mesas (estado consultado constantemente)
            new ConcurrentMapCache("mesas", true),
            
            // Cache para categorías (rara vez cambian)
            new ConcurrentMapCache("categorias", true),
            
            // Cache para ingredientes (consultados en validaciones)
            new ConcurrentMapCache("ingredientes", true),
            
            // Cache para cuadres de caja abiertos (muy consultado)
            new ConcurrentMapCache("cuadres-activos", true),
            
            // Cache para pedidos activos por mesa (consultado en tiempo real)
            new ConcurrentMapCache("pedidos-activos", true),
            
            // Cache para reportes de ventas diarias (consulta pesada)
            new ConcurrentMapCache("reportes-ventas", true),
            
            // Cache para configuración del negocio (cambia raramente)
            new ConcurrentMapCache("config-negocio", true),
            
            // Cache para usuarios activos (para autenticación)
            new ConcurrentMapCache("usuarios", true),
            
            // Cache para inventario crítico (stock bajo)
            new ConcurrentMapCache("inventario-critico", true)
        ));
        
        return cacheManager;
    }
}
