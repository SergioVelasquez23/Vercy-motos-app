# 📈 Guía de Optimización de Rendimiento
## Sistema de Restaurante "Sopa y Carbón"

---

## 🎯 Objetivo
Mejorar significativamente el rendimiento del sistema mediante optimización de base de datos y implementación de cache inteligente.

---

## 🔧 Optimizaciones Implementadas

### 1. **Índices MongoDB Optimizados** 📊

#### 📋 Índices Críticos para Pedidos:
```javascript
// Mesa + Estado + Fecha (consulta más frecuente)
db.pedidos.createIndex({ "mesa": 1, "estado": 1, "fecha": -1 })

// Cuadre + Estado (asignación automática)
db.pedidos.createIndex({ "cuadreCajaId": 1, "estado": 1 })

// Fecha Pago + Forma Pago (reportes)
db.pedidos.createIndex({ "fechaPago": -1, "formaPago": 1, "estado": 1 })
```

#### 🪑 Índices para Mesas:
```javascript
// Nombre único (búsqueda directa)
db.mesas.createIndex({ "nombre": 1 }, { unique: true })

// Estado ocupación (listado filtrado)
db.mesas.createIndex({ "ocupada": 1, "total": -1 })
```

#### 💰 Índices para Cuadres de Caja:
```javascript
// Cajas abiertas (consulta muy frecuente)
db.cuadreCaja.createIndex({ "cerrada": 1, "fechaApertura": -1 })
```

### 2. **Sistema de Cache Inteligente** ⚡

#### 🔄 Caches Implementados:
| Cache | TTL | Uso |
|-------|-----|-----|
| `productos` | 5 min | Lista de productos, búsquedas por categoría |
| `mesas` | 2 min | Estado de mesas, disponibilidad |
| `categorias` | 10 min | Listado de categorías (cambian poco) |
| `cuadres-activos` | 1 min | Cajas abiertas (crítico) |
| `pedidos-activos` | 30 seg | Pedidos activos por mesa |
| `ingredientes` | 10 min | Lista de ingredientes |
| `reportes-ventas` | 5 min | Estadísticas del dashboard |

#### 📚 Estrategias de Cache:
- **@Cacheable**: Almacena resultado de consultas frecuentes
- **@CacheEvict**: Invalida cache cuando datos cambian
- **@CachePut**: Actualiza cache con datos frescos
- **Precarga**: Carga datos importantes al iniciar

---

## 🚀 Beneficios Esperados

### ⚡ Rendimiento:
- **Consultas 5-10x más rápidas** para datos cacheados
- **Reducción 70-80% en latencia** de respuestas API
- **Menor carga en MongoDB** (especialmente en horas pico)
- **Mejor experiencia usuario** con respuestas instantáneas

### 📊 Métricas Objetivo:
- **Consulta mesas**: 500ms → 50ms
- **Lista productos**: 800ms → 80ms
- **Pedidos activos**: 300ms → 30ms
- **Dashboard stats**: 2s → 200ms

---

## 🛠️ Uso Práctico

### 1. **Aplicar Índices MongoDB**
```bash
# Conectar a MongoDB y ejecutar:
mongo your-database-name < mongodb-indexes-optimization.js
```

### 2. **Gestión de Cache via API**
```bash
# Precargar caches importantes
POST /api/cache/preload

# Limpiar cache específico
DELETE /api/cache/productos
DELETE /api/cache/mesas

# Limpiar todos los caches
DELETE /api/cache/all

# Ver estadísticas
GET /api/cache/stats

# Información del cache
GET /api/cache/info
```

### 3. **Integración en Controladores**
```java
@Autowired
private CacheOptimizationService cacheService;

// Usar métodos cacheados
List<Producto> productos = cacheService.getAllProductosCached();
List<Mesa> mesas = cacheService.getAllMesasCached();
```

---

## ⏰ Rutinas de Mantenimiento

### 🌅 Al Inicio del Día:
1. **Precargar caches importantes**
   ```bash
   curl -X POST http://localhost:8081/api/cache/preload
   ```
2. **Verificar índices MongoDB activos**
   ```javascript
   db.pedidos.getIndexes()
   ```

### 🌙 Al Final del Día:
1. **Limpiar caches** (opcional, para datos frescos al día siguiente)
2. **Revisar logs** de consultas lentas
3. **Verificar estadísticas** de uso de cache

### 📅 Semanalmente:
1. **Monitorear rendimiento** de consultas
2. **Revisar uso de memoria** del cache
3. **Analizar patrones** de acceso a datos
4. **Limpiar datos antiguos** si es necesario

---

## 🔍 Monitoreo y Debugging

### 📊 Indicadores de Rendimiento:
```javascript
// Ver consultas lentas en MongoDB
db.setProfilingLevel(2, { slowms: 100 })
db.getProfilingStatus()

// Estadísticas de índices
db.pedidos.aggregate([{$indexStats:{}}])

// Explicar plan de consulta
db.pedidos.find({"mesa": "C1", "estado": "activo"}).explain("executionStats")
```

### 🖥️ Logs de Cache:
```bash
# Buscar en logs del backend:
grep "🔄.*desde BD" logs/application.log  # Cache miss
grep "🗑️.*cache" logs/application.log     # Cache invalidation
```

---

## 🚨 Troubleshooting

### ❌ Cache No Funciona:
1. Verificar que `@EnableCaching` esté activo
2. Comprobar configuración en `CacheConfig.java`
3. Revisar logs para mensajes de error
4. Reiniciar aplicación si es necesario

### ❌ Consultas Siguen Lentas:
1. Verificar que índices se crearon correctamente
2. Usar `.explain()` para analizar planes de ejecución
3. Considerar índices adicionales para consultas específicas
4. Revisar si cache se está usando (logs)

### ❌ Memoria Alta por Cache:
1. Reducir TTL de caches menos críticos
2. Implementar límites de tamaño si es necesario
3. Limpiar caches manualmente más frecuentemente
4. Considerar migrar a Redis si el volumen crece mucho

---

## 📋 Checklist de Implementación

### ✅ Base de Datos:
- [ ] Ejecutar script de índices MongoDB
- [ ] Verificar índices creados correctamente
- [ ] Probar consultas con `.explain()`
- [ ] Configurar profiling para consultas lentas

### ✅ Cache:
- [ ] Configuración de cache activa (`@EnableCaching`)
- [ ] Servicio de cache implementado
- [ ] Controlador de gestión de cache
- [ ] Precarga automática configurada

### ✅ Monitoreo:
- [ ] Logs de cache configurados
- [ ] API endpoints de gestión funcionando
- [ ] Métricas de rendimiento establecidas
- [ ] Rutinas de mantenimiento definidas

---

## 🎯 Siguientes Pasos

### 🔮 Futuras Mejoras:
1. **Migración a Redis** (si el volumen de datos crece)
2. **Cache distribuido** (para múltiples instancias)
3. **Métricas avanzadas** (Micrometer + Prometheus)
4. **Cache warming automático** (scheduled tasks)
5. **Compression de datos** cacheados
6. **TTL dinámico** basado en patrones de uso

### 📊 Métricas Avanzadas:
- Hit rate del cache por endpoint
- Tiempo promedio de consultas por colección  
- Uso de memoria por tipo de cache
- Patrones de invalidación de cache

---

## 👥 Equipo y Responsabilidades

### 🛠️ Desarrollador Backend:
- Implementar y mantener índices
- Gestionar configuración de cache
- Monitorear rendimiento
- Troubleshooting de consultas lentas

### 🖥️ Administrador Sistema:
- Configurar MongoDB profiling
- Monitorear uso de recursos
- Backup de configuraciones
- Alertas de rendimiento

### 📊 Analista Performance:
- Revisar métricas semanalmente
- Identificar oportunidades de optimización
- Reportar tendencias de rendimiento
- Proponer mejoras

---

## 📞 Soporte

### 🔧 Comandos Útiles:
```bash
# Ver estado de cache
curl http://localhost:8081/api/cache/info

# Estadísticas del sistema
curl http://localhost:8081/api/cache/stats

# Limpiar cache en emergencia
curl -X DELETE http://localhost:8081/api/cache/all

# Ver logs en tiempo real
tail -f logs/application.log | grep cache
```

### 🆘 En Caso de Problemas:
1. **Restart del cache**: DELETE /api/cache/all
2. **Restart de la aplicación**: Último recurso
3. **Verificar MongoDB**: Estado de índices y conexión
4. **Revisar logs**: Buscar errores específicos

---

**🎉 ¡Sistema optimizado para máximo rendimiento!**

*Última actualización: Septiembre 2025*
*Versión: 2.0 - Optimización Completa*
