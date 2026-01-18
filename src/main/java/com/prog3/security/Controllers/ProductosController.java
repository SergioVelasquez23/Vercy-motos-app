package com.prog3.security.Controllers;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.prog3.security.Models.Producto;
import com.prog3.security.Models.Ingrediente;
import com.prog3.security.Models.IngredienteProducto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.prog3.security.DTOs.CrearProductoRequest;
import com.prog3.security.DTOs.IngredienteConCategoriaDTO;
import com.prog3.security.Repositories.ProductoRepository;
import com.prog3.security.Repositories.IngredienteRepository;
import com.prog3.security.Utils.ApiResponse;

@CrossOrigin
@RestController
@RequestMapping("api/productos")
public class ProductosController extends BaseController<Producto, String> {
    @Autowired
    private ProductoRepository theProductoRepository;

    @Autowired
    private IngredienteRepository theIngredienteRepository;

    @Autowired
    private com.prog3.security.Services.CacheOptimizationService cacheOptimizationService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected MongoRepository<Producto, String> getRepository() {
        return theProductoRepository;
    }
    
    @Override
    protected String getEntityName() {
        return "Producto";
    }
    
    @Override
    protected ResponseEntity<ApiResponse<Producto>> validateEntity(Producto entity, boolean isUpdate) {
        if (entity.getNombre() == null || entity.getNombre().trim().isEmpty()) {
            return responseService.badRequest("El nombre es obligatorio");
        }
        if (entity.getPrecio() <= 0) {
            return responseService.badRequest("El precio debe ser mayor a 0");
        }
        return responseService.success(entity, "Validación exitosa");
    }
    
    @Override
    protected void updateEntityFields(Producto existing, Producto updated) {
        // Información básica
        existing.setCodigo(updated.getCodigo());
        existing.setNombre(updated.getNombre());
        existing.setDescripcion(updated.getDescripcion());
        existing.setUnidadMedida(updated.getUnidadMedida());
        existing.setCodigoBarras(updated.getCodigoBarras());
        existing.setCodigoInterno(updated.getCodigoInterno());

        // Precios
        existing.setPrecio(updated.getPrecio());
        existing.setCosto(updated.getCosto());
        existing.setImpuestos(updated.getImpuestos());
        existing.setPrecioConIva(updated.getPrecioConIva());
        existing.setUtilidad(updated.getUtilidad());
        existing.setPreciosOpcionales(updated.getPreciosOpcionales());

        // Clasificación
        existing.setTipoItem(updated.getTipoItem());
        existing.setCategoriaId(updated.getCategoriaId());
        existing.setTipoProductoNombre(updated.getTipoProductoNombre());
        existing.setLineaProducto(updated.getLineaProducto());
        existing.setClaseProducto(updated.getClaseProducto());
        existing.setMarca(updated.getMarca());

        // Inventario
        existing.setControlInventario(updated.isControlInventario());
        existing.setCantidad(updated.getCantidad());
        existing.setStockMinimo(updated.getStockMinimo());
        existing.setStockOptimo(updated.getStockOptimo());
        existing.setCantidadAlmacen(updated.getCantidadAlmacen());
        existing.setCantidadBodega(updated.getCantidadBodega());

        // Ubicación
        existing.setLocalizacion(updated.getLocalizacion());
        existing.setUbicacion3(updated.getUbicacion3());
        existing.setUbicacion4(updated.getUbicacion4());
        existing.setLocalizacionUbi1(updated.getLocalizacionUbi1());
        existing.setLocalizacionUbi2(updated.getLocalizacionUbi2());
        existing.setLocalizacionUbi3(updated.getLocalizacionUbi3());
        existing.setLocalizacionUbi4(updated.getLocalizacionUbi4());

        // Proveedor
        existing.setProveedorNombre(updated.getProveedorNombre());
        existing.setProveedorNit(updated.getProveedorNit());

        // Otros
        existing.setEstado(updated.getEstado());
        existing.setImagenUrl(updated.getImagenUrl());
        existing.setNota(updated.getNota());
        existing.setTieneVariantes(updated.isTieneVariantes());

        // Ingredientes
        existing.setIngredientesDisponibles(updated.getIngredientesDisponibles());
        existing.setTieneIngredientes(updated.isTieneIngredientes());
        existing.setTipoProducto(updated.getTipoProducto());
        existing.setIngredientesRequeridos(updated.getIngredientesRequeridos());
        existing.setIngredientesOpcionales(updated.getIngredientesOpcionales());
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> find() {
        try {
            System.out.println("⚡ ENDPOINT ULTRA-RÁPIDO /api/productos - CON CACHÉ + PROYECCIÓN");
            long startTime = System.currentTimeMillis();

            // OPTIMIZACIÓN 1: Usar CACHÉ de productos activos (5 min TTL)
            List<Producto> productos = this.cacheOptimizationService.getProductosActivosCached();

            // OPTIMIZACIÓN 2: Proyección ligera - solo campos esenciales
            List<Map<String, Object>> productosLigeros = productos.stream().map(p -> {
                Map<String, Object> ligero = new HashMap<>();
                ligero.put("_id", p.get_id());
                ligero.put("nombre", p.getNombre());
                ligero.put("precio", p.getPrecio());
                ligero.put("imagenUrl", p.getImagenUrl());
                ligero.put("categoriaId", p.getCategoriaId());
                ligero.put("estado", p.getEstado());
                ligero.put("tieneIngredientes", p.isTieneIngredientes());
                ligero.put("tipoProducto", p.getTipoProducto());
                // NO incluir ingredientes (son pesados)
                return ligero;
            }).toList();

            long endTime = System.currentTimeMillis();
            System.out.println("⚡ Completado en: " + (endTime - startTime) + "ms (CACHÉ activo)");
            System.out.println("📦 Productos ligeros: " + productosLigeros.size());

            return responseService.success(productosLigeros, "Productos obtenidos exitosamente");
        } catch (Exception e) {
            System.err.println("❌ ERROR en /api/productos: " + e.getMessage());
            e.printStackTrace();
            return responseService.internalError("Error al obtener productos: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Producto>> findById(@PathVariable String id) {
        try {
            Producto producto = this.theProductoRepository.findById(id).orElse(null);
            if (producto == null) {
                return responseService.notFound("Producto no encontrado con ID: " + id);
            }
            return responseService.success(producto, "Producto encontrado exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al buscar producto: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Producto>> create(@RequestBody CrearProductoRequest request) {
        try {
            // Validar que no exista un producto con el mismo nombre
            if (this.theProductoRepository.existsByNombre(request.getNombre())) {
                return responseService.conflict("Ya existe un producto con el nombre: " + request.getNombre());
            }

            // Validaciones de negocio
            if (request.getPrecio() <= 0) {
                return responseService.badRequest("El precio debe ser mayor a 0");
            }

            if (request.getCosto() < 0) {
                return responseService.badRequest("El costo no puede ser negativo");
            }

            // Validar que los ingredientes existan si se especificaron
            if (request.getIngredientesRequeridos() != null) {
                for (IngredienteProducto ingredienteReq : request.getIngredientesRequeridos()) {
                    if (!theIngredienteRepository.existsById(ingredienteReq.getIngredienteId())) {
                        return responseService.badRequest("El ingrediente requerido con ID " + ingredienteReq.getIngredienteId() + " no existe");
                    }

                    // Resolver el nombre si no está presente
                    if (ingredienteReq.getNombre() == null || ingredienteReq.getNombre().isEmpty()) {
                        Ingrediente ingrediente = this.theIngredienteRepository.findById(ingredienteReq.getIngredienteId()).orElse(null);
                        if (ingrediente != null) {
                            ingredienteReq.setNombre(ingrediente.getNombre());
                        }
                    }
                }
            }

            if (request.getIngredientesOpcionales() != null) {
                for (IngredienteProducto ingredienteOpc : request.getIngredientesOpcionales()) {
                    if (!theIngredienteRepository.existsById(ingredienteOpc.getIngredienteId())) {
                        return responseService.badRequest("El ingrediente opcional con ID " + ingredienteOpc.getIngredienteId() + " no existe");
                    }

                    // Resolver el nombre si no está presente
                    if (ingredienteOpc.getNombre() == null || ingredienteOpc.getNombre().isEmpty()) {
                        Ingrediente ingrediente = this.theIngredienteRepository.findById(ingredienteOpc.getIngredienteId()).orElse(null);
                        if (ingrediente != null) {
                            ingredienteOpc.setNombre(ingrediente.getNombre());
                        }
                    }
                }
            }

            // Crear el producto
            Producto newProducto = new Producto();
            newProducto.setNombre(request.getNombre());
            newProducto.setPrecio(request.getPrecio());
            newProducto.setCosto(request.getCosto());
            newProducto.setImpuestos(request.getImpuestos());
            newProducto.setTieneVariantes(request.isTieneVariantes());
            newProducto.setEstado(request.getEstado());
            newProducto.setImagenUrl(request.getImagenUrl());
            newProducto.setCategoriaId(request.getCategoriaId());
            newProducto.setDescripcion(request.getDescripcion());
            newProducto.setCantidad(request.getCantidad());
            newProducto.setNota(request.getNota());
            newProducto.setIngredientesDisponibles(request.getIngredientesDisponibles());

            // Campos nuevos para combo/individual
            newProducto.setTieneIngredientes(request.isTieneIngredientes());
            newProducto.setTipoProducto(request.getTipoProducto());
            newProducto.setIngredientesRequeridos(request.getIngredientesRequeridos());
            newProducto.setIngredientesOpcionales(request.getIngredientesOpcionales());

            // Calcular utilidad si no se especifica
            if (request.getUtilidad() == 0.0) {
                double utilidad = request.getPrecio() - request.getCosto() - request.getImpuestos();
                newProducto.setUtilidad(utilidad);
            } else {
                newProducto.setUtilidad(request.getUtilidad());
            }

            Producto savedProducto = this.theProductoRepository.save(newProducto);
            return responseService.created(savedProducto, "Producto creado exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al crear producto: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        try {
            Producto producto = this.theProductoRepository.findById(id).orElse(null);
            if (producto == null) {
                return responseService.notFound("Producto no encontrado con ID: " + id);
            }
            this.theProductoRepository.deleteById(id);
            return responseService.success(null, "Producto eliminado exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al eliminar producto: " + e.getMessage());
        }
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<ApiResponse<List<Producto>>> findByCategoria(@PathVariable String categoriaId) {
        try {
            System.out.println("🏷️ ENDPOINT /categoria/" + categoriaId + " - Llamado desde frontend");
            long startTime = System.currentTimeMillis();
            
            List<Producto> productos = this.theProductoRepository.findByCategoriaId(categoriaId);
            
            long endTime = System.currentTimeMillis();
            System.out.println("⚡ ENDPOINT /categoria/" + categoriaId + " - Completado en: " + (endTime - startTime) + "ms");
            System.out.println("📦 Productos encontrados: " + productos.size());
            
            return responseService.success(productos, "Productos por categoría obtenidos");
        } catch (Exception e) {
            System.err.println("❌ ERROR en /categoria/" + categoriaId + ": " + e.getMessage());
            return responseService.internalError("Error al buscar productos por categoría: " + e.getMessage());
        }
    }

    /**
     * Endpoint ULTRA RÁPIDO: Productos ligeros por categoría
     * Solo devuelve datos esenciales para carga rápida
     */
    @GetMapping("/categoria/{categoriaId}/ligero")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getProductosPorCategoriaLigero(@PathVariable String categoriaId) {
        try {
            System.out.println("🚀 ENDPOINT /categoria/" + categoriaId + "/ligero - ULTRA RÁPIDO");
            long startTime = System.currentTimeMillis();
            
            List<Producto> productos = this.theProductoRepository.findByCategoriaId(categoriaId);
            
            // Convertir a formato ligero (solo datos esenciales)
            List<Map<String, Object>> productosLigeros = new ArrayList<>();
            for (Producto p : productos) {
                if ("Activo".equals(p.getEstado())) { // Solo productos activos
                    Map<String, Object> productoLigero = new HashMap<>();
                    productoLigero.put("_id", p.get_id());
                    productoLigero.put("nombre", p.getNombre());
                    productoLigero.put("precio", p.getPrecio());
                    productoLigero.put("imagenUrl", p.getImagenUrl());
                    productoLigero.put("tieneIngredientes", p.isTieneIngredientes());
                    productoLigero.put("tipoProducto", p.getTipoProducto());
                    productoLigero.put("estado", p.getEstado());
                    productosLigeros.add(productoLigero);
                }
            }
            
            long endTime = System.currentTimeMillis();
            System.out.println("⚡ ULTRA RÁPIDO completado en: " + (endTime - startTime) + "ms");
            System.out.println("📦 Productos ligeros (solo activos): " + productosLigeros.size() + "/" + productos.size());
            
            return responseService.success(productosLigeros, 
                "Productos ligeros por categoría: " + productosLigeros.size() + " activos");
        } catch (Exception e) {
            System.err.println("❌ ERROR en /categoria/" + categoriaId + "/ligero: " + e.getMessage());
            return responseService.internalError("Error al obtener productos ligeros: " + e.getMessage());
        }
    }

    /**
     * Endpoint SÚPER OPTIMIZADO: Resumen de productos agrupados por categoría Devuelve solo datos
     * básicos de todos los productos organizados por categoría IDEAL para carga inicial del
     * frontend
     */
    /**
     * Endpoint ULTRA RÁPIDO sin imágenes - Solo datos esenciales Paginado para carga progresiva
     */
    @GetMapping("/ligero")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProductosLigeros(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "40") int size) {
        try {
            System.out.println("⚡ /ligero - SIN IMÁGENES (ultra rápido)");
            long startTime = System.currentTimeMillis();

            // Aggregation sin campo imagenUrl (el campo más pesado)
            Aggregation aggregation = Aggregation.newAggregation(
                    Aggregation.match(Criteria.where("estado").regex("^activo$", "i")),
                    Aggregation.project("_id", "nombre", "precio", "categoriaId",
                            "tieneIngredientes", "tipoProducto", "estado"));

            List<Producto> productos = mongoTemplate
                    .aggregate(aggregation, "producto", Producto.class).getMappedResults();

            // Paginación
            int totalElements = productos.size();
            int start = page * size;
            int end = Math.min(start + size, totalElements);

            List<Producto> paginaActual =
                    start < totalElements ? productos.subList(start, end) : List.of();

            Map<String, Object> result = new HashMap<>();
            result.put("content", paginaActual);
            result.put("page", page);
            result.put("size", size);
            result.put("totalPages", (int) Math.ceil((double) totalElements / size));
            result.put("totalElements", totalElements);

            long endTime = System.currentTimeMillis();
            System.out.println("✅ /ligero completado en: " + (endTime - startTime) + "ms");
            System.out.println(
                    "📦 Productos sin imágenes: " + paginaActual.size() + "/" + totalElements);

            return responseService.success(result, "Productos ligeros (sin imágenes)");
        } catch (Exception e) {
            System.err.println("❌ ERROR en /ligero: " + e.getMessage());
            e.printStackTrace();
            return responseService.internalError("Error: " + e.getMessage());
        }
    }

    /**
     * Endpoint para cargar SOLO las imágenes de productos específicos Uso: POST
     * /api/productos/imagenes Body: ["producto_id_1", "producto_id_2", ...]
     * 
     * OPTIMIZACIÓN: Retorna solo IDs, el frontend cargará las imágenes bajo demanda usando GET
     * /api/productos/{id}/imagen individual
     */
    @PostMapping("/imagenes")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getImagenesProductos(
            @RequestBody List<String> productosIds) {
        try {
            System.out.println("🖼️ Verificando " + productosIds.size() + " productos");
            long startTime = System.currentTimeMillis();

            if (productosIds == null || productosIds.isEmpty()) {
                return responseService.badRequest("Lista de IDs vacía");
            }

            // Limitar a 50 productos por request
            if (productosIds.size() > 50) {
                return responseService.badRequest("Máximo 50 productos por request");
            }

            // Solo verificar que existen y retornar metadata ligera
            List<Producto> productos = this.theProductoRepository.findAllById(productosIds);

            // Retornar SOLO metadata (NO las imágenes completas)
            Map<String, Object> metadata = new HashMap<>();
            for (Producto p : productos) {
                Map<String, Object> info = new HashMap<>();

                // Determinar si tiene imagen
                String imgUrl = p.getImagenUrl();
                boolean tieneImagen = imgUrl != null && !imgUrl.isEmpty();

                info.put("tieneImagen", tieneImagen);

                // Si tiene imagen, indicar tipo y tamaño aproximado
                if (tieneImagen) {
                    if (imgUrl.startsWith("data:image/")) {
                        info.put("tipo", "base64");
                        info.put("tamanio", imgUrl.length());
                    } else {
                        info.put("tipo", "url");
                        info.put("url", imgUrl); // Solo si es URL corta
                    }
                }

                metadata.put(p.get_id(), info);
            }

            long endTime = System.currentTimeMillis();
            System.out.println("✅ Metadata de imágenes: " + (endTime - startTime) + "ms");
            System.out.println("📊 Productos verificados: " + metadata.size());

            return responseService.success(metadata,
                    "Metadata de " + metadata.size() + " productos");
        } catch (Exception e) {
            System.err.println("❌ ERROR verificando imágenes: " + e.getMessage());
            e.printStackTrace();
            return responseService.internalError("Error: " + e.getMessage());
        }
    }

    /**
     * Endpoint para obtener UNA SOLA imagen de un producto (lazy loading) Uso: GET
     * /api/productos/{id}/imagen
     * 
     * IMPORTANTE: Este es el ÚNICO endpoint que retorna la imagen completa (base64) El frontend
     * debe llamar a este endpoint solo cuando necesite mostrar la imagen
     */
    @GetMapping("/{id}/imagen")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getImagenProducto(
            @PathVariable String id) {
        try {
            System.out.println("🖼️ Cargando imagen individual de producto: " + id);
            long startTime = System.currentTimeMillis();

            Producto producto = this.theProductoRepository.findById(id).orElse(null);
            
            if (producto == null) {
                return responseService.notFound("Producto no encontrado");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("_id", producto.get_id());
            result.put("nombre", producto.getNombre());

            String imagenUrl = producto.getImagenUrl();

            if (imagenUrl == null || imagenUrl.isEmpty()) {
                result.put("tieneImagen", false);
                result.put("imagenUrl", null);
            } else {
                result.put("tieneImagen", true);
                result.put("imagenUrl", imagenUrl);

                // Log del tamaño para debugging
                if (imagenUrl.startsWith("data:image/")) {
                    int tamanioKB = imagenUrl.length() / 1024;
                    System.out.println("📊 Imagen base64 - Tamaño: " + tamanioKB + "KB");
                }
            }

            long endTime = System.currentTimeMillis();
            System.out.println("✅ Imagen cargada en: " + (endTime - startTime) + "ms");

            return responseService.success(result, "Imagen del producto");
        } catch (Exception e) {
            System.err.println("❌ ERROR obteniendo imagen: " + e.getMessage());
            return responseService.internalError("Error obteniendo imagen: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Producto>>> searchProductos() {
        // Usar aggregation pipeline con $search
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("estado").regex("^activo$", "i")),
                Aggregation.limit(1000));

        List<Producto> productos =
                mongoTemplate.aggregate(aggregation, "producto", Producto.class).getMappedResults();

        return responseService.success(productos, "Productos cargados exitosamente");
    }

    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<List<Producto>>> findByNombreContaining(@RequestParam String nombre) {
        try {
            List<Producto> productos = this.theProductoRepository.findByNombreContainingIgnoreCase(nombre);
            return responseService.success(productos, "Búsqueda completada");
        } catch (Exception e) {
            return responseService.internalError("Error en la búsqueda: " + e.getMessage());
        }
    }

    @GetMapping("/filtrar")
    public ResponseEntity<ApiResponse<List<Producto>>> filtrarProductos(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String categoriaId) {
        try {
            // Si ambos parámetros están vacíos, devolver todos los productos
            if ((nombre == null || nombre.isEmpty()) && (categoriaId == null || categoriaId.isEmpty())) {
                List<Producto> productos = this.theProductoRepository.findAll();
                return responseService.success(productos, "Todos los productos obtenidos");
            }

            // Procesamiento del nombre para la búsqueda
            String nombreProcesado = null;
            if (nombre != null && !nombre.isEmpty()) {
                nombreProcesado = nombre.toLowerCase();
            }

            // Procesamiento de la categoría para la búsqueda
            String categoriaProcesada = null;
            if (categoriaId != null && !categoriaId.isEmpty()) {
                categoriaProcesada = categoriaId;
            }

            // Realizar la búsqueda con los parámetros procesados
            List<Producto> productos = this.theProductoRepository.findByNombreAndCategoriaId(nombreProcesado, categoriaProcesada);
            return responseService.success(productos, "Filtrado de productos completado");
        } catch (Exception e) {
            return responseService.internalError("Error al filtrar productos: " + e.getMessage());
        }
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<ApiResponse<List<Producto>>> findByEstado(@PathVariable String estado) {
        try {
            List<Producto> productos = this.theProductoRepository.findByEstado(estado);
            return responseService.success(productos, "Productos por estado obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al buscar productos por estado: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Producto>> update(@PathVariable String id, @RequestBody Producto newProducto) {
        try {
            Producto actualProducto = this.theProductoRepository.findById(id).orElse(null);
            if (actualProducto == null) {
                return responseService.notFound("Producto no encontrado con ID: " + id);
            }

            // Validar que el nombre no exista en otro producto
            Producto existingByNombre = this.theProductoRepository.findByNombre(newProducto.getNombre());
            if (existingByNombre != null && !existingByNombre.get_id().equals(id)) {
                return responseService.conflict("Ya existe otro producto con el nombre: " + newProducto.getNombre());
            }

            // Validaciones de negocio
            if (newProducto.getPrecio() <= 0) {
                return responseService.badRequest("El precio debe ser mayor a 0");
            }

            if (newProducto.getCosto() < 0) {
                return responseService.badRequest("El costo no puede ser negativo");
            }

            // Actualizar campos
            actualProducto.setNombre(newProducto.getNombre());
            actualProducto.setPrecio(newProducto.getPrecio());
            actualProducto.setCosto(newProducto.getCosto());
            actualProducto.setImpuestos(newProducto.getImpuestos());
            actualProducto.setTieneVariantes(newProducto.isTieneVariantes());
            actualProducto.setEstado(newProducto.getEstado());
            actualProducto.setImagenUrl(newProducto.getImagenUrl());

            // Log para depuración de la categoría
            System.out.println("CategoriaId recibida en PUT: '" + newProducto.getCategoriaId() + "'");
            actualProducto.setCategoriaId(newProducto.getCategoriaId());
            System.out.println("CategoriaId asignada al producto: '" + actualProducto.getCategoriaId() + "'");

            actualProducto.setDescripcion(newProducto.getDescripcion());
            actualProducto.setCantidad(newProducto.getCantidad());
            actualProducto.setNota(newProducto.getNota());
            actualProducto.setIngredientesDisponibles(newProducto.getIngredientesDisponibles());

            // Campos nuevos para combo/individual
            actualProducto.setTieneIngredientes(newProducto.isTieneIngredientes());
            actualProducto.setTipoProducto(newProducto.getTipoProducto());

            // Resolver nombres de ingredientes requeridos antes de guardar
            if (newProducto.getIngredientesRequeridos() != null) {
                for (IngredienteProducto ingredienteReq : newProducto.getIngredientesRequeridos()) {
                    if (ingredienteReq.getNombre() == null || ingredienteReq.getNombre().isEmpty()) {
                        Ingrediente ingrediente = this.theIngredienteRepository.findById(ingredienteReq.getIngredienteId()).orElse(null);
                        if (ingrediente != null) {
                            ingredienteReq.setNombre(ingrediente.getNombre());
                        }
                    }
                }
            }

            // Resolver nombres de ingredientes opcionales antes de guardar
            if (newProducto.getIngredientesOpcionales() != null) {
                for (IngredienteProducto ingredienteOpc : newProducto.getIngredientesOpcionales()) {
                    if (ingredienteOpc.getNombre() == null || ingredienteOpc.getNombre().isEmpty()) {
                        Ingrediente ingrediente = this.theIngredienteRepository.findById(ingredienteOpc.getIngredienteId()).orElse(null);
                        if (ingrediente != null) {
                            ingredienteOpc.setNombre(ingrediente.getNombre());
                        }
                    }
                }
            }

            actualProducto.setIngredientesRequeridos(newProducto.getIngredientesRequeridos());
            actualProducto.setIngredientesOpcionales(newProducto.getIngredientesOpcionales());

            // Actualizar utilidad: si viene 0.0 en el request, calcular automáticamente
            if (newProducto.getUtilidad() == 0.0) {
                double utilidad = newProducto.getPrecio() - newProducto.getCosto() - newProducto.getImpuestos();
                actualProducto.setUtilidad(utilidad);
            } else {
                // Si viene un valor específico de utilidad, usarlo
                actualProducto.setUtilidad(newProducto.getUtilidad());
            }

            System.out.println("Guardando producto con categoriaId: '" + actualProducto.getCategoriaId() + "'");
            Producto updatedProducto = this.theProductoRepository.save(actualProducto);
            System.out.println("Producto guardado. CategoriaId después del save: '" + updatedProducto.getCategoriaId() + "'");
            return responseService.success(updatedProducto, "Producto actualizado exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al actualizar producto: " + e.getMessage());
        }
    }

    /**
     * Obtiene las opciones de ingredientes para un producto (requeridos +
     * opcionales)
     */
    @GetMapping("/{id}/opciones-ingredientes")
    public ResponseEntity<ApiResponse<List<IngredienteConCategoriaDTO>>> getOpcionesIngredientes(@PathVariable String id) {
        try {
            Producto producto = this.theProductoRepository.findById(id).orElse(null);
            if (producto == null) {
                return responseService.notFound("Producto no encontrado con ID: " + id);
            }

            if (!producto.isTieneIngredientes()) {
                return responseService.success(List.of(), "El producto no permite personalización de ingredientes");
            }

            // Obtener IDs de ingredientes opcionales únicamente
            List<String> ingredientesIds = producto.getIngredientesOpcionales()
                    .stream()
                    .map(IngredienteProducto::getIngredienteId)
                    .toList();

            if (ingredientesIds.isEmpty()) {
                return responseService.success(List.of(), "El producto no tiene ingredientes opcionales configurados");
            }

            List<Ingrediente> ingredientes = this.theIngredienteRepository.findAllById(ingredientesIds);

            // Convertir a DTO sin categorías
            List<IngredienteConCategoriaDTO> ingredientesConCategoria = ingredientes.stream()
                    .map(ingrediente -> {
                        // Usar directamente el campo unidad del ingrediente
                        String unidadNombre = ingrediente.getUnidad();
                        String unidadAbreviatura = ingrediente.getUnidad();
                        return new IngredienteConCategoriaDTO(
                                ingrediente.get_id(),
                                ingrediente.getCategoriaId(),
                                "Sin categoría",
                                ingrediente.getNombre(),
                                unidadNombre,
                                unidadAbreviatura,
                                ingrediente.getStockActual(),
                                ingrediente.getStockMinimo()
                        );
                    })
                    .toList();

            return responseService.success(ingredientesConCategoria, "Opciones de ingredientes obtenidas exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener opciones de ingredientes: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/es-combo")
    public ResponseEntity<ApiResponse<Boolean>> verificarSiEsCombo(@PathVariable String id) {
        try {
            Producto producto = this.theProductoRepository.findById(id).orElse(null);
            if (producto == null) {
                return responseService.notFound("Producto no encontrado con ID: " + id);
            }

            boolean esCombo = producto.isTieneIngredientes() && "combo".equals(producto.getTipoProducto());
            return responseService.success(esCombo, "Verificación de tipo de producto completada");
        } catch (Exception e) {
            return responseService.internalError("Error al verificar tipo de producto: " + e.getMessage());
        }
    }

    /**
     * Obtiene un producto con los nombres de los ingredientes resueltos
     */
    /**
     * Obtiene un producto con los nombres de los ingredientes resueltos
     * OPTIMIZADO: Usa carga por lotes para ingredientes del producto
     */
    @GetMapping("/{id}/con-nombres-ingredientes")
    public ResponseEntity<ApiResponse<Producto>> getProductoConNombresIngredientes(@PathVariable String id) {
        try {
            Producto producto = this.theProductoRepository.findById(id).orElse(null);
            if (producto == null) {
                return responseService.notFound("Producto no encontrado con ID: " + id);
            }

            // OPTIMIZACIÓN: Recopilar todos los IDs de ingredientes del producto
            Set<String> ingredientesIds = new HashSet<>();
            
            if (producto.getIngredientesRequeridos() != null) {
                for (IngredienteProducto ip : producto.getIngredientesRequeridos()) {
                    if (ip.getIngredienteId() != null) {
                        ingredientesIds.add(ip.getIngredienteId());
                    }
                }
            }
            if (producto.getIngredientesOpcionales() != null) {
                for (IngredienteProducto ip : producto.getIngredientesOpcionales()) {
                    if (ip.getIngredienteId() != null) {
                        ingredientesIds.add(ip.getIngredienteId());
                    }
                }
            }
            
            // CARGA POR LOTES: Una sola consulta para todos los ingredientes del producto
            Map<String, String> mapaIngredientes = new HashMap<>();
            if (!ingredientesIds.isEmpty()) {
                List<Ingrediente> ingredientes = this.theIngredienteRepository.findAllById(ingredientesIds);
                for (Ingrediente ingrediente : ingredientes) {
                    mapaIngredientes.put(ingrediente.get_id(), ingrediente.getNombre());
                }
            }

            // Resolver nombres de ingredientes requeridos
            if (producto.getIngredientesRequeridos() != null) {
                for (IngredienteProducto ingredienteProducto : producto.getIngredientesRequeridos()) {
                    if (ingredienteProducto.getNombre() == null || ingredienteProducto.getNombre().isEmpty()) {
                        String nombre = mapaIngredientes.get(ingredienteProducto.getIngredienteId());
                        ingredienteProducto.setNombre(nombre != null ? nombre : "Ingrediente no encontrado");
                    }
                }
            }

            // Resolver nombres de ingredientes opcionales
            if (producto.getIngredientesOpcionales() != null) {
                for (IngredienteProducto ingredienteProducto : producto.getIngredientesOpcionales()) {
                    if (ingredienteProducto.getNombre() == null || ingredienteProducto.getNombre().isEmpty()) {
                        String nombre = mapaIngredientes.get(ingredienteProducto.getIngredienteId());
                        ingredienteProducto.setNombre(nombre != null ? nombre : "Ingrediente no encontrado");
                    }
                }
            }

            return responseService.success(producto, "Producto con nombres de ingredientes obtenido exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener producto con nombres: " + e.getMessage());
        }
    }

    /**
     * Obtiene todos los productos con los nombres de ingredientes resueltos OPTIMIZADO: Usa carga
     * por lotes para evitar N+1 queries
     */
    @GetMapping("/con-nombres-ingredientes")
    public ResponseEntity<ApiResponse<List<Producto>>> getAllProductosConNombres() {
        try {
            System.out.println("⚡ ENDPOINT OPTIMIZADO /con-nombres-ingredientes - CON CACHÉ");
            long startTime = System.currentTimeMillis();

            // USAR CACHÉ en lugar de findAll() directo
            List<Producto> productos = this.cacheOptimizationService.getAllProductosCached();
            System.out.println("📦 Productos cargados: " + productos.size());

            // OPTIMIZACIÓN: Recopilar todos los IDs de ingredientes únicos
            Set<String> todosLosIngredientesIds = new HashSet<>();

            for (Producto producto : productos) {
                if (producto.getIngredientesRequeridos() != null) {
                    for (IngredienteProducto ip : producto.getIngredientesRequeridos()) {
                        if (ip.getIngredienteId() != null) {
                            todosLosIngredientesIds.add(ip.getIngredienteId());
                        }
                    }
                }
                if (producto.getIngredientesOpcionales() != null) {
                    for (IngredienteProducto ip : producto.getIngredientesOpcionales()) {
                        if (ip.getIngredienteId() != null) {
                            todosLosIngredientesIds.add(ip.getIngredienteId());
                        }
                    }
                }
            }

            System.out.println(
                    "🔍 IDs únicos de ingredientes encontrados: " + todosLosIngredientesIds.size());

            // CARGA POR LOTES: Una sola consulta para todos los ingredientes
            Map<String, String> mapaIngredientes = new HashMap<>();
            if (!todosLosIngredientesIds.isEmpty()) {
                List<Ingrediente> ingredientes =
                        this.theIngredienteRepository.findAllById(todosLosIngredientesIds);
                System.out.println("📋 Ingredientes cargados: " + ingredientes.size());

                for (Ingrediente ingrediente : ingredientes) {
                    mapaIngredientes.put(ingrediente.get_id(), ingrediente.getNombre());
                }
            }

            // RESOLUCIÓN RÁPIDA: Usar el mapa en memoria
            for (Producto producto : productos) {
                // Resolver nombres de ingredientes requeridos
                if (producto.getIngredientesRequeridos() != null) {
                    for (IngredienteProducto ingredienteProducto : producto.getIngredientesRequeridos()) {
                        if (ingredienteProducto.getNombre() == null || ingredienteProducto.getNombre().isEmpty()) {
                            String nombre =
                                    mapaIngredientes.get(ingredienteProducto.getIngredienteId());
                            ingredienteProducto.setNombre(
                                    nombre != null ? nombre : "Ingrediente no encontrado");
                        }
                    }
                }

                // Resolver nombres de ingredientes opcionales
                if (producto.getIngredientesOpcionales() != null) {
                    for (IngredienteProducto ingredienteProducto : producto.getIngredientesOpcionales()) {
                        if (ingredienteProducto.getNombre() == null || ingredienteProducto.getNombre().isEmpty()) {
                            String nombre =
                                    mapaIngredientes.get(ingredienteProducto.getIngredienteId());
                            ingredienteProducto.setNombre(
                                    nombre != null ? nombre : "Ingrediente no encontrado");
                        }
                    }
                }
            }

            long endTime = System.currentTimeMillis();
            System.out.println("⚡ Endpoint completado en: " + (endTime - startTime) + "ms");

            return responseService.success(productos, "Productos con nombres de ingredientes obtenidos exitosamente");
        } catch (Exception e) {
            System.err.println("❌ Error en /con-nombres-ingredientes: " + e.getMessage());
            e.printStackTrace();
            return responseService.internalError("Error al obtener productos con nombres: " + e.getMessage());
        }
    }

    /**
     * Crear múltiples productos al mismo tiempo
     */
    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<List<Producto>>> createBatch(@RequestBody List<Producto> productos) {
        try {
            if (productos == null || productos.isEmpty()) {
                return responseService.badRequest("La lista de productos no puede estar vacía");
            }

            List<Producto> productosCreados = new java.util.ArrayList<>();
            List<String> errores = new java.util.ArrayList<>();

            for (int i = 0; i < productos.size(); i++) {
                Producto producto = productos.get(i);

                try {
                    // Validar que no exista un producto con el mismo nombre
                    if (this.theProductoRepository.existsByNombre(producto.getNombre())) {
                        errores.add("Producto " + (i + 1) + ": Ya existe un producto con el nombre '" + producto.getNombre() + "'");
                        continue;
                    }

                    // La categoría ya no es obligatoria
                    // Se permite null o vacío

                    // Validar ingredientes requeridos si existen
                    if (producto.getIngredientesRequeridos() != null && !producto.getIngredientesRequeridos().isEmpty()) {
                        boolean ingredientesValidos = true;
                        for (IngredienteProducto ip : producto.getIngredientesRequeridos()) {
                            if (ip.getIngredienteId() == null || ip.getIngredienteId().trim().isEmpty()) {
                                errores.add("Producto " + (i + 1) + ": ID de ingrediente requerido");
                                ingredientesValidos = false;
                                break;
                            }

                            if (!this.theIngredienteRepository.existsById(ip.getIngredienteId())) {
                                errores.add("Producto " + (i + 1) + ": El ingrediente con ID '" + ip.getIngredienteId() + "' no existe");
                                ingredientesValidos = false;
                                break;
                            }

                            // Asignar nombre del ingrediente si no está presente
                            if (ip.getNombre() == null || ip.getNombre().isEmpty()) {
                                Ingrediente ingrediente = this.theIngredienteRepository.findById(ip.getIngredienteId()).orElse(null);
                                if (ingrediente != null) {
                                    ip.setNombre(ingrediente.getNombre());
                                }
                            }
                        }

                        if (!ingredientesValidos) {
                            continue;
                        }
                    }

                    // Validar ingredientes opcionales si existen
                    if (producto.getIngredientesOpcionales() != null && !producto.getIngredientesOpcionales().isEmpty()) {
                        boolean ingredientesValidos = true;
                        for (IngredienteProducto ip : producto.getIngredientesOpcionales()) {
                            if (ip.getIngredienteId() == null || ip.getIngredienteId().trim().isEmpty()) {
                                errores.add("Producto " + (i + 1) + ": ID de ingrediente opcional requerido");
                                ingredientesValidos = false;
                                break;
                            }

                            if (!this.theIngredienteRepository.existsById(ip.getIngredienteId())) {
                                errores.add("Producto " + (i + 1) + ": El ingrediente opcional con ID '" + ip.getIngredienteId() + "' no existe");
                                ingredientesValidos = false;
                                break;
                            }

                            // Asignar nombre del ingrediente si no está presente
                            if (ip.getNombre() == null || ip.getNombre().isEmpty()) {
                                Ingrediente ingrediente = this.theIngredienteRepository.findById(ip.getIngredienteId()).orElse(null);
                                if (ingrediente != null) {
                                    ip.setNombre(ingrediente.getNombre());
                                }
                            }
                        }

                        if (!ingredientesValidos) {
                            continue;
                        }
                    }

                    // Asegurar que el ID sea null antes de guardar
                    producto.set_id(null);

                    // Validar campos obligatorios
                    if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
                        errores.add("Producto " + (i + 1) + ": El nombre es obligatorio");
                        continue;
                    }

                    if (producto.getPrecio() <= 0) {
                        errores.add("Producto " + (i + 1) + ": El precio debe ser mayor a 0");
                        continue;
                    }

                    Producto nuevoProducto = this.theProductoRepository.save(producto);
                    if (nuevoProducto.get_id() != null) {
                        productosCreados.add(nuevoProducto);
                    } else {
                        errores.add("Producto " + (i + 1) + ": Error al generar ID para '" + producto.getNombre() + "'");
                    }
                } catch (Exception e) {
                    errores.add("Producto " + (i + 1) + ": Error al crear '" + producto.getNombre() + "' - " + e.getMessage());
                }
            }

            if (productosCreados.isEmpty()) {
                return responseService.badRequest("No se pudo crear ningún producto. Errores: " + String.join(", ", errores));
            } else if (!errores.isEmpty()) {
                return responseService.success(productosCreados,
                        "Se crearon " + productosCreados.size() + " de " + productos.size() + " productos. Errores: " + String.join(", ", errores));
            } else {
                return responseService.created(productosCreados,
                        "Se crearon exitosamente " + productosCreados.size() + " productos");
            }
        } catch (Exception e) {
            return responseService.internalError("Error al crear productos en lote: " + e.getMessage());
        }
    }

    /**
     * Endpoint que devuelve solo los nombres de los productos con sus complementos
     * obligatorios y opcionales (solo nombres)
     */
    @GetMapping("/nombres-completos")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getProductosNombresCompletos() {
        try {
            List<Producto> productos = theProductoRepository.findAll();
            List<Map<String, Object>> productosSimplificados = new ArrayList<>();
            
            for (Producto producto : productos) {
                Map<String, Object> productoInfo = new HashMap<>();
                productoInfo.put("nombre", producto.getNombre());
                
                // Obtener nombres de ingredientes obligatorios (requeridos)
                List<String> nombresObligatorios = new ArrayList<>();
                if (producto.getIngredientesRequeridos() != null) {
                    for (IngredienteProducto ingrediente : producto.getIngredientesRequeridos()) {
                        String nombreIng = ingrediente.getNombre();
                        if (nombreIng == null || nombreIng.isEmpty()) {
                            Ingrediente ing = this.theIngredienteRepository.findById(ingrediente.getIngredienteId()).orElse(null);
                            if (ing != null) nombreIng = ing.getNombre();
                            else nombreIng = "Ingrediente no encontrado";
                        }
                        nombresObligatorios.add(nombreIng);
                    }
                }
                productoInfo.put("obligatorios", nombresObligatorios);
                
                // Obtener nombres de ingredientes opcionales
                List<String> nombresOpcionales = new ArrayList<>();
                if (producto.getIngredientesOpcionales() != null) {
                    for (IngredienteProducto ingrediente : producto.getIngredientesOpcionales()) {
                        String nombreIng = ingrediente.getNombre();
                        if (nombreIng == null || nombreIng.isEmpty()) {
                            Ingrediente ing = this.theIngredienteRepository.findById(ingrediente.getIngredienteId()).orElse(null);
                            if (ing != null) nombreIng = ing.getNombre();
                            else nombreIng = "Ingrediente no encontrado";
                        }
                        nombresOpcionales.add(nombreIng);
                    }
                }
                productoInfo.put("opcionales", nombresOpcionales);
                
                productosSimplificados.add(productoInfo);
            }
            
            return responseService.success(productosSimplificados, 
                "Nombres de productos con complementos obtenidos exitosamente");
                
        } catch (Exception e) {
            return responseService.internalError("Error al obtener nombres de productos: " + e.getMessage());
        }
    }

    /**
     * Endpoint paginado para productos. Frontend expects /api/productos/paginados?page=0&size=50
     */
    @GetMapping("/paginados")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProductosPaginados(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            System.out.println("🚀 /paginados - OPTIMIZADO COMO INGREDIENTES");
            long startTime = System.currentTimeMillis();

            // USAR AGGREGATION PIPELINE directo (igual que /search)
            Aggregation aggregation = Aggregation.newAggregation(
                    Aggregation.match(Criteria.where("estado").regex("^activo$", "i")),
                    Aggregation
                            .project("_id", "nombre", "precio", "imagenUrl", "categoriaId",
                                    "tieneIngredientes", "tipoProducto", "estado")
                            .andExclude("ingredientesRequeridos", "ingredientesOpcionales",
                                    "descripcion", "nota", "ingredientesDisponibles"));

            List<Producto> productos = mongoTemplate
                    .aggregate(aggregation, "producto", Producto.class).getMappedResults();

            // Paginación en memoria
            int totalElements = productos.size();
            int start = page * size;
            int end = Math.min(start + size, totalElements);

            List<Producto> paginaActual =
                    start < totalElements ? productos.subList(start, end) : List.of();

            Map<String, Object> result = new HashMap<>();
            result.put("content", paginaActual);
            result.put("page", page);
            result.put("size", size);
            result.put("totalPages", (int) Math.ceil((double) totalElements / size));
            result.put("totalElements", totalElements);

            long endTime = System.currentTimeMillis();
            System.out.println(
                    "✅ Completado en: " + (endTime - startTime) + "ms (vs 220,000ms antes)");
            System.out.println("📊 Total productos activos: " + totalElements);

            return responseService.success(result, "Productos cargados exitosamente");
        } catch (Exception e) {
            System.err.println("❌ ERROR en /api/productos/paginados: " + e.getMessage());
            e.printStackTrace();
            return responseService.internalError("Error al obtener productos: " + e.getMessage());
        }
    }

    /**
     * 📊 CARGA MASIVA DE PRODUCTOS DESDE EXCEL Columnas esperadas según el formato del sistema:
     * CODIGO*, NOMBRE DEL PRODUCTO*, PRECIO VENTA PRINCIPAL*, COSTO UNITARIO*, PRODUCTO O
     * SERVICIO*, CONTROL DE INVENTARIO, % IMPUESTO, INVENTARIO BAJO, INVENTARIO ÓPTIMO, TIPO
     * PRODUCTO (NOMBRE), LINEA PRODUCTO, CLASE PRODUCTO, CÓDIGO DE BARRAS, LOCALIZACIÓN, NOMBRE
     * PROVEEDOR, NIT PROVEEDOR, MARCA, ALMACEN, BODEGA, etc.
     */
    @PostMapping("/carga-masiva")
    public ResponseEntity<ApiResponse<Map<String, Object>>> cargarProductosDesdeExcel(
            @RequestParam("archivo") org.springframework.web.multipart.MultipartFile archivo) {
        try {
            System.out.println("📊 CARGA MASIVA - Iniciando procesamiento de Excel");
            long startTime = System.currentTimeMillis();

            if (archivo.isEmpty()) {
                return responseService.badRequest("El archivo está vacío");
            }

            String nombreArchivo = archivo.getOriginalFilename();
            if (nombreArchivo == null
                    || (!nombreArchivo.endsWith(".xlsx") && !nombreArchivo.endsWith(".xls"))) {
                return responseService.badRequest("El archivo debe ser un Excel (.xlsx o .xls)");
            }

            List<Producto> productosCreados = new ArrayList<>();
            List<Map<String, Object>> errores = new ArrayList<>();
            int filasProcesadas = 0;

            try (org.apache.poi.ss.usermodel.Workbook workbook =
                    org.apache.poi.ss.usermodel.WorkbookFactory.create(archivo.getInputStream())) {

                org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);

                org.apache.poi.ss.usermodel.Row headerRow = sheet.getRow(0);
                if (headerRow == null) {
                    return responseService.badRequest("El archivo Excel no tiene encabezados");
                }

                // Mapear encabezados (normalizar a minúsculas, sin asteriscos, espacios a guion
                // bajo, sin tildes)
                Map<String, Integer> columnas = new HashMap<>();
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    org.apache.poi.ss.usermodel.Cell cell = headerRow.getCell(i);
                    if (cell != null) {
                        String header = obtenerValorCelda(cell).toLowerCase().trim()
                                .replace("*", "").replace(" ", "_").replace("á", "a")
                                .replace("é", "e").replace("í", "i").replace("ó", "o")
                                .replace("ú", "u").replace("ñ", "n");
                        columnas.put(header, i);
                        System.out.println("📋 Columna " + i + ": '" + header + "'");
                    }
                }
                System.out.println("📋 Total columnas encontradas: " + columnas.keySet());

                // Mapeo de alias de columnas para flexibilidad (coincide con Excel del usuario)
                // Los alias ya están normalizados (sin *, sin tildes, espacios como _)
                Map<String, String[]> aliasColumnas = new HashMap<>();
                // Fila 1 del Excel
                aliasColumnas.put("codigo", new String[] {"codigo"});
                aliasColumnas.put("nombre",
                        new String[] {"nombre", "nombre_del_producto"});
                aliasColumnas.put("precio", new String[] {"precio", "precio_venta_principal"});
                aliasColumnas.put("costo", new String[] {"costo", "costo_unitario"});
                aliasColumnas.put("tipoitem",
                        new String[] {"tipoitem", "tipo_item", "producto_o_servicio"});
                aliasColumnas.put("controlinventario",
                        new String[] {"controlinventario", "control_de_inventario"});
                aliasColumnas.put("impuesto",
                        new String[] {"impuesto", "impuestos", "%_impuesto", "iva"});
                aliasColumnas.put("stockminimo",
                        new String[] {"stockminimo", "inventario_bajo", "stock_minimo"});
                aliasColumnas.put("stockoptimo",
                        new String[] {"stockoptimo", "inventario_optimo", "stock_optimo"});
                aliasColumnas.put("tipoproductonombre", new String[] {"tipoproductonombre",
                        "tipo_producto_(nombre)", "tipo_producto"});
                aliasColumnas.put("lineaproducto", new String[] {"lineaproducto",
                        "linea_producto_(nombre)", "linea_producto"});

                // Fila 2 del Excel
                aliasColumnas.put("claseproducto", new String[] {"claseproducto",
                        "clase_producto_(nombre)", "clase_producto"});
                aliasColumnas.put("codigobarras",
                        new String[] {"codigobarras", "codigo_de_barras"});
                aliasColumnas.put("localizacion", new String[] {"localizacion"});
                aliasColumnas.put("proveedornombre",
                        new String[] {"proveedornombre", "nombre_proveedor"});
                aliasColumnas.put("proveedornit",
                        new String[] {"proveedornit", "nit_proveedor_(sin_dv)", "nit_proveedor"});
                aliasColumnas.put("marca", new String[] {"marca"});
                aliasColumnas.put("precioopc1",
                        new String[] {"precio_de_venta_opc_1", "precioopc1"});
                aliasColumnas.put("precioopc2",
                        new String[] {"precio_de_venta_opc_2", "precioopc2"});
                aliasColumnas.put("precioopc3",
                        new String[] {"precio_de_venta_opc_3", "precioopc3"});
                aliasColumnas.put("precioopc4",
                        new String[] {"precio_de_venta_opc_4", "precioopc4"});
                aliasColumnas.put("precioopc5",
                        new String[] {"precio_de_venta_opc_5", "precioopc5"});

                // Fila 3 del Excel
                aliasColumnas.put("almacen", new String[] {"almacen"});
                aliasColumnas.put("bodega", new String[] {"bodega"});
                aliasColumnas.put("ubicacion3", new String[] {"ubicacion_3"});
                aliasColumnas.put("ubicacion4", new String[] {"ubicacion_4"});
                aliasColumnas.put("localizacionubi1", new String[] {"localizacion_ubi_1"});
                aliasColumnas.put("localizacionubi2", new String[] {"localizacion_ubi_2"});
                aliasColumnas.put("localizacionubi3", new String[] {"localizacion_ubi_3"});
                aliasColumnas.put("localizacionubi4", new String[] {"localizacion_ubi_4"});

                // Resolver columnas con alias
                Map<String, Integer> columnasResueltas = new HashMap<>();
                for (Map.Entry<String, String[]> entry : aliasColumnas.entrySet()) {
                    for (String alias : entry.getValue()) {
                        if (columnas.containsKey(alias)) {
                            columnasResueltas.put(entry.getKey(), columnas.get(alias));
                            break;
                        }
                    }
                }
                System.out.println("✅ Columnas resueltas: " + columnasResueltas.keySet());

                // Validar columnas obligatorias
                if (!columnasResueltas.containsKey("nombre")) {
                    return responseService.badRequest(
                            "El Excel debe contener la columna 'NOMBRE DEL PRODUCTO' o 'nombre'");
                }
                if (!columnasResueltas.containsKey("precio")) {
                    return responseService.badRequest(
                            "El Excel debe contener la columna 'PRECIO VENTA PRINCIPAL' o 'precio'");
                }

                // Procesar cada fila
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
                    if (row == null)
                        continue;

                    filasProcesadas++;

                    try {
                        String nombre =
                                obtenerValorCeldaSeguro(row, columnasResueltas.get("nombre"));

                        if (nombre == null || nombre.trim().isEmpty()) {
                            Map<String, Object> error = new HashMap<>();
                            error.put("fila", i + 1);
                            error.put("error", "El nombre del producto está vacío");
                            errores.add(error);
                            continue;
                        }

                        String codigo =
                                obtenerValorCeldaSeguro(row, columnasResueltas.get("codigo"));

                        // Verificar si ya existe un producto con ese código o nombre
                        if (codigo != null && !codigo.isEmpty()
                                && this.theProductoRepository.existsByCodigo(codigo.trim())) {
                            Map<String, Object> error = new HashMap<>();
                            error.put("fila", i + 1);
                            error.put("codigo", codigo);
                            error.put("error", "Ya existe un producto con este código");
                            errores.add(error);
                            continue;
                        }

                        if (this.theProductoRepository.existsByNombre(nombre.trim())) {
                            Map<String, Object> error = new HashMap<>();
                            error.put("fila", i + 1);
                            error.put("nombre", nombre);
                            error.put("error", "Ya existe un producto con este nombre");
                            errores.add(error);
                            continue;
                        }

                        double precio =
                                obtenerValorNumerico(row, columnasResueltas.get("precio"), 0.0);
                        if (precio <= 0) {
                            Map<String, Object> error = new HashMap<>();
                            error.put("fila", i + 1);
                            error.put("nombre", nombre);
                            error.put("error", "El precio debe ser mayor a 0");
                            errores.add(error);
                            continue;
                        }

                        // Crear el producto con todos los campos
                        Producto producto = new Producto();

                        // Información básica
                        producto.setCodigo(codigo != null ? codigo.trim() : null);
                        producto.setNombre(nombre.trim());
                        producto.setCodigoBarras(obtenerValorCeldaSeguro(row,
                                columnasResueltas.get("codigobarras")));

                        // Precios
                        producto.setPrecio(precio);
                        producto.setCosto(
                                obtenerValorNumerico(row, columnasResueltas.get("costo"), 0.0));
                        producto.setImpuestos(
                                obtenerValorNumerico(row, columnasResueltas.get("impuesto"), 0.0));
                        producto.calcularPrecioConIva();
                        producto.calcularUtilidad();

                        // Precios opcionales
                        List<Double> preciosOpc = new ArrayList<>();
                        double precioOpc1 =
                                obtenerValorNumerico(row, columnasResueltas.get("precioopc1"), 0.0);
                        double precioOpc2 =
                                obtenerValorNumerico(row, columnasResueltas.get("precioopc2"), 0.0);
                        double precioOpc3 =
                                obtenerValorNumerico(row, columnasResueltas.get("precioopc3"), 0.0);
                        double precioOpc4 =
                                obtenerValorNumerico(row, columnasResueltas.get("precioopc4"), 0.0);
                        double precioOpc5 =
                                obtenerValorNumerico(row, columnasResueltas.get("precioopc5"), 0.0);
                        if (precioOpc1 > 0)
                            preciosOpc.add(precioOpc1);
                        if (precioOpc2 > 0)
                            preciosOpc.add(precioOpc2);
                        if (precioOpc3 > 0)
                            preciosOpc.add(precioOpc3);
                        if (precioOpc4 > 0)
                            preciosOpc.add(precioOpc4);
                        if (precioOpc5 > 0)
                            preciosOpc.add(precioOpc5);
                        if (!preciosOpc.isEmpty()) {
                            producto.setPreciosOpcionales(preciosOpc);
                        }

                        // Clasificación
                        String tipoItem =
                                obtenerValorCeldaSeguro(row, columnasResueltas.get("tipoitem"));
                        producto.setTipoItem(
                                tipoItem != null ? tipoItem.toUpperCase() : "PRODUCTO");
                        producto.setTipoProductoNombre(obtenerValorCeldaSeguro(row,
                                columnasResueltas.get("tipoproductonombre")));
                        producto.setLineaProducto(obtenerValorCeldaSeguro(row,
                                columnasResueltas.get("lineaproducto")));
                        producto.setClaseProducto(obtenerValorCeldaSeguro(row,
                                columnasResueltas.get("claseproducto")));
                        producto.setMarca(
                                obtenerValorCeldaSeguro(row, columnasResueltas.get("marca")));

                        // Inventario
                        String controlInv = obtenerValorCeldaSeguro(row,
                                columnasResueltas.get("controlinventario"));
                        producto.setControlInventario(
                                controlInv == null || "CONTROLAR".equalsIgnoreCase(controlInv)
                                        || "SI".equalsIgnoreCase(controlInv)
                                        || "SÍ".equalsIgnoreCase(controlInv));
                        producto.setStockMinimo((int) obtenerValorNumerico(row,
                                columnasResueltas.get("stockminimo"), 0.0));
                        producto.setStockOptimo((int) obtenerValorNumerico(row,
                                columnasResueltas.get("stockoptimo"), 0.0));
                        producto.setCantidadAlmacen((int) obtenerValorNumerico(row,
                                columnasResueltas.get("almacen"), 0.0));
                        producto.setCantidadBodega((int) obtenerValorNumerico(row,
                                columnasResueltas.get("bodega"), 0.0));
                        producto.setCantidad(
                                producto.getCantidadAlmacen() + producto.getCantidadBodega());

                        // Ubicación
                        producto.setLocalizacion(obtenerValorCeldaSeguro(row,
                                columnasResueltas.get("localizacion")));
                        producto.setUbicacion3(
                                obtenerValorCeldaSeguro(row, columnasResueltas.get("ubicacion3")));
                        producto.setUbicacion4(
                                obtenerValorCeldaSeguro(row, columnasResueltas.get("ubicacion4")));
                        producto.setLocalizacionUbi1(obtenerValorCeldaSeguro(row,
                                columnasResueltas.get("localizacionubi1")));
                        producto.setLocalizacionUbi2(obtenerValorCeldaSeguro(row,
                                columnasResueltas.get("localizacionubi2")));
                        producto.setLocalizacionUbi3(obtenerValorCeldaSeguro(row,
                                columnasResueltas.get("localizacionubi3")));
                        producto.setLocalizacionUbi4(obtenerValorCeldaSeguro(row,
                                columnasResueltas.get("localizacionubi4")));

                        // Proveedor
                        producto.setProveedorNombre(obtenerValorCeldaSeguro(row,
                                columnasResueltas.get("proveedornombre")));
                        producto.setProveedorNit(obtenerValorCeldaSeguro(row,
                                columnasResueltas.get("proveedornit")));

                        // Guardar producto
                        Producto saved = this.theProductoRepository.save(producto);
                        productosCreados.add(saved);

                    } catch (Exception e) {
                        Map<String, Object> error = new HashMap<>();
                        error.put("fila", i + 1);
                        error.put("error", "Error procesando fila: " + e.getMessage());
                        errores.add(error);
                    }
                }
            }

            long endTime = System.currentTimeMillis();

            Map<String, Object> resultado = new HashMap<>();
            resultado.put("productosCreados", productosCreados.size());
            resultado.put("filasProcesadas", filasProcesadas);
            resultado.put("errores", errores);
            resultado.put("tiempoMs", endTime - startTime);
            resultado.put("productos", productosCreados);

            System.out.println("✅ CARGA MASIVA completada en " + (endTime - startTime) + "ms");
            System.out.println("📦 Productos creados: " + productosCreados.size());
            System.out.println("⚠️ Errores: " + errores.size());

            if (productosCreados.isEmpty() && !errores.isEmpty()) {
                return responseService
                        .badRequest("No se pudieron crear productos. Errores: " + errores.size());
            }

            return responseService.success(resultado, "Carga masiva completada. Productos creados: "
                    + productosCreados.size() + ", Errores: " + errores.size());

        } catch (Exception e) {
            System.err.println("❌ ERROR en carga masiva: " + e.getMessage());
            e.printStackTrace();
            return responseService
                    .internalError("Error al procesar archivo Excel: " + e.getMessage());
        }
    }

    /**
     * 📥 DESCARGAR PLANTILLA EXCEL Genera una plantilla Excel con los encabezados según el formato
     * del sistema
     */
    @GetMapping("/plantilla-excel")
    public ResponseEntity<byte[]> descargarPlantillaExcel() {
        try {
            org.apache.poi.xssf.usermodel.XSSFWorkbook workbook =
                    new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Productos");

            // Crear estilo para encabezados obligatorios (rojo)
            org.apache.poi.ss.usermodel.CellStyle headerStyleRequired = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFontRequired = workbook.createFont();
            headerFontRequired.setBold(true);
            headerFontRequired.setColor(org.apache.poi.ss.usermodel.IndexedColors.RED.getIndex());
            headerStyleRequired.setFont(headerFontRequired);
            headerStyleRequired.setFillForegroundColor(
                    org.apache.poi.ss.usermodel.IndexedColors.LIGHT_YELLOW.getIndex());
            headerStyleRequired
                    .setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);

            // Crear estilo para encabezados opcionales
            org.apache.poi.ss.usermodel.CellStyle headerStyleOptional = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFontOptional = workbook.createFont();
            headerFontOptional.setBold(true);
            headerStyleOptional.setFont(headerFontOptional);
            headerStyleOptional.setFillForegroundColor(
                    org.apache.poi.ss.usermodel.IndexedColors.LIGHT_BLUE.getIndex());
            headerStyleOptional
                    .setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);

            // Crear encabezados según el formato del sistema
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            String[] headers = {"CODIGO*", "NOMBRE DEL PRODUCTO*", "PRECIO VENTA PRINCIPAL*",
                    "COSTO UNITARIO*", "PRODUCTO O SERVICIO*", "CONTROL DE INVENTARIO",
                    "% IMPUESTO", "INVENTARIO BAJO", "INVENTARIO ÓPTIMO", "TIPO PRODUCTO (NOMBRE)",
                    "LINEA PRODUCTO (NOMBRE)", "CLASE PRODUCTO (NOMBRE)", "CÓDIGO DE BARRAS",
                    "LOCALIZACIÓN", "NOMBRE PROVEEDOR", "NIT PROVEEDOR (SIN DV)", "MARCA",
                    "ALMACEN", "BODEGA"};
            boolean[] isRequired = {true, true, true, true, true, false, false, false, false, false,
                    false, false, false, false, false, false, false, false, false};

            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(isRequired[i] ? headerStyleRequired : headerStyleOptional);
                sheet.setColumnWidth(i, 5000);
            }

            // Agregar fila de ejemplo
            org.apache.poi.ss.usermodel.Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("PFFBNKD");
            exampleRow.createCell(1).setCellValue("PUFF BRILLANTE MULTICOLOR");
            exampleRow.createCell(2).setCellValue(30000);
            exampleRow.createCell(3).setCellValue(12500);
            exampleRow.createCell(4).setCellValue("PRODUCTO");
            exampleRow.createCell(5).setCellValue("CONTROLAR");
            exampleRow.createCell(6).setCellValue(0);
            exampleRow.createCell(7).setCellValue(5);
            exampleRow.createCell(8).setCellValue(10);
            exampleRow.createCell(9).setCellValue("ACCESORIO");
            exampleRow.createCell(10).setCellValue("");
            exampleRow.createCell(11).setCellValue("");
            exampleRow.createCell(12).setCellValue("750220000001");
            exampleRow.createCell(13).setCellValue("");
            exampleRow.createCell(14).setCellValue("");
            exampleRow.createCell(15).setCellValue("");
            exampleRow.createCell(16).setCellValue("");
            exampleRow.createCell(17).setCellValue(60);
            exampleRow.createCell(18).setCellValue(72);

            // Convertir a bytes
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            byte[] bytes = outputStream.toByteArray();

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=plantilla_productos.xlsx")
                    .header("Content-Type",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .body(bytes);

        } catch (Exception e) {
            System.err.println("❌ ERROR generando plantilla: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    // ==================== MÉTODOS AUXILIARES PARA EXCEL ====================

    private String obtenerValorCelda(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null)
            return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    return cell.getStringCellValue();
                }
            default:
                return "";
        }
    }

    private String obtenerValorCeldaSeguro(org.apache.poi.ss.usermodel.Row row, Integer colIndex) {
        if (colIndex == null || row == null)
            return null;
        org.apache.poi.ss.usermodel.Cell cell = row.getCell(colIndex);
        if (cell == null)
            return null;
        String valor = obtenerValorCelda(cell);
        return valor.isEmpty() ? null : valor;
    }

    private double obtenerValorNumerico(org.apache.poi.ss.usermodel.Row row, Integer colIndex,
            double defaultValue) {
        if (colIndex == null || row == null)
            return defaultValue;
        org.apache.poi.ss.usermodel.Cell cell = row.getCell(colIndex);
        if (cell == null)
            return defaultValue;

        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return cell.getNumericCellValue();
                case STRING:
                    String valor = cell.getStringCellValue().trim().replace(",", ".");
                    return valor.isEmpty() ? defaultValue : Double.parseDouble(valor);
                case FORMULA:
                    return cell.getNumericCellValue();
                default:
                    return defaultValue;
            }
        } catch (Exception e) {
            return defaultValue;
        }
    }

}
