package com.example.citasmedicas_backend.assistant.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "assistant")
@Data
public class AssistantProperties {
    
    private String name;
    private String instructions;
    
    // Configuración de límites
    private Integer maxTokens = 1000;
    private Double temperature = 0.7;
    private Integer maxConversationHistory = 20;
}
