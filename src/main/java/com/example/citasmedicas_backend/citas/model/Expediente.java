package com.example.citasmedicas_backend.citas.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "expediente")
public class Expediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idExpediente")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPacienteDetalle")
    private PacienteDetalle pacientedetalle;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idHistorial")
    private HistorialClinico historial;

    @Column(name = "fechaApertura", nullable = false)
    private LocalDate fechaApertura;

    @Column(name = "fechaCierre")
    private LocalDate fechaCierre;

    @Column(name = "fechaActualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "nombreArchivo", length = 100)
    private String nombreArchivo;

    @Column(name = "rutaArchivo", length = 255)
    private String rutaArchivo;

    // Constructores
    public Expediente() {}

    public Expediente(PacienteDetalle pacientedetalle, HistorialClinico historial, LocalDate fechaApertura,
                      LocalDate fechaCierre, LocalDateTime fechaActualizacion,
                      String nombreArchivo, String rutaArchivo) {
        this.pacientedetalle = pacientedetalle;
        this.historial = historial;
        this.fechaApertura = fechaApertura;
        this.fechaCierre = fechaCierre;
        this.fechaActualizacion = fechaActualizacion;
        this.nombreArchivo = nombreArchivo;
        this.rutaArchivo = rutaArchivo;
    }
    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PacienteDetalle getPaciente() { return pacientedetalle; }
    public void setPaciente(PacienteDetalle pacientedetalle) { this.pacientedetalle = pacientedetalle; }

    public HistorialClinico getHistorial() { return historial; }
    public void setHistorial(HistorialClinico historial) { this.historial = historial; }

    public LocalDate getFechaApertura() { return fechaApertura; }
    public void setFechaApertura(LocalDate fechaApertura) { this.fechaApertura = fechaApertura; }

    public LocalDate getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDate fechaCierre) { this.fechaCierre = fechaCierre; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }

    public String getRutaArchivo() { return rutaArchivo; }
    public void setRutaArchivo(String rutaArchivo) { this.rutaArchivo = rutaArchivo; }
}