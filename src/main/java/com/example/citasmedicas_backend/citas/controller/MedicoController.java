package com.example.citasmedicas_backend.citas.controller;

import com.example.citasmedicas_backend.citas.model.Medico;
import com.example.citasmedicas_backend.citas.service.MedicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicos")
public class MedicoController {
    @Autowired
    private MedicoService medicoService;

    @PostMapping
    public Medico createMedico(@RequestBody Medico medico) {
        return medicoService.createMedico(medico);
    }

    @GetMapping
    public List<Medico> getAllMedicos() {
        return medicoService.getAllMedicos();
    }
}
