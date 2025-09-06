package com.prog3.security.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.prog3.security.Services.CacheOptimizationService;
import com.prog3.security.Services.ResponseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador para la gestión del cache del sistema
 * Permite administrar y monitorear el rendimiento del cache
 */
@CrossOrigin
@RestController
@RequestMapping("api/cache")
@Tag(name = "Cache Management", description = "Gestión y monitoreo del cache del sistema")
public class CacheManagementController {

    @Autowired
    private CacheOptimizationService cacheService;
    
    @Autowired
    private ResponseService responseService;

    /**
     * Precarga los caches importantes del sistema
     */
    @Operation(
        summary = "Precargar caches importantes",
        description = "Precarga en memoria los datos más consultados para mejorar el rendimiento"
    )
    @PostMapping("/preload")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<String>> preloadCaches() {
        try {
            cacheService.preloadImportantCaches();
            return responseService.success("Caches precargados exitosamente", "Caches importantes precargados");
        } catch (Exception e) {
            return responseService.internalError("Error al precargar caches: " + e.getMessage());
        }
    }

    /**
     * Limpia el cache de productos
     */
    @Operation(
        summary = "Limpiar cache de productos",
        description = "Invalida todo el cache de productos para forzar recarga desde base de datos"
    )
    @DeleteMapping("/productos")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<String>> clearProductosCache() {
        try {
            cacheService.clearProductosCache();
            return responseService.success("Cache de productos limpiado", "Cache invalidado exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al limpiar cache de productos: " + e.getMessage());
        }
    }

    /**
     * Limpia el cache de mesas
     */
    @Operation(
        summary = "Limpiar cache de mesas",
        description = "Invalida todo el cache de mesas para forzar recarga desde base de datos"
    )
    @DeleteMapping("/mesas")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<String>> clearMesasCache() {
        try {
            cacheService.clearMesasCache();
            return responseService.success("Cache de mesas limpiado", "Cache invalidado exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al limpiar cache de mesas: " + e.getMessage());
        }
    }

    /**
     * Limpia todos los caches del sistema
     */
    @Operation(
        summary = "Limpiar todos los caches",
        description = "Invalida todos los caches del sistema. Usar con precaución en horarios de alto tráfico"
    )
    @DeleteMapping("/all")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<String>> clearAllCaches() {
        try {
            cacheService.clearAllCaches();
            return responseService.success("Todos los caches limpiados", "Sistema de cache reiniciado");
        } catch (Exception e) {
            return responseService.internalError("Error al limpiar todos los caches: " + e.getMessage());
        }
    }

    /**
     * Obtiene estadísticas básicas del sistema
     */
    @Operation(
        summary = "Obtener estadísticas del sistema",
        description = "Retorna estadísticas básicas del sistema (con cache para mejor rendimiento)"
    )
    @GetMapping("/stats")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<String>> getSystemStats() {
        try {
            String stats = cacheService.getSystemStatsCached();
            return responseService.success(stats, "Estadísticas obtenidas exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener estadísticas: " + e.getMessage());
        }
    }

    /**
     * Información sobre el estado del cache
     */
    @Operation(
        summary = "Estado del cache",
        description = "Información sobre la configuración y estado actual del cache"
    )
    @GetMapping("/info")
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<String>> getCacheInfo() {
        try {
            String info = """
                📊 CONFIGURACIÓN DEL CACHE
                
                🔧 Caches Activos:
                • productos (5 min TTL)
                • mesas (2 min TTL) 
                • categorias (10 min TTL)
                • ingredientes (10 min TTL)
                • cuadres-activos (1 min TTL)
                • pedidos-activos (30 seg TTL)
                • reportes-ventas (5 min TTL)
                • config-negocio (cache permanente)
                • usuarios (cache permanente)
                • inventario-critico (5 min TTL)
                
                ⚡ Beneficios:
                • Consultas 5-10x más rápidas
                • Menor carga en MongoDB
                • Mejor experiencia de usuario
                • Reducción de latencia
                
                🎯 Uso recomendado:
                • Precargar cache al inicio del día
                • Limpiar cache tras cambios importantes
                • Monitorear rendimiento regularmente
                """;
            
            return responseService.success(info, "Información del cache obtenida");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener información del cache: " + e.getMessage());
        }
    }
}
