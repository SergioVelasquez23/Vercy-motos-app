package com.prog3.security.Controllers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
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

import com.prog3.security.Models.Factura;
import com.prog3.security.Models.ItemFactura;
import com.prog3.security.Models.Pedido;
import com.prog3.security.Models.Producto;
import com.prog3.security.Repositories.FacturaRepository;
import com.prog3.security.Repositories.PedidoRepository;
import com.prog3.security.Repositories.ProductoRepository;

@CrossOrigin
@RestController
@RequestMapping("api/facturas")
public class FacturaController {

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @GetMapping("")
    public ResponseEntity<List<Factura>> findAll() {
        try {
            List<Factura> facturas = facturaRepository.findAll();
            return ResponseEntity.ok(facturas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Factura> findById(@PathVariable String id) {
        try {
            Factura factura = facturaRepository.findById(id).orElse(null);
            if (factura == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(factura);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/numero/{numero}")
    public ResponseEntity<Factura> findByNumero(@PathVariable String numero) {
        try {
            Factura factura = facturaRepository.findByNumero(numero);
            if (factura == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(factura);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/nit/{nit}")
    public ResponseEntity<List<Factura>> findByNit(@PathVariable String nit) {
        try {
            List<Factura> facturas = facturaRepository.findByNit(nit);
            return ResponseEntity.ok(facturas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/telefono/{telefono}")
    public ResponseEntity<List<Factura>> findByTelefono(@PathVariable String telefono) {
        try {
            List<Factura> facturas = facturaRepository.findByClienteTelefono(telefono);
            return ResponseEntity.ok(facturas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/medio-pago/{medioPago}")
    public ResponseEntity<List<Factura>> findByMedioPago(@PathVariable String medioPago) {
        try {
            List<Factura> facturas = facturaRepository.findByMedioPago(medioPago);
            return ResponseEntity.ok(facturas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/atendido-por/{atendidoPor}")
    public ResponseEntity<List<Factura>> findByAtendidoPor(@PathVariable String atendidoPor) {
        try {
            List<Factura> facturas = facturaRepository.findByAtendidoPor(atendidoPor);
            return ResponseEntity.ok(facturas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/pendientes-pago")
    public ResponseEntity<List<Factura>> findPendientesPago() {
        try {
            List<Factura> facturas = facturaRepository.findFacturasPendientesPago();
            return ResponseEntity.ok(facturas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/ventas-dia")
    public ResponseEntity<List<Factura>> findVentasDelDia() {
        try {
            LocalDateTime inicioDia = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            LocalDateTime finDia = inicioDia.plusDays(1);
            List<Factura> facturas = facturaRepository.findVentasDelDia(inicioDia, finDia);
            return ResponseEntity.ok(facturas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/ventas-periodo")
    public ResponseEntity<List<Factura>> findVentasPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        try {
            List<Factura> facturas = facturaRepository.findByFechaBetween(fechaInicio, fechaFin);
            return ResponseEntity.ok(facturas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/lista")
    public ResponseEntity<List<Factura>> getFacturasPorFecha(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") String fechaInicio,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") String fechaFin) {
        try {
            // Convertir strings a LocalDateTime
            LocalDateTime inicio = LocalDateTime.parse(fechaInicio + "T00:00:00");
            LocalDateTime fin = LocalDateTime.parse(fechaFin + "T23:59:59");

            List<Factura> facturas = facturaRepository.findByFechaBetween(inicio, fin);
            return ResponseEntity.ok(facturas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/resumen-ventas")
    public ResponseEntity<Map<String, Object>> getResumenVentas() {
        try {
            LocalDateTime inicioDia = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            LocalDateTime finDia = inicioDia.plusDays(1);

            List<Factura> ventasHoy = facturaRepository.findVentasDelDia(inicioDia, finDia);
            List<Factura> pendientesPago = facturaRepository.findFacturasPendientesPago();

            double totalVentasHoy = ventasHoy.stream()
                    .mapToDouble(Factura::getTotal)
                    .sum();

            double totalPendiente = pendientesPago.stream()
                    .mapToDouble(Factura::getTotal)
                    .sum();

            Map<String, Object> resumen = new HashMap<>();
            resumen.put("ventasHoy", ventasHoy.size());
            resumen.put("totalVentasHoy", totalVentasHoy);
            resumen.put("pendientesPago", pendientesPago.size());
            resumen.put("totalPendiente", totalPendiente);

            return ResponseEntity.ok(resumen);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<Factura> create(@RequestBody Factura factura) {
        try {
            // Generar número consecutivo si no viene
            if (factura.getNumero() == null || factura.getNumero().isEmpty()) {
                factura.setNumero(generarNumeroConsecutivo());
            }

            // Validar que no exista el número
            if (facturaRepository.existsByNumero(factura.getNumero())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            // Calcular total
            factura.calcularTotal();

            Factura nuevaFactura = facturaRepository.save(factura);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaFactura);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/desde-pedido/{pedidoId}")
    public ResponseEntity<Factura> createFromPedido(@PathVariable String pedidoId, @RequestBody Map<String, Object> datosFactura) {
        try {
            Pedido pedido = pedidoRepository.findById(pedidoId).orElse(null);
            // En el modelo simplificado no hay referencia a pedido, crear factura simple
            if (pedido == null) {
                return ResponseEntity.notFound().build();
            }

            Factura factura = new Factura();
            factura.setNumero(generarNumeroConsecutivo());
            factura.setNit(datosFactura.getOrDefault("nit", "22222222222").toString());
            factura.setClienteTelefono(datosFactura.getOrDefault("clienteTelefono", "").toString());
            factura.setClienteDireccion(datosFactura.getOrDefault("clienteDireccion", "").toString());
            factura.setMedioPago(datosFactura.getOrDefault("medioPago", "Efectivo").toString());
            factura.setAtendidoPor(pedido.getMesero());

            // Convertir items del pedido a items de factura
            if (pedido.getItems() != null) {
                for (var itemPedido : pedido.getItems()) {
                    ItemFactura itemFactura = new ItemFactura();
                    itemFactura.setProductoId(itemPedido.getProductoId());
                    itemFactura.setCantidad(itemPedido.getCantidad());
                    itemFactura.setObservaciones(itemPedido.getNotas());
                    // Aquí deberías obtener el precio del producto desde ProductoRepository
                    // itemFactura.setPrecioUnitario(producto.getPrecio());

                    factura.agregarItem(itemFactura);
                }
            }

            factura.calcularTotal();

            Factura nuevaFactura = facturaRepository.save(factura);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaFactura);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Factura> update(@PathVariable String id, @RequestBody Factura facturaActualizada) {
        try {
            Factura facturaExistente = facturaRepository.findById(id).orElse(null);
            if (facturaExistente == null) {
                return ResponseEntity.notFound().build();
            }

            // En el modelo simplificado no hay estados, actualizar directamente
            // Actualizar campos disponibles en el modelo simplificado
            facturaExistente.setNit(facturaActualizada.getNit());
            facturaExistente.setClienteTelefono(facturaActualizada.getClienteTelefono());
            facturaExistente.setClienteDireccion(facturaActualizada.getClienteDireccion());
            facturaExistente.setItems(facturaActualizada.getItems());
            facturaExistente.setMedioPago(facturaActualizada.getMedioPago());
            facturaExistente.setFormaPago(facturaActualizada.getFormaPago());
            facturaExistente.setAtendidoPor(facturaActualizada.getAtendidoPor());

            facturaExistente.calcularTotal();

            Factura facturaGuardada = facturaRepository.save(facturaExistente);
            return ResponseEntity.ok(facturaGuardada);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/emitir")
    public ResponseEntity<Factura> emitir(@PathVariable String id) {
        try {
            Factura factura = facturaRepository.findById(id).orElse(null);
            if (factura == null) {
                return ResponseEntity.notFound().build();
            }

            // En el modelo simplificado no hay estados, solo devolver la factura
            Factura facturaGuardada = facturaRepository.save(factura);
            return ResponseEntity.ok(facturaGuardada);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/pagar")
    public ResponseEntity<Factura> registrarPago(@PathVariable String id, @RequestBody Map<String, Object> datosPago) {
        try {
            Factura factura = facturaRepository.findById(id).orElse(null);
            if (factura == null) {
                return ResponseEntity.notFound().build();
            }

            // En el modelo simplificado solo actualizar el medio de pago
            String medioPago = datosPago.getOrDefault("medioPago", factura.getMedioPago()).toString();
            factura.setMedioPago(medioPago);

            Factura facturaGuardada = facturaRepository.save(factura);
            return ResponseEntity.ok(facturaGuardada);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/anular")
    public ResponseEntity<Factura> anular(@PathVariable String id, @RequestBody Map<String, Object> datosAnulacion) {
        try {
            Factura factura = facturaRepository.findById(id).orElse(null);
            if (factura == null) {
                return ResponseEntity.notFound().build();
            }

            // En el modelo simplificado no hay estados de anulación, simplemente eliminar la factura
            facturaRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        try {
            Factura factura = facturaRepository.findById(id).orElse(null);
            if (factura == null) {
                return ResponseEntity.notFound().build();
            }

            // En el modelo simplificado permitir eliminar cualquier factura
            facturaRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/resumen-impresion/{pedidoId}")
    public ResponseEntity<Map<String, Object>> generarResumenImpresion(@PathVariable String pedidoId) {
        try {
            Pedido pedido = pedidoRepository.findById(pedidoId).orElse(null);
            if (pedido == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> resumen = new HashMap<>();

            // Información básica del pedido
            resumen.put("pedidoId", pedido.get_id());
            resumen.put("mesa", pedido.getMesa());
            resumen.put("mesero", pedido.getMesero());
            resumen.put("fecha", pedido.getFecha().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            resumen.put("hora", pedido.getFecha().format(DateTimeFormatter.ofPattern("HH:mm")));
            resumen.put("tipo", pedido.getTipo());

            // Detalle de productos con ingredientes
            List<Map<String, Object>> detalleProductos = new ArrayList<>();

            if (pedido.getItems() != null) {
                for (var itemPedido : pedido.getItems()) {
                    Map<String, Object> itemDetalle = new HashMap<>();

                    // Información básica del item
                    itemDetalle.put("cantidad", itemPedido.getCantidad());
                    itemDetalle.put("productoId", itemPedido.getProductoId());

                    // Obtener información del producto incluyendo ingredientes
                    Producto producto = productoRepository.findById(itemPedido.getProductoId()).orElse(null);
                    if (producto != null) {
                        itemDetalle.put("nombre", producto.getNombre());
                        itemDetalle.put("precio", producto.getPrecio());

                        // Ingredientes del producto
                        List<Map<String, Object>> ingredientes = new ArrayList<>();

                        // Ingredientes requeridos
                        if (producto.getIngredientesRequeridos() != null) {
                            for (var ingrediente : producto.getIngredientesRequeridos()) {
                                Map<String, Object> ingDetalle = new HashMap<>();
                                ingDetalle.put("nombre", ingrediente.getNombre());
                                ingDetalle.put("cantidad", ingrediente.getCantidadNecesaria());
                                ingDetalle.put("unidad", ingrediente.getUnidad());
                                ingDetalle.put("tipo", "requerido");
                                ingredientes.add(ingDetalle);
                            }
                        }

                        // Ingredientes opcionales
                        if (producto.getIngredientesOpcionales() != null) {
                            for (var ingrediente : producto.getIngredientesOpcionales()) {
                                Map<String, Object> ingDetalle = new HashMap<>();
                                ingDetalle.put("nombre", ingrediente.getNombre());
                                ingDetalle.put("cantidad", ingrediente.getCantidadNecesaria());
                                ingDetalle.put("unidad", ingrediente.getUnidad());
                                ingDetalle.put("tipo", "opcional");
                                ingredientes.add(ingDetalle);
                            }
                        }

                        itemDetalle.put("ingredientes", ingredientes);
                    } else {
                        itemDetalle.put("nombre", "Producto no encontrado");
                        itemDetalle.put("precio", 0.0);
                        itemDetalle.put("ingredientes", new ArrayList<>());
                    }

                    // Observaciones del item
                    if (itemPedido.getNotas() != null && !itemPedido.getNotas().trim().isEmpty()) {
                        itemDetalle.put("observaciones", itemPedido.getNotas());
                    }

                    // Calcular subtotal
                    double subtotal = itemPedido.getCantidad() * (producto != null ? producto.getPrecio() : 0.0);
                    itemDetalle.put("subtotal", subtotal);

                    detalleProductos.add(itemDetalle);
                }
            }

            resumen.put("detalleProductos", detalleProductos);

            // Calcular totales
            double totalPedido = pedido.getTotal();
            resumen.put("total", totalPedido);

            // Información adicional
            if (pedido.getNotas() != null && !pedido.getNotas().trim().isEmpty()) {
                resumen.put("observacionesGenerales", pedido.getNotas());
            }

            resumen.put("estado", pedido.getEstado());

            // Información para el encabezado de la impresión
            resumen.put("nombreRestaurante", "Sopa y Carbón");
            resumen.put("direccionRestaurante", "Dirección del restaurante");
            resumen.put("telefonoRestaurante", "Teléfono del restaurante");

            return ResponseEntity.ok(resumen);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/factura-impresion/{facturaId}")
    public ResponseEntity<Map<String, Object>> generarFacturaImpresion(@PathVariable String facturaId) {
        try {
            Factura factura = facturaRepository.findById(facturaId).orElse(null);
            if (factura == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> resumen = new HashMap<>();

            // Información básica de la factura
            resumen.put("facturaId", factura.get_id());
            resumen.put("numero", factura.getNumero());
            resumen.put("fecha", factura.getFecha().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            resumen.put("hora", factura.getFecha().format(DateTimeFormatter.ofPattern("HH:mm")));

            // Información del cliente
            resumen.put("nit", factura.getNit());
            resumen.put("clienteTelefono", factura.getClienteTelefono());
            resumen.put("clienteDireccion", factura.getClienteDireccion());
            resumen.put("atendidoPor", factura.getAtendidoPor());

            // Detalle de productos
            List<Map<String, Object>> detalleProductos = new ArrayList<>();

            if (factura.getItems() != null) {
                for (ItemFactura item : factura.getItems()) {
                    Map<String, Object> itemDetalle = new HashMap<>();
                    itemDetalle.put("cantidad", item.getCantidad());
                    itemDetalle.put("nombre", item.getProductoNombre());
                    itemDetalle.put("precioUnitario", item.getPrecioUnitario());
                    itemDetalle.put("subtotal", item.getCantidad() * item.getPrecioUnitario());

                    if (item.getObservaciones() != null && !item.getObservaciones().trim().isEmpty()) {
                        itemDetalle.put("observaciones", item.getObservaciones());
                    }

                    detalleProductos.add(itemDetalle);
                }
            }

            resumen.put("detalleProductos", detalleProductos);
            resumen.put("total", factura.getTotal());
            resumen.put("medioPago", factura.getMedioPago());
            resumen.put("formaPago", factura.getFormaPago());

            // Información para el encabezado
            resumen.put("nombreRestaurante", "Sopa y Carbón");
            resumen.put("direccionRestaurante", "Dirección del restaurante");
            resumen.put("telefonoRestaurante", "Teléfono del restaurante");

            return ResponseEntity.ok(resumen);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String generarNumeroConsecutivo() {
        try {
            Factura ultimaFactura = facturaRepository.findTopByOrderByNumeroDesc();
            if (ultimaFactura == null) {
                return "FAC-000001";
            }

            String ultimoNumero = ultimaFactura.getNumero();
            String[] partes = ultimoNumero.split("-");
            int numero = Integer.parseInt(partes[1]) + 1;

            return String.format("FAC-%06d", numero);
        } catch (Exception e) {
            // Si hay error, generar número basado en timestamp
            return "FAC-" + System.currentTimeMillis();
        }
    }
}
