package com.example.citasmedicas_backend.citas.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.citasmedicas_backend.citas.model.Medico;
import com.example.citasmedicas_backend.citas.model.Paciente;
import com.example.citasmedicas_backend.citas.model.RolUser;
import com.example.citasmedicas_backend.citas.model.Usuario;
import com.example.citasmedicas_backend.citas.repository.PacienteRepository;
import com.example.citasmedicas_backend.citas.repository.RolUserRepository;
import com.example.citasmedicas_backend.citas.repository.ServicioRepository;
import com.example.citasmedicas_backend.citas.repository.UsuarioRepository;

@Service
@Transactional(rollbackFor = Exception.class) // Añadir esto para garantizar rollback
public class UsuarioService {

	private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private PacienteRepository pacienteRepository;

    

	@Autowired
	private RolUserRepository rolUserRepository;

	@Autowired
	private ServicioRepository servicioRepository;

	@Autowired
	private MedicoService medicoService;

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

		// Si el usuario es médico, crear la entidad Medico y sus horarios
		if (rol != null && (rol.getIdRol() == 2 || "MEDICO".equalsIgnoreCase(rol.getNombreRol()))) {
			logger.info("Creando registro de médico para usuario id={} nombre={}", saved.getIdUsuario(), saved.getNombre());

			// No asignar servicio ni área automáticamente: lo hará el administrador.
			// Solo crear la entidad Medico mínima vinculada al Usuario para que exista el registro;
			// el admin asignará servicio/área y otros datos posteriormente.
			Medico medico = new Medico();
			medico.setUsuario(saved);
			// No setServicio: lo decidirá el administrador
			medico.setCedulaProfecional("AUTO-" + saved.getIdUsuario());

			try {
				medico = medicoService.createMedico(medico); // Este método crea los horarios
				logger.info("Médico creado exitosamente (sin servicio/área): id={}", medico.getId());
			} catch (Exception e) {
				String error = "Error creando el médico: " + e.getMessage();
				logger.error(error, e);
				throw new RuntimeException(error, e);
			}
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
