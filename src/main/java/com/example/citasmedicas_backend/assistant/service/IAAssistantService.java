package com.example.citasmedicas_backend.assistant.service;

import com.example.citasmedicas_backend.citas.model.*;
import com.example.citasmedicas_backend.citas.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio que expone funciones para que la IA las llame
 * Cada función consulta la base de datos y devuelve solo datos necesarios
 */
@Service
@Transactional(readOnly = true)
public class IAAssistantService {

    private static final Logger log = LoggerFactory.getLogger(IAAssistantService.class);

    private final ServicioRepository servicioRepository;
    private final MedicoRepository medicoRepository;
    private final AgendaRepository agendaRepository;
    private final HorarioMedicoRepository horarioMedicoRepository;
    private final AreasRepository areasRepository;
    private final CitaRepository citaRepository;
    private final PacienteRepository pacienteRepository;
    private final EstatusRepository estatusRepository;

    public IAAssistantService(
        ServicioRepository servicioRepository,
        MedicoRepository medicoRepository,
        AgendaRepository agendaRepository,
        HorarioMedicoRepository horarioMedicoRepository,
        AreasRepository areasRepository,
        CitaRepository citaRepository,
        PacienteRepository pacienteRepository,
        EstatusRepository estatusRepository
    ) {
        this.servicioRepository = servicioRepository;
        this.medicoRepository = medicoRepository;
        this.agendaRepository = agendaRepository;
        this.horarioMedicoRepository = horarioMedicoRepository;
        this.areasRepository = areasRepository;
        this.citaRepository = citaRepository;
        this.pacienteRepository = pacienteRepository;
        this.estatusRepository = estatusRepository;
    }

    /**
     * Obtiene todas las áreas médicas disponibles
     */
    public Map<String, Object> obtenerAreas() {
        log.info("🤖 IA solicitó obtener áreas médicas");
        
        List<Area> areas = areasRepository.findAll();
        
        List<Map<String, Object>> areasData = areas.stream()
            .map(area -> {
                Map<String, Object> areaMap = new HashMap<>();
                areaMap.put("id", area.getId());
                areaMap.put("nombre", area.getNombreArea());
                return areaMap;
            })
            .collect(Collectors.toList());
        
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("exito", true);
        resultado.put("areas", areasData);
        resultado.put("total", areasData.size());
        
        return resultado;
    }

    /**
     * Obtiene servicios médicos disponibles
     */
    public Map<String, Object> obtenerServicios() {
        log.info("🤖 IA solicitó obtener servicios");
        
        List<Servicio> servicios = servicioRepository.findAll();
        
        List<Map<String, Object>> serviciosData = servicios.stream()
            .map(s -> {
                Map<String, Object> servicioMap = new HashMap<>();
                servicioMap.put("id", s.getId());
                servicioMap.put("nombre", s.getNombreServicio());
                servicioMap.put("descripcion", s.getDescripcionServicio());
                servicioMap.put("costo", s.getCosto());
                if (s.getArea() != null) {
                    servicioMap.put("area", s.getArea().getNombreArea());
                    servicioMap.put("areaId", s.getArea().getId());
                }
                return servicioMap;
            })
            .collect(Collectors.toList());
        
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("exito", true);
        resultado.put("servicios", serviciosData);
        resultado.put("total", serviciosData.size());
        
        return resultado;
    }

    /**
     * Busca médicos (opcionalmente por servicio)
     */
    public Map<String, Object> buscarMedicos(Long servicioId) {
        log.info("🤖 IA solicitó buscar médicos (servicio: {})", servicioId);
        
        List<Medico> medicos;
        String servicioNombre = null;
        
        if (servicioId != null) {
            Optional<Servicio> servicioOpt = servicioRepository.findById(servicioId);
            if (servicioOpt.isEmpty()) {
                return Map.of("exito", false, "mensaje", "Servicio no encontrado");
            }
            servicioNombre = servicioOpt.get().getNombreServicio();
            medicos = medicoRepository.findAll().stream()
                .filter(m -> m.getServicio() != null && m.getServicio().getId().equals(servicioId))
                .collect(Collectors.toList());
        } else {
            medicos = medicoRepository.findAll();
        }
        
        List<Map<String, Object>> medicosData = medicos.stream()
            .map(m -> {
                Map<String, Object> medicoMap = new HashMap<>();
                medicoMap.put("id", m.getId());
                if (m.getUsuario() != null) {
                    String nombreCompleto = m.getUsuario().getNombre() + " " + 
                                           m.getUsuario().getApellidoPaterno();
                    if (m.getUsuario().getApellidoMaterno() != null) {
                        nombreCompleto += " " + m.getUsuario().getApellidoMaterno();
                    }
                    medicoMap.put("nombre", nombreCompleto);
                }
                medicoMap.put("cedula", m.getCedulaProfecional());
                if (m.getServicio() != null) {
                    medicoMap.put("servicio", m.getServicio().getNombreServicio());
                }
                return medicoMap;
            })
            .collect(Collectors.toList());
        
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("exito", true);
        resultado.put("medicos", medicosData);
        if (servicioNombre != null) {
            resultado.put("servicio", servicioNombre);
        }
        resultado.put("total", medicosData.size());
        
        return resultado;
    }

    /**
     * Obtiene próximos horarios disponibles de un médico (siguientes 7 días)
     * Si medicoId es null, devuelve horarios de TODOS los médicos
     */
    public Map<String, Object> obtenerProximosHorariosDisponibles(Long medicoId) {
        log.info("🤖 IA solicitó próximos horarios del médico: {}", medicoId);
        
        try {
            LocalDate hoy = LocalDate.now();
            LocalDate finVentana = hoy.plusDays(30); // Extender a 30 días para encontrar más horarios
            
            List<Map<String, Object>> horariosDisponibles = new ArrayList<>();
            
            // Obtener todos los horarios médicos disponibles (filtrados por médico si se especifica)
            List<HorarioMedico> horarios = horarioMedicoRepository.findAll().stream()
                .filter(h -> h != null && h.getFecha() != null && h.getHorarioInicio() != null && h.getHorarioFin() != null)
                .filter(h -> medicoId == null || (h.getMedico() != null && h.getMedico().getId() != null && h.getMedico().getId().equals(medicoId)))
                .filter(h -> {
                    try {
                        LocalDate fecha = h.getFecha();
                        // Incluir desde hoy en adelante hasta 30 días
                        return !fecha.isBefore(hoy) && !fecha.isAfter(finVentana);
                    } catch (Exception e) {
                        log.warn("Error procesando fecha de horario {}: {}", h.getId(), e.getMessage());
                        return false;
                    }
                })
                .filter(h -> h.getEstadoMedico() == EstadoMedico.DISPONIBLE) // Solo horarios disponibles
                .sorted((h1, h2) -> h1.getFecha().compareTo(h2.getFecha())) // Ordenar por fecha
                .collect(Collectors.toList());
            
            log.info("📋 Encontrados {} horarios disponibles en el rango de fechas (desde {} hasta {})", 
                     horarios.size(), hoy, finVentana);
            
            for (HorarioMedico horario : horarios) {
                try {
                    Map<String, Object> horarioMap = new HashMap<>();
                    horarioMap.put("horarioId", horario.getId());
                    horarioMap.put("medicoId", horario.getMedico() != null && horario.getMedico().getId() != null ? horario.getMedico().getId() : null);
                    
                    // Obtener nombre del médico desde Usuario
                    String medicoNombre = "Desconocido";
                    if (horario.getMedico() != null && horario.getMedico().getUsuario() != null) {
                        Usuario usuario = horario.getMedico().getUsuario();
                        medicoNombre = (usuario.getNombre() != null ? usuario.getNombre() : "") + " " +
                                      (usuario.getApellidoPaterno() != null ? usuario.getApellidoPaterno() : "");
                        medicoNombre = medicoNombre.trim();
                        if (medicoNombre.isEmpty()) {
                            medicoNombre = "Desconocido";
                        }
                    }
                    horarioMap.put("medicoNombre", medicoNombre);
                    
                    horarioMap.put("fecha", horario.getFecha().toString());
                    horarioMap.put("diaSemana", horario.getFecha().getDayOfWeek().toString());
                    horarioMap.put("horaInicio", horario.getHorarioInicio().toString());
                    horarioMap.put("horaFin", horario.getHorarioFin().toString());
                    horarioMap.put("duracion", horario.getDuracion());
                    horarioMap.put("estado", horario.getEstadoMedico().toString());
                    
                    horariosDisponibles.add(horarioMap);
                } catch (Exception e) {
                    log.error("Error procesando horario {}: {}", horario.getId(), e.getMessage());
                }
            }
            
            log.info("✅ Retornando {} horarios disponibles", horariosDisponibles.size());
            
            // Si no hay horarios en el futuro, mostrar todos los disponibles para debugging
            if (horariosDisponibles.isEmpty()) {
                log.warn("⚠️ No se encontraron horarios futuros. Mostrando todos los horarios DISPONIBLES:");
                List<HorarioMedico> todosDisponibles = horarioMedicoRepository.findAll().stream()
                    .filter(h -> h.getEstadoMedico() == EstadoMedico.DISPONIBLE)
                    .collect(Collectors.toList());
                
                for (HorarioMedico h : todosDisponibles) {
                    log.info("  - ID: {}, Médico: {}, Fecha: {}, Hora: {}-{}", 
                             h.getId(), h.getMedico() != null ? h.getMedico().getId() : "N/A",
                             h.getFecha(), h.getHorarioInicio(), h.getHorarioFin());
                    
                    // Agregar todos los horarios disponibles sin importar la fecha
                    Map<String, Object> horarioMap = new HashMap<>();
                    horarioMap.put("horarioId", h.getId());
                    horarioMap.put("medicoId", h.getMedico() != null ? h.getMedico().getId() : null);
                    
                    String medicoNombre = "Desconocido";
                    if (h.getMedico() != null && h.getMedico().getUsuario() != null) {
                        Usuario usuario = h.getMedico().getUsuario();
                        medicoNombre = (usuario.getNombre() != null ? usuario.getNombre() : "") + " " +
                                      (usuario.getApellidoPaterno() != null ? usuario.getApellidoPaterno() : "");
                        medicoNombre = medicoNombre.trim();
                    }
                    horarioMap.put("medicoNombre", medicoNombre);
                    horarioMap.put("fecha", h.getFecha().toString());
                    horarioMap.put("diaSemana", h.getFecha().getDayOfWeek().toString());
                    horarioMap.put("horaInicio", h.getHorarioInicio().toString());
                    horarioMap.put("horaFin", h.getHorarioFin().toString());
                    horarioMap.put("duracion", h.getDuracion());
                    horarioMap.put("estado", h.getEstadoMedico().toString());
                    horarioMap.put("advertencia", "Fecha pasada o fuera de rango");
                    
                    horariosDisponibles.add(horarioMap);
                }
            }
            
            return Map.of(
                "exito", true,
                "horarios", horariosDisponibles,
                "total", horariosDisponibles.size(),
                "periodo", "Próximos 30 días desde " + hoy,
                "mensaje", medicoId == null ? "Horarios de todos los médicos" : "Horarios del médico " + medicoId
            );
        } catch (Exception e) {
            log.error("❌ Error al obtener horarios disponibles: {}", e.getMessage(), e);
            return Map.of(
                "exito", false,
                "mensaje", "Error al obtener horarios: " + e.getMessage(),
                "horarios", new ArrayList<>(),
                "total", 0
            );
        }
    }

    /**
     * Obtiene las citas de un paciente
     */
    public Map<String, Object> obtenerCitasPaciente(Long pacienteId) {
        log.info("🤖 IA solicitó citas del paciente: {}", pacienteId);
        
        if (pacienteId == null) {
            return Map.of("exito", false, "mensaje", "Se requiere el ID del paciente");
        }
        
        List<Cita> citas = citaRepository.findAll().stream()
            .filter(c -> c.getPaciente() != null && c.getPaciente().getId().equals(pacienteId))
            .collect(Collectors.toList());
        
        List<Map<String, Object>> citasData = citas.stream()
            .map(c -> {
                Map<String, Object> citaMap = new HashMap<>();
                citaMap.put("id", c.getId());
                if (c.getAgenda() != null) {
                    citaMap.put("fecha", c.getAgenda().getFecha().toLocalDate().toString());
                    citaMap.put("hora", c.getAgenda().getHoraInicio().toString());
                }
                if (c.getMedico() != null && c.getMedico().getUsuario() != null) {
                    citaMap.put("medico", c.getMedico().getUsuario().getNombre() + " " +
                                         c.getMedico().getUsuario().getApellidoPaterno());
                }
                if (c.getServicio() != null) {
                    citaMap.put("servicio", c.getServicio().getNombreServicio());
                }
                // El estado se puede obtener del estatus si existe
                citaMap.put("estado", "ACTIVA");
                citaMap.put("motivo", c.getMotivo());
                return citaMap;
            })
            .collect(Collectors.toList());
        
        return Map.of(
            "exito", true,
            "citas", citasData,
            "total", citasData.size()
        );
    }

    /**
     * Crea una nueva cita
     */
    @Transactional
    public Map<String, Object> crearCita(Long pacienteId, Long medicoId, Long servicioId, 
                                        Long agendaId, String motivo) {
        log.info("🤖 IA solicitó crear cita (paciente: {}, médico: {}, servicio: {}, agenda: {})", 
                 pacienteId, medicoId, servicioId, agendaId);
        
        try {
            // Validaciones
            if (pacienteId == null || medicoId == null || servicioId == null || agendaId == null) {
                return Map.of("exito", false, "mensaje", "Faltan datos requeridos para crear la cita");
            }
            
            // Verificar que la agenda existe
            Optional<Agenda> agendaOpt = agendaRepository.findById(agendaId);
            if (agendaOpt.isEmpty()) {
                return Map.of("exito", false, "mensaje", "Horario no encontrado");
            }
            
            // Verificar que no esté ocupada
            boolean agendaOcupada = citaRepository.findAll().stream()
                .anyMatch(c -> c.getAgenda() != null && c.getAgenda().getId().equals(agendaId));
            
            if (agendaOcupada) {
                return Map.of("exito", false, "mensaje", "Este horario ya está ocupado");
            }
            
            // Buscar entidades relacionadas
            Optional<PacienteDetalle> pacienteOpt = pacienteRepository.findById(pacienteId);
            Optional<Medico> medicoOpt = medicoRepository.findById(medicoId);
            Optional<Servicio> servicioOpt = servicioRepository.findById(servicioId);
            
            if (pacienteOpt.isEmpty() || medicoOpt.isEmpty() || servicioOpt.isEmpty()) {
                return Map.of("exito", false, "mensaje", "No se encontraron todas las entidades requeridas");
            }
            
            // Crear la cita
            Cita nuevaCita = new Cita();
            nuevaCita.setPaciente(pacienteOpt.get());
            nuevaCita.setMedico(medicoOpt.get());
            nuevaCita.setServicio(servicioOpt.get());
            nuevaCita.setAgenda(agendaOpt.get());
            nuevaCita.setMotivo(motivo != null ? motivo : "Consulta general");
            nuevaCita.setFechaSolicitud(LocalDateTime.now());
            
            Cita citaCreada = citaRepository.save(nuevaCita);
            
            Agenda agenda = agendaOpt.get();
            
            return Map.of(
                "exito", true,
                "mensaje", "Cita creada exitosamente",
                "citaId", citaCreada.getId(),
                "fecha", agenda.getFecha().toLocalDate().toString(),
                "hora", agenda.getHoraInicio().toString()
            );
            
        } catch (Exception e) {
            log.error("Error al crear cita: {}", e.getMessage(), e);
            return Map.of("exito", false, "mensaje", "Error al crear la cita: " + e.getMessage());
        }
    }

    /**
     * Cancela una cita
     */
    @Transactional
    public Map<String, Object> cancelarCita(Long citaId, Long pacienteId) {
        log.info("🤖 IA solicitó cancelar cita: {}", citaId);
        
        try {
            Optional<Cita> citaOpt = citaRepository.findById(citaId);
            
            if (citaOpt.isEmpty()) {
                return Map.of("exito", false, "mensaje", "Cita no encontrada");
            }
            
            Cita cita = citaOpt.get();
            
            // Verificar que la cita pertenece al paciente
            if (cita.getPaciente() == null || !cita.getPaciente().getId().equals(pacienteId)) {
                return Map.of("exito", false, "mensaje", "No tienes permiso para cancelar esta cita");
            }
            
            // Eliminar la cita (o marcarla como cancelada según tu lógica)
            citaRepository.delete(cita);
            
            return Map.of(
                "exito", true,
                "mensaje", "Cita cancelada exitosamente",
                "citaId", citaId
            );
            
        } catch (Exception e) {
            log.error("Error al cancelar cita: {}", e.getMessage(), e);
            return Map.of("exito", false, "mensaje", "Error al cancelar la cita: " + e.getMessage());
        }
    }

    /**
     * Crear/agendar una nueva cita médica
     * @param pacienteId ID del paciente
     * @param horarioId ID del horario médico
     * @param servicioId ID del servicio médico
     * @param motivo Motivo de la consulta
     */
    @Transactional
    public Map<String, Object> agendarCita(Long pacienteId, Long horarioId, Long servicioId, String motivo) {
        log.info("🤖 IA solicitó agendar cita - Paciente: {}, Horario: {}, Servicio: {}", pacienteId, horarioId, servicioId);
        
        try {
            // Validar paciente
            Optional<PacienteDetalle> pacienteOpt = pacienteRepository.findById(pacienteId);
            if (pacienteOpt.isEmpty()) {
                return Map.of("exito", false, "mensaje", "Paciente no encontrado");
            }
            
            // Validar horario
            Optional<HorarioMedico> horarioOpt = horarioMedicoRepository.findById(horarioId);
            if (horarioOpt.isEmpty()) {
                return Map.of("exito", false, "mensaje", "Horario no encontrado");
            }
            
            HorarioMedico horario = horarioOpt.get();
            
            // Verificar que el horario esté disponible
            if (horario.getEstadoMedico() != EstadoMedico.DISPONIBLE) {
                return Map.of("exito", false, "mensaje", "Este horario ya no está disponible");
            }
            
            // Validar servicio
            Optional<Servicio> servicioOpt = servicioRepository.findById(servicioId);
            if (servicioOpt.isEmpty()) {
                return Map.of("exito", false, "mensaje", "Servicio no encontrado");
            }
            
            Servicio servicio = servicioOpt.get();
            
            // Verificar que el médico ofrezca el servicio
            if (horario.getMedico().getServicio() == null || 
                !horario.getMedico().getServicio().getId().equals(servicioId)) {
                return Map.of("exito", false, "mensaje", "El médico no ofrece este servicio");
            }
            
            // Buscar estatus "PENDIENTE"
            Optional<Estatus> estatusOpt = estatusRepository.findAll().stream()
                .filter(e -> "PENDIENTE".equalsIgnoreCase(e.getEstatus()))
                .findFirst();
            
            if (estatusOpt.isEmpty()) {
                return Map.of("exito", false, "mensaje", "No se encontró el estatus PENDIENTE en el sistema");
            }
            
            // Crear la cita
            Cita nuevaCita = new Cita();
            nuevaCita.setPaciente(pacienteOpt.get());
            nuevaCita.setMedico(horario.getMedico());
            nuevaCita.setServicio(servicio);
            nuevaCita.setFechaSolicitud(LocalDateTime.now());
            nuevaCita.setMotivo(motivo != null ? motivo : "Consulta médica");
            nuevaCita.setEstatus(estatusOpt.get());
            nuevaCita.setEstadoPago("PENDIENTE");
            nuevaCita.setMontoPagado(servicio.getCosto());
            
            // Guardar la cita
            Cita citaGuardada = citaRepository.save(nuevaCita);
            
            // Actualizar el estado del horario a NO_DISPONIBLE
            horario.setEstadoMedico(EstadoMedico.NO_DISPONIBLE);
            horarioMedicoRepository.save(horario);
            
            log.info("✅ Cita agendada exitosamente: ID {}", citaGuardada.getId());
            
            return Map.of(
                "exito", true,
                "mensaje", "Cita agendada exitosamente",
                "citaId", citaGuardada.getId(),
                "fecha", horario.getFecha().toString(),
                "horaInicio", horario.getHorarioInicio().toString(),
                "horaFin", horario.getHorarioFin().toString(),
                "medico", horario.getMedico().getUsuario().getNombre() + " " + 
                         horario.getMedico().getUsuario().getApellidoPaterno(),
                "servicio", servicio.getNombreServicio(),
                "costo", servicio.getCosto()
            );
            
        } catch (Exception e) {
            log.error("❌ Error al agendar cita: {}", e.getMessage(), e);
            return Map.of("exito", false, "mensaje", "Error al agendar la cita: " + e.getMessage());
        }
    }

    /**
     * Obtener información del paciente por ID de usuario
     * @param usuarioId ID del usuario
     */
    public Map<String, Object> obtenerDatosPaciente(Long usuarioId) {
        log.info("🤖 IA solicitó datos del paciente (usuario: {})", usuarioId);
        
        try {
            // Buscar paciente por ID de usuario
            List<PacienteDetalle> pacientes = pacienteRepository.findAll();
            Optional<PacienteDetalle> pacienteOpt = pacientes.stream()
                .filter(p -> p.getUsuario() != null && p.getUsuario().getIdUsuario().equals(usuarioId))
                .findFirst();
            
            if (pacienteOpt.isEmpty()) {
                return Map.of("exito", false, "mensaje", "Paciente no encontrado");
            }
            
            PacienteDetalle paciente = pacienteOpt.get();
            Usuario usuario = paciente.getUsuario();
            
            return Map.of(
                "exito", true,
                "pacienteId", paciente.getId(),
                "usuarioId", usuario.getIdUsuario(),
                "nombre", usuario.getNombre() + " " + usuario.getApellidoPaterno() + 
                         (usuario.getApellidoMaterno() != null ? " " + usuario.getApellidoMaterno() : ""),
                "correo", usuario.getCorreoElectronico(),
                "telefono", usuario.getTelefono() != null ? usuario.getTelefono() : "No especificado"
            );
            
        } catch (Exception e) {
            log.error("❌ Error al obtener datos del paciente: {}", e.getMessage(), e);
            return Map.of("exito", false, "mensaje", "Error al obtener datos: " + e.getMessage());
        }
    }
}
