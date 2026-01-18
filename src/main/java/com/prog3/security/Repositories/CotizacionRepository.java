package com.prog3.security.Repositories;

import com.prog3.security.Models.Cotizacion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para manejar operaciones de base de datos de Cotizaciones
 */
@Repository
public interface CotizacionRepository extends MongoRepository<Cotizacion, String> {

    // Buscar por número de cotización
    Optional<Cotizacion> findByNumeroCotizacion(String numeroCotizacion);

    // Buscar cotizaciones por cliente
    List<Cotizacion> findByClienteId(String clienteId);
    
    // Buscar cotizaciones por estado
    List<Cotizacion> findByEstado(String estado);
    
    // Buscar cotizaciones por rango de fechas
    List<Cotizacion> findByFechaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    // Buscar cotizaciones por cliente y estado
    List<Cotizacion> findByClienteIdAndEstado(String clienteId, String estado);
    
    // Buscar cotizaciones activas de un cliente
    @Query("{ 'clienteId': ?0, 'estado': 'activa' }")
    List<Cotizacion> findCotizacionesActivasPorCliente(String clienteId);
    
    // Buscar cotizaciones vencidas
    @Query("{ 'fechaVencimiento': { $lt: ?0 }, 'estado': 'activa' }")
    List<Cotizacion> findCotizacionesVencidas(LocalDateTime fechaActual);
    
    // Buscar cotizaciones por creador
    List<Cotizacion> findByCreadoPor(String creadoPor);
    
    // Buscar cotizaciones ordenadas por fecha descendente
    List<Cotizacion> findAllByOrderByFechaDesc();
    
    // Buscar cotizaciones por cliente ordenadas por fecha descendente
    List<Cotizacion> findByClienteIdOrderByFechaDesc(String clienteId);
    
    // Contar cotizaciones por estado
    long countByEstado(String estado);
    
    // Contar cotizaciones de un cliente
    long countByClienteId(String clienteId);
}
