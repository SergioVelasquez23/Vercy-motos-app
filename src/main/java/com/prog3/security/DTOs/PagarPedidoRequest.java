package com.prog3.security.DTOs;

import java.util.ArrayList;
import java.util.List;
import jakarta.validation.constraints.*;
import jakarta.validation.groups.Default;

/**
 * DTO para procesar pagos de pedidos con validaciones robustas.
 * Soporta diferentes tipos de pago: pagado, cortesía, consumo interno y cancelación.
 * También soporta pagos mixtos con múltiples formas de pago.
 */
public class PagarPedidoRequest {

    @NotBlank(message = "El tipo de pago es obligatorio")
    @Pattern(regexp = "^(pagado|cortesia|consumo_interno|cancelado)$", 
             message = "El tipo de pago debe ser: pagado, cortesia, consumo_interno o cancelado")
    private String tipoPago;

    @Pattern(regexp = "^(efectivo|transferencia|tarjeta|otro|mixto)?$", 
             message = "La forma de pago debe ser: efectivo, transferencia, tarjeta, otro o mixto")
    private String formaPago;

    @PositiveOrZero(message = "La propina no puede ser negativa")
    @DecimalMax(value = "999999.99", message = "La propina no puede exceder $999,999.99")
    private double propina = 0.0;

    @NotBlank(message = "Debe especificar quién procesa la operación")
    @Size(min = 2, max = 100, message = "El nombre de quien procesa debe tener entre 2 y 100 caracteres")
    private String procesadoPor;

    @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
    private String notas;

    @Size(max = 200, message = "El motivo de cortesía no puede exceder 200 caracteres")
    private String motivoCortesia;

    @Size(max = 100, message = "El tipo de consumo interno no puede exceder 100 caracteres")
    private String tipoConsumoInterno;
    
    // Lista de pagos mixtos para cuando se utilizan múltiples formas de pago
    private List<PagoMixto> pagosMixtos = new ArrayList<>();

    public PagarPedidoRequest() {
    }

    public PagarPedidoRequest(String tipoPago, String formaPago, double propina, String procesadoPor, String notas) {
        this.tipoPago = tipoPago;
        this.formaPago = formaPago;
        this.propina = propina;
        this.procesadoPor = procesadoPor;
        this.notas = notas;
    }

    // Getters y Setters
    public String getTipoPago() {
        return tipoPago;
    }

    public void setTipoPago(String tipoPago) {
        this.tipoPago = tipoPago;
    }

    public String getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(String formaPago) {
        this.formaPago = formaPago;
    }

    public double getPropina() {
        return propina;
    }

    public void setPropina(double propina) {
        this.propina = propina;
    }

    public String getProcesadoPor() {
        return procesadoPor;
    }

    public void setProcesadoPor(String procesadoPor) {
        this.procesadoPor = procesadoPor;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public String getMotivoCortesia() {
        return motivoCortesia;
    }

    public void setMotivoCortesia(String motivoCortesia) {
        this.motivoCortesia = motivoCortesia;
    }

    public String getTipoConsumoInterno() {
        return tipoConsumoInterno;
    }

    public void setTipoConsumoInterno(String tipoConsumoInterno) {
        this.tipoConsumoInterno = tipoConsumoInterno;
    }
    
    public List<PagoMixto> getPagosMixtos() {
        return pagosMixtos;
    }

    public void setPagosMixtos(List<PagoMixto> pagosMixtos) {
        this.pagosMixtos = pagosMixtos;
    }
    
    public boolean esPagoMixto() {
        return this.pagosMixtos != null && !this.pagosMixtos.isEmpty();
    }

    // Métodos de utilidad para validar el tipo
    public boolean esPagado() {
        return "pagado".equals(this.tipoPago);
    }

    public boolean esCortesia() {
        return "cortesia".equals(this.tipoPago);
    }

    public boolean esConsumoInterno() {
        return "consumo_interno".equals(this.tipoPago);
    }

    public boolean esCancelado() {
        return "cancelado".equals(this.tipoPago);
    }

    public boolean sumaAVentas() {
        return esPagado(); // Solo los pagados suman a ventas
    }

    /**
     * Valida que los campos requeridos para cada tipo de pago estén presentes
     */
    public boolean isValid() {
        if (esPagado()) {
            // Caso especial para pagos mixtos
            if (esPagoMixto()) {
                // Verificar que hay al menos un pago mixto configurado
                if (pagosMixtos.isEmpty()) {
                    return false;
                }
                
                // Verificar que cada pago mixto tiene forma de pago y monto válido
                double totalMonto = 0;
                for (PagoMixto pago : pagosMixtos) {
                    if (pago.getFormaPago() == null || pago.getFormaPago().trim().isEmpty() || pago.getMonto() <= 0) {
                        return false;
                    }
                    totalMonto += pago.getMonto();
                }
                
                // El total debe ser mayor a cero
                return totalMonto > 0;
            } else {
                // Para pagos normales no mixtos, verificar que hay forma de pago
                return formaPago != null && !formaPago.trim().isEmpty();
            }
        }
        if (esCortesia()) {
            return motivoCortesia != null && !motivoCortesia.trim().isEmpty();
        }
        if (esConsumoInterno()) {
            return tipoConsumoInterno != null && !tipoConsumoInterno.trim().isEmpty();
        }
        // Para cancelado no se requieren campos adicionales
        return true;
    }

    /**
     * Obtiene el mensaje de error si la validación falla
     */
    public String getValidationError() {
        if (esPagado()) {
            if (esPagoMixto()) {
                if (pagosMixtos.isEmpty()) {
                    return "Debe especificar al menos un pago para pagos mixtos";
                }
                
                // Verificar cada pago mixto
                for (int i = 0; i < pagosMixtos.size(); i++) {
                    PagoMixto pago = pagosMixtos.get(i);
                    if (pago.getFormaPago() == null || pago.getFormaPago().trim().isEmpty()) {
                        return "La forma de pago es obligatoria para el pago mixto #" + (i+1);
                    }
                    if (pago.getMonto() <= 0) {
                        return "El monto debe ser mayor que cero para el pago mixto #" + (i+1);
                    }
                }
            } else if (formaPago == null || formaPago.trim().isEmpty()) {
                return "La forma de pago es obligatoria para pagos";
            }
        }
        if (esCortesia() && (motivoCortesia == null || motivoCortesia.trim().isEmpty())) {
            return "El motivo de cortesía es obligatorio";
        }
        if (esConsumoInterno() && (tipoConsumoInterno == null || tipoConsumoInterno.trim().isEmpty())) {
            return "El tipo de consumo interno es obligatorio";
        }
        return null;
    }

    @Override
    public String toString() {
        return "PagarPedidoRequest{" +
                "tipoPago='" + tipoPago + '\'' +
                ", formaPago='" + formaPago + '\'' +
                ", propina=" + propina +
                ", procesadoPor='" + procesadoPor + '\'' +
                ", motivoCortesia='" + motivoCortesia + '\'' +
                ", tipoConsumoInterno='" + tipoConsumoInterno + '\'' +
                ", pagosMixtos=" + pagosMixtos +
                '}';
    }
}
