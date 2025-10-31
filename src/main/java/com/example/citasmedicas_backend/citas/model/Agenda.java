package com.example.citasmedicas_backend.citas.model;

import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "agenda")
public class Agenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAgenda")
    private Long id;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "horaInicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "horaFin", nullable = false)
    private LocalTime horaFin;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idMedicoDetalle", referencedColumnName = "id")
    private Medico medico;

    // Constructores
    public Agenda() {}

    public Agenda(LocalDateTime fecha, LocalTime horaInicio, LocalTime horaFin, Medico medico) {
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.medico = medico;
    }

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public Medico getMedico() { return medico; }
    public void setMedico(Medico medico) { this.medico = medico; }
}