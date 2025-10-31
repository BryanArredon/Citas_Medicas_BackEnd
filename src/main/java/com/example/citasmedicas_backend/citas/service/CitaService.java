package com.example.citasmedicas_backend.citas.service;

import com.example.citasmedicas_backend.citas.dto.PagoRequestDto;
import com.example.citasmedicas_backend.citas.dto.MetodoPagoDto;
import com.example.citasmedicas_backend.citas.dto.SimulacionResponseDto;
import com.example.citasmedicas_backend.citas.model.Cita;
import com.example.citasmedicas_backend.citas.model.Agenda;
import com.example.citasmedicas_backend.citas.repository.CitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class CitaService {
    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private PagoClient pagoClient;
    
    @Autowired
    private AgendaService agendaService;

    public List<Cita> getAllCitas() {
        return citaRepository.findAll();
    }

    public Optional<Cita> getCitaById(Long id) {
        return citaRepository.findById(id);
    }

    public Cita createCita(Cita cita) {
        // TEMPORALMENTE: Guardar cita sin validar pago
        // TODO: Reactivar validación de pago cuando el microservicio esté disponible
        System.out.println("💾 Guardando cita sin validar pago (modo desarrollo)");
        System.out.println("📋 Cita recibida: " + cita);
        
        try {
            // 1. Guardar la cita
            Cita citaGuardada = citaRepository.save(cita);
            System.out.println("✅ Cita guardada exitosamente con ID: " + citaGuardada.getId());
            
            // 2. Si la cita no tiene agenda asignada, crear una automáticamente
            if (citaGuardada.getAgenda() == null && citaGuardada.getFechaSolicitud() != null) {
                System.out.println("📅 Creando agenda automáticamente para la cita...");
                
                LocalDateTime fechaCita = citaGuardada.getFechaSolicitud();
                LocalTime horaInicio = fechaCita.toLocalTime();
                LocalTime horaFin = horaInicio.plusHours(1); // Duración de 1 hora por defecto
                
                Agenda nuevaAgenda = new Agenda(
                    fechaCita,
                    horaInicio,
                    horaFin,
                    citaGuardada.getMedico()
                );
                
                Agenda agendaGuardada = agendaService.createAgenda(nuevaAgenda);
                System.out.println("✅ Agenda creada con ID: " + agendaGuardada.getId());
                
                // Actualizar la cita con la agenda creada
                citaGuardada.setAgenda(agendaGuardada);
                citaGuardada = citaRepository.save(citaGuardada);
                System.out.println("✅ Cita actualizada con agenda ID: " + agendaGuardada.getId());
            }
            
            return citaGuardada;
        } catch (Exception e) {
            System.err.println("❌ Error al guardar cita: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al guardar la cita: " + e.getMessage());
        }
        
        /* CÓDIGO ORIGINAL CON VALIDACIÓN DE PAGO - Comentado temporalmente
        // Build a payment request using the service cost (if available)
        PagoRequestDto pagoReq = new PagoRequestDto();
        if (cita.getServicio() != null && cita.getServicio().getCosto() != null) {
            pagoReq.setMonto(cita.getServicio().getCosto());
        }
        // Optionally set a reference and method (default: approve)
        MetodoPagoDto metodo = new MetodoPagoDto();
        metodo.setTipo("OTRO");
        pagoReq.setMetodoPago(metodo);

        // Call pagos microservice to simulate payment
        SimulacionResponseDto resp = pagoClient.simularPago(pagoReq);
        if (resp == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No response from pagos microservice");
        }

        if ("APROBADO".equalsIgnoreCase(resp.getEstado())) {
            return citaRepository.save(cita);
        } else {
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "Pago rechazado: " + resp.getMessage());
        }
        */
    }

    public Cita updateCita(Long id, Cita citaDetails) {
        return citaRepository.findById(id).map(cita -> {
            cita.setPaciente(citaDetails.getPaciente());
            cita.setMedico(citaDetails.getMedico());
            cita.setServicio(citaDetails.getServicio());
            cita.setAgenda(citaDetails.getAgenda());
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
