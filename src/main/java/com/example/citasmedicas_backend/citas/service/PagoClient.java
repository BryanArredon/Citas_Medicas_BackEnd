package com.example.citasmedicas_backend.citas.service;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.citasmedicas_backend.citas.dto.PagoRequestDto;
import com.example.citasmedicas_backend.citas.dto.SimulacionResponseDto;

import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Component
public class PagoClient {

    private static final Logger log = LoggerFactory.getLogger(PagoClient.class);

    private final WebClient webClient;
    private final Duration timeout;

    public PagoClient(@Value("${pagos.base-url}") String baseUrl,
                      @Value("${pagos.simular-timeout-ms:5000}") long timeoutMs) {
        // enable Reactor Netty wiretap so HTTP request/response headers are logged (helpful for debugging)
        HttpClient httpClient = HttpClient.create().wiretap(true);
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
        this.timeout = Duration.ofMillis(timeoutMs);
    }

    public SimulacionResponseDto simularPago(PagoRequestDto request) {
        try {
            log.info("Simulando pago contra microservicio pagos: monto={}, metodo={}",
                    request.getMonto(), request.getMetodoPago() != null ? request.getMetodoPago().getTipo() : "<n/a>");

            Mono<SimulacionResponseDto> mono = webClient.post()
                    .uri("/simular")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(SimulacionResponseDto.class)
                    .timeout(timeout);

            SimulacionResponseDto resp = mono.block();
            if (resp != null) {
                log.info("Respuesta de pagos: estado={}, message={}", resp.getEstado(), resp.getMessage());
            } else {
                log.warn("Respuesta de pagos fue nula");
            }
            return resp;
        } catch (Exception ex) {
            log.error("Error llamando al microservicio de pagos: {}", ex.toString());
            return null;
        }
    }
}
