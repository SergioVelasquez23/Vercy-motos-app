package com.prog3.security.Repositories;

import com.prog3.security.Models.PedidoInterno;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PedidoInternoRepository extends MongoRepository<PedidoInterno, String> {

    // Buscar pedidos internos por producto
    List<PedidoInterno> findByProductoId(String productoId);

    // Buscar pedidos internos por quien los guardó
    List<PedidoInterno> findByGuardadoPor(String guardadoPor);

    // Buscar pedidos internos por quien los pidió
    List<PedidoInterno> findByPedidoPor(String pedidoPor);

    // Buscar pedidos internos por estado
    List<PedidoInterno> findByEstado(String estado);

    // Buscar pedidos internos por rango de fechas
    List<PedidoInterno> findByFechaPedidoBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    // Buscar pedidos internos de hoy
    List<PedidoInterno> findByFechaPedidoGreaterThanEqual(LocalDateTime fecha);

    // Buscar pedidos internos pendientes
    List<PedidoInterno> findByEstadoAndFechaPedidoGreaterThanEqual(String estado, LocalDateTime fecha);
}
