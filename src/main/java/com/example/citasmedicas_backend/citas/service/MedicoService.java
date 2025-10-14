package com.example.citasmedicas_backend.citas.service;

import com.example.citasmedicas_backend.citas.model.Medico;
import com.example.citasmedicas_backend.citas.repository.MedicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicoService {
    @Autowired
    private MedicoRepository medicoRepository;

    public Medico createMedico(Medico medico) {
        return medicoRepository.save(medico);
    }

    public List<Medico> getAllMedicos() {
        return medicoRepository.findAll();
    }
}
