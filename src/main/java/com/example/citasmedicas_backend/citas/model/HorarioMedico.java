package com.example.citasmedicas_backend.citas.model;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "horarioMedico")
public class
HorarioMedico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "idMedico", nullable = false)
    private Medico medico;

    @Column(name = "fecha-horario")
    private LocalDate fecha;

    @Column(name = "horario-inicio")
    private LocalTime horarioInicio;

    @Column(name = "hoario-fin")
    private LocalTime horarioFin;

    @Column(name = "duracion")
    private Integer duracion;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Enumerated(EnumType.STRING)
    @Column(name = "Estado medico", nullable = false)
    private EstadoMedico estadoMedico = EstadoMedico.DISPONIBLE;

    //Constructor vacio
    public HorarioMedico () {}

    //Contructor con parametros

    public HorarioMedico(
            Long id,
            Medico medico,
            LocalDate fecha,
            LocalTime horarioInicio,
            LocalTime horarioFin,
            Integer duracion,
            EstadoMedico estadoMedico
    ) {
        this.id = id;
        this.medico = medico;
        this.fecha = fecha;
        this.horarioInicio = horarioInicio;
        this.horarioFin = horarioFin;
        this.duracion = duracion;
        this.estadoMedico = estadoMedico;
    }

    public LocalDate getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDate validUntil) {
        this.validUntil = validUntil;
    }

    //Gettes y setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Medico getMedico() {
        return medico;
    }

    public void setMedico(Medico medico) {
        this.medico = medico;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHorarioInicio() {
        return horarioInicio;
    }

    public void setHorarioInicio(LocalTime horarioInicio) {
        this.horarioInicio = horarioInicio;
    }

    public LocalTime getHorarioFin() {
        return horarioFin;
    }

    public void setHorarioFin(LocalTime horarioFin) {
        this.horarioFin = horarioFin;
    }

    public Integer getDuracion() {
        return duracion;
    }

    public void setDuracion(Integer duracion) {
        this.duracion = duracion;
    }

    public EstadoMedico getEstadoMedico() {
        return estadoMedico;
    }

    public void setEstadoMedico(EstadoMedico estadoMedico) {
        this.estadoMedico = estadoMedico;
    }
}
