package com.example.citasmedicas_backend.citas.model;


import java.util.List;

public class MedicoCreateDTO {
    private Long idUsuario;
    private List<Long> serviciosIds;
    private String cedulaProfecional;

    // Constructores
    public MedicoCreateDTO() {}

    public MedicoCreateDTO(Long idUsuario, List<Long> serviciosIds, String cedulaProfecional) {
        this.idUsuario = idUsuario;
        this.serviciosIds = serviciosIds;
        this.cedulaProfecional = cedulaProfecional;
    }

    // Getters y Setters
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public List<Long> getServiciosIds() { return serviciosIds; }
    public void setServiciosIds(List<Long> serviciosIds) { this.serviciosIds = serviciosIds; }

    public String getCedulaProfecional() { return cedulaProfecional; }
    public void setCedulaProfecional(String cedulaProfecional) { this.cedulaProfecional = cedulaProfecional; }
}