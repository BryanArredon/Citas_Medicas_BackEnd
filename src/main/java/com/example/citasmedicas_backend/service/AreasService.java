package com.example.citasmedicas_backend.service;

import java.time.*;
import java.util.*;

import com.example.citasmedicas_backend.model.Area;
import com.example.citasmedicas_backend.repository.AreasRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class AreasService {


    @Autowired
    private AreasRepository areasRepository;

    public Area save(Area area) {
        return areasRepository.save(area);
    }

    public List<Area> findAll() {
        return areasRepository.findAll();
    }

    public Area findById(Long id) {
        Optional<Area> area = areasRepository.findById(id);
        return area.orElse(null);
    }

    public void deleteById(Long id) {
        areasRepository.deleteById(id);
    }
}
