package com.example.citasmedicas_backend.citas.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.citasmedicas_backend.citas.dto.pagos.EstadoPagoDto;
import com.example.citasmedicas_backend.citas.dto.pagos.MetodoPagoCompletoDto;
import com.example.citasmedicas_backend.citas.dto.pagos.PagoCompletoDto;
import com.example.citasmedicas_backend.citas.dto.pagos.TarjetaCompletoDto;
import com.example.citasmedicas_backend.citas.model.Agenda;
import com.example.citasmedicas_backend.citas.model.Cita;
import com.example.citasmedicas_backend.citas.model.Estatus;
import com.example.citasmedicas_backend.citas.model.Medico;
import com.example.citasmedicas_backend.citas.model.PacienteDetalle;
import com.example.citasmedicas_backend.citas.model.Servicio;
import com.example.citasmedicas_backend.citas.repository.CitaRepository;
import com.example.citasmedicas_backend.citas.repository.EstatusRepository;

@Service
public class CitaService {
    
    private static final Logger log = LoggerFactory.getLogger(CitaService.class);
    
    @Autowired
    private CitaRepository citaRepository;
    
    @Autowired
    private AgendaService agendaService;

    @Autowired
    private EstatusRepository estatusRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private MetodosPagosService metodosPagosService;

    @Autowired
    private ComprobanteService comprobanteService;

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
        
        // Enviar notificación al paciente con PDF
        try {
            String emailPaciente = citaGuardada.getPaciente().getUsuario().getCorreoElectronico();
            String nombrePaciente = citaGuardada.getPaciente().getUsuario().getNombre() + " " + 
                                   citaGuardada.getPaciente().getUsuario().getApellidoPaterno();
            String nombreMedico = "Dr. " + citaGuardada.getMedico().getUsuario().getNombre() + " " + 
                                 citaGuardada.getMedico().getUsuario().getApellidoPaterno();
            String especialidad = citaGuardada.getServicio() != null ? citaGuardada.getServicio().getNombreServicio() : "General";
            String fechaCita = citaGuardada.getFechaSolicitud().format(FORMATTER);
            
            // Enviar email de notificación
            emailService.notificarCambioCita(emailPaciente, nombrePaciente, nombreMedico, especialidad, fechaCita, "aceptada", null);
            System.out.println("📧 Email de notificacion enviado a: " + emailPaciente);
            
            // Si la cita tiene pago, generar y enviar comprobante PDF
            if (citaGuardada.getIdPago() != null && citaGuardada.getMontoPagado() != null) {
                log.info("Generando comprobante PDF para cita aceptada ID: {}", citaGuardada.getId());
                enviarComprobantePDF(citaGuardada);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error al enviar email: " + e.getMessage());
            log.error("Error al enviar notificacion: {}", e.getMessage(), e);
            // No fallar la operación si el email falla
        }
        
        System.out.println("✅ Cita aceptada exitosamente");
        return citaGuardada;
    }

    // Método para cancelar una cita (elimina de la base de datos)
    @Transactional
    public void cancelarCita(Long id) {
        System.out.println("❌ Iniciando proceso de cancelar cita ID: " + id);
        log.info("🔴 Cancelando cita ID: {} - Iniciando proceso de reembolso si aplica", id);
        
        Cita cita = citaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + id));
        
        // Validar que la cita esté en estado válido para ser cancelada
        String estatusActual = cita.getEstatus() != null ? cita.getEstatus().getEstatus() : null;
        if (estatusActual != null && estatusActual.equals("Cancelada")) {
            throw new RuntimeException("La cita ya fue cancelada anteriormente");
        }
        
        // Guardar datos de la cita ANTES de procesar reembolso y eliminar
        String emailPaciente = cita.getPaciente().getUsuario().getCorreoElectronico();
        String nombrePaciente = cita.getPaciente().getUsuario().getNombre() + " " + 
                               cita.getPaciente().getUsuario().getApellidoPaterno();
        String nombreMedico = "Dr. " + cita.getMedico().getUsuario().getNombre() + " " + 
                             cita.getMedico().getUsuario().getApellidoPaterno();
        String especialidad = cita.getServicio() != null ? cita.getServicio().getNombreServicio() : "General";
        String fechaCita = cita.getFechaSolicitud().format(FORMATTER);
        boolean tienePago = cita.getIdPago() != null && "APROBADO".equals(cita.getEstadoPago());
        
        // PASO 1: Procesar reembolso si la cita tiene un pago aprobado (ANTES de eliminar)
        if (tienePago) {
            log.info("💸 La cita tiene un pago aprobado, procesando reembolso automático...");
            try {
                procesarReembolsoPorCancelacion(id, "Cancelación de cita por el médico");
                log.info("✅ Reembolso procesado exitosamente");
            } catch (Exception e) {
                log.error("❌ Error al procesar reembolso: {}", e.getMessage(), e);
                // Continuar con la cancelación aunque falle el reembolso
            }
        }
        
        // PASO 2: Eliminar la cita de la base de datos
        try {
            citaRepository.delete(cita);
            citaRepository.flush(); // Forzar la eliminación inmediata
            System.out.println("🗑️ Cita eliminada de la base de datos - ID: " + id);
            log.info("✅ Cita eliminada exitosamente");
            
            // PASO 3: Enviar notificación al paciente
            emailService.notificarCambioCita(emailPaciente, nombrePaciente, nombreMedico, especialidad, fechaCita, "cancelada", null);
            System.out.println("📧 Email de cancelación enviado a: " + emailPaciente);
        } catch (Exception e) {
            System.err.println("⚠️ Error al procesar la cancelación: " + e.getMessage());
            log.error("❌ Error en cancelación: {}", e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al cancelar la cita: " + e.getMessage());
        }
        
        System.out.println("✅ Cita cancelada y eliminada exitosamente");
        log.info("✅ Proceso de cancelación completado para cita ID: {}", id);
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
        
        // Enviar notificación al paciente con PDF
        try {
            String emailPaciente = citaGuardada.getPaciente().getUsuario().getCorreoElectronico();
            String nombrePaciente = citaGuardada.getPaciente().getUsuario().getNombre() + " " + 
                                   citaGuardada.getPaciente().getUsuario().getApellidoPaterno();
            String nombreMedico = "Dr. " + citaGuardada.getMedico().getUsuario().getNombre() + " " + 
                                 citaGuardada.getMedico().getUsuario().getApellidoPaterno();
            String especialidad = citaGuardada.getServicio() != null ? citaGuardada.getServicio().getNombreServicio() : "General";
            String nuevaFechaStr = nuevaFecha.format(FORMATTER);
            
            // Enviar email de notificación
            emailService.notificarCambioCita(emailPaciente, nombrePaciente, nombreMedico, especialidad, fechaAnterior, "pospuesta", nuevaFechaStr);
            System.out.println("📧 Email de notificacion enviado a: " + emailPaciente);
            
            // Si la cita tiene pago, generar y enviar comprobante PDF actualizado
            if (citaGuardada.getIdPago() != null && citaGuardada.getMontoPagado() != null) {
                log.info("Generando comprobante PDF actualizado para cita pospuesta ID: {}", citaGuardada.getId());
                enviarComprobantePDF(citaGuardada);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error al enviar email: " + e.getMessage());
            log.error("Error al enviar notificacion: {}", e.getMessage(), e);
            // No fallar la operación si el email falla
        }
        
        System.out.println("✅ Cita pospuesta exitosamente");
        return citaGuardada;
    }

    // ===============================
    // MÉTODOS DE PAGO Y REEMBOLSO
    // ===============================

    /**
     * Procesa el pago de una cita
     */
    @Transactional
    public Cita procesarPagoCita(Long citaId, Long idMetodoPago, Long idTarjeta) {
        log.info("💳 Iniciando proceso de pago para cita ID: {}", citaId);
        
        Cita cita = citaRepository.findById(citaId)
            .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + citaId));
        
        // Verificar que la cita no tenga ya un pago procesado
        if (cita.getEstadoPago() != null && cita.getEstadoPago().equals("APROBADO")) {
            throw new RuntimeException("La cita ya tiene un pago aprobado");
        }
        
        // Obtener el costo del servicio
        BigDecimal montoPago = cita.getServicio() != null && cita.getServicio().getCosto() != null
            ? cita.getServicio().getCosto()
            : BigDecimal.valueOf(500.00); // Costo por defecto si no está definido
        
        log.info("💰 Monto a pagar: ${}", montoPago);
        
        try {
            // Crear la solicitud de pago para el microservicio
            PagoCompletoDto pagoRequest = new PagoCompletoDto();
            pagoRequest.setMonto(montoPago);
            pagoRequest.setReferencia("CITA-" + citaId + "-" + System.currentTimeMillis());
            pagoRequest.setDescripcion("Pago por " + 
                (cita.getServicio() != null ? cita.getServicio().getNombreServicio() : "consulta médica"));
            
            // Configurar método de pago
            MetodoPagoCompletoDto metodoPago = new MetodoPagoCompletoDto();
            metodoPago.setId(idMetodoPago);
            pagoRequest.setMetodoPago(metodoPago);
            
            // Estado inicial: APROBADO (simulación automática)
            EstadoPagoDto estadoPago = new EstadoPagoDto();
            estadoPago.setId(2L); // 2 = APROBADO
            estadoPago.setNombre("APROBADO");
            pagoRequest.setEstadoPago(estadoPago);
            
            // Procesar el pago con el microservicio
            PagoCompletoDto pagoResponse = metodosPagosService.procesarPago(pagoRequest);
            
            if (pagoResponse != null && pagoResponse.getId() != null) {
                // Actualizar la cita con la información del pago
                cita.setIdPago(pagoResponse.getId());
                cita.setMontoPagado(pagoResponse.getMonto());
                cita.setEstadoPago(pagoResponse.getEstadoPago() != null ? 
                    pagoResponse.getEstadoPago().getNombre() : "APROBADO");
                cita.setNumeroReferenciaPago(pagoResponse.getReferencia());
                cita.setFechaPago(LocalDateTime.now());
                cita.setMetodoPago(pagoResponse.getMetodoPago() != null ? 
                    pagoResponse.getMetodoPago().getNombreMetodo() : "Tarjeta");
                
                Cita citaActualizada = citaRepository.save(cita);
                log.info("✅ Pago procesado exitosamente - ID Pago: {}, Referencia: {}", 
                    pagoResponse.getId(), pagoResponse.getReferencia());
                
                // Enviar email de confirmación de pago
                enviarEmailConfirmacionPago(citaActualizada);
                
                return citaActualizada;
            } else {
                throw new RuntimeException("Error: El microservicio no devolvió un pago válido");
            }
            
        } catch (Exception ex) {
            log.error("❌ Error procesando pago para cita {}: {}", citaId, ex.getMessage());
            
            // Actualizar estado de pago como RECHAZADO
            cita.setEstadoPago("RECHAZADO");
            citaRepository.save(cita);
            
            throw new RuntimeException("Error al procesar el pago: " + ex.getMessage(), ex);
        }
    }

    /**
     * Procesa el reembolso cuando el médico cancela una cita
     */
    @Transactional
    public void procesarReembolsoPorCancelacion(Long citaId, String motivoCancelacion) {
        log.info("💸 Iniciando proceso de reembolso para cita ID: {}", citaId);
        
        Cita cita = citaRepository.findById(citaId)
            .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + citaId));
        
        // Verificar que la cita tenga un pago aprobado
        if (cita.getIdPago() == null || !"APROBADO".equals(cita.getEstadoPago())) {
            log.warn("⚠️ La cita no tiene un pago aprobado para reembolsar");
            return; // No hay nada que reembolsar
        }
        
        try {
            // Procesar el reembolso con el microservicio
            String motivoCompleto = "Cancelación por el médico: " + motivoCancelacion;
            PagoCompletoDto reembolsoResponse = metodosPagosService.procesarReembolso(
                cita.getIdPago(), motivoCompleto);
            
            if (reembolsoResponse != null) {
                // Actualizar el estado de pago de la cita
                cita.setEstadoPago("REEMBOLSADO");
                citaRepository.save(cita);
                
                log.info("✅ Reembolso procesado exitosamente para cita ID: {}", citaId);
                
                // Enviar email de notificación de reembolso
                enviarEmailReembolso(cita, motivoCancelacion);
            }
            
        } catch (Exception ex) {
            log.error("❌ Error procesando reembolso para cita {}: {}", citaId, ex.getMessage());
            // No lanzamos excepción para no bloquear la cancelación de la cita
        }
    }

    /**
     * Envía email de confirmación de pago
     */
    private void enviarEmailConfirmacionPago(Cita cita) {
        try {
            String emailPaciente = cita.getPaciente().getUsuario().getCorreoElectronico();
            String nombrePaciente = cita.getPaciente().getUsuario().getNombre() + " " + 
                                   cita.getPaciente().getUsuario().getApellidoPaterno();
            
            // Formatear monto con símbolo de moneda
            String montoPagadoStr = cita.getMontoPagado() != null ? 
                String.format("$%.2f MXN", cita.getMontoPagado()) : "$0.00 MXN";
            
            String asunto = "✅ Confirmación de Pago y Cita Médica";
            String mensaje = String.format("""
                <html>
                <head>
                    <style>
                        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f5f5f5; }
                        .container { max-width: 650px; margin: 20px auto; background-color: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
                        .header { background: linear-gradient(135deg, #4CAF50 0%%, #45a049 100%%); padding: 30px 20px; text-align: center; color: white; }
                        .header h1 { margin: 0; font-size: 28px; font-weight: 600; }
                        .header p { margin: 10px 0 0 0; font-size: 16px; opacity: 0.95; }
                        .content { padding: 30px; }
                        .success-badge { background-color: #e8f5e9; color: #2e7d32; padding: 12px 20px; border-radius: 8px; text-align: center; font-weight: bold; font-size: 18px; margin: 20px 0; border-left: 4px solid #4CAF50; }
                        .section { background-color: #f9f9f9; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #2196F3; }
                        .section-title { color: #1976d2; font-size: 18px; font-weight: 600; margin: 0 0 15px 0; display: flex; align-items: center; }
                        .section-title::before { content: '💳'; margin-right: 10px; font-size: 24px; }
                        .info-row { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #e0e0e0; }
                        .info-row:last-child { border-bottom: none; }
                        .info-label { font-weight: 600; color: #666; }
                        .info-value { color: #333; text-align: right; }
                        .highlight { background-color: #fff3e0; color: #e65100; padding: 2px 8px; border-radius: 4px; font-weight: 600; }
                        .appointment-section { background: linear-gradient(135deg, #e3f2fd 0%%, #bbdefb 100%%); padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #2196F3; }
                        .appointment-section .section-title::before { content: '📅'; }
                        .footer { background-color: #f5f5f5; padding: 20px; text-align: center; font-size: 12px; color: #999; border-top: 1px solid #e0e0e0; }
                        .divider { height: 1px; background: linear-gradient(90deg, transparent, #e0e0e0, transparent); margin: 25px 0; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>💳 Pago Confirmado</h1>
                            <p>Tu cita médica ha sido agendada exitosamente</p>
                        </div>
                        
                        <div class="content">
                            <p style="font-size: 16px; margin-bottom: 10px;">Estimado/a <strong>%s</strong>,</p>
                            <p style="color: #666;">Le confirmamos que su pago ha sido procesado correctamente y su cita médica ha sido registrada en nuestro sistema.</p>
                            
                            <div class="success-badge">
                                ✓ PAGO APROBADO
                            </div>
                            
                            <div class="section">
                                <h3 class="section-title">Información del Pago</h3>
                                <div class="info-row">
                                    <span class="info-label">💰 Monto Pagado:</span>
                                    <span class="info-value highlight">%s</span>
                                </div>
                                <div class="info-row">
                                    <span class="info-label">🔖 Referencia:</span>
                                    <span class="info-value">%s</span>
                                </div>
                                <div class="info-row">
                                    <span class="info-label">💳 Método de Pago:</span>
                                    <span class="info-value">%s</span>
                                </div>
                                <div class="info-row">
                                    <span class="info-label">📅 Fecha de Pago:</span>
                                    <span class="info-value">%s</span>
                                </div>
                                <div class="info-row">
                                    <span class="info-label">📊 Estado:</span>
                                    <span class="info-value" style="color: #4CAF50; font-weight: bold;">%s</span>
                                </div>
                            </div>
                            
                            <div class="appointment-section">
                                <h3 class="section-title">Detalles de la Cita</h3>
                                <div class="info-row">
                                    <span class="info-label">🏥 Servicio:</span>
                                    <span class="info-value">%s</span>
                                </div>
                                <div class="info-row">
                                    <span class="info-label">👨‍⚕️ Médico:</span>
                                    <span class="info-value">Dr. %s %s</span>
                                </div>
                                <div class="info-row">
                                    <span class="info-label">📅 Fecha y Hora:</span>
                                    <span class="info-value highlight">%s</span>
                                </div>
                                <div class="info-row">
                                    <span class="info-label">🆔 ID de Cita:</span>
                                    <span class="info-value">#%s</span>
                                </div>
                            </div>
                            
                            <div class="divider"></div>
                            
                            <p style="background-color: #e3f2fd; padding: 15px; border-radius: 8px; border-left: 4px solid #2196F3; margin: 20px 0;">
                                <strong>📝 Nota importante:</strong> Por favor llegue 10 minutos antes de su cita. 
                                Recuerde llevar su identificación y este comprobante de pago.
                            </p>
                            
                            <p style="text-align: center; color: #666; margin-top: 30px;">
                                Gracias por confiar en nosotros para su atención médica.
                            </p>
                        </div>
                        
                        <div class="footer">
                            <p style="margin: 5px 0;"><strong>Sistema de Citas Médicas</strong></p>
                            <p style="margin: 5px 0;">Este es un correo automático, por favor no responder.</p>
                            <p style="margin: 5px 0; color: #bbb;">© 2025 Citas Médicas. Todos los derechos reservados.</p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                nombrePaciente,
                montoPagadoStr,
                cita.getNumeroReferenciaPago() != null ? cita.getNumeroReferenciaPago() : "N/A",
                cita.getMetodoPago() != null ? cita.getMetodoPago() : "Tarjeta de Crédito",
                cita.getFechaPago() != null ? cita.getFechaPago().format(FORMATTER) : "N/A",
                cita.getEstadoPago() != null ? cita.getEstadoPago() : "APROBADO",
                cita.getServicio() != null ? cita.getServicio().getNombreServicio() : "Consulta General",
                cita.getMedico().getUsuario().getNombre(),
                cita.getMedico().getUsuario().getApellidoPaterno(),
                cita.getFechaSolicitud().format(FORMATTER),
                cita.getId()
            );
            
            // Generar el PDF del comprobante
            log.info("=== INICIANDO GENERACION DE COMPROBANTE PDF ===");
            log.info("Generando comprobante PDF para la cita ID: {}", cita.getId());
            byte[] pdfComprobante = comprobanteService.generarComprobantePDF(cita);
            log.info("PDF generado exitosamente. Tamanio: {} bytes", pdfComprobante.length);
            
            // Nombre del archivo PDF
            String nombreArchivoPdf = String.format("Comprobante_Cita_%s_%s.pdf", 
                    cita.getId(), 
                    cita.getNumeroReferenciaPago() != null ? 
                            cita.getNumeroReferenciaPago().replace("CITA-", "") : 
                            System.currentTimeMillis());
            log.info("Nombre del archivo PDF: {}", nombreArchivoPdf);
            
            // Enviar email con el PDF adjunto
            log.info("Enviando email a: {} con adjunto PDF", emailPaciente);
            emailService.enviarEmailConAdjunto(emailPaciente, asunto, mensaje, pdfComprobante, nombreArchivoPdf);
            log.info("=== EMAIL CON COMPROBANTE PDF ENVIADO EXITOSAMENTE ===");
            
        } catch (Exception e) {
            log.error("=== ERROR AL ENVIAR EMAIL CON PDF ===");
            log.error("Mensaje de error: {}", e.getMessage());
            log.error("Tipo de excepcion: {}", e.getClass().getName());
            e.printStackTrace();
        }
    }

    /**
     * Envía email de notificación de reembolso
     */
    private void enviarEmailReembolso(Cita cita, String motivo) {
        try {
            String emailPaciente = cita.getPaciente().getUsuario().getCorreoElectronico();
            String nombrePaciente = cita.getPaciente().getUsuario().getNombre() + " " + 
                                   cita.getPaciente().getUsuario().getApellidoPaterno();
            
            String asunto = "💸 Reembolso Procesado - Cancelación de Cita";
            String mensaje = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px;">
                        <h2 style="color: #2196F3; text-align: center;">💸 Reembolso Procesado</h2>
                        <p>Estimado/a <strong>%s</strong>,</p>
                        <p>Lamentamos informarle que su cita ha sido cancelada por el médico.</p>
                        <p><strong>Motivo:</strong> %s</p>
                        <div style="background-color: #e3f2fd; padding: 15px; border-radius: 5px; margin: 20px 0;">
                            <h3 style="color: #2196F3; margin-top: 0;">Información del Reembolso</h3>
                            <p><strong>Monto reembolsado:</strong> $%s</p>
                            <p><strong>Referencia del pago:</strong> %s</p>
                            <p><strong>Método de pago original:</strong> %s</p>
                        </div>
                        <div style="background-color: #fff3e0; padding: 15px; border-radius: 5px; margin: 20px 0;">
                            <h3 style="color: #FF9800; margin-top: 0;">Detalles de la Cita Cancelada</h3>
                            <p><strong>Servicio:</strong> %s</p>
                            <p><strong>Médico:</strong> Dr. %s %s</p>
                            <p><strong>Fecha programada:</strong> %s</p>
                        </div>
                        <p style="color: #4CAF50; font-weight: bold;">✓ El reembolso será procesado en 3-5 días hábiles</p>
                        <p>Puede agendar una nueva cita cuando lo desee.</p>
                        <hr style="margin: 30px 0; border: none; border-top: 1px solid #e0e0e0;">
                        <p style="font-size: 12px; color: #999; text-align: center;">
                            Sistema de Citas Médicas<br>
                            Este es un correo automático, por favor no responder.
                        </p>
                    </div>
                </body>
                </html>
                """,
                nombrePaciente,
                motivo,
                cita.getMontoPagado(),
                cita.getNumeroReferenciaPago(),
                cita.getMetodoPago(),
                cita.getServicio() != null ? cita.getServicio().getNombreServicio() : "Consulta",
                cita.getMedico().getUsuario().getNombre(),
                cita.getMedico().getUsuario().getApellidoPaterno(),
                cita.getFechaSolicitud().format(FORMATTER)
            );
            
            emailService.enviarEmailHtml(emailPaciente, asunto, mensaje);
            log.info("📧 Email de reembolso enviado a: {}", emailPaciente);
            
        } catch (Exception e) {
            log.error("⚠️ Error al enviar email de reembolso: {}", e.getMessage());
        }
    }

    /**
     * Genera y envía comprobante PDF por email
     * Método reutilizable para enviar comprobante en diferentes estados de la cita
     */
    private void enviarComprobantePDF(Cita cita) {
        try {
            String emailPaciente = cita.getPaciente().getUsuario().getCorreoElectronico();
            String nombrePaciente = cita.getPaciente().getUsuario().getNombre() + " " + 
                                   cita.getPaciente().getUsuario().getApellidoPaterno();
            
            // Formatear monto con símbolo de moneda
            String montoPagadoStr = cita.getMontoPagado() != null ? 
                String.format("$%.2f MXN", cita.getMontoPagado()) : "$0.00 MXN";
            
            // Determinar el estado para el asunto del email
            String estadoCita = cita.getEstatus() != null ? cita.getEstatus().getEstatus() : "Confirmada";
            String asunto = String.format("Comprobante de Cita %s - ID #%s", estadoCita, cita.getId());
            
            String mensaje = String.format("""
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }
                        .container { max-width: 600px; margin: 20px auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px; }
                        .header { background: linear-gradient(135deg, #4CAF50 0%%, #45a049 100%%); color: white; padding: 20px; text-align: center; border-radius: 8px; margin-bottom: 20px; }
                        .info { background-color: #f9f9f9; padding: 15px; border-radius: 5px; margin: 15px 0; }
                        .footer { text-align: center; font-size: 12px; color: #999; margin-top: 30px; padding-top: 20px; border-top: 1px solid #e0e0e0; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h2 style="margin: 0;">Comprobante de Cita Medica</h2>
                            <p style="margin: 5px 0 0 0;">Estado: %s</p>
                        </div>
                        <p>Estimado/a <strong>%s</strong>,</p>
                        <p>Adjunto encontrara el comprobante en PDF de su cita medica.</p>
                        <div class="info">
                            <p><strong>ID de Cita:</strong> #%s</p>
                            <p><strong>Monto Pagado:</strong> %s</p>
                            <p><strong>Servicio:</strong> %s</p>
                            <p><strong>Medico:</strong> Dr. %s %s</p>
                            <p><strong>Fecha:</strong> %s</p>
                        </div>
                        <p>Por favor conserve este comprobante para su registro.</p>
                        <div class="footer">
                            <p><strong>Sistema de Citas Medicas</strong></p>
                            <p>Este es un correo automatico, por favor no responder.</p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                estadoCita,
                nombrePaciente,
                cita.getId(),
                montoPagadoStr,
                cita.getServicio() != null ? cita.getServicio().getNombreServicio() : "Consulta General",
                cita.getMedico().getUsuario().getNombre(),
                cita.getMedico().getUsuario().getApellidoPaterno(),
                cita.getFechaSolicitud().format(FORMATTER)
            );
            
            // Generar el PDF del comprobante
            log.info("=== GENERANDO COMPROBANTE PDF ===");
            byte[] pdfComprobante = comprobanteService.generarComprobantePDF(cita);
            log.info("PDF generado exitosamente. Tamanio: {} bytes", pdfComprobante.length);
            
            // Nombre del archivo PDF
            String nombreArchivoPdf = String.format("Comprobante_Cita_%s_%s_%s.pdf", 
                    cita.getId(),
                    estadoCita.replace(" ", "_"),
                    System.currentTimeMillis());
            log.info("Nombre del archivo PDF: {}", nombreArchivoPdf);
            
            // Enviar email con el PDF adjunto
            log.info("Enviando email a: {} con adjunto PDF", emailPaciente);
            emailService.enviarEmailConAdjunto(emailPaciente, asunto, mensaje, pdfComprobante, nombreArchivoPdf);
            log.info("=== EMAIL CON COMPROBANTE PDF ENVIADO EXITOSAMENTE ===");
            
        } catch (Exception e) {
            log.error("=== ERROR AL ENVIAR COMPROBANTE PDF ===");
            log.error("Mensaje de error: {}", e.getMessage());
            log.error("Tipo de excepcion: {}", e.getClass().getName());
            e.printStackTrace();
        }
    }

    /**
     * 🆕 NUEVO FLUJO: Crea una cita CON pago previo
     * 
     * Este método:
     * 1. Primero SIMULA el pago con el microservicio de pagos
     * 2. Si el pago es APROBADO, entonces crea la cita con los datos del pago
     * 3. Si el pago es RECHAZADO, lanza excepción y NO se crea la cita
     * 
     * @param citaData Datos de la cita (pacienteId, medicoId, servicioId, fechaHora, motivo)
     * @param pagoData Datos del pago (idMetodoPago, idTarjeta, etc)
     * @return Cita creada con datos de pago incluidos
     */
    @Transactional
    public Cita crearCitaConPagoPrevio(Map<String, Object> citaData, Map<String, Object> pagoData) {
        log.info("💳 Iniciando proceso: PAGO -> CITA");
        log.info("📥 citaData recibido: {}", citaData);
        log.info("📥 pagoData recibido: {}", pagoData);
        
        try {
            // PASO 1: Construir la cita temporalmente (sin guardar) para obtener el servicio y su costo
            log.info("🔨 Construyendo cita desde Map...");
            Cita citaTemporal = construirCitaDesdeMap(citaData);
            log.info("✅ Cita temporal construida correctamente");
            
            // Obtener el costo del servicio
            BigDecimal costoServicio = citaTemporal.getServicio().getCosto();
            log.info("💰 Costo del servicio: ${}", costoServicio);
            
            // PASO 2: Preparar y procesar el pago con el microservicio
            Long idMetodoPago = (pagoData.containsKey("idMetodoPago") && pagoData.get("idMetodoPago") != null)
                ? ((Number) pagoData.get("idMetodoPago")).longValue() 
                : 1L; // Default: Tarjeta de crédito
            
            log.info("💳 ID Método de Pago: {}", idMetodoPago);
                
            Long idTarjeta = (pagoData.containsKey("idTarjeta") && pagoData.get("idTarjeta") != null)
                ? ((Number) pagoData.get("idTarjeta")).longValue() 
                : null;
            
            log.info("💳 ID Tarjeta: {}", idTarjeta);
            
            // Consultar el método de pago completo
            MetodoPagoCompletoDto metodoPago = metodosPagosService.obtenerMetodoPagoPorId(idMetodoPago);
            
            // Crear DTO de pago
            PagoCompletoDto pagoDto = new PagoCompletoDto();
            pagoDto.setMonto(costoServicio);
            pagoDto.setMetodoPago(metodoPago);
            pagoDto.setDescripcion("Pago de cita médica - " + citaTemporal.getServicio().getNombreServicio());
            pagoDto.setReferencia("CITA-" + System.currentTimeMillis());
            
            // Crear tarjeta si viene idTarjeta
            if (idTarjeta != null) {
                TarjetaCompletoDto tarjeta = new TarjetaCompletoDto();
                tarjeta.setId(idTarjeta);
                pagoDto.setTarjeta(tarjeta);
            }
            
            log.info("🔄 Procesando pago con microservicio...");
            PagoCompletoDto resultadoPago = metodosPagosService.procesarPago(pagoDto);
            
            // PASO 3: Validar el resultado del pago
            if (resultadoPago == null) {
                log.error("❌ Respuesta NULL del microservicio de pagos");
                throw new RuntimeException("❌ Error al procesar el pago. Intente nuevamente.");
            }
            
            log.info("📦 Respuesta del microservicio - ID: {}, Monto: {}, Referencia: {}", 
                resultadoPago.getId(), resultadoPago.getMonto(), resultadoPago.getReferencia());
            
            // Determinar el estado del pago
            String estadoPago = "APROBADO"; // Default si viene sin estado
            
            if (resultadoPago.getEstadoPago() != null && resultadoPago.getEstadoPago().getNombre() != null) {
                estadoPago = resultadoPago.getEstadoPago().getNombre();
                log.info("📊 Estado del pago desde microservicio: {}", estadoPago);
                
                if (!"APROBADO".equalsIgnoreCase(estadoPago)) {
                    log.error("❌ Pago RECHAZADO: {}", estadoPago);
                    throw new RuntimeException("❌ Pago rechazado: " + estadoPago + 
                        ". No se creará la cita hasta que el pago sea exitoso.");
                }
            } else {
                log.warn("⚠️ El microservicio no devolvió estadoPago, asumiendo APROBADO porque retornó ID: {}", resultadoPago.getId());
            }
            
            log.info("✅ Pago APROBADO - Referencia: {}", resultadoPago.getReferencia());
            
            // PASO 4: Ahora SÍ guardamos la cita con los datos del pago
            citaTemporal.setMontoPagado(resultadoPago.getMonto());
            citaTemporal.setIdPago(resultadoPago.getId());
            citaTemporal.setEstadoPago("APROBADO");
            citaTemporal.setNumeroReferenciaPago(resultadoPago.getReferencia());
            citaTemporal.setFechaPago(LocalDateTime.now());
            
            // Establecer método de pago (manejar el caso cuando metodoPago es null)
            String nombreMetodoPago = "Tarjeta de Crédito"; // Default
            if (metodoPago != null && metodoPago.getNombreMetodo() != null) {
                nombreMetodoPago = metodoPago.getNombreMetodo();
                log.info("💳 Método de pago utilizado: {}", nombreMetodoPago);
            } else {
                log.warn("⚠️ No se pudo obtener el nombre del método de pago, usando default: {}", nombreMetodoPago);
            }
            citaTemporal.setMetodoPago(nombreMetodoPago);
            
            // Configurar estatus inicial (Pendiente de Confirmación)
            Estatus estatusInicial = estatusRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("No se encontró el estatus inicial"));
            citaTemporal.setEstatus(estatusInicial);
            
            // Guardar la cita
            Cita citaGuardada = citaRepository.save(citaTemporal);
            log.info("✅ Cita creada exitosamente con ID: {} y pago asociado", citaGuardada.getId());
            
            // PASO 5: Crear agenda automáticamente si no tiene una asignada
            if (citaGuardada.getAgenda() == null && citaGuardada.getFechaSolicitud() != null) {
                log.info("📅 Creando agenda automáticamente para la cita...");
                
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
                log.info("✅ Agenda creada con ID: {}", agendaGuardada.getId());
                
                // Actualizar la cita con la agenda creada
                citaGuardada.setAgenda(agendaGuardada);
                citaGuardada = citaRepository.save(citaGuardada);
                log.info("✅ Cita actualizada con agenda ID: {}", agendaGuardada.getId());
            } else if (citaGuardada.getAgenda() != null) {
                log.info("📅 Cita ya tiene agenda asignada: {}", citaGuardada.getAgenda().getId());
            }
            
            // PASO 6: Enviar email de confirmación
            enviarEmailConfirmacionPago(citaGuardada);
            
            return citaGuardada;
            
        } catch (Exception e) {
            log.error("❌ Error en el proceso de pago y creación de cita: {}", e.getMessage());
            throw new RuntimeException("Error al procesar pago y crear cita: " + e.getMessage());
        }
    }

    /**
     * Construye una entidad Cita desde un Map (sin guardarla en BD)
     */
    private Cita construirCitaDesdeMap(Map<String, Object> citaData) {
        log.info("🏗️ Iniciando construcción de cita desde Map");
        Cita cita = new Cita();
        
        // Paciente (solo referencia por ID)
        if (citaData.containsKey("pacienteId") && citaData.get("pacienteId") != null) {
            log.info("👤 Procesando pacienteId: {}", citaData.get("pacienteId"));
            PacienteDetalle paciente = new PacienteDetalle();
            paciente.setId(((Number) citaData.get("pacienteId")).longValue());
            cita.setPaciente(paciente);
        } else {
            log.warn("⚠️ pacienteId no encontrado o es null en citaData");
            throw new RuntimeException("El campo 'pacienteId' es requerido");
        }
        
        // Médico (solo referencia por ID)
        if (citaData.containsKey("medicoId") && citaData.get("medicoId") != null) {
            log.info("👨‍⚕️ Procesando medicoId: {}", citaData.get("medicoId"));
            Medico medico = new Medico();
            medico.setId(((Number) citaData.get("medicoId")).longValue());
            cita.setMedico(medico);
        } else {
            log.warn("⚠️ medicoId no encontrado o es null en citaData");
            throw new RuntimeException("El campo 'medicoId' es requerido");
        }
        
        // Servicio (NECESITAMOS cargar el servicio completo para obtener el costo)
        if (citaData.containsKey("servicioId") && citaData.get("servicioId") != null) {
            log.info("💼 Procesando servicioId: {}", citaData.get("servicioId"));
            Long servicioId = ((Number) citaData.get("servicioId")).longValue();
            log.info("🔍 Buscando servicio en base de datos...");
            Servicio servicio = citaRepository.findById(servicioId)
                .map(c -> c.getServicio()) // Obtener servicio de una cita existente
                .orElseGet(() -> {
                    // Si no hay citas, crear referencia básica
                    Servicio s = new Servicio();
                    s.setId(servicioId);
                    s.setCosto(new BigDecimal("500.00")); // Costo por defecto
                    s.setNombreServicio("Consulta General");
                    return s;
                });
            cita.setServicio(servicio);
        } else {
            log.warn("⚠️ servicioId no encontrado o es null en citaData");
            throw new RuntimeException("El campo 'servicioId' es requerido");
        }
        
        // Agenda (opcional)
        if (citaData.containsKey("agendaId") && citaData.get("agendaId") != null) {
            Agenda agenda = new Agenda();
            agenda.setId(((Number) citaData.get("agendaId")).longValue());
            cita.setAgenda(agenda);
        }
        
        // Fecha/hora
        if (citaData.containsKey("fechaHora")) {
            String fechaHoraStr = (String) citaData.get("fechaHora");
            log.info("📅 Procesando fechaHora: {}", fechaHoraStr);
            // Manejar formato ISO 8601 con zona horaria (ej: "2025-10-31T09:00:00.000Z")
            try {
                // Si viene con 'Z' o zona horaria, usar Instant y convertir a LocalDateTime
                if (fechaHoraStr.contains("Z") || fechaHoraStr.contains("+") || fechaHoraStr.matches(".*-\\d{2}:\\d{2}$")) {
                    log.info("🌐 Parseando fecha con zona horaria ISO 8601");
                    java.time.Instant instant = java.time.Instant.parse(fechaHoraStr);
                    LocalDateTime fechaLocal = LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault());
                    log.info("✅ Fecha convertida: {}", fechaLocal);
                    cita.setFechaSolicitud(fechaLocal);
                } else {
                    // Formato simple sin zona horaria
                    log.info("📆 Parseando fecha sin zona horaria");
                    cita.setFechaSolicitud(LocalDateTime.parse(fechaHoraStr.substring(0, 19)));
                }
            } catch (Exception e) {
                log.error("⚠️ Error parseando fecha: {} - {}", fechaHoraStr, e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Formato de fecha inválido: " + fechaHoraStr);
            }
        } else {
            log.warn("⚠️ No se encontró fechaHora en citaData");
        }
        
        // Motivo
        if (citaData.containsKey("motivo")) {
            log.info("📝 Procesando motivo: {}", citaData.get("motivo"));
            cita.setMotivo((String) citaData.get("motivo"));
        } else {
            log.warn("⚠️ No se encontró motivo en citaData");
        }
        
        log.info("✅ Cita construida exitosamente desde Map");
        
        return cita;
    }
}
