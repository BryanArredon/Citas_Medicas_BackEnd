package com.example.citasmedicas_backend.citas.service;

import com.example.citasmedicas_backend.citas.dto.PagoRequestDto;
import com.example.citasmedicas_backend.citas.dto.MetodoPagoDto;
import com.example.citasmedicas_backend.citas.dto.SimulacionResponseDto;
import com.example.citasmedicas_backend.citas.model.Cita;
import com.example.citasmedicas_backend.citas.model.Agenda;
import com.example.citasmedicas_backend.citas.model.Estatus;
import com.example.citasmedicas_backend.citas.repository.CitaRepository;
import com.example.citasmedicas_backend.citas.repository.EstatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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

    @Autowired
    private EstatusRepository estatusRepository;

    @Autowired
    private EmailService emailService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

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

    // Método para aceptar una cita
    @Transactional
    public Cita aceptarCita(Long id) {
        System.out.println("✅ Iniciando proceso de aceptar cita ID: " + id);
        
        Cita cita = citaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + id));
        
        // Validar que la cita esté en estado válido para ser aceptada
        String estatusActual = cita.getEstatus() != null ? cita.getEstatus().getEstatus() : null;
        if (estatusActual != null && !estatusActual.equals("Pendiente") && !estatusActual.equals("Confirmada")) {
            throw new RuntimeException("La cita ya fue procesada. Estado actual: " + estatusActual);
        }
        
        Estatus estatusAceptado = estatusRepository.findByEstatus("Aceptada")
            .orElseThrow(() -> new RuntimeException("Estatus 'Aceptada' no encontrado en la base de datos"));
        
        cita.setEstatus(estatusAceptado);
        Cita citaGuardada = citaRepository.save(cita);
        citaRepository.flush(); // Forzar la escritura inmediata a la base de datos
        System.out.println("💾 Cambios guardados en la base de datos - Estado: " + citaGuardada.getEstatus().getEstatus());
        
        // Enviar notificación al paciente
        try {
            String emailPaciente = citaGuardada.getPaciente().getUsuario().getCorreoElectronico();
            String nombrePaciente = citaGuardada.getPaciente().getUsuario().getNombre() + " " + 
                                   citaGuardada.getPaciente().getUsuario().getApellidoPaterno();
            String nombreMedico = "Dr. " + citaGuardada.getMedico().getUsuario().getNombre() + " " + 
                                 citaGuardada.getMedico().getUsuario().getApellidoPaterno();
            String especialidad = citaGuardada.getServicio() != null ? citaGuardada.getServicio().getNombreServicio() : "General";
            String fechaCita = citaGuardada.getFechaSolicitud().format(FORMATTER);
            
            emailService.notificarCambioCita(emailPaciente, nombrePaciente, nombreMedico, especialidad, fechaCita, "aceptada", null);
            System.out.println("📧 Email enviado a: " + emailPaciente);
        } catch (Exception e) {
            System.err.println("⚠️ Error al enviar email: " + e.getMessage());
            // No fallar la operación si el email falla
        }
        
        System.out.println("✅ Cita aceptada exitosamente");
        return citaGuardada;
    }

    // Método para cancelar una cita (elimina de la base de datos)
    @Transactional
    public void cancelarCita(Long id) {
        System.out.println("❌ Iniciando proceso de cancelar cita ID: " + id);
        
        Cita cita = citaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + id));
        
        // Validar que la cita esté en estado válido para ser cancelada
        String estatusActual = cita.getEstatus() != null ? cita.getEstatus().getEstatus() : null;
        if (estatusActual != null && estatusActual.equals("Cancelada")) {
            throw new RuntimeException("La cita ya fue cancelada anteriormente");
        }
        
        // Guardar datos de la cita antes de eliminar (para el email)
        try {
            String emailPaciente = cita.getPaciente().getUsuario().getCorreoElectronico();
            String nombrePaciente = cita.getPaciente().getUsuario().getNombre() + " " + 
                                   cita.getPaciente().getUsuario().getApellidoPaterno();
            String nombreMedico = "Dr. " + cita.getMedico().getUsuario().getNombre() + " " + 
                                 cita.getMedico().getUsuario().getApellidoPaterno();
            String especialidad = cita.getServicio() != null ? cita.getServicio().getNombreServicio() : "General";
            String fechaCita = cita.getFechaSolicitud().format(FORMATTER);
            
            // Eliminar la cita de la base de datos
            citaRepository.delete(cita);
            citaRepository.flush(); // Forzar la eliminación inmediata
            System.out.println("🗑️ Cita eliminada de la base de datos - ID: " + id);
            
            // Enviar notificación al paciente
            emailService.notificarCambioCita(emailPaciente, nombrePaciente, nombreMedico, especialidad, fechaCita, "cancelada", null);
            System.out.println("📧 Email de cancelación enviado a: " + emailPaciente);
        } catch (Exception e) {
            System.err.println("⚠️ Error al procesar la cancelación: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al cancelar la cita: " + e.getMessage());
        }
        
        System.out.println("✅ Cita cancelada y eliminada exitosamente");
    }

    // Método para posponer una cita
    @Transactional
    public Cita posponerCita(Long id, LocalDateTime nuevaFecha) {
        System.out.println("🕐 Iniciando proceso de posponer cita ID: " + id);
        
        Cita cita = citaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + id));
        
        // Validar que la cita esté en estado válido para ser pospuesta
        String estatusActual = cita.getEstatus() != null ? cita.getEstatus().getEstatus() : null;
        if (estatusActual != null && (estatusActual.equals("Cancelada") || estatusActual.equals("Pospuesta"))) {
            throw new RuntimeException("La cita ya fue procesada. Estado actual: " + estatusActual);
        }
        
        Estatus estatusPospuesto = estatusRepository.findByEstatus("Pospuesta")
            .orElseThrow(() -> new RuntimeException("Estatus 'Pospuesta' no encontrado en la base de datos"));
        
        // Guardar fecha anterior para el email
        String fechaAnterior = cita.getFechaSolicitud().format(FORMATTER);
        
        // Actualizar la fecha de la cita
        cita.setFechaSolicitud(nuevaFecha);
        cita.setEstatus(estatusPospuesto);
        
        // Si tiene agenda asociada, también actualizar la agenda
        if (cita.getAgenda() != null) {
            Agenda agenda = cita.getAgenda();
            agenda.setFecha(nuevaFecha);
            agenda.setHoraInicio(nuevaFecha.toLocalTime());
            agenda.setHoraFin(nuevaFecha.toLocalTime().plusHours(1));
            agendaService.updateAgenda(agenda.getId(), agenda);
            System.out.println("📅 Agenda actualizada con nueva fecha");
        }
        
        Cita citaGuardada = citaRepository.save(cita);
        citaRepository.flush(); // Forzar la escritura inmediata a la base de datos
        System.out.println("💾 Cambios guardados en la base de datos - Nueva fecha: " + citaGuardada.getFechaSolicitud() + " - Estado: " + citaGuardada.getEstatus().getEstatus());
        
        // Enviar notificación al paciente
        try {
            String emailPaciente = citaGuardada.getPaciente().getUsuario().getCorreoElectronico();
            String nombrePaciente = citaGuardada.getPaciente().getUsuario().getNombre() + " " + 
                                   citaGuardada.getPaciente().getUsuario().getApellidoPaterno();
            String nombreMedico = "Dr. " + citaGuardada.getMedico().getUsuario().getNombre() + " " + 
                                 citaGuardada.getMedico().getUsuario().getApellidoPaterno();
            String especialidad = citaGuardada.getServicio() != null ? citaGuardada.getServicio().getNombreServicio() : "General";
            String nuevaFechaStr = nuevaFecha.format(FORMATTER);
            
            emailService.notificarCambioCita(emailPaciente, nombrePaciente, nombreMedico, especialidad, fechaAnterior, "pospuesta", nuevaFechaStr);
            System.out.println("📧 Email enviado a: " + emailPaciente);
        } catch (Exception e) {
            System.err.println("⚠️ Error al enviar email: " + e.getMessage());
            // No fallar la operación si el email falla
        }
        
        System.out.println("✅ Cita pospuesta exitosamente");
        return citaGuardada;
    }
}
