package com.example.citasmedicas_backend.citas.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
import com.example.citasmedicas_backend.citas.service.HorarioMedicoService;

@RestController
@RequestMapping("/api/horarios")
public class HorarioMedicoController {

    @Autowired
    private HorarioMedicoService service;
    
    @Autowired
    private com.example.citasmedicas_backend.citas.service.MedicoService medicoService;

    private static final Logger logger = LoggerFactory.getLogger(HorarioMedicoController.class);

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
    public ResponseEntity<?> create(@RequestBody HorarioMedico horario) {
        // Resolve medico reference if only id is provided in payload
        logger.info("POST /api/horarios payload: {}", horario);
        try {
            if (horario.getMedico() != null) {
                Long mid = null;
                try { mid = horario.getMedico().getId(); } catch (Exception ignored) {}
                if (mid == null) {
                    // try field idMedico if client sent that
                    try {
                        java.lang.reflect.Field f = horario.getMedico().getClass().getDeclaredField("idMedico");
                        f.setAccessible(true);
                        Object v = f.get(horario.getMedico());
                        if (v instanceof Number) mid = ((Number)v).longValue();
                    } catch (NoSuchFieldException | IllegalAccessException ignored) {}
                    // if still null, try resolve by medico.usuario.nombre
                    if (mid == null) {
                        try {
                            java.lang.reflect.Field usuarioField = horario.getMedico().getClass().getDeclaredField("usuario");
                            usuarioField.setAccessible(true);
                            Object usuarioObj = usuarioField.get(horario.getMedico());
                            if (usuarioObj != null) {
                                try {
                                    java.lang.reflect.Field nombreField = usuarioObj.getClass().getDeclaredField("nombre");
                                    nombreField.setAccessible(true);
                                    Object nombreVal = nombreField.get(usuarioObj);
                                    if (nombreVal instanceof String) {
                                        com.example.citasmedicas_backend.citas.model.Medico found = medicoService.findByUsuarioNombre((String) nombreVal);
                                        if (found != null) mid = found.getId();
                                    }
                                } catch (NoSuchFieldException | IllegalAccessException ignored) {}
                            }
                        } catch (NoSuchFieldException | IllegalAccessException ignored) {}
                    }
                }

                if (mid != null) {
                    com.example.citasmedicas_backend.citas.model.Medico m = medicoService.findById(mid);
                    if (m == null) {
                        // Si el médico no existe, intentar crearlo con el ID del usuario
                        logger.warn("Médico con ID {} no encontrado. Intentando buscar por usuario...", mid);
                        m = medicoService.findByUsuario_Id(mid);
                        if (m == null) {
                            logger.error("No se encontró médico para el ID {} ni como médico ni como usuario", mid);
                        }
                    }
                    horario.setMedico(m);
                }
            }
        } catch (Exception e) {
            // log and continue; save() will likely fail and return 500 which will be propagated
            logger.error("Error resolviendo medico en payload", e);
        }

        // If medico couldn't be resolved, return 400 instead of letting JPA fail with a 500
        if (horario.getMedico() == null) {
            logger.warn("No se pudo resolver el medico del payload; devolviendo 400. Payload: {}", horario);
            return ResponseEntity.badRequest().body("Medico no encontrado o no proporcionado en el payload");
        }

        try {
            HorarioMedico saved = service.save(horario);
            return ResponseEntity.ok(saved);
        } catch (Exception ex) {
            logger.error("Error guardando HorarioMedico", ex);
            return ResponseEntity.status(500).body("Error interno al guardar horario: " + ex.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody HorarioMedico horario) {
        HorarioMedico existing = service.findById(id);
        if (existing == null) return ResponseEntity.notFound().build();
        horario.setId(id);

        // Resolve medico similar to create
        try {
            if (horario.getMedico() != null) {
                Long mid = null;
                try { mid = horario.getMedico().getId(); } catch (Exception ignored) {}
                if (mid == null) {
                    try {
                        java.lang.reflect.Field f = horario.getMedico().getClass().getDeclaredField("idMedico");
                        f.setAccessible(true);
                        Object v = f.get(horario.getMedico());
                        if (v instanceof Number) mid = ((Number)v).longValue();
                    } catch (NoSuchFieldException | IllegalAccessException ignored) {}
                    // if still null, try resolve by medico.usuario.nombre
                    if (mid == null) {
                        try {
                            java.lang.reflect.Field usuarioField = horario.getMedico().getClass().getDeclaredField("usuario");
                            usuarioField.setAccessible(true);
                            Object usuarioObj = usuarioField.get(horario.getMedico());
                            if (usuarioObj != null) {
                                try {
                                    java.lang.reflect.Field nombreField = usuarioObj.getClass().getDeclaredField("nombre");
                                    nombreField.setAccessible(true);
                                    Object nombreVal = nombreField.get(usuarioObj);
                                    if (nombreVal instanceof String) {
                                        com.example.citasmedicas_backend.citas.model.Medico found = medicoService.findByUsuarioNombre((String) nombreVal);
                                        if (found != null) mid = found.getId();
                                    }
                                } catch (NoSuchFieldException | IllegalAccessException ignored) {}
                            }
                        } catch (NoSuchFieldException | IllegalAccessException ignored) {}
                    }
                }

                if (mid != null) {
                    com.example.citasmedicas_backend.citas.model.Medico m = medicoService.findById(mid);
                    horario.setMedico(m);
                }
            }
        } catch (Exception e) {
            logger.error("Error resolviendo medico en update()", e);
        }

        if (horario.getMedico() == null) {
            logger.warn("No se pudo resolver el medico para update id={}; devolviendo 400. Payload: {}", id, horario);
            return ResponseEntity.badRequest().body("Medico no encontrado o no proporcionado en el payload");
        }

        try {
            HorarioMedico saved = service.save(horario);
            return ResponseEntity.ok(saved);
        } catch (Exception ex) {
            logger.error("Error guardando HorarioMedico en update", ex);
            return ResponseEntity.status(500).body("Error interno al guardar horario: " + ex.getMessage());
        }
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

