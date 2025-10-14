package com.example.citasmedicas_backend.citas.repository;

import com.example.citasmedicas_backend.citas.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {
}
