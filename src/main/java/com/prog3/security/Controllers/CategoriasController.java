package com.prog3.security.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
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
import com.prog3.security.Utils.ApiResponse;

@CrossOrigin
@RestController
@RequestMapping("api/categorias")
public class CategoriasController extends BaseController<Categoria, String> {

    @Autowired
    private CategoriaRepository theCategoriaRepository;

    @Override
    protected MongoRepository<Categoria, String> getRepository() {
        return theCategoriaRepository;
    }
    
    @Override
    protected String getEntityName() {
        return "Categoría";
    }
    
    @Override
    protected ResponseEntity<ApiResponse<Categoria>> validateEntity(Categoria entity, boolean isUpdate) {
        if (entity.getNombre() == null || entity.getNombre().trim().isEmpty()) {
            return responseService.badRequest("El nombre es obligatorio");
        }
        return responseService.success(entity, "Validación exitosa");
    }
    
    @Override
    protected void updateEntityFields(Categoria existing, Categoria updated) {
        existing.setNombre(updated.getNombre());
        existing.setDescripcion(updated.getDescripcion());
        existing.setImagenUrl(updated.getImagenUrl());
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<Categoria>>> find() {
        return super.findAllEntities();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Categoria>> findById(@PathVariable String id) {
        return super.findEntityById(id);
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
        // Validar que no exista una categoría con el mismo nombre antes de crear
        if (this.theCategoriaRepository.existsByNombre(newCategoria.getNombre())) {
            return responseService.conflict("Ya existe una categoría con el nombre: " + newCategoria.getNombre());
        }
        return super.createEntity(newCategoria);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Categoria>> update(@PathVariable String id, @RequestBody Categoria newCategoria) {
        // Validar que el nuevo nombre no esté en uso por otra categoría
        Categoria existingCategoria = this.theCategoriaRepository.findByNombre(newCategoria.getNombre());
        if (existingCategoria != null && !existingCategoria.get_id().equals(id)) {
            return responseService.conflict("Ya existe una categoría con el nombre: " + newCategoria.getNombre());
        }
        
        return super.updateEntity(id, newCategoria);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        // TODO: Verificar que no haya productos asociados a esta categoría
        // ProductoRepository.countByCategoriaId(id) == 0
        return super.deleteEntity(id);
    }
}
