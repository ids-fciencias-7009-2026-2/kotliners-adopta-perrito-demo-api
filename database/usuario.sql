DROP TABLE IF EXISTS Usuario;

CREATE TABLE Usuario (
    IDUsuario SERIAL PRIMARY KEY,
    nombres VARCHAR(100) NOT NULL,
    apellido_paterno VARCHAR(100) NOT NULL,
    apellido_materno VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    codigo_postal VARCHAR(10) NOT NULL,
    password VARCHAR(255) NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    token VARCHAR(255)
);