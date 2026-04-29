-- ========================================================================
-- This script is used to reset the database schema for the application.
-- It drops existing tables and recreates them with the necessary structure.
-- =========================================================================

-- Drop existing tables if they exist
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS Usuarios;
DROP TABLE IF EXISTS Grupos;
DROP TABLE IF EXISTS Chaveiro;
DROP TABLE IF EXISTS Mensagens;
DROP TABLE IF EXISTS Registros;
SET FOREIGN_KEY_CHECKS = 1;