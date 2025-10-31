-- Script para insertar los estados de citas necesarios
-- Ejecutar este script en la base de datos MySQL

-- Eliminar registros existentes si existen (opcional)
-- DELETE FROM estatus WHERE estatus IN ('Pendiente', 'Aceptada', 'Cancelada', 'Pospuesta');

-- Insertar estados de citas
INSERT INTO estatus (estatus) VALUES ('Pendiente');
INSERT INTO estatus (estatus) VALUES ('Aceptada');
INSERT INTO estatus (estatus) VALUES ('Cancelada');
INSERT INTO estatus (estatus) VALUES ('Pospuesta');

-- Verificar los registros insertados
SELECT * FROM estatus;

-- Actualizar la tabla cita para agregar la columna idEstatus si no existe
-- ALTER TABLE cita ADD COLUMN idEstatus BIGINT NULL;
-- ALTER TABLE cita ADD CONSTRAINT fk_cita_estatus FOREIGN KEY (idEstatus) REFERENCES estatus(id);

-- Establecer estado 'Pendiente' por defecto para citas existentes sin estado
UPDATE cita SET idEstatus = (SELECT id FROM estatus WHERE estatus = 'Pendiente' LIMIT 1) WHERE idEstatus IS NULL;
