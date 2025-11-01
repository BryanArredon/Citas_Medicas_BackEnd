package com.example.citasmedicas_backend.citas.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.citasmedicas_backend.citas.model.HorarioMedico;
import com.example.citasmedicas_backend.citas.model.Medico;
import com.example.citasmedicas_backend.citas.model.MedicoCreateDTO;
import com.example.citasmedicas_backend.citas.model.Servicio;
import com.example.citasmedicas_backend.citas.model.Usuario;
import com.example.citasmedicas_backend.citas.service.HorarioMedicoService;
import com.example.citasmedicas_backend.citas.service.MedicoService;
import com.example.citasmedicas_backend.citas.service.UsuarioService;
import com.example.citasmedicas_backend.citas.repository.ServicioRepository;

@RestController
@RequestMapping("/api/medicos")
public class MedicoController {
    @Autowired
    private MedicoService medicoService;

    @Autowired
    private HorarioMedicoService horarioMedicoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ServicioRepository servicioRepository;

    @PostMapping("/con-servicios")
    public ResponseEntity<?> createMedicoWithServices(@RequestBody MedicoCreateDTO medicoDTO) {
        try {
            System.out.println("=== CREANDO MÉDICO CON MÚLTIPLES SERVICIOS ===");
            System.out.println("ID Usuario: " + medicoDTO.getIdUsuario());
            System.out.println("Cédula: " + medicoDTO.getCedulaProfecional());
            System.out.println("Servicios: " + medicoDTO.getServiciosIds());

            // Verificar que el usuario existe
            Usuario usuario = usuarioService.findById(medicoDTO.getIdUsuario());
            if (usuario == null) {
                return ResponseEntity.badRequest().body("Usuario no encontrado con ID: " + medicoDTO.getIdUsuario());
            }

            List<Medico> medicosCreados = new ArrayList<>();
            
            // Crear un registro por cada servicio
            for (Long servicioId : medicoDTO.getServiciosIds()) {
                Servicio servicio = servicioRepository.findById(servicioId).orElse(null);
                if (servicio == null) {
                    System.out.println("⚠️ Servicio no encontrado: " + servicioId);
                    continue; // Saltar este servicio y continuar con los demás
                }

                // Crear nuevo registro de médico
                Medico medico = new Medico();
                medico.setUsuario(usuario);
                medico.setServicio(servicio);
                medico.setCedulaProfecional(medicoDTO.getCedulaProfecional());

                System.out.println("Creando médico - Servicio: " + servicioId);
                Medico medicoCreado = medicoService.createMedico(medico);
                medicosCreados.add(medicoCreado);
            }

            System.out.println("✅ Médicos creados exitosamente: " + medicosCreados.size() + " registros");
            return ResponseEntity.ok(medicosCreados);

        } catch (Exception e) {
            System.err.println("❌ Error creando médico: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error creando médico: " + e.getMessage());
        }
    }

    
    // En MedicoController.java - agregar estos métodos

// Eliminar médico por ID
@DeleteMapping("/{id}")
public ResponseEntity<?> deleteMedico(@PathVariable Long id) {
    try {
        System.out.println("🗑️ Eliminando médico con ID: " + id);
        Medico medico = medicoService.findById(id);
        if (medico == null) {
            return ResponseEntity.notFound().build();
        }
        
        medicoService.deleteMedico(id);
        System.out.println("✅ Médico eliminado exitosamente");
        return ResponseEntity.noContent().build();
        
    } catch (Exception e) {
        System.err.println("❌ Error eliminando médico: " + e.getMessage());
        return ResponseEntity.status(500).body("Error eliminando médico: " + e.getMessage());
    }
}

// Eliminar todos los registros de médico por usuario ID
@DeleteMapping("/usuario/{usuarioId}")
public ResponseEntity<?> deleteMedicosByUsuario(@PathVariable Long usuarioId) {
    try {
        System.out.println("🗑️ Eliminando médicos del usuario ID: " + usuarioId);
        
        // Buscar todos los registros de médico para este usuario
        List<Medico> medicos = medicoService.getAllMedicos().stream()
            .filter(m -> m.getUsuario() != null && m.getUsuario().getIdUsuario().equals(usuarioId))
            .collect(Collectors.toList());
            
        for (Medico medico : medicos) {
            medicoService.deleteMedico(medico.getId());
        }
        
        System.out.println("✅ " + medicos.size() + " registros de médico eliminados");
        return ResponseEntity.noContent().build();
        
    } catch (Exception e) {
        System.err.println("❌ Error eliminando médicos del usuario: " + e.getMessage());
        return ResponseEntity.status(500).body("Error eliminando médicos: " + e.getMessage());
    }
}

// Actualizar médico con múltiples servicios
@PutMapping("/usuario/{usuarioId}")
public ResponseEntity<?> updateMedicoWithServices(
        @PathVariable Long usuarioId, 
        @RequestBody MedicoCreateDTO medicoDTO) {
    try {
        System.out.println("🔄 Actualizando médico del usuario ID: " + usuarioId);
        System.out.println("Nuevos servicios: " + medicoDTO.getServiciosIds());
        System.out.println("Nueva cédula: " + medicoDTO.getCedulaProfecional());

        // Verificar que el usuario existe
        Usuario usuario = usuarioService.findById(usuarioId);
        if (usuario == null) {
            return ResponseEntity.badRequest().body("Usuario no encontrado con ID: " + usuarioId);
        }

        // 1. Eliminar todos los registros existentes de este médico
        List<Medico> medicosExistentes = medicoService.getAllMedicos().stream()
            .filter(m -> m.getUsuario() != null && m.getUsuario().getIdUsuario().equals(usuarioId))
            .collect(Collectors.toList());
            
        for (Medico medico : medicosExistentes) {
            medicoService.deleteMedico(medico.getId());
        }

        // 2. Crear nuevos registros con los servicios actualizados
        List<Medico> medicosActualizados = new ArrayList<>();
        for (Long servicioId : medicoDTO.getServiciosIds()) {
            Servicio servicio = servicioRepository.findById(servicioId).orElse(null);
            if (servicio == null) {
                System.out.println("⚠️ Servicio no encontrado: " + servicioId);
                continue;
            }

            Medico medico = new Medico();
            medico.setUsuario(usuario);
            medico.setServicio(servicio);
            medico.setCedulaProfecional(medicoDTO.getCedulaProfecional());

            Medico medicoActualizado = medicoService.createMedico(medico);
            medicosActualizados.add(medicoActualizado);
        }

        System.out.println("✅ Médico actualizado exitosamente: " + medicosActualizados.size() + " registros");
        return ResponseEntity.ok(medicosActualizados);

    } catch (Exception e) {
        System.err.println("❌ Error actualizando médico: " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.status(500).body("Error actualizando médico: " + e.getMessage());
    }
}

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
