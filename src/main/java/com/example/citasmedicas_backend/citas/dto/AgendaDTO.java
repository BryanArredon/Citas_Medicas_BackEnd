// AgendaDTO.java
package com.example.citasmedicas_backend.citas.dto;

import java.time.LocalTime;

public class AgendaDTO {
    private Long idAgenda;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    
    // Constructores
    public AgendaDTO() {}
    
    public AgendaDTO(Long idAgenda, LocalTime horaInicio, LocalTime horaFin) {
        this.idAgenda = idAgenda;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }
    
    // Getters y Setters
    public Long getIdAgenda() { return idAgenda; }
    public void setIdAgenda(Long idAgenda) { this.idAgenda = idAgenda; }
    
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    
    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
}