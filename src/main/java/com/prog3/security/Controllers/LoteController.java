package com.prog3.security.Controllers;

import com.prog3.security.Models.Lote;
import com.prog3.security.Services.LoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lotes")
@Tag(name = "Lotes", description = "Gestión de lotes y fechas de vencimiento")
@CrossOrigin(origins = "*")
public class LoteController {

    @Autowired
    private LoteService loteService;

    /**
     * Obtener todos los lotes
     */
    @GetMapping
    @Operation(summary = "Listar todos los lotes")
    public ResponseEntity<List<Lote>> getAllLotes() {
        return ResponseEntity.ok(loteService.getAllLotes());
    }

    /**
     * Obtener lote por ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener lote por ID")
    public ResponseEntity<Lote> getLoteById(@PathVariable String id) {
        return ResponseEntity.ok(loteService.getLoteById(id));
    }

    /**
     * Crear nuevo lote
     */
    @PostMapping
    @Operation(summary = "Crear nuevo lote",
            description = "Registra un nuevo lote con fecha de vencimiento y actualiza inventario")
    public ResponseEntity<Map<String, Object>> crearLote(@RequestBody Lote lote) {
        try {
            Lote nuevoLote = loteService.crearLote(lote);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("mensaje", "Lote creado exitosamente");
            response.put("lote", nuevoLote);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "Error al crear lote: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Actualizar lote
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar lote")
    public ResponseEntity<Map<String, Object>> actualizarLote(@PathVariable String id,
            @RequestBody Lote lote) {
        try {
            Lote loteActualizado = loteService.actualizarLote(id, lote);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("mensaje", "Lote actualizado exitosamente");
            response.put("lote", loteActualizado);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "Error al actualizar lote: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Consumir cantidad de un lote
     */
    @PostMapping("/{id}/consumir")
    @Operation(summary = "Consumir lote",
            description = "Descuenta una cantidad específica de un lote")
    public ResponseEntity<Map<String, Object>> consumirLote(@PathVariable String id,
            @RequestBody Map<String, Double> request) {
        try {
            double cantidad = request.get("cantidad");
            Lote lote = loteService.consumirLote(id, cantidad);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("mensaje", "Lote consumido exitosamente");
            response.put("lote", lote);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "Error al consumir lote: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Consumir item usando FIFO
     */
    @PostMapping("/consumir-fifo")
    @Operation(summary = "Consumir usando FIFO",
            description = "Consume primero los lotes más antiguos (por fecha de vencimiento)")
    public ResponseEntity<Map<String, Object>> consumirFIFO(
            @RequestBody Map<String, Object> request) {
        try {
            String itemId = (String) request.get("itemId");
            double cantidad = ((Number) request.get("cantidad")).doubleValue();

            List<Map<String, Object>> consumos = loteService.consumirItemFIFO(itemId, cantidad);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("mensaje", "Consumo FIFO realizado exitosamente");
            response.put("consumos", consumos);
            response.put("totalLotesUsados", consumos.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "Error al consumir FIFO: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Obtener lotes de un item
     */
    @GetMapping("/item/{itemId}")
    @Operation(summary = "Lotes de un item",
            description = "Lista todos los lotes de un producto o ingrediente")
    public ResponseEntity<List<Lote>> getLotesByItem(@PathVariable String itemId) {
        return ResponseEntity.ok(loteService.getLotesByItem(itemId));
    }

    /**
     * Obtener lotes activos de un item
     */
    @GetMapping("/item/{itemId}/activos")
    @Operation(summary = "Lotes activos de un item")
    public ResponseEntity<List<Lote>> getLotesActivosByItem(@PathVariable String itemId) {
        return ResponseEntity.ok(loteService.getLotesActivosByItem(itemId));
    }

    /**
     * Obtener lotes de una bodega
     */
    @GetMapping("/bodega/{bodegaId}")
    @Operation(summary = "Lotes de una bodega")
    public ResponseEntity<List<Lote>> getLotesByBodega(@PathVariable String bodegaId) {
        return ResponseEntity.ok(loteService.getLotesByBodega(bodegaId));
    }

    /**
     * Obtener lotes próximos a vencer
     */
    @GetMapping("/por-vencer")
    @Operation(summary = "Lotes próximos a vencer",
            description = "Lista lotes que vencen en los próximos N días (default: 30)")
    public ResponseEntity<List<Lote>> getLotesPorVencer(
            @RequestParam(defaultValue = "30") int dias) {
        return ResponseEntity.ok(loteService.getLotesPorVencer(dias));
    }

    /**
     * Obtener lotes vencidos
     */
    @GetMapping("/vencidos")
    @Operation(summary = "Lotes vencidos",
            description = "Lista lotes que ya pasaron su fecha de vencimiento")
    public ResponseEntity<List<Lote>> getLotesVencidos() {
        return ResponseEntity.ok(loteService.getLotesVencidos());
    }

    /**
     * Marcar lotes vencidos automáticamente
     */
    @PostMapping("/marcar-vencidos")
    @Operation(summary = "Marcar lotes vencidos",
            description = "Actualiza el estado de lotes que ya vencieron")
    public ResponseEntity<Map<String, Object>> marcarLotesVencidos() {
        try {
            int cantidad = loteService.marcarLotesVencidos();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("mensaje", "Lotes marcados como vencidos");
            response.put("cantidad", cantidad);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "Error al marcar lotes vencidos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Retirar lote
     */
    @PostMapping("/{id}/retirar")
    @Operation(summary = "Retirar lote",
            description = "Retira un lote del inventario (por vencimiento u otro motivo)")
    public ResponseEntity<Map<String, Object>> retirarLote(@PathVariable String id,
            @RequestBody Map<String, String> request) {
        try {
            String motivo = request.get("motivo");
            Lote lote = loteService.retirarLote(id, motivo);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("mensaje", "Lote retirado exitosamente");
            response.put("lote", lote);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "Error al retirar lote: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Obtener resumen de lotes
     */
    @GetMapping("/resumen")
    @Operation(summary = "Resumen de lotes",
            description = "Estadísticas generales del sistema de lotes")
    public ResponseEntity<Map<String, Object>> getResumenLotes() {
        return ResponseEntity.ok(loteService.getResumenLotes());
    }
}
