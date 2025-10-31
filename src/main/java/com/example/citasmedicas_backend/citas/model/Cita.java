package com.example.citasmedicas_backend.citas.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
@Table(name = "cita")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idEstatus")
    private Estatus estatus;

    @Column(name = "fechaSolicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "motivo", length = 255)
    private String motivo;

    // Campos relacionados con el pago
    @Column(name = "montoPagado")
    private java.math.BigDecimal montoPagado;

    @Column(name = "idPago")
    private Long idPago;

    @Column(name = "estadoPago", length = 50)
    private String estadoPago; // PENDIENTE, APROBADO, RECHAZADO, REEMBOLSADO

    @Column(name = "numeroReferenciaPago", length = 100)
    private String numeroReferenciaPago;

    @Column(name = "fechaPago")
    private LocalDateTime fechaPago;

    @Column(name = "metodoPago", length = 50)
    private String metodoPago;

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

    public Estatus getEstatus() { return estatus; }
    public void setEstatus(Estatus estatus) { this.estatus = estatus; }

    public java.math.BigDecimal getMontoPagado() { return montoPagado; }
    public void setMontoPagado(java.math.BigDecimal montoPagado) { this.montoPagado = montoPagado; }

    public Long getIdPago() { return idPago; }
    public void setIdPago(Long idPago) { this.idPago = idPago; }

    public String getEstadoPago() { return estadoPago; }
    public void setEstadoPago(String estadoPago) { this.estadoPago = estadoPago; }

    public String getNumeroReferenciaPago() { return numeroReferenciaPago; }
    public void setNumeroReferenciaPago(String numeroReferenciaPago) { this.numeroReferenciaPago = numeroReferenciaPago; }

    public LocalDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDateTime fechaPago) { this.fechaPago = fechaPago; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
}


