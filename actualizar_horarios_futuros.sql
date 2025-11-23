-- Script para actualizar fechas de horarios médicos a fechas futuras
-- Ejecutar en MySQL para que la IA muestre horarios disponibles

USE DBCitasMedicas;

-- Verificar fechas actuales
SELECT 'FECHAS ACTUALES:' as info;
SELECT id, `fecha-horario`, `horario-inicio`, `horario-fin`, `estado medico`
FROM horario_medico
WHERE `estado medico` = 'DISPONIBLE'
ORDER BY `fecha-horario`, `horario-inicio`
LIMIT 10;

-- Actualizar fechas pasadas a fechas futuras (próximos 7 días)
UPDATE horario_medico
SET `fecha-horario` = CASE
    WHEN `fecha-horario` < CURDATE() THEN DATE_ADD(CURDATE(), INTERVAL (DATEDIFF(`fecha-horario`, CURDATE()) % 7 + 1) DAY)
    WHEN `fecha-horario` = CURDATE() THEN DATE_ADD(CURDATE(), INTERVAL 1 DAY)
    ELSE `fecha-horario`
END
WHERE `estado medico` = 'DISPONIBLE';

-- ⚠️  IMPORTANTE: Este script crea horarios adicionales automáticamente
-- ⚠️  Ejecutar solo UNA vez para evitar duplicados
-- Si se ejecuta múltiples veces, usar limpiar_horarios_duplicados.sql

-- Crear horarios adicionales para los próximos 7 días si no hay suficientes
-- SOLO PARA HORARIOS QUE NO EXISTAN AÚN
INSERT INTO horario_medico (id_medico, `fecha-horario`, `horario-inicio`, `horario-fin`, `estado medico`, valid_until)
SELECT DISTINCT
    h.id_medico,
    DATE_ADD(CURDATE(), INTERVAL n.day_number DAY) as fecha,
    h.`horario-inicio`,
    h.`horario-fin`,
    'DISPONIBLE' as estado_medico,
    DATE_ADD(CURDATE(), INTERVAL 30 DAY) as valid_until
FROM horario_medico h
CROSS JOIN (
    SELECT 1 as day_number UNION ALL
    SELECT 2 UNION ALL
    SELECT 3 UNION ALL
    SELECT 4 UNION ALL
    SELECT 5 UNION ALL
    SELECT 6 UNION ALL
    SELECT 7
) n
WHERE h.`estado medico` = 'DISPONIBLE'
  AND h.`fecha-horario` >= CURDATE()
  AND NOT EXISTS (
      SELECT 1 FROM horario_medico h2
      WHERE h2.id_medico = h.id_medico
        AND h2.`fecha-horario` = DATE_ADD(CURDATE(), INTERVAL n.day_number DAY)
        AND h2.`horario-inicio` = h.`horario-inicio`
        AND h2.`horario-fin` = h.`horario-fin`
  )
ORDER BY h.id_medico, n.day_number
LIMIT 100; -- Limitar para evitar demasiados inserts

-- Verificar fechas actualizadas
SELECT 'FECHAS ACTUALIZADAS:' as info;
SELECT id, `fecha-horario`, `horario-inicio`, `horario-fin`, `estado medico`
FROM horario_medico
WHERE `estado medico` = 'DISPONIBLE'
  AND `fecha-horario` >= CURDATE()
ORDER BY `fecha-horario`, `horario-inicio`
LIMIT 20;

-- Contar horarios disponibles por día
SELECT 'HORARIOS POR DÍA:' as info;
SELECT
    DATE_FORMAT(`fecha-horario`, '%Y-%m-%d') as fecha,
    COUNT(*) as horarios_disponibles
FROM horario_medico
WHERE `estado medico` = 'DISPONIBLE'
  AND `fecha-horario` >= CURDATE()
  AND `fecha-horario` <= DATE_ADD(CURDATE(), INTERVAL 7 DAY)
GROUP BY `fecha-horario`
ORDER BY `fecha-horario`;