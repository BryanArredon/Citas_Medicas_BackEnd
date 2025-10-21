package com.example.citasmedicas_backend.citas.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.citasmedicas_backend.citas.model.Agenda;
import com.example.citasmedicas_backend.citas.repository.AgendaRepository;

@Service
public class AgendaService {
    @Autowired
    private AgendaRepository agendaRepository;

    public Agenda createAgenda(Agenda agenda) {
        return agendaRepository.save(agenda);
    }

    public List<Agenda> getAllAgendas() {
        return agendaRepository.findAll();
    }

    public List<Agenda> getAgendasByMedico(Long medicoId) {
        return agendaRepository.findByMedicoId(medicoId);
    }
}
