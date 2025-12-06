package com.prog3.security.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.prog3.security.Services.CacheOptimizationService;

/**
 * Configuración para precalentar los caches críticos al iniciar la aplicación
 * Esto elimina el "cold start" y mejora dramáticamente la primera carga
 */
@Component
public class CacheWarmupConfig {

    @Autowired
    private CacheOptimizationService cacheService;

    /**
     * Se ejecuta automáticamente cuando la aplicación está lista
     * Precarga los caches más importantes para evitar demoras en la primera petición
     */
    @EventListener(ApplicationReadyEvent.class)
    public void warmupCachesOnStartup() {
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("🔥 INICIANDO WARMUP DE CACHES...");
        System.out.println("═══════════════════════════════════════════════════════");
        
        try {
            long startTime = System.currentTimeMillis();
            
            // Precargar todos los caches importantes
            cacheService.preloadImportantCaches();
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            System.out.println("═══════════════════════════════════════════════════════");
            System.out.println("✅ WARMUP COMPLETADO en " + duration + "ms");
            System.out.println("🚀 La aplicación está lista para servir requests rápidos");
            System.out.println("═══════════════════════════════════════════════════════");
            
        } catch (Exception e) {
            System.err.println("❌ ERROR durante warmup de caches: " + e.getMessage());
            e.printStackTrace();
            // No lanzar excepción para que la app arranque de todos modos
        }
    }
}
