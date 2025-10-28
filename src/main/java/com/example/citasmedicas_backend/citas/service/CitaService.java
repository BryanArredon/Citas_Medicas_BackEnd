package com.example.citasmedicas_backend.citas.service;

import com.example.citasmedicas_backend.citas.dto.CitaRequestDTO;
import com.example.citasmedicas_backend.citas.model.Agenda;
import com.example.citasmedicas_backend.citas.model.Cita;
import com.example.citasmedicas_backend.citas.model.Medico;
import com.example.citasmedicas_backend.citas.model.Paciente;
import com.example.citasmedicas_backend.citas.model.Servicio;
import com.example.citasmedicas_backend.citas.model.Estatus;
import com.example.citasmedicas_backend.citas.repository.AgendaRepository;
import com.example.citasmedicas_backend.citas.repository.CitaRepository;
import com.example.citasmedicas_backend.citas.repository.MedicoRepository;
import com.example.citasmedicas_backend.citas.repository.PacienteRepository;
import com.example.citasmedicas_backend.citas.repository.ServicioRepository;
import com.example.citasmedicas_backend.citas.repository.EstatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CitaService {
    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private AgendaRepository agendaRepository;

    @Autowired
    private MedicoRepository medicoDetalleRepository;

    @Autowired
    private PacienteRepository pacienteDetalleRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private EstatusRepository estatusRepository;

    // Método simplificado sin pago (temporalmente)
    @Transactional
    public Cita createCitaConAgenda(CitaRequestDTO citaRequest) {
        // 1. Crear la agenda primero
        Agenda agenda = new Agenda();
        agenda.setFecha(citaRequest.getFecha().atStartOfDay());
        agenda.setHoraInicio(citaRequest.getHoraInicio());
        agenda.setHoraFin(citaRequest.getHoraFin());
        
        // Obtener el médico y asignarlo a la agenda
        Medico medico = medicoDetalleRepository.findById(citaRequest.getIdMedicoDetalle())
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));
        agenda.setMedico(medico);
        
        // Guardar la agenda
        Agenda agendaGuardada = agendaRepository.save(agenda);
        
        // 2. Crear la cita con la agenda recién creada
        Cita cita = new Cita();
        
        // Obtener y asignar el paciente
        Paciente paciente = pacienteDetalleRepository.findById(citaRequest.getIdPacienteDetalle())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        cita.setPaciente(paciente);
        
        // Asignar el médico (ya lo tenemos de arriba)
        cita.setMedico(medico);
        
        // Obtener y asignar el servicio
        Servicio servicio = servicioRepository.findById(citaRequest.getIdServicio())
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
        cita.setServicio(servicio);
        
        // Asignar la agenda creada
        cita.setAgenda(agendaGuardada);
        
        // Obtener y asignar el estatus (5 = Pendiente)
        Estatus estatus = estatusRepository.findById(5L)
                .orElseThrow(() -> new RuntimeException("Estatus no encontrado"));
        cita.setEstatus(estatus);
        
        // Asignar fecha de solicitud (actual) y motivo
        cita.setFechaSolicitud(LocalDateTime.now());
        cita.setMotivo(citaRequest.getMotivo());
        
        // Guardar la cita
        return citaRepository.save(cita);
    }

    // Mantén los métodos existentes pero quita temporalmente la lógica de pago
    public Cita createCita(Cita cita) {
        // Por ahora, solo guarda la cita sin verificar pago
        return citaRepository.save(cita);
    }

    public List<Cita> getAllCitas() {
        return citaRepository.findAll();
    }

    public Optional<Cita> getCitaById(Long id) {
        return citaRepository.findById(id);
    }

    public Cita updateCita(Long id, Cita citaDetails) {
        return citaRepository.findById(id).map(cita -> {
            cita.setPaciente(citaDetails.getPaciente());
            cita.setMedico(citaDetails.getMedico());
            cita.setServicio(citaDetails.getServicio());
            cita.setAgenda(citaDetails.getAgenda());
            cita.setEstatus(citaDetails.getEstatus());
            cita.setFechaSolicitud(citaDetails.getFechaSolicitud());
            cita.setMotivo(citaDetails.getMotivo());
            return citaRepository.save(cita);
        }).orElse(null);
    }

    public boolean deleteCita(Long id) {
        if (citaRepository.existsById(id)) {
            citaRepository.deleteById(id);
            return true;
        }
        return false;
    }
}