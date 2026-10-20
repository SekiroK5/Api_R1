-- =====================================================================
-- API REST para la administración de usuarios
-- Script de creación de base de datos (PostgreSQL 14 o superior)
-- Base de datos: usuarios_db  (alojada en Neon)
-- =====================================================================

-- En Neon la base de datos se crea desde la consola o la API.
-- En un PostgreSQL local, ejecutar primero (conectado a "postgres"):
-- CREATE DATABASE usuarios_db WITH ENCODING 'UTF8';

-- Extensión para acelerar las búsquedas parciales por nombre (LIKE '%texto%').
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- ---------------------------------------------------------------------
-- Tabla: usuarios
-- Contiene los datos de Persona (clase abstracta) y de Usuario (entidad).
-- Los valores de sexo, nacionalidad y estado_civil corresponden a los
-- enums de Catalogos.java y se guardan por nombre (EnumType.STRING).
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
    id                BIGINT       GENERATED ALWAYS AS IDENTITY,
    nombre            VARCHAR(60)  NOT NULL,
    segundo_nombre    VARCHAR(60),
    apellido_paterno  VARCHAR(60)  NOT NULL,
    apellido_materno  VARCHAR(60)  NOT NULL,
    fecha_nacimiento  DATE         NOT NULL,
    curp              VARCHAR(18)  NOT NULL,
    rfc               VARCHAR(13)  NOT NULL,
    sexo              VARCHAR(15)  NOT NULL,
    nacionalidad      VARCHAR(15)  NOT NULL,
    estado_civil      VARCHAR(15)  NOT NULL,
    correo            VARCHAR(120) NOT NULL,
    telefono          VARCHAR(10)  NOT NULL,
    activo            BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_creacion    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Llave primaria generada por la base de datos
    CONSTRAINT pk_usuarios PRIMARY KEY (id),

    -- Unicidad
    CONSTRAINT uk_usuarios_correo UNIQUE (correo),
    CONSTRAINT uk_usuarios_curp   UNIQUE (curp),
    CONSTRAINT uk_usuarios_rfc    UNIQUE (rfc),

    -- Textos obligatorios sin cadenas vacías ni solo espacios
    CONSTRAINT chk_usuarios_nombre           CHECK (btrim(nombre) <> ''),
    CONSTRAINT chk_usuarios_apellido_paterno CHECK (btrim(apellido_paterno) <> ''),
    CONSTRAINT chk_usuarios_apellido_materno CHECK (btrim(apellido_materno) <> ''),

    -- Formatos
    CONSTRAINT chk_usuarios_correo_formato CHECK (correo ~ '^[^@\s]+@[^@\s]+\.[^@\s]+$'),
    CONSTRAINT chk_usuarios_correo_minusc  CHECK (correo = lower(correo)),
    CONSTRAINT chk_usuarios_telefono       CHECK (telefono ~ '^[0-9]{10}$'),
    CONSTRAINT chk_usuarios_curp           CHECK (curp ~ '^[A-Z][AEIOUX][A-Z]{2}[0-9]{6}[HMX][A-Z]{2}[B-DF-HJ-NP-TV-Z]{3}[A-Z0-9][0-9]$'),
    CONSTRAINT chk_usuarios_rfc            CHECK (rfc ~ '^[A-ZÑ&]{4}[0-9]{6}[A-Z0-9]{3}$'),
    CONSTRAINT chk_usuarios_fecha_nac      CHECK (fecha_nacimiento >= DATE '1900-01-01'),

    -- Catálogos (Catalogos.java)
    CONSTRAINT chk_usuarios_sexo         CHECK (sexo IN ('HOMBRE', 'MUJER', 'NO_BINARIO')),
    CONSTRAINT chk_usuarios_nacionalidad CHECK (nacionalidad IN ('MEXICANA', 'EXTRANJERA')),
    CONSTRAINT chk_usuarios_estado_civil CHECK (estado_civil IN ('SOLTERO', 'CASADO', 'DIVORCIADO', 'VIUDO', 'UNION_LIBRE'))
);

COMMENT ON TABLE  usuarios                IS 'Usuarios administrados por la API REST.';
COMMENT ON COLUMN usuarios.id             IS 'Identificador primario generado por la base de datos.';
COMMENT ON COLUMN usuarios.nombre         IS 'Nombre del usuario. Junto con correo forma la llave compuesta de búsqueda.';
COMMENT ON COLUMN usuarios.correo         IS 'Correo electrónico único, almacenado en minúsculas.';
COMMENT ON COLUMN usuarios.sexo           IS 'Enum Catalogos.Sexo.';
COMMENT ON COLUMN usuarios.nacionalidad   IS 'Enum Catalogos.Nacionalidad.';
COMMENT ON COLUMN usuarios.estado_civil   IS 'Enum Catalogos.EstadoCivil.';
COMMENT ON COLUMN usuarios.activo         IS 'Estado del usuario.';
COMMENT ON COLUMN usuarios.fecha_creacion IS 'Fecha y hora de creación, controlada por el servidor.';

-- ---------------------------------------------------------------------
-- Índices
-- La llave compuesta (nombre, correo) se resuelve con uk_usuarios_correo,
-- porque el correo ya es único.
-- ---------------------------------------------------------------------

-- Búsqueda dinámica: coincidencia parcial por nombre
CREATE INDEX IF NOT EXISTS idx_usuarios_nombre_trgm
    ON usuarios USING gin (lower(nombre) gin_trgm_ops);

-- Búsqueda dinámica: coincidencia exacta por teléfono
CREATE INDEX IF NOT EXISTS idx_usuarios_telefono
    ON usuarios (telefono);

-- Listado paginado ordenado por fecha de creación
CREATE INDEX IF NOT EXISTS idx_usuarios_fecha_creacion
    ON usuarios (fecha_creacion DESC);
