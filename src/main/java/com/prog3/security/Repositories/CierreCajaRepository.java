package com.prog3.security.Repositories;

import com.prog3.security.Models.CierreCaja;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CierreCajaRepository extends MongoRepository<CierreCaja, String> {

    // Buscar el último cierre
    Optional<CierreCaja> findTopByOrderByFechaCierreDesc();

    // Buscar cierres por rango de fechas
    List<CierreCaja> findByFechaCierreBetween(LocalDateTime inicio, LocalDateTime fin);

    // Buscar cierres por estado
    List<CierreCaja> findByEstado(String estado);

    // Buscar cierres por responsable
    List<CierreCaja> findByResponsable(String responsable);

    // Buscar últimos N cierres
    @Query(value = "{}", sort = "{ 'fechaCierre' : -1 }")
    List<CierreCaja> findUltimosCierres(org.springframework.data.domain.Pageable pageable);

    // Verificar si ya existe un cierre para un período específico
    Optional<CierreCaja> findByFechaInicioAndFechaFin(LocalDateTime fechaInicio, LocalDateTime fechaFin);
}
