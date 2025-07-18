package com.prog3.security.Controllers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.prog3.security.Repositories.FacturaRepository;
import com.prog3.security.Repositories.PedidoRepository;

@CrossOrigin
@RestController
@RequestMapping("api/facturas")
public class FacturaController {

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

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
