#!/bin/bash
# Script para ejecutar el backend con variables de entorno

# Cargar variables desde .env
export $(cat .env | xargs)

# Ejecutar Maven
./mvnw spring-boot:run
