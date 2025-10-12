package com.example.citasmedicas_backend.citas.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.example.citasmedicas_backend.citas.model.EstadoMedico;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.citasmedicas_backend.citas.model.HorarioMedico;
import com.example.citasmedicas_backend.citas.repository.HorarioMedicoRepository;

@Service
@Transactional
public class HorarioMedicoService {

	@Autowired
	private HorarioMedicoRepository horarioMedicoRepository;

	// Create or update
	public HorarioMedico save(HorarioMedico horario) {
		return horarioMedicoRepository.save(horario);
	}

	public List<HorarioMedico> findAll() {
		return horarioMedicoRepository.findAll();
	}

	public HorarioMedico findById(Long id) {
		return horarioMedicoRepository.findById(id).orElse(null);
	}

	public void deleteById(Long id) {
		horarioMedicoRepository.deleteById(id);
	}

	// Reserve a horario: set estado to RESERVADO
	public HorarioMedico reserve(Long id) {
		HorarioMedico h = findById(id);
		if (h == null) return null;
		h.setEstadoMedico(EstadoMedico.RESERVADO);
		return horarioMedicoRepository.save(h);
	}

	// Release a horario: set estado to DISPONIBLE
	public HorarioMedico release(Long id) {
		HorarioMedico h = findById(id);
		if (h == null) return null;
		h.setEstadoMedico(EstadoMedico.DISPONIBLE);
		return horarioMedicoRepository.save(h);
	}

	// Postpone (posponer) a horario by shifting start and end times
	public HorarioMedico postpone(Long id, LocalTime newStart, LocalTime newEnd) {
		HorarioMedico h = findById(id);
		if (h == null) return null;
		h.setHorarioInicio(newStart);
		h.setHorarioFin(newEnd);
		return horarioMedicoRepository.save(h);
	}

	// Find by medico id
	public List<HorarioMedico> findByMedicoId(Long medicoId) {
		List<HorarioMedico> all = findAll();
		List<HorarioMedico> res = new ArrayList<>();
		for (HorarioMedico h : all) {
			if (h.getMedico() != null) {
				// use medico.getId() safely
				try {
					Long id = h.getMedico().getId();
					if (id != null && id.equals(medicoId)) res.add(h);
				} catch (Exception ignored) {
				}
			}
		}
		return res;
	}

	// Find by date
	public List<HorarioMedico> findByDate(LocalDate date) {
		List<HorarioMedico> all = findAll();
		List<HorarioMedico> res = new ArrayList<>();
		for (HorarioMedico h : all) {
			if (h.getFecha() != null && h.getFecha().equals(date)) res.add(h);
		}
		return res;
	}
}
