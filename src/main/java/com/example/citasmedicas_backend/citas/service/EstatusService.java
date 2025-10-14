package com.example.citasmedicas_backend.citas.service;

import com.example.citasmedicas_backend.citas.model.Estatus;
import com.example.citasmedicas_backend.citas.repository.EstatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EstatusService {
    @Autowired
    private EstatusRepository estatusRepository;

    public Estatus createEstatus(Estatus estatus) {
        return estatusRepository.save(estatus);
    }

    public List<Estatus> getAllEstatus() {
        return estatusRepository.findAll();
    }
}
