package com.prog3.security.Services;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.prog3.security.Models.CuadreCaja;
import com.prog3.security.Models.Pedido;
import com.prog3.security.Models.Factura;
import com.prog3.security.Models.Gasto;
import com.prog3.security.Repositories.CuadreCajaRepository;
import com.prog3.security.Repositories.PedidoRepository;
import com.prog3.security.Repositories.FacturaRepository;
import com.prog3.security.Repositories.GastoRepository;

/**
 * Servicio para generar resúmenes detallados de cuadre de caja
 */
@Service
public class ResumenCierreService {

    @Autowired
    private CuadreCajaRepository cuadreCajaRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private GastoRepository gastoRepository;

    /**
     * Genera un resumen completo de un cuadre de caja específico
     *
     * @param cuadreCajaId ID del cuadre de caja
     * @return Map con el resumen detallado
     */
    public Map<String, Object> generarResumenCuadre(String cuadreCajaId) {
        System.out.println("🧾 Generando resumen para cuadre de caja: " + cuadreCajaId);

        try {
            // Obtener el cuadre de caja
            CuadreCaja cuadre = cuadreCajaRepository.findById(cuadreCajaId).orElse(null);
            if (cuadre == null) {
                throw new RuntimeException("Cuadre de caja no encontrado");
            }

            Map<String, Object> resumen = new HashMap<>();

            // 1. Información básica del cuadre
            resumen.put("cuadreInfo", generarInfoBasicaCuadre(cuadre));

            // 2. Resumen de ventas por forma de pago
            Map<String, Object> resumenVentas = generarResumenVentas(cuadre);
            resumen.put("resumenVentas", resumenVentas);

            // 3. Resumen de gastos
            Map<String, Object> resumenGastos = generarResumenGastos(cuadre);
            resumen.put("resumenGastos", resumenGastos);

            // 4. Resumen de facturas de compras de ingredientes (si aplica)
            Map<String, Object> resumenCompras = generarResumenCompras(cuadre);
            resumen.put("resumenCompras", resumenCompras);

            // 5. Movimientos de efectivo detallados
            Map<String, Object> movimientosEfectivo = generarMovimientosEfectivo(cuadre);
            resumen.put("movimientosEfectivo", movimientosEfectivo);

            // 6. Resumen final y diferencias
            Map<String, Object> resumenFinal = generarResumenFinal(cuadre);
            resumen.put("resumenFinal", resumenFinal);

            System.out.println("✅ Resumen generado exitosamente");
            return resumen;

        } catch (Exception e) {
            System.err.println("❌ Error al generar resumen de cuadre: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al generar resumen de cuadre: " + e.getMessage(), e);
        }
    }

    /**
     * Información básica del cuadre
     */
    private Map<String, Object> generarInfoBasicaCuadre(CuadreCaja cuadre) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", cuadre.get_id());
        info.put("nombre", cuadre.getNombre());
        info.put("responsable", cuadre.getResponsable());
        info.put("fechaApertura", cuadre.getFechaApertura());
        info.put("fechaCierre", cuadre.getFechaCierre());
        info.put("estado", cuadre.getEstado());
        info.put("cerrada", cuadre.isCerrada());
        info.put("fondoInicial", cuadre.getFondoInicial());
        info.put("fondoInicialDesglosado", cuadre.getFondoInicialDesglosado());
        return info;
    }

    /**
     * Resumen de ventas por forma de pago - INCLUYE todos los pedidos de la
     * fecha (como CuadreCajaService)
     */
    private Map<String, Object> generarResumenVentas(CuadreCaja cuadre) {
        Map<String, Object> resumenVentas = new HashMap<>();

        // Obtener la fecha de referencia (fecha de apertura del cuadre)
        LocalDateTime fechaReferencia = cuadre.getFechaApertura();

        // CAMBIO: Usar el mismo criterio que CuadreCajaService - todos los pedidos de la fecha usando fechaPago
        List<Pedido> pedidosPagados = pedidoRepository.findByFechaPagoGreaterThanEqualAndEstado(fechaReferencia, "pagado");

        System.out.println("📊 Pedidos encontrados para fecha " + fechaReferencia + ": " + pedidosPagados.size());

        // Solo pedidos específicos del cuadre para comparación
        List<Pedido> pedidosDelCuadre = pedidoRepository.findByCuadreCajaIdAndEstado(cuadre.get_id(), "pagado");
        System.out.println("📊 Pedidos asignados al cuadre " + cuadre.get_id() + ": " + pedidosDelCuadre.size());

        // DEBUG: Verificar si hay pedidos sin asignar a cuadre
        List<Pedido> pedidosSinCuadre = pedidoRepository.findPedidosPagadosSinCuadre();
        System.out.println("⚠️ DEBUG: Pedidos pagados sin asignar a cuadre: " + pedidosSinCuadre.size());

        if (!pedidosSinCuadre.isEmpty()) {
            System.out.println("🔍 Primeros 5 pedidos sin cuadre:");
            for (int i = 0; i < Math.min(5, pedidosSinCuadre.size()); i++) {
                Pedido p = pedidosSinCuadre.get(i);
                System.out.println("   - ID: " + p.get_id() + ", Fecha: " + p.getFecha() + ", FormaPago: " + p.getFormaPago() + ", Total: " + p.getTotalPagado());
            }
        }

        // Agrupar por forma de pago
        Map<String, Double> ventasPorFormaPago = new HashMap<>();
        Map<String, Integer> cantidadPorFormaPago = new HashMap<>();
        double totalVentas = 0.0;

        for (Pedido pedido : pedidosPagados) {
            String formaPago = pedido.getFormaPago() != null ? pedido.getFormaPago().toLowerCase() : "sin_definir";
            double monto = pedido.getTotalPagado();

            ventasPorFormaPago.merge(formaPago, monto, Double::sum);
            cantidadPorFormaPago.merge(formaPago, 1, Integer::sum);
            totalVentas += monto;
        }

        resumenVentas.put("ventasPorFormaPago", ventasPorFormaPago);
        resumenVentas.put("cantidadPorFormaPago", cantidadPorFormaPago);
        resumenVentas.put("totalVentas", totalVentas);
        resumenVentas.put("totalPedidos", pedidosPagados.size());

        // Detalles adicionales
        List<Map<String, Object>> detallesPedidos = pedidosPagados.stream()
                .map(this::convertirPedidoADetalle)
                .collect(Collectors.toList());
        resumenVentas.put("detallesPedidos", detallesPedidos);

        System.out.println("📊 Resumen ventas generado - Total: " + totalVentas + ", Pedidos: " + pedidosPagados.size());
        return resumenVentas;
    }

    /**
     * Resumen de gastos agrupados por tipo
     */
    private Map<String, Object> generarResumenGastos(CuadreCaja cuadre) {
        Map<String, Object> resumenGastos = new HashMap<>();

        LocalDateTime fechaInicio = cuadre.getFechaApertura();
        LocalDateTime fechaFin = cuadre.getFechaCierre() != null ? cuadre.getFechaCierre() : LocalDateTime.now();

        // Obtener gastos del período
        List<Gasto> gastos = gastoRepository.findByFechaGastoBetween(fechaInicio, fechaFin);

        // Agrupar por tipo de gasto
        Map<String, Double> gastosPorTipo = new HashMap<>();
        Map<String, Integer> cantidadPorTipo = new HashMap<>();
        Map<String, Double> gastosPorFormaPago = new HashMap<>();
        double totalGastos = 0.0;

        for (Gasto gasto : gastos) {
            String tipoGasto = gasto.getTipoGastoNombre() != null ? gasto.getTipoGastoNombre() : "Otros";
            String formaPago = gasto.getFormaPago() != null ? gasto.getFormaPago().toLowerCase() : "efectivo";
            double monto = gasto.getMonto();

            gastosPorTipo.merge(tipoGasto, monto, Double::sum);
            cantidadPorTipo.merge(tipoGasto, 1, Integer::sum);
            gastosPorFormaPago.merge(formaPago, monto, Double::sum);
            totalGastos += monto;
        }

        resumenGastos.put("gastosPorTipo", gastosPorTipo);
        resumenGastos.put("cantidadPorTipo", cantidadPorTipo);
        resumenGastos.put("gastosPorFormaPago", gastosPorFormaPago);
        resumenGastos.put("totalGastos", totalGastos);
        resumenGastos.put("totalRegistros", gastos.size());

        // Detalles de gastos
        List<Map<String, Object>> detallesGastos = gastos.stream()
                .map(this::convertirGastoADetalle)
                .collect(Collectors.toList());
        resumenGastos.put("detallesGastos", detallesGastos);

        System.out.println("💰 Resumen gastos generado - Total: " + totalGastos + ", Registros: " + gastos.size());
        return resumenGastos;
    }

    /**
     * Resumen de facturas de compras de ingredientes
     */
    private Map<String, Object> generarResumenCompras(CuadreCaja cuadre) {
        Map<String, Object> resumenCompras = new HashMap<>();

        LocalDateTime fechaInicio = cuadre.getFechaApertura();
        LocalDateTime fechaFin = cuadre.getFechaCierre() != null ? cuadre.getFechaCierre() : LocalDateTime.now();

        // Obtener todas las facturas de compras del período
        List<Factura> todasLasFacturas = facturaRepository.findByFechaBetween(fechaInicio, fechaFin)
                .stream()
                .filter(f -> "compra".equals(f.getTipoFactura()))
                .collect(Collectors.toList());

        // Separar facturas pagadas desde caja y las que no
        List<Factura> facturasDesdeCaja = todasLasFacturas.stream()
                .filter(Factura::isPagadoDesdeCaja)
                .collect(Collectors.toList());

        List<Factura> facturasNoDesdeCaja = todasLasFacturas.stream()
                .filter(f -> !f.isPagadoDesdeCaja())
                .collect(Collectors.toList());

        // Resumen de facturas pagadas desde caja (estas afectan el flujo de efectivo)
        Map<String, Double> comprasPorFormaPago = new HashMap<>();
        double totalComprasDesdeCaja = 0.0;

        for (Factura factura : facturasDesdeCaja) {
            String formaPago = factura.getMedioPago() != null ? factura.getMedioPago().toLowerCase() : "efectivo";
            double monto = factura.getTotal();

            comprasPorFormaPago.merge(formaPago, monto, Double::sum);
            totalComprasDesdeCaja += monto;
        }

        // Resumen de facturas NO pagadas desde caja (solo informativo)
        double totalComprasNoDesdeCaja = facturasNoDesdeCaja.stream()
                .mapToDouble(Factura::getTotal)
                .sum();

        resumenCompras.put("comprasPorFormaPago", comprasPorFormaPago);
        resumenCompras.put("totalComprasDesdeCaja", totalComprasDesdeCaja);
        resumenCompras.put("totalFacturasDesdeCaja", facturasDesdeCaja.size());

        // Información adicional sobre compras no pagadas desde caja
        resumenCompras.put("totalComprasNoDesdeCaja", totalComprasNoDesdeCaja);
        resumenCompras.put("totalFacturasNoDesdeCaja", facturasNoDesdeCaja.size());
        resumenCompras.put("totalComprasGenerales", totalComprasDesdeCaja + totalComprasNoDesdeCaja);
        resumenCompras.put("totalFacturasGenerales", todasLasFacturas.size());

        // Detalles de compras desde caja (las que afectan el flujo)
        List<Map<String, Object>> detallesComprasDesdeCaja = facturasDesdeCaja.stream()
                .map(this::convertirFacturaADetalle)
                .collect(Collectors.toList());
        resumenCompras.put("detallesComprasDesdeCaja", detallesComprasDesdeCaja);

        // Detalles de compras NO desde caja (solo informativo)
        List<Map<String, Object>> detallesComprasNoDesdeCaja = facturasNoDesdeCaja.stream()
                .map(this::convertirFacturaADetalle)
                .collect(Collectors.toList());
        resumenCompras.put("detallesComprasNoDesdeCaja", detallesComprasNoDesdeCaja);

        System.out.println("🛒 Resumen compras generado - Total desde caja: " + totalComprasDesdeCaja + ", Facturas desde caja: " + facturasDesdeCaja.size());
        return resumenCompras;
    }

    /**
     * Movimientos de efectivo detallados
     */
    private Map<String, Object> generarMovimientosEfectivo(CuadreCaja cuadre) {
        Map<String, Object> movimientos = new HashMap<>();

        double fondoInicial = cuadre.getFondoInicial();

        // Calcular entradas y salidas de efectivo
        Map<String, Object> resumenVentas = generarResumenVentas(cuadre);
        Map<String, Object> resumenGastos = generarResumenGastos(cuadre);
        Map<String, Object> resumenCompras = generarResumenCompras(cuadre);

        @SuppressWarnings("unchecked")
        Map<String, Double> ventasPorFormaPago = (Map<String, Double>) resumenVentas.get("ventasPorFormaPago");
        @SuppressWarnings("unchecked")
        Map<String, Double> gastosPorFormaPago = (Map<String, Double>) resumenGastos.get("gastosPorFormaPago");
        @SuppressWarnings("unchecked")
        Map<String, Double> comprasPorFormaPago = (Map<String, Double>) resumenCompras.get("comprasPorFormaPago");

        // Entradas de efectivo
        double ventasEfectivo = ventasPorFormaPago != null ? ventasPorFormaPago.getOrDefault("efectivo", 0.0) : 0.0;

        // Salidas de efectivo
        double gastosEfectivo = gastosPorFormaPago != null ? gastosPorFormaPago.getOrDefault("efectivo", 0.0) : 0.0;
        double comprasEfectivo = comprasPorFormaPago != null ? comprasPorFormaPago.getOrDefault("efectivo", 0.0) : 0.0;

        // Cálculo del efectivo esperado
        double efectivoEsperado = fondoInicial + ventasEfectivo - gastosEfectivo - comprasEfectivo;
        double efectivoDeclarado = cuadre.getEfectivoDeclarado();
        double diferencia = efectivoDeclarado - efectivoEsperado;

        movimientos.put("fondoInicial", fondoInicial);
        movimientos.put("ventasEfectivo", ventasEfectivo);
        movimientos.put("gastosEfectivo", gastosEfectivo);
        movimientos.put("comprasEfectivo", comprasEfectivo);
        movimientos.put("efectivoEsperado", efectivoEsperado);
        movimientos.put("efectivoDeclarado", efectivoDeclarado);
        movimientos.put("diferencia", diferencia);
        movimientos.put("tolerancia", cuadre.getTolerancia());
        movimientos.put("cuadrado", Math.abs(diferencia) <= cuadre.getTolerancia());

        return movimientos;
    }

    /**
     * Resumen final con totales generales
     */
    private Map<String, Object> generarResumenFinal(CuadreCaja cuadre) {
        Map<String, Object> resumenFinal = new HashMap<>();

        Map<String, Object> resumenVentas = generarResumenVentas(cuadre);
        Map<String, Object> resumenGastos = generarResumenGastos(cuadre);
        Map<String, Object> resumenCompras = generarResumenCompras(cuadre);
        Map<String, Object> movimientos = generarMovimientosEfectivo(cuadre);

        // Obtener valores con validación de null
        Double ventasObj = (Double) resumenVentas.get("totalVentas");
        Double gastosObj = (Double) resumenGastos.get("totalGastos");
        Double comprasObj = (Double) resumenCompras.get("totalComprasDesdeCaja");

        double totalVentas = ventasObj != null ? ventasObj : 0.0;
        double totalGastos = gastosObj != null ? gastosObj : 0.0;
        double totalCompras = comprasObj != null ? comprasObj : 0.0;

        resumenFinal.put("totalVentas", totalVentas);
        resumenFinal.put("totalGastos", totalGastos);
        resumenFinal.put("totalCompras", totalCompras);
        resumenFinal.put("utilidadBruta", totalVentas - totalGastos - totalCompras);
        resumenFinal.put("efectivoEsperado", movimientos.get("efectivoEsperado"));
        resumenFinal.put("efectivoDeclarado", movimientos.get("efectivoDeclarado"));
        resumenFinal.put("diferencia", movimientos.get("diferencia"));
        resumenFinal.put("cuadrado", movimientos.get("cuadrado"));

        return resumenFinal;
    }

    // Métodos auxiliares para convertir entidades a detalles
    private Map<String, Object> convertirPedidoADetalle(Pedido pedido) {
        Map<String, Object> detalle = new HashMap<>();
        detalle.put("id", pedido.get_id());
        detalle.put("mesa", pedido.getMesa());
        detalle.put("total", pedido.getTotalPagado());
        detalle.put("formaPago", pedido.getFormaPago());
        detalle.put("fechaPago", pedido.getFechaPago());
        detalle.put("tipo", pedido.getTipo());
        return detalle;
    }

    private Map<String, Object> convertirGastoADetalle(Gasto gasto) {
        Map<String, Object> detalle = new HashMap<>();
        detalle.put("id", gasto.get_id());
        detalle.put("concepto", gasto.getConcepto());
        detalle.put("monto", gasto.getMonto());
        detalle.put("tipoGasto", gasto.getTipoGastoNombre());
        detalle.put("formaPago", gasto.getFormaPago());
        detalle.put("fecha", gasto.getFechaGasto());
        detalle.put("responsable", gasto.getResponsable());
        return detalle;
    }

    private Map<String, Object> convertirFacturaADetalle(Factura factura) {
        Map<String, Object> detalle = new HashMap<>();
        detalle.put("id", factura.get_id());
        detalle.put("numero", factura.getNumero());
        detalle.put("proveedor", factura.getProveedorNombre());
        detalle.put("total", factura.getTotal());
        detalle.put("medioPago", factura.getMedioPago());
        detalle.put("fecha", factura.getFecha());
        detalle.put("pagadoDesdeCaja", factura.isPagadoDesdeCaja());
        detalle.put("observaciones", factura.getObservaciones());
        return detalle;
    }
}
