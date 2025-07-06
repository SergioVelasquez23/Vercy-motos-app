package com.prog3.security.Repositories;

import com.prog3.security.Models.Categoria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategoriaRepository extends MongoRepository<Categoria, String> {

    // Buscar categoría por nombre
    Categoria findByNombre(String nombre);

    // Buscar categorías que contengan un texto en el nombre (case insensitive)
    List<Categoria> findByNombreContainingIgnoreCase(String nombre);

    // Verificar si existe una categoría con ese nombre
    boolean existsByNombre(String nombre);
}
