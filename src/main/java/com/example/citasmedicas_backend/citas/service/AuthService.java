package com.example.citasmedicas_backend.citas.service;


import com.example.citasmedicas_backend.citas.dto.AuthResponse;
import com.example.citasmedicas_backend.citas.model.Usuario;
import com.example.citasmedicas_backend.citas.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService   {


    @Autowired
    private UsuarioRepository usuarioRepository;

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

        // Devuelve los datos del usuario (incluyendo rol)
        return new AuthResponse(
                usuario.getIdUsuario(),
                usuario.getNombre(),
                usuario.getCorreoElectronico(),
                usuario.getRolUser().getNombreRol()
        );
    }

}
