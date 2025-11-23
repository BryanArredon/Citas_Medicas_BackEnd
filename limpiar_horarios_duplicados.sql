-- Script para eliminar horarios duplicados
-- Mantiene solo el registro con el ID más bajo para cada combinación única

USE DBCitasMedicas;

-- Crear tabla temporal con los IDs a mantener (el más antiguo de cada grupo duplicado)
CREATE TEMPORARY TABLE horarios_a_mantener AS
SELECT MIN(id) as id_mantener
FROM horario_medico
GROUP BY id_medico, `fecha-horario`, `horario-inicio`, `horario-fin`;

-- Verificar cuántos registros se van a eliminar
SELECT 'REGISTROS A ELIMINAR:' as info;
SELECT COUNT(*) as total_duplicados_a_eliminar
FROM horario_medico
WHERE id NOT IN (SELECT id_mantener FROM horarios_a_mantener);

-- Eliminar los duplicados (registros que NO están en la lista de IDs a mantener)
DELETE FROM horario_medico
WHERE id NOT IN (SELECT id_mantener FROM horarios_a_mantener);

-- Verificar el resultado
SELECT 'REGISTROS RESTANTES:' as info;
SELECT COUNT(*) as total_horarios_restantes FROM horario_medico;

-- Limpiar tabla temporal
DROP TEMPORARY TABLE horarios_a_mantener;

SELECT 'LIMPIEZA COMPLETADA' as resultado;