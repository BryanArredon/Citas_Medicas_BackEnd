package com.example.citasmedicas_backend.citas.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.citasmedicas_backend.citas.dto.CitaProximaDTO;
import com.example.citasmedicas_backend.citas.model.Agenda;
import com.example.citasmedicas_backend.citas.model.Cita;
import com.example.citasmedicas_backend.citas.model.Medico;
import com.example.citasmedicas_backend.citas.model.PacienteDetalle;
import com.example.citasmedicas_backend.citas.model.Servicio;
import com.example.citasmedicas_backend.citas.repository.CitaRepository;
import com.example.citasmedicas_backend.citas.service.CitaService;

@RestController
@RequestMapping("/api/citas")
public class CitaController {
    
    private static final Logger log = LoggerFactory.getLogger(CitaController.class);
    
    @Autowired
    private CitaService citaService;

    @Autowired
    private CitaRepository citaRepository;

    @GetMapping
    public List<Cita> getAllCitas() {
        return citaService.getAllCitas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> getCitaById(@PathVariable Long id) {
        Optional<Cita> cita = citaService.getCitaById(id);
        return cita.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Cita createCita(@RequestBody Map<String, Object> citaRequest) {
        System.out.println("📥 Datos recibidos del frontend: " + citaRequest);
        
        // Construir la entidad Cita desde el Map
        Cita cita = new Cita();
        
        // Paciente (solo referencia por ID)
        if (citaRequest.containsKey("pacienteId")) {
            PacienteDetalle paciente = new PacienteDetalle();
            paciente.setId(((Number) citaRequest.get("pacienteId")).longValue());
            cita.setPaciente(paciente);
        }
        
        // Médico (solo referencia por ID)
        if (citaRequest.containsKey("medicoId")) {
            Medico medico = new Medico();
            medico.setId(((Number) citaRequest.get("medicoId")).longValue());
            cita.setMedico(medico);
        }
        
        // Servicio (solo referencia por ID)
        if (citaRequest.containsKey("servicioId")) {
            Servicio servicio = new Servicio();
            servicio.setId(((Number) citaRequest.get("servicioId")).longValue());
            cita.setServicio(servicio);
        }
        
        // Agenda (opcional)
        if (citaRequest.containsKey("agendaId") && citaRequest.get("agendaId") != null) {
            Agenda agenda = new Agenda();
            agenda.setId(((Number) citaRequest.get("agendaId")).longValue());
            cita.setAgenda(agenda);
        }
        
        // Fecha/hora
        if (citaRequest.containsKey("fechaHora")) {
            String fechaHoraStr = (String) citaRequest.get("fechaHora");
            cita.setFechaSolicitud(LocalDateTime.parse(fechaHoraStr.substring(0, 19)));
        }
        
        // Motivo
        if (citaRequest.containsKey("motivo")) {
            cita.setMotivo((String) citaRequest.get("motivo"));
        }
        
        System.out.println("📋 Cita construida: Paciente=" + cita.getPaciente().getId() + 
                          ", Médico=" + cita.getMedico().getId() + 
                          ", Servicio=" + cita.getServicio().getId());
        
        return citaService.createCita(cita);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cita> updateCita(@PathVariable Long id, @RequestBody Cita citaDetails) {
        Cita updatedCita = citaService.updateCita(id, citaDetails);
        if (updatedCita != null) {
            return ResponseEntity.ok(updatedCita);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCita(@PathVariable Long id) {
        if (citaService.deleteCita(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/proximas/{usuarioId}")
    public ResponseEntity<List<CitaProximaDTO>> getCitasProximasByUsuario(@PathVariable Long usuarioId) {
        try {
            List<CitaProximaDTO> citasProximas = citaRepository.findCitasProximasByUsuarioId(usuarioId);
            
            if (citasProximas.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            
            return ResponseEntity.ok(citasProximas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/proximas/{usuarioId}/limit/{limit}")
    public ResponseEntity<List<CitaProximaDTO>> getCitasProximasByUsuarioWithLimit(
            @PathVariable Long usuarioId, 
            @PathVariable int limit) {
        try {
            List<CitaProximaDTO> todasLasCitas = citaRepository.findCitasProximasByUsuarioId(usuarioId);
            
            List<CitaProximaDTO> citasLimitadas = todasLasCitas.stream()
                    .limit(limit)
                    .toList();
            
            if (citasLimitadas.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            
            return ResponseEntity.ok(citasLimitadas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/medico/{medicoId}")
    public ResponseEntity<List<CitaProximaDTO>> getCitasByMedico(@PathVariable Long medicoId) {
        try {
            System.out.println("🔍 Buscando citas para médico ID: " + medicoId);
            List<CitaProximaDTO> citas = citaRepository.findCitasByMedicoId(medicoId);
            System.out.println("📊 Total de citas encontradas: " + citas.size());
            
            if (citas.isEmpty()) {
                return ResponseEntity.ok(List.of()); // Retornar lista vacía en lugar de 204
            }
            
            return ResponseEntity.ok(citas);
        } catch (Exception e) {
            System.err.println("❌ Error al buscar citas del médico: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }


    @PutMapping("/{id}/aceptar")
    public ResponseEntity<?> aceptarCita(@PathVariable Long id) {
        try {
            System.out.println("✅ Aceptando cita ID: " + id);
            Cita cita = citaService.aceptarCita(id);
            return ResponseEntity.ok(cita);
        } catch (RuntimeException e) {
            System.err.println("❌ Error al aceptar cita: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarCita(@PathVariable Long id) {
        try {
            citaService.cancelarCita(id);
            return ResponseEntity.ok(Map.of(
                "message", "Cita cancelada y eliminada exitosamente",
                "citaId", id
            ));
        } catch (RuntimeException e) {
            System.err.println("❌ Error al cancelar cita: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/posponer")
    public ResponseEntity<?> posponerCita(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        try {
            System.out.println("🕐 Posponiendo cita ID: " + id);
            String nuevaFecha = (String) data.get("nuevaFecha");
            if (nuevaFecha == null || nuevaFecha.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "La fecha nueva es requerida"));
            }
            
            LocalDateTime fechaHora = LocalDateTime.parse(nuevaFecha.substring(0, 19));
            Cita cita = citaService.posponerCita(id, fechaHora);
            return ResponseEntity.ok(cita);
        } catch (RuntimeException e) {
            System.err.println("❌ Error al posponer cita: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Procesa el pago de una cita
     * POST /api/citas/{id}/pagar
     */
    @PostMapping("/{id}/pagar")
    public ResponseEntity<?> procesarPagoCita(
            @PathVariable Long id,
            @RequestBody Map<String, Object> pagoData) {
        try {
            System.out.println("💳 Procesando pago para cita ID: " + id);
            System.out.println("📥 Datos de pago recibidos: " + pagoData);
            
            // Extraer datos del pago
            Long idMetodoPago = pagoData.containsKey("idMetodoPago") 
                ? ((Number) pagoData.get("idMetodoPago")).longValue() 
                : 1L; // Por defecto: Tarjeta de crédito
                
            Long idTarjeta = pagoData.containsKey("idTarjeta") 
                ? ((Number) pagoData.get("idTarjeta")).longValue() 
                : null;
            
            // Procesar el pago
            Cita citaActualizada = citaService.procesarPagoCita(id, idMetodoPago, idTarjeta);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Pago procesado exitosamente",
                "cita", citaActualizada,
                "pago", Map.of(
                    "idPago", citaActualizada.getIdPago(),
                    "monto", citaActualizada.getMontoPagado(),
                    "estado", citaActualizada.getEstadoPago(),
                    "referencia", citaActualizada.getNumeroReferenciaPago(),
                    "metodoPago", citaActualizada.getMetodoPago(),
                    "fechaPago", citaActualizada.getFechaPago()
                )
            ));
            
        } catch (RuntimeException e) {
            System.err.println("❌ Error al procesar pago: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Obtiene el estado del pago de una cita
     * GET /api/citas/{id}/pago
     */
    @GetMapping("/{id}/pago")
    public ResponseEntity<?> obtenerEstadoPago(@PathVariable Long id) {
        try {
            Optional<Cita> citaOpt = citaService.getCitaById(id);
            if (citaOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Cita cita = citaOpt.get();
            
            if (cita.getIdPago() == null) {
                return ResponseEntity.ok(Map.of(
                    "pagado", false,
                    "mensaje", "Esta cita aún no tiene un pago asociado"
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "pagado", true,
                "idPago", cita.getIdPago(),
                "monto", cita.getMontoPagado(),
                "estado", cita.getEstadoPago(),
                "referencia", cita.getNumeroReferenciaPago(),
                "metodoPago", cita.getMetodoPago(),
                "fechaPago", cita.getFechaPago()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        }
    }

    /**
     * 🆕 Crea una cita CON pago en una sola operación
     * POST /api/citas/con-pago
     * 
     * Este endpoint PRIMERO simula el pago, y solo si es aprobado, crea la cita.
     */
    @PostMapping("/con-pago")
    public ResponseEntity<?> crearCitaConPago(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("\n" + "=".repeat(80));
            System.out.println("🆕 ENDPOINT /con-pago INVOCADO");
            System.out.println("📥 Request completo: " + request);
            System.out.println("📦 Clase: " + request.getClass().getName());
            System.out.println("🔑 Keys: " + request.keySet());
            log.info("=".repeat(80));
            log.info("🆕 ENDPOINT /con-pago INVOCADO");
            log.info("📥 Request completo recibido: {}", request);
            log.info("📦 Tipo de request: {}", request.getClass().getName());
            log.info("🔑 Keys en request: {}", request.keySet());
            
            // Extraer datos de la cita
            @SuppressWarnings("unchecked")
            Map<String, Object> citaData = (Map<String, Object>) request.get("cita");
            @SuppressWarnings("unchecked")
            Map<String, Object> pagoData = (Map<String, Object>) request.get("pago");
            
            log.info("📋 citaData extraído: {}", citaData);
            log.info("💳 pagoData extraído: {}", pagoData);
            
            if (citaData == null) {
                log.error("❌ citaData es NULL");
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Faltan los datos de la cita"
                ));
            }
            
            if (pagoData == null) {
                log.error("❌ pagoData es NULL");
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Faltan los datos del pago"
                ));
            }
            
            log.info("✅ Datos validados, llamando al servicio...");
            // Llamar al servicio que maneja la lógica completa
            Cita citaCreada = citaService.crearCitaConPagoPrevio(citaData, pagoData);
            log.info("✅ Servicio ejecutado exitosamente");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Pago procesado y cita creada exitosamente",
                "cita", citaCreada,
                "pago", Map.of(
                    "idPago", citaCreada.getIdPago(),
                    "monto", citaCreada.getMontoPagado(),
                    "estado", citaCreada.getEstadoPago(),
                    "referencia", citaCreada.getNumeroReferenciaPago(),
                    "metodoPago", citaCreada.getMetodoPago(),
                    "fechaPago", citaCreada.getFechaPago()
                )
            ));
            
        } catch (RuntimeException e) {
            log.error("❌ ERROR CAPTURADO EN CONTROLADOR");
            log.error("❌ Tipo de excepción: {}", e.getClass().getName());
            log.error("❌ Mensaje: {}", e.getMessage());
            log.error("❌ Stack trace:", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("❌ ERROR INESPERADO EN CONTROLADOR");
            log.error("❌ Tipo: {}", e.getClass().getName());
            log.error("❌ Mensaje: {}", e.getMessage());
            log.error("❌ Stack trace:", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "Error interno del servidor: " + e.getMessage()
            ));
        }
    }
}
