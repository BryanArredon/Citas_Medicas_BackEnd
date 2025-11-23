-- Script para crear paciente para Aurora (usuario 29)
-- Ejecuta esto en MySQL para poder usar el chatbot de IA

-- Verificar si el usuario existe
SELECT id_usuario, nombre, apellido_paterno, correo_electronico, id_rol 
FROM usuario 
WHERE id_usuario = 29;

-- Verificar si ya tiene paciente
SELECT * FROM paciente_detalle WHERE id_usuario = 29;

-- Si NO tiene paciente, insertar uno
INSERT INTO paciente_detalle (
    id_usuario,
    curp,
    sexo,
    fecha_nacimiento,
    tipo_sangre
) VALUES (
    29,                           -- id_usuario de Aurora
    'TEMP000000HDFRRR00',        -- CURP temporal
    'F',                          -- Sexo (F = Femenino)
    '2000-01-01',                -- Fecha de nacimiento por defecto
    'O+'                          -- Tipo de sangre por defecto
);

-- Verificar que se creó correctamente
SELECT p.id_paciente, p.id_usuario, u.nombre, u.apellido_paterno, u.correo_electronico
FROM paciente_detalle p
JOIN usuario u ON p.id_usuario = u.id_usuario
WHERE p.id_usuario = 29;

-- Ahora Aurora podrá usar el chatbot de IA para agendar citas
