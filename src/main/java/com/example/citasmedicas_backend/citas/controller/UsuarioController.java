package com.example.citasmedicas_backend.citas.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.citasmedicas_backend.citas.model.Usuario;
import com.example.citasmedicas_backend.citas.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public List<Usuario> listAll() {
        return usuarioService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> getById(@PathVariable Long id) {
        Usuario u = usuarioService.findById(id);
        if (u == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(u);
    }

    @PostMapping
    public ResponseEntity<Usuario> create(@RequestBody Usuario usuario) {
        // ensure id is null so JPA will insert
        usuario.setIdUsuario(null);
        Usuario saved = usuarioService.save(usuario);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> update(@PathVariable Long id, @RequestBody Usuario usuario) {
        Usuario existing = usuarioService.findById(id);
        if (existing == null) return ResponseEntity.notFound().build();

        // Update only provided fields, keep required fields from existing user
        if (usuario.getNombre() != null) existing.setNombre(usuario.getNombre());
        if (usuario.getApellidoPaterno() != null) existing.setApellidoPaterno(usuario.getApellidoPaterno());
        if (usuario.getApellidoMaterno() != null) existing.setApellidoMaterno(usuario.getApellidoMaterno());
        if (usuario.getSexo() != null) existing.setSexo(usuario.getSexo());
        if (usuario.getFechaNacimiento() != null) existing.setFechaNacimiento(usuario.getFechaNacimiento());
        if (usuario.getDireccion() != null) existing.setDireccion(usuario.getDireccion());
        if (usuario.getTelefono() != null) existing.setTelefono(usuario.getTelefono());
        if (usuario.getCorreoElectronico() != null) existing.setCorreoElectronico(usuario.getCorreoElectronico());
        // Keep contraseña and rolUser from existing user

        Usuario saved = usuarioService.save(existing);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Usuario existing = usuarioService.findById(id);
        if (existing == null) return ResponseEntity.notFound().build();
        usuarioService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

