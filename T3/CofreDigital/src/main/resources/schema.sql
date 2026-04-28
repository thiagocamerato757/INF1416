-- ============================================================
-- DATABASE SCHEMA - DIGITAL VAULT (COFRE DIGITAL)
-- INF1416 - Information Security, PUC-Rio
-- ============================================================

-- ------------------------------------------------------------
-- 1. TABLE Grupos (User groups)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS Grupos (
    GID INT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE
);

-- ------------------------------------------------------------
-- 2. TABLE Chaveiro (Stores certificates and private keys)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS Chaveiro (
    KID INT AUTO_INCREMENT PRIMARY KEY,
    UID INT UNIQUE,   -- FK to Usuarios
    certificado_pem TEXT NOT NULL,
    chave_privada_encrypted BLOB NOT NULL
);

-- ------------------------------------------------------------
-- 3. TABLE Usuarios (Application users)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS Usuarios (
    UID INT AUTO_INCREMENT PRIMARY KEY,
    login VARCHAR(255) NOT NULL UNIQUE,        -- email address
    nome VARCHAR(255) NOT NULL,
    senha_bcrypt VARCHAR(60) NOT NULL,         -- bcrypt hash (2y, cost 8)
    totp_secret_encrypted BLOB NOT NULL,       -- TOTP secret encrypted with AES-256
    grupo_id INT NOT NULL,                     -- FK to Grupos.GID
    KID INT,                                   -- FK to Chaveiro.KID
    erro_senha INT DEFAULT 0,                  -- consecutive password error counter
    erro_token INT DEFAULT 0,                  -- consecutive token error counter
    bloqueado_ate TIMESTAMP NULL,              -- temporary block expiration
    total_acessos INT DEFAULT 0,
    total_consultas INT DEFAULT 0,
    FOREIGN KEY (grupo_id) REFERENCES Grupos(GID),
    FOREIGN KEY (KID) REFERENCES Chaveiro(KID) ON DELETE SET NULL
    );

-- Now add the foreign key constraint in Chaveiro
ALTER TABLE Chaveiro ADD FOREIGN KEY (UID) REFERENCES Usuarios(UID) ON DELETE CASCADE;

-- ------------------------------------------------------------
-- 4. TABLE Mensagens (Message catalog for logs)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS Mensagens (
     MID INT PRIMARY KEY,
     texto TEXT NOT NULL
);

-- ------------------------------------------------------------
-- 5. TABLE Registros (Audit logs)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS Registros (
     RID INT AUTO_INCREMENT PRIMARY KEY,
     data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
     MID INT NOT NULL,
     UID INT,
     detalhe VARCHAR(255),
    FOREIGN KEY (MID) REFERENCES Mensagens(MID),
    FOREIGN KEY (UID) REFERENCES Usuarios(UID) ON DELETE SET NULL
);