-- Script para configurar médicos y crear agendas de ejemplo
-- Ejecutar este script en MySQL/MariaDB

-- 1. ASIGNAR SERVICIO A LOS MÉDICOS EXISTENTES
-- Asumiendo que el servicio con id=1 es "Consulta General"
UPDATE medicoDetalle SET idServicio = 1 WHERE id = 8;  -- Bryan Lopez
UPDATE medicoDetalle SET idServicio = 1 WHERE id = 9;  -- Aurora Robelo (aunque es PACIENTE, por si acaso)

-- Verificar la actualización
SELECT id, idUsuario, idServicio, `cedula-profecional` FROM medicoDetalle;

-- 2. CREAR AGENDAS DE EJEMPLO PARA LOS MÉDICOS
-- Agendas para el médico Bryan Lopez (id=8) - Semana actual

-- Lunes 04 de Noviembre, 2025
INSERT INTO agenda (fecha, horaInicio, horaFin, idMedicoDetalle) VALUES
('2025-11-04', '09:00:00', '10:00:00', 8),
('2025-11-04', '10:00:00', '11:00:00', 8),
('2025-11-04', '11:00:00', '12:00:00', 8),
('2025-11-04', '14:00:00', '15:00:00', 8),
('2025-11-04', '15:00:00', '16:00:00', 8),
('2025-11-04', '16:00:00', '17:00:00', 8);

-- Martes 05 de Noviembre, 2025
INSERT INTO agenda (fecha, horaInicio, horaFin, idMedicoDetalle) VALUES
('2025-11-05', '09:00:00', '10:00:00', 8),
('2025-11-05', '10:00:00', '11:00:00', 8),
('2025-11-05', '11:00:00', '12:00:00', 8),
('2025-11-05', '14:00:00', '15:00:00', 8),
('2025-11-05', '15:00:00', '16:00:00', 8);

-- Miércoles 06 de Noviembre, 2025
INSERT INTO agenda (fecha, horaInicio, horaFin, idMedicoDetalle) VALUES
('2025-11-06', '09:00:00', '10:00:00', 8),
('2025-11-06', '10:00:00', '11:00:00', 8),
('2025-11-06', '11:00:00', '12:00:00', 8),
('2025-11-06', '14:00:00', '15:00:00', 8),
('2025-11-06', '15:00:00', '16:00:00', 8);

-- Jueves 07 de Noviembre, 2025
INSERT INTO agenda (fecha, horaInicio, horaFin, idMedicoDetalle) VALUES
('2025-11-07', '09:00:00', '10:00:00', 8),
('2025-11-07', '10:00:00', '11:00:00', 8),
('2025-11-07', '11:00:00', '12:00:00', 8),
('2025-11-07', '14:00:00', '15:00:00', 8),
('2025-11-07', '15:00:00', '16:00:00', 8),
('2025-11-07', '16:00:00', '17:00:00', 8);

-- Viernes 08 de Noviembre, 2025
INSERT INTO agenda (fecha, horaInicio, horaFin, idMedicoDetalle) VALUES
('2025-11-08', '09:00:00', '10:00:00', 8),
('2025-11-08', '10:00:00', '11:00:00', 8),
('2025-11-08', '11:00:00', '12:00:00', 8),
('2025-11-08', '14:00:00', '15:00:00', 8);

-- 3. VERIFICAR LAS AGENDAS CREADAS
SELECT 
    a.idAgenda,
    a.fecha,
    a.horaInicio,
    a.horaFin,
    m.id as medico_id,
    u.nombre,
    u.apellidoPaterno
FROM agenda a
JOIN medicoDetalle m ON a.idMedicoDetalle = m.id
JOIN usuarios u ON m.idUsuario = u.idUsuario
ORDER BY a.fecha, a.horaInicio;

-- 4. OPCIONAL: Si quieres crear más horarios para días específicos
-- Puedes modificar las fechas según tus necesidades
