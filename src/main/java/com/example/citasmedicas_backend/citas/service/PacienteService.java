package com.example.citasmedicas_backend.citas.service;

import com.example.citasmedicas_backend.citas.model.PacienteDetalle;
import com.example.citasmedicas_backend.citas.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PacienteService {
    @Autowired
    private PacienteRepository pacienteRepository;

    public PacienteDetalle createPaciente(PacienteDetalle paciente) {
        return pacienteRepository.save(paciente);
    }

    public List<PacienteDetalle> getAllPacientes() {
        return pacienteRepository.findAll();
    }

    public PacienteDetalle getPacienteByUsuarioId(Long usuarioId) {
        return pacienteRepository.findByUsuarioIdUsuario(usuarioId);
    }
}
