package com.prog3.security.DTOs;

import java.util.List;

public class NegocioInfoRequest {

    private String nombre;
    private String nit;
    private String contacto;
    private String email;
    private String direccion;
    private String pais;
    private String departamento;
    private String ciudad;
    private String telefono;
    private String paginaWeb;
    private boolean productosConIngredientes;
    private boolean utilizoMesas;
    private boolean envioADomicilio;
    private List<Integer> costosEnvio;
    private String tipoDocumento;
    private String prefijo;
    private Integer numeroInicio;
    private Integer porcentajePropinaSugerida;
    private String nombreDocumento;
    private String nota1;
    private String nota2;
    private String logoUrl;

    // Constructor vacío
    public NegocioInfoRequest() {
    }

    // Getters y setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getPaginaWeb() {
        return paginaWeb;
    }

    public void setPaginaWeb(String paginaWeb) {
        this.paginaWeb = paginaWeb;
    }

    public boolean isProductosConIngredientes() {
        return productosConIngredientes;
    }

    public void setProductosConIngredientes(boolean productosConIngredientes) {
        this.productosConIngredientes = productosConIngredientes;
    }

    public boolean isUtilizoMesas() {
        return utilizoMesas;
    }

    public void setUtilizoMesas(boolean utilizoMesas) {
        this.utilizoMesas = utilizoMesas;
    }

    public boolean isEnvioADomicilio() {
        return envioADomicilio;
    }

    public void setEnvioADomicilio(boolean envioADomicilio) {
        this.envioADomicilio = envioADomicilio;
    }

    public List<Integer> getCostosEnvio() {
        return costosEnvio;
    }

    public void setCostosEnvio(List<Integer> costosEnvio) {
        this.costosEnvio = costosEnvio;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getPrefijo() {
        return prefijo;
    }

    public void setPrefijo(String prefijo) {
        this.prefijo = prefijo;
    }

    public Integer getNumeroInicio() {
        return numeroInicio;
    }

    public void setNumeroInicio(Integer numeroInicio) {
        this.numeroInicio = numeroInicio;
    }

    public Integer getPorcentajePropinaSugerida() {
        return porcentajePropinaSugerida;
    }

    public void setPorcentajePropinaSugerida(Integer porcentajePropinaSugerida) {
        this.porcentajePropinaSugerida = porcentajePropinaSugerida;
    }

    public String getNombreDocumento() {
        return nombreDocumento;
    }

    public void setNombreDocumento(String nombreDocumento) {
        this.nombreDocumento = nombreDocumento;
    }

    public String getNota1() {
        return nota1;
    }

    public void setNota1(String nota1) {
        this.nota1 = nota1;
    }

    public String getNota2() {
        return nota2;
    }

    public void setNota2(String nota2) {
        this.nota2 = nota2;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }
}
