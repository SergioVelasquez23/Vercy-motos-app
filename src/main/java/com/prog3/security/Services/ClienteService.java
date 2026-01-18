package com.prog3.security.Services;

import com.prog3.security.Models.Cliente;
import com.prog3.security.Repositories.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

/**
 * Servicio para gestionar la lógica de negocio de Clientes
 */
@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    /**
     * Obtener todos los clientes ordenados por fecha de creación
     */
    public List<Cliente> obtenerTodosLosClientes() {
        return clienteRepository.findAllByOrderByFechaCreacionDesc();
    }

    /**
     * Obtener cliente por ID
     */
    public Optional<Cliente> obtenerClientePorId(String id) {
        return clienteRepository.findById(id);
    }

    /**
     * Obtener cliente por número de identificación
     */
    public Optional<Cliente> obtenerClientePorDocumento(String numeroIdentificacion) {
        return clienteRepository.findByNumeroIdentificacion(numeroIdentificacion);
    }

    /**
     * Crear nuevo cliente
     */
    public Cliente crearCliente(Cliente cliente, String usuarioId) {
        // Validar que no exista el número de identificación
        if (clienteRepository.existsByNumeroIdentificacion(cliente.getNumeroIdentificacion())) {
            throw new RuntimeException("Ya existe un cliente con el número de identificación: " + 
                                     cliente.getNumeroIdentificacion());
        }
        
        // Establecer información de creación
        cliente.setFechaCreacion(LocalDateTime.now());
        cliente.setCreadoPor(usuarioId);
        cliente.setEstado("activo");
        
        // Si es persona natural, generar razón social automáticamente
        if ("Persona Natural".equals(cliente.getTipoPersona())) {
            if (cliente.getRazonSocial() == null || cliente.getRazonSocial().isEmpty()) {
                String razonSocial = (cliente.getNombres() != null ? cliente.getNombres() : "") + 
                                    " " + 
                                    (cliente.getApellidos() != null ? cliente.getApellidos() : "");
                cliente.setRazonSocial(razonSocial.trim());
            }
        }
        
        return clienteRepository.save(cliente);
    }

    /**
     * Actualizar cliente existente
     */
    public Cliente actualizarCliente(String id, Cliente clienteActualizado, String usuarioId) {
        Optional<Cliente> clienteOpt = clienteRepository.findById(id);
        
        if (!clienteOpt.isPresent()) {
            throw new RuntimeException("Cliente no encontrado");
        }
        
        Cliente clienteExistente = clienteOpt.get();
        
        // Verificar si cambió el número de identificación y que no exista otro con el mismo
        if (!clienteExistente.getNumeroIdentificacion().equals(clienteActualizado.getNumeroIdentificacion())) {
            if (clienteRepository.existsByNumeroIdentificacion(clienteActualizado.getNumeroIdentificacion())) {
                throw new RuntimeException("Ya existe otro cliente con el número de identificación: " + 
                                         clienteActualizado.getNumeroIdentificacion());
            }
        }
        
        // Actualizar campos
        clienteExistente.setTipoPersona(clienteActualizado.getTipoPersona());
        clienteExistente.setTipoIdentificacion(clienteActualizado.getTipoIdentificacion());
        clienteExistente.setNumeroIdentificacion(clienteActualizado.getNumeroIdentificacion());
        clienteExistente.setDigitoVerificacion(clienteActualizado.getDigitoVerificacion());
        clienteExistente.setNombres(clienteActualizado.getNombres());
        clienteExistente.setApellidos(clienteActualizado.getApellidos());
        clienteExistente.setRazonSocial(clienteActualizado.getRazonSocial());
        clienteExistente.setCorreo(clienteActualizado.getCorreo());
        clienteExistente.setTelefono(clienteActualizado.getTelefono());
        clienteExistente.setTelefonoSecundario(clienteActualizado.getTelefonoSecundario());
        clienteExistente.setDireccion(clienteActualizado.getDireccion());
        clienteExistente.setDepartamento(clienteActualizado.getDepartamento());
        clienteExistente.setCiudad(clienteActualizado.getCiudad());
        clienteExistente.setCodigoPostal(clienteActualizado.getCodigoPostal());
        clienteExistente.setResponsableIVA(clienteActualizado.getResponsableIVA());
        clienteExistente.setCalidadAgenteRetencion(clienteActualizado.getCalidadAgenteRetencion());
        clienteExistente.setRegimenTributario(clienteActualizado.getRegimenTributario());
        clienteExistente.setResponsabilidadesFiscales(clienteActualizado.getResponsabilidadesFiscales());
        clienteExistente.setCuentasPorCobrar(clienteActualizado.getCuentasPorCobrar());
        clienteExistente.setCuentasParaDevoluciones(clienteActualizado.getCuentasParaDevoluciones());
        clienteExistente.setSobreabonos(clienteActualizado.getSobreabonos());
        clienteExistente.setDeterioroCartera(clienteActualizado.getDeterioroCartera());
        clienteExistente.setCondicionPago(clienteActualizado.getCondicionPago());
        clienteExistente.setDiasCredito(clienteActualizado.getDiasCredito());
        clienteExistente.setCupoCredito(clienteActualizado.getCupoCredito());
        clienteExistente.setCategoriaCliente(clienteActualizado.getCategoriaCliente());
        clienteExistente.setNombreContacto(clienteActualizado.getNombreContacto());
        clienteExistente.setCargoContacto(clienteActualizado.getCargoContacto());
        clienteExistente.setObservaciones(clienteActualizado.getObservaciones());
        clienteExistente.setVendedorAsignado(clienteActualizado.getVendedorAsignado());
        clienteExistente.setZonaVentas(clienteActualizado.getZonaVentas());
        clienteExistente.setHabilitadoFacturacionElectronica(clienteActualizado.getHabilitadoFacturacionElectronica());
        
        // Actualizar tracking
        clienteExistente.setModificadoPor(usuarioId);
        clienteExistente.setFechaModificacion(LocalDateTime.now());
        
        return clienteRepository.save(clienteExistente);
    }

    /**
     * Eliminar cliente (soft delete - cambiar estado a inactivo)
     */
    public boolean eliminarCliente(String id, String usuarioId) {
        Optional<Cliente> clienteOpt = clienteRepository.findById(id);
        
        if (!clienteOpt.isPresent()) {
            return false;
        }
        
        Cliente cliente = clienteOpt.get();
        
        // Verificar que no tenga saldo pendiente
        if (cliente.getSaldoActual() > 0) {
            throw new RuntimeException("No se puede eliminar un cliente con saldo pendiente");
        }
        
        // Cambiar estado a inactivo (soft delete)
        cliente.setEstado("inactivo");
        cliente.setModificadoPor(usuarioId);
        cliente.setFechaModificacion(LocalDateTime.now());
        
        clienteRepository.save(cliente);
        return true;
    }

    /**
     * Eliminar cliente permanentemente (hard delete)
     */
    public boolean eliminarClientePermanente(String id) {
        Optional<Cliente> clienteOpt = clienteRepository.findById(id);
        
        if (!clienteOpt.isPresent()) {
            return false;
        }
        
        Cliente cliente = clienteOpt.get();
        
        if (cliente.getSaldoActual() > 0) {
            throw new RuntimeException("No se puede eliminar un cliente con saldo pendiente");
        }
        
        clienteRepository.deleteById(id);
        return true;
    }

    /**
     * Obtener clientes activos
     */
    public List<Cliente> obtenerClientesActivos() {
        return clienteRepository.findClientesActivos();
    }

    /**
     * Obtener clientes por estado
     */
    public List<Cliente> obtenerClientesPorEstado(String estado) {
        return clienteRepository.findByEstado(estado);
    }

    /**
     * Búsqueda global de clientes
     */
    public List<Cliente> buscarClientes(String termino) {
        return clienteRepository.busquedaGlobal(termino);
    }

    /**
     * Obtener clientes con saldo pendiente
     */
    public List<Cliente> obtenerClientesConSaldo() {
        return clienteRepository.findClientesConSaldo();
    }

    /**
     * Bloquear cliente
     */
    public Cliente bloquearCliente(String id, String motivo, String usuarioId) {
        Optional<Cliente> clienteOpt = clienteRepository.findById(id);
        
        if (!clienteOpt.isPresent()) {
            throw new RuntimeException("Cliente no encontrado");
        }
        
        Cliente cliente = clienteOpt.get();
        cliente.bloquear(usuarioId, motivo);
        
        return clienteRepository.save(cliente);
    }

    /**
     * Activar cliente
     */
    public Cliente activarCliente(String id, String usuarioId) {
        Optional<Cliente> clienteOpt = clienteRepository.findById(id);
        
        if (!clienteOpt.isPresent()) {
            throw new RuntimeException("Cliente no encontrado");
        }
        
        Cliente cliente = clienteOpt.get();
        cliente.activar(usuarioId);
        
        return clienteRepository.save(cliente);
    }

    /**
     * Actualizar saldo del cliente
     */
    public Cliente actualizarSaldo(String id, Double monto) {
        Optional<Cliente> clienteOpt = clienteRepository.findById(id);
        
        if (!clienteOpt.isPresent()) {
            throw new RuntimeException("Cliente no encontrado");
        }
        
        Cliente cliente = clienteOpt.get();
        cliente.actualizarSaldo(monto);
        
        return clienteRepository.save(cliente);
    }

    /**
     * Verificar cupo de crédito disponible
     */
    public Map<String, Object> verificarCupoCredito(String id, Double montoFactura) {
        Optional<Cliente> clienteOpt = clienteRepository.findById(id);
        
        if (!clienteOpt.isPresent()) {
            throw new RuntimeException("Cliente no encontrado");
        }
        
        Cliente cliente = clienteOpt.get();
        Map<String, Object> resultado = new HashMap<>();
        
        resultado.put("cupoTotal", cliente.getCupoCredito());
        resultado.put("saldoActual", cliente.getSaldoActual());
        resultado.put("cupoDisponible", cliente.getCupoDisponible());
        resultado.put("montoFactura", montoFactura);
        resultado.put("tieneCupo", cliente.tieneCupoDisponible(montoFactura));
        
        return resultado;
    }

    /**
     * Obtener estadísticas de clientes
     */
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> estadisticas = new HashMap<>();
        
        estadisticas.put("total", clienteRepository.count());
        estadisticas.put("activos", clienteRepository.countByEstado("activo"));
        estadisticas.put("inactivos", clienteRepository.countByEstado("inactivo"));
        estadisticas.put("bloqueados", clienteRepository.countByEstado("bloqueado"));
        estadisticas.put("personasNaturales", clienteRepository.countByTipoPersona("Persona Natural"));
        estadisticas.put("personasJuridicas", clienteRepository.countByTipoPersona("Persona Jurídica"));
        estadisticas.put("conSaldo", clienteRepository.findClientesConSaldo().size());
        estadisticas.put("conCupoCredito", clienteRepository.findClientesConCupoCredito().size());
        
        return estadisticas;
    }

    /**
     * Obtener clientes por departamento
     */
    public List<Cliente> obtenerClientesPorDepartamento(String departamento) {
        return clienteRepository.findByDepartamento(departamento);
    }

    /**
     * Obtener clientes por ciudad
     */
    public List<Cliente> obtenerClientesPorCiudad(String ciudad) {
        return clienteRepository.findByCiudad(ciudad);
    }

    /**
     * Obtener clientes por vendedor
     */
    public List<Cliente> obtenerClientesPorVendedor(String vendedorId) {
        return clienteRepository.findByVendedorAsignado(vendedorId);
    }
}
