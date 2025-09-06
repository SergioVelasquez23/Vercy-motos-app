package com.prog3.security.Exception;

/**
 * Excepción para recursos no encontrados (404)
 */
public class ResourceNotFoundException extends RuntimeException {
    
    private final String resourceType;
    private final String resourceId;

    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceType = null;
        this.resourceId = null;
    }

    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(resourceType + " no encontrado con ID: " + resourceId);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public ResourceNotFoundException(String resourceType, String resourceId, String message) {
        super(message);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    // Métodos de conveniencia para casos comunes
    public static ResourceNotFoundException pedido(String id) {
        return new ResourceNotFoundException("Pedido", id);
    }

    public static ResourceNotFoundException producto(String id) {
        return new ResourceNotFoundException("Producto", id);
    }

    public static ResourceNotFoundException mesa(String id) {
        return new ResourceNotFoundException("Mesa", id);
    }

    public static ResourceNotFoundException usuario(String id) {
        return new ResourceNotFoundException("Usuario", id);
    }

    public static ResourceNotFoundException cuadreCaja(String id) {
        return new ResourceNotFoundException("Cuadre de caja", id);
    }
}
