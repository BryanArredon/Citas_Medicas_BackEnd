package com.example.citasmedicas_backend.citas.service;

import com.example.citasmedicas_backend.citas.model.Agenda;
import com.example.citasmedicas_backend.citas.repository.AgendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
