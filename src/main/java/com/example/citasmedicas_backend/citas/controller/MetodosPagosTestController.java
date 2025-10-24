package com.example.citasmedicas_backend.citas.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.citasmedicas_backend.citas.dto.pagos.EstadoPagoDto;
import com.example.citasmedicas_backend.citas.dto.pagos.MetodoPagoCompletoDto;
import com.example.citasmedicas_backend.citas.dto.pagos.PagoCompletoDto;
import com.example.citasmedicas_backend.citas.dto.pagos.TarjetaCompletoDto;
import com.example.citasmedicas_backend.citas.dto.pagos.TipoMetodoDto;
import com.example.citasmedicas_backend.citas.service.MetodosPagosService;

@RestController
@RequestMapping("/api/test/metodos-pagos")
public class MetodosPagosTestController {

    private static final Logger log = LoggerFactory.getLogger(MetodosPagosTestController.class);

    @Autowired
    private MetodosPagosService metodosPagosService;

    /**
     * Endpoint para verificar la conexión con el microservicio
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> verificarConexion() {
        log.info("=== VERIFICANDO CONEXIÓN CON MICROSERVICIO DE MÉTODOS DE PAGOS ===");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean disponible = metodosPagosService.verificarConexion();
            
            response.put("microservicio", "Métodos de Pagos");
            response.put("url", "http://localhost:8050");
            response.put("disponible", disponible);
            response.put("timestamp", java.time.LocalDateTime.now());
            
            if (disponible) {
                response.put("status", "SUCCESS");
                response.put("message", "Microservicio disponible y funcionando correctamente");
                log.info("✅ Conexión exitosa con microservicio de métodos de pagos");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "ERROR");
                response.put("message", "Microservicio no disponible");
                log.error("❌ No se pudo conectar con microservicio de métodos de pagos");
                return ResponseEntity.status(503).body(response);
            }
            
        } catch (Exception ex) {
            response.put("status", "ERROR");
            response.put("message", "Error verificando conexión: " + ex.getMessage());
            response.put("disponible", false);
            log.error("❌ Error verificando conexión: {}", ex.toString());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Endpoint para obtener todos los métodos de pago
     */
    @GetMapping("/metodos-pago")
    public ResponseEntity<Map<String, Object>> obtenerMetodosPago() {
        log.info("=== OBTENIENDO MÉTODOS DE PAGO DEL MICROSERVICIO ===");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<MetodoPagoCompletoDto> metodos = metodosPagosService.obtenerMetodosPago();
            
            response.put("status", "SUCCESS");
            response.put("message", "Métodos de pago obtenidos correctamente");
            response.put("count", metodos.size());
            response.put("data", metodos);
            response.put("timestamp", java.time.LocalDateTime.now());
            
            log.info("✅ Obtenidos {} métodos de pago", metodos.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception ex) {
            response.put("status", "ERROR");
            response.put("message", "Error obteniendo métodos de pago: " + ex.getMessage());
            response.put("count", 0);
            response.put("data", List.of());
            log.error("❌ Error obteniendo métodos de pago: {}", ex.toString());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Endpoint para obtener un método de pago por ID
     */
    @GetMapping("/metodos-pago/{id}")
    public ResponseEntity<Map<String, Object>> obtenerMetodoPagoPorId(@PathVariable Long id) {
        log.info("=== OBTENIENDO MÉTODO DE PAGO POR ID: {} ===", id);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            MetodoPagoCompletoDto metodo = metodosPagosService.obtenerMetodoPagoPorId(id);
            
            if (metodo != null) {
                response.put("status", "SUCCESS");
                response.put("message", "Método de pago encontrado");
                response.put("data", metodo);
                log.info("✅ Método de pago encontrado: {}", metodo.getNombreMetodo());
            } else {
                response.put("status", "NOT_FOUND");
                response.put("message", "Método de pago no encontrado");
                response.put("data", null);
                log.warn("⚠️ Método de pago no encontrado para ID: {}", id);
            }
            
            response.put("timestamp", java.time.LocalDateTime.now());
            return ResponseEntity.ok(response);
            
        } catch (Exception ex) {
            response.put("status", "ERROR");
            response.put("message", "Error obteniendo método de pago: " + ex.getMessage());
            response.put("data", null);
            log.error("❌ Error obteniendo método de pago por ID {}: {}", id, ex.toString());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Endpoint para obtener todas las tarjetas
     */
    @GetMapping("/tarjetas")
    public ResponseEntity<Map<String, Object>> obtenerTarjetas() {
        log.info("=== OBTENIENDO TARJETAS DEL MICROSERVICIO ===");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<TarjetaCompletoDto> tarjetas = metodosPagosService.obtenerTarjetas();
            
            response.put("status", "SUCCESS");
            response.put("message", "Tarjetas obtenidas correctamente");
            response.put("count", tarjetas.size());
            response.put("data", tarjetas);
            response.put("timestamp", java.time.LocalDateTime.now());
            
            log.info("✅ Obtenidas {} tarjetas", tarjetas.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception ex) {
            response.put("status", "ERROR");
            response.put("message", "Error obteniendo tarjetas: " + ex.getMessage());
            response.put("count", 0);
            response.put("data", List.of());
            log.error("❌ Error obteniendo tarjetas: {}", ex.toString());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Endpoint para obtener todos los pagos
     */
    @GetMapping("/pagos")
    public ResponseEntity<Map<String, Object>> obtenerPagos() {
        log.info("=== OBTENIENDO PAGOS DEL MICROSERVICIO ===");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<PagoCompletoDto> pagos = metodosPagosService.obtenerPagos();
            
            response.put("status", "SUCCESS");
            response.put("message", "Pagos obtenidos correctamente");
            response.put("count", pagos.size());
            response.put("data", pagos);
            response.put("timestamp", java.time.LocalDateTime.now());
            
            log.info("✅ Obtenidos {} pagos", pagos.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception ex) {
            response.put("status", "ERROR");
            response.put("message", "Error obteniendo pagos: " + ex.getMessage());
            response.put("count", 0);
            response.put("data", List.of());
            log.error("❌ Error obteniendo pagos: {}", ex.toString());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Endpoint para obtener todos los estados de pago
     */
    @GetMapping("/estados-pago")
    public ResponseEntity<Map<String, Object>> obtenerEstadosPago() {
        log.info("=== OBTENIENDO ESTADOS DE PAGO DEL MICROSERVICIO ===");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<EstadoPagoDto> estados = metodosPagosService.obtenerEstadosPago();
            
            response.put("status", "SUCCESS");
            response.put("message", "Estados de pago obtenidos correctamente");
            response.put("count", estados.size());
            response.put("data", estados);
            response.put("timestamp", java.time.LocalDateTime.now());
            
            log.info("✅ Obtenidos {} estados de pago", estados.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception ex) {
            response.put("status", "ERROR");
            response.put("message", "Error obteniendo estados de pago: " + ex.getMessage());
            response.put("count", 0);
            response.put("data", List.of());
            log.error("❌ Error obteniendo estados de pago: {}", ex.toString());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Endpoint para obtener todos los tipos de método
     */
    @GetMapping("/tipos-metodo")
    public ResponseEntity<Map<String, Object>> obtenerTiposMetodo() {
        log.info("=== OBTENIENDO TIPOS DE MÉTODO DEL MICROSERVICIO ===");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<TipoMetodoDto> tipos = metodosPagosService.obtenerTiposMetodo();
            
            response.put("status", "SUCCESS");
            response.put("message", "Tipos de método obtenidos correctamente");
            response.put("count", tipos.size());
            response.put("data", tipos);
            response.put("timestamp", java.time.LocalDateTime.now());
            
            log.info("✅ Obtenidos {} tipos de método", tipos.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception ex) {
            response.put("status", "ERROR");
            response.put("message", "Error obteniendo tipos de método: " + ex.getMessage());
            response.put("count", 0);
            response.put("data", List.of());
            log.error("❌ Error obteniendo tipos de método: {}", ex.toString());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Endpoint para probar todas las funcionalidades del microservicio
     */
    @GetMapping("/test-completo")
    public ResponseEntity<Map<String, Object>> pruebaCompleta() {
        log.info("=== INICIANDO PRUEBA COMPLETA DEL MICROSERVICIO ===");
        
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> resultados = new HashMap<>();
        
        try {
            // 1. Verificar conexión
            boolean conexion = metodosPagosService.verificarConexion();
            resultados.put("conexion", conexion);
            
            if (conexion) {
                // 2. Obtener métodos de pago
                List<MetodoPagoCompletoDto> metodos = metodosPagosService.obtenerMetodosPago();
                resultados.put("metodos_pago", Map.of("count", metodos.size(), "data", metodos));
                
                // 3. Obtener tarjetas
                List<TarjetaCompletoDto> tarjetas = metodosPagosService.obtenerTarjetas();
                resultados.put("tarjetas", Map.of("count", tarjetas.size(), "data", tarjetas));
                
                // 4. Obtener pagos
                List<PagoCompletoDto> pagos = metodosPagosService.obtenerPagos();
                resultados.put("pagos", Map.of("count", pagos.size(), "data", pagos));
                
                // 5. Obtener estados de pago
                List<EstadoPagoDto> estados = metodosPagosService.obtenerEstadosPago();
                resultados.put("estados_pago", Map.of("count", estados.size(), "data", estados));
                
                // 6. Obtener tipos de método
                List<TipoMetodoDto> tipos = metodosPagosService.obtenerTiposMetodo();
                resultados.put("tipos_metodo", Map.of("count", tipos.size(), "data", tipos));
            }
            
            response.put("status", "SUCCESS");
            response.put("message", "Prueba completa ejecutada correctamente");
            response.put("resultados", resultados);
            response.put("timestamp", java.time.LocalDateTime.now());
            
            log.info("✅ Prueba completa ejecutada correctamente");
            return ResponseEntity.ok(response);
            
        } catch (Exception ex) {
            response.put("status", "ERROR");
            response.put("message", "Error en prueba completa: " + ex.getMessage());
            response.put("resultados", resultados);
            log.error("❌ Error en prueba completa: {}", ex.toString());
            return ResponseEntity.status(500).body(response);
        }
    }
}