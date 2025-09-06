package com.prog3.security.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

import com.prog3.security.Models.*;
import com.prog3.security.Repositories.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de optimización de cache para mejorar el rendimiento
 * Implementa estrategias de cache para consultas frecuentes
 */
@Service
public class CacheOptimizationService {

    @Autowired
    private ProductoRepository productoRepository;
    
    @Autowired
    private MesaRepository mesaRepository;
    
    @Autowired
    private CategoriaRepository categoriaRepository;
    
    @Autowired
    private PedidoRepository pedidoRepository;
    
    @Autowired
    private CuadreCajaRepository cuadreCajaRepository;
    
    @Autowired
    private IngredienteRepository ingredienteRepository;

    // ==================================================
    // CACHE DE PRODUCTOS
    // ==================================================
    
    /**
     * Obtiene todos los productos con cache
     * Cache: 5 minutos (productos no cambian frecuentemente)
     */
    @Cacheable(value = "productos", key = "'all'")
    public List<Producto> getAllProductosCached() {
        System.out.println("🔄 Cargando todos los productos desde BD (NO desde cache)");
        return productoRepository.findAll();
    }
    
    /**
     * Obtiene productos por categoría con cache
     */
    @Cacheable(value = "productos", key = "'categoria_' + #categoriaId")
    public List<Producto> getProductosByCategoryCached(String categoriaId) {
        System.out.println("🔄 Cargando productos de categoría " + categoriaId + " desde BD");
        return productoRepository.findByCategoriaId(categoriaId);
    }
    
    /**
     * Obtiene un producto por ID con cache
     */
    @Cacheable(value = "productos", key = "'producto_' + #id")
    public Optional<Producto> getProductoByIdCached(String id) {
        System.out.println("🔄 Cargando producto ID: " + id + " desde BD");
        return productoRepository.findById(id);
    }
    
    /**
     * Invalida cache de productos cuando se actualiza uno
     */
    @CacheEvict(value = "productos", allEntries = true)
    public Producto updateProducto(Producto producto) {
        System.out.println("🗑️ Invalidando cache de productos por actualización");
        return productoRepository.save(producto);
    }

    // ==================================================
    // CACHE DE MESAS
    // ==================================================
    
    /**
     * Obtiene todas las mesas con cache
     * Cache: 2 minutos (estado cambia constantemente)
     */
    @Cacheable(value = "mesas", key = "'all'")
    public List<Mesa> getAllMesasCached() {
        System.out.println("🔄 Cargando todas las mesas desde BD");
        return mesaRepository.findAll();
    }
    
    /**
     * Obtiene mesa por nombre con cache
     */
    @Cacheable(value = "mesas", key = "'mesa_' + #nombre")
    public Mesa getMesaByNombreCached(String nombre) {
        System.out.println("🔄 Cargando mesa " + nombre + " desde BD");
        return mesaRepository.findByNombre(nombre);
    }
    
    /**
     * Actualiza una mesa e invalida su cache específico
     */
    @CachePut(value = "mesas", key = "'mesa_' + #mesa.nombre")
    @CacheEvict(value = "mesas", key = "'all'")
    public Mesa updateMesa(Mesa mesa) {
        System.out.println("🔄 Actualizando cache de mesa: " + mesa.getNombre());
        return mesaRepository.save(mesa);
    }

    // ==================================================
    // CACHE DE CATEGORÍAS
    // ==================================================
    
    /**
     * Obtiene todas las categorías con cache
     * Cache: 10 minutos (categorías cambian muy poco)
     */
    @Cacheable(value = "categorias", key = "'all'")
    public List<Categoria> getAllCategoriasCached() {
        System.out.println("🔄 Cargando todas las categorías desde BD");
        return categoriaRepository.findAll();
    }

    // ==================================================
    // CACHE DE CUADRES DE CAJA
    // ==================================================
    
    /**
     * Obtiene cuadres de caja abiertos (consulta muy frecuente)
     * Cache: 1 minuto (cambia cuando se abre/cierra caja)
     */
    @Cacheable(value = "cuadres-activos", key = "'cajas_abiertas'")
    public List<CuadreCaja> getCajasAbiertasCached() {
        System.out.println("🔄 Cargando cajas abiertas desde BD");
        return cuadreCajaRepository.findByCerradaFalse();
    }
    
    /**
     * Invalida cache de cajas abiertas cuando se abre/cierra una caja
     */
    @CacheEvict(value = "cuadres-activos", key = "'cajas_abiertas'")
    public CuadreCaja updateCuadreCaja(CuadreCaja cuadre) {
        System.out.println("🗑️ Invalidando cache de cajas abiertas");
        return cuadreCajaRepository.save(cuadre);
    }

    // ==================================================
    // CACHE DE PEDIDOS ACTIVOS
    // ==================================================
    
    /**
     * Obtiene pedidos activos por mesa con cache
     * Cache: 30 segundos (cambia constantemente en horario de servicio)
     */
    @Cacheable(value = "pedidos-activos", key = "'mesa_' + #mesa")
    public List<Pedido> getPedidosActivosByMesaCached(String mesa) {
        System.out.println("🔄 Cargando pedidos activos de mesa " + mesa + " desde BD");
        return pedidoRepository.findPedidosActivosByMesa(mesa);
    }
    
    /**
     * Invalida cache de pedidos activos cuando se actualiza un pedido
     */
    @CacheEvict(value = "pedidos-activos", key = "'mesa_' + #pedido.mesa")
    public Pedido updatePedidoAndInvalidateCache(Pedido pedido) {
        System.out.println("🗑️ Invalidando cache de pedidos activos para mesa: " + pedido.getMesa());
        return pedidoRepository.save(pedido);
    }

    // ==================================================
    // CACHE DE INGREDIENTES
    // ==================================================
    
    /**
     * Obtiene todos los ingredientes con cache
     * Cache: 10 minutos (ingredientes cambian poco)
     */
    @Cacheable(value = "ingredientes", key = "'all'")
    public List<Ingrediente> getAllIngredientesCached() {
        System.out.println("🔄 Cargando todos los ingredientes desde BD");
        return ingredienteRepository.findAll();
    }

    // ==================================================
    // MÉTODOS DE LIMPIEZA DE CACHE
    // ==================================================
    
    /**
     * Limpia todo el cache de productos
     */
    @CacheEvict(value = "productos", allEntries = true)
    public void clearProductosCache() {
        System.out.println("🗑️ Limpiando cache completo de productos");
    }
    
    /**
     * Limpia todo el cache de mesas
     */
    @CacheEvict(value = "mesas", allEntries = true)
    public void clearMesasCache() {
        System.out.println("🗑️ Limpiando cache completo de mesas");
    }
    
    /**
     * Limpia todos los caches
     */
    @CacheEvict(value = {"productos", "mesas", "categorias", "cuadres-activos", "pedidos-activos", "ingredientes"}, allEntries = true)
    public void clearAllCaches() {
        System.out.println("🗑️ Limpiando TODOS los caches del sistema");
    }
    
    /**
     * Obtiene estadísticas básicas del sistema (para dashboard)
     * Cache: 5 minutos (datos estadísticos no críticos)
     */
    @Cacheable(value = "reportes-ventas", key = "'stats_dashboard'")
    public String getSystemStatsCached() {
        System.out.println("🔄 Calculando estadísticas del sistema desde BD");
        long totalPedidos = pedidoRepository.count();
        long totalMesas = mesaRepository.count();
        long totalProductos = productoRepository.count();
        long cajasAbiertas = cuadreCajaRepository.findByCerradaFalse().size();
        
        return String.format("Pedidos:%d|Mesas:%d|Productos:%d|CajasAbiertas:%d", 
                           totalPedidos, totalMesas, totalProductos, cajasAbiertas);
    }
    
    /**
     * Precarga caches importantes al iniciar la aplicación
     */
    public void preloadImportantCaches() {
        System.out.println("🚀 Precargando caches importantes...");
        
        // Precargar productos (muy consultados)
        getAllProductosCached();
        
        // Precargar mesas (consultadas constantemente)
        getAllMesasCached();
        
        // Precargar categorías (usadas en menús)
        getAllCategoriasCached();
        
        // Precargar cajas abiertas (consulta crítica)
        getCajasAbiertasCached();
        
        System.out.println("✅ Caches importantes precargados");
    }
}
