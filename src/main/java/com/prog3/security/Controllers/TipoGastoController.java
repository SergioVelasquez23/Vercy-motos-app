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

import com.prog3.security.DTOs.TipoGastoRequest;
import com.prog3.security.Models.TipoGasto;
import com.prog3.security.Services.TipoGastoService;
import com.prog3.security.Services.ResponseService;
import com.prog3.security.Utils.ApiResponse;

@CrossOrigin
@RestController
@RequestMapping("api/tipos-gasto")
public class TipoGastoController {

    @Autowired
    private TipoGastoService tipoGastoService;

    @Autowired
    private ResponseService responseService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<TipoGasto>>> getAllTiposGasto(
            @RequestParam(required = false) Boolean activos) {
        try {
            List<TipoGasto> tipos;
            if (Boolean.TRUE.equals(activos)) {
                tipos = tipoGastoService.obtenerTiposGastoActivos();
            } else {
                tipos = tipoGastoService.obtenerTodosTiposGasto();
            }
            return responseService.success(tipos, "Tipos de gasto obtenidos exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener tipos de gasto: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TipoGasto>> getTipoGastoById(@PathVariable String id) {
        try {
            TipoGasto tipo = tipoGastoService.obtenerTipoGastoPorId(id);
            if (tipo == null) {
                return responseService.notFound("Tipo de gasto no encontrado con ID: " + id);
            }
            return responseService.success(tipo, "Tipo de gasto encontrado");
        } catch (Exception e) {
            return responseService.internalError("Error al buscar tipo de gasto: " + e.getMessage());
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<List<TipoGasto>>> searchTipoGasto(@RequestParam String nombre) {
        try {
            List<TipoGasto> tipos = tipoGastoService.buscarPorNombre(nombre);
            return responseService.success(tipos, "Tipos de gasto encontrados");
        } catch (Exception e) {
            return responseService.internalError("Error al buscar tipos de gasto: " + e.getMessage());
        }
    }

    @PostMapping("")
    public ResponseEntity<ApiResponse<TipoGasto>> createTipoGasto(@RequestBody TipoGastoRequest request) {
        try {
            if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
                return responseService.badRequest("El nombre es requerido");
            }

            TipoGasto nuevoTipo = tipoGastoService.crearTipoGasto(request);
            return responseService.created(nuevoTipo, "Tipo de gasto creado exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al crear tipo de gasto: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TipoGasto>> updateTipoGasto(
            @PathVariable String id,
            @RequestBody TipoGastoRequest request) {
        try {
            if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
                return responseService.badRequest("El nombre es requerido");
            }

            TipoGasto tipoActualizado = tipoGastoService.actualizarTipoGasto(id, request);
            if (tipoActualizado == null) {
                return responseService.notFound("Tipo de gasto no encontrado con ID: " + id);
            }

            return responseService.success(tipoActualizado, "Tipo de gasto actualizado exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al actualizar tipo de gasto: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTipoGasto(@PathVariable String id) {
        try {
            boolean eliminado = tipoGastoService.eliminarTipoGasto(id);
            if (!eliminado) {
                return responseService.notFound("Tipo de gasto no encontrado con ID: " + id);
            }

            return responseService.success(null, "Tipo de gasto eliminado exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al eliminar tipo de gasto: " + e.getMessage());
        }
    }
}
