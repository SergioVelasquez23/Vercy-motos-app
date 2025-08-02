package com.prog3.security.Repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.prog3.security.Models.CuadreCaja;

public interface CuadreCajaRepository extends MongoRepository<CuadreCaja, String> {

    // Buscar por responsable
    List<CuadreCaja> findByResponsable(String responsable);

    // Buscar por estado
    List<CuadreCaja> findByEstado(String estado);

    // Buscar por rango de fechas de apertura
    List<CuadreCaja> findByFechaAperturaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    // Buscar cuadres que tengan diferencias mayores que la tolerancia
    @Query("{'cuadrado': false}")
    List<CuadreCaja> findByNoCuadrados();

    // Buscar cuadres con diferencias que excedan un cierto monto
    @Query("{'diferencia': {$gt: ?0}}")
    List<CuadreCaja> findByDiferenciaExcede(double montoLimite);

    // Buscar cuadres por fecha de apertura (solo de hoy)
    @Query("{'fechaApertura': {$gte: ?0}}")
    List<CuadreCaja> findByFechaAperturaHoy(LocalDateTime inicioHoy);

    // Buscar cuadres aprobados por una persona específica
    List<CuadreCaja> findByAprobadoPor(String aprobador);
}
