package com.example.citasmedicas_backend.assistant.service;

import com.example.citasmedicas_backend.citas.model.*;
import com.example.citasmedicas_backend.citas.repository.*;
import com.example.citasmedicas_backend.citas.service.EmailService;
import com.example.citasmedicas_backend.citas.service.ComprobanteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Base64;

/**
 * Servicio que expone funciones para que la IA las llame
 * Cada función consulta la base de datos y devuelve solo datos necesarios
 */
@Service
@Transactional
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
    private final EmailService emailService;
    private final ComprobanteService comprobanteService;

    public IAAssistantService(
        ServicioRepository servicioRepository,
        MedicoRepository medicoRepository,
        AgendaRepository agendaRepository,
        HorarioMedicoRepository horarioMedicoRepository,
        AreasRepository areasRepository,
        CitaRepository citaRepository,
        PacienteRepository pacienteRepository,
        EstatusRepository estatusRepository,
        EmailService emailService,
        ComprobanteService comprobanteService
    ) {
        this.servicioRepository = servicioRepository;
        this.medicoRepository = medicoRepository;
        this.agendaRepository = agendaRepository;
        this.horarioMedicoRepository = horarioMedicoRepository;
        this.areasRepository = areasRepository;
        this.citaRepository = citaRepository;
        this.pacienteRepository = pacienteRepository;
        this.estatusRepository = estatusRepository;
        this.emailService = emailService;
        this.comprobanteService = comprobanteService;
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
        
        // Si no hay servicioId pero el nombre contiene palabras clave de servicios, intentar inferir
        final Long servicioIdFinal;
        if (servicioId == null && nombre != null) {
            List<Servicio> servicios = servicioRepository.findAll();
            String nombreLower = nombre.toLowerCase();
            Long inferredServicioId = null;
            
            for (Servicio s : servicios) {
                String servicioNomLower = s.getNombreServicio().toLowerCase();
                // Verificar si el nombre contiene palabras clave del servicio
                if (servicioNomLower.contains("evaluación") && nombreLower.contains("evaluación") ||
                    servicioNomLower.contains("desarrollo") && nombreLower.contains("desarrollo") ||
                    servicioNomLower.contains("general") && nombreLower.contains("general") ||
                    servicioNomLower.contains("cardi") && nombreLower.contains("cardi") ||
                    servicioNomLower.contains("ecocardiograma") && nombreLower.contains("ecocardi") ||
                    servicioNomLower.contains("holter") && nombreLower.contains("holter") ||
                    servicioNomLower.contains("electrocardiograma") && (nombreLower.contains("electro") || nombreLower.contains("ecg")) ||
                    servicioNomLower.contains("oftalm") && nombreLower.contains("oftalm") ||
                    servicioNomLower.contains("psicol") && nombreLower.contains("psicol") ||
                    servicioNomLower.contains("vacun") && nombreLower.contains("vacun")) {
                    inferredServicioId = s.getId();
                    servicioNombre = s.getNombreServicio();
                    log.info("Servicio inferido del nombre: {} -> {}", nombre, servicioNombre);
                    break;
                }
            }
            servicioIdFinal = inferredServicioId != null ? inferredServicioId : servicioId;
        } else {
            servicioIdFinal = servicioId;
        }
        
        if (servicioIdFinal != null && servicioNombre == null) {
            Optional<Servicio> servicioOpt = servicioRepository.findById(servicioIdFinal);
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
            // Filtro por servicio si se proporciona
            .filter(m -> {
                if (servicioIdFinal == null) return true;
                return m.getServicio() != null && m.getServicio().getId().equals(servicioIdFinal);
            })
            .collect(Collectors.groupingBy(m -> m.getUsuario().getIdUsuario()));
        
        List<Map<String, Object>> medicosData = new ArrayList<>();
        
        for (Map.Entry<Long, List<Medico>> entry : medicosPorUsuario.entrySet()) {
            List<Medico> registrosMedico = entry.getValue();
            
            // Si se especificó un servicio, usar el registro que ofrece ese servicio
            Medico medicoSeleccionado;
            if (servicioIdFinal != null) {
                medicoSeleccionado = registrosMedico.stream()
                    .filter(m -> m.getServicio() != null && m.getServicio().getId().equals(servicioIdFinal))
                    .findFirst()
                    .orElse(registrosMedico.get(0)); // Fallback al primero si no se encuentra
            } else {
                // Buscar el registro que tiene horarios asociados (prioridad) o el primero disponible
                medicoSeleccionado = registrosMedico.stream()
                    .filter(m -> {
                        long countHorarios = horarioMedicoRepository.findAll().stream()
                            .filter(h -> h.getMedico() != null && h.getMedico().getId().equals(m.getId()))
                            .count();
                        return countHorarios > 0;
                    })
                    .findFirst()
                    .orElse(registrosMedico.get(0)); // Si ninguno tiene horarios, usar el primero
            }
            
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
            medicoMap.put("id", medicoSeleccionado.getId()); // ID del médico que tiene horarios
            medicoMap.put("usuarioId", entry.getKey());
            
            if (medicoSeleccionado.getUsuario() != null) {
                String nombreCompleto = medicoSeleccionado.getUsuario().getNombre() + " " + 
                                       medicoSeleccionado.getUsuario().getApellidoPaterno();
                if (medicoSeleccionado.getUsuario().getApellidoMaterno() != null) {
                    nombreCompleto += " " + medicoSeleccionado.getUsuario().getApellidoMaterno();
                }
                medicoMap.put("nombre", nombreCompleto);
            }
            
            medicoMap.put("cedula", medicoSeleccionado.getCedulaProfecional());
            medicoMap.put("servicios", serviciosOfrecidos);
            
            // Contar horarios disponibles
            long horariosDisponibles = horarioMedicoRepository.findAll().stream()
                .filter(h -> h.getMedico() != null && h.getMedico().getId().equals(medicoSeleccionado.getId()))
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
     * Genera horarios disponibles desde HorarioMedico cuando no hay agendas específicas
     */
    private List<Agenda> generarHorariosDesdeHorarioMedico(Long medicoId, LocalDate inicio, LocalDate fin) {
        List<Agenda> horariosGenerados = new ArrayList<>();
        
        try {
            // Buscar el médico
            Medico medico = medicoRepository.findById(medicoId).orElse(null);
            if (medico == null) {
                log.warn("Médico con ID {} no encontrado", medicoId);
                return horariosGenerados;
            }
            
            // Obtener los horarios fijos del médico en el rango de fechas
            List<HorarioMedico> horariosMedico = horarioMedicoRepository.findAll().stream()
                .filter(h -> h.getMedico() != null && h.getMedico().getId().equals(medicoId))
                .filter(h -> h.getFecha() != null)
                .filter(h -> !h.getFecha().isBefore(inicio) && !h.getFecha().isAfter(fin))
                .filter(h -> h.getEstadoMedico() == EstadoMedico.DISPONIBLE)
                .collect(Collectors.toList());
            
            log.info("Médico {} tiene {} horarios disponibles en el rango", medicoId, horariosMedico.size());
            
            // Convertir HorarioMedico a Agenda
            for (HorarioMedico horarioFijo : horariosMedico) {
                Agenda agenda = new Agenda();
                agenda.setFecha(LocalDateTime.of(horarioFijo.getFecha(), horarioFijo.getHorarioInicio()));
                agenda.setHoraInicio(horarioFijo.getHorarioInicio());
                agenda.setHoraFin(horarioFijo.getHorarioFin());
                agenda.setMedico(medico);
                
                horariosGenerados.add(agenda);
                log.debug("Generado horario desde HorarioMedico: {} {}-{}", 
                        horarioFijo.getFecha(), horarioFijo.getHorarioInicio(), horarioFijo.getHorarioFin());
            }
            
            log.info("Generados {} horarios desde HorarioMedico para médico {}", 
                     horariosGenerados.size(), medicoId);
            
        } catch (Exception e) {
            log.error("Error generando horarios desde HorarioMedico: {}", e.getMessage(), e);
        }
        
        return horariosGenerados;
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
            
            log.info("Encontrados {} horarios en agenda para médico {}", horarios.size(), medicoId);
            
            // Si no hay horarios en agenda, intentar generar desde HorarioMedico
            if (horarios.isEmpty() && medicoId != null) {
                log.info("No hay agendas específicas, intentando generar desde HorarioMedico para médico {}", medicoId);
                horarios = generarHorariosDesdeHorarioMedico(medicoId, hoy, finVentana);
                log.info("Generados {} horarios desde HorarioMedico", horarios.size());
            }

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
     * Crear/agendar una nueva cita médica con opción de pago inmediato
     * @param pacienteId ID del paciente (opcional si se envía usuarioId)
     * @param usuarioId ID del usuario (opcional si se envía pacienteId)
     * @param horarioId ID del horario médico
     * @param servicioId ID del servicio médico
     * @param motivo Motivo de la consulta
     * @param pagoData Datos del pago (opcional, si es null se deja pendiente)
     */
    @Transactional
    public Map<String, Object> agendarCita(Long pacienteId, Long usuarioId, Long horarioId, Long servicioId, String motivo, Map<String, Object> pagoData) {
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
            
            // Si se proporcionaron datos de pago, procesar pago inmediato
            if (pagoData != null && !pagoData.isEmpty()) {
                log.info("Procesando pago inmediato para cita IA");
                try {
                    // Simular procesamiento de pago (en producción se integraría con pasarela)
                    nuevaCita.setEstadoPago("PAGADO");
                    nuevaCita.setFechaPago(LocalDateTime.now());
                    
                    // Cambiar estatus a CONFIRMADO si el pago fue exitoso
                    Optional<Estatus> estatusConfirmado = estatusRepository.findAll().stream()
                        .filter(e -> "Confirmado".equalsIgnoreCase(e.getEstatus()))
                        .findFirst();
                    
                    if (estatusConfirmado.isPresent()) {
                        nuevaCita.setEstatus(estatusConfirmado.get());
                    }
                    
                    log.info("Pago procesado exitosamente para cita IA");
                    
                    // El comprobante PDF se enviará cuando el médico acepte la cita
                    // No enviar inmediatamente
                    
                } catch (Exception e) {
                    log.error("Error procesando pago para cita IA: {}", e.getMessage());
                    // El pago queda pendiente si hay error
                }
            }
            
            // Guardar la cita
            Cita citaGuardada = citaRepository.save(nuevaCita);
            
            // Solo eliminar el horario si la cita queda PENDIENTE (sin pago)
            // Si la cita está CONFIRMADA (con pago), mantener el horario para que aparezca en la agenda del médico
            if ("PENDIENTE".equals(citaGuardada.getEstadoPago())) {
                agendaRepository.delete(horario);
                log.info("Horario eliminado de agenda - cita pendiente requiere confirmación médica");
            } else {
                log.info("Horario mantenido en agenda - cita confirmada con pago");
            }
            
            log.info("Cita agendada exitosamente: ID {}", citaGuardada.getId());
            
            // Notificar al médico sobre la nueva cita pendiente
            try {
                String emailMedico = horario.getMedico().getUsuario().getCorreoElectronico();
                String nombrePaciente = paciente.getUsuario().getNombre() + " " + paciente.getUsuario().getApellidoPaterno();
                String nombreMedico = horario.getMedico().getUsuario().getNombre() + " " + horario.getMedico().getUsuario().getApellidoPaterno();
                String especialidad = servicio.getNombreServicio();
                String fechaCita = horario.getFecha().toLocalDate().toString() + " " + horario.getHoraInicio().toString();
                
                // Enviar notificación al médico
                emailService.notificarNuevaCitaMedico(emailMedico, nombreMedico, nombrePaciente, especialidad, fechaCita, citaGuardada.getMotivo());
                
                log.info("Notificación enviada al médico: {}", emailMedico);
            } catch (Exception emailEx) {
                log.error("Error enviando notificación al médico: {}", emailEx.getMessage());
                // No fallar la operación por error de email
            }
            
            String mensaje;
            String estado;
            
            log.info("🔍 Verificando estado de pago - pagoData: {} (size: {}), estadoPago: '{}'", 
                     pagoData != null ? "presente" : "null", 
                     pagoData != null ? pagoData.size() : 0,
                     citaGuardada.getEstadoPago());
            
            // Verificar si la cita tiene pago procesado
            boolean tienePago = pagoData != null && !pagoData.isEmpty() && 
                               "PAGADO".equals(citaGuardada.getEstadoPago());
            
            log.info("💳 Análisis de pago - tienePago: {}, pagoData not null: {}, pagoData not empty: {}, estadoPago es PAGADO: {}", 
                     tienePago, pagoData != null, pagoData != null && !pagoData.isEmpty(), 
                     "PAGADO".equals(citaGuardada.getEstadoPago()));
            
            if (tienePago) {
                mensaje = "✅ ¡Pago procesado exitosamente! Tu cita está confirmada para [fecha y hora]. Recibirás el comprobante por email.";
                estado = "CONFIRMADO";
                log.info("✅ Cita CONFIRMADA con pago");
            } else {
                mensaje = "Cita agendada exitosamente. El médico revisará y confirmará la cita.";
                estado = "PENDIENTE";
                log.info("⏳ Cita PENDIENTE - no cumple criterios de pago");
            }
            
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("exito", true);
            resultado.put("mensaje", mensaje);
            resultado.put("citaId", citaGuardada.getId());
            resultado.put("fecha", horario.getFecha().toLocalDate().toString());
            resultado.put("horaInicio", horario.getHoraInicio().toString());
            resultado.put("horaFin", horario.getHoraFin().toString());
            resultado.put("medico", horario.getMedico().getUsuario().getNombre() + " " + 
                           horario.getMedico().getUsuario().getApellidoPaterno());
            resultado.put("servicio", servicio.getNombreServicio());
            resultado.put("estado", estado);
            resultado.put("pago", citaGuardada.getEstadoPago());
            resultado.put("nota", pagoData != null && !pagoData.isEmpty() ? 
                           "Pago procesado exitosamente." : 
                           "El médico debe aceptar, rechazar o posponer esta cita. Recibirás una notificación por email cuando se procese.");
            
            return resultado;
            
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

    /**
     * Procesa el pago de una cita pendiente para confirmarla
     */
    public Map<String, Object> procesarPago(Long citaId, Map<String, Object> pagoData) {
        log.info("🤖 IA solicitó procesar pago para cita: {}", citaId);
        
        try {
            // Buscar la cita
            Optional<Cita> citaOpt = citaRepository.findById(citaId);
            if (citaOpt.isEmpty()) {
                return Map.of("exito", false, "mensaje", "Cita no encontrada");
            }
            
            Cita cita = citaOpt.get();
            
            // Verificar que esté pendiente
            if (!"PENDIENTE".equals(cita.getEstatus().getEstatus())) {
                return Map.of("exito", false, "mensaje", "La cita no está en estado pendiente");
            }
            
            // Simular procesamiento de pago
            log.info("Procesando pago para cita {}...", citaId);
            
            // Aquí iría la lógica real de procesamiento de pago
            // Por ahora, simulamos que siempre es exitoso
            
            // Actualizar cita a CONFIRMADA
            Optional<Estatus> estatusConfirmadoOpt = estatusRepository.findByEstatus("CONFIRMADA");
            if (estatusConfirmadoOpt.isEmpty()) {
                return Map.of("exito", false, "mensaje", "Error interno: estatus CONFIRMADA no encontrado");
            }
            
            Estatus estatusConfirmado = estatusConfirmadoOpt.get();
            cita.setEstatus(estatusConfirmado);
            citaRepository.save(cita);
            
            // Generar comprobante PDF
            byte[] comprobantePdf = comprobanteService.generarComprobantePDF(cita);
            
            // Enviar email con comprobante
            String asunto = "Comprobante de Pago - Cita Médica Confirmada";
            String contenidoHtml = generarContenidoEmailPago(cita);
            emailService.enviarEmailConAdjunto(cita.getPaciente().getUsuario().getCorreoElectronico(), 
                                             asunto, contenidoHtml, comprobantePdf, "comprobante_pago.pdf");
            
            log.info("✅ Pago procesado exitosamente para cita {}", citaId);
            
            return Map.of(
                "exito", true,
                "mensaje", "Pago procesado exitosamente. Cita confirmada.",
                "citaId", citaId,
                "comprobantePdf", Base64.getEncoder().encodeToString(comprobantePdf)
            );
            
        } catch (Exception e) {
            log.error("Error procesando pago: {}", e.getMessage(), e);
            return Map.of("exito", false, "mensaje", "Error procesando pago: " + e.getMessage());
        }
    }

    /**
     * Genera el contenido HTML para el email de confirmación de pago
     */
    private String generarContenidoEmailPago(Cita cita) {
        String pacienteNombre = cita.getPaciente().getUsuario().getNombre() + " " + 
                              cita.getPaciente().getUsuario().getApellidoPaterno();
        String medicoNombre = cita.getMedico().getUsuario().getNombre() + " " + 
                             cita.getMedico().getUsuario().getApellidoPaterno();
        String servicioNombre = cita.getMedico().getServicio() != null ? 
                               cita.getMedico().getServicio().getNombreServicio() : "Servicio Médico";
        
        return "<html>" +
            "<body style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;\">" +
                "<div style=\"background-color: #4CAF50; color: white; padding: 20px; text-align: center;\">" +
                    "<h1>✅ Pago Procesado Exitosamente</h1>" +
                    "<p>Su cita médica ha sido confirmada</p>" +
                "</div>" +
                
                "<div style=\"padding: 20px; border: 1px solid #ddd; margin: 20px;\">" +
                    "<h2>Detalles de la Cita</h2>" +
                    "<p><strong>Paciente:</strong> " + pacienteNombre + "</p>" +
                    "<p><strong>Médico:</strong> " + medicoNombre + "</p>" +
                    "<p><strong>Servicio:</strong> " + servicioNombre + "</p>" +
                    "<p><strong>Fecha y Hora:</strong> " + cita.getAgenda().getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "</p>" +
                    "<p><strong>Estado:</strong> CONFIRMADA</p>" +
                "</div>" +
                
                "<div style=\"background-color: #f9f9f9; padding: 20px; margin: 20px; text-align: center;\">" +
                    "<p>Se adjunta el comprobante de pago en formato PDF.</p>" +
                    "<p>Si tiene alguna pregunta, no dude en contactarnos.</p>" +
                "</div>" +
                
                "<div style=\"text-align: center; color: #666; font-size: 12px; margin-top: 30px;\">" +
                    "<p>MediCitas - Sistema de Citas Médicas</p>" +
                "</div>" +
            "</body>" +
            "</html>";
    }

}
