package com.prog3.security.Controllers;

import com.prog3.security.Models.Categoria;
import com.prog3.security.Services.CategoriaService;
import com.prog3.security.Services.ResponseService;
import com.prog3.security.Utils.ApiResponse;
import com.prog3.security.Exception.BusinessException;
import com.prog3.security.Exception.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("api/categorias")
@Tag(name = "Categorías", description = "Gestión de categorías de productos")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private ResponseService responseService;

    /**
     * 📋 Listar todas las categorías activas GET /api/categorias
     */
    @GetMapping
    @Operation(summary = "Listar categorías activas",
            description = "Obtiene todas las categorías activas ordenadas por campo orden")
    public ResponseEntity<?> listarCategorias(
            @RequestParam(required = false, defaultValue = "true") @Parameter(
                    description = "Filtrar solo activas (true) o todas (false)") boolean soloActivas) {
        try {
            List<Categoria> categorias = soloActivas ? categoriaService.obtenerCategoriasActivas()
                    : categoriaService.obtenerTodas();

            return ResponseEntity.ok(categorias);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message",
                    "Error al listar categorías: " + e.getMessage()));
        }
    }

    /**
     * 🔍 Obtener una categoría por ID GET /api/categorias/:id
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener categoría por ID",
            description = "Obtiene los detalles de una categoría específica")
    public ResponseEntity<?> obtenerCategoria(@PathVariable String id) {
        try {
            Categoria categoria = categoriaService.obtenerPorId(id);
            return ResponseEntity.ok(categoria);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message",
                    "Error al obtener la categoría: " + e.getMessage()));
        }
    }

    /**
     * ➕ Crear una nueva categoría POST /api/categorias
     */
    @PostMapping
    @Operation(summary = "Crear categoría", description = "Crea una nueva categoría de productos")
    public ResponseEntity<?> crearCategoria(@Valid @RequestBody Categoria categoria) {
        try {
            Categoria categoriaCreada = categoriaService.crear(categoria);
            return ResponseEntity.ok(categoriaCreada);
        } catch (BusinessException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message",
                    "Error al crear la categoría: " + e.getMessage()));
        }
    }

    /**
     * ✏️ Actualizar una categoría PUT /api/categorias/:id
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar categoría",
            description = "Actualiza los datos de una categoría existente")
    public ResponseEntity<?> actualizarCategoria(@PathVariable String id,
            @Valid @RequestBody Categoria categoria) {
        try {
            Categoria categoriaActualizada = categoriaService.actualizar(id, categoria);
            return ResponseEntity.ok(categoriaActualizada);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message",
                    "Error al actualizar la categoría: " + e.getMessage()));
        }
    }

    /**
     * 🗑️ Eliminar una categoría DELETE /api/categorias/:id
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar categoría",
            description = "Elimina permanentemente una categoría")
    public ResponseEntity<?> eliminarCategoria(@PathVariable String id) {
        try {
            categoriaService.eliminar(id);
            return ResponseEntity
                    .ok(Map.of("success", true, "message", "Categoría eliminada correctamente"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message",
                    "Error al eliminar la categoría: " + e.getMessage()));
        }
    }

    /**
     * ❌ Desactivar una categoría PUT /api/categorias/:id/desactivar
     */
    @PutMapping("/{id}/desactivar")
    @Operation(summary = "Desactivar categoría",
            description = "Desactiva una categoría sin eliminarla permanentemente")
    public ResponseEntity<?> desactivarCategoria(@PathVariable String id) {
        try {
            Categoria categoria = categoriaService.desactivar(id);
            return ResponseEntity.ok(categoria);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message",
                    "Error al desactivar la categoría: " + e.getMessage()));
        }
    }

    /**
     * ✅ Activar una categoría PUT /api/categorias/:id/activar
     */
    @PutMapping("/{id}/activar")
    @Operation(summary = "Activar categoría",
            description = "Activa una categoría previamente desactivada")
    public ResponseEntity<?> activarCategoria(@PathVariable String id) {
        try {
            Categoria categoria = categoriaService.activar(id);
            return ResponseEntity.ok(categoria);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message",
                    "Error al activar la categoría: " + e.getMessage()));
        }
    }

    /**
     * 🔎 Buscar categorías por nombre GET /api/categorias/buscar
     */
    @GetMapping("/buscar")
    @Operation(summary = "Buscar categorías por nombre",
            description = "Busca categorías que contengan el texto especificado en el nombre")
    public ResponseEntity<?> buscarCategorias(
            @RequestParam @Parameter(description = "Texto a buscar en el nombre") String nombre) {
        try {
            List<Categoria> categorias = categoriaService.buscarPorNombre(nombre);
            return ResponseEntity.ok(categorias);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message",
                    "Error al buscar categorías: " + e.getMessage()));
        }
    }
}
