package com.prog3.security.Exception;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.validation.FieldError;

import com.prog3.security.Services.ResponseService;
// import com.prog3.security.Utils.ApiResponse; // Usando nombre completo para evitar conflictos

import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;

/**
 * Manejador global de excepciones para proporcionar respuestas consistentes
 * en toda la aplicación y mejorar la experiencia del usuario.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private ResponseService responseService;

    /**
     * Maneja errores de validación de Bean Validation (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        System.out.println("🚨 Errores de validación detectados: " + errors);
        
        return ResponseEntity.badRequest().body(
            com.prog3.security.Utils.ApiResponse.<Map<String, String>>builder()
                .success(false)
                .message("Errores de validación en los datos enviados")
                .data(errors)
                .timestamp(LocalDateTime.now())
                .build()
        );
    }

    /**
     * Maneja violaciones de restricciones de validación
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<Map<String, String>>> handleConstraintViolationException(
            ConstraintViolationException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        });

        System.out.println("🚨 Violaciones de restricciones detectadas: " + errors);
        
        return ResponseEntity.badRequest().body(
            com.prog3.security.Utils.ApiResponse.<Map<String, String>>builder()
                .success(false)
                .message("Errores de validación en los datos")
                .data(errors)
                .timestamp(LocalDateTime.now())
                .build()
        );
    }

    /**
     * Maneja excepciones de argumentos ilegales
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<String>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        System.out.println("🚨 Argumento ilegal: " + ex.getMessage() + " en " + request.getDescription(false));
        
        return responseService.badRequest("Datos inválidos: " + ex.getMessage());
    }

    /**
     * Maneja excepciones de negocio personalizadas
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<String>> handleBusinessException(
            BusinessException ex, WebRequest request) {
        
        System.out.println("💼 Excepción de negocio: " + ex.getMessage() + " en " + request.getDescription(false));
        
        return ResponseEntity.status(ex.getHttpStatus()).body(
            com.prog3.security.Utils.ApiResponse.<String>builder()
                .success(false)
                .message(ex.getMessage())
                .data(ex.getDetails())
                .timestamp(LocalDateTime.now())
                .build()
        );
    }

    /**
     * Maneja excepciones de recursos no encontrados
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<String>> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        
        System.out.println("🔍 Recurso no encontrado: " + ex.getMessage() + " en " + request.getDescription(false));
        
        return responseService.notFound(ex.getMessage());
    }

    /**
     * Maneja excepciones de acceso no autorizado
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<String>> handleUnauthorizedException(
            UnauthorizedException ex, WebRequest request) {
        
        System.out.println("🔒 Acceso no autorizado: " + ex.getMessage() + " en " + request.getDescription(false));
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            com.prog3.security.Utils.ApiResponse.<String>builder()
                .success(false)
                .message("Acceso no autorizado: " + ex.getMessage())
                .data(null)
                .timestamp(LocalDateTime.now())
                .build()
        );
    }

    /**
     * Maneja excepciones de MongoDB
     */
    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<String>> handleDataAccessException(
            org.springframework.dao.DataAccessException ex, WebRequest request) {
        
        System.err.println("🗄️ Error de base de datos: " + ex.getMessage() + " en " + request.getDescription(false));
        ex.printStackTrace();
        
        return responseService.internalError("Error en la base de datos. Intente nuevamente.");
    }

    /**
     * Maneja excepciones de conexión timeout
     */
    @ExceptionHandler({java.net.SocketTimeoutException.class, java.util.concurrent.TimeoutException.class})
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<String>> handleTimeoutException(
            Exception ex, WebRequest request) {
        
        System.err.println("⏱️ Timeout: " + ex.getMessage() + " en " + request.getDescription(false));
        
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(
            com.prog3.security.Utils.ApiResponse.<String>builder()
                .success(false)
                .message("La operación tardó demasiado tiempo. Intente nuevamente.")
                .data(null)
                .timestamp(LocalDateTime.now())
                .build()
        );
    }

    /**
     * Maneja todas las demás excepciones no capturadas
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<String>> handleGlobalException(
            Exception ex, WebRequest request) {
        
        System.err.println("💥 Error no manejado: " + ex.getClass().getSimpleName() + 
                          " - " + ex.getMessage() + " en " + request.getDescription(false));
        ex.printStackTrace();
        
        // En producción, no exponer detalles internos del error
        String message = ex.getMessage();
        if (message == null || message.isEmpty()) {
            message = "Ha ocurrido un error interno del servidor";
        }
        
        return responseService.internalError("Error interno del servidor: " + message);
    }

    /**
     * Maneja errores de JSON mal formateado
     */
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<String>> handleHttpMessageNotReadableException(
            org.springframework.http.converter.HttpMessageNotReadableException ex, WebRequest request) {
        
        System.out.println("📄 JSON mal formateado: " + ex.getMessage() + " en " + request.getDescription(false));
        
        return responseService.badRequest("El formato de los datos enviados es incorrecto. Verifique la estructura JSON.");
    }

    /**
     * Maneja errores de método HTTP no permitido
     */
    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<com.prog3.security.Utils.ApiResponse<String>> handleHttpRequestMethodNotSupportedException(
            org.springframework.web.HttpRequestMethodNotSupportedException ex, WebRequest request) {
        
        System.out.println("🚫 Método HTTP no permitido: " + ex.getMethod() + " en " + request.getDescription(false));
        
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(
            com.prog3.security.Utils.ApiResponse.<String>builder()
                .success(false)
                .message("Método HTTP '" + ex.getMethod() + "' no permitido para esta URL")
                .data(null)
                .timestamp(LocalDateTime.now())
                .build()
        );
    }
}
