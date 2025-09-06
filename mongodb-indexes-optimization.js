// ==================================================
// CONFIGURACIÓN DE ÍNDICES MONGODB PARA OPTIMIZACIÓN
// Sistema de Restaurante "Sopa y Carbón"
// ==================================================

// Conéctate a la base de datos
use('security');  // Cambiar por el nombre de tu base de datos

print("🚀 Iniciando optimización de índices MongoDB...");

// ==================================================
// ÍNDICES PARA COLECCIÓN PEDIDOS (Más crítica)
// ==================================================
print("📋 Optimizando colección 'pedidos'...");

// 1. Índice compuesto para consultas por mesa (muy frecuente)
db.pedidos.createIndex(
    { "mesa": 1, "estado": 1, "fecha": -1 }, 
    { 
        name: "idx_mesa_estado_fecha",
        background: true,
        partialFilterExpression: { "mesa": { $exists: true, $ne: null } }
    }
);

// 2. Índice para consultas por estado y fecha (para obtener pedidos activos/pagados)
db.pedidos.createIndex(
    { "estado": 1, "fecha": -1 }, 
    { 
        name: "idx_estado_fecha",
        background: true
    }
);

// 3. Índice para cuadres de caja (consulta muy frecuente)
db.pedidos.createIndex(
    { "cuadreCajaId": 1, "estado": 1 }, 
    { 
        name: "idx_cuadre_estado",
        background: true,
        partialFilterExpression: { "cuadreCajaId": { $exists: true, $ne: null } }
    }
);

// 4. Índice para pedidos pagados por fecha de pago (para reportes)
db.pedidos.createIndex(
    { "fechaPago": -1, "formaPago": 1, "estado": 1 }, 
    { 
        name: "idx_fechapago_formapago_estado",
        background: true,
        partialFilterExpression: { 
            "fechaPago": { $exists: true, $ne: null },
            "estado": "pagado"
        }
    }
);

// 5. Índice para consultas por mesero y fecha
db.pedidos.createIndex(
    { "mesero": 1, "fecha": -1, "estado": 1 }, 
    { 
        name: "idx_mesero_fecha_estado",
        background: true,
        partialFilterExpression: { "mesero": { $exists: true, $ne: null } }
    }
);

// 6. Índice para consultas por tipo y plataforma
db.pedidos.createIndex(
    { "tipo": 1, "plataforma": 1, "fecha": -1 }, 
    { 
        name: "idx_tipo_plataforma_fecha",
        background: true
    }
);

// 7. Índice para pedidos sin cuadre de caja (para asignación automática)
db.pedidos.createIndex(
    { "cuadreCajaId": 1, "estado": 1, "fechaPago": -1 }, 
    { 
        name: "idx_cuadre_null_estado_fechapago",
        background: true,
        sparse: true
    }
);

// ==================================================
// ÍNDICES PARA COLECCIÓN CUADRECAJA
// ==================================================
print("💰 Optimizando colección 'cuadreCaja'...");

// 1. Índice para cajas abiertas (consulta muy frecuente)
db.cuadreCaja.createIndex(
    { "cerrada": 1, "fechaApertura": -1 }, 
    { 
        name: "idx_cerrada_fechaapertura",
        background: true
    }
);

// 2. Índice para consultas por responsable y fecha
db.cuadreCaja.createIndex(
    { "responsable": 1, "fechaApertura": -1, "cerrada": 1 }, 
    { 
        name: "idx_responsable_fechaapertura_cerrada",
        background: true
    }
);

// 3. Índice para cuadres con diferencias
db.cuadreCaja.createIndex(
    { "cuadrado": 1, "diferencia": 1, "fechaApertura": -1 }, 
    { 
        name: "idx_cuadrado_diferencia_fecha",
        background: true
    }
);

// ==================================================
// ÍNDICES PARA COLECCIÓN PRODUCTOS
// ==================================================
print("🍽️ Optimizando colección 'productos'...");

// 1. Índice para búsquedas por nombre (case insensitive)
db.productos.createIndex(
    { "nombre": "text", "estado": 1 }, 
    { 
        name: "idx_nombre_text_estado",
        background: true,
        default_language: "spanish"
    }
);

// 2. Índice para consultas por categoría y estado
db.productos.createIndex(
    { "categoriaId": 1, "estado": 1, "precio": 1 }, 
    { 
        name: "idx_categoria_estado_precio",
        background: true
    }
);

// 3. Índice para productos con variantes
db.productos.createIndex(
    { "tieneVariantes": 1, "estado": 1 }, 
    { 
        name: "idx_variantes_estado",
        background: true
    }
);

// ==================================================
// ÍNDICES PARA COLECCIÓN MESAS
// ==================================================
print("🪑 Optimizando colección 'mesas'...");

// 1. Índice único para nombre de mesa
db.mesas.createIndex(
    { "nombre": 1 }, 
    { 
        name: "idx_nombre_unique",
        unique: true,
        background: true
    }
);

// 2. Índice para estado de ocupación
db.mesas.createIndex(
    { "ocupada": 1, "total": -1 }, 
    { 
        name: "idx_ocupada_total",
        background: true
    }
);

// ==================================================
// ÍNDICES PARA COLECCIÓN INVENTARIO
// ==================================================
print("📦 Optimizando colección 'inventario'...");

// 1. Índice para producto e ingrediente
db.inventario.createIndex(
    { "productoId": 1, "ingredienteId": 1 }, 
    { 
        name: "idx_producto_ingrediente",
        background: true
    }
);

// 2. Índice para stock bajo
db.inventario.createIndex(
    { "stockActual": 1, "stockMinimo": 1, "activo": 1 }, 
    { 
        name: "idx_stock_actual_minimo_activo",
        background: true
    }
);

// ==================================================
// ÍNDICES PARA COLECCIÓN GASTOS (Si existe)
// ==================================================
print("💸 Optimizando colección 'gastos'...");

// 1. Índice para fecha y tipo de gasto
db.gastos.createIndex(
    { "fecha": -1, "tipoGasto": 1, "monto": -1 }, 
    { 
        name: "idx_fecha_tipogasto_monto",
        background: true
    }
);

// ==================================================
// ÍNDICES PARA COLECCIÓN MOVIMIENTOINVENTARIO
// ==================================================
print("📊 Optimizando colección 'movimientoInventario'...");

// 1. Índice para tipo de movimiento y fecha
db.movimientoInventario.createIndex(
    { "tipoMovimiento": 1, "fecha": -1, "productoId": 1 }, 
    { 
        name: "idx_tipomovimiento_fecha_producto",
        background: true
    }
);

// ==================================================
// VERIFICACIÓN DE ÍNDICES CREADOS
// ==================================================
print("\n🔍 Verificando índices creados...");

// Mostrar índices de pedidos
print("\n📋 Índices en colección 'pedidos':");
db.pedidos.getIndexes().forEach(function(index) {
    print("  - " + index.name + ": " + JSON.stringify(index.key));
});

// Mostrar índices de cuadreCaja
print("\n💰 Índices en colección 'cuadreCaja':");
db.cuadreCaja.getIndexes().forEach(function(index) {
    print("  - " + index.name + ": " + JSON.stringify(index.key));
});

// Mostrar índices de productos
print("\n🍽️ Índices en colección 'productos':");
db.productos.getIndexes().forEach(function(index) {
    print("  - " + index.name + ": " + JSON.stringify(index.key));
});

// Mostrar índices de mesas
print("\n🪑 Índices en colección 'mesas':");
db.mesas.getIndexes().forEach(function(index) {
    print("  - " + index.name + ": " + JSON.stringify(index.key));
});

// ==================================================
// ESTADÍSTICAS Y RECOMENDACIONES
// ==================================================
print("\n📈 Estadísticas de colecciones:");
print("Pedidos: " + db.pedidos.countDocuments() + " documentos");
print("Cuadres: " + db.cuadreCaja.countDocuments() + " documentos");
print("Productos: " + db.productos.countDocuments() + " documentos");
print("Mesas: " + db.mesas.countDocuments() + " documentos");

print("\n✅ ¡Optimización de índices completada!");
print("\n📋 RECOMENDACIONES ADICIONALES:");
print("1. Ejecutar este script durante horarios de bajo tráfico");
print("2. Monitorear el rendimiento con db.currentOp() y explain()");
print("3. Considerar implementar cache para consultas muy frecuentes");
print("4. Revisar y limpiar datos antiguos periódicamente");
print("5. Configurar alertas para consultas lentas (> 100ms)");

print("\n🎯 ÍNDICES CRÍTICOS CREADOS:");
print("• Mesa + Estado + Fecha (pedidos por mesa)");
print("• Cuadre + Estado (asignación de pedidos a cajas)");
print("• Fecha de Pago + Forma de Pago (reportes de ventas)");
print("• Estado + Fecha (pedidos activos/históricos)");
print("• Cajas abiertas (cerrada=false)");
