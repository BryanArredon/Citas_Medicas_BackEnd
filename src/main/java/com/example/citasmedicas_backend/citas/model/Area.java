package com.example.citasmedicas_backend.citas.model;

import jakarta.persistence.*;

@Entity
@Table(name = "area") // ← coincide con tu tabla en la BD
public class Area { // ← singular

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idArea")
    private Long id;

    @Column(name = "nombreArea", nullable = false)
    private String nombreArea;

    @Column(name = "descripcion")
    private String descripcion;

    // Constructor vacío
    public Area() {}

    // Constructor con parámetros
    public Area(Long id, String nombreArea, String descripcion) {
        this.id = id;
        this.nombreArea = nombreArea;
        this.descripcion = descripcion;
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreArea() {
        return nombreArea;
    }

    public void setNombreArea(String nombreArea) {
        this.nombreArea = nombreArea;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}