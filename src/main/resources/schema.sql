-- =====================================================================
-- Script de creación de tablas para SENA Store (migración JPA → JDBC)
-- JDBC ya NO crea las tablas automáticamente como lo hacía Hibernate.
-- Ejecutar este script en MySQL (phpMyAdmin o consola) antes de iniciar.
-- =====================================================================

-- 1. Crear base de datos
CREATE DATABASE IF NOT EXISTS springpoo
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE springpoo;

-- 2. Tabla de usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id              BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(255)  NULL,
    tipo_documento  VARCHAR(255)  NULL,
    documento       VARCHAR(255)  NULL,
    telefono        VARCHAR(255)  NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Tabla de productos (con FK a usuarios)
CREATE TABLE IF NOT EXISTS productos (
    id          BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(100)  NOT NULL,
    precio      DOUBLE        NOT NULL,
    categoria   VARCHAR(80)   NULL,
    usuario_id  BIGINT        NULL,
    CONSTRAINT fk_productos_usuarios
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Tabla de notificaciones de correo
CREATE TABLE IF NOT EXISTS notificaciones_email (
    id              BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    destinatario    VARCHAR(150)  NOT NULL,
    asunto          VARCHAR(200)  NOT NULL,
    mensaje         TEXT          NULL,
    estado          VARCHAR(30)   NOT NULL,
    fecha_envio     DATETIME      NULL,
    producto_id     BIGINT        NULL,
    producto_nombre VARCHAR(100)  NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
