package com.example.citasmedicas_backend.citas.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.citasmedicas_backend.citas.model.HistorialClinico;
import com.example.citasmedicas_backend.citas.repository.HistorialClinicoRepository;

@Service
public class HistorialClinicoService {
    @Autowired
    private HistorialClinicoRepository historialClinicoRepository;

    public HistorialClinico createHistorialClinico(HistorialClinico historialClinico) {
        return historialClinicoRepository.save(historialClinico);
    }

    public List<HistorialClinico> getAllHistorialClinico() {
        return historialClinicoRepository.findAll();
    }

    public HistorialClinico getHistorialClinicoById(Long id) {
        return historialClinicoRepository.findById(id).orElse(null);
    }

    public List<HistorialClinico> getHistorialClinicoByPaciente(Long pacienteId) {
        return historialClinicoRepository.findByPacientedetalleId(pacienteId);
    }

    public List<HistorialClinico> getHistorialClinicoByMedico(Long medicoId) {
        return historialClinicoRepository.findByMedicoId(medicoId);
    }

    public HistorialClinico updateHistorialClinico(Long id, HistorialClinico historialClinico) {
        if (historialClinicoRepository.existsById(id)) {
            historialClinico.setId(id);
            return historialClinicoRepository.save(historialClinico);
        }
        return null;
    }

    public boolean deleteHistorialClinico(Long id) {
        if (historialClinicoRepository.existsById(id)) {
            historialClinicoRepository.deleteById(id);
            return true;
        }
        return false;
    }
}