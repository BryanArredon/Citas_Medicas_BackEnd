package com.example.citasmedicas_backend.assistant.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para proporcionar la configuración de OpenAI al frontend
 */
@RestController
@RequestMapping("/api/ia/config")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class IAConfigController {

    private static final Logger log = LoggerFactory.getLogger(IAConfigController.class);

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.assistant-id}")
    private String assistantId;

    /**
     * Endpoint que retorna las credenciales de OpenAI configuradas en el backend
     */
    @GetMapping
    public ResponseEntity<Map<String, String>> obtenerConfiguracion() {
        log.info("API: Solicitando configuración de OpenAI");
        
        Map<String, String> config = new HashMap<>();
        config.put("apiKey", apiKey);
        config.put("assistantId", assistantId);
        
        log.info("Configuración proporcionada (API Key: {}...)", 
                 apiKey != null && apiKey.length() > 10 ? apiKey.substring(0, 10) : "N/A");
        
        return ResponseEntity.ok(config);
    }
}
