package com.prog3.security.Repositories;

import com.prog3.security.Models.Deuda;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DeudaRepository extends MongoRepository<Deuda, String> {

    // Buscar por pedido
    Deuda findByPedidoId(String pedidoId);

    // Buscar por mesa
    List<Deuda> findByMesaId(String mesaId);

    // Buscar deudas activas
    List<Deuda> findByActivaTrue();

    // Buscar deudas inactivas
    List<Deuda> findByActivaFalse();

    // Buscar por estado y ordenar por fecha
    List<Deuda> findByActivaTrueOrderByFechaCreacionDesc();

    // Buscar deudas vencidas
    @Query("{ 'activa': true, 'fechaVencimiento': { $lt: ?0 } }")
    List<Deuda> findDeudasVencidas(LocalDateTime fechaActual);

    // Buscar por rango de fechas de creación
    List<Deuda> findByFechaCreacionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    // Buscar por cliente
    List<Deuda> findByClienteInfoContainingIgnoreCase(String clienteInfo);

    // Buscar por mesa y estado activo
    List<Deuda> findByMesaIdAndActivaTrue(String mesaId);

    // Buscar por usuario que creó la deuda
    List<Deuda> findByCreadaPor(String creadaPor);

    // Buscar deudas con monto mayor a un valor
    @Query("{ 'montoDeuda': { $gt: ?0 }, 'activa': true }")
    List<Deuda> findDeudasMayoresA(double monto);

    // Obtener total de deudas activas
    @Query(value = "{ 'activa': true }", fields = "{ 'montoDeuda': 1 }")
    List<Deuda> findActivasForSum();

    // Contar deudas activas
    long countByActivaTrue();
    
    // Contar deudas inactivas  
    long countByActivaFalse();

    // Contar deudas vencidas
    @Query(value = "{ 'activa': true, 'fechaVencimiento': { $lt: ?0 } }", count = true)
    long countDeudasVencidas(LocalDateTime fechaActual);

    // Buscar por mesa nombre
    List<Deuda> findByMesaNombreContainingIgnoreCase(String mesaNombre);

    // Buscar las más recientes
    @Query(value = "{}", sort = "{ fechaCreacion : -1 }")
    List<Deuda> findTopByOrderByFechaCreacionDesc();

    // Buscar por rango de montos
    @Query("{ 'montoTotal': { $gte: ?0, $lte: ?1 } }")
    List<Deuda> findByMontoTotalBetween(double montoMin, double montoMax);
}