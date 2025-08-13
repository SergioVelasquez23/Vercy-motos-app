package com.prog3.security.Controllers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.prog3.security.Services.ReporteService;
import com.prog3.security.Services.ResponseService;
import com.prog3.security.Utils.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;

@CrossOrigin
@RestController
@RequestMapping("api")
public class DashboardController {

    @Autowired
    private ReporteService reporteService;

    @Autowired
    private ResponseService responseService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard(HttpServletRequest request) {
        try {
            Map<String, Object> dashboard = reporteService.getDashboard();
            return responseService.success(dashboard, "Dashboard obtenido exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener dashboard: " + e.getMessage());
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

    @GetMapping("/ingresos-egresos")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getIngresosEgresos(
            @RequestParam(defaultValue = "12") int ultimosMeses) {
        try {
            List<Map<String, Object>> ingresosEgresos = reporteService.getIngresosVsEgresos(ultimosMeses);
            return responseService.success(ingresosEgresos, "Ingresos y egresos obtenidos exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener ingresos y egresos: " + e.getMessage());
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

    @GetMapping("/ultimos-pedidos")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getUltimosPedidos(
            @RequestParam(defaultValue = "10") int limite) {
        try {
            List<Map<String, Object>> ultimosPedidos = reporteService.getUltimosPedidosConDetalles(limite);
            return responseService.success(ultimosPedidos, "Últimos pedidos obtenidos exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener últimos pedidos: " + e.getMessage());
        }
    }

    @GetMapping("/vendedores-mes")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getVendedoresDelMes(
            @RequestParam(defaultValue = "30") int dias) {
        try {
            List<Map<String, Object>> vendedores = reporteService.getVendedoresDelMes(dias);
            return responseService.success(vendedores, "Vendedores del mes obtenidos exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener vendedores del mes: " + e.getMessage());
        }
    }
}
