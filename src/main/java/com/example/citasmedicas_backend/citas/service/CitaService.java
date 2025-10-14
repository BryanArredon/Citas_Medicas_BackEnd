package com.example.citasmedicas_backend.citas.service;

import com.example.citasmedicas_backend.citas.model.Cita;
import com.example.citasmedicas_backend.citas.repository.CitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CitaService {
    @Autowired
    private CitaRepository citaRepository;

    public List<Cita> getAllCitas() {
        return citaRepository.findAll();
    }

    public Optional<Cita> getCitaById(Long id) {
        return citaRepository.findById(id);
    }

    public Cita createCita(Cita cita) {
        return citaRepository.save(cita);
    }

    public Cita updateCita(Long id, Cita citaDetails) {
        return citaRepository.findById(id).map(cita -> {
            cita.setPaciente(citaDetails.getPaciente());
            cita.setMedico(citaDetails.getMedico());
            cita.setServicio(citaDetails.getServicio());
            cita.setAgenda(citaDetails.getAgenda());
            cita.setEstatus(citaDetails.getEstatus());
            cita.setFechaSolicitud(citaDetails.getFechaSolicitud());
            cita.setMotivo(citaDetails.getMotivo());
            return citaRepository.save(cita);
        }).orElse(null);
    }

    public boolean deleteCita(Long id) {
        if (citaRepository.existsById(id)) {
            citaRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
