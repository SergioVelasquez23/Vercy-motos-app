package com.prog3.security.Services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.prog3.security.Models.CuadreCaja;
import com.prog3.security.Models.Pedido;
import com.prog3.security.Repositories.CuadreCajaRepository;
import com.prog3.security.Repositories.PedidoRepository;
import com.prog3.security.Repositories.FacturaRepository;
import com.prog3.security.Models.Factura;
import com.prog3.security.DTOs.CuadreCajaRequest;

@Service
public class CuadreCajaService {

    @Autowired
    private CuadreCajaRepository cuadreCajaRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private FacturaRepository facturaRepository;

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
     * Calcula detalles completos de ventas y efectivo esperado
     */
    public Map<String, Object> calcularDetallesVentas() {
        // CAMBIO: Usar siempre el inicio del día en lugar de la fecha del último cuadre
        // Esto asegura que se incluyan todos los pedidos del día actual
        LocalDateTime inicioDia = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime fechaReferencia = inicioDia;
        double fondoInicial = 0.0;

        System.out.println("NUEVO CÁLCULO: Calculando detalles de ventas desde INICIO DEL DÍA: " + fechaReferencia);

        // Buscar el cuadre actual o el último cuadre abierto para obtener el fondo inicial
        List<CuadreCaja> cuadresActivos = cuadreCajaRepository.findByFechaAperturaHoy(inicioDia)
                .stream()
                .filter(c -> !c.isCerrada())
                .toList();

        if (!cuadresActivos.isEmpty()) {
            CuadreCaja cuadreActivo = cuadresActivos.get(0); // Tomar el primero si hay varios
            fondoInicial = cuadreActivo.getFondoInicial();
            System.out.println("Fondo inicial de la caja activa: " + fondoInicial);
            System.out.println("Fecha apertura caja activa: " + cuadreActivo.getFechaApertura());
        } else {
            System.out.println("No hay cajas activas, fondo inicial es 0");
        }

        // Buscar todos los pedidos pagados desde la fecha de referencia (USAR FECHA DE PAGO)
        List<Pedido> todosPedidosPagados = pedidoRepository.findByFechaPagoGreaterThanEqualAndEstado(fechaReferencia, "pagado");
        List<Factura> facturas = facturaRepository.findByFechaBetween(fechaReferencia, LocalDateTime.now());

        System.out.println("CAMBIO: Usando fechaPago en lugar de fecha para filtrar pedidos");
        System.out.println("Fecha de referencia para filtro: " + fechaReferencia);
        System.out.println("Total de pedidos pagados desde la fecha de referencia: " + todosPedidosPagados.size());
        System.out.println("Total de facturas desde la fecha de referencia: " + facturas.size());

        // Calcular totales por forma de pago (unificando criterios con cierre de caja)
        double totalEfectivo = 0.0;
        double totalTransferencias = 0.0;
        double totalTarjetas = 0.0;
        double totalOtros = 0.0;

        // Procesar pedidos
        for (Pedido pedido : todosPedidosPagados) {
            String formaPago = pedido.getFormaPago();
            double monto = pedido.getTotalPagado();
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

        // Procesar facturas
        for (Factura factura : facturas) {
            String medioPago = factura.getMedioPago();
            double monto = factura.getTotal();
            if (medioPago == null) {
                totalOtros += monto;
            } else if ("efectivo".equalsIgnoreCase(medioPago.trim())) {
                totalEfectivo += monto;
            } else if ("transferencia".equalsIgnoreCase(medioPago.trim())) {
                totalTransferencias += monto;
            } else if ("tarjeta".equalsIgnoreCase(medioPago.trim())) {
                totalTarjetas += monto;
            } else {
                totalOtros += monto;
            }
        }

        double totalVentas = totalEfectivo + totalTransferencias + totalTarjetas + totalOtros;

        System.out.println("=== RESUMEN DE VENTAS UNIFICADO ===");
        System.out.println("Efectivo: " + totalEfectivo);
        System.out.println("Transferencias: " + totalTransferencias);
        System.out.println("Tarjetas: " + totalTarjetas);
        System.out.println("Otros: " + totalOtros);
        System.out.println("Total ventas: " + totalVentas);

        // Calcular gastos para la caja activa
        double totalGastos = 0.0;
        double totalPagosFacturas = 0.0;

        if (!cuadresActivos.isEmpty()) {
            CuadreCaja cuadreActivo = cuadresActivos.get(0);
            totalGastos = cuadreActivo.getTotalGastos();
            totalPagosFacturas = cuadreActivo.getTotalPagosFacturas();
            System.out.println("Gastos registrados en caja: " + totalGastos);
            System.out.println("Pagos de facturas: " + totalPagosFacturas);
        }

        // CORRECCIÓN: El efectivo esperado es solo las ventas en efectivo - gastos - pagos de facturas
        double efectivoEsperadoPorVentas = totalEfectivo - totalGastos - totalPagosFacturas;

        System.out.println("=== CÁLCULO EFECTIVO ESPERADO ===");
        System.out.println("Efectivo esperado por ventas: " + efectivoEsperadoPorVentas
                + " (Ventas en efectivo: " + totalEfectivo
                + " - Gastos: " + totalGastos
                + " - Pagos facturas: " + totalPagosFacturas + ")");

        System.out.println("NOTA: El fondo inicial (" + fondoInicial + ") se maneja por separado");
        System.out.println("Total que debería haber en caja: " + (fondoInicial + efectivoEsperadoPorVentas));

        // Crear mapa de respuesta
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("fondoInicial", fondoInicial);
        resultado.put("totalVentas", totalVentas);
        resultado.put("ventasEfectivo", totalEfectivo);
        resultado.put("ventasTransferencias", totalTransferencias);
        resultado.put("ventasTarjetas", totalTarjetas);
        resultado.put("ventasOtros", totalOtros);
        resultado.put("totalGastos", totalGastos);
        resultado.put("totalPagosFacturas", totalPagosFacturas);
        resultado.put("efectivoEsperadoPorVentas", efectivoEsperadoPorVentas);
        resultado.put("totalEfectivoEnCaja", fondoInicial + efectivoEsperadoPorVentas);
        resultado.put("fechaReferencia", fechaReferencia);

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
                request.getEfectivoDeclarado(),
                efectivoEsperado,
                request.getTolerancia(),
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

        // Establecer información de gastos
        cuadreCaja.setTotalGastos(request.getTotalGastos());
        cuadreCaja.setGastosDesglosados(request.getGastosDesglosados());
        cuadreCaja.setTotalPagosFacturas(request.getTotalPagosFacturas());

        // Establecer domicilios
        cuadreCaja.setTotalDomicilios(request.getTotalDomicilios());

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
        cuadre.setEfectivoDeclarado(request.getEfectivoDeclarado());
        cuadre.setObservaciones(request.getObservaciones());
        cuadre.setTolerancia(request.getTolerancia());
        cuadre.setFondoInicial(request.getFondoInicial());

        // Actualizar campos extendidos
        cuadre.setIdentificacionMaquina(request.getIdentificacionMaquina());
        cuadre.setCajeros(request.getCajeros());
        cuadre.setFondoInicialDesglosado(request.getFondoInicialDesglosado());

        // Actualizar información de ventas
        cuadre.setTotalVentas(request.getTotalVentas());
        cuadre.setVentasDesglosadas(request.getVentasDesglosadas());
        cuadre.setTotalPropinas(request.getTotalPropinas());

        // Actualizar información de gastos
        cuadre.setTotalGastos(request.getTotalGastos());
        cuadre.setGastosDesglosados(request.getGastosDesglosados());
        cuadre.setTotalPagosFacturas(request.getTotalPagosFacturas());

        // Actualizar domicilios
        cuadre.setTotalDomicilios(request.getTotalDomicilios());

        // Recalcular diferencia (efectivo declarado - efectivo esperado)
        double efectivoEsperado = calcularEfectivoEsperado();
        cuadre.setEfectivoEsperado(efectivoEsperado);
        cuadre.setDiferencia(request.getEfectivoDeclarado() - efectivoEsperado);
        cuadre.setCuadrado(Math.abs(cuadre.getDiferencia()) <= cuadre.getTolerancia());

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
}
