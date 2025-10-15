package com.example.citasmedicas_backend.citas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.citasmedicas_backend.citas.model.RolUser;

@Repository
public interface RolUserRepository extends JpaRepository<RolUser, Long> {
    RolUser findByNombreRol(String nombreRol);
}
