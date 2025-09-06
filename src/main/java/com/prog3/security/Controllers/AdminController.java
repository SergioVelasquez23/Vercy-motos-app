package com.prog3.security.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * Limpiar todos los datos de desarrollo (USE CON CUIDADO)
     */
    @PostMapping("/clear-data")
    public ResponseEntity<Map<String, Object>> clearAllData() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Long> deletedCounts = new HashMap<>();
        
        try {
            // Lista de colecciones a limpiar
            String[] collections = {
                "pedidos",
                "facturas", 
                "documentos_mesa",
                "movimientos_inventario",
                "cuadre_caja",
                "gastos",
                "ingresos_caja"
                // NO eliminamos: users, roles, mesas, productos, categorias, ingredientes, inventarios
            };
            
            // Limpiar cada colección
            for (String collection : collections) {
                try {
                    long deleted = mongoTemplate.remove(new Query(), collection).getDeletedCount();
                    deletedCounts.put(collection, deleted);
                } catch (Exception e) {
                    deletedCounts.put(collection, 0L);
                    System.err.println("Error limpiando colección " + collection + ": " + e.getMessage());
                }
            }
            
            // Resetear estado de mesas
            try {
                mongoTemplate.getCollection("mesas").updateMany(
                    new org.bson.Document(),
                    new org.bson.Document("$set", new org.bson.Document()
                        .append("ocupada", false)
                        .append("total", 0.0)
                        .append("productos", java.util.Arrays.asList())
                        .append("pedidoActual", null))
                );
                deletedCounts.put("mesas_reseteadas", 1L);
            } catch (Exception e) {
                System.err.println("Error reseteando mesas: " + e.getMessage());
            }
            
            response.put("success", true);
            response.put("message", "Datos de desarrollo limpiados exitosamente");
            response.put("deletedCounts", deletedCounts);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error limpiando datos: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Resetear solo el estado de las mesas
     */
    @PostMapping("/reset-mesas")
    public ResponseEntity<Map<String, Object>> resetMesas() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            mongoTemplate.getCollection("mesas").updateMany(
                new org.bson.Document(),
                new org.bson.Document("$set", new org.bson.Document()
                    .append("ocupada", false)
                    .append("total", 0.0)
                    .append("productos", java.util.Arrays.asList())
                    .append("pedidoActual", null))
            );
            
            response.put("success", true);
            response.put("message", "Estado de mesas reseteado exitosamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error reseteando mesas: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Obtener estadísticas de la base de datos
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Long> collectionCounts = new HashMap<>();
        
        try {
            String[] collections = {
                "users", "roles", "mesas", "productos", "categorias", 
                "ingredientes", "inventarios", "pedidos", "facturas", 
                "documentos_mesa", "movimientos_inventario", "cuadre_caja", 
                "gastos", "ingresos_caja"
            };
            
            for (String collection : collections) {
                try {
                    long count = mongoTemplate.getCollection(collection).countDocuments();
                    collectionCounts.put(collection, count);
                } catch (Exception e) {
                    collectionCounts.put(collection, 0L);
                }
            }
            
            response.put("success", true);
            response.put("collectionCounts", collectionCounts);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error obteniendo estadísticas: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
