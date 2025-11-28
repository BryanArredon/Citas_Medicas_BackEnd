package com.example.citasmedicas_backend.assistant.controller;

import com.example.citasmedicas_backend.assistant.service.IAAssistantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para las funciones que la IA puede ejecutar
 * Estos endpoints son llamados desde el frontend cuando la IA decide ejecutar una función
 */
@RestController
@RequestMapping("/api/ia/funciones")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class IAFuncionesController {

    private static final Logger log = LoggerFactory.getLogger(IAFuncionesController.class);
    private final IAAssistantService iaService;

    public IAFuncionesController(IAAssistantService iaService) {
        this.iaService = iaService;
    }

    /**
     * Obtiene áreas médicas
     */
    @GetMapping("/areas")
    public ResponseEntity<Map<String, Object>> obtenerAreas() {
        log.info("📡 API: Solicitando áreas médicas");
        try {
            Map<String, Object> resultado = iaService.obtenerAreas();
            log.info("Áreas obtenidas correctamente: {}", resultado);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al obtener áreas: ", e);
            Map<String, Object> error = new HashMap<>();
            error.put("exito", false);
            error.put("mensaje", "Error al obtener áreas: " + e.getMessage());
            error.put("tipo", e.getClass().getSimpleName());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Obtiene servicios médicos
     */
    @GetMapping("/servicios")
    public ResponseEntity<Map<String, Object>> obtenerServicios() {
        log.info("API: Solicitando servicios");
        try {
            Map<String, Object> resultado = iaService.obtenerServicios();
            log.info("Servicios obtenidos correctamente");
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al obtener servicios: ", e);
            Map<String, Object> error = new HashMap<>();
            error.put("exito", false);
            error.put("mensaje", "Error al obtener servicios: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Busca médicos por servicio o nombre
     */
    @GetMapping("/medicos")
    public ResponseEntity<Map<String, Object>> buscarMedicos(
            @RequestParam(required = false) Long servicioId,
            @RequestParam(required = false) String nombre) {
        log.info("API: Buscando médicos (servicio: {}, nombre: {})", servicioId, nombre);
        return ResponseEntity.ok(iaService.buscarMedicos(servicioId, nombre));
    }

    /**
     * Busca un médico específico por nombre
     */
    @GetMapping("/medico-por-nombre")
    public ResponseEntity<Map<String, Object>> buscarMedicoPorNombre(
            @RequestParam String nombre) {
        log.info("API: Buscando médico por nombre: {}", nombre);
        try {
            Map<String, Object> resultado = iaService.buscarMedicoPorNombre(nombre);
            log.info("Médico encontrado correctamente: {}", resultado);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al buscar médico por nombre: ", e);
            Map<String, Object> error = new HashMap<>();
            error.put("exito", false);
            error.put("mensaje", "Error al buscar médico: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Obtiene los servicios que ofrece un médico específico
     */
    @GetMapping("/medico/{medicoId}/servicios")
    public ResponseEntity<Map<String, Object>> obtenerServiciosMedico(@PathVariable Long medicoId) {
        log.info("API: Obteniendo servicios del médico: {}", medicoId);
        try {
            Map<String, Object> resultado = iaService.obtenerServiciosMedico(medicoId);
            log.info("Servicios del médico obtenidos correctamente: {}", resultado);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al obtener servicios del médico: ", e);
            Map<String, Object> error = new HashMap<>();
            error.put("exito", false);
            error.put("mensaje", "Error al obtener servicios del médico: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Obtiene próximos horarios disponibles
     * Si medicoId es null, retorna horarios de TODOS los médicos
     */
    @GetMapping("/horarios")
    public ResponseEntity<Map<String, Object>> obtenerHorarios(
            @RequestParam(required = false) Long medicoId) {
        log.info("API: Obteniendo horarios (médico: {})", medicoId != null ? medicoId : "TODOS");
        try {
            Map<String, Object> resultado = iaService.obtenerProximosHorariosDisponibles(medicoId);
            log.info("Horarios obtenidos correctamente: {}", resultado);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al obtener horarios: ", e);
            Map<String, Object> error = new HashMap<>();
            error.put("exito", false);
            error.put("mensaje", "Error al obtener horarios: " + e.getMessage());
            error.put("tipo", e.getClass().getSimpleName());
            error.put("horarios", new ArrayList<>());
            error.put("total", 0);
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Obtiene citas de un paciente
     */
    @GetMapping("/citas/paciente/{pacienteId}")
    public ResponseEntity<Map<String, Object>> obtenerCitasPaciente(
            @PathVariable Long pacienteId) {
        log.info("API: Obteniendo citas del paciente: {}", pacienteId);
        return ResponseEntity.ok(iaService.obtenerCitasPaciente(pacienteId));
    }

    /**
     * Crea una nueva cita
     */
    @PostMapping("/citas")
    public ResponseEntity<Map<String, Object>> crearCita(
            @RequestBody Map<String, Object> datos) {
        
        Long pacienteId = getLong(datos, "pacienteId");
        Long medicoId = getLong(datos, "medicoId");
        Long servicioId = getLong(datos, "servicioId");
        Long agendaId = getLong(datos, "agendaId");
        String motivo = (String) datos.get("motivo");
        
        log.info("API: Creando cita (paciente: {}, médico: {})", pacienteId, medicoId);
        
        return ResponseEntity.ok(
            iaService.crearCita(pacienteId, medicoId, servicioId, agendaId, motivo)
        );
    }

    /**
     * Cancela una cita
     */
    @DeleteMapping("/citas/{citaId}")
    public ResponseEntity<Map<String, Object>> cancelarCita(
            @PathVariable Long citaId,
            @RequestParam Long pacienteId) {
        
        log.info("API: Cancelando cita: {}", citaId);
        return ResponseEntity.ok(iaService.cancelarCita(citaId, pacienteId));
    }

    /**
     * Agendar/crear una nueva cita médica
     */
    @PostMapping("/agendar-cita")
    public ResponseEntity<Map<String, Object>> agendarCita(@RequestBody Map<String, Object> body) {
        Long pacienteId = getLong(body, "pacienteId");
        Long usuarioId = getLong(body, "usuarioId"); // Nuevo parámetro opcional
        Long medicoId = getLong(body, "medicoId");
        String fecha = (String) body.get("fecha");
        String horaInicio = (String) body.get("horaInicio");
        String horaFin = (String) body.get("horaFin");
        Long servicioId = getLong(body, "servicioId");
        String motivo = (String) body.get("motivo");
        
        // Extraer datos de pago si están presentes
        Map<String, Object> pagoData = (Map<String, Object>) body.get("pago");
        
        log.info("API: Agendando cita - Paciente: {}, Usuario: {}, Médico: {}, Fecha: {}, Hora: {}-{}, Servicio: {}", 
                 pacienteId, usuarioId, medicoId, fecha, horaInicio, horaFin, servicioId);
        
        return ResponseEntity.ok(iaService.agendarCita(pacienteId, usuarioId, medicoId, fecha, horaInicio, horaFin, servicioId, motivo, pagoData));
    }

    /**
     * Obtener datos del paciente por ID de usuario
     */
    @GetMapping("/paciente/{usuarioId}")
    public ResponseEntity<Map<String, Object>> obtenerDatosPaciente(@PathVariable Long usuarioId) {
        log.info("API: Obteniendo datos del paciente (usuario: {})", usuarioId);
        return ResponseEntity.ok(iaService.obtenerDatosPaciente(usuarioId));
    }

    /**
     * Procesa el pago de una cita pendiente
     */
    @PostMapping("/procesar-pago")
    public ResponseEntity<Map<String, Object>> procesarPago(@RequestBody Map<String, Object> datos) {
        Long citaId = getLong(datos, "citaId");
        @SuppressWarnings("unchecked")
        Map<String, Object> pagoData = (Map<String, Object>) datos.get("pagoData");
        
        log.info("API: Procesando pago para cita: {}", citaId);
        try {
            Map<String, Object> resultado = iaService.procesarPago(citaId, pagoData);
            log.info("Pago procesado correctamente: {}", resultado);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error procesando pago: ", e);
            Map<String, Object> error = new HashMap<>();
            error.put("exito", false);
            error.put("mensaje", "Error procesando pago: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Helper para convertir números del JSON
     */
    private Long getLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof String) return Long.parseLong((String) value);
        return null;
    }
}
