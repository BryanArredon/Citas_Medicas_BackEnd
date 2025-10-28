package com.example.citasmedicas_backend.citas.service;

import com.example.citasmedicas_backend.citas.model.Servicio;
import com.example.citasmedicas_backend.citas.repository.ServicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ServicioService {
    @Autowired
    private ServicioRepository servicioRepository;

    public List<Servicio> findAll() {
        return servicioRepository.findAll();
    }

    public Servicio findById(Long id) {
        Optional<Servicio> servicio = servicioRepository.findById(id);
        return servicio.orElse(null);
    }

    public Servicio save(Servicio servicio) {
        return servicioRepository.save(servicio);
    }

    public void deleteById(Long id) {
        servicioRepository.deleteById(id);
    }

    public List<Servicio> findByAreaId(Long areaId) {
        return servicioRepository.findByAreaId(areaId);
    }

    public List<Servicio> getAllServicios() {
        return servicioRepository.findAll();
    }

    public Optional<Servicio> getServicioById(Long id) {
        return servicioRepository.findById(id);
    }

    public List<Servicio> getServiciosByArea(Long areaId) {
        return servicioRepository.findByAreaId(areaId);
    }

    public Servicio createServicio(Servicio servicio) {
        return servicioRepository.save(servicio);
    }

    public Servicio updateServicio(Long id, Servicio servicioDetails) {
        return servicioRepository.findById(id).map(servicio -> {
            servicio.setNombreServicio(servicioDetails.getNombreServicio());
            servicio.setArea(servicioDetails.getArea());
            servicio.setDescripcionServicio(servicioDetails.getDescripcionServicio());
            servicio.setCosto(servicioDetails.getCosto());
            return servicioRepository.save(servicio);
        }).orElse(null);
    }

    public boolean deleteServicio(Long id) {
        if (servicioRepository.existsById(id)) {
            servicioRepository.deleteById(id);
            return true;
        }
        return false;
    }
}