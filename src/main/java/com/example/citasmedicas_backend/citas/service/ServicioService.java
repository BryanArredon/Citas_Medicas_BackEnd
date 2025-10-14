package com.example.citasmedicas_backend.citas.service;

import com.example.citasmedicas_backend.citas.model.Servicio;
import com.example.citasmedicas_backend.citas.repository.ServicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicioService {
    @Autowired
    private ServicioRepository servicioRepository;

    public Servicio createServicio(Servicio servicio) {
        return servicioRepository.save(servicio);
    }

    public List<Servicio> getAllServicios() {
        return servicioRepository.findAll();
    }
}
