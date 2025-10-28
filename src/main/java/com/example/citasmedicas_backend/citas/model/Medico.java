package com.example.citasmedicas_backend.citas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "medico_detalle")
public class Medico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "idUsuario", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "idServicio", nullable = false)
    private Servicio servicio;

    @Column(name = "cedula-profecional", nullable = false)
    private String cedulaProfecional;

    //Contructor vacio
    public Medico () {}

    //Constructor con parametros


    public Medico(
            Long id,
            Usuario usuario,
            Servicio servicio,
            String cedulaProfecional
    ) {
        this.id = id;
        this.usuario = usuario;
        this.servicio = servicio;
        this.cedulaProfecional = cedulaProfecional;
    }
    
    // Getters y setters básicos necesarios
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Usuario getUsuario() {
        return usuario;
    }
    
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
    
    public Servicio getServicio() {
        return servicio;
    }
    
    public void setServicio(Servicio servicio) {
        this.servicio = servicio;
    }
    
    public String getCedulaProfecional() {
        return cedulaProfecional;
    }
    
    public void setCedulaProfecional(String cedulaProfecional) {
        this.cedulaProfecional = cedulaProfecional;
    }
}
