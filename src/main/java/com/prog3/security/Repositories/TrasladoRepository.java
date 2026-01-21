package com.prog3.security.Repositories;

import com.prog3.security.Models.Traslado;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TrasladoRepository extends MongoRepository<Traslado, String> {

    // Buscar traslados por estado
    List<Traslado> findByEstado(String estado);

    // Buscar traslados por bodega de origen
    List<Traslado> findByOrigenBodegaId(String origenBodegaId);

    // Buscar traslados por bodega de destino
    List<Traslado> findByDestinoBodegaId(String destinoBodegaId);

    // Buscar traslados por producto
    List<Traslado> findByProductoId(String productoId);

    // Buscar traslados por solicitante
    List<Traslado> findBySolicitante(String solicitante);

    // Obtener el último traslado para generar el número consecutivo
    Traslado findTopByOrderByFechaCreacionDesc();
}
