# Documentación de la API - Sistema de Gestión de Restaurante

## Endpoints principales

### Autenticación y Seguridad

#### Login con 2FA

- **URL**: `/api/public/security/login`
- **Método**: `POST`
- **Descripción**: Inicia sesión y envía código de 2 factores.

#### Validar código 2FA

- **URL**: `/api/public/security/login/validate/{twoFactorCode}`
- **Método**: `POST`
- **Descripción**: Valida el código 2FA y devuelve el token JWT.

### Gestión de Productos

#### Obtener todos los productos

- **URL**: `/api/productos`
- **Método**: `GET`
- **Descripción**: Devuelve lista de productos con información completa.

#### Crear producto

- **URL**: `/api/productos`
- **Método**: `POST`
- **Descripción**: Crea un nuevo producto.

#### Buscar productos

- **URL**: `/api/productos/buscar?nombre={nombre}`
- **Método**: `GET`
- **Descripción**: Busca productos por nombre.

### Gestión de Categorías

#### Obtener categorías

- **URL**: `/api/categorias`
- **Método**: `GET`
- **Descripción**: Lista todas las categorías.

#### Crear categoría

- **URL**: `/api/categorias`
- **Método**: `POST`
- **Descripción**: Crea una nueva categoría.

### Gestión de Mesas

#### Obtener mesas

- **URL**: `/api/mesas`
- **Método**: `GET`
- **Descripción**: Lista todas las mesas.

#### Mesas ocupadas

- **URL**: `/api/mesas/ocupadas`
- **Método**: `GET`
- **Descripción**: Lista mesas ocupadas.

#### Mesas libres

- **URL**: `/api/mesas/libres`
- **Método**: `GET`
- **Descripción**: Lista mesas disponibles.

#### Ocupar mesa

- **URL**: `/api/mesas/{id}/ocupar`
- **Método**: `PUT`
- **Descripción**: Marca una mesa como ocupada.

### Gestión de Pedidos

#### Crear pedido

- **URL**: `/api/pedidos`
- **Método**: `POST`
- **Descripción**: Crea un nuevo pedido.

#### Obtener pedidos por estado

- **URL**: `/api/pedidos/estado/{estado}`
- **Método**: `GET`
- **Descripción**: Filtra pedidos por estado.

#### Pedidos de hoy

- **URL**: `/api/pedidos/hoy`
- **Método**: `GET`
- **Descripción**: Obtiene pedidos del día actual.

### Gestión de Inventario

#### Obtener inventario completo

- **URL**: `/api/inventario`
- **Método**: `GET`
- **Descripción**: Lista todo el inventario.

#### Productos con stock bajo

- **URL**: `/api/inventario/stock-bajo`
- **Método**: `GET`
- **Descripción**: Productos que necesitan reabastecimiento.

#### Productos agotados

- **URL**: `/api/inventario/agotados`
- **Método**: `GET`
- **Descripción**: Productos sin stock.

#### Registrar entrada de inventario

- **URL**: `/api/inventario/{id}/entrada`
- **Método**: `POST`
- **Descripción**: Registra entrada de mercancía.
- **Cuerpo**:

```json
{
  "cantidad": 50,
  "motivo": "compra",
  "responsable": "usuario123",
  "proveedor": "Proveedor ABC"
}
```

#### Registrar salida de inventario

- **URL**: `/api/inventario/{id}/salida`
- **Método**: `POST`
- **Descripción**: Registra salida de mercancía.
- **Cuerpo**:

```json
{
  "cantidad": 10,
  "motivo": "venta",
  "responsable": "usuario123",
  "referencia": "pedido123"
}
```

#### Resumen de inventario

- **URL**: `/api/inventario/resumen`
- **Método**: `GET`
- **Descripción**: Estadísticas generales del inventario.

### Gestión de Facturación

#### Obtener todas las facturas

- **URL**: `/api/facturas`
- **Método**: `GET`
- **Descripción**: Lista todas las facturas.

#### Crear factura desde pedido

- **URL**: `/api/facturas/desde-pedido/{pedidoId}`
- **Método**: `POST`
- **Descripción**: Genera factura a partir de un pedido.
- **Cuerpo**:

```json
{
  "clienteNombre": "Juan Pérez",
  "clienteDocumento": "12345678",
  "metodoPago": "efectivo"
}
```

#### Emitir factura

- **URL**: `/api/facturas/{id}/emitir`
- **Método**: `PUT`
- **Descripción**: Cambia estado de borrador a emitida.

#### Registrar pago

- **URL**: `/api/facturas/{id}/pagar`
- **Método**: `PUT`
- **Descripción**: Registra pago de la factura.
- **Cuerpo**:

```json
{
  "montoPagado": 50000,
  "metodoPago": "tarjeta"
}
```

#### Facturas pendientes de pago

- **URL**: `/api/facturas/pendientes-pago`
- **Método**: `GET`
- **Descripción**: Lista facturas sin pagar completamente.

#### Ventas del día

- **URL**: `/api/facturas/ventas-dia`
- **Método**: `GET`
- **Descripción**: Facturas emitidas en el día actual.

#### Resumen de ventas

- **URL**: `/api/facturas/resumen-ventas`
- **Método**: `GET`
- **Descripción**: Estadísticas de ventas del día.

### Reportes y Analytics

#### Dashboard principal

- **URL**: `/api/reportes/dashboard`
- **Método**: `GET`
- **Descripción**: Métricas principales del negocio.

#### Reporte de ventas por período

- **URL**: `/api/reportes/ventas-periodo`
- **Método**: `GET`
- **Parámetros**: `fechaInicio`, `fechaFin`
- **Descripción**: Análisis detallado de ventas.

#### Productos más vendidos

- **URL**: `/api/reportes/productos-mas-vendidos`
- **Método**: `GET`
- **Parámetros**: `dias` (default: 30), `limite` (default: 10)
- **Descripción**: Top de productos por ventas.

#### Inventario valorizado

- **URL**: `/api/reportes/inventario-valorizado`
- **Método**: `GET`
- **Descripción**: Valor total del inventario por categorías.

#### Alertas del sistema

- **URL**: `/api/reportes/alertas`
- **Método**: `GET`
- **Descripción**: Stock bajo, productos vencidos, facturas vencidas.

### Usuarios y Roles

#### Crear usuario

- **URL**: `/api/users`
- **Método**: `POST`
- **Descripción**: Crea un nuevo usuario.

#### Obtener usuarios

- **URL**: `/api/users`
- **Método**: `GET`
- **Descripción**: Lista todos los usuarios.

#### Crear rol

- **URL**: `/api/roles`
- **Método**: `POST`
- **Descripción**: Crea un nuevo rol.

#### Asignar rol a usuario

- **URL**: `/api/usersroles/user/{userId}/role/{roleId}`
- **Método**: `POST`
- **Descripción**: Asigna un rol a un usuario.

## Notas importantes

- **Autenticación**: Todos los endpoints (excepto `/api/public/*`) requieren autenticación JWT.
- **Headers requeridos**: `Authorization: Bearer {token}`
- **Formatos de fecha**: ISO 8601 (yyyy-MM-ddTHH:mm:ss)
- **CORS**: Configurado para `http://localhost:53000`
- **WebSocket**: Disponible en `/ws` para notificaciones en tiempo real
- **Base de datos**: MongoDB Atlas
- **Puerto**: 8081

## Códigos de estado HTTP

- **200**: OK - Operación exitosa
- **201**: Created - Recurso creado exitosamente
- **400**: Bad Request - Datos inválidos
- **401**: Unauthorized - Token inválido o faltante
- **403**: Forbidden - Sin permisos suficientes
- **404**: Not Found - Recurso no encontrado
- **409**: Conflict - Recurso ya existe
- **500**: Internal Server Error - Error del servidor

## Ejemplos de uso

### Flujo típico de venta:

1. **Login**: `/api/public/security/login`
2. **Validar 2FA**: `/api/public/security/login/validate/{code}`
3. **Ocupar mesa**: `/api/mesas/{id}/ocupar`
4. **Crear pedido**: `/api/pedidos`
5. **Generar factura**: `/api/facturas/desde-pedido/{pedidoId}`
6. **Emitir factura**: `/api/facturas/{id}/emitir`
7. **Registrar pago**: `/api/facturas/{id}/pagar`
8. **Liberar mesa**: `/api/mesas/{id}/liberar`

### Gestión de inventario:

1. **Ver alertas**: `/api/reportes/alertas`
2. **Registrar entrada**: `/api/inventario/{id}/entrada`
3. **Ver movimientos**: `/api/inventario/{id}/movimientos`
