# Cambios Backend para Pagos Mixtos y Historial de Pagos

## 1. Pagos mixtos y parciales en pedidos

- Ahora el backend permite registrar pagos parciales en diferentes formas de pago (efectivo, transferencia, tarjeta, etc.) para un mismo pedido.
- Cada pago parcial se registra con monto, forma de pago, fecha y usuario que lo procesó.
- El pedido mantiene un historial de pagos parciales (`pagosParciales`).
- El estado del pedido será "pendiente" si el total pagado no cubre el total del pedido, y "pagado" si se completa.
- Cada pago parcial suma correctamente al cuadre de caja según la forma de pago.

## 2. Endpoint para registrar pagos parciales

- El endpoint `PUT /api/pedidos/{id}/pagar` acepta pagos parciales.
- El DTO de pago debe incluir el campo `montoPago` (double), además de `formaPago`, `propina` y `procesadoPor`.
- Ejemplo de payload:

```json
{
  "montoPago": 5000,
  "formaPago": "efectivo",
  "propina": 0,
  "procesadoPor": "usuario1"
}
```

## 3. Endpoint para consultar historial de pagos

- Nuevo endpoint `GET /api/pedidos/{id}/pagos`.
- Devuelve la lista de pagos parciales realizados sobre el pedido:

```json
[
  {
    "monto": 5000,
    "formaPago": "efectivo",
    "fecha": "2025-10-05T12:34:56",
    "procesadoPor": "usuario1"
  },
  {
    "monto": 3000,
    "formaPago": "transferencia",
    "fecha": "2025-10-05T13:00:00",
    "procesadoPor": "usuario2"
  }
]
```

## 4. Cambios en cuadre de caja

- Cada pago parcial suma al cuadre de caja activo según la forma de pago.
- El desglose de ventas por forma de pago se actualiza automáticamente.

## 5. Endpoint para eliminar pagos parciales

- Nuevo endpoint `DELETE /api/pedidos/{id}/pagos/{index}`.
- Permite eliminar un pago parcial específico por índice en el historial del pedido.
- Al eliminar el pago:
  - Se resta el monto del pago eliminado al total pagado del pedido.
  - Se actualiza el cuadre de caja, restando el monto y ajustando el desglose por forma de pago.
  - Si el total pagado queda menor al total del pedido, el estado del pedido vuelve a "pendiente".
- Respuesta: lista actualizada de pagos parciales del pedido.

```json
[
  {
    "monto": 5000,
    "formaPago": "efectivo",
    "fecha": "2025-10-05T12:34:56",
    "procesadoPor": "usuario1"
  }
]
```

## 6. Endpoint para editar pagos parciales

- Nuevo endpoint `PUT /api/pedidos/{id}/pagos/{index}`.
- Permite modificar los datos de un pago parcial específico (monto, forma de pago, fecha, usuario).
- Al editar el pago:
  - Se recalcula el total pagado del pedido.
  - Se actualiza el cuadre de caja (resta el monto anterior y suma el nuevo, ajustando por forma de pago).
  - El estado del pedido se actualiza según el total pagado.
- Ejemplo de payload:

```json
{
  "monto": 7000,
  "formaPago": "tarjeta",
  "fecha": "2025-10-05T14:00:00",
  "procesadoPor": "usuario3"
}
```

- Respuesta: lista actualizada de pagos parciales del pedido.

## 7. Recomendaciones para el frontend

- Permitir al usuario registrar pagos parciales y seleccionar la forma de pago en el diálogo de pago.
- Mostrar el historial de pagos parciales en la vista de pedido.
- Actualizar el estado del pedido según el total pagado.

## 8. Cambios recientes de backend (Oct 2025)

### Integración de Unidad

- Se creó el modelo `Unidad` con su repositorio y controlador para gestionar unidades de medida centralizadas.
- Ingredientes e inventario ahora referencian la unidad por `unidadId`.
- Los DTOs relevantes incluyen los campos `unidadId`, `unidadNombre` y `unidadAbreviatura`.

### Actualización de DTOs

- Los DTOs de ingredientes y devoluciones ahora devuelven información completa de la unidad.
- Los endpoints de inventario y productos muestran los datos de unidad correctamente.

### Lógica de costo ponderado en inventario

- Al procesar facturas de compras, el costo unitario del inventario se actualiza usando promedio ponderado:
  - `costoUnitario = (costoTotalAnterior + costoTotalNuevo) / nuevoStock`
  - El costo total se actualiza acorde al nuevo stock y compras.

### Resumen de cierre de caja

- El endpoint de resumen de cierre de caja ahora filtra correctamente por el `cuadreCajaId` solicitado.
- Los ingresos, ventas, gastos y compras se muestran solo para la caja actual.
- Los ingresos de caja se obtienen con `findByCuadreCajaId` y se agrupan por forma de pago.
- El resumen final y los movimientos reflejan únicamente los datos del cuadre consultado.

---

**Estos cambios permiten una gestión flexible y transparente de pagos mixtos en el sistema POS.**
