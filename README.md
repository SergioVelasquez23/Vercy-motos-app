# Sopa-y-Carbon

Aplicación multiplataforma para la gestión de restaurantes y bares

## Funcionalidades Principales

- Gestión completa de productos, ingredientes e inventario
- Administración de mesas y pedidos
- Sistema de facturación y cuadre de caja
- Panel de administración con reportes
- Sistema de usuarios y permisos
- **Actualizaciones en tiempo real** mediante WebSockets
- **Sistema de auditoría** para seguimiento de operaciones

## Nuevas Funcionalidades

### WebSockets para Actualizaciones en Tiempo Real

El sistema ahora incluye soporte para WebSockets, permitiendo recibir notificaciones en tiempo real sobre:

- Cambios en el estado de mesas
- Actualizaciones de pedidos
- Alertas de inventario
- Operaciones de caja

Para implementar esta funcionalidad en Flutter, consulta la documentación en `docs/flutter_websocket_guide.md`.

### Sistema de Auditoría

Se ha implementado un completo sistema de auditoría que registra todas las operaciones realizadas en:

- Inventario
- Mesas
- Pedidos
- Operaciones de caja

Cada acción registra el usuario, fecha, tipo de operación y los cambios realizados, permitiendo un seguimiento detallado de las actividades del sistema.

Para implementar la interfaz de auditoría en Flutter, consulta la documentación en `docs/flutter_auditoria_guide.md`.
