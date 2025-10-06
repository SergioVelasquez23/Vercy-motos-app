package com.prog3.security.Services;

import java.time.LocalDateTime;
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
import com.prog3.security.Models.IngresoCaja;
import com.prog3.security.Repositories.CuadreCajaRepository;
import com.prog3.security.Repositories.PedidoRepository;
import com.prog3.security.Repositories.FacturaRepository;
import com.prog3.security.Repositories.GastoRepository;
import com.prog3.security.Repositories.IngresoCajaRepository;

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

    @Autowired
    private IngresoCajaRepository ingresoCajaRepository;

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
     * Resumen de ventas por forma de pago - SOLO pedidos asignados al cuadre específico
     * CORREGIDO: Solo incluye pedidos realmente pagados y asignados a este cuadre
     */
    private Map<String, Object> generarResumenVentas(CuadreCaja cuadre) {
        Map<String, Object> resumenVentas = new HashMap<>();

        // CORRECCIÓN: Solo usar pedidos específicamente asignados a este cuadre
        // Esto evita incluir pedidos de otros días o cuadres
        List<Pedido> pedidosPagados = pedidoRepository.findByCuadreCajaIdAndEstado(cuadre.get_id(), "pagado");

        System.out.println("📊 CORREGIDO - Pedidos pagados del cuadre " + cuadre.get_id() + ": " + pedidosPagados.size());

        // DEBUG: Verificar si hay pedidos sin asignar a cuadre que podrían corresponder a este período
        LocalDateTime fechaInicio = cuadre.getFechaApertura();
        LocalDateTime fechaFin = cuadre.getFechaCierre() != null ? cuadre.getFechaCierre() : LocalDateTime.now();
        
        List<Pedido> pedidosSinCuadreEnPeriodo = pedidoRepository.findPedidosPagadosSinCuadreEnRango(fechaInicio, fechaFin);
        if (!pedidosSinCuadreEnPeriodo.isEmpty()) {
            System.out.println("⚠️ ADVERTENCIA: " + pedidosSinCuadreEnPeriodo.size() + " pedidos pagados en el período sin asignar a cuadre");
            System.out.println("🔍 Estos pedidos deberían ser asignados al cuadre para un reporte preciso");
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
     * ✅ ACTUALIZADO: Ahora incluye facturas pagadas desde caja como gastos
     * Las facturas con pagadoDesdeCaja=true son efectivamente gastos que reducen la caja
     */
    private Map<String, Object> generarResumenGastos(CuadreCaja cuadre) {
        Map<String, Object> resumenGastos = new HashMap<>();

        // ✅ CORREGIDO: Obtener gastos específicos de este cuadre (consistente con endpoints)
        List<Gasto> gastos = gastoRepository.findByCuadreCajaId(cuadre.get_id());
        
        System.out.println("✅ FILTRO CORREGIDO - Gastos del cuadre: " + cuadre.get_id() + " | Encontrados: " + gastos.size());

        // Agrupar por tipo de gasto
        Map<String, Double> gastosPorTipo = new HashMap<>();
        Map<String, Integer> cantidadPorTipo = new HashMap<>();
        Map<String, Double> gastosPorFormaPago = new HashMap<>();
        double totalGastos = 0.0;

        for (Gasto gasto : gastos) {
            String tipoGasto = gasto.getTipoGastoNombre() != null ? gasto.getTipoGastoNombre() : "Otros";
            String formaPago = gasto.getFormaPago() != null ? gasto.getFormaPago().toLowerCase() : "efectivo";
            double monto = gasto.getMonto();
            
            // 🔍 DEBUG: Log detallado de cada gasto de este cuadre
            System.out.println("💸 Gasto ID: " + gasto.get_id() + 
                              " | Monto: $" + monto + 
                              " | Tipo: " + tipoGasto + 
                              " | Forma de pago: " + formaPago + 
                              " | Cuadre: " + gasto.getCuadreCajaId() +
                              " | Fecha: " + gasto.getFechaGasto());

            gastosPorTipo.merge(tipoGasto, monto, Double::sum);
            cantidadPorTipo.merge(tipoGasto, 1, Integer::sum);
            gastosPorFormaPago.merge(formaPago, monto, Double::sum);
            totalGastos += monto;
        }
        
        // 🔍 DEBUG: Log del total calculado
        System.out.println("💰 TOTAL GASTOS CALCULADO: $" + totalGastos);
        System.out.println("🔢 CANTIDAD DE GASTOS: " + gastos.size());

        resumenGastos.put("gastosPorTipo", gastosPorTipo);
        resumenGastos.put("cantidadPorTipo", cantidadPorTipo);
        resumenGastos.put("gastosPorFormaPago", gastosPorFormaPago);
        resumenGastos.put("totalGastos", totalGastos);
        resumenGastos.put("totalRegistros", gastos.size());

        // ✅ AGREGAR: Incluir información sobre facturas pagadas desde caja
        // (Las facturas pagadas desde caja también son gastos efectivos)
        Map<String, Object> resumenCompras = generarResumenCompras(cuadre);
        Double facturasDesdeCaja = (Double) resumenCompras.get("totalComprasDesdeCaja");
        double totalFacturasDesdeCaja = facturasDesdeCaja != null ? facturasDesdeCaja : 0.0;
        
        resumenGastos.put("facturasPagadasDesdeCaja", totalFacturasDesdeCaja);
        resumenGastos.put("totalGastosIncluyendoFacturas", totalGastos + totalFacturasDesdeCaja);
        
        System.out.println("🧾 FACTURAS PAGADAS DESDE CAJA: $" + totalFacturasDesdeCaja);
        System.out.println("💰 TOTAL GASTOS + FACTURAS: $" + (totalGastos + totalFacturasDesdeCaja));

        // Detalles de gastos
    List<Map<String, Object>> detallesGastos = gastos.stream()
        .map(this::convertirGastoADetalle)
        .collect(Collectors.toList());
    resumenGastos.put("detallesGastos", detallesGastos);
    // Calcular totalGastosDesdeCaja usando detallesGastos
    double totalGastosDesdeCaja = detallesGastos.stream()
        .filter(g -> Boolean.TRUE.equals(g.get("pagadoDesdeCaja")))
        .mapToDouble(g -> g.get("monto") != null ? (Double) g.get("monto") : 0.0)
        .sum();
    resumenGastos.put("totalGastosDesdeCaja", totalGastosDesdeCaja);

        System.out.println("💰 Resumen gastos generado (cuadre específico) - Total: " + totalGastos + ", Registros: " + gastos.size());
        return resumenGastos;
    }

    /**
     * Resumen de facturas de compras de ingredientes
     */
    /**
     * Resumen de facturas de compras
     * ✅ IMPORTANTE: Las facturas pagadas desde caja (pagadoDesdeCaja=true) 
     * se consideran GASTOS en el resumen financiero, ya que reducen el efectivo en caja
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
            
            // 🔍 DEBUG: Log detallado de cada factura
            System.out.println("📄 Factura ID: " + factura.get_id() + 
                              " | Total: $" + monto + 
                              " | Forma de pago: " + formaPago + 
                              " | Fecha: " + factura.getFecha());

            comprasPorFormaPago.merge(formaPago, monto, Double::sum);
            totalComprasDesdeCaja += monto;
        }
        
        // 🔍 DEBUG: Log del total calculado
        System.out.println("🛒 TOTAL COMPRAS DESDE CAJA: $" + totalComprasDesdeCaja);
        System.out.println("🔢 CANTIDAD DE FACTURAS DESDE CAJA: " + facturasDesdeCaja.size());

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

        // ✅ CORREGIDO: Entradas adicionales de este cuadre específico (consistente with endpoints)
        List<IngresoCaja> ingresos = ingresoCajaRepository.findByCuadreCajaId(cuadre.get_id());
        
        System.out.println("✅ FILTRO CORREGIDO - Ingresos del cuadre: " + cuadre.get_id() + " | Encontrados: " + ingresos.size());
        Map<String, Double> ingresosPorFormaPago = new HashMap<>();
        for (IngresoCaja ingreso : ingresos) {
            String formaPago = ingreso.getFormaPago() != null ? ingreso.getFormaPago().toLowerCase() : "efectivo";
            double monto = ingreso.getMonto();
            
            // 🔍 DEBUG: Log detallado de cada ingreso de este cuadre
            System.out.println("💰 Ingreso ID: " + ingreso.getId() + 
                              " | Monto: $" + monto + 
                              " | Forma de pago: " + formaPago + 
                              " | Cuadre: " + ingreso.getCuadreCajaId() +
                              " | Fecha: " + ingreso.getFechaIngreso());
                              
            ingresosPorFormaPago.merge(formaPago, monto, Double::sum);
        }

        // Entradas de efectivo
        double ventasEfectivo = ventasPorFormaPago != null ? ventasPorFormaPago.getOrDefault("efectivo", 0.0) : 0.0;
        double ingresosEfectivo = ingresosPorFormaPago.getOrDefault("efectivo", 0.0);

        // Salidas de efectivo
    // Solo descontar gastos pagados desde caja
    double gastosEfectivo = resumenGastos.get("totalGastosDesdeCaja") != null ? (double) resumenGastos.get("totalGastosDesdeCaja") : 0.0;
        double comprasEfectivo = comprasPorFormaPago != null ? comprasPorFormaPago.getOrDefault("efectivo", 0.0) : 0.0;

        // 🔍 DEBUG: Verificar que las facturas pagadas desde caja se están contando correctamente
        System.out.println("💰 MOVIMIENTOS DE EFECTIVO:");
        System.out.println("  Fondo inicial: $" + fondoInicial);
        System.out.println("  Ventas en efectivo: $" + ventasEfectivo);
        System.out.println("  Ingresos adicionales: $" + ingresosEfectivo);
        System.out.println("  Gastos directos en efectivo: $" + gastosEfectivo);
        System.out.println("  Facturas pagadas en efectivo (desde caja): $" + comprasEfectivo);

        // ✅ Cálculo del efectivo esperado (facturas pagadas desde caja se restan como gastos)
        double efectivoEsperado = fondoInicial + ventasEfectivo + ingresosEfectivo - gastosEfectivo - comprasEfectivo;
        System.out.println("  EFECTIVO ESPERADO: $" + efectivoEsperado);
        movimientos.put("fondoInicial", fondoInicial);
        movimientos.put("ventasEfectivo", ventasEfectivo);
        movimientos.put("ingresosEfectivo", ingresosEfectivo);
        movimientos.put("gastosEfectivo", gastosEfectivo);
        movimientos.put("comprasEfectivo", comprasEfectivo);
        movimientos.put("efectivoEsperado", efectivoEsperado);

        // Repetir para otras formas de pago (ej: transferencia)
        double ventasTransferencia = ventasPorFormaPago != null ? ventasPorFormaPago.getOrDefault("transferencia", 0.0) : 0.0;
        double ingresosTransferencia = ingresosPorFormaPago.getOrDefault("transferencia", 0.0);
        double gastosTransferencia = gastosPorFormaPago != null ? gastosPorFormaPago.getOrDefault("transferencia", 0.0) : 0.0;
        double comprasTransferencia = comprasPorFormaPago != null ? comprasPorFormaPago.getOrDefault("transferencia", 0.0) : 0.0;
        double transferenciaEsperada = ventasTransferencia + ingresosTransferencia - gastosTransferencia - comprasTransferencia;
        movimientos.put("ventasTransferencia", ventasTransferencia);
        movimientos.put("ingresosTransferencia", ingresosTransferencia);
        movimientos.put("gastosTransferencia", gastosTransferencia);
        movimientos.put("comprasTransferencia", comprasTransferencia);
        movimientos.put("transferenciaEsperada", transferenciaEsperada);

        movimientos.put("ingresosPorFormaPago", ingresosPorFormaPago);
        movimientos.put("totalIngresosCaja", ingresos.stream().mapToDouble(IngresoCaja::getMonto).sum());
        // Eliminados: efectivoDeclarado, diferencia
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

        // Obtener valores con validación de null y logging para debug
        Double ventasObj = (Double) resumenVentas.get("totalVentas");
        Double gastosObj = (Double) resumenGastos.get("totalGastos");
        Double comprasObj = (Double) resumenCompras.get("totalComprasDesdeCaja");

        double totalVentas = ventasObj != null ? ventasObj : 0.0;
        double totalGastosDirectos = gastosObj != null ? gastosObj : 0.0;
        double totalFacturasPagadasDesdeCaja = comprasObj != null ? comprasObj : 0.0;
        
        // ✅ CORRECCIÓN: Las facturas pagadas desde caja SON gastos
        double totalGastosReales = totalGastosDirectos + totalFacturasPagadasDesdeCaja;
        
        // 🔍 DEBUG: Log para identificar inconsistencias
        System.out.println("=== DEBUG RESUMEN FINAL ===");
        System.out.println("Total Ventas calculado: $" + totalVentas);
        System.out.println("Gastos directos: $" + totalGastosDirectos);
        System.out.println("Facturas pagadas desde caja: $" + totalFacturasPagadasDesdeCaja);
        System.out.println("TOTAL GASTOS REALES (gastos + facturas): $" + totalGastosReales);
        System.out.println("Gastos en efectivo: $" + movimientos.get("gastosEfectivo"));
        System.out.println("Compras en efectivo: $" + movimientos.get("comprasEfectivo"));
        System.out.println("=============================");

        // Usar valores corregidos
        resumenFinal.put("totalVentas", totalVentas);
        resumenFinal.put("totalGastos", totalGastosReales); // ✅ Ahora incluye facturas pagadas desde caja
        resumenFinal.put("totalCompras", totalFacturasPagadasDesdeCaja); // Para mantener compatibilidad
        resumenFinal.put("utilidadBruta", totalVentas - totalGastosReales); // ✅ Cálculo correcto
        resumenFinal.put("efectivoEsperado", movimientos.get("efectivoEsperado"));
        
        // ✅ Agregar valores de efectivo para consistencia con "Movimientos de Efectivo"
        resumenFinal.put("gastosEfectivo", movimientos.get("gastosEfectivo"));
        resumenFinal.put("comprasEfectivo", movimientos.get("comprasEfectivo"));
        resumenFinal.put("ventasEfectivo", movimientos.get("ventasEfectivo"));
        resumenFinal.put("fondoInicial", movimientos.get("fondoInicial"));
        
        // Desglose para claridad
        resumenFinal.put("gastosDirectos", totalGastosDirectos);
        resumenFinal.put("facturasPagadasDesdeCaja", totalFacturasPagadasDesdeCaja);
        
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
