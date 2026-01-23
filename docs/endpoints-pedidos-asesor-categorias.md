# 📋 Endpoints Pedidos de Asesor y Categorías

## 🎯 Sistema de Pedidos de Asesor

### Base URL
```
/api/pedidos-asesor
```

### 1. Crear Pedido
**POST** `/api/pedidos-asesor`

**Body:**
```json
{
  "clienteNombre": "Juan Pérez",
  "clienteId": "optional_client_id",
  "clienteTelefono": "3001234567",
  "clienteDocumento": "1234567890",
  "asesorNombre": "María García",
  "asesorId": "user_id_from_token",
  "items": [
    {
      "productoId": "producto_123",
      "productoNombre": "Moto Honda XR190",
      "cantidad": 1,
      "precioUnitario": 8500000,
      "notas": "Color rojo",
      "ingredientesSeleccionados": [],
      "ingredientesUsados": []
    }
  ],
  "subtotal": 8500000,
  "impuestos": 0,
  "descuento": 0,
  "total": 8500000,
  "estado": "PENDIENTE",
  "observaciones": "Cliente solicita entrega a domicilio"
}
```

**Respuesta Exitosa (200):**
```json
{
  "_id": "pedido_789",
  "clienteNombre": "Juan Pérez",
  "asesorNombre": "María García",
  "items": [...],
  "total": 8500000,
  "estado": "PENDIENTE",
  "facturado": false,
  "fechaCreacion": "2026-01-22T15:30:00",
  "historial": [...]
}
```

---

### 2. Listar Pedidos
**GET** `/api/pedidos-asesor`

**Query Parameters:**
- `estado` (opcional): `PENDIENTE`, `FACTURADO`, `CANCELADO`
- `asesorId` (opcional): ID del asesor

**Ejemplos:**
```
GET /api/pedidos-asesor
GET /api/pedidos-asesor?estado=PENDIENTE
GET /api/pedidos-asesor?asesorId=user_123
GET /api/pedidos-asesor?estado=PENDIENTE&asesorId=user_123
```

**Respuesta:**
```json
[
  {
    "_id": "pedido_1",
    "clienteNombre": "Cliente 1",
    "asesorNombre": "Asesor 1",
    "total": 5000000,
    "estado": "PENDIENTE",
    "fechaCreacion": "2026-01-22T10:00:00"
  },
  ...
]
```

---

### 3. Obtener un Pedido
**GET** `/api/pedidos-asesor/:id`

**Respuesta:**
```json
{
  "_id": "pedido_789",
  "clienteNombre": "Juan Pérez",
  "clienteTelefono": "3001234567",
  "asesorNombre": "María García",
  "items": [...],
  "total": 8500000,
  "estado": "PENDIENTE",
  "historial": [...]
}
```

---

### 4. Facturar Pedido
**PUT** `/api/pedidos-asesor/:id/facturar`

**Body:**
```json
{
  "facturaId": "factura_123",
  "facturadoPor": "Admin User"
}
```

**Respuesta:**
```json
{
  "_id": "pedido_789",
  "estado": "FACTURADO",
  "facturado": true,
  "facturaId": "factura_123",
  "facturadoPor": "Admin User",
  "fechaFacturacion": "2026-01-22T16:00:00"
}
```

---

### 5. Cancelar Pedido
**PUT** `/api/pedidos-asesor/:id/cancelar`

**Body (opcional):**
```json
{
  "usuario": "Admin User",
  "motivo": "Cliente canceló la compra"
}
```

**Respuesta:**
```json
{
  "_id": "pedido_789",
  "estado": "CANCELADO",
  "historial": [...]
}
```

---

### 6. Actualizar Pedido
**PUT** `/api/pedidos-asesor/:id`

**Query Parameter:**
- `usuario` (opcional): Nombre del usuario que actualiza

**Body:**
```json
{
  "clienteNombre": "Juan Pérez Actualizado",
  "clienteTelefono": "3009876543",
  "items": [...],
  "observaciones": "Nuevas observaciones"
}
```

---

### 7. Eliminar Pedido
**DELETE** `/api/pedidos-asesor/:id`

**Respuesta:**
```json
{
  "success": true,
  "message": "Pedido eliminado correctamente"
}
```

---

### 8. Estadísticas
**GET** `/api/pedidos-asesor/estadisticas`

**Respuesta:**
```json
{
  "total": 150,
  "pendientes": 45,
  "facturados": 95,
  "cancelados": 10
}
```

---

## 🏷️ Sistema de Categorías

### Base URL
```
/api/categorias
```

### 1. Listar Categorías
**GET** `/api/categorias`

**Query Parameters:**
- `soloActivas` (opcional, default: true): Filtrar solo activas

**Ejemplos:**
```
GET /api/categorias
GET /api/categorias?soloActivas=false
```

**Respuesta:**
```json
[
  {
    "_id": "cat_1",
    "nombre": "Motos",
    "descripcion": "Motocicletas y vehículos",
    "icono": "motorcycle",
    "color": "#FF5722",
    "imagenUrl": null,
    "orden": 1,
    "activo": true,
    "fechaCreacion": "2026-01-22T10:00:00"
  },
  ...
]
```

---

### 2. Obtener Categoría
**GET** `/api/categorias/:id`

**Respuesta:**
```json
{
  "_id": "cat_1",
  "nombre": "Motos",
  "descripcion": "Motocicletas y vehículos",
  "icono": "motorcycle",
  "color": "#FF5722",
  "orden": 1,
  "activo": true
}
```

---

### 3. Crear Categoría
**POST** `/api/categorias`

**Body:**
```json
{
  "nombre": "Cascos",
  "descripcion": "Cascos de seguridad",
  "icono": "sports_motorsports",
  "color": "#3F51B5",
  "orden": 6,
  "activo": true
}
```

**Respuesta:**
```json
{
  "_id": "cat_6",
  "nombre": "Cascos",
  ...
}
```

---

### 4. Actualizar Categoría
**PUT** `/api/categorias/:id`

**Body:**
```json
{
  "nombre": "Cascos Actualizado",
  "descripcion": "Cascos de protección",
  "color": "#FF0000",
  "orden": 7
}
```

---

### 5. Eliminar Categoría
**DELETE** `/api/categorias/:id`

**Respuesta:**
```json
{
  "success": true,
  "message": "Categoría eliminada correctamente"
}
```

---

### 6. Desactivar Categoría
**PUT** `/api/categorias/:id/desactivar`

**Respuesta:**
```json
{
  "_id": "cat_1",
  "activo": false,
  ...
}
```

---

### 7. Activar Categoría
**PUT** `/api/categorias/:id/activar`

**Respuesta:**
```json
{
  "_id": "cat_1",
  "activo": true,
  ...
}
```

---

### 8. Buscar Categorías
**GET** `/api/categorias/buscar?nombre=moto`

**Respuesta:**
```json
[
  {
    "_id": "cat_1",
    "nombre": "Motos",
    ...
  }
]
```

---

## 🔔 Notificaciones WebSocket

### Pedidos de Asesor
Cuando se crea, factura o cancela un pedido, se envía una notificación:

**Topic:** `/topic/pedidos`

**Payload:**
```json
{
  "tipo": "PEDIDO_ACTUALIZADO",
  "pedidoId": "pedido_123",
  "mesaId": "nombre_cliente",
  "estado": "PENDIENTE",
  "timestamp": "2026-01-22T15:30:00"
}
```

---

## 📊 Estados de Pedido

- **PENDIENTE**: Pedido creado, esperando facturación
- **FACTURADO**: Pedido procesado y facturado
- **CANCELADO**: Pedido cancelado

---

## ✅ Categorías por Defecto

Al iniciar la aplicación, se crean automáticamente:

1. **Motos** - Motocicletas y vehículos (🏍️ motorcycle, #FF5722)
2. **Repuestos** - Repuestos y accesorios (🔧 build, #2196F3)
3. **Servicios** - Servicios de mantenimiento (🔨 construction, #4CAF50)
4. **Accesorios** - Accesorios y equipamiento (🛍️ shopping_bag, #FF9800)
5. **Llantas** - Llantas y neumáticos (💿 album, #9C27B0)

---

## 🔐 Autenticación

Todos los endpoints requieren autenticación mediante JWT:

```
Authorization: Bearer <JWT_TOKEN>
```

---

## ⚠️ Códigos de Error

- **400** - Bad Request (datos inválidos)
- **404** - Not Found (recurso no encontrado)
- **409** - Conflict (nombre duplicado)
- **500** - Internal Server Error

---

## 📝 Notas Importantes

1. **Caché de Categorías**: Las categorías activas se cachean por 5 minutos
2. **Historial**: Todos los cambios en pedidos se registran en el historial
3. **Validaciones**: No se pueden facturar pedidos cancelados ni cancelar pedidos facturados
4. **Eliminación**: Solo se pueden eliminar pedidos no facturados
