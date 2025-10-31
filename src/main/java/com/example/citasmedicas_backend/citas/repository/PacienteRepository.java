package com.example.citasmedicas_backend.citas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.citasmedicas_backend.citas.model.PacienteDetalle;

@Repository
public interface PacienteRepository extends JpaRepository<PacienteDetalle, Long> {
	// helper to check if a paciente row already exists for a usuario
	// note: Usuario primary key field is `idUsuario` so use that property name in the path
	boolean existsByUsuario_IdUsuario(Long usuarioId);

	PacienteDetalle findByUsuarioIdUsuario(Long usuarioId);
}
