package com.prog3.security.Services;

import com.prog3.security.Models.Cotizacion;
import com.prog3.security.Models.ItemCotizacion;
import com.prog3.security.Repositories.CotizacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

/**
 * Servicio para manejar la lógica de negocio de Cotizaciones
 */
@Service
public class CotizacionService {

    @Autowired
    private CotizacionRepository cotizacionRepository;

    /**
     * Obtener todas las cotizaciones ordenadas por fecha descendente
     */
    public List<Cotizacion> obtenerTodasLasCotizaciones() {
        return cotizacionRepository.findAllByOrderByFechaDesc();
    }

    /**
     * Obtener cotización por ID
     */
    public Optional<Cotizacion> obtenerCotizacionPorId(String id) {
        return cotizacionRepository.findById(id);
    }

    /**
     * Obtener cotización por número
     */
    public Optional<Cotizacion> obtenerCotizacionPorNumero(String numeroCotizacion) {
        return cotizacionRepository.findByNumeroCotizacion(numeroCotizacion);
    }

    /**
     * Crear nueva cotización
     */
    public Cotizacion crearCotizacion(Cotizacion cotizacion, String usuarioId) {
        // Generar número de cotización si no existe
        if (cotizacion.getNumeroCotizacion() == null || cotizacion.getNumeroCotizacion().isEmpty()) {
            cotizacion.setNumeroCotizacion(generarNumeroCotizacion());
        }
        
        // Establecer información de creación
        cotizacion.setFecha(LocalDateTime.now());
        cotizacion.setCreadoPor(usuarioId);
        cotizacion.setEstado("activa");
        
        // Calcular totales
        cotizacion.calcularTotales();
        
        return cotizacionRepository.save(cotizacion);
    }

    /**
     * Actualizar cotización existente
     */
    public Cotizacion actualizarCotizacion(String id, Cotizacion cotizacionActualizada, String usuarioId) {
        Optional<Cotizacion> cotizacionOpt = cotizacionRepository.findById(id);
        
        if (!cotizacionOpt.isPresent()) {
            throw new RuntimeException("Cotización no encontrada");
        }
        
        Cotizacion cotizacionExistente = cotizacionOpt.get();
        
        // Verificar que no esté convertida
        if ("convertida".equals(cotizacionExistente.getEstado())) {
            throw new RuntimeException("No se puede modificar una cotización convertida a factura");
        }
        
        // Actualizar campos
        cotizacionExistente.setClienteId(cotizacionActualizada.getClienteId());
        cotizacionExistente.setClienteNombre(cotizacionActualizada.getClienteNombre());
        cotizacionExistente.setClienteTelefono(cotizacionActualizada.getClienteTelefono());
        cotizacionExistente.setClienteEmail(cotizacionActualizada.getClienteEmail());
        cotizacionExistente.setFechaVencimiento(cotizacionActualizada.getFechaVencimiento());
        cotizacionExistente.setItems(cotizacionActualizada.getItems());
        cotizacionExistente.setDescripcion(cotizacionActualizada.getDescripcion());
        cotizacionExistente.setArchivosAdjuntos(cotizacionActualizada.getArchivosAdjuntos());
        cotizacionExistente.setSoportesPago(cotizacionActualizada.getSoportesPago());
        cotizacionExistente.setRetencion(cotizacionActualizada.getRetencion());
        cotizacionExistente.setReteIVA(cotizacionActualizada.getReteIVA());
        cotizacionExistente.setReteICA(cotizacionActualizada.getReteICA());
        cotizacionExistente.setTipoDescuentoGeneral(cotizacionActualizada.getTipoDescuentoGeneral());
        cotizacionExistente.setDescuentoGeneral(cotizacionActualizada.getDescuentoGeneral());
        
        // Actualizar tracking
        cotizacionExistente.setModificadoPor(usuarioId);
        cotizacionExistente.setFechaModificacion(LocalDateTime.now());
        
        // Recalcular totales
        cotizacionExistente.calcularTotales();
        
        return cotizacionRepository.save(cotizacionExistente);
    }

    /**
     * Calcular totales antes de guardar (útil para preview)
     */
    public Map<String, Object> calcularTotalesSinGuardar(Cotizacion cotizacion) {
        cotizacion.calcularTotales();
        
        Map<String, Object> totales = new HashMap<>();
        totales.put("subtotal", cotizacion.getSubtotal());
        totales.put("totalImpuestos", cotizacion.getTotalImpuestos());
        totales.put("totalDescuentos", cotizacion.getTotalDescuentos());
        totales.put("totalRetenciones", cotizacion.getTotalRetenciones());
        totales.put("totalFinal", cotizacion.getTotalFinal());
        
        return totales;
    }

    /**
     * Eliminar cotización
     */
    public boolean eliminarCotizacion(String id) {
        Optional<Cotizacion> cotizacionOpt = cotizacionRepository.findById(id);
        
        if (!cotizacionOpt.isPresent()) {
            return false;
        }
        
        Cotizacion cotizacion = cotizacionOpt.get();
        
        // No permitir eliminar cotizaciones convertidas
        if ("convertida".equals(cotizacion.getEstado())) {
            throw new RuntimeException("No se puede eliminar una cotización convertida a factura");
        }
        
        cotizacionRepository.deleteById(id);
        return true;
    }

    /**
     * Obtener cotizaciones por cliente
     */
    public List<Cotizacion> obtenerCotizacionesPorCliente(String clienteId) {
        return cotizacionRepository.findByClienteIdOrderByFechaDesc(clienteId);
    }

    /**
     * Obtener cotizaciones por estado
     */
    public List<Cotizacion> obtenerCotizacionesPorEstado(String estado) {
        return cotizacionRepository.findByEstado(estado);
    }

    /**
     * Aceptar cotización
     */
    public Cotizacion aceptarCotizacion(String id, String usuarioId) {
        Optional<Cotizacion> cotizacionOpt = cotizacionRepository.findById(id);
        
        if (!cotizacionOpt.isPresent()) {
            throw new RuntimeException("Cotización no encontrada");
        }
        
        Cotizacion cotizacion = cotizacionOpt.get();
        cotizacion.aceptar(usuarioId);
        
        return cotizacionRepository.save(cotizacion);
    }

    /**
     * Rechazar cotización
     */
    public Cotizacion rechazarCotizacion(String id, String usuarioId) {
        Optional<Cotizacion> cotizacionOpt = cotizacionRepository.findById(id);
        
        if (!cotizacionOpt.isPresent()) {
            throw new RuntimeException("Cotización no encontrada");
        }
        
        Cotizacion cotizacion = cotizacionOpt.get();
        cotizacion.rechazar(usuarioId);
        
        return cotizacionRepository.save(cotizacion);
    }

    /**
     * Convertir cotización a factura
     */
    public Cotizacion convertirAFactura(String id, String facturaId, String usuarioId) {
        Optional<Cotizacion> cotizacionOpt = cotizacionRepository.findById(id);
        
        if (!cotizacionOpt.isPresent()) {
            throw new RuntimeException("Cotización no encontrada");
        }
        
        Cotizacion cotizacion = cotizacionOpt.get();
        
        if (!"aceptada".equals(cotizacion.getEstado()) && !"activa".equals(cotizacion.getEstado())) {
            throw new RuntimeException("Solo se pueden convertir cotizaciones activas o aceptadas");
        }
        
        cotizacion.convertirAFactura(facturaId, usuarioId);
        
        return cotizacionRepository.save(cotizacion);
    }

    /**
     * Actualizar cotizaciones vencidas
     */
    public int marcarCotizacionesVencidas() {
        List<Cotizacion> cotizacionesVencidas = cotizacionRepository.findCotizacionesVencidas(LocalDateTime.now());
        
        for (Cotizacion cotizacion : cotizacionesVencidas) {
            cotizacion.setEstado("vencida");
            cotizacionRepository.save(cotizacion);
        }
        
        return cotizacionesVencidas.size();
    }

    /**
     * Obtener estadísticas de cotizaciones
     */
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> estadisticas = new HashMap<>();
        
        estadisticas.put("total", cotizacionRepository.count());
        estadisticas.put("activas", cotizacionRepository.countByEstado("activa"));
        estadisticas.put("aceptadas", cotizacionRepository.countByEstado("aceptada"));
        estadisticas.put("rechazadas", cotizacionRepository.countByEstado("rechazada"));
        estadisticas.put("convertidas", cotizacionRepository.countByEstado("convertida"));
        estadisticas.put("vencidas", cotizacionRepository.countByEstado("vencida"));
        
        return estadisticas;
    }

    /**
     * Generar número de cotización único
     */
    private String generarNumeroCotizacion() {
        LocalDateTime now = LocalDateTime.now();
        String anio = String.valueOf(now.getYear());
        String mes = String.format("%02d", now.getMonthValue());
        
        // Contar cotizaciones del mes actual
        LocalDateTime inicioMes = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime finMes = now.withDayOfMonth(now.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59);
        
        List<Cotizacion> cotizacionesMes = cotizacionRepository.findByFechaBetween(inicioMes, finMes);
        int consecutivo = cotizacionesMes.size() + 1;
        
        return String.format("COT-%s%s-%04d", anio, mes, consecutivo);
    }

    /**
     * Búsqueda por rango de fechas
     */
    public List<Cotizacion> buscarPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return cotizacionRepository.findByFechaBetween(fechaInicio, fechaFin);
    }
}
