// ========================================
// SCRIPT PARA CREAR ÍNDICES EN MONGODB
// ========================================
// Ejecutar con: mongosh mongodb://localhost:27017/sopa_carbon mongodb-create-indexes.js
// O desde MongoDB Compass: Copiar y pegar en la consola

print("🚀 Iniciando creación de índices para optimización de búsquedas...\n");

// Seleccionar la base de datos (cambiar si es necesario)
db = db.getSiblingDB("sopa_carbon");

// ========================================
// ÍNDICES PARA PRODUCTOS
// ========================================
print("📦 Creando índices para colección 'producto'...");

// 1. Índice en campo 'estado' (ACTIVO/INACTIVO)
// Beneficia: /api/productos/search, /api/productos/paginados
db.producto.createIndex(
  { estado: 1 },
  {
    name: "idx_producto_estado",
    background: true,
  }
);
print("✅ Índice 'idx_producto_estado' creado");

// 2. Índice compuesto: estado + categoriaId
// Beneficia: /api/productos/categoria/{id}/ligero
db.producto.createIndex(
  { estado: 1, categoriaId: 1 },
  {
    name: "idx_producto_estado_categoria",
    background: true,
  }
);
print("✅ Índice 'idx_producto_estado_categoria' creado");

// 3. Índice en nombre (para búsquedas futuras)
db.producto.createIndex(
  { nombre: 1 },
  {
    name: "idx_producto_nombre",
    background: true,
  }
);
print("✅ Índice 'idx_producto_nombre' creado");

// 4. Índice text para búsquedas full-text (opcional)
db.producto.createIndex(
  {
    nombre: "text",
    descripcion: "text",
  },
  {
    name: "idx_producto_text_search",
    background: true,
    default_language: "spanish",
  }
);
print("✅ Índice 'idx_producto_text_search' creado");

// ========================================
// ÍNDICES PARA INGREDIENTES
// ========================================
print("\n🥗 Creando índices para colección 'ingrediente'...");

// 1. Índice en categoriaId
db.ingrediente.createIndex(
  { categoriaId: 1 },
  {
    name: "idx_ingrediente_categoria",
    background: true,
  }
);
print("✅ Índice 'idx_ingrediente_categoria' creado");

// 2. Índice en nombre
db.ingrediente.createIndex(
  { nombre: 1 },
  {
    name: "idx_ingrediente_nombre",
    background: true,
  }
);
print("✅ Índice 'idx_ingrediente_nombre' creado");

// 3. Índice en stock bajo (para alertas)
db.ingrediente.createIndex(
  { stockActual: 1, stockMinimo: 1 },
  {
    name: "idx_ingrediente_stock",
    background: true,
  }
);
print("✅ Índice 'idx_ingrediente_stock' creado");

// ========================================
// ÍNDICES PARA PEDIDOS
// ========================================
print("\n📝 Creando índices para colección 'pedido'...");

// 1. Índice en mesaId
db.pedido.createIndex(
  { mesaId: 1 },
  {
    name: "idx_pedido_mesa",
    background: true,
  }
);
print("✅ Índice 'idx_pedido_mesa' creado");

// 2. Índice en estado + fecha
db.pedido.createIndex(
  { estado: 1, fechaCreacion: -1 },
  {
    name: "idx_pedido_estado_fecha",
    background: true,
  }
);
print("✅ Índice 'idx_pedido_estado_fecha' creado");

// ========================================
// VERIFICAR ÍNDICES CREADOS
// ========================================
print("\n\n📊 RESUMEN DE ÍNDICES CREADOS:\n");
print("===========================================");

print("\n🔹 PRODUCTOS:");
db.producto.getIndexes().forEach((idx) => {
  print(`   - ${idx.name}: ${JSON.stringify(idx.key)}`);
});

print("\n🔹 INGREDIENTES:");
db.ingrediente.getIndexes().forEach((idx) => {
  print(`   - ${idx.name}: ${JSON.stringify(idx.key)}`);
});

print("\n🔹 PEDIDOS:");
db.pedido.getIndexes().forEach((idx) => {
  print(`   - ${idx.name}: ${JSON.stringify(idx.key)}`);
});

print("\n===========================================");
print("✅ Script completado exitosamente");
print("\n💡 BENEFICIOS:");
print("   • Búsquedas 10-100x más rápidas");
print("   • Menos carga en memoria");
print("   • Mejor escalabilidad");
print("\n🚀 ENDPOINTS OPTIMIZADOS:");
print("   • GET /api/productos/search");
print("   • GET /api/productos/paginados");
print("   • GET /api/productos/categoria/{id}/ligero");
print("   • GET /api/ingredientes");
print("   • GET /api/pedidos/*");
