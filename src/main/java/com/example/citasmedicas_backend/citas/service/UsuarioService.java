package com.example.citasmedicas_backend.citas.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.citasmedicas_backend.citas.model.Paciente;
import com.example.citasmedicas_backend.citas.model.RolUser;
import com.example.citasmedicas_backend.citas.model.Usuario;
import com.example.citasmedicas_backend.citas.repository.PacienteRepository;
import com.example.citasmedicas_backend.citas.repository.RolUserRepository;
import com.example.citasmedicas_backend.citas.repository.UsuarioRepository;

@Service
@Transactional
public class UsuarioService {

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private PacienteRepository pacienteRepository;

    

	@Autowired
	private RolUserRepository rolUserRepository;

	public Usuario save(Usuario usuario) {
		// Resolve role: if client passed a RolUser with only nombreRol, fetch persisted RolUser
		RolUser incoming = usuario.getRolUser();
		if (incoming != null) {
			if (incoming.getIdRol() != null) {
				// try to load by id
				RolUser r = rolUserRepository.findById(incoming.getIdRol()).orElse(null);
				if (r != null) usuario.setRolUser(r);
			} else if (incoming.getNombreRol() != null) {
				RolUser r = rolUserRepository.findByNombreRol(incoming.getNombreRol());
				if (r != null) usuario.setRolUser(r);
			}
		}

		// Save usuario first
		Usuario saved = usuarioRepository.save(usuario);

		// If user's role is PACIENTE, ensure a Paciente row exists
		RolUser rol = saved.getRolUser();
		if (rol != null && "PACIENTE".equalsIgnoreCase(rol.getNombreRol())) {
			// create paciente if not exists
			if (!pacienteRepository.existsByUsuario_IdUsuario(saved.getIdUsuario())) {
				Paciente p = new Paciente();
				p.setUsuario(saved);
				pacienteRepository.save(p);
			}
		}

		// If user's role is MEDICO, do NOT auto-create a Medico row here because
		// the Medico entity requires additional non-null fields (servicio, cedulaProfesional)
		// that are not available from a simple user payload. Creating a stub causes
		// database constraint errors at flush/commit and marks the transaction rollback-only.
		// Instead, require that Medico rows are created via the Medico API/flow when the
		// necessary data is available.
		if (rol != null && "MEDICO".equalsIgnoreCase(rol.getNombreRol())) {
			// log/warn: admin should create a Medico record using the Medico endpoint
			System.out.println("Usuario creado con rol MEDICO: crear entidad Medico mediante el endpoint de Medicos cuando se disponga de servicio y cedula.");
		}

		return saved;
	}

	public List<Usuario> findAll() {
		return usuarioRepository.findAll();
	}

	public Usuario findById(Long id) {
		Optional<Usuario> u = usuarioRepository.findById(id);
		return u.orElse(null);
	}

	public void deleteById(Long id) {
		usuarioRepository.deleteById(id);
	}
}
