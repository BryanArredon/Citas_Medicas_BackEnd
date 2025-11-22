package com.example.citasmedicas_backend.assistant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    
    @NotBlank(message = "El mensaje no puede estar vacío")
    private String message;
    
    @NotNull(message = "El ID del usuario es requerido")
    private Long userId;
    
    private String threadId; // null para nueva conversación
    
    // Metadatos adicionales
    private String context; // "agendar_cita", "consultar_info", etc.
}
