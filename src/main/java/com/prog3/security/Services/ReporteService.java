package com.prog3.security.Services;

import com.prog3.security.Models.Reporte;
import com.prog3.security.Models.Pedido;
import com.prog3.security.Models.Factura;
import com.prog3.security.Models.Inventario;
import com.prog3.security.Repositories.ReporteRepository;
import com.prog3.security.Repositories.PedidoRepository;
import com.prog3.security.Repositories.FacturaRepository;
import com.prog3.security.Repositories.InventarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReporteService {

    @Autowired
    private ReporteRepository reporteRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    public Map<String, Object> getDashboard() {
        LocalDateTime inicioDia = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime finDia = inicioDia.plusDays(1);

        // Calcular ventas del día (facturas + pedidos pagados)
        List<Factura> facturas = facturaRepository.findByFechaBetween(inicioDia, finDia);
        double totalVentasFacturas = facturas.stream()
                .mapToDouble(Factura::getTotal)
                .sum();

        // Incluir pedidos pagados en las ventas del día
        List<Pedido> pedidosPagados = pedidoRepository.findByFechaBetween(inicioDia, finDia)
                .stream()
                .filter(p -> "pagado".equals(p.getEstado()))
                .collect(Collectors.toList());

        double totalVentasPedidos = pedidosPagados.stream()
                .mapToDouble(Pedido::getTotalPagado)
                .sum();

        double totalVentas = totalVentasFacturas + totalVentasPedidos;

        // Calcular ventas últimos 7 días (facturas + pedidos pagados)
        LocalDateTime inicio7Dias = LocalDateTime.now().minusDays(7);
        List<Factura> facturas7Dias = facturaRepository.findByFechaBetween(inicio7Dias, LocalDateTime.now());
        double totalVentasFacturas7Dias = facturas7Dias.stream().mapToDouble(Factura::getTotal).sum();

        List<Pedido> pedidosPagados7Dias = pedidoRepository.findByFechaBetween(inicio7Dias, LocalDateTime.now())
                .stream()
                .filter(p -> "pagado".equals(p.getEstado()))
                .collect(Collectors.toList());

        double totalVentasPedidos7Dias = pedidosPagados7Dias.stream()
                .mapToDouble(Pedido::getTotalPagado)
                .sum();

        double ventas7Dias = totalVentasFacturas7Dias + totalVentasPedidos7Dias;

        // Calcular ventas últimos 30 días (facturas + pedidos pagados)
        LocalDateTime inicio30Dias = LocalDateTime.now().minusDays(30);
        List<Factura> facturas30Dias = facturaRepository.findByFechaBetween(inicio30Dias, LocalDateTime.now());
        double totalVentasFacturas30Dias = facturas30Dias.stream().mapToDouble(Factura::getTotal).sum();

        List<Pedido> pedidosPagados30Dias = pedidoRepository.findByFechaBetween(inicio30Dias, LocalDateTime.now())
                .stream()
                .filter(p -> "pagado".equals(p.getEstado()))
                .collect(Collectors.toList());

        double totalVentasPedidos30Dias = pedidosPagados30Dias.stream()
                .mapToDouble(Pedido::getTotalPagado)
                .sum();

        double ventas30Dias = totalVentasFacturas30Dias + totalVentasPedidos30Dias;

        // Calcular ventas año actual (facturas + pedidos pagados)
        LocalDateTime inicioAño = LocalDateTime.now().withDayOfYear(1).withHour(0).withMinute(0).withSecond(0);
        List<Factura> facturasAño = facturaRepository.findByFechaBetween(inicioAño, LocalDateTime.now());
        double totalVentasFacturasAño = facturasAño.stream().mapToDouble(Factura::getTotal).sum();

        List<Pedido> pedidosPagadosAño = pedidoRepository.findByFechaBetween(inicioAño, LocalDateTime.now())
                .stream()
                .filter(p -> "pagado".equals(p.getEstado()))
                .collect(Collectors.toList());

        double totalVentasPedidosAño = pedidosPagadosAño.stream()
                .mapToDouble(Pedido::getTotalPagado)
                .sum();

        double ventasAño = totalVentasFacturasAño + totalVentasPedidosAño;

        // Definir objetivos (estos deberían venir de configuración)
        double objetivoHoy = 1800000;
        double objetivo7Dias = 18000000;
        double objetivo30Dias = 70000000;
        double objetivoAño = 1200000000;

        // Calcular porcentajes
        double porcentajeHoy = (totalVentas / objetivoHoy) * 100;
        double porcentaje7Dias = (ventas7Dias / objetivo7Dias) * 100;
        double porcentaje30Dias = (ventas30Dias / objetivo30Dias) * 100;
        double porcentajeAño = (ventasAño / objetivoAño) * 100;

        // Obtener pedidos del día
        List<Pedido> pedidos = pedidoRepository.findByFechaGreaterThanEqual(inicioDia);
        long pedidosPendientes = pedidos.stream().filter(p -> "pendiente".equals(p.getEstado())).count();
        long pedidosCompletados = pedidos.stream().filter(p -> "completado".equals(p.getEstado())).count();

        // Obtener estado del inventario
        List<Inventario> stockBajo = inventarioRepository.findProductosConStockBajo();
        List<Inventario> agotados = inventarioRepository.findProductosAgotados();

        // Obtener facturas pendientes
        List<Factura> facturasPendientes = facturaRepository.findFacturasPendientesPago();
        double montoPendiente = facturasPendientes.stream()
                .mapToDouble(Factura::getTotal)
                .sum();

        // Construir respuesta
        Map<String, Object> dashboard = new HashMap<>();

        // Ventas del día con porcentajes (incluye conteo total de transacciones)
        Map<String, Object> ventasHoy = new HashMap<>();
        ventasHoy.put("cantidad", facturas.size() + pedidosPagados.size()); // Total de transacciones
        ventasHoy.put("total", totalVentas);
        ventasHoy.put("objetivo", objetivoHoy);
        ventasHoy.put("porcentaje", Math.round(porcentajeHoy * 100.0) / 100.0);
        ventasHoy.put("facturas", facturas.size());
        ventasHoy.put("pedidosPagados", pedidosPagados.size());
        dashboard.put("ventasHoy", ventasHoy);

        // Ventas 7 días
        Map<String, Object> ventas7DiasMap = new HashMap<>();
        ventas7DiasMap.put("total", ventas7Dias);
        ventas7DiasMap.put("objetivo", objetivo7Dias);
        ventas7DiasMap.put("porcentaje", Math.round(porcentaje7Dias * 100.0) / 100.0);
        dashboard.put("ventas7Dias", ventas7DiasMap);

        // Ventas 30 días
        Map<String, Object> ventas30DiasMap = new HashMap<>();
        ventas30DiasMap.put("total", ventas30Dias);
        ventas30DiasMap.put("objetivo", objetivo30Dias);
        ventas30DiasMap.put("porcentaje", Math.round(porcentaje30Dias * 100.0) / 100.0);
        dashboard.put("ventas30Dias", ventas30DiasMap);

        // Ventas año
        Map<String, Object> ventasAñoMap = new HashMap<>();
        ventasAñoMap.put("total", ventasAño);
        ventasAñoMap.put("objetivo", objetivoAño);
        ventasAñoMap.put("porcentaje", Math.round(porcentajeAño * 100.0) / 100.0);
        dashboard.put("ventasAño", ventasAñoMap);

        // Pedidos del día
        Map<String, Object> pedidosHoy = new HashMap<>();
        pedidosHoy.put("total", pedidos.size());
        pedidosHoy.put("pendientes", pedidosPendientes);
        pedidosHoy.put("completados", pedidosCompletados);
        dashboard.put("pedidosHoy", pedidosHoy);

        // Inventario
        Map<String, Object> inventarioMap = new HashMap<>();
        inventarioMap.put("stockBajo", stockBajo.size());
        inventarioMap.put("agotados", agotados.size());
        inventarioMap.put("alertas", stockBajo.size() + agotados.size());
        dashboard.put("inventario", inventarioMap);

        // Facturación
        Map<String, Object> facturacionMap = new HashMap<>();
        facturacionMap.put("pendientesPago", facturasPendientes.size());
        facturacionMap.put("montoPendiente", montoPendiente);
        dashboard.put("facturacion", facturacionMap);

        dashboard.put("fecha", LocalDateTime.now());

        return dashboard;
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

    public List<Map<String, Object>> getPedidosPorHora(LocalDateTime fecha) {
        LocalDateTime inicioDia = fecha.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime finDia = inicioDia.plusDays(1);

        List<Pedido> pedidos = pedidoRepository.findByFechaBetween(inicioDia, finDia);

        // Inicializar contadores de pedidos por hora
        Map<Integer, Long> pedidosPorHora = new HashMap<>();
        for (int i = 0; i < 24; i++) {
            pedidosPorHora.put(i, 0L);
        }

        // Contar pedidos por hora
        pedidos.forEach(p -> {
            int hora = p.getFecha().getHour();
            pedidosPorHora.merge(hora, 1L, Long::sum);
        });

        // Convertir a lista de mapas para la respuesta
        return pedidosPorHora.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> horaPedido = new HashMap<>();
                    horaPedido.put("hora", String.format("%02d:00", entry.getKey()));
                    horaPedido.put("cantidad", entry.getValue());
                    return horaPedido;
                })
                .collect(Collectors.toList());
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
}
