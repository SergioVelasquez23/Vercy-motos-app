package com.prog3.security.Services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.prog3.security.Models.Gasto;
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
        // Verificar que el tipo de gasto exista
        TipoGasto tipoGasto = tipoGastoRepository.findById(request.getTipoGastoId()).orElse(null);
        if (tipoGasto == null) {
            throw new RuntimeException("Tipo de gasto no encontrado");
        }

        // Verificar que el cuadre exista y no esté cerrado
        CuadreCaja cuadreCaja = cuadreCajaRepository.findById(request.getCuadreCajaId()).orElse(null);
        if (cuadreCaja == null) {
            throw new RuntimeException("Cuadre de caja no encontrado");
        }

        if (cuadreCaja.isCerrada()) {
            throw new RuntimeException("No se pueden agregar gastos a un cuadre cerrado");
        }

        Gasto gasto = new Gasto(
                request.getCuadreCajaId(),
                request.getTipoGastoId(),
                tipoGasto.getNombre(), // Guardar también el nombre para facilitar consultas
                request.getConcepto(),
                request.getMonto(),
                request.getResponsable()
        );

        // Establecer campos opcionales
        if (request.getFechaGasto() != null) {
            gasto.setFechaGasto(request.getFechaGasto());
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
        if (request.getFormaPago() != null) {
            gasto.setFormaPago(request.getFormaPago());
        }
        if (request.getSubtotal() > 0) {
            gasto.setSubtotal(request.getSubtotal());
        }
        if (request.getImpuestos() > 0) {
            gasto.setImpuestos(request.getImpuestos());
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

        // Actualizar tipo de gasto si cambió
        if (request.getTipoGastoId() != null && !request.getTipoGastoId().equals(gasto.getTipoGastoId())) {
            TipoGasto tipoGasto = tipoGastoRepository.findById(request.getTipoGastoId()).orElse(null);
            if (tipoGasto == null) {
                throw new RuntimeException("Tipo de gasto no encontrado");
            }
            gasto.setTipoGastoId(request.getTipoGastoId());
            gasto.setTipoGastoNombre(tipoGasto.getNombre());
        }

        // Actualizar resto de campos
        if (request.getConcepto() != null) {
            gasto.setConcepto(request.getConcepto());
        }
        if (request.getMonto() > 0) {
            gasto.setMonto(request.getMonto());
        }
        if (request.getResponsable() != null) {
            gasto.setResponsable(request.getResponsable());
        }
        if (request.getFechaGasto() != null) {
            gasto.setFechaGasto(request.getFechaGasto());
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
        if (request.getFormaPago() != null) {
            gasto.setFormaPago(request.getFormaPago());
        }
        if (request.getSubtotal() > 0) {
            gasto.setSubtotal(request.getSubtotal());
        }
        if (request.getImpuestos() > 0) {
            gasto.setImpuestos(request.getImpuestos());
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

            gastoRepository.deleteById(id);

            // Actualizar totales en el cuadre
            if (cuadreCaja != null) {
                actualizarTotalesGastoEnCuadre(cuadreCaja);
            }

            return true;
        } catch (Exception e) {
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
}
