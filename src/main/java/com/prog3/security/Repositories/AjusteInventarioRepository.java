package com.prog3.security.Repositories;

import com.prog3.security.Models.AjusteInventario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AjusteInventarioRepository extends MongoRepository<AjusteInventario, String> {

    // Buscar por bodega
    List<AjusteInventario> findByBodegaId(String bodegaId);

    // Buscar por usuario
    List<AjusteInventario> findByUsuarioId(String usuarioId);

    // Buscar por tipo
    List<AjusteInventario> findByTipo(String tipo);

    // Buscar por estado
    List<AjusteInventario> findByEstado(String estado);

    // Buscar ajustes pendientes
    @Query("{ 'estado': 'PENDIENTE', 'requiereAprobacion': true }")
    List<AjusteInventario> findAjustesPendientes();

    // Buscar por rango de fechas
    @Query("{ 'fecha': { $gte: ?0, $lte: ?1 } }")
    List<AjusteInventario> findByFechaRange(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    // Buscar por bodega y tipo
    List<AjusteInventario> findByBodegaIdAndTipo(String bodegaId, String tipo);

    // Buscar ajustes de un item específico
    @Query("{ 'items.itemId': ?0 }")
    List<AjusteInventario> findByItemId(String itemId);
}
