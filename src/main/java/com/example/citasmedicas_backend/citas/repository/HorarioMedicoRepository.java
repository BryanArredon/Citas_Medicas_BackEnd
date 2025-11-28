package com.example.citasmedicas_backend.citas.repository;

import com.example.citasmedicas_backend.citas.model.HorarioMedico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HorarioMedicoRepository extends JpaRepository<HorarioMedico, Long>{

    @Modifying
    @Query("DELETE FROM HorarioMedico h WHERE h.medico.id = :medicoId")
    void deleteByMedicoId(@Param("medicoId") Long medicoId);
    
    /**
     * Busca un horario por médico y fecha específica
     */
    Optional<HorarioMedico> findByMedicoIdAndFecha(Long medicoId, LocalDate fecha);
    
    /**
     * Busca todos los horarios de un médico
     */
    List<HorarioMedico> findByMedicoId(Long medicoId);
    
    /**
     * Cuenta cuántos horarios tiene un médico
     */
    @Query("SELECT COUNT(h) FROM HorarioMedico h WHERE h.medico.id = :medicoId")
    long countByMedicoId(@Param("medicoId") Long medicoId);
}
