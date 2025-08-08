# Sistema de Cancelación Selectiva de Ingredientes

## Descripción

Este sistema permite cancelar productos de un pedido con la opción de devolver selectivamente los ingredientes al inventario. Esto es útil para casos donde algunos ingredientes ya fueron preparados (como carne asada) y no se pueden devolver, mientras que otros sí pueden regresar al stock.

## Funcionalidades Implementadas

### 1. DTO para Cancelación de Productos

**Archivo:** `CancelarProductoRequest.java`

Permite especificar:

- ID del pedido y producto a cancelar
- Cantidad a cancelar
- Motivo de cancelación
- Lista de ingredientes con opción individual de devolución
- Observaciones adicionales

### 2. Servicios de Inventario Extendidos

**Archivo:** `InventarioService.java`

Nuevos métodos agregados:

- `devolverIngredientesAlInventario()`: Devuelve ingredientes específicos al stock
- `getIngredientesDescontadosParaProducto()`: Obtiene los ingredientes que fueron descontados

### 3. Endpoints REST en PedidosController

#### GET /api/pedidos/{pedidoId}/producto/{productoId}/ingredientes-devolucion

- **Descripción**: Obtiene la lista de ingredientes que se pueden devolver al cancelar un producto
- **Parámetros**:
  - `pedidoId`: ID del pedido
  - `productoId`: ID del producto
  - `cantidad`: Cantidad del producto a cancelar
- **Respuesta**: Lista de ingredientes con información de cantidades descontadas

#### POST /api/pedidos/cancelar-producto

- **Descripción**: Cancela un producto del pedido con devolución selectiva de ingredientes
- **Body**: Objeto `CancelarProductoRequest`
- **Respuesta**: Pedido actualizado

## Flujo de Uso Desde el Frontend

### Paso 1: Obtener Ingredientes Disponibles para Devolución

```javascript
// Llamada para obtener los ingredientes que se pueden devolver
const response = await fetch(
  `/api/pedidos/${pedidoId}/producto/${productoId}/ingredientes-devolucion?cantidad=${cantidadACancelar}`
);
const ingredientesDisponibles = await response.json();
```

### Paso 2: Mostrar Interfaz de Selección

El frontend debe mostrar una lista con cada ingrediente permitiendo al usuario decidir:

- ✅ **Devolver al inventario**: El ingrediente no fue usado/preparado
- ❌ **No devolver**: El ingrediente ya fue preparado (ej: carne asada)
- **Motivo**: Campo opcional para especificar por qué no se devuelve

### Paso 3: Enviar Cancelación

```javascript
const cancelarRequest = {
  pedidoId: "66d5f8a...",
  productoId: "66d5f8b...",
  cantidadACancelar: 1,
  motivoCancelacion: "Cliente cambió de opinión",
  canceladoPor: "mesero@restaurante.com",
  ingredientesADevolver: [
    {
      ingredienteId: "66d5f8c...",
      nombreIngrediente: "Carne de Res",
      cantidadOriginal: 250,
      cantidadADevolver: 0, // No devolver
      unidad: "gramos",
      devolver: false,
      motivoNoDevolucion: "Ya fue asada",
    },
    {
      ingredienteId: "66d5f8d...",
      nombreIngrediente: "Lechuga",
      cantidadOriginal: 50,
      cantidadADevolver: 50, // Devolver completa
      unidad: "gramos",
      devolver: true,
      motivoNoDevolucion: null,
    },
  ],
  notas: "Verificado que la carne ya estaba en preparación",
};

const response = await fetch("/api/pedidos/cancelar-producto", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify(cancelarRequest),
});
```

## Características del Sistema

### ✅ Ventajas

1. **Flexibilidad**: Permite decidir ingrediente por ingrediente
2. **Control de inventario**: Evita pérdidas por devoluciones incorrectas
3. **Trazabilidad**: Registra todos los movimientos en `MovimientoInventario`
4. **Auditoria**: Guarda motivos y responsables de cada cancelación

### 📋 Consideraciones

1. **Validación**: Verifica que existe suficiente cantidad para cancelar
2. **Recálculo automático**: El total del pedido se recalcula automáticamente
3. **Historial**: Todas las acciones quedan registradas con timestamps
4. **Estados**: El sistema respeta los estados del pedido para las cancelaciones

## Ejemplo de Interfaz Sugerida

```html
<div class="cancelacion-producto">
  <h3>Cancelar: Hamburguesa Especial (1 unidad)</h3>

  <div class="ingredientes-list">
    <div class="ingrediente-item">
      <span class="nombre">🥩 Carne de Res (250g)</span>
      <label>
        <input type="checkbox" checked="false" /> Devolver al inventario
      </label>
      <input type="text" placeholder="Motivo (ej: ya fue asada)" />
    </div>

    <div class="ingrediente-item">
      <span class="nombre">🥬 Lechuga (50g)</span>
      <label>
        <input type="checkbox" checked="true" /> Devolver al inventario
      </label>
    </div>

    <div class="ingrediente-item">
      <span class="nombre">🍞 Pan (1 unidad)</span>
      <label>
        <input type="checkbox" checked="false" /> Devolver al inventario
      </label>
      <input type="text" placeholder="Motivo (ej: ya fue tostado)" />
    </div>
  </div>

  <div class="form-actions">
    <button onclick="confirmarCancelacion()">Confirmar Cancelación</button>
    <button onclick="cerrarModal()">Cancelar</button>
  </div>
</div>
```

## Configuración de Productos

Para que el sistema funcione correctamente, los productos deben tener configurados sus ingredientes:

```javascript
// Ejemplo de producto configurado
{
  "_id": "66d5f8b...",
  "nombre": "Hamburguesa Especial",
  "tieneIngredientes": true,
  "ingredientesRequeridos": [
    {
      "ingredienteId": "66d5f8c...",
      "nombre": "Carne de Res",
      "cantidadNecesaria": 250,
      "unidad": "gramos"
    },
    {
      "ingredienteId": "66d5f8d...",
      "nombre": "Lechuga",
      "cantidadNecesaria": 50,
      "unidad": "gramos"
    }
  ]
}
```

Este sistema proporciona un control granular sobre el inventario, permitiendo una gestión más precisa de los recursos del restaurante.
