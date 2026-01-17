package com.prog3.security.Services;

import com.prog3.security.DTOs.CrearTransferenciaRequest;
import com.prog3.security.DTOs.StockBodegaDTO;
import com.prog3.security.Exception.BusinessException;
import com.prog3.security.Exception.ResourceNotFoundException;
import com.prog3.security.Models.*;
import com.prog3.security.Repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BodegaService {

    @Autowired
    private BodegaRepository bodegaRepository;

    @Autowired
    private InventarioBodegaRepository inventarioBodegaRepository;

    @Autowired
    private TransferenciaBodegaRepository transferenciaBodegaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private IngredienteRepository ingredienteRepository;

    @Autowired
    private MovimientoInventarioRepository movimientoInventarioRepository;

    /**
     * Obtener todas las bodegas
     */
    public List<Bodega> getAllBodegas() {
        return bodegaRepository.findAll();
    }

    /**
     * Obtener bodegas activas
     */
    public List<Bodega> getBodegasActivas() {
        return bodegaRepository.findByActiva(true);
    }

    /**
     * Obtener bodega por ID
     */
    public Bodega getBodegaById(String id) {
        return bodegaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bodega no encontrada: " + id));
    }

    /**
     * Crear nueva bodega
     */
    public Bodega crearBodega(Bodega bodega) {
        // Validar que no exista código duplicado
        if (bodega.getCodigo() != null && bodegaRepository.existsByCodigo(bodega.getCodigo())) {
            throw new BusinessException(
                    "Ya existe una bodega con el código: " + bodega.getCodigo());
        }

        bodega.setFechaCreacion(LocalDateTime.now());
        bodega.setFechaActualizacion(LocalDateTime.now());

        return bodegaRepository.save(bodega);
    }

    /**
     * Actualizar bodega
     */
    public Bodega actualizarBodega(String id, Bodega bodegaActualizada) {
        Bodega bodega = getBodegaById(id);

        // Validar código único si cambió
        if (bodegaActualizada.getCodigo() != null
                && !bodegaActualizada.getCodigo().equals(bodega.getCodigo())
                && bodegaRepository.existsByCodigo(bodegaActualizada.getCodigo())) {
            throw new BusinessException(
                    "Ya existe una bodega con el código: " + bodegaActualizada.getCodigo());
        }

        bodega.setNombre(bodegaActualizada.getNombre());
        bodega.setCodigo(bodegaActualizada.getCodigo());
        bodega.setUbicacion(bodegaActualizada.getUbicacion());
        bodega.setDescripcion(bodegaActualizada.getDescripcion());
        bodega.setTipo(bodegaActualizada.getTipo());
        bodega.setActiva(bodegaActualizada.isActiva());
        bodega.setResponsable(bodegaActualizada.getResponsable());
        bodega.setTelefono(bodegaActualizada.getTelefono());
        bodega.setDireccion(bodegaActualizada.getDireccion());
        bodega.setFechaActualizacion(LocalDateTime.now());

        return bodegaRepository.save(bodega);
    }

    /**
     * Eliminar bodega (solo si no tiene inventario)
     */
    public void eliminarBodega(String id) {
        Bodega bodega = getBodegaById(id);

        // Verificar que no tenga inventario
        List<InventarioBodega> inventario = inventarioBodegaRepository.findByBodegaId(id);
        if (!inventario.isEmpty()) {
            throw new BusinessException(
                    "No se puede eliminar la bodega porque tiene inventario registrado");
        }

        bodegaRepository.delete(bodega);
    }

    /**
     * Obtener inventario de una bodega
     */
    public List<StockBodegaDTO> getInventarioBodega(String bodegaId) {
        Bodega bodega = getBodegaById(bodegaId);
        List<InventarioBodega> inventario = inventarioBodegaRepository.findByBodegaId(bodegaId);

        return inventario.stream().map(inv -> {
            StockBodegaDTO dto = new StockBodegaDTO();
            dto.setBodegaId(bodegaId);
            dto.setBodegaNombre(bodega.getNombre());
            dto.setItemId(inv.getItemId());
            dto.setTipoItem(inv.getTipoItem());
            dto.setStockActual(inv.getStockActual());
            dto.setStockMinimo(inv.getStockMinimo());
            dto.setStockMaximo(inv.getStockMaximo());
            dto.setUbicacionFisica(inv.getUbicacionFisica());
            dto.setStockBajo(inv.getStockActual() <= inv.getStockMinimo());

            // Obtener nombre del item
            if ("producto".equalsIgnoreCase(inv.getTipoItem())) {
                productoRepository.findById(inv.getItemId())
                        .ifPresent(p -> dto.setItemNombre(p.getNombre()));
            } else {
                ingredienteRepository.findById(inv.getItemId())
                        .ifPresent(i -> dto.setItemNombre(i.getNombre()));
            }

            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * Obtener stock de un item en todas las bodegas
     */
    public List<StockBodegaDTO> getStockItemEnBodegas(String itemId, String tipoItem) {
        List<InventarioBodega> inventarios =
                inventarioBodegaRepository.findByItemIdAndTipoItem(itemId, tipoItem);

        return inventarios.stream().map(inv -> {
            StockBodegaDTO dto = new StockBodegaDTO();
            dto.setItemId(itemId);
            dto.setTipoItem(tipoItem);
            dto.setBodegaId(inv.getBodegaId());
            dto.setStockActual(inv.getStockActual());
            dto.setStockMinimo(inv.getStockMinimo());
            dto.setStockMaximo(inv.getStockMaximo());
            dto.setUbicacionFisica(inv.getUbicacionFisica());
            dto.setStockBajo(inv.getStockActual() <= inv.getStockMinimo());

            // Obtener nombre de bodega
            bodegaRepository.findById(inv.getBodegaId())
                    .ifPresent(b -> dto.setBodegaNombre(b.getNombre()));

            // Obtener nombre del item
            if ("producto".equalsIgnoreCase(tipoItem)) {
                productoRepository.findById(itemId)
                        .ifPresent(p -> dto.setItemNombre(p.getNombre()));
            } else {
                ingredienteRepository.findById(itemId)
                        .ifPresent(i -> dto.setItemNombre(i.getNombre()));
            }

            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * Ajustar stock en bodega
     */
    @Transactional
    public InventarioBodega ajustarStockBodega(String bodegaId, String itemId, String tipoItem,
            double cantidad, String motivo) {
        getBodegaById(bodegaId); // Validar que existe

        InventarioBodega inventario = inventarioBodegaRepository
                .findByBodegaIdAndItemId(bodegaId, itemId).orElseGet(() -> {
                    InventarioBodega nuevo = new InventarioBodega();
                    nuevo.setBodegaId(bodegaId);
                    nuevo.setItemId(itemId);
                    nuevo.setTipoItem(tipoItem);
                    nuevo.setStockActual(0);
                    return nuevo;
                });

        double stockAnterior = inventario.getStockActual();
        inventario.setStockActual(stockAnterior + cantidad);
        inventario.setUltimoMovimiento(LocalDateTime.now());
        inventario.setFechaActualizacion(LocalDateTime.now());

        // Registrar movimiento
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProductoId(itemId);
        movimiento.setTipoMovimiento(cantidad > 0 ? "ENTRADA" : "SALIDA");
        movimiento.setCantidadMovimiento(cantidad);
        movimiento.setCantidadAnterior(stockAnterior);
        movimiento.setCantidadNueva(inventario.getStockActual());
        movimiento.setMotivo(
                motivo != null ? motivo : "Ajuste de inventario en bodega - " + tipoItem);
        movimiento.setFecha(LocalDateTime.now());
        movimiento.setObservaciones("Bodega: " + bodegaId + " - Tipo: " + tipoItem);
        movimientoInventarioRepository.save(movimiento);

        return inventarioBodegaRepository.save(inventario);
    }

    /**
     * Crear transferencia entre bodegas
     */
    @Transactional
    public TransferenciaBodega crearTransferencia(CrearTransferenciaRequest request) {
        // Validar bodegas
        Bodega origen = getBodegaById(request.getBodegaOrigenId());
        Bodega destino = getBodegaById(request.getBodegaDestinoId());

        if (!origen.isActiva() || !destino.isActiva()) {
            throw new BusinessException(
                    "Las bodegas deben estar activas para realizar transferencias");
        }

        if (request.getBodegaOrigenId().equals(request.getBodegaDestinoId())) {
            throw new BusinessException("La bodega origen y destino no pueden ser la misma");
        }

        // Validar stock disponible
        for (CrearTransferenciaRequest.ItemTransferencia item : request.getItems()) {
            InventarioBodega inventario = inventarioBodegaRepository
                    .findByBodegaIdAndItemId(request.getBodegaOrigenId(), item.getItemId())
                    .orElseThrow(() -> new BusinessException("No hay inventario del item "
                            + item.getItemId() + " en la bodega origen"));

            if (inventario.getStockActual() < item.getCantidad()) {
                throw new BusinessException("Stock insuficiente para transferir. Disponible: "
                        + inventario.getStockActual() + ", Solicitado: " + item.getCantidad());
            }
        }

        // Crear transferencia
        TransferenciaBodega transferencia = new TransferenciaBodega();
        transferencia.setBodegaOrigenId(request.getBodegaOrigenId());
        transferencia.setBodegaDestinoId(request.getBodegaDestinoId());
        transferencia.setUsuarioId(request.getUsuarioId());
        transferencia.setObservaciones(request.getObservaciones());
        transferencia.setEstado("PENDIENTE");
        transferencia.setFechaSolicitud(LocalDateTime.now());

        // Convertir items
        List<TransferenciaBodega.ItemTransferencia> items =
                request.getItems().stream().map(item -> {
                    TransferenciaBodega.ItemTransferencia itemTrans =
                            new TransferenciaBodega.ItemTransferencia();
                    itemTrans.setItemId(item.getItemId());
                    itemTrans.setTipoItem(item.getTipoItem());
                    itemTrans.setCantidadSolicitada(item.getCantidad());
                    itemTrans.setObservaciones(item.getObservaciones());
                    return itemTrans;
                }).collect(Collectors.toList());

        transferencia.setItems(items);

        return transferenciaBodegaRepository.save(transferencia);
    }

    /**
     * Aprobar y ejecutar transferencia
     */
    @Transactional
    public TransferenciaBodega aprobarTransferencia(String transferenciaId) {
        TransferenciaBodega transferencia = transferenciaBodegaRepository.findById(transferenciaId)
                .orElseThrow(() -> new ResourceNotFoundException("Transferencia no encontrada"));

        if (!"PENDIENTE".equals(transferencia.getEstado())) {
            throw new BusinessException(
                    "Solo se pueden aprobar transferencias en estado PENDIENTE");
        }

        // Descontar de bodega origen y agregar a bodega destino
        for (TransferenciaBodega.ItemTransferencia item : transferencia.getItems()) {
            // Descontar origen
            ajustarStockBodega(transferencia.getBodegaOrigenId(), item.getItemId(),
                    item.getTipoItem(), -item.getCantidadSolicitada(),
                    "Transferencia a bodega destino - Trans: " + transferenciaId);

            // Agregar destino
            ajustarStockBodega(transferencia.getBodegaDestinoId(), item.getItemId(),
                    item.getTipoItem(), item.getCantidadSolicitada(),
                    "Transferencia desde bodega origen - Trans: " + transferenciaId);

            item.setCantidadEnviada(item.getCantidadSolicitada());
            item.setCantidadRecibida(item.getCantidadSolicitada());
        }

        transferencia.setEstado("COMPLETADA");
        transferencia.setFechaEnvio(LocalDateTime.now());
        transferencia.setFechaRecepcion(LocalDateTime.now());
        transferencia.setFechaActualizacion(LocalDateTime.now());

        return transferenciaBodegaRepository.save(transferencia);
    }

    /**
     * Rechazar transferencia
     */
    public TransferenciaBodega rechazarTransferencia(String transferenciaId, String motivo) {
        TransferenciaBodega transferencia = transferenciaBodegaRepository.findById(transferenciaId)
                .orElseThrow(() -> new ResourceNotFoundException("Transferencia no encontrada"));

        if (!"PENDIENTE".equals(transferencia.getEstado())) {
            throw new BusinessException(
                    "Solo se pueden rechazar transferencias en estado PENDIENTE");
        }

        transferencia.setEstado("RECHAZADA");
        transferencia.setMotivoRechazo(motivo);
        transferencia.setFechaActualizacion(LocalDateTime.now());

        return transferenciaBodegaRepository.save(transferencia);
    }

    /**
     * Obtener transferencias de una bodega
     */
    public List<TransferenciaBodega> getTransferenciasByBodega(String bodegaId) {
        List<TransferenciaBodega> transferencias = new ArrayList<>();
        transferencias.addAll(transferenciaBodegaRepository.findByBodegaOrigenId(bodegaId));
        transferencias.addAll(transferenciaBodegaRepository.findByBodegaDestinoId(bodegaId));
        return transferencias;
    }

    /**
     * Obtener items con stock bajo en una bodega
     */
    public List<StockBodegaDTO> getItemsStockBajo(String bodegaId) {
        Bodega bodega = getBodegaById(bodegaId);
        List<InventarioBodega> inventarios =
                inventarioBodegaRepository.findStockBajoByBodega(bodegaId);

        return inventarios.stream().map(inv -> {
            StockBodegaDTO dto = new StockBodegaDTO();
            dto.setBodegaId(bodegaId);
            dto.setBodegaNombre(bodega.getNombre());
            dto.setItemId(inv.getItemId());
            dto.setTipoItem(inv.getTipoItem());
            dto.setStockActual(inv.getStockActual());
            dto.setStockMinimo(inv.getStockMinimo());
            dto.setStockMaximo(inv.getStockMaximo());
            dto.setStockBajo(true);

            // Obtener nombre del item
            if ("producto".equalsIgnoreCase(inv.getTipoItem())) {
                productoRepository.findById(inv.getItemId())
                        .ifPresent(p -> dto.setItemNombre(p.getNombre()));
            } else {
                ingredienteRepository.findById(inv.getItemId())
                        .ifPresent(i -> dto.setItemNombre(i.getNombre()));
            }

            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * Obtener resumen de inventario por bodega
     */
    public Map<String, Object> getResumenInventarioBodega(String bodegaId) {
        Bodega bodega = getBodegaById(bodegaId);
        List<InventarioBodega> inventarios = inventarioBodegaRepository.findByBodegaId(bodegaId);

        Map<String, Object> resumen = new HashMap<>();
        resumen.put("bodegaId", bodegaId);
        resumen.put("bodegaNombre", bodega.getNombre());
        resumen.put("totalItems", inventarios.size());

        long itemsStockBajo = inventarios.stream()
                .filter(inv -> inv.getStockActual() <= inv.getStockMinimo()).count();

        resumen.put("itemsStockBajo", itemsStockBajo);

        long productos = inventarios.stream()
                .filter(inv -> "producto".equalsIgnoreCase(inv.getTipoItem())).count();

        long ingredientes = inventarios.stream()
                .filter(inv -> "ingrediente".equalsIgnoreCase(inv.getTipoItem())).count();

        resumen.put("totalProductos", productos);
        resumen.put("totalIngredientes", ingredientes);

        return resumen;
    }
}
