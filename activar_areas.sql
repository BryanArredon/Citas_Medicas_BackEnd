-- Script para activar todas las áreas en la base de datos
-- Ejecutar este script en MySQL/MariaDB

UPDATE areas SET estatus = true WHERE estatus = false;

-- Verificar que se actualizaron
SELECT id, nombre_area, descripcion, estatus FROM areas;
