package com.example.citasmedicas_backend.citas.dto;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class CitaProximaDTO {
    private Long idCita;
    private LocalDateTime fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String nombreMedico;
    private String apellidoPaternoMedico;
    private String apellidoMaternoMedico;
    private String nombreServicio;
    private String nombreArea;
    private String estatus;
    private String motivo;
    private LocalDateTime fechaSolicitud;

    // Constructor
    public CitaProximaDTO(Long idCita, LocalDateTime fecha, LocalTime horaInicio, LocalTime horaFin,
                         String nombreMedico, String apellidoPaternoMedico, String apellidoMaternoMedico,
                         String nombreServicio, String nombreArea, String estatus, String motivo,
                         LocalDateTime fechaSolicitud) {
        this.idCita = idCita;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.nombreMedico = nombreMedico;
        this.apellidoPaternoMedico = apellidoPaternoMedico;
        this.apellidoMaternoMedico = apellidoMaternoMedico;
        this.nombreServicio = nombreServicio;
        this.nombreArea = nombreArea;
        this.estatus = estatus;
        this.motivo = motivo;
        this.fechaSolicitud = fechaSolicitud;
    }

    // Getters y setters
    public Long getIdCita() { return idCita; }
    public void setIdCita(Long idCita) { this.idCita = idCita; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public String getNombreMedico() { return nombreMedico; }
    public void setNombreMedico(String nombreMedico) { this.nombreMedico = nombreMedico; }

    public String getApellidoPaternoMedico() { return apellidoPaternoMedico; }
    public void setApellidoPaternoMedico(String apellidoPaternoMedico) { this.apellidoPaternoMedico = apellidoPaternoMedico; }

    public String getApellidoMaternoMedico() { return apellidoMaternoMedico; }
    public void setApellidoMaternoMedico(String apellidoMaternoMedico) { this.apellidoMaternoMedico = apellidoMaternoMedico; }

    public String getNombreServicio() { return nombreServicio; }
    public void setNombreServicio(String nombreServicio) { this.nombreServicio = nombreServicio; }

    public String getNombreArea() { return nombreArea; }
    public void setNombreArea(String nombreArea) { this.nombreArea = nombreArea; }

    public String getEstatus() { return estatus; }
    public void setEstatus(String estatus) { this.estatus = estatus; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }
}