package com.prog3.security.Controllers;

//@Autowired para inyección de dependencias
import org.springframework.beans.factory.annotation.Autowired;
//Mongotemplate para operaciones con MongoDB
import org.springframework.data.mongodb.core.MongoTemplate;
//Para consultas a MongoDB
import org.springframework.data.mongodb.core.query.Query;
//Para respuestas HTTP controladas
import org.springframework.http.ResponseEntity;
//Anotaciones para controladores REST y manejo de CORS, hash y hashmap
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;

// Importar modelos
import com.prog3.security.Models.*;
// Importar repositorios
import com.prog3.security.Repositories.*;
//marca la clase como controlador REST
@RestController
//todas las rutas en este controlador comienzan con /api/admin
@RequestMapping("/api/admin")
//permite solicitudes CORS con credenciales desde localhost y Firebase
@CrossOrigin(origins = {
    "http://localhost:3000", 
    "http://localhost:8080", 
    "http://127.0.0.1:3000",
    "https://sopa-y-carbon-app.web.app"
}, allowCredentials = "true")

public class AdminController {

    @Autowired
    private MongoTemplate mongoTemplate;
    
    // Repositorios para eliminación por fechas
    @Autowired
    private PedidoRepository pedidoRepository;
    
    @Autowired
    private DocumentoMesaRepository documentoMesaRepository;
    
    @Autowired
    private CuadreCajaRepository cuadreCajaRepository;
    
    @Autowired
    private GastoRepository gastoRepository;
    
    @Autowired
    private IngresoCajaRepository ingresoCajaRepository;
    
    @Autowired
    private FacturaRepository facturaRepository;

    //
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
    
    /**
     * 📊 CONTADOR PREVIO - Muestra qué se va a eliminar antes de hacerlo
     * Útil para confirmar antes de eliminar datos
     */
    @GetMapping("/contar-por-fechas")
    public ResponseEntity<Map<String, Object>> contarRegistrosPorFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        
        Map<String, Object> response = new HashMap<>();
        Map<String, Long> conteos = new HashMap<>();
        
        try {
            System.out.println("📊 CONTANDO REGISTROS ENTRE " + fechaInicio + " Y " + fechaFin);
            
            // Contar cada tipo de registro
            List<Pedido> pedidos = pedidoRepository.findByFechaBetween(fechaInicio, fechaFin);
            conteos.put("pedidos", (long) pedidos.size());
            
            List<DocumentoMesa> documentos = documentoMesaRepository.findByFechaBetween(fechaInicio, fechaFin);
            conteos.put("documentos_mesa", (long) documentos.size());
            
            List<CuadreCaja> cuadres = cuadreCajaRepository.findByFechaAperturaBetween(fechaInicio, fechaFin);
            conteos.put("cuadres_caja", (long) cuadres.size());
            
            List<Gasto> gastos = gastoRepository.findByFechaGastoBetween(fechaInicio, fechaFin);
            conteos.put("gastos", (long) gastos.size());
            
            List<IngresoCaja> ingresos = ingresoCajaRepository.findByFechaIngresoBetween(fechaInicio, fechaFin);
            conteos.put("ingresos_caja", (long) ingresos.size());
            
            List<Factura> facturas = facturaRepository.findByFechaBetween(fechaInicio, fechaFin);
            conteos.put("facturas", (long) facturas.size());
            
            // Calcular total
            long totalRegistros = conteos.values().stream().mapToLong(Long::longValue).sum();
            
            response.put("success", true);
            response.put("fechaInicio", fechaInicio);
            response.put("fechaFin", fechaFin);
            response.put("conteos", conteos);
            response.put("totalRegistros", totalRegistros);
            
            System.out.println("✅ CONTEO COMPLETADO - Total: " + totalRegistros + " registros");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error contando registros: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Error contando registros: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 🗑️ ELIMINAR TODO POR RANGO DE FECHAS
     * Elimina pedidos, documentos, cuadres, gastos e ingresos en un período específico
     * ⚠️ USAR CON PRECAUCIÓN - Esta operación es irreversible
     */
    @DeleteMapping("/eliminar-todo-por-fechas")
    public ResponseEntity<Map<String, Object>> eliminarTodoPorFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(defaultValue = "false") boolean incluirFacturas) {
        
        Map<String, Object> response = new HashMap<>();
        Map<String, Long> eliminados = new HashMap<>();
        
        try {
            System.out.println("⚠️ INICIANDO ELIMINACIÓN MASIVA ENTRE " + fechaInicio + " Y " + fechaFin);
            System.out.println("🧾 Incluir facturas: " + incluirFacturas);
            
            // 1. Eliminar pedidos en rango (tanto por fecha de creación como por fecha de pago)
            List<Pedido> pedidosPorFecha = pedidoRepository.findByFechaBetween(fechaInicio, fechaFin);
            List<Pedido> pedidosPorFechaPago = pedidoRepository.findByFechaPagoBetween(fechaInicio, fechaFin);
            
            // Combinar ambas listas evitando duplicados
            Set<String> idsEliminados = new HashSet<>();
            List<Pedido> todosLosPedidos = new ArrayList<>();
            
            for (Pedido pedido : pedidosPorFecha) {
                if (!idsEliminados.contains(pedido.get_id())) {
                    todosLosPedidos.add(pedido);
                    idsEliminados.add(pedido.get_id());
                }
            }
            
            for (Pedido pedido : pedidosPorFechaPago) {
                if (!idsEliminados.contains(pedido.get_id())) {
                    todosLosPedidos.add(pedido);
                    idsEliminados.add(pedido.get_id());
                }
            }
            
            if (!todosLosPedidos.isEmpty()) {
                System.out.println("🔍 DEPURACIÓN - Pedidos a eliminar:");
                for (Pedido pedido : todosLosPedidos) {
                    System.out.println("  📦 ID: " + pedido.get_id() + 
                                      " | Estado: " + pedido.getEstado() + 
                                      " | Forma pago: " + pedido.getFormaPago() + 
                                      " | Fecha creación: " + pedido.getFecha() +
                                      " | Fecha pago: " + pedido.getFechaPago());
                }
                
                pedidoRepository.deleteAll(todosLosPedidos);
                eliminados.put("pedidos", (long) todosLosPedidos.size());
                System.out.println("✅ Eliminados " + todosLosPedidos.size() + " pedidos (por fecha creación y fecha pago)");
            } else {
                eliminados.put("pedidos", 0L);
                System.out.println("⚠️ No se encontraron pedidos para eliminar en el rango de fechas");
            }
            
            // 2. Eliminar documentos mesa en rango  
            List<DocumentoMesa> documentos = documentoMesaRepository.findByFechaBetween(fechaInicio, fechaFin);
            if (!documentos.isEmpty()) {
                documentoMesaRepository.deleteAll(documentos);
                eliminados.put("documentos_mesa", (long) documentos.size());
                System.out.println("✅ Eliminados " + documentos.size() + " documentos de mesa");
            } else {
                eliminados.put("documentos_mesa", 0L);
            }
            
            // 3. Eliminar cuadres de caja en rango
            List<CuadreCaja> cuadres = cuadreCajaRepository.findByFechaAperturaBetween(fechaInicio, fechaFin);
                    
            if (!cuadres.isEmpty()) {
                cuadreCajaRepository.deleteAll(cuadres);
                eliminados.put("cuadres_caja", (long) cuadres.size());
                System.out.println("✅ Eliminados " + cuadres.size() + " cuadres de caja");
            } else {
                eliminados.put("cuadres_caja", 0L);
            }
            
            // 4. Eliminar gastos en rango
            List<Gasto> gastos = gastoRepository.findByFechaGastoBetween(fechaInicio, fechaFin);
            if (!gastos.isEmpty()) {
                gastoRepository.deleteAll(gastos);
                eliminados.put("gastos", (long) gastos.size());
                System.out.println("✅ Eliminados " + gastos.size() + " gastos");
            } else {
                eliminados.put("gastos", 0L);
            }
            
            // 5. Eliminar ingresos en rango
            List<IngresoCaja> ingresos = ingresoCajaRepository.findByFechaIngresoBetween(fechaInicio, fechaFin);
            if (!ingresos.isEmpty()) {
                ingresoCajaRepository.deleteAll(ingresos);
                eliminados.put("ingresos_caja", (long) ingresos.size());
                System.out.println("✅ Eliminados " + ingresos.size() + " ingresos de caja");
            } else {
                eliminados.put("ingresos_caja", 0L);
            }
            
            // 6. Eliminar facturas en rango (OPCIONAL)
            if (incluirFacturas) {
                List<Factura> facturas = facturaRepository.findByFechaBetween(fechaInicio, fechaFin);
                if (!facturas.isEmpty()) {
                    facturaRepository.deleteAll(facturas);
                    eliminados.put("facturas", (long) facturas.size());
                    System.out.println("✅ Eliminadas " + facturas.size() + " facturas");
                } else {
                    eliminados.put("facturas", 0L);
                }
            } else {
                eliminados.put("facturas", 0L);
                System.out.println("ℹ️ Facturas omitidas (incluirFacturas=false)");
            }
            
            // Calcular total eliminado
            long totalEliminado = eliminados.values().stream().mapToLong(Long::longValue).sum();
            
            // Resetear estado de mesas después de eliminar pedidos
            try {
                mongoTemplate.getCollection("mesas").updateMany(
                    new org.bson.Document(),
                    new org.bson.Document("$set", new org.bson.Document()
                        .append("ocupada", false)
                        .append("total", 0.0)
                        .append("productos", java.util.Arrays.asList())
                        .append("pedidoActual", null))
                );
                System.out.println("✅ Estado de mesas reseteado");
            } catch (Exception e) {
                System.err.println("⚠️ Error reseteando mesas: " + e.getMessage());
            }
            
            response.put("success", true);
            response.put("message", "Datos eliminados exitosamente en el rango de fechas");
            response.put("fechaInicio", fechaInicio);
            response.put("fechaFin", fechaFin);
            response.put("incluirFacturas", incluirFacturas);
            response.put("eliminados", eliminados);
            response.put("totalEliminado", totalEliminado);
            
            System.out.println("🎉 ELIMINACIÓN COMPLETADA - Total: " + totalEliminado + " registros eliminados");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error eliminando datos: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error eliminando datos: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Elimina todos los pedidos con estado activo o pendiente y reinicia las mesas
     * 
     * @return ResponseEntity con resultado de la operación
     */
    @DeleteMapping("/eliminar-todos-pedidos-activos")
    public ResponseEntity<Map<String, Object>> eliminarTodosPedidosActivos() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("🔄 Iniciando eliminación de pedidos activos...");
            
            // Buscar todos los pedidos con estado activo o pendiente
            List<String> estados = List.of("activo", "pendiente");
            List<Pedido> pedidosActivos = pedidoRepository.findByEstadoIn(estados);
            
            int cantidadPedidos = pedidosActivos.size();
            System.out.println("🔍 Se encontraron " + cantidadPedidos + " pedidos activos/pendientes para eliminar");
            
            if (cantidadPedidos > 0) {
                // Eliminar todos los pedidos activos
                pedidoRepository.deleteAll(pedidosActivos);
                System.out.println("✅ Eliminados " + cantidadPedidos + " pedidos activos/pendientes");
            } else {
                System.out.println("ℹ️ No se encontraron pedidos activos para eliminar");
            }
            
            // Resetear estado de todas las mesas
            try {
                mongoTemplate.getCollection("mesas").updateMany(
                    new org.bson.Document(),
                    new org.bson.Document("$set", new org.bson.Document()
                        .append("ocupada", false)
                        .append("total", 0.0)
                        .append("productos", java.util.Arrays.asList())
                        .append("pedidoActual", null))
                );
                System.out.println("✅ Estado de todas las mesas reseteado");
            } catch (Exception e) {
                System.err.println("⚠️ Error reseteando mesas: " + e.getMessage());
            }
            
            // Preparar respuesta
            response.put("success", true);
            response.put("message", "Pedidos activos eliminados y mesas reseteadas correctamente");
            response.put("pedidosEliminados", cantidadPedidos);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error eliminando pedidos activos: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error eliminando pedidos activos: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
