package com.prog3.security.Repositories;

import com.prog3.security.Models.Mesa;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MesaRepository extends MongoRepository<Mesa, String> {

    // Buscar mesa por nombre
    Mesa findByNombre(String nombre);

    // Buscar mesas ocupadas o libres
    List<Mesa> findByOcupada(boolean ocupada);

    // Buscar mesas libres
    List<Mesa> findByOcupadaFalse();

    // Buscar mesas ocupadas
    List<Mesa> findByOcupadaTrue();

    // Verificar si existe una mesa con ese nombre
    boolean existsByNombre(String nombre);

    // Buscar mesas por rango de total
    List<Mesa> findByTotalBetween(double totalMin, double totalMax);
}
