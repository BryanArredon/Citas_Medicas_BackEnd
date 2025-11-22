# Configuración Segura de OpenAI

## ⚠️ Importante
El archivo `application.properties` ahora usa **variables de entorno** para proteger las credenciales.

## Configuración Local

1. Copia el archivo de ejemplo:
```bash
cp .env.example .env
```

2. Edita `.env` con tus credenciales reales:
```bash
OPENAI_API_KEY=tu-api-key-real
OPENAI_ASSISTANT_ID=tu-assistant-id-real
```

3. **NUNCA** hagas commit del archivo `.env` (ya está en `.gitignore`)

## Ejecutar el Backend

### Opción 1: Con script (recomendado)
```bash
./run.sh
```

### Opción 2: Configuración manual en IDE

**IntelliJ IDEA / Eclipse:**
1. Ve a Run > Edit Configurations
2. Agrega las variables de entorno:
   - `OPENAI_API_KEY=tu-api-key`
   - `OPENAI_ASSISTANT_ID=tu-assistant-id`

**VS Code (launch.json):**
```json
{
  "configurations": [
    {
      "env": {
        "OPENAI_API_KEY": "tu-api-key",
        "OPENAI_ASSISTANT_ID": "tu-assistant-id"
      }
    }
  ]
}
```

### Opción 3: Maven directo
```bash
export OPENAI_API_KEY=tu-api-key
export OPENAI_ASSISTANT_ID=tu-assistant-id
./mvnw spring-boot:run
```

## Verificar Configuración

El endpoint `/api/ia/config` ahora devuelve las credenciales cargadas desde las variables de entorno.
