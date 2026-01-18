package com.prog3.security.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * Modelo de Cliente para el sistema de facturación electrónica
 * Compatible con requisitos DIAN y gestión contable
 */
@Document(collection = "clientes")
public class Cliente {

    @Id
    @JsonProperty("_id")
    private String _id;
    
    // 📋 TIPO DE PERSONA E IDENTIFICACIÓN
    private String tipoPersona;                     // "Persona Natural" o "Persona Jurídica"
    private String tipoIdentificacion;              // "CC", "NIT", "CE", "Pasaporte", "TI", etc.
    
    @Indexed(unique = true)
    private String numeroIdentificacion;            // Número de documento
    private String digitoVerificacion;              // DV para NIT (opcional)
    
    // 👤 DATOS PERSONALES
    private String nombres;                         // Nombres (requerido)
    private String apellidos;                       // Apellidos (opcional para persona jurídica)
    private String razonSocial;                     // Nombre completo o razón social
    private String correo;                          // Email
    private String telefono;                        // Teléfono principal
    private String telefonoSecundario;              // Teléfono adicional
    private String direccion;                       // Dirección física
    private String departamento;                    // Departamento
    private String ciudad;                          // Ciudad/Municipio
    private String codigoPostal;                    // Código postal
    
    // 💼 DATOS CONTRIBUYENTE (Facturación Electrónica DIAN)
    private String responsableIVA;                  // "Sí", "No", "No Aplica"
    private String calidadAgenteRetencion;          // "Autorretenedor", "Agente de retención", "No aplica"
    private String regimenTributario;               // "Común", "Simplificado", "No responsable"
    private String responsabilidadesFiscales;       // Códigos de responsabilidades (ej: "O-13, O-15")
    
    // 📊 CUENTAS CONTABLES
    private String cuentasPorCobrar;                // Código cuenta contable
    private String cuentasParaDevoluciones;         // Código cuenta contable
    private String sobreabonos;                     // Código cuenta contable
    private String deterioroCartera;                // Código cuenta contable
    
    // 💳 INFORMACIÓN COMERCIAL
    private String condicionPago;                   // "Contado", "Crédito 30 días", etc.
    private Integer diasCredito;                    // Días de crédito
    private Double cupoCredito;                     // Cupo de crédito aprobado
    private Double saldoActual;                     // Saldo pendiente actual
    private String categoriaCliente;                // "VIP", "Regular", "Nuevo", etc.
    
    // 🏢 INFORMACIÓN ADICIONAL
    private String nombreContacto;                  // Persona de contacto
    private String cargoContacto;                   // Cargo del contacto
    private String observaciones;                   // Notas generales
    private String vendedorAsignado;                // ID del vendedor asignado
    private String zonaVentas;                      // Zona geográfica
    
    // 📅 TRACKING Y ESTADO
    private LocalDateTime fechaCreacion;            // Cuándo se creó el cliente
    private LocalDateTime fechaModificacion;        // Última modificación
    private String creadoPor;                       // Usuario que creó
    private String modificadoPor;                   // Usuario que modificó
    private String estado;                          // "activo", "inactivo", "bloqueado"
    private Boolean habilitadoFacturacionElectronica; // Si puede recibir factura electrónica
    
    // 🏗️ CONSTRUCTOR
    public Cliente() {
        this.fechaCreacion = LocalDateTime.now();
        this.estado = "activo";
        this.habilitadoFacturacionElectronica = true;
        this.responsableIVA = "No";
        this.calidadAgenteRetencion = "No aplica";
        this.regimenTributario = "Común";
        this.diasCredito = 0;
        this.cupoCredito = 0.0;
        this.saldoActual = 0.0;
    }
    
    // 📐 GETTERS Y SETTERS
    
    public String get_id() { return _id; }
    public void set_id(String _id) { this._id = _id; }
    
    public String getTipoPersona() { return tipoPersona; }
    public void setTipoPersona(String tipoPersona) { this.tipoPersona = tipoPersona; }
    
    public String getTipoIdentificacion() { return tipoIdentificacion; }
    public void setTipoIdentificacion(String tipoIdentificacion) { this.tipoIdentificacion = tipoIdentificacion; }
    
    public String getNumeroIdentificacion() { return numeroIdentificacion; }
    public void setNumeroIdentificacion(String numeroIdentificacion) { 
        this.numeroIdentificacion = numeroIdentificacion;
        // Generar razón social automática si no existe
        if (this.razonSocial == null || this.razonSocial.isEmpty()) {
            generarRazonSocial();
        }
    }
    
    public String getDigitoVerificacion() { return digitoVerificacion; }
    public void setDigitoVerificacion(String digitoVerificacion) { this.digitoVerificacion = digitoVerificacion; }
    
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { 
        this.nombres = nombres;
        generarRazonSocial();
    }
    
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { 
        this.apellidos = apellidos;
        generarRazonSocial();
    }
    
    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }
    
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    
    public String getTelefonoSecundario() { return telefonoSecundario; }
    public void setTelefonoSecundario(String telefonoSecundario) { this.telefonoSecundario = telefonoSecundario; }
    
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    
    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }
    
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    
    public String getCodigoPostal() { return codigoPostal; }
    public void setCodigoPostal(String codigoPostal) { this.codigoPostal = codigoPostal; }
    
    public String getResponsableIVA() { return responsableIVA; }
    public void setResponsableIVA(String responsableIVA) { this.responsableIVA = responsableIVA; }
    
    public String getCalidadAgenteRetencion() { return calidadAgenteRetencion; }
    public void setCalidadAgenteRetencion(String calidadAgenteRetencion) { this.calidadAgenteRetencion = calidadAgenteRetencion; }
    
    public String getRegimenTributario() { return regimenTributario; }
    public void setRegimenTributario(String regimenTributario) { this.regimenTributario = regimenTributario; }
    
    public String getResponsabilidadesFiscales() { return responsabilidadesFiscales; }
    public void setResponsabilidadesFiscales(String responsabilidadesFiscales) { this.responsabilidadesFiscales = responsabilidadesFiscales; }
    
    public String getCuentasPorCobrar() { return cuentasPorCobrar; }
    public void setCuentasPorCobrar(String cuentasPorCobrar) { this.cuentasPorCobrar = cuentasPorCobrar; }
    
    public String getCuentasParaDevoluciones() { return cuentasParaDevoluciones; }
    public void setCuentasParaDevoluciones(String cuentasParaDevoluciones) { this.cuentasParaDevoluciones = cuentasParaDevoluciones; }
    
    public String getSobreabonos() { return sobreabonos; }
    public void setSobreabonos(String sobreabonos) { this.sobreabonos = sobreabonos; }
    
    public String getDeterioroCartera() { return deterioroCartera; }
    public void setDeterioroCartera(String deterioroCartera) { this.deterioroCartera = deterioroCartera; }
    
    public String getCondicionPago() { return condicionPago; }
    public void setCondicionPago(String condicionPago) { this.condicionPago = condicionPago; }
    
    public Integer getDiasCredito() { return diasCredito; }
    public void setDiasCredito(Integer diasCredito) { this.diasCredito = diasCredito; }
    
    public Double getCupoCredito() { return cupoCredito; }
    public void setCupoCredito(Double cupoCredito) { this.cupoCredito = cupoCredito; }
    
    public Double getSaldoActual() { return saldoActual; }
    public void setSaldoActual(Double saldoActual) { this.saldoActual = saldoActual; }
    
    public String getCategoriaCliente() { return categoriaCliente; }
    public void setCategoriaCliente(String categoriaCliente) { this.categoriaCliente = categoriaCliente; }
    
    public String getNombreContacto() { return nombreContacto; }
    public void setNombreContacto(String nombreContacto) { this.nombreContacto = nombreContacto; }
    
    public String getCargoContacto() { return cargoContacto; }
    public void setCargoContacto(String cargoContacto) { this.cargoContacto = cargoContacto; }
    
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    
    public String getVendedorAsignado() { return vendedorAsignado; }
    public void setVendedorAsignado(String vendedorAsignado) { this.vendedorAsignado = vendedorAsignado; }
    
    public String getZonaVentas() { return zonaVentas; }
    public void setZonaVentas(String zonaVentas) { this.zonaVentas = zonaVentas; }
    
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    
    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDateTime fechaModificacion) { this.fechaModificacion = fechaModificacion; }
    
    public String getCreadoPor() { return creadoPor; }
    public void setCreadoPor(String creadoPor) { this.creadoPor = creadoPor; }
    
    public String getModificadoPor() { return modificadoPor; }
    public void setModificadoPor(String modificadoPor) { this.modificadoPor = modificadoPor; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public Boolean getHabilitadoFacturacionElectronica() { return habilitadoFacturacionElectronica; }
    public void setHabilitadoFacturacionElectronica(Boolean habilitadoFacturacionElectronica) { 
        this.habilitadoFacturacionElectronica = habilitadoFacturacionElectronica; 
    }
    
    // 🔧 MÉTODOS AUXILIARES
    
    /**
     * Genera automáticamente la razón social basada en nombres y apellidos
     * o mantiene el valor si ya está establecido manualmente
     */
    private void generarRazonSocial() {
        if (this.razonSocial != null && !this.razonSocial.isEmpty()) {
            return; // Si ya tiene razón social, no la sobrescribir
        }
        
        if ("Persona Natural".equals(this.tipoPersona)) {
            StringBuilder rs = new StringBuilder();
            if (this.nombres != null && !this.nombres.isEmpty()) {
                rs.append(this.nombres);
            }
            if (this.apellidos != null && !this.apellidos.isEmpty()) {
                if (rs.length() > 0) rs.append(" ");
                rs.append(this.apellidos);
            }
            this.razonSocial = rs.toString();
        }
    }
    
    /**
     * Calcula el cupo disponible de crédito
     */
    public Double getCupoDisponible() {
        return this.cupoCredito - this.saldoActual;
    }
    
    /**
     * Verifica si el cliente tiene cupo disponible
     */
    public Boolean tieneCupoDisponible(Double monto) {
        return getCupoDisponible() >= monto;
    }
    
    /**
     * Actualiza el saldo del cliente
     */
    public void actualizarSaldo(Double monto) {
        this.saldoActual += monto;
    }
    
    /**
     * Verifica si el cliente está activo
     */
    public Boolean estaActivo() {
        return "activo".equals(this.estado);
    }
    
    /**
     * Bloquear cliente
     */
    public void bloquear(String usuarioId, String motivo) {
        this.estado = "bloqueado";
        this.modificadoPor = usuarioId;
        this.fechaModificacion = LocalDateTime.now();
        if (this.observaciones == null) {
            this.observaciones = "";
        }
        this.observaciones += "\n[BLOQUEADO] " + LocalDateTime.now() + " - " + motivo;
    }
    
    /**
     * Activar cliente
     */
    public void activar(String usuarioId) {
        this.estado = "activo";
        this.modificadoPor = usuarioId;
        this.fechaModificacion = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return String.format("Cliente{id='%s', documento='%s', razonSocial='%s', estado='%s'}", 
                           _id, numeroIdentificacion, razonSocial, estado);
    }
}
