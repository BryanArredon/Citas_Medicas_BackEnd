package com.example.citasmedicas_backend.citas.dto;

public class SimulacionResponseDto {
    private String estado;
    private String message;

    public SimulacionResponseDto() {}

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
