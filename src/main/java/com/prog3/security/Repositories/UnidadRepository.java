package com.prog3.security.Repositories;

import com.prog3.security.Models.Unidad;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnidadRepository extends MongoRepository<Unidad, String> {
    boolean existsByNombre(String nombre);
}
