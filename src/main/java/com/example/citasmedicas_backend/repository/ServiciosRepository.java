package com.example.citasmedicas_backend.repository;

import com.example.citasmedicas_backend.model.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiciosRepository extends JpaRepository<Servicio, Long>{
}
