package com.prog3.security.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.prog3.security.Services.ResponseService;
import com.prog3.security.Utils.ApiResponse;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Controlador base abstracto que elimina duplicación de código común
 * en todos los controladores CRUD del sistema.
 * 
 * @param <T> Tipo de entidad
 * @param <ID> Tipo de ID (generalmente String para MongoDB)
 */
public abstract class BaseController<T, ID> {
    
    @Autowired
    protected ResponseService responseService;
    
    /**
     * Método abstracto para obtener el repository específico
     */
    protected abstract MongoRepository<T, ID> getRepository();
    
    /**
     * Método abstracto para obtener el nombre de la entidad (para mensajes)
     */
    protected abstract String getEntityName();
    
    /**
     * Método abstracto para validar la entidad antes de crear/actualizar
     */
    protected abstract ResponseEntity<ApiResponse<T>> validateEntity(T entity, boolean isUpdate);
    
    /**
     * Método abstracto para actualizar los campos de la entidad existente
     */
    protected abstract void updateEntityFields(T existing, T updated);
    
    /**
     * GET /{id} - Buscar por ID (patrón estándar)
     */
    protected ResponseEntity<ApiResponse<T>> findEntityById(ID id) {
        try {
            Optional<T> entityOpt = getRepository().findById(id);
            if (entityOpt.isEmpty()) {
                return responseService.notFound(getEntityName() + " no encontrado con ID: " + id);
            }
            return responseService.success(entityOpt.get(), getEntityName() + " encontrado");
        } catch (Exception e) {
            return responseService.internalError("Error al buscar " + getEntityName() + ": " + e.getMessage());
        }
    }
    
    /**
     * GET - Obtener todos (patrón estándar)
     */
    protected ResponseEntity<ApiResponse<List<T>>> findAllEntities() {
        try {
            List<T> entities = getRepository().findAll();
            return responseService.success(entities, getEntityName() + "s obtenidos exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener " + getEntityName() + "s: " + e.getMessage());
        }
    }
    
    /**
     * POST - Crear entidad (patrón estándar)
     */
    protected ResponseEntity<ApiResponse<T>> createEntity(T entity) {
        try {
            // Validar antes de crear
            ResponseEntity<ApiResponse<T>> validationResult = validateEntity(entity, false);
            if (!validationResult.getBody().isSuccess()) {
                return validationResult;
            }
            
            T savedEntity = getRepository().save(entity);
            return responseService.created(savedEntity, getEntityName() + " creado exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al crear " + getEntityName() + ": " + e.getMessage());
        }
    }
    
    /**
     * PUT /{id} - Actualizar entidad (patrón estándar)
     */
    protected ResponseEntity<ApiResponse<T>> updateEntity(ID id, T updatedEntity) {
        try {
            Optional<T> existingOpt = getRepository().findById(id);
            if (existingOpt.isEmpty()) {
                return responseService.notFound(getEntityName() + " no encontrado con ID: " + id);
            }
            
            T existing = existingOpt.get();
            
            // Validar antes de actualizar
            ResponseEntity<ApiResponse<T>> validationResult = validateEntity(updatedEntity, true);
            if (!validationResult.getBody().isSuccess()) {
                return validationResult;
            }
            
            // Actualizar campos
            updateEntityFields(existing, updatedEntity);
            
            T savedEntity = getRepository().save(existing);
            return responseService.success(savedEntity, getEntityName() + " actualizado exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al actualizar " + getEntityName() + ": " + e.getMessage());
        }
    }
    
    /**
     * DELETE /{id} - Eliminar entidad (patrón estándar)
     */
    protected ResponseEntity<ApiResponse<Void>> deleteEntity(ID id) {
        try {
            Optional<T> entityOpt = getRepository().findById(id);
            if (entityOpt.isEmpty()) {
                return responseService.notFound(getEntityName() + " no encontrado con ID: " + id);
            }
            
            // Hook para validaciones antes de eliminar (override si es necesario)
            ResponseEntity<ApiResponse<Void>> deleteValidation = validateBeforeDelete(id, entityOpt.get());
            if (deleteValidation != null) {
                return deleteValidation;
            }
            
            getRepository().deleteById(id);
            return responseService.success(null, getEntityName() + " eliminado exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al eliminar " + getEntityName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Hook para validaciones antes de eliminar (override si es necesario)
     * Retorna null si no hay problemas, o ResponseEntity con error si hay problemas
     */
    protected ResponseEntity<ApiResponse<Void>> validateBeforeDelete(ID id, T entity) {
        return null; // Por defecto, no hay validaciones adicionales
    }
    
    /**
     * Ejecuta una operación con manejo estándar de errores
     */
    protected <R> ResponseEntity<ApiResponse<R>> executeWithErrorHandling(
            Function<Void, ResponseEntity<ApiResponse<R>>> operation,
            String operationName) {
        try {
            return operation.apply(null);
        } catch (Exception e) {
            return responseService.internalError("Error en " + operationName + ": " + e.getMessage());
        }
    }
    
    /**
     * Validación de ID no nulo/vacío
     */
    protected boolean isValidId(ID id) {
        return id != null && !id.toString().trim().isEmpty();
    }
}
