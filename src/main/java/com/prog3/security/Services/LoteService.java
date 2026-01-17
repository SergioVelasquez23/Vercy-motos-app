package com.prog3.security.Services;

import com.prog3.security.Exception.BusinessException;
import com.prog3.security.Exception.ResourceNotFoundException;
import com.prog3.security.Models.Lote;
import com.prog3.security.Repositories.LoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LoteService {

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private BodegaService bodegaService;

    /**
     * Crear nuevo lote
     */
    @Transactional
    public Lote crearLote(Lote lote) {
        // Validar código único
        if (lote.getCodigo() != null && loteRepository.existsByCodigo(lote.getCodigo())) {
            throw new BusinessException("Ya existe un lote con el código: " + lote.getCodigo());
        }

        // Generar código si no existe
        if (lote.getCodigo() == null || lote.getCodigo().isEmpty()) {
            lote.setCodigo(generarCodigoLote());
        }

        // Validar fechas
        if (lote.getFechaVencimiento() != null
                && lote.getFechaVencimiento().isBefore(LocalDate.now())) {
            throw new BusinessException("La fecha de vencimiento no puede ser anterior a hoy");
        }

        lote.setEstado("ACTIVO");
        lote.setFechaCreacion(LocalDateTime.now());
        lote.setFechaActualizacion(LocalDateTime.now());

        // Actualizar inventario en bodega
        if (lote.getBodegaId() != null && lote.getCantidadInicial() > 0) {
            bodegaService.ajustarStockBodega(lote.getBodegaId(), lote.getItemId(),
                    lote.getTipoItem(), lote.getCantidadInicial(),
                    "Ingreso de lote: " + lote.getCodigo());
        }

        return loteRepository.save(lote);
    }

    /**
     * Actualizar lote
     */
    public Lote actualizarLote(String id, Lote loteActualizado) {
        Lote lote = loteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado: " + id));

        // Validar código único si cambió
        if (loteActualizado.getCodigo() != null
                && !loteActualizado.getCodigo().equals(lote.getCodigo())
                && loteRepository.existsByCodigo(loteActualizado.getCodigo())) {
            throw new BusinessException(
                    "Ya existe un lote con el código: " + loteActualizado.getCodigo());
        }

        lote.setCodigo(loteActualizado.getCodigo());
        lote.setFechaFabricacion(loteActualizado.getFechaFabricacion());
        lote.setFechaVencimiento(loteActualizado.getFechaVencimiento());
        lote.setProveedor(loteActualizado.getProveedor());
        lote.setFactura(loteActualizado.getFactura());
        lote.setCostoUnitario(loteActualizado.getCostoUnitario());
        lote.setObservaciones(loteActualizado.getObservaciones());
        lote.setFechaActualizacion(LocalDateTime.now());

        return loteRepository.save(lote);
    }

    /**
     * Consumir cantidad de un lote específico
     */
    @Transactional
    public Lote consumirLote(String loteId, double cantidad) {
        Lote lote = loteRepository.findById(loteId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado: " + loteId));

        if (lote.getCantidadActual() < cantidad) {
            throw new BusinessException("Stock insuficiente en lote. Disponible: "
                    + lote.getCantidadActual() + ", Solicitado: " + cantidad);
        }

        double nuevaCantidad = lote.getCantidadActual() - cantidad;
        lote.setCantidadActual(nuevaCantidad);
        lote.setFechaActualizacion(LocalDateTime.now());

        // Cambiar estado si se agotó
        if (nuevaCantidad == 0) {
            lote.setEstado("AGOTADO");
        }

        // Actualizar inventario en bodega
        if (lote.getBodegaId() != null) {
            bodegaService.ajustarStockBodega(lote.getBodegaId(), lote.getItemId(),
                    lote.getTipoItem(), -cantidad, "Consumo de lote: " + lote.getCodigo());
        }

        return loteRepository.save(lote);
    }

    /**
     * Consumir cantidad usando FIFO (First In First Out)
     */
    @Transactional
    public List<Map<String, Object>> consumirItemFIFO(String itemId, double cantidadTotal) {
        List<Lote> lotesDisponibles = loteRepository.findLotesByItemIdFIFO(itemId);

        if (lotesDisponibles.isEmpty()) {
            throw new BusinessException("No hay lotes disponibles para el item: " + itemId);
        }

        double cantidadRestante = cantidadTotal;
        List<Map<String, Object>> consumos = new ArrayList<>();

        for (Lote lote : lotesDisponibles) {
            if (cantidadRestante <= 0)
                break;

            double cantidadAConsumir = Math.min(lote.getCantidadActual(), cantidadRestante);

            consumirLote(lote.get_id(), cantidadAConsumir);

            Map<String, Object> consumo = new HashMap<>();
            consumo.put("loteId", lote.get_id());
            consumo.put("codigoLote", lote.getCodigo());
            consumo.put("cantidadConsumida", cantidadAConsumir);
            consumo.put("fechaVencimiento", lote.getFechaVencimiento());
            consumos.add(consumo);

            cantidadRestante -= cantidadAConsumir;
        }

        if (cantidadRestante > 0) {
            throw new BusinessException(
                    "Stock insuficiente. Faltante: " + cantidadRestante + " unidades");
        }

        return consumos;
    }

    /**
     * Obtener lotes próximos a vencer
     */
    public List<Lote> getLotesPorVencer(int dias) {
        LocalDate fechaActual = LocalDate.now();
        LocalDate fechaLimite = fechaActual.plusDays(dias);
        return loteRepository.findLotesPorVencer(fechaActual, fechaLimite);
    }

    /**
     * Obtener lotes vencidos
     */
    public List<Lote> getLotesVencidos() {
        return loteRepository.findLotesVencidos(LocalDate.now());
    }

    /**
     * Marcar lotes vencidos
     */
    @Transactional
    public int marcarLotesVencidos() {
        List<Lote> lotesVencidos = getLotesVencidos();
        int contador = 0;

        for (Lote lote : lotesVencidos) {
            if ("ACTIVO".equals(lote.getEstado())) {
                lote.setEstado("VENCIDO");
                lote.setFechaActualizacion(LocalDateTime.now());
                loteRepository.save(lote);
                contador++;
            }
        }

        return contador;
    }

    /**
     * Obtener todos los lotes
     */
    public List<Lote> getAllLotes() {
        return loteRepository.findAll();
    }

    /**
     * Obtener lote por ID
     */
    public Lote getLoteById(String id) {
        return loteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado: " + id));
    }

    /**
     * Obtener lotes de un item
     */
    public List<Lote> getLotesByItem(String itemId) {
        return loteRepository.findByItemId(itemId);
    }

    /**
     * Obtener lotes activos de un item
     */
    public List<Lote> getLotesActivosByItem(String itemId) {
        return loteRepository.findByItemIdAndEstado(itemId, "ACTIVO");
    }

    /**
     * Obtener lotes de una bodega
     */
    public List<Lote> getLotesByBodega(String bodegaId) {
        return loteRepository.findByBodegaId(bodegaId);
    }

    /**
     * Retirar lote (por vencimiento u otro motivo)
     */
    @Transactional
    public Lote retirarLote(String loteId, String motivo) {
        Lote lote = getLoteById(loteId);

        if (lote.getCantidadActual() > 0 && lote.getBodegaId() != null) {
            // Descontar del inventario
            bodegaService.ajustarStockBodega(lote.getBodegaId(), lote.getItemId(),
                    lote.getTipoItem(), -lote.getCantidadActual(), "Retiro de lote: " + motivo);
        }

        lote.setCantidadActual(0);
        lote.setEstado("RETIRADO");
        lote.setObservaciones(
                (lote.getObservaciones() != null ? lote.getObservaciones() + " | " : "")
                        + "Retirado: " + motivo);
        lote.setFechaActualizacion(LocalDateTime.now());

        return loteRepository.save(lote);
    }

    /**
     * Generar código de lote automático
     */
    private String generarCodigoLote() {
        LocalDate hoy = LocalDate.now();
        String base =
                "LOTE-" + hoy.getYear() + "-" + String.format("%02d", hoy.getMonthValue()) + "-";

        long count = loteRepository.findAll().stream()
                .filter(l -> l.getCodigo() != null && l.getCodigo().startsWith(base)).count();

        return base + String.format("%04d", count + 1);
    }

    /**
     * Obtener resumen de lotes
     */
    public Map<String, Object> getResumenLotes() {
        List<Lote> todosLotes = loteRepository.findAll();

        Map<String, Object> resumen = new HashMap<>();
        resumen.put("totalLotes", todosLotes.size());

        long activos = todosLotes.stream().filter(l -> "ACTIVO".equals(l.getEstado())).count();
        long vencidos = todosLotes.stream().filter(l -> "VENCIDO".equals(l.getEstado())).count();
        long agotados = todosLotes.stream().filter(l -> "AGOTADO".equals(l.getEstado())).count();
        long retirados = todosLotes.stream().filter(l -> "RETIRADO".equals(l.getEstado())).count();

        resumen.put("activos", activos);
        resumen.put("vencidos", vencidos);
        resumen.put("agotados", agotados);
        resumen.put("retirados", retirados);

        List<Lote> porVencer = getLotesPorVencer(30);
        resumen.put("porVencer30Dias", porVencer.size());

        return resumen;
    }
}
