package com.prog3.security.Repositories;

import com.prog3.security.Models.Bodega;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BodegaRepository extends MongoRepository<Bodega, String> {

    // Buscar por código
    Optional<Bodega> findByCodigo(String codigo);

    // Buscar por nombre
    List<Bodega> findByNombreContainingIgnoreCase(String nombre);

    // Buscar bodegas activas
    List<Bodega> findByActiva(boolean activa);

    // Buscar por tipo
    List<Bodega> findByTipo(String tipo);

    // Buscar por responsable
    List<Bodega> findByResponsable(String responsable);

    // Verificar si existe por código
    boolean existsByCodigo(String codigo);

    // Buscar bodega principal
    @Query("{ 'tipo': 'PRINCIPAL', 'activa': true }")
    Optional<Bodega> findBodegaPrincipal();
}
