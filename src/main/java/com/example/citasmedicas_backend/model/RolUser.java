package com.example.citasmedicas_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "rolUser")
public class RolUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRol;

    @Column(name = "nombre-rol", nullable = false)
    private String nombreRol;

    //Constructor vacio
    public RolUser () {}

    //Constructor con parametros


    public RolUser(
            Long idRol,
            String nombreRol
    ) {
        this.idRol = idRol;
        this.nombreRol = nombreRol;
    }

    //Getters y Setters

    public Long getIdRol() {
        return idRol;
    }

    public void setIdRol(Long idRol) {
        this.idRol = idRol;
    }

    public String getNombreRol() {
        return nombreRol;
    }

    public void setNombreRol(String nombreRol) {
        this.nombreRol = nombreRol;
    }
}
