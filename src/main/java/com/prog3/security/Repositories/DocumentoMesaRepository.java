package com.prog3.security.Repositories;

import com.prog3.security.Models.DocumentoMesa;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DocumentoMesaRepository extends MongoRepository<DocumentoMesa, String> {

    // Encontrar documentos por nombre de mesa
    List<DocumentoMesa> findByMesaNombre(String mesaNombre);

    // Encontrar documentos por nombre de mesa y estado de pago
    List<DocumentoMesa> findByMesaNombreAndPagado(String mesaNombre, boolean pagado);

    // Encontrar documentos por vendedor
    List<DocumentoMesa> findByVendedor(String vendedor);

    // Encontrar documentos por rango de fechas
    @Query("{ 'fecha' : { $gte: ?0, $lte: ?1 } }")
    List<DocumentoMesa> findByFechaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    // Encontrar documentos por mesa y rango de fechas
    @Query("{ 'mesaNombre' : ?0, 'fecha' : { $gte: ?1, $lte: ?2 } }")
    List<DocumentoMesa> findByMesaNombreAndFechaBetween(String mesaNombre, LocalDateTime fechaInicio, LocalDateTime fechaFin);

    // Encontrar documentos pendientes de pago
    List<DocumentoMesa> findByPagadoFalse();

    // Encontrar documentos pagados
    List<DocumentoMesa> findByPagadoTrue();

    // Encontrar documentos por número de documento
    DocumentoMesa findByNumeroDocumento(String numeroDocumento);

    // Encontrar documentos que contengan un pedido específico
    @Query("{ 'pedidosIds' : { $in: [?0] } }")
    List<DocumentoMesa> findByPedidoId(String pedidoId);

    // Encontrar documentos por mesa y estado ordenados por fecha descendente
    @Query(value = "{ 'mesaNombre' : ?0, 'pagado' : ?1 }", sort = "{ 'fecha' : -1 }")
    List<DocumentoMesa> findByMesaNombreAndPagadoOrderByFechaDesc(String mesaNombre, boolean pagado);

    // Contar documentos pendientes por mesa
    @Query(value = "{ 'mesaNombre' : ?0, 'pagado' : false }", count = true)
    long countPendientesByMesaNombre(String mesaNombre);

    // Obtener el total de ventas de una mesa en un rango de fechas
    @Query(value = "{ 'mesaNombre' : ?0, 'pagado' : true, 'fechaPago' : { $gte: ?1, $lte: ?2 } }")
    List<DocumentoMesa> findVentasByMesaAndFechaPago(String mesaNombre, LocalDateTime fechaInicio, LocalDateTime fechaFin);
}
