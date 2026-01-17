# Sistema de Códigos de Barras - Guía de Implementación

## Descripción General

El sistema de códigos de barras permite generar, gestionar y buscar productos e ingredientes mediante códigos de barras estándar. Soporta múltiples formatos y permite impresión de etiquetas.

## Características Principales

### ✅ Formatos de Códigos Soportados
- **EAN-13**: Código estándar internacional (13 dígitos)
- **EAN-8**: Código para productos pequeños (8 dígitos)
- **CODE128**: Alfanumérico de alta densidad
- **QR**: Código QR para mayor información

### ✅ Funcionalidades
1. Generación automática de códigos de barras
2. Códigos personalizados opcionales
3. Uso del ID de MongoDB como código por defecto
4. Búsqueda por código de barras o código interno
5. Generación de etiquetas con imagen PNG
6. Impresión en lote de múltiples etiquetas
7. Campos adicionales: `codigoBarras` y `codigoInterno`

## Estructura de la Base de Datos

### Modelo Producto
```java
private String codigoBarras;    // Código de barras generado/asignado
private String codigoInterno;   // Código interno del negocio
```

### Modelo Ingrediente
```java
private String codigoBarras;    // Código de barras generado/asignado
private String codigoInterno;   // Código interno del negocio
```

## Endpoints API

### 1. Generar Código de Barras
**POST** `/api/codigos-barras/generar`

Genera un código de barras para un producto o ingrediente.

**Request Body:**
```json
{
  "itemId": "507f1f77bcf86cd799439011",
  "tipoItem": "producto",
  "codigoPersonalizado": "1234567890123",
  "tipoCodigo": "EAN13"
}
```

**Parámetros:**
- `itemId` (String, requerido): ID del producto o ingrediente
- `tipoItem` (String, requerido): "producto" o "ingrediente"
- `codigoPersonalizado` (String, opcional): Código personalizado. Si no se proporciona, usa el ID
- `tipoCodigo` (Enum, requerido): EAN13, EAN8, CODE128, QR

**Response:**
```json
{
  "success": true,
  "codigo": "1234567890128",
  "mensaje": "Código de barras generado exitosamente"
}
```

### 2. Obtener Imagen de Código de Barras
**GET** `/api/codigos-barras/imagen/{codigo}?tipo=CODE128`

Genera y retorna la imagen PNG del código de barras.

**Parámetros:**
- `codigo` (Path, requerido): El código a convertir en imagen
- `tipo` (Query, opcional): Tipo de código (default: CODE128)

**Response:** Imagen PNG (binary)

### 3. Generar Etiqueta Completa
**GET** `/api/codigos-barras/etiqueta/{itemId}/{tipoItem}?tipoCodigo=CODE128`

Genera una etiqueta completa con código de barras, nombre y precio.

**Parámetros:**
- `itemId` (Path, requerido): ID del item
- `tipoItem` (Path, requerido): "producto" o "ingrediente"
- `tipoCodigo` (Query, opcional): Tipo de código (default: CODE128)

**Response:**
```json
{
  "itemId": "507f1f77bcf86cd799439011",
  "codigo": "1234567890128",
  "nombre": "Hamburguesa Clásica",
  "precio": 15000.0,
  "imagenCodigoBarras": "base64_encoded_image...",
  "formatoImagen": "PNG"
}
```

### 4. Imprimir Etiquetas en Lote
**POST** `/api/codigos-barras/imprimir-etiquetas`

Genera múltiples etiquetas para impresión.

**Request Body:**
```json
{
  "items": [
    {
      "itemId": "507f1f77bcf86cd799439011",
      "tipoItem": "producto",
      "cantidad": 5
    },
    {
      "itemId": "507f1f77bcf86cd799439012",
      "tipoItem": "ingrediente",
      "cantidad": 3
    }
  ],
  "configuracion": {
    "tamano": "50x30",
    "incluirPrecio": true,
    "incluirDescripcion": true,
    "incluirLogo": false
  }
}
```

**Response:** Array de etiquetas con imágenes

### 5. Buscar por Código de Barras
**GET** `/api/codigos-barras/buscar/{codigo}`

Busca un producto o ingrediente por su código de barras o código interno.

**Response:**
```json
{
  "success": true,
  "item": {
    "_id": "507f1f77bcf86cd799439011",
    "nombre": "Hamburguesa Clásica",
    "precio": 15000.0,
    "codigoBarras": "1234567890128",
    "codigoInterno": "HAM-001",
    ...
  }
}
```

### 6. Tipos de Códigos Soportados
**GET** `/api/codigos-barras/tipos`

Lista todos los tipos de códigos que el sistema puede generar.

**Response:**
```json
{
  "success": true,
  "tipos": {
    "EAN13": "Código EAN-13 (13 dígitos) - Estándar internacional para productos",
    "EAN8": "Código EAN-8 (8 dígitos) - Para productos pequeños",
    "CODE128": "Code 128 - Alfanumérico, alta densidad",
    "QR": "Código QR - Almacena más información, escaneable con móviles"
  },
  "recomendacion": "CODE128 para uso general, EAN13 para retail"
}
```

## Casos de Uso

### Caso 1: Generar Códigos para Productos Existentes

1. **Escenario**: Tienes productos sin código de barras y quieres generarlos automáticamente.

```bash
# Generar código usando el ID del producto
POST /api/codigos-barras/generar
{
  "itemId": "6785d3f8e1b2c4a5d6789abc",
  "tipoItem": "producto",
  "tipoCodigo": "CODE128"
}

# El sistema usará el ID como base y generará el código
# Se guardará automáticamente en el campo codigoBarras
```

### Caso 2: Asignar Código Personalizado

1. **Escenario**: Ya tienes códigos de barras físicos y quieres registrarlos en el sistema.

```bash
# Asignar código específico
POST /api/codigos-barras/generar
{
  "itemId": "6785d3f8e1b2c4a5d6789abc",
  "tipoItem": "producto",
  "codigoPersonalizado": "7501234567890",
  "tipoCodigo": "EAN13"
}
```

### Caso 3: Imprimir Etiquetas para Inventario

1. **Escenario**: Necesitas imprimir etiquetas para varios productos nuevos.

```bash
# Solicitar etiquetas en lote
POST /api/codigos-barras/imprimir-etiquetas
{
  "items": [
    {"itemId": "prod1", "tipoItem": "producto", "cantidad": 10},
    {"itemId": "prod2", "tipoItem": "producto", "cantidad": 5},
    {"itemId": "ing1", "tipoItem": "ingrediente", "cantidad": 3}
  ],
  "configuracion": {
    "tamano": "50x30",
    "incluirPrecio": true,
    "incluirDescripcion": true
  }
}

# Respuesta: Array de etiquetas con imágenes PNG en Base64
# Puedes procesar estas imágenes e imprimirlas
```

### Caso 4: Búsqueda Rápida en Punto de Venta

1. **Escenario**: Escanear código de barras para agregar producto a un pedido.

```bash
# Buscar producto escaneado
GET /api/codigos-barras/buscar/1234567890128

# Respuesta incluye toda la info del producto
# Puedes agregarlo directamente al carrito/pedido
```

### Caso 5: Validación en Recepción de Mercancía

1. **Escenario**: Validar ingredientes al recibir una compra del proveedor.

```bash
# Escanear código del producto recibido
GET /api/codigos-barras/buscar/7501234567890

# Verificar que coincide con el ingrediente esperado
# Actualizar inventario automáticamente
```

## Integración con Frontend

### Ejemplo en JavaScript/Flutter

```javascript
// Generar código de barras
async function generarCodigo(productoId) {
  const response = await fetch('/api/codigos-barras/generar', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      itemId: productoId,
      tipoItem: 'producto',
      tipoCodigo: 'CODE128'
    })
  });
  
  const data = await response.json();
  console.log('Código generado:', data.codigo);
}

// Buscar por código escaneado
async function buscarPorCodigo(codigoEscaneado) {
  const response = await fetch(`/api/codigos-barras/buscar/${codigoEscaneado}`);
  const data = await response.json();
  
  if (data.success) {
    console.log('Producto encontrado:', data.item);
    return data.item;
  } else {
    console.error('Producto no encontrado');
    return null;
  }
}

// Obtener imagen de código de barras
function obtenerImagenCodigo(codigo) {
  return `/api/codigos-barras/imagen/${codigo}?tipo=CODE128`;
}

// Uso en HTML
// <img src="/api/codigos-barras/imagen/1234567890128?tipo=CODE128" alt="Código de barras">
```

## Dependencias

El sistema utiliza la librería ZXing (Zebra Crossing) para la generación de códigos:

```xml
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
    <version>3.5.2</version>
</dependency>
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>javase</artifactId>
    <version>3.5.2</version>
</dependency>
```

## Arquitectura

```
┌─────────────────────────────────────┐
│   CodigoBarrasController.java       │  ← Endpoints REST
└──────────────┬──────────────────────┘
               │
               ↓
┌─────────────────────────────────────┐
│   CodigoBarrasService.java          │  ← Lógica de negocio
│   - generarCodigoBarras()           │
│   - generarImagenCodigoBarras()     │
│   - buscarPorCodigoBarras()         │
│   - generarEtiqueta()               │
└──────────────┬──────────────────────┘
               │
               ↓
┌─────────────────────────────────────┐
│   ProductoRepository                │  ← Persistencia
│   IngredienteRepository             │
│   - findByCodigoBarras()            │
│   - findByCodigoInterno()           │
└─────────────────────────────────────┘
```

## DTOs

### GenerarCodigoBarrasRequest
```java
{
  String itemId;
  String tipoItem;
  String codigoPersonalizado;
  TipoCodigoBarras tipoCodigo; // EAN13, EAN8, CODE128, QR
}
```

### EtiquetaCodigoBarrasDTO
```java
{
  String itemId;
  String codigo;
  String nombre;
  double precio;
  byte[] imagenCodigoBarras;
  String formatoImagen;
}
```

### ImprimirEtiquetasRequest
```java
{
  List<ItemEtiqueta> items;
  ConfiguracionEtiqueta configuracion;
}
```

## Validaciones

1. **EAN-13**: Debe tener exactamente 13 dígitos con dígito de control
2. **EAN-8**: Debe tener exactamente 8 dígitos con dígito de control
3. **CODE128**: Acepta alfanuméricos sin restricción
4. **QR**: Acepta cualquier formato

El servicio calcula automáticamente los dígitos de control para EAN-13 y EAN-8.

## Mejores Prácticas

1. **Usar CODE128 por defecto**: Es el más versátil y acepta cualquier formato
2. **Guardar código interno**: Usa `codigoInterno` para tu sistema de referencia interno
3. **Imprimir en lote**: Más eficiente que generar etiquetas una por una
4. **Caché de imágenes**: Las imágenes se generan bajo demanda, considera cachearlas en producción
5. **Validación de duplicados**: Asegúrate de que los códigos personalizados sean únicos

## Troubleshooting

### Error: "Código EAN-13 debe tener 13 dígitos"
- Asegúrate de proporcionar un código de 13 dígitos para EAN-13
- El sistema ajustará automáticamente códigos más cortos

### Error: "No se encontró el item"
- Verifica que el `itemId` exista en la base de datos
- Confirma que el `tipoItem` sea "producto" o "ingrediente"

### Búsqueda no encuentra el código
- Verifica que el código esté guardado en `codigoBarras` o `codigoInterno`
- Los códigos son sensibles a mayúsculas/minúsculas
- Asegúrate de haber generado el código primero con `/generar`

## Próximas Mejoras

- [ ] Soporte para códigos UPC
- [ ] Generación de etiquetas con múltiples diseños
- [ ] Integración con impresoras térmicas
- [ ] Validación de códigos duplicados
- [ ] Historial de códigos generados
- [ ] Exportación de etiquetas en PDF
- [ ] Soporte para códigos Data Matrix

## Conclusión

El sistema de códigos de barras está completamente funcional y listo para usar. Puedes empezar generando códigos para tus productos existentes y luego integrar la búsqueda por código de barras en tu punto de venta.
