# Integración Microservicio Métodos de Pagos

## Descripción
Se ha implementado la integración para consumir el microservicio de métodos de pagos que está ejecutándose en el puerto 8050.

## Componentes Implementados

### 1. DTOs (Data Transfer Objects)
- `EstadoPagoDto`: Representa los estados de pago
- `TipoMetodoDto`: Representa los tipos de método de pago
- `MetodoPagoCompletoDto`: Información completa de métodos de pago
- `TarjetaCompletoDto`: Información completa de tarjetas
- `PagoCompletoDto`: Información completa de pagos

### 2. Servicio de Integración
- `MetodosPagosService`: Servicio principal para consumir el microservicio
  - Configuración automática con WebClient
  - Manejo de timeouts y errores
  - Logging detallado de operaciones

### 3. Controlador de Pruebas
- `MetodosPagosTestController`: Endpoints de prueba para verificar la integración

## Configuración

### application.properties
```properties
# Métodos de Pagos Microservice Configuration
metodos-pagos.base-url=http://localhost:8050
metodos-pagos.timeout-ms=5000
```

## Endpoints de Prueba

### Verificar Conexión
```
GET /api/test/metodos-pagos/health
```
Verifica si el microservicio está disponible y funcionando.

### Obtener Métodos de Pago
```
GET /api/test/metodos-pagos/metodos-pago
GET /api/test/metodos-pagos/metodos-pago/{id}
```

### Obtener Tarjetas
```
GET /api/test/metodos-pagos/tarjetas
```

### Obtener Pagos
```
GET /api/test/metodos-pagos/pagos
```

### Obtener Estados de Pago
```
GET /api/test/metodos-pagos/estados-pago
```

### Obtener Tipos de Método
```
GET /api/test/metodos-pagos/tipos-metodo
```

### Prueba Completa
```
GET /api/test/metodos-pagos/test-completo
```
Ejecuta todas las pruebas de conectividad y obtención de datos.

## Instrucciones para Probar

### 1. Asegurar que el microservicio esté ejecutándose
```bash
# Verificar que el microservicio esté corriendo en puerto 8050
curl http://localhost:8050/actuator/health
```

### 2. Iniciar el sistema de citas médicas
```bash
cd "/Users/juanfranciscomanzanogarcia/Documents/BryanEmilio/UTNG/CITAS MEDICAS/Citas_Medicas_BackEnd"
mvn spring-boot:run
```

### 3. Probar la conectividad
```bash
# Verificar conexión
curl http://localhost:8080/api/test/metodos-pagos/health

# Obtener métodos de pago
curl http://localhost:8080/api/test/metodos-pagos/metodos-pago

# Prueba completa
curl http://localhost:8080/api/test/metodos-pagos/test-completo
```

### 4. Usando un navegador web
Visita estos URLs en tu navegador:
- http://localhost:8080/api/test/metodos-pagos/health
- http://localhost:8080/api/test/metodos-pagos/metodos-pago
- http://localhost:8080/api/test/metodos-pagos/test-completo

## Formato de Respuesta

Todas las respuestas siguen este formato estándar:
```json
{
  "status": "SUCCESS|ERROR|NOT_FOUND",
  "message": "Descripción del resultado",
  "data": [], // Datos obtenidos del microservicio
  "count": 0, // Número de elementos (cuando aplique)
  "timestamp": "2025-10-23T22:35:00"
}
```

## Logging

El sistema incluye logging detallado que muestra:
- Llamadas al microservicio
- Respuestas recibidas
- Errores de conectividad
- Tiempos de respuesta

## Manejo de Errores

- **Timeout**: 5 segundos por defecto
- **Microservicio no disponible**: Respuesta de error con código 503
- **Errores de red**: Logging detallado y respuesta de error con código 500
- **Datos no encontrados**: Respuesta con status NOT_FOUND

## Próximos Pasos

1. **Integración con Entidades**: Mapear los DTOs con las entidades del sistema de citas
2. **Procesamiento de Pagos**: Implementar la lógica de pagos para las citas médicas
3. **Seguridad**: Añadir autenticación y autorización para las llamadas al microservicio
4. **Cache**: Implementar cache para mejorar el rendimiento
5. **Circuit Breaker**: Añadir patrón circuit breaker para mayor resiliencia