package com.example.citasmedicas_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.citasmedicas_backend.model.Servicio;
import com.example.citasmedicas_backend.service.ServiciosService;

@RestController
@RequestMapping("/api/servicios")
public class ServiciosController {

	@Autowired
	private ServiciosService service;

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

	@PostMapping
	public ResponseEntity<Servicio> create(@RequestBody Servicio servicio) {
		// ensure id is null so JPA will insert
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
