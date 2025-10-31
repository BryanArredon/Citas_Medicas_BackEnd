package com.example.citasmedicas_backend.citas.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.citasmedicas_backend.citas.model.HorarioMedico;
import com.example.citasmedicas_backend.citas.model.Medico;
import com.example.citasmedicas_backend.citas.service.HorarioMedicoService;
import com.example.citasmedicas_backend.citas.service.MedicoService;

@RestController
@RequestMapping("/api/medicos")
public class MedicoController {
    @Autowired
    private MedicoService medicoService;

    @Autowired
    private HorarioMedicoService horarioMedicoService;

    @PostMapping
    public Medico createMedico(@RequestBody Medico medico) {
        return medicoService.createMedico(medico);
    }

    @GetMapping
    public List<Medico> getAllMedicos() {
        return medicoService.getAllMedicos();
    }

    // Crear múltiples horarios ligados a un médico existente
    @PostMapping("/{id}/horarios")
    public ResponseEntity<?> createHorariosForMedico(@PathVariable("id") Long medicoId,
                                                     @RequestBody List<HorarioMedico> horarios) {
        Medico medico = medicoService.findById(medicoId);
        if (medico == null) return ResponseEntity.notFound().build();

        List<HorarioMedico> created = new ArrayList<>();
        try {
            for (HorarioMedico h : horarios) {
                h.setMedico(medico);
                HorarioMedico saved = horarioMedicoService.save(h);
                created.add(saved);
            }
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error creando horarios: " + e.getMessage());
        }
    }

    // Obtener medico por usuario id
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Medico> findByUsuarioId(@PathVariable("usuarioId") Long usuarioId) {
        Medico m = medicoService.findByUsuario_Id(usuarioId);
        if (m == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(m);
    }

    // Obtener medico por correo electrónico (query param)
    @GetMapping("/email")
    public ResponseEntity<Medico> findByUsuarioEmail(@RequestParam("correo") String correo) {
        Medico m = medicoService.findByUsuarioCorreo(correo);
        if (m == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(m);
    }

    // Obtener horarios del médico por usuario id
    @GetMapping("/usuario/{usuarioId}/horarios")
    public ResponseEntity<?> horariosByUsuarioId(@PathVariable("usuarioId") Long usuarioId) {
        Medico m = medicoService.findByUsuario_Id(usuarioId);
        if (m == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(horarioMedicoService.findByMedicoId(m.getId()));
    }

    // Obtener horarios del médico por correo
    @GetMapping("/email/horarios")
    public ResponseEntity<?> horariosByUsuarioEmail(@RequestParam("correo") String correo) {
        Medico m = medicoService.findByUsuarioCorreo(correo);
        if (m == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(horarioMedicoService.findByMedicoId(m.getId()));
    }

    // Obtener médicos por servicio
    @GetMapping("/servicio/{servicioId}")
    public ResponseEntity<List<Medico>> getMedicosByServicio(@PathVariable Long servicioId) {
        List<Medico> medicos = medicoService.getAllMedicos();
        List<Medico> medicosPorServicio = medicos.stream()
            .filter(m -> m.getServicio() != null && m.getServicio().getId().equals(servicioId))
            .toList();
        return ResponseEntity.ok(medicosPorServicio);
    }
}
