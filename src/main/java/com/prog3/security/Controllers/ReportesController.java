package com.prog3.security.Controllers;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.prog3.security.DTOs.CuadreCajaRequest;
import com.prog3.security.Models.Factura;
import com.prog3.security.Models.Inventario;
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

@CrossOrigin
@RestController
@RequestMapping("api/reportes")
public class ReportesController {

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private MovimientoInventarioRepository movimientoInventarioRepository;

    @Autowired
    private ReporteService reporteService;

    @Autowired
    private ResponseService responseService;

    @Autowired
    private ProductoRepository productoRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard() {
        try {
            Map<String, Object> dashboard = reporteService.getDashboard();

            return responseService.success(dashboard, "Dashboard obtenido exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener dashboard: " + e.getMessage());
        }
    }

    @GetMapping("/ventas-periodo")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getVentasPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        try {
            List<Factura> facturas = facturaRepository.findByFechaBetween(fechaInicio, fechaFin);
            // En el modelo simplificado no hay estado, todas las facturas son válidas
            List<Factura> facturasValidas = facturas;

            Map<String, Object> reporte = new HashMap<>();

            double totalVentas = facturasValidas.stream().mapToDouble(Factura::getTotal).sum();

            reporte.put("periodo", Map.of(
                    "inicio", fechaInicio.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    "fin", fechaFin.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            ));

            reporte.put("resumen", Map.of(
                    "cantidadFacturas", facturasValidas.size(),
                    "totalVentas", totalVentas,
                    "totalImpuestos", 0.0, // No hay impuestos en el modelo simplificado
                    "totalDescuentos", 0.0, // No hay descuentos en el modelo simplificado
                    "totalPropinas", 0.0, // No hay propinas en el modelo simplificado
                    "promedioVenta", facturasValidas.isEmpty() ? 0 : totalVentas / facturasValidas.size()
            ));

            // Ventas por método de pago
            Map<String, Double> ventasPorMetodoPago = facturasValidas.stream()
                    .collect(Collectors.groupingBy(
                            Factura::getMedioPago,
                            Collectors.summingDouble(Factura::getTotal)
                    ));
            reporte.put("ventasPorMetodoPago", ventasPorMetodoPago);

            // En el modelo simplificado no hay tipo de servicio
            reporte.put("ventasPorTipoServicio", new HashMap<>());

            return responseService.success(reporte, "Reporte de ventas por período obtenido exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener reporte de ventas: " + e.getMessage());
        }
    }

    @GetMapping("/productos-mas-vendidos")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProductosMasVendidos(
            @RequestParam(defaultValue = "30") int dias,
            @RequestParam(defaultValue = "10") int limite) {
        try {
            LocalDateTime fechaInicio = LocalDateTime.now().minusDays(dias);

            // Obtener facturas del período
            List<Factura> facturas = facturaRepository.findByFechaGreaterThanEqual(fechaInicio);

            // Obtener pedidos pagados del período
            List<Pedido> pedidosPagados = pedidoRepository.findByFechaBetween(fechaInicio, LocalDateTime.now())
                    .stream()
                    .filter(p -> "pagado".equals(p.getEstado()))
                    .collect(Collectors.toList());

            Map<String, Object> estadisticas = new HashMap<>();
            Map<String, Integer> conteoProductos = new HashMap<>();
            Map<String, Double> ventasProductos = new HashMap<>();

            // Procesar facturas
            for (Factura factura : facturas) {
                if (factura.getItems() != null) {
                    for (var item : factura.getItems()) {
                        String nombreProducto = item.getProductoNombre();
                        conteoProductos.merge(nombreProducto, item.getCantidad(), Integer::sum);
                        ventasProductos.merge(nombreProducto, item.getTotalItem(), Double::sum);
                    }
                }
            }

            // Procesar pedidos pagados
            for (Pedido pedido : pedidosPagados) {
                if (pedido.getItems() != null) {
                    for (var item : pedido.getItems()) {
                        // Para pedidos, necesitamos usar el ID del producto
                        String productoId = item.getProductoId();
                        String nombreProducto = productoId; // Por defecto usar el ID

                        // Buscar el nombre del producto
                        try {
                            var producto = productoRepository.findById(productoId);
                            if (producto.isPresent()) {
                                nombreProducto = producto.get().getNombre();
                            }
                        } catch (Exception e) {
                            // En caso de error, usar el ID como nombre
                            nombreProducto = "Producto ID: " + productoId;
                        }

                        conteoProductos.merge(nombreProducto, item.getCantidad(), Integer::sum);
                        ventasProductos.merge(nombreProducto, item.getSubtotal(), Double::sum);
                    }
                }
            }

            // Ordenar por cantidad vendida
            List<Map<String, Object>> topProductos = conteoProductos.entrySet()
                    .stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(limite)
                    .map(entry -> {
                        Map<String, Object> producto = new HashMap<>();
                        producto.put("nombre", entry.getKey());
                        producto.put("cantidad", entry.getValue());
                        producto.put("totalVentas", ventasProductos.getOrDefault(entry.getKey(), 0.0));
                        return producto;
                    })
                    .collect(Collectors.toList());

            estadisticas.put("periodo", dias + " días");
            estadisticas.put("topProductos", topProductos);
            estadisticas.put("totalProductosVendidos", conteoProductos.values().stream().mapToInt(Integer::intValue).sum());
            estadisticas.put("totalVentasProductos", ventasProductos.values().stream().mapToDouble(Double::doubleValue).sum());

            return responseService.success(estadisticas, "Productos más vendidos obtenidos exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener productos más vendidos: " + e.getMessage());
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
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCuadreCaja() {
        try {
            Map<String, Object> cuadre = new HashMap<>();
            LocalDateTime inicioDia = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            LocalDateTime finDia = inicioDia.plusDays(1);

            // Obtener todas las ventas del día
            List<Factura> ventasHoy = facturaRepository.findVentasDelDia(inicioDia, finDia);

            // Calcular totales por método de pago
            double totalEfectivo = ventasHoy.stream()
                    .filter(f -> "Efectivo".equals(f.getMedioPago()))
                    .mapToDouble(Factura::getTotal)
                    .sum();

            double totalTransferencias = ventasHoy.stream()
                    .filter(f -> "Transferencia".equals(f.getMedioPago()))
                    .mapToDouble(Factura::getTotal)
                    .sum();

            double totalTarjetas = ventasHoy.stream()
                    .filter(f -> ("Tarjeta".equals(f.getMedioPago())))
                    .mapToDouble(Factura::getTotal)
                    .sum();

            double totalVentas = totalEfectivo + totalTransferencias + totalTarjetas;

            // Obtener pedidos cancelados/cortesías
            List<Pedido> cortesias = pedidoRepository.findByTipo("cortesia");
            double totalCortesias = cortesias.stream()
                    .filter(p -> p.getFecha().isAfter(inicioDia) && p.getFecha().isBefore(finDia))
                    .mapToDouble(p -> calcularTotalPedido(p)) // Método auxiliar
                    .sum();

            cuadre.put("fecha", inicioDia.format(DateTimeFormatter.ISO_LOCAL_DATE));
            cuadre.put("horaGeneracion", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
            cuadre.put("totales", Map.of(
                    "efectivo", totalEfectivo,
                    "transferencias", totalTransferencias,
                    "tarjetas", totalTarjetas,
                    "total", totalVentas
            ));
            cuadre.put("cortesias", Map.of(
                    "cantidad", cortesias.size(),
                    "total", totalCortesias
            ));
            cuadre.put("estadisticas", Map.of(
                    "numeroFacturas", ventasHoy.size(),
                    "promedioVenta", ventasHoy.isEmpty() ? 0 : totalVentas / ventasHoy.size(),
                    "facturasPendientes", facturaRepository.findFacturasPendientesPago().size()
            ));

            return responseService.success(cuadre, "Cuadre de caja obtenido exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener cuadre de caja: " + e.getMessage());
        }
    }

    @PostMapping("/cuadre-caja/cerrar")
    public ResponseEntity<ApiResponse<Map<String, Object>>> cerrarCaja(
            @RequestBody CuadreCajaRequest request) {
        try {
            Map<String, Object> resultado = new HashMap<>();
            LocalDateTime ahora = LocalDateTime.now();
            LocalDateTime inicioDia = ahora.withHour(0).withMinute(0).withSecond(0);
            LocalDateTime finDia = inicioDia.plusDays(1);

            // Calcular efectivo del día directamente desde facturas
            List<Factura> ventasHoy = facturaRepository.findVentasDelDia(inicioDia, finDia);
            double efectivoSistema = ventasHoy.stream()
                    .filter(f -> "Efectivo".equals(f.getMedioPago()))
                    .mapToDouble(Factura::getTotal)
                    .sum();

            double diferencia = Math.abs(efectivoSistema - request.getEfectivoDeclarado());
            boolean cuadreOk = diferencia <= request.getTolerancia();

            // Crear registro de cierre
            Map<String, Object> cierreCaja = new HashMap<>();
            cierreCaja.put("fecha", ahora.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            cierreCaja.put("responsable", request.getResponsable());
            cierreCaja.put("efectivoSistema", efectivoSistema);
            cierreCaja.put("efectivoDeclarado", request.getEfectivoDeclarado());
            cierreCaja.put("diferencia", diferencia);
            cierreCaja.put("cuadreOk", cuadreOk);
            cierreCaja.put("observaciones", request.getObservaciones());

            // TODO: Guardar en base de datos - crear modelo CierreCaja
            // cierreCajaRepository.save(nuevoCierre);
            resultado.put("cierre", cierreCaja);
            resultado.put("mensaje", cuadreOk ? "Cuadre de caja correcto" : "Hay diferencias en el cuadre");

            return responseService.success(resultado, "Caja cerrada exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al cerrar caja: " + e.getMessage());
        }
    }

    @GetMapping("/cuadre-caja/historial")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getHistorialCuadres(
            @RequestParam(defaultValue = "30") int dias) {
        try {
            List<Map<String, Object>> historial = new java.util.ArrayList<>();

            // TODO: Implementar cuando se cree el modelo CierreCaja
            // List<CierreCaja> cierres = cierreCajaRepository.findUltimosCierres(dias);
            // Por ahora devolvemos datos simulados
            LocalDateTime ahora = LocalDateTime.now();
            for (int i = 1; i <= Math.min(dias, 7); i++) {
                Map<String, Object> cierre = new HashMap<>();
                cierre.put("fecha", ahora.minusDays(i).format(DateTimeFormatter.ISO_LOCAL_DATE));
                cierre.put("responsable", "Sistema");
                cierre.put("totalVentas", 450000 + (i * 50000));
                cierre.put("efectivo", 200000 + (i * 25000));
                cierre.put("diferencia", i % 3 == 0 ? 5000 : 0);
                cierre.put("cuadreOk", i % 3 != 0);
                historial.add(cierre);
            }

            return responseService.success(historial, "Historial de cuadres obtenido");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener historial: " + e.getMessage());
        }
    }

    @GetMapping("/ventas-por-hora")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getVentasPorHora(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fecha) {
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
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fecha) {
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
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        try {
            Map<String, Object> resultado = new HashMap<>();

            // Eliminar facturas del período
            List<Factura> facturas = facturaRepository.findByFechaBetween(fechaInicio, fechaFin);
            for (Factura factura : facturas) {
                facturaRepository.deleteById(factura.get_id());
            }

            // Eliminar pedidos del período
            List<Pedido> pedidos = pedidoRepository.findByFechaBetween(fechaInicio, fechaFin);
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
    public ResponseEntity<ApiResponse<Map<String, Object>>> getIngresosVsEgresos(
            @RequestParam(defaultValue = "30") int dias) {
        try {
            LocalDateTime fechaInicio = LocalDateTime.now().minusDays(dias);

            // Calcular ingresos (facturas + pedidos pagados)
            List<Factura> facturas = facturaRepository.findByFechaBetween(fechaInicio, LocalDateTime.now());
            List<Pedido> pedidosPagados = pedidoRepository.findByFechaBetween(fechaInicio, LocalDateTime.now())
                    .stream()
                    .filter(p -> "pagado".equals(p.getEstado()))
                    .collect(Collectors.toList());

            double totalIngresos = facturas.stream().mapToDouble(Factura::getTotal).sum()
                    + pedidosPagados.stream().mapToDouble(Pedido::getTotalPagado).sum();

            // Calcular egresos (movimientos de salida de inventario)
            List<MovimientoInventario> movimientosSalida = movimientoInventarioRepository.findByFechaGreaterThanEqual(fechaInicio)
                    .stream()
                    .filter(m -> "salida".equals(m.getTipoMovimiento()))
                    .collect(Collectors.toList());

            double totalEgresos = movimientosSalida.stream()
                    .mapToDouble(MovimientoInventario::getCostoTotal)
                    .sum();

            // Calcular por día para gráfica
            Map<String, Double> ingresosPorDia = new HashMap<>();
            Map<String, Double> egresosPorDia = new HashMap<>();

            // Agrupar ingresos por día
            facturas.forEach(f -> {
                String dia = f.getFecha().toLocalDate().toString();
                ingresosPorDia.merge(dia, f.getTotal(), Double::sum);
            });

            pedidosPagados.forEach(p -> {
                String dia = p.getFecha().toLocalDate().toString();
                ingresosPorDia.merge(dia, p.getTotalPagado(), Double::sum);
            });

            // Agrupar egresos por día
            movimientosSalida.forEach(m -> {
                String dia = m.getFecha().toLocalDate().toString();
                egresosPorDia.merge(dia, m.getCostoTotal(), Double::sum);
            });

            // Crear lista ordenada para gráfica
            List<Map<String, Object>> grafica = new java.util.ArrayList<>();
            Set<String> todasLasFechas = new java.util.HashSet<>();
            todasLasFechas.addAll(ingresosPorDia.keySet());
            todasLasFechas.addAll(egresosPorDia.keySet());

            todasLasFechas.stream()
                    .sorted()
                    .forEach(fecha -> {
                        Map<String, Object> diaGrafica = new HashMap<>();
                        diaGrafica.put("fecha", fecha);
                        diaGrafica.put("ingresos", ingresosPorDia.getOrDefault(fecha, 0.0));
                        diaGrafica.put("egresos", egresosPorDia.getOrDefault(fecha, 0.0));
                        grafica.add(diaGrafica);
                    });

            Map<String, Object> resultado = new HashMap<>();
            resultado.put("periodo", dias + " días");
            resultado.put("totalIngresos", totalIngresos);
            resultado.put("totalEgresos", totalEgresos);
            resultado.put("utilidad", totalIngresos - totalEgresos);
            resultado.put("margenUtilidad", totalIngresos > 0 ? ((totalIngresos - totalEgresos) / totalIngresos) * 100 : 0);
            resultado.put("grafica", grafica);

            return responseService.success(resultado, "Ingresos vs Egresos obtenidos exitosamente");
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
}
