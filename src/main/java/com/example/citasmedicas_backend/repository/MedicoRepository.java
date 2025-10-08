package com.example.citasmedicas_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.citasmedicas_backend.model.Medico;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {
}
