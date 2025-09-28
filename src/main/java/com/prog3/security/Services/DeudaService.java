package com.prog3.security.Services;

import com.prog3.security.Models.Deuda;
import com.prog3.security.Models.PagoDeuda;
import com.prog3.security.Repositories.DeudaRepository;
import com.prog3.security.Repositories.PagoDeudaRepository;
import com.prog3.security.Repositories.PedidoRepository;
import com.prog3.security.Repositories.MesaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DeudaService {

    @Autowired
    private DeudaRepository deudaRepository;

    @Autowired
    private PagoDeudaRepository pagoDeudaRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private MesaRepository mesaRepository;

    /**
     * Crear una nueva deuda
     */
    public Deuda crearDeuda(Deuda deuda) {
        try {
            // Validar datos requeridos
            if (deuda.getPedidoId() == null || deuda.getPedidoId().trim().isEmpty()) {
                throw new IllegalArgumentException("El ID del pedido es requerido");
            }
            
            if (deuda.getMontoTotal() <= 0) {
                throw new IllegalArgumentException("El monto total debe ser mayor a 0");
            }

            // Verificar si ya existe una deuda para este pedido
            Deuda deudaExistente = deudaRepository.findByPedidoId(deuda.getPedidoId());
            if (deudaExistente != null && deudaExistente.isActiva()) {
                throw new RuntimeException("Ya existe una deuda activa para este pedido");
            }

            // Establecer valores por defecto
            if (deuda.getFechaCreacion() == null) {
                deuda.setFechaCreacion(LocalDateTime.now());
            }

            deuda.setMontoPagado(0.0);
            deuda.setMontoDeuda(deuda.getMontoTotal());
            deuda.setActiva(true);

            // Obtener información adicional del pedido y mesa si está disponible
            try {
                var pedido = pedidoRepository.findById(deuda.getPedidoId());
                if (pedido.isPresent()) {
                    var mesa = mesaRepository.findById(pedido.get().getMesa());
                    if (mesa.isPresent()) {
                        deuda.setMesaId(mesa.get().get_id());
                        deuda.setMesaNombre(mesa.get().getNombre());
                    }
                }
            } catch (Exception e) {
                System.out.println("Warning: No se pudo obtener información adicional del pedido: " + e.getMessage());
            }

            return deudaRepository.save(deuda);

        } catch (Exception e) {
            System.err.println("Error al crear deuda: " + e.getMessage());
            throw new RuntimeException("Error al crear la deuda: " + e.getMessage(), e);
        }
    }

    /**
     * Obtener todas las deudas
     */
    public List<Deuda> obtenerTodasDeudas() {
        return deudaRepository.findAll();
    }

    /**
     * Obtener todas las deudas activas
     */
    public List<Deuda> obtenerDeudasActivas() {
        return deudaRepository.findByActivaTrueOrderByFechaCreacionDesc();
    }

    /**
     * Obtener deuda por ID
     */
    public Optional<Deuda> obtenerDeudaPorId(String id) {
        return deudaRepository.findById(id);
    }

    /**
     * Obtener deuda por pedido
     */
    public Optional<Deuda> obtenerDeudaPorPedido(String pedidoId) {
        Deuda deuda = deudaRepository.findByPedidoId(pedidoId);
        return Optional.ofNullable(deuda);
    }

    /**
     * Obtener deudas por mesa
     */
    public List<Deuda> obtenerDeudasPorMesa(String mesaId) {
        return deudaRepository.findByMesaId(mesaId);
    }

    /**
     * Obtener deudas activas por mesa
     */
    public List<Deuda> obtenerDeudasActivasPorMesa(String mesaId) {
        return deudaRepository.findByMesaIdAndActivaTrue(mesaId);
    }

    /**
     * Obtener deudas vencidas
     */
    public List<Deuda> obtenerDeudasVencidas() {
        return deudaRepository.findDeudasVencidas(LocalDateTime.now());
    }

    /**
     * Registrar pago de deuda
     */
    public Deuda registrarPago(String deudaId, PagoDeuda pago) {
        try {
            Optional<Deuda> deudaOpt = deudaRepository.findById(deudaId);
            
            if (!deudaOpt.isPresent()) {
                throw new RuntimeException("Deuda no encontrada con ID: " + deudaId);
            }

            Deuda deuda = deudaOpt.get();
            
            if (!deuda.isActiva()) {
                throw new RuntimeException("No se puede registrar pago en una deuda inactiva");
            }

            if (pago.getMonto() <= 0) {
                throw new IllegalArgumentException("El monto del pago debe ser mayor a 0");
            }

            if (pago.getMonto() > deuda.getMontoDeuda()) {
                throw new IllegalArgumentException("El monto del pago no puede ser mayor a la deuda pendiente");
            }

            // Establecer relación con la deuda
            pago.setDeudaId(deudaId);
            if (pago.getFechaPago() == null) {
                pago.setFechaPago(LocalDateTime.now());
            }

            // Guardar el pago
            PagoDeuda pagoGuardado = pagoDeudaRepository.save(pago);

            // Actualizar la deuda
            deuda.agregarPago(pagoGuardado);
            deuda.setUltimoModificadoPor(pago.getRecibidoPor());

            return deudaRepository.save(deuda);

        } catch (Exception e) {
            System.err.println("Error al registrar pago: " + e.getMessage());
            throw new RuntimeException("Error al registrar el pago: " + e.getMessage(), e);
        }
    }

    /**
     * Obtener pagos de una deuda
     */
    public List<PagoDeuda> obtenerPagosDeuda(String deudaId) {
        return pagoDeudaRepository.findByDeudaIdOrderByFechaPagoDesc(deudaId);
    }

    /**
     * Activar/Desactivar deuda
     */
    public Deuda cambiarEstadoDeuda(String id, boolean activa, String modificadoPor) {
        try {
            Optional<Deuda> deudaOpt = deudaRepository.findById(id);
            
            if (!deudaOpt.isPresent()) {
                throw new RuntimeException("Deuda no encontrada con ID: " + id);
            }

            Deuda deuda = deudaOpt.get();
            deuda.setActiva(activa);
            deuda.setUltimoModificadoPor(modificadoPor);

            return deudaRepository.save(deuda);

        } catch (Exception e) {
            System.err.println("Error al cambiar estado de deuda: " + e.getMessage());
            throw new RuntimeException("Error al cambiar el estado de la deuda: " + e.getMessage(), e);
        }
    }

    /**
     * Actualizar deuda
     */
    public Deuda actualizarDeuda(String id, Deuda deudaActualizada) {
        try {
            Optional<Deuda> deudaOpt = deudaRepository.findById(id);
            
            if (!deudaOpt.isPresent()) {
                throw new RuntimeException("Deuda no encontrada con ID: " + id);
            }

            Deuda deuda = deudaOpt.get();

            // Actualizar campos permitidos
            if (deudaActualizada.getDescripcion() != null) {
                deuda.setDescripcion(deudaActualizada.getDescripcion());
            }
            
            if (deudaActualizada.getClienteInfo() != null) {
                deuda.setClienteInfo(deudaActualizada.getClienteInfo());
            }
            
            if (deudaActualizada.getFechaVencimiento() != null) {
                deuda.setFechaVencimiento(deudaActualizada.getFechaVencimiento());
            }

            // Solo permitir cambio de monto si no hay pagos registrados
            if (deudaActualizada.getMontoTotal() > 0 && deuda.getMontoPagado() == 0) {
                deuda.setMontoTotal(deudaActualizada.getMontoTotal());
                deuda.recalcularMontoDeuda();
            }

            if (deudaActualizada.getUltimoModificadoPor() != null) {
                deuda.setUltimoModificadoPor(deudaActualizada.getUltimoModificadoPor());
            }

            return deudaRepository.save(deuda);

        } catch (Exception e) {
            System.err.println("Error al actualizar deuda: " + e.getMessage());
            throw new RuntimeException("Error al actualizar la deuda: " + e.getMessage(), e);
        }
    }

    /**
     * Obtener estadísticas de deudas
     */
    public Map<String, Object> obtenerEstadisticasDeudas() {
        try {
            Map<String, Object> estadisticas = new HashMap<>();

            // Contar deudas activas e inactivas
            long deudasActivas = deudaRepository.countByActivaTrue();
            long deudasInactivas = deudaRepository.countByActivaFalse();
            long totalDeudas = deudasActivas + deudasInactivas;

            estadisticas.put("deudasActivas", deudasActivas);
            estadisticas.put("deudasInactivas", deudasInactivas);
            estadisticas.put("totalDeudas", totalDeudas);

            // Calcular montos
            List<Deuda> deudasActivasList = deudaRepository.findByActivaTrue();
            
            double totalDeudas_monto = deudasActivasList.stream()
                    .mapToDouble(Deuda::getMontoDeuda)
                    .sum();
            
            double promedioDeuda = deudasActivas > 0 ? totalDeudas_monto / deudasActivas : 0;

            estadisticas.put("totalDeudas", totalDeudas_monto);
            estadisticas.put("promedioDeuda", promedioDeuda);

            // Contar deudas vencidas
            long deudasVencidas = deudaRepository.countDeudasVencidas(LocalDateTime.now());
            estadisticas.put("deudasVencidas", deudasVencidas);

            // Estadísticas por mesa (top 10 mesas con más deudas)
            Map<String, Long> porMesa = deudasActivasList.stream()
                    .filter(d -> d.getMesaNombre() != null)
                    .collect(Collectors.groupingBy(
                            Deuda::getMesaNombre,
                            Collectors.counting()
                    ))
                    .entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(10)
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            HashMap::new
                    ));
            estadisticas.put("porMesa", porMesa);

            // Distribución por rangos de monto
            Map<String, Long> porRangoMonto = new HashMap<>();
            long rango1 = deudasActivasList.stream().filter(d -> d.getMontoDeuda() < 50000).count();
            long rango2 = deudasActivasList.stream().filter(d -> d.getMontoDeuda() >= 50000 && d.getMontoDeuda() < 100000).count();
            long rango3 = deudasActivasList.stream().filter(d -> d.getMontoDeuda() >= 100000 && d.getMontoDeuda() < 200000).count();
            long rango4 = deudasActivasList.stream().filter(d -> d.getMontoDeuda() >= 200000).count();
            
            porRangoMonto.put("menos_50k", rango1);
            porRangoMonto.put("50k_100k", rango2);
            porRangoMonto.put("100k_200k", rango3);
            porRangoMonto.put("mas_200k", rango4);
            estadisticas.put("porRangoMonto", porRangoMonto);

            return estadisticas;

        } catch (Exception e) {
            System.err.println("Error al obtener estadísticas de deudas: " + e.getMessage());
            throw new RuntimeException("Error al obtener estadísticas: " + e.getMessage(), e);
        }
    }

    /**
     * Buscar deudas por cliente
     */
    public List<Deuda> buscarDeudasPorCliente(String clienteInfo) {
        return deudaRepository.findByClienteInfoContainingIgnoreCase(clienteInfo);
    }

    /**
     * Obtener deudas por rango de fechas
     */
    public List<Deuda> obtenerDeudasPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return deudaRepository.findByFechaCreacionBetween(fechaInicio, fechaFin);
    }

    /**
     * Obtener deudas recientes
     */
    public List<Deuda> obtenerDeudasRecientes() {
        return deudaRepository.findTopByOrderByFechaCreacionDesc()
                .stream()
                .limit(50)
                .collect(Collectors.toList());
    }

    /**
     * Eliminar deuda (solo si no tiene pagos)
     */
    public boolean eliminarDeuda(String id) {
        try {
            Optional<Deuda> deudaOpt = deudaRepository.findById(id);
            
            if (!deudaOpt.isPresent()) {
                throw new RuntimeException("Deuda no encontrada con ID: " + id);
            }

            // Verificar si tiene pagos
            List<PagoDeuda> pagos = pagoDeudaRepository.findByDeudaId(id);
            if (!pagos.isEmpty()) {
                throw new RuntimeException("No se puede eliminar una deuda que tiene pagos registrados");
            }

            deudaRepository.deleteById(id);
            return true;

        } catch (Exception e) {
            System.err.println("Error al eliminar deuda: " + e.getMessage());
            return false;
        }
    }

    /**
     * Recalcular monto de deuda basado en pagos
     */
    public Deuda recalcularDeuda(String id) {
        try {
            Optional<Deuda> deudaOpt = deudaRepository.findById(id);
            
            if (!deudaOpt.isPresent()) {
                throw new RuntimeException("Deuda no encontrada con ID: " + id);
            }

            Deuda deuda = deudaOpt.get();
            
            // Obtener todos los pagos y recalcular
            List<PagoDeuda> pagos = pagoDeudaRepository.findByDeudaId(id);
            deuda.setPagos(pagos);
            deuda.recalcularMontoPagado();

            return deudaRepository.save(deuda);

        } catch (Exception e) {
            System.err.println("Error al recalcular deuda: " + e.getMessage());
            throw new RuntimeException("Error al recalcular la deuda: " + e.getMessage(), e);
        }
    }
}