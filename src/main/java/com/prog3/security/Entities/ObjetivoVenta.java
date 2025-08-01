package com.prog3.security.Entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "objetivos_ventas")
public class ObjetivoVenta {

    @Id
    private String id;

    private String periodo;
    private Double valor;

    public ObjetivoVenta() {
    }

    public ObjetivoVenta(String periodo, Double valor) {
        this.periodo = periodo;
        this.valor = valor;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    @Override
    public String toString() {
        return "ObjetivoVenta{"
                + "id='" + id + '\''
                + ", periodo='" + periodo + '\''
                + ", valor=" + valor
                + '}';
    }
}
