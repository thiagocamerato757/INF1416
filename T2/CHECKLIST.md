# DigestCalculator – Tarefas para a dupla

## 1. entrada, leitura, hash

- [X] **1.1** – Validar os 3 argumentos; se faltar, mostrar ajuda e sair.
- [X] **1.2** – Verificar se `Tipo_Digest` é MD5, SHA1, SHA256 ou SHA512.
- [X] **1.3** – Confirmar que `Caminho_da_Pasta` existe e é uma pasta.
- [X] **1.4** – Listar todos os arquivos da pasta (ignorar subpastas).
- [X] **1.7** – Se o arquivo XML não existir, criar um novo documento com raiz `<CATALOG>`.
- [X] **1.8** – Ler o XML (se existir) e extrair um mapa: nome_arquivo → (tipo → hash).
- [ ] **1.9** – Tratar erros: algoritmo inválido, problemas de leitura do XML, e fechar recursos (try-with-resources).

## 2. comparação, status, XML de saída

- [ ] **2.1** – Construir um mapa invertido (hash → lista de nomes) usando:
    - os hashes calculados da pasta (apenas o tipo atual)
    - os hashes do XML (apenas o tipo atual)
- [X] **2.2** – Para cada arquivo da pasta, decidir o status:
    - `COLISION` se o hash for igual ao de outro arquivo (nome diferente).
    - Senão, se o nome existe no XML e o hash confere → `OK`
    - Senão, se o nome existe no XML mas hash diferente → `NOT OK`
    - Senão → `NOT FOUND`
- [X] **2.3** – Imprimir uma linha por arquivo: `nome tipo hash (STATUS)` (um espaço entre campos).
- [X] **2.4** – Para cada arquivo com status `NOT FOUND`:
    - Localizar ou criar a entrada `<FILE_ENTRY>` com o nome do arquivo.
    - Adicionar ou atualizar o `<DIGEST_ENTRY>` com o tipo e hash calculado.
- [X] **2.5** – Salvar o XML atualizado no mesmo caminho (com indentação de 2 espaços).

## Lembrete para entrega

- [ ] **Entrega** – Comentar início do arquivo com nomes e matrículas; compilar com JDK 1.8; submeter **apenas o `.java`** no EAD (cada um por si).