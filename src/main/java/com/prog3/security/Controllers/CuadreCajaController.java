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

import com.prog3.security.DTOs.CuadreCajaRequest;
import com.prog3.security.Models.CuadreCaja;
import com.prog3.security.Services.CuadreCajaService;
import com.prog3.security.Services.ResponseService;
import com.prog3.security.Utils.ApiResponse;

@CrossOrigin
@RestController
@RequestMapping("api/cuadres-caja")
public class CuadreCajaController {

    @Autowired
    private CuadreCajaService cuadreCajaService;

    @Autowired
    private ResponseService responseService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<CuadreCaja>>> getAllCuadres() {
        try {
            List<CuadreCaja> cuadres = cuadreCajaService.obtenerTodosCuadres();
            return responseService.success(cuadres, "Cuadres de caja obtenidos exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener cuadres de caja: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CuadreCaja>> getCuadreById(@PathVariable String id) {
        try {
            CuadreCaja cuadre = cuadreCajaService.obtenerCuadrePorId(id);
            if (cuadre == null) {
                return responseService.notFound("Cuadre de caja no encontrado con ID: " + id);
            }

            // Logs para depuración
            System.out.println("Detalles del cuadre " + id + ":");
            System.out.println("Fondo Inicial: " + cuadre.getFondoInicial());
            System.out.println("Fondo Desglosado: " + cuadre.getFondoInicialDesglosado());
            System.out.println("Efectivo Esperado: " + cuadre.getEfectivoEsperado());
            System.out.println("Fecha Apertura: " + cuadre.getFechaApertura());
            System.out.println("Cerrada: " + cuadre.isCerrada());
            System.out.println("Estado: " + cuadre.getEstado());

            return responseService.success(cuadre, "Cuadre de caja encontrado");
        } catch (Exception e) {
            return responseService.internalError("Error al buscar cuadre de caja: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/detalles")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCuadreDetalles(@PathVariable String id) {
        try {
            CuadreCaja cuadre = cuadreCajaService.obtenerCuadrePorId(id);
            if (cuadre == null) {
                return responseService.notFound("Cuadre de caja no encontrado con ID: " + id);
            }

            // Recalcular el efectivo esperado actual
            double efectivoEsperadoActual = cuadreCajaService.calcularEfectivoEsperado();

            Map<String, Object> detalles = new HashMap<>();
            detalles.put("id", cuadre.get_id());
            detalles.put("nombre", cuadre.getNombre());
            detalles.put("responsable", cuadre.getResponsable());
            detalles.put("estado", cuadre.getEstado());
            detalles.put("fechaApertura", cuadre.getFechaApertura());
            detalles.put("fechaCierre", cuadre.getFechaCierre());
            detalles.put("fondoInicial", cuadre.getFondoInicial());
            detalles.put("fondoInicialDesglosado", cuadre.getFondoInicialDesglosado());
            detalles.put("efectivoEsperadoOriginal", cuadre.getEfectivoEsperado());
            detalles.put("efectivoEsperadoActual", efectivoEsperadoActual);
            detalles.put("efectivoDeclarado", cuadre.getEfectivoDeclarado());
            detalles.put("diferencia", cuadre.getDiferencia());
            detalles.put("cerrada", cuadre.isCerrada());

            return responseService.success(detalles, "Detalles del cuadre de caja obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener detalles del cuadre: " + e.getMessage());
        }
    }

    @GetMapping("/responsable/{responsable}")
    public ResponseEntity<ApiResponse<List<CuadreCaja>>> getCuadresByResponsable(@PathVariable String responsable) {
        try {
            List<CuadreCaja> cuadres = cuadreCajaService.obtenerCuadresPorResponsable(responsable);
            return responseService.success(cuadres, "Cuadres de caja por responsable obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener cuadres por responsable: " + e.getMessage());
        }
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<ApiResponse<List<CuadreCaja>>> getCuadresByEstado(@PathVariable String estado) {
        try {
            List<CuadreCaja> cuadres = cuadreCajaService.obtenerCuadresPorEstado(estado);
            return responseService.success(cuadres, "Cuadres de caja por estado obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener cuadres por estado: " + e.getMessage());
        }
    }

    @GetMapping("/fechas")
    public ResponseEntity<ApiResponse<List<CuadreCaja>>> getCuadresByFechaRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        try {
            List<CuadreCaja> cuadres = cuadreCajaService.obtenerCuadresPorRangoFechas(fechaInicio, fechaFin);
            return responseService.success(cuadres, "Cuadres de caja filtrados por fecha obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al filtrar cuadres por fecha: " + e.getMessage());
        }
    }

    @GetMapping("/hoy")
    public ResponseEntity<ApiResponse<List<CuadreCaja>>> getCuadresHoy() {
        try {
            List<CuadreCaja> cuadres = cuadreCajaService.obtenerCuadresHoy();
            return responseService.success(cuadres, "Cuadres de caja de hoy obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener cuadres de hoy: " + e.getMessage());
        }
    }

    @GetMapping("/efectivo-esperado")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEfectivoEsperado() {
        try {
            double efectivoEsperado = cuadreCajaService.calcularEfectivoEsperado();

            Map<String, Object> response = new HashMap<>();
            response.put("efectivoEsperado", efectivoEsperado);
            response.put("fechaConsulta", LocalDateTime.now());

            return responseService.success(response, "Efectivo esperado calculado exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al calcular efectivo esperado: " + e.getMessage());
        }
    }

    @GetMapping("/info-apertura")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getInfoAperturaCaja() {
        try {
            // Obtener información relevante para abrir una caja
            double fondoInicialSugerido = 500000.0; // Valor predeterminado o desde configuración

            Map<String, Object> response = new HashMap<>();
            response.put("fondoInicialSugerido", fondoInicialSugerido);
            response.put("fechaConsulta", LocalDateTime.now());

            // Calcular efectivo esperado actual
            double efectivoEsperado = cuadreCajaService.calcularEfectivoEsperado();
            response.put("efectivoEsperado", efectivoEsperado);

            // También puedes añadir el último fondo de cierre si existe
            List<CuadreCaja> ultimosCuadres = cuadreCajaService.obtenerCuadresHoy();
            if (!ultimosCuadres.isEmpty()) {
                // Si hay cuadres hoy, podríamos sugerir el mismo fondo
                CuadreCaja ultimoCuadre = ultimosCuadres.get(ultimosCuadres.size() - 1);
                response.put("ultimoFondoInicial", ultimoCuadre.getFondoInicial());

                // Log para depuración
                System.out.println("Último fondo inicial encontrado: " + ultimoCuadre.getFondoInicial());
                System.out.println("ID del último cuadre: " + ultimoCuadre.get_id());
            } else {
                System.out.println("No se encontraron cuadres de caja para hoy");
            }

            return responseService.success(response, "Información para apertura de caja obtenida exitosamente");
        } catch (Exception e) {
            System.out.println("Error al obtener información para apertura: " + e.getMessage());
            return responseService.internalError("Error al obtener información para apertura: " + e.getMessage());
        }
    }

    @GetMapping("/abiertas")
    public ResponseEntity<ApiResponse<List<CuadreCaja>>> getCajasAbiertas() {
        try {
            // Obtener solo cajas no cerradas
            List<CuadreCaja> cuadres = cuadreCajaService.obtenerTodosCuadres().stream()
                    .filter(c -> !c.isCerrada())
                    .toList();

            return responseService.success(cuadres, "Cajas abiertas obtenidas exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener cajas abiertas: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CuadreCaja>> createCuadreCaja(@RequestBody CuadreCajaRequest request) {
        try {
            // Validaciones básicas
            if (request.getResponsable() == null || request.getResponsable().trim().isEmpty()) {
                return responseService.badRequest("El responsable es requerido");
            }

            // Validación específica para el fondo inicial
            if (request.getFondoInicial() < 0) {
                return responseService.badRequest("El fondo inicial debe ser un valor positivo");
            }

            // Verificación debug para el fondo inicial
            System.out.println("Fondo Inicial recibido: " + request.getFondoInicial());
            System.out.println("Fondo inicial desglosado recibido: " + request.getFondoInicialDesglosado());

            // Si no se ha proporcionado un fondo inicial desglosado, crearlo automáticamente
            if (request.getFondoInicialDesglosado() == null || request.getFondoInicialDesglosado().isEmpty()) {
                Map<String, Double> desglose = new HashMap<>();
                desglose.put("Efectivo", request.getFondoInicial());
                request.setFondoInicialDesglosado(desglose);
                System.out.println("Generado automáticamente fondo inicial desglosado: " + desglose);
            }

            CuadreCaja nuevoCuadre = cuadreCajaService.crearCuadreCaja(request);

            // Verificar que se ha guardado correctamente
            System.out.println("Cuadre creado con ID: " + nuevoCuadre.get_id());
            System.out.println("Fondo inicial guardado: " + nuevoCuadre.getFondoInicial());
            System.out.println("Efectivo esperado guardado: " + nuevoCuadre.getEfectivoEsperado());

            return responseService.created(nuevoCuadre, "Cuadre de caja creado exitosamente");
        } catch (Exception e) {
            System.out.println("Error al crear cuadre de caja: " + e.getMessage());
            e.printStackTrace();
            return responseService.internalError("Error al crear cuadre de caja: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/aprobar")
    public ResponseEntity<ApiResponse<CuadreCaja>> aprobarCuadre(
            @PathVariable String id,
            @RequestBody Map<String, String> request) {
        try {
            String aprobador = request.get("aprobador");

            if (aprobador == null || aprobador.trim().isEmpty()) {
                return responseService.badRequest("El aprobador es requerido");
            }

            CuadreCaja cuadreAprobado = cuadreCajaService.aprobarCuadre(id, aprobador);
            if (cuadreAprobado == null) {
                return responseService.notFound("Cuadre de caja no encontrado con ID: " + id);
            }

            return responseService.success(cuadreAprobado, "Cuadre de caja aprobado exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al aprobar cuadre de caja: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/rechazar")
    public ResponseEntity<ApiResponse<CuadreCaja>> rechazarCuadre(
            @PathVariable String id,
            @RequestBody Map<String, String> request) {
        try {
            String aprobador = request.get("aprobador");
            String observacion = request.get("observacion");

            if (aprobador == null || aprobador.trim().isEmpty()) {
                return responseService.badRequest("El aprobador es requerido");
            }

            CuadreCaja cuadreRechazado = cuadreCajaService.rechazarCuadre(id, aprobador, observacion);
            if (cuadreRechazado == null) {
                return responseService.notFound("Cuadre de caja no encontrado con ID: " + id);
            }

            return responseService.success(cuadreRechazado, "Cuadre de caja rechazado exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al rechazar cuadre de caja: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CuadreCaja>> updateCuadreCaja(
            @PathVariable String id,
            @RequestBody CuadreCajaRequest request) {
        try {
            // Validaciones básicas
            if (request.getResponsable() == null || request.getResponsable().trim().isEmpty()) {
                return responseService.badRequest("El responsable es requerido");
            }

            // Log de depuración para ver qué valores están llegando
            System.out.println("Actualizando cuadre: " + id);
            System.out.println("Fondo Inicial recibido en actualización: " + request.getFondoInicial());
            System.out.println("Fondo desglosado recibido: " + request.getFondoInicialDesglosado());
            System.out.println("Cerrar caja: " + request.isCerrarCaja());

            // Obtener el cuadre existente para referencia
            CuadreCaja cuadreExistente = cuadreCajaService.obtenerCuadrePorId(id);
            if (cuadreExistente == null) {
                return responseService.notFound("Cuadre de caja no encontrado con ID: " + id);
            }

            // Verificar el fondo inicial - si viene vacío o cero pero ya existe un valor, mantener el existente
            if (request.getFondoInicial() <= 0 && cuadreExistente.getFondoInicial() > 0) {
                System.out.println("⚠️ Fondo inicial recibido inválido (" + request.getFondoInicial()
                        + "), manteniendo el existente: " + cuadreExistente.getFondoInicial());
                request.setFondoInicial(cuadreExistente.getFondoInicial());
            }

            // Validación específica para el fondo inicial
            if (request.getFondoInicial() < 0) {
                return responseService.badRequest("El fondo inicial debe ser un valor positivo");
            }

            // Si el fondo desglosado viene vacío pero ya existía uno, mantener el existente
            if ((request.getFondoInicialDesglosado() == null || request.getFondoInicialDesglosado().isEmpty())
                    && cuadreExistente.getFondoInicialDesglosado() != null && !cuadreExistente.getFondoInicialDesglosado().isEmpty()) {
                System.out.println("⚠️ Fondo inicial desglosado vacío, manteniendo el existente");
                request.setFondoInicialDesglosado(cuadreExistente.getFondoInicialDesglosado());
            } else if (request.getFondoInicialDesglosado() == null || request.getFondoInicialDesglosado().isEmpty()) {
                // Si no hay desglose, crear uno por defecto con todo en efectivo
                Map<String, Double> desglose = new HashMap<>();
                desglose.put("Efectivo", request.getFondoInicial());
                request.setFondoInicialDesglosado(desglose);
                System.out.println("Creado desglose por defecto en actualización: " + desglose);
            }

            CuadreCaja cuadreActualizado = cuadreCajaService.actualizarCuadreCaja(id, request);
            if (cuadreActualizado == null) {
                return responseService.notFound("Cuadre de caja no encontrado con ID: " + id);
            }

            System.out.println("Cuadre actualizado con fondoInicial: " + cuadreActualizado.getFondoInicial());
            System.out.println("Fondo inicial desglosado: " + cuadreActualizado.getFondoInicialDesglosado());
            System.out.println("Efectivo esperado: " + cuadreActualizado.getEfectivoEsperado());
            System.out.println("Estado del cuadre actualizado: " + cuadreActualizado.getEstado());
            System.out.println("Caja cerrada: " + cuadreActualizado.isCerrada());
            System.out.println("Fecha de cierre: " + cuadreActualizado.getFechaCierre());

            return responseService.success(cuadreActualizado, "Cuadre de caja actualizado exitosamente");
        } catch (Exception e) {
            System.out.println("Error al actualizar cuadre de caja: " + e.getMessage());
            return responseService.internalError("Error al actualizar cuadre de caja: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/cerrar")
    public ResponseEntity<ApiResponse<CuadreCaja>> cerrarCaja(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, Object> request) {
        try {
            System.out.println("Solicitud para cerrar caja con ID: " + id);

            // Buscar el cuadre existente
            CuadreCaja cuadre = cuadreCajaService.obtenerCuadrePorId(id);
            if (cuadre == null) {
                return responseService.notFound("Cuadre de caja no encontrado con ID: " + id);
            }

            // Verificar si ya está cerrada
            if (cuadre.isCerrada()) {
                System.out.println("La caja ya estaba cerrada - Estado: " + cuadre.getEstado());
                System.out.println("Fecha de cierre anterior: " + cuadre.getFechaCierre());
                return responseService.badRequest("La caja ya está cerrada");
            }

            System.out.println("Datos antes del cierre - Fondo inicial: " + cuadre.getFondoInicial());
            System.out.println("Efectivo esperado actual: " + cuadre.getEfectivoEsperado());
            System.out.println("Efectivo declarado: " + cuadre.getEfectivoDeclarado());

            // Crear un request con los datos actuales y el flag de cierre
            CuadreCajaRequest cajaRequest = new CuadreCajaRequest();
            cajaRequest.setNombre(cuadre.getNombre());
            cajaRequest.setResponsable(cuadre.getResponsable());
            cajaRequest.setFondoInicial(cuadre.getFondoInicial());
            cajaRequest.setFondoInicialDesglosado(cuadre.getFondoInicialDesglosado());
            cajaRequest.setEfectivoDeclarado(cuadre.getEfectivoDeclarado());
            cajaRequest.setTolerancia(cuadre.getTolerancia());
            cajaRequest.setObservaciones(cuadre.getObservaciones());

            // Mantener los datos de ventas
            cajaRequest.setTotalVentas(cuadre.getTotalVentas());
            cajaRequest.setVentasDesglosadas(cuadre.getVentasDesglosadas());
            cajaRequest.setTotalPropinas(cuadre.getTotalPropinas());
            cajaRequest.setTotalDomicilios(cuadre.getTotalDomicilios());

            // Mantener los datos de gastos
            cajaRequest.setTotalGastos(cuadre.getTotalGastos());
            cajaRequest.setGastosDesglosados(cuadre.getGastosDesglosados());
            cajaRequest.setTotalPagosFacturas(cuadre.getTotalPagosFacturas());

            // Esto es lo importante - marcar como cerrada
            cajaRequest.setCerrarCaja(true);

            // Actualizar la caja cerrándola
            CuadreCaja cajaActualizada = cuadreCajaService.actualizarCuadreCaja(id, cajaRequest);

            System.out.println("Resultado del cierre:");
            System.out.println("Caja cerrada con estado: " + cajaActualizada.getEstado());
            System.out.println("Cerrada: " + cajaActualizada.isCerrada());
            System.out.println("Fecha de cierre: " + cajaActualizada.getFechaCierre());
            System.out.println("Fondo inicial final: " + cajaActualizada.getFondoInicial());
            System.out.println("Efectivo esperado final: " + cajaActualizada.getEfectivoEsperado());

            return responseService.success(cajaActualizada, "Caja cerrada exitosamente");
        } catch (Exception e) {
            System.out.println("Error al cerrar caja: " + e.getMessage());
            e.printStackTrace();
            return responseService.internalError("Error al cerrar caja: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCuadre(@PathVariable String id) {
        try {
            boolean eliminado = cuadreCajaService.eliminarCuadre(id);
            if (!eliminado) {
                return responseService.notFound("Cuadre de caja no encontrado con ID: " + id + " o no se puede eliminar");
            }

            return responseService.success(null, "Cuadre de caja eliminado exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al eliminar cuadre de caja: " + e.getMessage());
        }
    }
}
