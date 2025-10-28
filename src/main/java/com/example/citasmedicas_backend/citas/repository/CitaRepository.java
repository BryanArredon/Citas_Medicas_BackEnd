package com.example.citasmedicas_backend.citas.repository;

import com.example.citasmedicas_backend.citas.dto.CitaProximaDTO;
import com.example.citasmedicas_backend.citas.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {
    @Query("SELECT new com.example.citasmedicas_backend.citas.dto.CitaProximaDTO(" +
           "c.id, ag.fecha, ag.horaInicio, ag.horaFin, " +
           "m.usuario.nombre, m.usuario.apellidoPaterno, m.usuario.apellidoMaterno, " +
           "s.nombreServicio, s.area.nombreArea, e.estatus, c.motivo, c.fechaSolicitud) " +
           "FROM Cita c " +
           "JOIN c.paciente p " +
           "JOIN c.medico m " +
           "JOIN c.agenda ag " +
           "JOIN c.servicio s " +
           "JOIN s.area " +
           "JOIN c.estatus e " +
           "WHERE p.usuario.idUsuario = :usuarioId " +
           "AND e.id IN (1, 2, 4, 5) " + // Aprobada, En proceso, Pospuesta, Pendiente
           "AND ag.fecha >= CURRENT_DATE " +
           "ORDER BY ag.fecha ASC, ag.horaInicio ASC")
    List<CitaProximaDTO> findCitasProximasByUsuarioId(@Param("usuarioId") Long usuarioId);

    @Query("SELECT c FROM Cita c JOIN c.agenda a " +
       "WHERE DATE(a.fecha) = :fecha AND c.estatus.id NOT IN (3, 6)")
    List<Cita> findCitasByFecha(@Param("fecha") LocalDate fecha);

    
    @Query("SELECT c FROM Cita c JOIN c.agenda a " +
       "WHERE c.medico.id = :idMedico AND DATE(a.fecha) = :fecha AND c.estatus.id NOT IN (3, 6)")
    List<Cita> findCitasByMedicoAndFecha(@Param("idMedico") Long idMedico, @Param("fecha") LocalDate fecha);
     
}
