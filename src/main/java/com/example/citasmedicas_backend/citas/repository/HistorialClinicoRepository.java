package com.example.citasmedicas_backend.citas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.citasmedicas_backend.citas.model.HistorialClinico;

@Repository
public interface HistorialClinicoRepository extends JpaRepository<HistorialClinico, Long> {
    List<HistorialClinico> findByPacientedetalleId(Long pacientedetalleId);
    List<HistorialClinico> findByMedicoId(Long medicoId);
    List<HistorialClinico> findByCitaId(Long citaId);
}