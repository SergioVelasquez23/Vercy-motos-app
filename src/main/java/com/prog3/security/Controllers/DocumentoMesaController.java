package com.prog3.security.Controllers;

import com.prog3.security.Models.DocumentoMesa;
import com.prog3.security.Services.DocumentoMesaService;
import com.prog3.security.Services.ResponseService;
import com.prog3.security.Utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("api/documentos-mesa")
public class DocumentoMesaController {

    @Autowired
    private DocumentoMesaService documentoMesaService;

    @Autowired
    private ResponseService responseService;

    /**
     * Crear un nuevo documento para mesa especial
     */
    @PostMapping
    public ResponseEntity<ApiResponse<DocumentoMesa>> crearDocumento(@RequestBody Map<String, Object> request) {
        try {
            String mesaNombre = (String) request.get("mesaNombre");
            String vendedor = (String) request.get("vendedor");
            @SuppressWarnings("unchecked")
            List<String> pedidosIds = (List<String>) request.get("pedidosIds");

            if (mesaNombre == null || vendedor == null || pedidosIds == null) {
                return responseService.badRequest("Faltan campos requeridos: mesaNombre, vendedor, pedidosIds");
            }

            DocumentoMesa documento = documentoMesaService.crearDocumento(mesaNombre, vendedor, pedidosIds);
            return responseService.created(documento, "Documento creado exitosamente");

        } catch (Exception e) {
            return responseService.internalError("Error al crear documento: " + e.getMessage());
        }
    }

    /**
     * Obtener documentos por mesa
     */
    @GetMapping("/mesa/{mesaNombre}")
    public ResponseEntity<ApiResponse<List<DocumentoMesa>>> getDocumentosPorMesa(@PathVariable String mesaNombre) {
        try {
            List<DocumentoMesa> documentos = documentoMesaService.getDocumentosPorMesa(mesaNombre);
            return responseService.success(documentos, "Documentos obtenidos exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener documentos: " + e.getMessage());
        }
    }

    /**
     * Obtener documento por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DocumentoMesa>> getDocumentoPorId(@PathVariable String id) {
        try {
            DocumentoMesa documento = documentoMesaService.getDocumentoPorId(id);
            if (documento == null) {
                return responseService.notFound("Documento no encontrado");
            }
            return responseService.success(documento, "Documento obtenido exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener documento: " + e.getMessage());
        }
    }

    /**
     * Agregar pedido a documento existente
     */
    @PutMapping("/{id}/agregar-pedido")
    public ResponseEntity<ApiResponse<DocumentoMesa>> agregarPedidoADocumento(
            @PathVariable String id,
            @RequestBody Map<String, String> request) {
        try {
            String pedidoId = request.get("pedidoId");
            if (pedidoId == null) {
                return responseService.badRequest("Campo requerido: pedidoId");
            }

            DocumentoMesa documento = documentoMesaService.agregarPedidoADocumento(id, pedidoId);
            return responseService.success(documento, "Pedido agregado al documento exitosamente");

        } catch (RuntimeException e) {
            return responseService.badRequest(e.getMessage());
        } catch (Exception e) {
            return responseService.internalError("Error al agregar pedido: " + e.getMessage());
        }
    }

    /**
     * Pagar un documento
     */
    @PutMapping("/{id}/pagar")
    public ResponseEntity<ApiResponse<DocumentoMesa>> pagarDocumento(
            @PathVariable String id,
            @RequestBody Map<String, Object> request) {
        try {
            String formaPago = (String) request.get("formaPago");
            String pagadoPor = (String) request.get("pagadoPor");
            Double propina = request.get("propina") != null
                    ? Double.valueOf(request.get("propina").toString()) : 0.0;

            if (formaPago == null || pagadoPor == null) {
                return responseService.badRequest("Campos requeridos: formaPago, pagadoPor");
            }

            DocumentoMesa documento = documentoMesaService.pagarDocumento(id, formaPago, pagadoPor, propina);
            return responseService.success(documento, "Documento pagado exitosamente");

        } catch (RuntimeException e) {
            return responseService.badRequest(e.getMessage());
        } catch (Exception e) {
            return responseService.internalError("Error al pagar documento: " + e.getMessage());
        }
    }

    /**
     * Eliminar un documento (solo si no está pagado)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> eliminarDocumento(@PathVariable String id) {
        try {
            boolean eliminado = documentoMesaService.eliminarDocumento(id);
            if (eliminado) {
                return responseService.success(true, "Documento eliminado exitosamente");
            } else {
                return responseService.notFound("Documento no encontrado");
            }
        } catch (RuntimeException e) {
            return responseService.badRequest(e.getMessage());
        } catch (Exception e) {
            return responseService.internalError("Error al eliminar documento: " + e.getMessage());
        }
    }

    /**
     * Obtener documentos pendientes de una mesa
     */
    @GetMapping("/mesa/{mesaNombre}/pendientes")
    public ResponseEntity<ApiResponse<List<DocumentoMesa>>> getDocumentosPendientes(@PathVariable String mesaNombre) {
        try {
            List<DocumentoMesa> documentos = documentoMesaService.getDocumentosPendientes(mesaNombre);
            return responseService.success(documentos, "Documentos pendientes obtenidos exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener documentos pendientes: " + e.getMessage());
        }
    }

    /**
     * Obtener documentos pagados de una mesa
     */
    @GetMapping("/mesa/{mesaNombre}/pagados")
    public ResponseEntity<ApiResponse<List<DocumentoMesa>>> getDocumentosPagados(@PathVariable String mesaNombre) {
        try {
            List<DocumentoMesa> documentos = documentoMesaService.getDocumentosPagados(mesaNombre);
            return responseService.success(documentos, "Documentos pagados obtenidos exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener documentos pagados: " + e.getMessage());
        }
    }

    /**
     * Obtener resumen de una mesa especial
     */
    @GetMapping("/mesa/{mesaNombre}/resumen")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getResumenMesa(@PathVariable String mesaNombre) {
        try {
            Map<String, Object> resumen = documentoMesaService.getResumenMesa(mesaNombre);
            return responseService.success(resumen, "Resumen de mesa obtenido exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener resumen: " + e.getMessage());
        }
    }

    /**
     * Verificar si una mesa es especial
     */
    @GetMapping("/verificar-mesa-especial/{mesaNombre}")
    public ResponseEntity<ApiResponse<Boolean>> verificarMesaEspecial(@PathVariable String mesaNombre) {
        try {
            boolean esEspecial = documentoMesaService.esMesaEspecial(mesaNombre);
            return responseService.success(esEspecial, "Verificación completada");
        } catch (Exception e) {
            return responseService.internalError("Error al verificar mesa: " + e.getMessage());
        }
    }

    /**
     * Obtener documentos con pedidos completos
     */
    @GetMapping("/mesa/{mesaNombre}/completos")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDocumentosConPedidos(@PathVariable String mesaNombre) {
        try {
            List<Map<String, Object>> documentosCompletos = documentoMesaService.getDocumentosConPedidos(mesaNombre);
            return responseService.success(documentosCompletos, "Documentos con pedidos obtenidos exitosamente");
        } catch (Exception e) {
            return responseService.internalError("Error al obtener documentos completos: " + e.getMessage());
        }
    }
}
