    # 💸 NexusBank

    Sistema bancário digital desenvolvido em Java 21 com interface web moderna.

    ![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)
    ![SQLite](https://img.shields.io/badge/SQLite-3.50-blue?style=flat-square&logo=sqlite)
    ![JDBC](https://img.shields.io/badge/JDBC-nativo-green?style=flat-square)
    ![HTML](https://img.shields.io/badge/Frontend-HTML%2FCSS%2FJS-purple?style=flat-square)

    ---

    ## Sobre o projeto

    O NexusBank é um sistema bancário completo desenvolvido do zero em Java puro, sem frameworks. O projeto conta com uma API REST feita com o `HttpServer` nativo do JDK e uma interface web moderna com design inspirado em bancos digitais.

    ---

    ## Funcionalidades

    - Criar conta bancária
    - Login com autenticação por senha
    - Consultar saldo
    - Realizar depósitos
    - Realizar saques (com validação de saldo mínimo)
    - Transferências entre contas (com ID único por transferência)
    - Extrato completo de movimentações
    - Alterar nome e senha
    - Excluir conta
    - Persistência de dados com SQLite

    ---

    ## 📸 Screenshots

### Login
![Login](C:\Users\Bruno\Downloads\LoginBank.png)

### Dashboard
![Dashboard](C:\Users\Bruno\Downloads\TelaInicial.png)

### Info-Conta
![Dashboard](C:\Users\Bruno\Downloads\InfoContas.png)

### Extrato
![Extrato](C:\Users\Bruno\Downloads\Extrato.png)

### Saque
![Extrato](C:\Users\Bruno\Downloads\Saque.png)

### Depósito
![Extrato](C:\Users\Bruno\Downloads\Deposito.png)

### Transferências
![Extrato](C:\Users\Bruno\Downloads\Transferencia.png)

    ---

    ## Tecnologias utilizadas

    | Tecnologia | Uso |
    |---|---|
    | Java 21 | Linguagem principal, lógica de negócio |
    | JDBC | Conexão com banco de dados |
    | SQLite | Banco de dados relacional |
    | HttpServer (JDK) | Servidor REST nativo, sem frameworks |
    | HTML / CSS / JS | Interface web responsiva |

    ---

    ## Arquitetura

    ```
    NexusBank/
    ├── src/
    │   ├── Banco.java           # Regras de negócio (depósito, saque, transferência...)
    │   ├── ContaBancaria.java   # Modelo de conta com histórico de transações
    │   ├── Database.java        # Camada de acesso ao banco de dados (JDBC)
    │   ├── Transacao.java       # Modelo de transação
    │   ├── Main.java            # Interface via terminal (versão console)
    │   └── ApiServer.java       # Servidor REST HTTP (interface web)
    ├── lib/
    │   └── sqlite-jdbc-3.50.3.0.jar
    ├── banco.db                 # Banco de dados SQLite
    └── index.html               # Interface web
    ```

    ### Fluxo de uma operação

    ```
    Navegador (index.html)
        │
        │  HTTP POST /api/deposito
        ▼
    ApiServer.java  →  banco.realizarDeposito()  →  ContaBancaria.depositar()
                                                            │
                                                            ▼
                                                Database.salvaConta()
                                                Database.salvarTransacao()
                                                            │
                                                            ▼
                                                        banco.db
    ```

    ---

    ## Como executar

    ### Pré-requisitos

    - Java 21 ou superior instalado
    - Download do [sqlite-jdbc-3.50.3.0.jar](https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.50.3.0/sqlite-jdbc-3.50.3.0.jar) na pasta `lib/`

    ### 1. Compilar

    ```bash
    # Linux / macOS
    javac -cp "lib/*:src" src/ApiServer.java src/Banco.java src/ContaBancaria.java src/Database.java src/Transacao.java -d out

    # Windows
    javac -cp "lib\sqlite-jdbc-3.50.3.0.jar;src" src\ApiServer.java src\Banco.java src\ContaBancaria.java src\Database.java src\Transacao.java -d out
    ```

    ### 2. Executar o servidor

    ```bash
    # Linux / macOS
    java -cp "lib/*:out" ApiServer

    # Windows
    java -cp "lib\sqlite-jdbc-3.50.3.0.jar;out" ApiServer
    ```

    ### 3. Abrir a interface

    Abra o arquivo `index.html` no navegador. O servidor precisa estar rodando.

    > Versão terminal (original): `java -cp "lib\sqlite-jdbc-3.50.3.0.jar;out" Main`

    ---

    ## Banco de dados

    O sistema utiliza SQLite com duas tabelas:

    ```sql
    CREATE TABLE contas (
        numeroConta INTEGER PRIMARY KEY,
        nome        TEXT    NOT NULL,
        senha       TEXT    NOT NULL,
        saldo       REAL    NOT NULL
    );

    CREATE TABLE transacoes (
        id               INTEGER PRIMARY KEY AUTOINCREMENT,
        numeroConta      INTEGER NOT NULL,
        tipo             TEXT    NOT NULL,
        valor            REAL    NOT NULL,
        data             TEXT    NOT NULL,
        idTransferencia  INTEGER,
        FOREIGN KEY(numeroConta) REFERENCES contas(numeroConta)
    );
    ```

    ---

    ## Endpoints da API

    | Método | Endpoint | Descrição |
    |---|---|---|
    | POST | `/api/login` | Autenticar na conta |
    | POST | `/api/cadastro` | Criar nova conta |
    | POST | `/api/logout` | Encerrar sessão |
    | GET | `/api/conta` | Dados e saldo da conta |
    | GET | `/api/extrato` | Histórico de transações |
    | POST | `/api/deposito` | Realizar depósito |
    | POST | `/api/saque` | Realizar saque |
    | POST | `/api/transferencia` | Transferir entre contas |
    | PUT | `/api/perfil/nome` | Alterar nome |
    | PUT | `/api/perfil/senha` | Alterar senha |
    | DELETE | `/api/conta` | Excluir conta |

    ---

    ## 👨‍💻 Autor

    Desenvolvido por **Bruno da Silva Chagas**

    [![GitHub](https://img.shields.io/badge/GitHub-seu--usuario-black?style=flat-square&logo=github)](https://github.com/seu-usuario)
    [![LinkedIn](https://img.shields.io/badge/LinkedIn-seu--perfil-blue?style=flat-square&logo=linkedin)](https://linkedin.com/in/seu-perfil)

    ---

    ## Licença

    Este projeto está sob a licença MIT.