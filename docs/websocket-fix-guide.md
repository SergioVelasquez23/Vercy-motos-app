# Guía de WebSocket - Sopa y Carbon

## ✅ Problemas Solucionados

### 1. Error 400 BAD_REQUEST eliminado

- **Antes**: Múltiples endpoints (`/ws`, `/ws/updates`, `/ws-native`) causaban conflictos
- **Ahora**: Un solo endpoint `/ws` optimizado para Render

### 2. Configuración simplificada

- **Heartbeat**: 25 segundos para evitar timeouts en Render
- **Session cookies**: Deshabilitadas para mejor compatibilidad
- **CORS**: Configurado para todos los orígenes frontend

### 3. Herramientas de debug añadidas

- **Página de test**: `https://sopa-y-carbon.onrender.com/websocket-test.html`
- **Endpoint de test**: `GET /api/test-websocket`
- **Controladores de test**: `/app/test` y `/app/echo`

## 📋 Configuración Frontend

### Conexión correcta al WebSocket:

```javascript
// ✅ Correcto - usar solo /ws
const socket = new SockJS("https://sopa-y-carbon.onrender.com/ws");

// ❌ Incorrecto - no usar /ws/updates
const socket = new SockJS("https://sopa-y-carbon.onrender.com/ws/updates");
```

### Código completo de conexión:

```javascript
const stompClient = new StompJs.Client({
  webSocketFactory: () => new SockJS("https://sopa-y-carbon.onrender.com/ws"),
  debug: (str) => console.log("STOMP:", str),
  onConnect: (frame) => {
    console.log("✅ Conectado a WebSocket");

    // Suscribirse a notificaciones de pedidos
    stompClient.subscribe("/topic/pedidos", (message) => {
      const data = JSON.parse(message.body);
      console.log("Pedido actualizado:", data);
    });

    // Suscribirse a notificaciones de mesas
    stompClient.subscribe("/topic/mesas", (message) => {
      const data = JSON.parse(message.body);
      console.log("Mesa actualizada:", data);
    });
  },
  onStompError: (frame) => {
    console.error("❌ Error STOMP:", frame.headers["message"]);
  },
});

stompClient.activate();
```

## 🧪 Testing

### 1. Usar la página de debug

Visita: `https://sopa-y-carbon.onrender.com/websocket-test.html`

### 2. Test desde API

```bash
curl https://sopa-y-carbon.onrender.com/api/test-websocket
```

### 3. Test de conectividad básica

```bash
curl https://sopa-y-carbon.onrender.com/api/status
```

## 📡 Endpoints WebSocket Disponibles

### Suscripciones (recibir mensajes):

- `/topic/pedidos` - Notificaciones de pedidos
- `/topic/mesas` - Notificaciones de mesas
- `/topic/test` - Para testing
- `/topic/echo` - Para testing echo

### Envío de mensajes:

- `/app/test` - Enviar mensaje de prueba
- `/app/echo` - Enviar mensaje echo

## 🔧 Configuración de Render

En el panel de Render, asegúrate de que:

1. **WebSocket Support** esté activado
2. El servicio use el puerto correcto (8080)
3. No haya proxy que bloquee WebSockets

## 📊 Monitoreo

### Logs a revistar:

```
✅ "STOMP broker relay started" - Broker iniciado
✅ "WebSocket connection established" - Conexión establecida
❌ "WebSocket connection failed" - Revisar configuración
❌ "STOMP ERROR" - Revisar endpoints
```

### Estados de conexión:

- **CONNECTING** - Estableciendo conexión
- **OPEN** - Conectado y listo
- **CLOSING** - Cerrando conexión
- **CLOSED** - Desconectado

## ⚡ Siguiente pasos

1. **Actualizar frontend**: Cambiar todas las conexiones a usar solo `/ws`
2. **Verificar en producción**: Usar la página de test
3. **Monitorear logs**: Revisar que no aparezcan más errores 400
4. **Optimizar**: Ajustar heartbeat si es necesario

---

**Nota**: Esta configuración está optimizada para Render.com y debería eliminar completamente los errores 400 BAD_REQUEST.
