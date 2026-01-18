package com.prog3.security.Models;

import java.util.List;
import java.util.ArrayList;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Producto {

    @Id
    private String _id;

    // ==================== INFORMACIÓN BÁSICA ====================
    private String codigo; // Código único del producto (CODIGO*)
    private String nombre; // Nombre del producto (NOMBRE DEL PRODUCTO*)
    private String descripcion; // Descripción del producto
    private String unidadMedida; // Unidad de medida (unidad, kg, litro, etc.)
    private String codigoBarras; // Código de barras (CÓDIGO DE BARRAS)
    private String codigoInterno; // Código interno del negocio

    // ==================== PRECIOS ====================
    private double precio; // Precio de venta principal (PRECIO VENTA PRINCIPAL*)
    private double costo; // Costo unitario (COSTO UNITARIO*)
    private double impuestos; // % Impuesto/IVA (% IMPUESTO)
    private double precioConIva; // Precio + IVA calculado
    private double utilidad; // Utilidad calculada
    private List<Double> preciosOpcionales; // Precios de venta opcionales (OPC 1-5)

    // ==================== CLASIFICACIÓN ====================
    private String tipoItem; // "PRODUCTO" o "SERVICIO" (PRODUCTO O SERVICIO*)
    private String categoriaId; // ID de la categoría
    private String tipoProductoNombre; // TIPO PRODUCTO (NOMBRE) - ej: "ACCESORIO"
    private String lineaProducto; // LINEA PRODUCTO (NOMBRE)
    private String claseProducto; // CLASE PRODUCTO (NOMBRE)
    private String marca; // MARCA

    // ==================== INVENTARIO ====================
    private boolean controlInventario; // CONTROL DE INVENTARIO (Sí/No)
    private int cantidad; // Cantidad actual total
    private int stockMinimo; // INVENTARIO BAJO - Stock mínimo
    private int stockOptimo; // INVENTARIO ÓPTIMO - Stock óptimo
    private int cantidadAlmacen; // Cantidad en ALMACEN
    private int cantidadBodega; // Cantidad en BODEGA

    // ==================== UBICACIÓN ====================
    private String localizacion; // LOCALIZACIÓN principal
    private String ubicacion3; // UBICACIÓN 3
    private String ubicacion4; // UBICACIÓN 4
    private String localizacionUbi1; // LOCALIZACION UBI 1
    private String localizacionUbi2; // LOCALIZACION UBI 2
    private String localizacionUbi3; // LOCALIZACION UBI 3
    private String localizacionUbi4; // LOCALIZACION UBI 4

    // ==================== PROVEEDOR ====================
    private String proveedorNombre; // NOMBRE PROVEEDOR
    private String proveedorNit; // NIT PROVEEDOR (SIN DV)

    // ==================== OTROS ====================
    private String estado; // Activo/Inactivo
    private String imagenUrl; // URL de imagen
    private String nota; // Notas adicionales
    private boolean tieneVariantes; // Si tiene variantes

    // ==================== INGREDIENTES (para restaurantes) ====================
    private List<String> ingredientesDisponibles;
    private boolean tieneIngredientes;
    private String tipoProducto; // "combo" o "individual"
    private List<IngredienteProducto> ingredientesRequeridos;
    private List<IngredienteProducto> ingredientesOpcionales;

    public Producto() {
        this.estado = "Activo";
        this.tieneVariantes = false;
        this.cantidad = 0;
        this.impuestos = 0.0;
        this.imagenUrl = "";
        this.categoriaId = "";
        this.descripcion = "";
        this.nota = "";
        this.ingredientesDisponibles = new ArrayList<>();
        this.tieneIngredientes = false;
        this.tipoProducto = "individual";
        this.ingredientesRequeridos = new ArrayList<>();
        this.ingredientesOpcionales = new ArrayList<>();
        this.tipoItem = "PRODUCTO";
        this.controlInventario = true;
        this.stockMinimo = 0;
        this.stockOptimo = 0;
        this.cantidadAlmacen = 0;
        this.cantidadBodega = 0;
        this.preciosOpcionales = new ArrayList<>();
    }

    public Producto(String nombre, double precio, double costo, double utilidad) {
        this();
        this.nombre = nombre;
        this.precio = precio;
        this.costo = costo;
        this.utilidad = utilidad;
    }

    // ==================== GETTERS Y SETTERS ====================

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public String getCodigoInterno() {
        return codigoInterno;
    }

    public void setCodigoInterno(String codigoInterno) {
        this.codigoInterno = codigoInterno;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public double getCosto() {
        return costo;
    }

    public void setCosto(double costo) {
        this.costo = costo;
    }

    public double getImpuestos() {
        return impuestos;
    }

    public void setImpuestos(double impuestos) {
        this.impuestos = impuestos;
    }

    public double getPrecioConIva() {
        return precioConIva;
    }

    public void setPrecioConIva(double precioConIva) {
        this.precioConIva = precioConIva;
    }

    public double getUtilidad() {
        return utilidad;
    }

    public void setUtilidad(double utilidad) {
        this.utilidad = utilidad;
    }

    public List<Double> getPreciosOpcionales() {
        return preciosOpcionales;
    }

    public void setPreciosOpcionales(List<Double> preciosOpcionales) {
        this.preciosOpcionales = preciosOpcionales;
    }

    public String getTipoItem() {
        return tipoItem;
    }

    public void setTipoItem(String tipoItem) {
        this.tipoItem = tipoItem;
    }

    public String getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(String categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getTipoProductoNombre() {
        return tipoProductoNombre;
    }

    public void setTipoProductoNombre(String tipoProductoNombre) {
        this.tipoProductoNombre = tipoProductoNombre;
    }

    public String getLineaProducto() {
        return lineaProducto;
    }

    public void setLineaProducto(String lineaProducto) {
        this.lineaProducto = lineaProducto;
    }

    public String getClaseProducto() {
        return claseProducto;
    }

    public void setClaseProducto(String claseProducto) {
        this.claseProducto = claseProducto;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public boolean isControlInventario() {
        return controlInventario;
    }

    public void setControlInventario(boolean controlInventario) {
        this.controlInventario = controlInventario;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public int getStockOptimo() {
        return stockOptimo;
    }

    public void setStockOptimo(int stockOptimo) {
        this.stockOptimo = stockOptimo;
    }

    public int getCantidadAlmacen() {
        return cantidadAlmacen;
    }

    public void setCantidadAlmacen(int cantidadAlmacen) {
        this.cantidadAlmacen = cantidadAlmacen;
    }

    public int getCantidadBodega() {
        return cantidadBodega;
    }

    public void setCantidadBodega(int cantidadBodega) {
        this.cantidadBodega = cantidadBodega;
    }

    public String getLocalizacion() {
        return localizacion;
    }

    public void setLocalizacion(String localizacion) {
        this.localizacion = localizacion;
    }

    public String getUbicacion3() {
        return ubicacion3;
    }

    public void setUbicacion3(String ubicacion3) {
        this.ubicacion3 = ubicacion3;
    }

    public String getUbicacion4() {
        return ubicacion4;
    }

    public void setUbicacion4(String ubicacion4) {
        this.ubicacion4 = ubicacion4;
    }

    public String getLocalizacionUbi1() {
        return localizacionUbi1;
    }

    public void setLocalizacionUbi1(String localizacionUbi1) {
        this.localizacionUbi1 = localizacionUbi1;
    }

    public String getLocalizacionUbi2() {
        return localizacionUbi2;
    }

    public void setLocalizacionUbi2(String localizacionUbi2) {
        this.localizacionUbi2 = localizacionUbi2;
    }

    public String getLocalizacionUbi3() {
        return localizacionUbi3;
    }

    public void setLocalizacionUbi3(String localizacionUbi3) {
        this.localizacionUbi3 = localizacionUbi3;
    }

    public String getLocalizacionUbi4() {
        return localizacionUbi4;
    }

    public void setLocalizacionUbi4(String localizacionUbi4) {
        this.localizacionUbi4 = localizacionUbi4;
    }

    public String getProveedorNombre() {
        return proveedorNombre;
    }

    public void setProveedorNombre(String proveedorNombre) {
        this.proveedorNombre = proveedorNombre;
    }

    public String getProveedorNit() {
        return proveedorNit;
    }

    public void setProveedorNit(String proveedorNit) {
        this.proveedorNit = proveedorNit;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public String getNota() {
        return nota;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }

    public boolean isTieneVariantes() {
        return tieneVariantes;
    }

    public void setTieneVariantes(boolean tieneVariantes) {
        this.tieneVariantes = tieneVariantes;
    }

    public List<String> getIngredientesDisponibles() {
        return ingredientesDisponibles;
    }

    public void setIngredientesDisponibles(List<String> ingredientesDisponibles) {
        this.ingredientesDisponibles = ingredientesDisponibles;
    }

    public boolean isTieneIngredientes() {
        return tieneIngredientes;
    }

    public void setTieneIngredientes(boolean tieneIngredientes) {
        this.tieneIngredientes = tieneIngredientes;
    }

    public String getTipoProducto() {
        return tipoProducto;
    }

    public void setTipoProducto(String tipoProducto) {
        this.tipoProducto = tipoProducto;
    }

    public boolean esCombo() {
        return "combo".equals(this.tipoProducto);
    }

    public boolean esIndividual() {
        return "individual".equals(this.tipoProducto);
    }

    public List<IngredienteProducto> getIngredientesRequeridos() {
        return ingredientesRequeridos;
    }

    public void setIngredientesRequeridos(List<IngredienteProducto> ingredientesRequeridos) {
        this.ingredientesRequeridos = ingredientesRequeridos;
    }

    public List<IngredienteProducto> getIngredientesOpcionales() {
        return ingredientesOpcionales;
    }

    public void setIngredientesOpcionales(List<IngredienteProducto> ingredientesOpcionales) {
        this.ingredientesOpcionales = ingredientesOpcionales;
    }

    // Método para calcular precio con IVA
    public void calcularPrecioConIva() {
        this.precioConIva = this.precio + (this.precio * this.impuestos / 100);
    }

    // Método para calcular utilidad
    public void calcularUtilidad() {
        this.utilidad = this.precio - this.costo;
    }

    // Método para calcular cantidad total
    public int getCantidadTotal() {
        return this.cantidadAlmacen + this.cantidadBodega;
    }
}
