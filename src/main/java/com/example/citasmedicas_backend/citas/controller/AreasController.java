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

    @GetMapping("/{id}")
    public ResponseEntity<Area> getById(@PathVariable Long id) {
        Area a = service.findById(id);
            if (a == null) {
                return ResponseEntity.notFound().build();
            }
        return ResponseEntity.ok(a);
    }


    @PostMapping
    public ResponseEntity<Area> create(@RequestBody Area area) {
        Area saved = service.save(area);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Area> update(@PathVariable Long id, @RequestBody Area areaDetails) {
        Area existing = service.findById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        existing.setNombreArea(areaDetails.getNombreArea());
        existing.setDescripcion(areaDetails.getDescripcion());
        Area updated = service.save(existing);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (service.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
