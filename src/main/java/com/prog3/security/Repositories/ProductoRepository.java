package com.prog3.security.Repositories;

import com.prog3.security.Models.Producto;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductoRepository extends MongoRepository<Producto, String> {

    // Buscar productos por categoría
    List<Producto> findByCategoriaId(String categoriaId);

    // Buscar productos por nombre (case insensitive)
    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    // Buscar productos por estado
    List<Producto> findByEstado(String estado);

    // Buscar productos en un rango de precios
    List<Producto> findByPrecioBetween(double precioMin, double precioMax);

    // Buscar productos que tengan variantes
    List<Producto> findByTieneVariantes(boolean tieneVariantes);

    // Verificar si existe un producto con ese nombre
    boolean existsByNombre(String nombre);
}
