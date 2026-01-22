# Guía de Integración Flutter - Gastos y Compras

## 📋 Resumen de Cambios

Este documento describe los cambios realizados en los endpoints de **Gastos** y **Compras (Facturas de Compra)** para su integración en Flutter.

---

## 🧾 MÓDULO DE COMPRAS (Facturas de Compra)

### Endpoint Base
```
POST /api/facturas-compras/crear
GET /api/facturas-compras
GET /api/facturas-compras/{id}
DELETE /api/facturas-compras/{id}
```

### Modelo de Datos: `ItemFacturaIngrediente`

```dart
class ItemFacturaIngrediente {
  String? ingredienteId;
  String? codigo;              // Código interno del producto
  String? codigoBarras;        // Código de barras
  String? ingredienteNombre;   // Nombre del producto
  double cantidad;
  String? unidad;
  double precioUnitario;       // Valor unitario
  double valorTotal;           // cantidad * precioUnitario
  
  // Impuestos
  double porcentajeImpuesto;   // % Imp (ej: 19 para IVA)
  String tipoImpuesto;         // Tipo (default: "%")
  double valorImpuesto;        // Calculado automáticamente
  
  // Descuentos
  double porcentajeDescuento;  // % Descuento por producto
  double valorDescuento;       // Calculado automáticamente
  
  double precioTotal;          // Total final del item
  bool descontable;            // Si afecta inventario
  String? observaciones;

  ItemFacturaIngrediente({
    this.ingredienteId,
    this.codigo,
    this.codigoBarras,
    this.ingredienteNombre,
    this.cantidad = 0,
    this.unidad,
    this.precioUnitario = 0,
    this.valorTotal = 0,
    this.porcentajeImpuesto = 0,
    this.tipoImpuesto = "%",
    this.valorImpuesto = 0,
    this.porcentajeDescuento = 0,
    this.valorDescuento = 0,
    this.precioTotal = 0,
    this.descontable = true,
    this.observaciones,
  });

  Map<String, dynamic> toJson() => {
    'ingredienteId': ingredienteId,
    'codigo': codigo,
    'codigoBarras': codigoBarras,
    'ingredienteNombre': ingredienteNombre,
    'cantidad': cantidad,
    'unidad': unidad,
    'precioUnitario': precioUnitario,
    'valorTotal': valorTotal,
    'porcentajeImpuesto': porcentajeImpuesto,
    'tipoImpuesto': tipoImpuesto,
    'valorImpuesto': valorImpuesto,
    'porcentajeDescuento': porcentajeDescuento,
    'valorDescuento': valorDescuento,
    'precioTotal': precioTotal,
    'descontable': descontable,
    'observaciones': observaciones,
  };
}
```

### Request para Crear Factura de Compra

```dart
class FacturaCompraRequest {
  // Datos básicos
  String? numeroFacturaProveedor;  // Número de factura del proveedor
  String? fecha;                   // Formato: "2026-01-21"
  String? fechaVencimiento;        // Formato: "2026-02-21"
  
  // Proveedor
  String? proveedorNit;
  String? proveedorNombre;
  String? proveedorTelefono;
  String? proveedorDireccion;
  
  // Items (productos)
  List<ItemFacturaIngrediente> items;
  
  // Descripción/Observaciones
  String? descripcion;
  
  // Retenciones (porcentajes)
  double porcentajeRetencion;      // Retención en la fuente %
  double porcentajeReteIva;        // ReteIVA %
  double porcentajeReteIca;        // ReteICA %
  
  // Descuento general
  String tipoDescuento;            // "Valor" o "Porcentaje"
  double descuentoGeneral;         // Valor o % según tipoDescuento
  
  // Pago
  String? medioPago;               // "Efectivo", "Transferencia"
  String? formaPago;               // "Contado", "Crédito"
  bool pagadoDesdeCaja;
  
  // Usuario
  String? registradoPor;

  Map<String, dynamic> toJson() => {
    'numeroFacturaProveedor': numeroFacturaProveedor,
    'fecha': fecha,
    'fechaVencimiento': fechaVencimiento,
    'proveedorNit': proveedorNit,
    'proveedorNombre': proveedorNombre,
    'proveedorTelefono': proveedorTelefono,
    'proveedorDireccion': proveedorDireccion,
    'items': items.map((e) => e.toJson()).toList(),
    'descripcion': descripcion,
    'porcentajeRetencion': porcentajeRetencion,
    'porcentajeReteIva': porcentajeReteIva,
    'porcentajeReteIca': porcentajeReteIca,
    'tipoDescuento': tipoDescuento,
    'descuentoGeneral': descuentoGeneral,
    'medioPago': medioPago,
    'formaPago': formaPago,
    'pagadoDesdeCaja': pagadoDesdeCaja,
    'registradoPor': registradoPor,
  };
}
```

### Cálculos de Factura de Compra

```dart
/// Fórmulas de cálculo (el backend las aplica automáticamente):
/// 
/// Por cada item:
/// - valorTotal = cantidad * precioUnitario
/// - valorDescuento = valorTotal * (porcentajeDescuento / 100)
/// - baseParaImpuesto = valorTotal - valorDescuento
/// - valorImpuesto = baseParaImpuesto * (porcentajeImpuesto / 100)
/// - precioTotal = baseParaImpuesto + valorImpuesto
/// 
/// Totales de factura:
/// - subtotal = suma de valorTotal de todos los items
/// - totalDescuentosProductos = suma de valorDescuento de todos los items
/// - totalImpuestos = suma de valorImpuesto de todos los items
/// - descuentoGeneral = (si es %) subtotal * porcentaje / 100, o valor fijo
/// - totalDescuentos = totalDescuentosProductos + descuentoGeneral
/// - baseGravable = subtotal - totalDescuentos + totalImpuestos
/// 
/// Retenciones:
/// - valorRetencion = baseGravable * (porcentajeRetencion / 100)
/// - valorReteIva = totalImpuestos * (porcentajeReteIva / 100)
/// - valorReteIca = baseGravable * (porcentajeReteIca / 100)
/// - totalRetenciones = valorRetencion + valorReteIva + valorReteIca
/// 
/// TOTAL = baseGravable - totalRetenciones
```

### Ejemplo de Uso en Flutter

```dart
Future<void> crearFacturaCompra() async {
  final request = FacturaCompraRequest(
    numeroFacturaProveedor: "FAC-001",
    fecha: "2026-01-21",
    fechaVencimiento: "2026-02-21",
    proveedorNombre: "Proveedor ABC",
    proveedorNit: "900123456-1",
    items: [
      ItemFacturaIngrediente(
        ingredienteId: "abc123",
        codigo: "PROD-001",
        codigoBarras: "7701234567890",
        ingredienteNombre: "Producto X",
        cantidad: 10,
        unidad: "und",
        precioUnitario: 50000,
        porcentajeImpuesto: 19,  // IVA 19%
        tipoImpuesto: "%",
        porcentajeDescuento: 5,  // 5% descuento
        descontable: true,
      ),
    ],
    descripcion: "Compra de insumos",
    porcentajeRetencion: 2.5,    // Retefuente 2.5%
    porcentajeReteIva: 15,       // ReteIVA 15%
    porcentajeReteIca: 0,
    tipoDescuento: "Valor",
    descuentoGeneral: 0,
    medioPago: "Transferencia",
    formaPago: "Crédito",
    pagadoDesdeCaja: false,
    registradoPor: "admin",
  );

  final response = await http.post(
    Uri.parse('$baseUrl/api/facturas-compras/crear'),
    headers: {'Content-Type': 'application/json'},
    body: jsonEncode(request.toJson()),
  );
}
```

---

## 💸 MÓDULO DE GASTOS

### Endpoint Base
```
POST /api/gastos
GET /api/gastos
GET /api/gastos/{id}
PUT /api/gastos/{id}
DELETE /api/gastos/{id}
GET /api/gastos/cuadre/{cuadreId}
```

### Modelo de Datos: `ItemGasto`

```dart
class ItemGasto {
  String? concepto;            // Descripción del concepto
  double valor;                // Valor base
  double porcentajeDescuento;  // % descuento
  double valorDescuento;       // Calculado
  String tipoImpuesto;         // "IVA", etc.
  double porcentajeImpuesto;   // Tasa %
  double valorImpuesto;        // Calculado
  double total;                // Total del item

  ItemGasto({
    this.concepto,
    this.valor = 0,
    this.porcentajeDescuento = 0,
    this.valorDescuento = 0,
    this.tipoImpuesto = "IVA",
    this.porcentajeImpuesto = 0,
    this.valorImpuesto = 0,
    this.total = 0,
  });

  Map<String, dynamic> toJson() => {
    'concepto': concepto,
    'valor': valor,
    'porcentajeDescuento': porcentajeDescuento,
    'valorDescuento': valorDescuento,
    'tipoImpuesto': tipoImpuesto,
    'porcentajeImpuesto': porcentajeImpuesto,
    'valorImpuesto': valorImpuesto,
    'total': total,
  };
}
```

### Request para Crear/Actualizar Gasto

```dart
class GastoRequest {
  // Campos requeridos
  String cuadreCajaId;
  String tipoGastoId;          // ID de la categoría
  
  // Proveedor
  String? proveedor;
  String? proveedorId;
  
  // Fechas
  DateTime? fechaGasto;
  DateTime? fechaVencimiento;
  
  // Categoría
  String? tipoGastoNombre;
  
  // Documento soporte
  bool documentoSoporte;
  
  // Items del gasto (múltiples líneas de concepto)
  List<ItemGasto>? items;
  
  // Campos de cálculo (se pueden enviar vacíos, el backend calcula)
  double subtotal;
  double totalDescuentos;
  double impuestos;            // Legacy
  double totalImpuestos;
  double monto;                // Total final
  
  // Retenciones (porcentajes)
  double porcentajeRetencion;
  double porcentajeReteIva;
  double porcentajeReteIca;
  
  // Otros campos
  String? concepto;            // Descripción general
  String? responsable;
  String? numeroRecibo;
  String? numeroFactura;
  String? formaPago;           // "efectivo", "transferencia"
  bool pagadoDesdeCaja;

  Map<String, dynamic> toJson() => {
    'cuadreCajaId': cuadreCajaId,
    'tipoGastoId': tipoGastoId,
    'proveedor': proveedor,
    'proveedorId': proveedorId,
    'fechaGasto': fechaGasto?.toIso8601String(),
    'fechaVencimiento': fechaVencimiento?.toIso8601String(),
    'tipoGastoNombre': tipoGastoNombre,
    'documentoSoporte': documentoSoporte,
    'items': items?.map((e) => e.toJson()).toList(),
    'subtotal': subtotal,
    'totalDescuentos': totalDescuentos,
    'impuestos': impuestos,
    'totalImpuestos': totalImpuestos,
    'monto': monto,
    'porcentajeRetencion': porcentajeRetencion,
    'porcentajeReteIva': porcentajeReteIva,
    'porcentajeReteIca': porcentajeReteIca,
    'concepto': concepto,
    'responsable': responsable,
    'numeroRecibo': numeroRecibo,
    'numeroFactura': numeroFactura,
    'formaPago': formaPago,
    'pagadoDesdeCaja': pagadoDesdeCaja,
  };
}
```

### Cálculos de Gasto

```dart
/// Fórmulas de cálculo (el backend las aplica automáticamente):
/// 
/// Por cada item:
/// - valorDescuento = valor * (porcentajeDescuento / 100)
/// - baseImponible = valor - valorDescuento
/// - valorImpuesto = baseImponible * (porcentajeImpuesto / 100)
/// - total = baseImponible + valorImpuesto
/// 
/// Totales del gasto:
/// - subtotal = suma de valor de todos los items
/// - totalDescuentos = suma de valorDescuento de todos los items
/// - totalImpuestos = suma de valorImpuesto de todos los items
/// - baseGravable = subtotal - totalDescuentos + totalImpuestos
/// 
/// Retenciones:
/// - valorRetencion = baseGravable * (porcentajeRetencion / 100)
/// - valorReteIva = totalImpuestos * (porcentajeReteIva / 100)
/// - valorReteIca = baseGravable * (porcentajeReteIca / 100)
/// - totalRetenciones = valorRetencion + valorReteIva + valorReteIca
/// 
/// MONTO TOTAL = baseGravable - totalRetenciones
```

### Ejemplo de Uso en Flutter

```dart
Future<void> crearGasto() async {
  final request = GastoRequest(
    cuadreCajaId: "caja123",
    tipoGastoId: "cat456",      // Categoría del gasto
    proveedor: "Proveedor XYZ",
    fechaGasto: DateTime.now(),
    fechaVencimiento: DateTime.now().add(Duration(days: 30)),
    documentoSoporte: true,
    items: [
      ItemGasto(
        concepto: "Servicio de mantenimiento",
        valor: 100000,
        porcentajeDescuento: 0,
        tipoImpuesto: "IVA",
        porcentajeImpuesto: 19,
      ),
      ItemGasto(
        concepto: "Repuestos",
        valor: 50000,
        porcentajeDescuento: 10,
        tipoImpuesto: "IVA",
        porcentajeImpuesto: 19,
      ),
    ],
    porcentajeRetencion: 0,
    porcentajeReteIva: 0,
    porcentajeReteIca: 0,
    concepto: "Mantenimiento mensual",
    responsable: "Juan Pérez",
    formaPago: "efectivo",
    pagadoDesdeCaja: true,
  );

  final response = await http.post(
    Uri.parse('$baseUrl/api/gastos'),
    headers: {'Content-Type': 'application/json'},
    body: jsonEncode(request.toJson()),
  );
}
```

---

## 🔄 Endpoint para Agregar Retenciones (Toggle)

Para el botón "Agregar Retenciones" en la UI, simplemente envía los porcentajes:

```dart
// Sin retenciones
porcentajeRetencion: 0,
porcentajeReteIva: 0,
porcentajeReteIca: 0,

// Con retenciones (ejemplo)
porcentajeRetencion: 2.5,   // Retefuente 2.5%
porcentajeReteIva: 15,      // ReteIVA 15% del IVA
porcentajeReteIca: 0.5,     // ReteICA 0.5%
```

---

## 📊 Respuesta del Backend

### Factura de Compra Response
```json
{
  "success": true,
  "message": "Factura de compras creada exitosamente",
  "factura": {
    "_id": "abc123",
    "numero": "COMP-1234567",
    "numeroFacturaProveedor": "FAC-001",
    "fecha": "2026-01-21T00:00:00",
    "fechaVencimiento": "2026-02-21T00:00:00",
    "tipoFactura": "compra",
    "proveedorNombre": "Proveedor ABC",
    "itemsIngredientes": [...],
    "subtotal": 500000,
    "totalDescuentosProductos": 25000,
    "totalImpuestos": 90250,
    "descuentoGeneral": 0,
    "totalDescuentos": 25000,
    "baseGravable": 565250,
    "porcentajeRetencion": 2.5,
    "valorRetencion": 14131.25,
    "porcentajeReteIva": 15,
    "valorReteIva": 13537.50,
    "porcentajeReteIca": 0,
    "valorReteIca": 0,
    "totalRetenciones": 27668.75,
    "total": 537581.25
  },
  "numeroFactura": "COMP-1234567"
}
```

### Gasto Response
```json
{
  "data": {
    "_id": "gasto123",
    "cuadreCajaId": "caja123",
    "tipoGastoId": "cat456",
    "tipoGastoNombre": "Servicios",
    "proveedor": "Proveedor XYZ",
    "fechaGasto": "2026-01-21T10:30:00",
    "fechaVencimiento": "2026-02-21T00:00:00",
    "documentoSoporte": true,
    "items": [...],
    "subtotal": 150000,
    "totalDescuentos": 5000,
    "totalImpuestos": 27550,
    "baseGravable": 172550,
    "porcentajeRetencion": 0,
    "valorRetencion": 0,
    "porcentajeReteIva": 0,
    "valorReteIva": 0,
    "porcentajeReteIca": 0,
    "valorReteIca": 0,
    "totalRetenciones": 0,
    "monto": 172550,
    "pagadoDesdeCaja": true,
    "formaPago": "efectivo"
  },
  "message": "Gasto creado exitosamente y descontado del efectivo de caja",
  "success": true
}
```

---

## ⚠️ Notas Importantes

1. **Cálculos automáticos**: El backend calcula automáticamente todos los valores (descuentos, impuestos, retenciones, totales). Flutter puede enviar solo los valores base y porcentajes.

2. **Pagado desde caja**: Si `pagadoDesdeCaja = true`, el gasto/factura se descuenta del efectivo de la caja activa.

3. **Documento soporte**: El campo `documentoSoporte` es un toggle que indica si el gasto tiene documentación física/digital asociada.

4. **Fechas**: Enviar en formato ISO 8601 o "YYYY-MM-DD" para fechas simples.

5. **Items opcionales**: Si no se envían items, el backend usa los valores de `monto`/`subtotal` directamente.

6. **Retenciones**: Solo se aplican si los porcentajes son mayores a 0. El toggle "Agregar Retenciones" en la UI simplemente habilita/deshabilita los campos de porcentaje.
