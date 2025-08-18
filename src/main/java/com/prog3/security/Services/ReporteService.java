package com.prog3.security.Services;

import com.prog3.security.Entities.ObjetivoVenta;
import com.prog3.security.Models.Pedido;
import com.prog3.security.Models.Factura;
import com.prog3.security.Models.Inventario;
import com.prog3.security.Models.Gasto;
import com.prog3.security.Repositories.PedidoRepository;
import com.prog3.security.Repositories.FacturaRepository;
import com.prog3.security.Repositories.InventarioRepository;
import com.prog3.security.Repositories.ObjetivoVentaRepository;
import com.prog3.security.Repositories.ProductoRepository;
import com.prog3.security.Repositories.GastoRepository;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class ReporteService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ObjetivoVentaRepository objetivoVentaRepository;

    @Autowired
    private GastoRepository gastoRepository;

    // Valores por defecto para los objetivos
    private static final Map<String, Double> OBJETIVOS_DEFAULT = Map.of(
            "hoy", 100000.0, // $100,000 por día
            "semana", 700000.0, // $700,000 por semana
            "mes", 3000000.0, // $3,000,000 por mes
            "año", 36000000.0 // $36,000,000 por año
    );

    @PostConstruct
    public void inicializarObjetivos() {
        // Verificar si ya existen objetivos en la base de datos
        // Si no existen, crear con valores por defecto
        for (Map.Entry<String, Double> entry : OBJETIVOS_DEFAULT.entrySet()) {
            String periodo = entry.getKey();
            Double valorDefault = entry.getValue();

            ObjetivoVenta objetivo = objetivoVentaRepository.findByPeriodo(periodo);
            if (objetivo == null) {
                System.out.println("🎯 Inicializando objetivo para " + periodo + " con valor $" + valorDefault);
                objetivoVentaRepository.save(new ObjetivoVenta(periodo, valorDefault));
            }
        }
    }

    public Map<String, Object> getDashboard() {
        System.out.println("=== DEBUG DASHBOARD ===");
        try {
            // Calcular diferentes períodos de tiempo
            LocalDateTime ahora = LocalDateTime.now();

            // HOY: Últimas 24 horas (incluye ayer si es necesario)
            LocalDateTime inicio24h = ahora.minusHours(24);

            // SEMANA: Últimos 7 días
            LocalDateTime inicioSemana = ahora.minusDays(7);

            // MES: Últimos 30 días
            LocalDateTime inicioMes = ahora.minusDays(30);

            // AÑO: Desde inicio del año calendario
            LocalDateTime inicioAño = ahora.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0);

            System.out.println("Período HOY (24h): " + inicio24h + " hasta " + ahora);
            System.out.println("Período SEMANA (7d): " + inicioSemana + " hasta " + ahora);
            System.out.println("Período MES (30d): " + inicioMes + " hasta " + ahora);
            System.out.println("Período AÑO: " + inicioAño + " hasta " + ahora);

            // === VENTAS HOY (24 horas) ===
            Map<String, Object> ventasHoy = calcularVentasPeriodo(inicio24h, ahora, "HOY");
            double objetivoHoy = obtenerObjetivo("hoy");
            double totalVentasHoy = (Double) ventasHoy.get("total");
            double porcentajeHoy = (totalVentasHoy / objetivoHoy) * 100.0;

            ventasHoy.put("objetivo", objetivoHoy);
            ventasHoy.put("porcentaje", porcentajeHoy);

            System.out.println("=== VENTAS HOY ===");
            System.out.println("Total: " + totalVentasHoy + " | Objetivo: " + objetivoHoy + " | Porcentaje: " + porcentajeHoy);

            // === VENTAS SEMANA (7 días) ===
            Map<String, Object> ventasSemana = calcularVentasPeriodo(inicioSemana, ahora, "SEMANA");
            double objetivoSemana = obtenerObjetivo("semana");
            double totalVentasSemana = (Double) ventasSemana.get("total");
            double porcentajeSemana = (totalVentasSemana / objetivoSemana) * 100.0;

            ventasSemana.put("objetivo", objetivoSemana);
            ventasSemana.put("porcentaje", porcentajeSemana);

            System.out.println("=== VENTAS SEMANA ===");
            System.out.println("Total: " + totalVentasSemana + " | Objetivo: " + objetivoSemana + " | Porcentaje: " + porcentajeSemana);

            // === VENTAS MES (30 días) ===
            Map<String, Object> ventasMes = calcularVentasPeriodo(inicioMes, ahora, "MES");
            double objetivoMes = obtenerObjetivo("mes");
            double totalVentasMes = (Double) ventasMes.get("total");
            double porcentajeMes = (totalVentasMes / objetivoMes) * 100.0;

            ventasMes.put("objetivo", objetivoMes);
            ventasMes.put("porcentaje", porcentajeMes);

            System.out.println("=== VENTAS MES ===");
            System.out.println("Total: " + totalVentasMes + " | Objetivo: " + objetivoMes + " | Porcentaje: " + porcentajeMes);

            // === VENTAS AÑO ===
            Map<String, Object> ventasAño = calcularVentasPeriodo(inicioAño, ahora, "AÑO");
            double objetivoAño = obtenerObjetivo("año");
            double totalVentasAño = (Double) ventasAño.get("total");
            double porcentajeAño = (totalVentasAño / objetivoAño) * 100.0;

            ventasAño.put("objetivo", objetivoAño);
            ventasAño.put("porcentaje", porcentajeAño);

            System.out.println("=== VENTAS AÑO ===");
            System.out.println("Total: " + totalVentasAño + " | Objetivo: " + objetivoAño + " | Porcentaje: " + porcentajeAño);

            // === DATOS ADICIONALES DEL DASHBOARD ===
            // Para pedidos del día usar día calendario actual
            LocalDateTime inicioDiaCalendario = ahora.withHour(0).withMinute(0).withSecond(0);
            LocalDateTime finDiaCalendario = inicioDiaCalendario.plusDays(1);

            List<Pedido> pedidosHoy = pedidoRepository.findByFechaBetween(inicioDiaCalendario, finDiaCalendario);
            long pedidosPendientes = pedidosHoy.stream().filter(p -> "pendiente".equals(p.getEstado())).count();
            long pedidosCompletados = pedidosHoy.stream().filter(p -> "completado".equals(p.getEstado())).count();

            // Estado del inventario
            List<Inventario> stockBajo = inventarioRepository.findProductosConStockBajo();
            List<Inventario> agotados = inventarioRepository.findProductosAgotados();

            // Facturas pendientes
            List<Factura> facturasPendientes = facturaRepository.findFacturasPendientesPago();
            double montoPendiente = facturasPendientes.stream()
                    .mapToDouble(Factura::getTotal)
                    .sum();

            // === CONSTRUIR RESPUESTA FINAL ===
            Map<String, Object> dashboard = new HashMap<>();

            dashboard.put("ventasHoy", ventasHoy);
            dashboard.put("ventasSemana", ventasSemana);
            dashboard.put("ventasMes", ventasMes);
            dashboard.put("ventasAño", ventasAño);

            // Resumen de pedidos del día
            Map<String, Object> pedidosResumen = new HashMap<>();
            pedidosResumen.put("total", pedidosHoy.size());
            pedidosResumen.put("pendientes", pedidosPendientes);
            pedidosResumen.put("completados", pedidosCompletados);
            dashboard.put("pedidosHoy", pedidosResumen);

            // Inventario
            Map<String, Object> inventario = new HashMap<>();
            inventario.put("stockBajo", stockBajo.size());
            inventario.put("agotados", agotados.size());
            inventario.put("alertas", stockBajo.size() + agotados.size());
            dashboard.put("inventario", inventario);

            // Facturación
            Map<String, Object> facturacion = new HashMap<>();
            facturacion.put("pendientesPago", facturasPendientes.size());
            facturacion.put("montoPendiente", montoPendiente);
            dashboard.put("facturacion", facturacion);

            dashboard.put("fecha", ahora);

            System.out.println("Dashboard response: " + dashboard);
            return dashboard;

        } catch (Exception e) {
            System.out.println("Error en getDashboard: " + e.getMessage());
            e.printStackTrace();

            // Crear un dashboard mínimo con error
            Map<String, Object> dashboardError = new HashMap<>();
            dashboardError.put("error", "Error generando el dashboard: " + e.getMessage());
            dashboardError.put("fecha", LocalDateTime.now());
            return dashboardError;
        }
    }

    private Map<String, Object> calcularVentasPeriodo(LocalDateTime inicio, LocalDateTime fin, String periodo) {
        System.out.println("=== Calculando ventas para " + periodo + " ===");
        System.out.println("Desde: " + inicio + " Hasta: " + fin);

        try {
            // CORREGIDO: Obtener solo facturas de VENTA del período (no de compra)
            List<Factura> todasFacturas = facturaRepository.findByFechaBetween(inicio, fin);

            // Filtrar solo facturas de venta (excluir facturas de compra que son gastos)
            List<Factura> facturasVenta = todasFacturas.stream()
                    .filter(f -> f.getTipoFactura() == null || !"compra".equals(f.getTipoFactura()))
                    .collect(Collectors.toList());

            double totalFacturas = facturasVenta.stream().mapToDouble(Factura::getTotal).sum();

            // Debug facturas
            System.out.println("Total facturas encontradas: " + todasFacturas.size());
            System.out.println("Facturas de VENTA (excluye compras): " + facturasVenta.size());
            System.out.println("Facturas de COMPRA (excluidas del cálculo): " + (todasFacturas.size() - facturasVenta.size()));

            // Log de facturas de compra excluidas para debug
            long facturasCompra = todasFacturas.stream()
                    .filter(f -> "compra".equals(f.getTipoFactura()))
                    .count();
            if (facturasCompra > 0) {
                System.out.println("⚠️ Se excluyeron " + facturasCompra + " facturas de compra del cálculo de ventas");
            }

            // Obtener TODOS los pedidos del período primero
            List<Pedido> todosPedidos = pedidoRepository.findByFechaBetween(inicio, fin);
            System.out.println("Todos los pedidos en el período: " + todosPedidos.size());

            // Comentado para reducir logs excesivos
            /*for (Pedido p : todosPedidos) {
                double totalReal = p.calcularTotalReal();
                System.out.println("  - Pedido ID: " + p.get_id() + " - Estado: " + p.getEstado() + " - Fecha: " + p.getFecha() + " - Total: " + p.getTotal() + " - TotalPagado: " + p.getTotalPagado() + " - TotalReal: " + totalReal + " - FormaPago: " + p.getFormaPago());
            }*/
            // Filtrar pedidos pagados del período (incluir tanto "pagado" como "completado")
            List<Pedido> pedidosPagados = todosPedidos.stream()
                    .filter(p -> "pagado".equals(p.getEstado()) || "completado".equals(p.getEstado()))
                    .collect(Collectors.toList());

            // Calcular total de pedidos - usar el total real calculado desde los items
            double totalPedidos = pedidosPagados.stream()
                    .mapToDouble(p -> {
                        double totalReal = p.calcularTotalReal();
                        double totalPagado = p.getTotalPagado();
                        double total = p.getTotal();

                        // Comentado para reducir logs excesivos
                        // System.out.println("    -> Pedido " + p.get_id() + " - TotalReal: " + totalReal + " - TotalPagado: " + totalPagado + " - Total: " + total);
                        // Usar el total real calculado desde items como primera opción
                        if (totalReal > 0.0) {
                            // System.out.println("    -> Usando 'totalReal' para pedido " + p.get_id() + ": " + totalReal);
                            return totalReal;
                        }

                        // Si totalPagado es 0 pero total tiene valor, usar total
                        if (totalPagado == 0.0 && total > 0.0) {
                            // System.out.println("    -> Usando 'total' como fallback para pedido " + p.get_id() + ": " + total);
                            return total;
                        }

                        // System.out.println("    -> Usando 'totalPagado' para pedido " + p.get_id() + ": " + totalPagado);
                        return totalPagado;
                    })
                    .sum();
            double totalVentas = totalFacturas + totalPedidos;

            System.out.println(periodo + " - Facturas de VENTA: " + facturasVenta.size() + " (Total: " + totalFacturas + ")");
            System.out.println(periodo + " - Pedidos pagados/completados: " + pedidosPagados.size() + " (Total: " + totalPedidos + ")");
            System.out.println(periodo + " - Total ventas: " + totalVentas);

            Map<String, Object> resultado = new HashMap<>();
            resultado.put("total", totalVentas);
            resultado.put("totalFacturas", totalFacturas);
            resultado.put("totalPedidos", totalPedidos);
            resultado.put("cantidadFacturas", facturasVenta.size());
            resultado.put("cantidadPedidos", pedidosPagados.size());
            resultado.put("cantidadTotal", facturasVenta.size() + pedidosPagados.size());

            return resultado;
        } catch (Exception e) {
            System.out.println("Error calculando ventas para periodo " + periodo + ": " + e.getMessage());
            e.printStackTrace();

            // Devolver un resultado vacío para no romper el flujo
            Map<String, Object> resultadoError = new HashMap<>();
            resultadoError.put("total", 0.0);
            resultadoError.put("totalFacturas", 0.0);
            resultadoError.put("totalPedidos", 0.0);
            resultadoError.put("cantidadFacturas", 0);
            resultadoError.put("cantidadPedidos", 0);
            resultadoError.put("cantidadTotal", 0);
            resultadoError.put("error", "Error en el cálculo: " + e.getMessage());
            return resultadoError;
        }
    }

    public Map<String, Object> getVentasPorPeriodo(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        // Obtener facturas del período
        List<Factura> facturas = facturaRepository.findByFechaBetween(fechaInicio, fechaFin);

        // Obtener pedidos pagados del período
        List<Pedido> pedidosPagados = pedidoRepository.findByFechaBetween(fechaInicio, fechaFin)
                .stream()
                .filter(p -> "pagado".equals(p.getEstado()))
                .collect(Collectors.toList());

        // Calcular totales
        double totalVentasFacturas = facturas.stream().mapToDouble(Factura::getTotal).sum();
        double totalVentasPedidos = pedidosPagados.stream().mapToDouble(Pedido::getTotalPagado).sum();
        double totalVentas = totalVentasFacturas + totalVentasPedidos;

        // Agrupar por método de pago - facturas
        Map<String, Double> ventasPorMetodoFacturas = facturas.stream()
                .collect(Collectors.groupingBy(
                        Factura::getMedioPago,
                        Collectors.summingDouble(Factura::getTotal)
                ));

        // Agrupar por método de pago - pedidos pagados
        Map<String, Double> ventasPorMetodoPedidos = pedidosPagados.stream()
                .collect(Collectors.groupingBy(
                        Pedido::getFormaPago,
                        Collectors.summingDouble(Pedido::getTotalPagado)
                ));

        // Combinar ambos mapas
        Map<String, Double> ventasPorMetodo = new HashMap<>();
        ventasPorMetodoFacturas.forEach((metodo, total)
                -> ventasPorMetodo.merge(metodo, total, Double::sum));
        ventasPorMetodoPedidos.forEach((metodo, total)
                -> ventasPorMetodo.merge(metodo, total, Double::sum));

        // Construir respuesta
        Map<String, Object> reporte = new HashMap<>();
        reporte.put("totalVentas", totalVentas);
        reporte.put("totalSubtotal", totalVentas); // En modelo simplificado es lo mismo
        reporte.put("totalImpuestos", 0.0); // No hay impuestos separados
        reporte.put("totalDescuentos", 0.0); // No hay descuentos
        reporte.put("ventasPorMetodo", ventasPorMetodo);
        reporte.put("cantidadFacturas", facturas.size());
        reporte.put("cantidadPedidosPagados", pedidosPagados.size());
        reporte.put("cantidadTotal", facturas.size() + pedidosPagados.size());
        reporte.put("promedioVenta", (facturas.size() + pedidosPagados.size()) == 0 ? 0 : totalVentas / (facturas.size() + pedidosPagados.size()));

        return reporte;
    }

    public List<Map<String, Object>> getVentasPorHora(LocalDateTime fecha) {
        LocalDateTime inicioDia = fecha.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime finDia = inicioDia.plusDays(1);

        List<Factura> facturas = facturaRepository.findByFechaBetween(inicioDia, finDia);

        // Inicializar mapa de ventas por hora
        Map<Integer, Double> ventasPorHora = new HashMap<>();
        for (int i = 0; i < 24; i++) {
            ventasPorHora.put(i, 0.0);
        }

        // Agrupar ventas por hora
        facturas.forEach(f -> {
            int hora = f.getFecha().getHour();
            ventasPorHora.merge(hora, f.getTotal(), Double::sum);
        });

        // Convertir a lista de mapas para la respuesta
        return ventasPorHora.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> horaVenta = new HashMap<>();
                    horaVenta.put("hora", String.format("%02d:00", entry.getKey()));
                    horaVenta.put("ventas", entry.getValue());
                    return horaVenta;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getPedidosPorHora(LocalDateTime fechaDesde) {
        // Si no se proporciona fecha, usar hoy
        LocalDateTime fechaConsulta = fechaDesde != null ? fechaDesde : LocalDateTime.now();

        // Establecer el inicio y fin del día para la fecha especificada
        LocalDateTime inicioDelDia = fechaConsulta.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime finDelDia = fechaConsulta.withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        System.out.println("=== DEBUG getPedidosPorHora ===");
        System.out.println("Fecha consulta: " + fechaConsulta);
        System.out.println("Inicio del día: " + inicioDelDia);
        System.out.println("Fin del día: " + finDelDia);

        List<Pedido> pedidos = pedidoRepository.findByFechaBetween(inicioDelDia, finDelDia);
        System.out.println("Pedidos encontrados: " + pedidos.size());

        // Debug: mostrar algunos pedidos
        if (pedidos.size() > 0) {
            System.out.println("Primer pedido: " + pedidos.get(0).getFecha());
            if (pedidos.size() > 1) {
                System.out.println("Último pedido: " + pedidos.get(pedidos.size() - 1).getFecha());
            }
        }

        // Crear mapa de horas para las 24 horas del día
        Map<Integer, Long> pedidosPorHora = new LinkedHashMap<>();

        // Inicializar todas las horas (0-23)
        for (int hora = 0; hora < 24; hora++) {
            pedidosPorHora.put(hora, 0L);
        }

        // Contar pedidos por hora
        pedidos.forEach(p -> {
            int hora = p.getFecha().getHour();
            pedidosPorHora.merge(hora, 1L, Long::sum);
            System.out.println("Pedido en hora " + hora + ": " + p.getFecha() + " - Mesa: " + p.getMesa());
        });

        // Convertir a lista
        List<Map<String, Object>> resultado = pedidosPorHora.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> horaPedido = new HashMap<>();
                    horaPedido.put("hora", String.format("%02d:00", entry.getKey()));
                    horaPedido.put("cantidad", entry.getValue());
                    return horaPedido;
                })
                .collect(Collectors.toList());

        System.out.println("Resultado: " + resultado.size() + " registros");
        return resultado;
    }

    public List<Map<String, Object>> getVentasPorDia(int ultimosDias) {
        LocalDateTime fechaInicio = LocalDateTime.now().minusDays(ultimosDias);

        // Obtener facturas y pedidos pagados
        List<Factura> facturas = facturaRepository.findByFechaBetween(fechaInicio, LocalDateTime.now());
        List<Pedido> pedidosPagados = pedidoRepository.findByFechaBetween(fechaInicio, LocalDateTime.now())
                .stream()
                .filter(p -> "pagado".equals(p.getEstado()))
                .collect(Collectors.toList());

        // Agrupar facturas por día
        Map<String, Double> ventasPorDiaFacturas = facturas.stream()
                .collect(Collectors.groupingBy(
                        f -> f.getFecha().toLocalDate().toString(),
                        Collectors.summingDouble(Factura::getTotal)
                ));

        // Agrupar pedidos pagados por día
        Map<String, Double> ventasPorDiaPedidos = pedidosPagados.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getFecha().toLocalDate().toString(),
                        Collectors.summingDouble(Pedido::getTotalPagado)
                ));

        // Combinar ambos mapas
        Map<String, Double> ventasPorDia = new HashMap<>();
        ventasPorDiaFacturas.forEach((dia, total)
                -> ventasPorDia.merge(dia, total, Double::sum));
        ventasPorDiaPedidos.forEach((dia, total)
                -> ventasPorDia.merge(dia, total, Double::sum));

        // Convertir a lista ordenada
        return ventasPorDia.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    Map<String, Object> diaVenta = new HashMap<>();
                    diaVenta.put("fecha", entry.getKey());
                    diaVenta.put("total", entry.getValue());
                    return diaVenta;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getIngresosVsEgresos(int ultimosMeses) {
        LocalDateTime fechaInicio = LocalDateTime.now().minusMonths(ultimosMeses);
        LocalDateTime fechaFin = LocalDateTime.now();

        System.out.println("=== DEBUG getIngresosVsEgresos ===");
        System.out.println("Consultando desde: " + fechaInicio + " hasta: " + fechaFin);

        // Obtener facturas y pedidos pagados (ingresos)
        List<Factura> facturas = facturaRepository.findByFechaBetween(fechaInicio, fechaFin);
        List<Pedido> pedidosPagados = pedidoRepository.findByFechaBetween(fechaInicio, fechaFin)
                .stream()
                .filter(p -> "pagado".equals(p.getEstado()))
                .collect(Collectors.toList());

        // Obtener gastos reales (egresos)
        List<Gasto> gastos = gastoRepository.findByFechaGastoBetween(fechaInicio, fechaFin)
                .stream()
                .filter(g -> "aprobado".equals(g.getEstado()) || "pendiente".equals(g.getEstado())) // Solo gastos válidos
                .collect(Collectors.toList());

        System.out.println("Facturas encontradas: " + facturas.size());
        System.out.println("Pedidos pagados encontrados: " + pedidosPagados.size());
        System.out.println("Gastos encontrados: " + gastos.size());

        // Agrupar ingresos por mes
        Map<String, Double> ingresosPorMes = new HashMap<>();

        // Procesar facturas
        facturas.forEach(f -> {
            String mesKey = f.getFecha().getYear() + "-" + String.format("%02d", f.getFecha().getMonthValue());
            ingresosPorMes.merge(mesKey, f.getTotal(), Double::sum);
        });

        // Procesar pedidos pagados
        pedidosPagados.forEach(p -> {
            String mesKey = p.getFecha().getYear() + "-" + String.format("%02d", p.getFecha().getMonthValue());
            ingresosPorMes.merge(mesKey, p.getTotalPagado(), Double::sum);
        });

        // Agrupar egresos (gastos) por mes
        Map<String, Double> egresosPorMes = new HashMap<>();

        // Procesar gastos reales
        gastos.forEach(g -> {
            String mesKey = g.getFechaGasto().getYear() + "-" + String.format("%02d", g.getFechaGasto().getMonthValue());
            egresosPorMes.merge(mesKey, g.getMonto(), Double::sum);
        });

        System.out.println("Ingresos por mes: " + ingresosPorMes);
        System.out.println("Egresos por mes: " + egresosPorMes);

        // Combinar datos de ingresos y egresos
        Map<String, Map<String, Object>> datosCombinados = new LinkedHashMap<>();

        // Agregar todos los meses que tienen ingresos
        ingresosPorMes.forEach((mes, ingresos) -> {
            Map<String, Object> mesData = datosCombinados.getOrDefault(mes, new HashMap<>());
            mesData.put("ingresos", ingresos);
            datosCombinados.put(mes, mesData);
        });

        // Agregar todos los meses que tienen egresos
        egresosPorMes.forEach((mes, egresos) -> {
            Map<String, Object> mesData = datosCombinados.getOrDefault(mes, new HashMap<>());
            mesData.put("egresos", egresos);
            datosCombinados.put(mes, mesData);
        });

        // Convertir a lista y completar datos faltantes
        return datosCombinados.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    String[] yearMonth = entry.getKey().split("-");
                    String nombreMes = obtenerNombreMes(Integer.parseInt(yearMonth[1]));

                    Map<String, Object> mesData = new HashMap<>();
                    mesData.put("mes", nombreMes);
                    mesData.put("año", Integer.parseInt(yearMonth[0]));
                    mesData.put("ingresos", entry.getValue().getOrDefault("ingresos", 0.0));
                    mesData.put("egresos", entry.getValue().getOrDefault("egresos", 0.0));

                    // Calcular utilidad
                    double ingresos = (Double) entry.getValue().getOrDefault("ingresos", 0.0);
                    double egresos = (Double) entry.getValue().getOrDefault("egresos", 0.0);
                    mesData.put("utilidad", ingresos - egresos);

                    return mesData;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getTopProductos(int limite) {
        LocalDateTime fechaInicio = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime fechaFin = LocalDateTime.now();

        System.out.println("=== DEBUG getTopProductos ===");
        System.out.println("Fecha inicio (mes actual): " + fechaInicio);
        System.out.println("Fecha fin: " + fechaFin);

        // Obtener facturas del mes actual
        List<Factura> facturas = facturaRepository.findByFechaBetween(fechaInicio, fechaFin);
        System.out.println("Facturas encontradas: " + facturas.size());

        // Obtener pedidos pagados del mes actual
        List<Pedido> pedidosPagados = pedidoRepository.findByFechaBetween(fechaInicio, fechaFin)
                .stream()
                .filter(p -> "pagado".equals(p.getEstado()) || "completado".equals(p.getEstado()))
                .collect(Collectors.toList());
        System.out.println("Pedidos pagados encontrados: " + pedidosPagados.size());

        // Contar productos vendidos
        Map<String, Integer> conteoProductos = new HashMap<>();
        Map<String, Double> ventasProductos = new HashMap<>();

        // Procesar facturas
        for (Factura factura : facturas) {
            if (factura.getItems() != null) {
                for (var item : factura.getItems()) {
                    String nombreProducto = item.getProductoNombre() != null
                            ? item.getProductoNombre()
                            : obtenerNombreProducto(item.getProductoId());

                    if (nombreProducto != null) {
                        conteoProductos.merge(nombreProducto, item.getCantidad(), Integer::sum);
                        ventasProductos.merge(nombreProducto, item.getSubtotalItem(), Double::sum);
                    }
                }
            }
        }

        // Procesar pedidos pagados
        for (Pedido pedido : pedidosPagados) {
            if (pedido.getItems() != null) {
                for (var item : pedido.getItems()) {
                    String nombreProducto = item.getProductoNombre() != null
                            ? item.getProductoNombre()
                            : obtenerNombreProducto(item.getProductoId());

                    if (nombreProducto != null) {
                        conteoProductos.merge(nombreProducto, item.getCantidad(), Integer::sum);
                        // Usar subtotal
                        ventasProductos.merge(nombreProducto, item.getSubtotal(), Double::sum);
                    }
                }
            }
        }

        // Calcular total de CANTIDAD para porcentajes (no de ventas)
        int totalCantidad = conteoProductos.values().stream().mapToInt(Integer::intValue).sum();
        System.out.println("Total cantidad productos vendidos: " + totalCantidad);
        System.out.println("Productos únicos: " + conteoProductos.size());

        // Convertir a lista ordenada por cantidad vendida
        List<Map<String, Object>> resultado = conteoProductos.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limite)
                .map(entry -> {
                    String producto = entry.getKey();
                    Integer cantidad = entry.getValue();
                    Double ventas = ventasProductos.getOrDefault(producto, 0.0);
                    // Calcular porcentaje basado en cantidad, no en ventas
                    double porcentaje = totalCantidad > 0 ? ((double) cantidad / totalCantidad) * 100 : 0;

                    Map<String, Object> productoData = new HashMap<>();
                    productoData.put("nombre", producto);
                    productoData.put("cantidad", cantidad);
                    productoData.put("ventas", ventas);
                    productoData.put("porcentaje", Math.round(porcentaje * 100.0) / 100.0);

                    System.out.println("Producto: " + producto + " - Cantidad: " + cantidad + " - Porcentaje: " + porcentaje);
                    return productoData;
                })
                .collect(Collectors.toList());

        System.out.println("Resultado final: " + resultado.size() + " productos");
        return resultado;
    }

    private String obtenerNombreMes(int mes) {
        String[] meses = {"", "Ene", "Feb", "Mar", "Abr", "May", "Jun",
            "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
        return meses[mes];
    }

    private String obtenerNombreProducto(String productoId) {
        try {
            var producto = productoRepository.findById(productoId);
            return producto.isPresent() ? producto.get().getNombre() : "Producto Desconocido";
        } catch (Exception e) {
            return "Producto Desconocido";
        }
    }

    public boolean actualizarObjetivo(String periodo, Double nuevoObjetivo) {
        try {
            System.out.println("🎯 Actualizando objetivo para período: " + periodo + " a $" + nuevoObjetivo);

            // Validar período
            if (!OBJETIVOS_DEFAULT.containsKey(periodo)) {
                System.out.println("❌ Período no válido: " + periodo);
                return false;
            }

            // Buscar objetivo existente o crear uno nuevo
            ObjetivoVenta objetivo = objetivoVentaRepository.findByPeriodo(periodo);
            if (objetivo == null) {
                objetivo = new ObjetivoVenta(periodo, nuevoObjetivo);
            } else {
                objetivo.setValor(nuevoObjetivo);
            }

            // Guardar en la base de datos
            objetivoVentaRepository.save(objetivo);

            System.out.println("✅ Objetivo actualizado exitosamente:");
            System.out.println("   Período: " + periodo);
            System.out.println("   Nuevo objetivo: $" + nuevoObjetivo);

            return true;

        } catch (Exception e) {
            System.out.println("❌ Error al actualizar objetivo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Double obtenerObjetivo(String periodo) {
        ObjetivoVenta objetivo = objetivoVentaRepository.findByPeriodo(periodo);
        if (objetivo != null) {
            return objetivo.getValor();
        }
        // Si no existe, retornar el valor por defecto
        return OBJETIVOS_DEFAULT.getOrDefault(periodo, 0.0);
    }

    public List<Map<String, Object>> getUltimosPedidosConDetalles(int limite) {
        System.out.println("=== DEBUG getUltimosPedidosConDetalles ===");
        System.out.println("Límite solicitado: " + limite);

        try {
            // Obtener los últimos pedidos ordenados por fecha
            List<Pedido> pedidos = pedidoRepository.findAll()
                    .stream()
                    .sorted((p1, p2) -> p2.getFecha().compareTo(p1.getFecha()))
                    .limit(limite)
                    .collect(Collectors.toList());

            System.out.println("Pedidos encontrados: " + pedidos.size());

            // Convertir a formato detallado para el frontend (UN PEDIDO POR FILA)
            List<Map<String, Object>> pedidosDetallados = new ArrayList<>();

            for (Pedido pedido : pedidos) {
                Map<String, Object> pedidoDetalle = new HashMap<>();

                // Información básica del pedido
                pedidoDetalle.put("pedidoId", pedido.get_id());
                pedidoDetalle.put("mesa", pedido.getMesa() != null ? pedido.getMesa() : "N/A");
                pedidoDetalle.put("fecha", pedido.getFecha().format(DateTimeFormatter.ofPattern("HH:mm")));
                pedidoDetalle.put("fechaCompleta", pedido.getFecha().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                pedidoDetalle.put("estado", traducirEstado(pedido.getEstado()));
                pedidoDetalle.put("vendedor", pedido.getMesero() != null ? pedido.getMesero() : "N/A");
                pedidoDetalle.put("tipo", pedido.getTipo() != null ? pedido.getTipo() : "Normal");

                // Agrupar productos del pedido
                if (pedido.getItems() != null && !pedido.getItems().isEmpty()) {
                    List<String> productos = new ArrayList<>();
                    double totalPedido = 0.0;
                    int cantidadTotal = 0;

                    for (var item : pedido.getItems()) {
                        String nombreProducto = item.getProductoNombre() != null
                                ? item.getProductoNombre()
                                : obtenerNombreProducto(item.getProductoId());

                        // Agregar cantidad si es mayor a 1
                        if (item.getCantidad() > 1) {
                            productos.add(item.getCantidad() + "x " + nombreProducto);
                        } else {
                            productos.add(nombreProducto);
                        }

                        totalPedido += item.getPrecio() * item.getCantidad();
                        cantidadTotal += item.getCantidad();
                    }

                    // Unir productos con comas
                    pedidoDetalle.put("producto", String.join(", ", productos));
                    pedidoDetalle.put("productos", productos); // Lista separada por si la necesita el frontend
                    pedidoDetalle.put("cantidad", cantidadTotal);
                    pedidoDetalle.put("total", totalPedido);
                } else {
                    pedidoDetalle.put("producto", "Sin productos");
                    pedidoDetalle.put("productos", new ArrayList<>());
                    pedidoDetalle.put("cantidad", 0);
                    pedidoDetalle.put("total", 0.0);
                }

                // Agregar notas generales del pedido si existen
                if (pedido.getNotas() != null && !pedido.getNotas().trim().isEmpty()) {
                    pedidoDetalle.put("notas", pedido.getNotas());
                }

                pedidosDetallados.add(pedidoDetalle);
            }

            System.out.println("Pedidos procesados: " + pedidosDetallados.size());
            return pedidosDetallados;

        } catch (Exception e) {
            System.err.println("Error en getUltimosPedidosConDetalles: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> getVendedoresDelMes(int dias) {
        System.out.println("=== DEBUG getVendedoresDelMes ===");
        System.out.println("Días a consultar: " + dias);

        try {
            LocalDateTime fechaInicio = LocalDateTime.now().minusDays(dias);
            LocalDateTime fechaFin = LocalDateTime.now();

            System.out.println("Rango de fechas: " + fechaInicio + " a " + fechaFin);

            // Obtener pedidos pagados del período
            List<Pedido> pedidosPagados = pedidoRepository.findByFechaBetween(fechaInicio, fechaFin)
                    .stream()
                    .filter(p -> "pagado".equals(p.getEstado()) && p.getMesero() != null)
                    .collect(Collectors.toList());

            // Obtener facturas del período
            List<Factura> facturas = facturaRepository.findByFechaBetween(fechaInicio, fechaFin)
                    .stream()
                    .filter(f -> f.getAtendidoPor() != null)
                    .collect(Collectors.toList());

            System.out.println("Pedidos pagados encontrados: " + pedidosPagados.size());
            System.out.println("Facturas encontradas: " + facturas.size());

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

            System.out.println("Vendedores encontrados: " + ventasPorVendedor.size());

            // Convertir a lista ordenada con ranking
            List<Map<String, Object>> vendedores = new ArrayList<>();

            ventasPorVendedor.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .forEach((entry) -> {
                        Map<String, Object> vendedor = new HashMap<>();
                        vendedor.put("nombre", entry.getKey());
                        vendedor.put("totalVentas", entry.getValue());
                        vendedor.put("cantidadPedidos", pedidosPorVendedor.getOrDefault(entry.getKey(), 0));
                        vendedor.put("promedioVenta", pedidosPorVendedor.getOrDefault(entry.getKey(), 0) > 0
                                ? entry.getValue() / pedidosPorVendedor.get(entry.getKey()) : 0);
                        vendedor.put("puesto", vendedores.size() + 1); // Posición en el ranking
                        vendedores.add(vendedor);
                    });

            System.out.println("Vendedores procesados: " + vendedores.size());
            return vendedores;

        } catch (Exception e) {
            System.err.println("Error en getVendedoresDelMes: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private String traducirEstado(String estado) {
        if (estado == null) {
            return "Desconocido";
        }

        switch (estado.toLowerCase()) {
            case "pendiente":
                return "Pendiente";
            case "pagado":
                return "Pagada";
            case "interno":
                return "Interno";
            case "cancelado":
                return "Cancelado";
            default:
                return estado;
        }
    }
}
