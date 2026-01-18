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
import java.util.Optional;
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

    /**
     * 📊 INVENTARIO CONSOLIDADO Muestra todos los productos con su stock en cada bodega
     */
    public Map<String, Object> getInventarioConsolidado(String tipoItem) {
        Map<String, Object> resultado = new HashMap<>();
        List<Map<String, Object>> itemsConsolidados = new ArrayList<>();
        List<Bodega> bodegasActivas = getBodegasActivas();

        // Obtener todos los items según el tipo
        List<?> items;
        if ("producto".equalsIgnoreCase(tipoItem)) {
            items = productoRepository.findAll();
        } else {
            items = ingredienteRepository.findAll();
        }

        for (Object item : items) {
            Map<String, Object> itemConsolidado = new HashMap<>();
            String itemId;
            String itemNombre;

            if (item instanceof Producto) {
                Producto p = (Producto) item;
                itemId = p.get_id();
                itemNombre = p.getNombre();
                itemConsolidado.put("precio", p.getPrecio());
                itemConsolidado.put("costo", p.getCosto());
                itemConsolidado.put("categoriaId", p.getCategoriaId());
            } else {
                Ingrediente i = (Ingrediente) item;
                itemId = i.get_id();
                itemNombre = i.getNombre();
                itemConsolidado.put("unidad", i.getUnidad());
            }

            itemConsolidado.put("itemId", itemId);
            itemConsolidado.put("nombre", itemNombre);
            itemConsolidado.put("tipoItem", tipoItem);

            // Stock por bodega
            List<Map<String, Object>> stockPorBodega = new ArrayList<>();
            double stockTotal = 0;

            for (Bodega bodega : bodegasActivas) {
                Map<String, Object> stockBodega = new HashMap<>();
                stockBodega.put("bodegaId", bodega.get_id());
                stockBodega.put("bodegaNombre", bodega.getNombre());
                stockBodega.put("bodegaTipo", bodega.getTipo());

                Optional<InventarioBodega> invOpt =
                        inventarioBodegaRepository.findByBodegaIdAndItemId(bodega.get_id(), itemId);

                if (invOpt.isPresent()) {
                    InventarioBodega inv = invOpt.get();
                    stockBodega.put("stockActual", inv.getStockActual());
                    stockBodega.put("stockMinimo", inv.getStockMinimo());
                    stockBodega.put("stockMaximo", inv.getStockMaximo());
                    stockBodega.put("ubicacionFisica", inv.getUbicacionFisica());
                    stockBodega.put("stockBajo", inv.getStockActual() <= inv.getStockMinimo());
                    stockTotal += inv.getStockActual();
                } else {
                    stockBodega.put("stockActual", 0);
                    stockBodega.put("stockMinimo", 0);
                    stockBodega.put("stockMaximo", 0);
                    stockBodega.put("ubicacionFisica", null);
                    stockBodega.put("stockBajo", false);
                }

                stockPorBodega.add(stockBodega);
            }

            itemConsolidado.put("stockPorBodega", stockPorBodega);
            itemConsolidado.put("stockTotal", stockTotal);
            itemConsolidado.put("enAlgunaBodega", stockTotal > 0);

            itemsConsolidados.add(itemConsolidado);
        }

        resultado.put("items", itemsConsolidados);
        resultado.put("totalItems", itemsConsolidados.size());
        resultado.put("bodegas", bodegasActivas.stream().map(b -> {
            Map<String, Object> bodegaInfo = new HashMap<>();
            bodegaInfo.put("_id", b.get_id());
            bodegaInfo.put("nombre", b.getNombre());
            bodegaInfo.put("tipo", b.getTipo());
            return bodegaInfo;
        }).collect(Collectors.toList()));

        return resultado;
    }

    /**
     * 📦 ASIGNAR PRODUCTOS MASIVAMENTE A UNA BODEGA
     */
    @Transactional
    public Map<String, Object> asignarProductosMasivo(String bodegaId,
            List<Map<String, Object>> productos) {
        getBodegaById(bodegaId); // Validar que existe

        List<InventarioBodega> asignados = new ArrayList<>();
        List<Map<String, Object>> errores = new ArrayList<>();

        for (Map<String, Object> prod : productos) {
            try {
                String itemId = (String) prod.get("itemId");
                String tipoItem =
                        prod.get("tipoItem") != null ? (String) prod.get("tipoItem") : "producto";
                double cantidad =
                        prod.get("cantidad") != null ? ((Number) prod.get("cantidad")).doubleValue()
                                : 0;
                double stockMinimo = prod.get("stockMinimo") != null
                        ? ((Number) prod.get("stockMinimo")).doubleValue()
                        : 0;
                double stockMaximo = prod.get("stockMaximo") != null
                        ? ((Number) prod.get("stockMaximo")).doubleValue()
                        : 0;
                String ubicacionFisica = (String) prod.get("ubicacionFisica");

                // Verificar si ya existe
                Optional<InventarioBodega> existente =
                        inventarioBodegaRepository.findByBodegaIdAndItemId(bodegaId, itemId);

                InventarioBodega inventario;
                if (existente.isPresent()) {
                    inventario = existente.get();
                    inventario.setStockActual(inventario.getStockActual() + cantidad);
                } else {
                    inventario = new InventarioBodega();
                    inventario.setBodegaId(bodegaId);
                    inventario.setItemId(itemId);
                    inventario.setTipoItem(tipoItem);
                    inventario.setStockActual(cantidad);
                }

                inventario.setStockMinimo(stockMinimo);
                inventario.setStockMaximo(stockMaximo);
                if (ubicacionFisica != null) {
                    inventario.setUbicacionFisica(ubicacionFisica);
                }
                inventario.setUltimoMovimiento(LocalDateTime.now());
                inventario.setFechaActualizacion(LocalDateTime.now());

                asignados.add(inventarioBodegaRepository.save(inventario));

                // Registrar movimiento si hay cantidad
                if (cantidad != 0) {
                    MovimientoInventario movimiento = new MovimientoInventario();
                    movimiento.setProductoId(itemId);
                    movimiento.setTipoMovimiento(cantidad > 0 ? "ENTRADA" : "SALIDA");
                    movimiento.setCantidadMovimiento(cantidad);
                    movimiento.setCantidadAnterior(
                            existente.isPresent() ? existente.get().getStockActual() : 0);
                    movimiento.setCantidadNueva(inventario.getStockActual());
                    movimiento.setMotivo("Asignación masiva a bodega");
                    movimiento.setFecha(LocalDateTime.now());
                    movimiento.setObservaciones("Bodega: " + bodegaId);
                    movimientoInventarioRepository.save(movimiento);
                }

            } catch (Exception e) {
                Map<String, Object> error = new HashMap<>();
                error.put("item", prod);
                error.put("error", e.getMessage());
                errores.add(error);
            }
        }

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("success", true);
        resultado.put("asignados", asignados.size());
        resultado.put("errores", errores);
        resultado.put("inventarios", asignados);

        return resultado;
    }

    /**
     * 📈 RESUMEN GENERAL DE TODAS LAS BODEGAS
     */
    public Map<String, Object> getResumenGeneralBodegas() {
        List<Bodega> bodegasActivas = getBodegasActivas();
        List<Map<String, Object>> resumenBodegas = new ArrayList<>();

        int totalProductosGlobal = 0;
        int totalItemsStockBajoGlobal = 0;
        double valorInventarioGlobal = 0;

        for (Bodega bodega : bodegasActivas) {
            Map<String, Object> resumenBodega = getResumenInventarioBodega(bodega.get_id());
            resumenBodega.put("bodegaTipo", bodega.getTipo());
            resumenBodega.put("bodegaUbicacion", bodega.getUbicacion());

            // Calcular valor del inventario (solo productos)
            List<InventarioBodega> inventarios = inventarioBodegaRepository
                    .findByBodegaIdAndTipoItem(bodega.get_id(), "producto");

            double valorBodega = 0;
            for (InventarioBodega inv : inventarios) {
                productoRepository.findById(inv.getItemId()).ifPresent(p -> {
                    // El valor es mutable, necesitamos usar un array
                });
                Optional<Producto> prodOpt = productoRepository.findById(inv.getItemId());
                if (prodOpt.isPresent()) {
                    valorBodega += inv.getStockActual() * prodOpt.get().getCosto();
                }
            }
            resumenBodega.put("valorInventario", valorBodega);
            valorInventarioGlobal += valorBodega;

            totalProductosGlobal += ((Number) resumenBodega.get("totalItems")).intValue();
            totalItemsStockBajoGlobal += ((Number) resumenBodega.get("itemsStockBajo")).intValue();

            resumenBodegas.add(resumenBodega);
        }

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("totalBodegas", bodegasActivas.size());
        resultado.put("totalProductosGlobal", totalProductosGlobal);
        resultado.put("totalItemsStockBajo", totalItemsStockBajoGlobal);
        resultado.put("valorInventarioTotal", valorInventarioGlobal);
        resultado.put("bodegas", resumenBodegas);

        return resultado;
    }

    /**
     * 🔄 MOVER TODO EL STOCK DE UN PRODUCTO A OTRA BODEGA
     */
    @Transactional
    public Map<String, Object> moverTodoStock(String itemId, String tipoItem, String bodegaOrigenId,
            String bodegaDestinoId, String motivo) {

        // Validar bodegas
        getBodegaById(bodegaOrigenId);
        getBodegaById(bodegaDestinoId);

        if (bodegaOrigenId.equals(bodegaDestinoId)) {
            throw new BusinessException("La bodega origen y destino no pueden ser la misma");
        }

        // Obtener stock en origen
        InventarioBodega origen =
                inventarioBodegaRepository.findByBodegaIdAndItemId(bodegaOrigenId, itemId)
                        .orElseThrow(() -> new BusinessException(
                                "No hay inventario de este item en la bodega origen"));

        double cantidadAMover = origen.getStockActual();
        if (cantidadAMover <= 0) {
            throw new BusinessException("No hay stock disponible para mover");
        }

        // Mover stock
        ajustarStockBodega(bodegaOrigenId, itemId, tipoItem, -cantidadAMover,
                motivo != null ? motivo : "Movimiento total a otra bodega");
        ajustarStockBodega(bodegaDestinoId, itemId, tipoItem, cantidadAMover,
                motivo != null ? motivo : "Recepción de movimiento total");

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("success", true);
        resultado.put("cantidadMovida", cantidadAMover);
        resultado.put("bodegaOrigenId", bodegaOrigenId);
        resultado.put("bodegaDestinoId", bodegaDestinoId);
        resultado.put("itemId", itemId);

        return resultado;
    }

    /**
     * 📋 PRODUCTOS SIN ASIGNAR A NINGUNA BODEGA
     */
    public List<Map<String, Object>> getProductosSinBodega() {
        List<Producto> todosProductos = productoRepository.findAll();
        List<Map<String, Object>> productosSinBodega = new ArrayList<>();

        for (Producto producto : todosProductos) {
            List<InventarioBodega> inventarios = inventarioBodegaRepository
                    .findByItemIdAndTipoItem(producto.get_id(), "producto");

            // Si no tiene inventario en ninguna bodega, o todos tienen stock 0
            boolean tieneStock = inventarios.stream().anyMatch(inv -> inv.getStockActual() > 0);

            if (!tieneStock) {
                Map<String, Object> prod = new HashMap<>();
                prod.put("_id", producto.get_id());
                prod.put("nombre", producto.getNombre());
                prod.put("precio", producto.getPrecio());
                prod.put("costo", producto.getCosto());
                prod.put("categoriaId", producto.getCategoriaId());
                prod.put("estado", producto.getEstado());
                productosSinBodega.add(prod);
            }
        }

        return productosSinBodega;
    }
}
