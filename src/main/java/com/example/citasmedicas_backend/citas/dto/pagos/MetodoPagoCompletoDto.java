package com.example.citasmedicas_backend.citas.dto.pagos;

import java.time.LocalDateTime;

public class MetodoPagoCompletoDto {
    private Long id;
    private String nombreMetodo;
    private String descripcion;
    private Boolean activo;
    private TipoMetodoDto tipoMetodo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    public MetodoPagoCompletoDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreMetodo() { return nombreMetodo; }
    public void setNombreMetodo(String nombreMetodo) { this.nombreMetodo = nombreMetodo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public TipoMetodoDto getTipoMetodo() { return tipoMetodo; }
    public void setTipoMetodo(TipoMetodoDto tipoMetodo) { this.tipoMetodo = tipoMetodo; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    @Override
    public String toString() {
        return "MetodoPagoCompletoDto{" +
                "id=" + id +
                ", nombreMetodo='" + nombreMetodo + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", activo=" + activo +
                ", tipoMetodo=" + tipoMetodo +
                ", fechaCreacion=" + fechaCreacion +
                ", fechaActualizacion=" + fechaActualizacion +
                '}';
    }
}