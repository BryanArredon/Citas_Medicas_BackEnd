package com.example.citasmedicas_backend.assistant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {
    
    private String id;
    private String role; // "user", "assistant"
    private String content;
    private LocalDateTime timestamp;
    private String threadId;
}
