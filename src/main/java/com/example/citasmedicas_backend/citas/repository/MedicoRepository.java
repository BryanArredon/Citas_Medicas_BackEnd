package com.example.citasmedicas_backend.citas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

	Medico findByUsuario_IdUsuario(Long idUsuario);

	@Query(value = "SELECT * FROM medico_detalle WHERE id_servicio = :id", nativeQuery = true)
    List<Medico> findByServicioId(@Param("id") Long id);

    @Query(value = "SELECT * FROM medico_detalle WHERE id_usuario = :idUsuario", nativeQuery = true)
    List<Medico> findByUsuarioId(@Param("idUsuario") Long idUsuario);

}
