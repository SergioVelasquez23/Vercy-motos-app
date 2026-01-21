package com.prog3.security.Services;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.springframework.stereotype.Service;
import com.prog3.security.Models.Pedido;
import com.prog3.security.Models.ItemPedido;

/**
 * Servicio centralizado para todos los cálculos de pedidos incluyendo
 * impuestos, descuentos, retenciones y totales según DIAN
 */
@Service
public class PedidoCalculosService {

    /**
     * Calcular todos los valores de un item (impuestos, descuentos, subtotal)
     */
    public void calcularItem(ItemPedido item) {
        if (item == null)
            return;

        // Calcular valor del impuesto si tiene porcentaje configurado
        if (item.getPorcentajeImpuesto() > 0) {
            double valorBase = item.getPrecioUnitario() * item.getCantidad();
            item.setValorImpuesto((valorBase * item.getPorcentajeImpuesto()) / 100.0);
        } else {
            item.setValorImpuesto(0.0);
        }

        // Calcular valor del descuento si tiene porcentaje configurado
        if (item.getPorcentajeDescuento() > 0) {
            double valorBase = item.getPrecioUnitario() * item.getCantidad();
            item.setValorDescuento((valorBase * item.getPorcentajeDescuento()) / 100.0);
        } else {
            item.setValorDescuento(0.0);
        }
    }

    /**
     * Calcular todos los totales de un pedido completo
     * Actualiza: subtotal, totalImpuestos, totalDescuentos, totalRetenciones,
     * totalFinal
     */
    public void calcularPedido(Pedido pedido) {
        if (pedido == null || pedido.getItems() == null) {
            return;
        }

        // 1. Calcular cada item
        for (ItemPedido item : pedido.getItems()) {
            calcularItem(item);
        }

        // 2. Calcular subtotal (suma de precio * cantidad de todos los items, sin
        // impuestos ni descuentos)
        double subtotal = pedido.getItems().stream()
                .mapToDouble(item -> item.getPrecioUnitario() * item.getCantidad())
                .sum();
        pedido.setSubtotal(subtotal);

        // 3. Calcular total de descuentos de productos
        double descuentoProductos = pedido.getItems().stream()
                .mapToDouble(ItemPedido::getValorDescuento)
                .sum();
        pedido.setDescuentoProductos(descuentoProductos);

        // 4. Calcular descuento general
        double descuentoGeneral = 0.0;
        if ("Porcentaje".equals(pedido.getTipoDescuentoGeneral())) {
            descuentoGeneral = (subtotal * pedido.getDescuentoGeneral()) / 100.0;
        } else {
            descuentoGeneral = pedido.getDescuentoGeneral();
        }

        // 5. Total de descuentos
        double totalDescuentos = descuentoProductos + descuentoGeneral;
        pedido.setTotalDescuentos(totalDescuentos);

        // 6. Base gravable (subtotal - descuentos)
        double baseGravable = Math.max(0, subtotal - totalDescuentos);

        // 7. Calcular total de impuestos sobre items (después de descuentos)
        // Los impuestos se calculan sobre el valor con descuento
        double totalImpuestos = pedido.getItems().stream()
                .mapToDouble(item -> {
                    double valorItem = (item.getPrecioUnitario() * item.getCantidad()) - item.getValorDescuento();
                    return (valorItem * item.getPorcentajeImpuesto()) / 100.0;
                })
                .sum();
        pedido.setTotalImpuestos(totalImpuestos);

        // 8. Calcular retenciones
        double valorRetencion = 0.0;
        double valorReteIVA = 0.0;
        double valorReteICA = 0.0;

        if (pedido.getRetencion() > 0) {
            valorRetencion = (baseGravable * pedido.getRetencion()) / 100.0;
        }
        if (pedido.getReteIVA() > 0) {
            valorReteIVA = (totalImpuestos * pedido.getReteIVA()) / 100.0;
        }
        if (pedido.getReteICA() > 0) {
            valorReteICA = (baseGravable * pedido.getReteICA()) / 100.0;
        }

        pedido.setValorRetencion(valorRetencion);
        pedido.setValorReteIVA(valorReteIVA);
        pedido.setValorReteICA(valorReteICA);

        double totalRetenciones = valorRetencion + valorReteIVA + valorReteICA;
        pedido.setTotalRetenciones(totalRetenciones);

        // 9. Total final = Subtotal - Descuentos + Impuestos - Retenciones
        double totalFinal = baseGravable + totalImpuestos - totalRetenciones;
        pedido.setTotalFinal(Math.max(0, totalFinal));

        // 10. Actualizar campo "total" legacy para compatibilidad
        pedido.setTotal(subtotal);

        System.out.println("📊 Cálculos del pedido:");
        System.out.println("  Subtotal: $" + subtotal);
        System.out.println("  Descuentos: $" + totalDescuentos);
        System.out.println("  Base gravable: $" + baseGravable);
        System.out.println("  Impuestos: $" + totalImpuestos);
        System.out.println("  Retenciones: $" + totalRetenciones);
        System.out.println("  Total final: $" + totalFinal);
    }

    /**
     * Obtener desglose detallado de un pedido
     */
    public Map<String, Object> obtenerDesglose(Pedido pedido) {
        Map<String, Object> desglose = new HashMap<>();

        if (pedido == null) {
            return desglose;
        }

        desglose.put("subtotal", pedido.getSubtotal());
        desglose.put("descuentoProductos", pedido.getDescuentoProductos());
        desglose.put("descuentoGeneral", pedido.getDescuentoGeneral());
        desglose.put("totalDescuentos", pedido.getTotalDescuentos());
        desglose.put("baseGravable", pedido.getSubtotal() - pedido.getTotalDescuentos());
        desglose.put("totalImpuestos", pedido.getTotalImpuestos());
        desglose.put("valorRetencion", pedido.getValorRetencion());
        desglose.put("valorReteIVA", pedido.getValorReteIVA());
        desglose.put("valorReteICA", pedido.getValorReteICA());
        desglose.put("totalRetenciones", pedido.getTotalRetenciones());
        desglose.put("propina", pedido.getPropina());
        desglose.put("totalFinal", pedido.getTotalFinal());
        desglose.put("totalAPagar", pedido.getTotalFinal() + (pedido.getPropina() != 0 ? pedido.getPropina() : 0));

        return desglose;
    }

    /**
     * Calcular el total a pagar (total final + propina)
     */
    public double calcularTotalAPagar(Pedido pedido) {
        if (pedido == null) {
            return 0.0;
        }

        double propina = pedido.getPropina() != 0 ? pedido.getPropina() : 0.0;
        return pedido.getTotalFinal() + propina;
    }

    /**
     * Validar que los cálculos del pedido sean correctos
     */
    public boolean validarCalculos(Pedido pedido) {
        if (pedido == null) {
            return false;
        }

        // Recalcular
        calcularPedido(pedido);

        // Validar que el total pagado sea suficiente
        double totalAPagar = calcularTotalAPagar(pedido);

        return pedido.getTotalPagado() >= totalAPagar;
    }

    /**
     * Obtener resumen de impuestos por tipo
     */
    public Map<String, Double> obtenerResumenImpuestos(Pedido pedido) {
        Map<String, Double> resumen = new HashMap<>();

        if (pedido == null || pedido.getItems() == null) {
            return resumen;
        }

        for (ItemPedido item : pedido.getItems()) {
            String tipoImpuesto = item.getTipoImpuesto() != null ? item.getTipoImpuesto() : "Sin impuesto";
            double valorItem = (item.getPrecioUnitario() * item.getCantidad()) - item.getValorDescuento();
            double impuestoItem = (valorItem * item.getPorcentajeImpuesto()) / 100.0;

            resumen.put(tipoImpuesto, resumen.getOrDefault(tipoImpuesto, 0.0) + impuestoItem);
        }

        return resumen;
    }

    /**
     * Calcular totales de una lista de pedidos (para reportes y cuadre de caja)
     */
    public Map<String, Object> calcularTotalesLista(List<Pedido> pedidos) {
        Map<String, Object> totales = new HashMap<>();

        if (pedidos == null || pedidos.isEmpty()) {
            totales.put("cantidadPedidos", 0);
            totales.put("subtotal", 0.0);
            totales.put("totalDescuentos", 0.0);
            totales.put("totalImpuestos", 0.0);
            totales.put("totalRetenciones", 0.0);
            totales.put("totalPropinas", 0.0);
            totales.put("totalFinal", 0.0);
            return totales;
        }

        int cantidadPedidos = pedidos.size();
        double subtotal = pedidos.stream().mapToDouble(Pedido::getSubtotal).sum();
        double totalDescuentos = pedidos.stream().mapToDouble(Pedido::getTotalDescuentos).sum();
        double totalImpuestos = pedidos.stream().mapToDouble(Pedido::getTotalImpuestos).sum();
        double totalRetenciones = pedidos.stream().mapToDouble(Pedido::getTotalRetenciones).sum();
        double totalPropinas = pedidos.stream().mapToDouble(p -> p.getPropina() != 0 ? p.getPropina() : 0.0).sum();
        double totalFinal = pedidos.stream().mapToDouble(Pedido::getTotalFinal).sum();

        totales.put("cantidadPedidos", cantidadPedidos);
        totales.put("subtotal", subtotal);
        totales.put("totalDescuentos", totalDescuentos);
        totales.put("totalImpuestos", totalImpuestos);
        totales.put("totalRetenciones", totalRetenciones);
        totales.put("totalPropinas", totalPropinas);
        totales.put("totalFinal", totalFinal);
        totales.put("totalConPropinas", totalFinal + totalPropinas);

        return totales;
    }
}
