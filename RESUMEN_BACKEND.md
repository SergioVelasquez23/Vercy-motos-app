# Resumen del Backend - Sistema Sopa y Carbon

Este documento proporciona un resumen completo de las capacidades del backend para el sistema de gestión de restaurante "Sopa y Carbon". El sistema está construido sobre Spring Boot y utiliza MongoDB como base de datos.

## Índice

1. [Gestión de Usuarios y Seguridad](#1-gestión-de-usuarios-y-seguridad)
2. [Gestión de Mesas](#2-gestión-de-mesas)
3. [Gestión de Pedidos](#3-gestión-de-pedidos)
4. [Gestión de Inventario](#4-gestión-de-inventario)
5. [Gestión de Productos](#5-gestión-de-productos)
6. [Facturación y Caja](#6-facturación-y-caja)
7. [Reportes y Dashboard](#7-reportes-y-dashboard)
8. [Gestión de Documentos](#8-gestión-de-documentos)
9. [Gestión de Categorías](#9-gestión-de-categorías)
10. [Gestión de Roles y Permisos](#10-gestión-de-roles-y-permisos)

---

## 1. Gestión de Usuarios y Seguridad

### Endpoints de Autenticación

- **Iniciar sesión**: `/api/public/security/login-no-auth` (POST)
  - Autentica usuarios y devuelve token JWT
- **Verificar roles de usuario**: `/api/public/security/me/roles` (GET)
  - Obtiene los roles asociados al usuario actual
- **Gestionar sesiones**:
  - `/api/public/security/{userId}/matchSession/{sessionId}` (PUT)
  - `/api/public/security/{userId}/unmatchSession/{sessionId}` (PUT)
  - `/api/public/security/user/{userId}/numeroDeSesiones` (GET)

### Gestión de Usuarios

- **Listar usuarios**: `/api/users` (GET)
- **Obtener usuario por ID**: `/api/users/{id}` (GET)
- **Crear usuario**: `/api/users` (POST)
- **Actualizar usuario**: `/api/users/{id}` (PUT)
- **Eliminar usuario**: `/api/users/{id}` (DELETE)

### Comunicación

- **Enviar emails**: `/api/public/security/send-email` (POST)
- **Enviar enlaces de restablecimiento**: `/api/public/security/send-reset-link` (POST)
- **Enviar notificaciones de pago**: `/api/public/security/send-payment-notification` (POST)

---

## 2. Gestión de Mesas

### Operaciones Básicas de Mesas

- **Listar todas las mesas**: `/api/mesas` (GET)
- **Obtener mesa por ID**: `/api/mesas/{id}` (GET)
- **Obtener mesa por nombre**: `/api/mesas/nombre/{nombre}` (GET)
- **Crear mesa nueva**: `/api/mesas` (POST)
- **Actualizar mesa**: `/api/mesas/{id}` (PUT)
- **Eliminar mesa**: `/api/mesas/{id}` (DELETE)

### Filtros de Mesas

- **Listar mesas ocupadas**: `/api/mesas/ocupadas` (GET)
- **Listar mesas libres**: `/api/mesas/libres` (GET)
- **Filtrar por estado**: `/api/mesas/estado/{ocupada}` (GET)
- **Filtrar por rango de total**: `/api/mesas/total?min=X&max=Y` (GET)

### Estado de Mesas

- **Ocupar mesa**: `/api/mesas/{id}/ocupar` (PUT)
- **Liberar mesa**: `/api/mesas/{id}/liberar` (PUT)
- **Calcular total**: `/api/mesas/{id}/calcular-total` (PUT)

### Productos en Mesas

- **Agregar producto**: `/api/mesas/{id}/productos` (POST)
- **Eliminar producto**: `/api/mesas/{id}/productos/{productoId}` (DELETE)

---

## 3. Gestión de Pedidos

### Operaciones Básicas de Pedidos

- **Listar todos los pedidos**: `/api/pedidos` (GET)
- **Obtener pedido por ID**: `/api/pedidos/{id}` (GET)
- **Crear nuevo pedido**: `/api/pedidos` (POST)
- **Actualizar pedido**: `/api/pedidos/{id}` (PUT)
- **Eliminar pedido**: `/api/pedidos/{id}` (DELETE)

### Filtros de Pedidos

- **Filtrar por mesa**: `/api/pedidos/mesa/{mesa}` (GET)
- **Filtrar por estado**: `/api/pedidos/estado/{estado}` (GET)
- **Filtrar por fecha**: `/api/pedidos/fecha?inicio=X&fin=Y` (GET)
- **Filtrar por cliente**: `/api/pedidos/cliente/{cliente}` (GET)
- **Filtrar por mesero**: `/api/pedidos/mesero/{mesero}` (GET)
- **Obtener pedido de mesa por nombre**: `/api/pedidos/mesa/{mesa}/nombre/{nombrePedido}` (GET)

### Gestión de Estado de Pedidos

- **Cambiar estado**: `/api/pedidos/{id}/estado/{estado}` (PUT)
- **Marcar como pagado**: `/api/pedidos/{id}/pagar` (PUT)
  - Requiere body con detalles de pago

### Operaciones Especiales

- **Crear pedido para mesa especial**: `/api/pedidos/mesa-especial` (POST)
  - Basado en `CrearPedidoMesaEspecialRequest`
- **Cancelar pedido**: `/api/pedidos/cancelar` (POST)
  - Basado en `CancelarPedidoRequest`
- **Cancelar producto en pedido**: `/api/pedidos/cancelar-producto` (POST)
  - Basado en `CancelarProductoRequest`
  - Permite devolución selectiva de ingredientes al inventario
- **Vaciar pedidos de mesa**: `/api/pedidos/mesa/{mesa}/vaciar` (DELETE)
- **Eliminar pedido específico**: `/api/pedidos/mesa/{mesa}/pedido/{nombrePedido}` (DELETE)

### Integraciones con Inventario

- **Obtener ingredientes para devolución**: `/api/pedidos/{pedidoId}/producto/{productoId}/ingredientes-devolucion` (GET)
- **Procesar inventario (test)**: `/api/pedidos/{id}/test-inventario` (POST)

### Reportes de Ventas

- **Obtener total de ventas**: `/api/pedidos/total-ventas` (GET)
  - Parámetros: fechaInicio, fechaFin

---

## 4. Gestión de Inventario

### Operaciones Básicas de Inventario

- **Listar todo el inventario**: `/api/inventario` (GET)
- **Obtener inventario por ID**: `/api/inventario/{id}` (GET)
- **Crear nuevo registro**: `/api/inventario` (POST)
- **Actualizar registro**: `/api/inventario/{id}` (PUT)
- **Eliminar registro**: `/api/inventario/{id}` (DELETE)

### Gestión de Stock

- **Registrar entrada de inventario**: `/api/inventario/{id}/entrada` (POST)
- **Registrar salida de inventario**: `/api/inventario/{id}/salida` (POST)
- **Obtener movimientos**:
  - Por item: `/api/inventario/{id}/movimientos` (GET)
  - Todos: `/api/inventario/movimientos` (GET)

### Procesamiento de Pedidos

- **Procesar pedido (descontar inventario)**: `/api/inventario/procesar-pedido/{pedidoId}` (POST)
  - Actualiza automáticamente el stock de ingredientes al realizar pedidos

### Diagnóstico

- **Ver estado de stock**: `/api/inventario/debug/estado-stock` (GET)

---

## 5. Gestión de Ingredientes

### Operaciones Básicas

- **Listar todos los ingredientes**: `/api/ingredientes` (GET)
- **Obtener ingrediente por ID**: `/api/ingredientes/{id}` (GET)
- **Crear ingrediente**: `/api/ingredientes` (POST)
- **Crear múltiples ingredientes**: `/api/ingredientes/batch` (POST)
- **Actualizar ingrediente**: `/api/ingredientes/{id}` (PUT)
- **Eliminar ingrediente**: `/api/ingredientes/{id}` (DELETE)
- **Eliminar todos los ingredientes**: `/api/ingredientes/deleteAll` (DELETE)

### Gestión de Stock

- **Actualizar stock**: `/api/ingredientes/{id}/stock` (PUT)
  - Parámetro: cantidad

---

## 6. Facturación y Caja

### Facturas

- **Listar facturas**: `/api/facturas` (GET)
- **Obtener factura por ID**: `/api/facturas/{id}` (GET)
- **Crear factura**: `/api/facturas` (POST)
- **Actualizar factura**: `/api/facturas/{id}` (PUT)
- **Eliminar factura**: `/api/facturas/{id}` (DELETE)

### Cuadres de Caja

- **Listar cuadres**: `/api/cuadres-caja` (GET)
- **Obtener cuadre por ID**: `/api/cuadres-caja/{id}` (GET)
- **Listar cuadres de hoy**: `/api/cuadres-caja/hoy` (GET)
- **Crear cuadre**: `/api/cuadres-caja` (POST)
- **Actualizar cuadre**: `/api/cuadres-caja/{id}` (PUT)

### Operaciones de Caja

- **Obtener cuadre completo**: `/api/cuadres-caja/cuadre-completo` (GET)
- **Obtener efectivo esperado**: `/api/cuadres-caja/efectivo-esperado` (GET)
- **Obtener información de apertura**: `/api/cuadres-caja/info-apertura` (GET)
- **Obtener cajas abiertas**: `/api/cuadres-caja/abiertas` (GET)
- **Aprobar cuadre**: `/api/cuadres-caja/{id}/aprobar` (PUT)
- **Rechazar cuadre**: `/api/cuadres-caja/{id}/rechazar` (PUT)

### Reportes de Ventas

- **Obtener detalles de ventas**: `/api/cuadres-caja/detalles-ventas` (GET)
- **Obtener todos los pedidos de hoy**: `/api/cuadres-caja/todos-pedidos-hoy` (GET)

---

## 7. Reportes y Dashboard

### Dashboard

- **Obtener dashboard principal**: `/api/dashboard` (GET)
  - Resumen general de métricas del negocio

### Reportes de Ventas

- **Top productos**: `/api/top-productos?limite=X` (GET)
- **Ventas por día**: `/api/ventas-por-dia?ultimosDias=X` (GET)
- **Ingresos/Egresos**: `/api/ingresos-egresos?ultimosMeses=X` (GET)
- **Pedidos por hora**: `/api/pedidos-por-hora` (GET)
- **Últimos pedidos**: `/api/ultimos-pedidos?limite=X` (GET)
- **Vendedores del mes**: `/api/vendedores-mes?dias=X` (GET)

### Objetivos de Ventas

- **Gestión de objetivos**: Disponibles mediante la entidad `ObjetivoVenta`

---

## 8. Gestión de Documentos

### Documentos Mesa

- **Listar documentos**: `/api/documentos-mesa` (GET)
- **Obtener documento por ID**: `/api/documentos-mesa/{id}` (GET)
- **Crear documento**: `/api/documentos-mesa` (POST)
- **Actualizar documento**: `/api/documentos-mesa/{id}` (PUT)
- **Eliminar documento**: `/api/documentos-mesa/{id}` (DELETE)

### Filtros y Consultas Especiales

- **Filtrar por mesa**: `/api/documentos-mesa/mesa/{mesaNombre}` (GET)
- **Obtener documentos con pedidos**: `/api/documentos-mesa/mesa/{mesaNombre}/completos` (GET)

---

## 9. Gestión de Categorías

### Operaciones Básicas

- **Listar categorías**: `/api/categorias` (GET)
- **Obtener categoría por ID**: `/api/categorias/{id}` (GET)
- **Obtener categoría por nombre**: `/api/categorias/nombre/{nombre}` (GET)
- **Buscar categorías**: `/api/categorias/buscar?nombre=X` (GET)
- **Crear categoría**: `/api/categorias` (POST)
- **Actualizar categoría**: `/api/categorias/{id}` (PUT)
- **Eliminar categoría**: `/api/categorias/{id}` (DELETE)

---

## 10. Gestión de Roles y Permisos

### Roles

- **Listar roles**: `/api/roles` (GET)
- **Obtener rol por ID**: `/api/roles/{id}` (GET)
- **Crear rol**: `/api/roles` (POST)
- **Actualizar rol**: `/api/roles/{id}` (PUT)
- **Eliminar rol**: `/api/roles/{id}` (DELETE)

### Permisos

- **Listar permisos**: `/api/permissions` (GET)
- **Obtener permiso específico**: `/api/permissions/{url}/{method}` (GET)
- **Crear permiso**: `/api/permissions` (POST)
- **Crear múltiples permisos**: `/api/permissions/all` (POST)
- **Actualizar permiso**: `/api/permissions/{id}` (PUT)
- **Eliminar permiso**: `/api/permissions/{id}` (DELETE)

### Asignación de Roles

- **Asignar rol a usuario**: `/api/roles-users` (POST)
- **Obtener roles de usuario**: `/api/users/{userId}/roles` (GET)

### Asignación de Permisos

- **Asignar permisos a rol**: `/api/roles-permissions` (POST)
- **Obtener permisos de rol**: `/api/roles/{roleId}/permissions` (GET)

---

## Estructura de Datos

### Principales Modelos

- **User**: Usuarios del sistema
- **Role**: Roles de usuario
- **Permission**: Permisos de acceso
- **Mesa**: Mesas del restaurante
- **Pedido**: Pedidos de clientes
- **ItemPedido**: Items individuales en un pedido
- **Producto**: Productos del menú
- **Categoria**: Categorías de productos
- **Ingrediente**: Ingredientes usados en productos
- **IngredienteProducto**: Relación entre productos e ingredientes
- **Inventario**: Control de stock de ingredientes
- **MovimientoInventario**: Registro de cambios en inventario
- **DocumentoMesa**: Documento asociado a una mesa con pedidos
- **Factura**: Facturas generadas
- **CuadreCaja**: Registro de cuadres de caja
- **CierreCaja**: Cierres diarios de caja
- **ObjetivoVenta**: Objetivos de ventas configurados

---

Este resumen proporciona una visión general de las capacidades del backend. El sistema está diseñado con una arquitectura de microservicios sobre Spring Boot y MongoDB, permitiendo la gestión completa de un restaurante, desde el inventario hasta la facturación.
