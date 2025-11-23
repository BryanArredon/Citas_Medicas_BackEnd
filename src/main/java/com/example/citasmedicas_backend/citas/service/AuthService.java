package com.example.citasmedicas_backend.citas.service;

import com.example.citasmedicas_backend.citas.dto.AuthResponse;
import com.example.citasmedicas_backend.citas.model.PacienteDetalle;
import com.example.citasmedicas_backend.citas.model.Usuario;
import com.example.citasmedicas_backend.citas.repository.PacienteRepository;
import com.example.citasmedicas_backend.citas.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Transactional
    public AuthResponse login(String correo, String contraseña) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreoElectronico(correo);

        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException("Correo no registrado");
        }

        Usuario usuario = usuarioOpt.get();

        // Comparación simple de contraseñas (¡en texto plano!)
        if (!usuario.getContraseña().equals(contraseña)) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        // Verificar y crear PacienteDetalle si es necesario
        if (usuario.getRolUser() != null && "PACIENTE".equalsIgnoreCase(usuario.getRolUser().getNombreRol())) {
            verificarYCrearPacienteDetalle(usuario);
        }

        // Devuelve los datos del usuario (incluyendo rol)
        return new AuthResponse(
                usuario.getIdUsuario(),
                usuario.getNombre(),
                usuario.getCorreoElectronico(),
                usuario.getRolUser().getIdRol()
        );
    }

    private void verificarYCrearPacienteDetalle(Usuario usuario) {
        try {
            // Usamos findAll().stream() como fallback si existsByUsuario_IdUsuario no está definido
            // Pero lo ideal es usar el método del repositorio si existe.
            // Dado que en UsuarioService se usó existsByUsuario_IdUsuario, asumo que existe.
            if (!pacienteRepository.existsByUsuario_IdUsuario(usuario.getIdUsuario())) {
                logger.info("Usuario {} es PACIENTE pero no tiene registro en PacienteDetalle. Creando...", usuario.getIdUsuario());
                PacienteDetalle paciente = new PacienteDetalle();
                paciente.setUsuario(usuario);
                pacienteRepository.save(paciente);
                logger.info("✅ Registro de PacienteDetalle creado exitosamente para usuario {}", usuario.getIdUsuario());
            }
        } catch (Exception e) {
            logger.error("Error al verificar/crear PacienteDetalle para usuario {}: {}", usuario.getIdUsuario(), e.getMessage());
            // No lanzamos excepción para no bloquear el login, pero logueamos el error
        }
    }
}
