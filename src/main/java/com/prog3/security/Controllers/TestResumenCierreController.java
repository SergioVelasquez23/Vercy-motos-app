package com.prog3.security.Controllers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prog3.security.Models.CuadreCaja;
import com.prog3.security.DTOs.CuadreCajaRequest;
import com.prog3.security.Services.CuadreCajaService;
import com.prog3.security.Services.ResumenCierreService;
import com.prog3.security.Services.ResponseService;
import com.prog3.security.Utils.ApiResponse;

/**
 * Controlador de prueba para el sistema de resumen de cierre de caja
 */
@CrossOrigin
@RestController
@RequestMapping("api/test-resumen-cierre")
public class TestResumenCierreController {

    @Autowired
    private CuadreCajaService cuadreCajaService;

    @Autowired
    private ResumenCierreService resumenCierreService;

    @Autowired
    private ResponseService responseService;

    /**
     * Crear un cuadre de caja de prueba
     */
    @PostMapping("/crear-cuadre-prueba")
    public ResponseEntity<ApiResponse<CuadreCaja>> crearCuadrePrueba() {
        try {
            CuadreCajaRequest request = new CuadreCajaRequest();
            request.setNombre("Caja Principal - Prueba");
            request.setResponsable("admin-test");
            request.setFondoInicial(500000.0);

            // Fondo inicial desglosado
            Map<String, Double> fondoDesglosado = new HashMap<>();
            fondoDesglosado.put("efectivo", 500000.0);
            fondoDesglosado.put("transferencia", 0.0);
            request.setFondoInicialDesglosado(fondoDesglosado);
            // $5,000 de tolerancia
            request.setObservaciones("Cuadre de prueba para testing del resumen de cierre");
            request.setCerrarCaja(false); // No cerrar aún

            CuadreCaja cuadreGuardado = cuadreCajaService.crearCuadreCaja(request);

            return responseService.created(cuadreGuardado, "Cuadre de prueba creado exitosamente");

        } catch (Exception e) {
            return responseService.internalError("Error al crear cuadre de prueba: " + e.getMessage());
        }
    }

    /**
     * Probar el resumen de cierre con un cuadre específico
     */
    @GetMapping("/probar-resumen/{cuadreId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> probarResumen(@PathVariable String cuadreId) {
        try {
            System.out.println("🧪 Probando resumen de cierre para cuadre: " + cuadreId);

            // Verificar que el cuadre existe
            CuadreCaja cuadre = cuadreCajaService.obtenerCuadrePorId(cuadreId);
            if (cuadre == null) {
                return responseService.notFound("Cuadre de caja no encontrado con ID: " + cuadreId);
            }

            System.out.println("📊 Cuadre encontrado: " + cuadre.getNombre());
            System.out.println("📅 Fecha apertura: " + cuadre.getFechaApertura());
            System.out.println("💰 Fondo inicial: " + cuadre.getFondoInicial());

            // Generar el resumen
            Map<String, Object> resumen = resumenCierreService.generarResumenCuadre(cuadreId);

            // Agregar información adicional de debugging
            resumen.put("debugInfo", Map.of(
                    "cuadreId", cuadreId,
                    "fechaGeneracion", LocalDateTime.now(),
                    "mensaje", "Resumen generado exitosamente desde endpoint de prueba"
            ));

            return responseService.success(resumen, "Resumen de prueba generado exitosamente");

        } catch (Exception e) {
            System.err.println("❌ Error en prueba de resumen: " + e.getMessage());
            return responseService.internalError("Error en prueba de resumen: " + e.getMessage());
        }
    }

    /**
     * Obtener información básica de prueba
     */
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getInfo() {
        try {
            Map<String, Object> info = new HashMap<>();
            info.put("servicio", "Test Resumen Cierre Controller");
            info.put("version", "1.0");
            info.put("descripcion", "Controlador de prueba para el sistema de resumen de cierre de caja");
            info.put("endpoints", Map.of(
                    "POST /crear-cuadre-prueba", "Crea un cuadre de caja de prueba",
                    "GET /probar-resumen/{cuadreId}", "Prueba el resumen de cierre para un cuadre específico",
                    "GET /info", "Información del controlador de prueba"
            ));
            info.put("fechaConsulta", LocalDateTime.now());

            return responseService.success(info, "Información del controlador de prueba obtenida");

        } catch (Exception e) {
            return responseService.internalError("Error al obtener información: " + e.getMessage());
        }
    }
}
