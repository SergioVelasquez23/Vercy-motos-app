package com.prog3.security.Controllers;

import com.prog3.security.DTOs.CrearTransferenciaRequest;
import com.prog3.security.DTOs.StockBodegaDTO;
import com.prog3.security.Models.Bodega;
import com.prog3.security.Models.InventarioBodega;
import com.prog3.security.Models.TransferenciaBodega;
import com.prog3.security.Services.BodegaService;
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
@RequestMapping("/api/bodegas")
@Tag(name = "Bodegas", description = "Gestión de múltiples bodegas/almacenes")
@CrossOrigin(origins = "*")
public class BodegaController {

    @Autowired
    private BodegaService bodegaService;

    /**
     * Obtener todas las bodegas
     */
    @GetMapping
    @Operation(summary = "Listar todas las bodegas",
            description = "Obtiene la lista completa de bodegas del sistema")
    public ResponseEntity<List<Bodega>> getAllBodegas() {
        return ResponseEntity.ok(bodegaService.getAllBodegas());
    }

    /**
     * Obtener bodegas activas
     */
    @GetMapping("/activas")
    @Operation(summary = "Listar bodegas activas",
            description = "Obtiene solo las bodegas que están activas")
    public ResponseEntity<List<Bodega>> getBodegasActivas() {
        return ResponseEntity.ok(bodegaService.getBodegasActivas());
    }

    /**
     * Obtener bodega por ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener bodega por ID")
    public ResponseEntity<Bodega> getBodegaById(@PathVariable String id) {
        return ResponseEntity.ok(bodegaService.getBodegaById(id));
    }

    /**
     * Crear nueva bodega
     */
    @PostMapping
    @Operation(summary = "Crear nueva bodega",
            description = "Registra una nueva bodega en el sistema")
    public ResponseEntity<Map<String, Object>> crearBodega(@RequestBody Bodega bodega) {
        try {
            Bodega nuevaBodega = bodegaService.crearBodega(bodega);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("mensaje", "Bodega creada exitosamente");
            response.put("bodega", nuevaBodega);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "Error al crear bodega: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Actualizar bodega
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar bodega",
            description = "Actualiza la información de una bodega existente")
    public ResponseEntity<Map<String, Object>> actualizarBodega(@PathVariable String id,
            @RequestBody Bodega bodega) {
        try {
            Bodega bodegaActualizada = bodegaService.actualizarBodega(id, bodega);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("mensaje", "Bodega actualizada exitosamente");
            response.put("bodega", bodegaActualizada);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "Error al actualizar bodega: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Eliminar bodega
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar bodega",
            description = "Elimina una bodega (solo si no tiene inventario)")
    public ResponseEntity<Map<String, Object>> eliminarBodega(@PathVariable String id) {
        try {
            bodegaService.eliminarBodega(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("mensaje", "Bodega eliminada exitosamente");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "Error al eliminar bodega: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Obtener inventario de una bodega
     */
    @GetMapping("/{id}/inventario")
    @Operation(summary = "Inventario de bodega",
            description = "Lista todos los items en el inventario de una bodega")
    public ResponseEntity<List<StockBodegaDTO>> getInventarioBodega(@PathVariable String id) {
        return ResponseEntity.ok(bodegaService.getInventarioBodega(id));
    }

    /**
     * Obtener stock de un item en todas las bodegas
     */
    @GetMapping("/stock/{tipoItem}/{itemId}")
    @Operation(summary = "Stock de item en bodegas",
            description = "Muestra el stock de un producto/ingrediente en todas las bodegas")
    public ResponseEntity<List<StockBodegaDTO>> getStockItemEnBodegas(@PathVariable String tipoItem,
            @PathVariable String itemId) {
        return ResponseEntity.ok(bodegaService.getStockItemEnBodegas(itemId, tipoItem));
    }

    /**
     * Ajustar stock en bodega
     */
    @PostMapping("/{bodegaId}/ajustar-stock")
    @Operation(summary = "Ajustar stock",
            description = "Ajusta el stock de un item en una bodega específica")
    public ResponseEntity<Map<String, Object>> ajustarStock(@PathVariable String bodegaId,
            @RequestBody Map<String, Object> request) {
        try {
            String itemId = (String) request.get("itemId");
            String tipoItem = (String) request.get("tipoItem");
            double cantidad = ((Number) request.get("cantidad")).doubleValue();
            String motivo = (String) request.get("motivo");

            InventarioBodega inventario =
                    bodegaService.ajustarStockBodega(bodegaId, itemId, tipoItem, cantidad, motivo);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("mensaje", "Stock ajustado exitosamente");
            response.put("inventario", inventario);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "Error al ajustar stock: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Crear transferencia entre bodegas
     */
    @PostMapping("/transferencias")
    @Operation(summary = "Crear transferencia",
            description = "Crea una solicitud de transferencia entre bodegas")
    public ResponseEntity<Map<String, Object>> crearTransferencia(
            @RequestBody CrearTransferenciaRequest request) {
        try {
            TransferenciaBodega transferencia = bodegaService.crearTransferencia(request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("mensaje", "Transferencia creada exitosamente");
            response.put("transferencia", transferencia);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "Error al crear transferencia: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Aprobar transferencia
     */
    @PostMapping("/transferencias/{id}/aprobar")
    @Operation(summary = "Aprobar transferencia",
            description = "Aprueba y ejecuta una transferencia pendiente")
    public ResponseEntity<Map<String, Object>> aprobarTransferencia(@PathVariable String id) {
        try {
            TransferenciaBodega transferencia = bodegaService.aprobarTransferencia(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("mensaje", "Transferencia aprobada y ejecutada");
            response.put("transferencia", transferencia);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "Error al aprobar transferencia: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Rechazar transferencia
     */
    @PostMapping("/transferencias/{id}/rechazar")
    @Operation(summary = "Rechazar transferencia",
            description = "Rechaza una transferencia pendiente")
    public ResponseEntity<Map<String, Object>> rechazarTransferencia(@PathVariable String id,
            @RequestBody Map<String, String> request) {
        try {
            String motivo = request.get("motivo");
            TransferenciaBodega transferencia = bodegaService.rechazarTransferencia(id, motivo);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("mensaje", "Transferencia rechazada");
            response.put("transferencia", transferencia);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "Error al rechazar transferencia: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Obtener transferencias de una bodega
     */
    @GetMapping("/{bodegaId}/transferencias")
    @Operation(summary = "Transferencias de bodega",
            description = "Lista todas las transferencias relacionadas con una bodega")
    public ResponseEntity<List<TransferenciaBodega>> getTransferenciasByBodega(
            @PathVariable String bodegaId) {
        return ResponseEntity.ok(bodegaService.getTransferenciasByBodega(bodegaId));
    }

    /**
     * Obtener items con stock bajo
     */
    @GetMapping("/{id}/stock-bajo")
    @Operation(summary = "Items con stock bajo",
            description = "Lista items que están por debajo del stock mínimo")
    public ResponseEntity<List<StockBodegaDTO>> getItemsStockBajo(@PathVariable String id) {
        return ResponseEntity.ok(bodegaService.getItemsStockBajo(id));
    }

    /**
     * Obtener resumen de inventario
     */
    @GetMapping("/{id}/resumen")
    @Operation(summary = "Resumen de inventario",
            description = "Estadísticas generales del inventario de una bodega")
    public ResponseEntity<Map<String, Object>> getResumenInventario(@PathVariable String id) {
        return ResponseEntity.ok(bodegaService.getResumenInventarioBodega(id));
    }
}
