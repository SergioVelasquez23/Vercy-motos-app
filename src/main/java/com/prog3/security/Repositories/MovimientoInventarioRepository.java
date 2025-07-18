package com.prog3.security.Repositories;

import com.prog3.security.Models.MovimientoInventario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimientoInventarioRepository extends MongoRepository<MovimientoInventario, String> {

    // Buscar movimientos por inventario
    List<MovimientoInventario> findByInventarioId(String inventarioId);

    // Buscar movimientos por producto
    List<MovimientoInventario> findByProductoId(String productoId);

    // Buscar por tipo de movimiento
    List<MovimientoInventario> findByTipoMovimiento(String tipoMovimiento);

    // Buscar por motivo
    List<MovimientoInventario> findByMotivo(String motivo);

    // Buscar por responsable
    List<MovimientoInventario> findByResponsable(String responsable);

    // Buscar por referencia (ej: ID de pedido)
    List<MovimientoInventario> findByReferencia(String referencia);

    // Buscar por proveedor
    List<MovimientoInventario> findByProveedor(String proveedor);

    // Buscar por rango de fechas
    List<MovimientoInventario> findByFechaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    // Buscar movimientos de hoy
    List<MovimientoInventario> findByFechaGreaterThanEqual(LocalDateTime fecha);

    // Movimientos por producto y tipo
    List<MovimientoInventario> findByProductoIdAndTipoMovimiento(String productoId, String tipoMovimiento);

    // Entradas de inventario (movimientos positivos)
    @Query("{ 'cantidadMovimiento': { $gt: 0 } }")
    List<MovimientoInventario> findEntradas();

    // Salidas de inventario (movimientos negativos)
    @Query("{ 'cantidadMovimiento': { $lt: 0 } }")
    List<MovimientoInventario> findSalidas();

    // Obtener últimos movimientos por producto
    @Query("{ 'productoId': ?0 }")
    List<MovimientoInventario> findByProductoIdOrderByFechaDesc(String productoId);
}
