package com.example.citasmedicas_backend.citas.model;

import jakarta.persistence.*;

@Entity
@Table(name = "rol_user") // ← Nombre exacto de tu tabla
public class RolUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idRol")
    private Long idRol;

    @Column(name = "nombreRol", nullable = false)
    private String nombreRol;

    public RolUser() {}

    // Getters y setters
    public Long getIdRol() { return idRol; }
    public void setIdRol(Long idRol) { this.idRol = idRol; }

    public String getNombreRol() { return nombreRol; }
    public void setNombreRol(String nombreRol) { this.nombreRol = nombreRol; }
}