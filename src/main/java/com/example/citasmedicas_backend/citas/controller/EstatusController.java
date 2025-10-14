package com.example.citasmedicas_backend.citas.controller;

import com.example.citasmedicas_backend.citas.model.Estatus;
import com.example.citasmedicas_backend.citas.service.EstatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estatus")
public class EstatusController {
    @Autowired
    private EstatusService estatusService;

    @PostMapping
    public Estatus createEstatus(@RequestBody Estatus estatus) {
        return estatusService.createEstatus(estatus);
    }

    @GetMapping
    public List<Estatus> getAllEstatus() {
        return estatusService.getAllEstatus();
    }
}
