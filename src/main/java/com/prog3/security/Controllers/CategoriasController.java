package com.prog3.security.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
import com.prog3.security.Services.ResponseService;
import com.prog3.security.Utils.ApiResponse;

@CrossOrigin
@RestController
@RequestMapping("api/categorias")
public class CategoriasController {

    @Autowired
    CategoriaRepository theCategoriaRepository;

    @Autowired
    private ResponseService responseService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<Categoria>>> find() {
        try {
            List<Categoria> categorias = this.theCategoriaRepository.findAll();
            return responseService.success(categorias, "Categorías obtenidas exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener categorías: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Categoria>> findById(@PathVariable String id) {
        try {
            Categoria categoria = this.theCategoriaRepository.findById(id).orElse(null);
            if (categoria == null) {
                return responseService.notFound("Categoría no encontrada con ID: " + id);
            }
            return responseService.success(categoria, "Categoría encontrada");
        } catch (Exception e) {
            return responseService.internalError("Error al buscar categoría: " + e.getMessage());
        }
    }

    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<ApiResponse<Categoria>> findByNombre(@PathVariable String nombre) {
        try {
            Categoria categoria = this.theCategoriaRepository.findByNombre(nombre);
            if (categoria == null) {
                return responseService.notFound("Categoría no encontrada con nombre: " + nombre);
            }
            return responseService.success(categoria, "Categoría encontrada");
        } catch (Exception e) {
            return responseService.internalError("Error al buscar categoría por nombre: " + e.getMessage());
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<List<Categoria>>> findByNombreContaining(@RequestParam String nombre) {
        try {
            List<Categoria> categorias = this.theCategoriaRepository.findByNombreContainingIgnoreCase(nombre);
            return responseService.success(categorias, "Categorías filtradas obtenidas");
        } catch (Exception e) {
            return responseService.internalError("Error al buscar categorías: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Categoria>> create(@RequestBody Categoria newCategoria) {
        try {
            // Validar que no exista una categoría con el mismo nombre
            if (this.theCategoriaRepository.existsByNombre(newCategoria.getNombre())) {
                return responseService.conflict("Ya existe una categoría con el nombre: " + newCategoria.getNombre());
            }
            Categoria categoriaCreada = this.theCategoriaRepository.save(newCategoria);
            return responseService.created(categoriaCreada, "Categoría creada exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al crear categoría: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Categoria>> update(@PathVariable String id, @RequestBody Categoria newCategoria) {
        try {
            Categoria actualCategoria = this.theCategoriaRepository.findById(id).orElse(null);
            if (actualCategoria == null) {
                return responseService.notFound("Categoría no encontrada con ID: " + id);
            }

            // Validar que el nuevo nombre no esté en uso por otra categoría
            Categoria existingCategoria = this.theCategoriaRepository.findByNombre(newCategoria.getNombre());
            if (existingCategoria != null && !existingCategoria.get_id().equals(id)) {
                return responseService.conflict("Ya existe una categoría con el nombre: " + newCategoria.getNombre());
            }

            actualCategoria.setNombre(newCategoria.getNombre());
            actualCategoria.setImagenUrl(newCategoria.getImagenUrl());
            Categoria categoriaActualizada = this.theCategoriaRepository.save(actualCategoria);
            return responseService.success(categoriaActualizada, "Categoría actualizada exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al actualizar categoría: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        try {
            Categoria categoria = this.theCategoriaRepository.findById(id).orElse(null);
            if (categoria == null) {
                return responseService.notFound("Categoría no encontrada con ID: " + id);
            }

            // TODO: Verificar que no haya productos asociados a esta categoría
            // ProductoRepository.countByCategoriaId(id) == 0
            this.theCategoriaRepository.delete(categoria);
            return responseService.success(null, "Categoría eliminada exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al eliminar categoría: " + e.getMessage());
        }
    }
}
