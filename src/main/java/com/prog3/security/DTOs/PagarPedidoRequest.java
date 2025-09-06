package com.prog3.security.DTOs;

import jakarta.validation.constraints.*;
import jakarta.validation.groups.Default;

/**
 * DTO para procesar pagos de pedidos con validaciones robustas.
 * Soporta diferentes tipos de pago: pagado, cortesía, consumo interno y cancelación.
 */
public class PagarPedidoRequest {

    @NotBlank(message = "El tipo de pago es obligatorio")
    @Pattern(regexp = "^(pagado|cortesia|consumo_interno|cancelado)$", 
             message = "El tipo de pago debe ser: pagado, cortesia, consumo_interno o cancelado")
    private String tipoPago;

    @Pattern(regexp = "^(efectivo|transferencia|tarjeta|otro)?$", 
             message = "La forma de pago debe ser: efectivo, transferencia, tarjeta u otro")
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
            return formaPago != null && !formaPago.trim().isEmpty();
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
        if (esPagado() && (formaPago == null || formaPago.trim().isEmpty())) {
            return "La forma de pago es obligatoria para pagos";
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
                '}';
    }
}
