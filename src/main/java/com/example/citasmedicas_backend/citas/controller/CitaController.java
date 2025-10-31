package com.example.citasmedicas_backend.citas.controller;

import com.example.citasmedicas_backend.citas.dto.CitaProximaDTO;
import com.example.citasmedicas_backend.citas.model.*;
import com.example.citasmedicas_backend.citas.repository.CitaRepository;
import com.example.citasmedicas_backend.citas.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
    public Cita createCita(@RequestBody Map<String, Object> citaRequest) {
        System.out.println("📥 Datos recibidos del frontend: " + citaRequest);
        
        // Construir la entidad Cita desde el Map
        Cita cita = new Cita();
        
        // Paciente (solo referencia por ID)
        if (citaRequest.containsKey("pacienteId")) {
            PacienteDetalle paciente = new PacienteDetalle();
            paciente.setId(((Number) citaRequest.get("pacienteId")).longValue());
            cita.setPaciente(paciente);
        }
        
        // Médico (solo referencia por ID)
        if (citaRequest.containsKey("medicoId")) {
            Medico medico = new Medico();
            medico.setId(((Number) citaRequest.get("medicoId")).longValue());
            cita.setMedico(medico);
        }
        
        // Servicio (solo referencia por ID)
        if (citaRequest.containsKey("servicioId")) {
            Servicio servicio = new Servicio();
            servicio.setId(((Number) citaRequest.get("servicioId")).longValue());
            cita.setServicio(servicio);
        }
        
        // Agenda (opcional)
        if (citaRequest.containsKey("agendaId") && citaRequest.get("agendaId") != null) {
            Agenda agenda = new Agenda();
            agenda.setId(((Number) citaRequest.get("agendaId")).longValue());
            cita.setAgenda(agenda);
        }
        
        // Fecha/hora
        if (citaRequest.containsKey("fechaHora")) {
            String fechaHoraStr = (String) citaRequest.get("fechaHora");
            cita.setFechaSolicitud(LocalDateTime.parse(fechaHoraStr.substring(0, 19)));
        }
        
        // Motivo
        if (citaRequest.containsKey("motivo")) {
            cita.setMotivo((String) citaRequest.get("motivo"));
        }
        
        System.out.println("📋 Cita construida: Paciente=" + cita.getPaciente().getId() + 
                          ", Médico=" + cita.getMedico().getId() + 
                          ", Servicio=" + cita.getServicio().getId());
        
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

    @GetMapping("/medico/{medicoId}")
    public ResponseEntity<List<CitaProximaDTO>> getCitasByMedico(@PathVariable Long medicoId) {
        try {
            System.out.println("🔍 Buscando citas para médico ID: " + medicoId);
            List<CitaProximaDTO> citas = citaRepository.findCitasByMedicoId(medicoId);
            System.out.println("📊 Total de citas encontradas: " + citas.size());
            
            if (citas.isEmpty()) {
                return ResponseEntity.ok(List.of()); // Retornar lista vacía en lugar de 204
            }
            
            return ResponseEntity.ok(citas);
        } catch (Exception e) {
            System.err.println("❌ Error al buscar citas del médico: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }


    @PutMapping("/{id}/aceptar")
    public ResponseEntity<?> aceptarCita(@PathVariable Long id) {
        try {
            System.out.println("✅ Aceptando cita ID: " + id);
            Cita cita = citaService.aceptarCita(id);
            return ResponseEntity.ok(cita);
        } catch (RuntimeException e) {
            System.err.println("❌ Error al aceptar cita: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarCita(@PathVariable Long id) {
        try {
            citaService.cancelarCita(id);
            return ResponseEntity.ok(Map.of(
                "message", "Cita cancelada y eliminada exitosamente",
                "citaId", id
            ));
        } catch (RuntimeException e) {
            System.err.println("❌ Error al cancelar cita: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/posponer")
    public ResponseEntity<?> posponerCita(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        try {
            System.out.println("🕐 Posponiendo cita ID: " + id);
            String nuevaFecha = (String) data.get("nuevaFecha");
            if (nuevaFecha == null || nuevaFecha.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "La fecha nueva es requerida"));
            }
            
            LocalDateTime fechaHora = LocalDateTime.parse(nuevaFecha.substring(0, 19));
            Cita cita = citaService.posponerCita(id, fechaHora);
            return ResponseEntity.ok(cita);
        } catch (RuntimeException e) {
            System.err.println("❌ Error al posponer cita: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
