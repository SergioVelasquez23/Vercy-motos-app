package com.prog3.security.Services;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.prog3.security.Models.Producto;
import com.prog3.security.Models.Ingrediente;
import com.prog3.security.Models.ProductoPedido;
import com.prog3.security.Models.ProductoPedido.IngredienteSeleccionado;
import com.prog3.security.Repositories.ProductoRepository;
import com.prog3.security.Repositories.IngredienteRepository;

@Service
public class ProductoPedidoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private IngredienteRepository ingredienteRepository;

    /**
     * Valida que los ingredientes seleccionados sean válidos para el producto
     */
    public boolean validarIngredientesSeleccionados(String productoId, List<String> ingredientesSeleccionados) {
        if (ingredientesSeleccionados == null || ingredientesSeleccionados.isEmpty()) {
            return true; // No hay ingredientes seleccionados, es válido
        }

        Producto producto = productoRepository.findById(productoId).orElse(null);
        if (producto == null) {
            return false;
        }

        List<String> ingredientesDisponibles = producto.getIngredientesDisponibles();
        if (ingredientesDisponibles == null || ingredientesDisponibles.isEmpty()) {
            return ingredientesSeleccionados.isEmpty(); // Si el producto no tiene ingredientes configurados, no debería tener selecciones
        }

        // Verificar que todos los ingredientes seleccionados estén en la lista de disponibles
        return ingredientesDisponibles.containsAll(ingredientesSeleccionados);
    }

    /**
     * Crea un ProductoPedido con los ingredientes seleccionados
     */
    public ProductoPedido crearProductoPedido(String productoId, int cantidad, List<String> ingredientesSeleccionados, String notas) {
        Producto producto = productoRepository.findById(productoId).orElse(null);
        if (producto == null) {
            throw new RuntimeException("Producto no encontrado con ID: " + productoId);
        }

        ProductoPedido productoPedido = new ProductoPedido(
                productoId,
                producto.getNombre(),
                cantidad,
                producto.getPrecio()
        );

        productoPedido.setNotas(notas);
        productoPedido.setIngredientesSeleccionados(ingredientesSeleccionados != null ? ingredientesSeleccionados : new ArrayList<>());

        // Obtener detalles de los ingredientes seleccionados
        if (ingredientesSeleccionados != null && !ingredientesSeleccionados.isEmpty()) {
            List<Ingrediente> ingredientes = ingredienteRepository.findAllById(ingredientesSeleccionados);
            List<IngredienteSeleccionado> detalleIngredientes = ingredientes.stream()
                    .map(ing -> new IngredienteSeleccionado(
                    ing.get_id(),
                    ing.getNombre(),
                    ing.getCategoriaId(),
                    1.0, // Cantidad por defecto, puedes ajustar según necesites
                    ing.getUnidad()
            ))
                    .collect(Collectors.toList());

            productoPedido.setDetalleIngredientes(detalleIngredientes);
        }

        return productoPedido;
    }

    /**
     * Procesa el descuento de stock de los ingredientes utilizados
     */
    public void procesarDescontarIngredientes(ProductoPedido productoPedido) {
        if (productoPedido.getDetalleIngredientes() == null || productoPedido.getDetalleIngredientes().isEmpty()) {
            return;
        }

        for (IngredienteSeleccionado ingredienteSelec : productoPedido.getDetalleIngredientes()) {
            Ingrediente ingrediente = ingredienteRepository.findById(ingredienteSelec.getIngredienteId()).orElse(null);
            if (ingrediente != null) {
                // Calcular cantidad total a descontar (cantidad del ingrediente * cantidad del producto)
                double cantidadADescontar = ingredienteSelec.getCantidadUsada() * productoPedido.getCantidad();

                // Verificar que hay suficiente stock
                if (ingrediente.getStockActual() >= cantidadADescontar) {
                    ingrediente.setStockActual(ingrediente.getStockActual() - cantidadADescontar);
                    ingredienteRepository.save(ingrediente);
                } else {
                    throw new RuntimeException("Stock insuficiente para el ingrediente: " + ingrediente.getNombre());
                }
            }
        }
    }

    /**
     * Obtiene los ingredientes disponibles para un producto específico
     */
    public List<Ingrediente> getIngredientesDisponiblesParaProducto(String productoId) {
        Producto producto = productoRepository.findById(productoId).orElse(null);
        if (producto == null || producto.getIngredientesDisponibles() == null || producto.getIngredientesDisponibles().isEmpty()) {
            return new ArrayList<>();
        }

        return ingredienteRepository.findAllById(producto.getIngredientesDisponibles());
    }
}
