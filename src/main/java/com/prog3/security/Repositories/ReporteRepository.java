package com.prog3.security.Repositories;

import com.prog3.security.Models.Reporte;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReporteRepository extends MongoRepository<Reporte, String> {

    List<Reporte> findByFechaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    List<Reporte> findByFechaGreaterThanEqual(LocalDateTime fecha);
}
