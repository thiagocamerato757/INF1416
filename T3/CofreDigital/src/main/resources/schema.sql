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
     fname TEXT,
    FOREIGN KEY (MID) REFERENCES Mensagens(MID),
    FOREIGN KEY (UID) REFERENCES Usuarios(UID) ON DELETE SET NULL
);


INSERT INTO Grupos (GID, nome) VALUES (1, 'admin'), (2, 'user');
INSERT INTO Mensagens (MID, texto) VALUES
(1001, 'Sistema iniciado.'),
(1002, 'Sistema encerrado.'),
(1003, 'Sessão iniciada para %s.'),
(1004, 'Sessão encerrada para %s.'),
(1005, 'Partida do sistema iniciada para cadastro do administrador.'),
(1006, 'Partida do sistema iniciada para operação normal pelos usuários.'),
(2001, 'Autenticação etapa 1 iniciada.'),
(2002, 'Autenticação etapa 1 encerrada.'),
(2003, 'Login name %s identificado com acesso liberado.'),
(2004, 'Login name %s identificado com acesso bloqueado.'),
(2005, 'Login name %s não identificado.'),
(3001, 'Autenticação etapa 2 iniciada para %s.'),
(3002, 'Autenticação etapa 2 encerrada para %s.'),
(3003, 'Senha pessoal verificada positivamente para %s.'),
(3004, 'Primeiro erro da senha pessoal contabilizado para %s.'),
(3005, 'Segundo erro da senha pessoal contabilizado para %s.'),
(3006, 'Terceiro erro da senha pessoal contabilizado para %s.'),
(3007, 'Acesso do usuario %s bloqueado pela autenticação etapa 2.'),
(4001, 'Autenticação etapa 3 iniciada para %s.'),
(4002, 'Autenticação etapa 3 encerrada para %s.'),
(4003, 'Token verificado positivamente para %s.'),
(4004, 'Primeiro erro de token contabilizado para %s.'),
(4005, 'Segundo erro de token contabilizado para %s.'),
(4006, 'Terceiro erro de token contabilizado para %s.'),
(4007, 'Acesso do usuario %s bloqueado pela autenticação etapa 3.'),
(5001, 'Tela principal apresentada para %s.'),
(5002, 'Opção 1 do menu principal selecionada por %s.'),
(5003, 'Opção 2 do menu principal selecionada por %s.'),
(5004, 'Opção 3 do menu principal selecionada por %s.'),
(6001, 'Tela de cadastro apresentada para %s.'),
(6002, 'Botão cadastrar pressionado por %s.'),
(6003, 'Senha pessoal inválida fornecida por %s.'),
(6004, 'Caminho do certificado digital inválido fornecido por %s.'),
(6005, 'Chave privada verificada negativamente para %s (caminho inválido).'),
(6006, 'Chave privada verificada negativamente para %s (frase secreta inválida).'),
(6007, 'Chave privada verificada negativamente para %s (assinatura digital inválida).'),
(6008, 'Confirmação de dados aceita por %s.'),
(6009, 'Confirmação de dados rejeitada por %s.'),
(6010, 'Botão voltar de cadastro para o menu principal pressionado por %s.'),
(7001, 'Tela de consulta de arquivos secretos apresentada para %s.'),
(7002, 'Botão voltar de consulta para o menu principal pressionado por %s.'),
(7003, 'Botão Listar de consulta pressionado por %s.'),
(7004, 'Caminho de pasta inválido fornecido por %s.'),
(7005, 'Arquivo de índice decriptado com sucesso para %s.'),
(7006, 'Arquivo de índice verificado (integridade e autenticidade) com sucesso para %s.'),
(7007, 'Falha na decriptação do arquivo de índice para %s.'),
(7008, 'Falha na verificação (integridade e autenticidade) do arquivo de índice para %s.'),
(7009, 'Lista de arquivos presentes no índice apresentada para %s.'),
(7010, 'Arquivo %s selecionado por %s para decriptação.'),
(7011, 'Acesso permitido ao arquivo %s para %s.'),
(7012, 'Acesso negado ao arquivo %s para %s.'),
(7013, 'Arquivo %s decriptado com sucesso para %s.'),
(7014, 'Arquivo %s verificado (integridade e autenticidade) com sucesso para %s.'),
(7015, 'Falha na decriptação do arquivo %s para %s.'),
(7016, 'Falha na verificação (integridade e autenticidade) do arquivo %s para %s.'),
(8001, 'Tela de saída apresentada para %s.'),
(8002, 'Botão encerrar sessão pressionado por %s.'),
(8003, 'Botão encerrar sistema pressionado por %s.'),
(8004, 'Botão voltar de sair para o menu principal pressionado por %s.');