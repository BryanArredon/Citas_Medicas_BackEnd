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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
    /**
     * Obtiene los próximos horarios disponibles de un médico
     * LÓGICA CORRECTA:
     * 1. Buscar en HORARIO_MEDICO la disponibilidad del médico (cuando puede atender)
     * 2. EXPANDIR horarios según valid_until y duración de citas
     * 3. Buscar en AGENDA las citas ya agendadas (horas ocupadas)
     * 4. Retornar solo las horas LIBRES (disponibilidad - ocupadas)
     */
    public Map<String, Object> obtenerProximosHorariosDisponibles(Long medicoId) {
        log.info("🤖 IA solicitó próximos horarios del médico ID: {}", medicoId);
        
        try {
            LocalDate hoy = LocalDate.now();
            LocalDate finVentana = hoy.plusDays(30);
            
            log.info("📅 Buscando horarios entre {} y {}", hoy, finVentana);
            
            // Verificar que el médico existe
            if (medicoId != null) {
                Optional<Medico> medicoOpt = medicoRepository.findById(medicoId);
                if (!medicoOpt.isPresent()) {
                    log.error("❌ Médico con ID {} no existe", medicoId);
                    return Map.of(
                        "exito", false,
                        "mensaje", "El médico especificado no existe en el sistema.",
                        "horarios", new ArrayList<>(),
                        "total", 0
                    );
                }
                String nombreMedico = medicoOpt.get().getUsuario() != null ? 
                    (medicoOpt.get().getUsuario().getNombre() + " " + medicoOpt.get().getUsuario().getApellidoPaterno()).trim() : 
                    "Desconocido";
                log.info("✅ Médico encontrado: {} (ID: {})", nombreMedico, medicoId);
            }
            
            // PASO 1: Buscar configuración de DISPONIBILIDAD en horario_medico
            log.info("📋 PASO 1: Buscando configuración de horarios en horario_medico...");
            List<HorarioMedico> configuracionHorarios = horarioMedicoRepository.findAll().stream()
                .filter(h -> h.getMedico() != null && (medicoId == null || h.getMedico().getId().equals(medicoId)))
                .collect(Collectors.toList());
                
            log.info("✅ Configuraciones de horario encontradas: {}", configuracionHorarios.size());
            
            // PASO 2: EXPANDIR horarios en slots individuales
            log.info("🔄 PASO 2: Expandiendo horarios en slots de tiempo...");
            List<Map<String, Object>> todosLosSlots = new ArrayList<>();
            
            for (HorarioMedico config : configuracionHorarios) {
                try {
                    LocalDate fechaInicio = config.getFecha();
                    LocalDate fechaFin = config.getValidUntil() != null ? config.getValidUntil() : fechaInicio;
                    
                    // Ajustar al rango de búsqueda
                    if (fechaInicio.isBefore(hoy)) fechaInicio = hoy;
                    if (fechaFin.isAfter(finVentana)) fechaFin = finVentana;
                    
                    log.debug("📅 Generando slots desde {} hasta {} para médico {}", 
                        fechaInicio, fechaFin, config.getMedico().getId());
                    
                    // Generar slots para cada día en el rango
                    LocalDate fechaActual = fechaInicio;
                    while (!fechaActual.isAfter(fechaFin)) {
                        // Generar slots de tiempo para este día
                        LocalTime horaActual = config.getHorarioInicio();
                        LocalTime horaFin = config.getHorarioFin();
                        int duracionMinutos = config.getDuracion() != null ? config.getDuracion() : 30;
                        
                        while (horaActual.plusMinutes(duracionMinutos).isBefore(horaFin) || 
                               horaActual.plusMinutes(duracionMinutos).equals(horaFin)) {
                            
                            Map<String, Object> slot = new HashMap<>();
                            slot.put("horarioConfigId", config.getId());
                            slot.put("medicoId", config.getMedico().getId());
                            
                            String medicoNombre = "Desconocido";
                            if (config.getMedico().getUsuario() != null) {
                                Usuario usuario = config.getMedico().getUsuario();
                                medicoNombre = (usuario.getNombre() + " " + usuario.getApellidoPaterno()).trim();
                            }
                            slot.put("medicoNombre", medicoNombre);
                            
                            slot.put("fecha", fechaActual.toString());
                            slot.put("diaSemana", fechaActual.getDayOfWeek().toString());
                            slot.put("horaInicio", horaActual.toString());
                            slot.put("horaFin", horaActual.plusMinutes(duracionMinutos).toString());
                            slot.put("duracion", duracionMinutos);
                            slot.put("estado", "DISPONIBLE");
                            
                            if (config.getMedico().getServicio() != null) {
                                slot.put("servicioId", config.getMedico().getServicio().getId());
                                slot.put("servicioNombre", config.getMedico().getServicio().getNombreServicio());
                            }
                            
                            todosLosSlots.add(slot);
                            
                            horaActual = horaActual.plusMinutes(duracionMinutos);
                        }
                        
                        fechaActual = fechaActual.plusDays(1);
                    }
                } catch (Exception e) {
                    log.error("Error expandiendo horario {}: {}", config.getId(), e.getMessage());
                }
            }
            
            log.info("✅ Total de slots generados: {}", todosLosSlots.size());
            
            // PASO 3: Buscar CITAS AGENDADAS en agenda
            log.info("📅 PASO 3: Buscando citas ya agendadas en agenda...");
            List<Agenda> citasAgendadas = agendaRepository.findAll().stream()
                .filter(a -> a.getFecha() != null && !a.getFecha().toLocalDate().isBefore(hoy) && !a.getFecha().toLocalDate().isAfter(finVentana))
                .filter(a -> medicoId == null || (a.getMedico() != null && a.getMedico().getId().equals(medicoId)))
                .collect(Collectors.toList());
                
            log.info("✅ Citas AGENDADAS encontradas: {}", citasAgendadas.size());
            
            // PASO 4: Filtrar slots LIBRES (disponibilidad - ocupadas)
            log.info("🔍 PASO 4: Filtrando slots libres...");
            List<Map<String, Object>> horariosLibres = new ArrayList<>();
            
            for (Map<String, Object> slot : todosLosSlots) {
                try {
                    String fechaStr = (String) slot.get("fecha");
                    String horaInicioStr = (String) slot.get("horaInicio");
                    Long slotMedicoId = (Long) slot.get("medicoId");
                    
                    LocalDate fechaSlot = LocalDate.parse(fechaStr);
                    LocalTime horaSlot = LocalTime.parse(horaInicioStr);
                    
                    // Verificar si este slot está ocupado
                    boolean estaOcupado = citasAgendadas.stream()
                        .anyMatch(cita -> 
                            cita.getMedico() != null &&
                            cita.getMedico().getId().equals(slotMedicoId) &&
                            cita.getFecha().toLocalDate().equals(fechaSlot) &&
                            cita.getHoraInicio().equals(horaSlot)
                        );
                    
                    if (!estaOcupado) {
                        horariosLibres.add(slot);
                    }
                } catch (Exception e) {
                    log.error("Error filtrando slot: {}", e.getMessage());
                }
            }

            log.info("📊 RESUMEN: {} slots generados, {} ocupados, {} LIBRES", 
                todosLosSlots.size(), 
                citasAgendadas.size(), 
                horariosLibres.size());

            // Mensajes según el caso
            if (horariosLibres.isEmpty()) {
                if (configuracionHorarios.isEmpty()) {
                    // Caso 1: No tiene horarios configurados
                    String mensaje = "No hay horarios configurados.";
                    if (medicoId != null) {
                        Optional<Medico> medicoOpt = medicoRepository.findById(medicoId);
                        if (medicoOpt.isPresent()) {
                            String nombreMedico = medicoOpt.get().getUsuario() != null ? 
                                (medicoOpt.get().getUsuario().getNombre() + " " + medicoOpt.get().getUsuario().getApellidoPaterno()).trim() : 
                                "este médico";
                            
                            mensaje = String.format(
                                "El Dr(a). %s aún no tiene horarios de disponibilidad configurados.\n\n" +
                                "**Acción requerida:**\n" +
                                "El médico o el administrador debe configurar los horarios de atención en el sistema.\n\n" +
                                "**Cómo configurar:**\n" +
                                "1. Ir al panel de gestión de médicos\n" +
                                "2. Configurar días y horas de atención\n" +
                                "3. Guardar la disponibilidad\n\n" +
                                "Una vez configurado, los pacientes podrán agendar citas.",
                                nombreMedico
                            );
                        }
                    }
                    
                    return Map.of(
                        "exito", false,
                        "mensaje", mensaje,
                        "horarios", new ArrayList<>(),
                        "total", 0,
                        "razon", "SIN_HORARIOS_CONFIGURADOS"
                    );
                } else {
                    // Caso 2: Tiene horarios pero todos están ocupados
                    return Map.of(
                        "exito", false,
                        "mensaje", String.format(
                            "Todos los horarios están ocupados en los próximos 30 días.\n\n" +
                            "**Estadísticas:**\n" +
                            "• Slots generados: %d\n" +
                            "• Citas agendadas: %d\n" +
                            "• Horarios libres: 0\n\n" +
                            "**Sugerencias:**\n" +
                            "• Intenta con fechas posteriores\n" +
                            "• Consulta con otro médico de la misma especialidad\n" +
                            "• Contacta al consultorio para más opciones",
                            todosLosSlots.size(),
                            citasAgendadas.size()
                        ),
                        "horarios", new ArrayList<>(),
                        "total", 0,
                        "razon", "TODOS_OCUPADOS",
                        "slotsGenerados", todosLosSlots.size(),
                        "citasAgendadas", citasAgendadas.size()
                    );
                }
            }
            
            // ¡Hay horarios libres!
            return Map.of(
                "exito", true,
                "horarios", horariosLibres,
                "total", horariosLibres.size(),
                "periodo", "Próximos 30 días",
                "mensaje", "Horarios disponibles para agendar",
                "slotsGenerados", todosLosSlots.size(),
                "citasAgendadas", citasAgendadas.size(),
                "horariosLibres", horariosLibres.size()
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
    public Map<String, Object> agendarCita(Long pacienteId, Long usuarioId, Long medicoId, String fecha, String horaInicio, String horaFin, Long servicioId, String motivo, Map<String, Object> pagoData) {
        log.info("IA solicitó agendar cita - PacienteID: {}, UsuarioID: {}, Médico: {}, Fecha: {}, Hora: {}-{}, Servicio: {}", 
                 pacienteId, usuarioId, medicoId, fecha, horaInicio, horaFin, servicioId);
        
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
            
            // Validar médico
            Optional<Medico> medicoOpt = medicoRepository.findById(medicoId);
            if (medicoOpt.isEmpty()) {
                return Map.of("exito", false, "mensaje", "Médico no encontrado");
            }
            
            Medico medico = medicoOpt.get();
            log.info("Médico encontrado: ID {}, Nombre: {}", medicoId, medico.getUsuario().getNombre());
            
            // Validar servicio
            Optional<Servicio> servicioOpt = servicioRepository.findById(servicioId);
            if (servicioOpt.isEmpty()) {
                return Map.of("exito", false, "mensaje", "Servicio no encontrado");
            }
            
            Servicio servicio = servicioOpt.get();
            
            // Validar fecha y hora
            if (fecha == null || horaInicio == null || horaFin == null) {
                return Map.of("exito", false, "mensaje", "Fecha y horas son requeridas");
            }
            
            LocalDate fechaCita;
            LocalTime horaInicioCita;
            LocalTime horaFinCita;
            
            try {
                fechaCita = LocalDate.parse(fecha);
                horaInicioCita = LocalTime.parse(horaInicio);
                horaFinCita = LocalTime.parse(horaFin);
            } catch (Exception e) {
                return Map.of("exito", false, "mensaje", "Formato de fecha u hora inválido");
            }
            
            // Verificar que la fecha no sea en el pasado
            if (fechaCita.isBefore(LocalDate.now())) {
                return Map.of("exito", false, "mensaje", "No se pueden agendar citas en fechas pasadas");
            }
            
            // Verificar que el horario esté dentro del horario del médico
            List<HorarioMedico> horariosDelMedico = horarioMedicoRepository.findByMedicoId(medicoId);
            if (horariosDelMedico.isEmpty()) {
                return Map.of("exito", false, "mensaje", "El médico no tiene horario configurado");
            }
            
            // Buscar el horario que aplica para esta fecha
            HorarioMedico horarioAplicable = null;
            for (HorarioMedico hm : horariosDelMedico) {
                // Verificar si la fecha de la cita está dentro del rango de validez
                if (!fechaCita.isBefore(hm.getFecha()) && 
                    (hm.getValidUntil() == null || !fechaCita.isAfter(hm.getValidUntil()))) {
                    horarioAplicable = hm;
                    break;
                }
            }
            
            if (horarioAplicable == null) {
                return Map.of("exito", false, "mensaje", "El médico no tiene horario disponible para la fecha solicitada");
            }
            
            // Verificar que la hora esté dentro del horario laboral del médico
            if (horaInicioCita.isBefore(horarioAplicable.getHorarioInicio()) || 
                horaFinCita.isAfter(horarioAplicable.getHorarioFin())) {
                return Map.of("exito", false, "mensaje", 
                    String.format("El horario solicitado está fuera del horario laboral del médico (%s - %s)", 
                        horarioAplicable.getHorarioInicio(), horarioAplicable.getHorarioFin()));
            }
            
            // Verificar que no haya conflictos con otras citas del médico en esa fecha y hora
            // Buscar citas en la agenda para este médico en este horario
            List<Agenda> agendasOcupadas = agendaRepository.findAll().stream()
                .filter(a -> a.getMedico() != null && a.getMedico().getId().equals(medicoId))
                .filter(a -> a.getFecha() != null && a.getFecha().toLocalDate().equals(fechaCita))
                .filter(a -> {
                    // Verificar superposición de horarios
                    LocalTime inicioExistente = a.getHoraInicio();
                    LocalTime finExistente = a.getHoraFin();
                    
                    // Hay conflicto si los rangos se superponen
                    return !(horaFinCita.isBefore(inicioExistente) || horaFinCita.equals(inicioExistente) ||
                            horaInicioCita.isAfter(finExistente) || horaInicioCita.equals(finExistente));
                })
                .collect(Collectors.toList());
            
            if (!agendasOcupadas.isEmpty()) {
                return Map.of("exito", false, "mensaje", "El horario solicitado ya está ocupado. Por favor selecciona otro horario.");
            }
            
            // Crear registro en la agenda para esta cita
            Agenda nuevaAgenda = new Agenda();
            nuevaAgenda.setMedico(medico);
            nuevaAgenda.setFecha(LocalDateTime.of(fechaCita, horaInicioCita));
            nuevaAgenda.setHoraInicio(horaInicioCita);
            nuevaAgenda.setHoraFin(horaFinCita);
            
            Agenda agendaGuardada = agendaRepository.save(nuevaAgenda);
            log.info("Agenda creada para cita: ID {}, Médico: {}, Fecha: {}, Hora: {}-{}", 
                     agendaGuardada.getId(), medicoId, fechaCita, horaInicioCita, horaFinCita);
            
            // Verificar que el médico ofrezca el servicio
            // NOTA: Se relaja esta validación porque ahora el médico puede tener múltiples servicios o la relación puede ser compleja
            // Si el servicio existe y el médico tiene horario, permitimos la cita.
            
            // Crear la cita con estado PENDIENTE para que el médico la revise
            Cita nuevaCita = new Cita();
            nuevaCita.setPaciente(paciente);
            nuevaCita.setMedico(medico);
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
                    nuevaCita.setMontoPagado(servicio.getCosto()); // IMPORTANTE: Asegurar que tenga el monto
                    
                    // Generar un ID de pago simulado para compatibilidad con comprobantes
                    nuevaCita.setIdPago(System.currentTimeMillis());
                    nuevaCita.setNumeroReferenciaPago("REF-IA-" + System.currentTimeMillis());
                    nuevaCita.setMetodoPago("Tarjeta de Crédito");
                    
                    // MANTENER estado PENDIENTE para que el médico pueda aceptar
                    // El PDF se enviará cuando el médico acepte la cita
                    if (estatusPendiente.isPresent()) {
                        nuevaCita.setEstatus(estatusPendiente.get());
                    }
                    
                    log.info("Pago procesado exitosamente para cita IA - Estado: PENDIENTE (requiere aceptación del médico)");
                    
                } catch (Exception e) {
                    log.error("Error procesando pago para cita IA: {}", e.getMessage());
                    // El pago queda pendiente si hay error
                }
            }
            
            // Guardar la cita
            Cita citaGuardada = citaRepository.save(nuevaCita);
            log.info("Cita guardada con ID: {}, Médico: {}, EstadoPago: {}, Estatus: {}", 
                     citaGuardada.getId(), 
                     citaGuardada.getMedico() != null ? citaGuardada.getMedico().getId() : "null",
                     citaGuardada.getEstadoPago(),
                     citaGuardada.getEstatus() != null ? citaGuardada.getEstatus().getEstatus() : "null");
            
            // Asociar la agenda a la cita
            citaGuardada.setAgenda(agendaGuardada);
            citaRepository.save(citaGuardada);
            
            log.info("Cita agendada exitosamente: ID {}", citaGuardada.getId());
            
            // NO enviar comprobante inmediatamente - se enviará cuando el médico acepte la cita
            if (pagoData != null && !pagoData.isEmpty() && "PAGADO".equals(citaGuardada.getEstadoPago())) {
                log.info("=== PAGO PROCESADO - COMPROBANTE SE ENVIARÁ AL ACEPTAR CITA ===");
                log.info("Cita agendada con pago exitoso ID: {}, Estado: PENDIENTE", citaGuardada.getId());
                log.info("El comprobante PDF se enviará automáticamente cuando el médico acepte la cita");
            }
            
            // Notificar al médico sobre la nueva cita pendiente
            try {
                String emailMedico = medico.getUsuario().getCorreoElectronico();
                String nombrePaciente = paciente.getUsuario().getNombre() + " " + paciente.getUsuario().getApellidoPaterno();
                String nombreMedico = medico.getUsuario().getNombre() + " " + medico.getUsuario().getApellidoPaterno();
                String especialidad = servicio.getNombreServicio();
                String fechaCitaStr = agendaGuardada.getFecha().toLocalDate().toString() + " " + agendaGuardada.getHoraInicio().toString();
                
                // Enviar notificación al médico
                emailService.notificarNuevaCitaMedico(emailMedico, nombreMedico, nombrePaciente, especialidad, fechaCitaStr, citaGuardada.getMotivo());
                
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
                mensaje = "✅ ¡Pago procesado exitosamente! Tu cita está agendada y pendiente de aceptación del médico. Recibirás el comprobante PDF por email cuando el médico confirme la cita.";
                estado = "PAGADO_PENDIENTE";
                log.info("✅ Cita PAGADA pero PENDIENTE de aceptación médica");
            } else {
                mensaje = "Cita agendada exitosamente. El médico revisará y confirmará la cita.";
                estado = "PENDIENTE";
                log.info("⏳ Cita PENDIENTE - no cumple criterios de pago");
            }
            
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("exito", true);
            resultado.put("mensaje", mensaje);
            resultado.put("citaId", citaGuardada.getId());
            resultado.put("fecha", agendaGuardada.getFecha().toLocalDate().toString());
            resultado.put("horaInicio", agendaGuardada.getHoraInicio().toString());
            resultado.put("horaFin", agendaGuardada.getHoraFin().toString());
            resultado.put("medico", medico.getUsuario().getNombre() + " " + 
                           medico.getUsuario().getApellidoPaterno());
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

    /* 
@Transactional
public Map<String, Object> agendarCita
                            (Long pacienteId, 
                            Long usuarioId, 
                            Long horarioId, 
                            Long servicioId, 
                            String motivo, 
        Map<String, Object> pagoData) {
    
    // 1. SUBSISTEMA: Validar paciente (PacienteRepository)
    PacienteDetalle paciente = pacienteRepository.findByUsuarioIdUsuario(usuarioId);
    
    // 2. SUBSISTEMA: Validar horario (AgendaRepository)  
    Optional<Agenda> horarioOpt = agendaRepository.findById(horarioId);
    
    // 3. SUBSISTEMA: Validar servicio (ServicioRepository)
    Optional<Servicio> servicioOpt = servicioRepository.findById(servicioId);
    
    // 4. SUBSISTEMA: Crear cita (CitaRepository)
    Cita nuevaCita = new Cita();
    nuevaCita.setPaciente(paciente);
    nuevaCita.setMedico(horario.getMedico());
    nuevaCita.setServicio(servicio);
    Cita citaGuardada = citaRepository.save(nuevaCita);
    
    // 5. SUBSISTEMA: Procesar pago y enviar email (EmailService, ComprobanteService)
    if (pagoData != null) {
        emailService.enviarEmail(...);
    }
    
    // Retorna respuesta simplificada al cliente
    return Map.of("exito", true, "citaId", citaGuardada.getId(), ...);
    
    }
        */

    /**
     * Busca un médico específico por nombre
     */
    public Map<String, Object> buscarMedicoPorNombre(String nombre) {
        try {
            log.info("🔍 Buscando médico por nombre: {}", nombre);
            
            // Buscar médico por nombre (puede ser parcial)
            List<Medico> todosLosMedicos = medicoRepository.findAll();
            List<Medico> medicosEncontrados = todosLosMedicos.stream()
                .filter(medico -> {
                    String nombreCompleto = medico.getUsuario().getNombre() + " " + 
                                          medico.getUsuario().getApellidoPaterno();
                    return nombreCompleto.toLowerCase().contains(nombre.toLowerCase());
                })
                .toList();
            
            if (medicosEncontrados.isEmpty()) {
                log.warn("⚠️ No se encontró ningún médico con el nombre: {}", nombre);
                return Map.of(
                    "exito", false,
                    "mensaje", "No se encontró ningún médico con el nombre '" + nombre + "'",
                    "medicos", new ArrayList<>(),
                    "total", 0
                );
            }
            
            // Obtener servicios de cada médico encontrado
            List<Map<String, Object>> medicosConServicios = new ArrayList<>();
            for (Medico medico : medicosEncontrados) {
                Map<String, Object> medicoInfo = new HashMap<>();
                medicoInfo.put("id", medico.getId());
                medicoInfo.put("nombre", medico.getUsuario().getNombre());
                medicoInfo.put("apellidoPaterno", medico.getUsuario().getApellidoPaterno());
                medicoInfo.put("apellidoMaterno", medico.getUsuario().getApellidoMaterno());
                medicoInfo.put("nombreCompleto", 
                    "Dr. " + medico.getUsuario().getNombre() + " " + 
                    medico.getUsuario().getApellidoPaterno());
                
                // Obtener servicios del médico
                if (medico.getServicio() != null) {
                    Map<String, Object> servicioInfo = new HashMap<>();
                    servicioInfo.put("id", medico.getServicio().getId());
                    servicioInfo.put("nombre", medico.getServicio().getNombreServicio());
                    servicioInfo.put("descripcion", medico.getServicio().getDescripcionServicio());
                    servicioInfo.put("costo", medico.getServicio().getCosto());
                    medicoInfo.put("servicio", servicioInfo);
                } else {
                    medicoInfo.put("servicio", null);
                }
                
                medicosConServicios.add(medicoInfo);
            }
            
            log.info("✅ Encontrados {} médicos con el nombre '{}'", medicosEncontrados.size(), nombre);
            
            return Map.of(
                "exito", true,
                "mensaje", "Médicos encontrados correctamente",
                "medicos", medicosConServicios,
                "total", medicosEncontrados.size()
            );
            
        } catch (Exception e) {
            log.error("❌ Error buscando médico por nombre: {}", e.getMessage(), e);
            return Map.of(
                "exito", false,
                "mensaje", "Error interno del servidor: " + e.getMessage(),
                "medicos", new ArrayList<>(),
                "total", 0
            );
        }
    }

    /**
     * Obtiene los servicios que ofrece un médico específico
     */
    public Map<String, Object> obtenerServiciosMedico(Long medicoId) {
        try {
            log.info("🏥 Obteniendo servicios del médico ID: {}", medicoId);
            
            // Buscar el médico
            Optional<Medico> medicoOpt = medicoRepository.findById(medicoId);
            if (medicoOpt.isEmpty()) {
                log.warn("⚠️ Médico no encontrado con ID: {}", medicoId);
                return Map.of(
                    "exito", false,
                    "mensaje", "Médico no encontrado con ID: " + medicoId,
                    "servicios", new ArrayList<>(),
                    "total", 0
                );
            }
            
            Medico medico = medicoOpt.get();
            
            // Obtener información del médico
            Map<String, Object> medicoInfo = new HashMap<>();
            medicoInfo.put("id", medico.getId());
            medicoInfo.put("nombre", medico.getUsuario().getNombre());
            medicoInfo.put("apellidoPaterno", medico.getUsuario().getApellidoPaterno());
            medicoInfo.put("apellidoMaterno", medico.getUsuario().getApellidoMaterno());
            medicoInfo.put("nombreCompleto", 
                "Dr. " + medico.getUsuario().getNombre() + " " + 
                medico.getUsuario().getApellidoPaterno());
            
            // Obtener todos los servicios del médico (en la base de datos, puede haber múltiples registros)
            List<Medico> registrosMedico = medicoRepository.findAllByUsuario_IdUsuario(medico.getUsuario().getIdUsuario());
            
            List<Map<String, Object>> servicios = new ArrayList<>();
            for (Medico registro : registrosMedico) {
                if (registro.getServicio() != null) {
                    Map<String, Object> servicioInfo = new HashMap<>();
                    servicioInfo.put("id", registro.getServicio().getId());
                    servicioInfo.put("nombre", registro.getServicio().getNombreServicio());
                    servicioInfo.put("descripcion", registro.getServicio().getDescripcionServicio());
                    servicioInfo.put("costo", registro.getServicio().getCosto());
                    
                    // Verificar que no esté duplicado
                    boolean yaExiste = servicios.stream()
                        .anyMatch(s -> s.get("id").equals(registro.getServicio().getId()));
                    
                    if (!yaExiste) {
                        servicios.add(servicioInfo);
                    }
                }
            }
            
            log.info("✅ Encontrados {} servicios para el médico {}", servicios.size(), 
                medico.getUsuario().getNombre());
            
            String mensaje;
            if (servicios.isEmpty()) {
                mensaje = "El Dr. " + medico.getUsuario().getNombre() + " " + 
                         medico.getUsuario().getApellidoPaterno() + " no tiene servicios registrados";
            } else {
                mensaje = "El Dr. " + medico.getUsuario().getNombre() + " " + 
                         medico.getUsuario().getApellidoPaterno() + " ofrece " + servicios.size() + 
                         " tipo(s) de consulta";
            }
            
            return Map.of(
                "exito", true,
                "mensaje", mensaje,
                "medico", medicoInfo,
                "servicios", servicios,
                "total", servicios.size()
            );
            
        } catch (Exception e) {
            log.error("❌ Error obteniendo servicios del médico: {}", e.getMessage(), e);
            return Map.of(
                "exito", false,
                "mensaje", "Error interno del servidor: " + e.getMessage(),
                "servicios", new ArrayList<>(),
                "total", 0
            );
        }
    }
}
