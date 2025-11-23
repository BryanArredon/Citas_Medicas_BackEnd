package com.example.citasmedicas_backend.citas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.citasmedicas_backend.citas.model.Medico;
import java.util.List;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {
	// Buscar un médico por el nombre del usuario asociado (exact match)
	Medico findByUsuario_Nombre(String nombre);

	// También permitir buscar por correo en caso de necesitarlo
	Medico findByUsuario_CorreoElectronico(String correo);

	// Buscar por id de usuario
	List<Medico> findAllByUsuario_IdUsuario(Long idUsuario);
}