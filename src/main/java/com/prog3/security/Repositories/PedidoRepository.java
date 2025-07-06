package com.prog3.security.Repositories;

import com.prog3.security.Models.Pedido;
import org.springframework.data.mongodb.repository.MongoRepository;
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
}
