package com.prog3.security.Services;

import com.prog3.security.Models.CierreCaja;
import com.prog3.security.Models.Factura;
import com.prog3.security.Models.Pedido;
import com.prog3.security.Models.Gasto;
import com.prog3.security.Repositories.CierreCajaRepository;
import com.prog3.security.Repositories.FacturaRepository;
import com.prog3.security.Repositories.PedidoRepository;
import com.prog3.security.Repositories.GastoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CierreCajaService {

    @Autowired
    private CierreCajaRepository cierreCajaRepository;

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private GastoRepository gastoRepository;

    public CierreCaja generarCierreCaja(LocalDateTime fechaInicio, LocalDateTime fechaFin,
            String responsable, Map<String, Double> montosIniciales) {

        System.out.println("=== Generando Cierre de Caja ===");
        System.out.println("Período: " + fechaInicio + " a " + fechaFin);
        System.out.println("Responsable: " + responsable);

        // Crear el objeto cierre
        CierreCaja cierre = new CierreCaja(fechaInicio, fechaFin, responsable);

        // Establecer montos iniciales
        cierre.setEfectivoInicial(montosIniciales.getOrDefault("efectivo", 0.0));
        cierre.setTransferenciasIniciales(montosIniciales.getOrDefault("transferencias", 0.0));
        cierre.setTotalInicial(cierre.getEfectivoInicial() + cierre.getTransferenciasIniciales());

        // === CALCULAR VENTAS ===
                List<Factura> facturas = facturaRepository.findByFechaBetween(fechaInicio, fechaFin);
                // Suponiendo que el cuadreCajaId se puede obtener aquí, si no, pásalo como parámetro
                String cuadreCajaId = null;
                if (montosIniciales.containsKey("cuadreCajaId")) {
                        cuadreCajaId = String.valueOf(montosIniciales.get("cuadreCajaId"));
                }
                List<Pedido> pedidosPagados;
                if (cuadreCajaId != null) {
                        pedidosPagados = pedidoRepository.findByCuadreCajaIdAndEstado(cuadreCajaId, "pagado");
                } else {
                        pedidosPagados = pedidoRepository.findByFechaBetween(fechaInicio, fechaFin)
                                .stream()
                                .filter(p -> "pagado".equals(p.getEstado()) || "completado".equals(p.getEstado()))
                                .collect(Collectors.toList());
                }

        System.out.println("Facturas encontradas: " + facturas.size());
        System.out.println("Pedidos pagados encontrados: " + pedidosPagados.size());

        // Ventas por método de pago
        Map<String, Double> ventasPorMetodo = new HashMap<>();

        // Procesar facturas
        double ventasEfectivoFacturas = facturas.stream()
                .filter(f -> f.getMedioPago() != null && f.getMedioPago().equalsIgnoreCase("Efectivo"))
                .mapToDouble(Factura::getTotal).sum();
        double ventasTransferenciasFacturas = facturas.stream()
                .filter(f -> f.getMedioPago() != null && f.getMedioPago().equalsIgnoreCase("Transferencia"))
                .mapToDouble(Factura::getTotal).sum();
        double ventasTarjetasFacturas = facturas.stream()
                .filter(f -> f.getMedioPago() != null && f.getMedioPago().equalsIgnoreCase("Tarjeta"))
                .mapToDouble(Factura::getTotal).sum();

        // Procesar pedidos
        double ventasEfectivoPedidos = pedidosPagados.stream()
                .filter(p -> p.getFormaPago() != null && p.getFormaPago().equalsIgnoreCase("Efectivo"))
                .mapToDouble(Pedido::getTotalPagado).sum();
        double ventasTransferenciasPedidos = pedidosPagados.stream()
                .filter(p -> p.getFormaPago() != null && p.getFormaPago().equalsIgnoreCase("Transferencia"))
                .mapToDouble(Pedido::getTotalPagado).sum();
        double ventasTarjetasPedidos = pedidosPagados.stream()
                .filter(p -> p.getFormaPago() != null && p.getFormaPago().equalsIgnoreCase("Tarjeta"))
                .mapToDouble(Pedido::getTotalPagado).sum();

        // Totales por método
        cierre.setVentasEfectivo(ventasEfectivoFacturas + ventasEfectivoPedidos);
        cierre.setVentasTransferencias(ventasTransferenciasFacturas + ventasTransferenciasPedidos);
        cierre.setVentasTarjetas(ventasTarjetasFacturas + ventasTarjetasPedidos);
        cierre.setTotalVentas(cierre.getVentasEfectivo() + cierre.getVentasTransferencias() + cierre.getVentasTarjetas());

        // Detalle de ventas
        ventasPorMetodo.put("Efectivo", cierre.getVentasEfectivo());
        ventasPorMetodo.put("Transferencia", cierre.getVentasTransferencias());
        ventasPorMetodo.put("Tarjeta", cierre.getVentasTarjetas());
        cierre.setDetalleVentas(ventasPorMetodo);

        // === CALCULAR GASTOS ===
        List<Gasto> gastos = gastoRepository.findByFechaGastoBetween(fechaInicio, fechaFin)
                .stream()
                .filter(g -> ("aprobado".equals(g.getEstado()) || "pendiente".equals(g.getEstado())))
                .collect(Collectors.toList());

        System.out.println("Gastos encontrados: " + gastos.size());

        Map<String, Double> gastosPorTipo = gastos.stream()
                .collect(Collectors.groupingBy(
                        Gasto::getTipoGastoNombre,
                        Collectors.summingDouble(Gasto::getMonto)
                ));

        cierre.setGastosPorTipo(gastosPorTipo);
        cierre.setTotalGastos(gastos.stream().mapToDouble(Gasto::getMonto).sum());

        // === CALCULAR DEBE TENER ===
        // Solo descontar gastos pagados desde caja
        double gastosEfectivoDesdeCaja = gastos.stream()
                .filter(g -> g.isPagadoDesdeCaja() && g.getFormaPago() != null && g.getFormaPago().trim().toLowerCase().equals("efectivo"))
                .mapToDouble(Gasto::getMonto).sum();

        cierre.setDebeTener(cierre.getEfectivoInicial() + cierre.getVentasEfectivo() - gastosEfectivoDesdeCaja);

        // Información adicional
        cierre.setCantidadFacturas(facturas.size());
        cierre.setCantidadPedidos(pedidosPagados.size());

        // TODO: Calcular propinas si existe el campo
        cierre.setTotalPropinas(0.0);

        System.out.println("Cierre generado - Ventas totales: " + cierre.getTotalVentas());
        System.out.println("Total gastos: " + cierre.getTotalGastos());
        System.out.println("Debe tener en efectivo: " + cierre.getDebeTener());

        return cierre;
    }

        // Nueva versión: cerrar caja sin efectivo declarado ni domicilios
        public CierreCaja cerrarCaja(CierreCaja cierre, String observaciones) {
                cierre.setObservaciones(observaciones);
                // No se calcula diferencia ni cuadreOk, solo se marca como cerrada
                cierre.setEstado("cerrado");
                cierre.setFechaCierre(LocalDateTime.now());
                return cierreCajaRepository.save(cierre);
        }

    public List<CierreCaja> getHistorialCierres(int limite) {
        return cierreCajaRepository.findUltimosCierres(PageRequest.of(0, limite));
    }

    public CierreCaja getUltimoCierre() {
        return cierreCajaRepository.findTopByOrderByFechaCierreDesc().orElse(null);
    }

    public List<CierreCaja> getCierresPorFechas(LocalDateTime inicio, LocalDateTime fin) {
        return cierreCajaRepository.findByFechaCierreBetween(inicio, fin);
    }

    public CierreCaja getCierrePorId(String id) {
        return cierreCajaRepository.findById(id).orElse(null);
    }
}
