package com.prog3.security.Repositories;

import com.prog3.security.Models.PagoDeuda;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PagoDeudaRepository extends MongoRepository<PagoDeuda, String> {

    // Buscar pagos por deuda
    List<PagoDeuda> findByDeudaId(String deudaId);

    // Buscar pagos por usuario que recibió
    List<PagoDeuda> findByRecibidoPor(String recibidoPor);

    // Buscar pagos por método de pago
    List<PagoDeuda> findByMetodoPago(String metodoPago);

    // Buscar pagos por rango de fechas
    List<PagoDeuda> findByFechaPagoBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    // Buscar pagos de una deuda ordenados por fecha
    List<PagoDeuda> findByDeudaIdOrderByFechaPagoDesc(String deudaId);

    // Buscar los pagos más recientes
    @Query(value = "{}", sort = "{ fechaPago : -1 }")
    List<PagoDeuda> findTopByOrderByFechaPagoDesc();

    // Buscar pagos por monto mayor a un valor
    @Query("{ 'monto': { $gt: ?0 } }")
    List<PagoDeuda> findPagosMayoresA(double monto);

    // Obtener suma de pagos de una deuda
    @Query(value = "{ 'deudaId': ?0 }", fields = "{ 'monto': 1 }")
    List<PagoDeuda> findByDeudaIdForSum(String deudaId);

    // Contar pagos por deuda
    long countByDeudaId(String deudaId);

    // Buscar pagos por rango de fechas y método
    List<PagoDeuda> findByFechaPagoBetweenAndMetodoPago(LocalDateTime fechaInicio, LocalDateTime fechaFin, String metodoPago);
}