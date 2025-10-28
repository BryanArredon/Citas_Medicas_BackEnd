package com.example.citasmedicas_backend.citas.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "servicio")
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre-servicio", nullable = false)
    private String nombreServicio;

    @ManyToOne
    @JoinColumn(name = "idArea", nullable = false)
    private Area area;

    @Column(name = "descripcion-servicio", nullable = false)
    private String descripcionServicio;

    @Column(name = "costo", precision = 10, scale = 2)
    private BigDecimal costo;

    @Column(name = "duracion", nullable = false)
    private Integer duracion; // Duración en minutos

    // Constructor vacío
    public Servicio() {}

    // Constructor con parámetros actualizado
    public Servicio(
            Long id,
            String nombreServicio,
            Area area,
            String descripcionServicio,
            BigDecimal costo,
            Integer duracion
    ) {
        this.id = id;
        this.nombreServicio = nombreServicio;
        this.area = area;
        this.descripcionServicio = descripcionServicio;
        this.costo = costo;
        this.duracion = duracion;
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreServicio() {
        return nombreServicio;
    }

    public void setNombreServicio(String nombreServicio) {
        this.nombreServicio = nombreServicio;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public String getDescripcionServicio() {
        return descripcionServicio;
    }

    public void setDescripcionServicio(String descripcionServicio) {
        this.descripcionServicio = descripcionServicio;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    public void setCosto(BigDecimal costo) {
        this.costo = costo;
    }

    public Integer getDuracion() {
        return duracion;
    }

    public void setDuracion(Integer duracion) {
        this.duracion = duracion;
    }
}