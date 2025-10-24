package com.example.citasmedicas_backend.citas.service;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.citasmedicas_backend.citas.dto.pagos.EstadoPagoDto;
import com.example.citasmedicas_backend.citas.dto.pagos.MetodoPagoCompletoDto;
import com.example.citasmedicas_backend.citas.dto.pagos.PagoCompletoDto;
import com.example.citasmedicas_backend.citas.dto.pagos.TarjetaCompletoDto;
import com.example.citasmedicas_backend.citas.dto.pagos.TipoMetodoDto;

import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Service
public class MetodosPagosService {

    private static final Logger log = LoggerFactory.getLogger(MetodosPagosService.class);

    private final WebClient webClient;
    private final Duration timeout;

    public MetodosPagosService(@Value("${metodos-pagos.base-url:http://localhost:8050}") String baseUrl,
                              @Value("${metodos-pagos.timeout-ms:5000}") long timeoutMs) {
        
        HttpClient httpClient = HttpClient.create().wiretap(true);
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
        this.timeout = Duration.ofMillis(timeoutMs);
        
        log.info("MetodosPagosService inicializado con URL: {} y timeout: {}ms", baseUrl, timeoutMs);
    }

    // ===============================
    // MÉTODOS DE PAGO
    // ===============================

    /**
     * Obtiene todos los métodos de pago disponibles
     */
    public List<MetodoPagoCompletoDto> obtenerMetodosPago() {
        try {
            log.info("Obteniendo todos los métodos de pago del microservicio");
            
            Mono<List<MetodoPagoCompletoDto>> mono = webClient.get()
                    .uri("/api/metodos-pago")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<MetodoPagoCompletoDto>>() {})
                    .timeout(timeout);

            List<MetodoPagoCompletoDto> metodos = mono.block();
            log.info("Obtenidos {} métodos de pago del microservicio", metodos != null ? metodos.size() : 0);
            return metodos;
            
        } catch (Exception ex) {
            log.error("Error obteniendo métodos de pago del microservicio: {}", ex.toString());
            return List.of();
        }
    }

    /**
     * Obtiene un método de pago por ID
     */
    public MetodoPagoCompletoDto obtenerMetodoPagoPorId(Long id) {
        try {
            log.info("Obteniendo método de pago por ID: {}", id);
            
            Mono<MetodoPagoCompletoDto> mono = webClient.get()
                    .uri("/api/metodos-pago/{id}", id)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(MetodoPagoCompletoDto.class)
                    .timeout(timeout);

            MetodoPagoCompletoDto metodo = mono.block();
            log.info("Método de pago obtenido: {}", metodo != null ? metodo.getNombreMetodo() : "no encontrado");
            return metodo;
            
        } catch (Exception ex) {
            log.error("Error obteniendo método de pago por ID {}: {}", id, ex.toString());
            return null;
        }
    }

    // ===============================
    // TARJETAS
    // ===============================

    /**
     * Obtiene todas las tarjetas disponibles
     */
    public List<TarjetaCompletoDto> obtenerTarjetas() {
        try {
            log.info("Obteniendo todas las tarjetas del microservicio");
            
            Mono<List<TarjetaCompletoDto>> mono = webClient.get()
                    .uri("/api/tarjetas")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<TarjetaCompletoDto>>() {})
                    .timeout(timeout);

            List<TarjetaCompletoDto> tarjetas = mono.block();
            log.info("Obtenidas {} tarjetas del microservicio", tarjetas != null ? tarjetas.size() : 0);
            return tarjetas;
            
        } catch (Exception ex) {
            log.error("Error obteniendo tarjetas del microservicio: {}", ex.toString());
            return List.of();
        }
    }

    /**
     * Obtiene una tarjeta por ID
     */
    public TarjetaCompletoDto obtenerTarjetaPorId(Long id) {
        try {
            log.info("Obteniendo tarjeta por ID: {}", id);
            
            Mono<TarjetaCompletoDto> mono = webClient.get()
                    .uri("/api/tarjetas/{id}", id)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(TarjetaCompletoDto.class)
                    .timeout(timeout);

            TarjetaCompletoDto tarjeta = mono.block();
            log.info("Tarjeta obtenida: {}", tarjeta != null ? tarjeta.getNombreTitular() : "no encontrada");
            return tarjeta;
            
        } catch (Exception ex) {
            log.error("Error obteniendo tarjeta por ID {}: {}", id, ex.toString());
            return null;
        }
    }

    // ===============================
    // PAGOS
    // ===============================

    /**
     * Obtiene todos los pagos
     */
    public List<PagoCompletoDto> obtenerPagos() {
        try {
            log.info("Obteniendo todos los pagos del microservicio");
            
            Mono<List<PagoCompletoDto>> mono = webClient.get()
                    .uri("/api/pagos")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<PagoCompletoDto>>() {})
                    .timeout(timeout);

            List<PagoCompletoDto> pagos = mono.block();
            log.info("Obtenidos {} pagos del microservicio", pagos != null ? pagos.size() : 0);
            return pagos;
            
        } catch (Exception ex) {
            log.error("Error obteniendo pagos del microservicio: {}", ex.toString());
            return List.of();
        }
    }

    /**
     * Obtiene un pago por ID
     */
    public PagoCompletoDto obtenerPagoPorId(Long id) {
        try {
            log.info("Obteniendo pago por ID: {}", id);
            
            Mono<PagoCompletoDto> mono = webClient.get()
                    .uri("/api/pagos/{id}", id)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(PagoCompletoDto.class)
                    .timeout(timeout);

            PagoCompletoDto pago = mono.block();
            log.info("Pago obtenido: {}", pago != null ? pago.getReferencia() : "no encontrado");
            return pago;
            
        } catch (Exception ex) {
            log.error("Error obteniendo pago por ID {}: {}", id, ex.toString());
            return null;
        }
    }

    // ===============================
    // ESTADOS DE PAGO
    // ===============================

    /**
     * Obtiene todos los estados de pago disponibles
     */
    public List<EstadoPagoDto> obtenerEstadosPago() {
        try {
            log.info("Obteniendo todos los estados de pago del microservicio");
            
            Mono<List<EstadoPagoDto>> mono = webClient.get()
                    .uri("/api/estados-pago")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<EstadoPagoDto>>() {})
                    .timeout(timeout);

            List<EstadoPagoDto> estados = mono.block();
            log.info("Obtenidos {} estados de pago del microservicio", estados != null ? estados.size() : 0);
            return estados;
            
        } catch (Exception ex) {
            log.error("Error obteniendo estados de pago del microservicio: {}", ex.toString());
            return List.of();
        }
    }

    // ===============================
    // TIPOS DE MÉTODO
    // ===============================

    /**
     * Obtiene todos los tipos de método disponibles
     */
    public List<TipoMetodoDto> obtenerTiposMetodo() {
        try {
            log.info("Obteniendo todos los tipos de método del microservicio");
            
            Mono<List<TipoMetodoDto>> mono = webClient.get()
                    .uri("/api/tipos-metodo")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<TipoMetodoDto>>() {})
                    .timeout(timeout);

            List<TipoMetodoDto> tipos = mono.block();
            log.info("Obtenidos {} tipos de método del microservicio", tipos != null ? tipos.size() : 0);
            return tipos;
            
        } catch (Exception ex) {
            log.error("Error obteniendo tipos de método del microservicio: {}", ex.toString());
            return List.of();
        }
    }

    // ===============================
    // MÉTODO DE VERIFICACIÓN
    // ===============================

    /**
     * Verifica si el microservicio de métodos de pagos está disponible
     */
    public boolean verificarConexion() {
        try {
            log.info("Verificando conexión con el microservicio de métodos de pagos");
            
            Mono<String> mono = webClient.get()
                    .uri("/actuator/health")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(3));

            String resultado = mono.block();
            boolean disponible = resultado != null && resultado.contains("UP");
            log.info("Microservicio de métodos de pagos disponible: {}", disponible);
            return disponible;
            
        } catch (Exception ex) {
            log.warn("Microservicio de métodos de pagos no disponible: {}", ex.toString());
            return false;
        }
    }
}