package com.prog3.security.Services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.prog3.security.Models.CuadreCaja;
import com.prog3.security.Models.Pedido;
import com.prog3.security.Models.Gasto;
import com.prog3.security.Repositories.CuadreCajaRepository;
import com.prog3.security.Repositories.PedidoRepository;
import com.prog3.security.Repositories.FacturaRepository;
import com.prog3.security.Repositories.GastoRepository;
import com.prog3.security.Models.Factura;
import com.prog3.security.DTOs.CuadreCajaRequest;

@Service
public class CuadreCajaService {

    /**
     * Suma un pago parcial al cuadre de caja activo según la forma de pago
     */
    public void sumarPagoACuadreActivo(double monto, String formaPago) {
        List<CuadreCaja> cuadresActivos = cuadreCajaRepository.findByCerradaFalse();
        if (cuadresActivos.isEmpty()) {
            System.out.println("❌ No hay cajas activas para sumar pago");
            return;
        }
        CuadreCaja cuadre = cuadresActivos.get(0);
        // Sumar al total de ventas
        cuadre.setTotalVentas(cuadre.getTotalVentas() + monto);
        // Sumar al desglose por forma de pago
        Map<String, Double> ventasDesglosadas = cuadre.getVentasDesglosadas();
        double actual = ventasDesglosadas.getOrDefault(formaPago, 0.0);
        ventasDesglosadas.put(formaPago, actual + monto);
        cuadre.setVentasDesglosadas(ventasDesglosadas);
        // Sumar al efectivo esperado si es efectivo
        if ("efectivo".equalsIgnoreCase(formaPago)) {
            cuadre.setEfectivoEsperado(cuadre.getEfectivoEsperado() + monto);
        }
        cuadreCajaRepository.save(cuadre);
        System.out.println("✅ Pago parcial registrado en caja: " + monto + " por " + formaPago);
    }

    /**
     * Resta un pago del cuadre de caja activo según la forma de pago
     * Se usa al eliminar pedidos ya pagados
     */
    public void restarPagoDelCuadreActivo(double monto, String formaPago) {
        List<CuadreCaja> cuadresActivos = cuadreCajaRepository.findByCerradaFalse();
        if (cuadresActivos.isEmpty()) {
            System.out.println("❌ No hay cajas activas para restar pago");
            return;
        }
        CuadreCaja cuadre = cuadresActivos.get(0);
        
        // Restar del total de ventas
        cuadre.setTotalVentas(Math.max(0, cuadre.getTotalVentas() - monto));
        
        // Restar del desglose por forma de pago
        Map<String, Double> ventasDesglosadas = cuadre.getVentasDesglosadas();
        if (ventasDesglosadas == null) {
            ventasDesglosadas = new HashMap<>();
        }
        double actual = ventasDesglosadas.getOrDefault(formaPago, 0.0);
        ventasDesglosadas.put(formaPago, Math.max(0, actual - monto));
        cuadre.setVentasDesglosadas(ventasDesglosadas);
        
        // Restar del efectivo esperado si es efectivo
        if ("efectivo".equalsIgnoreCase(formaPago)) {
            cuadre.setEfectivoEsperado(Math.max(0, cuadre.getEfectivoEsperado() - monto));
        }
        
        cuadreCajaRepository.save(cuadre);
        System.out.println("✅ Pago restado de caja: " + monto + " por " + formaPago);
    }

    /**
     * Resta múltiples pagos del cuadre activo (para pagos mixtos)
     */
    public void restarPagosDelCuadreActivo(List<Pedido.PagoParcial> pagos) {
        if (pagos == null || pagos.isEmpty()) {
            return;
        }
        
        System.out.println("🔄 Restando pagos mixtos del cuadre activo...");
        for (Pedido.PagoParcial pago : pagos) {
            restarPagoDelCuadreActivo(pago.getMonto(), pago.getFormaPago());
        }
    }

    @Autowired
    private CuadreCajaRepository cuadreCajaRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private GastoRepository gastoRepository;

    @Autowired
    private PedidoCalculosService pedidoCalculosService;

    /**
     * Calcula el efectivo esperado en base al fondo inicial más los pedidos
     * pagados en efectivo desde el último cuadre de caja o desde el inicio del
     * día.
     */
    public double calcularEfectivoEsperado() {
        Map<String, Object> detalles = calcularDetallesVentas();
        return (double) detalles.get("efectivoEsperadoPorVentas");
    }

    /**
     * Calcula detalles completos de ventas y efectivo esperado ✅ CORREGIDO: -
     * Solo cuenta pedidos pagados de la caja específica (no documentos
     * duplicados) - No incluye facturas en ventas (facturas = gastos/compras) -
     * Cada caja maneja solo sus pedidos específicos
     */
    public Map<String, Object> calcularDetallesVentas() {
        LocalDateTime inicioDia = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        double fondoInicial = 0.0;

        System.out.println("✅ CÁLCULO ESPECÍFICO POR CAJA: Calculando detalles de ventas");

        // Buscar el cuadre activo específico (no todos los del día)
        List<CuadreCaja> cuadresActivos = cuadreCajaRepository.findByFechaAperturaHoy(inicioDia)
                .stream()
                .filter(c -> !c.isCerrada())
                .toList();

        if (cuadresActivos.isEmpty()) {
            System.out.println("❌ No hay cajas activas");
            return crearResultadoVacio();
        }

        CuadreCaja cuadreActivo = cuadresActivos.get(0);
        fondoInicial = cuadreActivo.getFondoInicial();
        String cuadreCajaId = cuadreActivo.get_id();

        System.out.println("📦 Procesando caja específica: " + cuadreActivo.getNombre() + " (ID: " + cuadreCajaId + ")");
        System.out.println("💰 Fondo inicial: $" + fondoInicial);

        // ✅ SOLO pedidos pagados asignados a esta caja específica
        List<Pedido> pedidosPagados = pedidoRepository.findByCuadreCajaIdAndEstado(cuadreCajaId, "pagado");

        System.out.println("✅ PEDIDOS PAGADOS DE ESTA CAJA: " + pedidosPagados.size());

        // ✅ DEBUG: Mostrar detalles de cada pedido para verificar
        for (Pedido pedido : pedidosPagados) {
            System.out.println("  📦 Pedido ID: " + pedido.get_id()
                    + " | Estado: " + pedido.getEstado()
                    + " | Total: $" + pedido.getTotalPagado()
                    + " | Forma pago: " + pedido.getFormaPago()
                    + " | Caja: " + pedido.getCuadreCajaId());
        }

        // Calcular totales por forma de pago (SOLO de pedidos pagados)
        double totalEfectivo = 0.0;
        double totalTransferencias = 0.0;
        double totalTarjetas = 0.0;
        double totalOtros = 0.0;

        // ✅ PROCESAR PEDIDOS PAGADOS CON SOPORTE PARA PAGOS MIXTOS
        for (Pedido pedido : pedidosPagados) {
            // Si tiene pagos parciales, procesarlos individualmente
            if (pedido.getPagosParciales() != null && !pedido.getPagosParciales().isEmpty()) {
                System.out.println("🔄 Procesando pagos mixtos para pedido: " + pedido.get_id());
                for (Pedido.PagoParcial pago : pedido.getPagosParciales()) {
                    String formaPago = pago.getFormaPago();
                    double monto = pago.getMonto();
                    
                    System.out.println("  💰 Pago parcial: $" + monto + " por " + formaPago);
                    
                    if (formaPago == null) {
                        totalOtros += monto;
                    } else if ("efectivo".equalsIgnoreCase(formaPago.trim())) {
                        totalEfectivo += monto;
                    } else if ("transferencia".equalsIgnoreCase(formaPago.trim())) {
                        totalTransferencias += monto;
                    } else if ("tarjeta".equalsIgnoreCase(formaPago.trim())) {
                        totalTarjetas += monto;
                    } else {
                        totalOtros += monto;
                    }
                }
            } else {
                // Fallback: usar forma de pago principal (para compatibilidad con pedidos antiguos)
                String formaPago = pedido.getFormaPago();
                double monto = pedido.getTotalPagado();
                
                System.out.println("📦 Pago tradicional: $" + monto + " por " + formaPago);

                if (formaPago == null) {
                    totalOtros += monto;
                } else if ("efectivo".equalsIgnoreCase(formaPago.trim())) {
                    totalEfectivo += monto;
                } else if ("transferencia".equalsIgnoreCase(formaPago.trim())) {
                    totalTransferencias += monto;
                } else if ("tarjeta".equalsIgnoreCase(formaPago.trim())) {
                    totalTarjetas += monto;
                } else {
                    totalOtros += monto;
                }
            }
        }

        double totalVentas = totalEfectivo + totalTransferencias + totalTarjetas + totalOtros;

        System.out.println("=== RESUMEN DE VENTAS DE ESTA CAJA ===");
        System.out.println("Efectivo: " + totalEfectivo);
        System.out.println("Transferencias: " + totalTransferencias);
        System.out.println("Tarjetas: " + totalTarjetas);
        System.out.println("Otros: " + totalOtros);
        System.out.println("Total ventas: " + totalVentas);

        // ✅ CALCULAR TOTALES DE IMPUESTOS, DESCUENTOS Y RETENCIONES
        Map<String, Object> totalesCalculados = pedidoCalculosService.calcularTotalesLista(pedidosPagados);

        double subtotalVentas = (double) totalesCalculados.get("subtotal");
        double totalImpuestosVentas = (double) totalesCalculados.get("totalImpuestos");
        double totalDescuentosVentas = (double) totalesCalculados.get("totalDescuentos");
        double totalRetencionesVentas = (double) totalesCalculados.get("totalRetenciones");
        double totalPropinasPedidos = (double) totalesCalculados.get("totalPropinas");

        System.out.println("📊 DETALLE DE VENTAS CON IMPUESTOS:");
        System.out.println("  Subtotal (sin impuestos): $" + subtotalVentas);
        System.out.println("  Total descuentos: $" + totalDescuentosVentas);
        System.out.println("  Total impuestos: $" + totalImpuestosVentas);
        System.out.println("  Total retenciones: $" + totalRetencionesVentas);
        System.out.println("  Total propinas: $" + totalPropinasPedidos);

        // Filtrar por período y por cuadreCajaId
        LocalDateTime fechaInicio = cuadreActivo.getFechaApertura();
        LocalDateTime fechaFin = cuadreActivo.getFechaCierre() != null ? cuadreActivo.getFechaCierre() : LocalDateTime.now();

        System.out.println("📅 Período de esta caja: " + fechaInicio + " hasta " + fechaFin);

        // Gastos directos del período de esta caja y de este cuadre
        List<Gasto> gastos = gastoRepository.findByCuadreCajaIdAndFechaGastoBetween(cuadreActivo.get_id(), fechaInicio, fechaFin);
        double totalGastosDirectos = gastos.stream().mapToDouble(Gasto::getMonto).sum();

        // Solo gastos que salen de caja
        List<Gasto> gastosDesdeCaja = gastos.stream().filter(Gasto::isPagadoDesdeCaja).collect(Collectors.toList());
        double totalGastosDesdeCaja = gastosDesdeCaja.stream().mapToDouble(Gasto::getMonto).sum();

        // Facturas pagadas desde caja del período de esta caja (también son gastos)
        List<Factura> facturasPagadasDesdeCaja = facturaRepository.findByFechaBetween(fechaInicio, fechaFin)
                .stream()
                .filter(f -> "compra".equals(f.getTipoFactura()) && f.isPagadoDesdeCaja() && f.getCuadreCajaId() != null && f.getCuadreCajaId().equals(cuadreActivo.get_id()))
                .collect(Collectors.toList());
        double totalFacturasDesdeCaja = facturasPagadasDesdeCaja.stream().mapToDouble(Factura::getTotal).sum();

        // ✅ Total de gastos reales (gastos + facturas pagadas desde caja)
        double totalGastosReales = totalGastosDesdeCaja + totalFacturasDesdeCaja;

        System.out.println("✅ GASTOS DEL PERÍODO DE ESTA CAJA:");
        System.out.println("  Gastos directos desde caja: $" + totalGastosDesdeCaja + " (" + gastosDesdeCaja.size() + " registros)");
        System.out.println("  Facturas pagadas desde caja: $" + totalFacturasDesdeCaja + " (" + facturasPagadasDesdeCaja.size() + " registros)");
        System.out.println("  Total gastos reales: $" + totalGastosReales);

        // ✅ DEBUG: Vamos a revisar cada gasto individualmente
        System.out.println("=== DEBUG GASTOS INDIVIDUALES ===");
        System.out.println("Total de gastos encontrados: " + gastos.size());
        for (Gasto g : gastos) {
            System.out.println("Gasto ID: " + g.get_id()
                    + " | Monto: $" + g.getMonto()
                    + " | FormaPago: " + g.getFormaPago()
                    + " | PagadoDesdeCaja: " + g.isPagadoDesdeCaja()
                    + " | Concepto: " + g.getConcepto());
        }

        // ...existing code...
        double facturasEfectivo = facturasPagadasDesdeCaja.stream()
                .filter(f -> "efectivo".equalsIgnoreCase(f.getMedioPago()))
                .mapToDouble(Factura::getTotal)
                .sum();
        double totalSalidasEfectivo = totalGastosDesdeCaja + facturasEfectivo;

        // ✅ CORRECCIÓN: El efectivo esperado considera solo movimientos de efectivo de esta caja
        double efectivoEsperadoPorVentas = totalEfectivo - totalSalidasEfectivo;

        System.out.println("=== CÁLCULO EFECTIVO ESPERADO CORREGIDO ===");
        System.out.println("Efectivo esperado por ventas: " + efectivoEsperadoPorVentas
                + " (Ventas en efectivo: " + totalEfectivo
                + " - Gastos desde caja: " + totalGastosDesdeCaja
                + " - Facturas efectivo: " + facturasEfectivo + ")");
        System.out.println("NOTA: Gastos no pagados desde caja NO afectan el efectivo esperado");
        System.out.println("NOTA: El fondo inicial (" + fondoInicial + ") se maneja por separado");
        System.out.println("Total que debería haber en caja: " + (fondoInicial + efectivoEsperadoPorVentas));

        // ✅ CONTAR PEDIDOS POR FORMA DE PAGO CON SOPORTE PARA PAGOS MIXTOS
        int cantidadEfectivo = 0;
        int cantidadTransferencias = 0;
        int cantidadTarjetas = 0;
        int cantidadOtros = 0;

        for (Pedido pedido : pedidosPagados) {
            // Si tiene pagos parciales, contar según los tipos de pago usados
            if (pedido.getPagosParciales() != null && !pedido.getPagosParciales().isEmpty()) {
                boolean tieneEfectivo = false;
                boolean tieneTransferencia = false;
                boolean tieneTarjeta = false;
                boolean tieneOtros = false;
                
                for (Pedido.PagoParcial pago : pedido.getPagosParciales()) {
                    String formaPago = pago.getFormaPago();
                    
                    if (formaPago == null) {
                        tieneOtros = true;
                    } else if ("efectivo".equalsIgnoreCase(formaPago.trim())) {
                        tieneEfectivo = true;
                    } else if ("transferencia".equalsIgnoreCase(formaPago.trim())) {
                        tieneTransferencia = true;
                    } else if ("tarjeta".equalsIgnoreCase(formaPago.trim())) {
                        tieneTarjeta = true;
                    } else {
                        tieneOtros = true;
                    }
                }
                
                // Contar el pedido en cada forma de pago que se usó
                if (tieneEfectivo) cantidadEfectivo++;
                if (tieneTransferencia) cantidadTransferencias++;
                if (tieneTarjeta) cantidadTarjetas++;
                if (tieneOtros) cantidadOtros++;
                
            } else {
                // Fallback: contar según forma de pago principal
                String formaPago = pedido.getFormaPago();

                if (formaPago == null) {
                    cantidadOtros++;
                } else if ("efectivo".equalsIgnoreCase(formaPago.trim())) {
                    cantidadEfectivo++;
                } else if ("transferencia".equalsIgnoreCase(formaPago.trim())) {
                    cantidadTransferencias++;
                } else if ("tarjeta".equalsIgnoreCase(formaPago.trim())) {
                    cantidadTarjetas++;
                } else {
                    cantidadOtros++;
                }
            }
        }

        int totalPedidos = pedidosPagados.size();

        System.out.println("=== CANTIDADES DE PEDIDOS ===");
        System.out.println("Total pedidos: " + totalPedidos);
        System.out.println("Efectivo: " + cantidadEfectivo + " pedidos");
        System.out.println("Transferencias: " + cantidadTransferencias + " pedidos");
        System.out.println("Tarjetas: " + cantidadTarjetas + " pedidos");
        System.out.println("Otros: " + cantidadOtros + " pedidos");

        // Calcular gastos separados por si salen de caja o no
        double gastosNoDesdeCaja = gastos.stream()
                .filter(g -> !g.isPagadoDesdeCaja())
                .mapToDouble(Gasto::getMonto)
                .sum();
        double gastosTotalDesdeCaja = gastos.stream()
                .filter(g -> g.isPagadoDesdeCaja())
                .mapToDouble(Gasto::getMonto)
                .sum();

        System.out.println("💰 DESGLOSE DE GASTOS POR ORIGEN:");
        System.out.println("  Gastos DESDE caja: $" + gastosTotalDesdeCaja + " (afectan efectivo)");
        System.out.println("  Gastos NO desde caja: $" + gastosNoDesdeCaja + " (NO afectan efectivo)");
        System.out.println("  Total gastos desde caja: $" + totalGastosDesdeCaja);

        // Crear mapa de respuesta con valores corregidos
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("fondoInicial", fondoInicial);
        resultado.put("totalVentas", totalVentas);
        resultado.put("ventasEfectivo", totalEfectivo);
        resultado.put("ventasTransferencias", totalTransferencias);
        resultado.put("ventasTarjetas", totalTarjetas);
        resultado.put("ventasOtros", totalOtros);
        // ✅ AGREGAR: Cantidades de pedidos
        resultado.put("totalPedidos", totalPedidos);
        resultado.put("cantidadEfectivo", cantidadEfectivo);
        resultado.put("cantidadTransferencias", cantidadTransferencias);
        resultado.put("cantidadTarjetas", cantidadTarjetas);
        resultado.put("cantidadOtros", cantidadOtros);
        // totalGastos ahora solo suma los gastos que salen de caja y facturas pagadas desde caja
        resultado.put("totalGastos", totalGastosDesdeCaja + totalFacturasDesdeCaja);
        resultado.put("totalGastosDirectos", totalGastosDirectos);
        resultado.put("totalFacturasDesdeCaja", totalFacturasDesdeCaja);
        // ✅ NUEVO: Separar gastos según si salen de caja o no
        resultado.put("gastosDesdeCaja", gastosTotalDesdeCaja);
        resultado.put("gastosNoDesdeCaja", gastosNoDesdeCaja);
        resultado.put("gastosEfectivoDesdeCaja", totalGastosDesdeCaja);
        resultado.put("efectivoEsperadoPorVentas", efectivoEsperadoPorVentas);
        resultado.put("totalEfectivoEnCaja", fondoInicial + efectivoEsperadoPorVentas);
        resultado.put("fechaReferencia", inicioDia);

        // ✅ NUEVOS: Campos de impuestos, descuentos y retenciones
        resultado.put("subtotalVentas", subtotalVentas);
        resultado.put("totalImpuestosVentas", totalImpuestosVentas);
        resultado.put("totalDescuentosVentas", totalDescuentosVentas);
        resultado.put("totalRetencionesVentas", totalRetencionesVentas);
        resultado.put("totalPropinasPedidos", totalPropinasPedidos);

        return resultado;
    }

    /**
     * Crea un resultado vacío cuando no hay cajas activas
     */
    private Map<String, Object> crearResultadoVacio() {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("fondoInicial", 0.0);
        resultado.put("totalVentas", 0.0);
        resultado.put("ventasEfectivo", 0.0);
        resultado.put("ventasTransferencias", 0.0);
        resultado.put("ventasTarjetas", 0.0);
        resultado.put("ventasOtros", 0.0);
        // ✅ AGREGAR: Cantidades de pedidos vacías
        resultado.put("totalPedidos", 0);
        resultado.put("cantidadEfectivo", 0);
        resultado.put("cantidadTransferencias", 0);
        resultado.put("cantidadTarjetas", 0);
        resultado.put("cantidadOtros", 0);
        resultado.put("totalGastos", 0.0);
        resultado.put("totalGastosDirectos", 0.0);
        resultado.put("totalFacturasDesdeCaja", 0.0);
        // ✅ NUEVO: Campos para gastos desde caja
        resultado.put("gastosDesdeCaja", 0.0);
        resultado.put("gastosNoDesdeCaja", 0.0);
        resultado.put("gastosEfectivoDesdeCaja", 0.0);
        resultado.put("efectivoEsperadoPorVentas", 0.0);
        resultado.put("totalEfectivoEnCaja", 0.0);
        resultado.put("fechaReferencia", LocalDateTime.now());
        // ✅ NUEVOS: Campos de impuestos vacíos
        resultado.put("subtotalVentas", 0.0);
        resultado.put("totalImpuestosVentas", 0.0);
        resultado.put("totalDescuentosVentas", 0.0);
        resultado.put("totalRetencionesVentas", 0.0);
        resultado.put("totalPropinasPedidos", 0.0);
        return resultado;
    }

    /**
     * Obtiene la fecha del último cuadre de caja aprobado o el inicio del día
     * actual
     */
    private LocalDateTime obtenerFechaUltimoCuadreOInicioDia() {
        LocalDateTime inicioDia = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        System.out.println("Inicio del día para referencia: " + inicioDia);

        // Buscar el último cuadre aprobado
        List<CuadreCaja> cuadresHoy = cuadreCajaRepository.findByFechaAperturaHoy(inicioDia);
        System.out.println("Encontrados " + (cuadresHoy != null ? cuadresHoy.size() : 0) + " cuadres para hoy");

        if (cuadresHoy != null && !cuadresHoy.isEmpty()) {
            // Encontrar el más reciente
            LocalDateTime fechaUltimoCuadre = inicioDia;
            CuadreCaja ultimoCuadre = null;

            for (CuadreCaja cuadre : cuadresHoy) {
                System.out.println("Cuadre ID: " + cuadre.get_id() + ", fecha apertura: " + cuadre.getFechaApertura());
                if (cuadre.getFechaApertura().isAfter(fechaUltimoCuadre)) {
                    fechaUltimoCuadre = cuadre.getFechaApertura();
                    ultimoCuadre = cuadre;
                }
            }

            if (ultimoCuadre != null) {
                System.out.println("Último cuadre encontrado - ID: " + ultimoCuadre.get_id()
                        + ", Fecha: " + fechaUltimoCuadre
                        + ", Fondo: " + ultimoCuadre.getFondoInicial());
            }

            return fechaUltimoCuadre;
        }

        // Si no hay cuadres hoy, retornar inicio del día
        System.out.println("No hay cuadres hoy, usando inicio del día: " + inicioDia);
        return inicioDia;
    }

    /**
     * Crea un nuevo cuadre de caja
     */
    public CuadreCaja crearCuadreCaja(CuadreCajaRequest request) {
        // Calcular el efectivo esperado
        double efectivoEsperado = calcularEfectivoEsperado();
        System.out.println("Creando cuadre con efectivo esperado: " + efectivoEsperado);

        // Verificar y ajustar el fondo inicial solo si es negativo o cero
        if (request.getFondoInicial() <= 0) {
            System.out.println("⚠️ Advertencia: Fondo inicial recibido es <= 0, ajustando a valor predeterminado.");
            request.setFondoInicial(500000.0); // Valor predeterminado solo si no se proporciona uno válido
        } else {
            System.out.println("Usando el fondo inicial proporcionado por el usuario: " + request.getFondoInicial());
        }

        System.out.println("Fondo inicial para nuevo cuadre: " + request.getFondoInicial());

        // Crear el cuadre de caja
        CuadreCaja cuadreCaja = new CuadreCaja(
                request.getNombre(),
                request.getResponsable(),
                request.getFondoInicial(),
                efectivoEsperado,
                request.getObservaciones()
        );

        // Establecer los campos extendidos
        cuadreCaja.setIdentificacionMaquina(request.getIdentificacionMaquina());
        cuadreCaja.setCajeros(request.getCajeros());

        // Verificar que el fondo inicial desglosado contenga datos
        if (request.getFondoInicialDesglosado() == null || request.getFondoInicialDesglosado().isEmpty()) {
            Map<String, Double> desglose = new HashMap<>();
            desglose.put("Efectivo", request.getFondoInicial());
            cuadreCaja.setFondoInicialDesglosado(desglose);
            System.out.println("Generado automáticamente fondo inicial desglosado en servicio: " + desglose);
        } else {
            cuadreCaja.setFondoInicialDesglosado(request.getFondoInicialDesglosado());
            System.out.println("Usando fondo inicial desglosado proporcionado: " + request.getFondoInicialDesglosado());
        }

        // Establecer información de ventas
        cuadreCaja.setTotalVentas(request.getTotalVentas());
        cuadreCaja.setVentasDesglosadas(request.getVentasDesglosadas());
        cuadreCaja.setTotalPropinas(request.getTotalPropinas());

        // ✅ NUEVO: Establecer información de impuestos y descuentos
        Map<String, Object> detallesVentas = calcularDetallesVentas();
        cuadreCaja.setSubtotalVentas((double) detallesVentas.getOrDefault("subtotalVentas", 0.0));
        cuadreCaja.setTotalImpuestosVentas((double) detallesVentas.getOrDefault("totalImpuestosVentas", 0.0));
        cuadreCaja.setTotalDescuentosVentas((double) detallesVentas.getOrDefault("totalDescuentosVentas", 0.0));
        cuadreCaja.setTotalRetencionesVentas((double) detallesVentas.getOrDefault("totalRetencionesVentas", 0.0));

        // Establecer información de gastos
        cuadreCaja.setTotalGastos(request.getTotalGastos());
        cuadreCaja.setGastosDesglosados(request.getGastosDesglosados());
        cuadreCaja.setTotalPagosFacturas(request.getTotalPagosFacturas());

        // Establecer domicilios
        // Eliminado: cuadreCaja.setTotalDomicilios(request.getTotalDomicilios());
        // Por defecto, el estado es "pendiente" (configurado en el constructor)
        cuadreCaja.setEstado("pendiente");

        // Si se solicita cerrar la caja
        if (request.isCerrarCaja()) {
            cuadreCaja.setCerrada(true);
            cuadreCaja.setFechaCierre(LocalDateTime.now());
            cuadreCaja.setEstado("cerrada"); // Cambiar estado a "cerrada"
            System.out.println("Creando caja cerrada con ID: " + cuadreCaja.get_id());
            System.out.println("Fecha de cierre: " + cuadreCaja.getFechaCierre());
            System.out.println("Estado: " + cuadreCaja.getEstado());
            
            // Limpiar cache al cerrar la caja
            limpiarCacheAlCerrarCaja();
        }

        // Guardar en la base de datos
        return cuadreCajaRepository.save(cuadreCaja);
    }

    /**
     * Obtiene todos los cuadres de caja
     */
    public List<CuadreCaja> obtenerTodosCuadres() {
        return cuadreCajaRepository.findAll();
    }

    /**
     * Obtiene un cuadre por ID
     */
    public CuadreCaja obtenerCuadrePorId(String id) {
        return cuadreCajaRepository.findById(id).orElse(null);
    }

    /**
     * Obtiene cuadres por responsable
     */
    public List<CuadreCaja> obtenerCuadresPorResponsable(String responsable) {
        return cuadreCajaRepository.findByResponsable(responsable);
    }

    /**
     * Obtiene cuadres por estado
     */
    public List<CuadreCaja> obtenerCuadresPorEstado(String estado) {
        return cuadreCajaRepository.findByEstado(estado);
    }

    /**
     * Obtiene cuadres por rango de fechas de apertura
     */
    public List<CuadreCaja> obtenerCuadresPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return cuadreCajaRepository.findByFechaAperturaBetween(fechaInicio, fechaFin);
    }

    /**
     * Obtiene cuadres de hoy
     */
    public List<CuadreCaja> obtenerCuadresHoy() {
        LocalDateTime inicioDia = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        return cuadreCajaRepository.findByFechaAperturaHoy(inicioDia);
    }

    /**
     * Aprueba un cuadre de caja
     */
    public CuadreCaja aprobarCuadre(String id, String aprobador) {
        CuadreCaja cuadre = cuadreCajaRepository.findById(id).orElse(null);
        if (cuadre == null) {
            return null;
        }

        cuadre.setEstado("aprobado");
        cuadre.setAprobadoPor(aprobador);
        cuadre.setFechaAprobacion(LocalDateTime.now());

        return cuadreCajaRepository.save(cuadre);
    }

    /**
     * Rechaza un cuadre de caja
     */
    public CuadreCaja rechazarCuadre(String id, String aprobador, String observacion) {
        CuadreCaja cuadre = cuadreCajaRepository.findById(id).orElse(null);
        if (cuadre == null) {
            return null;
        }

        cuadre.setEstado("rechazado");
        cuadre.setAprobadoPor(aprobador);
        cuadre.setFechaAprobacion(LocalDateTime.now());

        if (observacion != null && !observacion.isEmpty()) {
            String observacionActual = cuadre.getObservaciones();
            cuadre.setObservaciones(observacionActual + " | RECHAZADO: " + observacion);
        }

        return cuadreCajaRepository.save(cuadre);
    }

    /**
     * Actualiza un cuadre de caja existente
     */
    public CuadreCaja actualizarCuadreCaja(String id, CuadreCajaRequest request) {
        CuadreCaja cuadre = cuadreCajaRepository.findById(id).orElse(null);
        if (cuadre == null) {
            return null;
        }

        // Actualizar los campos básicos
        cuadre.setNombre(request.getNombre());
        cuadre.setResponsable(request.getResponsable());
        cuadre.setObservaciones(request.getObservaciones());
        cuadre.setFondoInicial(request.getFondoInicial());

        // Actualizar campos extendidos
        cuadre.setIdentificacionMaquina(request.getIdentificacionMaquina());
        cuadre.setCajeros(request.getCajeros());
        cuadre.setFondoInicialDesglosado(request.getFondoInicialDesglosado());

        // Actualizar información de ventas
        cuadre.setTotalVentas(request.getTotalVentas());
        cuadre.setVentasDesglosadas(request.getVentasDesglosadas());
        cuadre.setTotalPropinas(request.getTotalPropinas());

        // ✅ NUEVO: Actualizar información de impuestos y descuentos
        Map<String, Object> detallesVentas = calcularDetallesVentas();
        cuadre.setSubtotalVentas((double) detallesVentas.getOrDefault("subtotalVentas", 0.0));
        cuadre.setTotalImpuestosVentas((double) detallesVentas.getOrDefault("totalImpuestosVentas", 0.0));
        cuadre.setTotalDescuentosVentas((double) detallesVentas.getOrDefault("totalDescuentosVentas", 0.0));
        cuadre.setTotalRetencionesVentas((double) detallesVentas.getOrDefault("totalRetencionesVentas", 0.0));

        // Actualizar información de gastos
        cuadre.setTotalGastos(request.getTotalGastos());
        cuadre.setGastosDesglosados(request.getGastosDesglosados());
        cuadre.setTotalPagosFacturas(request.getTotalPagosFacturas());

        // Actualizar domicilios
        // Eliminado: cuadre.setTotalDomicilios(request.getTotalDomicilios());
        // Recalcular diferencia (efectivo declarado - efectivo esperado)
        double efectivoEsperado = calcularEfectivoEsperado();
        cuadre.setEfectivoEsperado(efectivoEsperado);
        cuadre.setDiferencia(0);
        cuadre.setCuadrado(false);

        // Si se solicita cerrar la caja y no está cerrada todavía
        if (request.isCerrarCaja() && !cuadre.isCerrada()) {
            cuadre.setCerrada(true);
            cuadre.setFechaCierre(LocalDateTime.now());
            cuadre.setEstado("cerrada"); // Actualizar el estado a "cerrada"
            System.out.println("Cerrando caja con ID: " + cuadre.get_id());
            System.out.println("Nueva fecha de cierre: " + cuadre.getFechaCierre());
            System.out.println("Nuevo estado: " + cuadre.getEstado());
        } else if (request.isCerrarCaja()) {
            System.out.println("Ya estaba cerrada: " + cuadre.get_id());
        } else {
            System.out.println("No se solicitó cerrar la caja: cerrarCaja=" + request.isCerrarCaja());
        }

        return cuadreCajaRepository.save(cuadre);
    }

    /**
     * Elimina un cuadre de caja
     */
    public boolean eliminarCuadre(String id) {
        try {
            CuadreCaja cuadre = cuadreCajaRepository.findById(id).orElse(null);
            if (cuadre == null) {
                return false;
            }

            // Solo permitir eliminar cuadres pendientes
            if (!"pendiente".equals(cuadre.getEstado())) {
                return false;
            }

            cuadreCajaRepository.delete(cuadre);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtiene el cuadre de caja activo (abierto) del día actual
     *
     * @return CuadreCaja activo o null si no hay ninguno abierto
     */
    public CuadreCaja obtenerCuadreActivo() {
        LocalDateTime inicioDia = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);

        List<CuadreCaja> cuadresActivos = cuadreCajaRepository.findByFechaAperturaHoy(inicioDia)
                .stream()
                .filter(c -> !c.isCerrada() && "abierto".equals(c.getEstado()))
                .toList();

        if (!cuadresActivos.isEmpty()) {
            return cuadresActivos.get(0); // Retornar el primer cuadre activo
        }

        return null; // No hay cuadre activo
    }

    /**
     * Asigna un pedido al cuadre de caja activo
     *
     * @param pedidoId ID del pedido a asignar
     * @return true si se asignó correctamente, false si no hay cuadre activo
     */
    public boolean asignarPedidoACuadreActivo(String pedidoId) {
        CuadreCaja cuadreActivo = obtenerCuadreActivo();

        if (cuadreActivo == null) {
            System.out.println("⚠️ No hay cuadre de caja activo para asignar el pedido: " + pedidoId);
            return false;
        }

        Pedido pedido = pedidoRepository.findById(pedidoId).orElse(null);
        if (pedido != null && "pagado".equals(pedido.getEstado())) {
            pedido.setCuadreCajaId(cuadreActivo.get_id());
            pedidoRepository.save(pedido);
            System.out.println("✅ Pedido " + pedidoId + " asignado al cuadre " + cuadreActivo.get_id());
            return true;
        }

        return false;
    }

    /**
     * Migra automáticamente pedidos pagados sin cuadre a un cuadre específico
     * basándose en las fechas de apertura y cierre del cuadre
     */
    public int migrarPedidosAutomaticamente(String cuadreCajaId) {
        CuadreCaja cuadre = cuadreCajaRepository.findById(cuadreCajaId).orElse(null);
        if (cuadre == null) {
            return 0;
        }

        LocalDateTime fechaInicio = cuadre.getFechaApertura();
        LocalDateTime fechaFin = cuadre.getFechaCierre() != null ? cuadre.getFechaCierre() : LocalDateTime.now();

        // Obtener pedidos pagados sin cuadre en el rango de fechas del cuadre
        List<Pedido> pedidosSinCuadre = pedidoRepository.findPedidosPagadosSinCuadreEnRango(fechaInicio, fechaFin);

        int migrados = 0;
        for (Pedido pedido : pedidosSinCuadre) {
            pedido.setCuadreCajaId(cuadreCajaId);
            pedidoRepository.save(pedido);
            migrados++;
        }

        if (migrados > 0) {
            System.out.println("📋 Migración automática: " + migrados + " pedidos asignados al cuadre " + cuadreCajaId);
        }

        return migrados;
    }

    /**
     * Limpia el cache cuando se cierra la caja
     * Incluye cache de caja y pedidos
     */
    private void limpiarCacheAlCerrarCaja() {
        try {
            System.out.println("🧹 Limpiando cache al cerrar caja...");
            
            // Si tienes un servicio de cache específico, úsalo aquí
            // Por ejemplo: cacheService.clearAll();
            
            // También puedes limpiar caches específicos
            // cacheService.evict("pedidos");
            // cacheService.evict("cuadre-caja");
            
            // Si usas Spring Cache, puedes usar CacheManager para limpiar
            // cacheManager.getCacheNames().forEach(cacheName -> {
            //     Cache cache = cacheManager.getCache(cacheName);
            //     if (cache != null) cache.clear();
            // });
            
            System.out.println("✅ Cache limpiado exitosamente");
            
        } catch (Exception e) {
            System.err.println("❌ Error al limpiar cache: " + e.getMessage());
        }
    }
}
