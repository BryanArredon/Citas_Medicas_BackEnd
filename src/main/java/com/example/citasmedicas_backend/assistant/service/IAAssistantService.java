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
     * Busca médicos (opcionalmente por servicio o nombre)
     * IMPORTANTE: Agrupa por usuario y devuelve el medico_id que tiene horarios asociados
     */
    public Map<String, Object> buscarMedicos(Long servicioId, String nombre) {
        log.info("🤖 IA solicitó buscar médicos (servicio: {}, nombre: {})", servicioId, nombre);
        
        List<Medico> todosMedicos = medicoRepository.findAll();
        String servicioNombre = null;
        
        if (servicioId != null) {
            Optional<Servicio> servicioOpt = servicioRepository.findById(servicioId);
            if (servicioOpt.isEmpty()) {
                return Map.of("exito", false, "mensaje", "Servicio no encontrado");
            }
            servicioNombre = servicioOpt.get().getNombreServicio();
        }
        
        // Filtrar y agrupar médicos
        final String nombreBusqueda = nombre != null ? nombre.toLowerCase() : null;

        Map<Long, List<Medico>> medicosPorUsuario = todosMedicos.stream()
            .filter(m -> m.getUsuario() != null && m.getUsuario().getIdUsuario() != null)
            // Filtro por nombre si se proporciona
            .filter(m -> {
                if (nombreBusqueda == null) return true;
                Usuario u = m.getUsuario();
                String nombreCompleto = (u.getNombre() + " " + u.getApellidoPaterno() + " " + 
                                       (u.getApellidoMaterno() != null ? u.getApellidoMaterno() : "")).toLowerCase();
                return nombreCompleto.contains(nombreBusqueda);
            })
            .collect(Collectors.groupingBy(m -> m.getUsuario().getIdUsuario()));
        
        List<Map<String, Object>> medicosData = new ArrayList<>();
        
        for (Map.Entry<Long, List<Medico>> entry : medicosPorUsuario.entrySet()) {
            List<Medico> registrosMedico = entry.getValue();
            
            // Verificar si algún registro ofrece el servicio solicitado
            if (servicioId != null) {
                boolean ofreceServicio = registrosMedico.stream()
                    .anyMatch(m -> m.getServicio() != null && m.getServicio().getId().equals(servicioId));
                if (!ofreceServicio) {
                    continue; // Saltar este médico
                }
            }
            
            // Buscar el registro que tiene horarios asociados (prioridad) o el primero disponible
            Medico medicoConHorarios = registrosMedico.stream()
                .filter(m -> {
                    long countHorarios = horarioMedicoRepository.findAll().stream()
                        .filter(h -> h.getMedico() != null && h.getMedico().getId().equals(m.getId()))
                        .count();
                    return countHorarios > 0;
                })
                .findFirst()
                .orElse(registrosMedico.get(0)); // Si ninguno tiene horarios, usar el primero
            
            // Obtener todos los servicios que ofrece este médico
            List<Map<String, Object>> serviciosOfrecidos = registrosMedico.stream()
                .filter(m -> m.getServicio() != null)
                .map(m -> {
                    Map<String, Object> svc = new HashMap<>();
                    svc.put("id", m.getServicio().getId());
                    svc.put("nombre", m.getServicio().getNombreServicio());
                    return svc;
                })
                // Eliminar duplicados basados en ID
                .collect(Collectors.collectingAndThen(
                    Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(m -> ((Long)m.get("id"))))),
                    ArrayList::new
                ));
            
            Map<String, Object> medicoMap = new HashMap<>();
            medicoMap.put("id", medicoConHorarios.getId()); // ID del médico que tiene horarios
            medicoMap.put("usuarioId", entry.getKey());
            
            if (medicoConHorarios.getUsuario() != null) {
                String nombreCompleto = medicoConHorarios.getUsuario().getNombre() + " " + 
                                       medicoConHorarios.getUsuario().getApellidoPaterno();
                if (medicoConHorarios.getUsuario().getApellidoMaterno() != null) {
                    nombreCompleto += " " + medicoConHorarios.getUsuario().getApellidoMaterno();
                }
                medicoMap.put("nombre", nombreCompleto);
            }
            
            medicoMap.put("cedula", medicoConHorarios.getCedulaProfecional());
            medicoMap.put("servicios", serviciosOfrecidos);
            
            // Contar horarios disponibles
            long horariosDisponibles = horarioMedicoRepository.findAll().stream()
                .filter(h -> h.getMedico() != null && h.getMedico().getId().equals(medicoConHorarios.getId()))
                .filter(h -> h.getEstadoMedico() == EstadoMedico.DISPONIBLE)
                .count();
            medicoMap.put("horariosDisponibles", horariosDisponibles);
            
            medicosData.add(medicoMap);
        }
        
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("exito", true);
        resultado.put("medicos", medicosData);
        if (servicioNombre != null) {
            resultado.put("servicio", servicioNombre);
        }
        resultado.put("total", medicosData.size());
        
        log.info("Encontrados {} médicos que ofrecen {}", medicosData.size(), 
                 servicioNombre != null ? servicioNombre : "cualquier servicio");
        
        return resultado;
    }

    /**
     * Obtiene próximos horarios disponibles de un médico (siguientes 30 días)
     * Si medicoId es null, devuelve horarios de TODOS los médicos
     * Método alias para compatibilidad con la configuración de OpenAI Assistant
     */
    public Map<String, Object> obtenerHorarios(Long medicoId) {
        log.info("🤖 IA solicitó obtenerHorarios (médico: {})", medicoId);
        return obtenerProximosHorariosDisponibles(medicoId);
    }

    /**
     * Obtiene próximos horarios disponibles de un médico (siguientes 30 días)
     * Si medicoId es null, devuelve horarios de TODOS los médicos
     */
    public Map<String, Object> obtenerProximosHorariosDisponibles(Long medicoId) {
        log.info("IA solicitó próximos horarios del médico: {}", medicoId);
        
        try {
            LocalDate hoy = LocalDate.now();
            LocalDate finVentana = hoy.plusDays(30); // Extender a 30 días para encontrar más horarios
            
            List<Map<String, Object>> horariosDisponibles = new ArrayList<>();
            
            // Obtener todos los horarios de la agenda disponibles (filtrados por médico si se especifica)
            List<Agenda> horarios = agendaRepository.findAll().stream()
                .filter(a -> a != null && a.getFecha() != null && a.getHoraInicio() != null && a.getHoraFin() != null)
                .filter(a -> medicoId == null || (a.getMedico() != null && a.getMedico().getId() != null && a.getMedico().getId().equals(medicoId)))
                .filter(a -> {
                    try {
                        LocalDate fecha = a.getFecha().toLocalDate();
                        // Incluir desde hoy en adelante hasta 30 días
                        return !fecha.isBefore(hoy) && !fecha.isAfter(finVentana);
                    } catch (Exception e) {
                        log.warn("Error procesando fecha de agenda {}: {}", a.getId(), e.getMessage());
                        return false;
                    }
                })
                .sorted((a1, a2) -> a1.getFecha().compareTo(a2.getFecha())) // Ordenar por fecha
                .collect(Collectors.toList());
            
            log.info("Encontrados {} horarios disponibles en el rango de fechas (desde {} hasta {})",
                     horarios.size(), hoy, finVentana);

            for (Agenda horario : horarios) {
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

                    horarioMap.put("fecha", horario.getFecha().toLocalDate().toString());
                    horarioMap.put("diaSemana", horario.getFecha().toLocalDate().getDayOfWeek().toString());
                    horarioMap.put("horaInicio", horario.getHoraInicio().toString());
                    horarioMap.put("horaFin", horario.getHoraFin().toString());
                    horarioMap.put("duracion", 60); // Duración por defecto de 60 minutos para agenda
                    horarioMap.put("estado", "DISPONIBLE"); // Los horarios en agenda están disponibles

                    // Agregar información del servicio asociado a este horario/médico
                    if (horario.getMedico() != null && horario.getMedico().getServicio() != null) {
                        horarioMap.put("servicioId", horario.getMedico().getServicio().getId());
                        horarioMap.put("servicioNombre", horario.getMedico().getServicio().getNombreServicio());
                    }

                    horariosDisponibles.add(horarioMap);
                } catch (Exception e) {
                    log.error("Error procesando horario {}: {}", horario.getId(), e.getMessage());
                }
            }

            log.info("Retornando {} horarios disponibles", horariosDisponibles.size());

            // SI NO HAY HORARIOS FUTUROS, NO MOSTRAR FECHAS PASADAS
            if (horariosDisponibles.isEmpty()) {
                log.warn("No se encontraron horarios futuros disponibles para médico: {}", medicoId);
                return Map.of(
                    "exito", false,
                    "mensaje", "No hay horarios disponibles en los próximos 30 días",
                    "horarios", new ArrayList<>(),
                    "total", 0,
                    "periodo", "Próximos 30 días",
                    "sugerencia", "Contacta directamente con el médico para agendar una cita"
                );
            }
            
            return Map.of(
                "exito", true,
                "horarios", horariosDisponibles,
                "total", horariosDisponibles.size(),
                "periodo", "Próximos 30 días",
                "mensaje", medicoId == null ? "Horarios de todos los médicos" : "Horarios del médico solicitado"
            );
        } catch (Exception e) {
            log.error("Error al obtener horarios disponibles: {}", e.getMessage(), e);
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
        log.info("IA solicitó citas del paciente: {}", pacienteId);
        
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
        log.info("IA solicitó crear cita (paciente: {}, médico: {}, servicio: {}, agenda: {})", 
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
     * @param pacienteId ID del paciente (opcional si se envía usuarioId)
     * @param usuarioId ID del usuario (opcional si se envía pacienteId)
     * @param horarioId ID del horario médico
     * @param servicioId ID del servicio médico
     * @param motivo Motivo de la consulta
     */
    @Transactional
    public Map<String, Object> agendarCita(Long pacienteId, Long usuarioId, Long horarioId, Long servicioId, String motivo) {
        log.info("IA solicitó agendar cita - PacienteID: {}, UsuarioID: {}, Horario: {}, Servicio: {}", 
                 pacienteId, usuarioId, horarioId, servicioId);
        
        try {
            // Validar paciente
            PacienteDetalle paciente = null;
            
            // 1. Intentar por ID de paciente directo
            if (pacienteId != null) {
                Optional<PacienteDetalle> pOpt = pacienteRepository.findById(pacienteId);
                if (pOpt.isPresent()) {
                    paciente = pOpt.get();
                }
            }
            
            // 2. Si no se encontró, intentar por ID de usuario
            if (paciente == null && usuarioId != null) {
                paciente = pacienteRepository.findByUsuarioIdUsuario(usuarioId);
            }
            
            if (paciente == null) {
                return Map.of("exito", false, "mensaje", "Paciente no encontrado (ID: " + pacienteId + ", Usuario: " + usuarioId + ")");
            }
            
            // Validar horario - ahora desde tabla agenda
            Optional<Agenda> horarioOpt = agendaRepository.findById(horarioId);
            if (horarioOpt.isEmpty()) {
                return Map.of("exito", false, "mensaje", "Horario no encontrado en agenda");
            }
            
            Agenda horario = horarioOpt.get();
            
            // Validar servicio
            Optional<Servicio> servicioOpt = servicioRepository.findById(servicioId);
            if (servicioOpt.isEmpty()) {
                return Map.of("exito", false, "mensaje", "Servicio no encontrado");
            }
            
            Servicio servicio = servicioOpt.get();
            
            // Verificar que el médico ofrezca el servicio
            // NOTA: Se relaja esta validación porque ahora el médico puede tener múltiples servicios o la relación puede ser compleja
            // Si el servicio existe y el médico tiene horario, permitimos la cita.
            
            // Buscar estatus "PENDIENTE"
            Optional<Estatus> estatusOpt = estatusRepository.findAll().stream()
                .filter(e -> "PENDIENTE".equalsIgnoreCase(e.getEstatus()))
                .findFirst();
            
            if (estatusOpt.isEmpty()) {
                return Map.of("exito", false, "mensaje", "No se encontró el estatus PENDIENTE en el sistema");
            }
            
            // Crear la cita con estado PENDIENTE para que el médico la revise
            Cita nuevaCita = new Cita();
            nuevaCita.setPaciente(paciente);
            nuevaCita.setMedico(horario.getMedico());
            nuevaCita.setServicio(servicio);
            nuevaCita.setFechaSolicitud(LocalDateTime.now());
            nuevaCita.setMotivo(motivo != null ? motivo : "Consulta médica");
            
            // Buscar estatus "Pendiente" para que el médico pueda aceptar/rechazar/posponer
            Optional<Estatus> estatusPendiente = estatusRepository.findAll().stream()
                .filter(e -> "Pendiente".equalsIgnoreCase(e.getEstatus()))
                .findFirst();
            
            if (estatusPendiente.isPresent()) {
                nuevaCita.setEstatus(estatusPendiente.get());
            } else {
                log.warn("No se encontró estatus 'Pendiente', la cita se creará sin estatus definido");
            }
            
            nuevaCita.setEstadoPago("PENDIENTE");
            nuevaCita.setMontoPagado(servicio.getCosto());
            
            // Guardar la cita
            Cita citaGuardada = citaRepository.save(nuevaCita);
            
            // Eliminar el horario de la agenda para que ya no aparezca como disponible
            agendaRepository.delete(horario);
            
            log.info("Cita agendada exitosamente: ID {}", citaGuardada.getId());
            
            return Map.of(
                "exito", true,
                "mensaje", "Cita agendada exitosamente. El médico revisará y confirmará la cita.",
                "citaId", citaGuardada.getId(),
                "fecha", horario.getFecha().toLocalDate().toString(),
                "horaInicio", horario.getHoraInicio().toString(),
                "horaFin", horario.getHoraFin().toString(),
                "medico", horario.getMedico().getUsuario().getNombre() + " " + 
                         horario.getMedico().getUsuario().getApellidoPaterno(),
                "servicio", servicio.getNombreServicio(),
                "estado", "PENDIENTE",
                "nota", "El médico debe aceptar, rechazar o posponer esta cita. Recibirás una notificación por email cuando se procese."
            );
            
        } catch (Exception e) {
            log.error("Error al agendar cita: {}", e.getMessage(), e);
            return Map.of("exito", false, "mensaje", "Error al agendar la cita: " + e.getMessage());
        }
    }

    /**
     * Obtener información del paciente por ID de usuario
     * @param usuarioId ID del usuario
     */
    public Map<String, Object> obtenerDatosPaciente(Long usuarioId) {
        log.info("IA solicitó datos del paciente (usuario: {})", usuarioId);
        
        try {
            // Buscar paciente por ID de usuario
            List<PacienteDetalle> pacientes = pacienteRepository.findAll();
            log.info("Total de pacientes en BD: {}", pacientes.size());
            
            Optional<PacienteDetalle> pacienteOpt = pacientes.stream()
                .filter(p -> {
                    if (p.getUsuario() == null) {
                        log.warn("Paciente {} sin usuario asociado", p.getId());
                        return false;
                    }
                    boolean coincide = p.getUsuario().getIdUsuario().equals(usuarioId);
                    if (coincide) {
                        log.info("Paciente encontrado: {} (usuario: {})", p.getId(), usuarioId);
                    }
                    return coincide;
                })
                .findFirst();
            
            if (pacienteOpt.isEmpty()) {
                log.error("No se encontró paciente para usuario: {}", usuarioId);
                log.info("Usuarios disponibles en pacientes: {}", 
                    pacientes.stream()
                        .filter(p -> p.getUsuario() != null)
                        .map(p -> p.getUsuario().getIdUsuario())
                        .distinct()
                        .toList());
                return Map.of("exito", false, "mensaje", "Paciente no encontrado para el usuario ID: " + usuarioId);
            }
            
            PacienteDetalle paciente = pacienteOpt.get();
            Usuario usuario = paciente.getUsuario();
            
            String nombreCompleto = usuario.getNombre() + " " + usuario.getApellidoPaterno() + 
                     (usuario.getApellidoMaterno() != null ? " " + usuario.getApellidoMaterno() : "");
            
            log.info("Datos obtenidos - Paciente: {} ({}), Usuario: {}", 
                     paciente.getId(), nombreCompleto, usuario.getIdUsuario());
            
            return Map.of(
                "exito", true,
                "pacienteId", paciente.getId(),
                "usuarioId", usuario.getIdUsuario(),
                "nombre", nombreCompleto,
                "correo", usuario.getCorreoElectronico(),
                "telefono", usuario.getTelefono() != null ? usuario.getTelefono() : "No especificado"
            );
            
        } catch (Exception e) {
            log.error("Error al obtener datos del paciente: {}", e.getMessage(), e);
            return Map.of("exito", false, "mensaje", "Error al obtener datos: " + e.getMessage());
        }
    }
}
