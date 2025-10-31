package com.example.citasmedicas_backend.citas.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.citasmedicas_backend.citas.model.Agenda;
import com.example.citasmedicas_backend.citas.service.AgendaService;

@RestController
@RequestMapping("/api/agendas")
public class AgendaController {
    @Autowired
    private AgendaService agendaService;

    @PostMapping
    public Agenda createAgenda(@RequestBody Agenda agenda) {
        return agendaService.createAgenda(agenda);
    }

    @GetMapping
    public List<Agenda> getAllAgendas() {
        return agendaService.getAllAgendas();
    }

    @GetMapping("/medico/{medicoId}")
    public List<Agenda> getAgendasByMedico(@PathVariable Long medicoId) {
        System.out.println("🔍 Buscando agendas para médico ID: " + medicoId);
        List<Agenda> agendas = agendaService.getAgendasByMedico(medicoId);
        System.out.println("📊 Total de agendas encontradas: " + agendas.size());
        agendas.forEach(a -> {
            System.out.println("  - Agenda ID: " + a.getId() + 
                             ", Fecha: " + a.getFecha() + 
                             ", Hora: " + a.getHoraInicio() + "-" + a.getHoraFin() +
                             ", Médico ID: " + (a.getMedico() != null ? a.getMedico().getId() : "null"));
        });
        return agendas;
    }
}
