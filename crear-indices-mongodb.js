// ========================================
// ÍNDICES MONGODB PARA MÁXIMA VELOCIDAD
// ========================================
// Ejecutar en MongoDB Compass o mongo shell:
// mongosh "mongodb+srv://..." crear-indices-mongodb.js

use("db_security");

print("🔍 Verificando índices existentes...");
printjson(db.producto.getIndexes());

// Eliminar índices antiguos si existen (excepto _id_)
print("\n🗑️ Limpiando índices antiguos...");
const indexesToDrop = [
  "estado_1",
  "idx_estado",
  "categoriaId_1",
  "estado_1_categoriaId_1",
  "idx_estado_categoria",
  "nombre_text",
  "idx_nombre",
  "idx_precio",
];

indexesToDrop.forEach((indexName) => {
  try {
    db.producto.dropIndex(indexName);
    print(`✅ Eliminado: ${indexName}`);
  } catch (e) {
    print(`ℹ️ ${indexName} no existe`);
  }
});

print("\n📝 Creando nuevos índices optimizados...");

// 1. Índice para buscar productos por estado (CRÍTICO)
db.producto.createIndex(
  { estado: 1 },
  {
    name: "idx_estado",
    background: true,
  }
);
print("✅ Creado: idx_estado");

// 2. Índice compuesto para estado + categoría (muy común)
db.producto.createIndex(
  { estado: 1, categoriaId: 1 },
  {
    name: "idx_estado_categoria",
    background: true,
  }
);
print("✅ Creado: idx_estado_categoria");

// 3. Índice para búsqueda por nombre
db.producto.createIndex(
  { nombre: 1 },
  {
    name: "idx_nombre",
    background: true,
  }
);
print("✅ Creado: idx_nombre");

// 4. Índice para ordenar por precio
db.producto.createIndex(
  { precio: 1 },
  {
    name: "idx_precio",
    background: true,
  }
);
print("✅ Creado: idx_precio");

// ========================================
// ÍNDICES PARA OTRAS COLECCIONES
// ========================================

// Ingredientes por categoría
db.ingrediente.createIndex(
  { categoriaId: 1 },
  {
    name: "idx_ingrediente_categoria",
    background: true,
  }
);
print("✅ Creado: idx_ingrediente_categoria");

// Mesas por nombre
try {
  db.mesa.createIndex(
    { nombre: 1 },
    {
      name: "idx_mesa_nombre",
      unique: true,
      background: true,
    }
  );
  print("✅ Creado: idx_mesa_nombre");
} catch (e) {
  print("ℹ️ idx_mesa_nombre ya existe (es único)");
}

print("\n🎉 ¡Índices creados exitosamente!");
print("\n📊 Verificando índices de productos:");
printjson(db.producto.getIndexes());

print("\n📊 Índices de ingredientes:");
printjson(db.ingrediente.getIndexes());

print("\n📊 Índices de mesas:");
printjson(db.mesa.getIndexes());
