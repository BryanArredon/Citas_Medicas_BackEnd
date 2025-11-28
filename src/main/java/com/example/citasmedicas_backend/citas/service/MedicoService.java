package com.example.citasmedicas_backend.citas.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.citasmedicas_backend.citas.model.EstadoMedico;
import com.example.citasmedicas_backend.citas.model.HorarioMedico;
import com.example.citasmedicas_backend.citas.model.Medico;
import com.example.citasmedicas_backend.citas.model.Servicio;
import com.example.citasmedicas_backend.citas.model.Usuario;
import com.example.citasmedicas_backend.citas.repository.MedicoRepository;

@Service
@Transactional
public class MedicoService {
    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private HorarioMedicoService horarioMedicoService;

    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MedicoService.class);

    @Transactional(rollbackFor = Exception.class)
    public Medico createMedico(Medico medico) {
        logger.info("Iniciando creación de médico");

        // 1. Validaciones mínimas: requiere usuario; servicio puede asignarlo el admin más tarde
        if (medico.getUsuario() == null) {
            String error = "Medico requiere usuario";
            logger.error(error);
            throw new IllegalArgumentException(error);
        }

        // 2. Persistir médico
        try {
            logger.info("Guardando médico: usuario={}, servicio={}",
                medico.getUsuario().getIdUsuario(),
                medico.getServicio() != null ? medico.getServicio().getId() : "(sin servicio)");

            Medico saved = medicoRepository.save(medico);
            logger.info("Médico guardado con id={}", saved.getId());

            // 3. Crear horarios por defecto
            LocalDate today = LocalDate.now();
            createDefaultHorarios(saved, today);

            return saved;

        } catch (Exception e) {
            String error = "Error al crear médico: " + e.getMessage();
            logger.error(error, e);
            throw new RuntimeException(error, e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Medico updateMedico(Medico medico) {
        logger.info("Actualizando médico existente: id={}", medico.getId());

        // 1. Validaciones mínimas
        if (medico.getId() == null) {
            String error = "ID de médico requerido para actualización";
            logger.error(error);
            throw new IllegalArgumentException(error);
        }

        // 2. Verificar que el médico existe
        Medico existing = medicoRepository.findById(medico.getId()).orElse(null);
        if (existing == null) {
            String error = "Médico no encontrado con ID: " + medico.getId();
            logger.error(error);
            throw new IllegalArgumentException(error);
        }

        // 3. Actualizar solo los campos permitidos (sin crear horarios)
        try {
            logger.info("Actualizando médico: id={}, cedula={}",
                medico.getId(), medico.getCedulaProfecional());

            existing.setCedulaProfecional(medico.getCedulaProfecional());
            // No actualizar servicio aquí, se maneja por separado

            Medico saved = medicoRepository.save(existing);
            logger.info("Médico actualizado exitosamente: id={}", saved.getId());

            return saved;

        } catch (Exception e) {
            String error = "Error al actualizar médico: " + e.getMessage();
            logger.error(error, e);
            throw new RuntimeException(error, e);
        }
    }

    public void deleteMedico(Long id) {
    medicoRepository.deleteById(id);
    logger.info("Médico y todos sus registros relacionados eliminados con CASCADE: {}", id);
}

    @Transactional(rollbackFor = Exception.class)
    protected void createDefaultHorarios(Medico medico, LocalDate fecha) {
        logger.info("Creando horario por defecto para médico id={}, fecha={}", medico.getId(), fecha);
        
        try {
            // Primero, eliminar cualquier horario existente para este médico
            logger.info("Eliminando horarios existentes para médico id={}", medico.getId());
            horarioMedicoService.deleteByMedicoId(medico.getId());
            
            // Crear un horario único para el día completo
            HorarioMedico horario = new HorarioMedico();
            horario.setMedico(medico);
            horario.setFecha(fecha);
            horario.setHorarioInicio(LocalTime.of(9, 0));
            horario.setHorarioFin(LocalTime.of(17, 0));
            horario.setDuracion(60); // 60 minutos por cita
            horario.setEstadoMedico(EstadoMedico.DISPONIBLE);
            
            horarioMedicoService.save(horario);
            
            logger.info("Creado horario único para médico id={}", medico.getId());
            
        } catch (Exception e) {
            String error = "Error al crear horario para médico id=" + medico.getId() + ": " + e.getMessage();
            logger.error(error, e);
            throw new RuntimeException(error, e);
        }
    }

    public List<Medico> getAllMedicos() {
        return medicoRepository.findAll();
    }

    public Medico findById(Long id) {
        return medicoRepository.findById(id).orElse(null);
    }

    public Medico findByUsuarioNombre(String nombre) {
        return medicoRepository.findByUsuario_Nombre(nombre);
    }

    public Medico findByUsuarioCorreo(String correo) {
        return medicoRepository.findByUsuario_CorreoElectronico(correo);
    }

    public List<Medico> createMedicoWithServices(Usuario usuario, List<Servicio> servicios, String cedula) {
        logger.info("Creando médico con múltiples servicios para usuario id={}, servicios={}", usuario.getIdUsuario(), servicios.size());
        
        List<Medico> medicosCreados = new ArrayList<>();
        
        if (servicios.isEmpty()) {
            // Crear un médico sin servicio asignado
            Medico medico = new Medico();
            medico.setUsuario(usuario);
            medico.setServicio(null);
            medico.setCedulaProfecional(cedula);
            Medico creado = createMedico(medico);
            medicosCreados.add(creado);
        } else {
            // Crear un registro por cada servicio
            for (Servicio servicio : servicios) {
                Medico medico = new Medico();
                medico.setUsuario(usuario);
                medico.setServicio(servicio);
                medico.setCedulaProfecional(cedula);
                Medico creado = createMedico(medico);
                medicosCreados.add(creado);
            }
        }
        
        logger.info("Creados {} registros de médico", medicosCreados.size());
        return medicosCreados;
    }

    public List<Servicio> getServiciosByUsuarioId(Long usuarioId) {
        List<Medico> medicos = findAllByUsuario_Id(usuarioId);
        return medicos.stream()
            .filter(m -> m.getServicio() != null)
            .map(Medico::getServicio)
            .distinct()
            .collect(Collectors.toList());
    }

    public List<Medico> findAllByUsuario_Id(Long usuarioId) {
        return medicoRepository.findAllByUsuario_IdUsuario(usuarioId);
    }
}