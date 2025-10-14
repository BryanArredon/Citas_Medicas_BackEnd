package com.example.citasmedicas_backend.citas.repository;

import com.example.citasmedicas_backend.citas.model.Agenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgendaRepository extends JpaRepository<Agenda, Long> {
}
