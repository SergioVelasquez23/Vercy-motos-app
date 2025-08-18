package com.prog3.security.Controllers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

import com.prog3.security.Models.Proveedor;
import com.prog3.security.Repositories.ProveedorRepository;
import com.prog3.security.Services.ResponseService;
import com.prog3.security.Utils.ApiResponse;

/**
 * Controlador para manejar operaciones CRUD de proveedores
 */
@CrossOrigin
@RestController
@RequestMapping("api/proveedores")
public class ProveedorController {

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private ResponseService responseService;

    /**
     * Obtener todos los proveedores
     */
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<Proveedor>>> getAllProveedores() {
        try {
            List<Proveedor> proveedores = proveedorRepository.findAllByOrderByFechaCreacionDesc();
            return responseService.success(proveedores, "Proveedores obtenidos exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener proveedores: " + e.getMessage());
        }
    }

    /**
     * Obtener todos los proveedores activos
     */
    @GetMapping("/activos")
    public ResponseEntity<ApiResponse<List<Proveedor>>> getProveedoresActivos() {
        try {
            List<Proveedor> proveedores = proveedorRepository.findByActivoTrueOrderByNombreAsc();
            return responseService.success(proveedores, "Proveedores activos obtenidos exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener proveedores activos: " + e.getMessage());
        }
    }

    /**
     * Obtener proveedor por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Proveedor>> getProveedorById(@PathVariable String id) {
        try {
            Optional<Proveedor> proveedor = proveedorRepository.findById(id);
            if (proveedor.isPresent()) {
                return responseService.success(proveedor.get(), "Proveedor encontrado");
            } else {
                return responseService.notFound("Proveedor no encontrado con ID: " + id);
            }
        } catch (Exception e) {
            return responseService.internalError("Error al buscar proveedor: " + e.getMessage());
        }
    }

    /**
     * Buscar proveedores por texto
     */
    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<List<Proveedor>>> buscarProveedores(
            @RequestParam String texto,
            @RequestParam(defaultValue = "true") boolean soloActivos) {
        try {
            List<Proveedor> proveedores;

            if (soloActivos) {
                proveedores = proveedorRepository.buscarActivosPorTexto(texto);
            } else {
                proveedores = proveedorRepository.buscarPorTexto(texto);
            }

            return responseService.success(proveedores,
                    "Búsqueda completada. " + proveedores.size() + " proveedores encontrados");
        } catch (Exception e) {
            return responseService.internalError("Error en la búsqueda: " + e.getMessage());
        }
    }

    /**
     * Buscar proveedor por documento
     */
    @GetMapping("/documento/{documento}")
    public ResponseEntity<ApiResponse<Proveedor>> getProveedorByDocumento(@PathVariable String documento) {
        try {
            Optional<Proveedor> proveedor = proveedorRepository.findByDocumento(documento);
            if (proveedor.isPresent()) {
                return responseService.success(proveedor.get(), "Proveedor encontrado por documento");
            } else {
                return responseService.notFound("Proveedor no encontrado con documento: " + documento);
            }
        } catch (Exception e) {
            return responseService.internalError("Error al buscar proveedor por documento: " + e.getMessage());
        }
    }

    /**
     * Buscar proveedor por teléfono
     */
    @GetMapping("/telefono/{telefono}")
    public ResponseEntity<ApiResponse<Proveedor>> getProveedorByTelefono(@PathVariable String telefono) {
        try {
            Optional<Proveedor> proveedor = proveedorRepository.findByTelefono(telefono);
            if (proveedor.isPresent()) {
                return responseService.success(proveedor.get(), "Proveedor encontrado por teléfono");
            } else {
                return responseService.notFound("Proveedor no encontrado con teléfono: " + telefono);
            }
        } catch (Exception e) {
            return responseService.internalError("Error al buscar proveedor por teléfono: " + e.getMessage());
        }
    }

    /**
     * Crear nuevo proveedor
     */
    @PostMapping("")
    public ResponseEntity<ApiResponse<Proveedor>> crearProveedor(@RequestBody Proveedor proveedor) {
        try {
            // Validar campos obligatorios
            if (proveedor.getNombre() == null || proveedor.getNombre().trim().isEmpty()) {
                return responseService.badRequest("El nombre del proveedor es obligatorio");
            }

            if (proveedor.getTelefono() == null || proveedor.getTelefono().trim().isEmpty()) {
                return responseService.badRequest("El teléfono del proveedor es obligatorio");
            }

            // Validar duplicados
            String mensajeValidacion = validarDuplicados(proveedor, null);
            if (mensajeValidacion != null) {
                return responseService.badRequest(mensajeValidacion);
            }

            // Limpiar espacios y normalizar datos
            normalizarDatos(proveedor);

            // Establecer campos de auditoría
            proveedor.setFechaCreacion(LocalDateTime.now());
            proveedor.setFechaActualizacion(LocalDateTime.now());
            proveedor.setCreadoPor("sistema"); // TODO: Obtener del usuario autenticado

            Proveedor proveedorGuardado = proveedorRepository.save(proveedor);
            return responseService.created(proveedorGuardado, "Proveedor creado exitosamente");

        } catch (Exception e) {
            return responseService.internalError("Error al crear proveedor: " + e.getMessage());
        }
    }

    /**
     * Actualizar proveedor
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Proveedor>> actualizarProveedor(
            @PathVariable String id, @RequestBody Proveedor proveedorActualizado) {
        try {
            Optional<Proveedor> proveedorExistente = proveedorRepository.findById(id);
            if (!proveedorExistente.isPresent()) {
                return responseService.notFound("Proveedor no encontrado con ID: " + id);
            }

            // Validar campos obligatorios
            if (proveedorActualizado.getNombre() == null || proveedorActualizado.getNombre().trim().isEmpty()) {
                return responseService.badRequest("El nombre del proveedor es obligatorio");
            }

            if (proveedorActualizado.getTelefono() == null || proveedorActualizado.getTelefono().trim().isEmpty()) {
                return responseService.badRequest("El teléfono del proveedor es obligatorio");
            }

            // Validar duplicados (excluyendo el registro actual)
            String mensajeValidacion = validarDuplicados(proveedorActualizado, id);
            if (mensajeValidacion != null) {
                return responseService.badRequest(mensajeValidacion);
            }

            Proveedor proveedor = proveedorExistente.get();

            // Actualizar campos
            proveedor.setNombre(proveedorActualizado.getNombre());
            proveedor.setTelefono(proveedorActualizado.getTelefono());
            proveedor.setNombreComercial(proveedorActualizado.getNombreComercial());
            proveedor.setDocumento(proveedorActualizado.getDocumento());
            proveedor.setEmail(proveedorActualizado.getEmail());
            proveedor.setDireccion(proveedorActualizado.getDireccion());
            proveedor.setPaginaWeb(proveedorActualizado.getPaginaWeb());
            proveedor.setContacto(proveedorActualizado.getContacto());
            proveedor.setNota(proveedorActualizado.getNota());
            proveedor.setActivo(proveedorActualizado.isActivo());

            // Limpiar espacios y normalizar datos
            normalizarDatos(proveedor);

            // Actualizar campos de auditoría
            proveedor.setFechaActualizacion(LocalDateTime.now());
            proveedor.setActualizadoPor("sistema"); // TODO: Obtener del usuario autenticado

            Proveedor proveedorGuardado = proveedorRepository.save(proveedor);
            return responseService.success(proveedorGuardado, "Proveedor actualizado exitosamente");

        } catch (Exception e) {
            return responseService.internalError("Error al actualizar proveedor: " + e.getMessage());
        }
    }

    /**
     * Cambiar estado activo/inactivo de un proveedor
     */
    @PutMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<Proveedor>> cambiarEstado(
            @PathVariable String id, @RequestBody Map<String, Boolean> estado) {
        try {
            Optional<Proveedor> proveedorOpt = proveedorRepository.findById(id);
            if (!proveedorOpt.isPresent()) {
                return responseService.notFound("Proveedor no encontrado con ID: " + id);
            }

            Proveedor proveedor = proveedorOpt.get();
            boolean nuevoEstado = estado.getOrDefault("activo", true);

            proveedor.setActivo(nuevoEstado);
            proveedor.setFechaActualizacion(LocalDateTime.now());
            proveedor.setActualizadoPor("sistema"); // TODO: Obtener del usuario autenticado

            Proveedor proveedorGuardado = proveedorRepository.save(proveedor);

            String mensaje = nuevoEstado ? "Proveedor activado exitosamente" : "Proveedor desactivado exitosamente";
            return responseService.success(proveedorGuardado, mensaje);

        } catch (Exception e) {
            return responseService.internalError("Error al cambiar estado del proveedor: " + e.getMessage());
        }
    }

    /**
     * Eliminar proveedor (solo si no tiene facturas asociadas)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarProveedor(@PathVariable String id) {
        try {
            Optional<Proveedor> proveedor = proveedorRepository.findById(id);
            if (!proveedor.isPresent()) {
                return responseService.notFound("Proveedor no encontrado con ID: " + id);
            }

            // TODO: Verificar que no tenga facturas asociadas antes de eliminar
            // Esto requiere consultar el FacturaRepository
            proveedorRepository.deleteById(id);
            return responseService.success(null, "Proveedor eliminado exitosamente");

        } catch (Exception e) {
            return responseService.internalError("Error al eliminar proveedor: " + e.getMessage());
        }
    }

    /**
     * Obtener estadísticas de proveedores
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEstadisticas() {
        try {
            Map<String, Object> estadisticas = new HashMap<>();

            long totalProveedores = proveedorRepository.count();
            long proveedoresActivos = proveedorRepository.findByActivoTrue().size();
            long proveedoresInactivos = proveedorRepository.findByActivoFalse().size();

            estadisticas.put("total", totalProveedores);
            estadisticas.put("activos", proveedoresActivos);
            estadisticas.put("inactivos", proveedoresInactivos);
            estadisticas.put("porcentajeActivos",
                    totalProveedores > 0 ? (proveedoresActivos * 100.0 / totalProveedores) : 0);

            return responseService.success(estadisticas, "Estadísticas obtenidas exitosamente");

        } catch (Exception e) {
            return responseService.internalError("Error al obtener estadísticas: " + e.getMessage());
        }
    }

    /**
     * Validar duplicados
     */
    private String validarDuplicados(Proveedor proveedor, String idExcluir) {
        // Validar nombre único
        List<Proveedor> proveedoresConMismoNombre = (idExcluir != null)
                ? proveedorRepository.findByNombreAndIdNot(proveedor.getNombre(), idExcluir)
                : (proveedorRepository.existsByNombre(proveedor.getNombre())
                ? List.of(new Proveedor()) : List.of());

        if (!proveedoresConMismoNombre.isEmpty()) {
            return "Ya existe un proveedor con el nombre: " + proveedor.getNombre();
        }

        // Validar teléfono único
        List<Proveedor> proveedoresConMismoTelefono = (idExcluir != null)
                ? proveedorRepository.findByTelefonoAndIdNot(proveedor.getTelefono(), idExcluir)
                : (proveedorRepository.existsByTelefono(proveedor.getTelefono())
                ? List.of(new Proveedor()) : List.of());

        if (!proveedoresConMismoTelefono.isEmpty()) {
            return "Ya existe un proveedor con el teléfono: " + proveedor.getTelefono();
        }

        // Validar documento único (si se proporciona)
        if (proveedor.getDocumento() != null && !proveedor.getDocumento().trim().isEmpty()) {
            List<Proveedor> proveedoresConMismoDocumento = (idExcluir != null)
                    ? proveedorRepository.findByDocumentoAndIdNot(proveedor.getDocumento(), idExcluir)
                    : (proveedorRepository.existsByDocumento(proveedor.getDocumento())
                    ? List.of(new Proveedor()) : List.of());

            if (!proveedoresConMismoDocumento.isEmpty()) {
                return "Ya existe un proveedor con el documento: " + proveedor.getDocumento();
            }
        }

        // Validar email único (si se proporciona)
        if (proveedor.getEmail() != null && !proveedor.getEmail().trim().isEmpty()) {
            List<Proveedor> proveedoresConMismoEmail = (idExcluir != null)
                    ? proveedorRepository.findByEmailAndIdNot(proveedor.getEmail(), idExcluir)
                    : (proveedorRepository.existsByEmail(proveedor.getEmail())
                    ? List.of(new Proveedor()) : List.of());

            if (!proveedoresConMismoEmail.isEmpty()) {
                return "Ya existe un proveedor con el email: " + proveedor.getEmail();
            }
        }

        return null; // No hay duplicados
    }

    /**
     * Normalizar datos del proveedor
     */
    private void normalizarDatos(Proveedor proveedor) {
        if (proveedor.getNombre() != null) {
            proveedor.setNombre(proveedor.getNombre().trim());
        }
        if (proveedor.getNombreComercial() != null) {
            proveedor.setNombreComercial(proveedor.getNombreComercial().trim().isEmpty()
                    ? null : proveedor.getNombreComercial().trim());
        }
        if (proveedor.getDocumento() != null) {
            proveedor.setDocumento(proveedor.getDocumento().trim().isEmpty()
                    ? null : proveedor.getDocumento().trim());
        }
        if (proveedor.getEmail() != null) {
            proveedor.setEmail(proveedor.getEmail().trim().toLowerCase().isEmpty()
                    ? null : proveedor.getEmail().trim().toLowerCase());
        }
        if (proveedor.getTelefono() != null) {
            proveedor.setTelefono(proveedor.getTelefono().trim());
        }
        if (proveedor.getDireccion() != null) {
            proveedor.setDireccion(proveedor.getDireccion().trim().isEmpty()
                    ? null : proveedor.getDireccion().trim());
        }
        if (proveedor.getPaginaWeb() != null) {
            proveedor.setPaginaWeb(proveedor.getPaginaWeb().trim().isEmpty()
                    ? null : proveedor.getPaginaWeb().trim());
        }
        if (proveedor.getContacto() != null) {
            proveedor.setContacto(proveedor.getContacto().trim().isEmpty()
                    ? null : proveedor.getContacto().trim());
        }
        if (proveedor.getNota() != null) {
            proveedor.setNota(proveedor.getNota().trim().isEmpty()
                    ? null : proveedor.getNota().trim());
        }
    }
}
