package com.prog3.security.Controllers;

import com.prog3.security.Models.*;
import com.prog3.security.Repositories.*;
import com.prog3.security.Services.BodegaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controlador para gestionar traslados de productos entre bodegas/almacenes. Permite mover
 * productos de un lugar a otro actualizando el stock de cada ubicación.
 */
@RestController
@RequestMapping("/api/inventario/traslados")
@CrossOrigin(originPatterns = {
        "https://vercy-motos.web.app",
        "https://vercy-motos-app.web.app",
        "https://vercy-motos-app-048m.onrender.com",
        "http://localhost:*",
        "http://127.0.0.1:*",
        "http://192.168.*.*:*"
}, allowCredentials = "true")
@Tag(name = "Traslados", description = "Gestión de traslados de productos entre bodegas")
public class TrasladoController {

    @Autowired
    private TrasladoRepository trasladoRepository;

    @Autowired
    private BodegaRepository bodegaRepository;

    @Autowired
    private InventarioBodegaRepository inventarioBodegaRepository;

    @Autowired
    private IngredienteRepository ingredienteRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private BodegaService bodegaService;

    // ====================================
    // 🔧 MÉTODOS AUXILIARES PARA MANEJO DE STOCK DIRECTO
    // ====================================

    /**
     * Actualiza el stock de un producto entre bodega y almacén
     * 
     * @param productoId ID del producto
     * @param origenTipo "BODEGA" o "ALMACEN"
     * @param destinoTipo "BODEGA" o "ALMACEN"
     * @param cantidad Cantidad a trasladar
     * @return true si el traslado fue exitoso
     */
    private boolean trasladarStockProducto(String productoId, String origenTipo, String destinoTipo,
            double cantidad) {
        try {
            Optional<Producto> productoOpt = productoRepository.findById(productoId);
            if (!productoOpt.isPresent()) {
                return false;
            }

            Producto producto = productoOpt.get();

            // Verificar stock disponible en origen
            int stockOrigen = origenTipo.equals("BODEGA") ? producto.getCantidadBodega()
                    : producto.getCantidadAlmacen();

            if (stockOrigen < cantidad) {
                return false; // Stock insuficiente
            }

            // Realizar el traslado
            if (origenTipo.equals("BODEGA")) {
                producto.setCantidadBodega(producto.getCantidadBodega() - (int) cantidad);
            } else {
                producto.setCantidadAlmacen(producto.getCantidadAlmacen() - (int) cantidad);
            }

            if (destinoTipo.equals("BODEGA")) {
                producto.setCantidadBodega(producto.getCantidadBodega() + (int) cantidad);
            } else {
                producto.setCantidadAlmacen(producto.getCantidadAlmacen() + (int) cantidad);
            }

            // Actualizar cantidad total
            producto.setCantidad(producto.getCantidadBodega() + producto.getCantidadAlmacen());

            // Guardar cambios
            productoRepository.save(producto);
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtiene el stock disponible de un producto en una ubicación específica
     * 
     * @param productoId ID del producto
     * @param ubicacion "BODEGA" o "ALMACEN"
     * @return cantidad disponible
     */
    private int obtenerStockProducto(String productoId, String ubicacion) {
        try {
            Optional<Producto> productoOpt = productoRepository.findById(productoId);
            if (!productoOpt.isPresent()) {
                return 0;
            }

            Producto producto = productoOpt.get();
            return ubicacion.equals("BODEGA") ? producto.getCantidadBodega()
                    : producto.getCantidadAlmacen();

        } catch (Exception e) {
            return 0;
        }
    }

    // ====================================
    // 📋 OBTENER TODOS LOS TRASLADOS
    // ====================================
    @GetMapping
    @Operation(summary = "Listar todos los traslados",
            description = "Obtiene el historial completo de traslados")
    public ResponseEntity<Map<String, Object>> obtenerTodosLosTraslados(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String bodegaId) {
        try {
            List<Traslado> traslados;

            if (estado != null && !estado.isEmpty()) {
                traslados = trasladoRepository.findByEstado(estado);
            } else if (bodegaId != null && !bodegaId.isEmpty()) {
                List<Traslado> origen = trasladoRepository.findByOrigenBodegaId(bodegaId);
                List<Traslado> destino = trasladoRepository.findByDestinoBodegaId(bodegaId);
                traslados = new ArrayList<>();
                traslados.addAll(origen);
                traslados.addAll(destino);
            } else {
                traslados = trasladoRepository.findAll();
            }

            // Ordenar por fecha más reciente
            traslados.sort((a, b) -> {
                if (a.getFechaCreacion() == null)
                    return 1;
                if (b.getFechaCreacion() == null)
                    return -1;
                return b.getFechaCreacion().compareTo(a.getFechaCreacion());
            });

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", traslados);
            response.put("total", traslados.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al obtener traslados: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ====================================
    // 🔍 OBTENER TRASLADO POR ID
    // ====================================
    @GetMapping("/{id}")
    @Operation(summary = "Obtener traslado por ID")
    public ResponseEntity<Map<String, Object>> obtenerTrasladoPorId(@PathVariable String id) {
        try {
            Optional<Traslado> trasladoOpt = trasladoRepository.findById(id);

            if (trasladoOpt.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Traslado no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", trasladoOpt.get());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al obtener traslado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ====================================
    // 📝 CREAR NUEVO TRASLADO
    // ====================================
    @PostMapping
    @Operation(summary = "Crear nuevo traslado",
            description = "Crea una solicitud de traslado de un producto entre bodegas. "
                    + "El traslado queda en estado PENDIENTE hasta ser aprobado.")
    public ResponseEntity<Map<String, Object>> crearTraslado(
            @RequestBody Map<String, Object> datos) {
        try {
            System.out.println("📦 Creando traslado con datos: " + datos);

            // Validar datos requeridos
            String productoId = (String) datos.get("productoId");
            String origenBodegaId = (String) datos.get("origenBodegaId");
            String destinoBodegaId = (String) datos.get("destinoBodegaId");
            Object cantidadObj = datos.get("cantidad");

            if (productoId == null || productoId.isEmpty()) {
                throw new IllegalArgumentException("El productoId es requerido");
            }
            if (origenBodegaId == null || origenBodegaId.isEmpty()) {
                throw new IllegalArgumentException("La bodega de origen es requerida");
            }
            if (destinoBodegaId == null || destinoBodegaId.isEmpty()) {
                throw new IllegalArgumentException("La bodega de destino es requerida");
            }
            if (cantidadObj == null) {
                throw new IllegalArgumentException("La cantidad es requerida");
            }

            double cantidad = cantidadObj instanceof Number ? ((Number) cantidadObj).doubleValue()
                    : Double.parseDouble(cantidadObj.toString());

            if (cantidad <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
            }

            if (origenBodegaId.equals(destinoBodegaId)) {
                throw new IllegalArgumentException(
                        "La bodega origen y destino no pueden ser la misma");
            }

            // Validar que las bodegas existen y están activas
            Bodega bodegaOrigen = bodegaRepository.findById(origenBodegaId).orElseThrow(
                    () -> new IllegalArgumentException("Bodega de origen no encontrada"));
            Bodega bodegaDestino = bodegaRepository.findById(destinoBodegaId).orElseThrow(
                    () -> new IllegalArgumentException("Bodega de destino no encontrada"));

            if (!bodegaOrigen.isActiva()) {
                throw new IllegalArgumentException("La bodega de origen no está activa");
            }
            if (!bodegaDestino.isActiva()) {
                throw new IllegalArgumentException("La bodega de destino no está activa");
            }

            // Buscar el producto (puede ser ingrediente o producto)
            String tipoItem = datos.get("tipoItem") != null ? datos.get("tipoItem").toString()
                    : "ingrediente";
            String productoNombre = "";
            String unidad = "UND";

            if ("producto".equalsIgnoreCase(tipoItem)) {
                Optional<Producto> productoOpt = productoRepository.findById(productoId);
                if (productoOpt.isPresent()) {
                    productoNombre = productoOpt.get().getNombre();
                    unidad = productoOpt.get().getUnidadMedida() != null
                            ? productoOpt.get().getUnidadMedida()
                            : "UND";
                }
            } else {
                Optional<Ingrediente> ingredienteOpt = ingredienteRepository.findById(productoId);
                if (ingredienteOpt.isPresent()) {
                    productoNombre = ingredienteOpt.get().getNombre();
                    unidad = ingredienteOpt.get().getUnidad() != null
                            ? ingredienteOpt.get().getUnidad()
                            : "UND";
                }
            }

            // Si se envía el nombre desde el frontend, usarlo
            if (datos.get("productoNombre") != null) {
                productoNombre = datos.get("productoNombre").toString();
            }
            if (datos.get("unidad") != null) {
                unidad = datos.get("unidad").toString();
            }

            // Validar stock disponible en bodega origen
            String tipoOrigenUbicacion =
                    "PRINCIPAL".equals(bodegaOrigen.getTipo()) ? "BODEGA" : "ALMACEN";
            int stockDisponible = obtenerStockProducto(productoId, tipoOrigenUbicacion);

            if (stockDisponible < cantidad) {
                throw new IllegalArgumentException(
                        "Stock insuficiente en " + tipoOrigenUbicacion.toLowerCase()
                                + " origen. Disponible: " + stockDisponible
                                + ", Solicitado: " + cantidad);
            }

            // Generar número de traslado
            String numeroTraslado = generarNumeroTraslado();

            // Crear el traslado
            Traslado traslado = new Traslado();
            traslado.setNumero(numeroTraslado);
            traslado.setProductoId(productoId);
            traslado.setProductoNombre(productoNombre);
            traslado.setOrigenBodegaId(origenBodegaId);
            traslado.setOrigenBodegaNombre(bodegaOrigen.getNombre());
            traslado.setDestinoBodegaId(destinoBodegaId);
            traslado.setDestinoBodegaNombre(bodegaDestino.getNombre());
            traslado.setCantidad(cantidad);
            traslado.setUnidad(unidad);
            traslado.setEstado("PENDIENTE");
            traslado.setSolicitante(
                    datos.get("solicitante") != null ? datos.get("solicitante").toString()
                            : "admin");
            traslado.setObservaciones(
                    datos.get("observaciones") != null ? datos.get("observaciones").toString()
                            : "");
            traslado.setFechaSolicitud(LocalDateTime.now());
            traslado.setFechaCreacion(LocalDateTime.now());
            traslado.setFechaActualizacion(LocalDateTime.now());

            Traslado trasladoGuardado = trasladoRepository.save(traslado);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Traslado creado exitosamente. Pendiente de aprobación.");
            response.put("data", trasladoGuardado);
            response.put("numeroTraslado", numeroTraslado);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            System.err.println("❌ Error al crear traslado: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al crear traslado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ====================================
    // ✅ APROBAR TRASLADO (Ejecuta el movimiento)
    // ====================================
    @PostMapping("/{id}/aprobar")
    @Operation(summary = "Aprobar traslado",
            description = "Aprueba el traslado y ejecuta el movimiento de stock entre bodegas")
    public ResponseEntity<Map<String, Object>> aprobarTraslado(@PathVariable String id,
            @RequestBody(required = false) Map<String, Object> datos) {
        try {
            Optional<Traslado> trasladoOpt = trasladoRepository.findById(id);

            if (trasladoOpt.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Traslado no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Traslado traslado = trasladoOpt.get();

            if (!"PENDIENTE".equals(traslado.getEstado())) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message",
                        "Solo se pueden aprobar traslados en estado PENDIENTE. Estado actual: "
                                + traslado.getEstado());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            // Validar y obtener bodegas
            Optional<Bodega> bodegaOrigenOpt =
                    bodegaRepository.findById(traslado.getOrigenBodegaId());
            Optional<Bodega> bodegaDestinoOpt =
                    bodegaRepository.findById(traslado.getDestinoBodegaId());

            if (!bodegaOrigenOpt.isPresent() || !bodegaDestinoOpt.isPresent()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Una o ambas bodegas no encontradas");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            Bodega bodegaOrigen = bodegaOrigenOpt.get();
            Bodega bodegaDestino = bodegaDestinoOpt.get();

            // Determinar tipos de ubicación según tipo de bodega
            String tipoOrigenUbicacion =
                    "PRINCIPAL".equals(bodegaOrigen.getTipo()) ? "BODEGA" : "ALMACEN";
            String tipoDestinoUbicacion =
                    "PRINCIPAL".equals(bodegaDestino.getTipo()) ? "BODEGA" : "ALMACEN";

            // Validar nuevamente el stock disponible
            int stockDisponible =
                    obtenerStockProducto(traslado.getProductoId(), tipoOrigenUbicacion);

            if (stockDisponible < traslado.getCantidad()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message",
                        "Stock insuficiente en " + tipoOrigenUbicacion.toLowerCase()
                                + ". Disponible: " + stockDisponible + ", Requerido: "
                                + traslado.getCantidad());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            // Ejecutar el movimiento de stock directamente en el producto
            boolean trasladorExitoso = trasladarStockProducto(traslado.getProductoId(),
                    tipoOrigenUbicacion, tipoDestinoUbicacion, traslado.getCantidad());

            if (!trasladorExitoso) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Error al ejecutar el traslado de stock");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
            }

            // Actualizar estado del traslado
            traslado.setEstado("COMPLETADO");
            traslado.setAprobador(datos != null && datos.get("aprobador") != null
                    ? datos.get("aprobador").toString()
                    : "admin");
            traslado.setFechaAprobacion(LocalDateTime.now());
            traslado.setFechaCompletado(LocalDateTime.now());
            traslado.setFechaActualizacion(LocalDateTime.now());

            Traslado trasladoActualizado = trasladoRepository.save(traslado);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Traslado aprobado y ejecutado exitosamente");
            response.put("data", trasladoActualizado);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error al aprobar traslado: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al aprobar traslado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ====================================
    // ❌ RECHAZAR TRASLADO
    // ====================================
    @PostMapping("/{id}/rechazar")
    @Operation(summary = "Rechazar traslado",
            description = "Rechaza el traslado. No se ejecuta ningún movimiento de stock.")
    public ResponseEntity<Map<String, Object>> rechazarTraslado(@PathVariable String id,
            @RequestBody Map<String, Object> datos) {
        try {
            Optional<Traslado> trasladoOpt = trasladoRepository.findById(id);

            if (trasladoOpt.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Traslado no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Traslado traslado = trasladoOpt.get();

            if (!"PENDIENTE".equals(traslado.getEstado())) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Solo se pueden rechazar traslados en estado PENDIENTE");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            traslado.setEstado("RECHAZADO");
            traslado.setAprobador(
                    datos.get("aprobador") != null ? datos.get("aprobador").toString() : "admin");
            traslado.setObservaciones(traslado.getObservaciones() + "\n[RECHAZADO] "
                    + (datos.get("motivo") != null ? datos.get("motivo").toString()
                            : "Sin motivo"));
            traslado.setFechaActualizacion(LocalDateTime.now());

            Traslado trasladoActualizado = trasladoRepository.save(traslado);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Traslado rechazado");
            response.put("data", trasladoActualizado);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al rechazar traslado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ====================================
    // � PROCESAR TRASLADO (Compatible con Flutter)
    // ====================================
    @PutMapping("/procesar")
    @Operation(summary = "Procesar traslado",
            description = "Procesa un traslado: ACEPTAR ejecuta el movimiento, RECHAZAR lo cancela")
    public ResponseEntity<Map<String, Object>> procesarTraslado(
            @RequestBody Map<String, Object> datos) {
        try {
            String trasladoId = (String) datos.get("trasladoId");
            String accion = (String) datos.get("accion");
            String aprobador = (String) datos.get("aprobador");
            String observaciones = (String) datos.get("observaciones");

            if (trasladoId == null || trasladoId.isEmpty()) {
                throw new IllegalArgumentException("El trasladoId es requerido");
            }
            if (accion == null || accion.isEmpty()) {
                throw new IllegalArgumentException("La acción es requerida (ACEPTAR o RECHAZAR)");
            }

            Optional<Traslado> trasladoOpt = trasladoRepository.findById(trasladoId);

            if (trasladoOpt.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Traslado no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Traslado traslado = trasladoOpt.get();

            if (!"PENDIENTE".equals(traslado.getEstado())) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message",
                        "Solo se pueden procesar traslados en estado PENDIENTE. Estado actual: "
                                + traslado.getEstado());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            if ("ACEPTAR".equalsIgnoreCase(accion)) {
                // Validar stock disponible
                Optional<InventarioBodega> inventarioOrigenOpt =
                        inventarioBodegaRepository.findByBodegaIdAndItemId(
                                traslado.getOrigenBodegaId(), traslado.getProductoId());
                double stockDisponible =
                        inventarioOrigenOpt.map(InventarioBodega::getStockActual).orElse(0.0);

                if (stockDisponible < traslado.getCantidad()) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "Stock insuficiente. Disponible: " + stockDisponible
                            + ", Requerido: " + traslado.getCantidad());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                }

                // Determinar tipo de item
                String tipoItem = "ingrediente";
                if (productoRepository.existsById(traslado.getProductoId())) {
                    tipoItem = "producto";
                }

                // Ejecutar movimiento de stock
                bodegaService.ajustarStockBodega(traslado.getOrigenBodegaId(),
                        traslado.getProductoId(), tipoItem, -traslado.getCantidad(),
                        "Traslado " + traslado.getNumero() + " - Salida hacia "
                                + traslado.getDestinoBodegaNombre());

                bodegaService.ajustarStockBodega(traslado.getDestinoBodegaId(),
                        traslado.getProductoId(), tipoItem, traslado.getCantidad(),
                        "Traslado " + traslado.getNumero() + " - Entrada desde "
                                + traslado.getOrigenBodegaNombre());

                traslado.setEstado("ACEPTADO");
                traslado.setFechaAprobacion(LocalDateTime.now());
                traslado.setFechaCompletado(LocalDateTime.now());

            } else if ("RECHAZAR".equalsIgnoreCase(accion)) {
                traslado.setEstado("RECHAZADO");
                if (observaciones != null && !observaciones.isEmpty()) {
                    String obsActual =
                            traslado.getObservaciones() != null ? traslado.getObservaciones() : "";
                    traslado.setObservaciones(obsActual + "\n[RECHAZADO] " + observaciones);
                }
            } else {
                throw new IllegalArgumentException("Acción no válida. Use ACEPTAR o RECHAZAR");
            }

            traslado.setAprobador(aprobador != null ? aprobador : "admin");
            traslado.setFechaActualizacion(LocalDateTime.now());

            Traslado trasladoActualizado = trasladoRepository.save(traslado);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Traslado " + accion.toLowerCase() + " exitosamente");
            response.put("data", trasladoActualizado);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            System.err.println("❌ Error al procesar traslado: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al procesar traslado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ====================================
    // �🚀 CREAR Y APROBAR EN UN SOLO PASO (Traslado directo)
    // ====================================
    @PostMapping("/directo")
    @Operation(summary = "Traslado directo",
            description = "Crea y ejecuta un traslado inmediatamente sin necesidad de aprobación")
    public ResponseEntity<Map<String, Object>> trasladoDirecto(
            @RequestBody Map<String, Object> datos) {
        try {
            System.out.println("🚀 Ejecutando traslado directo: " + datos);

            // Validar datos
            String productoId = (String) datos.get("productoId");
            String origenBodegaId = (String) datos.get("origenBodegaId");
            String destinoBodegaId = (String) datos.get("destinoBodegaId");
            Object cantidadObj = datos.get("cantidad");

            if (productoId == null || origenBodegaId == null || destinoBodegaId == null
                    || cantidadObj == null) {
                throw new IllegalArgumentException(
                        "Faltan datos requeridos: productoId, origenBodegaId, destinoBodegaId, cantidad");
            }

            double cantidad = cantidadObj instanceof Number ? ((Number) cantidadObj).doubleValue()
                    : Double.parseDouble(cantidadObj.toString());

            if (cantidad <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
            }

            if (origenBodegaId.equals(destinoBodegaId)) {
                throw new IllegalArgumentException(
                        "La bodega origen y destino no pueden ser la misma");
            }

            // Validar bodegas
            Bodega bodegaOrigen = bodegaRepository.findById(origenBodegaId).orElseThrow(
                    () -> new IllegalArgumentException("Bodega de origen no encontrada"));
            Bodega bodegaDestino = bodegaRepository.findById(destinoBodegaId).orElseThrow(
                    () -> new IllegalArgumentException("Bodega de destino no encontrada"));

            // Obtener información del producto
            String tipoItem = datos.get("tipoItem") != null ? datos.get("tipoItem").toString()
                    : "ingrediente";
            String productoNombre =
                    datos.get("productoNombre") != null ? datos.get("productoNombre").toString()
                            : "";
            String unidad = datos.get("unidad") != null ? datos.get("unidad").toString() : "UND";

            if (productoNombre.isEmpty()) {
                if ("producto".equalsIgnoreCase(tipoItem)) {
                    productoRepository.findById(productoId).ifPresent(p -> {
                    });
                } else {
                    ingredienteRepository.findById(productoId).ifPresent(i -> {
                    });
                }
            }

            // Validar stock
            Optional<InventarioBodega> inventarioOrigenOpt =
                    inventarioBodegaRepository.findByBodegaIdAndItemId(origenBodegaId, productoId);
            double stockDisponible =
                    inventarioOrigenOpt.map(InventarioBodega::getStockActual).orElse(0.0);

            if (stockDisponible < cantidad) {
                throw new IllegalArgumentException("Stock insuficiente. Disponible: "
                        + stockDisponible + ", Solicitado: " + cantidad);
            }

            // Generar número
            String numeroTraslado = generarNumeroTraslado();

            // Ejecutar movimientos
            // 1. Descontar de origen
            bodegaService.ajustarStockBodega(origenBodegaId, productoId, tipoItem, -cantidad,
                    "Traslado directo " + numeroTraslado + " hacia " + bodegaDestino.getNombre());

            // 2. Agregar a destino
            bodegaService.ajustarStockBodega(destinoBodegaId, productoId, tipoItem, cantidad,
                    "Traslado directo " + numeroTraslado + " desde " + bodegaOrigen.getNombre());

            // Crear registro del traslado
            Traslado traslado = new Traslado();
            traslado.setNumero(numeroTraslado);
            traslado.setProductoId(productoId);
            traslado.setProductoNombre(productoNombre);
            traslado.setOrigenBodegaId(origenBodegaId);
            traslado.setOrigenBodegaNombre(bodegaOrigen.getNombre());
            traslado.setDestinoBodegaId(destinoBodegaId);
            traslado.setDestinoBodegaNombre(bodegaDestino.getNombre());
            traslado.setCantidad(cantidad);
            traslado.setUnidad(unidad);
            traslado.setEstado("COMPLETADO");
            traslado.setSolicitante(
                    datos.get("solicitante") != null ? datos.get("solicitante").toString()
                            : "admin");
            traslado.setAprobador(
                    datos.get("solicitante") != null ? datos.get("solicitante").toString()
                            : "admin");
            traslado.setObservaciones(
                    datos.get("observaciones") != null ? datos.get("observaciones").toString()
                            : "Traslado directo");
            traslado.setFechaSolicitud(LocalDateTime.now());
            traslado.setFechaAprobacion(LocalDateTime.now());
            traslado.setFechaCompletado(LocalDateTime.now());
            traslado.setFechaCreacion(LocalDateTime.now());
            traslado.setFechaActualizacion(LocalDateTime.now());

            Traslado trasladoGuardado = trasladoRepository.save(traslado);

            // Obtener stocks actualizados
            double nuevoStockOrigen =
                    inventarioBodegaRepository.findByBodegaIdAndItemId(origenBodegaId, productoId)
                            .map(InventarioBodega::getStockActual).orElse(0.0);
            double nuevoStockDestino =
                    inventarioBodegaRepository.findByBodegaIdAndItemId(destinoBodegaId, productoId)
                            .map(InventarioBodega::getStockActual).orElse(0.0);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Traslado ejecutado exitosamente");
            response.put("data", trasladoGuardado);
            response.put("stockOrigen", nuevoStockOrigen);
            response.put("stockDestino", nuevoStockDestino);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            System.err.println("❌ Error en traslado directo: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error en traslado directo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ====================================
    // 📊 STOCK DE UN PRODUCTO EN TODAS LAS BODEGAS
    // ====================================
    @GetMapping("/stock/{productoId}")
    @Operation(summary = "Stock por bodega",
            description = "Muestra el stock de un producto en cada bodega")
    public ResponseEntity<Map<String, Object>> getStockPorBodega(@PathVariable String productoId) {
        try {
            List<InventarioBodega> inventarios =
                    inventarioBodegaRepository.findByItemId(productoId);

            List<Map<String, Object>> stockPorBodega = new ArrayList<>();
            double stockTotal = 0;

            for (InventarioBodega inv : inventarios) {
                Map<String, Object> item = new HashMap<>();
                item.put("bodegaId", inv.getBodegaId());
                item.put("stockActual", inv.getStockActual());
                item.put("stockMinimo", inv.getStockMinimo());
                item.put("ubicacionFisica", inv.getUbicacionFisica());

                bodegaRepository.findById(inv.getBodegaId()).ifPresent(b -> {
                    item.put("bodegaNombre", b.getNombre());
                    item.put("bodegaTipo", b.getTipo());
                });

                stockPorBodega.add(item);
                stockTotal += inv.getStockActual();
            }

            // Obtener nombre del producto
            String productoNombre = "";
            Optional<Ingrediente> ingredienteOpt = ingredienteRepository.findById(productoId);
            if (ingredienteOpt.isPresent()) {
                productoNombre = ingredienteOpt.get().getNombre();
            } else {
                Optional<Producto> productoOpt = productoRepository.findById(productoId);
                if (productoOpt.isPresent()) {
                    productoNombre = productoOpt.get().getNombre();
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("productoId", productoId);
            response.put("productoNombre", productoNombre);
            response.put("stockTotal", stockTotal);
            response.put("bodegas", stockPorBodega);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al obtener stock: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ====================================
    // 📈 HISTORIAL DE TRASLADOS POR PRODUCTO
    // ====================================
    @GetMapping("/historial/{productoId}")
    @Operation(summary = "Historial de traslados de un producto")
    public ResponseEntity<Map<String, Object>> getHistorialPorProducto(
            @PathVariable String productoId) {
        try {
            List<Traslado> traslados = trasladoRepository.findByProductoId(productoId);

            traslados.sort((a, b) -> {
                if (a.getFechaCreacion() == null)
                    return 1;
                if (b.getFechaCreacion() == null)
                    return -1;
                return b.getFechaCreacion().compareTo(a.getFechaCreacion());
            });

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("productoId", productoId);
            response.put("data", traslados);
            response.put("total", traslados.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al obtener historial: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ====================================
    // 🗑️ ELIMINAR TRASLADO (Solo pendientes)
    // ====================================
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar traslado",
            description = "Elimina un traslado que esté en estado PENDIENTE")
    public ResponseEntity<Map<String, Object>> eliminarTraslado(@PathVariable String id) {
        try {
            Optional<Traslado> trasladoOpt = trasladoRepository.findById(id);

            if (trasladoOpt.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Traslado no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Traslado traslado = trasladoOpt.get();

            if (!"PENDIENTE".equals(traslado.getEstado())) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Solo se pueden eliminar traslados en estado PENDIENTE");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            trasladoRepository.deleteById(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Traslado eliminado exitosamente");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al eliminar traslado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ====================================
    // 📊 RESUMEN DE TRASLADOS
    // ====================================
    @GetMapping("/resumen")
    @Operation(summary = "Resumen de traslados",
            description = "Estadísticas generales de traslados")
    public ResponseEntity<Map<String, Object>> getResumenTraslados() {
        try {
            List<Traslado> todos = trasladoRepository.findAll();

            long pendientes = todos.stream().filter(t -> "PENDIENTE".equals(t.getEstado())).count();
            long completados =
                    todos.stream().filter(t -> "COMPLETADO".equals(t.getEstado())).count();
            long rechazados = todos.stream().filter(t -> "RECHAZADO".equals(t.getEstado())).count();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("total", todos.size());
            response.put("pendientes", pendientes);
            response.put("completados", completados);
            response.put("rechazados", rechazados);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al obtener resumen: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ====================================
    // 🔧 MÉTODOS AUXILIARES
    // ====================================

    /**
     * Genera un número único para el traslado
     */
    private String generarNumeroTraslado() {
        Traslado ultimo = trasladoRepository.findTopByOrderByFechaCreacionDesc();
        int siguienteNumero = 1;

        if (ultimo != null && ultimo.getNumero() != null) {
            try {
                String numeroStr = ultimo.getNumero().replace("T", "");
                siguienteNumero = Integer.parseInt(numeroStr) + 1;
            } catch (NumberFormatException e) {
                siguienteNumero = (int) (System.currentTimeMillis() % 10000);
            }
        }

        return "T" + String.format("%04d", siguienteNumero);
    }

    // ====================================
    // 📊 NUEVOS ENDPOINTS PARA STOCK DIRECTO
    // ====================================

    /**
     * Obtener stock de un producto específico en bodega y almacén
     */
    @GetMapping("/producto/{productoId}/stock")
    @Operation(summary = "Stock de producto en bodega y almacén",
            description = "Consulta el stock actual de un producto en bodega y almacén")
    public ResponseEntity<Map<String, Object>> getStockProducto(@PathVariable String productoId) {
        try {
            Optional<Producto> productoOpt = productoRepository.findById(productoId);

            if (!productoOpt.isPresent()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Producto no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Producto producto = productoOpt.get();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("productoId", productoId);
            response.put("productoNombre", producto.getNombre());
            response.put("cantidadBodega", producto.getCantidadBodega());
            response.put("cantidadAlmacen", producto.getCantidadAlmacen());
            response.put("cantidadTotal", producto.getCantidad());
            response.put("unidadMedida", producto.getUnidadMedida());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al obtener stock: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Traslado rápido entre bodega y almacén (sin aprobación)
     */
    @PostMapping("/traslado-rapido")
    @Operation(summary = "Traslado rápido bodega-almacén",
            description = "Realiza un traslado inmediato entre bodega y almacén sin necesidad de aprobación")
    public ResponseEntity<Map<String, Object>> trasladoRapido(
            @RequestBody Map<String, Object> datos) {
        try {
            String productoId = (String) datos.get("productoId");
            String origen = (String) datos.get("origen"); // "BODEGA" o "ALMACEN"
            String destino = (String) datos.get("destino"); // "BODEGA" o "ALMACEN"
            Object cantidadObj = datos.get("cantidad");

            // Validaciones
            if (productoId == null || productoId.isEmpty()) {
                throw new IllegalArgumentException("El productoId es requerido");
            }
            if (origen == null || (!origen.equals("BODEGA") && !origen.equals("ALMACEN"))) {
                throw new IllegalArgumentException("Origen debe ser 'BODEGA' o 'ALMACEN'");
            }
            if (destino == null || (!destino.equals("BODEGA") && !destino.equals("ALMACEN"))) {
                throw new IllegalArgumentException("Destino debe ser 'BODEGA' o 'ALMACEN'");
            }
            if (origen.equals(destino)) {
                throw new IllegalArgumentException("Origen y destino no pueden ser iguales");
            }
            if (cantidadObj == null) {
                throw new IllegalArgumentException("La cantidad es requerida");
            }

            double cantidad = cantidadObj instanceof Number ? ((Number) cantidadObj).doubleValue()
                    : Double.parseDouble(cantidadObj.toString());

            if (cantidad <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
            }

            // Verificar que el producto existe
            Optional<Producto> productoOpt = productoRepository.findById(productoId);
            if (!productoOpt.isPresent()) {
                throw new IllegalArgumentException("Producto no encontrado");
            }

            // Ejecutar el traslado
            boolean trasladoExitoso = trasladarStockProducto(productoId, origen, destino, cantidad);

            if (!trasladoExitoso) {
                throw new RuntimeException(
                        "Error al ejecutar el traslado (posiblemente stock insuficiente)");
            }

            // Obtener el producto actualizado
            Producto productoActualizado = productoRepository.findById(productoId).get();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", String.format("Traslado exitoso: %s → %s (%.0f unidades)",
                    origen.toLowerCase(), destino.toLowerCase(), cantidad));
            response.put("productoId", productoId);
            response.put("productoNombre", productoActualizado.getNombre());
            response.put("cantidadBodega", productoActualizado.getCantidadBodega());
            response.put("cantidadAlmacen", productoActualizado.getCantidadAlmacen());
            response.put("cantidadTotal", productoActualizado.getCantidad());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al realizar traslado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
