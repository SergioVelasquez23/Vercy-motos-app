package com.prog3.security.Controllers;
import com.prog3.security.Models.CuadreCaja;
import com.prog3.security.Repositories.CuadreCajaRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.prog3.security.Services.RoleValidatorService;
import com.prog3.security.Services.CierreCajaService;

import jakarta.servlet.http.HttpServletRequest;

import com.prog3.security.DTOs.CuadreCajaRequest;
import com.prog3.security.Models.Factura;
import com.prog3.security.Models.Inventario;
import com.prog3.security.Models.CierreCaja;
import com.prog3.security.Models.MovimientoInventario;
import com.prog3.security.Models.Pedido;
import com.prog3.security.Repositories.FacturaRepository;
import com.prog3.security.Repositories.InventarioRepository;
import com.prog3.security.Repositories.MovimientoInventarioRepository;
import com.prog3.security.Repositories.ProductoRepository;
import com.prog3.security.Repositories.PedidoRepository;
import com.prog3.security.Services.ResponseService;

import com.prog3.security.Utils.ApiResponse;
import com.prog3.security.Services.ReporteService;
import com.prog3.security.Services.JwtService;

@CrossOrigin
@RestController
@RequestMapping("api/reportes")
public class ReportesController {

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private CuadreCajaRepository cuadreCajaRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private MovimientoInventarioRepository movimientoInventarioRepository;

    @Autowired
    private ReporteService reporteService;

    @Autowired
    private ResponseService responseService;

    @Autowired
    private RoleValidatorService roleValidator;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CierreCajaService cierreCajaService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard(HttpServletRequest request) {
        try {
            // Obtenemos el token del encabezado para verificar manualmente los roles
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                List<String> roles = jwtService.getRolesFromToken(token);

                System.out.println("Roles del usuario: " + roles);

                // Verificar si el usuario tiene alguno de los roles requeridos
                if (!(roles.contains("SUPERADMIN") || roles.contains("ADMIN"))) {
                    System.out.println("Usuario no tiene roles requeridos para dashboard");
                    return responseService.forbidden("No tienes permisos para acceder al dashboard. Requiere rol ADMIN o SUPERADMIN.");
                }
            } else {
                System.out.println("No se proporcionó token de autenticación");
                return responseService.unauthorized("No se proporcionó token de autenticación");
            }

            // Si llegó hasta aquí, tiene los permisos correctos
            Map<String, Object> dashboard = reporteService.getDashboard();

            return responseService.success(dashboard, "Dashboard obtenido exitosamente");
        } catch (Exception e) {
            e.printStackTrace(); // Agregamos esto para tener más detalles del error en los logs
            System.out.println("Error específico en dashboard: " + e.getMessage());
            System.out.println("Causa: " + (e.getCause() != null ? e.getCause().getMessage() : "desconocida"));
            return responseService.internalError("Error al obtener dashboard: " + e.getMessage());
        }
    }

    @GetMapping("/ventas-periodo")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getVentasPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        try {
            Map<String, Object> reporte = reporteService.getVentasPorPeriodo(fechaInicio, fechaFin);
            return responseService.success(reporte, "Reporte de ventas por período obtenido exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener reporte de ventas: " + e.getMessage());
        }
    }

    @GetMapping("/inventario-valorizado")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getInventarioValorizado() {
        try {
            List<Inventario> inventarios = inventarioRepository.findAll();

            Map<String, Object> reporte = new HashMap<>();

            double valorTotal = inventarios.stream().mapToDouble(Inventario::getCostoTotal).sum();

            // Agrupar por categoría
            Map<String, Double> valorPorCategoria = inventarios.stream()
                    .filter(i -> i.getCategoria() != null)
                    .collect(Collectors.groupingBy(
                            Inventario::getCategoria,
                            Collectors.summingDouble(Inventario::getCostoTotal)
                    ));

            // Agrupar por ubicación
            Map<String, Double> valorPorUbicacion = inventarios.stream()
                    .filter(i -> i.getUbicacion() != null)
                    .collect(Collectors.groupingBy(
                            Inventario::getUbicacion,
                            Collectors.summingDouble(Inventario::getCostoTotal)
                    ));

            reporte.put("valorTotal", valorTotal);
            reporte.put("cantidadProductos", inventarios.size());
            reporte.put("valorPorCategoria", valorPorCategoria);
            reporte.put("valorPorUbicacion", valorPorUbicacion);

            // Top 10 productos más valiosos
            List<Inventario> masValiosos = inventarios.stream()
                    .sorted((a, b) -> Double.compare(b.getCostoTotal(), a.getCostoTotal()))
                    .limit(10)
                    .collect(Collectors.toList());
            reporte.put("productosMasValiosos", masValiosos);

            return responseService.success(reporte, "Inventario valorizado obtenido exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener inventario valorizado: " + e.getMessage());
        }
    }

    @GetMapping("/movimientos-inventario")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMovimientosInventario(
            @RequestParam(defaultValue = "7") int dias) {
        try {
            LocalDateTime fechaInicio = LocalDateTime.now().minusDays(dias);
            List<MovimientoInventario> movimientos = movimientoInventarioRepository.findByFechaGreaterThanEqual(fechaInicio);

            Map<String, Object> reporte = new HashMap<>();

            // Agrupar por tipo de movimiento
            Map<String, Long> movimientosPorTipo = movimientos.stream()
                    .collect(Collectors.groupingBy(
                            MovimientoInventario::getTipoMovimiento,
                            Collectors.counting()
                    ));

            // Agrupar por motivo
            Map<String, Long> movimientosPorMotivo = movimientos.stream()
                    .collect(Collectors.groupingBy(
                            MovimientoInventario::getMotivo,
                            Collectors.counting()
                    ));

            double totalEntradas = movimientos.stream()
                    .filter(m -> "entrada".equals(m.getTipoMovimiento()))
                    .mapToDouble(MovimientoInventario::getCostoTotal)
                    .sum();

            double totalSalidas = movimientos.stream()
                    .filter(m -> "salida".equals(m.getTipoMovimiento()))
                    .mapToDouble(MovimientoInventario::getCostoTotal)
                    .sum();

            reporte.put("periodo", dias + " días");
            reporte.put("totalMovimientos", movimientos.size());
            reporte.put("movimientosPorTipo", movimientosPorTipo);
            reporte.put("movimientosPorMotivo", movimientosPorMotivo);
            reporte.put("valorEntradas", totalEntradas);
            reporte.put("valorSalidas", totalSalidas);

            return responseService.success(reporte, "Movimientos de inventario obtenidos exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener movimientos de inventario: " + e.getMessage());
        }
    }

    @GetMapping("/alertas")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAlertas() {
        try {
            Map<String, Object> alertas = new HashMap<>();

            // Stock bajo
            List<Inventario> stockBajo = inventarioRepository.findProductosConStockBajo();

            // Productos agotados
            List<Inventario> agotados = inventarioRepository.findProductosAgotados();

            // Productos próximos a vencer (30 días)
            LocalDateTime fechaLimite = LocalDateTime.now().plusDays(30);
            List<Inventario> proximosVencer = inventarioRepository.findProductosProximosAVencer(fechaLimite);

            // En el modelo simplificado no hay facturas vencidas
            List<Factura> facturasVencidas = List.of(); // Lista vacía

            alertas.put("stockBajo", Map.of(
                    "cantidad", stockBajo.size(),
                    "productos", stockBajo
            ));

            alertas.put("agotados", Map.of(
                    "cantidad", agotados.size(),
                    "productos", agotados
            ));

            alertas.put("proximosVencer", Map.of(
                    "cantidad", proximosVencer.size(),
                    "productos", proximosVencer
            ));

            alertas.put("facturasVencidas", Map.of(
                    "cantidad", facturasVencidas.size(),
                    "facturas", facturasVencidas
            ));

            return responseService.success(alertas, "Alertas obtenidas exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener alertas: " + e.getMessage());
        }
    }

    // Endpoints de Cuadre de Caja
    @GetMapping("/cuadre-caja")
    public ResponseEntity<ApiResponse<CierreCaja>> getCuadreCaja() {
        try {
            LocalDateTime inicioDia = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            LocalDateTime finDia = LocalDateTime.now();

            // Generar cierre temporal (sin guardar)
            Map<String, Double> montosIniciales = new HashMap<>();
            montosIniciales.put("efectivo", 0.0); // Por defecto, se puede parametrizar
            montosIniciales.put("transferencias", 0.0);

            CierreCaja cierre = cierreCajaService.generarCierreCaja(inicioDia, finDia, "Sistema", montosIniciales);

            return responseService.success(cierre, "Cuadre de caja generado exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al generar cuadre de caja: " + e.getMessage());
        }
    }

    @PostMapping("/cuadre-caja/cerrar")
    public ResponseEntity<ApiResponse<CierreCaja>> cerrarCaja(@RequestBody Map<String, Object> request) {
        try {
            LocalDateTime inicioDia = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            LocalDateTime finDia = LocalDateTime.now();

            String responsable = (String) request.get("responsable");
            Double efectivoDeclarado = ((Number) request.get("efectivoDeclarado")).doubleValue();
            String observaciones = (String) request.getOrDefault("observaciones", "");

            // Obtener montos iniciales del request
            @SuppressWarnings("unchecked")
            Map<String, Object> montosInicialesObj = (Map<String, Object>) request.getOrDefault("montosIniciales", new HashMap<>());
            Map<String, Double> montosIniciales = new HashMap<>();
            montosInicialesObj.forEach((key, value) -> {
                if (value instanceof Number) {
                    montosIniciales.put(key, ((Number) value).doubleValue());
                }
            });

            // Si no hay montos iniciales, usar valores por defecto
            if (montosIniciales.isEmpty()) {
                montosIniciales.put("efectivo", 0.0);
                montosIniciales.put("transferencias", 0.0);
            }

            // Generar y cerrar caja
            CierreCaja cierre = cierreCajaService.generarCierreCaja(inicioDia, finDia, responsable, montosIniciales);
            CierreCaja cierreCerrado = cierreCajaService.cerrarCaja(cierre, observaciones);

            return responseService.success(cierreCerrado, "Caja cerrada exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al cerrar caja: " + e.getMessage());
        }
    }

    @GetMapping("/cuadre-caja/historial")
    public ResponseEntity<ApiResponse<List<CierreCaja>>> getHistorialCuadres(
            @RequestParam(defaultValue = "30") int limite) {
        try {
            List<CierreCaja> historial = cierreCajaService.getHistorialCierres(limite);
            return responseService.success(historial, "Historial de cuadres obtenido");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener historial: " + e.getMessage());
        }
    }

    @GetMapping("/cuadre-caja/ultimo")
    public ResponseEntity<ApiResponse<CierreCaja>> getUltimoCierre() {
        try {
            CierreCaja ultimoCierre = cierreCajaService.getUltimoCierre();
            if (ultimoCierre != null) {
                return responseService.success(ultimoCierre, "Último cierre obtenido");
            } else {
                return responseService.notFound("No se encontró ningún cierre de caja");
            }
        } catch (Exception e) {
            return responseService.internalError("Error al obtener último cierre: " + e.getMessage());
        }
    }

    @GetMapping("/ventas-por-hora")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getVentasPorHora(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fecha) {
        try {
            fecha = fecha != null ? fecha : LocalDateTime.now();
            List<Map<String, Object>> ventas = reporteService.getVentasPorHora(fecha);
            return responseService.success(ventas, "Ventas por hora obtenidas exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener ventas por hora: " + e.getMessage());
        }
    }

    @GetMapping("/pedidos-por-hora")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPedidosPorHora(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fecha) {
        try {
            LocalDateTime fechaConsulta = fecha != null ? fecha : LocalDateTime.now();
            List<Map<String, Object>> pedidosPorHora = reporteService.getPedidosPorHora(fechaConsulta);
            return responseService.success(pedidosPorHora, "Pedidos por hora obtenidos exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener pedidos por hora: " + e.getMessage());
        }
    }

    @GetMapping("/ventas-por-dia")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getVentasPorDia(
            @RequestParam(defaultValue = "7") int ultimosDias) {
        try {
            List<Map<String, Object>> ventasPorDia = reporteService.getVentasPorDia(ultimosDias);
            return responseService.success(ventasPorDia, "Ventas por día obtenidas exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener ventas por día: " + e.getMessage());
        }
    }

    @GetMapping("/top-productos")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTopProductos(
            @RequestParam(defaultValue = "5") int limite) {
        try {
            List<Map<String, Object>> topProductos = reporteService.getTopProductos(limite);
            return responseService.success(topProductos, "Top productos obtenidos exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener top productos: " + e.getMessage());
        }
    }

    // Método auxiliar para calcular total de pedido
    private double calcularTotalPedido(Pedido pedido) {
        if (pedido == null || pedido.getItems() == null || pedido.getItems().isEmpty()) {
            return 0.0;
        }

        return pedido.getItems().stream()
                .mapToDouble(item -> {
                    if (item == null) {
                        return 0.0;
                    }
                    double precio = item.getPrecio();
                    int cantidad = item.getCantidad();
                    return precio * cantidad;
                })
                .sum();
    }

    @DeleteMapping("/eliminar")
    public ResponseEntity<ApiResponse<Map<String, Object>>> eliminarReportes(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        try {
            Map<String, Object> resultado = new HashMap<>();

            // Eliminar facturas del período
            List<Factura> facturas = facturaRepository.findByFechaBetween(fechaInicio, fechaFin);
            for (Factura factura : facturas) {
                facturaRepository.deleteById(factura.get_id());
            }

            // Eliminar pedidos del período
            // Buscar caja activa para filtrar pedidos
            List<CuadreCaja> cuadresActivos = cuadreCajaRepository.findByFechaAperturaHoy(fechaInicio)
                .stream().filter(c -> !c.isCerrada()).toList();
            List<Pedido> pedidos;
            if (!cuadresActivos.isEmpty()) {
                String cuadreCajaId = cuadresActivos.get(0).get_id();
                pedidos = pedidoRepository.findByCuadreCajaIdAndEstado(cuadreCajaId, "pagado");
            } else {
                pedidos = pedidoRepository.findByFechaBetween(fechaInicio, fechaFin);
            }
            for (Pedido pedido : pedidos) {
                pedidoRepository.deleteById(pedido.get_id());
            }

            // Eliminar movimientos de inventario del período
            List<MovimientoInventario> movimientos = movimientoInventarioRepository.findByFechaBetween(fechaInicio, fechaFin);
            for (MovimientoInventario movimiento : movimientos) {
                movimientoInventarioRepository.deleteById(movimiento.get_id());
            }

            resultado.put("periodo", Map.of(
                    "inicio", fechaInicio.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    "fin", fechaFin.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            ));
            resultado.put("facturasEliminadas", facturas.size());
            resultado.put("pedidosEliminados", pedidos.size());
            resultado.put("movimientosEliminados", movimientos.size());
            resultado.put("totalEliminados", facturas.size() + pedidos.size() + movimientos.size());

            return responseService.success(resultado, "Reportes eliminados exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al eliminar reportes: " + e.getMessage());
        }
    }

    @DeleteMapping("/eliminar-todo")
    public ResponseEntity<ApiResponse<Map<String, Object>>> eliminarTodosLosReportes() {
        try {
            Map<String, Object> resultado = new HashMap<>();

            // Eliminar todas las facturas
            long facturasEliminadas = facturaRepository.count();
            facturaRepository.deleteAll();

            // Eliminar todos los pedidos
            long pedidosEliminados = pedidoRepository.count();
            pedidoRepository.deleteAll();

            // Eliminar todos los movimientos de inventario
            long movimientosEliminados = movimientoInventarioRepository.count();
            movimientoInventarioRepository.deleteAll();

            resultado.put("facturasEliminadas", facturasEliminadas);
            resultado.put("pedidosEliminados", pedidosEliminados);
            resultado.put("movimientosEliminados", movimientosEliminados);
            resultado.put("totalEliminados", facturasEliminadas + pedidosEliminados + movimientosEliminados);

            return responseService.success(resultado, "Todos los reportes han sido eliminados exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al eliminar los reportes: " + e.getMessage());
        }
    }

    @GetMapping("/ingresos-egresos")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getIngresosVsEgresos(
            @RequestParam(defaultValue = "12") int ultimosMeses) {
        try {
            List<Map<String, Object>> ingresosVsEgresos = reporteService.getIngresosVsEgresos(ultimosMeses);
            return responseService.success(ingresosVsEgresos, "Ingresos vs egresos obtenidos exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener ingresos vs egresos: " + e.getMessage());
        }
    }

    @GetMapping("/vendedores-mes")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getVendedoresDelMes(
            @RequestParam(defaultValue = "30") int dias) {
        try {
            LocalDateTime fechaInicio = LocalDateTime.now().minusDays(dias);

            // Obtener pedidos pagados del período
            List<Pedido> pedidosPagados = pedidoRepository.findByFechaBetween(fechaInicio, LocalDateTime.now())
                    .stream()
                    .filter(p -> "pagado".equals(p.getEstado()) && p.getMesero() != null)
                    .collect(Collectors.toList());

            // Obtener facturas del período
            List<Factura> facturas = facturaRepository.findByFechaBetween(fechaInicio, LocalDateTime.now())
                    .stream()
                    .filter(f -> f.getAtendidoPor() != null)
                    .collect(Collectors.toList());

            // Agrupar por vendedor/mesero
            Map<String, Double> ventasPorVendedor = new HashMap<>();
            Map<String, Integer> pedidosPorVendedor = new HashMap<>();

            // Contar pedidos pagados por mesero
            pedidosPagados.forEach(p -> {
                ventasPorVendedor.merge(p.getMesero(), p.getTotalPagado(), Double::sum);
                pedidosPorVendedor.merge(p.getMesero(), 1, Integer::sum);
            });

            // Contar facturas por quien atendió
            facturas.forEach(f -> {
                ventasPorVendedor.merge(f.getAtendidoPor(), f.getTotal(), Double::sum);
                pedidosPorVendedor.merge(f.getAtendidoPor(), 1, Integer::sum);
            });

            // Convertir a lista ordenada
            List<Map<String, Object>> vendedores = ventasPorVendedor.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .map(entry -> {
                        Map<String, Object> vendedor = new HashMap<>();
                        vendedor.put("nombre", entry.getKey());
                        vendedor.put("totalVentas", entry.getValue());
                        vendedor.put("cantidadPedidos", pedidosPorVendedor.getOrDefault(entry.getKey(), 0));
                        vendedor.put("promedioVenta", pedidosPorVendedor.getOrDefault(entry.getKey(), 0) > 0
                                ? entry.getValue() / pedidosPorVendedor.get(entry.getKey()) : 0);
                        return vendedor;
                    })
                    .collect(Collectors.toList());

            return responseService.success(vendedores, "Vendedores del mes obtenidos exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener vendedores del mes: " + e.getMessage());
        }
    }

    @GetMapping("/ultimos-pedidos")
    public ResponseEntity<ApiResponse<List<Pedido>>> getUltimosPedidos(
            @RequestParam(defaultValue = "10") int limite) {
        try {
            List<Pedido> pedidos = pedidoRepository.findAll()
                    .stream()
                    .sorted((p1, p2) -> p2.getFecha().compareTo(p1.getFecha()))
                    .limit(limite)
                    .collect(Collectors.toList());

            return responseService.success(pedidos, "Últimos pedidos obtenidos exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener últimos pedidos: " + e.getMessage());
        }
    }

    @PutMapping("/objetivo")
    public ResponseEntity<ApiResponse<Map<String, Object>>> actualizarObjetivo(@RequestBody Map<String, Object> request) {
        try {
            String periodo = (String) request.get("periodo");
            Double objetivo = null;

            // Manejar diferentes tipos de datos para el objetivo
            Object objetivoObj = request.get("objetivo");
            if (objetivoObj instanceof Number) {
                objetivo = ((Number) objetivoObj).doubleValue();
            } else if (objetivoObj instanceof String) {
                try {
                    objetivo = Double.parseDouble((String) objetivoObj);
                } catch (NumberFormatException e) {
                    return responseService.badRequest("El objetivo debe ser un número válido");
                }
            }

            if (periodo == null || periodo.trim().isEmpty()) {
                return responseService.badRequest("El período es requerido");
            }

            if (objetivo == null || objetivo <= 0) {
                return responseService.badRequest("El objetivo debe ser un número mayor a 0");
            }

            System.out.println("🎯 Actualizando objetivo para período: " + periodo + " a $" + objetivo);

            // Llamar al servicio para actualizar el objetivo
            boolean actualizado = reporteService.actualizarObjetivo(periodo, objetivo);

            if (actualizado) {
                Map<String, Object> response = new HashMap<>();
                response.put("periodo", periodo);
                response.put("objetivo", objetivo);
                response.put("mensaje", "Objetivo actualizado exitosamente");

                System.out.println("✅ Objetivo actualizado exitosamente para " + periodo);
                return responseService.success(response, "Objetivo actualizado exitosamente");
            } else {
                System.out.println("❌ Error al actualizar objetivo para " + periodo);
                return responseService.internalError("Error al actualizar el objetivo");
            }

        } catch (Exception e) {
            System.out.println("❌ Excepción al actualizar objetivo: " + e.getMessage());
            e.printStackTrace();
            return responseService.internalError("Error interno al actualizar objetivo: " + e.getMessage());
        }
    }

    /**
     * Exportar estadísticas mensuales completas para trazabilidad
     * Incluye todas las tablas del dashboard y reportes de un mes específico
     */
    @GetMapping("/exportar-mes")
    public ResponseEntity<ApiResponse<Map<String, Object>>> exportarEstadisticasMensuales(
            @RequestParam int año,
            @RequestParam int mes,
            HttpServletRequest request) {
        try {
            // Validar permisos usando el mismo patrón del dashboard
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                List<String> roles = jwtService.getRolesFromToken(token);

                // Verificar si el usuario tiene alguno de los roles requeridos
                if (!(roles.contains("SUPERADMIN") || roles.contains("ADMIN"))) {
                    return responseService.forbidden("No tienes permisos para exportar estadísticas mensuales. Requiere rol ADMIN o SUPERADMIN.");
                }
                
                Map<String, Object> estadisticasMensuales = reporteService.exportarEstadisticasMensuales(año, mes);
                return responseService.success(estadisticasMensuales, 
                    String.format("Estadísticas de %02d/%d exportadas exitosamente", mes, año));
            } else {
                return responseService.unauthorized("No se proporcionó token de autenticación");
            }
        } catch (Exception e) {
            return responseService.internalError("Error al exportar estadísticas mensuales: " + e.getMessage());
        }
    }

    /**
     * Limpiar datos del mes después de exportar
     * Elimina pedidos, gastos, facturas, etc. del mes especificado
     */
    @DeleteMapping("/limpiar-mes")
    public ResponseEntity<ApiResponse<Map<String, Object>>> limpiarDatosMensuales(
            @RequestParam int año,
            @RequestParam int mes,
            @RequestParam(defaultValue = "false") boolean confirmar,
            HttpServletRequest request) {
        try {
            // Validar permisos (solo SUPERADMIN puede eliminar datos)
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                List<String> roles = jwtService.getRolesFromToken(token);

                // Solo SUPERADMIN puede eliminar datos
                if (!roles.contains("SUPERADMIN")) {
                    return responseService.forbidden("No tienes permisos para limpiar datos mensuales. Requiere rol SUPERADMIN.");
                }
                
                if (!confirmar) {
                    return responseService.badRequest("Debe confirmar la limpieza con el parámetro confirmar=true");
                }
                
                Map<String, Object> resultadoLimpieza = reporteService.limpiarDatosMensuales(año, mes);
                return responseService.success(resultadoLimpieza, 
                    String.format("Datos de %02d/%d eliminados exitosamente", mes, año));
            } else {
                return responseService.unauthorized("No se proporcionó token de autenticación");
            }
        } catch (Exception e) {
            return responseService.internalError("Error al limpiar datos mensuales: " + e.getMessage());
        }
    }
}
