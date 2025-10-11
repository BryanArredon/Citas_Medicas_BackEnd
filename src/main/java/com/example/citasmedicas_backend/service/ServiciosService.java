package com.example.citasmedicas_backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.citasmedicas_backend.model.Servicio;
import com.example.citasmedicas_backend.repository.ServiciosRepository;

@Service
@Transactional
public class ServiciosService {

	@Autowired
	private ServiciosRepository serviciosRepository;

	public Servicio save(Servicio servicio) {
		return serviciosRepository.save(servicio);
	}

	public List<Servicio> findAll() {
		return serviciosRepository.findAll();
	}

	public Servicio findById(Long id) {
		Optional<Servicio> s = serviciosRepository.findById(id);
		return s.orElse(null);
	}

	public void deleteById(Long id) {
		serviciosRepository.deleteById(id);
	}
}
