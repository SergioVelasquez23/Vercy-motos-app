package com.prog3.security.Controllers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prog3.security.Models.Factura;
import com.prog3.security.Models.ItemFacturaIngrediente;
import com.prog3.security.Models.Ingrediente;
import com.prog3.security.Repositories.IngredienteRepository;
import com.prog3.security.Services.FacturaComprasService;

/**
 * Controlador de prueba para verificar la funcionalidad de facturas de compras
 */
@CrossOrigin
@RestController
@RequestMapping("api/test-compras")
public class TestComprasController {

    @Autowired
    private FacturaComprasService facturaComprasService;

    @Autowired
    private IngredienteRepository ingredienteRepository;

    /**
     * Crear un ingrediente de prueba
     */
    @PostMapping("/crear-ingrediente-prueba")
    public ResponseEntity<Map<String, Object>> crearIngredientePrueba() {
        try {
            Ingrediente ingrediente = new Ingrediente();
            ingrediente.setNombre("Carne de Res - Prueba");
            ingrediente.setUnidad("kg");
            ingrediente.setStockActual(0.0);
            ingrediente.setStockMinimo(5.0);
            ingrediente.setCosto(10000.0); // Agregar costo
            ingrediente.setDescontable(true); // Se puede descontar del stock

            Ingrediente ingredienteGuardado = ingredienteRepository.save(ingrediente);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("ingrediente", ingredienteGuardado);
            response.put("message", "Ingrediente de prueba creado");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Crear una factura de compras de prueba
     */
    @PostMapping("/crear-factura-prueba")
    public ResponseEntity<Map<String, Object>> crearFacturaPrueba() {
        try {
            // Buscar o crear un ingrediente de prueba
            List<Ingrediente> ingredientes = ingredienteRepository.findByNombreContainingIgnoreCase("prueba");

            if (ingredientes.isEmpty()) {
                // Crear ingrediente si no existe
                Ingrediente ingrediente = new Ingrediente();
                ingrediente.setNombre("Carne de Res - Prueba");
                ingrediente.setUnidad("kg");
                ingrediente.setStockActual(0.0);
                ingrediente.setStockMinimo(5.0);
                ingrediente.setCosto(10000.0); // Agregar costo
                ingrediente.setDescontable(true);
                ingredientes.add(ingredienteRepository.save(ingrediente));
            }

            Ingrediente ingrediente = ingredientes.get(0);

            // Crear la factura
            Factura factura = new Factura();
            factura.setNumero(facturaComprasService.generarNumeroFactura());
            factura.setProveedorNit("900123456-7");
            factura.setProveedorNombre("Proveedor de Carnes SAC");
            factura.setProveedorTelefono("3001234567");
            factura.setProveedorDireccion("Calle 123 #45-67");
            factura.setFecha(LocalDateTime.now());
            factura.setRegistradoPor("admin-test");
            factura.setObservaciones("Factura de prueba del sistema");

            // Crear item de ingrediente
            ItemFacturaIngrediente item = new ItemFacturaIngrediente();
            item.setIngredienteId(ingrediente.get_id());
            item.setIngredienteNombre(ingrediente.getNombre());
            item.setCantidad(10.0); // 10 kg
            item.setUnidad(ingrediente.getUnidad());
            item.setPrecioUnitario(25000.0); // $25,000 por kg
            item.setDescontable(true); // Se aumentará el stock
            item.setObservaciones("Compra de prueba - stock inicial");
            item.calcularPrecioTotal();

            // Agregar item a la factura
            factura.agregarItemIngrediente(item);

            // Procesar la factura (esto actualizará el stock automáticamente)
            Factura facturaGuardada = facturaComprasService.procesarFacturaCompras(factura);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("factura", facturaGuardada);
            response.put("message", "Factura de prueba creada y procesada");
            response.put("stockAnterior", 0.0);
            response.put("stockNuevo", ingrediente.getStockActual());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Verificar el estado actual del sistema
     */
    @GetMapping("/estado")
    public ResponseEntity<Map<String, Object>> verificarEstado() {
        try {
            Map<String, Object> estado = new HashMap<>();

            // Contar ingredientes
            List<Ingrediente> todosIngredientes = ingredienteRepository.findAll();
            List<Ingrediente> ingredientesDescontables = new ArrayList<>();
            List<Ingrediente> ingredientesConStock = new ArrayList<>();

            for (Ingrediente ing : todosIngredientes) {
                if (ing.isDescontable()) {
                    ingredientesDescontables.add(ing);
                }
                if (ing.getStockActual() != null && ing.getStockActual() > 0) {
                    ingredientesConStock.add(ing);
                }
            }

            // Contar facturas
            List<Factura> todasFacturas = facturaComprasService.obtenerTodasLasFacturas();

            estado.put("totalIngredientes", todosIngredientes.size());
            estado.put("ingredientesDescontables", ingredientesDescontables.size());
            estado.put("ingredientesConStock", ingredientesConStock.size());
            estado.put("totalFacturas", todasFacturas.size());

            // Mostrar algunos ingredientes con stock
            List<Map<String, Object>> stockInfo = new ArrayList<>();
            for (Ingrediente ing : ingredientesConStock) {
                Map<String, Object> info = new HashMap<>();
                info.put("nombre", ing.getNombre());
                info.put("stock", ing.getStockActual());
                info.put("unidad", ing.getUnidad());
                info.put("descontable", ing.isDescontable());
                stockInfo.add(info);
            }
            estado.put("stockDetails", stockInfo);

            return ResponseEntity.ok(estado);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
