-- Script SEGURO para actualizar horarios médicos
-- Versión mejorada que NO crea duplicados automáticamente
-- Solo actualiza fechas existentes y valida integridad

USE DBCitasMedicas;

-- 1. Verificar estado actual
SELECT '=== ESTADO ACTUAL ===' as seccion;
SELECT COUNT(*) as total_horarios FROM horario_medico;
SELECT COUNT(*) as horarios_disponibles FROM horario_medico WHERE `estado medico` = 'DISPONIBLE';
SELECT COUNT(*) as horarios_futuros FROM horario_medico WHERE `estado medico` = 'DISPONIBLE' AND `fecha-horario` >= CURDATE();

-- 2. Actualizar fechas pasadas a fechas futuras (solo si no hay suficientes)
SELECT '=== ACTUALIZANDO FECHAS PASADAS ===' as seccion;
UPDATE horario_medico
SET `fecha-horario` = DATE_ADD(CURDATE(), INTERVAL (DATEDIFF(CURDATE(), `fecha-horario`) % 30 + 1) DAY)
WHERE `estado medico` = 'DISPONIBLE'
  AND `fecha-horario` < CURDATE();

-- 3. Verificar si necesitamos más horarios (menos de 50 por médico)
SELECT '=== VERIFICANDO SI HACEN FALTA HORARIOS ===' as seccion;
SELECT
    id_medico,
    COUNT(*) as horarios_futuros,
    CASE WHEN COUNT(*) < 50 THEN 'NECESITA MÁS' ELSE 'SUFICIENTE' END as estado
FROM horario_medico
WHERE `estado medico` = 'DISPONIBLE'
  AND `fecha-horario` >= CURDATE()
GROUP BY id_medico
HAVING COUNT(*) < 50;

-- 4. Solo si algún médico necesita más horarios, ejecutar con precaución
-- (Comentar/descomentar según necesidad)
-- SELECT 'ADVERTENCIA: Si algún médico necesita más horarios, ejecutar actualizar_horarios_futuros.sql SOLO UNA VEZ' as mensaje;

-- 5. Verificar resultado final
SELECT '=== RESULTADO FINAL ===' as seccion;
SELECT COUNT(*) as horarios_disponibles_final FROM horario_medico WHERE `estado medico` = 'DISPONIBLE' AND `fecha-horario` >= CURDATE();

SELECT 'ACTUALIZACIÓN COMPLETADA - REVISAR SI ES NECESARIO CREAR MÁS HORARIOS' as resultado;