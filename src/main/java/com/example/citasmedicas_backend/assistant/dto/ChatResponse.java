package com.example.citasmedicas_backend.assistant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    
    private String threadId;
    private String message;
    private String role; // "assistant", "user"
    private LocalDateTime timestamp;
    
    // Funciones ejecutadas (si las hay)
    private List<FunctionCallDto> functionsExecuted;
    
    // Datos adicionales para el frontend
    private Object data; // Puede contener citas, médicos, etc.
    private Boolean requiresAction; // true si necesita confirmación
    private String actionType; // "confirm_payment", "select_doctor", etc.
}
