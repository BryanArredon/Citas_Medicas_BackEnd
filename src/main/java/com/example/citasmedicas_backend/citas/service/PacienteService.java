package com.example.citasmedicas_backend.citas.service;

import com.example.citasmedicas_backend.citas.model.Paciente;
import com.example.citasmedicas_backend.citas.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PacienteService {
    @Autowired
    private PacienteRepository pacienteRepository;

    public Paciente createPaciente(Paciente paciente) {
        return pacienteRepository.save(paciente);
    }

    public List<Paciente> getAllPacientes() {
        return pacienteRepository.findAll();
    }
}
