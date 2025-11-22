-- Script para actualizar fechas de horarios a fechas futuras
-- Ejecutar en MySQL Workbench o terminal MySQL

USE citas_medicas;

-- Aurora Robelo (médico ID 9) - Actualizar de 2025-10-29 a la próxima semana
UPDATE horario_medico 
SET 
    `fecha-horario` = DATE_ADD(CURDATE(), INTERVAL 7 DAY),
    valid_until = DATE_ADD(CURDATE(), INTERVAL 37 DAY)
WHERE id_medico = 9 AND `fecha-horario` = '2025-10-29';

-- Armando Ruano (médico ID 10) - Actualizar de 2025-10-31 a dentro de 8 días
UPDATE horario_medico 
SET 
    `fecha-horario` = DATE_ADD(CURDATE(), INTERVAL 8 DAY),
    valid_until = DATE_ADD(CURDATE(), INTERVAL 38 DAY)
WHERE id_medico = 10 AND `fecha-horario` = '2025-10-31';

-- bryan lopez (médico ID 8) - Actualizar de 2025-11-20 a mañana
UPDATE horario_medico 
SET 
    `fecha-horario` = DATE_ADD(CURDATE(), INTERVAL 1 DAY),
    valid_until = DATE_ADD(CURDATE(), INTERVAL 31 DAY)
WHERE id_medico = 8 AND `fecha-horario` = '2025-11-20';

-- Verificar los cambios
SELECT 
    id,
    id_medico,
    `fecha-horario` as fecha,
    `horario-inicio` as hora_inicio,
    `horario-fin` as hora_fin,
    `estado medico` as estado,
    valid_until
FROM horario_medico 
WHERE `estado medico` = 'DISPONIBLE'
ORDER BY `fecha-horario`, `horario-inicio`;
