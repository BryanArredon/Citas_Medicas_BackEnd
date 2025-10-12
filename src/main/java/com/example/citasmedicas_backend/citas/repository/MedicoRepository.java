package com.example.citasmedicas_backend.citas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.citasmedicas_backend.citas.model.Medico;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {
}
