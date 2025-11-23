#!/bin/bash
# Script para ejecutar el backend con variables de entorno

# Cargar variables desde .env (ignorando líneas comentadas)
export $(grep -v '^#' .env | xargs)

# Ejecutar Maven con las variables de entorno
env ./mvnw spring-boot:run
