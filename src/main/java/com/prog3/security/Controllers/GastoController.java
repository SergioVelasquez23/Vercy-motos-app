package com.prog3.security.Controllers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.prog3.security.DTOs.GastoRequest;
import com.prog3.security.Models.Gasto;
import com.prog3.security.Services.GastoService;
import com.prog3.security.Services.ResponseService;
import com.prog3.security.Utils.ApiResponse;

@CrossOrigin
@RestController
@RequestMapping("api/gastos")
public class GastoController {

    @Autowired
    private GastoService gastoService;

    @Autowired
    private ResponseService responseService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<Gasto>>> getAllGastos() {
        try {
            List<Gasto> gastos = gastoService.obtenerTodosGastos();
            return responseService.success(gastos, "Gastos obtenidos exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener gastos: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Gasto>> getGastoById(@PathVariable String id) {
        try {
            Gasto gasto = gastoService.obtenerGastoPorId(id);
            if (gasto == null) {
                return responseService.notFound("Gasto no encontrado con ID: " + id);
            }
            return responseService.success(gasto, "Gasto encontrado");
        } catch (Exception e) {
            return responseService.internalError("Error al buscar gasto: " + e.getMessage());
        }
    }

    @GetMapping("/cuadre/{cuadreId}")
    public ResponseEntity<ApiResponse<List<Gasto>>> getGastosByCuadre(@PathVariable String cuadreId) {
        try {
            List<Gasto> gastos = gastoService.obtenerGastosPorCuadre(cuadreId);
            return responseService.success(gastos, "Gastos por cuadre obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener gastos por cuadre: " + e.getMessage());
        }
    }

    @GetMapping("/fechas")
    public ResponseEntity<ApiResponse<List<Gasto>>> getGastosByFechaRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        try {
            List<Gasto> gastos = gastoService.obtenerGastosPorFechas(fechaInicio, fechaFin);
            return responseService.success(gastos, "Gastos filtrados por fecha obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al filtrar gastos por fecha: " + e.getMessage());
        }
    }

    @PostMapping("")
    public ResponseEntity<ApiResponse<Gasto>> createGasto(@RequestBody GastoRequest request) {
        try {
            // Validaciones básicas
            if (request.getCuadreCajaId() == null || request.getCuadreCajaId().trim().isEmpty()) {
                return responseService.badRequest("El ID del cuadre de caja es requerido");
            }
            if (request.getTipoGastoId() == null || request.getTipoGastoId().trim().isEmpty()) {
                return responseService.badRequest("El tipo de gasto es requerido");
            }
            if (request.getMonto() <= 0) {
                return responseService.badRequest("El monto debe ser un valor positivo");
            }

            // ✅ NUEVA VALIDACIÓN: Si se marca como pagado desde caja, verificar disponibilidad
            if (request.isPagadoDesdeCaja()) {
                System.out.println("💰 Creando gasto pagado desde caja por valor de $" + request.getMonto());
            }

            Gasto nuevoGasto = gastoService.crearGasto(request);
            
            String mensaje = "Gasto creado exitosamente";
            if (nuevoGasto.isPagadoDesdeCaja()) {
                mensaje += " y descontado del efectivo de caja";
            }
            
            return responseService.created(nuevoGasto, mensaje);
        } catch (Exception e) {
            return responseService.internalError("Error al crear gasto: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Gasto>> updateGasto(
            @PathVariable String id,
            @RequestBody GastoRequest request) {
        try {
            // Validación básica del monto
            if (request.getMonto() < 0) {
                return responseService.badRequest("El monto no puede ser negativo");
            }

            // ✅ NUEVA VALIDACIÓN: Informar si se cambia a pagado desde caja
            if (request.isPagadoDesdeCaja()) {
                System.out.println("💰 Actualizando gasto como pagado desde caja por valor de $" + request.getMonto());
            }

            Gasto gastoActualizado;
            try {
                gastoActualizado = gastoService.actualizarGasto(id, request);
            } catch (RuntimeException e) {
                return responseService.badRequest(e.getMessage());
            }

            if (gastoActualizado == null) {
                return responseService.notFound("Gasto no encontrado con ID: " + id);
            }

            String mensaje = "Gasto actualizado exitosamente";
            if (gastoActualizado.isPagadoDesdeCaja()) {
                mensaje += " (pagado desde efectivo de caja)";
            }

            return responseService.success(gastoActualizado, mensaje);
        } catch (Exception e) {
            return responseService.internalError("Error al actualizar gasto: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGasto(@PathVariable String id) {
        try {
            boolean eliminado;
            try {
                eliminado = gastoService.eliminarGasto(id);
            } catch (RuntimeException e) {
                return responseService.badRequest(e.getMessage());
            }

            if (!eliminado) {
                return responseService.notFound("Gasto no encontrado con ID: " + id);
            }

            return responseService.success(null, "Gasto eliminado exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al eliminar gasto: " + e.getMessage());
        }
    }

    @PostMapping("/migrar-campo-pagado-desde-caja")
    public ResponseEntity<ApiResponse<Map<String, Object>>> migrarCampoPagadoDesdeCaja() {
        try {
            Map<String, Object> result = gastoService.migrarCampoPagadoDesdeCaja();
            return responseService.success(result, "Migración completada: todos los gastos ahora tienen pagadoDesdeCaja = false");
            
        } catch (Exception e) {
            return responseService.internalError("Error en la migración: " + e.getMessage());
        }
    }

    @GetMapping("/cuadre/{cuadreId}/efectivo-disponible")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEfectivoDisponible(@PathVariable String cuadreId) {
        try {
            Map<String, Object> efectivoInfo = gastoService.calcularEfectivoDisponible(cuadreId);
            
            if (efectivoInfo.containsKey("error")) {
                return responseService.badRequest((String) efectivoInfo.get("error"));
            }
            
            return responseService.success(efectivoInfo, "Información de efectivo disponible obtenida");
        } catch (Exception e) {
            return responseService.internalError("Error al calcular efectivo disponible: " + e.getMessage());
        }
    }
}
