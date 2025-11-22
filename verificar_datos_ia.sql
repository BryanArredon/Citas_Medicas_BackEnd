-- Script para verificar datos necesarios para la IA

-- 1. Verificar áreas
SELECT 'ÁREAS:' as tabla;
SELECT idArea, nombreArea, estatus FROM area;

-- 2. Verificar servicios
SELECT 'SERVICIOS:' as tabla;
SELECT idServicio, nombreServicio, costo, idArea FROM servicio;

-- 3. Verificar médicos
SELECT 'MÉDICOS:' as tabla;
SELECT idMedico, nombreCompleto, cedula FROM medico LIMIT 5;

-- 4. Verificar agenda
SELECT 'AGENDA (próximos días):' as tabla;
SELECT idAgenda, idMedico, fecha, horaInicio, horaFin, estatus 
FROM agenda 
WHERE fecha >= CURDATE() 
ORDER BY fecha, horaInicio 
LIMIT 10;

-- 5. Si no hay áreas, insertar algunas de prueba:
-- INSERT INTO area (nombreArea, descripcion, estatus) VALUES
-- ('Cardiología', 'Área de cardiología', true),
-- ('Dermatología', 'Área de dermatología', true),
-- ('Pediatría', 'Área de pediatría', true);

-- 6. Si no hay servicios, insertar algunos de prueba:
-- INSERT INTO servicio (nombreServicio, descripcionServicio, costo, idArea) VALUES
-- ('Consulta Cardiológica General', 'Consulta con cardiólogo', 500.00, 1),
-- ('Electrocardiograma', 'Estudio del corazón', 300.00, 1),
-- ('Consulta Dermatológica', 'Consulta con dermatólogo', 400.00, 2);
