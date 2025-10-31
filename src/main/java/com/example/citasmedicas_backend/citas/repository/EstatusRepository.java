package com.example.citasmedicas_backend.citas.repository;

import com.example.citasmedicas_backend.citas.model.Estatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstatusRepository extends JpaRepository<Estatus, Long> {
    Optional<Estatus> findByEstatus(String estatus);
}
