package com.example.citasmedicas_backend.citas.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cita")
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCita")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPacienteDetalle")
    private PacienteDetalle paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idMedicoDetalle")
    private Medico medico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idServicio")
    private Servicio servicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idAgenda")
    private Agenda agenda;

    @Column(name = "fechaSolicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "motivo", length = 255)
    private String motivo;

    // Constructores
    public Cita() {}

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PacienteDetalle getPaciente() { return paciente; }
    public void setPaciente(PacienteDetalle paciente) { this.paciente = paciente; }

    public Medico getMedico() { return medico; }
    public void setMedico(Medico medico) { this.medico = medico; }

    public Servicio getServicio() { return servicio; }
    public void setServicio(Servicio servicio) { this.servicio = servicio; }

    public Agenda getAgenda() { return agenda; }
    public void setAgenda(Agenda agenda) { this.agenda = agenda; }

    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}

