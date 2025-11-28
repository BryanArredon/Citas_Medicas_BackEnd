-- ============================================
-- SCRIPT: Limpiar registros incorrectos de tabla AGENDA
-- ============================================
-- Este script elimina los registros que se crearon por error
-- en la tabla 'agenda' cuando se guardaban horarios de disponibilidad
--
-- PROBLEMA: El código anterior registraba automáticamente en 'agenda'
-- cada vez que se creaba un horario en 'horario_medico', lo cual es incorrecto.
--
-- SOLUCIÓN: La tabla 'agenda' debe tener solo CITAS AGENDADAS por pacientes,
-- no la disponibilidad del médico.
--
-- Fecha: 27 de noviembre de 2025
-- ============================================

USE citas_medicas;

-- Paso 1: Ver registros actuales en agenda
SELECT 
    'ANTES DE LIMPIAR' AS estado,
    COUNT(*) AS total_registros_agenda
FROM agenda;

SELECT 
    a.id_agenda,
    a.fecha,
    a.hora_inicio,
    a.hora_fin,
    m.id AS medico_id,
    CONCAT(u.nombre, ' ', u.apellido_paterno) AS medico_nombre
FROM agenda a
INNER JOIN medico m ON a.id_medico_detalle = m.id
INNER JOIN usuario u ON m.id_usuario = u.id
ORDER BY a.fecha, a.hora_inicio;

-- Paso 2: Ver horarios configurados (correctos)
SELECT 
    'HORARIOS CONFIGURADOS (horario_medico)' AS tabla,
    COUNT(*) AS total_horarios
FROM horarioMedico;

SELECT 
    h.id,
    h.`fecha-horario`,
    h.`horario-inicio`,
    h.`horario-fin`,
    CONCAT(u.nombre, ' ', u.apellido_paterno) AS medico
FROM horarioMedico h
INNER JOIN medico m ON h.idMedico = m.id
INNER JOIN usuario u ON m.id_usuario = u.id;

-- Paso 3: LIMPIAR TABLA AGENDA
-- ⚠️ ADVERTENCIA: Esto eliminará TODOS los registros de agenda
-- Solo ejecuta esto si NO tienes citas reales agendadas
-- o si quieres empezar de cero

-- OPCIÓN A: Eliminar TODOS los registros (usar con cuidado)
TRUNCATE TABLE agenda;

-- OPCIÓN B: Eliminar solo registros que no tienen paciente asociado
-- (si tu tabla agenda tiene un campo id_paciente)
-- DELETE FROM agenda WHERE id_paciente IS NULL;

-- Paso 4: Verificar que la tabla quedó limpia
SELECT 
    'DESPUÉS DE LIMPIAR' AS estado,
    COUNT(*) AS total_registros_agenda
FROM agenda;

-- Paso 5: Verificar que los horarios siguen en horario_medico
SELECT 
    'HORARIOS EN horario_medico' AS estado,
    COUNT(*) AS total_horarios
FROM horarioMedico;

-- ============================================
-- RESULTADO ESPERADO:
-- ============================================
-- - Tabla 'agenda' vacía (0 registros) o solo con citas reales
-- - Tabla 'horario_medico' con los horarios de disponibilidad intactos
--
-- COMPORTAMIENTO CORRECTO A PARTIR DE AHORA:
-- 1. Médico configura horario → Se guarda en 'horario_medico'
-- 2. Paciente agenda cita → Se crea registro en 'agenda'
-- 3. Chatbot consulta 'horario_medico' y descarta slots en 'agenda'
-- ============================================
