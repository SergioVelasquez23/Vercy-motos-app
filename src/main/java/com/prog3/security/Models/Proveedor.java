package com.prog3.security.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

/**
 * Modelo para representar un proveedor
 */
@Document(collection = "proveedores")
public class Proveedor {

    @Id
    private String _id;

    // Campos obligatorios
    private String nombre;              // Obligatorio
    private String telefono;            // Obligatorio

    // Campos opcionales
    private String nombreComercial;     // Opcional
    private String documento;           // Opcional (NIT, RUT, etc.)
    private String email;               // Opcional
    private String direccion;           // Opcional
    private String paginaWeb;           // Opcional
    private String contacto;            // Opcional (persona de contacto)
    private String nota;                // Opcional (observaciones)

    // Campos de auditoría
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private String creadoPor;
    private String actualizadoPor;

    // Campos de control
    private boolean activo;             // Para habilitar/deshabilitar proveedor

    // Constructor vacío
    public Proveedor() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        this.activo = true;
    }

    // Constructor con campos obligatorios
    public Proveedor(String nombre, String telefono) {
        this();
        this.nombre = nombre;
        this.telefono = telefono;
    }

    // Constructor completo
    public Proveedor(String nombre, String telefono, String nombreComercial,
            String documento, String email, String direccion,
            String paginaWeb, String contacto, String nota, String creadoPor) {
        this(nombre, telefono);
        this.nombreComercial = nombreComercial;
        this.documento = documento;
        this.email = email;
        this.direccion = direccion;
        this.paginaWeb = paginaWeb;
        this.contacto = contacto;
        this.nota = nota;
        this.creadoPor = creadoPor;
    }

    // Getters y Setters
    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getNombreComercial() {
        return nombreComercial;
    }

    public void setNombreComercial(String nombreComercial) {
        this.nombreComercial = nombreComercial;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
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

    public String getPaginaWeb() {
        return paginaWeb;
    }

    public void setPaginaWeb(String paginaWeb) {
        this.paginaWeb = paginaWeb;
    }

    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
    }

    public String getNota() {
        return nota;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public String getCreadoPor() {
        return creadoPor;
    }

    public void setCreadoPor(String creadoPor) {
        this.creadoPor = creadoPor;
    }

    public String getActualizadoPor() {
        return actualizadoPor;
    }

    public void setActualizadoPor(String actualizadoPor) {
        this.actualizadoPor = actualizadoPor;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    // Métodos de utilidad
    /**
     * Actualiza la fecha de modificación
     */
    public void actualizarFechaModificacion() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Obtiene el nombre para mostrar (comercial si existe, sino el nombre)
     */
    public String getNombreParaMostrar() {
        return (nombreComercial != null && !nombreComercial.trim().isEmpty())
                ? nombreComercial
                : nombre;
    }

    /**
     * Verifica si el proveedor tiene información de contacto completa
     */
    public boolean tieneContactoCompleto() {
        return nombre != null && !nombre.trim().isEmpty()
                && telefono != null && !telefono.trim().isEmpty();
    }

    /**
     * Obtiene información básica del proveedor para mostrar en listas
     */
    public String getInfoBasica() {
        StringBuilder info = new StringBuilder(getNombreParaMostrar());
        if (telefono != null && !telefono.trim().isEmpty()) {
            info.append(" - ").append(telefono);
        }
        if (documento != null && !documento.trim().isEmpty()) {
            info.append(" (").append(documento).append(")");
        }
        return info.toString();
    }

    @Override
    public String toString() {
        return "Proveedor{"
                + "_id='" + _id + '\''
                + ", nombre='" + nombre + '\''
                + ", nombreComercial='" + nombreComercial + '\''
                + ", telefono='" + telefono + '\''
                + ", documento='" + documento + '\''
                + ", email='" + email + '\''
                + ", activo=" + activo
                + '}';
    }
}
