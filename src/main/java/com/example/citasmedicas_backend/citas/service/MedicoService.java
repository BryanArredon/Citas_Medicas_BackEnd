package com.example.citasmedicas_backend.citas.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.citasmedicas_backend.citas.model.EstadoMedico;
import com.example.citasmedicas_backend.citas.model.HorarioMedico;
import com.example.citasmedicas_backend.citas.model.Medico;
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

    public void deleteMedico(Long id) {
    medicoRepository.deleteById(id);
    logger.info("Médico y todos sus registros relacionados eliminados con CASCADE: {}", id);
}

    @Transactional(rollbackFor = Exception.class)
    protected void createDefaultHorarios(Medico medico, LocalDate fecha) {
        logger.info("Creando horarios por defecto para médico id={}, fecha={}", medico.getId(), fecha);
        
        try {
            // Crear 8 franjas horarias de 1 hora
            LocalTime startTime = LocalTime.of(9, 0);
            List<HorarioMedico> horarios = new ArrayList<>();
            
            for (int i = 0; i < 8; i++) {
                HorarioMedico horario = new HorarioMedico();
                horario.setMedico(medico);
                horario.setFecha(fecha);
                horario.setHorarioInicio(startTime.plusHours(i));
                horario.setHorarioFin(startTime.plusHours(i + 1));
                horario.setDuracion(60);
                horario.setEstadoMedico(EstadoMedico.DISPONIBLE);
                horarios.add(horario);
            }

            // Guardar todos los horarios
            for (HorarioMedico horario : horarios) {
                horarioMedicoService.save(horario);
            }
            
            logger.info("Creados {} horarios para médico id={}", horarios.size(), medico.getId());
            
        } catch (Exception e) {
            String error = "Error al crear horarios para médico id=" + medico.getId() + ": " + e.getMessage();
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

    public Medico findByUsuario_Id(Long usuarioId) {
        return medicoRepository.findByUsuario_IdUsuario(usuarioId);
    }
}