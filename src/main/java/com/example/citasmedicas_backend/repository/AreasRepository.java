package com.example.citasmedicas_backend.repository;

import com.example.citasmedicas_backend.model.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AreasRepository extends JpaRepository<Area, Long>{
}
