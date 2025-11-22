package com.example.citasmedicas_backend.assistant.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "openai")
@Data
public class OpenAIConfig {
    
    private String apiKey;
    private String assistantId;
    private String model;
    private String organizationId;
}
