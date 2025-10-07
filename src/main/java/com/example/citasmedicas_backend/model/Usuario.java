package com.example.citasmedicas_backend.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre-usuario", nullable = false)
    private String nombreUsuario;

    @Column(name = "apellido-paterno", nullable = false)
    private String apellidoPaterno;

    @Column(name = "apellido-materno", nullable = false)
    private String apellidoMaterno;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexo", nullable = false)
    private Sexo sexo;

    @Column(name = "fecha-nacimiento", nullable = false)
    private LocalDate fechaNacimeinto;

    @Column(name = "direccion", nullable = false)
    private String direccion;

    @Column(name = "telefono", length = 15)
    private String telefono;

    @Column(name = "correo-electronico", nullable = false)
    private String correoElectronico;

    @Column(name = "contraseña", nullable = false)
    private String contraseña;

    @ManyToOne
    @JoinColumn(name = "idRol", nullable = false)
    private RolUser rolUser;

    //Constructor vacio
    public Usuario () {}

    //Contructor con parametros

    public Usuario(
            Long id,
            String nombreUsuario,
            String apellidoPaterno,
            String apellidoMaterno,
            Sexo sexo,
            LocalDate fechaNacimeinto,
            String direccion,
            String telefono,
            String correoElectronico,
            String contraseña,
            RolUser rolUser
    ) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
        this.sexo = sexo;
        this.fechaNacimeinto = fechaNacimeinto;
        this.direccion = direccion;
        this.telefono = telefono;
        this.correoElectronico = correoElectronico;
        this.contraseña = contraseña;
        this.rolUser = rolUser;
    }
}
