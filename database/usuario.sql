DROP TABLE IF EXISTS Usuario;

CREATE TABLE Usuario (
    IDUsuario SERIAL PRIMARY KEY,
    curp VARCHAR(18) NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    rol VARCHAR(20) NOT NULL,
    foto_perfil VARCHAR(255), -- url
    nombres VARCHAR(100) NOT NULL,
    apellido_paterno VARCHAR(100) NOT NULL,
    apellido_materno VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    codigo_postal VARCHAR(10) NOT NULL,
    password VARCHAR(255) NOT NULL,
    token VARCHAR(255)
);

-- Restricción para el campo 'rol' para que solo acepte 'cuidador' o 'adoptante'
ALTER TABLE Usuario
    ADD CONSTRAINT rol_check CHECK (rol IN ('cuidador', 'adoptante'));