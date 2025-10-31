package com.example.citasmedicas_backend.citas.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.citasmedicas_backend.citas.model.Agenda;
import com.example.citasmedicas_backend.citas.model.EstadoMedico;
import com.example.citasmedicas_backend.citas.model.HorarioMedico;
import com.example.citasmedicas_backend.citas.repository.AgendaRepository;
import com.example.citasmedicas_backend.citas.repository.HorarioMedicoRepository;

@Service
@Transactional
public class HorarioMedicoService {

	@Autowired
	private HorarioMedicoRepository horarioMedicoRepository;
	
	@Autowired
	private AgendaRepository agendaRepository;

	// Create or update
	@Transactional(propagation = Propagation.REQUIRED)
	public HorarioMedico save(HorarioMedico horario) {
		if (horario.getMedico() == null || horario.getMedico().getId() == null) {
			throw new IllegalArgumentException("Horario requiere un médico persistido");
		}

		// Si no se proporcionó validUntil, fijarlo a fecha + 1 mes (o hoy +1 mes si fecha es null)
		try {
			if (horario.getValidUntil() == null) {
				if (horario.getFecha() != null) {
					horario.setValidUntil(horario.getFecha().plusMonths(1));
				} else {
					horario.setValidUntil(java.time.LocalDate.now().plusMonths(1));
				}
			}
		} catch (Exception ignored) {}

		// Guardar en horario_medico
		HorarioMedico savedHorario = horarioMedicoRepository.save(horario);
		
		// También registrar en la tabla agenda
		try {
			Agenda agenda = new Agenda();
			// Convertir LocalDate a LocalDateTime (fecha a medianoche)
			LocalDateTime fechaDateTime = savedHorario.getFecha().atStartOfDay();
			agenda.setFecha(fechaDateTime);
			agenda.setHoraInicio(savedHorario.getHorarioInicio());
			agenda.setHoraFin(savedHorario.getHorarioFin());
			agenda.setMedico(savedHorario.getMedico());
			
			agendaRepository.save(agenda);
			System.out.println("✅ Horario también registrado en tabla agenda: " + agenda.getId());
		} catch (Exception e) {
			System.err.println("⚠️ Error al registrar en tabla agenda: " + e.getMessage());
			// No lanzar excepción para no afectar el guardado del horario principal
		}

		return savedHorario;
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
