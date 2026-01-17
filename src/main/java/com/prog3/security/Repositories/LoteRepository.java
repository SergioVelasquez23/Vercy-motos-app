package com.prog3.security.Repositories;

import com.prog3.security.Models.Lote;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoteRepository extends MongoRepository<Lote, String> {

    // Buscar por código
    Optional<Lote> findByCodigo(String codigo);

    // Buscar por item
    List<Lote> findByItemId(String itemId);

    // Buscar lotes activos de un item
    List<Lote> findByItemIdAndEstado(String itemId, String estado);

    // Buscar por bodega
    List<Lote> findByBodegaId(String bodegaId);

    // Buscar lotes con stock disponible de un item
    @Query("{ 'itemId': ?0, 'estado': 'ACTIVO', 'cantidadActual': { $gt: 0 } }")
    List<Lote> findLotesDisponiblesbyItem(String itemId);

    // Buscar lotes próximos a vencer (30 días o menos)
    @Query("{ 'estado': 'ACTIVO', 'fechaVencimiento': { $gte: ?0, $lte: ?1 } }")
    List<Lote> findLotesPorVencer(LocalDate fechaActual, LocalDate fechaLimite);

    // Buscar lotes vencidos
    @Query("{ 'estado': 'ACTIVO', 'fechaVencimiento': { $lt: ?0 } }")
    List<Lote> findLotesVencidos(LocalDate fechaActual);

    // Buscar lotes de un item ordenados por fecha de vencimiento (FIFO)
    @Query(value = "{ 'itemId': ?0, 'estado': 'ACTIVO', 'cantidadActual': { $gt: 0 } }",
            sort = "{ 'fechaVencimiento': 1 }")
    List<Lote> findLotesByItemIdFIFO(String itemId);

    // Verificar si existe código
    boolean existsByCodigo(String codigo);

    // Buscar lotes de un proveedor
    List<Lote> findByProveedor(String proveedor);

    // Buscar lotes de una factura
    List<Lote> findByFactura(String factura);
}
