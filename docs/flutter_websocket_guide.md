# Guía de Implementación de WebSockets para Flutter

Este documento proporciona una guía sobre cómo implementar la conexión WebSocket en Flutter para recibir actualizaciones en tiempo real desde el servidor backend de Sopa y Carbón.

## Dependencias Necesarias

Añade estas dependencias a tu archivo `pubspec.yaml`:

```yaml
dependencies:
  web_socket_channel: ^2.2.0
  stomp_dart_client: ^0.4.4
```

## Clase WebSocketService para Flutter

```dart
import 'dart:async';
import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:stomp_dart_client/stomp_dart_client.dart';
import 'package:stomp_dart_client/stomp_config.dart';

/// Servicio para gestionar la conexión WebSocket en Flutter
class WebSocketService {
  // Singleton instance
  static final WebSocketService _instance = WebSocketService._internal();
  factory WebSocketService() => _instance;
  WebSocketService._internal();

  // Variables
  StompClient? _stompClient;
  bool isConnected = false;

  // Controllers para los diferentes tipos de notificaciones
  final _mesasController = StreamController<dynamic>.broadcast();
  final _pedidosController = StreamController<dynamic>.broadcast();
  final _inventarioController = StreamController<dynamic>.broadcast();

  // Streams para escuchar las actualizaciones
  Stream<dynamic> get mesasStream => _mesasController.stream;
  Stream<dynamic> get pedidosStream => _pedidosController.stream;
  Stream<dynamic> get inventarioStream => _inventarioController.stream;

  // Conectar al servidor WebSocket
  void connect(String baseUrl) {
    final socketUrl = '$baseUrl/ws';

    _stompClient = StompClient(
      config: StompConfig(
        url: socketUrl,
        onConnect: onConnect,
        onWebSocketError: (dynamic error) => print('Error WebSocket: $error'),
        stompConnectHeaders: {},
        webSocketConnectHeaders: {},
      ),
    );

    _stompClient!.activate();
  }

  // Callback cuando se establece la conexión
  void onConnect(StompFrame frame) {
    isConnected = true;

    // Suscribirse a los canales
    _stompClient!.subscribe(
      destination: '/topic/mesas',
      callback: (frame) {
        if (frame.body != null) {
          final data = json.decode(frame.body!);
          _mesasController.add(data);
        }
      },
    );

    _stompClient!.subscribe(
      destination: '/topic/pedidos',
      callback: (frame) {
        if (frame.body != null) {
          final data = json.decode(frame.body!);
          _pedidosController.add(data);
        }
      },
    );

    _stompClient!.subscribe(
      destination: '/topic/inventario',
      callback: (frame) {
        if (frame.body != null) {
          final data = json.decode(frame.body!);
          _inventarioController.add(data);
        }
      },
    );

    print('Conectado a WebSocket');
  }

  // Desconectar del servidor WebSocket
  void disconnect() {
    if (_stompClient != null && _stompClient!.connected) {
      _stompClient!.deactivate();
    }
    isConnected = false;
    print('Desconectado de WebSocket');
  }

  // Liberar recursos
  void dispose() {
    disconnect();
    _mesasController.close();
    _pedidosController.close();
    _inventarioController.close();
  }
}
```

## Uso en Flutter

### Inicialización

```dart
@override
void initState() {
  super.initState();

  // Conectar al WebSocket
  final webSocketService = WebSocketService();
  webSocketService.connect('http://tu-servidor-backend.com');

  // Escuchar actualizaciones de mesas
  webSocketService.mesasStream.listen((data) {
    setState(() {
      // Actualizar el estado basado en los datos recibidos
      print('Mesa actualizada: ${data['nombreMesa']}');

      // Aquí puedes actualizar tu UI o estado
    });
  });

  // Similar para pedidos e inventario
}
```

### Ejemplo de Widget para Mostrar Mesas con Actualizaciones en Tiempo Real

```dart
class MesasListScreen extends StatefulWidget {
  @override
  _MesasListScreenState createState() => _MesasListScreenState();
}

class _MesasListScreenState extends State<MesasListScreen> {
  final WebSocketService _webSocketService = WebSocketService();
  List<Mesa> mesas = [];
  bool isLoading = true;

  @override
  void initState() {
    super.initState();
    _webSocketService.connect('http://tu-servidor-backend.com');

    // Escuchar actualizaciones de mesas
    _webSocketService.mesasStream.listen((data) {
      _handleMesaUpdate(data);
    });

    // Cargar datos iniciales
    _loadMesas();
  }

  Future<void> _loadMesas() async {
    setState(() => isLoading = true);

    try {
      final response = await http.get(Uri.parse('http://tu-servidor-backend.com/api/mesas'));
      if (response.statusCode == 200) {
        final jsonData = json.decode(response.body);
        if (jsonData['status'] == 'success') {
          setState(() {
            mesas = (jsonData['data'] as List)
                .map((item) => Mesa.fromJson(item))
                .toList();
            isLoading = false;
          });
        }
      }
    } catch (e) {
      setState(() => isLoading = false);
      print('Error al cargar mesas: $e');
    }
  }

  void _handleMesaUpdate(dynamic data) {
    // Buscar la mesa en la lista por ID
    final mesaId = data['mesaId'];
    final index = mesas.indexWhere((mesa) => mesa.id == mesaId);

    setState(() {
      if (index >= 0) {
        // Actualizar mesa existente
        if (data['accion'] == 'ELIMINAR') {
          mesas.removeAt(index);
        } else {
          // Hacer una petición para obtener los datos actualizados
          _refreshMesa(mesaId, index);
        }
      } else if (data['accion'] == 'CREAR') {
        // Añadir nueva mesa
        _loadMesas(); // Recargar todas para simplicidad
      }
    });
  }

  Future<void> _refreshMesa(String mesaId, int index) async {
    try {
      final response = await http.get(
        Uri.parse('http://tu-servidor-backend.com/api/mesas/$mesaId')
      );

      if (response.statusCode == 200) {
        final jsonData = json.decode(response.body);
        if (jsonData['status'] == 'success' && jsonData['data'] != null) {
          setState(() {
            mesas[index] = Mesa.fromJson(jsonData['data']);
          });
        }
      }
    } catch (e) {
      print('Error al actualizar mesa: $e');
    }
  }

  @override
  void dispose() {
    // No es necesario llamar a dispose aquí si es un singleton
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (isLoading) {
      return Center(child: CircularProgressIndicator());
    }

    return ListView.builder(
      itemCount: mesas.length,
      itemBuilder: (context, index) {
        final mesa = mesas[index];
        return Card(
          color: mesa.ocupada ? Colors.red.shade100 : Colors.green.shade100,
          child: ListTile(
            title: Text('Mesa ${mesa.nombre}'),
            subtitle: Text(mesa.ocupada ? 'Ocupada' : 'Libre'),
            trailing: Text('\$${mesa.total.toStringAsFixed(2)}'),
            onTap: () {
              // Navegar a detalles de la mesa
            },
          ),
        );
      },
    );
  }
}
```

### Ejemplo de Modelo de Mesa

```dart
class Mesa {
  final String id;
  final String nombre;
  final bool ocupada;
  final double total;
  final List<String> productosIds;

  Mesa({
    required this.id,
    required this.nombre,
    required this.ocupada,
    required this.total,
    required this.productosIds,
  });

  factory Mesa.fromJson(Map<String, dynamic> json) {
    return Mesa(
      id: json['_id'],
      nombre: json['nombre'],
      ocupada: json['ocupada'],
      total: json['total'].toDouble(),
      productosIds: List<String>.from(json['productosIds'] ?? []),
    );
  }
}
```

## Manejo de Reconexiones

Para manejar reconexiones, puedes implementar una lógica que intente reconectar automáticamente cuando se pierde la conexión:

```dart
void setupReconnection() {
  Timer.periodic(Duration(seconds: 5), (timer) {
    if (!isConnected) {
      print('Intentando reconectar...');
      connect('http://tu-servidor-backend.com');
    }
  });
}
```

---

Esta guía proporciona los componentes básicos necesarios para integrar las notificaciones en tiempo real mediante WebSockets en tu aplicación Flutter para el sistema Sopa y Carbón.
