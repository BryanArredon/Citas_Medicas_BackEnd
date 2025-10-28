package com.example.citasmedicas_backend.citas.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.citasmedicas_backend.citas.model.Servicio;
import com.example.citasmedicas_backend.citas.service.ServicioService;

@RestController
@RequestMapping("/api/servicios")
public class ServiciosController {

    @Autowired
    private ServicioService service;

    @GetMapping
    public List<Servicio> listAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Servicio> getById(@PathVariable Long id) {
        Servicio s = service.findById(id);
        if (s == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(s);
    }

    @GetMapping("/area/{areaId}")
    public ResponseEntity<List<Servicio>> getServiciosByArea(@PathVariable Long areaId) {
        List<Servicio> servicios = service.findByAreaId(areaId);
        if (servicios.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(servicios);
    }

    @PostMapping
    public ResponseEntity<Servicio> create(@RequestBody Servicio servicio) {
        servicio.setId(null);
        Servicio saved = service.save(servicio);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Servicio> update(@PathVariable Long id, @RequestBody Servicio servicioDetails) {
        Servicio existing = service.findById(id);
        if (existing == null) return ResponseEntity.notFound().build();

        existing.setNombreServicio(servicioDetails.getNombreServicio());
        existing.setArea(servicioDetails.getArea());
        existing.setDescripcionServicio(servicioDetails.getDescripcionServicio());
        existing.setCosto(servicioDetails.getCosto());
        existing.setDuracion(servicioDetails.getDuracion());

        Servicio updated = service.save(existing);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (service.findById(id) == null) return ResponseEntity.notFound().build();
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}