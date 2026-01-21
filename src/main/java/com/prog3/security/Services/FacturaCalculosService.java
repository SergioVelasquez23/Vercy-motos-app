package com.prog3.security.Services;

import com.prog3.security.Models.Factura;
import com.prog3.security.Models.ItemFactura;
import com.prog3.security.Models.ItemFacturaIngrediente;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 💰 Servicio para cálculos de facturas con impuestos, retenciones y descuentos
 * Implementa los cálculos según normativa DIAN para facturación colombiana
 * 
 * Fórmula de cálculo:
 * 1. Subtotal = Σ(cantidad × precio unitario) de todos los items
 * 2. Base gravable = Subtotal - Descuentos
 * 3. Impuestos = Base gravable × Σ(porcentajes de impuestos: IVA, INC, etc.)
 * 4. Retenciones = Base gravable × Σ(porcentajes: retención, reteIVA, reteICA)
 * 5. Total final = Base gravable + Impuestos - Retenciones
 */
@Service
public class FacturaCalculosService {

    /**
     * 📊 Calcula los valores de impuestos y descuentos para un item individual
     * Actualiza los campos: valorImpuesto, valorDescuento en el item
     * 
     * @param item Item a calcular
     */
    public void calcularItem(ItemFactura item) {
        if (item == null) {
            return;
        }

        // Calcular subtotal del item
        double subtotal = item.getCantidad() * item.getPrecioUnitario();

        // Calcular descuento
        double valorDescuento = 0.0;
        if (item.getPorcentajeDescuento() > 0) {
            valorDescuento = subtotal * (item.getPorcentajeDescuento() / 100.0);
        }
        item.setValorDescuento(valorDescuento);

        // Base para calcular impuesto (después del descuento)
        double baseImponible = subtotal - valorDescuento;

        // Calcular impuesto
        double valorImpuesto = 0.0;
        if (item.getPorcentajeImpuesto() > 0) {
            valorImpuesto = baseImponible * (item.getPorcentajeImpuesto() / 100.0);
        }
        item.setValorImpuesto(valorImpuesto);

        // Calcular total del item
        item.setSubtotalItem(subtotal);
        item.setTotalItem(baseImponible + valorImpuesto);
    }

    /**
     * 💰 Calcula todos los totales de la factura aplicando impuestos, descuentos y
     * retenciones
     * Actualiza los campos: subtotal, totalImpuestos, totalDescuentos,
     * totalRetenciones,
     * baseGravable, valorRetencion, valorReteIva, valorReteIca, total
     * 
     * @param factura Factura a calcular
     */
    public void calcularFactura(Factura factura) {
        if (factura == null) {
            return;
        }

        // 1. Calcular items individuales
        if (factura.getItems() != null) {
            for (ItemFactura item : factura.getItems()) {
                calcularItem(item);
            }
        }

        // 2. Calcular subtotal (suma de todos los items antes de descuentos/impuestos)
        double subtotal = 0.0;

        if (factura.getItems() != null) {
            subtotal += factura.getItems().stream()
                    .mapToDouble(item -> item.getCantidad() * item.getPrecioUnitario())
                    .sum();
        }

        if (factura.getItemsIngredientes() != null) {
            subtotal += factura.getItemsIngredientes().stream()
                    .mapToDouble(ItemFacturaIngrediente::getPrecioTotal)
                    .sum();
        }

        factura.setSubtotal(subtotal);

        // 3. Calcular descuentos
        double totalDescuentos = 0.0;

        // Descuentos de items individuales
        if (factura.getItems() != null) {
            totalDescuentos += factura.getItems().stream()
                    .mapToDouble(ItemFactura::getValorDescuento)
                    .sum();
        }

        // Descuento general de la factura
        if (factura.getDescuentoGeneral() > 0) {
            if ("Porcentaje".equals(factura.getTipoDescuento())) {
                totalDescuentos += subtotal * (factura.getDescuentoGeneral() / 100.0);
            } else {
                totalDescuentos += factura.getDescuentoGeneral();
            }
        }

        factura.setTotalDescuentos(totalDescuentos);

        // 4. Calcular base gravable (subtotal - descuentos)
        double baseGravable = Math.max(0, subtotal - totalDescuentos);
        factura.setBaseGravable(baseGravable);

        // 5. Calcular impuestos (IVA, INC, etc.)
        double totalImpuestos = 0.0;

        if (factura.getItems() != null) {
            totalImpuestos += factura.getItems().stream()
                    .mapToDouble(ItemFactura::getValorImpuesto)
                    .sum();
        }

        factura.setTotalImpuestos(totalImpuestos);

        // 6. Calcular retenciones (retención en la fuente, reteIVA, reteICA)
        double valorRetencion = 0.0;
        double valorReteIva = 0.0;
        double valorReteIca = 0.0;

        if (factura.getPorcentajeRetencion() > 0) {
            valorRetencion = baseGravable * (factura.getPorcentajeRetencion() / 100.0);
        }

        if (factura.getPorcentajeReteIva() > 0) {
            valorReteIva = totalImpuestos * (factura.getPorcentajeReteIva() / 100.0);
        }

        if (factura.getPorcentajeReteIca() > 0) {
            valorReteIca = baseGravable * (factura.getPorcentajeReteIca() / 100.0);
        }

        double totalRetenciones = valorRetencion + valorReteIva + valorReteIca;

        factura.setValorRetencion(valorRetencion);
        factura.setValorReteIva(valorReteIva);
        factura.setValorReteIca(valorReteIca);
        factura.setTotalRetenciones(totalRetenciones);

        // 7. Calcular total final
        // Total = Base gravable + Impuestos - Retenciones
        double totalFinal = baseGravable + totalImpuestos - totalRetenciones;
        factura.setTotal(Math.max(0, totalFinal));

        System.out.println("💰 Cálculo de factura " + factura.getNumero() + " completado:");
        System.out.println("   - Subtotal: $" + subtotal);
        System.out.println("   - Descuentos: $" + totalDescuentos);
        System.out.println("   - Base gravable: $" + baseGravable);
        System.out.println("   - Impuestos: $" + totalImpuestos);
        System.out.println("   - Retenciones: $" + totalRetenciones);
        System.out.println("   - Total final: $" + factura.getTotal());
    }

    /**
     * 📋 Obtiene un desglose detallado de los cálculos de la factura
     * 
     * @param factura Factura a desglosar
     * @return Map con todos los valores calculados
     */
    public Map<String, Double> obtenerDesglose(Factura factura) {
        Map<String, Double> desglose = new HashMap<>();

        if (factura == null) {
            return desglose;
        }

        desglose.put("subtotal", factura.getSubtotal());
        desglose.put("totalDescuentos", factura.getTotalDescuentos());
        desglose.put("baseGravable", factura.getBaseGravable());
        desglose.put("totalImpuestos", factura.getTotalImpuestos());
        desglose.put("valorRetencion", factura.getValorRetencion());
        desglose.put("valorReteIva", factura.getValorReteIva());
        desglose.put("valorReteIca", factura.getValorReteIca());
        desglose.put("totalRetenciones", factura.getTotalRetenciones());
        desglose.put("total", factura.getTotal());

        return desglose;
    }

    /**
     * 📊 Calcula totales agregados para una lista de facturas
     * Útil para reportes y cuadres de caja
     * 
     * @param facturas Lista de facturas
     * @return Map con totales agregados
     */
    public Map<String, Double> calcularTotalesLista(List<Factura> facturas) {
        Map<String, Double> totales = new HashMap<>();

        if (facturas == null || facturas.isEmpty()) {
            totales.put("subtotal", 0.0);
            totales.put("totalImpuestos", 0.0);
            totales.put("totalDescuentos", 0.0);
            totales.put("totalRetenciones", 0.0);
            totales.put("total", 0.0);
            return totales;
        }

        double subtotal = facturas.stream().mapToDouble(Factura::getSubtotal).sum();
        double totalImpuestos = facturas.stream().mapToDouble(Factura::getTotalImpuestos).sum();
        double totalDescuentos = facturas.stream().mapToDouble(Factura::getTotalDescuentos).sum();
        double totalRetenciones = facturas.stream().mapToDouble(Factura::getTotalRetenciones).sum();
        double total = facturas.stream().mapToDouble(Factura::getTotal).sum();

        totales.put("subtotal", subtotal);
        totales.put("totalImpuestos", totalImpuestos);
        totales.put("totalDescuentos", totalDescuentos);
        totales.put("totalRetenciones", totalRetenciones);
        totales.put("total", total);

        return totales;
    }

    /**
     * 📈 Obtiene un resumen de impuestos agrupados por tipo
     * 
     * @param factura Factura a analizar
     * @return Map con impuestos agrupados por tipo (IVA 19%, IVA 5%, INC 8%, etc.)
     */
    public Map<String, Double> obtenerResumenImpuestos(Factura factura) {
        Map<String, Double> resumen = new HashMap<>();

        if (factura == null || factura.getItems() == null) {
            return resumen;
        }

        for (ItemFactura item : factura.getItems()) {
            double porcentaje = item.getPorcentajeImpuesto();
            if (porcentaje > 0) {
                String tipoImpuesto = obtenerTipoImpuesto(porcentaje);
                resumen.put(tipoImpuesto,
                        resumen.getOrDefault(tipoImpuesto, 0.0) + item.getValorImpuesto());
            }
        }

        return resumen;
    }

    /**
     * 🏷️ Determina el tipo de impuesto según el porcentaje
     * 
     * @param porcentaje Porcentaje del impuesto
     * @return Tipo de impuesto (IVA 19%, IVA 5%, INC 8%, etc.)
     */
    private String obtenerTipoImpuesto(double porcentaje) {
        if (porcentaje == 19.0) {
            return "IVA 19%";
        } else if (porcentaje == 5.0) {
            return "IVA 5%";
        } else if (porcentaje == 0.0) {
            return "Excluido";
        } else if (porcentaje == 8.0) {
            return "INC 8%";
        } else if (porcentaje == 16.0) {
            return "INC 16%";
        } else {
            return "Otro " + porcentaje + "%";
        }
    }
}
