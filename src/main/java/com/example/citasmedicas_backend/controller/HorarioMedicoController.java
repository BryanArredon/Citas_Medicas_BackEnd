package com.example.citasmedicas_backend.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.citasmedicas_backend.model.HorarioMedico;
import com.example.citasmedicas_backend.service.HorarioMedicoService;

@RestController
@RequestMapping("/api/horarios")
public class HorarioMedicoController {

    @Autowired
    private HorarioMedicoService service;

    @GetMapping
    public List<HorarioMedico> listAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<HorarioMedico> getById(@PathVariable Long id) {
        HorarioMedico h = service.findById(id);
        if (h == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(h);
    }

    @PostMapping
    public ResponseEntity<HorarioMedico> create(@RequestBody HorarioMedico horario) {
        HorarioMedico saved = service.save(horario);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HorarioMedico> update(@PathVariable Long id, @RequestBody HorarioMedico horario) {
        HorarioMedico existing = service.findById(id);
        if (existing == null) return ResponseEntity.notFound().build();
        horario.setId(id);
        HorarioMedico saved = service.save(horario);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        HorarioMedico existing = service.findById(id);
        if (existing == null) return ResponseEntity.notFound().build();
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reserve")
    public ResponseEntity<HorarioMedico> reserve(@PathVariable Long id) {
        HorarioMedico h = service.reserve(id);
        if (h == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(h);
    }

    @PostMapping("/{id}/release")
    public ResponseEntity<HorarioMedico> release(@PathVariable Long id) {
        HorarioMedico h = service.release(id);
        if (h == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(h);
    }
//Posponer cita
    @PostMapping("/{id}/postpone")
    public ResponseEntity<HorarioMedico> postpone(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime newStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime newEnd
    ) {
        HorarioMedico h = service.postpone(id, newStart, newEnd);
        if (h == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(h);
    }
//Obtener citas medico
    @GetMapping("/medico/{medicoId}")
    public List<HorarioMedico> byMedico(@PathVariable Long medicoId) {
        return service.findByMedicoId(medicoId);
    }

    @GetMapping("/date/{date}")
    public List<HorarioMedico> byDate(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.findByDate(date);
    }
}

