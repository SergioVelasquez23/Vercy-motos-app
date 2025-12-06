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
import com.prog3.security.Models.Categoria;
import com.prog3.security.Models.IngredienteProducto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.prog3.security.DTOs.CrearProductoRequest;
import com.prog3.security.DTOs.IngredienteConCategoriaDTO;
import com.prog3.security.Repositories.ProductoRepository;
import com.prog3.security.Repositories.IngredienteRepository;
import com.prog3.security.Repositories.CategoriaRepository;
import com.prog3.security.Utils.ApiResponse;

@CrossOrigin
@RestController
@RequestMapping("api/productos")
public class ProductosController extends BaseController<Producto, String> {
    @Autowired
    private com.prog3.security.Repositories.UnidadRepository unidadRepository;

    @Autowired
    private ProductoRepository theProductoRepository;

    @Autowired
    private IngredienteRepository theIngredienteRepository;

    @Autowired
    private CategoriaRepository theCategoriaRepository;

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
        existing.setNombre(updated.getNombre());
        existing.setPrecio(updated.getPrecio());
        existing.setCosto(updated.getCosto());
        existing.setImpuestos(updated.getImpuestos());
        existing.setUtilidad(updated.getUtilidad());
        existing.setTieneVariantes(updated.isTieneVariantes());
        existing.setEstado(updated.getEstado());
        existing.setImagenUrl(updated.getImagenUrl());
        existing.setCategoriaId(updated.getCategoriaId());
        existing.setDescripcion(updated.getDescripcion());
        existing.setCantidad(updated.getCantidad());
        existing.setNota(updated.getNota());
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
     * Endpoint SÚPER OPTIMIZADO: Resumen de productos agrupados por categoría
     * Devuelve solo datos básicos de todos los productos organizados por categoría
     * IDEAL para carga inicial del frontend
     */
    @GetMapping("/resumen-por-categorias")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getResumenPorCategorias(
            @RequestParam(defaultValue = "5") int limitePorCategoria) {
        try {
            System.out.println("📊 ENDPOINT /resumen-por-categorias - SÚPER OPTIMIZADO");
            System.out.println("📋 Límite por categoría: " + limitePorCategoria);
            long startTime = System.currentTimeMillis();
            
            // Obtener todas las categorías
            List<Categoria> categorias = this.theCategoriaRepository.findAll();
            
            Map<String, Object> resumen = new HashMap<>();
            List<Map<String, Object>> categoriasSummary = new ArrayList<>();
            int totalProductos = 0;
            
            for (Categoria categoria : categorias) {
                List<Producto> productosCategoria = this.theProductoRepository.findByCategoriaId(categoria.get_id());
                
                // Filtrar solo productos activos
                List<Producto> productosActivos = productosCategoria.stream()
                    .filter(p -> "Activo".equals(p.getEstado()))
                    .limit(limitePorCategoria) // Limitar cantidad por categoría
                    .toList();
                
                if (!productosActivos.isEmpty()) {
                    Map<String, Object> categoriaInfo = new HashMap<>();
                    categoriaInfo.put("categoriaId", categoria.get_id());
                    categoriaInfo.put("categoriaNombre", categoria.getNombre());
                    categoriaInfo.put("totalProductos", productosActivos.size());
                    
                    // Productos ligeros de esta categoría
                    List<Map<String, Object>> productosLigeros = new ArrayList<>();
                    for (Producto p : productosActivos) {
                        Map<String, Object> productoLigero = new HashMap<>();
                        productoLigero.put("_id", p.get_id());
                        productoLigero.put("nombre", p.getNombre());
                        productoLigero.put("precio", p.getPrecio());
                        productoLigero.put("imagenUrl", p.getImagenUrl());
                        productosLigeros.add(productoLigero);
                    }
                    
                    categoriaInfo.put("productos", productosLigeros);
                    categoriasSummary.add(categoriaInfo);
                    totalProductos += productosActivos.size();
                }
            }
            
            resumen.put("categorias", categoriasSummary);
            resumen.put("totalCategorias", categoriasSummary.size());
            resumen.put("totalProductos", totalProductos);
            resumen.put("limitePorCategoria", limitePorCategoria);
            
            long endTime = System.currentTimeMillis();
            System.out.println("⚡ SÚPER OPTIMIZADO completado en: " + (endTime - startTime) + "ms");
            System.out.println("🏷️ Categorías procesadas: " + categoriasSummary.size());
            System.out.println("📦 Total productos ligeros: " + totalProductos);
            
            return responseService.success(resumen, 
                "Resumen por categorías: " + categoriasSummary.size() + " categorías, " + totalProductos + " productos");
        } catch (Exception e) {
            System.err.println("❌ ERROR en /resumen-por-categorias: " + e.getMessage());
            e.printStackTrace();
            return responseService.internalError("Error al obtener resumen: " + e.getMessage());
        }
    }

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
     * Endpoint para cargar SOLO las imágenes de productos específicos
     * Uso: POST /api/productos/imagenes
     * Body: ["producto_id_1", "producto_id_2", ...]
     * 
     * Estrategia de carga progresiva:
     * 1. Cargar productos con /ligero (sin imágenes)
     * 2. Cargar imágenes solo de los productos visibles en pantalla
     * 3. Cargar más imágenes cuando el usuario haga scroll
     */
    @PostMapping("/imagenes")
    public ResponseEntity<ApiResponse<Map<String, String>>> getImagenesProductos(
            @RequestBody List<String> productosIds) {
        try {
            System.out.println("🖼️ Cargando imágenes de " + productosIds.size() + " productos");
            long startTime = System.currentTimeMillis();

            if (productosIds == null || productosIds.isEmpty()) {
                return responseService.badRequest("Lista de IDs vacía");
            }

            // Limitar a 20 productos por request para evitar payloads gigantes
            if (productosIds.size() > 20) {
                return responseService.badRequest("Máximo 20 productos por request");
            }

            // Buscar solo los productos solicitados
            List<Producto> productos = this.theProductoRepository.findAllById(productosIds);

            // Mapear: productoId -> imagenUrl
            Map<String, String> imagenesMap = new HashMap<>();
            for (Producto p : productos) {
                imagenesMap.put(p.get_id(), p.getImagenUrl());
            }

            long endTime = System.currentTimeMillis();
            System.out.println("✅ Imágenes cargadas en: " + (endTime - startTime) + "ms");
            System.out.println("🖼️ Total imágenes: " + imagenesMap.size());

            return responseService.success(imagenesMap, 
                "Imágenes de " + imagenesMap.size() + " productos");
        } catch (Exception e) {
            System.err.println("❌ ERROR cargando imágenes: " + e.getMessage());
            e.printStackTrace();
            return responseService.internalError("Error cargando imágenes: " + e.getMessage());
        }
    }

    /**
     * Endpoint para obtener UNA SOLA imagen de un producto
     * Uso: GET /api/productos/{id}/imagen
     * 
     * Ideal para lazy loading de imágenes individuales
     */
    @GetMapping("/{id}/imagen")
    public ResponseEntity<ApiResponse<Map<String, String>>> getImagenProducto(@PathVariable String id) {
        try {
            Producto producto = this.theProductoRepository.findById(id).orElse(null);
            
            if (producto == null) {
                return responseService.notFound("Producto no encontrado");
            }

            Map<String, String> result = new HashMap<>();
            result.put("_id", producto.get_id());
            result.put("imagenUrl", producto.getImagenUrl());

            return responseService.success(result, "Imagen del producto");
        } catch (Exception e) {
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
                // Validar que la categoría exista
                if (!theCategoriaRepository.existsById(categoriaId)) {
                    return responseService.badRequest("La categoría con ID " + categoriaId + " no existe");
                }
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

            // Convertir a DTO con información de categorías
            List<IngredienteConCategoriaDTO> ingredientesConCategoria = ingredientes.stream()
                    .map(ingrediente -> {
                        String categoriaNombre = "Sin categoría";
                        if (ingrediente.getCategoriaId() != null) {
                            Categoria categoria = this.theCategoriaRepository.findById(ingrediente.getCategoriaId()).orElse(null);
                            if (categoria != null) {
                                categoriaNombre = categoria.getNombre();
                            }
                        }

                        // Usar directamente el campo unidad del ingrediente
                        String unidadNombre = ingrediente.getUnidad();
                        String unidadAbreviatura = ingrediente.getUnidad();
                        return new IngredienteConCategoriaDTO(
                                ingrediente.get_id(),
                                ingrediente.getCategoriaId(),
                                categoriaNombre,
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

                    // Validar que la categoría exista
                    if (producto.getCategoriaId() == null || producto.getCategoriaId().trim().isEmpty()) {
                        errores.add("Producto " + (i + 1) + ": El ID de categoría es obligatorio");
                        continue;
                    }

                    if (!this.theCategoriaRepository.existsById(producto.getCategoriaId())) {
                        errores.add("Producto " + (i + 1) + ": La categoría con ID '" + producto.getCategoriaId() + "' no existe");
                        continue;
                    }

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

}
