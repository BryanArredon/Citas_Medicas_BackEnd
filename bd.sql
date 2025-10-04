CREATE DATABASE IF NOT EXISTS DBCitasMedicas;

USE DBCitasMedicas;

CREATE TABLE tRol (
    idRol INT PRIMARY KEY AUTO_INCREMENT,
    nombreRol VARCHAR(50) NOT NULL
);

INSERT INTO tRol (nombreRol) VALUES ('Administrador'), ('Médico'), ('Paciente');
-- Roles:
-- 1 Administrador
-- 2 Médico
-- 3 Paciente

CREATE TABLE tEstatus (
    idEstatus INT PRIMARY KEY AUTO_INCREMENT,
    estatus VARCHAR(35) NOT NULL
);

CREATE TABLE tArea (
    idArea INT PRIMARY KEY AUTO_INCREMENT,
    nombreArea VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255)
);

CREATE TABLE tServicio (
    idServicio INT PRIMARY KEY AUTO_INCREMENT,
    nombreServicio VARCHAR(100) NOT NULL,
    idArea INT,
    descripcion VARCHAR(255),
    costo DECIMAL(10, 2),
    FOREIGN KEY (idArea) REFERENCES tArea(idArea)
);

CREATE TABLE tUsuario (
    idUsuario INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(50) NOT NULL,
    apellidoPaterno VARCHAR(50) NOT NULL,
    apellidoMaterno VARCHAR(50),
    sexo ENUM('M','F') NOT NULL,
    fechaNacimiento DATE,
    direccion VARCHAR(255),
    telefono VARCHAR(15),
    correoElectronico VARCHAR(100) UNIQUE NOT NULL,
    contraseña VARCHAR(255) NOT NULL,
    idRol INT NOT NULL,
    FOREIGN KEY (idRol) REFERENCES tRol(idRol)
);

CREATE TABLE tMedicoDetalle (
    idMedicoDetalle INT PRIMARY KEY AUTO_INCREMENT,
    idUsuario INT UNIQUE,
    idServicio INT,
    cedulaProfesional VARCHAR(20),
    FOREIGN KEY (idUsuario) REFERENCES tUsuario(idUsuario),
    FOREIGN KEY (idServicio) REFERENCES tServicio(idServicio)
);

CREATE TABLE tPacienteDetalle (
    idPacienteDetalle INT PRIMARY KEY AUTO_INCREMENT,
    idUsuario INT UNIQUE,
    tipoSangre VARCHAR(5),
    alergias TEXT,
    FOREIGN KEY (idUsuario) REFERENCES tUsuario(idUsuario)
);

CREATE TABLE tAgenda (
    idAgenda INT PRIMARY KEY AUTO_INCREMENT,
    fecha DATETIME NOT NULL,
    horaInicio TIME NOT NULL,
    horaFin TIME NOT NULL,
    idMedicoDetalle INT,
    FOREIGN KEY (idMedicoDetalle) REFERENCES tMedicoDetalle(idMedicoDetalle)
);

CREATE TABLE tCita (
    idCita INT PRIMARY KEY AUTO_INCREMENT,
    idPacienteDetalle INT,
    idMedicoDetalle INT,
    idServicio INT,
    idAgenda INT,
    idEstatus INT,
    fechaSolicitud DATETIME NOT NULL,
    motivo VARCHAR(255),
    FOREIGN KEY (idPacienteDetalle) REFERENCES tPacienteDetalle(idPacienteDetalle),
    FOREIGN KEY (idMedicoDetalle) REFERENCES tMedicoDetalle(idMedicoDetalle),
    FOREIGN KEY (idServicio) REFERENCES tServicio(idServicio),
    FOREIGN KEY (idAgenda) REFERENCES tAgenda(idAgenda),
    FOREIGN KEY (idEstatus) REFERENCES tEstatus(idEstatus)
);

CREATE TABLE tHistorialClinico (
    idHistorial INT PRIMARY KEY AUTO_INCREMENT,
    idPacienteDetalle INT,
    idMedicoDetalle INT,
    idCita INT,
    fecha DATE NOT NULL,
    diagnostico VARCHAR(255),
    tratamiento VARCHAR(255),
    notasAdicionales TEXT,
    fechaActualizacion DATETIME,
    FOREIGN KEY (idPacienteDetalle) REFERENCES tPacienteDetalle(idPacienteDetalle),
    FOREIGN KEY (idMedicoDetalle) REFERENCES tMedicoDetalle(idMedicoDetalle),
    FOREIGN KEY (idCita) REFERENCES tCita(idCita)
);

CREATE TABLE tExpediente (
    idExpediente INT PRIMARY KEY AUTO_INCREMENT,
    idPacienteDetalle INT,
    idHistorial INT,
    fechaApertura DATE NOT NULL,
    fechaCierre DATE,
    fechaActualizacion DATETIME,
    nombreArchivo VARCHAR(100),
    rutaArchivo VARCHAR(255),
    FOREIGN KEY (idPacienteDetalle) REFERENCES tPacienteDetalle(idPacienteDetalle),
    FOREIGN KEY (idHistorial) REFERENCES tHistorialClinico(idHistorial)
);