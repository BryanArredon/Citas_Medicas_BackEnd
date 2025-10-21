package com.example.citasmedicas_backend.citas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.citasmedicas_backend.citas.model.Agenda;

@Repository
public interface AgendaRepository extends JpaRepository<Agenda, Long> {
    List<Agenda> findByMedicoId(Long medicoId);
}
