package com.prog3.security.Repositories;

import com.prog3.security.Models.Factura;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FacturaRepository extends MongoRepository<Factura, String> {

    // Buscar por número de factura
    Factura findByNumero(String numero);

    // Buscar por NIT
    List<Factura> findByNit(String nit);

    // Buscar por teléfono del cliente
    List<Factura> findByClienteTelefono(String clienteTelefono);

    // Buscar por dirección del cliente
    List<Factura> findByClienteDireccion(String clienteDireccion);

    // Buscar por medio de pago
    List<Factura> findByMedioPago(String medioPago);

    // Buscar por forma de pago
    List<Factura> findByFormaPago(String formaPago);

    // Buscar por quien atendió
    List<Factura> findByAtendidoPor(String atendidoPor);

    // Buscar por rango de fechas
    List<Factura> findByFechaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    // Buscar facturas de hoy
    List<Factura> findByFechaGreaterThanEqual(LocalDateTime fecha);

    // Buscar por rango de totales
    List<Factura> findByTotalBetween(double totalMin, double totalMax);

    // Facturas pendientes de pago (simulado con medio de pago null o vacío)
    @Query("{ 'medioPago': { $in: [null, ''] } }")
    List<Factura> findFacturasPendientesPago();

    // Ventas del día
    @Query("{ 'fecha': { $gte: ?0, $lt: ?1 } }")
    List<Factura> findVentasDelDia(LocalDateTime inicioDelDia, LocalDateTime finDelDia);

    // Total de ventas por fecha
    @Query("{ 'fecha': { $gte: ?0, $lt: ?1 } }")
    List<Factura> findForTotalVentas(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    // Verificar si existe número de factura
    boolean existsByNumero(String numero);

    // Obtener última factura para generar consecutivo
    @Query(value = "{}", sort = "{ 'numero': -1 }")
    Factura findTopByOrderByNumeroDesc();
}
