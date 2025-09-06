package com.prog3.security.Exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción personalizada para errores de lógica de negocio.
 * Permite especificar el código de estado HTTP y detalles adicionales.
 */
public class BusinessException extends RuntimeException {
    
    private final HttpStatus httpStatus;
    private final String details;

    public BusinessException(String message) {
        this(message, HttpStatus.BAD_REQUEST, null);
    }

    public BusinessException(String message, HttpStatus httpStatus) {
        this(message, httpStatus, null);
    }

    public BusinessException(String message, HttpStatus httpStatus, String details) {
        super(message);
        this.httpStatus = httpStatus;
        this.details = details;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getDetails() {
        return details;
    }

    // Métodos de conveniencia para casos comunes
    public static BusinessException cajaNoAbierta() {
        return new BusinessException(
            "No se puede realizar esta operación sin una caja abierta",
            HttpStatus.BAD_REQUEST,
            "Debe abrir una caja antes de continuar"
        );
    }

    public static BusinessException pedidoYaPagado(String pedidoId) {
        return new BusinessException(
            "El pedido ya está pagado y no se puede modificar",
            HttpStatus.CONFLICT,
            "Pedido ID: " + pedidoId
        );
    }

    public static BusinessException inventarioInsuficiente(String producto, double disponible) {
        return new BusinessException(
            "Inventario insuficiente para el producto: " + producto,
            HttpStatus.BAD_REQUEST,
            "Disponible: " + disponible
        );
    }

    public static BusinessException mesaOcupada(String mesa) {
        return new BusinessException(
            "La mesa " + mesa + " ya tiene pedidos activos",
            HttpStatus.CONFLICT,
            "Mesa: " + mesa
        );
    }
}
