DROP DATABASE IF EXISTS colitas_db;

CREATE DATABASE colitas_db;

\c colitas_db;

-- =========================
-- TIPOS
-- =========================

-- Roles: ADMINISTRADOR incluido para uso futuro; backend usa ADOPTANTE y CUIDADOR
CREATE TYPE rol_enum AS ENUM ('ADMINISTRADOR', 'ADOPTANTE', 'CUIDADOR');
CREATE TYPE sexo_enum AS ENUM ('MACHO', 'HEMBRA');
CREATE TYPE estatus_enum AS ENUM ('DISPONIBLE', 'ADOPTADO');

-- =========================
-- CODIGO POSTAL
-- =========================
DROP TABLE IF EXISTS codigo_postal CASCADE;

CREATE TABLE codigo_postal (
    cp        VARCHAR(5)     NOT NULL PRIMARY KEY,
    latitud   DECIMAL(10, 6) NOT NULL,
    longitud  DECIMAL(10, 6) NOT NULL
);

-- CP mock para desarrollo: permite registrar usuarios sin validación geográfica real
INSERT INTO codigo_postal (cp, latitud, longitud) VALUES ('00000', 19.432608, -99.133209);

-- =========================
-- USUARIO
-- Nombres de columna alineados con UsuarioEntity.kt del backend
-- =========================
DROP TABLE IF EXISTS usuario CASCADE;

CREATE TABLE usuario (
    usuario_id       UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    curp             VARCHAR(18)   NOT NULL UNIQUE,
    username         VARCHAR(50)   NOT NULL UNIQUE,
    foto_perfil      TEXT,
    email            VARCHAR(100)  NOT NULL UNIQUE,
    nombres          VARCHAR(100)  NOT NULL,
    apellido_paterno VARCHAR(100)  NOT NULL,
    apellido_materno VARCHAR(100)  NOT NULL,
    password         VARCHAR(255)  NOT NULL,
    token            TEXT,
    codigo_postal    VARCHAR(5)    NOT NULL,
    rol              rol_enum      NOT NULL,
    fecha_registro   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    fecha_update     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    fecha_eliminado  TIMESTAMP,
    FOREIGN KEY (codigo_postal) REFERENCES codigo_postal(cp)
);

CREATE INDEX idx_usuario_cp ON usuario(codigo_postal);

-- =========================
-- ACCION
-- =========================
DROP TABLE IF EXISTS accion CASCADE;

CREATE TABLE accion (
    act_id     SERIAL  PRIMARY KEY,
    usuario_id UUID,
    accion     TEXT    NOT NULL,
    fecha      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuario(usuario_id) ON DELETE SET NULL
);

-- =========================
-- ANIMAL
-- =========================
DROP TABLE IF EXISTS animal CASCADE;

CREATE TABLE animal (
    animal_id       SERIAL        PRIMARY KEY,
    nombre          VARCHAR(50)   NOT NULL,
    especie         VARCHAR(50)   NOT NULL,
    raza            VARCHAR(50),
    fecha_nacimiento DATE          NOT NULL,
    sexo            sexo_enum     NOT NULL,
    descripcion     TEXT          NOT NULL,
    estatus         estatus_enum  NOT NULL,
    usuario_id      UUID          NOT NULL,
    fecha_registro  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    inapropiado     BOOLEAN       DEFAULT FALSE,
    esterilizado    BOOLEAN       DEFAULT FALSE,
    updated_at      TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuario(usuario_id) ON DELETE CASCADE
);

CREATE INDEX idx_animal_usuario_id ON animal(usuario_id);

DROP TABLE IF EXISTS foto_animal CASCADE;

CREATE TABLE foto_animal (
    foto_id    SERIAL PRIMARY KEY,
    animal_id  INT    NOT NULL,
    foto       TEXT   NOT NULL,
    fecha      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (animal_id) REFERENCES animal(animal_id) ON DELETE CASCADE
);

DROP TABLE IF EXISTS padecimiento CASCADE;

CREATE TABLE padecimiento (
    padecimiento_id SERIAL       PRIMARY KEY,
    nombre          VARCHAR(255) NOT NULL UNIQUE
);

DROP TABLE IF EXISTS animal_padecimiento CASCADE;

CREATE TABLE animal_padecimiento (
    animal_id       INT NOT NULL,
    padecimiento_id INT NOT NULL,
    PRIMARY KEY (animal_id, padecimiento_id),
    FOREIGN KEY (animal_id)       REFERENCES animal(animal_id)             ON DELETE CASCADE,
    FOREIGN KEY (padecimiento_id) REFERENCES padecimiento(padecimiento_id) ON DELETE CASCADE
);

DROP TABLE IF EXISTS vacuna CASCADE;

CREATE TABLE vacuna (
    vacuna_id SERIAL       PRIMARY KEY,
    nombre    VARCHAR(255) NOT NULL UNIQUE
);

DROP TABLE IF EXISTS animal_vacuna CASCADE;

CREATE TABLE animal_vacuna (
    animal_id INT NOT NULL,
    vacuna_id INT NOT NULL,
    PRIMARY KEY (animal_id, vacuna_id),
    FOREIGN KEY (animal_id) REFERENCES animal(animal_id) ON DELETE CASCADE,
    FOREIGN KEY (vacuna_id) REFERENCES vacuna(vacuna_id) ON DELETE CASCADE
);

-- =========================
-- USUARIO_INTERES
-- =========================
DROP TABLE IF EXISTS usuario_interes CASCADE;

CREATE TABLE usuario_interes (
    usuario_id UUID      NOT NULL,
    animal_id  INT       NOT NULL,    fecha      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (usuario_id, animal_id),
    FOREIGN KEY (usuario_id) REFERENCES usuario(usuario_id),
    FOREIGN KEY (animal_id)  REFERENCES animal(animal_id) ON DELETE CASCADE
);

CREATE INDEX idx_usuario_interes_usuario_id ON usuario_interes(usuario_id);
