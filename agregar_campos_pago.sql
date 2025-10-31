-- ============================================
-- Script: Agregar Campos de Pago a la tabla Cita
-- Fecha: 31 de octubre de 2025
-- Descripción: Agrega campos para tracking de pagos
-- ============================================

USE DBCitasMedicas;

-- Verificar estructura actual de la tabla
DESCRIBE cita;

-- Agregar columnas para tracking de pagos
ALTER TABLE cita
ADD COLUMN montoPagado DECIMAL(10,2) NULL COMMENT 'Monto pagado por la cita' AFTER motivo,
ADD COLUMN idPago BIGINT NULL COMMENT 'ID del pago en el microservicio' AFTER montoPagado,
ADD COLUMN estadoPago VARCHAR(50) NULL COMMENT 'Estado: PENDIENTE, APROBADO, RECHAZADO, REEMBOLSADO' AFTER idPago,
ADD COLUMN numeroReferenciaPago VARCHAR(100) NULL COMMENT 'Referencia única del pago' AFTER estadoPago,
ADD COLUMN fechaPago DATETIME NULL COMMENT 'Fecha y hora del pago' AFTER numeroReferenciaPago,
ADD COLUMN metodoPago VARCHAR(50) NULL COMMENT 'Método de pago utilizado' AFTER fechaPago;

-- Agregar índices para mejorar consultas
ALTER TABLE cita
ADD INDEX idx_estado_pago (estadoPago),
ADD INDEX idx_id_pago (idPago),
ADD INDEX idx_referencia_pago (numeroReferenciaPago);

-- Verificar cambios
DESCRIBE cita;

-- Consultar citas con pagos (después de implementar)
SELECT 
    idCita,
    montoPagado,
    estadoPago,
    numeroReferenciaPago,
    metodoPago,
    fechaPago,
    fechaSolicitud
FROM cita
WHERE idPago IS NOT NULL
ORDER BY fechaPago DESC;

-- Estadísticas de pagos por estado
SELECT 
    estadoPago,
    COUNT(*) as total_citas,
    SUM(montoPagado) as total_monto,
    AVG(montoPagado) as monto_promedio
FROM cita
WHERE idPago IS NOT NULL
GROUP BY estadoPago;

COMMIT;

-- ============================================
-- FIN DEL SCRIPT
-- ============================================
