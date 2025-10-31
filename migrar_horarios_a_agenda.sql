-- Script para migrar los horarios existentes de horario_medico a agenda
-- Ejecutar este script en la base de datos DBCitasMedicas

USE DBCitasMedicas;

-- Insertar los horarios existentes de horario_medico a agenda
INSERT INTO agenda (fecha, horaInicio, horaFin, idMedicoDetalle)
SELECT 
    TIMESTAMP(`fecha-horario`),  -- Convertir DATE a DATETIME
    `horario-inicio`,
    `horario-fin`,
    id_medico
FROM horario_medico hm
WHERE NOT EXISTS (
    SELECT 1 
    FROM agenda a 
    WHERE DATE(a.fecha) = hm.`fecha-horario`
    AND a.horaInicio = hm.`horario-inicio`
    AND a.horaFin = hm.`horario-fin`
    AND a.idMedicoDetalle = hm.id_medico
);

-- Verificar los registros insertados
SELECT COUNT(*) as 'Total en agenda' FROM agenda;

-- Mostrar los primeros registros
SELECT 
    a.idAgenda,
    DATE(a.fecha) as fecha,
    a.horaInicio,
    a.horaFin,
    a.idMedicoDetalle,
    m.`cedula-profecional`
FROM agenda a
LEFT JOIN medico_detalle m ON a.idMedicoDetalle = m.id
ORDER BY a.fecha, a.horaInicio
LIMIT 10;
