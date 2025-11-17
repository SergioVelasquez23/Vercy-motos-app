# 🔧 **BACKEND CORREGIDO** - Descuentos y Propinas

## ✅ **Problema Resuelto**

El backend ahora **incluye descuentos y propinas** en:
1. ✅ **Endpoints de resumen de cierre de caja**
2. ✅ **Todos los pedidos pagados** (incluidas cortesías)
3. ✅ **Cálculos correctos** de totales aplicando descuentos

---

## 📊 **Estructura de Respuestas del Backend**

### **🏦 Resumen de Cierre de Caja**

**Endpoint:** `GET /api/cuadre-caja/{id}/resumen-cierre`

```json
{
  "success": true,
  "data": {
    "resumenVentas": {
      "detallesPedidos": [
        {
          "id": "64f123abc...",
          "mesa": "Mesa-01",
          "cliente": "Juan Pérez",
          "tipo": "pagado",  // o "cortesia"
          "fecha": "2025-11-17T14:30:00",
          "fechaPago": "2025-11-17T15:45:00",
          "formaPago": "efectivo",
          "total": 45000.0,           // ← Total base de items
          "totalPagado": 47000.0,
          "pagadoPor": "mesero123",
          
          // 🔥 NUEVOS CAMPOS INCLUIDOS
          "descuento": 3000.0,        // ← Descuento aplicado
          "propina": 5000.0,          // ← Propina añadida
          
          // 🔥 CÁLCULOS AUTOMÁTICOS
          "totalItems": 45000.0,      // ← Total base antes de descuentos
          "totalConDescuento": 42000.0, // ← max(totalItems - descuento, 0)
          "totalFinal": 47000.0       // ← totalConDescuento + propina
        }
      ],
      "ventasPorFormaPago": {
        "efectivo": 47000.0,
        "tarjeta": 25000.0
      },
      "totalVentas": 72000.0
    }
  }
}
```

### **📋 Endpoints de Pedidos Normales**

**Endpoint:** `GET /api/pedidos` | `GET /api/pedidos/{id}` | `GET /api/pedidos/mesa/{mesa}`

```json
{
  "success": true,
  "data": [
    {
      "_id": "64f123abc...",
      "mesa": "Mesa-01",
      "cliente": "Juan Pérez",
      "mesero": "mesero123",
      "items": [
        {
          "productoId": "prod123",
          "productoNombre": "Hamburguesa",
          "cantidad": 2,
          "precioUnitario": 15000.0
        }
      ],
      "total": 45000.0,              // ← Total base de items
      
      // 🔥 CAMPOS SIEMPRE INCLUIDOS
      "descuento": 3000.0,           // ← Descuento aplicado (frontend lo controla)
      "incluyePropina": true,        // ← Si incluye propina en el total
      "propina": 5000.0,             // ← Propina añadida
      
      "estado": "pagado",            // ← pagado, pendiente, cortesia, etc.
      "tipo": "pagado",              // ← tipo específico
      "formaPago": "efectivo",
      "totalPagado": 47000.0,        // ← Monto final pagado
      "fecha": "2025-11-17T14:30:00",
      "fechaPago": "2025-11-17T15:45:00",
      "pagadoPor": "mesero123"
    }
  ]
}
```

---

## 🧮 **Lógica de Cálculos del Backend**

### **📊 Fórmulas Aplicadas:**

```java
// 1. Total de items (suma de productos)
double totalItems = pedido.getTotal(); // Base sin descuentos ni propinas

// 2. Aplicar descuento
double descuento = pedido.getDescuento() != null ? pedido.getDescuento() : 0.0;
double totalConDescuento = Math.max(totalItems - descuento, 0.0); // Nunca negativo

// 3. Añadir propina
double propina = pedido.getPropina() != null ? pedido.getPropina() : 0.0;
double totalFinal = totalConDescuento + propina;

// 4. Total pagado efectivo
double totalPagado = pedido.getTotalPagado() > 0 ? 
    pedido.getTotalPagado() : totalFinal;
```

### **🔄 Escenarios Cubiertos:**

1. **Pedidos normales pagados:** ✅ Incluyen descuento y propina
2. **Pedidos de cortesía:** ✅ Incluyen descuento (propina opcional)
3. **Pagos parciales:** ✅ Mantienen proporciones de descuentos
4. **Pedidos eliminados:** ✅ Se restan correctamente del resumen
5. **Resumen de cierre:** ✅ Incluye todos los campos necesarios

---

## 🌐 **Cambios Implementados en el Backend**

### **📁 Archivo Modificado:**
`src/main/java/com/prog3/security/Services/ResumenCierreServiceUnificado.java`

### **🔧 Métodos Actualizados:**

1. **`convertirPedidoADetalle()`**
   - ✅ Añade `descuento` a la respuesta
   - ✅ Añade `propina` a la respuesta
   - ✅ Calcula `totalItems`, `totalConDescuento`, `totalFinal`

2. **`generarResumenVentas()` - Pedidos normales**
   - ✅ Calcula totales aplicando descuentos y propinas
   - ✅ Usa `montoFinalPedido` para pagos únicos

3. **`generarResumenVentas()` - Pedidos eliminados**
   - ✅ Calcula totales aplicando descuentos y propinas
   - ✅ Resta correctamente del total de ventas

### **💰 Impacto en Cálculos:**

| Escenario | Antes | Después |
|-----------|-------|---------|
| Pedido con descuento | Total = 45000 | Total = max(45000 - 3000, 0) = 42000 |
| Pedido con propina | Total = 45000 | Total = 42000 + 5000 = 47000 |
| Resumen de cierre | No incluía campos | Incluye descuento + propina + cálculos |
| Cortesías | Solo total base | Total con descuento aplicado |

---

## 🎯 **Para el Frontend Flutter**

### **✅ Lo que YA NO necesitas hacer:**

1. ❌ **No calcules totales finales** - El backend ya lo hace
2. ❌ **No apliques descuentos manualmente** - El backend los aplica
3. ❌ **No sumes propinas al mostrar totales** - Ya están incluidas

### **✅ Lo que SÍ debes hacer:**

1. ✅ **Envía `descuento` en actualizaciones** - Para que backend lo aplique
2. ✅ **Envía `propina` al pagar** - Para que se incluya en total final
3. ✅ **Usa valores devueltos por backend** - Para mostrar totales correctos
4. ✅ **Valida descuentos antes de enviar** - No mayores al total de items

### **📱 Ejemplo de Petición Frontend:**

```dart
// PUT /api/pedidos/{id}
{
  "mesa": "Mesa-01",
  "items": [...],
  "descuento": 3000.0,        // ← Frontend controla este valor
  "incluyePropina": false,    // ← Si se incluye en el total
  "propina": 0.0,             // ← Propina (normalmente al pagar)
  // Backend calculará automáticamente:
  // - totalConDescuento = max(totalItems - 3000, 0)
  // - totalFinal = totalConDescuento + propina
}
```

### **📊 Ejemplo de Respuesta Backend:**

```dart
// El backend devuelve:
{
  "_id": "64f123...",
  "total": 45000.0,           // Total base de items
  "descuento": 3000.0,        // Descuento aplicado
  "propina": 0.0,             // Propina
  "totalPagado": 42000.0,     // Total final = max(45000-3000, 0) + 0
  // Usar totalPagado para mostrar el total correcto al usuario
}
```

---

## 🚀 **Estado Actual**

- ✅ **Backend corregido** - Incluye descuentos y propinas en todas las respuestas
- ✅ **Cálculos automáticos** - Aplica descuentos y suma propinas correctamente  
- ✅ **Resumen de cierre** - Incluye todos los campos necesarios
- ✅ **Cortesías incluidas** - Se aplican descuentos también a cortesías
- ✅ **Pedidos eliminados** - Se restan correctamente con descuentos aplicados

**🎯 El frontend Flutter ya puede trabajar correctamente con estos endpoints mejorados.**