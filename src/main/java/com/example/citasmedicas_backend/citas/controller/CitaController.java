package com.example.citasmedicas_backend.citas.controller;

import com.example.citasmedicas_backend.citas.model.Cita;
import com.example.citasmedicas_backend.citas.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/citas")
public class CitaController {
    @Autowired
    private CitaService citaService;

    @GetMapping
    public List<Cita> getAllCitas() {
        return citaService.getAllCitas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> getCitaById(@PathVariable Long id) {
        Optional<Cita> cita = citaService.getCitaById(id);
        return cita.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Cita createCita(@RequestBody Cita cita) {
        return citaService.createCita(cita);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cita> updateCita(@PathVariable Long id, @RequestBody Cita citaDetails) {
        Cita updatedCita = citaService.updateCita(id, citaDetails);
        if (updatedCita != null) {
            return ResponseEntity.ok(updatedCita);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCita(@PathVariable Long id) {
        if (citaService.deleteCita(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
