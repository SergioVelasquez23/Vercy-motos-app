package com.prog3.security.Controllers;

import com.prog3.security.Models.Unidad;
import com.prog3.security.Repositories.UnidadRepository;
import com.prog3.security.Services.ResponseService;
import com.prog3.security.Utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("/api/unidades")
public class UnidadController {
    @Autowired
    private UnidadRepository unidadRepository;
    @Autowired
    private ResponseService responseService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<Unidad>>> findAll() {
        List<Unidad> unidades = unidadRepository.findAll();
        return responseService.success(unidades, "Unidades obtenidas exitosamente");
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Unidad>> findById(@PathVariable String id) {
        Optional<Unidad> unidad = unidadRepository.findById(id);
        return unidad.map(u -> responseService.success(u, "Unidad encontrada"))
                .orElseGet(() -> responseService.notFound("Unidad no encontrada"));
    }

    @PostMapping("")
    public ResponseEntity<ApiResponse<Unidad>> create(@RequestBody Unidad unidad) {
        if (unidadRepository.existsByNombre(unidad.getNombre())) {
            return responseService.badRequest("Ya existe una unidad con ese nombre");
        }
        Unidad saved = unidadRepository.save(unidad);
        return responseService.success(saved, "Unidad creada exitosamente");
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Unidad>> update(@PathVariable String id, @RequestBody Unidad unidad) {
        Optional<Unidad> unidadOpt = unidadRepository.findById(id);
        if (!unidadOpt.isPresent()) {
            return responseService.notFound("Unidad no encontrada");
        }
        Unidad existente = unidadOpt.get();
        existente.setNombre(unidad.getNombre());
        existente.setAbreviatura(unidad.getAbreviatura());
        existente.setActivo(unidad.isActivo());
        Unidad updated = unidadRepository.save(existente);
        return responseService.success(updated, "Unidad actualizada exitosamente");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        if (!unidadRepository.existsById(id)) {
            return responseService.notFound("Unidad no encontrada");
        }
        unidadRepository.deleteById(id);
        return responseService.success(null, "Unidad eliminada exitosamente");
    }
}
