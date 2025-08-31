package com.prog3.security.Repositories;

import com.prog3.security.Models.IngresoCaja;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IngresoCajaRepository extends MongoRepository<IngresoCaja, String> {
    List<IngresoCaja> findByFechaIngresoBetween(LocalDateTime inicio, LocalDateTime fin);
    List<IngresoCaja> findByCuadreCajaId(String cuadreCajaId);
}
