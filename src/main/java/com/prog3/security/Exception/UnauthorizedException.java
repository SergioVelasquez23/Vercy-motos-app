package com.prog3.security.Exception;

/**
 * Excepción para accesos no autorizados (401)
 */
public class UnauthorizedException extends RuntimeException {
    
    private final String operation;
    private final String requiredRole;

    public UnauthorizedException(String message) {
        super(message);
        this.operation = null;
        this.requiredRole = null;
    }

    public UnauthorizedException(String operation, String requiredRole) {
        super("No tiene permisos para realizar la operación: " + operation + 
              (requiredRole != null ? " (Rol requerido: " + requiredRole + ")" : ""));
        this.operation = operation;
        this.requiredRole = requiredRole;
    }

    public UnauthorizedException(String operation, String requiredRole, String message) {
        super(message);
        this.operation = operation;
        this.requiredRole = requiredRole;
    }

    public String getOperation() {
        return operation;
    }

    public String getRequiredRole() {
        return requiredRole;
    }

    // Métodos de conveniencia para casos comunes
    public static UnauthorizedException eliminarPedidos() {
        return new UnauthorizedException("eliminar pedidos", "ADMIN");
    }

    public static UnauthorizedException cerrarCaja() {
        return new UnauthorizedException("cerrar caja", "CAJERO");
    }

    public static UnauthorizedException modificarInventario() {
        return new UnauthorizedException("modificar inventario", "ADMIN");
    }

    public static UnauthorizedException verReportes() {
        return new UnauthorizedException("ver reportes", "GERENTE");
    }
}
