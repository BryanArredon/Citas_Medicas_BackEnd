package com.example.citasmedicas_backend.citas.service;

import com.example.citasmedicas_backend.citas.model.Area;
import com.example.citasmedicas_backend.citas.repository.AreasRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AreasService {

    @Autowired
    private AreasRepository areasRepository;

    // Guardar área (crear o actualizar)
    public Area save(Area area) {
        // Si es nuevo, asegurar que estatus sea true
        if (area.getId() == null) {
            area.setEstatus(true);
        }
        return areasRepository.save(area);
    }

    // Encontrar todas las áreas (incluyendo inactivas)
    public List<Area> findAll() {
        return areasRepository.findAll();
    }

    // Encontrar solo áreas activas
    public List<Area> findActiveAreas() {
        return areasRepository.findByEstatusTrue();
    }

    // Encontrar por ID (incluye inactivas)
    public Area findById(Long id) {
        return areasRepository.findById(id).orElse(null);
    }

    // Encontrar por ID solo si está activa
    public Area findActiveById(Long id) {
        return areasRepository.findByIdAndEstatusTrue(id).orElse(null);
    }

    // Eliminación física (no recomendada)
    public void deleteById(Long id) {
        areasRepository.deleteById(id);
    }

    // Eliminación lógica (cambiar estatus a false)
    public boolean deactivateArea(Long id) {
        Area area = findById(id);
        if (area != null && area.isEstatus()) {
            areasRepository.desactivarArea(id);
            return true;
        }
        return false;
    }

    // Métodos adicionales para mantener compatibilidad
    public List<Area> getAllAreas() {
        return findActiveAreas();
    }

    public Optional<Area> getAreaById(Long id) {
        return areasRepository.findById(id);
    }

    public Area createArea(Area area) {
        area.setEstatus(true);
        return areasRepository.save(area);
    }

    public Area updateArea(Long id, Area areaDetails) {
        return areasRepository.findById(id).map(area -> {
            area.setNombreArea(areaDetails.getNombreArea());
            area.setDescripcion(areaDetails.getDescripcion());
            // No actualizamos el estatus aquí, solo en desactivar
            return areasRepository.save(area);
        }).orElse(null);
    }

    // Método deleteArea ahora hace eliminación lógica
    public boolean deleteArea(Long id) {
        return deactivateArea(id);
    }
}