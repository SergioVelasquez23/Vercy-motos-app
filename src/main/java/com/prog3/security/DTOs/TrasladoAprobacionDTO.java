package com.prog3.security.DTOs;

public class TrasladoAprobacionDTO {

    private String trasladoId;
    private String accion; // ACEPTAR, RECHAZAR
    private String aprobador;
    private String observaciones;

    public TrasladoAprobacionDTO() {
    }

    public String getTrasladoId() {
        return trasladoId;
    }

    public void setTrasladoId(String trasladoId) {
        this.trasladoId = trasladoId;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getAprobador() {
        return aprobador;
    }

    public void setAprobador(String aprobador) {
        this.aprobador = aprobador;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
