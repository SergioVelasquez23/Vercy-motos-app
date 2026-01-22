package com.prog3.security.Services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.prog3.security.Models.Gasto;
import com.prog3.security.Models.ItemGasto;
import com.prog3.security.Models.TipoGasto;
import com.prog3.security.Models.CuadreCaja;
import com.prog3.security.Repositories.GastoRepository;
import com.prog3.security.Repositories.TipoGastoRepository;
import com.prog3.security.Repositories.CuadreCajaRepository;
import com.prog3.security.DTOs.GastoRequest;

@Service
public class GastoService {

    @Autowired
    private GastoRepository gastoRepository;

    @Autowired
    private TipoGastoRepository tipoGastoRepository;

    @Autowired
    private CuadreCajaRepository cuadreCajaRepository;

    @Autowired
    private CuadreCajaService cuadreCajaService;

    /**
     * Obtiene todos los gastos
     */
    public List<Gasto> obtenerTodosGastos() {
        return gastoRepository.findAll();
    }

    /**
     * Busca un gasto por ID
     */
    public Gasto obtenerGastoPorId(String id) {
        return gastoRepository.findById(id).orElse(null);
    }

    /**
     * Obtiene los gastos de un cuadre de caja
     */
    public List<Gasto> obtenerGastosPorCuadre(String cuadreId) {
        return gastoRepository.findByCuadreCajaId(cuadreId);
    }

    /**
     * Crea un nuevo gasto
     */
    public Gasto crearGasto(GastoRequest request) {
        // Verificar que el tipo de gasto exista (categoría)
        TipoGasto tipoGasto = tipoGastoRepository.findById(request.getTipoGastoId()).orElse(null);
        if (tipoGasto == null) {
            throw new RuntimeException("Tipo de gasto (categoría) no encontrado");
        }

        // Verificar que el cuadre exista y no esté cerrado
        CuadreCaja cuadreCaja = cuadreCajaRepository.findById(request.getCuadreCajaId()).orElse(null);
        if (cuadreCaja == null) {
            throw new RuntimeException("Cuadre de caja no encontrado");
        }

        if (cuadreCaja.isCerrada()) {
            throw new RuntimeException("No se pueden agregar gastos a un cuadre cerrado");
        }

        // Calcular monto total si hay items
        double montoTotal = request.getMonto();
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            montoTotal = calcularMontoDesdeItems(request);
        }

        // ✅ Validar si se puede pagar desde caja
        if (request.isPagadoDesdeCaja()) {
            validarPagoDesdeEfectivoCaja(cuadreCaja, montoTotal);
        }

        Gasto gasto = new Gasto(
                request.getCuadreCajaId(),
                request.getTipoGastoId(),
                tipoGasto.getNombre(), // Guardar también el nombre para facilitar consultas
                request.getConcepto(),
                montoTotal,
                request.getResponsable()
        );

        // Establecer campos básicos opcionales
        if (request.getFechaGasto() != null) {
            gasto.setFechaGasto(request.getFechaGasto());
        }
        if (request.getFechaVencimiento() != null) {
            gasto.setFechaVencimiento(request.getFechaVencimiento());
        }
        if (request.getNumeroRecibo() != null) {
            gasto.setNumeroRecibo(request.getNumeroRecibo());
        }
        if (request.getNumeroFactura() != null) {
            gasto.setNumeroFactura(request.getNumeroFactura());
        }
        if (request.getProveedor() != null) {
            gasto.setProveedor(request.getProveedor());
        }
        if (request.getProveedorId() != null) {
            gasto.setProveedorId(request.getProveedorId());
        }
        if (request.getFormaPago() != null) {
            gasto.setFormaPago(request.getFormaPago());
        }

        // Documento soporte
        gasto.setDocumentoSoporte(request.isDocumentoSoporte());

        // Items del gasto (múltiples líneas de concepto)
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            gasto.setItems(new ArrayList<>(request.getItems()));
        }

        // Campos de cálculo
        if (request.getSubtotal() > 0) {
            gasto.setSubtotal(request.getSubtotal());
        }
        if (request.getTotalDescuentos() > 0) {
            gasto.setTotalDescuentos(request.getTotalDescuentos());
        }
        if (request.getImpuestos() > 0) {
            gasto.setImpuestos(request.getImpuestos());
        }
        if (request.getTotalImpuestos() > 0) {
            gasto.setTotalImpuestos(request.getTotalImpuestos());
        }

        // 💰 Porcentajes de retenciones
        gasto.setPorcentajeRetencion(request.getPorcentajeRetencion());
        gasto.setPorcentajeReteIva(request.getPorcentajeReteIva());
        gasto.setPorcentajeReteIca(request.getPorcentajeReteIca());

        // Calcular totales (incluye retenciones)
        gasto.calcularTotales();

        // ✅ Establecer pagadoDesdeCaja
        gasto.setPagadoDesdeCaja(request.isPagadoDesdeCaja());

        // ✅ Si se paga desde caja, forzar forma de pago a efectivo
        if (request.isPagadoDesdeCaja()) {
            gasto.setFormaPago("efectivo");
            System.out.println("💰 Gasto marcado como pagado desde caja - Forma de pago establecida en 'efectivo'");
        }

        // Guardar el gasto
        Gasto gastoGuardado = gastoRepository.save(gasto);

        // Actualizar el total de gastos en el cuadre de caja
        actualizarTotalesGastoEnCuadre(cuadreCaja);

        return gastoGuardado;
    }

    /**
     * Actualiza un gasto existente
     */
    public Gasto actualizarGasto(String id, GastoRequest request) {
        Gasto gasto = gastoRepository.findById(id).orElse(null);

        if (gasto == null) {
            return null;
        }

        // Verificar que el cuadre no esté cerrado
        CuadreCaja cuadreCaja = cuadreCajaRepository.findById(gasto.getCuadreCajaId()).orElse(null);
        if (cuadreCaja != null && cuadreCaja.isCerrada()) {
            throw new RuntimeException("No se pueden modificar gastos de un cuadre cerrado");
        }

        // Calcular monto total si hay items
        double montoTotal = request.getMonto();
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            montoTotal = calcularMontoDesdeItems(request);
        }

        // Validar cambio en pagadoDesdeCaja
        boolean cambiaAPagadoDesdeCaja = !gasto.isPagadoDesdeCaja() && request.isPagadoDesdeCaja();
        if (cambiaAPagadoDesdeCaja) {
            validarPagoDesdeEfectivoCaja(cuadreCaja, montoTotal > 0 ? montoTotal : gasto.getMonto());
        }

        // Actualizar tipo de gasto (categoría) si cambió
        if (request.getTipoGastoId() != null && !request.getTipoGastoId().equals(gasto.getTipoGastoId())) {
            TipoGasto tipoGasto = tipoGastoRepository.findById(request.getTipoGastoId()).orElse(null);
            if (tipoGasto == null) {
                throw new RuntimeException("Tipo de gasto (categoría) no encontrado");
            }
            gasto.setTipoGastoId(request.getTipoGastoId());
            gasto.setTipoGastoNombre(tipoGasto.getNombre());
        }

        // Actualizar campos básicos
        if (request.getConcepto() != null) {
            gasto.setConcepto(request.getConcepto());
        }
        if (montoTotal > 0) {
            gasto.setMonto(montoTotal);
        }
        if (request.getResponsable() != null) {
            gasto.setResponsable(request.getResponsable());
        }
        if (request.getFechaGasto() != null) {
            gasto.setFechaGasto(request.getFechaGasto());
        }
        if (request.getFechaVencimiento() != null) {
            gasto.setFechaVencimiento(request.getFechaVencimiento());
        }
        if (request.getNumeroRecibo() != null) {
            gasto.setNumeroRecibo(request.getNumeroRecibo());
        }
        if (request.getNumeroFactura() != null) {
            gasto.setNumeroFactura(request.getNumeroFactura());
        }
        if (request.getProveedor() != null) {
            gasto.setProveedor(request.getProveedor());
        }
        if (request.getProveedorId() != null) {
            gasto.setProveedorId(request.getProveedorId());
        }
        if (request.getFormaPago() != null) {
            gasto.setFormaPago(request.getFormaPago());
        }

        // Documento soporte
        gasto.setDocumentoSoporte(request.isDocumentoSoporte());

        // Items del gasto
        if (request.getItems() != null) {
            gasto.setItems(new ArrayList<>(request.getItems()));
        }

        // Campos de cálculo
        if (request.getSubtotal() > 0) {
            gasto.setSubtotal(request.getSubtotal());
        }
        if (request.getTotalDescuentos() > 0) {
            gasto.setTotalDescuentos(request.getTotalDescuentos());
        }
        if (request.getImpuestos() > 0) {
            gasto.setImpuestos(request.getImpuestos());
        }
        if (request.getTotalImpuestos() > 0) {
            gasto.setTotalImpuestos(request.getTotalImpuestos());
        }

        // 💰 Actualizar porcentajes de retenciones
        gasto.setPorcentajeRetencion(request.getPorcentajeRetencion());
        gasto.setPorcentajeReteIva(request.getPorcentajeReteIva());
        gasto.setPorcentajeReteIca(request.getPorcentajeReteIca());

        // Recalcular totales
        gasto.calcularTotales();

        // Actualizar pagadoDesdeCaja
        gasto.setPagadoDesdeCaja(request.isPagadoDesdeCaja());

        // Si se paga desde caja, forzar forma de pago a efectivo
        if (request.isPagadoDesdeCaja()) {
            gasto.setFormaPago("efectivo");
            System.out.println("💰 Gasto actualizado como pagado desde caja - Forma de pago establecida en 'efectivo'");
        }

        // Guardar el gasto actualizado
        Gasto gastoActualizado = gastoRepository.save(gasto);

        // Actualizar totales en el cuadre
        if (cuadreCaja != null) {
            actualizarTotalesGastoEnCuadre(cuadreCaja);
        }

        return gastoActualizado;
    }

    /**
     * Calcula el monto total desde los items del request
     * Incluye cálculo de descuentos, impuestos y retenciones
     */
    private double calcularMontoDesdeItems(GastoRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            return request.getMonto();
        }

        double subtotal = 0.0;
        double totalDescuentos = 0.0;
        double totalImpuestos = 0.0;

        for (ItemGasto item : request.getItems()) {
            item.calcularTotales();
            subtotal += item.getValor();
            totalDescuentos += item.getValorDescuento();
            totalImpuestos += item.getValorImpuesto();
        }

        // Base gravable
        double baseGravable = subtotal - totalDescuentos + totalImpuestos;

        // Calcular retenciones
        double valorRetencion = baseGravable * (request.getPorcentajeRetencion() / 100.0);
        double valorReteIva = totalImpuestos * (request.getPorcentajeReteIva() / 100.0);
        double valorReteIca = baseGravable * (request.getPorcentajeReteIca() / 100.0);
        double totalRetenciones = valorRetencion + valorReteIva + valorReteIca;

        // Monto total = baseGravable - retenciones
        return baseGravable - totalRetenciones;
    }

    /**
     * Elimina un gasto
     */
    public boolean eliminarGasto(String id) {
        try {
            Gasto gasto = gastoRepository.findById(id).orElse(null);
            if (gasto == null) {
                return false;
            }

            // Verificar que el cuadre no esté cerrado
            CuadreCaja cuadreCaja = cuadreCajaRepository.findById(gasto.getCuadreCajaId()).orElse(null);
            if (cuadreCaja != null && cuadreCaja.isCerrada()) {
                throw new RuntimeException("No se pueden eliminar gastos de un cuadre cerrado");
            }

            // Si el gasto fue pagado desde caja Y en efectivo, devolver el dinero al cuadre
            if (gasto.isPagadoDesdeCaja() && cuadreCaja != null && 
                "efectivo".equalsIgnoreCase(gasto.getFormaPago())) {
                
                double efectivoActual = cuadreCaja.getEfectivoEsperado();
                cuadreCaja.setEfectivoEsperado(efectivoActual + gasto.getMonto());
                cuadreCajaRepository.save(cuadreCaja);
                
                System.out.println("✅ Revirtiendo gasto EN EFECTIVO de caja: $" + gasto.getMonto() + 
                                   ". Efectivo esperado antes: $" + efectivoActual + 
                                   ", después: $" + (efectivoActual + gasto.getMonto()));
            } else if (gasto.isPagadoDesdeCaja() && cuadreCaja != null) {
                System.out.println("ℹ️ Gasto pagado desde caja pero por TRANSFERENCIA: $" + gasto.getMonto() + 
                                   " - No afecta efectivo esperado");
            }

            gastoRepository.deleteById(id);

            // Actualizar totales en el cuadre
            if (cuadreCaja != null) {
                actualizarTotalesGastoEnCuadre(cuadreCaja);
            }

            return true;
        } catch (Exception e) {
            System.err.println("Error al eliminar gasto: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene gastos por rango de fechas
     */
    public List<Gasto> obtenerGastosPorFechas(LocalDateTime inicio, LocalDateTime fin) {
        return gastoRepository.findByFechaGastoBetween(inicio, fin);
    }

    /**
     * Actualiza los totales de gastos en un cuadre de caja
     */
    private void actualizarTotalesGastoEnCuadre(CuadreCaja cuadreCaja) {
        List<Gasto> gastos = gastoRepository.findByCuadreCajaId(cuadreCaja.get_id());

        // Calcular el total de gastos
        double totalGastos = 0.0;

        // Inicializar o resetear el mapa de gastos desglosados por tipo
        cuadreCaja.setGastosDesglosados(new HashMap<>());

        for (Gasto gasto : gastos) {
            // Sumar al total
            totalGastos += gasto.getMonto();

            // Agregar al desglose por tipo de gasto
            String tipoGastoNombre = gasto.getTipoGastoNombre();
            double montoActual = cuadreCaja.getGastosDesglosados().getOrDefault(tipoGastoNombre, 0.0);
            cuadreCaja.getGastosDesglosados().put(tipoGastoNombre, montoActual + gasto.getMonto());
        }

        // Actualizar el total de gastos en el cuadre
        cuadreCaja.setTotalGastos(totalGastos);

        // Guardar el cuadre actualizado
        cuadreCajaRepository.save(cuadreCaja);

        System.out.println("Totales de gastos actualizados en cuadre " + cuadreCaja.get_id()
                + ": Total = " + totalGastos
                + ", Desglose = " + cuadreCaja.getGastosDesglosados());
    }

    /**
     * ✅ NUEVA FUNCIONALIDAD: Valida si hay suficiente efectivo en caja para realizar un gasto
     */
    private void validarPagoDesdeEfectivoCaja(CuadreCaja cuadreCaja, double montoGasto) {
        try {
            System.out.println("💰 Validando pago desde efectivo de caja...");
            System.out.println("💰 Monto del gasto: $" + montoGasto);
            
            // Obtener los detalles actuales de la caja usando el servicio
            Map<String, Object> detallesCaja = cuadreCajaService.calcularDetallesVentas();
            
            // Obtener el fondo inicial de la caja
            double fondoInicial = cuadreCaja.getFondoInicial();
            
            // Obtener el efectivo por ventas del cálculo
            double efectivoPorVentas = (double) detallesCaja.get("efectivoEsperadoPorVentas");
            
            // Calcular el efectivo total disponible
            double efectivoTotalDisponible = fondoInicial + efectivoPorVentas;
            
            // Obtener gastos en efectivo ya registrados para esta caja
            List<Gasto> gastosEfectivoExistentes = gastoRepository.findByCuadreCajaId(cuadreCaja.get_id())
                    .stream()
                    .filter(g -> "efectivo".equalsIgnoreCase(g.getFormaPago()) || g.isPagadoDesdeCaja())
                    .toList();
            
            double gastosEfectivoYaRealizados = gastosEfectivoExistentes.stream()
                    .mapToDouble(Gasto::getMonto)
                    .sum();
            
            // Calcular efectivo disponible después de gastos existentes
            double efectivoDisponible = efectivoTotalDisponible - gastosEfectivoYaRealizados;
            
            System.out.println("💰 === VALIDACIÓN DE EFECTIVO EN CAJA ===");
            System.out.println("💰 Fondo inicial: $" + fondoInicial);
            System.out.println("💰 Efectivo por ventas: $" + efectivoPorVentas);
            System.out.println("💰 Efectivo total: $" + efectivoTotalDisponible);
            System.out.println("💰 Gastos en efectivo ya realizados: $" + gastosEfectivoYaRealizados + " (" + gastosEfectivoExistentes.size() + " gastos)");
            System.out.println("💰 Efectivo disponible: $" + efectivoDisponible);
            System.out.println("💰 Monto del nuevo gasto: $" + montoGasto);
            
            if (efectivoDisponible < montoGasto) {
                String mensajeError = String.format(
                    "Efectivo insuficiente en caja. Disponible: $%.2f, Requerido: $%.2f. Faltante: $%.2f",
                    efectivoDisponible, montoGasto, (montoGasto - efectivoDisponible)
                );
                System.err.println("❌ " + mensajeError);
                throw new RuntimeException(mensajeError);
            }
            
            System.out.println("✅ Efectivo suficiente para realizar el gasto");
            
        } catch (RuntimeException e) {
            // Re-lanzar errores de validación
            throw e;
        } catch (Exception e) {
            System.err.println("❌ Error al validar efectivo en caja: " + e.getMessage());
            throw new RuntimeException("Error al validar disponibilidad de efectivo: " + e.getMessage());
        }
    }

    /**
     * ✅ NUEVA FUNCIONALIDAD: Calcula el efectivo disponible en una caja
     */
    public Map<String, Object> calcularEfectivoDisponible(String cuadreCajaId) {
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            CuadreCaja cuadreCaja = cuadreCajaRepository.findById(cuadreCajaId).orElse(null);
            if (cuadreCaja == null) {
                resultado.put("error", "Cuadre de caja no encontrado");
                return resultado;
            }
            
            // Obtener detalles de ventas
            Map<String, Object> detallesCaja = cuadreCajaService.calcularDetallesVentas();
            
            double fondoInicial = cuadreCaja.getFondoInicial();
            double efectivoPorVentas = (double) detallesCaja.get("efectivoEsperadoPorVentas");
            double efectivoTotal = fondoInicial + efectivoPorVentas;
            
            // Calcular gastos en efectivo
            List<Gasto> gastosEfectivo = gastoRepository.findByCuadreCajaId(cuadreCajaId)
                    .stream()
                    .filter(g -> "efectivo".equalsIgnoreCase(g.getFormaPago()) || g.isPagadoDesdeCaja())
                    .toList();
            
            double totalGastosEfectivo = gastosEfectivo.stream()
                    .mapToDouble(Gasto::getMonto)
                    .sum();
            
            double efectivoDisponible = efectivoTotal - totalGastosEfectivo;
            
            resultado.put("fondoInicial", fondoInicial);
            resultado.put("efectivoPorVentas", efectivoPorVentas);
            resultado.put("efectivoTotal", efectivoTotal);
            resultado.put("gastosEfectivo", totalGastosEfectivo);
            resultado.put("efectivoDisponible", efectivoDisponible);
            resultado.put("cantidadGastosEfectivo", gastosEfectivo.size());
            resultado.put("exito", true);
            
        } catch (Exception e) {
            resultado.put("error", "Error al calcular efectivo disponible: " + e.getMessage());
            resultado.put("exito", false);
        }
        
        return resultado;
    }

    /**
     * Migrar todos los gastos existentes para que tengan el campo pagadoDesdeCaja en false
     */
    public Map<String, Object> migrarCampoPagadoDesdeCaja() {
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            // Obtener todos los gastos
            List<Gasto> todosLosGastos = gastoRepository.findAll();
            int gastosActualizados = 0;
            
            for (Gasto gasto : todosLosGastos) {
                // Forzar que todos los gastos existentes tengan pagadoDesdeCaja = false
                gasto.setPagadoDesdeCaja(false);
                gastoRepository.save(gasto);
                gastosActualizados++;
            }
            
            resultado.put("gastosActualizados", gastosActualizados);
            resultado.put("totalGastos", todosLosGastos.size());
            resultado.put("exito", true);
            
        } catch (Exception e) {
            resultado.put("error", "Error en la migración: " + e.getMessage());
            resultado.put("exito", false);
        }
        
        return resultado;
    }
}
