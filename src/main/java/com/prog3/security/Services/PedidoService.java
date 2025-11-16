package com.prog3.security.Services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.prog3.security.Models.Pedido;
import com.prog3.security.Models.CuadreCaja;
import com.prog3.security.Repositories.PedidoRepository;
import com.prog3.security.Repositories.CuadreCajaRepository;

/**
 * Servicio para manejar operaciones de pedidos
 */
@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private CuadreCajaRepository cuadreCajaRepository;

    @Autowired
    private MesaService mesaService;

    /**
     * Elimina un pedido y revierte los efectos en ventas y caja
     */
    public boolean eliminarPedidoPagado(String pedidoId) {
        try {
            System.out.println("🗑️ Iniciando eliminación de pedido: " + pedidoId);

            // Buscar el pedido
            Optional<Pedido> pedidoOpt = pedidoRepository.findById(pedidoId);
            if (!pedidoOpt.isPresent()) {
                System.err.println("❌ Pedido no encontrado: " + pedidoId);
                return false;
            }

            Pedido pedido = pedidoOpt.get();

            // Verificar que el pedido esté pagado
            if (!"pagado".equals(pedido.getEstado())) {
                throw new RuntimeException("Solo se pueden eliminar pedidos que estén pagados");
            }

            // 1. Revertir el dinero en caja si fue pagado en efectivo
            if ("efectivo".equals(pedido.getFormaPago()) && pedido.getCuadreCajaId() != null) {
                revertirPagoEnCaja(pedido);
            }

            // 2. Revertir las ventas del cuadre de caja
            revertirVentasEnCuadre(pedido);

            // 3. Eliminar el pedido
            pedidoRepository.deleteById(pedidoId);
            // Limpieza automática de la mesa
            mesaService.limpiarMesaSiNoTienePedidos(pedido.getMesa());

            System.out.println("✅ Pedido eliminado exitosamente: " + pedidoId);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Error al eliminar pedido: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Revierte el pago en efectivo del cuadre de caja
     */
    private void revertirPagoEnCaja(Pedido pedido) {
        try {
            System.out.println("💰 Revirtiendo pago en efectivo para pedido: " + pedido.get_id() + " - $" + pedido.getTotalPagado());

            // Buscar el cuadre de caja
            Optional<CuadreCaja> cuadreOpt = cuadreCajaRepository.findById(pedido.getCuadreCajaId());
            if (!cuadreOpt.isPresent()) {
                System.err.println("⚠️ Cuadre de caja no encontrado: " + pedido.getCuadreCajaId());
                return;
            }

            CuadreCaja cuadre = cuadreOpt.get();

            // Verificar que el cuadre no esté cerrado
            if (cuadre.isCerrada()) {
                throw new RuntimeException("No se puede revertir el pago de un cuadre cerrado");
            }

            // Restar el dinero del efectivo esperado
            double efectivoAnterior = cuadre.getEfectivoEsperado();
            double nuevoEfectivo = efectivoAnterior - pedido.getTotalPagado();
            cuadre.setEfectivoEsperado(nuevoEfectivo);

            cuadreCajaRepository.save(cuadre);

            System.out.println("💵 Efectivo revertido en caja: $" + pedido.getTotalPagado()
                    + ". Efectivo antes: $" + efectivoAnterior
                    + ", después: $" + nuevoEfectivo);

        } catch (Exception e) {
            System.err.println("❌ Error al revertir pago en caja: " + e.getMessage());
        }
    }

    /**
     * Revierte las ventas del cuadre de caja
     */
    private void revertirVentasEnCuadre(Pedido pedido) {
        try {
            System.out.println("📊 Revirtiendo ventas para pedido: " + pedido.get_id() + " - $" + pedido.getTotalPagado());

            if (pedido.getCuadreCajaId() == null) {
                System.err.println("⚠️ El pedido no tiene cuadre de caja asociado");
                return;
            }

            // Buscar el cuadre de caja
            Optional<CuadreCaja> cuadreOpt = cuadreCajaRepository.findById(pedido.getCuadreCajaId());
            if (!cuadreOpt.isPresent()) {
                System.err.println("⚠️ Cuadre de caja no encontrado: " + pedido.getCuadreCajaId());
                return;
            }

            CuadreCaja cuadre = cuadreOpt.get();

            // Verificar que el cuadre no esté cerrado
            if (cuadre.isCerrada()) {
                throw new RuntimeException("No se puede revertir las ventas de un cuadre cerrado");
            }

            // Revertir los totales de ventas
            double totalVentasAnterior = cuadre.getTotalVentas();
            double nuevoTotalVentas = totalVentasAnterior - pedido.getTotal();

            // Revertir las propinas
            double totalPropinaAnterior = cuadre.getTotalPropinas();
            double nuevaTotalPropina = totalPropinaAnterior - pedido.getPropina();

            cuadre.setTotalVentas(nuevoTotalVentas);
            cuadre.setTotalPropinas(nuevaTotalPropina);

            // Revertir las ventas por forma de pago
            revertirVentasPorFormaPago(cuadre, pedido);

            cuadreCajaRepository.save(cuadre);

            System.out.println("📈 Ventas revertidas en cuadre: "
                    + "Total ventas: $" + totalVentasAnterior + " → $" + nuevoTotalVentas
                    + ", Total propinas: $" + totalPropinaAnterior + " → $" + nuevaTotalPropina);

        } catch (Exception e) {
            System.err.println("❌ Error al revertir ventas en cuadre: " + e.getMessage());
        }
    }

    /**
     * Revierte las ventas por forma de pago en el cuadre
     */
    private void revertirVentasPorFormaPago(CuadreCaja cuadre, Pedido pedido) {
        try {
            String formaPago = pedido.getFormaPago();
            double totalPagado = pedido.getTotalPagado();

            // Obtener las ventas desglosadas
            Map<String, Double> ventasDesglosadas = cuadre.getVentasDesglosadas();
            if (ventasDesglosadas == null) {
                ventasDesglosadas = new HashMap<>();
            }

            // Normalizar la forma de pago para coincidir con las claves del mapa
            String claveFormaPago = formaPago.substring(0, 1).toUpperCase() + formaPago.substring(1).toLowerCase();

            double ventaAnterior = ventasDesglosadas.getOrDefault(claveFormaPago, 0.0);
            double nuevaVenta = ventaAnterior - totalPagado;

            if (nuevaVenta <= 0) {
                ventasDesglosadas.remove(claveFormaPago);
                System.out.println("� Ventas por " + formaPago + " completamente revertidas: $" + ventaAnterior + " → $0");
            } else {
                ventasDesglosadas.put(claveFormaPago, nuevaVenta);
                System.out.println("💰 Ventas por " + formaPago + " revertidas: $" + ventaAnterior + " → $" + nuevaVenta);
            }

            cuadre.setVentasDesglosadas(ventasDesglosadas);

        } catch (Exception e) {
            System.err.println("❌ Error al revertir ventas por forma de pago: " + e.getMessage());
        }
    }

    /**
     * Busca un pedido por ID
     */
    public Optional<Pedido> buscarPedidoPorId(String id) {
        return pedidoRepository.findById(id);
    }

    /**
     * Obtiene todos los pedidos
     */
    public List<Pedido> obtenerTodosLosPedidos() {
        return pedidoRepository.findAll();
    }
}
