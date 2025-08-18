# Guía de Implementación de Auditoría para Flutter

Este documento proporciona una guía para implementar la visualización y gestión de registros de auditoría en la aplicación Flutter de Sopa y Carbón.

## Dependencias Necesarias

Añade estas dependencias a tu archivo `pubspec.yaml` si aún no las tienes:

```yaml
dependencies:
  http: ^0.13.4
  intl: ^0.17.0
  provider: ^6.0.1
  flutter_datetime_picker: ^1.5.1
```

## Modelo para AuditLog

```dart
import 'package:intl/intl.dart';

class AuditLog {
  final String id;
  final String moduloSistema;
  final String tipoAccion;
  final String descripcion;
  final String entidadId;
  final String usuarioId;
  final String valoresAnteriores;
  final String valoresNuevos;
  final DateTime fecha;

  AuditLog({
    required this.id,
    required this.moduloSistema,
    required this.tipoAccion,
    required this.descripcion,
    required this.entidadId,
    required this.usuarioId,
    this.valoresAnteriores = '',
    this.valoresNuevos = '',
    required this.fecha,
  });

  factory AuditLog.fromJson(Map<String, dynamic> json) {
    return AuditLog(
      id: json['_id'] ?? '',
      moduloSistema: json['moduloSistema'] ?? '',
      tipoAccion: json['tipoAccion'] ?? '',
      descripcion: json['descripcion'] ?? '',
      entidadId: json['entidadId'] ?? '',
      usuarioId: json['usuarioId'] ?? '',
      valoresAnteriores: json['valoresAnteriores'] ?? '',
      valoresNuevos: json['valoresNuevos'] ?? '',
      fecha: json['fecha'] != null
          ? DateTime.parse(json['fecha'])
          : DateTime.now(),
    );
  }

  String get fechaFormateada {
    final formatter = DateFormat('dd/MM/yyyy HH:mm');
    return formatter.format(fecha);
  }
}
```

## Proveedor para la Gestión de Auditoría

```dart
import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;
import 'audit_log.dart';

class AuditoriaProvider extends ChangeNotifier {
  final String baseUrl;

  List<AuditLog> _logs = [];
  bool _isLoading = false;
  String _error = '';
  Map<String, dynamic> _resumen = {};

  AuditoriaProvider({required this.baseUrl});

  // Getters
  List<AuditLog> get logs => _logs;
  bool get isLoading => _isLoading;
  String get error => _error;
  Map<String, dynamic> get resumen => _resumen;

  // Cargar todos los registros (limitado a 50)
  Future<void> cargarRegistros() async {
    _setLoading(true);

    try {
      final response = await http.get(Uri.parse('$baseUrl/api/auditoria'));

      if (response.statusCode == 200) {
        final jsonData = json.decode(response.body);
        if (jsonData['status'] == 'success' && jsonData['data'] != null) {
          _logs = (jsonData['data'] as List)
              .map((item) => AuditLog.fromJson(item))
              .toList();
          _error = '';
        } else {
          _error = jsonData['message'] ?? 'Error desconocido';
        }
      } else {
        _error = 'Error en la solicitud: ${response.statusCode}';
      }
    } catch (e) {
      _error = 'Error de conexión: $e';
    } finally {
      _setLoading(false);
    }
  }

  // Cargar registros paginados
  Future<void> cargarRegistrosPaginados(int pagina, {int tamanio = 20}) async {
    _setLoading(true);

    try {
      final response = await http.get(
        Uri.parse('$baseUrl/api/auditoria/paginado?pagina=$pagina&tamanio=$tamanio')
      );

      if (response.statusCode == 200) {
        final jsonData = json.decode(response.body);
        if (jsonData['status'] == 'success' && jsonData['data'] != null) {
          _logs = (jsonData['data'] as List)
              .map((item) => AuditLog.fromJson(item))
              .toList();
          _error = '';
        } else {
          _error = jsonData['message'] ?? 'Error desconocido';
        }
      } else {
        _error = 'Error en la solicitud: ${response.statusCode}';
      }
    } catch (e) {
      _error = 'Error de conexión: $e';
    } finally {
      _setLoading(false);
    }
  }

  // Cargar registros por módulo
  Future<void> cargarPorModulo(String modulo) async {
    _setLoading(true);

    try {
      final response = await http.get(
        Uri.parse('$baseUrl/api/auditoria/modulo/$modulo')
      );

      if (response.statusCode == 200) {
        final jsonData = json.decode(response.body);
        if (jsonData['status'] == 'success' && jsonData['data'] != null) {
          _logs = (jsonData['data'] as List)
              .map((item) => AuditLog.fromJson(item))
              .toList();
          _error = '';
        } else {
          _error = jsonData['message'] ?? 'Error desconocido';
        }
      } else {
        _error = 'Error en la solicitud: ${response.statusCode}';
      }
    } catch (e) {
      _error = 'Error de conexión: $e';
    } finally {
      _setLoading(false);
    }
  }

  // Cargar registros por módulo y tipo de acción
  Future<void> cargarPorModuloYAccion(String modulo, String accion) async {
    _setLoading(true);

    try {
      final response = await http.get(
        Uri.parse('$baseUrl/api/auditoria/modulo/$modulo/accion/$accion')
      );

      if (response.statusCode == 200) {
        final jsonData = json.decode(response.body);
        if (jsonData['status'] == 'success' && jsonData['data'] != null) {
          _logs = (jsonData['data'] as List)
              .map((item) => AuditLog.fromJson(item))
              .toList();
          _error = '';
        } else {
          _error = jsonData['message'] ?? 'Error desconocido';
        }
      } else {
        _error = 'Error en la solicitud: ${response.statusCode}';
      }
    } catch (e) {
      _error = 'Error de conexión: $e';
    } finally {
      _setLoading(false);
    }
  }

  // Cargar registros por usuario
  Future<void> cargarPorUsuario(String usuarioId) async {
    _setLoading(true);

    try {
      final response = await http.get(
        Uri.parse('$baseUrl/api/auditoria/usuario/$usuarioId')
      );

      if (response.statusCode == 200) {
        final jsonData = json.decode(response.body);
        if (jsonData['status'] == 'success' && jsonData['data'] != null) {
          _logs = (jsonData['data'] as List)
              .map((item) => AuditLog.fromJson(item))
              .toList();
          _error = '';
        } else {
          _error = jsonData['message'] ?? 'Error desconocido';
        }
      } else {
        _error = 'Error en la solicitud: ${response.statusCode}';
      }
    } catch (e) {
      _error = 'Error de conexión: $e';
    } finally {
      _setLoading(false);
    }
  }

  // Cargar registros por rango de fechas
  Future<void> cargarPorFechas(DateTime inicio, DateTime fin) async {
    _setLoading(true);

    try {
      final inicioStr = inicio.toIso8601String();
      final finStr = fin.toIso8601String();

      final response = await http.get(
        Uri.parse('$baseUrl/api/auditoria/fecha?fechaInicio=$inicioStr&fechaFin=$finStr')
      );

      if (response.statusCode == 200) {
        final jsonData = json.decode(response.body);
        if (jsonData['status'] == 'success' && jsonData['data'] != null) {
          _logs = (jsonData['data'] as List)
              .map((item) => AuditLog.fromJson(item))
              .toList();
          _error = '';
        } else {
          _error = jsonData['message'] ?? 'Error desconocido';
        }
      } else {
        _error = 'Error en la solicitud: ${response.statusCode}';
      }
    } catch (e) {
      _error = 'Error de conexión: $e';
    } finally {
      _setLoading(false);
    }
  }

  // Cargar resumen de auditoría
  Future<void> cargarResumen() async {
    _setLoading(true);

    try {
      final response = await http.get(Uri.parse('$baseUrl/api/auditoria/resumen'));

      if (response.statusCode == 200) {
        final jsonData = json.decode(response.body);
        if (jsonData['status'] == 'success' && jsonData['data'] != null) {
          _resumen = jsonData['data'];

          // Convertir las últimas actividades a objetos AuditLog
          if (_resumen['ultimasActividades'] != null) {
            final actividades = (_resumen['ultimasActividades'] as List)
                .map((item) => AuditLog.fromJson(item))
                .toList();
            _resumen['ultimasActividades'] = actividades;
          }

          _error = '';
        } else {
          _error = jsonData['message'] ?? 'Error desconocido';
        }
      } else {
        _error = 'Error en la solicitud: ${response.statusCode}';
      }
    } catch (e) {
      _error = 'Error de conexión: $e';
    } finally {
      _setLoading(false);
    }
  }

  void _setLoading(bool loading) {
    _isLoading = loading;
    notifyListeners();
  }
}
```

## Pantalla de Auditoría

```dart
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:intl/intl.dart';
import 'package:flutter_datetime_picker/flutter_datetime_picker.dart';
import '../providers/auditoria_provider.dart';
import '../models/audit_log.dart';

class AuditoriaScreen extends StatefulWidget {
  @override
  _AuditoriaScreenState createState() => _AuditoriaScreenState();
}

class _AuditoriaScreenState extends State<AuditoriaScreen> {
  String _selectedModulo = '';
  String _selectedAccion = '';
  DateTime? _fechaInicio;
  DateTime? _fechaFin;
  int _currentPage = 0;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      Provider.of<AuditoriaProvider>(context, listen: false).cargarRegistros();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Registros de Auditoría'),
        actions: [
          IconButton(
            icon: Icon(Icons.dashboard),
            onPressed: () => _mostrarResumen(),
            tooltip: 'Ver resumen',
          ),
        ],
      ),
      body: Column(
        children: [
          _buildFilters(),
          Expanded(child: _buildLogList()),
          _buildPagination(),
        ],
      ),
    );
  }

  Widget _buildFilters() {
    return Container(
      padding: EdgeInsets.all(8.0),
      color: Colors.grey.shade100,
      child: Column(
        children: [
          Row(
            children: [
              Expanded(
                child: DropdownButtonFormField<String>(
                  decoration: InputDecoration(
                    labelText: 'Módulo',
                    border: OutlineInputBorder(),
                    contentPadding: EdgeInsets.symmetric(horizontal: 10.0),
                  ),
                  value: _selectedModulo.isEmpty ? null : _selectedModulo,
                  items: [
                    DropdownMenuItem(value: '', child: Text('Todos')),
                    DropdownMenuItem(value: 'MESAS', child: Text('Mesas')),
                    DropdownMenuItem(value: 'PEDIDOS', child: Text('Pedidos')),
                    DropdownMenuItem(value: 'INVENTARIO', child: Text('Inventario')),
                    DropdownMenuItem(value: 'CAJA', child: Text('Caja')),
                  ],
                  onChanged: (value) {
                    setState(() => _selectedModulo = value ?? '');
                  },
                ),
              ),
              SizedBox(width: 8),
              Expanded(
                child: DropdownButtonFormField<String>(
                  decoration: InputDecoration(
                    labelText: 'Acción',
                    border: OutlineInputBorder(),
                    contentPadding: EdgeInsets.symmetric(horizontal: 10.0),
                  ),
                  value: _selectedAccion.isEmpty ? null : _selectedAccion,
                  items: [
                    DropdownMenuItem(value: '', child: Text('Todas')),
                    DropdownMenuItem(value: 'CREAR', child: Text('Crear')),
                    DropdownMenuItem(value: 'ACTUALIZAR', child: Text('Actualizar')),
                    DropdownMenuItem(value: 'ELIMINAR', child: Text('Eliminar')),
                  ],
                  onChanged: (value) {
                    setState(() => _selectedAccion = value ?? '');
                  },
                ),
              ),
            ],
          ),
          SizedBox(height: 8),
          Row(
            children: [
              Expanded(
                child: InkWell(
                  onTap: () => _seleccionarFecha(true),
                  child: InputDecorator(
                    decoration: InputDecoration(
                      labelText: 'Fecha Inicio',
                      border: OutlineInputBorder(),
                      contentPadding: EdgeInsets.symmetric(horizontal: 10.0),
                    ),
                    child: Text(
                      _fechaInicio != null
                          ? DateFormat('dd/MM/yyyy').format(_fechaInicio!)
                          : 'Seleccione',
                    ),
                  ),
                ),
              ),
              SizedBox(width: 8),
              Expanded(
                child: InkWell(
                  onTap: () => _seleccionarFecha(false),
                  child: InputDecorator(
                    decoration: InputDecoration(
                      labelText: 'Fecha Fin',
                      border: OutlineInputBorder(),
                      contentPadding: EdgeInsets.symmetric(horizontal: 10.0),
                    ),
                    child: Text(
                      _fechaFin != null
                          ? DateFormat('dd/MM/yyyy').format(_fechaFin!)
                          : 'Seleccione',
                    ),
                  ),
                ),
              ),
            ],
          ),
          SizedBox(height: 8),
          ElevatedButton.icon(
            icon: Icon(Icons.search),
            label: Text('Buscar'),
            style: ElevatedButton.styleFrom(
              minimumSize: Size(double.infinity, 45),
            ),
            onPressed: _aplicarFiltros,
          ),
        ],
      ),
    );
  }

  Widget _buildLogList() {
    return Consumer<AuditoriaProvider>(
      builder: (context, provider, child) {
        if (provider.isLoading) {
          return Center(child: CircularProgressIndicator());
        }

        if (provider.error.isNotEmpty) {
          return Center(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Text('Error: ${provider.error}',
                     style: TextStyle(color: Colors.red)),
                ElevatedButton(
                  child: Text('Reintentar'),
                  onPressed: () => provider.cargarRegistros(),
                ),
              ],
            ),
          );
        }

        if (provider.logs.isEmpty) {
          return Center(
            child: Text('No hay registros de auditoría disponibles'),
          );
        }

        return ListView.builder(
          itemCount: provider.logs.length,
          itemBuilder: (context, index) {
            final log = provider.logs[index];
            return _buildLogItem(log);
          },
        );
      },
    );
  }

  Widget _buildLogItem(AuditLog log) {
    Color moduleColor;
    IconData moduleIcon;

    switch (log.moduloSistema) {
      case 'MESAS':
        moduleColor = Colors.green;
        moduleIcon = Icons.table_bar;
        break;
      case 'PEDIDOS':
        moduleColor = Colors.orange;
        moduleIcon = Icons.receipt_long;
        break;
      case 'INVENTARIO':
        moduleColor = Colors.blue;
        moduleIcon = Icons.inventory_2;
        break;
      case 'CAJA':
        moduleColor = Colors.purple;
        moduleIcon = Icons.point_of_sale;
        break;
      default:
        moduleColor = Colors.grey;
        moduleIcon = Icons.circle;
    }

    return Card(
      margin: EdgeInsets.symmetric(vertical: 4, horizontal: 8),
      child: ListTile(
        leading: CircleAvatar(
          backgroundColor: moduleColor,
          child: Icon(moduleIcon, color: Colors.white, size: 20),
        ),
        title: Text(log.descripcion),
        subtitle: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('${log.tipoAccion} - Entidad: ${log.entidadId}'),
            Text(
              'Usuario: ${log.usuarioId} - ${log.fechaFormateada}',
              style: TextStyle(fontSize: 12, color: Colors.grey),
            ),
          ],
        ),
        isThreeLine: true,
        onTap: () => _mostrarDetallesLog(log),
      ),
    );
  }

  Widget _buildPagination() {
    return Container(
      padding: EdgeInsets.symmetric(vertical: 8, horizontal: 16),
      color: Colors.grey.shade200,
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          TextButton.icon(
            icon: Icon(Icons.arrow_back),
            label: Text('Anterior'),
            onPressed: _currentPage > 0
                ? () {
                    setState(() => _currentPage--);
                    _cargarPaginaActual();
                  }
                : null,
          ),
          Text('Página ${_currentPage + 1}'),
          TextButton.icon(
            icon: Icon(Icons.arrow_forward),
            label: Text('Siguiente'),
            onPressed: () {
              setState(() => _currentPage++);
              _cargarPaginaActual();
            },
          ),
        ],
      ),
    );
  }

  void _seleccionarFecha(bool esInicio) {
    DatePicker.showDatePicker(
      context,
      showTitleActions: true,
      minTime: DateTime(2020, 1, 1),
      maxTime: DateTime.now().add(Duration(days: 1)),
      onConfirm: (date) {
        setState(() {
          if (esInicio) {
            _fechaInicio = date;
          } else {
            _fechaFin = date;
          }
        });
      },
      currentTime: esInicio ? _fechaInicio ?? DateTime.now() : _fechaFin ?? DateTime.now(),
      locale: LocaleType.es,
    );
  }

  void _aplicarFiltros() {
    _currentPage = 0;
    final provider = Provider.of<AuditoriaProvider>(context, listen: false);

    if (_selectedModulo.isNotEmpty && _selectedAccion.isNotEmpty) {
      // Filtro por módulo y acción
      provider.cargarPorModuloYAccion(_selectedModulo, _selectedAccion);
    } else if (_selectedModulo.isNotEmpty) {
      // Filtro solo por módulo
      provider.cargarPorModulo(_selectedModulo);
    } else if (_fechaInicio != null && _fechaFin != null) {
      // Filtro por fechas
      provider.cargarPorFechas(_fechaInicio!, _fechaFin!);
    } else {
      // Sin filtros específicos, cargar todos
      provider.cargarRegistrosPaginados(_currentPage);
    }
  }

  void _cargarPaginaActual() {
    final provider = Provider.of<AuditoriaProvider>(context, listen: false);
    provider.cargarRegistrosPaginados(_currentPage);
  }

  void _mostrarDetallesLog(AuditLog log) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('Detalles del Registro'),
        content: SingleChildScrollView(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisSize: MainAxisSize.min,
            children: [
              _buildDetailItem('ID', log.id),
              _buildDetailItem('Módulo', log.moduloSistema),
              _buildDetailItem('Acción', log.tipoAccion),
              _buildDetailItem('Descripción', log.descripcion),
              _buildDetailItem('Entidad ID', log.entidadId),
              _buildDetailItem('Usuario', log.usuarioId),
              _buildDetailItem('Fecha', log.fechaFormateada),
              if (log.valoresAnteriores.isNotEmpty)
                _buildDetailItem('Valores Anteriores', log.valoresAnteriores),
              if (log.valoresNuevos.isNotEmpty)
                _buildDetailItem('Valores Nuevos', log.valoresNuevos),
            ],
          ),
        ),
        actions: [
          TextButton(
            child: Text('Cerrar'),
            onPressed: () => Navigator.of(context).pop(),
          ),
        ],
      ),
    );
  }

  Widget _buildDetailItem(String label, String value) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(label, style: TextStyle(fontWeight: FontWeight.bold)),
          Text(value.isEmpty ? 'N/A' : value),
          Divider(),
        ],
      ),
    );
  }

  void _mostrarResumen() {
    final provider = Provider.of<AuditoriaProvider>(context, listen: false);
    provider.cargarResumen();

    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('Resumen de Auditoría'),
        content: Consumer<AuditoriaProvider>(
          builder: (context, provider, child) {
            if (provider.isLoading) {
              return Container(
                height: 200,
                child: Center(child: CircularProgressIndicator()),
              );
            }

            if (provider.error.isNotEmpty) {
              return Text('Error: ${provider.error}',
                         style: TextStyle(color: Colors.red));
            }

            final resumen = provider.resumen;
            if (resumen.isEmpty) {
              return Text('No hay datos disponibles');
            }

            return Container(
              width: double.maxFinite,
              child: SingleChildScrollView(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Text('Total de Registros: ${resumen['total'] ?? 0}',
                         style: TextStyle(fontWeight: FontWeight.bold)),
                    Divider(),
                    Text('Registros por Módulo:',
                         style: TextStyle(fontWeight: FontWeight.bold)),
                    SizedBox(height: 8),
                    _buildResumenModulos(resumen),
                    Divider(),
                    Text('Últimas Actividades:',
                         style: TextStyle(fontWeight: FontWeight.bold)),
                    SizedBox(height: 8),
                    _buildUltimasActividades(resumen),
                  ],
                ),
              ),
            );
          },
        ),
        actions: [
          TextButton(
            child: Text('Cerrar'),
            onPressed: () => Navigator.of(context).pop(),
          ),
        ],
      ),
    );
  }

  Widget _buildResumenModulos(Map<String, dynamic> resumen) {
    final conteoModulos = resumen['conteoModulos'] as Map<String, dynamic>?;
    if (conteoModulos == null || conteoModulos.isEmpty) {
      return Text('No hay datos');
    }

    return Column(
      children: conteoModulos.entries.map((entry) {
        return ListTile(
          title: Text(entry.key),
          trailing: Text(entry.value.toString(),
                       style: TextStyle(fontWeight: FontWeight.bold)),
          dense: true,
        );
      }).toList(),
    );
  }

  Widget _buildUltimasActividades(Map<String, dynamic> resumen) {
    final actividades = resumen['ultimasActividades'] as List<AuditLog>?;
    if (actividades == null || actividades.isEmpty) {
      return Text('No hay actividades recientes');
    }

    return Column(
      children: actividades.map((log) {
        return ListTile(
          title: Text(log.descripcion),
          subtitle: Text('${log.fechaFormateada} - ${log.moduloSistema}'),
          dense: true,
        );
      }).toList(),
    );
  }
}
```

## Configuración del Provider

Para usar esta funcionalidad en tu aplicación, agrega el provider en el archivo principal:

```dart
void main() {
  runApp(
    MultiProvider(
      providers: [
        ChangeNotifierProvider(
          create: (_) => AuditoriaProvider(
            baseUrl: 'http://tu-servidor-backend.com',
          ),
        ),
        // Otros providers...
      ],
      child: MyApp(),
    ),
  );
}
```

## Navegación a la Pantalla de Auditoría

Para acceder a la pantalla de auditoría:

```dart
Navigator.of(context).push(
  MaterialPageRoute(
    builder: (context) => AuditoriaScreen(),
  ),
);
```

---

Esta implementación proporciona una interfaz completa para visualizar y filtrar los registros de auditoría en la aplicación Flutter para Sopa y Carbón. Incluye funcionalidades de filtrado por módulo, acción, rango de fechas y paginación para manejar grandes volúmenes de registros.
