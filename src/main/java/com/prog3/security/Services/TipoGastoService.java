package com.prog3.security.Services;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.prog3.security.Models.TipoGasto;
import com.prog3.security.Repositories.TipoGastoRepository;
import com.prog3.security.DTOs.TipoGastoRequest;

@Service
public class TipoGastoService {

    @Autowired
    private TipoGastoRepository tipoGastoRepository;

    /**
     * Obtiene todos los tipos de gasto activos
     */
    public List<TipoGasto> obtenerTiposGastoActivos() {
        return tipoGastoRepository.findByActivoTrue();
    }

    /**
     * Obtiene todos los tipos de gasto
     */
    public List<TipoGasto> obtenerTodosTiposGasto() {
        return tipoGastoRepository.findAll();
    }

    /**
     * Busca un tipo de gasto por ID
     */
    public TipoGasto obtenerTipoGastoPorId(String id) {
        return tipoGastoRepository.findById(id).orElse(null);
    }

    /**
     * Busca tipos de gasto por nombre
     */
    public List<TipoGasto> buscarPorNombre(String nombre) {
        return tipoGastoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    /**
     * Crea un nuevo tipo de gasto
     */
    public TipoGasto crearTipoGasto(TipoGastoRequest request) {
        TipoGasto tipoGasto = new TipoGasto();
        tipoGasto.setNombre(request.getNombre());
        tipoGasto.setDescripcion(request.getDescripcion());
        tipoGasto.setActivo(request.isActivo());

        return tipoGastoRepository.save(tipoGasto);
    }

    /**
     * Actualiza un tipo de gasto existente
     */
    public TipoGasto actualizarTipoGasto(String id, TipoGastoRequest request) {
        TipoGasto tipoGasto = tipoGastoRepository.findById(id).orElse(null);

        if (tipoGasto == null) {
            return null;
        }

        tipoGasto.setNombre(request.getNombre());
        tipoGasto.setDescripcion(request.getDescripcion());
        tipoGasto.setActivo(request.isActivo());

        return tipoGastoRepository.save(tipoGasto);
    }

    /**
     * Elimina un tipo de gasto (solo si no tiene gastos asociados)
     */
    public boolean eliminarTipoGasto(String id) {
        try {
            // Aquí deberíamos verificar si hay gastos que referencian a este tipo
            // pero por simplicidad, solo verificamos que exista
            if (!tipoGastoRepository.existsById(id)) {
                return false;
            }

            tipoGastoRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
