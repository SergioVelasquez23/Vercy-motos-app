package com.prog3.security.Repositories;

import com.prog3.security.Models.TransferenciaBodega;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransferenciaBodegaRepository
        extends MongoRepository<TransferenciaBodega, String> {

    // Buscar por bodega origen
    List<TransferenciaBodega> findByBodegaOrigenId(String bodegaOrigenId);

    // Buscar por bodega destino
    List<TransferenciaBodega> findByBodegaDestinoId(String bodegaDestinoId);

    // Buscar por estado
    List<TransferenciaBodega> findByEstado(String estado);

    // Buscar por usuario
    List<TransferenciaBodega> findByUsuarioId(String usuarioId);

    // Buscar transferencias pendientes de una bodega
    List<TransferenciaBodega> findByBodegaOrigenIdAndEstado(String bodegaOrigenId, String estado);

    // Buscar transferencias en un rango de fechas
    @Query("{ 'fechaSolicitud': { $gte: ?0, $lte: ?1 } }")
    List<TransferenciaBodega> findByFechaRange(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    // Buscar transferencias entre dos bodegas
    List<TransferenciaBodega> findByBodegaOrigenIdAndBodegaDestinoId(String bodegaOrigenId,
            String bodegaDestinoId);
}
