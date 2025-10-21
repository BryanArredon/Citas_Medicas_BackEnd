package com.example.citasmedicas_backend.citas.dto;

public class AuthResponse {

    private Long idUsuario;
    private String nombre;
    private String correoElectronico;
    private long rol;

    // Constructor
    public AuthResponse(Long idUsuario, String nombre, String correoElectronico, long rol) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.correoElectronico = correoElectronico;
        this.rol = rol;
    }

    // Getters (sin setters, es de solo lectura)
    public Long getIdUsuario() { return idUsuario; }
    public String getNombre() { return nombre; }
    public String getCorreoElectronico() { return correoElectronico; }
    public Long getRol() { return rol; }

}
