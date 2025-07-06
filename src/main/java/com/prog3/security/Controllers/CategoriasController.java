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

import com.prog3.security.Models.Categoria;
import com.prog3.security.Repositories.CategoriaRepository;

@CrossOrigin
@RestController
@RequestMapping("api/categorias")
public class CategoriasController {

    @Autowired
    CategoriaRepository theCategoriaRepository;

    @GetMapping("")
    public List<Categoria> find() {
        return this.theCategoriaRepository.findAll();
    }

    @GetMapping("/{id}")
    public Categoria findById(@PathVariable String id) {
        return this.theCategoriaRepository.findById(id).orElse(null);
    }

    @GetMapping("/nombre/{nombre}")
    public Categoria findByNombre(@PathVariable String nombre) {
        return this.theCategoriaRepository.findByNombre(nombre);
    }

    @GetMapping("/buscar")
    public List<Categoria> findByNombreContaining(@RequestParam String nombre) {
        return this.theCategoriaRepository.findByNombreContainingIgnoreCase(nombre);
    }

    @PostMapping
    public Categoria create(@RequestBody Categoria newCategoria) {
        // Validar que no exista una categoría con el mismo nombre
        if (this.theCategoriaRepository.existsByNombre(newCategoria.getNombre())) {
            return null; // O lanzar una excepción personalizada
        }
        return this.theCategoriaRepository.save(newCategoria);
    }

    @PutMapping("/{id}")
    public Categoria update(@PathVariable String id, @RequestBody Categoria newCategoria) {
        Categoria actualCategoria = this.theCategoriaRepository.findById(id).orElse(null);
        if (actualCategoria != null) {
            // Validar que el nuevo nombre no esté en uso por otra categoría
            Categoria existingCategoria = this.theCategoriaRepository.findByNombre(newCategoria.getNombre());
            if (existingCategoria != null && !existingCategoria.get_id().equals(id)) {
                return null; // Nombre ya existe en otra categoría
            }

            actualCategoria.setNombre(newCategoria.getNombre());
            actualCategoria.setImagenUrl(newCategoria.getImagenUrl());
            this.theCategoriaRepository.save(actualCategoria);
            return actualCategoria;
        } else {
            return null;
        }
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        this.theCategoriaRepository.findById(id).ifPresent(theCategoria
                -> this.theCategoriaRepository.delete(theCategoria));
    }
}
