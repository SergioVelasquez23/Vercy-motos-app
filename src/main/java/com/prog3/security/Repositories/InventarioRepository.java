package com.prog3.security.Repositories;

import com.prog3.security.Models.Inventario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventarioRepository extends MongoRepository<Inventario, String> {

    // Buscar por producto
    Inventario findByProductoId(String productoId);

    // Buscar por nombre de producto
    List<Inventario> findByProductoNombreContainingIgnoreCase(String nombre);

    // Buscar por estado
    List<Inventario> findByEstado(String estado);

    // Buscar por categoría
    List<Inventario> findByCategoria(String categoria);

    // Buscar por ubicación
    List<Inventario> findByUbicacion(String ubicacion);

    // Buscar por proveedor
    List<Inventario> findByProveedor(String proveedor);

    // Productos con stock bajo (cantidad actual <= cantidad mínima)
    @Query("{ 'cantidadActual': { $lte: '$cantidadMinima' }, 'estado': 'activo' }")
    List<Inventario> findProductosConStockBajo();

    // Productos agotados
    @Query("{ 'cantidadActual': 0, 'estado': 'activo' }")
    List<Inventario> findProductosAgotados();

    // Productos próximos a vencer
    @Query("{ 'fechaVencimiento': { $lte: ?0 }, 'estado': 'activo' }")
    List<Inventario> findProductosProximosAVencer(LocalDateTime fechaLimite);

    // Buscar por rango de fechas de actualización
    List<Inventario> findByFechaUltimaActualizacionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    // Productos por rango de costo
    List<Inventario> findByCostoTotalBetween(double costoMin, double costoMax);

    // Verificar si existe inventario para un producto
    boolean existsByProductoId(String productoId);

    // Obtener valor total del inventario
    @Query(value = "{}", fields = "{ 'costoTotal': 1 }")
    List<Inventario> findAllCostoTotal();
}
