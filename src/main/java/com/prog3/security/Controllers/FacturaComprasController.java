package com.prog3.security.Controllers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;

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

    @Autowired
    private com.prog3.security.Services.FacturaCalculosService facturaCalculosService;

    /**
     * Crear una nueva factura de compras de ingredientes
     */
    @PostMapping("/crear")
    public ResponseEntity<?> crearFacturaCompras(@RequestBody Map<String, Object> datos) {
        try {
            System.out.println("🧾 Creando factura de compras con datos: " + datos);
            System.out.println("📋 Campos recibidos: " + datos.keySet());
            System.out.println("📦 Campo 'items': " + datos.get("items"));
            System.out.println("📦 Campo 'itemsIngredientes': " + datos.get("itemsIngredientes"));
            System.out.println("💰 Campo 'total': " + datos.get("total"));

            // Crear la factura
            Factura factura = new Factura();

            // Datos básicos
            // Número interno de la factura (generado por el sistema)
            factura.setNumero(facturaComprasService.generarNumeroFactura());

            // Número de factura del proveedor (campo "factura" en el formulario)
            Object numeroFacturaProveedorObj = datos.getOrDefault("numeroFacturaProveedor",
                    datos.getOrDefault("factura", null));
            if (numeroFacturaProveedorObj != null && !numeroFacturaProveedorObj.toString().trim().isEmpty()) {
                factura.setNumeroFacturaProveedor(numeroFacturaProveedorObj.toString().trim());
            }
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

            // Fechas
            // Fecha de la compra
            Object fechaObj = datos.getOrDefault("fecha", null);
            if (fechaObj != null && !fechaObj.toString().trim().isEmpty()) {
                try {
                    LocalDate fechaCompra = LocalDate.parse(fechaObj.toString().trim());
                    factura.setFecha(fechaCompra.atStartOfDay());
                } catch (Exception e) {
                    factura.setFecha(LocalDateTime.now());
                }
            } else {
                factura.setFecha(LocalDateTime.now());
            }

            // Fecha de vencimiento
            Object fechaVencimientoObj = datos.getOrDefault("fechaVencimiento", null);
            if (fechaVencimientoObj != null && !fechaVencimientoObj.toString().trim().isEmpty()) {
                try {
                    LocalDate fechaVenc = LocalDate.parse(fechaVencimientoObj.toString().trim());
                    factura.setFechaVencimiento(fechaVenc.atStartOfDay());
                } catch (Exception e) {
                    // Si falla el parseo, dejar null
                    System.out.println("⚠️ No se pudo parsear fecha de vencimiento: " + fechaVencimientoObj);
                }
            }

            // Usuario que registra
            Object registradoPorObj = datos.getOrDefault("registradoPor", "admin");
            String registradoPor = registradoPorObj != null ? registradoPorObj.toString() : "admin";
            factura.setRegistradoPor(registradoPor);

            // Observaciones/Descripción generales
            Object observacionesObj = datos.getOrDefault("descripcion", datos.getOrDefault("observaciones", ""));
            String observaciones = observacionesObj != null ? observacionesObj.toString() : "";
            factura.setObservaciones(observaciones);

            // 💰 Porcentajes de retenciones (Retención, Reteiva, Reteica)
            Object porcentajeRetencionObj = datos.getOrDefault("porcentajeRetencion", "0");
            double porcentajeRetencion = parseDoubleSeguro(porcentajeRetencionObj);
            factura.setPorcentajeRetencion(porcentajeRetencion);

            Object porcentajeReteIvaObj = datos.getOrDefault("porcentajeReteIva", "0");
            double porcentajeReteIva = parseDoubleSeguro(porcentajeReteIvaObj);
            factura.setPorcentajeReteIva(porcentajeReteIva);

            Object porcentajeReteIcaObj = datos.getOrDefault("porcentajeReteIca", "0");
            double porcentajeReteIca = parseDoubleSeguro(porcentajeReteIcaObj);
            factura.setPorcentajeReteIca(porcentajeReteIca);

            // 💸 Descuento general
            Object tipoDescuentoObj = datos.getOrDefault("tipoDescuento", "Valor");
            String tipoDescuento = tipoDescuentoObj != null ? tipoDescuentoObj.toString() : "Valor";
            factura.setTipoDescuento(tipoDescuento);

            Object descuentoGeneralObj = datos.getOrDefault("descuentoGeneral", "0");
            double descuentoGeneral = parseDoubleSeguro(descuentoGeneralObj);
            factura.setDescuentoGeneral(descuentoGeneral);

            // Procesar items de ingredientes
            // Aceptar tanto "items" como "itemsIngredientes" para compatibilidad con frontend
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> itemsData = (List<Map<String, Object>>) datos.get("items");

            // Si no hay items, buscar en itemsIngredientes (campo usado por Flutter)
            if (itemsData == null || itemsData.isEmpty()) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> itemsIngredientesData =
                        (List<Map<String, Object>>) datos.get("itemsIngredientes");
                itemsData = itemsIngredientesData;
            }

            System.out.println(
                    "📦 Items recibidos: " + (itemsData != null ? itemsData.size() : 0) + " items");

            if (itemsData != null && !itemsData.isEmpty()) {
                for (Map<String, Object> itemData : itemsData) {
                    System.out.println("📋 Procesando item: " + itemData);
                    ItemFacturaIngrediente item = crearItemIngrediente(itemData);
                    if (item != null) {
                        factura.agregarItemIngrediente(item);
                        System.out.println("✅ Item agregado: " + item.getIngredienteNombre()
                                + " | ID: " + item.getIngredienteId() + " | Cantidad: "
                                + item.getCantidad());
                    } else {
                        System.out.println(
                                "❌ Error: crearItemIngrediente devolvió null para: " + itemData);
                    }
                }
                System.out.println("📊 Total items agregados a factura: "
                        + factura.getItemsIngredientes().size());
            } else {
                System.out.println("⚠️ No se recibieron items en la factura. Datos recibidos: "
                        + datos.keySet());
            }

            // Calcular totales con el nuevo servicio de cálculos
            facturaCalculosService.calcularFactura(factura);

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
     * Crear un item de ingrediente desde los datos del request NOTA: Ahora acepta items incluso si
     * el ingrediente no existe en la BD, usando los datos que vienen del frontend directamente.
     */
    private ItemFacturaIngrediente crearItemIngrediente(Map<String, Object> itemData) {
        try {
            System.out.println("📋 Creando item desde datos: " + itemData);

            // Crear el item
            ItemFacturaIngrediente item = new ItemFacturaIngrediente();

            // Obtener ingredienteId (puede ser null para items manuales)
            Object ingredienteIdObj = itemData.get("ingredienteId");
            String ingredienteId = ingredienteIdObj != null ? ingredienteIdObj.toString() : null;
            item.setIngredienteId(ingredienteId);

            // Intentar obtener el ingrediente de la BD para datos adicionales
            Ingrediente ingrediente = null;
            if (ingredienteId != null && !ingredienteId.isEmpty()) {
                Optional<Ingrediente> ingredienteOpt =
                        ingredienteRepository.findById(ingredienteId);
                if (ingredienteOpt.isPresent()) {
                    ingrediente = ingredienteOpt.get();
                    System.out
                            .println("✅ Ingrediente encontrado en BD: " + ingrediente.getNombre());
                } else {
                    System.out.println(
                            "⚠️ Ingrediente no encontrado en BD, usando datos del request");
                }
            }

            // Nombre del ingrediente: usar del request o de la BD
            Object nombreObj = itemData.get("ingredienteNombre");
            String nombre = nombreObj != null ? nombreObj.toString()
                    : (ingrediente != null ? ingrediente.getNombre() : "Producto sin nombre");
            item.setIngredienteNombre(nombre);

            // Código interno del producto
            Object codigoObj = itemData.getOrDefault("codigo",
                    ingrediente != null ? ingrediente.get_id() : ingredienteId);
            String codigo = codigoObj != null ? codigoObj.toString() : "";
            item.setCodigo(codigo);

            // Código de barras del producto
            Object codigoBarrasObj = itemData.getOrDefault("codigoBarras", "");
            String codigoBarras = codigoBarrasObj != null ? codigoBarrasObj.toString() : "";
            item.setCodigoBarras(codigoBarras);

            // Manejar cantidad de forma segura
            double cantidad = parseDoubleSeguro(itemData.getOrDefault("cantidad", "0"));
            item.setCantidad(cantidad);

            // Unidad: usar del request o de la BD
            Object unidadObj = itemData.get("unidad");
            String unidad = unidadObj != null ? unidadObj.toString()
                    : (ingrediente != null ? ingrediente.getUnidad() : "UND");
            item.setUnidad(unidad);

            // Manejar precio unitario (valor unitario)
            double precioUnitario = parseDoubleSeguro(itemData.getOrDefault("precioUnitario",
                    itemData.getOrDefault("valorUnitario", "0")));
            item.setPrecioUnitario(precioUnitario);

            // Porcentaje de impuesto (% Imp.)
            double porcentajeImpuesto = parseDoubleSeguro(itemData.getOrDefault("porcentajeImpuesto", "0"));
            item.setPorcentajeImpuesto(porcentajeImpuesto);

            // Tipo de impuesto ("%" por defecto)
            Object tipoImpuestoObj = itemData.getOrDefault("tipoImpuesto", "%");
            String tipoImpuesto = tipoImpuestoObj != null ? tipoImpuestoObj.toString() : "%";
            item.setTipoImpuesto(tipoImpuesto);

            // Porcentaje de descuento (% Descu)
            double porcentajeDescuento = parseDoubleSeguro(itemData.getOrDefault("porcentajeDescuento", "0"));
            item.setPorcentajeDescuento(porcentajeDescuento);

            // Determinar si es descontable
            Object descontableObj = itemData.getOrDefault("descontable",
                    ingrediente != null ? Boolean.toString(ingrediente.isDescontable()) : "true");
            String descontableStr = descontableObj != null ? descontableObj.toString() : "true";
            boolean descontableItem = Boolean.parseBoolean(descontableStr);
            item.setDescontable(descontableItem);

            // Manejar observaciones de forma segura
            Object observacionesObj = itemData.getOrDefault("observaciones", "");
            String observacionesStr = observacionesObj != null ? observacionesObj.toString() : "";
            item.setObservaciones(observacionesStr);

            // Si vienen valores calculados del frontend, usarlos
            Object subtotalObj = itemData.get("subtotal");
            Object precioTotalObj = itemData.get("precioTotal");
            Object valorImpuestoObj = itemData.get("valorImpuesto");
            Object valorDescuentoObj = itemData.get("valorDescuento");

            if (subtotalObj != null || precioTotalObj != null) {
                // Usar valores del frontend
                double subtotal =
                        parseDoubleSeguro(subtotalObj != null ? subtotalObj : precioTotalObj);
                item.setValorTotal(subtotal);

                if (valorImpuestoObj != null) {
                    item.setValorImpuesto(parseDoubleSeguro(valorImpuestoObj));
                }
                if (valorDescuentoObj != null) {
                    item.setValorDescuento(parseDoubleSeguro(valorDescuentoObj));
                }

                // Calcular precio total final
                double valorImpuesto = item.getValorImpuesto();
                double valorDescuento = item.getValorDescuento();
                item.setPrecioTotal(subtotal - valorDescuento + valorImpuesto);
            } else {
                // Calcular precio total si no viene del frontend
                item.calcularPrecioTotal();
            }

            System.out.println("✅ Item creado: " + item.getIngredienteNombre()
                    + " | Código: " + item.getCodigo()
                    + " | Cantidad: " + item.getCantidad() + " " + item.getUnidad()
                    + " | Precio Unit: $" + item.getPrecioUnitario()
                    + " | Subtotal: $" + item.getValorTotal()
                    + " | % Imp: " + item.getPorcentajeImpuesto() + "%"
                    + " | Valor Imp: $" + item.getValorImpuesto()
                    + " | % Desc: " + item.getPorcentajeDescuento() + "%"
                    + " | Descontable: " + item.isDescontable()
                    + " | Total: $" + item.getPrecioTotal());

            return item;

        } catch (Exception e) {
            System.err.println("❌ Error al crear item de ingrediente: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Método auxiliar para parsear double de forma segura
     */
    private double parseDoubleSeguro(Object valor) {
        if (valor == null)
            return 0.0;
        try {
            String valorStr = valor.toString().trim();
            if (valorStr.isEmpty())
                return 0.0;
            return Double.parseDouble(valorStr);
        } catch (NumberFormatException e) {
            return 0.0;
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

    /**
     * Elimina una factura de compra y revierte los cambios en inventario y caja
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminarFacturaCompra(@PathVariable String id) {
        try {
            boolean eliminada = facturaComprasService.eliminarFacturaCompra(id);
            
            Map<String, String> response = new HashMap<>();
            if (eliminada) {
                response.put("mensaje", "Factura de compra eliminada exitosamente. Se han revertido los cambios en inventario y caja.");
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "No se pudo eliminar la factura de compra");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al eliminar factura de compra: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Actualizar una factura de compras existente. Ajusta el stock de los ingredientes según los
     * cambios en los items.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarFacturaCompras(@PathVariable String id,
            @RequestBody Map<String, Object> datos) {
        try {
            System.out.println("✏️ Actualizando factura de compras ID: " + id);
            System.out.println("📋 Datos recibidos: " + datos.keySet());

            // Buscar la factura existente
            Optional<Factura> facturaExistenteOpt = facturaComprasService.buscarFacturaPorId(id);
            if (!facturaExistenteOpt.isPresent()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Factura no encontrada con ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Factura facturaExistente = facturaExistenteOpt.get();

            // Revertir el stock de los items anteriores antes de actualizar
            facturaComprasService.revertirStockFactura(facturaExistente);

            // Actualizar datos básicos
            Object numeroFacturaProveedorObj = datos.getOrDefault("numeroFacturaProveedor",
                    datos.getOrDefault("factura", facturaExistente.getNumeroFacturaProveedor()));
            if (numeroFacturaProveedorObj != null
                    && !numeroFacturaProveedorObj.toString().trim().isEmpty()) {
                facturaExistente
                        .setNumeroFacturaProveedor(numeroFacturaProveedorObj.toString().trim());
            }

            // Datos del proveedor
            Object proveedorNitObj = datos.get("proveedorNit");
            if (proveedorNitObj != null) {
                facturaExistente.setProveedorNit(proveedorNitObj.toString().trim());
            }

            Object proveedorNombreObj = datos.get("proveedorNombre");
            if (proveedorNombreObj != null) {
                facturaExistente.setProveedorNombre(proveedorNombreObj.toString().trim());
            }

            Object telefonoObj = datos.get("proveedorTelefono");
            if (telefonoObj != null) {
                facturaExistente.setProveedorTelefono(telefonoObj.toString().trim());
            }

            Object direccionObj = datos.get("proveedorDireccion");
            if (direccionObj != null) {
                facturaExistente.setProveedorDireccion(direccionObj.toString().trim());
            }

            // Forma y medio de pago
            Object medioPagoObj = datos.get("medioPago");
            if (medioPagoObj != null) {
                facturaExistente.setMedioPago(medioPagoObj.toString());
            }

            Object formaPagoObj = datos.get("formaPago");
            if (formaPagoObj != null) {
                facturaExistente.setFormaPago(formaPagoObj.toString());
            }

            Object pagadoDesdeCajaObj = datos.get("pagadoDesdeCaja");
            if (pagadoDesdeCajaObj != null) {
                facturaExistente
                        .setPagadoDesdeCaja(Boolean.parseBoolean(pagadoDesdeCajaObj.toString()));
            }

            // Registrado por
            Object registradoPorObj = datos.get("registradoPor");
            if (registradoPorObj != null) {
                facturaExistente.setRegistradoPor(registradoPorObj.toString());
            }

            // Observaciones
            Object observacionesObj = datos.getOrDefault("descripcion", datos.get("observaciones"));
            if (observacionesObj != null) {
                facturaExistente.setObservaciones(observacionesObj.toString());
            }

            // Retenciones
            Object porcentajeRetencionObj = datos.get("porcentajeRetencion");
            if (porcentajeRetencionObj != null) {
                facturaExistente.setPorcentajeRetencion(parseDoubleSeguro(porcentajeRetencionObj));
            }

            Object porcentajeReteIvaObj = datos.get("porcentajeReteIva");
            if (porcentajeReteIvaObj != null) {
                facturaExistente.setPorcentajeReteIva(parseDoubleSeguro(porcentajeReteIvaObj));
            }

            Object porcentajeReteIcaObj = datos.get("porcentajeReteIca");
            if (porcentajeReteIcaObj != null) {
                facturaExistente.setPorcentajeReteIca(parseDoubleSeguro(porcentajeReteIcaObj));
            }

            // Descuento general
            Object tipoDescuentoObj = datos.get("tipoDescuento");
            if (tipoDescuentoObj != null) {
                facturaExistente.setTipoDescuento(tipoDescuentoObj.toString());
            }

            Object descuentoGeneralObj = datos.get("descuentoGeneral");
            if (descuentoGeneralObj != null) {
                facturaExistente.setDescuentoGeneral(parseDoubleSeguro(descuentoGeneralObj));
            }

            // Limpiar items anteriores y procesar nuevos items
            facturaExistente.getItemsIngredientes().clear();

            // Procesar items de ingredientes
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> itemsData = (List<Map<String, Object>>) datos.get("items");

            if (itemsData == null || itemsData.isEmpty()) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> itemsIngredientesData =
                        (List<Map<String, Object>>) datos.get("itemsIngredientes");
                itemsData = itemsIngredientesData;
            }

            System.out.println("📦 Items recibidos para actualización: "
                    + (itemsData != null ? itemsData.size() : 0) + " items");

            if (itemsData != null && !itemsData.isEmpty()) {
                for (Map<String, Object> itemData : itemsData) {
                    ItemFacturaIngrediente item = crearItemIngrediente(itemData);
                    if (item != null) {
                        facturaExistente.agregarItemIngrediente(item);
                        System.out.println("✅ Item actualizado: " + item.getIngredienteNombre());
                    }
                }
            }

            // Calcular totales
            facturaCalculosService.calcularFactura(facturaExistente);

            // Procesar la factura (actualizar stocks con los nuevos items)
            Factura facturaActualizada =
                    facturaComprasService.procesarFacturaCompras(facturaExistente);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Factura de compras actualizada exitosamente");
            response.put("factura", facturaActualizada);
            response.put("numeroFactura", facturaActualizada.getNumero());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error al actualizar factura de compras: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al actualizar la factura de compras: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
