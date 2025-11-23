-- Script para migrar los horarios existentes de horario_medico a agenda
-- Ejecutar este script en la base de datos DBCitasMedicas

USE DBCitasMedicas;

-- Insertar los horarios existentes de horario_medico a agenda
INSERT INTO agenda (fecha, hora_inicio, hora_fin, id_medico_detalle)
SELECT
    TIMESTAMP(hm.`fecha-horario`),  -- Convertir DATE a DATETIME
    hm.`horario-inicio`,
    hm.`horario-fin`,
    hm.id_medico
FROM horario_medico hm
WHERE NOT EXISTS (
    SELECT 1
    FROM agenda a
    WHERE DATE(a.fecha) = hm.`fecha-horario`
    AND a.hora_inicio = hm.`horario-inicio`
    AND a.hora_fin = hm.`horario-fin`
    AND a.id_medico_detalle = hm.id_medico
);

-- Verificar los registros insertados
SELECT COUNT(*) as 'Total en agenda' FROM agenda;

-- Mostrar los primeros registros
SELECT
    a.id_agenda,
    DATE(a.fecha) as fecha,
    a.hora_inicio,
    a.hora_fin,
    a.id_medico_detalle,
    m.`cedula-profecional`
FROM agenda a
LEFT JOIN medico_detalle m ON a.id_medico_detalle = m.id
ORDER BY a.fecha, a.hora_inicio
LIMIT 10;
