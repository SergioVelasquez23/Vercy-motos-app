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

import com.prog3.security.Models.Producto;
import com.prog3.security.Models.Ingrediente;
import com.prog3.security.DTOs.CrearProductoRequest;
import com.prog3.security.Repositories.ProductoRepository;
import com.prog3.security.Repositories.IngredienteRepository;
import com.prog3.security.Services.ResponseService;
import com.prog3.security.Utils.ApiResponse;

@CrossOrigin
@RestController
@RequestMapping("api/productos")
public class ProductosController {

    @Autowired
    private ProductoRepository theProductoRepository;

    @Autowired
    private IngredienteRepository theIngredienteRepository;

    @Autowired
    private ResponseService responseService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<Producto>>> find() {
        try {
            List<Producto> productos = this.theProductoRepository.findAll();
            return responseService.success(productos, "Productos obtenidos exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener productos: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Producto>> findById(@PathVariable String id) {
        try {
            Producto producto = this.theProductoRepository.findById(id).orElse(null);
            if (producto == null) {
                return responseService.notFound("Producto no encontrado con ID: " + id);
            }
            return responseService.success(producto, "Producto encontrado");
        } catch (Exception e) {
            return responseService.internalError("Error al buscar producto: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Producto>> create(@RequestBody CrearProductoRequest request) {
        try {
            // Validar que no exista un producto con el mismo nombre
            if (this.theProductoRepository.existsByNombre(request.getNombre())) {
                return responseService.conflict("Ya existe un producto con el nombre: " + request.getNombre());
            }

            // Validaciones de negocio
            if (request.getPrecio() <= 0) {
                return responseService.badRequest("El precio debe ser mayor a 0");
            }

            if (request.getCosto() < 0) {
                return responseService.badRequest("El costo no puede ser negativo");
            }

            // Validar que los ingredientes existan si se especificaron
            if (request.getIngredientesDisponibles() != null && !request.getIngredientesDisponibles().isEmpty()) {
                for (String ingredienteId : request.getIngredientesDisponibles()) {
                    if (!theIngredienteRepository.existsById(ingredienteId)) {
                        return responseService.badRequest("El ingrediente con ID " + ingredienteId + " no existe");
                    }
                }
            }

            // Crear el producto
            Producto newProducto = new Producto();
            newProducto.setNombre(request.getNombre());
            newProducto.setPrecio(request.getPrecio());
            newProducto.setCosto(request.getCosto());
            newProducto.setImpuestos(request.getImpuestos());
            newProducto.setTieneVariantes(request.isTieneVariantes());
            newProducto.setEstado(request.getEstado());
            newProducto.setImagenUrl(request.getImagenUrl());
            newProducto.setCategoriaId(request.getCategoriaId());
            newProducto.setDescripcion(request.getDescripcion());
            newProducto.setCantidad(request.getCantidad());
            newProducto.setNota(request.getNota());
            newProducto.setIngredientesDisponibles(request.getIngredientesDisponibles());

            // Calcular utilidad si no se especifica
            if (request.getUtilidad() == 0.0) {
                double utilidad = request.getPrecio() - request.getCosto() - request.getImpuestos();
                newProducto.setUtilidad(utilidad);
            } else {
                newProducto.setUtilidad(request.getUtilidad());
            }

            Producto savedProducto = this.theProductoRepository.save(newProducto);
            return responseService.created(savedProducto, "Producto creado exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al crear producto: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable String id) {
        try {
            Producto producto = this.theProductoRepository.findById(id).orElse(null);
            if (producto == null) {
                return responseService.notFound("Producto no encontrado con ID: " + id);
            }

            this.theProductoRepository.delete(producto);
            return responseService.success(null, "Producto eliminado exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al eliminar producto: " + e.getMessage());
        }
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<ApiResponse<List<Producto>>> findByCategoria(@PathVariable String categoriaId) {
        try {
            List<Producto> productos = this.theProductoRepository.findByCategoriaId(categoriaId);
            return responseService.success(productos, "Productos por categoría obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al buscar productos por categoría: " + e.getMessage());
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<List<Producto>>> findByNombreContaining(@RequestParam String nombre) {
        try {
            List<Producto> productos = this.theProductoRepository.findByNombreContainingIgnoreCase(nombre);
            return responseService.success(productos, "Búsqueda completada");
        } catch (Exception e) {
            return responseService.internalError("Error en la búsqueda: " + e.getMessage());
        }
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<ApiResponse<List<Producto>>> findByEstado(@PathVariable String estado) {
        try {
            List<Producto> productos = this.theProductoRepository.findByEstado(estado);
            return responseService.success(productos, "Productos por estado obtenidos");
        } catch (Exception e) {
            return responseService.internalError("Error al buscar productos por estado: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Producto>> update(@PathVariable String id, @RequestBody Producto newProducto) {
        try {
            Producto actualProducto = this.theProductoRepository.findById(id).orElse(null);
            if (actualProducto == null) {
                return responseService.notFound("Producto no encontrado con ID: " + id);
            }

            // Validar que el nombre no exista si se está cambiando
            if (!actualProducto.getNombre().equals(newProducto.getNombre())
                    && this.theProductoRepository.existsByNombre(newProducto.getNombre())) {
                return responseService.conflict("Ya existe otro producto con el nombre: " + newProducto.getNombre());
            }

            // Validaciones de negocio
            if (newProducto.getPrecio() <= 0) {
                return responseService.badRequest("El precio debe ser mayor a 0");
            }

            if (newProducto.getCosto() < 0) {
                return responseService.badRequest("El costo no puede ser negativo");
            }

            // Actualizar campos
            actualProducto.setNombre(newProducto.getNombre());
            actualProducto.setPrecio(newProducto.getPrecio());
            actualProducto.setCosto(newProducto.getCosto());
            actualProducto.setImpuestos(newProducto.getImpuestos());
            actualProducto.setUtilidad(newProducto.getUtilidad());
            actualProducto.setTieneVariantes(newProducto.isTieneVariantes());
            actualProducto.setEstado(newProducto.getEstado());
            actualProducto.setImagenUrl(newProducto.getImagenUrl());
            actualProducto.setCategoriaId(newProducto.getCategoriaId());
            actualProducto.setDescripcion(newProducto.getDescripcion());
            actualProducto.setCantidad(newProducto.getCantidad());
            actualProducto.setNota(newProducto.getNota());
            actualProducto.setIngredientesDisponibles(newProducto.getIngredientesDisponibles());

            // Recalcular utilidad si es necesario
            if (newProducto.getUtilidad() == 0.0) {
                double utilidad = newProducto.getPrecio() - newProducto.getCosto() - newProducto.getImpuestos();
                actualProducto.setUtilidad(utilidad);
            }

            Producto updatedProducto = this.theProductoRepository.save(actualProducto);
            return responseService.success(updatedProducto, "Producto actualizado exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al actualizar producto: " + e.getMessage());
        }
    }

    @GetMapping("/ingredientes-carnes")
    public ResponseEntity<ApiResponse<List<Ingrediente>>> getIngredientesCarnes() {
        try {
            List<Ingrediente> carnes = this.theIngredienteRepository.findByCategoria("carne");
            return responseService.success(carnes, "Ingredientes de carnes obtenidos exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener ingredientes de carnes: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/ingredientes")
    public ResponseEntity<ApiResponse<List<Ingrediente>>> getIngredientesProducto(@PathVariable String id) {
        try {
            Producto producto = this.theProductoRepository.findById(id).orElse(null);
            if (producto == null) {
                return responseService.notFound("Producto no encontrado con ID: " + id);
            }

            if (producto.getIngredientesDisponibles() == null || producto.getIngredientesDisponibles().isEmpty()) {
                return responseService.success(List.of(), "El producto no tiene ingredientes configurados");
            }

            List<Ingrediente> ingredientes = this.theIngredienteRepository.findAllById(producto.getIngredientesDisponibles());
            return responseService.success(ingredientes, "Ingredientes del producto obtenidos exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener ingredientes del producto: " + e.getMessage());
        }
    }
}
