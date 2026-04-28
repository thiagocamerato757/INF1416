# CHECKLIST — Trabalho 3: Cofre Digital

**INF1416 – Segurança da Informação · PUC-Rio · Prof. Anderson Oliveira da Silva**  
⏰ **Entrega: 17/05/2026 às 23:59h**

---

## 0. Configuração inicial (já realizado)

- [x] Projeto Maven criado no IntelliJ
- [x] Dependências adicionadas (Driver do Banco de dados, Bouncy Castle)
- [x] Arquivo `application.properties` criado com parâmetros de conexão ao banco
- [x] Classe `DataBaseStarter` implementada (conexão com logging)
- [x] Classe principal `DigitalVault` testando conexão com sucesso

---

## 1. Estrutura do banco de dados

- [X] Criar script SQL (`schema.sql`) em `src/main/resources/`
- [X] Criar tabela **Grupos** (GID único, nome do grupo)
- [X] Criar tabela **Usuarios** (UID único, login/email, nome, senha bcrypt, segredo TOTP cifrado, GID, KID, contador de erros, status de bloqueio, timestamp de bloqueio, total de acessos, total de consultas)
- [X] Criar tabela **Chaveiro** (KID único, UID, certificado PEM, chave privada criptografada em BLOB)
- [X] Criar tabela **Mensagens** (MID único, texto da mensagem)
- [X] Criar tabela **Registros** (RID único, MID, UID opcional, detalhe opcional, timestamp)
---

## 2. Partida do sistema — Primeira execução (cadastro do administrador)

- [ ] Verificar se existe algum usuário na tabela **Usuarios**
- [ ] Se não houver usuários: registrar log **MID 1005** (partida para cadastro do administrador)
- [ ] Abrir tela de cadastro do administrador (sem autenticação prévia)
- [ ] Coletar: caminho do certificado X.509 (PEM), caminho da chave privada (PKCS8 criptografada), frase secreta e senha pessoal
- [ ] Validar certificado X.509: caminho válido, arquivo legível, formato PEM correto
- [ ] Decriptar chave privada com a frase secreta (AES/ECB/PKCS5Padding, chave AES-256 via SHA1PRNG)
- [ ] Validar o par certificado+chave: assinar array aleatório de **9216 bytes** e verificar com a chave pública do certificado
- [ ] Extrair nome e e-mail do campo Sujeito do certificado
- [ ] Validar senha pessoal: 8–10 dígitos, apenas números (0–9), sem sequência de dígitos repetidos
- [ ] Armazenar senha pessoal com bcrypt: versão **2y**, custo **8** (via `OpenBSDBCrypt.generate()` do Bouncy Castle)
- [ ] Gerar segredo TOTP: **20 bytes aleatórios**
- [ ] Codificar segredo em BASE32 (RFC 4648) e exibir ao administrador para cadastro no Google Authenticator
- [ ] (Opcional) Exibir QR Code com URI `otpauth://totp/...`
- [ ] Criptografar segredo TOTP com AES-256/ECB/PKCS5Padding (chave derivada da senha pessoal via SHA1PRNG) e salvar no banco
- [ ] Armazenar certificado no formato PEM e chave privada em formato binário criptografado na tabela **Chaveiro**, gerando KID único
- [ ] Armazenar KID no registro do administrador na tabela **Usuarios**
- [ ] Manter a **frase secreta do administrador em memória** durante toda a execução do sistema
- [ ] Após cadastro inicial, iniciar o processo de autenticação de usuários

---

## 3. Partida do sistema — Execuções normais (2ª vez em diante)

- [ ] Detectar que já existem usuários cadastrados → registrar log **MID 1006**
- [ ] Solicitar frase secreta da chave privada do administrador (primeiro UID cadastrado)
- [ ] Recuperar chave privada do admin do banco e decriptá-la com a frase secreta fornecida
- [ ] Validar par: assinar array aleatório de **9216 bytes** e verificar com chave pública do certificado do admin
- [ ] Se validação **negativa**: notificar o usuário e **encerrar o sistema**
- [ ] Se validação **positiva**: manter frase secreta em memória e prosseguir para autenticação de usuários

---

## 4. Autenticação multifator — 3 etapas (LoginFrame)

### Etapa 1 — Identificação (login name)
- [ ] Registrar log **MID 2001** (etapa 1 iniciada)
- [ ] Exibir campo de e-mail para identificação
- [ ] Se e-mail **não encontrado**: registrar **MID 2005**, avisar e permanecer na etapa 1
- [ ] Se e-mail encontrado e usuário **bloqueado**: registrar **MID 2004**, avisar e permanecer na etapa 1
- [ ] Se e-mail válido e acesso liberado: registrar **MID 2003** e avançar para etapa 2
- [ ] Registrar log **MID 2002** ao encerrar etapa 1 com sucesso

### Etapa 2 — Senha pessoal (teclado virtual)
- [ ] Registrar log **MID 3001** (etapa 2 iniciada)
- [ ] Implementar teclado virtual numérico sobrecarregado: **5 botões, 2 números por botão**, distribuídos aleatoriamente e sem repetição
- [ ] A cada clique de botão, redistribuir os números aleatoriamente entre os 5 botões
- [ ] Coletar senha de 8–10 dígitos sem exibir os valores reais pressionados
- [ ] Verificar senha com `OpenBSDBCrypt.checkPassword()` (Bouncy Castle)
- [ ] Se senha **incorreta**: registrar MID 3004 / 3005 / 3006 conforme contagem (1º, 2º, 3º erro)
- [ ] Após **3 erros consecutivos**: registrar **MID 3007**, bloquear usuário por **2 minutos** e retornar à etapa 1 (outros usuários podem tentar)
- [ ] Se senha **correta**: registrar **MID 3003**, zerar contador de erros de senha e avançar para etapa 3
- [ ] Registrar log **MID 3002** ao encerrar etapa 2 com sucesso

### Etapa 3 — Token TOTP (Google Authenticator)
- [ ] Registrar log **MID 4001** (etapa 3 iniciada)
- [ ] Exibir campo para entrada do token TOTP de 6 dígitos
- [ ] Recuperar segredo TOTP cifrado do banco e decriptá-lo usando chave AES-256 derivada da senha pessoal (via SHA1PRNG)
- [ ] Implementar classe **TOTP** usando APENAS: `javax.crypto.Mac`, `javax.crypto.spec.SecretKeySpec`, `java.util.Date` e `Base32` — nenhuma biblioteca externa
- [ ] Calcular 3 tokens: intervalo atual, intervalo −30s e intervalo +30s
- [ ] Validar o token fornecido contra os 3 valores calculados
- [ ] Se token **inválido**: registrar MID 4004 / 4005 / 4006 conforme contagem (1º, 2º, 3º erro); retornar ao início da etapa 3
- [ ] Após **3 erros consecutivos**: registrar **MID 4007**, bloquear usuário por **2 minutos** e retornar à etapa 1
- [ ] Se token **válido**: registrar **MID 4003**, zerar contador de erros de token e permitir acesso
- [ ] Registrar log **MID 4002** ao encerrar etapa 3 com sucesso
- [ ] Registrar log **MID 1003** (sessão iniciada para login_name) e incrementar total de acessos do usuário

---

## 5. Implementação da classe TOTP

- [ ] Implementar construtor: receber segredo em BASE32 e intervalo de tempo (default 30s); decodificar BASE32 e armazenar em `key` (APENAS classes permitidas)
- [ ] Implementar `HMAC_SHA1(byte[] counter, byte[] key)`: produzir hash HMAC-SHA1 usando `javax.crypto.Mac`
- [ ] Implementar `getTOTPCodeFromHash(byte[] hash)`: truncamento dinâmico → valor de 6 dígitos, preenchido com zeros à esquerda quando necessário
- [ ] Implementar `TOTPCode(long timeInterval)`: calcular contador (segundos desde epoch / timeStep), chamar HMAC_SHA1 e getTOTPCodeFromHash
- [ ] Implementar `generateCode()`: retornar token para o intervalo atual
- [ ] Implementar `validateCode(String inputTOTP)`: verificar contra intervalos atual, −30s e +30s; retornar `true` se qualquer um coincidir
- [ ] Testar TOTP manualmente contra o Google Authenticator com o mesmo segredo BASE32

---

## 6. Interface principal — MenuFrame

- [ ] Registrar log **MID 5001** ao apresentar a tela principal
- [ ] Exibir **cabeçalho** com: Login (email), Grupo e Nome do usuário logado
- [ ] Exibir **corpo 1**: Total de acessos do usuário
- [ ] **Grupo Administrador** — corpo 2 com menu: 1 – Cadastrar novo usuário | 2 – Consultar pasta de arquivos secretos | 3 – Sair
- [ ] **Grupo Usuário** — corpo 2 com menu: 2 – Consultar pasta de arquivos secretos | 3 – Sair (opção 1 não aparece)
- [ ] Registrar log **MID 5002** ao selecionar opção 1
- [ ] Registrar log **MID 5003** ao selecionar opção 2
- [ ] Registrar log **MID 5004** ao selecionar opção 3

---

## 7. Cadastro de novos usuários — CadastroFrame (apenas admin)

- [ ] Registrar log **MID 6001** ao apresentar a tela de cadastro
- [ ] Exibir cabeçalho com dados do admin logado + corpo 1 com total de usuários do sistema
- [ ] Formulário com campos: caminho do certificado (255 chars), caminho da chave privada (255 chars), frase secreta (255 chars), grupo (Administrador | Usuário), senha pessoal (10 chars), confirmação da senha pessoal (10 chars)
- [ ] Registrar log **MID 6002** ao pressionar Botão Cadastrar
- [ ] Validar senha pessoal: 8–10 dígitos (0–9), sem sequência de dígitos repetidos; registrar **MID 6003** se inválida
- [ ] Verificar se as duas senhas digitadas são iguais
- [ ] Validar caminho do certificado digital; registrar **MID 6004** se inválido
- [ ] Validar caminho da chave privada; registrar **MID 6005** se inválido
- [ ] Tentar decriptar chave privada com frase secreta; registrar **MID 6006** se frase inválida
- [ ] Validar par: assinar array aleatório de 9216 bytes e verificar com chave pública do certificado; registrar **MID 6007** se inválido
- [ ] Exibir **tela de confirmação** com os dados do certificado: Versão, Série, Validade, Tipo de Assinatura, Emissor, Sujeito (Friendly Name) e E-mail
- [ ] Se dados confirmados: registrar **MID 6008**; se rejeitados: registrar **MID 6009** e retornar ao formulário com dados preenchidos
- [ ] Verificar unicidade do e-mail (login name) no banco; notificar se duplicado
- [ ] Armazenar: hash bcrypt da senha pessoal (2y, custo 8), segredo TOTP cifrado com AES-256 derivado da senha, certificado PEM e chave privada binária cifrada na tabela Chaveiro (novo KID)
- [ ] Gerar segredo TOTP de 20 bytes aleatórios, codificar em BASE32 e **exibir ao usuário** para configurar no Google Authenticator
- [ ] (Opcional) Exibir QR Code com URI `otpauth://totp/Cofre%20Digital:email?secret=BASE32SECRET`
- [ ] Após cadastro com **sucesso**: retornar ao formulário com os **campos vazios**
- [ ] Após cadastro com **falha**: retornar ao formulário com os **campos preenchidos** com os dados já fornecidos
- [ ] Botão Voltar: registrar **MID 6010** e retornar ao MenuFrame

---

## 8. Consulta de arquivos secretos — ConsultaFrame (opção 2)

- [ ] Registrar log **MID 7001** ao apresentar a tela de consulta
- [ ] Exibir cabeçalho + corpo 1 (mesmo da tela principal) + corpo 2 com: total de consultas do usuário, campo "Caminho da pasta" (255 chars), campo "Frase secreta" (255 chars) e Botão Listar
- [ ] Ao pressionar Botão Listar: registrar **MID 7003**
- [ ] Validar caminho da pasta; registrar **MID 7004** se inválido e notificar usuário
- [ ] Validar a frase secreta da mesma forma que no cadastro (decriptar chave + assinar array de 9216 bytes)
- [ ] Recuperar semente do envelope digital: decriptar `index.env` com a **chave privada do administrador** (em memória) usando a classe `Cipher`
- [ ] Decriptar `index.enc` com AES/ECB/PKCS5Padding usando chave AES derivada da semente via SHA1PRNG; registrar **MID 7005** se sucesso ou **MID 7007** se falha
- [ ] Verificar integridade e autenticidade do índice usando `index.asd` (assinatura digital) com a **chave pública do administrador** e a classe `Signature`; registrar **MID 7006** se sucesso ou **MID 7008** se falha
- [ ] Ler linhas do índice decriptado no formato: `NOME_CODIGO NOME_SECRETO DONO GRUPO\n`
- [ ] Exibir na lista **apenas** os arquivos cujo DONO ou GRUPO corresponda ao usuário logado; registrar **MID 7009**
- [ ] Ao selecionar um arquivo: registrar **MID 7010** com nome do arquivo
- [ ] **Política de acesso: o usuário só pode acessar o arquivo se for o DONO**; se não for dono: registrar **MID 7012** e notificar; se for dono: registrar **MID 7011**
- [ ] Se acesso permitido: decriptar `NOME_CODIGO.env` com chave privada do usuário para obter semente; derivar chave AES via SHA1PRNG; decriptar `NOME_CODIGO.enc` (AES/ECB/PKCS5Padding); registrar **MID 7013** se sucesso ou **MID 7015** se falha
- [ ] Verificar assinatura digital em `NOME_CODIGO.asd` com chave pública do usuário dono; registrar **MID 7014** se sucesso ou **MID 7016** se falha
- [ ] Gravar arquivo decriptado com o **NOME_SECRETO_DO_ARQUIVO** (não o nome código) em subpasta `decrypted/` dentro da pasta informada
- [ ] Botão Voltar: registrar **MID 7002** e retornar ao MenuFrame

---

## 9. Tela de saída — SaidaFrame (opção 3)

- [ ] Registrar log **MID 8001** ao apresentar a tela de saída
- [ ] Exibir cabeçalho + corpo 1 (total de acessos) + corpo 2 com mensagem: *"Pressione o botão Encerrar Sessão ou o botão Encerrar Sistema para confirmar."*
- [ ] Botão **Encerrar Sessão**: registrar **MID 8002** e **MID 1004** (sessão encerrada), limpar dados do usuário da memória e retornar à tela de autenticação (etapa 1)
- [ ] Botão **Encerrar Sistema**: registrar **MID 8003** e **MID 1002** (sistema encerrado), **apagar chave privada do admin da memória** e chamar `System.exit(0)`
- [ ] Botão **Voltar**: registrar **MID 8004** e retornar ao MenuFrame

---

## 10. Sistema de logs — classe Logger

- [ ] Implementar `Logger.log(int MID, Integer UID, String detalhe)` que insere registro na tabela Registros com data/hora atual
- [ ] **Não armazenar o texto das mensagens** na tabela Registros — apenas o MID (join com tabela Mensagens para exibição)
- [ ] Registrar log **MID 1001** ao iniciar o sistema
- [ ] Garantir que todos os MIDs (1001–8004) sejam chamados nos momentos corretos conforme mapeamento do enunciado
- [ ] Incluir o campo **detalhe** nos registros onde aplicável (ex: nome do arquivo em MIDs 7010–7016)

---

## 11. Programa logView (programa separado de auditoria)

- [ ] **Programa separado** — não pode fazer parte do CofreDigital principal
- [ ] Receber caminho do arquivo da chave privada do administrador na **linha de comando**
- [ ] Solicitar frase secreta da chave privada **via teclado real sem echo**
- [ ] Validar par chave privada + certificado: assinar array aleatório de **2048 bytes** (atenção: diferente dos 9216 bytes do CofreDigital)
- [ ] Se validação **negativa**: encerrar o programa
- [ ] Se validação **positiva**: conectar ao banco de dados
- [ ] Exibir todos os registros em **ordem cronológica** com: data/hora, texto da mensagem (join com tabela Mensagens pelo MID), UID e detalhe

---

## 12. Testes finais e qualidade de código

- [ ] Testar fluxo completo: 1ª execução → cadastro admin → autenticação → cadastro de outro usuário → acesso ao cofre → logs → logView
- [ ] Testar bloqueio por senha errada (3 erros consecutivos → bloqueio 2 min)
- [ ] Testar bloqueio por token inválido (3 erros consecutivos → bloqueio 2 min)
- [ ] Testar frase secreta inválida na partida do sistema (2ª execução): sistema deve encerrar
- [ ] Testar acesso negado a arquivo (usuário tentando acessar arquivo de outro dono)
- [ ] Testar comportamento após cadastro com sucesso (formulário vazio) e com falha (formulário preenchido)
- [ ] Verificar que o texto das mensagens NÃO está armazenado na tabela Registros
- [ ] Compilar com **JDK 1.8** (`javac`) e executar na linha de comando
- [ ] Testar com diferentes bancos (MariaDB e/ou MySQL): alterar `.properties` e `pom.xml`
- [ ] Remover `System.out.println` desnecessários (manter apenas logs via Logger)
- [ ] Revisar comentários, identação e formatação do código

---

## 13. Entrega

- [ ] Confirmar que todos os arquivos `.java` estão versionados no repositório (nenhum faltando)
- [ ] Verificar que não há caminhos absolutos ou credenciais no código
- [ ] Fazer commit final e push para o repositório
- [ ] Submeter **apenas os arquivos `.java`** (não zipados) no EAD da PUC-Rio
- [ ] **Cada integrante** do grupo deve fazer sua submissão individualmente
- [ ] Prazo: **17/05/2026 às 23:59h**