
package com.prog3.security.Services;

import com.prog3.security.Models.IngresoCaja;
import com.prog3.security.Repositories.IngresoCajaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class IngresoCajaService {
    @Autowired
    private IngresoCajaRepository ingresoCajaRepository;

    public IngresoCaja registrarIngreso(IngresoCaja ingreso) {
        if (ingreso.getFechaIngreso() == null) {
            ingreso.setFechaIngreso(LocalDateTime.now());
        }
        return ingresoCajaRepository.save(ingreso);
    }

    public List<IngresoCaja> obtenerIngresosEntreFechas(LocalDateTime inicio, LocalDateTime fin) {
        return ingresoCajaRepository.findByFechaIngresoBetween(inicio, fin);
    }

    public List<IngresoCaja> obtenerTodos() {
        return ingresoCajaRepository.findAll();
    }

    public void eliminarIngreso(String id) {
        ingresoCajaRepository.deleteById(id);
    }

    public List<IngresoCaja> obtenerPorCuadreCajaId(String cuadreCajaId) {
        return ingresoCajaRepository.findByCuadreCajaId(cuadreCajaId);
    }
}
