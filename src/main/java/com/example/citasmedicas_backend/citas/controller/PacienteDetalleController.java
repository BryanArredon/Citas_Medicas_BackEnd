// PacienteDetalleController.java
package com.example.citasmedicas_backend.citas.controller;

import com.example.citasmedicas_backend.citas.model.Paciente;
import com.example.citasmedicas_backend.citas.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/paciente-detalle")
public class PacienteDetalleController {
    
    @Autowired
    private PacienteRepository pacienteDetalleRepository;

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Paciente> getPacienteDetalleByUsuarioId(@PathVariable Long usuarioId) {
        Optional<Paciente> pacienteDetalle = pacienteDetalleRepository.findByUsuarioId(usuarioId);
        
        if (pacienteDetalle.isPresent()) {
            return ResponseEntity.ok(pacienteDetalle.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Paciente> getPacienteDetalleById(@PathVariable Long id) {
        Optional<Paciente> pacienteDetalle = pacienteDetalleRepository.findById(id);
        
        if (pacienteDetalle.isPresent()) {
            return ResponseEntity.ok(pacienteDetalle.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}