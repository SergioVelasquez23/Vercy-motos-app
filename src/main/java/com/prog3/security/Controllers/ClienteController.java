package com.prog3.security.Controllers;

import com.prog3.security.Models.Cliente;
import com.prog3.security.Services.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

/**
 * Controlador REST para gestionar Clientes
 * Endpoints completos para el módulo de clientes con facturación electrónica
 */
@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    /**
     * Obtener todos los clientes
     * GET /api/clientes
     */
    @GetMapping
    public ResponseEntity<?> obtenerTodosLosClientes() {
        try {
            List<Cliente> clientes = clienteService.obtenerTodosLosClientes();
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener clientes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Obtener cliente por ID
     * GET /api/clientes/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerClientePorId(@PathVariable String id) {
        try {
            Optional<Cliente> cliente = clienteService.obtenerClientePorId(id);
            
            if (cliente.isPresent()) {
                return ResponseEntity.ok(cliente.get());
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Cliente no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener cliente: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Obtener cliente por número de identificación
     * GET /api/clientes/documento/{numeroIdentificacion}
     */
    @GetMapping("/documento/{numeroIdentificacion}")
    public ResponseEntity<?> obtenerClientePorDocumento(@PathVariable String numeroIdentificacion) {
        try {
            Optional<Cliente> cliente = clienteService.obtenerClientePorDocumento(numeroIdentificacion);
            
            if (cliente.isPresent()) {
                return ResponseEntity.ok(cliente.get());
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Cliente no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener cliente: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Crear nuevo cliente
     * POST /api/clientes
     * Body: { "tipoPersona": "Persona Natural", "numeroIdentificacion": "123456789", ... }
     */
    @PostMapping
    public ResponseEntity<?> crearCliente(
            @RequestBody Cliente cliente,
            @RequestHeader(value = "X-Usuario-Id", required = false, defaultValue = "sistema") String usuarioId) {
        try {
            // Validaciones básicas
            if (cliente.getNumeroIdentificacion() == null || cliente.getNumeroIdentificacion().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "El número de identificación es requerido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            if (cliente.getTipoPersona() == null || cliente.getTipoPersona().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "El tipo de persona es requerido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            Cliente nuevoCliente = clienteService.crearCliente(cliente, usuarioId);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoCliente);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al crear cliente: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Actualizar cliente existente
     * PUT /api/clientes/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarCliente(
            @PathVariable String id,
            @RequestBody Cliente cliente,
            @RequestHeader(value = "X-Usuario-Id", required = false, defaultValue = "sistema") String usuarioId) {
        try {
            Cliente clienteActualizado = clienteService.actualizarCliente(id, cliente, usuarioId);
            return ResponseEntity.ok(clienteActualizado);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al actualizar cliente: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Eliminar cliente (soft delete)
     * DELETE /api/clientes/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarCliente(
            @PathVariable String id,
            @RequestHeader(value = "X-Usuario-Id", required = false, defaultValue = "sistema") String usuarioId) {
        try {
            boolean eliminado = clienteService.eliminarCliente(id, usuarioId);
            
            if (eliminado) {
                Map<String, String> respuesta = new HashMap<>();
                respuesta.put("mensaje", "Cliente eliminado exitosamente");
                return ResponseEntity.ok(respuesta);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Cliente no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al eliminar cliente: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Eliminar cliente permanentemente
     * DELETE /api/clientes/{id}/permanente
     */
    @DeleteMapping("/{id}/permanente")
    public ResponseEntity<?> eliminarClientePermanente(@PathVariable String id) {
        try {
            boolean eliminado = clienteService.eliminarClientePermanente(id);
            
            if (eliminado) {
                Map<String, String> respuesta = new HashMap<>();
                respuesta.put("mensaje", "Cliente eliminado permanentemente");
                return ResponseEntity.ok(respuesta);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Cliente no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al eliminar cliente: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Obtener clientes activos
     * GET /api/clientes/estado/activos
     */
    @GetMapping("/estado/activos")
    public ResponseEntity<?> obtenerClientesActivos() {
        try {
            List<Cliente> clientes = clienteService.obtenerClientesActivos();
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener clientes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Obtener clientes por estado
     * GET /api/clientes/estado/{estado}
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<?> obtenerClientesPorEstado(@PathVariable String estado) {
        try {
            List<Cliente> clientes = clienteService.obtenerClientesPorEstado(estado);
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener clientes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Buscar clientes (búsqueda global)
     * GET /api/clientes/buscar?q=termino
     */
    @GetMapping("/buscar")
    public ResponseEntity<?> buscarClientes(@RequestParam String q) {
        try {
            List<Cliente> clientes = clienteService.buscarClientes(q);
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al buscar clientes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Obtener clientes con saldo pendiente
     * GET /api/clientes/con-saldo
     */
    @GetMapping("/con-saldo")
    public ResponseEntity<?> obtenerClientesConSaldo() {
        try {
            List<Cliente> clientes = clienteService.obtenerClientesConSaldo();
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener clientes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Bloquear cliente
     * PUT /api/clientes/{id}/bloquear
     * Body: { "motivo": "Razón del bloqueo" }
     */
    @PutMapping("/{id}/bloquear")
    public ResponseEntity<?> bloquearCliente(
            @PathVariable String id,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-Usuario-Id", required = false, defaultValue = "sistema") String usuarioId) {
        try {
            String motivo = body.get("motivo");
            if (motivo == null || motivo.isEmpty()) {
                motivo = "Sin motivo especificado";
            }
            
            Cliente cliente = clienteService.bloquearCliente(id, motivo, usuarioId);
            return ResponseEntity.ok(cliente);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al bloquear cliente: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Activar cliente
     * PUT /api/clientes/{id}/activar
     */
    @PutMapping("/{id}/activar")
    public ResponseEntity<?> activarCliente(
            @PathVariable String id,
            @RequestHeader(value = "X-Usuario-Id", required = false, defaultValue = "sistema") String usuarioId) {
        try {
            Cliente cliente = clienteService.activarCliente(id, usuarioId);
            return ResponseEntity.ok(cliente);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al activar cliente: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Actualizar saldo del cliente
     * PUT /api/clientes/{id}/saldo
     * Body: { "monto": 100000 }
     */
    @PutMapping("/{id}/saldo")
    public ResponseEntity<?> actualizarSaldo(
            @PathVariable String id,
            @RequestBody Map<String, Double> body) {
        try {
            Double monto = body.get("monto");
            if (monto == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "El monto es requerido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            Cliente cliente = clienteService.actualizarSaldo(id, monto);
            return ResponseEntity.ok(cliente);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al actualizar saldo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Verificar cupo de crédito
     * POST /api/clientes/{id}/verificar-cupo
     * Body: { "montoFactura": 500000 }
     */
    @PostMapping("/{id}/verificar-cupo")
    public ResponseEntity<?> verificarCupoCredito(
            @PathVariable String id,
            @RequestBody Map<String, Double> body) {
        try {
            Double montoFactura = body.get("montoFactura");
            if (montoFactura == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "El monto de la factura es requerido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            Map<String, Object> resultado = clienteService.verificarCupoCredito(id, montoFactura);
            return ResponseEntity.ok(resultado);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al verificar cupo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Obtener estadísticas de clientes
     * GET /api/clientes/estadisticas
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<?> obtenerEstadisticas() {
        try {
            Map<String, Object> estadisticas = clienteService.obtenerEstadisticas();
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener estadísticas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Obtener clientes por departamento
     * GET /api/clientes/departamento/{departamento}
     */
    @GetMapping("/departamento/{departamento}")
    public ResponseEntity<?> obtenerClientesPorDepartamento(@PathVariable String departamento) {
        try {
            List<Cliente> clientes = clienteService.obtenerClientesPorDepartamento(departamento);
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener clientes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Obtener clientes por ciudad
     * GET /api/clientes/ciudad/{ciudad}
     */
    @GetMapping("/ciudad/{ciudad}")
    public ResponseEntity<?> obtenerClientesPorCiudad(@PathVariable String ciudad) {
        try {
            List<Cliente> clientes = clienteService.obtenerClientesPorCiudad(ciudad);
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener clientes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Obtener clientes por vendedor
     * GET /api/clientes/vendedor/{vendedorId}
     */
    @GetMapping("/vendedor/{vendedorId}")
    public ResponseEntity<?> obtenerClientesPorVendedor(@PathVariable String vendedorId) {
        try {
            List<Cliente> clientes = clienteService.obtenerClientesPorVendedor(vendedorId);
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener clientes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
