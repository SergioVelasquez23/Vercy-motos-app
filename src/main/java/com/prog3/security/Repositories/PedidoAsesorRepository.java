package com.prog3.security.Repositories;

import com.prog3.security.Models.PedidoAsesor;
import com.prog3.security.Models.PedidoAsesor.EstadoPedidoAsesor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface PedidoAsesorRepository extends MongoRepository<PedidoAsesor, String> {

    // Buscar por estado
    List<PedidoAsesor> findByEstado(EstadoPedidoAsesor estado);

    // Buscar por asesor
    List<PedidoAsesor> findByAsesorId(String asesorId);

    // Buscar por asesor y estado
    List<PedidoAsesor> findByAsesorIdAndEstado(String asesorId, EstadoPedidoAsesor estado);

    // Buscar por cliente
    List<PedidoAsesor> findByClienteId(String clienteId);

    // Buscar por nombre de cliente (búsqueda parcial)
    @Query("{ 'clienteNombre': { $regex: ?0, $options: 'i' } }")
    List<PedidoAsesor> findByClienteNombreContaining(String clienteNombre);

    // Buscar pedidos facturados
    List<PedidoAsesor> findByFacturado(boolean facturado);

    // Buscar por factura ID
    PedidoAsesor findByFacturaId(String facturaId);

    // Buscar por rango de fechas
    List<PedidoAsesor> findByFechaCreacionBetween(LocalDateTime inicio, LocalDateTime fin);

    // Buscar por rango de fechas y estado
    List<PedidoAsesor> findByFechaCreacionBetweenAndEstado(LocalDateTime inicio, LocalDateTime fin,
            EstadoPedidoAsesor estado);

    // Buscar por asesor en un rango de fechas
    List<PedidoAsesor> findByAsesorIdAndFechaCreacionBetween(String asesorId, LocalDateTime inicio,
            LocalDateTime fin);

    // Contar pedidos por estado
    long countByEstado(EstadoPedidoAsesor estado);

    // Contar pedidos por asesor
    long countByAsesorId(String asesorId);

    // Obtener todos ordenados por fecha de creación descendente
    List<PedidoAsesor> findAllByOrderByFechaCreacionDesc();

    // Obtener por estado ordenados por fecha
    List<PedidoAsesor> findByEstadoOrderByFechaCreacionDesc(EstadoPedidoAsesor estado);
}
