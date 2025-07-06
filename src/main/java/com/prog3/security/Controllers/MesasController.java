package com.prog3.security.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.prog3.security.Models.Mesa;
import com.prog3.security.Repositories.MesaRepository;

@CrossOrigin
@RestController
@RequestMapping("api/mesas")
public class MesasController {

    @Autowired
    MesaRepository theMesaRepository;

    @GetMapping("")
    public List<Mesa> find() {
        return this.theMesaRepository.findAll();
    }

    @GetMapping("/{id}")
    public Mesa findById(@PathVariable String id) {
        return this.theMesaRepository.findById(id).orElse(null);
    }

    @GetMapping("/nombre/{nombre}")
    public Mesa findByNombre(@PathVariable String nombre) {
        return this.theMesaRepository.findByNombre(nombre);
    }

    @GetMapping("/ocupadas")
    public List<Mesa> findOcupadas() {
        return this.theMesaRepository.findByOcupadaTrue();
    }

    @GetMapping("/libres")
    public List<Mesa> findLibres() {
        return this.theMesaRepository.findByOcupadaFalse();
    }

    @GetMapping("/estado/{ocupada}")
    public List<Mesa> findByEstado(@PathVariable boolean ocupada) {
        return this.theMesaRepository.findByOcupada(ocupada);
    }

    @GetMapping("/total")
    public List<Mesa> findByTotalRange(@RequestParam double min, @RequestParam double max) {
        return this.theMesaRepository.findByTotalBetween(min, max);
    }

    @PostMapping
    public Mesa create(@RequestBody Mesa newMesa) {
        // Validar que no exista una mesa con el mismo nombre
        if (this.theMesaRepository.existsByNombre(newMesa.getNombre())) {
            return null; // O lanzar una excepción personalizada
        }
        return this.theMesaRepository.save(newMesa);
    }

    @PutMapping("/{id}")
    public Mesa update(@PathVariable String id, @RequestBody Mesa newMesa) {
        Mesa actualMesa = this.theMesaRepository.findById(id).orElse(null);
        if (actualMesa != null) {
            // Validar que el nuevo nombre no esté en uso por otra mesa
            if (!actualMesa.getNombre().equals(newMesa.getNombre())
                    && this.theMesaRepository.existsByNombre(newMesa.getNombre())) {
                return null; // Nombre ya existe en otra mesa
            }

            actualMesa.setNombre(newMesa.getNombre());
            actualMesa.setOcupada(newMesa.isOcupada());
            actualMesa.setTotal(newMesa.getTotal());
            actualMesa.setProductosIds(newMesa.getProductosIds());

            this.theMesaRepository.save(actualMesa);
            return actualMesa;
        } else {
            return null;
        }
    }

    @PutMapping("/{id}/ocupar")
    public Mesa ocuparMesa(@PathVariable String id) {
        Mesa mesa = this.theMesaRepository.findById(id).orElse(null);
        if (mesa != null) {
            mesa.setOcupada(true);
            mesa.setTotal(0.0); // Reiniciar total al ocupar
            mesa.getProductosIds().clear(); // Limpiar productos
            this.theMesaRepository.save(mesa);
            return mesa;
        }
        return null;
    }

    @PutMapping("/{id}/liberar")
    public Mesa liberarMesa(@PathVariable String id) {
        Mesa mesa = this.theMesaRepository.findById(id).orElse(null);
        if (mesa != null) {
            mesa.setOcupada(false);
            mesa.setTotal(0.0); // Reiniciar total al liberar
            mesa.getProductosIds().clear(); // Limpiar productos
            this.theMesaRepository.save(mesa);
            return mesa;
        }
        return null;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        this.theMesaRepository.findById(id).ifPresent(theMesa
                -> this.theMesaRepository.delete(theMesa));
    }
}
