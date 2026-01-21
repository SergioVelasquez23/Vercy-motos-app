# 💰 Sistema de Cálculos para Facturas con Impuestos y Retenciones

## 📋 Resumen General

Se ha implementado un sistema completo de cálculo de impuestos, retenciones y descuentos para facturas, similar al implementado en pedidos, siguiendo la normativa DIAN para facturación colombiana.

## 🆕 Componentes Creados

### 1. FacturaCalculosService
**Ubicación:** `Services/FacturaCalculosService.java`

Servicio centralizado para realizar todos los cálculos de facturas con impuestos y retenciones.

#### Métodos Principales:

- **`calcularItem(ItemFactura item)`**
  - Calcula los valores de impuesto y descuento para un item individual
  - Actualiza: `valorImpuesto`, `valorDescuento`, `subtotalItem`, `totalItem`

- **`calcularFactura(Factura factura)`**
  - Calcula todos los totales de la factura
  - Fórmula: `Total = (Subtotal - Descuentos) + Impuestos - Retenciones`
  - Actualiza todos los campos de totales

- **`obtenerDesglose(Factura factura)`**
  - Retorna un Map con el desglose completo de la factura
  - Útil para mostrar al usuario o generar reportes

- **`calcularTotalesLista(List<Factura> facturas)`**
  - Calcula totales agregados para una lista de facturas
  - Usado en reportes y cuadres de caja

- **`obtenerResumenImpuestos(Factura factura)`**
  - Agrupa impuestos por tipo (IVA 19%, IVA 5%, INC 8%, etc.)
  - Útil para declaraciones tributarias

## 📊 Campos Agregados

### ItemFactura
```java
// Campos detallados para cálculos DIAN
private double porcentajeImpuesto = 0.0;  // % de IVA, INC, etc.
private double valorImpuesto = 0.0;       // Valor calculado del impuesto
private double porcentajeDescuento = 0.0; // % de descuento sobre el item
private double valorDescuento = 0.0;      // Valor calculado del descuento
```

### Factura
```java
// Campos adicionales para retenciones DIAN
private double baseGravable = 0.0;        // Base sobre la que se calculan impuestos
private double totalRetenciones = 0.0;    // Total de retenciones aplicadas

// Retención en la fuente
private double porcentajeRetencion = 0.0; // % Retención en la fuente
private double valorRetencion = 0.0;      // Valor calculado

// Retención de IVA
private double porcentajeReteIva = 0.0;   // % Retención de IVA
private double valorReteIva = 0.0;        // Valor calculado

// Retención de ICA
private double porcentajeReteIca = 0.0;   // % Retención de ICA
private double valorReteIca = 0.0;        // Valor calculado
```

## 🔄 Integración en FacturaComprasController

El controlador ahora usa `FacturaCalculosService` para calcular totales:

```java
@Autowired
private FacturaCalculosService facturaCalculosService;

// En el endpoint de creación
facturaCalculosService.calcularFactura(factura);
```

## 📐 Fórmula de Cálculo

```
1. Subtotal = Σ(cantidad × precio unitario) de todos los items

2. Descuentos = Descuentos individuales + Descuento general

3. Base Gravable = Subtotal - Descuentos

4. Impuestos = Σ(Base gravable × % impuesto por cada item)
   - IVA 19%, 5%, 0%
   - INC 8%, 16%
   - Otros impuestos

5. Retenciones:
   - Retención Fuente = Base gravable × % retención
   - Rete IVA = Total impuestos × % reteIVA
   - Rete ICA = Base gravable × % reteICA
   - Total Retenciones = Σ todas las retenciones

6. Total Final = Base Gravable + Impuestos - Retenciones
```

## 💡 Uso del Sistema

### Crear una Factura con Impuestos

```java
// Crear factura
Factura factura = new Factura();
factura.setNumero("FC-001");

// Agregar items con impuestos
ItemFactura item = new ItemFactura();
item.setProductoNombre("Producto A");
item.setCantidad(10);
item.setPrecioUnitario(1000.0);
item.setPorcentajeImpuesto(19.0); // IVA 19%
item.setPorcentajeDescuento(5.0); // 5% descuento

factura.getItems().add(item);

// Configurar retenciones si aplica
factura.setPorcentajeRetencion(2.5);  // 2.5% retención fuente
factura.setPorcentajeReteIva(15.0);   // 15% rete IVA
factura.setPorcentajeReteIca(0.966);  // 0.966% rete ICA

// Calcular totales
facturaCalculosService.calcularFactura(factura);

// Resultado:
// - factura.getSubtotal() = 10,000
// - factura.getTotalDescuentos() = 500
// - factura.getBaseGravable() = 9,500
// - factura.getTotalImpuestos() = 1,805 (19% de 9,500)
// - factura.getValorRetencion() = 237.5 (2.5% de 9,500)
// - factura.getValorReteIva() = 271.75 (15% de 1,805)
// - factura.getValorReteIca() = 91.77 (0.966% de 9,500)
// - factura.getTotalRetenciones() = 601.02
// - factura.getTotal() = 10,703.98
```

### Obtener Desglose

```java
Map<String, Double> desglose = facturaCalculosService.obtenerDesglose(factura);

// desglose contiene:
// - subtotal: 10000.0
// - totalDescuentos: 500.0
// - baseGravable: 9500.0
// - totalImpuestos: 1805.0
// - valorRetencion: 237.5
// - valorReteIva: 271.75
// - valorReteIca: 91.77
// - totalRetenciones: 601.02
// - total: 10703.98
```

### Resumen de Impuestos

```java
Map<String, Double> resumen = facturaCalculosService.obtenerResumenImpuestos(factura);

// resumen contiene:
// - "IVA 19%": 1805.0
// - "IVA 5%": 0.0
// - "INC 8%": 0.0
// etc.
```

## 🎯 Características Importantes

1. **Compatibilidad**: El método `calcularTotal()` original se mantiene para compatibilidad con código existente

2. **Validación**: Todos los cálculos validan que los valores no sean negativos

3. **Logging**: Los cálculos generan logs en consola para debugging:
   ```
   💰 Cálculo de factura FC-001 completado:
      - Subtotal: $10000.0
      - Descuentos: $500.0
      - Base gravable: $9500.0
      - Impuestos: $1805.0
      - Retenciones: $601.02
      - Total final: $10703.98
   ```

4. **Agregación**: El servicio permite calcular totales para listas de facturas (útil para reportes)

5. **Normativa DIAN**: Todos los cálculos siguen las reglas de la DIAN para facturación electrónica en Colombia

## 🔗 Archivos Modificados

### Creados:
- ✅ `Services/FacturaCalculosService.java`

### Actualizados:
- ✅ `Models/Factura.java` - Agregados campos de retenciones
- ✅ `Models/ItemFactura.java` - Agregados campos de impuestos detallados
- ✅ `Controllers/FacturaComprasController.java` - Integración del servicio

## 📱 Próximos Pasos para Flutter

1. **Actualizar DTOs**: Agregar los nuevos campos a los modelos de Flutter
2. **UI de Impuestos**: Crear campos para ingresar porcentajes de impuestos y retenciones
3. **Visualización**: Mostrar el desglose detallado en la app
4. **Validaciones**: Validar porcentajes según normativa DIAN

## 🧪 Ejemplo de Uso en API

### Request POST `/api/facturas-compras`
```json
{
  "numero": "FC-001",
  "proveedorNit": "900123456-1",
  "proveedorNombre": "Proveedor ABC",
  "tipoFactura": "compra",
  "medioPago": "Transferencia",
  "formaPago": "Crédito",
  "porcentajeRetencion": 2.5,
  "porcentajeReteIva": 15.0,
  "porcentajeReteIca": 0.966,
  "items": [
    {
      "productoId": "123",
      "productoNombre": "Producto A",
      "cantidad": 10,
      "precioUnitario": 1000,
      "porcentajeImpuesto": 19,
      "porcentajeDescuento": 5
    }
  ]
}
```

### Response
```json
{
  "success": true,
  "factura": {
    "_id": "...",
    "numero": "FC-001",
    "subtotal": 10000,
    "totalDescuentos": 500,
    "baseGravable": 9500,
    "totalImpuestos": 1805,
    "valorRetencion": 237.5,
    "valorReteIva": 271.75,
    "valorReteIca": 91.77,
    "totalRetenciones": 601.02,
    "total": 10703.98
  }
}
```

## 🎓 Notas Importantes

- **Retención en la Fuente**: Se aplica sobre la base gravable
- **Rete IVA**: Se aplica sobre el total de impuestos
- **Rete ICA**: Se aplica sobre la base gravable
- **Orden de Cálculo**: Siempre calcular en el orden: Subtotal → Descuentos → Base → Impuestos → Retenciones → Total
- **Redondeo**: Usar 2 decimales para valores monetarios
