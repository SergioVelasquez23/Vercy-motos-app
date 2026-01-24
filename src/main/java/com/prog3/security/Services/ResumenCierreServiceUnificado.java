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
 * Servicio unificado para generar resúmenes detallados de cuadre de caja
 * Incluye lógica mejorada para manejar:
 * - Facturas de compras que salen de caja (se descuentan del efectivo esperado)
 * - Gastos que salen de caja (se descuentan del efectivo esperado)
 * - Pedidos eliminados (se restan de ventas)
 */
@Service
public class ResumenCierreServiceUnificado {

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
     * con todos los cálculos unificados
     *
     * @param cuadreCajaId ID del cuadre de caja
     * @return Map con el resumen detallado
     */
    public Map<String, Object> generarResumenCuadre(String cuadreCajaId) {
        System.out.println("🧾 Generando resumen unificado para cuadre de caja: " + cuadreCajaId);

        try {
            // Obtener el cuadre de caja
            CuadreCaja cuadre = cuadreCajaRepository.findById(cuadreCajaId).orElse(null);
            if (cuadre == null) {
                throw new RuntimeException("Cuadre de caja no encontrado");
            }

            Map<String, Object> resumen = new HashMap<>();

            // 1. Información básica del cuadre
            resumen.put("cuadreInfo", generarInfoBasicaCuadre(cuadre));

            // 2. Resumen de ventas por forma de pago (incluyendo manejo de pedidos eliminados)
            Map<String, Object> resumenVentas = generarResumenVentas(cuadre);
            resumen.put("resumenVentas", resumenVentas);

            // 3. Resumen de gastos (incluyendo facturas pagadas desde caja)
            Map<String, Object> resumenGastos = generarResumenGastos(cuadre);
            resumen.put("resumenGastos", resumenGastos);

            // 4. Resumen de facturas de compras de ingredientes
            Map<String, Object> resumenCompras = generarResumenCompras(cuadre);
            resumen.put("resumenCompras", resumenCompras);

            // 5. Movimientos de efectivo detallados
            Map<String, Object> movimientosEfectivo = generarMovimientosEfectivo(cuadre);
            resumen.put("movimientosEfectivo", movimientosEfectivo);

            // 6. Resumen final y diferencias
            Map<String, Object> resumenFinal = generarResumenFinal(cuadre);
            resumen.put("resumenFinal", resumenFinal);

            System.out.println("✅ Resumen unificado generado exitosamente");
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
        info.put("fondoInicial", cuadre.getFondoInicial());
        info.put("fondoInicialDesglosado", cuadre.getFondoInicialDesglosado());
        info.put("cerrada", cuadre.isCerrada());
        info.put("estado", cuadre.getEstado());
        
        return info;
    }

    /**
     * Resumen de ventas por forma de pago
     * - Maneja pedidos pagados regulares
     * - Incluye pagos parciales 
     * - Tiene en cuenta pedidos eliminados (eliminados después de pagados)
     * - NO cuenta facturas de compra como ventas
     */
    private Map<String, Object> generarResumenVentas(CuadreCaja cuadre) {
        Map<String, Object> resumenVentas = new HashMap<>();

        // CORRECCIÓN: Solo usar pedidos específicamente asignados a este cuadre
        // Esto evita incluir pedidos de otros días o cuadres
        List<Pedido> pedidosPagados = pedidoRepository.findByCuadreCajaIdAndEstado(cuadre.get_id(), "pagado");

        System.out.println("📊 Pedidos pagados del cuadre " + cuadre.get_id() + ": " + pedidosPagados.size());

        // DEBUG: Verificar si hay pedidos sin asignar a cuadre que podrían corresponder a este período
        LocalDateTime fechaInicio = cuadre.getFechaApertura();
        LocalDateTime fechaFin = cuadre.getFechaCierre() != null ? cuadre.getFechaCierre() : LocalDateTime.now();
        
        List<Pedido> pedidosSinCuadreEnPeriodo = pedidoRepository.findPedidosPagadosSinCuadreEnRango(fechaInicio, fechaFin);
        if (!pedidosSinCuadreEnPeriodo.isEmpty()) {
            System.out.println("⚠️ ADVERTENCIA: " + pedidosSinCuadreEnPeriodo.size() + " pedidos pagados en el período sin asignar a cuadre");
            System.out.println("🔍 Estos pedidos deberían ser asignados al cuadre para un reporte preciso");
        }
        
        // Obtener pedidos eliminados que fueron pagados previamente, para restarlos de las ventas
        List<Pedido> pedidosEliminados = pedidoRepository.findByCuadreCajaIdAndEstado(cuadre.get_id(), "eliminado_pagado");
        System.out.println("⚠️ Pedidos eliminados después de pagados: " + pedidosEliminados.size());

        // Agrupar por forma de pago
        Map<String, Double> ventasPorFormaPago = new HashMap<>();
        Map<String, Integer> cantidadPorFormaPago = new HashMap<>();
        double totalVentas = 0.0;
        
        // Registrar ventas eliminadas por forma de pago
        Map<String, Double> ventasEliminadasPorFormaPago = new HashMap<>();
        double totalVentasEliminadas = 0.0;
        
        // Primero procesar pedidos pagados normales
        for (Pedido pedido : pedidosPagados) {
            // ✅ CALCULAR MONTO CORRECTO: Total con descuentos y propinas aplicados
            double totalItems = pedido.getTotal();
            double descuento = pedido.getDescuento(); // Ya es primitivo, no puede ser null
            double propina = pedido.getPropina(); // Ya es primitivo, no puede ser null
            double totalConDescuento = Math.max(totalItems - descuento, 0.0);
            double montoFinalPedido = totalConDescuento + propina;

            // Si tiene pagos parciales, procesar cada pago individualmente
            if (pedido.getPagosParciales() != null && !pedido.getPagosParciales().isEmpty()) {
                for (Pedido.PagoParcial pago : pedido.getPagosParciales()) {
                    String formaPago = pago.getFormaPago().toLowerCase();
                    double monto = pago.getMonto();
                    
                    ventasPorFormaPago.merge(formaPago, monto, Double::sum);
                    cantidadPorFormaPago.merge(formaPago, 1, Integer::sum);
                    totalVentas += monto;
                }
            } else {
                // Pago único - usar el monto final calculado con descuentos y propinas
                String formaPago = pedido.getFormaPago() != null ? pedido.getFormaPago().toLowerCase() : "efectivo";
                double monto =
                        pedido.getTotalPagado() > 0 ? pedido.getTotalPagado() : montoFinalPedido;
                
                ventasPorFormaPago.merge(formaPago, monto, Double::sum);
                cantidadPorFormaPago.merge(formaPago, 1, Integer::sum);
                totalVentas += monto;
            }
        }
        
        // Ahora restar pedidos eliminados que fueron pagados
        for (Pedido pedidoEliminado : pedidosEliminados) {
            // ✅ CALCULAR MONTO CORRECTO: Total con descuentos y propinas aplicados
            double totalItems = pedidoEliminado.getTotal();
            double descuento = pedidoEliminado.getDescuento(); // Ya es primitivo, no puede ser null
            double propina = pedidoEliminado.getPropina(); // Ya es primitivo, no puede ser null
            double totalConDescuento = Math.max(totalItems - descuento, 0.0);
            double montoFinalEliminado = totalConDescuento + propina;

            // Si tiene pagos parciales, procesar cada pago individualmente
            if (pedidoEliminado.getPagosParciales() != null && !pedidoEliminado.getPagosParciales().isEmpty()) {
                for (Pedido.PagoParcial pago : pedidoEliminado.getPagosParciales()) {
                    String formaPago = pago.getFormaPago().toLowerCase();
                    double monto = pago.getMonto();
                    
                    ventasEliminadasPorFormaPago.merge(formaPago, monto, Double::sum);
                    totalVentasEliminadas += monto;
                    
                    // Restar de las ventas totales
                    ventasPorFormaPago.merge(formaPago, -monto, Double::sum);
                    cantidadPorFormaPago.merge(formaPago, -1, Integer::sum);
                    totalVentas -= monto;
                }
            } else {
                // Pago único - usar el monto final calculado con descuentos y propinas
                String formaPago = pedidoEliminado.getFormaPago() != null ? 
                    pedidoEliminado.getFormaPago().toLowerCase() : "efectivo";
                double monto = pedidoEliminado.getTotalPagado() > 0 ? 
                        pedidoEliminado.getTotalPagado() : montoFinalEliminado;
                
                ventasEliminadasPorFormaPago.merge(formaPago, monto, Double::sum);
                totalVentasEliminadas += monto;
                
                // Restar de las ventas totales
                ventasPorFormaPago.merge(formaPago, -monto, Double::sum);
                cantidadPorFormaPago.merge(formaPago, -1, Integer::sum);
                totalVentas -= monto;
            }
        }

        resumenVentas.put("ventasPorFormaPago", ventasPorFormaPago);
        resumenVentas.put("cantidadPorFormaPago", cantidadPorFormaPago);
        resumenVentas.put("totalVentas", totalVentas);
        resumenVentas.put("totalPedidos", pedidosPagados.size());
        
        // Agregar información sobre pedidos eliminados
        resumenVentas.put("ventasEliminadasPorFormaPago", ventasEliminadasPorFormaPago);
        resumenVentas.put("totalVentasEliminadas", totalVentasEliminadas);
        resumenVentas.put("totalPedidosEliminados", pedidosEliminados.size());

        // Detalles adicionales
        List<Map<String, Object>> detallesPedidos = pedidosPagados.stream()
                .map(this::convertirPedidoADetalle)
                .collect(Collectors.toList());
        resumenVentas.put("detallesPedidos", detallesPedidos);
        
        // Agregar detalles de pedidos eliminados
        List<Map<String, Object>> detallesPedidosEliminados = pedidosEliminados.stream()
                .map(this::convertirPedidoADetalle)
                .collect(Collectors.toList());
        resumenVentas.put("detallesPedidosEliminados", detallesPedidosEliminados);

        System.out.println("📊 Resumen ventas generado - Total: " + totalVentas + 
                          ", Pedidos: " + pedidosPagados.size() + 
                          ", Eliminados: " + pedidosEliminados.size() + 
                          ", Monto Eliminado: " + totalVentasEliminadas);
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
        
        System.out.println("✅ Gastos del cuadre: " + cuadre.get_id() + " | Encontrados: " + gastos.size());

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
        // Calcular totalGastosDesdeCaja usando detallesGastos - solo gastos con pagadoDesdeCaja=true
        // Añadimos logs para depurar
        System.out.println("🔍 DEPURANDO GASTOS PAGADOS DESDE CAJA:");
        for (Map<String, Object> g : detallesGastos) {
            System.out.println("  - ID: " + g.get("id") 
                + " | Monto: $" + g.get("monto") 
                + " | Forma de Pago: " + g.get("formaPago") 
                + " | PagadoDesdeCaja: " + g.get("pagadoDesdeCaja"));
        }
        
        double totalGastosDesdeCaja = detallesGastos.stream()
            .filter(g -> {
                boolean isPagadoDesdeCaja = Boolean.TRUE.equals(g.get("pagadoDesdeCaja"));
                if (isPagadoDesdeCaja) {
                    System.out.println("✅ Gasto contado como pagado desde caja: " + g.get("id") + " - $" + g.get("monto"));
                } else {
                    System.out.println("❌ Gasto NO contado como pagado desde caja: " + g.get("id") + " - $" + g.get("monto"));
                }
                return isPagadoDesdeCaja;
            })
            .mapToDouble(g -> g.get("monto") != null ? (Double) g.get("monto") : 0.0)
            .sum();
        
        System.out.println("💰 TOTAL GASTOS DESDE CAJA CALCULADO: $" + totalGastosDesdeCaja);
        resumenGastos.put("totalGastosDesdeCaja", totalGastosDesdeCaja);

        System.out.println("💰 Resumen gastos generado (cuadre específico) - Total: " + totalGastos + ", Registros: " + gastos.size());
        return resumenGastos;
    }

    /**
     * Resumen de facturas de compras
     * ✅ IMPORTANTE: Las facturas pagadas desde caja (pagadoDesdeCaja=true) 
     * se consideran GASTOS en el resumen financiero, ya que reducen el efectivo en caja
     */
    private Map<String, Object> generarResumenCompras(CuadreCaja cuadre) {
        Map<String, Object> resumenCompras = new HashMap<>();

        LocalDateTime fechaInicio = cuadre.getFechaApertura();
        LocalDateTime fechaFin = cuadre.getFechaCierre() != null ? cuadre.getFechaCierre() : LocalDateTime.now();
        String cuadreCajaId = cuadre.get_id();

        // Obtener todas las facturas de compras del período
        List<Factura> todasLasFacturas = facturaRepository.findByFechaBetween(fechaInicio, fechaFin)
                .stream()
                .filter(f -> "compra".equals(f.getTipoFactura()))
                .collect(Collectors.toList());

        System.out.println("🔍 DEBUG COMPRAS: Total facturas de compra en período: "
                + todasLasFacturas.size());

        // ✅ CORREGIDO: Filtrar por cuadreCajaId para facturas pagadas desde caja
        // Las facturas deben tener cuadreCajaId asignado O ser del período y marcadas como
        // pagadoDesdeCaja
        List<Factura> facturasDesdeCaja = todasLasFacturas.stream()
                .filter(f -> {
                    boolean isPagadoDesdeCaja = f.isPagadoDesdeCaja();
                    String facturaCuadreCajaId = f.getCuadreCajaId();

                    // Opción 1: La factura tiene el cuadreCajaId asignado correctamente
                    boolean tieneIdCorrecto =
                            facturaCuadreCajaId != null && facturaCuadreCajaId.equals(cuadreCajaId);

                    // Opción 2: La factura está marcada como pagada desde caja pero no tiene
                    // cuadreCajaId
                    // (para compatibilidad con facturas antiguas)
                    boolean sinIdPeroEnPeriodo = isPagadoDesdeCaja && facturaCuadreCajaId == null;

                    System.out.println("📄 Factura " + f.get_id() + " | PagadoDesdeCaja: "
                            + isPagadoDesdeCaja + " | CuadreCajaId: " + facturaCuadreCajaId
                            + " | Incluir: " + (tieneIdCorrecto || sinIdPeroEnPeriodo));

                    return tieneIdCorrecto || sinIdPeroEnPeriodo;
                })
                .collect(Collectors.toList());

        List<Factura> facturasNoDesdeCaja = todasLasFacturas.stream()
                .filter(f -> !f.isPagadoDesdeCaja() || (f.getCuadreCajaId() != null
                        && !f.getCuadreCajaId().equals(cuadreCajaId)))
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
        
        System.out.println("✅ Ingresos del cuadre: " + cuadre.get_id() + " | Encontrados: " + ingresos.size());
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
        double totalGastosDesdeCaja = resumenGastos.get("totalGastosDesdeCaja") != null ? 
            (double) resumenGastos.get("totalGastosDesdeCaja") : 0.0;
        System.out.println("🔍 VALOR DE totalGastosDesdeCaja RECUPERADO: $" + totalGastosDesdeCaja);
        
        double gastosEfectivo = totalGastosDesdeCaja;
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
        resumenFinal.put("efectivoInicial", movimientos.get("fondoInicial"));
        resumenFinal.put("gastosEfectivo", movimientos.get("gastosEfectivo"));
        resumenFinal.put("comprasEfectivo", movimientos.get("comprasEfectivo"));
        resumenFinal.put("ventasEfectivo", movimientos.get("ventasEfectivo"));
        resumenFinal.put("ingresosEfectivo", movimientos.get("ingresosEfectivo"));
        
        // Efectivo real (si está presente en el cuadre)
        Double efectivoDeclarado = cuadre.getEfectivoDeclarado();
        resumenFinal.put("efectivoReal", efectivoDeclarado);
        
        // Calcular diferencia si tenemos el efectivo real declarado
        if (efectivoDeclarado != null) {
            double diferencia = efectivoDeclarado - (Double) movimientos.get("efectivoEsperado");
            resumenFinal.put("diferencia", diferencia);
            System.out.println("Efectivo declarado: $" + efectivoDeclarado + " | Diferencia: $" + diferencia);
        } else {
            resumenFinal.put("diferencia", null);
            System.out.println("No hay efectivo declarado para calcular diferencia");
        }
        
        // Resumen de pedidos eliminados y su impacto
        Double ventasEliminadasObj = (Double) resumenVentas.get("totalVentasEliminadas");
        double totalVentasEliminadas = ventasEliminadasObj != null ? ventasEliminadasObj : 0.0;
        resumenFinal.put("ventasEliminadas", totalVentasEliminadas);
        resumenFinal.put("totalPedidosEliminados", resumenVentas.get("totalPedidosEliminados"));
        
        return resumenFinal;
    }

    /**
     * Convierte un pedido a un objeto detalle para el resumen ✅ INCLUYE descuento y propina en la
     * respuesta
     */
    private Map<String, Object> convertirPedidoADetalle(Pedido pedido) {
        // 🔍 DEBUG: Verificar datos del pedido al convertirlo
        System.out.println("\n🔍 ===== DEBUG CONVERSIÓN PEDIDO A DETALLE =====");
        System.out.println("  - ID Pedido: " + pedido.get_id());
        System.out.println("  - Mesa: " + pedido.getMesa());
        System.out.println("  - Total original: " + pedido.getTotal());
        System.out.println("  - Descuento leído: " + pedido.getDescuento());
        System.out.println("  - Propina leída: " + pedido.getPropina());
        System.out.println("  - Total pagado: " + pedido.getTotalPagado());
        System.out.println("===============================================\n");

        Map<String, Object> detalle = new HashMap<>();
        detalle.put("id", pedido.get_id());
        detalle.put("mesa", pedido.getMesa());
        detalle.put("cliente", pedido.getCliente());
        detalle.put("tipo", pedido.getTipo());
        detalle.put("fecha", pedido.getFecha());
        detalle.put("fechaPago", pedido.getFechaPago());
        detalle.put("formaPago", pedido.getFormaPago());
        detalle.put("total", pedido.getTotal());
        detalle.put("totalPagado", pedido.getTotalPagado());
        detalle.put("pagadoPor", pedido.getPagadoPor());

        // ✅ CRÍTICO: Incluir descuento y propina para el frontend
        detalle.put("descuento", pedido.getDescuento()); // Ya es primitivo, no puede ser null
        detalle.put("propina", pedido.getPropina()); // Ya es primitivo, no puede ser null

        // ✅ Calcular totales aplicando descuentos y propinas
        double totalItems = pedido.getTotal(); // Total base de items
        double descuento = pedido.getDescuento(); // Ya es primitivo, no puede ser null
        double propina = pedido.getPropina(); // Ya es primitivo, no puede ser null
        double totalConDescuento = Math.max(totalItems - descuento, 0.0);
        double totalFinal = totalConDescuento + propina;

        detalle.put("totalItems", totalItems);
        detalle.put("totalConDescuento", totalConDescuento);
        detalle.put("totalFinal", totalFinal);

        return detalle;
    }

    /**
     * Convierte un gasto a un objeto detalle para el resumen
     */
    private Map<String, Object> convertirGastoADetalle(Gasto gasto) {
        Map<String, Object> detalle = new HashMap<>();
        detalle.put("id", gasto.get_id());
        detalle.put("descripcion", gasto.getConcepto());
        detalle.put("tipoGasto", gasto.getTipoGastoNombre());
        detalle.put("monto", gasto.getMonto());
        detalle.put("formaPago", gasto.getFormaPago());
        detalle.put("fecha", gasto.getFechaGasto());
        detalle.put("pagadoDesdeCaja", gasto.isPagadoDesdeCaja());
        detalle.put("autorizado", gasto.getResponsable());
        return detalle;
    }

    /**
     * Convierte una factura a un objeto detalle para el resumen
     */
    private Map<String, Object> convertirFacturaADetalle(Factura factura) {
        Map<String, Object> detalle = new HashMap<>();
        detalle.put("id", factura.get_id());
        detalle.put("numero", factura.getNumero());
        detalle.put("proveedor", factura.getProveedorNombre());
        detalle.put("fecha", factura.getFecha());
        detalle.put("total", factura.getTotal());
        detalle.put("medioPago", factura.getMedioPago());
        detalle.put("pagadoDesdeCaja", factura.isPagadoDesdeCaja());
        detalle.put("tipoFactura", factura.getTipoFactura());
        return detalle;
    }
}