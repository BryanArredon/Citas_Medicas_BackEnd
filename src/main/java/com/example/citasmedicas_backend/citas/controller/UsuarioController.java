package com.example.citasmedicas_backend.citas.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;

import com.example.citasmedicas_backend.citas.model.RolUser;
import com.example.citasmedicas_backend.citas.model.Servicio;
import com.example.citasmedicas_backend.citas.model.Usuario;
import com.example.citasmedicas_backend.citas.repository.RolUserRepository;
import com.example.citasmedicas_backend.citas.service.EmailService;
import com.example.citasmedicas_backend.citas.service.MedicoService;
import com.example.citasmedicas_backend.citas.service.ServicioService;
import com.example.citasmedicas_backend.citas.service.UsuarioService;

import jakarta.mail.MessagingException;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private RolUserRepository rolUserRepository;

    @Autowired
    private MedicoService medicoService;

    @Autowired
    private ServicioService servicioService;



    @GetMapping
    public List<Usuario> listAll() {
        return usuarioService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> getById(@PathVariable Long id) {
        Usuario u = usuarioService.findById(id);
        if (u == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(u);
    }

    @PostMapping
    public ResponseEntity<Usuario> create(@RequestBody Usuario usuario) {
        System.out.println("=== INICIANDO REGISTRO DE USUARIO ===");
        System.out.println("Datos recibidos: " + usuario);

        // ensure id is null so JPA will insert
        usuario.setIdUsuario(null);

        // Si no tiene rolUser pero viene con idRol en el JSON, resolver el rol
        if (usuario.getRolUser() == null) {
            // Buscar si hay un campo idRol en el JSON (usando reflexión o creando un DTO)
            // Por ahora, asumir que si no hay rolUser, es paciente (idRol = 3)
            System.out.println("No se recibió rolUser, asignando rol de PACIENTE por defecto");
            RolUser rolPaciente = rolUserRepository.findById(3L).orElse(null);
            if (rolPaciente != null) {
                usuario.setRolUser(rolPaciente);
                System.out.println("Rol asignado: " + rolPaciente.getNombreRol());
            } else {
                System.out.println("❌ ERROR: No se pudo encontrar el rol PACIENTE");
                return ResponseEntity.badRequest().build();
            }
        }

        System.out.println("Guardando usuario...");
        Usuario saved;
        try {
            saved = usuarioService.save(usuario);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("ya está registrado")) {
                System.out.println("❌ ERROR: " + e.getMessage());
                return ResponseEntity.status(409).body(null); // 409 Conflict
            }
            throw e; // Re-throw other exceptions
        }
        System.out.println("Usuario guardado exitosamente con ID: " + saved.getIdUsuario());

        // Verificar que el usuario existe en la base de datos
        Usuario verificado = usuarioService.findById(saved.getIdUsuario());
        if (verificado != null) {
            System.out.println("✅ Usuario verificado en BD: " + verificado.getCorreoElectronico());
        } else {
            System.out.println("❌ ERROR: Usuario no encontrado en BD después del guardado!");
        }

        // Enviar email de bienvenida
        System.out.println("Enviando email de bienvenida...");
        try {
            String nombreCompleto = saved.getNombre() + " " + saved.getApellidoPaterno();
            String contenidoHtml = emailService.generarHtmlBienvenida(nombreCompleto, saved.getCorreoElectronico());
            emailService.enviarEmailHtml(saved.getCorreoElectronico(), "Bienvenido a MediCitas", contenidoHtml);
            System.out.println("✅ Email enviado exitosamente a: " + saved.getCorreoElectronico());
        } catch (MessagingException e) {
            // Log the error but don't fail the registration
            System.err.println("❌ Error al enviar email de bienvenida: " + e.getMessage());
        }

        System.out.println("=== REGISTRO COMPLETADO ===");
        return ResponseEntity.ok(saved);
    }
    public ResponseEntity<Usuario> createMedico(@RequestBody RegistroMedicoDTO registro) {
        System.out.println("=== INICIANDO REGISTRO DE MÉDICO ===");
        System.out.println("Datos recibidos: usuario=" + registro.getUsuario() + ", servicios=" + registro.getServicioIds());

        Usuario usuario = registro.getUsuario();
        // ensure id is null
        usuario.setIdUsuario(null);

        // Set rol to MEDICO if not set
        if (usuario.getRolUser() == null) {
            RolUser rolMedico = rolUserRepository.findById(2L).orElse(null);
            if (rolMedico != null) {
                usuario.setRolUser(rolMedico);
                System.out.println("Rol asignado: " + rolMedico.getNombreRol());
            } else {
                System.out.println("❌ ERROR: No se pudo encontrar el rol MEDICO");
                return ResponseEntity.badRequest().build();
            }
        }

        System.out.println("Guardando usuario...");
        Usuario saved;
        try {
            saved = usuarioService.save(usuario);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("ya está registrado")) {
                System.out.println("❌ ERROR: " + e.getMessage());
                return ResponseEntity.status(409).body(null);
            }
            throw e;
        }
        System.out.println("Usuario guardado exitosamente con ID: " + saved.getIdUsuario());

        // Asignar servicios al médico
        if (registro.getServicioIds() != null && !registro.getServicioIds().isEmpty()) {
            System.out.println("Asignando servicios al médico...");
            try {
                List<Servicio> servicios = new ArrayList<>();
                for (Long id : registro.getServicioIds()) {
                    Servicio s = servicioService.findById(id);
                    if (s != null) {
                        servicios.add(s);
                    } else {
                        System.out.println("⚠️ Servicio con ID " + id + " no encontrado, omitiendo");
                    }
                }
                if (!servicios.isEmpty()) {
                    medicoService.createMedicoWithServices(saved, servicios, "AUTO-" + saved.getIdUsuario());
                    System.out.println("✅ Servicios asignados al médico");
                }
            } catch (Exception e) {
                System.err.println("❌ Error al asignar servicios: " + e.getMessage());
                // No fallar el registro por esto
            }
        }

        // Enviar email de bienvenida
        System.out.println("Enviando email de bienvenida...");
        try {
            String nombreCompleto = saved.getNombre() + " " + saved.getApellidoPaterno();
            String contenidoHtml = emailService.generarHtmlBienvenida(nombreCompleto, saved.getCorreoElectronico());
            emailService.enviarEmailHtml(saved.getCorreoElectronico(), "Bienvenido a MediCitas", contenidoHtml);
            System.out.println("✅ Email enviado exitosamente a: " + saved.getCorreoElectronico());
        } catch (MessagingException e) {
            System.err.println("❌ Error al enviar email de bienvenida: " + e.getMessage());
        }

        System.out.println("=== REGISTRO DE MÉDICO COMPLETADO ===");
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> update(@PathVariable Long id, @RequestBody Usuario usuario) {
        Usuario existing = usuarioService.findById(id);
        if (existing == null) return ResponseEntity.notFound().build();

        // Update only provided fields, keep required fields from existing user
        if (usuario.getNombre() != null) existing.setNombre(usuario.getNombre());
        if (usuario.getApellidoPaterno() != null) existing.setApellidoPaterno(usuario.getApellidoPaterno());
        if (usuario.getApellidoMaterno() != null) existing.setApellidoMaterno(usuario.getApellidoMaterno());
        if (usuario.getSexo() != null) existing.setSexo(usuario.getSexo());
        if (usuario.getFechaNacimiento() != null) existing.setFechaNacimiento(usuario.getFechaNacimiento());
        if (usuario.getDireccion() != null) existing.setDireccion(usuario.getDireccion());
        if (usuario.getTelefono() != null) existing.setTelefono(usuario.getTelefono());
        if (usuario.getCorreoElectronico() != null) existing.setCorreoElectronico(usuario.getCorreoElectronico());
        // Keep contraseña and rolUser from existing user

        Usuario saved = usuarioService.save(existing);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Usuario existing = usuarioService.findById(id);
        if (existing == null) return ResponseEntity.notFound().build();
        usuarioService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

// DTO para el registro de usuarios
class UsuarioRegistroDTO {
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String correoElectronico;
    private String contraseña;
    private String telefono;
    private String direccion;
    private String sexo;
    private String fechaNacimiento;
    private Long idRol;

    // Getters y setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidoPaterno() { return apellidoPaterno; }
    public void setApellidoPaterno(String apellidoPaterno) { this.apellidoPaterno = apellidoPaterno; }

    public String getApellidoMaterno() { return apellidoMaterno; }
    public void setApellidoMaterno(String apellidoMaterno) { this.apellidoMaterno = apellidoMaterno; }

    public String getCorreoElectronico() { return correoElectronico; }
    public void setCorreoElectronico(String correoElectronico) { this.correoElectronico = correoElectronico; }

    public String getContraseña() { return contraseña; }
    public void setContraseña(String contraseña) { this.contraseña = contraseña; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public String getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(String fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public Long getIdRol() { return idRol; }
    public void setIdRol(Long idRol) { this.idRol = idRol; }
}

// DTO para el registro de médicos con servicios
class RegistroMedicoDTO {
    private Usuario usuario;
    private List<Long> servicioIds;

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public List<Long> getServicioIds() { return servicioIds; }
    public void setServicioIds(List<Long> servicioIds) { this.servicioIds = servicioIds; }
}