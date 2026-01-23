package com.prog3.security.Repositories;

import com.prog3.security.Models.Categoria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends MongoRepository<Categoria, String> {

    // Buscar por nombre
    Optional<Categoria> findByNombre(String nombre);

    // Buscar por nombre (case insensitive)
    @Query("{ 'nombre': { $regex: ?0, $options: 'i' } }")
    List<Categoria> findByNombreContaining(String nombre);

    // Buscar solo categorías activas
    List<Categoria> findByActivoTrue();

    // Buscar categorías ordenadas por campo orden
    List<Categoria> findByActivoTrueOrderByOrdenAsc();

    // Verificar si existe por nombre
    boolean existsByNombre(String nombre);

    // Buscar por nombre ignorando mayúsculas y excluyendo un ID
    @Query("{ 'nombre': { $regex: ?0, $options: 'i' }, '_id': { $ne: ?1 } }")
    List<Categoria> findByNombreIgnoreCaseAndIdNot(String nombre, String id);
}
