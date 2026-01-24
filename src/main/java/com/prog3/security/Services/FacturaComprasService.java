package com.prog3.security.Services;

import com.prog3.security.Models.Factura;
import com.prog3.security.Models.ItemFacturaIngrediente;
import com.prog3.security.Models.Ingrediente;
import com.prog3.security.Models.Inventario;
import com.prog3.security.Models.MovimientoInventario;
import com.prog3.security.Models.CuadreCaja;
import com.prog3.security.Repositories.FacturaRepository;
import com.prog3.security.Repositories.IngredienteRepository;
import com.prog3.security.Repositories.InventarioRepository;
import com.prog3.security.Repositories.MovimientoInventarioRepository;
import com.prog3.security.Repositories.CuadreCajaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para manejar facturas de compras de ingredientes
 */
@Service
public class FacturaComprasService {

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private IngredienteRepository ingredienteRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private MovimientoInventarioRepository movimientoInventarioRepository;

    @Autowired
    private CuadreCajaRepository cuadreCajaRepository;

    /**
     * Procesa una factura de compras, aumentando el stock de ingredientes
     * descontables
     *
     * @param factura La factura a procesar
     * @return La factura procesada
     */
    public Factura procesarFacturaCompras(Factura factura) {
        System.out.println("🧾 Procesando factura de compras: " + factura.getNumero());

        // Primero guardar la factura
        Factura facturaGuardada = facturaRepository.save(factura);

        // Procesar cada item de ingrediente
        for (ItemFacturaIngrediente item : factura.getItemsIngredientes()) {
            procesarItemIngrediente(item, facturaGuardada.get_id(), factura.getRegistradoPor());
        }

        // ✅ Si la factura se paga desde caja, descontar del efectivo esperado
        if (factura.isPagadoDesdeCaja()) {
            descontarPagoDeCaja(factura);
        }

        System.out.println("✅ Factura procesada exitosamente: " + facturaGuardada.get_id());
        return facturaGuardada;
    }

    /**
     * Descuenta el monto de la factura del efectivo esperado en caja Solo descuenta si fue pagada
     * en efectivo
     */
    private void descontarPagoDeCaja(Factura factura) {
        try {
            System.out.println("💸 Descontando pago de caja para factura: " + factura.getNumero()
                    + " - $" + factura.getTotal());

            // Solo descontar efectivo si fue pagado EN EFECTIVO
            if (!"efectivo".equalsIgnoreCase(factura.getMedioPago())) {
                System.out.println("ℹ️ Factura pagada por " + factura.getMedioPago() + ": $"
                        + factura.getTotal() + " - No afecta efectivo esperado");
                return;
            }

            // Buscar el cuadre de caja activo
            List<CuadreCaja> cuadresAbiertos = cuadreCajaRepository.findAll().stream()
                    .filter(c -> !c.isCerrada())
                    .sorted((c1, c2) -> c2.getFechaApertura().compareTo(c1.getFechaApertura()))
                    .toList();

            if (!cuadresAbiertos.isEmpty()) {
                CuadreCaja cuadreActivo = cuadresAbiertos.get(0);
                double efectivoActual = cuadreActivo.getEfectivoEsperado();
                double nuevoEfectivo = efectivoActual - factura.getTotal();

                cuadreActivo.setEfectivoEsperado(nuevoEfectivo);
                cuadreCajaRepository.save(cuadreActivo);

                System.out.println("✅ Efectivo EN EFECTIVO descontado de caja: $"
                        + factura.getTotal() + ". Efectivo esperado antes: $" + efectivoActual
                        + ", después: $" + nuevoEfectivo);
            } else {
                System.err.println(
                        "⚠️ No se encontró un cuadre de caja abierto para descontar el dinero");
            }

        } catch (Exception e) {
            System.err.println("❌ Error al descontar pago de caja: " + e.getMessage());
        }
    }

    /**
     * Procesa un item de ingrediente individual
     */
    private void procesarItemIngrediente(ItemFacturaIngrediente item, String facturaId, String usuario) {
        try {
            // Obtener el ingrediente
            Optional<Ingrediente> ingredienteOpt = ingredienteRepository.findById(item.getIngredienteId());
            if (!ingredienteOpt.isPresent()) {
                System.err.println("⚠️ Ingrediente no encontrado: " + item.getIngredienteId());
                return;
            }

            Ingrediente ingrediente = ingredienteOpt.get();

            // Verificar si el ingrediente es descontable
            if (item.isDescontable() && ingrediente.isDescontable()) {
                // Aumentar el stock del ingrediente
                double stockAnterior = ingrediente.getStockActual() != null ? ingrediente.getStockActual() : 0.0;
                double nuevoStock = stockAnterior + item.getCantidad();

                ingrediente.setStockActual(nuevoStock);
                ingredienteRepository.save(ingrediente);

                // Actualizar o crear registro de inventario
                actualizarInventario(ingrediente, item, stockAnterior, nuevoStock, facturaId, usuario);

                System.out.println("📈 Stock actualizado para " + ingrediente.getNombre()
                        + ": " + stockAnterior + " → " + nuevoStock
                        + " (+" + item.getCantidad() + " " + item.getUnidad() + ")");
            } else {
                System.out.println("ℹ️ Ingrediente registrado sin actualizar stock: " + ingrediente.getNombre()
                        + " (" + item.getCantidad() + " " + item.getUnidad()
                        + ") - Descontable: " + item.isDescontable());
            }

        } catch (Exception e) {
            System.err.println("❌ Error al procesar ingrediente " + item.getIngredienteId() + ": " + e.getMessage());
        }
    }

    /**
     * Actualiza o crea el registro de inventario para un ingrediente
     */
    private void actualizarInventario(Ingrediente ingrediente, ItemFacturaIngrediente item,
            double stockAnterior, double nuevoStock,
            String facturaId, String usuario) {

        // Buscar inventario existente
        Inventario inventario = inventarioRepository.findByProductoId(ingrediente.get_id());

        if (inventario == null) {
            // Crear nuevo registro de inventario
            inventario = new Inventario();
            inventario.setProductoId(ingrediente.get_id());
            inventario.setProductoNombre(ingrediente.getNombre());
            inventario.setCategoria("Ingrediente");
            inventario.setUnidadMedida(ingrediente.getUnidad());
            inventario.setCantidadMinima(ingrediente.getStockMinimo() != null ? ingrediente.getStockMinimo() : 0.0);
            inventario.setEstado("activo");
        }

        // Actualizar stock y costos
        inventario.setCantidadActual(nuevoStock);

        // Calcular costo promedio ponderado
        if (item.getPrecioUnitario() > 0) {
            double costoTotalAnterior = inventario.getCostoUnitario() * stockAnterior;
            double costoTotalNuevo = item.getPrecioUnitario() * item.getCantidad();
            double costoTotalFinal = costoTotalAnterior + costoTotalNuevo;

            if (nuevoStock > 0) {
                inventario.setCostoUnitario(costoTotalFinal / nuevoStock);
            }
            inventario.setCostoTotal(costoTotalFinal);
        }

        inventario.setFechaUltimaActualizacion(LocalDateTime.now());
        inventarioRepository.save(inventario);

        // Registrar movimiento de inventario
        registrarMovimientoInventario(inventario, item, stockAnterior, nuevoStock, facturaId, usuario);
    }

    /**
     * Registra el movimiento de inventario por la compra
     */
    private void registrarMovimientoInventario(Inventario inventario, ItemFacturaIngrediente item,
            double stockAnterior, double nuevoStock,
            String facturaId, String usuario) {

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setInventarioId(inventario.get_id());
        movimiento.setProductoId(inventario.getProductoId());
        movimiento.setProductoNombre(inventario.getProductoNombre());
        movimiento.setTipoMovimiento("entrada");
        movimiento.setCantidadAnterior(stockAnterior);
        movimiento.setCantidadMovimiento(item.getCantidad());
        movimiento.setCantidadNueva(nuevoStock);
        movimiento.setMotivo("Compra de ingredientes");
        movimiento.setReferencia(facturaId);
        movimiento.setResponsable(usuario != null ? usuario : "sistema");
        movimiento.setFecha(LocalDateTime.now());
        movimiento.setCostoUnitario(item.getPrecioUnitario());
        movimiento.setCostoTotal(item.getPrecioTotal());
        movimiento.setObservaciones("Factura de compras - " + item.getObservaciones());

        movimientoInventarioRepository.save(movimiento);
    }

    /**
     * Revierte el stock de los ingredientes de una factura. Se usa al actualizar una factura para
     * primero revertir el stock anterior y luego aplicar el nuevo stock.
     */
    public void revertirStockFactura(Factura factura) {
        System.out.println("🔄 Revirtiendo stock para factura: " + factura.getNumero());
        revertirCambiosInventario(factura);
    }

    /**
     * Genera un número de factura único
     */
    public String generarNumeroFactura() {
        long timestamp = System.currentTimeMillis();
        return "COMP-" + String.valueOf(timestamp).substring(6);
    }

    /**
     * Obtiene todas las facturas de compras
     */
    public List<Factura> obtenerTodasLasFacturas() {
        return facturaRepository.findAll();
    }

    /**
     * Busca una factura por ID
     */
    public Optional<Factura> buscarFacturaPorId(String id) {
        return facturaRepository.findById(id);
    }

    /**
     * Busca facturas por proveedor
     */
    public List<Factura> buscarFacturasPorProveedor(String proveedorNit) {
        // Nota: necesitarás agregar este método al repository
        return facturaRepository.findAll().stream()
                .filter(f -> f.getProveedorNit() != null && f.getProveedorNit().equals(proveedorNit))
                .toList();
    }

    /**
     * Elimina una factura de compras y revierte todos los cambios realizados
     */
    public boolean eliminarFacturaCompra(String facturaId) {
        try {
            System.out.println("🗑️ Iniciando eliminación de factura de compra: " + facturaId);

            // Buscar la factura
            Optional<Factura> facturaOpt = facturaRepository.findById(facturaId);
            if (!facturaOpt.isPresent()) {
                System.err.println("❌ Factura no encontrada: " + facturaId);
                return false;
            }

            Factura factura = facturaOpt.get();
            
            // Verificar que es una factura de compra
            if (!"compra".equals(factura.getTipoFactura())) {
                throw new RuntimeException("La factura no es de tipo compra");
            }

            // 1. Revertir los cambios en el inventario
            revertirCambiosInventario(factura);

            // 2. Si la factura fue pagada desde caja, devolver el dinero
            if (factura.isPagadoDesdeCaja()) {
                revertirPagoEnCaja(factura);
            }

            // 3. Eliminar los movimientos de inventario relacionados
            eliminarMovimientosInventario(facturaId);

            // 4. Finalmente eliminar la factura
            facturaRepository.deleteById(facturaId);

            System.out.println("✅ Factura de compra eliminada exitosamente: " + facturaId);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Error al eliminar factura de compra: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Revierte los cambios realizados en el inventario por la factura de compra
     */
    private void revertirCambiosInventario(Factura factura) {
        System.out.println("📦 Revirtiendo cambios en inventario para factura: " + factura.getNumero());

        for (ItemFacturaIngrediente item : factura.getItemsIngredientes()) {
            try {
                // Buscar el ingrediente
                Optional<Ingrediente> ingredienteOpt = ingredienteRepository.findById(item.getIngredienteId());
                if (!ingredienteOpt.isPresent()) {
                    System.err.println("⚠️ Ingrediente no encontrado: " + item.getIngredienteId());
                    continue;
                }

                Ingrediente ingrediente = ingredienteOpt.get();
                
                // Solo procesar ingredientes descontables
                if (!ingrediente.isDescontable()) {
                    System.out.println("ℹ️ Saltando ingrediente no descontable: " + ingrediente.getNombre());
                    continue;
                }

                // Revertir el stock del ingrediente
                double stockAnterior = ingrediente.getStockActual() != null ? ingrediente.getStockActual() : 0.0;
                double nuevaCantidad = stockAnterior - item.getCantidad(); // Descontar lo que se había agregado

                // Validar que no quede negativo
                if (nuevaCantidad < 0) {
                    System.err.println("⚠️ Warning: El stock quedaría negativo para " + ingrediente.getNombre() + 
                                      ". Stock actual: " + stockAnterior + ", cantidad a descontar: " + item.getCantidad());
                    nuevaCantidad = 0; // No permitir stock negativo
                }

                ingrediente.setStockActual(nuevaCantidad);
                ingredienteRepository.save(ingrediente);

                // Buscar el inventario del ingrediente para crear el movimiento de reversión
                Inventario inventario = inventarioRepository.findByProductoId(ingrediente.get_id());
                if (inventario != null) {
                    // Actualizar también el inventario
                    inventario.setCantidadActual(nuevaCantidad);
                    inventarioRepository.save(inventario);
                    
                    // Crear movimiento de inventario para registrar la reversión
                    crearMovimientoReversion(inventario, stockAnterior, nuevaCantidad, item, factura.get_id());
                } else {
                    System.err.println("⚠️ Inventario no encontrado para ingrediente: " + ingrediente.getNombre());
                }

                System.out.println("📉 Stock revertido para " + ingrediente.getNombre() + 
                                  ": " + stockAnterior + " → " + nuevaCantidad);

            } catch (Exception e) {
                System.err.println("❌ Error al revertir inventario para item: " + item.getIngredienteId() + " - " + e.getMessage());
            }
        }
    }

    /**
     * Revierte el pago en caja si la factura fue pagada desde allí
     */
    private void revertirPagoEnCaja(Factura factura) {
        try {
            System.out.println("💰 Revirtiendo pago en caja para factura: " + factura.getNumero() + " - $" + factura.getTotal());

            // Solo devolver efectivo si fue pagado EN EFECTIVO
            if (!"efectivo".equalsIgnoreCase(factura.getMedioPago())) {
                System.out.println("ℹ️ Factura pagada por TRANSFERENCIA: $" + factura.getTotal() + 
                                 " - No afecta efectivo esperado");
                return;
            }

            // Buscar el cuadre de caja activo más reciente
            // Nota: Podrías necesitar ajustar esta lógica según cómo manejes los cuadres
            List<CuadreCaja> cuadresAbiertos = cuadreCajaRepository.findAll().stream()
                .filter(c -> !c.isCerrada())
                .sorted((c1, c2) -> c2.getFechaApertura().compareTo(c1.getFechaApertura()))
                .toList();

            if (!cuadresAbiertos.isEmpty()) {
                CuadreCaja cuadreActivo = cuadresAbiertos.get(0);
                double efectivoActual = cuadreActivo.getEfectivoEsperado();
                double nuevoEfectivo = efectivoActual + factura.getTotal();

                cuadreActivo.setEfectivoEsperado(nuevoEfectivo);
                cuadreCajaRepository.save(cuadreActivo);

                System.out.println("✅ Efectivo EN EFECTIVO devuelto a caja: $" + factura.getTotal() + 
                                  ". Efectivo esperado antes: $" + efectivoActual + 
                                  ", después: $" + nuevoEfectivo);
            } else {
                System.err.println("⚠️ No se encontró un cuadre de caja abierto para devolver el dinero");
            }

        } catch (Exception e) {
            System.err.println("❌ Error al revertir pago en caja: " + e.getMessage());
        }
    }

    /**
     * Elimina los movimientos de inventario relacionados con la factura
     */
    private void eliminarMovimientosInventario(String facturaId) {
        try {
            List<MovimientoInventario> movimientos = movimientoInventarioRepository.findByReferencia(facturaId);
            for (MovimientoInventario movimiento : movimientos) {
                movimientoInventarioRepository.delete(movimiento);
            }
            System.out.println("🗑️ Eliminados " + movimientos.size() + " movimientos de inventario");
        } catch (Exception e) {
            System.err.println("❌ Error al eliminar movimientos de inventario: " + e.getMessage());
        }
    }

    /**
     * Crea un movimiento de inventario para registrar la reversión
     */
    private void crearMovimientoReversion(Inventario inventario, double stockAnterior, double nuevoStock, 
                                         ItemFacturaIngrediente item, String facturaId) {
        try {
            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setInventarioId(inventario.get_id());
            movimiento.setProductoId(inventario.getProductoId());
            movimiento.setProductoNombre(inventario.getProductoNombre());
            movimiento.setTipoMovimiento("salida");
            movimiento.setCantidadAnterior(stockAnterior);
            movimiento.setCantidadMovimiento(item.getCantidad());
            movimiento.setCantidadNueva(nuevoStock);
            movimiento.setMotivo("Reversión por eliminación de factura de compra");
            movimiento.setReferencia("REV-" + facturaId);
            movimiento.setResponsable("sistema");
            movimiento.setFecha(LocalDateTime.now());
            movimiento.setCostoUnitario(item.getPrecioUnitario());
            movimiento.setCostoTotal(item.getPrecioTotal());
            movimiento.setObservaciones("Reversión automática - Eliminación de factura de compra");

            movimientoInventarioRepository.save(movimiento);
        } catch (Exception e) {
            System.err.println("❌ Error al crear movimiento de reversión: " + e.getMessage());
        }
    }
}
