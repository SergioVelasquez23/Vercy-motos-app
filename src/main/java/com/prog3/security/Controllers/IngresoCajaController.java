
package com.prog3.security.Controllers;

import com.prog3.security.Models.IngresoCaja;
import com.prog3.security.Services.IngresoCajaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/ingresos-caja")
public class IngresoCajaController {
    @Autowired
    private IngresoCajaService ingresoCajaService;

    @PostMapping
    public IngresoCaja registrarIngreso(@RequestBody IngresoCaja ingreso) {
        return ingresoCajaService.registrarIngreso(ingreso);
    }

    @GetMapping("/por-caja/{cuadreCajaId}")
    public List<IngresoCaja> obtenerPorCuadreCaja(@PathVariable String cuadreCajaId) {
        return ingresoCajaService.obtenerPorCuadreCajaId(cuadreCajaId);
    }

    @GetMapping
    public List<IngresoCaja> obtenerTodos() {
        return ingresoCajaService.obtenerTodos();
    }

    @GetMapping("/rango")
    public List<IngresoCaja> obtenerPorRangoFechas(
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam("fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return ingresoCajaService.obtenerIngresosEntreFechas(inicio, fin);
    }

    @DeleteMapping("/{id}")
    public void eliminarIngreso(@PathVariable String id) {
        ingresoCajaService.eliminarIngreso(id);
    }
}
