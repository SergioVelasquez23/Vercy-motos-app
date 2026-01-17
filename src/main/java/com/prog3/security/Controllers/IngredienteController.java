package com.prog3.security.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.prog3.security.Models.Ingrediente;
import com.prog3.security.Models.MovimientoInventario;
import com.prog3.security.Repositories.IngredienteRepository;
import com.prog3.security.Repositories.MovimientoInventarioRepository;
import com.prog3.security.Services.ResponseService;
import com.prog3.security.Utils.ApiResponse;

@CrossOrigin
@RestController
@RequestMapping("/api/ingredientes")
public class IngredienteController {

    @Autowired
    private IngredienteRepository ingredienteRepository;

    @Autowired
    private MovimientoInventarioRepository movimientoInventarioRepository;

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

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<ApiResponse<List<Ingrediente>>> findByCategoriaId(@PathVariable String categoriaId) {
        try {
            List<Ingrediente> ingredientes = this.ingredienteRepository.findByCategoriaId(categoriaId);
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
            // Validar que no exista un ingrediente con el mismo nombre
            if (this.ingredienteRepository.existsByNombre(ingrediente.getNombre())) {
                return responseService.conflict("Ya existe un ingrediente con el nombre: " + ingrediente.getNombre());
            }

            // La categoría ya no es obligatoria

            // Asegurar que el ID sea null antes de guardar (MongoDB lo generará automáticamente)
            ingrediente.set_id(null);
            
            System.out.println("⚠️ Creando ingrediente: " + ingrediente.getNombre());
            System.out.println("⚠️ Costo antes de guardar: " + ingrediente.getCosto());
            
            Ingrediente nuevoIngrediente = this.ingredienteRepository.save(ingrediente);

            // Verificar que el ID se haya generado
            if (nuevoIngrediente.get_id() == null) {
                return responseService.internalError("Error: No se pudo generar el ID del ingrediente");
            }
            
            System.out.println("⚠️ Costo después de guardar: " + nuevoIngrediente.getCosto());
            System.out.println("⚠️ Ingrediente creado: " + nuevoIngrediente.toString());

            return responseService.created(nuevoIngrediente, "Ingrediente creado exitosamente con ID: " + nuevoIngrediente.get_id());
        } catch (Exception e) {
            return responseService.internalError("Error al crear ingrediente: " + e.getMessage());
        }
    }

    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<List<Ingrediente>>> createBatch(@RequestBody List<Ingrediente> ingredientes) {
        try {
            if (ingredientes == null || ingredientes.isEmpty()) {
                return responseService.badRequest("La lista de ingredientes no puede estar vacía");
            }

            List<Ingrediente> ingredientesCreados = new java.util.ArrayList<>();
            List<String> errores = new java.util.ArrayList<>();

            for (int i = 0; i < ingredientes.size(); i++) {
                Ingrediente ingrediente = ingredientes.get(i);

                try {
                    // Validar que no exista un ingrediente con el mismo nombre
                    if (this.ingredienteRepository.existsByNombre(ingrediente.getNombre())) {
                        errores.add("Ingrediente " + (i + 1) + ": Ya existe un ingrediente con el nombre '" + ingrediente.getNombre() + "'");
                        continue;
                    }

                    // La categoría ya no es obligatoria

                    // Asegurar que el ID sea null antes de guardar
                    ingrediente.set_id(null);

                    Ingrediente nuevoIngrediente = this.ingredienteRepository.save(ingrediente);
                    if (nuevoIngrediente.get_id() != null) {
                        ingredientesCreados.add(nuevoIngrediente);
                    } else {
                        errores.add("Ingrediente " + (i + 1) + ": Error al generar ID para '" + ingrediente.getNombre() + "'");
                    }
                } catch (Exception e) {
                    errores.add("Ingrediente " + (i + 1) + ": Error al crear '" + ingrediente.getNombre() + "' - " + e.getMessage());
                }
            }

            if (ingredientesCreados.isEmpty()) {
                return responseService.badRequest("No se pudo crear ningún ingrediente. Errores: " + String.join(", ", errores));
            } else if (!errores.isEmpty()) {
                return responseService.success(ingredientesCreados,
                        "Se crearon " + ingredientesCreados.size() + " de " + ingredientes.size() + " ingredientes. Errores: " + String.join(", ", errores));
            } else {
                return responseService.created(ingredientesCreados,
                        "Se crearon exitosamente " + ingredientesCreados.size() + " ingredientes");
            }
        } catch (Exception e) {
            return responseService.internalError("Error al crear ingredientes en lote: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Ingrediente>> update(@PathVariable String id, @RequestBody Ingrediente ingrediente) {
        try {
            Ingrediente actualIngrediente = this.ingredienteRepository.findById(id).orElse(null);
            if (actualIngrediente == null) {
                return responseService.notFound("Ingrediente no encontrado con ID: " + id);
            }

            // Validar nombre único
            Ingrediente existingByNombre = this.ingredienteRepository.findByNombre(ingrediente.getNombre());
            if (existingByNombre != null && !existingByNombre.get_id().equals(id)) {
                return responseService.conflict("Ya existe un ingrediente con el nombre: " + ingrediente.getNombre());
            }

            // La categoría ya no es obligatoria

            // Log de valores originales para depuración
            System.out.println("📝 VALORES ORIGINALES:");
            System.out.println("  - Nombre: " + actualIngrediente.getNombre());
            System.out.println("  - Costo Original: " + actualIngrediente.getCosto());
            System.out.println("  - Costo a Actualizar: " + ingrediente.getCosto());
            System.out.println("  - Stock Actual: " + actualIngrediente.getStockActual());
            
            // Actualizar campos
            actualIngrediente.setCategoriaId(ingrediente.getCategoriaId());
            actualIngrediente.setNombre(ingrediente.getNombre());
            actualIngrediente.setUnidad(ingrediente.getUnidad());
            actualIngrediente.setStockActual(ingrediente.getStockActual());
            actualIngrediente.setStockMinimo(ingrediente.getStockMinimo());
            
            // Asegurarnos que el costo se actualiza explícitamente
            double nuevoCosto = ingrediente.getCosto();
            actualIngrediente.setCosto(nuevoCosto);
            actualIngrediente.setDescontable(ingrediente.isDescontable());

            System.out.println("⚠️ Actualizando ingrediente: " + id);
            System.out.println("⚠️ Costo antes de guardar: " + actualIngrediente.getCosto());
            
            // Guardar el ingrediente actualizado
            Ingrediente ingredienteActualizado = this.ingredienteRepository.save(actualIngrediente);
            
            // Verificar que el costo se haya actualizado correctamente
            System.out.println("⚠️ Costo después de guardar: " + ingredienteActualizado.getCosto());
            System.out.println("⚠️ Ingrediente actualizado completo: " + ingredienteActualizado.toString());
            
            // Log del objeto que se va a devolver como respuesta
            System.out.println("🔄 RESPUESTA JSON que se enviará:");
            System.out.println("  - _id: " + ingredienteActualizado.get_id());
            System.out.println("  - nombre: " + ingredienteActualizado.getNombre());
            System.out.println("  - costo: " + ingredienteActualizado.getCosto());
            System.out.println("  - descontable: " + ingredienteActualizado.isDescontable());
            // Asegurar que todos los campos sean correctamente serializados en la respuesta
            ApiResponse<Ingrediente> response = ApiResponse.<Ingrediente>builder()
                .success(true)
                .message("Ingrediente actualizado exitosamente")
                .data(ingredienteActualizado)
                .build();
                
            // Verificamos que el campo costo esté correctamente incluido en la respuesta
            System.out.println("🧪 DEBUG - Verificando ApiResponse.data:");
            System.out.println("  - ID: " + response.getData().get_id());
            System.out.println("  - Nombre: " + response.getData().getNombre());
            System.out.println("  - Costo en respuesta: " + response.getData().getCosto());
            
            return ResponseEntity.ok(response);
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

    @DeleteMapping("/deleteAll")
    public ResponseEntity<ApiResponse<Void>> deleteAll() {
        try {
            long count = this.ingredienteRepository.count();
            this.ingredienteRepository.deleteAll();
            return responseService.success(null, "Se eliminaron " + count + " ingredientes exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al eliminar todos los ingredientes: " + e.getMessage());
        }
    }

    @GetMapping("/movimientos")
    public ResponseEntity<ApiResponse<List<MovimientoInventario>>> getMovimientos() {
        try {
            List<MovimientoInventario> movimientos = movimientoInventarioRepository.findAll();
            return responseService.success(movimientos, "Movimientos de ingredientes obtenidos exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener movimientos de ingredientes: " + e.getMessage());
        }
    }
}
