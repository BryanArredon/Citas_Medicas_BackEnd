package com.example.citasmedicas_backend.citas.controller;

import com.example.citasmedicas_backend.citas.dto.CitaProximaDTO;
import com.example.citasmedicas_backend.citas.dto.CitaRequestDTO;
import com.example.citasmedicas_backend.citas.model.Cita;
import com.example.citasmedicas_backend.citas.repository.CitaRepository;
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

    @Autowired
    private CitaRepository citaRepository;

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
    public ResponseEntity<Cita> createCita(@RequestBody CitaRequestDTO citaRequest) {
        try {
            Cita nuevaCita = citaService.createCitaConAgenda(citaRequest);
            return ResponseEntity.ok(nuevaCita);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
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

    @GetMapping("/proximas/{usuarioId}")
    public ResponseEntity<List<CitaProximaDTO>> getCitasProximasByUsuario(@PathVariable Long usuarioId) {
        try {
            List<CitaProximaDTO> citasProximas = citaRepository.findCitasProximasByUsuarioId(usuarioId);
            
            if (citasProximas.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            
            return ResponseEntity.ok(citasProximas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/proximas/{usuarioId}/limit/{limit}")
    public ResponseEntity<List<CitaProximaDTO>> getCitasProximasByUsuarioWithLimit(
            @PathVariable Long usuarioId, 
            @PathVariable int limit) {
        try {
            List<CitaProximaDTO> todasLasCitas = citaRepository.findCitasProximasByUsuarioId(usuarioId);
            
            List<CitaProximaDTO> citasLimitadas = todasLasCitas.stream()
                    .limit(limit)
                    .toList();
            
            if (citasLimitadas.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            
            return ResponseEntity.ok(citasLimitadas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
