package com.prog3.security.Repositories;

import com.prog3.security.Models.Pedido;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PedidoRepository extends MongoRepository<Pedido, String> {

    // Buscar pedidos por tipo
    List<Pedido> findByTipo(String tipo);

    // Buscar pedidos por mesa
    List<Pedido> findByMesa(String mesa);

    // Buscar pedidos por cliente
    List<Pedido> findByCliente(String cliente);

    // Buscar pedidos por mesero
    List<Pedido> findByMesero(String mesero);

    // Buscar pedidos por estado
    List<Pedido> findByEstado(String estado);

    // Buscar pedidos por rango de fechas
    List<Pedido> findByFechaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    // Buscar pedidos de hoy
    List<Pedido> findByFechaGreaterThanEqual(LocalDateTime fecha);

    // Buscar pedidos por plataforma (para RT)
    List<Pedido> findByPlataforma(String plataforma);

    // Buscar pedidos por guardadoPor
    List<Pedido> findByGuardadoPor(String guardadoPor);

    // Buscar pedidos por pedidoPor
    List<Pedido> findByPedidoPor(String pedidoPor);

    // Buscar pedidos por tipo, estado y fecha
    List<Pedido> findByTipoAndEstadoAndFechaGreaterThanEqual(String tipo, String estado, LocalDateTime fecha);

    // Calcular total de ventas por rango de fechas (solo pedidos cancelados que representan ventas completadas)
    @Query("{ 'fecha': { $gte: ?0, $lte: ?1 }, 'estado': 'cancelado' }")
    List<Pedido> findPedidosForTotalVentas(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    // Eliminar todos los pedidos con estado específico
    @Query("{ 'estado': ?0 }")
    void deleteAllByEstado(String estado);

    // Eliminar pedidos por rango de fechas
    void deleteByFechaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
}
