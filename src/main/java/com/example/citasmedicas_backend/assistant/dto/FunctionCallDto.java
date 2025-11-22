package com.example.citasmedicas_backend.assistant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FunctionCallDto {
    
    private String functionName;
    private Map<String, Object> arguments;
    private Object result;
    private Boolean success;
    private String errorMessage;
}
