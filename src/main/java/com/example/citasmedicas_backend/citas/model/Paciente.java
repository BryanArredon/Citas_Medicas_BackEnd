package com.example.citasmedicas_backend.citas.model;

import jakarta.persistence.*;

@Entity
@Table(name = "pacienteDetalle")
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPacienteDetalle")
    private Long id;

    @OneToOne
    @JoinColumn(name = "idUsuario", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "tipoSangre", length = 5)
    private String tipoSangre;

    @Column(name = "alergias", columnDefinition = "TEXT")
    private String alergias;

    // Constructores
    public Paciente() {}

    public Paciente(Usuario usuario, String tipoSangre, String alergias) {
        this.usuario = usuario;
        this.tipoSangre = tipoSangre;
        this.alergias = alergias;
    }

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getTipoSangre() { return tipoSangre; }
    public void setTipoSangre(String tipoSangre) { this.tipoSangre = tipoSangre; }

    public String getAlergias() { return alergias; }
    public void setAlergias(String alergias) { this.alergias = alergias; }
}