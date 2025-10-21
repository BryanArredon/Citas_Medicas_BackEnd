package com.example.citasmedicas_backend.citas.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.citasmedicas_backend.citas.model.HistorialClinico;
import com.example.citasmedicas_backend.citas.service.HistorialClinicoService;

@RestController
@RequestMapping("/api/historial-clinico")
public class HistorialClinicoController {
    @Autowired
    private HistorialClinicoService historialClinicoService;

    @PostMapping
    public HistorialClinico createHistorialClinico(@RequestBody HistorialClinico historialClinico) {
        return historialClinicoService.createHistorialClinico(historialClinico);
    }

    @GetMapping
    public List<HistorialClinico> getAllHistorialClinico() {
        return historialClinicoService.getAllHistorialClinico();
    }

    @GetMapping("/{id}")
    public HistorialClinico getHistorialClinicoById(@PathVariable Long id) {
        return historialClinicoService.getHistorialClinicoById(id);
    }

    @GetMapping("/paciente/{pacienteId}")
    public List<HistorialClinico> getHistorialClinicoByPaciente(@PathVariable Long pacienteId) {
        return historialClinicoService.getHistorialClinicoByPaciente(pacienteId);
    }

    @GetMapping("/medico/{medicoId}")
    public List<HistorialClinico> getHistorialClinicoByMedico(@PathVariable Long medicoId) {
        return historialClinicoService.getHistorialClinicoByMedico(medicoId);
    }

    @PutMapping("/{id}")
    public HistorialClinico updateHistorialClinico(@PathVariable Long id, @RequestBody HistorialClinico historialClinico) {
        return historialClinicoService.updateHistorialClinico(id, historialClinico);
    }

    @DeleteMapping("/{id}")
    public boolean deleteHistorialClinico(@PathVariable Long id) {
        return historialClinicoService.deleteHistorialClinico(id);
    }
}