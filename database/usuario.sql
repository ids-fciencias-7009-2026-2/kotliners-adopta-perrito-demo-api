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
    codigo_postal VARCHAR(5) NOT NULL,
    password VARCHAR(255) NOT NULL,
    token VARCHAR(255)
);

-- Restricción para el campo 'rol' para que solo acepte 'cuidador' o 'adoptante'
ALTER TABLE Usuario
    ADD CONSTRAINT rol_check CHECK (rol IN ('ADOPTANTE', 'CUIDADOR'));

-- Restricción para el campo 'codigo_postal' para que solo acepte códigos postales mexicanos, de 5 dígitos
ALTER TABLE Usuario
    ADD CONSTRAINT codigo_postal_check CHECK (codigo_postal ~ '^\d{5}$');

-- Restricción para el campo 'curp' para que solo acepte CURP válidos, de 18 caracteres alfanuméricos.
ALTER TABLE Usuario
    ADD CONSTRAINT curp_check CHECK (curp ~ '^[A-Z0-9]{18}$');

COMMENT ON TABLE Usuario IS 'Tabla que almacena la información de los usuarios del sistema, incluyendo cuidadores y adoptantes.';
COMMENT ON COLUMN Usuario.IDUsuario IS 'Identificador único del usuario, generado automáticamente.';
COMMENT ON COLUMN Usuario.curp IS 'CURP del usuario.';
COMMENT ON COLUMN Usuario.username IS 'Nombre de usuario.';
COMMENT ON COLUMN Usuario.rol IS 'Rol del usuario, puede ser "ADOPTANTE" o "CUIDADOR".';
COMMENT ON COLUMN Usuario.foto_perfil IS 'URL de la foto de perfil del usuario.';
COMMENT ON COLUMN Usuario.nombres IS 'Nombres del usuario.';
COMMENT ON COLUMN Usuario.apellido_paterno IS 'Apellido paterno del usuario.';
COMMENT ON COLUMN Usuario.apellido_materno IS 'Apellido materno del usuario.';
COMMENT ON COLUMN Usuario.email IS 'Correo electrónico del usuario.';
COMMENT ON COLUMN Usuario.codigo_postal IS 'Código postal del usuario.';
COMMENT ON COLUMN Usuario.password IS 'Contraseña del usuario, almacenada de forma segura (hasheada).';
COMMENT ON COLUMN Usuario.token IS 'Token de autenticación para el usuario';

COMMENT ON CONSTRAINT rol_check ON Usuario IS 'Restricción que asegura que el rol del usuario sea "ADOPTANTE" o "CUIDADOR".';
COMMENT ON CONSTRAINT codigo_postal_check ON Usuario IS 'Restricción que asegura que el código postal tenga exactamente 5 dígitos.';
COMMENT ON CONSTRAINT curp_check ON Usuario IS 'Restricción que asegura que el CURP tenga exactamente 18 caracteres alfanuméricos.';