package com.prog3.security.Controllers;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
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
import com.prog3.security.Repositories.ProductoRepository;

@CrossOrigin
@RestController
@RequestMapping("api/productos")
public class ProductosController {

    @Autowired
    ProductoRepository theProductoRepository;

    @GetMapping("")
    public ResponseEntity<Map<String, Object>> find() {
        try {
            List<Producto> productos = this.theProductoRepository.findAll();
            Map<String, Object> response = createSuccessResponse(productos, "Productos obtenidos exitosamente");
            System.out.println("Productos obtenidos: " + productos.size()); // Log simple
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error al obtener productos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error al obtener productos", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> findById(@PathVariable String id) {
        try {
            Producto producto = this.theProductoRepository.findById(id).orElse(null);
            if (producto == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Producto no encontrado", "ID: " + id));
            }
            Map<String, Object> response = createSuccessResponse(producto, "Producto encontrado");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error al buscar producto " + id + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error al buscar producto", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Producto newProducto) {
        try {
            // Validar que no exista un producto con el mismo nombre
            if (this.theProductoRepository.existsByNombre(newProducto.getNombre())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(createErrorResponse("Producto ya existe", "Ya existe un producto con el nombre: " + newProducto.getNombre()));
            }

            // Validaciones de negocio
            if (newProducto.getPrecio() <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Precio inválido", "El precio debe ser mayor a 0"));
            }

            if (newProducto.getCosto() < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Costo inválido", "El costo no puede ser negativo"));
            }

            // Calcular utilidad si no se especifica
            if (newProducto.getUtilidad() == 0.0) {
                double utilidad = newProducto.getPrecio() - newProducto.getCosto() - newProducto.getImpuestos();
                newProducto.setUtilidad(utilidad);
            }

            Producto savedProducto = this.theProductoRepository.save(newProducto);

            Map<String, Object> response = createSuccessResponse(savedProducto, "Producto creado exitosamente");
            System.out.println("Producto creado: " + savedProducto.get_id() + " - " + savedProducto.getNombre());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            System.err.println("Error al crear producto: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error al crear producto", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable String id) {
        try {
            Producto producto = this.theProductoRepository.findById(id).orElse(null);
            if (producto == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Producto no encontrado", "ID: " + id));
            }

            this.theProductoRepository.delete(producto);

            Map<String, Object> response = createSuccessResponse(null, "Producto eliminado exitosamente");
            System.out.println("Producto eliminado: " + id + " - " + producto.getNombre());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error al eliminar producto " + id + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error al eliminar producto", e.getMessage()));
        }
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<Map<String, Object>> findByCategoria(@PathVariable String categoriaId) {
        try {
            List<Producto> productos = this.theProductoRepository.findByCategoriaId(categoriaId);
            Map<String, Object> response = createSuccessResponse(productos, "Productos por categoría obtenidos");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error al buscar productos por categoría: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error al buscar productos por categoría", e.getMessage()));
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<Map<String, Object>> findByNombreContaining(@RequestParam String nombre) {
        try {
            List<Producto> productos = this.theProductoRepository.findByNombreContainingIgnoreCase(nombre);
            Map<String, Object> response = createSuccessResponse(productos, "Búsqueda completada");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error en la búsqueda: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error en la búsqueda", e.getMessage()));
        }
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<Map<String, Object>> findByEstado(@PathVariable String estado) {
        try {
            List<Producto> productos = this.theProductoRepository.findByEstado(estado);
            Map<String, Object> response = createSuccessResponse(productos, "Productos por estado obtenidos");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error al buscar productos por estado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error al buscar productos por estado", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable String id, @RequestBody Producto newProducto) {
        try {
            Producto actualProducto = this.theProductoRepository.findById(id).orElse(null);
            if (actualProducto == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Producto no encontrado", "ID: " + id));
            }

            // Validar que el nombre no exista si se está cambiando
            if (!actualProducto.getNombre().equals(newProducto.getNombre())
                    && this.theProductoRepository.existsByNombre(newProducto.getNombre())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(createErrorResponse("Nombre ya existe", "Ya existe otro producto con el nombre: " + newProducto.getNombre()));
            }

            // Validaciones de negocio
            if (newProducto.getPrecio() <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Precio inválido", "El precio debe ser mayor a 0"));
            }

            if (newProducto.getCosto() < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Costo inválido", "El costo no puede ser negativo"));
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

            // Recalcular utilidad si es necesario
            if (newProducto.getUtilidad() == 0.0) {
                double utilidad = newProducto.getPrecio() - newProducto.getCosto() - newProducto.getImpuestos();
                actualProducto.setUtilidad(utilidad);
            }

            Producto updatedProducto = this.theProductoRepository.save(actualProducto);
            Map<String, Object> response = createSuccessResponse(updatedProducto, "Producto actualizado exitosamente");
            System.out.println("Producto actualizado: " + id + " - " + updatedProducto.getNombre());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error al actualizar producto " + id + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error al actualizar producto", e.getMessage()));
        }
    }

    // Métodos auxiliares para respuestas consistentes
    private Map<String, Object> createSuccessResponse(Object data, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("message", message);
        response.put("timestamp", new Date());
        return response;
    }

    private Map<String, Object> createErrorResponse(String message, String error) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("data", null);
        response.put("message", message);
        response.put("error", error);
        response.put("timestamp", new Date());
        return response;
    }
}
