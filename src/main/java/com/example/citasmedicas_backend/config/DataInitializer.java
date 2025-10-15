package com.example.citasmedicas_backend.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.example.citasmedicas_backend.citas.model.RolUser;
import com.example.citasmedicas_backend.citas.repository.RolUserRepository;

@Component
public class DataInitializer {

    private final RolUserRepository rolRepo;

    public DataInitializer(RolUserRepository rolRepo) {
        this.rolRepo = rolRepo;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        if (rolRepo.count() == 0) {
            RolUser r1 = new RolUser(); r1.setNombreRol("PACIENTE"); rolRepo.save(r1);
            RolUser r2 = new RolUser(); r2.setNombreRol("MEDICO"); rolRepo.save(r2);
            RolUser r3 = new RolUser(); r3.setNombreRol("ADMIN"); rolRepo.save(r3);
        }
    }
}
