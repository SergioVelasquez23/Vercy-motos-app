package com.prog3.security.Controllers;

import com.google.zxing.WriterException;
import com.prog3.security.DTOs.EtiquetaCodigoBarrasDTO;
import com.prog3.security.DTOs.GenerarCodigoBarrasRequest;
import com.prog3.security.DTOs.ImprimirEtiquetasRequest;
import com.prog3.security.Services.CodigoBarrasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/codigos-barras")
@Tag(name = "Códigos de Barras",
        description = "Gestión de códigos de barras para productos e ingredientes")
@CrossOrigin(origins = "*")
public class CodigoBarrasController {

    @Autowired
    private CodigoBarrasService codigoBarrasService;

    /**
     * Genera un código de barras para un producto o ingrediente
     */
    @PostMapping("/generar")
    @Operation(summary = "Generar código de barras",
            description = "Genera un código de barras para un producto o ingrediente. "
                    + "Si no se proporciona código personalizado, usa el ID del item.")
    public ResponseEntity<Map<String, Object>> generarCodigoBarras(
            @RequestBody GenerarCodigoBarrasRequest request) {
        try {
            String codigo = codigoBarrasService.generarCodigoBarras(request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("codigo", codigo);
            response.put("mensaje", "Código de barras generado exitosamente");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "Error al generar código de barras: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Genera la imagen del código de barras
     */
    @GetMapping(value = "/imagen/{codigo}", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(summary = "Obtener imagen de código de barras",
            description = "Genera y retorna la imagen del código de barras en formato PNG")
    public ResponseEntity<byte[]> obtenerImagenCodigoBarras(@PathVariable String codigo,
            @RequestParam(defaultValue = "CODE128") String tipo) {
        try {
            GenerarCodigoBarrasRequest.TipoCodigoBarras tipoCodigo =
                    GenerarCodigoBarrasRequest.TipoCodigoBarras.valueOf(tipo);

            byte[] imagen = codigoBarrasService.generarImagenCodigoBarras(codigo, tipoCodigo);

            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(imagen);
        } catch (WriterException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Genera una etiqueta completa con código de barras
     */
    @GetMapping("/etiqueta/{itemId}/{tipoItem}")
    @Operation(summary = "Generar etiqueta con código de barras",
            description = "Genera una etiqueta completa con código de barras, nombre y precio")
    public ResponseEntity<EtiquetaCodigoBarrasDTO> generarEtiqueta(@PathVariable String itemId,
            @PathVariable String tipoItem,
            @RequestParam(defaultValue = "CODE128") String tipoCodigo) {
        try {
            GenerarCodigoBarrasRequest.TipoCodigoBarras tipo =
                    GenerarCodigoBarrasRequest.TipoCodigoBarras.valueOf(tipoCodigo);

            EtiquetaCodigoBarrasDTO etiqueta =
                    codigoBarrasService.generarEtiqueta(itemId, tipoItem, tipo);

            return ResponseEntity.ok(etiqueta);
        } catch (WriterException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Genera múltiples etiquetas para impresión
     */
    @PostMapping("/imprimir-etiquetas")
    @Operation(summary = "Generar etiquetas para impresión",
            description = "Genera múltiples etiquetas con códigos de barras para impresión en lote")
    public ResponseEntity<List<EtiquetaCodigoBarrasDTO>> imprimirEtiquetas(
            @RequestBody ImprimirEtiquetasRequest request) {
        try {
            List<EtiquetaCodigoBarrasDTO> etiquetas =
                    codigoBarrasService.generarEtiquetasParaImprimir(request);

            return ResponseEntity.ok(etiquetas);
        } catch (WriterException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Busca un producto o ingrediente por código de barras
     */
    @GetMapping("/buscar/{codigo}")
    @Operation(summary = "Buscar por código de barras",
            description = "Busca un producto o ingrediente usando su código de barras o código interno")
    public ResponseEntity<Map<String, Object>> buscarPorCodigoBarras(@PathVariable String codigo) {
        try {
            Object item = codigoBarrasService.buscarPorCodigoBarras(codigo);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("item", item);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "No se encontró el item: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Obtiene información sobre tipos de códigos soportados
     */
    @GetMapping("/tipos")
    @Operation(summary = "Tipos de códigos soportados",
            description = "Lista los tipos de códigos de barras que el sistema puede generar")
    public ResponseEntity<Map<String, Object>> obtenerTiposCodigos() {
        Map<String, Object> response = new HashMap<>();

        Map<String, String> tipos = new HashMap<>();
        tipos.put("EAN13", "Código EAN-13 (13 dígitos) - Estándar internacional para productos");
        tipos.put("EAN8", "Código EAN-8 (8 dígitos) - Para productos pequeños");
        tipos.put("CODE128", "Code 128 - Alfanumérico, alta densidad");
        tipos.put("QR", "Código QR - Almacena más información, escaneable con móviles");

        response.put("success", true);
        response.put("tipos", tipos);
        response.put("recomendacion", "CODE128 para uso general, EAN13 para retail");

        return ResponseEntity.ok(response);
    }
}
