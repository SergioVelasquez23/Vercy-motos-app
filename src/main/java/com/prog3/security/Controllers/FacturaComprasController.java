package com.prog3.security.Controllers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.prog3.security.Models.Factura;
import com.prog3.security.Models.ItemFacturaIngrediente;
import com.prog3.security.Models.Ingrediente;
import com.prog3.security.Models.Proveedor;
import com.prog3.security.Repositories.FacturaRepository;
import com.prog3.security.Repositories.IngredienteRepository;
import com.prog3.security.Repositories.ProveedorRepository;
import com.prog3.security.Services.FacturaComprasService;

/**
 * Controlador para manejar facturas de compras de ingredientes
 */
@CrossOrigin
@RestController
@RequestMapping("api/facturas-compras")
public class FacturaComprasController {

    @Autowired
    private FacturaComprasService facturaComprasService;

    @Autowired
    private IngredienteRepository ingredienteRepository;

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    /**
     * Crear una nueva factura de compras de ingredientes
     */
    @PostMapping("/crear")
    public ResponseEntity<?> crearFacturaCompras(@RequestBody Map<String, Object> datos) {
        try {
            System.out.println("🧾 Creando factura de compras con datos: " + datos);

            // Crear la factura
            Factura factura = new Factura();

            // Datos básicos
            factura.setNumero(facturaComprasService.generarNumeroFactura());
            factura.setTipoFactura("compra"); // Establecer como factura de compra

            // Datos del proveedor (opcionales)
            Object proveedorNitObj = datos.getOrDefault("proveedorNit", "");
            Object proveedorNombreObj = datos.getOrDefault("proveedorNombre", "Proveedor general");

            String proveedorNit = (proveedorNitObj != null ? proveedorNitObj.toString() : "").trim();
            String proveedorNombre = (proveedorNombreObj != null ? proveedorNombreObj.toString() : "Proveedor general").trim();

            // Si no se proporciona nombre, usar un valor por defecto
            if (proveedorNombre.isEmpty()) {
                proveedorNombre = "Proveedor general";
            }

            factura.setProveedorNit(proveedorNit.isEmpty() ? null : proveedorNit);
            factura.setProveedorNombre(proveedorNombre);

            // Manejar otros campos de forma segura
            Object telefonoObj = datos.getOrDefault("proveedorTelefono", "");
            String telefono = telefonoObj != null ? telefonoObj.toString().trim() : "";
            factura.setProveedorTelefono(telefono);

            Object direccionObj = datos.getOrDefault("proveedorDireccion", "");
            String direccion = direccionObj != null ? direccionObj.toString().trim() : "";
            factura.setProveedorDireccion(direccion);

            // Control de pago desde caja
            Object pagadoDesdeCajaObj = datos.getOrDefault("pagadoDesdeCaja", "false");
            String pagadoDesdeCajaStr = pagadoDesdeCajaObj != null ? pagadoDesdeCajaObj.toString() : "false";
            boolean pagadoDesdeCaja = Boolean.parseBoolean(pagadoDesdeCajaStr);
            factura.setPagadoDesdeCaja(pagadoDesdeCaja);

            // Información de pago de forma segura
            Object medioPagoObj = datos.getOrDefault("medioPago", "Efectivo");
            String medioPago = medioPagoObj != null ? medioPagoObj.toString() : "Efectivo";
            factura.setMedioPago(medioPago);

            Object formaPagoObj = datos.getOrDefault("formaPago", "Contado");
            String formaPago = formaPagoObj != null ? formaPagoObj.toString() : "Contado";
            factura.setFormaPago(formaPago);

            // Fechas y usuario
            factura.setFecha(LocalDateTime.now());

            Object registradoPorObj = datos.getOrDefault("registradoPor", "admin");
            String registradoPor = registradoPorObj != null ? registradoPorObj.toString() : "admin";
            factura.setRegistradoPor(registradoPor);

            // Observaciones generales de forma segura
            Object observacionesObj = datos.getOrDefault("observaciones", "");
            String observaciones = observacionesObj != null ? observacionesObj.toString() : "";
            factura.setObservaciones(observaciones);

            // Procesar items de ingredientes
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> itemsData = (List<Map<String, Object>>) datos.get("items");

            if (itemsData != null && !itemsData.isEmpty()) {
                for (Map<String, Object> itemData : itemsData) {
                    ItemFacturaIngrediente item = crearItemIngrediente(itemData);
                    if (item != null) {
                        factura.agregarItemIngrediente(item);
                    }
                }
            }

            // Calcular totales
            factura.calcularTotales();

            // Procesar la factura (actualizar stocks si corresponde)
            Factura facturaGuardada = facturaComprasService.procesarFacturaCompras(factura);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Factura de compras creada exitosamente");
            response.put("factura", facturaGuardada);
            response.put("numeroFactura", facturaGuardada.getNumero());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            System.err.println("❌ Error al crear factura de compras: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al crear la factura de compras: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Crear un item de ingrediente desde los datos del request
     */
    private ItemFacturaIngrediente crearItemIngrediente(Map<String, Object> itemData) {
        try {
            // Verificar que el ingredienteId no sea null
            Object ingredienteIdObj = itemData.get("ingredienteId");
            if (ingredienteIdObj == null) {
                System.err.println("⚠️ ingredienteId es null en itemData: " + itemData);
                return null;
            }

            String ingredienteId = ingredienteIdObj.toString();

            // Obtener el ingrediente
            Optional<Ingrediente> ingredienteOpt = ingredienteRepository.findById(ingredienteId);
            if (!ingredienteOpt.isPresent()) {
                System.err.println("⚠️ Ingrediente no encontrado: " + ingredienteId);
                return null;
            }

            Ingrediente ingrediente = ingredienteOpt.get();

            // Crear el item
            ItemFacturaIngrediente item = new ItemFacturaIngrediente();
            item.setIngredienteId(ingredienteId);
            item.setIngredienteNombre(ingrediente.getNombre());

            // Manejar cantidad de forma segura
            Object cantidadObj = itemData.getOrDefault("cantidad", "0");
            String cantidadStr = cantidadObj != null ? cantidadObj.toString() : "0";
            item.setCantidad(Double.parseDouble(cantidadStr));

            item.setUnidad(ingrediente.getUnidad());

            // Manejar precio unitario de forma segura
            Object precioObj = itemData.getOrDefault("precioUnitario", "0");
            String precioStr = precioObj != null ? precioObj.toString() : "0";
            item.setPrecioUnitario(Double.parseDouble(precioStr));

            // Determinar si es descontable (por defecto usar el valor del ingrediente)
            Object descontableObj = itemData.getOrDefault("descontable", Boolean.toString(ingrediente.isDescontable()));
            String descontableStr = descontableObj != null ? descontableObj.toString() : "false";
            boolean descontableItem = Boolean.parseBoolean(descontableStr);
            item.setDescontable(descontableItem);

            // Manejar observaciones de forma segura
            Object observacionesObj = itemData.getOrDefault("observaciones", "");
            String observacionesStr = observacionesObj != null ? observacionesObj.toString() : "";
            item.setObservaciones(observacionesStr);

            // Calcular precio total
            item.calcularPrecioTotal();

            System.out.println("✅ Item creado: " + ingrediente.getNombre()
                    + " (" + item.getCantidad() + " " + item.getUnidad()
                    + ") - Descontable: " + item.isDescontable()
                    + " - Precio: $" + item.getPrecioTotal());

            return item;

        } catch (Exception e) {
            System.err.println("❌ Error al crear item de ingrediente: " + e.getMessage());
            return null;
        }
    }

    /**
     * Obtener todas las facturas de compras
     */
    @GetMapping("")
    public ResponseEntity<List<Factura>> obtenerTodasLasFacturas() {
        try {
            List<Factura> facturas = facturaComprasService.obtenerTodasLasFacturas();
            return ResponseEntity.ok(facturas);
        } catch (Exception e) {
            System.err.println("❌ Error al obtener facturas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Obtener una factura por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerFacturaPorId(@PathVariable String id) {
        try {
            Optional<Factura> factura = facturaComprasService.buscarFacturaPorId(id);

            if (factura.isPresent()) {
                return ResponseEntity.ok(factura.get());
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Factura no encontrada");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

        } catch (Exception e) {
            System.err.println("❌ Error al obtener factura: " + e.getMessage());

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al obtener la factura: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Buscar facturas por proveedor
     */
    @GetMapping("/proveedor/{proveedorNit}")
    public ResponseEntity<List<Factura>> buscarFacturasPorProveedor(@PathVariable String proveedorNit) {
        try {
            List<Factura> facturas = facturaComprasService.buscarFacturasPorProveedor(proveedorNit);
            return ResponseEntity.ok(facturas);
        } catch (Exception e) {
            System.err.println("❌ Error al buscar facturas por proveedor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Generar número de factura único
     */
    @GetMapping("/numero-factura")
    public ResponseEntity<Map<String, String>> generarNumeroFactura() {
        try {
            String numero = facturaComprasService.generarNumeroFactura();

            Map<String, String> response = new HashMap<>();
            response.put("numeroFactura", numero);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error al generar número de factura: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Obtener ingredientes disponibles para facturación
     */
    @GetMapping("/ingredientes")
    public ResponseEntity<List<Ingrediente>> obtenerIngredientesDisponibles(
            @RequestParam(required = false) String busqueda) {
        try {
            List<Ingrediente> ingredientes;

            if (busqueda != null && !busqueda.trim().isEmpty()) {
                ingredientes = ingredienteRepository.findByNombreContainingIgnoreCase(busqueda);
            } else {
                ingredientes = ingredienteRepository.findAll();
            }

            return ResponseEntity.ok(ingredientes);

        } catch (Exception e) {
            System.err.println("❌ Error al obtener ingredientes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Obtener proveedores activos para facturas de compras
     */
    @GetMapping("/proveedores")
    public ResponseEntity<List<Proveedor>> obtenerProveedoresDisponibles(
            @RequestParam(required = false) String busqueda) {
        try {
            List<Proveedor> proveedores;

            if (busqueda != null && !busqueda.trim().isEmpty()) {
                proveedores = proveedorRepository.buscarActivosPorTexto(busqueda);
            } else {
                proveedores = proveedorRepository.findByActivoTrueOrderByNombreAsc();
            }

            return ResponseEntity.ok(proveedores);

        } catch (Exception e) {
            System.err.println("❌ Error al obtener proveedores: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Obtener facturas de compras pagadas desde caja
     */
    @GetMapping("/pagadas-desde-caja")
    public ResponseEntity<List<Factura>> obtenerFacturasPagadasDesdeCaja() {
        try {
            List<Factura> facturas = facturaRepository.findAll()
                    .stream()
                    .filter(f -> "compra".equals(f.getTipoFactura()) && f.isPagadoDesdeCaja())
                    .collect(java.util.stream.Collectors.toList());

            return ResponseEntity.ok(facturas);

        } catch (Exception e) {
            System.err.println("❌ Error al obtener facturas pagadas desde caja: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Obtener facturas de compras NO pagadas desde caja
     */
    @GetMapping("/no-pagadas-desde-caja")
    public ResponseEntity<List<Factura>> obtenerFacturasNoPagadasDesdeCaja() {
        try {
            List<Factura> facturas = facturaRepository.findAll()
                    .stream()
                    .filter(f -> "compra".equals(f.getTipoFactura()) && !f.isPagadoDesdeCaja())
                    .collect(java.util.stream.Collectors.toList());

            return ResponseEntity.ok(facturas);

        } catch (Exception e) {
            System.err.println("❌ Error al obtener facturas no pagadas desde caja: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Obtener resumen de facturas de compras por filtro de pago desde caja
     */
    @GetMapping("/resumen-pago-caja")
    public ResponseEntity<Map<String, Object>> obtenerResumenPagoCaja() {
        try {
            List<Factura> todasCompras = facturaRepository.findAll()
                    .stream()
                    .filter(f -> "compra".equals(f.getTipoFactura()))
                    .collect(java.util.stream.Collectors.toList());

            List<Factura> pagadasDesdeCaja = todasCompras.stream()
                    .filter(Factura::isPagadoDesdeCaja)
                    .collect(java.util.stream.Collectors.toList());

            List<Factura> noPagadasDesdeCaja = todasCompras.stream()
                    .filter(f -> !f.isPagadoDesdeCaja())
                    .collect(java.util.stream.Collectors.toList());

            double totalPagadasDesdeCaja = pagadasDesdeCaja.stream()
                    .mapToDouble(Factura::getTotal)
                    .sum();

            double totalNoPagadasDesdeCaja = noPagadasDesdeCaja.stream()
                    .mapToDouble(Factura::getTotal)
                    .sum();

            Map<String, Object> resumen = new HashMap<>();
            resumen.put("totalFacturas", todasCompras.size());
            resumen.put("facturasPagadasDesdeCaja", pagadasDesdeCaja.size());
            resumen.put("facturasNoPagadasDesdeCaja", noPagadasDesdeCaja.size());
            resumen.put("totalPagadasDesdeCaja", totalPagadasDesdeCaja);
            resumen.put("totalNoPagadasDesdeCaja", totalNoPagadasDesdeCaja);
            resumen.put("totalGeneral", totalPagadasDesdeCaja + totalNoPagadasDesdeCaja);

            return ResponseEntity.ok(resumen);

        } catch (Exception e) {
            System.err.println("❌ Error al obtener resumen de pago desde caja: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
