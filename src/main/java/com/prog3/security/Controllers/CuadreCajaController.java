package com.prog3.security.Controllers;
import com.prog3.security.Repositories.CuadreCajaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

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

import com.prog3.security.Services.CierreCajaService;
import com.prog3.security.Models.CierreCaja;

import com.prog3.security.DTOs.CuadreCajaRequest;
import com.prog3.security.Models.CuadreCaja;
import com.prog3.security.Models.Pedido;
import com.prog3.security.Repositories.PedidoRepository;
import com.prog3.security.Services.CuadreCajaService;
import com.prog3.security.Services.ResponseService;
import com.prog3.security.Services.ResumenCierreService;
import com.prog3.security.Services.ResumenCierreServiceUnificado;

import com.prog3.security.Utils.ApiResponse;

@CrossOrigin
@RestController
@RequestMapping("api/cuadres-caja")
public class CuadreCajaController {

    @Autowired
    private CierreCajaService cierreCajaService;

    @Autowired
    private ResumenCierreService resumenCierreService;
    
    @Autowired
    private ResumenCierreServiceUnificado resumenUnificadoService;

    @Autowired
    private CuadreCajaRepository cuadreCajaRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private CuadreCajaService cuadreCajaService;

    @Autowired
    private ResponseService responseService;

    /**
     * Endpoint para obtener el cuadre/cierre de caja del día (igual que
     * /api/reportes/cuadre-caja)
     */
    @GetMapping("/cuadre-completo")
    public ResponseEntity<ApiResponse<CierreCaja>> getCuadreCompleto() {
        try {
            // Buscar la caja abierta real
            List<CuadreCaja> cajasAbiertas = cuadreCajaRepository.findByCerradaFalse();
            if (cajasAbiertas.isEmpty()) {
                return responseService.notFound("No hay caja abierta en este momento");
            }
            CuadreCaja cajaAbierta = cajasAbiertas.get(0); // Tomar la primera caja abierta

            LocalDateTime inicioCaja = cajaAbierta.getFechaApertura();
            LocalDateTime finCaja = LocalDateTime.now();

            Map<String, Double> montosIniciales = new HashMap<>();
            montosIniciales.put("efectivo", cajaAbierta.getFondoInicialDesglosado() != null && cajaAbierta.getFondoInicialDesglosado().containsKey("Efectivo") ? cajaAbierta.getFondoInicialDesglosado().get("Efectivo") : cajaAbierta.getFondoInicial());
            montosIniciales.put("transferencias", cajaAbierta.getFondoInicialDesglosado() != null && cajaAbierta.getFondoInicialDesglosado().containsKey("Transferencia") ? cajaAbierta.getFondoInicialDesglosado().get("Transferencia") : 0.0);

            CierreCaja cierre = cierreCajaService.generarCierreCaja(inicioCaja, finCaja, cajaAbierta.getResponsable(), montosIniciales);
            return responseService.success(cierre, "Cuadre de caja generado exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al generar cuadre de caja: " + e.getMessage());
        }
    }
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
            // detalles.put("diferencia", cuadre.getDiferencia()); // Eliminado: diferencia
            detalles.put("cerrada", cuadre.isCerrada());

            return responseService.success(detalles, "Detalles del cuadre de caja obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener detalles del cuadre: " + e.getMessage());
        }
    }

    /**
     * Endpoint unificado para obtener el resumen completo de cierre de caja.
     * Incluye ventas, gastos, compras, movimientos de efectivo y resumen final.
     * Aplica correctamente la lógica de:
     * - Gastos y compras que salen de caja (se descuentan del efectivo esperado)
     * - Facturas de compra pagadas en efectivo (reducen el efectivo esperado)
     * - Pedidos eliminados (se restan de las ventas)
     */
    @GetMapping("/{id}/resumen-cierre")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getResumenCierre(@PathVariable String id) {
        try {
            System.out.println("🧾 Solicitando resumen de cierre unificado para cuadre: " + id);

            // Verificar que el cuadre existe
            CuadreCaja cuadre = cuadreCajaService.obtenerCuadrePorId(id);
            if (cuadre == null) {
                return responseService.notFound("Cuadre de caja no encontrado con ID: " + id);
            }

            // Generar el resumen completo usando el servicio unificado
            Map<String, Object> resumen = resumenUnificadoService.generarResumenCuadre(id);

            // Información de debug para validar los cálculos
            System.out.println("✅ Resumen de cierre unificado generado exitosamente");
            System.out.println("📊 Detalles del balance de efectivo:");
            if (resumen.containsKey("resumenFinal")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> resumenFinal = (Map<String, Object>) resumen.get("resumenFinal");
                System.out.println("  - Efectivo inicial: " + resumenFinal.getOrDefault("efectivoInicial", 0.0));
                System.out.println("  - Ventas en efectivo: " + resumenFinal.getOrDefault("ventasEfectivo", 0.0));
                System.out.println("  - Ingresos en efectivo: " + resumenFinal.getOrDefault("ingresosEfectivo", 0.0));
                System.out.println("  - Gastos en efectivo: " + resumenFinal.getOrDefault("gastosEfectivo", 0.0));
                System.out.println("  - Compras en efectivo: " + resumenFinal.getOrDefault("comprasEfectivo", 0.0));
                System.out.println("  - Efectivo esperado: " + resumenFinal.getOrDefault("efectivoEsperado", 0.0));
                System.out.println("  - Efectivo real: " + resumenFinal.getOrDefault("efectivoReal", 0.0));
                System.out.println("  - Diferencia: " + resumenFinal.getOrDefault("diferencia", 0.0));
                // Información sobre pedidos eliminados
                System.out.println("  - Ventas eliminadas: " + resumenFinal.getOrDefault("ventasEliminadas", 0.0));
                System.out.println("  - Pedidos eliminados: " + resumenFinal.getOrDefault("totalPedidosEliminados", 0));
            }

            return responseService.success(resumen, "Resumen de cierre unificado generado exitosamente");

        } catch (Exception e) {
            System.err.println("❌ Error al generar resumen de cierre: " + e.getMessage());
            e.printStackTrace();
            return responseService.internalError("Error al generar resumen de cierre: " + e.getMessage());
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

    @GetMapping("/debug-pedidos")
    public ResponseEntity<ApiResponse<Map<String, Object>>> debugPedidos() {
        try {
            LocalDateTime inicioDia = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);


            // Buscar caja activa para filtrar pedidos
            List<CuadreCaja> cuadresActivos = cuadreCajaRepository.findByFechaAperturaHoy(inicioDia)
                .stream().filter(c -> !c.isCerrada()).toList();
            List<Pedido> todosPedidos;
            if (!cuadresActivos.isEmpty()) {
                String cuadreCajaId = cuadresActivos.get(0).get_id();
                todosPedidos = pedidoRepository.findByCuadreCajaIdAndEstado(cuadreCajaId, "activo");
            } else {
                todosPedidos = pedidoRepository.findByFechaGreaterThanEqual(inicioDia);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("totalPedidosHoy", todosPedidos.size());
            response.put("inicioDia", inicioDia);

            // Agrupar por estado
            Map<String, Long> pedidosPorEstado = todosPedidos.stream()
                    .collect(Collectors.groupingBy(Pedido::getEstado, Collectors.counting()));
            response.put("pedidosPorEstado", pedidosPorEstado);

            // Agrupar por forma de pago (solo los pagados)
            Map<String, Long> pedidosPorFormaPago = todosPedidos.stream()
                    .filter(p -> "pagado".equals(p.getEstado()))
                    .collect(Collectors.groupingBy(
                            p -> p.getFormaPago() != null ? p.getFormaPago() : "sin_forma_pago",
                            Collectors.counting()
                    ));
            response.put("pedidosPorFormaPago", pedidosPorFormaPago);

            // Calcular totales por forma de pago
            Map<String, Double> totalesPorFormaPago = todosPedidos.stream()
                    .filter(p -> "pagado".equals(p.getEstado()))
                    .collect(Collectors.groupingBy(
                            p -> p.getFormaPago() != null ? p.getFormaPago() : "sin_forma_pago",
                            Collectors.summingDouble(Pedido::getTotalPagado)
                    ));
            response.put("totalesPorFormaPago", totalesPorFormaPago);

            // Mostrar algunos pedidos de ejemplo
            List<Map<String, Object>> ejemplosPedidos = todosPedidos.stream()
                    .limit(5)
                    .map(p -> {
                        Map<String, Object> pedidoInfo = new HashMap<>();
                        pedidoInfo.put("id", p.get_id());
                        pedidoInfo.put("estado", p.getEstado());
                        pedidoInfo.put("formaPago", p.getFormaPago());
                        pedidoInfo.put("totalPagado", p.getTotalPagado());
                        pedidoInfo.put("fecha", p.getFecha());
                        return pedidoInfo;
                    })
                    .collect(Collectors.toList());
            response.put("ejemplosPedidos", ejemplosPedidos);

            return responseService.success(response, "Debug de pedidos obtenido exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener debug de pedidos: " + e.getMessage());
        }
    }

    @GetMapping("/detalles-ventas")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDetallesVentas() {
        try {
            Map<String, Object> detalles = cuadreCajaService.calcularDetallesVentas();
            return responseService.success(detalles, "Detalles de ventas obtenidos exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener detalles de ventas: " + e.getMessage());
        }
    }

    @GetMapping("/todos-pedidos-hoy")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTodosPedidosHoy() {
        try {
            LocalDateTime inicioDia = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            // Usar ambos métodos para comparar

            // Buscar caja activa para filtrar pedidos
            List<CuadreCaja> cuadresActivos = cuadreCajaRepository.findByFechaAperturaHoy(inicioDia)
                .stream().filter(c -> !c.isCerrada()).toList();
            List<Pedido> todosPedidos;
            List<Pedido> pedidosPagadosPorFechaPago;
            if (!cuadresActivos.isEmpty()) {
                String cuadreCajaId = cuadresActivos.get(0).get_id();
                todosPedidos = pedidoRepository.findByCuadreCajaIdAndEstado(cuadreCajaId, "activo");
                pedidosPagadosPorFechaPago = pedidoRepository.findByCuadreCajaIdAndEstado(cuadreCajaId, "pagado");
            } else {
                todosPedidos = pedidoRepository.findByFechaGreaterThanEqual(inicioDia);
                pedidosPagadosPorFechaPago = pedidoRepository.findByFechaPagoGreaterThanEqualAndEstado(inicioDia, "pagado");
            }

            Map<String, Object> response = new HashMap<>();
            response.put("totalPedidosCreados", todosPedidos.size());
            response.put("totalPedidosPagadosPorFechaPago", pedidosPagadosPorFechaPago.size());
            response.put("inicioDia", inicioDia);

            // Crear lista detallada de todos los pedidos pagados (por fecha de pago)
            List<Map<String, Object>> pedidosDetalle = pedidosPagadosPorFechaPago.stream()
                    .map(p -> {
                        Map<String, Object> pedidoInfo = new HashMap<>();
                        pedidoInfo.put("id", p.get_id());
                        pedidoInfo.put("estado", p.getEstado());
                        pedidoInfo.put("formaPago", p.getFormaPago());
                        pedidoInfo.put("formaPagoLength", p.getFormaPago() != null ? p.getFormaPago().length() : null);
                        pedidoInfo.put("totalPagado", p.getTotalPagado());
                        pedidoInfo.put("fechaCreacion", p.getFecha());
                        pedidoInfo.put("fechaPago", p.getFechaPago());
                        pedidoInfo.put("mesa", p.getMesa() != null ? p.getMesa() : "Sin mesa");
                        return pedidoInfo;
                    })
                    .collect(Collectors.toList());

            response.put("pedidosPagados", pedidosDetalle);

            return responseService.success(response, "Todos los pedidos pagados de hoy obtenidos exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener todos los pedidos: " + e.getMessage());
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

            // NUEVA VALIDACIÓN: Verificar que no haya cajas abiertas antes de crear una nueva
            List<CuadreCaja> cajasAbiertas = cuadreCajaService.obtenerTodosCuadres().stream()
                    .filter(c -> !c.isCerrada())
                    .toList();

            if (!cajasAbiertas.isEmpty()) {
                CuadreCaja cajaAbierta = cajasAbiertas.get(0);
                return responseService.conflict(
                        "Ya existe una caja abierta. ID: " + cajaAbierta.get_id()
                        + ", Responsable: " + cajaAbierta.getResponsable()
                        + ". Debe cerrar la caja actual antes de abrir una nueva."
                );
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

            // NUEVO: Migrar automáticamente pedidos pagados sin cuadre asignado
            int pedidosMigrados = cuadreCajaService.migrarPedidosAutomaticamente(nuevoCuadre.get_id());
            if (pedidosMigrados > 0) {
                System.out.println("✅ Se migraron automáticamente " + pedidosMigrados + " pedidos al nuevo cuadre");
            }

            return responseService.created(nuevoCuadre, "Cuadre de caja creado exitosamente");
        } catch (Exception e) {
            System.out.println("Error al crear cuadre de caja: " + e.getMessage());
            e.printStackTrace();
            return responseService.internalError("Error al crear cuadre de caja: " + e.getMessage());
        }
    }

    /**
     * Endpoint para generar el cuadre completo de una caja.
     * Este es un alias del endpoint unificado /{id}/resumen-cierre para mantener compatibilidad con el frontend.
     * Incluye el mismo cálculo unificado que maneja correctamente:
     * - Las facturas de compras pagadas desde caja (se descuentan del efectivo esperado)
     * - Los gastos que salen de caja (se descuentan del efectivo esperado)
     * - Los pedidos eliminados (se restan de las ventas)
     */
    @GetMapping("/{id}/generar-cuadre")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generarCuadre(@PathVariable String id) {
        System.out.println("🧾 Usando endpoint unificado para generar-cuadre: " + id);
        // Simplemente redirigimos al endpoint unificado
        return getResumenCierre(id);
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

            // NUEVA VALIDACIÓN: No permitir cerrar una caja que ya está cerrada
            if (request.isCerrarCaja() && cuadreExistente.isCerrada()) {
                return responseService.badRequest(
                        "La caja ya está cerrada. No se puede cerrar una caja que ya ha sido cerrada."
                        + " Fecha de cierre anterior: " + cuadreExistente.getFechaCierre()
                );
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

            // Crear un request con los datos actuales y el flag de cierre
            CuadreCajaRequest cajaRequest = new CuadreCajaRequest();
            cajaRequest.setNombre(cuadre.getNombre());
            cajaRequest.setResponsable(cuadre.getResponsable());
            cajaRequest.setFondoInicial(cuadre.getFondoInicial());
            cajaRequest.setFondoInicialDesglosado(cuadre.getFondoInicialDesglosado());
            cajaRequest.setObservaciones(cuadre.getObservaciones());

            // Mantener los datos de ventas
            cajaRequest.setTotalVentas(cuadre.getTotalVentas());
            cajaRequest.setVentasDesglosadas(cuadre.getVentasDesglosadas());
            cajaRequest.setTotalPropinas(cuadre.getTotalPropinas());
            // cajaRequest.setTotalDomicilios(cuadre.getTotalDomicilios()); // Eliminado: totalDomicilios

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

    /**
     * Endpoint para migrar pedidos pagados sin cuadre asignado Asigna pedidos
     * pagados en un rango de fechas al cuadre especificado
     */
    @PostMapping("/{id}/migrar-pedidos")
    public ResponseEntity<ApiResponse<Map<String, Object>>> migrarPedidosACuadre(
            @PathVariable String id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        try {
            // Verificar que el cuadre existe
            CuadreCaja cuadre = cuadreCajaService.obtenerCuadrePorId(id);
            if (cuadre == null) {
                return responseService.notFound("Cuadre de caja no encontrado con ID: " + id);
            }

            // Obtener pedidos pagados sin cuadre en el rango de fechas
            List<Pedido> pedidosSinCuadre = pedidoRepository.findPedidosPagadosSinCuadreEnRango(fechaInicio, fechaFin);

            int pedidosMigrados = 0;
            double totalMigrado = 0.0;

            for (Pedido pedido : pedidosSinCuadre) {
                pedido.setCuadreCajaId(id);
                pedidoRepository.save(pedido);
                pedidosMigrados++;
                totalMigrado += pedido.getTotalPagado();
            }

            Map<String, Object> resultado = new HashMap<>();
            resultado.put("pedidosMigrados", pedidosMigrados);
            resultado.put("totalMigrado", totalMigrado);
            resultado.put("cuadreId", id);
            resultado.put("fechaInicio", fechaInicio);
            resultado.put("fechaFin", fechaFin);

            return responseService.success(resultado,
                    "Se migraron " + pedidosMigrados + " pedidos al cuadre de caja");

        } catch (Exception e) {
            return responseService.internalError("Error al migrar pedidos: " + e.getMessage());
        }
    }

    /**
     * Endpoint para obtener pedidos pagados sin cuadre asignado
     */
    @GetMapping("/pedidos-sin-cuadre")
    public ResponseEntity<ApiResponse<List<Pedido>>> getPedidosSinCuadre() {
        try {
            List<Pedido> pedidosSinCuadre = pedidoRepository.findPedidosPagadosSinCuadre();
            return responseService.success(pedidosSinCuadre,
                    "Pedidos sin cuadre asignado obtenidos: " + pedidosSinCuadre.size());
        } catch (Exception e) {
            return responseService.internalError("Error al obtener pedidos sin cuadre: " + e.getMessage());
        }
    }

    /**
     * Endpoint para migrar manualmente todos los pedidos sin cuadre al cuadre
     * activo
     */
    @PostMapping("/migrar-pedidos-sin-cuadre")
    public ResponseEntity<ApiResponse<String>> migrarPedidosSinCuadre() {
        try {
            // Obtener cuadre activo
            CuadreCaja cuadreActivo = cuadreCajaService.obtenerCuadreActivo();
            if (cuadreActivo == null) {
                return responseService.badRequest("No hay ningún cuadre de caja activo");
            }

            // Obtener pedidos sin cuadre
            List<Pedido> pedidosSinCuadre = pedidoRepository.findPedidosPagadosSinCuadre();

            System.out.println("🔄 Migrando " + pedidosSinCuadre.size() + " pedidos al cuadre " + cuadreActivo.get_id());

            // Asignar todos los pedidos al cuadre activo
            int migrados = 0;
            for (Pedido pedido : pedidosSinCuadre) {
                pedido.setCuadreCajaId(cuadreActivo.get_id());
                pedidoRepository.save(pedido);
                migrados++;
            }

            System.out.println("✅ Se migraron " + migrados + " pedidos al cuadre " + cuadreActivo.get_id());

            return responseService.success(
                    "PEDIDOS_MIGRADOS",
                    "Se migraron " + migrados + " pedidos al cuadre activo '" + cuadreActivo.getNombre() + "'"
            );
        } catch (Exception e) {
            System.err.println("❌ Error al migrar pedidos: " + e.getMessage());
            return responseService.internalError("Error al migrar pedidos: " + e.getMessage());
        }
    }

    /**
     * Endpoint para obtener estadísticas consolidadas del panel de administración
     * por rango de fechas. Incluye totales de ventas, gastos, compras, ingresos
     * y otras métricas agregadas de múltiples cuadres de caja.
     */
    @GetMapping("/admin/estadisticas-consolidadas")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEstadisticasConsolidadas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        try {
            System.out.println("📊 Generando estadísticas consolidadas del " + fechaInicio + " al " + fechaFin);

            Map<String, Object> estadisticas = new HashMap<>();
            
            // Obtener todos los cuadres de caja en el rango de fechas
            List<CuadreCaja> cuadresEnRango = cuadreCajaRepository.findByFechaAperturaBetween(fechaInicio, fechaFin);
            
            if (cuadresEnRango.isEmpty()) {
                estadisticas.put("mensaje", "No se encontraron cuadres de caja en el rango de fechas especificado");
                estadisticas.put("totalCuadres", 0);
                return responseService.success(estadisticas, "No hay datos en el rango especificado");
            }

            // Variables para acumular totales
            double totalVentas = 0.0;
            double totalVentasEfectivo = 0.0;
            double totalVentasTransferencia = 0.0;
            double totalVentasTarjeta = 0.0;
            double totalVentasOtro = 0.0;
            
            double totalGastos = 0.0;
            double totalGastosEfectivo = 0.0;
            double totalCompras = 0.0;
            double totalComprasEfectivo = 0.0;
            
            int totalPedidos = 0;
            int totalPedidosEfectivo = 0;
            int totalPedidosTransferencia = 0;
            int totalPedidosTarjeta = 0;
            int totalPedidosOtro = 0;
            
            double totalPropinas = 0.0;
            
            Map<String, Double> resumenPorFormaPago = new HashMap<>();
            Map<String, Integer> cantidadPorFormaPago = new HashMap<>();
            
            // Procesar cada cuadre en el rango
            for (CuadreCaja cuadre : cuadresEnRango) {
                try {
                    Map<String, Object> resumenCuadre = resumenUnificadoService.generarResumenCuadre(cuadre.get_id());
                    
                    // Extraer datos del resumen de ventas
                    if (resumenCuadre.containsKey("resumenVentas")) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> ventas = (Map<String, Object>) resumenCuadre.get("resumenVentas");
                        
                        totalVentas += (Double) ventas.getOrDefault("totalVentas", 0.0);
                        totalPedidos += (Integer) ventas.getOrDefault("totalPedidos", 0);
                        
                        // Ventas por forma de pago
                        @SuppressWarnings("unchecked")
                        Map<String, Double> ventasPorFormaPago = (Map<String, Double>) ventas.getOrDefault("ventasPorFormaPago", new HashMap<>());
                        
                        totalVentasEfectivo += ventasPorFormaPago.getOrDefault("efectivo", 0.0);
                        totalVentasTransferencia += ventasPorFormaPago.getOrDefault("transferencia", 0.0);
                        totalVentasTarjeta += ventasPorFormaPago.getOrDefault("tarjeta", 0.0);
                        totalVentasOtro += ventasPorFormaPago.getOrDefault("otro", 0.0);
                        
                        // Cantidad de pedidos por forma de pago
                        @SuppressWarnings("unchecked")
                        Map<String, Integer> cantidadPorForma = (Map<String, Integer>) ventas.getOrDefault("cantidadPorFormaPago", new HashMap<>());
                        
                        totalPedidosEfectivo += cantidadPorForma.getOrDefault("efectivo", 0);
                        totalPedidosTransferencia += cantidadPorForma.getOrDefault("transferencia", 0);
                        totalPedidosTarjeta += cantidadPorForma.getOrDefault("tarjeta", 0);
                        totalPedidosOtro += cantidadPorForma.getOrDefault("otro", 0);
                        
                        // Acumular en el resumen general
                        ventasPorFormaPago.forEach((forma, monto) -> {
                            resumenPorFormaPago.merge(forma, monto, Double::sum);
                        });
                        cantidadPorForma.forEach((forma, cantidad) -> {
                            cantidadPorFormaPago.merge(forma, cantidad, Integer::sum);
                        });
                    }
                    
                    // Extraer datos de gastos
                    if (resumenCuadre.containsKey("resumenGastos")) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> gastos = (Map<String, Object>) resumenCuadre.get("resumenGastos");
                        
                        totalGastos += (Double) gastos.getOrDefault("totalGastosIncluyendoFacturas", 0.0);
                        
                        @SuppressWarnings("unchecked")
                        Map<String, Double> gastosPorFormaPago = (Map<String, Double>) gastos.getOrDefault("gastosPorFormaPago", new HashMap<>());
                        totalGastosEfectivo += gastosPorFormaPago.getOrDefault("efectivo", 0.0);
                    }
                    
                    // Extraer datos de compras
                    if (resumenCuadre.containsKey("resumenCompras")) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> compras = (Map<String, Object>) resumenCuadre.get("resumenCompras");
                        
                        totalCompras += (Double) compras.getOrDefault("totalComprasGenerales", 0.0);
                        totalComprasEfectivo += (Double) compras.getOrDefault("totalComprasDesdeCaja", 0.0);
                    }
                    
                    // Calcular propinas de los pedidos del cuadre
                    List<Pedido> pedidosCuadre = pedidoRepository.findByCuadreCajaIdAndEstado(cuadre.get_id(), "pagado");
                    for (Pedido pedido : pedidosCuadre) {
                        totalPropinas += pedido.getPropina();
                    }
                    
                } catch (Exception e) {
                    System.err.println("⚠️ Error procesando cuadre " + cuadre.get_id() + ": " + e.getMessage());
                    // Continuar con el siguiente cuadre
                }
            }
            
            // Construir respuesta consolidada
            estadisticas.put("rangoFechas", Map.of(
                "fechaInicio", fechaInicio,
                "fechaFin", fechaFin
            ));
            
            estadisticas.put("totalCuadres", cuadresEnRango.size());
            
            // Totales de ventas
            Map<String, Object> resumenVentas = new HashMap<>();
            resumenVentas.put("totalVentas", totalVentas);
            resumenVentas.put("totalPedidos", totalPedidos);
            resumenVentas.put("totalPropinas", totalPropinas);
            
            Map<String, Object> ventasPorFormaPago = new HashMap<>();
            ventasPorFormaPago.put("efectivo", totalVentasEfectivo);
            ventasPorFormaPago.put("transferencia", totalVentasTransferencia);
            ventasPorFormaPago.put("tarjeta", totalVentasTarjeta);
            ventasPorFormaPago.put("otro", totalVentasOtro);
            resumenVentas.put("ventasPorFormaPago", ventasPorFormaPago);
            
            Map<String, Object> pedidosPorFormaPago = new HashMap<>();
            pedidosPorFormaPago.put("efectivo", totalPedidosEfectivo);
            pedidosPorFormaPago.put("transferencia", totalPedidosTransferencia);
            pedidosPorFormaPago.put("tarjeta", totalPedidosTarjeta);
            pedidosPorFormaPago.put("otro", totalPedidosOtro);
            resumenVentas.put("pedidosPorFormaPago", pedidosPorFormaPago);
            
            estadisticas.put("resumenVentas", resumenVentas);
            
            // Totales de gastos y compras
            Map<String, Object> resumenGastosCompras = new HashMap<>();
            resumenGastosCompras.put("totalGastos", totalGastos);
            resumenGastosCompras.put("totalGastosEfectivo", totalGastosEfectivo);
            resumenGastosCompras.put("totalCompras", totalCompras);
            resumenGastosCompras.put("totalComprasEfectivo", totalComprasEfectivo);
            estadisticas.put("resumenGastosCompras", resumenGastosCompras);
            
            // Métricas generales
            Map<String, Object> metricas = new HashMap<>();
            metricas.put("utilidadBrutaTotal", totalVentas - totalCompras);
            metricas.put("promedioVentasPorCuadre", totalVentas / cuadresEnRango.size());
            metricas.put("promedioVentasPorPedido", totalPedidos > 0 ? totalVentas / totalPedidos : 0.0);
            metricas.put("porcentajeVentasEfectivo", totalVentas > 0 ? (totalVentasEfectivo / totalVentas) * 100 : 0.0);
            metricas.put("porcentajeVentasTransferencia", totalVentas > 0 ? (totalVentasTransferencia / totalVentas) * 100 : 0.0);
            estadisticas.put("metricas", metricas);
            
            System.out.println("✅ Estadísticas consolidadas generadas: " + cuadresEnRango.size() + " cuadres, $" + totalVentas + " en ventas totales");
            
            return responseService.success(estadisticas, "Estadísticas consolidadas generadas exitosamente");
            
        } catch (Exception e) {
            System.err.println("❌ Error al generar estadísticas consolidadas: " + e.getMessage());
            e.printStackTrace();
            return responseService.internalError("Error al generar estadísticas consolidadas: " + e.getMessage());
        }
    }
}
