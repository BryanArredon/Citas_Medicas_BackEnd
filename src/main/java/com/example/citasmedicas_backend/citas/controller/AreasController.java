package com.example.citasmedicas_backend.citas.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.citasmedicas_backend.citas.model.Area;
import com.example.citasmedicas_backend.citas.service.AreasService;

@RestController
@RequestMapping("/api/areas")

public class AreasController {

    @Autowired
    private AreasService service;

    @GetMapping
    public List<Area> listAll(){return service.findAll();}


    // Obtener solo áreas activas
    @GetMapping("/activas")
    public List<Area> listActive() {
        return service.findActiveAreas();
    }

    // Obtener área por ID
    @GetMapping("/{id}")
    public ResponseEntity<Area> getById(@PathVariable Long id) {
        Area area = service.findById(id);
        if (area == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(area);
    }

    // Crear nueva área
    @PostMapping
    public ResponseEntity<Area> create(@RequestBody Area area) {
        // Asegurar que el área se cree como activa
        area.setEstatus(true);
        Area saved = service.save(area);
        return ResponseEntity.ok(saved);
    }

    // Actualizar área existente
    @PutMapping("/{id}")
    public ResponseEntity<Area> update(@PathVariable Long id, @RequestBody Area areaDetails) {
        Area existing = service.findById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Actualizar solo nombre y descripción, mantener el estatus actual
        existing.setNombreArea(areaDetails.getNombreArea());
        existing.setDescripcion(areaDetails.getDescripcion());
        
        Area updated = service.save(existing);
        return ResponseEntity.ok(updated);
    }

    // Eliminación lógica (cambiar estatus a false)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Area existing = service.findById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        
        boolean deactivated = service.deactivateArea(id);
        if (deactivated) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    // Endpoint adicional para reactivar área
    @PutMapping("/{id}/reactivar")
    public ResponseEntity<Area> reactivar(@PathVariable Long id) {
        Area area = service.findById(id);
        if (area == null) {
            return ResponseEntity.notFound().build();
        }
        
        area.setEstatus(true);
        Area reactivated = service.save(area);
        return ResponseEntity.ok(reactivated);
    }
}
