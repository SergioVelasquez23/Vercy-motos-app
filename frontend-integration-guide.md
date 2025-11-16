# 📱 Guía de Integración Frontend Flutter - Sistema de Totales Dinámicos

## 🎯 **Funcionalidades del Sistema**

### **1. Cálculo Dinámico de Totales de Mesa**
- ✅ Los totales se **recalculan automáticamente** cuando hay cambios en los productos
- ✅ Se aplican **descuentos** marcados desde el frontend
- ✅ Los totales reflejan los productos **actuales** en la mesa, no los iniciales
- ✅ **Propinas** se pueden incluir opcionalmente al momento del pago

### **2. Escenarios de Actualización Automática**
- ✅ **Cancelar productos** → Total de mesa se actualiza
- ✅ **Mover productos entre mesas** → Ambas mesas actualizan totales
- ✅ **Modificar pedido** → Cambios de cantidades, precios, descuentos
- ✅ **Pago parcial** → Total restante se recalcula con descuentos

---

## 🔧 **Estructura de Peticiones del Frontend**

### **📊 Modelo de Datos - Pedido**

```dart
class Pedido {
  String? id;
  String mesa;
  String? cliente;
  String? mesero;
  List<ItemPedido> items;
  double total;              // ← Calculado automáticamente por backend
  double descuento;          // ← Controlado por frontend
  bool incluyePropina;       // ← Controlado por frontend
  double propina;            // ← Valor de propina (si aplica)
  String? notas;
  String estado;
  // ... otros campos
}
```

### **📦 Modelo de Datos - Item de Pedido**

```dart
class ItemPedido {
  String productoId;
  String productoNombre;
  int cantidad;
  double precioUnitario;
  double get subtotal => cantidad * precioUnitario; // Calculado dinámicamente
  List<String>? ingredientesSeleccionados;
  String? notas;
  String? agregadoPor;
}
```

---

## 🌐 **Peticiones HTTP - Ejemplos Completos**

### **1. 📝 Actualizar Pedido (Cambios Generales)**

```dart
// PUT /api/pedidos/{id}
Future<ApiResponse> actualizarPedido(Pedido pedido) async {
  final body = {
    "mesa": pedido.mesa,
    "cliente": pedido.cliente,
    "mesero": pedido.mesero,
    "items": pedido.items.map((item) => {
      "productoId": item.productoId,
      "productoNombre": item.productoNombre,
      "cantidad": item.cantidad,
      "precioUnitario": item.precioUnitario,
      "ingredientesSeleccionados": item.ingredientesSeleccionados ?? [],
      "notas": item.notas,
      "agregadoPor": item.agregadoPor ?? "sistema"
    }).toList(),
    
    // 💰 CAMPOS IMPORTANTES PARA DESCUENTOS
    "descuento": pedido.descuento,           // ← Frontend controla descuentos
    "incluyePropina": pedido.incluyePropina, // ← Si incluye propina en el total
    
    "notas": pedido.notas,
    "estado": pedido.estado
  };

  final response = await http.put(
    Uri.parse('$baseUrl/api/pedidos/${pedido.id}'),
    headers: {'Content-Type': 'application/json'},
    body: json.encode(body),
  );

  // Backend devuelve pedido con total recalculado automáticamente
  return ApiResponse.fromJson(json.decode(response.body));
}
```

### **2. 🗑️ Cancelar Producto**

```dart
// POST /api/pedidos/cancelar-producto
Future<ApiResponse> cancelarProducto({
  required String pedidoId,
  required String productoId,
  required int cantidadACancelar,
  required String motivoCancelacion,
  required String canceladoPor,
  String? notas,
  List<IngredienteADevolver>? ingredientesADevolver,
}) async {
  final body = {
    "pedidoId": pedidoId,
    "productoId": productoId,
    "cantidadACancelar": cantidadACancelar,
    "motivoCancelacion": motivoCancelacion,
    "canceladoPor": canceladoPor,
    "notas": notas,
    "ingredientesADevolver": ingredientesADevolver?.map((ing) => {
      "ingredienteId": ing.ingredienteId,
      "nombreIngrediente": ing.nombreIngrediente,
      "cantidadOriginal": ing.cantidadOriginal,
      "cantidadADevolver": ing.cantidadADevolver,
      "unidad": ing.unidad,
      "devolver": ing.devolver,
      "motivoNoDevolucion": ing.motivoNoDevolucion,
    }).toList() ?? [],
  };

  final response = await http.post(
    Uri.parse('$baseUrl/api/pedidos/cancelar-producto'),
    headers: {'Content-Type': 'application/json'},
    body: json.encode(body),
  );

  // Backend recalcula total automáticamente aplicando descuentos
  return ApiResponse.fromJson(json.decode(response.body));
}
```

### **3. 🔄 Mover Productos Entre Mesas**

```dart
// POST /api/pedidos/mover-productos-especificos
Future<ApiResponse> moverProductosEspecificos({
  required String pedidoId,
  required String mesaDestino,
  required List<ProductoAMover> productos,
}) async {
  final body = {
    "pedidoId": pedidoId,
    "mesaDestino": mesaDestino,
    "productos": productos.map((producto) => {
      "productoId": producto.productoId,
      "cantidad": producto.cantidad,
    }).toList(),
  };

  final response = await http.post(
    Uri.parse('$baseUrl/api/pedidos/mover-productos-especificos'),
    headers: {'Content-Type': 'application/json'},
    body: json.encode(body),
  );

  // Backend recalcula totales de ambas mesas automáticamente
  return ApiResponse.fromJson(json.decode(response.body));
}
```

### **4. 💰 Pagar Pedido (Con Propinas)**

```dart
// PUT /api/pedidos/{id}/pagar
Future<ApiResponse> pagarPedido({
  required String pedidoId,
  required String tipo, // "pagado", "cortesia", "interno", "cancelado"
  String? formaPago,
  double propina = 0.0,
  required String procesadoPor,
  String? notas,
  String? motivoCortesia,
  List<PagoMixto>? pagosMixtos,
}) async {
  final body = {
    "tipo": tipo,
    "formaPago": formaPago,
    "propina": propina,              // ← Propina adicional al total
    "procesadoPor": procesadoPor,
    "notas": notas,
    "motivoCortesia": motivoCortesia,
    "pagosMixtos": pagosMixtos?.map((pago) => {
      "formaPago": pago.formaPago,
      "monto": pago.monto,
    }).toList() ?? [],
  };

  final response = await http.put(
    Uri.parse('$baseUrl/api/pedidos/$pedidoId/pagar'),
    headers: {'Content-Type': 'application/json'},
    body: json.encode(body),
  );

  return ApiResponse.fromJson(json.decode(response.body));
}
```

### **5. 💸 Pago Parcial de Productos**

```dart
// POST /api/pedidos/pago-parcial
Future<ApiResponse> pagarProductosParcial({
  required String pedidoId,
  required List<ProductoAPagar> productos,
  required String metodoPago,
  required String procesadoPor,
  String? clienteNombre,
  double? totalCalculado,
  String? notas,
}) async {
  final body = {
    "pedidoId": pedidoId,
    "productos": productos.map((producto) => {
      "productoId": producto.productoId,
      "cantidad": producto.cantidad,
      "precioUnitario": producto.precioUnitario,
    }).toList(),
    "metodoPago": metodoPago,
    "procesadoPor": procesadoPor,
    "clienteNombre": clienteNombre,
    "totalCalculado": totalCalculado,
    "notas": notas,
  };

  final response = await http.post(
    Uri.parse('$baseUrl/api/pedidos/pago-parcial'),
    headers: {'Content-Type': 'application/json'},
    body: json.encode(body),
  );

  // Backend mantiene descuentos proporcionales en pedido restante
  return ApiResponse.fromJson(json.decode(response.body));
}
```

---

## 🧮 **Lógica de Cálculos en el Frontend**

### **📱 Clase Utilitaria para Cálculos**

```dart
class CalculadoraTotales {
  
  /// Calcula el total de items sin descuentos
  static double calcularTotalItems(List<ItemPedido> items) {
    return items.fold(0.0, (sum, item) => sum + item.subtotal);
  }
  
  /// Calcula el total con descuentos aplicados
  static double calcularTotalConDescuento(List<ItemPedido> items, double descuento) {
    double totalItems = calcularTotalItems(items);
    double totalConDescuento = totalItems - descuento;
    return math.max(totalConDescuento, 0.0); // Nunca negativo
  }
  
  /// Calcula el total final a pagar (con propina opcional)
  static double calcularTotalAPagar(double totalBase, double propina, bool incluyePropina) {
    return totalBase + (incluyePropina ? propina : 0.0);
  }
  
  /// Valida que el descuento no exceda el total de items
  static bool validarDescuento(List<ItemPedido> items, double descuento) {
    double totalItems = calcularTotalItems(items);
    return descuento <= totalItems;
  }
}
```

### **🎯 Widget de Gestión de Descuentos**

```dart
class DescuentoWidget extends StatefulWidget {
  final double totalItems;
  final double descuentoActual;
  final Function(double) onDescuentoChanged;
  
  const DescuentoWidget({
    Key? key,
    required this.totalItems,
    required this.descuentoActual,
    required this.onDescuentoChanged,
  }) : super(key: key);

  @override
  State<DescuentoWidget> createState() => _DescuentoWidgetState();
}

class _DescuentoWidgetState extends State<DescuentoWidget> {
  late TextEditingController _controller;
  
  @override
  void initState() {
    super.initState();
    _controller = TextEditingController(
      text: widget.descuentoActual.toString()
    );
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text('Total Items: \$${widget.totalItems.toStringAsFixed(2)}'),
        const SizedBox(height: 8),
        
        TextField(
          controller: _controller,
          keyboardType: TextInputType.numberWithOptions(decimal: true),
          decoration: InputDecoration(
            labelText: 'Descuento',
            prefixText: '\$',
            border: OutlineInputBorder(),
            helperText: 'Máximo: \$${widget.totalItems.toStringAsFixed(2)}',
          ),
          onChanged: (value) {
            double descuento = double.tryParse(value) ?? 0.0;
            
            // Validar que no exceda el total
            if (descuento > widget.totalItems) {
              descuento = widget.totalItems;
              _controller.text = descuento.toString();
              _controller.selection = TextSelection.fromPosition(
                TextPosition(offset: _controller.text.length),
              );
            }
            
            widget.onDescuentoChanged(descuento);
          },
        ),
        
        const SizedBox(height: 8),
        Text(
          'Total con Descuento: \$${CalculadoraTotales.calcularTotalConDescuento([], widget.descuentoActual).toStringAsFixed(2)}',
          style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
        ),
      ],
    );
  }
}
```

### **💰 Widget de Gestión de Propinas**

```dart
class PropinaWidget extends StatefulWidget {
  final double totalBase;
  final double propinaActual;
  final bool incluyePropina;
  final Function(double, bool) onPropinaChanged;
  
  const PropinaWidget({
    Key? key,
    required this.totalBase,
    required this.propinaActual,
    required this.incluyePropina,
    required this.onPropinaChanged,
  }) : super(key: key);

  @override
  State<PropinaWidget> createState() => _PropinaWidgetState();
}

class _PropinaWidgetState extends State<PropinaWidget> {
  late TextEditingController _controller;
  
  @override
  void initState() {
    super.initState();
    _controller = TextEditingController(
      text: widget.propinaActual.toString()
    );
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            Checkbox(
              value: widget.incluyePropina,
              onChanged: (value) {
                widget.onPropinaChanged(widget.propinaActual, value ?? false);
              },
            ),
            Text('Incluir Propina'),
          ],
        ),
        
        if (widget.incluyePropina) ...[
          TextField(
            controller: _controller,
            keyboardType: TextInputType.numberWithOptions(decimal: true),
            decoration: InputDecoration(
              labelText: 'Propina',
              prefixText: '\$',
              border: OutlineInputBorder(),
            ),
            onChanged: (value) {
              double propina = double.tryParse(value) ?? 0.0;
              widget.onPropinaChanged(propina, widget.incluyePropina);
            },
          ),
          
          const SizedBox(height: 8),
          
          // Botones de porcentajes rápidos
          Row(
            children: [
              _buildPercentButton('10%', 0.10),
              SizedBox(width: 8),
              _buildPercentButton('15%', 0.15),
              SizedBox(width: 8),
              _buildPercentButton('20%', 0.20),
            ],
          ),
        ],
        
        const SizedBox(height: 8),
        Text(
          'Total a Pagar: \$${CalculadoraTotales.calcularTotalAPagar(widget.totalBase, widget.propinaActual, widget.incluyePropina).toStringAsFixed(2)}',
          style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18, color: Colors.green),
        ),
      ],
    );
  }
  
  Widget _buildPercentButton(String label, double percent) {
    return ElevatedButton(
      onPressed: () {
        double propina = widget.totalBase * percent;
        _controller.text = propina.toStringAsFixed(2);
        widget.onPropinaChanged(propina, widget.incluyePropina);
      },
      child: Text(label),
    );
  }
}
```

---

## 🔄 **Flujo de Trabajo Completo**

### **📱 Pantalla de Edición de Pedido**

```dart
class PedidoEditScreen extends StatefulWidget {
  final Pedido pedido;
  
  @override
  State<PedidoEditScreen> createState() => _PedidoEditScreenState();
}

class _PedidoEditScreenState extends State<PedidoEditScreen> {
  late Pedido _pedido;
  bool _isLoading = false;
  
  @override
  void initState() {
    super.initState();
    _pedido = widget.pedido.copy(); // Crear copia para editar
  }

  @override
  Widget build(BuildContext context) {
    double totalItems = CalculadoraTotales.calcularTotalItems(_pedido.items);
    double totalConDescuento = CalculadoraTotales.calcularTotalConDescuento(_pedido.items, _pedido.descuento);
    
    return Scaffold(
      appBar: AppBar(title: Text('Editar Pedido - Mesa ${_pedido.mesa}')),
      body: Column(
        children: [
          // Lista de productos
          Expanded(
            child: ListView.builder(
              itemCount: _pedido.items.length,
              itemBuilder: (context, index) {
                return ItemPedidoTile(
                  item: _pedido.items[index],
                  onChanged: (item) {
                    setState(() {
                      _pedido.items[index] = item;
                    });
                  },
                  onRemove: () {
                    setState(() {
                      _pedido.items.removeAt(index);
                    });
                  },
                );
              },
            ),
          ),
          
          // Widget de descuentos
          Padding(
            padding: const EdgeInsets.all(16.0),
            child: DescuentoWidget(
              totalItems: totalItems,
              descuentoActual: _pedido.descuento,
              onDescuentoChanged: (descuento) {
                setState(() {
                  _pedido.descuento = descuento;
                });
              },
            ),
          ),
          
          // Widget de propinas
          Padding(
            padding: const EdgeInsets.all(16.0),
            child: PropinaWidget(
              totalBase: totalConDescuento,
              propinaActual: _pedido.propina,
              incluyePropina: _pedido.incluyePropina,
              onPropinaChanged: (propina, incluye) {
                setState(() {
                  _pedido.propina = propina;
                  _pedido.incluyePropina = incluye;
                });
              },
            ),
          ),
          
          // Botón para guardar
          Padding(
            padding: const EdgeInsets.all(16.0),
            child: SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                onPressed: _isLoading ? null : _guardarPedido,
                child: _isLoading
                  ? CircularProgressIndicator()
                  : Text('Guardar Cambios'),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Future<void> _guardarPedido() async {
    setState(() {
      _isLoading = true;
    });

    try {
      final response = await PedidoService.actualizarPedido(_pedido);
      
      if (response.success) {
        // Backend devuelve pedido con total recalculado
        Navigator.pop(context, response.data);
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Pedido actualizado correctamente')),
        );
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error: ${response.message}')),
        );
      }
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error de conexión: $e')),
      );
    } finally {
      setState(() {
        _isLoading = false;
      });
    }
  }
}
```

---

## 📝 **Notas Importantes para el Frontend**

### **🔥 Puntos Clave:**

1. **No calcules totales finales en el frontend** - El backend siempre tiene la autoridad
2. **Envía descuentos y propinas en cada actualización** - Para que se apliquen correctamente
3. **Usa los valores devueltos por el backend** - Para mostrar totales actualizados
4. **Valida descuentos antes de enviar** - No permitas descuentos mayores al total de items
5. **Maneja estados de loading** - Las operaciones pueden tomar tiempo

### **⚠️ Consideraciones:**

- **Descuentos se aplican al total de items**, no a productos individuales
- **Propinas son opcionales** y se agregan al total final
- **Totales nunca son negativos** - El backend valida esto automáticamente
- **Movimiento de productos** actualiza ambas mesas automáticamente
- **Cancelaciones** devuelven ingredientes al inventario proporcionalmente

### **🚀 Ventajas:**

- **Consistencia**: Backend siempre calcula correctamente
- **Flexibilidad**: Frontend controla descuentos y propinas dinámicamente  
- **Robustez**: Validaciones evitan errores de cálculo
- **Tiempo real**: Actualizaciones inmediatas en todas las operaciones