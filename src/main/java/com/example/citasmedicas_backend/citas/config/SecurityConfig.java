package com.example.citasmedicas_backend.citas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(CorsConfigurationSource corsConfigurationSource) {
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource)) // Habilita CORS
            .csrf(csrf -> csrf.disable()) // Desactiva CSRF para APIs REST
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/auth/**").permitAll() // Permite acceso sin autenticación al login
                .requestMatchers("/api/usuarios/**").permitAll() // Permite acceso a usuarios
                .requestMatchers("/api/**").permitAll() // El resto de APIs requieren autenticación
                .anyRequest().permitAll() // Otras rutas públicas
            );
        return http.build();
    }
}