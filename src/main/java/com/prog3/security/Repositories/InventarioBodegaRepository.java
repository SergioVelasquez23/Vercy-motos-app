package com.prog3.security.Repositories;

import com.prog3.security.Models.InventarioBodega;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventarioBodegaRepository extends MongoRepository<InventarioBodega, String> {

    // Buscar por bodega
    List<InventarioBodega> findByBodegaId(String bodegaId);

    // Buscar por item
    List<InventarioBodega> findByItemId(String itemId);

    // Buscar por bodega e item
    Optional<InventarioBodega> findByBodegaIdAndItemId(String bodegaId, String itemId);

    // Buscar items con stock bajo en una bodega
    @Query("{ 'bodegaId': ?0, $expr: { $lte: ['$stockActual', '$stockMinimo'] } }")
    List<InventarioBodega> findStockBajoByBodega(String bodegaId);

    // Buscar por tipo de item en una bodega
    List<InventarioBodega> findByBodegaIdAndTipoItem(String bodegaId, String tipoItem);

    // Obtener stock total de un item en todas las bodegas
    List<InventarioBodega> findByItemIdAndTipoItem(String itemId, String tipoItem);

    // Verificar si existe inventario
    boolean existsByBodegaIdAndItemId(String bodegaId, String itemId);
}
