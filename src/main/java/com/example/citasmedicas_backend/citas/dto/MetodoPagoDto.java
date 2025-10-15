package com.example.citasmedicas_backend.citas.dto;

public class MetodoPagoDto {
    private Long id;
    private String tipo; // e.g. TARJETA, TRANSFERENCIA

    public MetodoPagoDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}
