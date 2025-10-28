package com.example.citasmedicas_backend.citas.repository;

import com.example.citasmedicas_backend.citas.model.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface AreasRepository extends JpaRepository<Area, Long> {
    
    // Buscar áreas activas
    List<Area> findByEstatusTrue();
    
    // Buscar área por ID solo si está activa
    Optional<Area> findByIdAndEstatusTrue(Long id);
    
    // Eliminación lógica
    @Modifying
    @Query("UPDATE Area a SET a.estatus = false WHERE a.id = :id")
    void desactivarArea(@Param("id") Long id);
}