// MÉTODOS ADMINISTRATIVOS RECOMENDADOS PARA AGREGAR AL AdminController

/**
 * 🗑️ ELIMINAR TODO POR RANGO DE FECHAS
 * Elimina pedidos, documentos, cuadres, gastos e ingresos en un período específico
 */
@DeleteMapping("/eliminar-por-fechas")
public ResponseEntity<Map<String, Object>> eliminarTodoPorFechas(
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
    
    Map<String, Object> response = new HashMap<>();
    Map<String, Long> eliminados = new HashMap<>();
    
    try {
        // Eliminar pedidos en rango
        List<Pedido> pedidos = pedidoRepository.findByFechaBetween(fechaInicio, fechaFin);
        pedidoRepository.deleteAll(pedidos);
        eliminados.put("pedidos", (long) pedidos.size());
        
        // Eliminar documentos mesa en rango  
        List<DocumentoMesa> documentos = documentoMesaRepository.findByFechaBetween(fechaInicio, fechaFin);
        documentoMesaRepository.deleteAll(documentos);
        eliminados.put("documentos_mesa", (long) documentos.size());
        
        // Eliminar cuadres de caja en rango
        List<CuadreCaja> cuadres = cuadreCajaRepository.findByFechaAperturaBetween(fechaInicio, fechaFin);
        cuadreCajaRepository.deleteAll(cuadres);
        eliminados.put("cuadres_caja", (long) cuadres.size());
        
        // Eliminar gastos en rango
        List<Gasto> gastos = gastoRepository.findByFechaGastoBetween(fechaInicio, fechaFin);
        gastoRepository.deleteAll(gastos);
        eliminados.put("gastos", (long) gastos.size());
        
        // Eliminar ingresos en rango
        List<IngresoCaja> ingresos = ingresoCajaRepository.findByFechaIngresoBetween(fechaInicio, fechaFin);
        ingresoCajaRepository.deleteAll(ingresos);
        eliminados.put("ingresos_caja", (long) ingresos.size());
        
        // Eliminar facturas en rango (OPCIONAL - cuidado con facturas de compras importantes)
        List<Factura> facturas = facturaRepository.findByFechaBetween(fechaInicio, fechaFin);
        facturaRepository.deleteAll(facturas);
        eliminados.put("facturas", (long) facturas.size());
        
        response.put("success", true);
        response.put("message", "Datos eliminados exitosamente en el rango de fechas");
        response.put("fechaInicio", fechaInicio);
        response.put("fechaFin", fechaFin);
        response.put("eliminados", eliminados);
        
        return ResponseEntity.ok(response);
        
    } catch (Exception e) {
        response.put("success", false);
        response.put("message", "Error eliminando datos: " + e.getMessage());
        return ResponseEntity.status(500).body(response);
    }
}

/**
 * 📊 RESUMEN ANTES DE ELIMINAR
 * Muestra qué se va a eliminar antes de hacerlo (para confirmación)
 */
@GetMapping("/contar-por-fechas")
public ResponseEntity<Map<String, Object>> contarRegistrosPorFechas(
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
    
    Map<String, Object> response = new HashMap<>();
    Map<String, Long> conteos = new HashMap<>();
    
    try {
        conteos.put("pedidos", (long) pedidoRepository.findByFechaBetween(fechaInicio, fechaFin).size());
        conteos.put("documentos_mesa", (long) documentoMesaRepository.findByFechaBetween(fechaInicio, fechaFin).size());
        conteos.put("cuadres_caja", (long) cuadreCajaRepository.findByFechaAperturaBetween(fechaInicio, fechaFin).size());
        conteos.put("gastos", (long) gastoRepository.findByFechaGastoBetween(fechaInicio, fechaFin).size());
        conteos.put("ingresos_caja", (long) ingresoCajaRepository.findByFechaIngresoBetween(fechaInicio, fechaFin).size());
        conteos.put("facturas", (long) facturaRepository.findByFechaBetween(fechaInicio, fechaFin).size());
        
        response.put("success", true);
        response.put("fechaInicio", fechaInicio);
        response.put("fechaFin", fechaFin);
        response.put("conteos", conteos);
        
        return ResponseEntity.ok(response);
        
    } catch (Exception e) {
        response.put("success", false);
        response.put("message", "Error contando registros: " + e.getMessage());
        return ResponseEntity.status(500).body(response);
    }
}

/**
 * 🔄 BACKUP ANTES DE ELIMINAR
 * Crea un backup de los datos que se van a eliminar
 */
@PostMapping("/backup-por-fechas")
public ResponseEntity<Map<String, Object>> crearBackupPorFechas(
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
    @RequestParam String nombreBackup) {
    
    // Implementar lógica de backup en archivos JSON
    Map<String, Object> response = new HashMap<>();
    // ... código de backup
    return ResponseEntity.ok(response);
}

/**
 * 🧹 MANTENIMIENTO AUTOMÁTICO
 * Elimina registros antiguos automáticamente (ej: más de 6 meses)
 */
@PostMapping("/mantenimiento-automatico")
public ResponseEntity<Map<String, Object>> mantenimientoAutomatico(
    @RequestParam(defaultValue = "6") int mesesAntiguedad) {
    
    LocalDateTime fechaLimite = LocalDateTime.now().minusMonths(mesesAntiguedad);
    // Reutilizar método eliminarTodoPorFechas
    return eliminarTodoPorFechas(LocalDateTime.of(2020, 1, 1, 0, 0), fechaLimite);
}

/**
 * 📈 ESTADÍSTICAS AVANZADAS
 * Obtiene estadísticas detalladas por períodos
 */
@GetMapping("/estadisticas-detalladas")
public ResponseEntity<Map<String, Object>> estadisticasDetalladas(
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
    
    Map<String, Object> response = new HashMap<>();
    
    // Estadísticas por mes/semana/día
    // Totales de ventas, gastos, utilidades
    // Productos más vendidos en el período
    // Análisis de crecimiento
    
    return ResponseEntity.ok(response);
}

/**
 * 🔧 REPARAR DATOS INCONSISTENTES
 * Detecta y repara inconsistencias en los datos
 */
@PostMapping("/reparar-inconsistencias")
public ResponseEntity<Map<String, Object>> repararInconsistencias() {
    
    Map<String, Object> response = new HashMap<>();
    List<String> reparaciones = new ArrayList<>();
    
    try {
        // 1. Pedidos sin asignar a cuadre pero en fechas de cuadres existentes
        // 2. Documentos huérfanos sin pedido correspondiente
        // 3. Gastos sin cuadre asignado
        // 4. Facturas duplicadas
        // 5. Inventarios negativos
        
        response.put("success", true);
        response.put("reparaciones", reparaciones);
        
        return ResponseEntity.ok(response);
        
    } catch (Exception e) {
        response.put("success", false);
        response.put("message", "Error reparando datos: " + e.getMessage());
        return ResponseEntity.status(500).body(response);
    }
}