package com.example.citasmedicas_backend.citas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.citasmedicas_backend.citas.model.Paciente;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {
	// helper to check if a paciente row already exists for a usuario
	// note: Usuario primary key field is `idUsuario` so use that property name in the path
	boolean existsByUsuario_IdUsuario(Long usuarioId);

	@Query("SELECT p FROM Paciente p WHERE p.usuario.idUsuario = :usuarioId")
    Optional<Paciente> findByUsuarioId(@Param("usuarioId") Long usuarioId);
	
}
