DROP DATABASE IF EXISTS colitas_db;

CREATE DATABASE colitas_db;

\c colitas_db;

-- =========================
-- TIPOS
-- =========================
CREATE TYPE rol_enum AS ENUM ('ADMINISTRADOR', 'ADOPTANTE', 'CUIDADOR');
CREATE TYPE sexo_enum AS ENUM ('MACHO', 'HEMBRA');
CREATE TYPE estatus_enum AS ENUM ('DISPONIBLE', 'ADOPTADO');

-- =========================
-- CODIGO POSTAL
-- =========================
DROP TABLE IF EXISTS codigo_postal CASCADE;

CREATE TABLE codigo_postal (
    codigo_postal   VARCHAR(5)     NOT NULL PRIMARY KEY,
    latitud         DECIMAL(10, 6) NOT NULL,
    longitud        DECIMAL(10, 6) NOT NULL
);

-- CP mock
INSERT INTO codigo_postal (codigo_postal, latitud, longitud) VALUES ('00000', 19.432608, -99.133209);

-- =========================
-- USUARIO
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
    FOREIGN KEY (codigo_postal) REFERENCES codigo_postal(codigo_postal)
);

CREATE INDEX idx_usuario_cp ON usuario(codigo_postal);

-- =========================
-- ACCION
-- =========================
DROP TABLE IF EXISTS accion CASCADE;

CREATE TABLE accion (
    act_id     UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID,
    accion     TEXT    NOT NULL,
    fecha      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuario(usuario_id) ON DELETE SET NULL
);

-- =========================
-- RAZA
-- =========================
DROP TABLE IF EXISTS raza CASCADE;

CREATE TABLE raza (
    raza_id    UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    especie    VARCHAR(10)  NOT NULL CHECK (especie IN ('PERRO', 'GATO')),
    nombre_es  VARCHAR(100) NOT NULL,
    nombre_en  VARCHAR(100) NOT NULL,
    UNIQUE (especie, nombre_en)
);

CREATE INDEX idx_raza_especie ON raza(especie);

-- =========================
-- ANIMAL
-- =========================
DROP TABLE IF EXISTS animal CASCADE;

CREATE TABLE animal (
    animal_id       UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre          VARCHAR(50)   NOT NULL,
    especie         VARCHAR(50)   NOT NULL,
    raza            VARCHAR(50),
    raza_id         UUID          REFERENCES raza(raza_id) ON DELETE SET NULL,
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
    foto_id    UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    animal_id  UUID    NOT NULL,
    foto       TEXT   NOT NULL,
    fecha      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (animal_id) REFERENCES animal(animal_id) ON DELETE CASCADE
);

DROP TABLE IF EXISTS padecimiento CASCADE;

CREATE TABLE padecimiento (
    padecimiento_id UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre          VARCHAR(255) NOT NULL UNIQUE
);

DROP TABLE IF EXISTS animal_padecimiento CASCADE;

CREATE TABLE animal_padecimiento (
    animal_id       UUID NOT NULL,
    padecimiento_id UUID NOT NULL,
    PRIMARY KEY (animal_id, padecimiento_id),
    FOREIGN KEY (animal_id)       REFERENCES animal(animal_id)             ON DELETE CASCADE,
    FOREIGN KEY (padecimiento_id) REFERENCES padecimiento(padecimiento_id) ON DELETE CASCADE
);

DROP TABLE IF EXISTS vacuna CASCADE;

CREATE TABLE vacuna (
    vacuna_id UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre    VARCHAR(255) NOT NULL UNIQUE
);

DROP TABLE IF EXISTS animal_vacuna CASCADE;

CREATE TABLE animal_vacuna (
    animal_id UUID NOT NULL,
    vacuna_id UUID NOT NULL,
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
    animal_id  UUID       NOT NULL,
    fecha      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (usuario_id, animal_id),
    FOREIGN KEY (usuario_id) REFERENCES usuario(usuario_id),
    FOREIGN KEY (animal_id)  REFERENCES animal(animal_id) ON DELETE CASCADE
);

CREATE INDEX idx_usuario_interes_usuario_id ON usuario_interes(usuario_id);

-- =========================
-- TRIGGER: Solo cuidadores pueden publicar animales
-- =========================

CREATE OR REPLACE FUNCTION validar_rol_cuidador()
RETURNS TRIGGER AS $$
DECLARE
    rol_usuario rol_enum;
BEGIN
    SELECT rol INTO rol_usuario FROM usuario WHERE usuario_id = NEW.usuario_id;

    IF rol_usuario != 'CUIDADOR' THEN
        RAISE EXCEPTION 'Solo los usuarios con rol CUIDADOR pueden publicar animales.';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_validar_rol_cuidador ON animal;

CREATE TRIGGER trg_validar_rol_cuidador
BEFORE INSERT ON animal
FOR EACH ROW
EXECUTE FUNCTION validar_rol_cuidador();

-- =========================
-- SEED: Vacunas comunes
-- =========================
INSERT INTO vacuna (nombre) VALUES
  ('Rabia'),
  ('Moquillo'),
  ('Parvovirus'),
  ('Hepatitis infecciosa canina'),
  ('Leptospirosis'),
  ('Bordetella (tos de las perreras)'),
  ('Parainfluenza'),
  ('Leucemia felina'),
  ('Calicivirus felino'),
  ('Rinotraqueitis viral felina'),
  ('Panleucopenia felina'),
  ('Clamidiosis felina')
ON CONFLICT (nombre) DO NOTHING;

-- =========================
-- SEED: Padecimientos comunes
-- =========================
INSERT INTO padecimiento (nombre) VALUES
  ('Diabetes'),
  ('Artritis'),
  ('Epilepsia'),
  ('Enfermedad renal cronica'),
  ('Hipotiroidismo'),
  ('Hipertiroidismo'),
  ('Displasia de cadera'),
  ('Alergia alimentaria'),
  ('Alergia ambiental'),
  ('Enfermedad cardiaca'),
  ('Cataratas'),
  ('Leishmaniasis'),
  ('Hernia discal'),
  ('Obesidad'),
  ('Anemia')
ON CONFLICT (nombre) DO NOTHING;

-- =========================

-- =========================
-- SEED: Razas de perros y gatos
-- =========================
-- SEED: Razas de perros y gatos (con nombres en espanol)
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Affenpinscher', 'Affenpinscher') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Afghan Hound', 'Sabueso afgano') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Africanis', 'Africanis') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Aidi', 'Aidi') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Airedale Terrier', 'Airedale Terrier') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Airedoodle', 'Airedoodle') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Akbash', 'Akbash') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Akita', 'Akita') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Aksaray Malaklisi', 'Aksaray Malaklisi') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Alano Español', 'Alano Español') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Alapaha Blue Blood Bulldog', 'Alapaha Blue Blood Bulldog') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Alaskan Husky', 'Alaskan Husky') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Alaskan Klee Kai', 'Alaskan Klee Kai') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Alaskan Malamute', 'Alaskan Malamute') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Alopekis', 'Alopekis') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Alpine Dachsbracke', 'Alpine Dachsbracke') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'American Bulldog', 'American Bulldog') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'American Bully', 'American Bully') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'American Cocker Spaniel', 'American Cocker Spaniel') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'American English Coonhound', 'American English Coonhound') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'American Eskimo Dog', 'American Eskimo Dog') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'American Foxhound', 'American Foxhound') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'American Hairless Terrier', 'American Hairless Terrier') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'American Leopard Hound', 'American Leopard Hound') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'American Pit Bull Terrier', 'American Pit Bull Terrier') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'American Staffordshire Terrier', 'American Staffordshire Terrier') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'American Water Spaniel', 'American Water Spaniel') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Andalusian Terrier', 'Terrier andaluz') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Anglo-Français de Petite Vénerie', 'Anglo-Français de Petite Vénerie') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Appenzeller Sennenhund', 'Appenzeller Sennenhund') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Argentine Pila', 'Pila argentina') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Ariégeois', 'Ariégeois') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Ariège Pointer', 'Ariège Pointer') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Armant', 'Armant') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Armenian Gampr', 'Armenian Gampr') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Artois Hound', 'Artois Hound') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Aussiedoodle', 'Aussiedoodle') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Australian Cattle Dog', 'Australian Cattle Dog') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Australian Kelpie', 'Australian Kelpie') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Australian Shepherd', 'Australian Shepherd') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Australian Silky Terrier', 'Australian Silky Terrier') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Australian Stumpy Tail Cattle Dog', 'Australian Stumpy Tail Cattle Dog') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Australian Terrier', 'Australian Terrier') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Austrian Black and Tan Hound', 'Austrian Black and Tan Hound') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Austrian Pinscher', 'Austrian Pinscher') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Azawakh', 'Azawakh') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Bắc Hà', 'Bắc Hà') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Bakharwal Dog', 'Bakharwal Dog') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Banjara Hound', 'Banjara Hound') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Bankhar Dog', 'Bankhar Dog') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Barak Hound', 'Barak Hound') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Barbado da Terceira', 'Barbado da Terceira') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Barbet', 'Barbet') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Basenji', 'Basenji') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Basque Shepherd Dog', 'Perro Pastor Vasco') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Bassador', 'Bassador') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Basset Artésien Normand', 'Basset Artésien Normand') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Basset Bleu de Gascogne', 'Basset Bleu de Gascogne') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Basset Fauve de Bretagne', 'Basset Fauve de Bretagne') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Basset Hound', 'Basset Hound') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Bassettoodle', 'Bassettoodle') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Bavarian Mountain Hound', 'Sabueso de montaña bávaro') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Beagle', 'Beagle') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Beagle-Harrier', 'Beagle-Harrier') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Bearded Collie', 'Collie barbudo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Beauceron', 'Beauceron') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Bedlington Terrier', 'Bedlington Terrier') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Belgian Laekenois', 'Laekenois belga') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Belgian Malinois', 'Malinois belga') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Belgian Shepherd', 'Pastor belga') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Belgian Tervuren', 'Tervuren belga') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Bergamasco Sheepdog', 'Perro pastor de Bergamasco') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Berger Picard', 'Berger Picard') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Bernedoodle', 'Bernedoodle') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Bernese Mountain Dog', 'Perro de montaña bernés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Bichon Frisé', 'Bichon Frisé') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Biewer Terrier', 'Biewer Terrier') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Billy', 'Billy') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Black and Tan Coonhound', 'Black and Tan Coonhound') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Black Mouth Cur', 'Black Mouth Cur') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Black Norwegian Elkhound', 'Elkhound Noruego Negro') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Black Russian Terrier', 'Terrier Ruso Negro') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Bloodhound', 'Bloodhound') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Blue Picardy Spaniel', 'Blue Picardy Spaniel') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Bluetick Coonhound', 'Bluetick Coonhound') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Boerboel', 'Boerboel') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Bohemian Shepherd', 'Bohemian Shepherd') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Bohemian Spotted Dog', 'Bohemian Spotted Dog') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Bolognese', 'Bolognese') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Border Collie', 'Border Collie') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Border Terrier', 'Border Terrier') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Bordoodle', 'Bordoodle') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Borzoi', 'Borzoi') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Boston Terrier', 'Boston Terrier') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Bouvier des Ardennes', 'Bouvier des Ardennes') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Bouvier des Flandres', 'Bouvier des Flandres') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Boxer', 'Boxer') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Boxerdoodle', 'Boxerdoodle') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Boykin Spaniel', 'Boykin Spaniel') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Bracco Italiano', 'Bracco Italiano') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Braque d''Auvergne', 'Braque d''Auvergne') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Braque du Bourbonnais', 'Braque du Bourbonnais') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Braque Français', 'Braque Français') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Braque Saint-Germain', 'Braque Saint-Germain') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Brazilian Terrier', 'Terrier brasileño') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Briard', 'Briard') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Briquet de Provence', 'Briquet de Provence') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Briquet Griffon Vendéen', 'Briquet Griffon Vendéen') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Brittany', 'Bretón') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Brittanydoodle', 'Brittanydoodle') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Broholmer', 'Broholmer') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Bruno Jura Hound', 'Bruno Jura Hound') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Bucovina Shepherd Dog', 'Bucovina Shepherd Dog') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Bugg', 'Bugg') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Bulgarian Hound', 'Bulgarian Hound') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Bulgarian Scenthound', 'Bulgarian Scenthound') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Bull Arab', 'Bull Arab') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Bulldog', 'Bulldog') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Bullmastiff', 'Bullmastiff') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Bulloxer', 'Bulloxer') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Bull Terrier', 'Bull Terrier') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Bull Terrier (Miniature)', 'Bull Terrier (Miniatura)') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Bully Kutta', 'Bully Kutta') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Burgos Pointer', 'Burgos Pointer') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Ca de Bou', 'Ca de Bou') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Cairn Terrier', 'Cairn Terrier') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Calupoh', 'Calupoh') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Ca Mè Mallorquí', 'Ca Mè Mallorquí') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Campeiro Bulldog', 'Campeiro Bulldog') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Canaan Dog', 'Canaan Dog') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Canadian Eskimo Dog', 'Perro Esquimal Canadiense') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Can de Chira', 'Can de Chira') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Can de Palleiro', 'Can de Palleiro') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Cane Corso', 'Cane Corso') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Cane di Oropa', 'Cane di Oropa') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Cane Paratore', 'Cane Paratore') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Cantabrian Water Dog', 'Perro de Agua Cantábrico') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Cão de Gado Transmontano', 'Cão de Gado Transmontano') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Ca Rater Mallorquí', 'Ca Rater Mallorquí') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Cardigan Welsh Corgi', 'Cardigan Welsh Corgi') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Carea Leonés', 'Carea Leonés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Carolina Dog', 'Perro Carolina') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Carpathian Shepherd Dog', 'Perro Pastor Cárpato') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Castro Laboreiro Dog', 'Perro Castro Laboreiro') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Catahoula Leopard Dog', 'Perro Leopardo Catahoula') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Catalan Sheepdog', 'Perro Pastor Catalán') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Caucasian Shepherd Dog', 'Perro Pastor Caucásico') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Caucasian Shepherd Dog', 'Perro Pastor Caucásico') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Cavalier King Charles Spaniel', 'Cavalier King Charles Spaniel') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Cavapom', 'Cavapom') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Cavapoo', 'Cavapoo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Central Asian Shepherd Dog', 'Perro pastor de Asia Central') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Český fousek', 'Český fousek') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Cesky Terrier', 'Cesky Terrier') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Chart Polski', 'Chart Polski') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Chesapeake Bay Retriever', 'Chesapeake Bay Retriever') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Chien Français Blanc et Noir', 'Chien Français Blanc et Noir') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Chien Français Blanc et Orange', 'Chien Français Blanc et Orange') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Chien Français Tricolore', 'Chien Français Tricolore') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Chihuahua', 'Chihuahua') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Chilean Terrier', 'Terrier Chileno') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Chinese Crested', 'Crestado Chino') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Chinese Shar-Pei', 'Shar-Pei Chino') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Chinook', 'Chinook') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Chipoo', 'Chipoo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Chippiparai', 'Chippiparai') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Chiweenie', 'Chiweenie') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Chongqing Dog', 'Perro Chongqing') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Chorkie', 'Chorkie') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Chortai', 'Chortai') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Chow Chow', 'Chow Chow') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Chug', 'Chug') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Chukotka Sled Dog', 'Perro de Trineo Chukotka') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Cimarrón Uruguayo', 'Cimarrón Uruguayo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Cirneco dell''Etna', 'Cirneco dell ''Etna') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Clumber Spaniel', 'Clumber Spaniel') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Cockapoo', 'Cockapoo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Cocker Spaniel', 'Cocker Spaniel') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Colombian Fino Hound', 'Perro Fino Colombiano') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Continental Bulldog', 'Bulldog Continental') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Corgipoo', 'Corgipoo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Corsican Dog', 'Perro corso') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Coton de Tulear', 'Coton de Tulear') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Cretan Hound', 'Perro de Creta') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Croatian Sheepdog', 'Perro pastor croata') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Curly-Coated Retriever', 'Perro perdiguero de pelo rizado') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Czechoslovakian Wolfdog', 'Perro lobo checoslovaco') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Dachshund', 'Perro salchicha') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Dalmadoodle', 'Dalmadoodle') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Dalmatian', 'Dálmata') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Dandie Dinmont Terrier', 'Dandie Dinmont Terrier') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Danish Spitz', 'Spitz danés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Danish-Swedish Farmdog', 'Perro de granja danés-sueco') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Denmark Feist', 'Dinamarca Feist') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Dikkulak', 'Dikkulak') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Dingo', 'Dingo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Doberman Pinscher', 'Doberman Pinscher') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Dogo Argentino', 'Dogo Argentino') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Dogo Sardesco', 'Dogo Sardesco') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Dogue Brasileiro', 'Dogue Brasileiro') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Dogue de Bordeaux', 'Dogo de Burdeos') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Donggyeongi', 'Donggyeongi') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Dorkie', 'Dorkie') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Doxiepoo', 'Doxiepoo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Drentse Patrijshond', 'Drentse Patrijshond') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Drever', 'Drever') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Dunker', 'Dunker') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Dutch Shepherd', 'Dutch Shepherd') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Dutch Smoushond', 'Dutch Smoushond') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'East European Shepherd', 'East European Shepherd') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'East Siberian Laika', 'Laika de Siberia Oriental') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Ecuadorian Hairless Dog', 'Perro sin pelo ecuatoriano') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'English Cocker Spaniel', 'Cocker spaniel inglés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'English Foxhound', 'Foxhound inglés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'English Mastiff', 'Mastín inglés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'English Setter', 'Setter inglés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'English Shepherd', 'Pastor inglés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'English Springer Spaniel', 'Springer spaniel inglés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'English Toy Terrier (Black & Tan)', 'Terrier de juguete inglés (negro y bronceado)') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Entlebucher Mountain Dog', 'Perro de montaña Entlebucher') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Erbi Txakur', 'Erbi Txakur') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Eskapoo', 'Eskapoo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Estonian Hound', 'Sabueso estonio') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Estrela Mountain Dog', 'Perro de montaña Estrela') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Eurasier', 'Eurasier') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Faroese Sheepdog', 'Perro pastor feroés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Field Spaniel', 'Field Spaniel') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Fila Brasileiro', 'Fila Brasileiro') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Finnish Hound', 'Sabueso finlandés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Finnish Lapphund', 'Lapphund finlandés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Finnish Spitz', 'Spitz finlandés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Flat-Coated Retriever', 'Flat-Coated Retriever') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Florida Cracker Cur', 'Florida Cracker Cur') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Foxhoodle', 'Foxhoodle') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'French Bulldog', 'Bulldog francés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'French Spaniel', 'French Spaniel') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Frenchton', 'Frenchton') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Frug', 'Frug') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Gascon Saintongeois', 'Gascon Saintongeois') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Gaucho Sheepdog', 'Perro pastor gaucho') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Georgian Shepherd', 'Pastor georgiano') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Gerberian Shepsky', 'Gerberian Shepsky') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'German Hound', 'Perro alemán') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'German Longhaired Pointer', 'Puntero alemán de pelo largo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'German Pinscher', 'Pinscher alemán') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'German Roughhaired Pointer', 'Puntero alemán de pelo duro') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'German Shepherd', 'Pastor alemán') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'German Sheprador', 'Sheprador alemán') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'German Shorthaired Pointer', 'Puntero alemán de pelo corto') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'German Spaniel', 'Spaniel alemán') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'German Spitz', 'Spitz alemán') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'German Wirehaired Pointer', 'Puntero alemán de pelo duro') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Giant Schnauzer', 'Schnauzer gigante') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Glen of Imaal Terrier', 'Glen of Imaal Terrier') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Goberian', 'Goberian') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Goldendoodle', 'Goldendoodle') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Golden Mountain Dog', 'Golden Mountain Dog') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Golden Retriever', 'Golden Retriever') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Gończy Polski', 'Gończy Polski') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Gordon Setter', 'Gordon Setter') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Grand Anglo-Français Blanc et Noir', 'Grand Anglo-Français Blanc et Noir') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Grand Anglo-Français Blanc et Orange', 'Grand Anglo-Français Blanc et Orange') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Grand Anglo-Français Tricolore', 'Grand Anglo-Français Tricolore') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Grand Basset Griffon Vendéen', 'Grand Basset Griffon Vendéen') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Grand Bleu de Gascogne', 'Grand Bleu de Gascogne') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Grand Griffon Vendéen', 'Grand Griffon Vendéen') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Great Dane', 'Gran Danés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Greater Swiss Mountain Dog', 'Gran Perro de Montaña Suizo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Greek Harehound', 'Harehound Griego') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Greek Shepherd', 'Pastor Griego') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Greenland Dog', 'Perro de Groenlandia') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Greyhound', 'Galgo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Griffon Bleu de Gascogne', 'Griffon Bleu de Gascogne') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Griffon Bruxellois', 'Griffon Bruxellois') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Griffon Fauve de Bretagne', 'Griffon Fauve de Bretagne') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Griffon Nivernais', 'Griffon Nivernais') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Gull Dong', 'Gull Dong') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Gull Terrier', 'Gull Terrier') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Halden Hound', 'Halden Hound') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Hällefors Elkhound', 'Hällefors Elkhound') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Hamiltonstövare', 'Hamiltonstövare') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Hanoverian Scenthound', 'Sabueso de Hannover') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Harrier', 'Harrier') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Havanese', 'Havanés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Havapoo', 'Havapoo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Himalayan Sheepdog', 'Perro pastor del Himalaya') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Hmong Bobtail Dog', 'Perro Bobtail Hmong') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Hokkaido', 'Hokkaido') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Hovawart', 'Hovawart') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Huntaway', 'Huntaway') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Huskimo', 'Huskimo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Hygen Hound', 'Hygen Hound') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Ibizan Hound', 'Perro ibicenco') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Icelandic Sheepdog', 'Perro pastor islandés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Indian Pariah Dog', 'Perro paria indio') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Indian Spitz', 'Spitz indio') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Irish Doodle', 'Garabato irlandés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Irish Red and White Setter', 'Setter rojo y blanco irlandés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Irish Setter', 'Setter irlandés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Irish Terrier', 'Terrier irlandés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Irish Water Spaniel', 'Perro de aguas irlandés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Irish Wolfhound', 'Perro de aguas irlandés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Istrian Coarse-haired Hound', 'Perro de pelo grueso de Istria') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Istrian Shorthaired Hound', 'Perro de pelo corto de Istria') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Italian Greyhound', 'Galgo italiano') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Jack Russell Terrier', 'Jack Russel terrier') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Jagdterrier', 'Jagdterrier') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Jämthund', 'Jämthund') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Japanese Chin', 'Mentón japonés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Japanese Spitz', 'Spitz japonés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Japanese Terrier', 'Terrier japonés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Jeju Dog', 'Perro Jeju') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Jindo', 'Jindo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Jonangi', 'Jonangi') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Kaikadi', 'Kaikadi') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Kai Ken', 'Kai Ken') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Kamchatka Sled Dog', 'Perro de trineo Kamchatka') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Kangal Shepherd Dog', 'Perro pastor Kangal') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Kanni', 'Kanni') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Karakachan', 'Karakachan') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Karelian Bear Dog', 'Perro Oso Kareliano') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Karelo-Finnish Laika', 'Laika Karelo-Finlandesa') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Kars', 'Kars') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Karst Shepherd', 'Karst Shepherd') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Kazakh Tazy', 'Kazakh Tazy') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Keeshond', 'perro Keeshond') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Kerry Beagle', 'Kerry BeagleGenericName') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Kerry Blue Terrier', 'Kerry Blue Terrier') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Khala', 'Khala') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'King Charles Spaniel', 'English toy spaniel') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'King Shepherd', 'Rey Pastor') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Kintamani', 'Kintamani') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Kishu Ken', 'Kishu Ken') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Kokoni', 'Kokoni') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Kombai', 'Kombai') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Komondor', 'perro Komondor') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Kooikerhondje', 'Kooikerhondje') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Koolie', 'Koolie') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Kromfohrländer', 'Kromfohrländer') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Kuchi', 'Kuchi') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Kunming Dog', 'Perro Kunming') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Kurdish Mastiff', 'Mastín Kurdo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Kuvasz', 'perro Kuvasz') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Labradoodle', 'Labrapoodle') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Labrador Retriever', 'Labrador retriever ') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Labraheeler', 'Labraheeler') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Labsky', 'Labsky') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Lacy Dog', 'Lacy Dog') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Lagotto Romagnolo', 'Lagotto romagnolo			') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Lài', 'Lài') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Laizhou Hong', 'Laizhou Hong') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Lakeland Terrier', 'perro Lakeland terrier') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Lancashire Heeler', 'Lancashire Heeler') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Landseer', 'Landseer') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Langqing', 'Langqing') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Lapponian Herder', 'Lapponian Herder') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Large Münsterländer', 'Münsterländer grande') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Leonberger', 'Leonberger') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Levriero Sardo', 'Levriero Sardo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Lhasa Apso', 'perro Lhasa apso') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Lhasapoo', 'Lhasapoo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Liangshan Dog', 'Perro de Liangshan') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Lithuanian Hound', 'Sabueso lituano') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Lobito Herreño', 'Lobito Herreño') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Löwchen', 'Löwchen') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Lucas Terrier', 'lucas terrier') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Lupo Italiano', 'ITALIANO') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Lurcher', 'Lurcher') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Mackenzie River Husky', 'Husky de río Mackenzie') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Magyar Agár', 'Magyar') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Mahratta Hound', 'Perro mahratta') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Majorca Shepherd Dog', 'Perro pastor de Mallorca') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Maltese', 'Maltés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Maltipoo', 'Maltipoo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Maltshi', 'Maltshi') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Manchester Terrier', 'terrier de Manchester') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Maneto', 'Maneto') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Maremma Sheepdog', 'Perro pastor de Maremma') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Markiesje', 'Markiesje') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Mastidoodle', 'Mastidoodle') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'McNab', 'McNab') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Miniature American Shepherd', 'Pastor americano en miniatura') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Miniature Bull Terrier', 'Bull Terrier en miniatura') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Miniature Fox Terrier', 'Fox Terrier en miniatura') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Miniature Pinscher', 'Pinscher en miniatura') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Miniature Schnauzer', 'Schnauzer en miniatura') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Mojee', 'Mojee') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Molossus of Epirus', 'Moloso de Epiro') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Mongrel', 'Perro mestizo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Montenegrin Mountain Hound', 'Sabueso montés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Morkie', 'Morkie') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Moscow Watchdog', 'Perro guardián de Moscú') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Mountain Cur', 'Mountain Cur ') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Mountain Feist', 'Mountain Feist') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Mudhol Hound', 'Mudhol Hound') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Mudi', 'Mudi') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Neapolitan Mastiff', 'Mastín napolitano') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Nenets Herding Laika', 'Nenets pastoreando a Laika') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Newfoundland', 'Terranova') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Newfypoo', 'Newfypoo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'New Guinea Singing Dog', 'Perro cantor de Nueva Guinea') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'New Zealand Heading Dog', 'Perro de cabecera de Nueva Zelanda') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Norfolk Terrier', 'terrier de Norfolk') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Norrbottenspets', 'Norrbottenspets') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Northern Inuit Dog', 'Perro esquimal del norte') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Norwegian Buhund', 'Buhund noruego') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Norwegian Elkhound', 'Elkhound noruego') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Norwegian Lundehund', 'Lundehund noruego') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Norwich Terrier', 'terrier de Norwich') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Nova Scotia Duck Tolling Retriever', 'Nueva Escocia Duck Tolling Retriever') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Nureongi', 'Nureongi') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Old Danish Pointer', 'Antiguo puntero danés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Olde English Bulldogge', 'perro Olde English Bulldogge') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Old English Sheepdog', 'viejo pastor inglés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Otterhound', 'Otterhound') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Pachón Navarro', 'Pachón Navarro') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Pampas Deerhound', 'Deerhound pampeano') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Papillon', 'Papillon') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Papipoo', 'Papipoo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Parson Russell Terrier', 'terrier Parson Russell') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Pastore della Lessinia e del Lagorai', 'Pastore della Lessinia e del Lagorai') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Pastor Garafiano', 'Pastor Garafiano') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Patagonian Sheepdog', 'Perro pastor patagónico') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Patterdale Terrier', 'perro Patterdale Terrier') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Peekapoo', 'peekapoo ') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Pekeapoo', 'Pekeapoo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Pekingese', 'Pekinés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Pembroke Welsh Corgi', 'Pembroke Welsh Corgi') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Perdigueiro Galego', 'Perdigueiro Galego') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Perro Majorero', 'Perro Majorero') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Peruvian Inca Orchid', 'Orquídea inca peruana') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Petit Basset Griffon Vendéen', 'Petit basset griffon vendeen dog') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Petit Bleu de Gascogne', 'perro Petit Bleu de Gascogne') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Phalène', 'Phalène') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Pharaoh Hound', 'Pharaoh Hound') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Philippine Forest Dog', 'Perro del bosque filipino') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Phu Quoc Ridgeback', 'Phu Quoc Ridgeback') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Picardy Spaniel', 'Spaniel de picardía') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Pitsky', 'Pitsky') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Plott Hound', 'perro Plott hound') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Plummer Terrier', 'Plummer Terrier') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Podenco Andaluz', 'Podenco Andaluz') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Podenco Canario', 'Podenco Canario') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Podenco Valenciano', 'Podenco Valenciano') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Pointer', 'Pointer') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Poitevin', 'Poitevin') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Polish Hound', 'Sabueso polaco') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Polish Lowland Sheepdog', 'Perro pastor polaco de tierras bajas') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Pomapoo', 'Pomapoo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Pomeranian', 'Pomerania') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Pomsky', 'Pomsky') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Pont-Audemer Spaniel', 'Pont-Audemer Spaniel') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Poochon', 'Poochon') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Poodle', 'Caniche') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Poodle (Miniature)', 'Caniche (miniatura)') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Poodle (Toy)', 'Caniche (juguete)') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Porcelaine', 'Porcelana') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Portuguese Podengo', 'Podenco portugués') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Portuguese Pointer', 'Puntero portugués') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Portuguese Sheepdog', 'Perro pastor portugués') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Portuguese Water Dog', 'Perro de agua portugués') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Posavac Hound', 'Posavac Hound') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Pražský Krysařík', 'Pražský Krysařík') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Presa Canario', 'Perro de Pressa Canario') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Pudelpointer', 'perro Pudelpointer') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Pug', 'Carlino') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Pugapoo', 'Pugapoo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Puggle', ' Puggle') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Puli', 'perro Puli') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Pumi', 'Pumi') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Pungsan', 'Pungsan') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Pyredoodle', 'Pyredoodle') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Pyrenean Mastiff', 'Mastín de los Pirineos') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Pyrenean Mountain Dog', 'Perro de montaña de los Pirineos') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Pyrenean Sheepdog', 'Perro pastor de los Pirineos') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Rafeiro do Alentejo', 'Rafeiro do Alentejo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Rajapalayam', 'Rajapalayam') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Rampur Greyhound', 'Galgo de Rampur') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Rastreador Brasileiro', 'perro rastreador brasilero') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Ratonero Murciano', 'Ratonero Murciano') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Rat Terrier', 'perro Rat Terrier') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Redbone Coonhound', 'Redbone Coonhound') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Rhodesian Ridgeback', 'RHODESIAN RIDGEBACK&#10;') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Ridgeypoo', 'Ridgeypoo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Rize Koyun', 'Rize Koyun') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Romanian Mioritic Shepherd Dog', 'Perro pastor miorítico rumano') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Romanian Raven Shepherd Dog', 'Perro pastor cuervo rumano') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Rottle', 'Rottle') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Rottsky', 'Rottsky') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Rottweiler', 'Rottweiler') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Rough Collie', 'collie peludo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Russian Spaniel', 'Spaniel ruso') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Russian Toy', 'Juguete ruso') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Russo-European Laika', 'Laika ruso-europea') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Ryukyu', 'Islas Ryūkyū') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Saarloos Wolfdog', 'Perro lobo de Saarloos') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Sabueso Español', 'perro sabueso español') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Saint Berdoodle', 'San Berdoodle') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Saint Bernard', 'San Bernardo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Saint Miguel Cattle Dog', 'Perro de ganado de San Miguel') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Saint-Usuge Spaniel', 'Spaniel de Saint-Usuge') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Sakhalin Husky', 'Sakhalin Husky') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Saluki', 'perro galgo Saluki') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Samoyed', 'Samoyedo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Sapsali', 'Sapsali') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Sarabi', 'Sarabi') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Sarail Hound', 'Sarail Hound') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Sardinian Shepherd Dog', 'Perro pastor sardo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Šarplaninac', 'Šarplaninac') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Schapendoes', 'Schapendoes') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Schillerstövare', 'Schillerstövare') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Schipperke', 'perro Schipperke') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Schnoodle', 'Schnoodle') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Schweizerischer Niederlaufhund', 'Schweizerischer Niederlaufhund') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Schweizer Laufhund', 'Schweizer Laufhund') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Scoodle', 'Scoodle') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Scottish Deerhound', 'Lebrel escocés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Scottish Terrier', 'Terrier escocés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Sealyham Terrier', 'perro Sealyham terrier') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Segugio dell''Appennino', 'Segugio dell ''Appennino') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Segugio Italiano', 'Segugio Italiano') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Segugio Maremmano', 'Segugio Maremmano') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Serbian Hound', 'Sabueso serbio') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Serbian Tricolour Hound', 'Serbian Tricolor Hound') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Serrano Bulldog', 'Bulldog serrano') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Sheepadoodle', 'Sheepadoodle') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Shetland Sheepdog', 'Pastor de las islas Shetland') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Shiba Inu', 'perro Shiba Inu') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Shih Poo', 'Shih Poo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Shih Tzu', 'Shih Tzu') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Shikoku', 'Shikoku') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Shiloh Shepherd', 'Shiloh Shepherd ') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Shorkie', 'Perro mestizo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Siberian Husky', 'Husky siberiano') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Silken Windhound', ' silken windhound') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Sinhala Hound', 'Perro cingalés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Skye Terrier', 'perro Skye terrier') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Sloughi', 'Sloughi') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Slovak Cuvac', 'Cuvac eslovaco') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Slovak Rough-haired Pointer', 'Puntero eslovaco de pelo áspero') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Slovenský kopov', 'Slovenský kopov') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Smaland Hound', 'Sabueso de Smaland') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Small Međimurje Dog', 'Perro pequeño de Međimurje') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Small Münsterländer', 'Pequeño Münsterländer') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Smithfield', 'Smithfield') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Smooth Collie', 'collie de pelo corto') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Smooth Fox Terrier', 'fox terrier de pelo suave') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Soft Coated Wheaten Terrier', 'Soft-coated wheaten terrier') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Spanish Greyhound', 'Galgo español') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Spanish Mastiff', 'Mastín español') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Spanish Water Dog', 'Perro de agua español') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Spino degli Iblei', 'Spino degli Iblei') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Spinone Italiano', 'perro Spinone italiano') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Sporting Lucas Terrier', 'Sporting Lucas Terrier') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Springerdoodle', 'Springerdoodle') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Sprollie', 'Sprollie') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Stabyhoun', 'Stabyhoun') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Staffordshire Bull Terrier', 'Staffordshire Bull Terrier') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Standard Schnauzer', 'Schnauzer estándar') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Stephens Stock', 'Stephens Stock') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'St. Hubert Jura Hound', 'St. Hubert Jura Hound') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Styrian Coarse-haired Hound', 'Styrian Hound de pelo grueso') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Sussex Spaniel', 'perro de aguas de Sussex') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Swedish Lapphund', 'Lapphund sueco') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Swedish Vallhund', 'Vallhund sueco') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Taigan', 'Taigan') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Taiwan Dog', 'Perro de Taiwán') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Tamaskan Dog', 'Tamaskan Dog') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Tang Dog', 'Tang Dog') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Tarsus Çatalburun', 'Tarso Çatalburun') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Tatra Shepherd Dog', 'Tatra Shepherd Dog') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Teddy Roosevelt Terrier', 'Teddy Roosevelt Terrier') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Telomian', 'Telomian') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Tenterfield Terrier', 'Tenterfield Terrier') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Thai Bangkaew Dog', 'Perro bangkaew tailandés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Thai Ridgeback', 'perro Thai Ridgeback') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Tibetan Kyi Apso', 'Tibetano Kyi Apso') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Tibetan Mastiff', 'Mastín tibetano') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Tibetan Spaniel', 'perro de aguas tibetano') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Tibetan Terrier', 'terrier tibetano') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Tonya Finosu', 'Tonya Finosu') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Tornjak', 'Tornjak') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Tosa', 'Tosa') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Tosa Ken', 'Tosa Ken') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Toy Fox Terrier', 'fox terrier doméstico') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Toy Manchester Terrier', 'terrier de Manchester doméstico') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Transylvanian Hound', 'perro sabueso transilvano') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Treeing Cur', 'perro Treeing Cur') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Treeing Feist', 'perro Treeing Feist') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Treeing Tennessee Brindle', 'perro Treeing Tennessee Brindle') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Treeing Walker Coonhound', 'Treeing Walker Coonhound') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Trigg Hound', 'Trigg Hound ') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Tyrolean Hound', 'Sabueso del Tirol') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Valencian Terrier', 'Terrier Valenciano') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Valley Bulldog', 'Bulldog del Valle') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Vikhan', 'Vikhan') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Villano de Las Encartaciones', 'Villano de Las Encartaciones') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Villanuco de Las Encartaciones', 'Villanuco de Las Encartaciones') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Vizsla', 'Vizsla') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Vizsladoodle', 'Vizsladoodle') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Volkosob', 'Volkosob') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Volpino Italiano', 'Volpino Italiano') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Weimaraner', 'Braco de Weimar') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Weimardoodle', 'Weimardoodle') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Welsh Hound', 'Sabueso galés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Welsh Sheepdog', 'perro ovejero galés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Welsh Springer Spaniel', 'perro Welsh Springer spaniel') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Welsh Terrier', 'terrier galés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'West Country Harrier', 'West Country Harrier') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'West Highland White Terrier', 'West Highland White Terrier') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Westiepoo', 'Westiepoo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Westphalian Dachsbracke', 'Dachsbracke de Westfalia') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'West Siberian Laika', 'West Siberian Laika ') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Wetterhoun', 'Wetterhoun') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Whippet', 'perro lebrel') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'White Shepherd', 'White Shepherd ') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'White Swiss Shepherd Dog', 'Perro Pastor Blanco Suizo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Wire Fox Terrier', 'fox terrier de pelo ensortijado') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Wirehaired Pointing Griffon', 'Grifón de pelo duro o Grifón Korthals') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Wirehaired Vizsla', 'Wirehaired Vizsla') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Xiasi Dog', 'Perro Xiasi') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Xigou', 'Xigou') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Xoloitzcuintli', 'perro mexicano sin pelo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Yakutian Laika', 'Yakutian Laika') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Yorkipoo', 'Yorkipoo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Yorkshire Terrier', 'Yorkshire terrier ') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Yorktese', 'Yorktese') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('PERRO', 'Zerdava', 'Zerdava') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Abyssinian', 'Abisinio') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Aegean', 'Egeo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'American Bobtail', 'Bobtail americano') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'American Curl', 'Rizo americano') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'American Shorthair', 'Pelo corto americano') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'American Wirehair', 'Pelo de alambre americano') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Arabian Mau', 'Mau árabe') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Australian Mist', 'Niebla australiana') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Balinese', 'Balinés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Bambino', 'Bambino') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Bengal', 'Bengala') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Birman', 'Birmano') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Bombay', 'Bombay') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'British Longhair', 'Británico de pelo largo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'British Shorthair', 'Británico de pelo corto') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Burmese', 'Burmese') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Burmilla', 'Burmilla') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'California Spangled', 'California Spangled') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Chantilly-Tiffany', 'Chantilly-Tiffany') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Chartreux', 'Cartujo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Chausie', 'Chausie') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Cheetoh', 'Cheetoh') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Colorpoint Shorthair', 'Colorpoint de pelo corto') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Cornish Rex', 'Cornish Rex') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Cymric', 'Cymric') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Cyprus', 'Chipre') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Devon Rex', 'Devon Rex') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Donskoy', 'Donskoy') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Dragon Li', 'Dragón Li') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Egyptian Mau', 'Mau Egipcio') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'European Burmese', 'Cocina birmana europea') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Exotic Shorthair', 'Exótico de pelo corto') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Havana Brown', 'Havana Brown') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Himalayan', 'Himalaya') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Japanese Bobtail', 'Bobtail japonés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Javanese', 'Javanés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Khao Manee', 'Khao Manee') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Korat', 'KoratCity in Thailand') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Kurilian', 'Kurilian') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'LaPerm', 'LaPerm') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Maine Coon', 'Maine Coon') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Malayan', 'Malayo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Manx', 'Manés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Munchkin', 'Munchkin') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Nebelung', 'Nebelung') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Norwegian Forest Cat', 'Gato del Bosque de Noruega ') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Ocicat', 'Ocicat') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Oriental', 'Oriental') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Persian', 'Persa') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Pixie-bob', 'gato Pixiebob') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Ragamuffin', 'pelagatos') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Ragdoll', 'Muñeca de trapo') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Russian Blue', 'Azul ruso') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Savannah', 'Sabana') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Scottish Fold', 'Pliegue escocés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Selkirk Rex', 'Selkirk Rex') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Siamese', 'Siamés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Siberian', 'Siberiano') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Singapura', 'Singapur') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Snowshoe', 'Raqueta de nieve') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Somali', 'Somalí') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Sphynx', 'Esfinge') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Tonkinese', 'Tonkinés') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Toyger', 'Juguetero') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Turkish Angora', 'Angora Turco') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'Turkish Van', 'Furgoneta Turca') ON CONFLICT (especie, nombre_en) DO NOTHING;
INSERT INTO raza (especie, nombre_en, nombre_es) VALUES ('GATO', 'York Chocolate', 'Chocolate York') ON CONFLICT (especie, nombre_en) DO NOTHING;
