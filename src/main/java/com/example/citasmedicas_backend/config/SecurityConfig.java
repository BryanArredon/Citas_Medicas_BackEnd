package com.example.citasmedicas_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Desactiva CSRF para APIs REST
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers("/api/**").permitAll() // Permite acceso sin autenticación
                        .requestMatchers("/api/**").permitAll()
                        // Agrega aquí otros endpoints públicos
                        .anyRequest().permitAll() // ← OJO: esto desactiva la seguridad para TODO
                );
        return http.build();
    }
}