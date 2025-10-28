package com.example.citasmedicas_backend.citas.repository;

import com.example.citasmedicas_backend.citas.model.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {
    
    List<Servicio> findByAreaId(Long areaId);
    
    @Query("SELECT s FROM Servicio s WHERE s.area.id = :areaId")
    List<Servicio> findServiciosByAreaId(@Param("areaId") Long areaId);
}