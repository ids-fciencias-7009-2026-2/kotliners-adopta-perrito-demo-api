-- =========================================================================
-- Usuario
-- =========================================================================
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

COMMENT ON TABLE Usuario IS 'Tabla que almacena la información de los usuarios del sistema, incluyendo cuidadores y adoptantes.';
COMMENT ON COLUMN Usuario.IDUsuario IS 'Identificador único del usuario, generado automáticamente.';
COMMENT ON COLUMN Usuario.curp IS 'CURP del usuario.';
COMMENT ON COLUMN Usuario.username IS 'Nombre de usuario.';
COMMENT ON COLUMN Usuario.rol IS 'Rol del usuario, puede ser "cuidador" o "adoptante".';
COMMENT ON COLUMN Usuario.foto_perfil IS 'URL de la foto de perfil del usuario.';
COMMENT ON COLUMN Usuario.nombres IS 'Nombres del usuario.';
COMMENT ON COLUMN Usuario.apellido_paterno IS 'Apellido paterno del usuario.';
COMMENT ON COLUMN Usuario.apellido_materno IS 'Apellido materno del usuario.';
COMMENT ON COLUMN Usuario.email IS 'Correo electrónico del usuario.';
COMMENT ON COLUMN Usuario.codigo_postal IS 'Código postal del usuario.';
COMMENT ON COLUMN Usuario.password IS 'Contraseña del usuario, almacenada de forma segura (hasheada).';
COMMENT ON COLUMN Usuario.token IS 'Token de autenticación para el usuario';

COMMENT ON CONSTRAINT rol_check ON Usuario IS 'Restricción que asegura que el rol del usuario sea "cuidador" o "adoptante".';