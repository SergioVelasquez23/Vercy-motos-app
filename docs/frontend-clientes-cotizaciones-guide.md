# 📱 Guía Frontend Flutter - Módulos Clientes y Cotizaciones

## 🎯 Modelos Dart a Crear

### 1️⃣ **Cliente Model** (`lib/models/cliente.dart`)

```dart
class Cliente {
  // Identificación
  String? id;
  String tipoPersona;              // "Persona Natural" | "Persona Jurídica"
  String tipoIdentificacion;       // "CC" | "NIT" | "CE" | "Pasaporte" | "TI"
  String numeroIdentificacion;     // Documento único
  String? digitoVerificacion;      // DV para NIT
  
  // Datos Personales
  String? nombres;
  String? apellidos;
  String? razonSocial;             // Auto-generado o manual
  String? correo;
  String? telefono;
  String? telefonoSecundario;
  String? direccion;
  String? departamento;
  String? ciudad;
  String? codigoPostal;
  
  // Datos Contribuyente (DIAN)
  String responsableIVA;           // "Sí" | "No" | "No Aplica"
  String calidadAgenteRetencion;   // "Autorretenedor" | "Agente de retención" | "No aplica"
  String? regimenTributario;       // "Común" | "Simplificado"
  String? responsabilidadesFiscales;
  
  // Cuentas Contables
  String? cuentasPorCobrar;
  String? cuentasParaDevoluciones;
  String? sobreabonos;
  String? deterioroCartera;
  
  // Información Comercial
  String? condicionPago;
  int diasCredito;
  double cupoCredito;
  double saldoActual;
  String? categoriaCliente;
  String? vendedorAsignado;
  String? zonaVentas;
  
  // Tracking
  DateTime? fechaCreacion;
  String? creadoPor;
  String estado;                   // "activo" | "inactivo" | "bloqueado"
  bool habilitadoFacturacionElectronica;
  
  // Constructor
  Cliente({
    this.id,
    required this.tipoPersona,
    required this.tipoIdentificacion,
    required this.numeroIdentificacion,
    this.digitoVerificacion,
    this.nombres,
    this.apellidos,
    this.razonSocial,
    this.correo,
    this.telefono,
    this.telefonoSecundario,
    this.direccion,
    this.departamento,
    this.ciudad,
    this.codigoPostal,
    this.responsableIVA = "No",
    this.calidadAgenteRetencion = "No aplica",
    this.regimenTributario,
    this.responsabilidadesFiscales,
    this.cuentasPorCobrar,
    this.cuentasParaDevoluciones,
    this.sobreabonos,
    this.deterioroCartera,
    this.condicionPago,
    this.diasCredito = 0,
    this.cupoCredito = 0.0,
    this.saldoActual = 0.0,
    this.categoriaCliente,
    this.vendedorAsignado,
    this.zonaVentas,
    this.fechaCreacion,
    this.creadoPor,
    this.estado = "activo",
    this.habilitadoFacturacionElectronica = true,
  });
  
  // Getter: cupo disponible
  double get cupoDisponible => cupoCredito - saldoActual;
  
  // Getter: tiene cupo
  bool tieneCupoDisponible(double monto) => cupoDisponible >= monto;
  
  // fromJson
  factory Cliente.fromJson(Map<String, dynamic> json) {
    return Cliente(
      id: json['_id'],
      tipoPersona: json['tipoPersona'] ?? '',
      tipoIdentificacion: json['tipoIdentificacion'] ?? '',
      numeroIdentificacion: json['numeroIdentificacion'] ?? '',
      digitoVerificacion: json['digitoVerificacion'],
      nombres: json['nombres'],
      apellidos: json['apellidos'],
      razonSocial: json['razonSocial'],
      correo: json['correo'],
      telefono: json['telefono'],
      telefonoSecundario: json['telefonoSecundario'],
      direccion: json['direccion'],
      departamento: json['departamento'],
      ciudad: json['ciudad'],
      codigoPostal: json['codigoPostal'],
      responsableIVA: json['responsableIVA'] ?? 'No',
      calidadAgenteRetencion: json['calidadAgenteRetencion'] ?? 'No aplica',
      regimenTributario: json['regimenTributario'],
      responsabilidadesFiscales: json['responsabilidadesFiscales'],
      cuentasPorCobrar: json['cuentasPorCobrar'],
      cuentasParaDevoluciones: json['cuentasParaDevoluciones'],
      sobreabonos: json['sobreabonos'],
      deterioroCartera: json['deterioroCartera'],
      condicionPago: json['condicionPago'],
      diasCredito: json['diasCredito'] ?? 0,
      cupoCredito: (json['cupoCredito'] ?? 0.0).toDouble(),
      saldoActual: (json['saldoActual'] ?? 0.0).toDouble(),
      categoriaCliente: json['categoriaCliente'],
      vendedorAsignado: json['vendedorAsignado'],
      zonaVentas: json['zonaVentas'],
      fechaCreacion: json['fechaCreacion'] != null 
        ? DateTime.parse(json['fechaCreacion']) 
        : null,
      creadoPor: json['creadoPor'],
      estado: json['estado'] ?? 'activo',
      habilitadoFacturacionElectronica: json['habilitadoFacturacionElectronica'] ?? true,
    );
  }
  
  // toJson
  Map<String, dynamic> toJson() {
    return {
      if (id != null) '_id': id,
      'tipoPersona': tipoPersona,
      'tipoIdentificacion': tipoIdentificacion,
      'numeroIdentificacion': numeroIdentificacion,
      if (digitoVerificacion != null) 'digitoVerificacion': digitoVerificacion,
      if (nombres != null) 'nombres': nombres,
      if (apellidos != null) 'apellidos': apellidos,
      if (razonSocial != null) 'razonSocial': razonSocial,
      if (correo != null) 'correo': correo,
      if (telefono != null) 'telefono': telefono,
      if (telefonoSecundario != null) 'telefonoSecundario': telefonoSecundario,
      if (direccion != null) 'direccion': direccion,
      if (departamento != null) 'departamento': departamento,
      if (ciudad != null) 'ciudad': ciudad,
      if (codigoPostal != null) 'codigoPostal': codigoPostal,
      'responsableIVA': responsableIVA,
      'calidadAgenteRetencion': calidadAgenteRetencion,
      if (regimenTributario != null) 'regimenTributario': regimenTributario,
      if (responsabilidadesFiscales != null) 'responsabilidadesFiscales': responsabilidadesFiscales,
      if (cuentasPorCobrar != null) 'cuentasPorCobrar': cuentasPorCobrar,
      if (cuentasParaDevoluciones != null) 'cuentasParaDevoluciones': cuentasParaDevoluciones,
      if (sobreabonos != null) 'sobreabonos': sobreabonos,
      if (deterioroCartera != null) 'deterioroCartera': deterioroCartera,
      if (condicionPago != null) 'condicionPago': condicionPago,
      'diasCredito': diasCredito,
      'cupoCredito': cupoCredito,
      'saldoActual': saldoActual,
      if (categoriaCliente != null) 'categoriaCliente': categoriaCliente,
      if (vendedorAsignado != null) 'vendedorAsignado': vendedorAsignado,
      if (zonaVentas != null) 'zonaVentas': zonaVentas,
      'estado': estado,
      'habilitadoFacturacionElectronica': habilitadoFacturacionElectronica,
    };
  }
}
```

### 2️⃣ **ItemCotizacion Model** (`lib/models/item_cotizacion.dart`)

```dart
class ItemCotizacion {
  String? id;
  String productoId;
  String? productoNombre;
  int cantidad;
  double precioUnitario;
  
  // Facturación
  String? codigoProducto;
  String? codigoBarras;
  String? tipoImpuesto;            // "IVA" | "INC" | "Exento"
  double porcentajeImpuesto;
  double valorImpuesto;
  double porcentajeDescuento;
  double valorDescuento;
  String? notas;
  
  ItemCotizacion({
    this.id,
    required this.productoId,
    this.productoNombre,
    this.cantidad = 1,
    this.precioUnitario = 0.0,
    this.codigoProducto,
    this.codigoBarras,
    this.tipoImpuesto,
    this.porcentajeImpuesto = 0.0,
    this.valorImpuesto = 0.0,
    this.porcentajeDescuento = 0.0,
    this.valorDescuento = 0.0,
    this.notas,
  });
  
  // Getters calculados
  double get subtotal => cantidad * precioUnitario;
  double get valorTotal => subtotal + valorImpuesto - valorDescuento;
  
  // Métodos de cálculo
  void calcularValorImpuesto() {
    valorImpuesto = (subtotal * porcentajeImpuesto) / 100.0;
  }
  
  void calcularValorDescuento() {
    valorDescuento = (subtotal * porcentajeDescuento) / 100.0;
  }
  
  // fromJson
  factory ItemCotizacion.fromJson(Map<String, dynamic> json) {
    return ItemCotizacion(
      id: json['id'],
      productoId: json['productoId'] ?? '',
      productoNombre: json['productoNombre'],
      cantidad: json['cantidad'] ?? 1,
      precioUnitario: (json['precioUnitario'] ?? 0.0).toDouble(),
      codigoProducto: json['codigoProducto'],
      codigoBarras: json['codigoBarras'],
      tipoImpuesto: json['tipoImpuesto'],
      porcentajeImpuesto: (json['porcentajeImpuesto'] ?? 0.0).toDouble(),
      valorImpuesto: (json['valorImpuesto'] ?? 0.0).toDouble(),
      porcentajeDescuento: (json['porcentajeDescuento'] ?? 0.0).toDouble(),
      valorDescuento: (json['valorDescuento'] ?? 0.0).toDouble(),
      notas: json['notas'],
    );
  }
  
  // toJson
  Map<String, dynamic> toJson() {
    return {
      if (id != null) 'id': id,
      'productoId': productoId,
      if (productoNombre != null) 'productoNombre': productoNombre,
      'cantidad': cantidad,
      'precioUnitario': precioUnitario,
      if (codigoProducto != null) 'codigoProducto': codigoProducto,
      if (codigoBarras != null) 'codigoBarras': codigoBarras,
      if (tipoImpuesto != null) 'tipoImpuesto': tipoImpuesto,
      'porcentajeImpuesto': porcentajeImpuesto,
      'valorImpuesto': valorImpuesto,
      'porcentajeDescuento': porcentajeDescuento,
      'valorDescuento': valorDescuento,
      if (notas != null) 'notas': notas,
    };
  }
}
```

### 3️⃣ **Cotizacion Model** (`lib/models/cotizacion.dart`)

```dart
class Cotizacion {
  String? id;
  DateTime fecha;
  DateTime? fechaVencimiento;
  String estado;                   // "activa" | "aceptada" | "rechazada" | "vencida" | "convertida"
  
  // Cliente
  String clienteId;
  String? clienteNombre;
  String? clienteTelefono;
  String? clienteEmail;
  
  // Items
  List<ItemCotizacion> items;
  
  // Información adicional
  String? descripcion;
  List<String> archivosAdjuntos;
  List<String> soportesPago;
  
  // Retenciones
  double retencion;
  double valorRetencion;
  double reteIVA;
  double valorReteIVA;
  double reteICA;
  double valorReteICA;
  
  // Descuentos
  String tipoDescuentoGeneral;     // "Valor" | "Porcentaje"
  double descuentoGeneral;
  double descuentoProductos;
  
  // Totales
  double subtotal;
  double totalImpuestos;
  double totalDescuentos;
  double totalRetenciones;
  double totalFinal;
  
  // Tracking
  String? numeroCotizacion;        // Auto: COT-202601-0001
  String? creadoPor;
  String? facturaRelacionadaId;
  
  Cotizacion({
    this.id,
    DateTime? fecha,
    this.fechaVencimiento,
    this.estado = 'activa',
    required this.clienteId,
    this.clienteNombre,
    this.clienteTelefono,
    this.clienteEmail,
    List<ItemCotizacion>? items,
    this.descripcion,
    List<String>? archivosAdjuntos,
    List<String>? soportesPago,
    this.retencion = 0.0,
    this.valorRetencion = 0.0,
    this.reteIVA = 0.0,
    this.valorReteIVA = 0.0,
    this.reteICA = 0.0,
    this.valorReteICA = 0.0,
    this.tipoDescuentoGeneral = 'Valor',
    this.descuentoGeneral = 0.0,
    this.descuentoProductos = 0.0,
    this.subtotal = 0.0,
    this.totalImpuestos = 0.0,
    this.totalDescuentos = 0.0,
    this.totalRetenciones = 0.0,
    this.totalFinal = 0.0,
    this.numeroCotizacion,
    this.creadoPor,
    this.facturaRelacionadaId,
  })  : this.fecha = fecha ?? DateTime.now(),
        this.items = items ?? [],
        this.archivosAdjuntos = archivosAdjuntos ?? [],
        this.soportesPago = soportesPago ?? [];
  
  // Método: calcularTotales
  void calcularTotales() {
    // 1. Calcular valores de impuestos y descuentos de cada item
    for (var item in items) {
      item.calcularValorImpuesto();
      item.calcularValorDescuento();
    }
    
    // 2. Calcular subtotal
    subtotal = items.fold(0.0, (sum, item) => sum + item.subtotal);
    
    // 3. Calcular total de impuestos
    totalImpuestos = items.fold(0.0, (sum, item) => sum + item.valorImpuesto);
    
    // 4. Calcular descuento de productos
    descuentoProductos = items.fold(0.0, (sum, item) => sum + item.valorDescuento);
    
    // 5. Calcular descuento general (si es porcentaje)
    if (tipoDescuentoGeneral == 'Porcentaje') {
      descuentoGeneral = (subtotal * descuentoGeneral) / 100.0;
    }
    
    // 6. Calcular total de descuentos
    totalDescuentos = descuentoGeneral + descuentoProductos;
    
    // 7. Calcular retenciones
    valorRetencion = (subtotal * retencion) / 100.0;
    valorReteIVA = (totalImpuestos * reteIVA) / 100.0;
    valorReteICA = (subtotal * reteICA) / 100.0;
    totalRetenciones = valorRetencion + valorReteIVA + valorReteICA;
    
    // 8. Calcular total final
    totalFinal = subtotal + totalImpuestos - totalDescuentos - totalRetenciones;
  }
  
  // fromJson
  factory Cotizacion.fromJson(Map<String, dynamic> json) {
    return Cotizacion(
      id: json['_id'],
      fecha: json['fecha'] != null ? DateTime.parse(json['fecha']) : DateTime.now(),
      fechaVencimiento: json['fechaVencimiento'] != null 
        ? DateTime.parse(json['fechaVencimiento']) 
        : null,
      estado: json['estado'] ?? 'activa',
      clienteId: json['clienteId'] ?? '',
      clienteNombre: json['clienteNombre'],
      clienteTelefono: json['clienteTelefono'],
      clienteEmail: json['clienteEmail'],
      items: (json['items'] as List?)
        ?.map((item) => ItemCotizacion.fromJson(item))
        .toList() ?? [],
      descripcion: json['descripcion'],
      archivosAdjuntos: List<String>.from(json['archivosAdjuntos'] ?? []),
      soportesPago: List<String>.from(json['soportesPago'] ?? []),
      retencion: (json['retencion'] ?? 0.0).toDouble(),
      valorRetencion: (json['valorRetencion'] ?? 0.0).toDouble(),
      reteIVA: (json['reteIVA'] ?? 0.0).toDouble(),
      valorReteIVA: (json['valorReteIVA'] ?? 0.0).toDouble(),
      reteICA: (json['reteICA'] ?? 0.0).toDouble(),
      valorReteICA: (json['valorReteICA'] ?? 0.0).toDouble(),
      tipoDescuentoGeneral: json['tipoDescuentoGeneral'] ?? 'Valor',
      descuentoGeneral: (json['descuentoGeneral'] ?? 0.0).toDouble(),
      descuentoProductos: (json['descuentoProductos'] ?? 0.0).toDouble(),
      subtotal: (json['subtotal'] ?? 0.0).toDouble(),
      totalImpuestos: (json['totalImpuestos'] ?? 0.0).toDouble(),
      totalDescuentos: (json['totalDescuentos'] ?? 0.0).toDouble(),
      totalRetenciones: (json['totalRetenciones'] ?? 0.0).toDouble(),
      totalFinal: (json['totalFinal'] ?? 0.0).toDouble(),
      numeroCotizacion: json['numeroCotizacion'],
      creadoPor: json['creadoPor'],
      facturaRelacionadaId: json['facturaRelacionadaId'],
    );
  }
  
  // toJson
  Map<String, dynamic> toJson() {
    return {
      if (id != null) '_id': id,
      'fecha': fecha.toIso8601String(),
      if (fechaVencimiento != null) 'fechaVencimiento': fechaVencimiento!.toIso8601String(),
      'estado': estado,
      'clienteId': clienteId,
      if (clienteNombre != null) 'clienteNombre': clienteNombre,
      if (clienteTelefono != null) 'clienteTelefono': clienteTelefono,
      if (clienteEmail != null) 'clienteEmail': clienteEmail,
      'items': items.map((item) => item.toJson()).toList(),
      if (descripcion != null) 'descripcion': descripcion,
      'archivosAdjuntos': archivosAdjuntos,
      'soportesPago': soportesPago,
      'retencion': retencion,
      'valorRetencion': valorRetencion,
      'reteIVA': reteIVA,
      'valorReteIVA': valorReteIVA,
      'reteICA': reteICA,
      'valorReteICA': valorReteICA,
      'tipoDescuentoGeneral': tipoDescuentoGeneral,
      'descuentoGeneral': descuentoGeneral,
      'descuentoProductos': descuentoProductos,
      'subtotal': subtotal,
      'totalImpuestos': totalImpuestos,
      'totalDescuentos': totalDescuentos,
      'totalRetenciones': totalRetenciones,
      'totalFinal': totalFinal,
      if (numeroCotizacion != null) 'numeroCotizacion': numeroCotizacion,
      if (creadoPor != null) 'creadoPor': creadoPor,
      if (facturaRelacionadaId != null) 'facturaRelacionadaId': facturaRelacionadaId,
    };
  }
}
```

---

## 🛠️ Servicios/Providers a Crear

### **ClienteService** (`lib/services/cliente_service.dart`)

```dart
import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/cliente.dart';

class ClienteService {
  final String baseUrl = "https://tu-api.render.com/api/clientes";
  
  // CRUD
  Future<List<Cliente>> obtenerClientes() async {
    final response = await http.get(Uri.parse(baseUrl));
    if (response.statusCode == 200) {
      List<dynamic> data = json.decode(response.body);
      return data.map((json) => Cliente.fromJson(json)).toList();
    }
    throw Exception('Error al obtener clientes');
  }
  
  Future<Cliente?> obtenerClientePorId(String id) async {
    final response = await http.get(Uri.parse('$baseUrl/$id'));
    if (response.statusCode == 200) {
      return Cliente.fromJson(json.decode(response.body));
    }
    return null;
  }
  
  Future<Cliente?> obtenerClientePorDocumento(String doc) async {
    final response = await http.get(Uri.parse('$baseUrl/documento/$doc'));
    if (response.statusCode == 200) {
      return Cliente.fromJson(json.decode(response.body));
    }
    return null;
  }
  
  Future<Cliente> crearCliente(Cliente cliente) async {
    final response = await http.post(
      Uri.parse(baseUrl),
      headers: {'Content-Type': 'application/json'},
      body: json.encode(cliente.toJson()),
    );
    if (response.statusCode == 201) {
      return Cliente.fromJson(json.decode(response.body));
    }
    throw Exception('Error al crear cliente');
  }
  
  Future<Cliente> actualizarCliente(String id, Cliente cliente) async {
    final response = await http.put(
      Uri.parse('$baseUrl/$id'),
      headers: {'Content-Type': 'application/json'},
      body: json.encode(cliente.toJson()),
    );
    if (response.statusCode == 200) {
      return Cliente.fromJson(json.decode(response.body));
    }
    throw Exception('Error al actualizar cliente');
  }
  
  Future<bool> eliminarCliente(String id) async {
    final response = await http.delete(Uri.parse('$baseUrl/$id'));
    return response.statusCode == 200;
  }
  
  // Búsquedas
  Future<List<Cliente>> buscarClientes(String q) async {
    final response = await http.get(Uri.parse('$baseUrl/buscar?q=$q'));
    if (response.statusCode == 200) {
      List<dynamic> data = json.decode(response.body);
      return data.map((json) => Cliente.fromJson(json)).toList();
    }
    return [];
  }
  
  Future<List<Cliente>> obtenerClientesActivos() async {
    final response = await http.get(Uri.parse('$baseUrl/estado/activos'));
    if (response.statusCode == 200) {
      List<dynamic> data = json.decode(response.body);
      return data.map((json) => Cliente.fromJson(json)).toList();
    }
    return [];
  }
  
  Future<List<Cliente>> obtenerClientesConSaldo() async {
    final response = await http.get(Uri.parse('$baseUrl/con-saldo'));
    if (response.statusCode == 200) {
      List<dynamic> data = json.decode(response.body);
      return data.map((json) => Cliente.fromJson(json)).toList();
    }
    return [];
  }
  
  // Acciones
  Future<Cliente> bloquearCliente(String id, String motivo) async {
    final response = await http.put(
      Uri.parse('$baseUrl/$id/bloquear'),
      headers: {'Content-Type': 'application/json'},
      body: json.encode({'motivo': motivo}),
    );
    if (response.statusCode == 200) {
      return Cliente.fromJson(json.decode(response.body));
    }
    throw Exception('Error al bloquear cliente');
  }
  
  Future<Cliente> activarCliente(String id) async {
    final response = await http.put(Uri.parse('$baseUrl/$id/activar'));
    if (response.statusCode == 200) {
      return Cliente.fromJson(json.decode(response.body));
    }
    throw Exception('Error al activar cliente');
  }
  
  Future<Map<String, dynamic>> verificarCupoCredito(String id, double monto) async {
    final response = await http.post(
      Uri.parse('$baseUrl/$id/verificar-cupo'),
      headers: {'Content-Type': 'application/json'},
      body: json.encode({'montoFactura': monto}),
    );
    if (response.statusCode == 200) {
      return json.decode(response.body);
    }
    throw Exception('Error al verificar cupo');
  }
  
  Future<Map<String, dynamic>> obtenerEstadisticas() async {
    final response = await http.get(Uri.parse('$baseUrl/estadisticas'));
    if (response.statusCode == 200) {
      return json.decode(response.body);
    }
    throw Exception('Error al obtener estadísticas');
  }
}
```

### **CotizacionService** (`lib/services/cotizacion_service.dart`)

```dart
import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/cotizacion.dart';

class CotizacionService {
  final String baseUrl = "https://tu-api.render.com/api/cotizaciones";
  
  // CRUD
  Future<List<Cotizacion>> obtenerCotizaciones() async {
    final response = await http.get(Uri.parse(baseUrl));
    if (response.statusCode == 200) {
      List<dynamic> data = json.decode(response.body);
      return data.map((json) => Cotizacion.fromJson(json)).toList();
    }
    throw Exception('Error al obtener cotizaciones');
  }
  
  Future<Cotizacion?> obtenerCotizacionPorId(String id) async {
    final response = await http.get(Uri.parse('$baseUrl/$id'));
    if (response.statusCode == 200) {
      return Cotizacion.fromJson(json.decode(response.body));
    }
    return null;
  }
  
  Future<Cotizacion> crearCotizacion(Cotizacion cotizacion) async {
    final response = await http.post(
      Uri.parse(baseUrl),
      headers: {'Content-Type': 'application/json'},
      body: json.encode(cotizacion.toJson()),
    );
    if (response.statusCode == 201) {
      return Cotizacion.fromJson(json.decode(response.body));
    }
    throw Exception('Error al crear cotización');
  }
  
  Future<Cotizacion> actualizarCotizacion(String id, Cotizacion cotizacion) async {
    final response = await http.put(
      Uri.parse('$baseUrl/$id'),
      headers: {'Content-Type': 'application/json'},
      body: json.encode(cotizacion.toJson()),
    );
    if (response.statusCode == 200) {
      return Cotizacion.fromJson(json.decode(response.body));
    }
    throw Exception('Error al actualizar cotización');
  }
  
  Future<bool> eliminarCotizacion(String id) async {
    final response = await http.delete(Uri.parse('$baseUrl/$id'));
    return response.statusCode == 200;
  }
  
  // Cálculos
  Future<Map<String, dynamic>> calcularTotales(Cotizacion cotizacion) async {
    final response = await http.post(
      Uri.parse('$baseUrl/calcular-totales'),
      headers: {'Content-Type': 'application/json'},
      body: json.encode(cotizacion.toJson()),
    );
    if (response.statusCode == 200) {
      return json.decode(response.body);
    }
    throw Exception('Error al calcular totales');
  }
  
  // Estados
  Future<Cotizacion> aceptarCotizacion(String id) async {
    final response = await http.put(Uri.parse('$baseUrl/$id/aceptar'));
    if (response.statusCode == 200) {
      return Cotizacion.fromJson(json.decode(response.body));
    }
    throw Exception('Error al aceptar cotización');
  }
  
  Future<Cotizacion> rechazarCotizacion(String id) async {
    final response = await http.put(Uri.parse('$baseUrl/$id/rechazar'));
    if (response.statusCode == 200) {
      return Cotizacion.fromJson(json.decode(response.body));
    }
    throw Exception('Error al rechazar cotización');
  }
  
  Future<Cotizacion> convertirAFactura(String id, String facturaId) async {
    final response = await http.post(
      Uri.parse('$baseUrl/$id/convertir-factura'),
      headers: {'Content-Type': 'application/json'},
      body: json.encode({'facturaId': facturaId}),
    );
    if (response.statusCode == 200) {
      return Cotizacion.fromJson(json.decode(response.body));
    }
    throw Exception('Error al convertir cotización');
  }
  
  // Filtros
  Future<List<Cotizacion>> obtenerPorCliente(String clienteId) async {
    final response = await http.get(Uri.parse('$baseUrl/cliente/$clienteId'));
    if (response.statusCode == 200) {
      List<dynamic> data = json.decode(response.body);
      return data.map((json) => Cotizacion.fromJson(json)).toList();
    }
    return [];
  }
  
  Future<List<Cotizacion>> obtenerPorEstado(String estado) async {
    final response = await http.get(Uri.parse('$baseUrl/estado/$estado'));
    if (response.statusCode == 200) {
      List<dynamic> data = json.decode(response.body);
      return data.map((json) => Cotizacion.fromJson(json)).toList();
    }
    return [];
  }
  
  Future<Map<String, dynamic>> obtenerEstadisticas() async {
    final response = await http.get(Uri.parse('$baseUrl/estadisticas'));
    if (response.statusCode == 200) {
      return json.decode(response.body);
    }
    throw Exception('Error al obtener estadísticas');
  }
}
```

---

## 🎨 Pantallas Sugeridas

### 📋 **Módulo Clientes**

#### 1. **ClientesListScreen** - Lista de clientes
```dart
// Características:
- Búsqueda global (TextField con debounce)
- Filtros: estado, tipo persona, con saldo
- Card con: nombre, documento, teléfono, saldo
- Pull to refresh
- Floating Action Button para crear nuevo
```

#### 2. **ClienteFormScreen** - Crear/Editar cliente
```dart
// Tabs sugeridos:
TabBar(
  tabs: [
    Tab(text: 'Identificación'),
    Tab(text: 'Datos Personales'),
    Tab(text: 'Datos Contribuyente'),
    Tab(text: 'Cuentas Contables'),
    Tab(text: 'Info Comercial'),
  ],
)

// Tab 1: Identificación
- DropdownButton: Tipo persona
- DropdownButton: Tipo identificación
- TextField: Número + DV (autocompletar)
- TextField: Nombres, apellidos
- TextField: Razón social (auto-generado si es persona natural)

// Tab 2: Datos Personales
- TextField: Correo (validación)
- TextField: Teléfono, teléfono secundario
- TextField: Dirección
- DropdownButton: Departamento (API Colombia)
- DropdownButton: Ciudad (filtrado por departamento)
- TextField: Código postal

// Tab 3: Datos Contribuyente
- DropdownButton: Responsable IVA (Sí/No/No Aplica)
- DropdownButton: Calidad agente retención
- DropdownButton: Régimen tributario
- TextField: Responsabilidades fiscales

// Tab 4: Cuentas Contables
- TextField con autocompletado: Cuentas por cobrar
- TextField con autocompletado: Cuentas para devoluciones
- TextField con autocompletado: Sobreabonos
- TextField con autocompletado: Deterioro cartera

// Tab 5: Información Comercial
- TextField: Condición pago
- TextField: Días crédito (numeric)
- TextField: Cupo crédito (currency)
- TextField: Categoría cliente
- DropdownButton: Vendedor asignado
- TextField: Zona de ventas
```

#### 3. **ClienteDetalleScreen** - Ver cliente
```dart
// Secciones:
- AppBar con acciones (Editar, Bloquear/Activar, Eliminar)
- Card: Información personal
- Card: Información tributaria
- Card: Información comercial
- Card: Estado de cuenta (saldo, cupo disponible)
- List: Historial de facturas/cotizaciones
- Botón: "Ver Movimientos"
```

---

### 💰 **Módulo Cotizaciones**

#### 1. **CotizacionesListScreen** - Lista de cotizaciones
```dart
// Características:
- Filtros: estado, cliente, rango de fechas
- Card con:
  * Número cotización
  * Cliente
  * Fecha + fecha vencimiento
  * Total (formato moneda)
  * Estado (Chip con color)
    - Activa: azul
    - Aceptada: verde
    - Rechazada: rojo
    - Vencida: naranja
    - Convertida: morado
- Pull to refresh
- FAB para crear nueva
```

#### 2. **CotizacionFormScreen** - Crear/Editar cotización
```dart
// Layout sugerido:
SingleChildScrollView(
  child: Column(
    children: [
      // Sección 1: Cliente
      Card(
        child: Column([
          // Autocompletar cliente
          TypeAheadField<Cliente>(
            suggestionsCallback: (pattern) async {
              return await clienteService.buscarClientes(pattern);
            },
          ),
          // Botón nuevo cliente (abre dialog)
          ElevatedButton.icon(
            icon: Icon(Icons.person_add),
            label: Text('Nuevo Cliente'),
          ),
          // Fecha vencimiento
          DatePickerField(),
        ]),
      ),
      
      // Sección 2: Productos
      Card(
        child: Column([
          // Buscador/Escáner
          Row([
            Expanded(
              child: TextField(
                decoration: InputDecoration(
                  labelText: 'Buscar producto',
                  suffixIcon: Icon(Icons.search),
                ),
              ),
            ),
            IconButton(
              icon: Icon(Icons.qr_code_scanner),
              onPressed: () => escanearEAN(),
            ),
          ]),
          
          // Lista de items
          ListView.builder(
            shrinkWrap: true,
            physics: NeverScrollableScrollPhysics(),
            itemCount: items.length,
            itemBuilder: (context, index) {
              return ItemCotizacionCard(
                item: items[index],
                onEdit: () => editarItem(index),
                onDelete: () => eliminarItem(index),
              );
            },
          ),
          
          // Botón agregar
          ElevatedButton.icon(
            icon: Icon(Icons.add),
            label: Text('Agregar Producto'),
          ),
        ]),
      ),
      
      // Sección 3: Descuentos y Retenciones
      ExpansionTile(
        title: Text('Descuentos y Retenciones'),
        children: [
          // Toggle Valor/Porcentaje
          SegmentedButton(
            segments: [
              ButtonSegment(value: 'Valor', label: Text('Valor')),
              ButtonSegment(value: 'Porcentaje', label: Text('%')),
            ],
          ),
          TextField(
            decoration: InputDecoration(labelText: 'Descuento General'),
          ),
          TextField(
            decoration: InputDecoration(labelText: 'Retención %'),
            keyboardType: TextInputType.number,
          ),
          TextField(
            decoration: InputDecoration(labelText: 'ReteIVA %'),
          ),
          TextField(
            decoration: InputDecoration(labelText: 'ReteICA %'),
          ),
        ],
      ),
      
      // Sección 4: Observaciones
      TextField(
        maxLines: 3,
        decoration: InputDecoration(labelText: 'Descripción'),
      ),
      
      SizedBox(height: 100), // Espacio para el resumen fixed
    ],
  ),
)

// Bottom Sheet Fixed - Resumen
Positioned(
  bottom: 0,
  left: 0,
  right: 0,
  child: Container(
    color: Colors.white,
    padding: EdgeInsets.all(16),
    child: Column([
      Row([Text('Subtotal:'), Text('\$${subtotal}')]),
      Row([Text('Descuentos:'), Text('-\$${totalDescuentos}')]),
      Row([Text('Impuestos:'), Text('+\$${totalImpuestos}')]),
      Row([Text('Retenciones:'), Text('-\$${totalRetenciones}')]),
      Divider(),
      Row([
        Text('TOTAL:', style: TextStyle(fontSize: 20, fontWeight: bold)),
        Text('\$${totalFinal}', style: TextStyle(fontSize: 24, fontWeight: bold)),
      ]),
      Row([
        ElevatedButton(
          child: Text('Calcular'),
          onPressed: () => calcularPreview(),
        ),
        ElevatedButton(
          child: Text('Guardar'),
          onPressed: () => guardarCotizacion(),
        ),
      ]),
    ]),
  ),
)
```

#### 3. **CotizacionDetalleScreen** - Ver cotización
```dart
// Layout:
AppBar(
  actions: [
    IconButton(icon: Icon(Icons.share), onPressed: compartir),
    IconButton(icon: Icon(Icons.picture_as_pdf), onPressed: generarPDF),
    PopupMenuButton(
      itemBuilder: (context) => [
        if (estado == 'activa') ...[
          PopupMenuItem(child: Text('Editar')),
          PopupMenuItem(child: Text('Aceptar')),
          PopupMenuItem(child: Text('Rechazar')),
          PopupMenuItem(child: Text('Convertir a Factura')),
        ],
        if (estado == 'aceptada') ...[
          PopupMenuItem(child: Text('Convertir a Factura')),
        ],
        PopupMenuItem(child: Text('Eliminar')),
      ],
    ),
  ],
)

// Body:
- Chip: Estado con color
- Card: Info cliente
- Card: Lista de items (no editable)
- Card: Resumen de totales
- Si está convertida: Botón "Ver Factura"
```

---

## ✅ Validaciones Importantes

### Cliente:
```dart
// Formulario
final _formKey = GlobalKey<FormState>();

// Validaciones
String? validarDocumento(String? value) {
  if (value == null || value.isEmpty) {
    return 'Documento requerido';
  }
  // Verificar si ya existe
  if (await clienteService.obtenerClientePorDocumento(value) != null) {
    return 'Ya existe un cliente con este documento';
  }
  return null;
}

String? validarEmail(String? value) {
  if (value != null && value.isNotEmpty) {
    if (!EmailValidator.validate(value)) {
      return 'Email inválido';
    }
  }
  return null;
}

String? validarNombres(String? value) {
  if (tipoPersona == 'Persona Natural' && (value == null || value.isEmpty)) {
    return 'Nombres requeridos para Persona Natural';
  }
  return null;
}

String? validarRazonSocial(String? value) {
  if (tipoPersona == 'Persona Jurídica' && (value == null || value.isEmpty)) {
    return 'Razón social requerida para Persona Jurídica';
  }
  return null;
}
```

### Cotización:
```dart
String? validarItems(List<ItemCotizacion> items) {
  if (items.isEmpty) {
    return 'Debe agregar al menos un producto';
  }
  for (var item in items) {
    if (item.cantidad <= 0) {
      return 'La cantidad debe ser mayor a 0';
    }
    if (item.precioUnitario < 0) {
      return 'El precio no puede ser negativo';
    }
  }
  return null;
}

String? validarFechaVencimiento(DateTime? fecha) {
  if (fecha != null && fecha.isBefore(DateTime.now())) {
    return 'La fecha de vencimiento debe ser futura';
  }
  return null;
}

String? validarPorcentaje(double? value) {
  if (value != null && (value < 0 || value > 100)) {
    return 'El porcentaje debe estar entre 0 y 100';
  }
  return null;
}
```

---

## 🎨 Widgets Reutilizables

### **ClienteAutocompleteField**
```dart
class ClienteAutocompleteField extends StatelessWidget {
  final Function(Cliente) onClienteSelected;
  
  @override
  Widget build(BuildContext context) {
    return TypeAheadField<Cliente>(
      textFieldConfiguration: TextFieldConfiguration(
        decoration: InputDecoration(
          labelText: 'Buscar Cliente',
          suffixIcon: Icon(Icons.search),
        ),
      ),
      suggestionsCallback: (pattern) async {
        if (pattern.isEmpty) return [];
        return await ClienteService().buscarClientes(pattern);
      },
      itemBuilder: (context, Cliente cliente) {
        return ListTile(
          title: Text(cliente.razonSocial ?? ''),
          subtitle: Text(cliente.numeroIdentificacion),
        );
      },
      onSuggestionSelected: onClienteSelected,
    );
  }
}
```

### **ItemCotizacionCard**
```dart
class ItemCotizacionCard extends StatelessWidget {
  final ItemCotizacion item;
  final VoidCallback onEdit;
  final VoidCallback onDelete;
  
  @override
  Widget build(BuildContext context) {
    return Card(
      child: ListTile(
        title: Text(item.productoNombre ?? ''),
        subtitle: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Cantidad: ${item.cantidad} x \$${item.precioUnitario}'),
            if (item.porcentajeImpuesto > 0)
              Text('IVA ${item.porcentajeImpuesto}%: +\$${item.valorImpuesto}'),
            if (item.porcentajeDescuento > 0)
              Text('Descuento ${item.porcentajeDescuento}%: -\$${item.valorDescuento}'),
          ],
        ),
        trailing: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text('\$${item.valorTotal}', style: TextStyle(fontWeight: FontWeight.bold)),
            IconButton(icon: Icon(Icons.edit), onPressed: onEdit),
            IconButton(icon: Icon(Icons.delete), onPressed: onDelete),
          ],
        ),
      ),
    );
  }
}
```

### **ResumenTotalesWidget**
```dart
class ResumenTotalesWidget extends StatelessWidget {
  final double subtotal;
  final double totalDescuentos;
  final double totalImpuestos;
  final double totalRetenciones;
  final double totalFinal;
  
  @override
  Widget build(BuildContext context) {
    return Container(
      padding: EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        boxShadow: [
          BoxShadow(color: Colors.black26, blurRadius: 4, offset: Offset(0, -2)),
        ],
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          _buildRow('Subtotal:', '\$${subtotal.toStringAsFixed(2)}'),
          _buildRow('Descuentos:', '-\$${totalDescuentos.toStringAsFixed(2)}', color: Colors.red),
          _buildRow('Impuestos:', '+\$${totalImpuestos.toStringAsFixed(2)}', color: Colors.green),
          _buildRow('Retenciones:', '-\$${totalRetenciones.toStringAsFixed(2)}', color: Colors.orange),
          Divider(thickness: 2),
          _buildRow(
            'TOTAL:',
            '\$${totalFinal.toStringAsFixed(2)}',
            fontSize: 24,
            fontWeight: FontWeight.bold,
          ),
        ],
      ),
    );
  }
  
  Widget _buildRow(String label, String value, {Color? color, double? fontSize, FontWeight? fontWeight}) {
    return Padding(
      padding: EdgeInsets.symmetric(vertical: 4),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label, style: TextStyle(fontSize: fontSize ?? 16, fontWeight: fontWeight)),
          Text(value, style: TextStyle(fontSize: fontSize ?? 16, fontWeight: fontWeight, color: color)),
        ],
      ),
    );
  }
}
```

### **TipoImpuestoDropdown**
```dart
class TipoImpuestoDropdown extends StatelessWidget {
  final String? value;
  final Function(String?) onChanged;
  
  @override
  Widget build(BuildContext context) {
    return DropdownButtonFormField<String>(
      value: value,
      decoration: InputDecoration(labelText: 'Tipo Impuesto'),
      items: [
        DropdownMenuItem(value: null, child: Text('--')),
        DropdownMenuItem(value: 'IVA', child: Text('IVA')),
        DropdownMenuItem(value: 'INC', child: Text('INC')),
        DropdownMenuItem(value: 'Exento', child: Text('Exento')),
        DropdownMenuItem(value: 'IVA+INC', child: Text('IVA + INC')),
      ],
      onChanged: onChanged,
    );
  }
}
```

---

## 📊 Gestión de Estado (Provider)

```dart
// cliente_provider.dart
class ClienteProvider extends ChangeNotifier {
  final ClienteService _service = ClienteService();
  
  List<Cliente> _clientes = [];
  Cliente? _clienteSeleccionado;
  bool _isLoading = false;
  String? _error;
  
  List<Cliente> get clientes => _clientes;
  Cliente? get clienteSeleccionado => _clienteSeleccionado;
  bool get isLoading => _isLoading;
  String? get error => _error;
  
  Future<void> cargarClientes() async {
    _isLoading = true;
    _error = null;
    notifyListeners();
    
    try {
      _clientes = await _service.obtenerClientes();
    } catch (e) {
      _error = e.toString();
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }
  
  Future<void> crearCliente(Cliente cliente) async {
    try {
      final nuevoCliente = await _service.crearCliente(cliente);
      _clientes.insert(0, nuevoCliente);
      notifyListeners();
    } catch (e) {
      throw e;
    }
  }
  
  Future<void> buscarClientes(String query) async {
    _isLoading = true;
    notifyListeners();
    
    try {
      _clientes = await _service.buscarClientes(query);
    } catch (e) {
      _error = e.toString();
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }
  
  void seleccionarCliente(Cliente cliente) {
    _clienteSeleccionado = cliente;
    notifyListeners();
  }
}

// cotizacion_provider.dart
class CotizacionProvider extends ChangeNotifier {
  final CotizacionService _service = CotizacionService();
  
  List<Cotizacion> _cotizaciones = [];
  Cotizacion? _cotizacionActual;
  List<ItemCotizacion> _items = [];
  bool _isLoading = false;
  
  List<Cotizacion> get cotizaciones => _cotizaciones;
  Cotizacion? get cotizacionActual => _cotizacionActual;
  List<ItemCotizacion> get items => _items;
  bool get isLoading => _isLoading;
  
  // Totales calculados en tiempo real
  double get subtotal => _items.fold(0.0, (sum, item) => sum + item.subtotal);
  double get totalImpuestos => _items.fold(0.0, (sum, item) => sum + item.valorImpuesto);
  double get descuentoProductos => _items.fold(0.0, (sum, item) => sum + item.valorDescuento);
  
  void agregarItem(ItemCotizacion item) {
    _items.add(item);
    calcularTotales();
    notifyListeners();
  }
  
  void eliminarItem(int index) {
    _items.removeAt(index);
    calcularTotales();
    notifyListeners();
  }
  
  void calcularTotales() {
    for (var item in _items) {
      item.calcularValorImpuesto();
      item.calcularValorDescuento();
    }
    notifyListeners();
  }
  
  Future<void> guardarCotizacion(Cotizacion cotizacion) async {
    try {
      final nueva = await _service.crearCotizacion(cotizacion);
      _cotizaciones.insert(0, nueva);
      limpiarFormulario();
      notifyListeners();
    } catch (e) {
      throw e;
    }
  }
  
  void limpiarFormulario() {
    _items.clear();
    _cotizacionActual = null;
    notifyListeners();
  }
}
```

---

## 🔗 Endpoints Backend

**Base URL:** `https://vercy-motos-app.onrender.com/api`

### Clientes:
```
GET    /clientes                       - Listar todos
GET    /clientes/{id}                  - Obtener por ID
GET    /clientes/documento/{doc}       - Obtener por documento
GET    /clientes/buscar?q=termino      - Búsqueda global
GET    /clientes/estado/activos        - Clientes activos
GET    /clientes/con-saldo             - Con saldo pendiente
POST   /clientes                       - Crear
PUT    /clientes/{id}                  - Actualizar
DELETE /clientes/{id}                  - Eliminar (soft)
PUT    /clientes/{id}/bloquear         - Bloquear
PUT    /clientes/{id}/activar          - Activar
POST   /clientes/{id}/verificar-cupo   - Verificar cupo
GET    /clientes/estadisticas          - Estadísticas
```

### Cotizaciones:
```
GET    /cotizaciones                      - Listar todas
GET    /cotizaciones/{id}                 - Obtener por ID
GET    /cotizaciones/cliente/{clienteId}  - Por cliente
GET    /cotizaciones/estado/{estado}      - Por estado
POST   /cotizaciones                      - Crear
PUT    /cotizaciones/{id}                 - Actualizar
DELETE /cotizaciones/{id}                 - Eliminar
POST   /cotizaciones/calcular-totales     - Preview cálculo
PUT    /cotizaciones/{id}/aceptar         - Aceptar
PUT    /cotizaciones/{id}/rechazar        - Rechazar
POST   /cotizaciones/{id}/convertir-factura  - Convertir
GET    /cotizaciones/estadisticas         - Estadísticas
```

---

## 📦 Paquetes Flutter Recomendados

```yaml
dependencies:
  flutter:
    sdk: flutter
  
  # HTTP Clients
  http: ^1.1.0
  dio: ^5.4.0
  
  # Estado
  provider: ^6.1.1
  # O usar: riverpod: ^2.4.9
  
  # Formularios y Validación
  flutter_form_builder: ^9.1.1
  form_builder_validators: ^9.1.0
  email_validator: ^2.1.17
  
  # Autocompletar
  flutter_typeahead: ^4.8.0
  
  # Fechas y Formato
  intl: ^0.18.1
  
  # PDF
  pdf: ^3.10.7
  printing: ^5.11.1
  
  # Escáner de códigos
  mobile_scanner: ^3.5.5
  qr_flutter: ^4.1.0
  
  # UI Components
  flutter_slidable: ^3.0.1
  shimmer: ^3.0.0
  
  # Almacenamiento local
  shared_preferences: ^2.2.2
  
  # Loading indicators
  flutter_spinkit: ^5.2.0
```

---

## 🚀 Flujo de Trabajo Completo

### 1. **Crear Cliente**
```
Usuario → ClienteFormScreen
  ↓
Llenar datos (5 tabs)
  ↓
Validar formulario
  ↓
ClienteProvider.crearCliente()
  ↓
ClienteService.POST /api/clientes
  ↓
Backend guarda y retorna cliente
  ↓
Actualizar lista local
  ↓
Navegar a ClienteDetalleScreen
```

### 2. **Crear Cotización**
```
Usuario → CotizacionFormScreen
  ↓
Seleccionar/crear cliente
  ↓
Agregar productos (con escaneo EAN o búsqueda)
  ↓
Para cada producto:
  - Configurar cantidad
  - Configurar impuesto (IVA 19%, INC 8%, Exento)
  - Configurar descuento
  ↓
Agregar retenciones si aplica
  ↓
Agregar descuento general
  ↓
Calcular totales en tiempo real (Provider)
  ↓
"Calcular" → Preview (POST /calcular-totales)
  ↓
"Guardar" → POST /api/cotizaciones
  ↓
Backend:
  - Genera número automático
  - Calcula totales
  - Guarda
  ↓
Navegar a CotizacionDetalleScreen
```

### 3. **Flujo de Estados de Cotización**
```
ACTIVA
  ↓ [Cliente aprueba]
ACEPTADA
  ↓ [Convertir a factura]
CONVERTIDA → Genera Factura definitiva
  ↓
Actualizar saldo del cliente
Enviar factura electrónica (si aplica)
```

---

## 🎯 Prioridades de Implementación

### Fase 1 (MVP):
1. ✅ Modelos: Cliente, ItemCotizacion, Cotizacion
2. ✅ Services: ClienteService, CotizacionService
3. ✅ Pantalla: ClientesListScreen (básica)
4. ✅ Pantalla: ClienteFormScreen (tabs principales)
5. ✅ Pantalla: CotizacionFormScreen (básica)

### Fase 2 (Mejoras):
6. ✅ Provider: ClienteProvider, CotizacionProvider
7. ✅ Validaciones completas
8. ✅ Búsqueda con TypeAhead
9. ✅ Cálculo de totales en tiempo real
10. ✅ PDF generation

### Fase 3 (Avanzado):
11. ✅ Escaneo de código de barras
12. ✅ Compartir cotizaciones (WhatsApp, Email)
13. ✅ Sincronización offline
14. ✅ Notificaciones push
15. ✅ Gráficas y estadísticas

---

**Fecha de creación:** 17 de enero de 2026  
**Autor:** Backend Team  
**Para:** Frontend Flutter Team  
**Versión:** 1.0

