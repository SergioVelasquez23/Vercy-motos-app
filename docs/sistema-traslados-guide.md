# Sistema de Traslados entre Bodegas

## Descripción General

El sistema de traslados permite mover productos entre diferentes puntos de almacenamiento (bodegas, almacenes, etc.) con un flujo de aprobación. Cuando se solicita un traslado, queda en estado PENDIENTE hasta que un usuario autorizado lo apruebe o rechace.

## Arquitectura

### Modelos Creados

1. **Traslado.java** (`Models/Traslado.java`)
   - Entidad principal que registra los traslados
   - Campos: número, producto, origen, destino, cantidad, estado, fechas, etc.
   - Estados: PENDIENTE, ACEPTADO, RECHAZADO

2. **TrasladoRequestDTO.java** (`DTOs/TrasladoRequestDTO.java`)
   - DTO para crear nuevas solicitudes de traslado
   - Campos: productoId, origenBodegaId, destinoBodegaId, cantidad, solicitante, observaciones

3. **TrasladoAprobacionDTO.java** (`DTOs/TrasladoAprobacionDTO.java`)
   - DTO para aprobar o rechazar traslados
   - Campos: trasladoId, accion (ACEPTAR/RECHAZAR), aprobador, observaciones

### Repositorio

**TrasladoRepository.java** (`Repositories/TrasladoRepository.java`)
- Métodos de búsqueda por estado, bodega, producto, solicitante
- Generación de número consecutivo de traslado

## Endpoints de la API

Base URL: `/api/inventario/traslados`

### 1. Crear Traslado

**POST** `/api/inventario/traslados`

Crea una nueva solicitud de traslado entre bodegas.

**Request Body:**
```json
{
  "productoId": "64abc123...",
  "origenBodegaId": "64def456...",
  "destinoBodegaId": "64ghi789...",
  "cantidad": 10,
  "solicitante": "Juan Diego Caycedo Cardona",
  "observaciones": "Traslado urgente para punto de venta"
}
```

**Validaciones:**
- Verifica que el producto exista
- Verifica que las bodegas existan
- Valida que origen y destino sean diferentes
- Verifica que haya stock suficiente en la bodega origen
- Genera número consecutivo automáticamente (T1000, T1001, etc.)

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Traslado creado exitosamente",
  "data": {
    "_id": "64jkl012...",
    "numero": "T1856",
    "productoId": "64abc123...",
    "productoNombre": "Aceite Motor 20W50",
    "origenBodegaId": "64def456...",
    "origenBodegaNombre": "BODEGA",
    "destinoBodegaId": "64ghi789...",
    "destinoBodegaNombre": "ALMACEN",
    "cantidad": 10,
    "unidad": "unidad",
    "estado": "PENDIENTE",
    "solicitante": "Juan Diego Caycedo Cardona",
    "observaciones": "Traslado urgente para punto de venta",
    "fechaSolicitud": "2026-01-19T20:05:19",
    "fechaCreacion": "2026-01-19T20:05:19"
  }
}
```

### 2. Listar Traslados

**GET** `/api/inventario/traslados`

Lista todos los traslados del sistema.

**Query Parameters (opcionales):**
- `estado`: Filtrar por estado (PENDIENTE, ACEPTADO, RECHAZADO)
- `bodegaId`: Filtrar por bodega (incluye traslados donde la bodega sea origen o destino)

**Ejemplos:**
```
GET /api/inventario/traslados
GET /api/inventario/traslados?estado=PENDIENTE
GET /api/inventario/traslados?bodegaId=64def456...
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Traslados obtenidos exitosamente",
  "data": [
    {
      "_id": "64jkl012...",
      "numero": "T1856",
      "productoNombre": "Aceite Motor 20W50",
      "origenBodegaNombre": "BODEGA",
      "destinoBodegaNombre": "ALMACEN",
      "cantidad": 1,
      "estado": "ACEPTADO",
      "solicitante": "Juan Diego Caycedo Cardona",
      "fechaSolicitud": "2026-01-19T20:05:19",
      "fechaCompletado": "2026-01-19T20:10:30"
    },
    {
      "_id": "64mno345...",
      "numero": "T1855",
      "productoNombre": "Filtro de Aceite",
      "origenBodegaNombre": "BODEGA",
      "destinoBodegaNombre": "ALMACEN",
      "cantidad": 4,
      "estado": "PENDIENTE",
      "solicitante": "katherin dahiana caycedo",
      "fechaSolicitud": "2026-01-19T19:22:29"
    }
  ]
}
```

### 3. Obtener Traslado por ID

**GET** `/api/inventario/traslados/{id}`

Obtiene el detalle completo de un traslado específico.

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Traslado obtenido exitosamente",
  "data": {
    "_id": "64jkl012...",
    "numero": "T1856",
    "productoId": "64abc123...",
    "productoNombre": "Aceite Motor 20W50",
    "origenBodegaId": "64def456...",
    "origenBodegaNombre": "BODEGA",
    "destinoBodegaId": "64ghi789...",
    "destinoBodegaNombre": "ALMACEN",
    "cantidad": 10,
    "unidad": "unidad",
    "estado": "PENDIENTE",
    "solicitante": "Juan Diego Caycedo Cardona",
    "observaciones": "Traslado urgente para punto de venta",
    "fechaSolicitud": "2026-01-19T20:05:19",
    "fechaCreacion": "2026-01-19T20:05:19",
    "fechaActualizacion": "2026-01-19T20:05:19"
  }
}
```

### 4. Aprobar o Rechazar Traslado

**PUT** `/api/inventario/traslados/procesar`

Procesa un traslado pendiente (acepta o rechaza).

**Request Body:**
```json
{
  "trasladoId": "64jkl012...",
  "accion": "ACEPTAR",
  "aprobador": "Admin Sistema",
  "observaciones": "Aprobado - Stock verificado"
}
```

**Acciones válidas:**
- `ACEPTAR`: Aprueba el traslado y realiza el movimiento de inventario
- `RECHAZAR`: Rechaza el traslado sin mover inventario

**Proceso al ACEPTAR:**
1. Verifica que el traslado esté en estado PENDIENTE
2. Valida que haya stock suficiente en la bodega origen
3. Descuenta la cantidad del inventario origen
4. Agrega la cantidad al inventario destino (crea el registro si no existe)
5. Registra movimientos de inventario (SALIDA en origen, ENTRADA en destino)
6. Actualiza el traslado a estado ACEPTADO

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Traslado aceptar exitosamente",
  "data": {
    "_id": "64jkl012...",
    "numero": "T1856",
    "estado": "ACEPTADO",
    "aprobador": "Admin Sistema",
    "fechaAprobacion": "2026-01-19T20:10:30",
    "fechaCompletado": "2026-01-19T20:10:30",
    "observaciones": "Traslado urgente para punto de venta | Aprobado - Stock verificado"
  }
}
```

## Flujo de Trabajo

```
1. Usuario solicita traslado
   ↓
2. Sistema valida stock y crea traslado en estado PENDIENTE
   ↓
3. Usuario autorizado revisa el traslado
   ↓
4. Usuario aprueba o rechaza
   ↓
   ├─→ ACEPTAR: Se mueve el inventario automáticamente
   │             - Descuenta de origen
   │             - Agrega a destino
   │             - Registra movimientos
   │
   └─→ RECHAZAR: No se mueve inventario
                 - Solo se marca como rechazado
```

## Integración con Sistema Existente

Los traslados se integran con:

1. **InventarioBodega**: Actualiza el stock en ambas bodegas
2. **MovimientoInventario**: Registra las salidas y entradas
3. **Bodega**: Valida que las bodegas existan y estén activas
4. **Producto**: Obtiene información del producto

## Consideraciones Importantes

1. **Stock Temporal**: Cuando un traslado está PENDIENTE, el stock NO se reserva. Se valida al momento de aprobar.

2. **Números Consecutivos**: Los números de traslado (T1000, T1001...) se generan automáticamente.

3. **Auditoría**: Cada traslado registra:
   - Quién lo solicitó
   - Quién lo aprobó/rechazó
   - Todas las fechas de cambio de estado
   - Observaciones en cada paso

4. **Movimientos de Inventario**: Al aceptar un traslado se crean dos movimientos:
   - SALIDA en bodega origen
   - ENTRADA en bodega destino

## Casos de Uso Comunes

### Caso 1: Reposición de Punto de Venta
```javascript
// Crear traslado de bodega principal a punto de venta
POST /api/inventario/traslados
{
  "productoId": "aceite_20w50_id",
  "origenBodegaId": "bodega_principal_id",
  "destinoBodegaId": "punto_venta_id",
  "cantidad": 20,
  "solicitante": "Vendedor 1",
  "observaciones": "Reposición semanal"
}
```

### Caso 2: Devolución a Bodega
```javascript
// Devolver stock no vendido
POST /api/inventario/traslados
{
  "productoId": "filtro_aire_id",
  "origenBodegaId": "punto_venta_id",
  "destinoBodegaId": "bodega_principal_id",
  "cantidad": 5,
  "solicitante": "Vendedor 1",
  "observaciones": "Devolución por cierre de mes"
}
```

### Caso 3: Listar Traslados Pendientes
```javascript
// Ver todos los traslados que necesitan aprobación
GET /api/inventario/traslados?estado=PENDIENTE
```

## Ejemplo de Implementación Frontend

```javascript
// Crear traslado
const crearTraslado = async (datos) => {
  const response = await fetch('/api/inventario/traslados', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(datos)
  });
  return response.json();
};

// Aprobar traslado
const aprobarTraslado = async (trasladoId, aprobador) => {
  const response = await fetch('/api/inventario/traslados/procesar', {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      trasladoId,
      accion: 'ACEPTAR',
      aprobador,
      observaciones: 'Aprobado desde interfaz'
    })
  });
  return response.json();
};

// Listar pendientes
const listarPendientes = async () => {
  const response = await fetch('/api/inventario/traslados?estado=PENDIENTE');
  return response.json();
};
```

## Mensajes de Error Comunes

- **400 Bad Request**: "Datos incompletos para crear el traslado"
- **400 Bad Request**: "La bodega de origen y destino no pueden ser la misma"
- **400 Bad Request**: "Stock insuficiente. Disponible: X, solicitado: Y"
- **400 Bad Request**: "El traslado ya fue procesado"
- **404 Not Found**: "Producto no encontrado"
- **404 Not Found**: "Bodega de origen o destino no encontrada"
- **404 Not Found**: "No hay stock del producto en la bodega de origen"
- **404 Not Found**: "Traslado no encontrado"

## Próximas Mejoras Sugeridas

1. **WebSocket**: Notificaciones en tiempo real cuando hay traslados pendientes
2. **Estados adicionales**: EN_TRANSITO para traslados entre ubicaciones físicas distantes
3. **Permisos**: Control de quién puede crear vs aprobar traslados
4. **Reportes**: Dashboard de traslados por periodo, bodega, producto
5. **Impresión**: Generar documentos PDF de guías de traslado
