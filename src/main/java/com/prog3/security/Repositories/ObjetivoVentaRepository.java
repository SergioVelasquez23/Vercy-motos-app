package com.prog3.security.Repositories;

import com.prog3.security.Entities.ObjetivoVenta;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ObjetivoVentaRepository extends MongoRepository<ObjetivoVenta, String> {

    ObjetivoVenta findByPeriodo(String periodo);
}
