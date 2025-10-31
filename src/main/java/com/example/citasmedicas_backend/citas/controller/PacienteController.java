package com.example.citasmedicas_backend.citas.controller;

import com.example.citasmedicas_backend.citas.model.PacienteDetalle;
import com.example.citasmedicas_backend.citas.service.PacienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/paciente-detalle")
public class PacienteController {
    @Autowired
    private PacienteService pacienteService;

    @PostMapping
    public PacienteDetalle createPaciente(@RequestBody PacienteDetalle paciente) {
        return pacienteService.createPaciente(paciente);
    }

    @GetMapping
    public List<PacienteDetalle> getAllPacientes() {
        return pacienteService.getAllPacientes();
    }

    @GetMapping("/usuario/{usuarioId}")
    public PacienteDetalle getPacienteByUsuarioId(@PathVariable Long usuarioId) {
        return pacienteService.getPacienteByUsuarioId(usuarioId);
    }
}
