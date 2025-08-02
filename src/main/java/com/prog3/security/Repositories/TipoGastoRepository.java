package com.prog3.security.Repositories;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.prog3.security.Models.TipoGasto;

public interface TipoGastoRepository extends MongoRepository<TipoGasto, String> {

    List<TipoGasto> findByNombreContainingIgnoreCase(String nombre);

    List<TipoGasto> findByActivoTrue();
}
