package com.example.citasmedicas_backend.citas.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "historialClinico")
public class HistorialClinico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idHistorial")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPacienteDetalle")
    private PacienteDetalle pacientedetalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idMedicoDetalle")
    private Medico medico;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idCita")
    private Cita cita;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "diagnostico", length = 255)
    private String diagnostico;

    @Column(name = "tratamiento", length = 255)
    private String tratamiento;

    @Column(name = "medicamentos", columnDefinition = "TEXT")
    private String medicamentos;

    @Column(name = "notasAdicionales", columnDefinition = "TEXT")
    private String notasAdicionales;

    @Column(name = "fechaActualizacion")
    private LocalDateTime fechaActualizacion;

    // Constructores
    public HistorialClinico() {}

    public HistorialClinico(PacienteDetalle pacientedetalle, Medico medico, Cita cita, LocalDate fecha,
                           String diagnostico, String tratamiento, String medicamentos,
                           String notasAdicionales, LocalDateTime fechaActualizacion) {
        this.pacientedetalle = pacientedetalle;
        this.medico = medico;
        this.cita = cita;
        this.fecha = fecha;
        this.diagnostico = diagnostico;
        this.tratamiento = tratamiento;
        this.medicamentos = medicamentos;
        this.notasAdicionales = notasAdicionales;
        this.fechaActualizacion = fechaActualizacion;
    }

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PacienteDetalle getPaciente() { return pacientedetalle; }
    public void setPaciente(PacienteDetalle pacientedetalle) { this.pacientedetalle = pacientedetalle; }

    public Medico getMedico() { return medico; }
    public void setMedico(Medico medico) { this.medico = medico; }

    public Cita getCita() { return cita; }
    public void setCita(Cita cita) { this.cita = cita; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }

    public String getTratamiento() { return tratamiento; }
    public void setTratamiento(String tratamiento) { this.tratamiento = tratamiento; }

    public String getMedicamentos() { return medicamentos; }
    public void setMedicamentos(String medicamentos) { this.medicamentos = medicamentos; }

    public String getNotasAdicionales() { return notasAdicionales; }
    public void setNotasAdicionales(String notasAdicionales) { this.notasAdicionales = notasAdicionales; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}