package com.prog3.security.Services;

import com.prog3.security.Models.Factura;
import com.prog3.security.Models.ItemFacturaIngrediente;
import com.prog3.security.Models.Ingrediente;
import com.prog3.security.Models.Inventario;
import com.prog3.security.Models.MovimientoInventario;
import com.prog3.security.Repositories.FacturaRepository;
import com.prog3.security.Repositories.IngredienteRepository;
import com.prog3.security.Repositories.InventarioRepository;
import com.prog3.security.Repositories.MovimientoInventarioRepository;
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

        System.out.println("✅ Factura procesada exitosamente: " + facturaGuardada.get_id());
        return facturaGuardada;
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
}
