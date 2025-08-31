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

import com.prog3.security.Models.Mesa;
import com.prog3.security.Repositories.MesaRepository;
import com.prog3.security.Services.ResponseService;
import com.prog3.security.Services.WebSocketNotificationService;
import com.prog3.security.Utils.ApiResponse;
import com.prog3.security.DTOs.ProductoMesaRequest;

@CrossOrigin
@RestController
@RequestMapping("api/mesas")
public class MesasController {

    @Autowired
    private MesaRepository theMesaRepository;

    @Autowired
    private ResponseService responseService;

    @Autowired
    private WebSocketNotificationService webSocketService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<Mesa>>> find() {
        try {
            List<Mesa> mesas = this.theMesaRepository.findAll();
            return responseService.success(mesas, "Mesas obtenidas exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener mesas: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Mesa>> findById(@PathVariable String id) {
        try {
            Mesa mesa = this.theMesaRepository.findById(id).orElse(null);
            if (mesa == null) {
                return responseService.notFound("Mesa no encontrada con ID: " + id);
            }
            return responseService.success(mesa, "Mesa encontrada");
        } catch (Exception e) {
            return responseService.internalError("Error al buscar mesa: " + e.getMessage());
        }
    }

    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<ApiResponse<Mesa>> findByNombre(@PathVariable String nombre) {
        try {
            Mesa mesa = this.theMesaRepository.findByNombre(nombre);
            if (mesa == null) {
                return responseService.notFound("Mesa no encontrada con nombre: " + nombre);
            }
            return responseService.success(mesa, "Mesa encontrada");
        } catch (Exception e) {
            return responseService.internalError("Error al buscar mesa: " + e.getMessage());
        }
    }

    @GetMapping("/ocupadas")
    public ResponseEntity<ApiResponse<List<Mesa>>> findOcupadas() {
        try {
            List<Mesa> mesas = this.theMesaRepository.findByOcupadaTrue();
            return responseService.success(mesas, "Mesas ocupadas obtenidas");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener mesas ocupadas: " + e.getMessage());
        }
    }

    @GetMapping("/libres")
    public ResponseEntity<ApiResponse<List<Mesa>>> findLibres() {
        try {
            List<Mesa> mesas = this.theMesaRepository.findByOcupadaFalse();
            return responseService.success(mesas, "Mesas libres obtenidas");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener mesas libres: " + e.getMessage());
        }
    }

    @GetMapping("/estado/{ocupada}")
    public ResponseEntity<ApiResponse<List<Mesa>>> findByEstado(@PathVariable boolean ocupada) {
        try {
            List<Mesa> mesas = this.theMesaRepository.findByOcupada(ocupada);
            return responseService.success(mesas, "Mesas filtradas por estado obtenidas");
        } catch (Exception e) {
            return responseService.internalError("Error al filtrar mesas por estado: " + e.getMessage());
        }
    }

    @GetMapping("/total")
    public ResponseEntity<ApiResponse<List<Mesa>>> findByTotalRange(@RequestParam double min, @RequestParam double max) {
        try {
            List<Mesa> mesas = this.theMesaRepository.findByTotalBetween(min, max);
            return responseService.success(mesas, "Mesas filtradas por total obtenidas");
        } catch (Exception e) {
            return responseService.internalError("Error al filtrar mesas por total: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Mesa>> create(@RequestBody Mesa newMesa) {
        try {
            // Validar que no exista una mesa con el mismo nombre
            if (this.theMesaRepository.existsByNombre(newMesa.getNombre())) {
                return responseService.conflict("Ya existe una mesa con el nombre: " + newMesa.getNombre());
            }
            Mesa mesaCreada = this.theMesaRepository.save(newMesa);

            // Notificar por WebSocket
            webSocketService.notifyTableUpdate(mesaCreada);

            return responseService.created(mesaCreada, "Mesa creada exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al crear mesa: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Mesa>> update(@PathVariable String id, @RequestBody Mesa newMesa) {
        try {
            Mesa actualMesa = this.theMesaRepository.findById(id).orElse(null);
            if (actualMesa == null) {
                return responseService.notFound("Mesa no encontrada con ID: " + id);
            }

            // Validar que el nuevo nombre no esté en uso por otra mesa
            if (!actualMesa.getNombre().equals(newMesa.getNombre())
                    && this.theMesaRepository.existsByNombre(newMesa.getNombre())) {
                return responseService.conflict("Ya existe una mesa con el nombre: " + newMesa.getNombre());
            }

            String nombreAnterior = actualMesa.getNombre();
            actualMesa.setNombre(newMesa.getNombre());
            actualMesa.setOcupada(newMesa.isOcupada());
            actualMesa.setTotal(newMesa.getTotal());
            actualMesa.setProductosIds(newMesa.getProductosIds());

            Mesa mesaActualizada = this.theMesaRepository.save(actualMesa);

            // Notificar por WebSocket
            webSocketService.notifyTableUpdate(mesaActualizada);

            return responseService.success(mesaActualizada, "Mesa actualizada exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al actualizar mesa: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/ocupar")
    public ResponseEntity<ApiResponse<Mesa>> ocuparMesa(@PathVariable String id) {
        try {
            Mesa mesa = this.theMesaRepository.findById(id).orElse(null);
            if (mesa == null) {
                return responseService.notFound("Mesa no encontrada con ID: " + id);
            }

            if (mesa.isOcupada()) {
                return responseService.conflict("La mesa ya está ocupada");
            }

            mesa.setOcupada(true);
            mesa.setTotal(0.0); // Reiniciar total al ocupar
            mesa.getProductosIds().clear(); // Limpiar productos
            Mesa mesaActualizada = this.theMesaRepository.save(mesa);

            // Notificar por WebSocket
            webSocketService.notifyTableUpdate(mesaActualizada);

            return responseService.success(mesaActualizada, "Mesa ocupada exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al ocupar mesa: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/liberar")
    public ResponseEntity<ApiResponse<Mesa>> liberarMesa(@PathVariable String id) {
        try {
            Mesa mesa = this.theMesaRepository.findById(id).orElse(null);
            if (mesa == null) {
                return responseService.notFound("Mesa no encontrada con ID: " + id);
            }

            if (!mesa.isOcupada()) {
                return responseService.conflict("La mesa ya está libre");
            }

            mesa.setOcupada(false);
            mesa.setTotal(0.0); // Reiniciar total al liberar
            mesa.getProductosIds().clear(); // Limpiar productos
            Mesa mesaActualizada = this.theMesaRepository.save(mesa);

            // Registrar en auditoría
            // Notificar por WebSocket
            webSocketService.notifyTableUpdate(mesaActualizada);

            return responseService.success(mesaActualizada, "Mesa liberada exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al liberar mesa: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        try {
            Mesa mesa = this.theMesaRepository.findById(id).orElse(null);
            if (mesa == null) {
                return responseService.notFound("Mesa no encontrada con ID: " + id);
            }

            if (mesa.isOcupada()) {
                return responseService.conflict("No se puede eliminar una mesa ocupada");
            }

            this.theMesaRepository.delete(mesa);
            return responseService.success(null, "Mesa eliminada exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al eliminar mesa: " + e.getMessage());
        }
    }

    // Nuevos endpoints para gestión de productos en mesas
    @PostMapping("/{id}/productos")
    public ResponseEntity<ApiResponse<Mesa>> agregarProducto(
            @PathVariable String id,
            @RequestBody ProductoMesaRequest request) {
        try {
            Mesa mesa = this.theMesaRepository.findById(id).orElse(null);
            if (mesa == null) {
                return responseService.notFound("Mesa no encontrada con ID: " + id);
            }

            if (!mesa.isOcupada()) {
                return responseService.conflict("La mesa debe estar ocupada para agregar productos");
            }

            // Agregar el producto a la lista
            mesa.getProductosIds().add(request.getProductoId());

            // Recalcular el total (aquí se podría llamar a un servicio)
            // TODO: Implementar cálculo real del total con precios de productos
            mesa.setTotal(mesa.getTotal() + request.getPrecio() * request.getCantidad());

            Mesa mesaActualizada = this.theMesaRepository.save(mesa);
            return responseService.success(mesaActualizada, "Producto agregado a la mesa exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al agregar producto a la mesa: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}/productos/{productoId}")
    public ResponseEntity<ApiResponse<Mesa>> removerProducto(
            @PathVariable String id,
            @PathVariable String productoId) {
        try {
            Mesa mesa = this.theMesaRepository.findById(id).orElse(null);
            if (mesa == null) {
                return responseService.notFound("Mesa no encontrada con ID: " + id);
            }

            if (!mesa.getProductosIds().contains(productoId)) {
                return responseService.notFound("Producto no encontrado en la mesa");
            }

            // Remover el producto de la lista
            mesa.getProductosIds().remove(productoId);

            // Recalcular el total
            // TODO: Implementar cálculo real del total
            if (mesa.getProductosIds().isEmpty()) {
                mesa.setTotal(0.0);
            }

            Mesa mesaActualizada = this.theMesaRepository.save(mesa);
            return responseService.success(mesaActualizada, "Producto removido de la mesa exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al remover producto de la mesa: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/calcular-total")
    public ResponseEntity<ApiResponse<Mesa>> calcularTotal(@PathVariable String id) {
        try {
            Mesa mesa = this.theMesaRepository.findById(id).orElse(null);
            if (mesa == null) {
                return responseService.notFound("Mesa no encontrada con ID: " + id);
            }

            // TODO: Implementar cálculo real basado en productos y sus precios
            // Por ahora mantenemos el total actual
            double nuevoTotal = calcularTotalReal(mesa.getProductosIds());
            mesa.setTotal(nuevoTotal);

            Mesa mesaActualizada = this.theMesaRepository.save(mesa);
            return responseService.success(mesaActualizada, "Total de la mesa calculado exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al calcular total de la mesa: " + e.getMessage());
        }
    }

    // Método auxiliar para calcular el total real
    private double calcularTotalReal(List<String> productosIds) {
        // TODO: Implementar consulta a ProductoRepository para obtener precios reales
        // Por ahora devolvemos un cálculo simulado
        return productosIds.size() * 15000.0; // Precio promedio temporal
    }
}
