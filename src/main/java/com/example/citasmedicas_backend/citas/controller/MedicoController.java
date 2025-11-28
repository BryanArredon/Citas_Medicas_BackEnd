package com.example.citasmedicas_backend.citas.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.citasmedicas_backend.citas.model.HorarioMedico;
import com.example.citasmedicas_backend.citas.model.Medico;
import com.example.citasmedicas_backend.citas.model.MedicoCreateDTO;
import com.example.citasmedicas_backend.citas.model.Servicio;
import com.example.citasmedicas_backend.citas.model.Usuario;
import com.example.citasmedicas_backend.citas.service.HorarioMedicoService;
import com.example.citasmedicas_backend.citas.service.MedicoService;
import com.example.citasmedicas_backend.citas.service.UsuarioService;
import com.example.citasmedicas_backend.citas.repository.ServicioRepository;

@RestController
@RequestMapping("/api/medicos")
public class MedicoController {
    @Autowired
    private MedicoService medicoService;

    @Autowired
    private HorarioMedicoService horarioMedicoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ServicioRepository servicioRepository;

    @GetMapping("/usuario/{usuarioId}/servicios")
    public ResponseEntity<List<Servicio>> getServiciosByUsuario(@PathVariable Long usuarioId) {
        try {
            List<Servicio> servicios = medicoService.getServiciosByUsuarioId(usuarioId);
            return ResponseEntity.ok(servicios);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/con-servicios")
    public ResponseEntity<?> createMedicoWithServices(@RequestBody MedicoCreateDTO medicoDTO) {
        try {
            System.out.println("=== CREANDO MÉDICO CON MÚLTIPLES SERVICIOS ===");
            System.out.println("ID Usuario: " + medicoDTO.getIdUsuario());
            System.out.println("Cédula: " + medicoDTO.getCedulaProfecional());
            System.out.println("Servicios: " + medicoDTO.getServiciosIds());

            // Verificar que el usuario existe
            Usuario usuario = usuarioService.findById(medicoDTO.getIdUsuario());
            if (usuario == null) {
                return ResponseEntity.badRequest().body("Usuario no encontrado con ID: " + medicoDTO.getIdUsuario());
            }

            // Verificar si ya existe un médico para este usuario
            List<Medico> existing = medicoService.findAllByUsuario_Id(medicoDTO.getIdUsuario());
            if (!existing.isEmpty()) {
                System.out.println("Médico ya existe para usuario " + medicoDTO.getIdUsuario() + ", retornando existente");
                return ResponseEntity.ok(existing.get(0));
            }

            List<Medico> medicosCreados = new ArrayList<>();
            
            // Crear un registro por cada servicio
            for (Long servicioId : medicoDTO.getServiciosIds()) {
                Servicio servicio = servicioRepository.findById(servicioId).orElse(null);
                if (servicio == null) {
                    System.out.println("⚠️ Servicio no encontrado: " + servicioId);
                    continue; // Saltar este servicio y continuar con los demás
                }

                // Crear nuevo registro de médico
                Medico medico = new Medico();
                medico.setUsuario(usuario);
                medico.setServicio(servicio);
                medico.setCedulaProfecional(medicoDTO.getCedulaProfecional());

                System.out.println("Creando médico - Servicio: " + servicioId);
                Medico medicoCreado = medicoService.createMedico(medico);
                medicosCreados.add(medicoCreado);
            }

            // Si no se especificaron servicios, crear un médico sin servicio asignado
            if (medicosCreados.isEmpty()) {
                Medico medico = new Medico();
                medico.setUsuario(usuario);
                medico.setServicio(null); // Sin servicio asignado
                medico.setCedulaProfecional(medicoDTO.getCedulaProfecional());

                System.out.println("Creando médico sin servicio asignado");
                Medico medicoCreado = medicoService.createMedico(medico);
                medicosCreados.add(medicoCreado);
            }

            System.out.println("✅ Médicos creados exitosamente: " + medicosCreados.size() + " registros");
            // Si solo se creó uno, devolver el objeto individual, sino la lista
            if (medicosCreados.size() == 1) {
                return ResponseEntity.ok(medicosCreados.get(0));
            } else {
                return ResponseEntity.ok(medicosCreados);
            }

        } catch (Exception e) {
            System.err.println("❌ Error creando médico: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error creando médico: " + e.getMessage());
        }
    }

    
    // En MedicoController.java - agregar estos métodos

// Eliminar médico por ID
@DeleteMapping("/{id}")
public ResponseEntity<?> deleteMedico(@PathVariable Long id) {
    try {
        System.out.println("🗑️ Eliminando médico con ID: " + id);
        Medico medico = medicoService.findById(id);
        if (medico == null) {
            return ResponseEntity.notFound().build();
        }
        
        medicoService.deleteMedico(id);
        System.out.println("✅ Médico eliminado exitosamente");
        return ResponseEntity.noContent().build();
        
    } catch (Exception e) {
        System.err.println("❌ Error eliminando médico: " + e.getMessage());
        return ResponseEntity.status(500).body("Error eliminando médico: " + e.getMessage());
    }
}

// Eliminar todos los registros de médico por usuario ID
@DeleteMapping("/usuario/{usuarioId}")
public ResponseEntity<?> deleteMedicosByUsuario(@PathVariable Long usuarioId) {
    try {
        System.out.println("🗑️ Eliminando médicos del usuario ID: " + usuarioId);
        
        // Buscar todos los registros de médico para este usuario
        List<Medico> medicos = medicoService.getAllMedicos().stream()
            .filter(m -> m.getUsuario() != null && m.getUsuario().getIdUsuario().equals(usuarioId))
            .collect(Collectors.toList());
            
        for (Medico medico : medicos) {
            medicoService.deleteMedico(medico.getId());
        }
        
        System.out.println("✅ " + medicos.size() + " registros de médico eliminados");
        return ResponseEntity.noContent().build();
        
    } catch (Exception e) {
        System.err.println("❌ Error eliminando médicos del usuario: " + e.getMessage());
        return ResponseEntity.status(500).body("Error eliminando médicos: " + e.getMessage());
    }
}

// Actualizar médico con múltiples servicios
@PutMapping("/usuario/{usuarioId}")
public ResponseEntity<?> updateMedicoWithServices(
        @PathVariable Long usuarioId, 
        @RequestBody MedicoCreateDTO medicoDTO) {
    try {
        System.out.println("🔄 Actualizando médico del usuario ID: " + usuarioId);
        System.out.println("Nuevos servicios: " + medicoDTO.getServiciosIds());
        System.out.println("Nueva cédula: " + medicoDTO.getCedulaProfecional());

        // Verificar que el usuario existe
        Usuario usuario = usuarioService.findById(usuarioId);
        if (usuario == null) {
            return ResponseEntity.badRequest().body("Usuario no encontrado con ID: " + usuarioId);
        }

        // 1. Obtener registros existentes de este médico
        List<Medico> medicosExistentes = medicoService.getAllMedicos().stream()
            .filter(m -> m.getUsuario() != null && m.getUsuario().getIdUsuario().equals(usuarioId))
            .collect(Collectors.toList());
        
        System.out.println("📋 Registros existentes: " + medicosExistentes.size());
        
        // Crear listas de IDs de servicios
        List<Long> serviciosExistentesIds = medicosExistentes.stream()
            .filter(m -> m.getServicio() != null)
            .map(m -> m.getServicio().getId())
            .collect(Collectors.toList());
        
        List<Long> nuevosServiciosIds = medicoDTO.getServiciosIds();
        
        // 2. Identificar servicios a eliminar (existentes pero no en la nueva lista)
        List<Long> serviciosAEliminar = serviciosExistentesIds.stream()
            .filter(id -> !nuevosServiciosIds.contains(id))
            .collect(Collectors.toList());
        
        // 3. Identificar servicios a mantener (que siguen en la nueva lista)
        List<Long> serviciosAMantener = serviciosExistentesIds.stream()
            .filter(id -> nuevosServiciosIds.contains(id))
            .collect(Collectors.toList());
        
        // 4. Identificar servicios a agregar (nuevos que no existen)
        List<Long> serviciosAAgregar = nuevosServiciosIds.stream()
            .filter(id -> !serviciosExistentesIds.contains(id))
            .collect(Collectors.toList());
        
        // 5. Eliminar registros de servicios que ya no están seleccionados
        // PERO primero verificar si tienen horarios/citas asociadas
        List<String> serviciosConCitas = new ArrayList<>();
        
        for (Medico medico : medicosExistentes) {
            if (medico.getServicio() != null && serviciosAEliminar.contains(medico.getServicio().getId())) {
                // Verificar si este registro tiene horarios asociados
                List<HorarioMedico> horarios = horarioMedicoService.findByMedicoId(medico.getId());
                
                if (!horarios.isEmpty()) {
                    // Si tiene horarios, no puede eliminarse porque puede tener citas o agendas asociadas
                    serviciosConCitas.add(medico.getServicio().getNombreServicio());
                    System.out.println("⚠️ No se puede eliminar servicio con horarios: " + medico.getServicio().getNombreServicio());
                } else {
                    System.out.println("🗑️ Eliminando servicio: " + medico.getServicio().getNombreServicio());
                    medicoService.deleteMedico(medico.getId());
                }
            }
        }
        
        // Si hay servicios con horarios/citas, retornar error
        if (!serviciosConCitas.isEmpty()) {
            String mensaje = "No se pueden eliminar los siguientes servicios porque tienen horarios o citas agendadas: " + 
                           String.join(", ", serviciosConCitas) + 
                           ". Por favor, elimine primero todos los horarios y citas asociadas a estos servicios.";
            System.err.println("❌ " + mensaje);
            return ResponseEntity.status(400).body(mensaje);
        }
        
        // 6. Actualizar cédula SOLO en los registros que se mantienen
        for (Medico medico : medicosExistentes) {
            if (medico.getServicio() != null && serviciosAMantener.contains(medico.getServicio().getId())) {
                medico.setCedulaProfecional(medicoDTO.getCedulaProfecional());
                medicoService.updateMedico(medico); // Usar updateMedico en lugar de createMedico
                System.out.println("✏️ Actualizando cédula del servicio: " + medico.getServicio().getNombreServicio());
            }
        }
        
        // 7. Crear registros para servicios nuevos
        for (Long servicioId : serviciosAAgregar) {
            Servicio servicio = servicioRepository.findById(servicioId).orElse(null);
            if (servicio == null) {
                System.out.println("⚠️ Servicio no encontrado: " + servicioId);
                continue;
            }

            Medico nuevoMedico = new Medico();
            nuevoMedico.setUsuario(usuario);
            nuevoMedico.setServicio(servicio);
            nuevoMedico.setCedulaProfecional(medicoDTO.getCedulaProfecional());

            System.out.println("➕ Agregando nuevo servicio: " + servicio.getNombreServicio());
            medicoService.createMedico(nuevoMedico);
        }
        
        // 8. Obtener todos los registros actualizados
        List<Medico> medicosActualizados = medicoService.getAllMedicos().stream()
            .filter(m -> m.getUsuario() != null && m.getUsuario().getIdUsuario().equals(usuarioId))
            .collect(Collectors.toList());

        System.out.println("✅ Médico actualizado exitosamente: " + medicosActualizados.size() + " registros");
        System.out.println("   - Servicios eliminados: " + serviciosAEliminar.size());
        System.out.println("   - Servicios agregados: " + serviciosAAgregar.size());
        System.out.println("   - Servicios mantenidos: " + serviciosAMantener.size());
        
        return ResponseEntity.ok(medicosActualizados);

    } catch (Exception e) {
        System.err.println("❌ Error actualizando médico: " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.status(500).body("Error actualizando médico: " + e.getMessage());
    }
}

    @PostMapping
    public Medico createMedico(@RequestBody Medico medico) {
        return medicoService.createMedico(medico);
    }

    @GetMapping
    public List<Medico> getAllMedicos() {
        return medicoService.getAllMedicos();
    }
    
    // Obtener médico por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getMedicoById(@PathVariable Long id) {
        try {
            System.out.println("🔍 Buscando médico con ID: " + id);
            Medico medico = medicoService.findById(id);
            
            if (medico == null) {
                return ResponseEntity.notFound().build();
            }
            
            System.out.println("✅ Médico encontrado: " + medico.getUsuario().getNombre());
            return ResponseEntity.ok(medico);
            
        } catch (Exception e) {
            System.err.println("❌ Error al buscar médico: " + e.getMessage());
            return ResponseEntity.status(500).body("Error al buscar médico: " + e.getMessage());
        }
    }
    
    // Obtener médicos agrupados por usuario con todos sus servicios
    @GetMapping("/con-servicios")
    public ResponseEntity<?> getMedicosConServicios() {
        try {
            System.out.println("📋 Obteniendo médicos agrupados con servicios");
            
            List<Medico> todosMedicos = medicoService.getAllMedicos();
            
            // Agrupar por usuario
            Map<Long, List<Medico>> medicosPorUsuario = todosMedicos.stream()
                .filter(m -> m.getUsuario() != null && m.getUsuario().getIdUsuario() != null)
                .collect(Collectors.groupingBy(m -> m.getUsuario().getIdUsuario()));
            
            List<Map<String, Object>> resultado = new ArrayList<>();
            
            for (Map.Entry<Long, List<Medico>> entry : medicosPorUsuario.entrySet()) {
                List<Medico> registros = entry.getValue();
                Medico primero = registros.get(0);
                
                Map<String, Object> medicoInfo = new HashMap<>();
                medicoInfo.put("usuarioId", entry.getKey());
                medicoInfo.put("nombre", primero.getUsuario().getNombre());
                medicoInfo.put("apellidoPaterno", primero.getUsuario().getApellidoPaterno());
                medicoInfo.put("apellidoMaterno", primero.getUsuario().getApellidoMaterno());
                medicoInfo.put("cedula", primero.getCedulaProfecional());
                
                // Listar todos los registros de médico con sus servicios
                List<Map<String, Object>> registrosDetalle = registros.stream().map(m -> {
                    Map<String, Object> reg = new HashMap<>();
                    reg.put("medicoId", m.getId());
                    reg.put("servicioId", m.getServicio() != null ? m.getServicio().getId() : null);
                    reg.put("servicio", m.getServicio() != null ? m.getServicio().getNombreServicio() : "SIN SERVICIO");
                    
                    // Contar horarios de este registro específico
                    long horarios = horarioMedicoService.findByMedicoId(m.getId()).size();
                    reg.put("horarios", horarios);
                    
                    return reg;
                }).collect(Collectors.toList());
                
                medicoInfo.put("registros", registrosDetalle);
                medicoInfo.put("totalRegistros", registros.size());
                
                resultado.add(medicoInfo);
            }
            
            System.out.println("✅ Total usuarios médicos: " + resultado.size());
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // Crear múltiples horarios ligados a un médico existente
    @PostMapping("/{id}/horarios")
    public ResponseEntity<?> createHorariosForMedico(@PathVariable("id") Long medicoId,
                                                     @RequestBody List<HorarioMedico> horarios) {
        Medico medico = medicoService.findById(medicoId);
        if (medico == null) return ResponseEntity.notFound().build();

        List<HorarioMedico> created = new ArrayList<>();
        try {
            for (HorarioMedico h : horarios) {
                h.setMedico(medico);
                HorarioMedico saved = horarioMedicoService.save(h);
                created.add(saved);
            }
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error creando horarios: " + e.getMessage());
        }
    }

    // Obtener información completa del médico por usuario id
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Map<String, Object>> getMedicoByUsuarioId(@PathVariable Long usuarioId) {
        try {
            System.out.println("🔍 Obteniendo información de médico para usuario ID: " + usuarioId);
            
            Usuario usuario = usuarioService.findById(usuarioId);
            if (usuario == null) {
                System.out.println("❌ Usuario no encontrado con ID: " + usuarioId);
                return ResponseEntity.notFound().build();
            }

            List<Servicio> servicios = medicoService.getServiciosByUsuarioId(usuarioId);
            System.out.println("✅ Servicios encontrados: " + servicios.size());
            servicios.forEach(s -> System.out.println("  - " + s.getNombreServicio() + " (ID: " + s.getId() + ")"));
            
            String cedula = "AUTO-" + usuarioId; // Default cedula

            Map<String, Object> response = new HashMap<>();
            response.put("usuario", usuario);
            response.put("servicios", servicios);
            response.put("cedulaProfesional", cedula);

            System.out.println("📤 Respuesta completa preparada para enviar");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // Obtener medico por correo electrónico (query param)
    @GetMapping("/email")
    public ResponseEntity<Medico> findByUsuarioEmail(@RequestParam("correo") String correo) {
        Medico m = medicoService.findByUsuarioCorreo(correo);
        if (m == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(m);
    }

    // Obtener horarios del médico por usuario id
    @GetMapping("/usuario/{usuarioId}/horarios")
    public ResponseEntity<?> horariosByUsuarioId(@PathVariable("usuarioId") Long usuarioId) {
        List<Medico> medicos = medicoService.findAllByUsuario_Id(usuarioId);
        if (medicos.isEmpty()) return ResponseEntity.notFound().build();
        Medico m = medicos.get(0); // Usar el primer médico encontrado
        return ResponseEntity.ok(horarioMedicoService.findByMedicoId(m.getId()));
    }

    // Obtener horarios del médico por correo
    @GetMapping("/email/horarios")
    public ResponseEntity<?> horariosByUsuarioEmail(@RequestParam("correo") String correo) {
        Medico m = medicoService.findByUsuarioCorreo(correo);
        if (m == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(horarioMedicoService.findByMedicoId(m.getId()));
    }

    // Obtener médicos por servicio
    @GetMapping("/servicio/{servicioId}")
    public ResponseEntity<List<Medico>> getMedicosByServicio(@PathVariable Long servicioId) {
        List<Medico> medicos = medicoService.getAllMedicos();
        List<Medico> medicosPorServicio = medicos.stream()
            .filter(m -> m.getServicio() != null && m.getServicio().getId().equals(servicioId))
            .toList();
        return ResponseEntity.ok(medicosPorServicio);
    }

    // Nuevo endpoint para obtener registros de médicos por usuario (incluyendo IDs)
    @GetMapping("/registros/usuario/{usuarioId}")
    public ResponseEntity<List<Medico>> getMedicosRegistrosByUsuario(@PathVariable Long usuarioId) {
        try {
            System.out.println("🔍 Obteniendo registros de médicos para usuario ID: " + usuarioId);
            
            List<Medico> medicos = medicoService.findAllByUsuario_Id(usuarioId);
            
            System.out.println("📋 Registros encontrados: " + medicos.size());
            for (Medico medico : medicos) {
                System.out.println("  - Médico ID: " + medico.getId() + 
                                   ", Usuario: " + medico.getUsuario().getNombre() + 
                                   ", Servicio: " + (medico.getServicio() != null ? medico.getServicio().getNombreServicio() : "Sin servicio"));
            }
            
            if (medicos.isEmpty()) {
                System.out.println("⚠️ No se encontraron registros de médico para el usuario " + usuarioId);
                return ResponseEntity.ok(new ArrayList<>()); // Retornar lista vacía
            }
            
            return ResponseEntity.ok(medicos);
            
        } catch (Exception e) {
            System.err.println("❌ Error al obtener registros de médicos: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
