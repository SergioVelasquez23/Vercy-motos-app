package com.prog3.security.Repositories;

import com.prog3.security.Models.Proveedor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para manejar operaciones de base de datos de proveedores
 */
@Repository
public interface ProveedorRepository extends MongoRepository<Proveedor, String> {

    // Búsquedas básicas
    /**
     * Buscar proveedor por nombre exacto
     */
    Optional<Proveedor> findByNombre(String nombre);

    /**
     * Buscar proveedor por documento
     */
    Optional<Proveedor> findByDocumento(String documento);

    /**
     * Buscar proveedor por teléfono
     */
    Optional<Proveedor> findByTelefono(String telefono);

    /**
     * Buscar proveedor por email
     */
    Optional<Proveedor> findByEmail(String email);

    // Búsquedas con filtros
    /**
     * Buscar proveedores activos
     */
    List<Proveedor> findByActivoTrue();

    /**
     * Buscar proveedores inactivos
     */
    List<Proveedor> findByActivoFalse();

    /**
     * Buscar por estado activo
     */
    List<Proveedor> findByActivo(boolean activo);

    // Búsquedas por texto (case insensitive)
    /**
     * Buscar proveedores por nombre que contenga el texto (ignorando
     * mayúsculas/minúsculas)
     */
    List<Proveedor> findByNombreContainingIgnoreCase(String nombre);

    /**
     * Buscar proveedores por nombre comercial que contenga el texto
     */
    List<Proveedor> findByNombreComercialContainingIgnoreCase(String nombreComercial);

    /**
     * Buscar proveedores por contacto que contenga el texto
     */
    List<Proveedor> findByContactoContainingIgnoreCase(String contacto);

    /**
     * Buscar proveedores por dirección que contenga el texto
     */
    List<Proveedor> findByDireccionContainingIgnoreCase(String direccion);

    // Búsquedas avanzadas con queries personalizadas
    /**
     * Buscar proveedores por cualquier campo que contenga el texto
     */
    @Query("{ $or: [ "
            + "{ 'nombre': { $regex: ?0, $options: 'i' } }, "
            + "{ 'nombreComercial': { $regex: ?0, $options: 'i' } }, "
            + "{ 'documento': { $regex: ?0, $options: 'i' } }, "
            + "{ 'telefono': { $regex: ?0, $options: 'i' } }, "
            + "{ 'email': { $regex: ?0, $options: 'i' } }, "
            + "{ 'contacto': { $regex: ?0, $options: 'i' } } "
            + "] }")
    List<Proveedor> buscarPorTexto(String texto);

    /**
     * Buscar proveedores activos por cualquier campo que contenga el texto
     */
    @Query("{ 'activo': true, $or: [ "
            + "{ 'nombre': { $regex: ?0, $options: 'i' } }, "
            + "{ 'nombreComercial': { $regex: ?0, $options: 'i' } }, "
            + "{ 'documento': { $regex: ?0, $options: 'i' } }, "
            + "{ 'telefono': { $regex: ?0, $options: 'i' } }, "
            + "{ 'email': { $regex: ?0, $options: 'i' } }, "
            + "{ 'contacto': { $regex: ?0, $options: 'i' } } "
            + "] }")
    List<Proveedor> buscarActivosPorTexto(String texto);

    /**
     * Verificar si existe un proveedor con el mismo documento
     */
    boolean existsByDocumento(String documento);

    /**
     * Verificar si existe un proveedor con el mismo teléfono
     */
    boolean existsByTelefono(String telefono);

    /**
     * Verificar si existe un proveedor con el mismo email
     */
    boolean existsByEmail(String email);

    /**
     * Verificar si existe un proveedor con el mismo nombre
     */
    boolean existsByNombre(String nombre);

    // Búsquedas para validación (excluyendo el ID actual en actualizaciones)
    /**
     * Verificar si existe otro proveedor con el mismo documento (excluyendo el
     * ID actual)
     */
    @Query("{ 'documento': ?0, '_id': { $ne: ?1 } }")
    List<Proveedor> findByDocumentoAndIdNot(String documento, String id);

    /**
     * Verificar si existe otro proveedor con el mismo teléfono (excluyendo el
     * ID actual)
     */
    @Query("{ 'telefono': ?0, '_id': { $ne: ?1 } }")
    List<Proveedor> findByTelefonoAndIdNot(String telefono, String id);

    /**
     * Verificar si existe otro proveedor con el mismo email (excluyendo el ID
     * actual)
     */
    @Query("{ 'email': ?0, '_id': { $ne: ?1 } }")
    List<Proveedor> findByEmailAndIdNot(String email, String id);

    /**
     * Verificar si existe otro proveedor con el mismo nombre (excluyendo el ID
     * actual)
     */
    @Query("{ 'nombre': ?0, '_id': { $ne: ?1 } }")
    List<Proveedor> findByNombreAndIdNot(String nombre, String id);

    // Ordenamiento
    /**
     * Obtener todos los proveedores activos ordenados por nombre
     */
    List<Proveedor> findByActivoTrueOrderByNombreAsc();

    /**
     * Obtener todos los proveedores ordenados por fecha de creación (más
     * recientes primero)
     */
    List<Proveedor> findAllByOrderByFechaCreacionDesc();

    /**
     * Obtener proveedores activos ordenados por fecha de creación
     */
    List<Proveedor> findByActivoTrueOrderByFechaCreacionDesc();
}
