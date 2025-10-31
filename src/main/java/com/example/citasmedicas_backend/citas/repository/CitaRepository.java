package com.example.citasmedicas_backend.citas.repository;

import com.example.citasmedicas_backend.citas.dto.CitaProximaDTO;
import com.example.citasmedicas_backend.citas.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {
    @Query("SELECT new com.example.citasmedicas_backend.citas.dto.CitaProximaDTO(" +
           "c.id, ag.fecha, ag.horaInicio, ag.horaFin, " +
           "m.usuario.nombre, m.usuario.apellidoPaterno, m.usuario.apellidoMaterno, " +
           "s.nombreServicio, s.area.nombreArea, 'Confirmada', c.motivo, c.fechaSolicitud) " +
           "FROM Cita c " +
           "JOIN c.paciente p " +
           "JOIN c.medico m " +
           "JOIN c.agenda ag " +
           "JOIN c.servicio s " +
           "JOIN s.area " +
           "WHERE p.usuario.idUsuario = :usuarioId " +
           "AND ag.fecha >= CURRENT_DATE " +
           "ORDER BY ag.fecha ASC, ag.horaInicio ASC")
    List<CitaProximaDTO> findCitasProximasByUsuarioId(@Param("usuarioId") Long usuarioId);

    @Query("SELECT new com.example.citasmedicas_backend.citas.dto.CitaProximaDTO(" +
           "c.id, ag.fecha, ag.horaInicio, ag.horaFin, " +
           "p.usuario.nombre, p.usuario.apellidoPaterno, p.usuario.apellidoMaterno, " +
           "s.nombreServicio, s.area.nombreArea, 'Confirmada', c.motivo, c.fechaSolicitud) " +
           "FROM Cita c " +
           "JOIN c.paciente p " +
           "JOIN c.medico m " +
           "JOIN c.agenda ag " +
           "JOIN c.servicio s " +
           "JOIN s.area " +
           "WHERE m.id = :medicoId " +
           "ORDER BY c.fechaSolicitud DESC")
    List<CitaProximaDTO> findCitasByMedicoId(@Param("medicoId") Long medicoId);
}
