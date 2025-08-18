package com.prog3.security.Services;

import com.prog3.security.Models.*;
import com.prog3.security.Repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Servicio encargado de recuperar el stock de ingredientes cuando se cancela un
 * pedido que previamente había sido entregado y descontado del inventario.
 */
@Service
public class RecuperacionInventarioService {

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private MovimientoInventarioRepository movimientoInventarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private IngredienteRepository ingredienteRepository;

    /**
     * Recupera los ingredientes descontados para un pedido específico basándose
     * en los movimientos de inventario registrados
     *
     * @param pedidoId ID del pedido cancelado
     * @param usuarioId ID del usuario que realiza la cancelación
     * @return Resultado del proceso de recuperación
     */
    public Map<String, Object> recuperarIngredientesPorCancelacionPedido(String pedidoId, String usuarioId) {
        Map<String, Object> resultado = new HashMap<>();
        List<Map<String, Object>> ingredientesRecuperados = new ArrayList<>();
        int totalIngredientesRecuperados = 0;

        try {
            System.out.println("🔄 Iniciando recuperación de inventario para pedido cancelado: " + pedidoId);

            // 1. Buscar todos los movimientos de inventario relacionados con este pedido
            List<MovimientoInventario> movimientos = movimientoInventarioRepository
                    .findByReferencia(pedidoId);

            // 2. Si no hay movimientos, no hay nada que recuperar
            if (movimientos == null || movimientos.isEmpty()) {
                System.out.println("ℹ️ No se encontraron movimientos de inventario para el pedido: " + pedidoId);
                resultado.put("exito", true);
                resultado.put("mensaje", "No se encontraron ingredientes para recuperar");
                resultado.put("ingredientes", ingredientesRecuperados);
                return resultado;
            }

            System.out.println("📋 Se encontraron " + movimientos.size() + " movimientos de inventario para este pedido");

            // 3. Procesar cada movimiento de salida para crear una entrada correspondiente
            for (MovimientoInventario movimiento : movimientos) {
                // Solo recuperamos las salidas (que tienen cantidad negativa)
                if (!"salida".equals(movimiento.getTipoMovimiento()) || movimiento.getCantidadMovimiento() >= 0) {
                    continue;
                }

                // Obtener el inventario actual de este ingrediente
                String ingredienteId = movimiento.getProductoId();
                Inventario inventario = inventarioRepository.findByProductoId(ingredienteId);

                if (inventario == null) {
                    System.out.println("⚠️ No se encontró inventario para el ingrediente: " + ingredienteId);
                    continue;
                }

                // Obtener el ingrediente para actualizar su stock
                Optional<Ingrediente> ingredienteOpt = ingredienteRepository.findById(ingredienteId);
                if (!ingredienteOpt.isPresent()) {
                    System.out.println("⚠️ No se encontró el ingrediente: " + ingredienteId);
                    continue;
                }

                Ingrediente ingrediente = ingredienteOpt.get();

                // Calcular la cantidad a recuperar (valor absoluto del movimiento original)
                double cantidadARecuperar = Math.abs(movimiento.getCantidadMovimiento());

                // Actualizar el stock del ingrediente
                double stockAnterior = ingrediente.getStockActual();
                ingrediente.setStockActual(stockAnterior + cantidadARecuperar);
                ingredienteRepository.save(ingrediente);

                // Actualizar el inventario
                double inventarioAnterior = inventario.getCantidadActual();
                inventario.setCantidadActual(inventarioAnterior + cantidadARecuperar);
                inventarioRepository.save(inventario);

                // Registrar el movimiento de entrada
                MovimientoInventario movimientoEntrada = new MovimientoInventario();
                movimientoEntrada.setInventarioId(inventario.get_id());
                movimientoEntrada.setProductoId(ingredienteId);
                movimientoEntrada.setProductoNombre(inventario.getProductoNombre());
                movimientoEntrada.setTipoMovimiento("entrada");
                movimientoEntrada.setCantidadAnterior(inventarioAnterior);
                movimientoEntrada.setCantidadMovimiento(cantidadARecuperar);
                movimientoEntrada.setCantidadNueva(inventario.getCantidadActual());
                movimientoEntrada.setMotivo("Recuperación por cancelación de pedido");
                movimientoEntrada.setReferencia(pedidoId);
                movimientoEntrada.setResponsable(usuarioId);
                movimientoEntrada.setFecha(LocalDateTime.now());
                movimientoEntrada.setObservaciones("Recuperación automática de inventario por cancelación del pedido: " + pedidoId);

                movimientoInventarioRepository.save(movimientoEntrada);

                // Agregar información del ingrediente recuperado al resultado
                Map<String, Object> infoIngrediente = new HashMap<>();
                infoIngrediente.put("ingredienteId", ingredienteId);
                infoIngrediente.put("nombre", inventario.getProductoNombre());
                infoIngrediente.put("cantidadRecuperada", cantidadARecuperar);
                infoIngrediente.put("unidad", inventario.getUnidadMedida());
                infoIngrediente.put("stockAnterior", inventarioAnterior);
                infoIngrediente.put("stockNuevo", inventario.getCantidadActual());
                ingredientesRecuperados.add(infoIngrediente);

                totalIngredientesRecuperados++;

                System.out.println("✅ Recuperado " + cantidadARecuperar + " " + inventario.getUnidadMedida()
                        + " de " + inventario.getProductoNombre());
            }

            // 4. Preparar respuesta
            resultado.put("exito", true);
            resultado.put("mensaje", "Se recuperaron " + totalIngredientesRecuperados + " ingredientes correctamente");
            resultado.put("cantidadIngredientes", totalIngredientesRecuperados);
            resultado.put("ingredientes", ingredientesRecuperados);

            System.out.println("✅ Proceso de recuperación completado exitosamente para el pedido: " + pedidoId);

        } catch (Exception e) {
            System.err.println("❌ Error al recuperar inventario: " + e.getMessage());
            e.printStackTrace();
            resultado.put("exito", false);
            resultado.put("mensaje", "Error al recuperar inventario: " + e.getMessage());
        }

        return resultado;
    }

    /**
     * Recupera ingredientes específicos para un producto
     *
     * @param productoId ID del producto
     * @param cantidad Cantidad del producto
     * @param ingredientesSeleccionados Lista de ingredientes opcionales
     * seleccionados (solo para combos)
     * @param usuarioId ID del usuario que realiza la operación
     * @param pedidoId ID del pedido como referencia
     * @return Resultado del proceso
     */
    public Map<String, Object> recuperarIngredientesProducto(
            String productoId,
            int cantidad,
            List<String> ingredientesSeleccionados,
            String usuarioId,
            String pedidoId) {

        Map<String, Object> resultado = new HashMap<>();
        List<Map<String, Object>> ingredientesRecuperados = new ArrayList<>();

        try {
            System.out.println("🔄 Iniciando recuperación para producto: " + productoId + " x " + cantidad);

            // 1. Obtener el producto
            Optional<Producto> productoOpt = productoRepository.findById(productoId);
            if (!productoOpt.isPresent()) {
                System.out.println("❌ Producto no encontrado: " + productoId);
                resultado.put("exito", false);
                resultado.put("mensaje", "Producto no encontrado: " + productoId);
                return resultado;
            }

            Producto producto = productoOpt.get();

            // 2. Verificar si el producto tiene ingredientes
            if (!producto.isTieneIngredientes()) {
                System.out.println("ℹ️ El producto no maneja ingredientes: " + producto.getNombre());
                resultado.put("exito", true);
                resultado.put("mensaje", "El producto no maneja ingredientes");
                return resultado;
            }

            // 3. Procesar ingredientes requeridos
            if (producto.getIngredientesRequeridos() != null) {
                for (IngredienteProducto ingredienteReq : producto.getIngredientesRequeridos()) {
                    double cantidadTotal = ingredienteReq.getCantidadNecesaria() * cantidad;

                    Map<String, Object> infoIngrediente = recuperarIngrediente(
                            ingredienteReq.getIngredienteId(),
                            cantidadTotal,
                            "Recuperación por cancelación - " + producto.getNombre(),
                            usuarioId,
                            pedidoId
                    );

                    if (infoIngrediente != null) {
                        ingredientesRecuperados.add(infoIngrediente);
                    }
                }
            }

            // 4. Procesar ingredientes opcionales según el tipo de producto
            if (producto.esCombo()) {
                // Combos: solo recuperar los ingredientes seleccionados
                if (producto.getIngredientesOpcionales() != null && ingredientesSeleccionados != null) {
                    System.out.println("🔸 Procesando recuperación de ingredientes opcionales para combo");
                    for (IngredienteProducto ingredienteOpc : producto.getIngredientesOpcionales()) {
                        if (ingredientesSeleccionados.contains(ingredienteOpc.getIngredienteId())) {
                            double cantidadTotal = ingredienteOpc.getCantidadNecesaria() * cantidad;

                            Map<String, Object> infoIngrediente = recuperarIngrediente(
                                    ingredienteOpc.getIngredienteId(),
                                    cantidadTotal,
                                    "Recuperación por cancelación (selección de combo) - " + producto.getNombre(),
                                    usuarioId,
                                    pedidoId
                            );

                            if (infoIngrediente != null) {
                                ingredientesRecuperados.add(infoIngrediente);
                            }
                        }
                    }
                }
            } else if (producto.esIndividual()) {
                // Productos individuales: recuperar todos los ingredientes opcionales
                if (producto.getIngredientesOpcionales() != null) {
                    System.out.println("🔹 Procesando recuperación de ingredientes opcionales para producto individual");
                    for (IngredienteProducto ingredienteOpc : producto.getIngredientesOpcionales()) {
                        double cantidadTotal = ingredienteOpc.getCantidadNecesaria() * cantidad;

                        Map<String, Object> infoIngrediente = recuperarIngrediente(
                                ingredienteOpc.getIngredienteId(),
                                cantidadTotal,
                                "Recuperación por cancelación (producto individual) - " + producto.getNombre(),
                                usuarioId,
                                pedidoId
                        );

                        if (infoIngrediente != null) {
                            ingredientesRecuperados.add(infoIngrediente);
                        }
                    }
                }
            }

            // 5. Preparar resultado
            resultado.put("exito", true);
            resultado.put("mensaje", "Ingredientes recuperados correctamente: " + ingredientesRecuperados.size());
            resultado.put("producto", producto.getNombre());
            resultado.put("ingredientes", ingredientesRecuperados);

        } catch (Exception e) {
            System.err.println("❌ Error al recuperar ingredientes del producto: " + e.getMessage());
            e.printStackTrace();
            resultado.put("exito", false);
            resultado.put("mensaje", "Error al recuperar ingredientes: " + e.getMessage());
        }

        return resultado;
    }

    /**
     * Método auxiliar para recuperar un ingrediente específico
     */
    private Map<String, Object> recuperarIngrediente(
            String ingredienteId,
            double cantidad,
            String motivo,
            String usuarioId,
            String pedidoId) {

        try {
            // 1. Verificar que el ingrediente existe
            Optional<Ingrediente> ingredienteOpt = ingredienteRepository.findById(ingredienteId);
            if (!ingredienteOpt.isPresent()) {
                System.out.println("⚠️ Ingrediente no encontrado: " + ingredienteId);
                return null;
            }

            Ingrediente ingrediente = ingredienteOpt.get();

            // 2. Buscar el inventario de este ingrediente
            Inventario inventario = inventarioRepository.findByProductoId(ingredienteId);
            if (inventario == null) {
                System.out.println("⚠️ No hay registro de inventario para el ingrediente: " + ingrediente.getNombre());

                // Crear un registro de inventario
                inventario = new Inventario();
                inventario.setProductoId(ingredienteId);
                inventario.setProductoNombre(ingrediente.getNombre());
                inventario.setCategoria("Ingrediente");
                inventario.setCantidadActual(ingrediente.getStockActual());
                inventario.setCantidadMinima(ingrediente.getStockMinimo());
                inventario.setUnidadMedida(ingrediente.getUnidad());
                inventario.setCostoUnitario(0.0);
                inventario.setEstado("activo");
                inventario = inventarioRepository.save(inventario);
            }

            // 3. Actualizar el stock
            double stockIngredienteAnterior = ingrediente.getStockActual();
            double stockInventarioAnterior = inventario.getCantidadActual();

            // Actualizar ambos (ingrediente e inventario)
            ingrediente.setStockActual(stockIngredienteAnterior + cantidad);
            ingredienteRepository.save(ingrediente);

            inventario.setCantidadActual(stockInventarioAnterior + cantidad);
            inventarioRepository.save(inventario);

            // 4. Registrar el movimiento
            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setInventarioId(inventario.get_id());
            movimiento.setProductoId(ingredienteId);
            movimiento.setProductoNombre(inventario.getProductoNombre());
            movimiento.setTipoMovimiento("entrada");
            movimiento.setCantidadAnterior(stockInventarioAnterior);
            movimiento.setCantidadMovimiento(cantidad);
            movimiento.setCantidadNueva(inventario.getCantidadActual());
            movimiento.setMotivo(motivo);
            movimiento.setReferencia(pedidoId);
            movimiento.setResponsable(usuarioId);
            movimiento.setFecha(LocalDateTime.now());

            movimientoInventarioRepository.save(movimiento);

            System.out.println("✅ Recuperado " + cantidad + " " + inventario.getUnidadMedida()
                    + " de " + inventario.getProductoNombre());

            // 5. Devolver información del ingrediente recuperado
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("ingredienteId", ingredienteId);
            resultado.put("nombre", inventario.getProductoNombre());
            resultado.put("cantidadRecuperada", cantidad);
            resultado.put("unidad", inventario.getUnidadMedida());
            resultado.put("stockAnterior", stockInventarioAnterior);
            resultado.put("stockNuevo", inventario.getCantidadActual());
            resultado.put("movimientoId", movimiento.get_id());

            return resultado;

        } catch (Exception e) {
            System.err.println("❌ Error al recuperar ingrediente " + ingredienteId + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
