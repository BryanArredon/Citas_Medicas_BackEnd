package com.example.citasmedicas_backend.citas.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class CitaRequestDTO {
    private Long idPacienteDetalle;
    private Long idMedicoDetalle;
    private Long idServicio;
    private String motivo;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;

    // Constructores
    public CitaRequestDTO() {}

    public CitaRequestDTO(Long idPacienteDetalle, Long idMedicoDetalle, Long idServicio, 
                         String motivo, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        this.idPacienteDetalle = idPacienteDetalle;
        this.idMedicoDetalle = idMedicoDetalle;
        this.idServicio = idServicio;
        this.motivo = motivo;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    // Getters y Setters
    public Long getIdPacienteDetalle() { return idPacienteDetalle; }
    public void setIdPacienteDetalle(Long idPacienteDetalle) { this.idPacienteDetalle = idPacienteDetalle; }

    public Long getIdMedicoDetalle() { return idMedicoDetalle; }
    public void setIdMedicoDetalle(Long idMedicoDetalle) { this.idMedicoDetalle = idMedicoDetalle; }

    public Long getIdServicio() { return idServicio; }
    public void setIdServicio(Long idServicio) { this.idServicio = idServicio; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
}