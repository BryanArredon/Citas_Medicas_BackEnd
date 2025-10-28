package com.example.citasmedicas_backend.citas.controller;

import java.util.List;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.citasmedicas_backend.citas.dto.AgendaDTO;
import com.example.citasmedicas_backend.citas.model.Agenda;
import com.example.citasmedicas_backend.citas.service.AgendaService;

@RestController
@RequestMapping("/api/agenda")
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
        return agendaService.getAgendasByMedico(medicoId);
    }

    @GetMapping("/medico/{idMedico}/fecha/{fecha}")
    public ResponseEntity<List<AgendaDTO>> getHorariosDisponibles(
            @PathVariable Long idMedico,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        
        try {
            List<AgendaDTO> horarios = agendaService.getHorariosDisponiblesSimplificado(idMedico, fecha);
            
            if (horarios.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            
            return ResponseEntity.ok(horarios);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
