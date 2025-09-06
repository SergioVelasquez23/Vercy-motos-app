package com.prog3.security.Utils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private Map<String, Object> meta;
    private LocalDateTime timestamp;

    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
        this.meta = new HashMap<>();
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        response.message = message;
        return response;
    }

    public static <T> ApiResponse<T> error(String message, String errorCode) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.message = message;
        response.meta.put("errorCode", errorCode);
        return response;
    }

    /**
     * Método builder para crear instancias de ApiResponse de forma fluida
     * @param <T> Tipo de datos que contendrá la respuesta
     * @return Nuevo builder para ApiResponse
     */
    public static <T> ApiResponseBuilder<T> builder() {
        return new ApiResponseBuilder<>();
    }

    /**
     * Clase Builder para ApiResponse siguiendo el patrón Builder
     */
    public static class ApiResponseBuilder<T> {
        private boolean success;
        private String message;
        private T data;
        private Map<String, Object> meta;
        private LocalDateTime timestamp;

        public ApiResponseBuilder() {
            this.meta = new HashMap<>();
            this.timestamp = LocalDateTime.now();
        }

        public ApiResponseBuilder<T> success(boolean success) {
            this.success = success;
            return this;
        }

        public ApiResponseBuilder<T> message(String message) {
            this.message = message;
            return this;
        }

        public ApiResponseBuilder<T> data(T data) {
            this.data = data;
            return this;
        }

        public ApiResponseBuilder<T> meta(Map<String, Object> meta) {
            this.meta = meta;
            return this;
        }

        public ApiResponseBuilder<T> timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ApiResponseBuilder<T> addMeta(String key, Object value) {
            this.meta.put(key, value);
            return this;
        }

        public ApiResponse<T> build() {
            ApiResponse<T> response = new ApiResponse<>();
            response.success = this.success;
            response.message = this.message;
            response.data = this.data;
            response.meta = this.meta;
            response.timestamp = this.timestamp;
            return response;
        }
    }

    // Getters y Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
