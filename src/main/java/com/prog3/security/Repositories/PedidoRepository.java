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

    // Buscar pedidos por mesa y rango de fechas (para recuperar pedidos recién creados)
    List<Pedido> findByMesaAndFechaBetween(String mesa, LocalDateTime fechaInicio, LocalDateTime fechaFin);

    // Buscar pedidos por cliente
    List<Pedido> findByCliente(String cliente);

    // Buscar pedidos por mesero
    List<Pedido> findByMesero(String mesero);

    // Buscar pedidos por estado
    List<Pedido> findByEstado(String estado);

    // Buscar pedidos por estado y fecha mayor o igual
    List<Pedido> findByFechaGreaterThanEqualAndEstado(LocalDateTime fecha, String estado);

    // Buscar pedidos por rango de fechas y estado
    List<Pedido> findByFechaBetweenAndEstado(LocalDateTime fechaInicio, LocalDateTime fechaFin, String estado);

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

    // Calcular total de ventas por rango de fechas (solo pedidos pagados que representan ventas completadas)
    @Query("{ 'fecha': { $gte: ?0, $lte: ?1 }, 'estado': 'pagado' }")
    List<Pedido> findPedidosForTotalVentas(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    // Buscar pedidos por forma de pago y fecha
    @Query("{ 'formaPago': ?0, 'fecha': { $gte: ?1 }, 'estado': 'pagado' }")
    List<Pedido> findByFormaPagoAndFechaGreaterThanEqual(String formaPago, LocalDateTime fecha);

    // Buscar pedidos pagados por fecha de pago (NUEVO - para cuadres de caja)
    @Query("{ 'fechaPago': { $gte: ?0 }, 'estado': 'pagado' }")
    List<Pedido> findByFechaPagoGreaterThanEqualAndEstado(LocalDateTime fechaPago, String estado);

    // Eliminar todos los pedidos con estado específico
    @Query("{ 'estado': ?0 }")
    void deleteAllByEstado(String estado);

    // Eliminar pedidos por rango de fechas
    void deleteByFechaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    // Buscar pedidos por mesa y nombre del pedido (para mesas especiales)
    List<Pedido> findByMesaAndNombrePedido(String mesa, String nombrePedido);

    // Buscar pedidos activos por mesa (para mesas especiales)
    @Query("{ 'mesa': ?0, 'estado': { $in: ['activo', 'pendiente'] } }")
    List<Pedido> findPedidosActivosByMesa(String mesa);

    // Verificar si existe un pedido activo con el nombre especificado en una mesa
    @Query("{ 'mesa': ?0, 'nombrePedido': ?1, 'estado': { $in: ['activo', 'pendiente'] } }")
    List<Pedido> findPedidoActivoByMesaAndNombre(String mesa, String nombrePedido);

    // ===== MÉTODOS PARA CUADRES DE CAJA =====
    // Buscar pedidos pagados por cuadre de caja específico
    List<Pedido> findByCuadreCajaIdAndEstado(String cuadreCajaId, String estado);

    // Buscar todos los pedidos de un cuadre de caja
    List<Pedido> findByCuadreCajaId(String cuadreCajaId);

    // Buscar pedidos pagados sin asignar a ningún cuadre
    @Query("{ 'cuadreCajaId': { $exists: false }, 'estado': 'pagado' }")
    List<Pedido> findPedidosPagadosSinCuadre();

    // Buscar pedidos pagados sin cuadre en un rango de fechas
    @Query("{ 'cuadreCajaId': { $exists: false }, 'estado': 'pagado', 'fechaPago': { $gte: ?0, $lte: ?1 } }")
    List<Pedido> findPedidosPagadosSinCuadreEnRango(LocalDateTime fechaInicio, LocalDateTime fechaFin);
}
