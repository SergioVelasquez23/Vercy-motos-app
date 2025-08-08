package com.prog3.security.Services;

import com.prog3.security.Models.DocumentoMesa;
import com.prog3.security.Models.Pedido;
import com.prog3.security.Repositories.DocumentoMesaRepository;
import com.prog3.security.Repositories.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class DocumentoMesaService {

    @Autowired
    private DocumentoMesaRepository documentoMesaRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    /**
     * Genera un número de documento único basado en timestamp
     */
    private String generarNumeroDocumento() {
        long timestamp = System.currentTimeMillis();
        return String.valueOf(timestamp).substring(6); // Últimos 7 dígitos
    }

    /**
     * Crea un nuevo documento para una mesa especial
     */
    public DocumentoMesa crearDocumento(String mesaNombre, String vendedor, List<String> pedidosIds) {
        // Calcular el total sumando los pedidos
        double total = 0.0;
        for (String pedidoId : pedidosIds) {
            Pedido pedido = pedidoRepository.findById(pedidoId).orElse(null);
            if (pedido != null) {
                total += pedido.getTotal();
            }
        }

        DocumentoMesa documento = new DocumentoMesa(
                generarNumeroDocumento(),
                LocalDateTime.now(),
                total,
                vendedor,
                mesaNombre,
                pedidosIds
        );

        System.out.println("Creando documento para mesa: " + mesaNombre);
        System.out.println("Número de documento: " + documento.getNumeroDocumento());
        System.out.println("Total: " + total);
        System.out.println("Pedidos incluidos: " + pedidosIds.size());

        return documentoMesaRepository.save(documento);
    }

    /**
     * Obtiene todos los documentos de una mesa
     */
    public List<DocumentoMesa> getDocumentosPorMesa(String mesaNombre) {
        return documentoMesaRepository.findByMesaNombre(mesaNombre);
    }

    /**
     * Obtiene un documento por ID
     */
    public DocumentoMesa getDocumentoPorId(String id) {
        return documentoMesaRepository.findById(id).orElse(null);
    }

    /**
     * Agrega un pedido a un documento existente
     */
    public DocumentoMesa agregarPedidoADocumento(String documentoId, String pedidoId) {
        DocumentoMesa documento = getDocumentoPorId(documentoId);
        if (documento == null) {
            throw new RuntimeException("Documento no encontrado");
        }

        if (documento.isPagado()) {
            throw new RuntimeException("No se puede agregar pedidos a un documento ya pagado");
        }

        // Verificar que el pedido existe
        Pedido pedido = pedidoRepository.findById(pedidoId).orElse(null);
        if (pedido == null) {
            throw new RuntimeException("Pedido no encontrado");
        }

        // Agregar el pedido y recalcular total
        documento.agregarPedido(pedidoId);
        double nuevoTotal = documento.getTotal() + pedido.getTotal();
        documento.setTotal(nuevoTotal);

        System.out.println("Agregando pedido " + pedidoId + " al documento " + documentoId);
        System.out.println("Nuevo total: " + nuevoTotal);

        return documentoMesaRepository.save(documento);
    }

    /**
     * Paga un documento completo
     */
    public DocumentoMesa pagarDocumento(String documentoId, String formaPago, String pagadoPor, double propina) {
        DocumentoMesa documento = getDocumentoPorId(documentoId);
        if (documento == null) {
            throw new RuntimeException("Documento no encontrado");
        }

        if (documento.isPagado()) {
            throw new RuntimeException("El documento ya está pagado");
        }

        // Marcar documento como pagado
        documento.setPagado(true);
        documento.setFechaPago(LocalDateTime.now());
        documento.setFormaPago(formaPago);
        documento.setPagadoPor(pagadoPor);
        documento.setPropina(propina);

        // Actualizar el total si hay propina
        if (propina > 0) {
            documento.setTotal(documento.getTotal() + propina);
        }

        // Marcar todos los pedidos asociados como pagados
        for (String pedidoId : documento.getPedidosIds()) {
            Pedido pedido = pedidoRepository.findById(pedidoId).orElse(null);
            if (pedido != null && !"pagado".equals(pedido.getEstado())) {
                pedido.setEstado("pagado");
                pedido.setFormaPago(formaPago);
                pedido.setPropina(propina / documento.getPedidosIds().size()); // Distribuir propina
                pedido.setFechaPago(LocalDateTime.now());
                pedidoRepository.save(pedido);
            }
        }

        System.out.println("Pagando documento " + documentoId);
        System.out.println("Forma de pago: " + formaPago);
        System.out.println("Pagado por: " + pagadoPor);
        System.out.println("Propina: " + propina);
        System.out.println("Pedidos marcados como pagados: " + documento.getPedidosIds().size());

        return documentoMesaRepository.save(documento);
    }

    /**
     * Elimina un documento (solo si no está pagado)
     */
    public boolean eliminarDocumento(String documentoId) {
        DocumentoMesa documento = getDocumentoPorId(documentoId);
        if (documento == null) {
            return false;
        }

        if (documento.isPagado()) {
            throw new RuntimeException("No se puede eliminar un documento pagado");
        }

        // Opcional: también eliminar o actualizar los pedidos asociados
        documentoMesaRepository.deleteById(documentoId);

        System.out.println("Documento eliminado: " + documentoId);
        return true;
    }

    /**
     * Verifica si una mesa es especial
     */
    public boolean esMesaEspecial(String mesaNombre) {
        List<String> mesasEspeciales = List.of("DOMICILIO", "CAJA", "MESA AUXILIAR");
        return mesasEspeciales.contains(mesaNombre.toUpperCase());
    }

    /**
     * Obtiene documentos pendientes de una mesa
     */
    public List<DocumentoMesa> getDocumentosPendientes(String mesaNombre) {
        return documentoMesaRepository.findByMesaNombreAndPagado(mesaNombre, false);
    }

    /**
     * Obtiene documentos pagados de una mesa
     */
    public List<DocumentoMesa> getDocumentosPagados(String mesaNombre) {
        return documentoMesaRepository.findByMesaNombreAndPagado(mesaNombre, true);
    }

    /**
     * Obtiene el resumen de una mesa especial
     */
    public Map<String, Object> getResumenMesa(String mesaNombre) {
        List<DocumentoMesa> todosDocumentos = getDocumentosPorMesa(mesaNombre);

        List<DocumentoMesa> pendientes = todosDocumentos.stream()
                .filter(doc -> !doc.isPagado())
                .collect(Collectors.toList());

        List<DocumentoMesa> pagados = todosDocumentos.stream()
                .filter(DocumentoMesa::isPagado)
                .collect(Collectors.toList());

        double totalPendiente = pendientes.stream()
                .mapToDouble(DocumentoMesa::getTotal)
                .sum();

        double totalPagado = pagados.stream()
                .mapToDouble(DocumentoMesa::getTotal)
                .sum();

        Map<String, Object> resumen = new HashMap<>();
        resumen.put("totalDocumentos", todosDocumentos.size());
        resumen.put("documentosPendientes", pendientes.size());
        resumen.put("documentosPagados", pagados.size());
        resumen.put("totalPendiente", totalPendiente);
        resumen.put("totalPagado", totalPagado);
        resumen.put("totalGeneral", totalPendiente + totalPagado);

        System.out.println("Resumen para mesa " + mesaNombre + ": " + resumen);

        return resumen;
    }

    /**
     * Obtiene todos los documentos con sus pedidos completos
     */
    public List<Map<String, Object>> getDocumentosConPedidos(String mesaNombre) {
        List<DocumentoMesa> documentos = getDocumentosPorMesa(mesaNombre);

        return documentos.stream().map(doc -> {
            Map<String, Object> documentoCompleto = new HashMap<>();
            documentoCompleto.put("documento", doc);

            // Cargar pedidos completos
            List<Pedido> pedidos = doc.getPedidosIds().stream()
                    .map(pedidoId -> pedidoRepository.findById(pedidoId).orElse(null))
                    .filter(pedido -> pedido != null)
                    .collect(Collectors.toList());

            documentoCompleto.put("pedidos", pedidos);
            return documentoCompleto;
        }).collect(Collectors.toList());
    }
}
