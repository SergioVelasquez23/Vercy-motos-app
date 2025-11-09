# Configuración de Keep-Alive para Render.com (GRATIS)

Tu servidor de Render.com (plan gratuito) se duerme después de 15 minutos de inactividad y tarda 30-60 segundos en despertar. Esto causa errores de timeout en el frontend.

## Solución: UptimeRobot (100% Gratuito)

UptimeRobot hará ping a tu servidor cada 5 minutos para mantenerlo despierto.

### Paso 1: Crear cuenta en UptimeRobot

1. Ve a: https://uptimerobot.com/signUp
2. Crea cuenta gratuita (no requiere tarjeta de crédito)
3. Confirma tu email

### Paso 2: Configurar Monitor

1. Click en **"+ Add New Monitor"**
2. Configura así:
   ```
   Monitor Type: HTTP(s)
   Friendly Name: Sopa y Carbon Backend
   URL: https://sopa-y-carbon.onrender.com/api/health/ping
   Monitoring Interval: 5 minutes
   Monitor Timeout: 30 seconds
   ```
3. Click **"Create Monitor"**

### Paso 3: Verificar

- Ve a tu dashboard en UptimeRobot
- Deberías ver el monitor activo con estado "Up"
- Tu servidor ahora se mantendrá despierto 24/7

## Alternativa: Cron-job.org (También Gratuito)

Si prefieres otra opción:

1. Ve a: https://cron-job.org/en/signup/
2. Crea cuenta gratuita
3. Click **"Create cronjob"**
4. Configura:
   ```
   Title: Keep Sopa y Carbon Awake
   Address: https://sopa-y-carbon.onrender.com/api/health/ping
   Schedule: Every 10 minutes
   ```
5. Guarda

## Alternativa: GitHub Actions (Avanzado)

Si quieres usar GitHub Actions, crea este archivo:

`.github/workflows/keep-alive.yml`:

```yaml
name: Keep Render Server Awake

on:
  schedule:
    # Ejecutar cada 10 minutos
    - cron: '*/10 * * * *'
  workflow_dispatch: # Permite ejecutar manualmente

jobs:
  ping:
    runs-on: ubuntu-latest
    steps:
      - name: Ping backend health endpoint
        run: |
          curl -f https://sopa-y-carbon.onrender.com/api/health/ping || exit 0
```

Sube este archivo a tu repositorio de GitHub y se ejecutará automáticamente cada 10 minutos.

## Endpoints Disponibles

Tu backend ya tiene estos endpoints configurados:

- `GET /api/health` - Health check completo (retorna JSON)
- `GET /api/health/ping` - Ping rápido (retorna "pong")

Ambos funcionan para keep-alive, pero `/ping` es más ligero.

## ¿Cómo funciona?

1. UptimeRobot/Cron-job hace ping cada 5-10 minutos
2. Render detecta actividad y NO duerme el servidor
3. Tu app siempre está lista (sin cold starts)
4. Usuarios tienen respuesta instantánea

## Notas Importantes

- **Gratuito permanente** - Estos servicios son gratis para siempre
- **Sin tarjeta requerida** - No necesitas ingresar datos de pago
- **Fácil de cancelar** - Puedes desactivar cuando quieras
- **Limitación de Render** - Render free tier tiene límite de 750 horas/mes
  - Con keep-alive usas 720 horas/mes (30 días × 24h)
  - Queda margen para desarrollo

## Recomendación

Te recomiendo **UptimeRobot** porque:
- ✅ Monitoreo cada 5 minutos (vs 10 en cron-job)
- ✅ Dashboard visual con estadísticas
- ✅ Alertas por email si el servidor cae
- ✅ App móvil disponible
- ✅ SSL monitoring incluido

## Si quieres eliminar el límite

Para servidor 24/7 sin límites ni cold starts:
- Render Starter Plan: $7/mes
- Railway Hobby Plan: $5/mes
- Fly.io Hobby: $5/mes
