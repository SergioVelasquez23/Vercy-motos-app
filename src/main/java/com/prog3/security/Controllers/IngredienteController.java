package com.prog3.security.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.prog3.security.Models.Ingrediente;
import com.prog3.security.Repositories.IngredienteRepository;
import com.prog3.security.Services.ResponseService;
import com.prog3.security.Utils.ApiResponse;

@CrossOrigin
@RestController
@RequestMapping("/api/ingredientes")
public class IngredienteController {

    @Autowired
    private IngredienteRepository ingredienteRepository;

    @Autowired
    private ResponseService responseService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<Ingrediente>>> findAll() {
        try {
            List<Ingrediente> ingredientes = this.ingredienteRepository.findAll();
            return responseService.success(ingredientes, "Ingredientes obtenidos exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener ingredientes: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Ingrediente>> findById(@PathVariable String id) {
        try {
            Ingrediente ingrediente = this.ingredienteRepository.findById(id).orElse(null);
            if (ingrediente == null) {
                return responseService.notFound("Ingrediente no encontrado con ID: " + id);
            }
            return responseService.success(ingrediente, "Ingrediente encontrado");
        } catch (Exception e) {
            return responseService.internalError("Error al buscar ingrediente: " + e.getMessage());
        }
    }

    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<ApiResponse<List<Ingrediente>>> findByCategoria(@PathVariable String categoria) {
        try {
            List<Ingrediente> ingredientes = this.ingredienteRepository.findByCategoria(categoria);
            return responseService.success(ingredientes, "Ingredientes encontrados por categoría");
        } catch (Exception e) {
            return responseService.internalError("Error al buscar ingredientes por categoría: " + e.getMessage());
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<List<Ingrediente>>> findByNombreContaining(@RequestParam String nombre) {
        try {
            List<Ingrediente> ingredientes = this.ingredienteRepository.findByNombreContainingIgnoreCase(nombre);
            return responseService.success(ingredientes, "Ingredientes encontrados");
        } catch (Exception e) {
            return responseService.internalError("Error al buscar ingredientes: " + e.getMessage());
        }
    }

    @GetMapping("/stock-bajo")
    public ResponseEntity<ApiResponse<List<Ingrediente>>> findByStockBajo() {
        try {
            List<Ingrediente> ingredientes = this.ingredienteRepository.findByStockBajo();
            return responseService.success(ingredientes, "Ingredientes con stock bajo encontrados");
        } catch (Exception e) {
            return responseService.internalError("Error al buscar ingredientes con stock bajo: " + e.getMessage());
        }
    }

    @PostMapping("")
    public ResponseEntity<ApiResponse<Ingrediente>> create(@RequestBody Ingrediente ingrediente) {
        try {
            // Validar que no exista un ingrediente con el mismo nombre o código
            if (this.ingredienteRepository.existsByNombre(ingrediente.getNombre())) {
                return responseService.conflict("Ya existe un ingrediente con el nombre: " + ingrediente.getNombre());
            }
            if (this.ingredienteRepository.existsByCodigo(ingrediente.getCodigo())) {
                return responseService.conflict("Ya existe un ingrediente con el código: " + ingrediente.getCodigo());
            }

            // Asegurar que el ID sea null antes de guardar (MongoDB lo generará automáticamente)
            ingrediente.set_id(null);

            Ingrediente nuevoIngrediente = this.ingredienteRepository.save(ingrediente);

            // Verificar que el ID se haya generado
            if (nuevoIngrediente.get_id() == null) {
                return responseService.internalError("Error: No se pudo generar el ID del ingrediente");
            }

            return responseService.created(nuevoIngrediente, "Ingrediente creado exitosamente con ID: " + nuevoIngrediente.get_id());
        } catch (Exception e) {
            return responseService.internalError("Error al crear ingrediente: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Ingrediente>> update(@PathVariable String id, @RequestBody Ingrediente ingrediente) {
        try {
            Ingrediente actualIngrediente = this.ingredienteRepository.findById(id).orElse(null);
            if (actualIngrediente == null) {
                return responseService.notFound("Ingrediente no encontrado con ID: " + id);
            }

            // Validar nombre y código únicos
            Ingrediente existingByNombre = this.ingredienteRepository.findByNombre(ingrediente.getNombre());
            if (existingByNombre != null && !existingByNombre.get_id().equals(id)) {
                return responseService.conflict("Ya existe un ingrediente con el nombre: " + ingrediente.getNombre());
            }

            Ingrediente existingByCodigo = this.ingredienteRepository.findByCodigo(ingrediente.getCodigo());
            if (existingByCodigo != null && !existingByCodigo.get_id().equals(id)) {
                return responseService.conflict("Ya existe un ingrediente con el código: " + ingrediente.getCodigo());
            }

            // Actualizar campos
            actualIngrediente.setCategoria(ingrediente.getCategoria());
            actualIngrediente.setCodigo(ingrediente.getCodigo());
            actualIngrediente.setNombre(ingrediente.getNombre());
            actualIngrediente.setUnidad(ingrediente.getUnidad());
            actualIngrediente.setPrecioCompra(ingrediente.getPrecioCompra());
            actualIngrediente.setStockActual(ingrediente.getStockActual());
            actualIngrediente.setStockMinimo(ingrediente.getStockMinimo());
            actualIngrediente.setEstado(ingrediente.getEstado());

            Ingrediente ingredienteActualizado = this.ingredienteRepository.save(actualIngrediente);
            return responseService.success(ingredienteActualizado, "Ingrediente actualizado exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al actualizar ingrediente: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        try {
            Ingrediente ingrediente = this.ingredienteRepository.findById(id).orElse(null);
            if (ingrediente == null) {
                return responseService.notFound("Ingrediente no encontrado con ID: " + id);
            }

            this.ingredienteRepository.delete(ingrediente);
            return responseService.success(null, "Ingrediente eliminado exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al eliminar ingrediente: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/stock")
    public ResponseEntity<ApiResponse<Ingrediente>> updateStock(
            @PathVariable String id,
            @RequestParam Double cantidad) {
        try {
            Ingrediente ingrediente = this.ingredienteRepository.findById(id).orElse(null);
            if (ingrediente == null) {
                return responseService.notFound("Ingrediente no encontrado con ID: " + id);
            }

            ingrediente.setStockActual(cantidad);
            Ingrediente ingredienteActualizado = this.ingredienteRepository.save(ingrediente);
            return responseService.success(ingredienteActualizado, "Stock actualizado exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al actualizar stock: " + e.getMessage());
        }
    }
}
