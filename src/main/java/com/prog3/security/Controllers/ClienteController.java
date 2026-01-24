package com.prog3.security.Controllers;

import com.prog3.security.Models.Cliente;
import com.prog3.security.Services.ClienteService;
import com.prog3.security.Repositories.ClienteRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.ArrayList;

/**
 * Controlador REST para gestionar Clientes
 * Endpoints completos para el módulo de clientes con facturación electrónica
 */
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ClienteRepository clienteRepository;

    /**
     * Obtener todos los clientes
     * GET /api/clientes
     */
    @GetMapping
    public ResponseEntity<?> obtenerTodosLosClientes() {
        try {
            List<Cliente> clientes = clienteService.obtenerTodosLosClientes();
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener clientes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Obtener cliente por ID
     * GET /api/clientes/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerClientePorId(@PathVariable String id) {
        try {
            Optional<Cliente> cliente = clienteService.obtenerClientePorId(id);
            
            if (cliente.isPresent()) {
                return ResponseEntity.ok(cliente.get());
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Cliente no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener cliente: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Obtener cliente por número de identificación
     * GET /api/clientes/documento/{numeroIdentificacion}
     */
    @GetMapping("/documento/{numeroIdentificacion}")
    public ResponseEntity<?> obtenerClientePorDocumento(@PathVariable String numeroIdentificacion) {
        try {
            Optional<Cliente> cliente = clienteService.obtenerClientePorDocumento(numeroIdentificacion);
            
            if (cliente.isPresent()) {
                return ResponseEntity.ok(cliente.get());
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Cliente no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener cliente: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Crear nuevo cliente
     * POST /api/clientes
     * Body: { "tipoPersona": "Persona Natural", "numeroIdentificacion": "123456789", ... }
     */
    @PostMapping
    public ResponseEntity<?> crearCliente(
            @RequestBody Cliente cliente,
            @RequestHeader(value = "X-Usuario-Id", required = false, defaultValue = "sistema") String usuarioId) {
        try {
            // Validaciones básicas
            if (cliente.getNumeroIdentificacion() == null || cliente.getNumeroIdentificacion().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "El número de identificación es requerido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            if (cliente.getTipoPersona() == null || cliente.getTipoPersona().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "El tipo de persona es requerido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            Cliente nuevoCliente = clienteService.crearCliente(cliente, usuarioId);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoCliente);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al crear cliente: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Actualizar cliente existente
     * PUT /api/clientes/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarCliente(
            @PathVariable String id,
            @RequestBody Cliente cliente,
            @RequestHeader(value = "X-Usuario-Id", required = false, defaultValue = "sistema") String usuarioId) {
        try {
            Cliente clienteActualizado = clienteService.actualizarCliente(id, cliente, usuarioId);
            return ResponseEntity.ok(clienteActualizado);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al actualizar cliente: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Eliminar cliente (soft delete)
     * DELETE /api/clientes/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarCliente(
            @PathVariable String id,
            @RequestHeader(value = "X-Usuario-Id", required = false, defaultValue = "sistema") String usuarioId) {
        try {
            boolean eliminado = clienteService.eliminarCliente(id, usuarioId);
            
            if (eliminado) {
                Map<String, String> respuesta = new HashMap<>();
                respuesta.put("mensaje", "Cliente eliminado exitosamente");
                return ResponseEntity.ok(respuesta);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Cliente no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al eliminar cliente: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Eliminar cliente permanentemente
     * DELETE /api/clientes/{id}/permanente
     */
    @DeleteMapping("/{id}/permanente")
    public ResponseEntity<?> eliminarClientePermanente(@PathVariable String id) {
        try {
            boolean eliminado = clienteService.eliminarClientePermanente(id);
            
            if (eliminado) {
                Map<String, String> respuesta = new HashMap<>();
                respuesta.put("mensaje", "Cliente eliminado permanentemente");
                return ResponseEntity.ok(respuesta);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Cliente no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al eliminar cliente: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Obtener clientes activos
     * GET /api/clientes/estado/activos
     */
    @GetMapping("/estado/activos")
    public ResponseEntity<?> obtenerClientesActivos() {
        try {
            List<Cliente> clientes = clienteService.obtenerClientesActivos();
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener clientes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Obtener clientes por estado
     * GET /api/clientes/estado/{estado}
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<?> obtenerClientesPorEstado(@PathVariable String estado) {
        try {
            List<Cliente> clientes = clienteService.obtenerClientesPorEstado(estado);
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener clientes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Buscar clientes (búsqueda global)
     * GET /api/clientes/buscar?q=termino
     */
    @GetMapping("/buscar")
    public ResponseEntity<?> buscarClientes(@RequestParam String q) {
        try {
            List<Cliente> clientes = clienteService.buscarClientes(q);
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al buscar clientes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Obtener clientes con saldo pendiente
     * GET /api/clientes/con-saldo
     */
    @GetMapping("/con-saldo")
    public ResponseEntity<?> obtenerClientesConSaldo() {
        try {
            List<Cliente> clientes = clienteService.obtenerClientesConSaldo();
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener clientes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Bloquear cliente
     * PUT /api/clientes/{id}/bloquear
     * Body: { "motivo": "Razón del bloqueo" }
     */
    @PutMapping("/{id}/bloquear")
    public ResponseEntity<?> bloquearCliente(
            @PathVariable String id,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-Usuario-Id", required = false, defaultValue = "sistema") String usuarioId) {
        try {
            String motivo = body.get("motivo");
            if (motivo == null || motivo.isEmpty()) {
                motivo = "Sin motivo especificado";
            }
            
            Cliente cliente = clienteService.bloquearCliente(id, motivo, usuarioId);
            return ResponseEntity.ok(cliente);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al bloquear cliente: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Activar cliente
     * PUT /api/clientes/{id}/activar
     */
    @PutMapping("/{id}/activar")
    public ResponseEntity<?> activarCliente(
            @PathVariable String id,
            @RequestHeader(value = "X-Usuario-Id", required = false, defaultValue = "sistema") String usuarioId) {
        try {
            Cliente cliente = clienteService.activarCliente(id, usuarioId);
            return ResponseEntity.ok(cliente);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al activar cliente: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Actualizar saldo del cliente
     * PUT /api/clientes/{id}/saldo
     * Body: { "monto": 100000 }
     */
    @PutMapping("/{id}/saldo")
    public ResponseEntity<?> actualizarSaldo(
            @PathVariable String id,
            @RequestBody Map<String, Double> body) {
        try {
            Double monto = body.get("monto");
            if (monto == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "El monto es requerido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            Cliente cliente = clienteService.actualizarSaldo(id, monto);
            return ResponseEntity.ok(cliente);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al actualizar saldo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Verificar cupo de crédito
     * POST /api/clientes/{id}/verificar-cupo
     * Body: { "montoFactura": 500000 }
     */
    @PostMapping("/{id}/verificar-cupo")
    public ResponseEntity<?> verificarCupoCredito(
            @PathVariable String id,
            @RequestBody Map<String, Double> body) {
        try {
            Double montoFactura = body.get("montoFactura");
            if (montoFactura == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "El monto de la factura es requerido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            Map<String, Object> resultado = clienteService.verificarCupoCredito(id, montoFactura);
            return ResponseEntity.ok(resultado);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al verificar cupo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Obtener estadísticas de clientes
     * GET /api/clientes/estadisticas
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<?> obtenerEstadisticas() {
        try {
            Map<String, Object> estadisticas = clienteService.obtenerEstadisticas();
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener estadísticas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Obtener clientes por departamento
     * GET /api/clientes/departamento/{departamento}
     */
    @GetMapping("/departamento/{departamento}")
    public ResponseEntity<?> obtenerClientesPorDepartamento(@PathVariable String departamento) {
        try {
            List<Cliente> clientes = clienteService.obtenerClientesPorDepartamento(departamento);
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener clientes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Obtener clientes por ciudad
     * GET /api/clientes/ciudad/{ciudad}
     */
    @GetMapping("/ciudad/{ciudad}")
    public ResponseEntity<?> obtenerClientesPorCiudad(@PathVariable String ciudad) {
        try {
            List<Cliente> clientes = clienteService.obtenerClientesPorCiudad(ciudad);
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener clientes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Obtener clientes por vendedor
     * GET /api/clientes/vendedor/{vendedorId}
     */
    @GetMapping("/vendedor/{vendedorId}")
    public ResponseEntity<?> obtenerClientesPorVendedor(@PathVariable String vendedorId) {
        try {
            List<Cliente> clientes = clienteService.obtenerClientesPorVendedor(vendedorId);
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener clientes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ====================================
    // 📊 CARGA MASIVA DE CLIENTES DESDE EXCEL
    // ====================================
    /**
     * Carga masiva de clientes desde archivo Excel POST /api/clientes/carga-masiva
     * 
     * Columnas esperadas del Excel: IDCLIENTE, TIPOCLIENTE, TIPDOCUMENTO, CEDULA, DIGITO
     * VERIFICACION, NOMBRE, DPTO, CIUDAD, DIRECCION, TELEFONO, CORREO, MES CUMPLEAÑOS, DIA
     * CUMPLEAÑOS, TIPO CLIENTE, PAIS ENV, DIRECCION ENVIO, COD POSTAL, TELEFONO ENVIO, RESPONSABLE
     * IVA, CIUDAD ENVIO, ESTADO ENVIO, REG CLIENTE, NOM COM, TIP ESTABLECIMIENTO, LISTA PRECIO, EST
     * REG, USUARIO SISTEMA, FECHA CREACION, ZONA, PLAZO, CUPO, CONCEPTO, OBSERVACIONES, BLOQUEADO,
     * VENDEDOR, NOMBRE ALTERNATIVO
     */
    @PostMapping(value = "/carga-masiva", consumes = "multipart/form-data")
    public ResponseEntity<?> cargaMasivaClientes(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "file", required = false) MultipartFile fileParam,
            @RequestHeader(value = "X-Usuario-Id", required = false,
                    defaultValue = "sistema") String usuarioId) {

        long startTime = System.currentTimeMillis();
        System.out.println("📊 CARGA MASIVA CLIENTES - Iniciando procesamiento de Excel");

        // Usar el archivo que venga (puede venir como RequestPart o RequestParam)
        MultipartFile archivoFinal = file != null ? file : fileParam;

        int creados = 0;
        int actualizados = 0;
        int errores = 0;
        List<Map<String, Object>> detalleErrores = new ArrayList<>();

        try {
            // Validar archivo
            if (archivoFinal == null || archivoFinal.isEmpty()) {
                System.out.println("❌ Archivo no recibido o vacío");
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message",
                                "El archivo está vacío o no fue enviado"));
            }

            System.out.println("📦 Archivo recibido: " + archivoFinal.getOriginalFilename() + " ("
                    + archivoFinal.getSize() + " bytes)");

            String fileName = archivoFinal.getOriginalFilename();
            if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message",
                        "El archivo debe ser Excel (.xlsx o .xls)"));
            }

            InputStream inputStream = archivoFinal.getInputStream();
            // WorkbookFactory detecta automáticamente si es .xls (HSSF) o .xlsx (XSSF)
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            // Obtener encabezados de la primera fila
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                workbook.close();
                return ResponseEntity.badRequest().body(
                        Map.of("success", false, "message", "El archivo no tiene encabezados"));
            }

            // Mapear índices de columnas
            Map<String, Integer> columnIndex = new HashMap<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    String header = getCellValueAsString(cell).toUpperCase().trim();
                    columnIndex.put(header, i);
                }
            }

            System.out.println("📋 Columnas encontradas: " + columnIndex.keySet());

            // Procesar filas de datos (empezando desde la fila 1)
            int totalRows = sheet.getLastRowNum();
            System.out.println("📊 Total de filas a procesar: " + totalRows);

            for (int rowNum = 1; rowNum <= totalRows; rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row == null)
                    continue;

                try {
                    // Obtener cédula/documento (campo clave)
                    String cedula = getColumnValue(row, columnIndex, "CEDULA");
                    if (cedula == null || cedula.isEmpty()) {
                        cedula = getColumnValue(row, columnIndex, "DOCUMENTO");
                    }
                    if (cedula == null || cedula.isEmpty()) {
                        cedula = getColumnValue(row, columnIndex, "NUMERO IDENTIFICACION");
                    }

                    if (cedula == null || cedula.isEmpty()) {
                        // Fila vacía, saltar
                        continue;
                    }

                    // Limpiar cédula (quitar puntos, espacios, etc.)
                    cedula = cedula.replaceAll("[^0-9]", "");

                    if (cedula.isEmpty()) {
                        continue;
                    }

                    // Buscar si existe el cliente
                    Optional<Cliente> existenteOpt =
                            clienteRepository.findByNumeroIdentificacion(cedula);
                    Cliente cliente;
                    boolean esNuevo = false;

                    if (existenteOpt.isPresent()) {
                        cliente = existenteOpt.get();
                    } else {
                        cliente = new Cliente();
                        cliente.setNumeroIdentificacion(cedula);
                        esNuevo = true;
                    }

                    // Mapear campos del Excel al modelo Cliente

                    // Tipo de persona/cliente
                    String tipoCliente = getColumnValue(row, columnIndex, "TIPOCLIENTE");
                    if (tipoCliente == null)
                        tipoCliente = getColumnValue(row, columnIndex, "TIPO CLIENTE");
                    if (tipoCliente != null && !tipoCliente.isEmpty()) {
                        cliente.setTipoPersona(tipoCliente);
                    } else if (esNuevo) {
                        cliente.setTipoPersona("Persona Natural");
                    }

                    // Tipo de documento
                    String tipoDoc = getColumnValue(row, columnIndex, "TIPDOCUMENTO");
                    if (tipoDoc == null)
                        tipoDoc = getColumnValue(row, columnIndex, "TIPO DOCUMENTO");
                    if (tipoDoc != null && !tipoDoc.isEmpty()) {
                        // Convertir a formato corto
                        if (tipoDoc.toLowerCase().contains("cedula")
                                || tipoDoc.toLowerCase().contains("cédula")) {
                            cliente.setTipoIdentificacion("CC");
                        } else if (tipoDoc.toLowerCase().contains("nit")) {
                            cliente.setTipoIdentificacion("NIT");
                        } else if (tipoDoc.toLowerCase().contains("pasaporte")) {
                            cliente.setTipoIdentificacion("Pasaporte");
                        } else if (tipoDoc.toLowerCase().contains("extranjer")) {
                            cliente.setTipoIdentificacion("CE");
                        } else {
                            cliente.setTipoIdentificacion(tipoDoc);
                        }
                    } else if (esNuevo) {
                        cliente.setTipoIdentificacion("CC");
                    }

                    // Dígito de verificación
                    String dv = getColumnValue(row, columnIndex, "DIGITO VERIFICACION");
                    if (dv == null)
                        dv = getColumnValue(row, columnIndex, "DV");
                    if (dv != null && !dv.isEmpty()) {
                        cliente.setDigitoVerificacion(dv);
                    }

                    // Nombre
                    String nombre = getColumnValue(row, columnIndex, "NOMBRE");
                    if (nombre == null)
                        nombre = getColumnValue(row, columnIndex, "NOMBRES");
                    if (nombre == null)
                        nombre = getColumnValue(row, columnIndex, "RAZON SOCIAL");
                    if (nombre != null && !nombre.isEmpty()) {
                        // Separar nombres y apellidos si es posible
                        String[] partes = nombre.trim().split("\\s+", 2);
                        if (partes.length >= 2) {
                            cliente.setNombres(partes[0]);
                            cliente.setApellidos(partes[1]);
                        } else {
                            cliente.setNombres(nombre);
                        }
                        cliente.setRazonSocial(nombre);
                    }

                    // Nombre alternativo
                    String nombreAlt = getColumnValue(row, columnIndex, "NOMBRE ALTERNATIVO");
                    if (nombreAlt == null)
                        nombreAlt = getColumnValue(row, columnIndex, "NOM COM");
                    // Se puede guardar en observaciones si no hay campo específico

                    // Departamento
                    String dpto = getColumnValue(row, columnIndex, "DPTO");
                    if (dpto == null)
                        dpto = getColumnValue(row, columnIndex, "DEPARTAMENTO");
                    if (dpto != null && !dpto.isEmpty()) {
                        cliente.setDepartamento(dpto);
                    }

                    // Ciudad
                    String ciudad = getColumnValue(row, columnIndex, "CIUDAD");
                    if (ciudad != null && !ciudad.isEmpty()) {
                        cliente.setCiudad(ciudad);
                    }

                    // Dirección
                    String direccion = getColumnValue(row, columnIndex, "DIRECCION");
                    if (direccion != null && !direccion.isEmpty()) {
                        cliente.setDireccion(direccion);
                    }

                    // Teléfono
                    String telefono = getColumnValue(row, columnIndex, "TELEFONO");
                    if (telefono != null && !telefono.isEmpty()) {
                        cliente.setTelefono(telefono);
                    }

                    // Teléfono envío (como secundario)
                    String telefonoEnvio = getColumnValue(row, columnIndex, "TELEFONO ENVIO");
                    if (telefonoEnvio != null && !telefonoEnvio.isEmpty()) {
                        cliente.setTelefonoSecundario(telefonoEnvio);
                    }

                    // Correo
                    String correo = getColumnValue(row, columnIndex, "CORREO");
                    if (correo == null)
                        correo = getColumnValue(row, columnIndex, "EMAIL");
                    if (correo != null && !correo.isEmpty()) {
                        cliente.setCorreo(correo);
                    }

                    // Código postal
                    String codPostal = getColumnValue(row, columnIndex, "COD POSTAL");
                    if (codPostal == null)
                        codPostal = getColumnValue(row, columnIndex, "CODIGO POSTAL");
                    if (codPostal != null && !codPostal.isEmpty()) {
                        cliente.setCodigoPostal(codPostal);
                    }

                    // Responsable IVA
                    String respIva = getColumnValue(row, columnIndex, "RESPONSABLE IVA");
                    if (respIva != null && !respIva.isEmpty()) {
                        if (respIva.equalsIgnoreCase("SI") || respIva.equalsIgnoreCase("SÍ")
                                || respIva.equals("1")) {
                            cliente.setResponsableIVA("Sí");
                        } else {
                            cliente.setResponsableIVA("No");
                        }
                    }

                    // Zona
                    String zona = getColumnValue(row, columnIndex, "ZONA");
                    if (zona != null && !zona.isEmpty()) {
                        cliente.setZonaVentas(zona);
                    }

                    // Plazo (días de crédito)
                    String plazo = getColumnValue(row, columnIndex, "PLAZO");
                    if (plazo != null && !plazo.isEmpty()) {
                        try {
                            // Limpiar "Días", "días", etc.
                            String plazoDias = plazo.replaceAll("[^0-9]", "");
                            if (!plazoDias.isEmpty()) {
                                cliente.setDiasCredito(Integer.parseInt(plazoDias));
                            }
                        } catch (NumberFormatException e) {
                            // Ignorar si no es número
                        }
                    }

                    // Cupo de crédito
                    String cupo = getColumnValue(row, columnIndex, "CUPO");
                    if (cupo != null && !cupo.isEmpty()) {
                        try {
                            double cupoValue = Double.parseDouble(cupo.replaceAll("[^0-9.]", ""));
                            cliente.setCupoCredito(cupoValue);
                        } catch (NumberFormatException e) {
                            // Ignorar si no es número
                        }
                    }

                    // Vendedor
                    String vendedor = getColumnValue(row, columnIndex, "VENDEDOR");
                    if (vendedor != null && !vendedor.isEmpty()) {
                        cliente.setVendedorAsignado(vendedor);
                    }

                    // Observaciones
                    String observaciones = getColumnValue(row, columnIndex, "OBSERVACIONES");
                    String concepto = getColumnValue(row, columnIndex, "CONCEPTO");
                    StringBuilder obs = new StringBuilder();
                    if (observaciones != null && !observaciones.isEmpty()) {
                        obs.append(observaciones);
                    }
                    if (concepto != null && !concepto.isEmpty()) {
                        if (obs.length() > 0)
                            obs.append(" | ");
                        obs.append("Concepto: ").append(concepto);
                    }
                    if (nombreAlt != null && !nombreAlt.isEmpty()) {
                        if (obs.length() > 0)
                            obs.append(" | ");
                        obs.append("Nombre comercial: ").append(nombreAlt);
                    }
                    if (obs.length() > 0) {
                        cliente.setObservaciones(obs.toString());
                    }

                    // Estado (bloqueado)
                    String bloqueado = getColumnValue(row, columnIndex, "BLOQUEADO");
                    if (bloqueado != null && !bloqueado.isEmpty()) {
                        if (bloqueado.equalsIgnoreCase("SI") || bloqueado.equalsIgnoreCase("SÍ")
                                || bloqueado.equals("1")) {
                            cliente.setEstado("bloqueado");
                        } else {
                            cliente.setEstado("activo");
                        }
                    } else if (esNuevo) {
                        cliente.setEstado("activo");
                    }

                    // Usuario que crea/modifica
                    String usuarioSistema = getColumnValue(row, columnIndex, "USUARIO SISTEMA");
                    if (usuarioSistema == null || usuarioSistema.isEmpty()) {
                        usuarioSistema = usuarioId;
                    }

                    // Fechas
                    if (esNuevo) {
                        cliente.setFechaCreacion(LocalDateTime.now());
                        cliente.setCreadoPor(usuarioSistema);
                    }
                    cliente.setFechaModificacion(LocalDateTime.now());
                    cliente.setModificadoPor(usuarioSistema);

                    // Guardar cliente
                    clienteRepository.save(cliente);

                    if (esNuevo) {
                        creados++;
                    } else {
                        actualizados++;
                    }

                } catch (Exception e) {
                    errores++;
                    Map<String, Object> errorDetail = new HashMap<>();
                    errorDetail.put("fila", rowNum + 1);
                    errorDetail.put("error", e.getMessage());
                    detalleErrores.add(errorDetail);
                    System.err.println("❌ Error en fila " + (rowNum + 1) + ": " + e.getMessage());
                }
            }

            workbook.close();
            inputStream.close();

            long endTime = System.currentTimeMillis();
            System.out.println(
                    "✅ CARGA MASIVA CLIENTES completada en " + (endTime - startTime) + "ms");
            System.out.println("   Creados: " + creados + ", Actualizados: " + actualizados
                    + ", Errores: " + errores);

            Map<String, Object> resultado = new HashMap<>();
            resultado.put("success", true);
            resultado.put("message", "Carga masiva completada");
            resultado.put("creados", creados);
            resultado.put("actualizados", actualizados);
            resultado.put("errores", errores);
            resultado.put("totalProcesados", creados + actualizados);
            resultado.put("tiempoMs", endTime - startTime);
            if (!detalleErrores.isEmpty()) {
                resultado.put("detalleErrores",
                        detalleErrores.subList(0, Math.min(10, detalleErrores.size())));
            }

            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            System.err.println("❌ ERROR en carga masiva clientes: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error en carga masiva: " + e.getMessage());
            error.put("creados", creados);
            error.put("actualizados", actualizados);
            error.put("errores", errores);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ====================================
    // 🔧 MÉTODOS AUXILIARES PARA EXCEL
    // ====================================

    /**
     * Obtiene el valor de una columna por nombre
     */
    private String getColumnValue(Row row, Map<String, Integer> columnIndex, String columnName) {
        Integer index = columnIndex.get(columnName.toUpperCase().trim());
        if (index == null)
            return null;

        Cell cell = row.getCell(index);
        if (cell == null)
            return null;

        return getCellValueAsString(cell);
    }

    /**
     * Convierte una celda a String
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null)
            return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                }
                // Evitar notación científica
                double numValue = cell.getNumericCellValue();
                if (numValue == Math.floor(numValue)) {
                    return String.valueOf((long) numValue);
                }
                return String.valueOf(numValue);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    try {
                        return String.valueOf(cell.getNumericCellValue());
                    } catch (Exception e2) {
                        return "";
                    }
                }
            case BLANK:
                return "";
            default:
                return "";
        }
    }
}
