# 🔧 Resumen de Problemas y Soluciones - Sistema Sopa y Carbón

## ❌ Problemas Detectados

### 1. **Timeouts del Servidor (CRÍTICO)**
```
⚠️ Error: TimeoutException after 0:00:10.000000
```

**Causa:** Render.com plan gratuito desactiva el servidor después de 15 minutos sin actividad.  
**Efecto:** Primer request tarda 30-60 segundos en responder (cold start).

**Solución:**
- ✅ Ya configurado timeout de 60 segundos en `application.properties`
- 🔄 Necesitas: Hacer ping cada 10 minutos o actualizar a plan pago de Render

### 2. **WebSocket No Conecta**
```
❌ WebSocket connection to 'wss://sopa-y-carbon.onrender.com/ws/updates' failed
```

**Causa:** Servidor dormido + WebSocket requiere conexión persistente  
**Efecto:** No hay actualizaciones en tiempo real

**Solución:**
- ✅ Ya configurado correctamente en `WebSocketConfig.java`
- 🔄 Solo funciona cuando servidor está activo
- 💡 Alternativa: Polling cada 30 segundos en lugar de WebSocket

### 3. **Endpoint Mesa Vacía (ERROR 500)** ✅ CORREGIDO
```
GET https://sopa-y-carbon.onrender.com/api/pedidos/mesa/ 500
```

**Causa:** Frontend enviaba nombre de mesa vacío  
**Solución:** ✅ Agregada validación en `PedidosController.java` línea 213-216

### 4. **Productos No Cargan**
```
📦 Productos cargados: 0
```

**Causa:** Timeout antes de que el servidor responda  
**Solución:** Esperar a que el servidor despierte (30-60 segundos)

### 5. **Fuentes Roboto Corruptas**
```
Failed to decode downloaded font: Roboto-Regular.ttf
OTS parsing error: invalid sfntVersion
```

**Causa:** Archivos .ttf en `/web/assets/fonts/` están dañados  
**Solución:** 
```bash
# Desde la carpeta del proyecto Flutter
cd "D:\prueba sopa y carbon\serch-restapp"
# Re-descargar fuentes
flutter pub get
# O copiar fuentes válidas de Google Fonts
```

---

## ✅ Lo Que YA FUNCIONA (Backend/Frontend Sincronizados)

1. ✅ **Modelos sincronizados**: Pedido, Mesa, Factura, ItemPedido
2. ✅ **Pagos mixtos**: Backend soporta múltiples formas de pago
3. ✅ **Validaciones**: Caja abierta, stock, etc.
4. ✅ **WebSocket configurado**: Solo necesita servidor activo
5. ✅ **Timeouts aumentados**: 60 segundos en servidor
6. ✅ **Campo `agregadoPor`**: Ya existe en ItemPedido
7. ✅ **Historial de ediciones**: Backend registra automáticamente

---

## 🔨 Cambios Realizados Hoy

### Backend (Java Spring Boot)
- ✅ Validación de mesa no vacía en `/api/pedidos/mesa/{mesa}`

---

## 📋 Tareas Pendientes (Requieren Decisiones Tuyas)

### Backend
1. **NIT y nombre cliente en facturas** - Agregar campos `nombreCliente` y `correoCliente`
2. **Eliminar System.out.println** - Limpiar logs (300+ ocurrencias)
3. **Notificación WebSocket para productos** - Cuando se actualiza un producto
4. **Bug decoración eventos** - Necesito ver qué producto se está agregando
5. **Campo agregadoPor en todos los lugares** - Asegurar que se use consistentemente
6. **Soporte 2 cajas simultáneas** - Agregar campo `esSecundaria`
7. **Arreglar panel admin** - Mejorar eliminaciones

### Frontend (Flutter)
1. **Aumentar timeout a 30 segundos** - Para manejar cold start de Render
2. **Agregar retry automático** - Si falla, reintentar después de 10 segundos
3. **Indicador de "servidor iniciando"** - Mostrar cuando está en cold start
4. **Recargas automáticas post-diálogo** - Refrescar después de acciones
5. **Consolidar botones Excel** - Un solo botón en contador de efectivo
6. **Mejorar vista resumen móvil** - Ajustes de UI/UX

---

## 🚀 Recomendaciones Inmediatas

### Para Desarrollo Local
```bash
# Backend
cd "D:\prueba sopa y carbon\Sopa-y-Carbon"
mvnw spring-boot:run

# Frontend (en otra terminal)
cd "D:\prueba sopa y carbon\serch-restapp"
flutter run -d chrome
```

Esto evita los problemas de Render durante desarrollo.

### Para Producción
**Opción 1: Mantener Plan Gratuito Render**
- Crear servicio que haga ping cada 10 minutos
- Aceptar 30-60 segundos de carga inicial
- Mostrar mensaje "Iniciando servidor..." en frontend

**Opción 2: Actualizar a Render Paid ($7/mes)**
- Servidor siempre activo
- Sin cold starts
- WebSocket funciona 24/7

**Opción 3: Migrar a Railway/Fly.io**
- Planes gratuitos más generosos
- Mejor para WebSockets

---

## 🐛 Bug de Decoración de Eventos (No Resuelto)

```
Punto 5: Las mesas están agregando decoración de eventos 
siempre aunque no se seleccione, sumando 50mil pesos extra
```

**No encontré** en el código ninguna referencia a "decoración de eventos" o "50000".

**Necesito que me digas:**
1. ¿Qué producto específico se está agregando?
2. ¿En qué momento sucede? (¿Al crear pedido?, ¿Al agregar producto?)
3. ¿Captura de pantalla del problema?

---

## 📊 Estado Actual

| Problema | Estado | Prioridad |
|----------|--------|-----------|
| Timeouts | 🟡 Limitación de Render | Alta |
| WebSocket | 🟡 Depende de servidor activo | Media |
| Mesa vacía | ✅ Corregido | - |
| Productos no cargan | 🟡 Por timeouts | Alta |
| Fuentes corruptas | ⚠️ Requiere acción | Baja |
| NIT en facturas | 🔴 Pendiente | Media |
| Decoración eventos | 🔴 Necesito más info | Alta |
| 2 cajas simultáneas | 🔴 Pendiente | Media |

---

## 💡 Próximos Pasos Sugeridos

1. **Inmediato**: Trabajar en local para evitar timeouts de Render
2. **Corto plazo**: Agregar NIT/cliente en facturas
3. **Mediano plazo**: Eliminar prints y mejorar logging
4. **Decisión**: ¿Migrar servidor o crear keep-alive service?

---

¿Qué quieres que aborde primero? Puedo ayudarte con cualquiera de las tareas backend listadas.
