package com.prog3.security.Repositories;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.prog3.security.Models.Gasto;

public interface GastoRepository extends MongoRepository<Gasto, String> {

    List<Gasto> findByCuadreCajaId(String cuadreCajaId);

    List<Gasto> findByTipoGastoId(String tipoGastoId);

    List<Gasto> findByResponsable(String responsable);

    List<Gasto> findByFechaGastoBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    List<Gasto> findByEstado(String estado);
}
