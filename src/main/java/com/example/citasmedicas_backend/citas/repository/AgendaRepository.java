package com.example.citasmedicas_backend.citas.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.citasmedicas_backend.citas.model.Agenda;

@Repository
public interface AgendaRepository extends JpaRepository<Agenda, Long> {
    List<Agenda> findByMedicoId(Long medicoId);

    @Query("SELECT a FROM Agenda a WHERE a.medico.id = :idMedico AND DATE(a.fecha) = :fecha " +
           "AND a.id IN (SELECT c.agenda.id FROM Cita c WHERE c.estatus.id IN (1, 2, 5))")
    List<Agenda> findHorariosOcupados(@Param("idMedico") Long idMedico, @Param("fecha") LocalDate fecha);
}
