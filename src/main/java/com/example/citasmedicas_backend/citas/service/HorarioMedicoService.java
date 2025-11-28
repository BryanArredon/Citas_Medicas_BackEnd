package com.example.citasmedicas_backend.citas.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.citasmedicas_backend.citas.model.EstadoMedico;
import com.example.citasmedicas_backend.citas.model.HorarioMedico;
import com.example.citasmedicas_backend.citas.repository.HorarioMedicoRepository;

@Service
@Transactional
public class HorarioMedicoService {

	@Autowired
	private HorarioMedicoRepository horarioMedicoRepository;

	// Create or update
	@Transactional(propagation = Propagation.REQUIRED)
	public HorarioMedico save(HorarioMedico horario) {
		if (horario.getMedico() == null || horario.getMedico().getId() == null) {
			throw new IllegalArgumentException("Horario requiere un médico persistido");
		}

		// ⚠️ VALIDACIÓN: Solo permitir UN horario por médico
		// Si estamos creando un nuevo horario (sin ID), verificar que el médico no tenga ya uno
		if (horario.getId() == null) {
			long horariosExistentes = horarioMedicoRepository.countByMedicoId(horario.getMedico().getId());
			if (horariosExistentes > 0) {
				throw new IllegalStateException(
					String.format(
						"El médico con ID %d ya tiene un horario registrado. " +
						"Solo se permite un horario por médico. " +
						"Si desea modificarlo, use la opción de actualizar (PUT) en lugar de crear uno nuevo.",
						horario.getMedico().getId()
					)
				);
			}
			System.out.println("✅ Validación exitosa: el médico no tiene horarios previos");
		} else {
			// Si estamos actualizando (tiene ID), permitir la operación
			System.out.println("✅ Actualizando horario existente ID: " + horario.getId());
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
		
		// ⚠️ NO REGISTRAR EN AGENDA AUTOMÁTICAMENTE
		// La tabla 'agenda' es para CITAS AGENDADAS (slots ocupados)
		// La tabla 'horario_medico' es para DISPONIBILIDAD del médico
		// Solo se debe crear un registro en 'agenda' cuando un PACIENTE agende una cita
		System.out.println("✅ Horario de disponibilidad guardado correctamente con ID: " + savedHorario.getId());

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

	@Transactional
	public void deleteByMedicoId(Long medicoId) {
		horarioMedicoRepository.deleteByMedicoId(medicoId);
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

	// Find by medico id - ahora usando el método del repository
	public List<HorarioMedico> findByMedicoId(Long medicoId) {
		return horarioMedicoRepository.findByMedicoId(medicoId);
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
