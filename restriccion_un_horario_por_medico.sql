-- ============================================
-- SCRIPT: Restricción UN HORARIO POR MÉDICO
-- ============================================
-- Este script asegura que cada médico solo pueda tener
-- un registro de horario en la tabla horarioMedico
--
-- Fecha: 27 de noviembre de 2025
-- ============================================

USE citas_medicas;

-- Paso 1: Verificar si hay médicos con múltiples horarios
-- (Ejecutar esta consulta antes de aplicar la restricción)
SELECT 
    m.id AS medico_id,
    CONCAT(u.nombre, ' ', u.apellido_paterno) AS medico_nombre,
    COUNT(h.id) AS total_horarios,
    GROUP_CONCAT(h.id ORDER BY h.id) AS horarios_ids
FROM medico m
INNER JOIN usuario u ON m.id_usuario = u.id
LEFT JOIN horarioMedico h ON h.idMedico = m.id
GROUP BY m.id, u.nombre, u.apellido_paterno
HAVING COUNT(h.id) > 1;

-- Si la consulta anterior devuelve resultados, hay médicos con múltiples horarios
-- Debes decidir cuál horario mantener y eliminar los demás manualmente antes de continuar

-- Paso 2 (OPCIONAL): Si necesitas eliminar horarios duplicados, 
-- descomenta y ejecuta esto para mantener solo el más reciente de cada médico:
/*
DELETE h1 FROM horarioMedico h1
INNER JOIN (
    SELECT idMedico, MAX(id) AS max_id
    FROM horarioMedico
    GROUP BY idMedico
    HAVING COUNT(*) > 1
) h2 ON h1.idMedico = h2.idMedico
WHERE h1.id < h2.max_id;
*/

-- Paso 3: Agregar restricción UNIQUE para garantizar un solo horario por médico
-- Esta restricción previene la creación de nuevos horarios duplicados
ALTER TABLE horarioMedico
ADD CONSTRAINT uk_horario_medico_unique 
UNIQUE (idMedico);

-- Paso 4: Verificar que la restricción se aplicó correctamente
SHOW INDEX FROM horarioMedico WHERE Key_name = 'uk_horario_medico_unique';

-- ============================================
-- NOTAS IMPORTANTES:
-- ============================================
-- 1. Esta restricción permite solo UN horario por médico
-- 2. Si intentas crear un segundo horario para el mismo médico,
--    la base de datos rechazará la operación con error de duplicado
-- 3. Para actualizar el horario de un médico, usa UPDATE en lugar de INSERT
-- 4. Si necesitas eliminar la restricción en el futuro:
--    ALTER TABLE horarioMedico DROP INDEX uk_horario_medico_unique;
-- ============================================

-- Consulta útil: Ver todos los horarios actuales
SELECT 
    h.id AS horario_id,
    h.idMedico,
    CONCAT(u.nombre, ' ', u.apellido_paterno) AS medico,
    h.`fecha-horario` AS fecha,
    h.`horario-inicio` AS hora_inicio,
    h.`horario-fin` AS hora_fin,
    h.duracion,
    h.`Estado medico` AS estado
FROM horarioMedico h
INNER JOIN medico m ON h.idMedico = m.id
INNER JOIN usuario u ON m.id_usuario = u.id
ORDER BY h.idMedico, h.`fecha-horario`;
