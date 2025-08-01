package com.prog3.security.Repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

import com.prog3.security.Models.Ingrediente;

@Repository
public interface IngredienteRepository extends MongoRepository<Ingrediente, String> {

    @Query("{ 'nombre': ?0 }")
    Ingrediente findByNombre(String nombre);

    @Query("{ 'codigo': ?0 }")
    Ingrediente findByCodigo(String codigo);

    @Query("{ 'categoria': ?0 }")
    List<Ingrediente> findByCategoria(String categoria);

    @Query("{ 'nombre': { $regex: ?0, $options: 'i' } }")
    List<Ingrediente> findByNombreContainingIgnoreCase(String nombre);

    @Query("{ 'estado': ?0 }")
    List<Ingrediente> findByEstado(String estado);

    @Query("{ 'stockActual': { $lte: 'stockMinimo' } }")
    List<Ingrediente> findByStockBajo();

    boolean existsByNombre(String nombre);

    boolean existsByCodigo(String codigo);
}
