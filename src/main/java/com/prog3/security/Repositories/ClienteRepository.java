package com.prog3.security.Repositories;

import com.prog3.security.Models.Cliente;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar operaciones de base de datos de Clientes
 */
@Repository
public interface ClienteRepository extends MongoRepository<Cliente, String> {

    // Buscar por número de identificación (único)
    Optional<Cliente> findByNumeroIdentificacion(String numeroIdentificacion);
    
    // Verificar si existe un número de identificación
    boolean existsByNumeroIdentificacion(String numeroIdentificacion);
    
    // Buscar por correo electrónico
    Optional<Cliente> findByCorreo(String correo);
    
    // Buscar por tipo de persona
    List<Cliente> findByTipoPersona(String tipoPersona);
    
    // Buscar por estado
    List<Cliente> findByEstado(String estado);
    
    // Buscar clientes activos
    @Query("{ 'estado': 'activo' }")
    List<Cliente> findClientesActivos();
    
    // Buscar por razón social (búsqueda parcial, case insensitive)
    @Query("{ 'razonSocial': { $regex: ?0, $options: 'i' } }")
    List<Cliente> buscarPorRazonSocial(String razonSocial);
    
    // Buscar por nombres o apellidos (búsqueda parcial)
    @Query("{ $or: [ { 'nombres': { $regex: ?0, $options: 'i' } }, { 'apellidos': { $regex: ?0, $options: 'i' } } ] }")
    List<Cliente> buscarPorNombresOApellidos(String busqueda);
    
    // Buscar por departamento
    List<Cliente> findByDepartamento(String departamento);
    
    // Buscar por ciudad
    List<Cliente> findByCiudad(String ciudad);
    
    // Buscar por departamento y ciudad
    List<Cliente> findByDepartamentoAndCiudad(String departamento, String ciudad);
    
    // Buscar por responsable IVA
    List<Cliente> findByResponsableIVA(String responsableIVA);
    
    // Buscar por categoría de cliente
    List<Cliente> findByCategoriaCliente(String categoriaCliente);
    
    // Buscar por vendedor asignado
    List<Cliente> findByVendedorAsignado(String vendedorAsignado);
    
    // Buscar por zona de ventas
    List<Cliente> findByZonaVentas(String zonaVentas);
    
    // Buscar clientes con saldo mayor a cero
    @Query("{ 'saldoActual': { $gt: 0 } }")
    List<Cliente> findClientesConSaldo();
    
    // Buscar clientes con saldo mayor a un valor específico
    @Query("{ 'saldoActual': { $gt: ?0 } }")
    List<Cliente> findClientesConSaldoMayorA(Double saldo);
    
    // Buscar clientes con cupo de crédito
    @Query("{ 'cupoCredito': { $gt: 0 } }")
    List<Cliente> findClientesConCupoCredito();
    
    // Buscar clientes habilitados para facturación electrónica
    List<Cliente> findByHabilitadoFacturacionElectronica(Boolean habilitado);
    
    // Contar clientes por estado
    long countByEstado(String estado);
    
    // Contar clientes por tipo de persona
    long countByTipoPersona(String tipoPersona);
    
    // Ordenar por fecha de creación descendente
    List<Cliente> findAllByOrderByFechaCreacionDesc();
    
    // Buscar por estado ordenados por razón social
    List<Cliente> findByEstadoOrderByRazonSocialAsc(String estado);
    
    // Búsqueda global (razón social, documento, correo, teléfono)
    @Query("{ $or: [ " +
           "{ 'razonSocial': { $regex: ?0, $options: 'i' } }, " +
           "{ 'numeroIdentificacion': { $regex: ?0, $options: 'i' } }, " +
           "{ 'correo': { $regex: ?0, $options: 'i' } }, " +
           "{ 'telefono': { $regex: ?0, $options: 'i' } } " +
           "] }")
    List<Cliente> busquedaGlobal(String termino);
}
