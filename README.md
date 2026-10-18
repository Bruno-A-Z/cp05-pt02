# Amazin — Mercado Express | CP5 (Parte 1: Spring Security MVC · Parte 2: Microsserviços com RabbitMQ)

Aplicação Web desenvolvida em **Java + Spring Boot + Maven**, com **Thymeleaf**, **Spring Data JPA/Hibernate**, **PostgreSQL**, **Lombok** e **Spring Security**.

O CP5 aproveita o domínio de mercado express do CP anterior e acrescenta **autenticação com login e cadastro (Sign Up) próprios**, **dois perfis de usuário (ADMIN e USER)**, **banco PostgreSQL** e **deploy em produção**.

> **Parte 2 (Microsserviços, RabbitMQ, Banco Tranquilo, e-mail e dashboard):** veja a seção [Parte 2](#parte-2--mercado-express--rabbitmq--banco-tranquilo) no final deste README.

## Integrantes

| Nome | RM |
|---|---|
| Bruno A Zanateli | RM563736 |
| Christian S Freitas | RM566098 |
| Rodrigo Tiezzi | RM562975 |
| Pedro P Biasolli | RM562521 |
| Matheus Souza | RM562532 |

## Links

- **GitHub:** [https://github.com/SEU-USUARIO/SEU-REPOSITORIO](https://github.com/Bruno-A-Z/cp05)
- **Deploy:** https://SEU-APP.onrender.com
- **Sistema utilizado no deploy:** Render (Web Service com Docker + PostgreSQL do Render)
- **IDE utilizada:** IntelliJ IDEA

## Requisitos do enunciado atendidos

- Spring Framework MVC configurado com Maven e Java 21.
- Dependências: Spring Web, Thymeleaf, Spring Security, Spring Data JPA, Validation, HATEOAS, Lombok, PostgreSQL Driver e DevTools.
- Banco **PostgreSQL**, configurado em `application.yml`.
- Tabela de produtos `TDS_Sec_MVC_TB_Mercado` e tabela de usuários `TDS_Users_Mercado`.
- CRUD completo (Create, Read, Update, Delete) com links e botões na interface Web.
- Tela de **login personalizada**, diferente da fornecida pelo Spring Security.
- Tela de **Sign Up (cadastro)** que salva o perfil na tabela `TDS_Users_Mercado`.
- Após o login, redirecionamento para uma tela de acesso restrito conforme o perfil.
- **Dois tipos de usuário** (`ADMIN` e `USER`) com acessos diferentes.
- Página base (landing page) em `index.html`.
- Deploy realizado e link de produção disponibilizado neste README.

## Tecnologias

- Java 21
- Spring Boot 3.3.3
- Maven
- Spring Web MVC
- Spring Security (BCrypt)
- Spring Data JPA / Hibernate
- PostgreSQL
- Lombok
- Thymeleaf
- Bean Validation
- Spring HATEOAS (mantido do CP anterior)
- Springdoc OpenAPI / Swagger
- Bootstrap 5
- Docker + Render

## Spring Initializr

![Spring Initializr](docs/screenshots/00-initializr.png)

## Modelo de dados

### Produtos — tabela `TDS_Sec_MVC_TB_Mercado`

Entidade `Item`.

| Coluna | Campo Java | Tipo |
|---|---|---|
| `ID` | `id` | Long |
| `NAME_ITEM` | `name` | String |
| `KIND_ITEM` | `kind` | String |
| `ITEM_SECTOR` | `sector` | Enum `Sector` |
| `ITEM_SIZE` | `size` | Enum `Size` |
| `ITEM_PRICE` | `price` | Double |

Setores: `KITCHEN`, `CLEANING`, `SHIRT`, `SOCKS`, `PANTS`.
Tamanhos: `BIG`, `MEDIUM`, `LARGE`.

### Usuários — tabela `TDS_Users_Mercado`

Entidade `Usuario`.

| Coluna | Campo Java | Tipo |
|---|---|---|
| `ID` | `id` | Long |
| `NOME` | `nome` | String |
| `USERNAME` | `username` | String (único) |
| `EMAIL` | `email` | String (único) |
| `SENHA` | `senha` | String (criptografada com BCrypt) |
| `PERFIL` | `role` | Enum `Role` (`ADMIN` ou `USER`) |

> O PostgreSQL grava os nomes das tabelas em minúsculas (`tds_sec_mvc_tb_mercado` e `tds_users_mercado`). As tabelas são criadas automaticamente pelo Hibernate.

## Estrutura principal

```text
src/main/java/com/java/cp04/Amazin
├── assembler
│   └── ItemModelAssembler.java
├── config
│   ├── DataInitializer.java
│   ├── SecurityConfig.java
│   └── SwaggerConfig.java
├── controller
│   └── ItemController.java
├── dto
│   └── CadastroForm.java
├── enumerator
│   ├── Role.java
│   ├── Sector.java
│   └── Size.java
├── model
│   ├── Item.java
│   └── Usuario.java
├── repository
│   ├── ItemRepository.java
│   └── UsuarioRepository.java
├── service
│   ├── ItemService.java
│   ├── UsuarioDetailsService.java
│   └── UsuarioService.java
└── web
    ├── AuthController.java
    ├── GlobalModelAttributes.java
    ├── HomeController.java
    └── ItemViewController.java
```

Templates Thymeleaf:

```text
src/main/resources/templates
├── fragments/layout.html
├── itens/detalhes.html
├── itens/form.html
├── itens/lista.html
├── acesso-negado.html
├── admin.html
├── cadastro.html
├── cliente.html
├── index.html
└── login.html
```

## Configuração do banco (PostgreSQL)

Por segurança, as credenciais **não ficam gravadas no GitHub**. O arquivo `application.yml` lê variáveis de ambiente, com valores padrão para uso local:

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/mercadoexpress}
    username: ${DB_USER:postgres}
    password: ${DB_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: ${DDL_AUTO:update}
```

### Criando o banco local

```sql
CREATE DATABASE mercadoexpress;
```

### Variáveis de ambiente

#### Windows PowerShell

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/mercadoexpress"
$env:DB_USER="postgres"
$env:DB_PASSWORD="SUA_SENHA"
$env:ADMIN_USERNAME="admin"
$env:ADMIN_PASSWORD="admin123"
```

#### Linux/macOS

```bash
export DB_URL="jdbc:postgresql://localhost:5432/mercadoexpress"
export DB_USER="postgres"
export DB_PASSWORD="SUA_SENHA"
export ADMIN_USERNAME="admin"
export ADMIN_PASSWORD="admin123"
```

### Por que `update`?

O modo `update` cria as tabelas na primeira execução e preserva os dados nas seguintes. Não é recomendado usar `create-drop` em produção, pois apagaria os dados a cada reinício.

### Dados iniciais

Na primeira execução, a classe `DataInitializer` cria automaticamente:

- o usuário **ADMIN** (definido por `ADMIN_USERNAME` e `ADMIN_PASSWORD`);
- três produtos de exemplo, caso a tabela de produtos esteja vazia.

## Executando localmente

```bash
mvn spring-boot:run
```

Ou, no Windows:

```powershell
mvnw.cmd spring-boot:run
```

A aplicação ficará disponível em:

- Landing page: `http://localhost:8082/`
- Login: `http://localhost:8082/req/login`
- Cadastro: `http://localhost:8082/req/cadastro`
- Produtos: `http://localhost:8082/web/itens`
- Swagger: `http://localhost:8082/swagger-ui/index.html` (somente ADMIN)

## Perfis de acesso

| Perfil | O que pode acessar |
|---|---|
| **Público** (sem login) | Landing page, login e cadastro |
| **USER** | Consulta de produtos (lista e detalhes) e tela `/cliente` |
| **ADMIN** | Tudo do USER + CRUD de produtos, painel `/admin` e Swagger |

Após o login, o redirecionamento é automático:

- **ADMIN** → `/admin`
- **USER** → `/cliente`

O cadastro público (`/req/cadastro`) sempre cria usuários com perfil **USER**. O perfil **ADMIN** só existe pelo usuário criado na inicialização da aplicação.

## Segurança

A aplicação usa uma cadeia de segurança com **Spring Security**:

- Login por formulário **personalizado** (`/req/login`), com usuários carregados da tabela `TDS_Users_Mercado` pelo `UsuarioDetailsService`.
- Senhas armazenadas com **BCrypt**.
- Proteção **CSRF** ativa nos formulários Web.
- Página de **acesso negado** para quem tenta acessar uma rota sem permissão.
- **HTTP Basic** habilitado para testar a API REST em `/item/**` no Postman/Insomnia.

### Rotas públicas

```text
/
/req/login
/req/cadastro
/acesso-negado
/css/**
```

### Rotas para USER e ADMIN

```text
GET /web/itens
GET /web/itens/{id}
/cliente
GET /item/**
```

### Rotas somente ADMIN

```text
/admin
GET  /web/itens/novo
POST /web/itens
GET  /web/itens/{id}/editar
POST /web/itens/{id}/editar
POST /web/itens/{id}/deletar
POST, PUT, PATCH, DELETE /item/**
/swagger-ui/**
```

## Interface Web e CRUD

### READ — consulta de produtos (USER e ADMIN)

A página `/web/itens` lista os produtos carregados da tabela `TDS_Sec_MVC_TB_Mercado` através do `ItemRepository`.

Cada produto possui o botão **Ver**, que abre:

```text
GET /web/itens/{id}
```

Na tela de detalhes são exibidos nome, tipo, setor, tamanho e preço.

### CREATE — somente ADMIN

O botão **Novo produto** abre:

```text
GET /web/itens/novo
```

O formulário é enviado para:

```text
POST /web/itens
```

Após o cadastro, o usuário retorna para a lista e visualiza a mensagem de sucesso.

### UPDATE — somente ADMIN

O botão **Editar** abre:

```text
GET /web/itens/{id}/editar
```

O formulário é enviado para:

```text
POST /web/itens/{id}/editar
```

Os dados são atualizados pela camada `ItemService` e persistidos pelo `ItemRepository`.

### DELETE — somente ADMIN

O botão **Excluir** realiza:

```text
POST /web/itens/{id}/deletar
```

Antes do envio, a interface solicita confirmação ao usuário.

## Endpoints REST

Base URL:

```text
http://localhost:8082/item
```

| Método | Endpoint | Acesso |
|---|---|---|
| GET | `/item` | USER ou ADMIN |
| GET | `/item/{id}` | USER ou ADMIN |
| POST | `/item` | ADMIN |
| PUT | `/item/{id}` | ADMIN |
| PATCH | `/item/{id}` | ADMIN |
| DELETE | `/item/{id}` | ADMIN |

As chamadas exigem **HTTP Basic** com usuário e senha cadastrados.

Exemplo de `POST /item`:

```json
{
  "name": "Sabão líquido",
  "kind": "Limpeza",
  "sector": "CLEANING",
  "size": "LARGE",
  "price": 18.90
}
```

## Telas e demonstração

### 00 — Landing page (`index.html`)

<img width="2557" height="1270" alt="image" src="https://github.com/user-attachments/assets/a9b5297d-ffe3-445c-af2f-1b783c65e991" />


Página base da aplicação, com acesso a **Entrar** e **Criar conta**.

### 01 — Cadastro (Sign Up)

<img width="2556" height="1268" alt="image" src="https://github.com/user-attachments/assets/4e7dac83-f2bf-476b-9ee0-9acf33195bcb" />


Formulário com validação (nome, usuário, e-mail e senha). O usuário é salvo na tabela `TDS_Users_Mercado` com perfil `USER` e senha criptografada.

### 02 — Login personalizado

<img width="2555" height="1270" alt="image" src="https://github.com/user-attachments/assets/a3efe0d0-be88-4b56-9d6d-d8ea8dd6685e" />


Tela de login própria, diferente da fornecida pelo Spring Security. Quem não possui conta é direcionado ao cadastro.

### 03 — Área restrita do USER

<img width="2557" height="1267" alt="image" src="https://github.com/user-attachments/assets/4e4b1ef5-cd3e-4b6b-91b2-0153463b7f66" />


Após o login com perfil `USER`, o redirecionamento é para `/cliente`.

### 04 — Painel do ADMIN

<img width="2559" height="1272" alt="image" src="https://github.com/user-attachments/assets/3eab229a-56a0-4ad1-b219-a56672ab8558" />


Após o login com perfil `ADMIN`, o redirecionamento é para `/admin`.

### 05 — READ: lista de produtos

<img width="2557" height="1269" alt="image" src="https://github.com/user-attachments/assets/11c08836-e68e-4832-95a2-39b9d8284ae4" />


Produtos carregados da tabela `TDS_Sec_MVC_TB_Mercado`. O `USER` vê apenas o botão **Ver**; o `ADMIN` também vê **Editar** e **Excluir**.

### 06 — CREATE: novo produto

<img width="2557" height="1270" alt="image" src="https://github.com/user-attachments/assets/71b22a22-2db8-48fb-aec2-a1caa9aafcbf" />


### 07 — UPDATE: editar produto

<img width="2557" height="1266" alt="image" src="https://github.com/user-attachments/assets/7a9e9827-b47d-486f-9b7b-c3e499371bc1" />


### 08 — DELETE: excluir produto

<img width="2539" height="1267" alt="image" src="https://github.com/user-attachments/assets/1cbff32b-d3fc-4c04-a4d0-9d3dd322cc3e" />


### 09 — Acesso negado

<img width="2557" height="1263" alt="image" src="https://github.com/user-attachments/assets/6edde8a3-fdfa-424a-85a9-a14a30275c87" />


Usuário com perfil `USER` tentando acessar uma rota exclusiva do `ADMIN` (`/web/itens/novo`).

### 10 — Banco de dados

(<img width="875" height="150" alt="image" src="https://github.com/user-attachments/assets/10e8364d-a528-489e-a49d-af27509b1839" />

Tabelas `tds_sec_mvc_tb_mercado` e `tds_users_mercado` no pgAdmin.

## Deploy

**Link de produção:** https://SEU-APP.onrender.com

---

# Parte 2 — Mercado Express + RabbitMQ + Banco Tranquilo

O Mercado Express agora tem **carrinho de compras** e cobra o cliente por meio de uma integração assíncrona com a **API do Banco Tranquilo**, usando **RabbitMQ** como broker. Se o banco confirmar o pagamento, o cliente recebe um **e-mail de confirmação**, o envio fica registrado no **PostgreSQL** e as vendas aparecem no **dashboard da gerência**.

## Arquitetura

```text
 Cliente (navegador / Postman)
        │  Finalizar compra
        ▼
 ┌──────────────────┐  1. cria Pedido PENDENTE + baixa estoque (PostgreSQL)
 │ Mercado Express  │  2. publica PagamentoRequisicao
 └──────────────────┘
        │ routing key: pagamento.requisicao
        ▼
 ┌─────────────────────────────  RabbitMQ  ─────────────────────────────┐
 │  mercado.pagamentos.exchange (direct)                                │
 │     ├─ pagamento.requisicao.queue ──► (falha ► pagamento.requisicao.dlq)
 │     └─ pagamento.resposta.queue   ──► (falha ► pagamento.resposta.dlq)
 └──────────────────────────────────────────────────────────────────────┘
        │                                         ▲
        ▼ consome requisição                      │ publica PagamentoResposta
 ┌──────────────────────┐  POST /banco-tranquilo/api/pagamentos  (X-API-KEY)
 │ Banco Tranquilo      │──────────────────────────────► API do Banco (autoriza/recusa)
 └──────────────────────┘
        │ resposta consumida pelo Mercado Express
        ▼
 Pedido APROVADO ──► e-mail de confirmação ──► registro em TDS_Email_Log_Mercado
 Pedido RECUSADO/ERRO ──► estoque devolvido
```

| Peça | Classe(s) | Papel |
|---|---|---|
| Topologia AMQP | `config/RabbitConfig` | Exchange, filas, bindings e DLQs; mensagens em JSON |
| Publicação | `messaging/PagamentoPublisher` | `RabbitTemplate.convertAndSend` |
| Lado Banco | `banco/BancoTranquiloListener`, `BancoTranquiloClient`, `BancoTranquiloApiController`, `BancoTranquiloService` | Consome a fila, chama a API do banco e devolve a resposta |
| Lado Mercado | `messaging/PagamentoRespostaListener`, `service/PedidoService`, `service/CompraService` | Cria pedidos, aplica a resposta, controla estoque |
| E-mail | `service/EmailService` | Envia via SMTP e grava em `TDS_Email_Log_Mercado` |
| Dashboard | `dashboard/DashboardService`, `templates/dashboard.html` | KPIs e gráficos (Chart.js) |

> A API do Banco Tranquilo roda no mesmo projeto, em `/banco-tranquilo/api`, para facilitar a demonstração, mas é consumida **via HTTP** (`BancoTranquiloClient`). Para usar um banco externo, basta definir `BANCO_API_URL` e `BANCO_API_KEY`.

### Regras de autorização do Banco Tranquilo (simulação)

| Cartão / valor | Resultado |
|---|---|
| Final `0000` | RECUSADO — saldo insuficiente |
| Final `1111` | RECUSADO — cartão bloqueado |
| Valor acima de R$ 5.000 por transação | RECUSADO — limite excedido |
| Número com menos de 13 ou mais de 19 dígitos | RECUSADO — cartão inválido |
| Qualquer outro | **APROVADO** (código `AUT-XXXXXXXX`) |

O número completo do cartão trafega apenas na mensageria e **nunca é gravado**: o pedido guarda só os 4 últimos dígitos.

## Novas tabelas

| Tabela | Entidade | Conteúdo |
|---|---|---|
| `TDS_Sec_MVC_TB_Mercado` | `Item` | Agora com `ITEM_STOCK` (estoque) e `ITEM_COST` (custo, usado no lucro) |
| `TDS_Pedidos_Mercado` | `Pedido` | Cliente, total, custo, status (`PENDENTE`, `APROVADO`, `RECUSADO`, `ERRO`), `TRANSACTION_ID`, código de autorização, final do cartão |
| `TDS_Itens_Pedido_Mercado` | `ItemPedido` | Itens do pedido (nome, setor, quantidade, preço e custo no momento da compra) |
| `TDS_Email_Log_Mercado` | `EmailLog` | Destinatário, assunto, corpo, status (`ENVIADO`, `SIMULADO`, `FALHA`), erro, data |

O Hibernate (`ddl-auto: update`) cria as tabelas e adiciona as novas colunas de `Item`; produtos antigos recebem estoque 50 e custo de 60% do preço na inicialização.

## Novas telas e rotas

| Rota | Acesso | Descrição |
|---|---|---|
| `/web/itens` | USER, ADMIN | Lista com estoque e botão **Adicionar** ao carrinho |
| `/carrinho` | USER, ADMIN | Carrinho + formulário de pagamento (cartão) |
| `/pedidos`, `/pedidos/{id}` | USER, ADMIN | Pedidos do usuário; a tela do pedido atualiza sozinha enquanto está `PENDENTE` |
| `/admin/pedidos` | ADMIN | Todas as compras |
| `/admin/emails` | ADMIN | Log de e-mails enviados |
| `/admin/dashboard` | ADMIN | Dashboard gerencial |
| `POST /api/compras` | USER, ADMIN (HTTP Basic) | Compra via JSON (Postman/Insomnia) |
| `GET /api/compras/{id}` | dono do pedido ou ADMIN | Status do pedido |
| `GET /admin/dashboard/dados` | ADMIN | JSON do dashboard |
| `POST /banco-tranquilo/api/pagamentos` | cabeçalho `X-API-KEY` | API do Banco Tranquilo |

## Dashboard da gerência

Em `/admin/dashboard` (atualiza a cada 5 s): receita, lucro, ticket médio, pedidos aprovados/pendentes/recusados, itens vendidos, e-mails registrados e gráficos de **vendas e lucro por dia**, **status dos pedidos**, **estoque por produto**, **receita por setor** e **produtos mais vendidos**, além de alerta de estoque baixo (< 10 un.) e a lista das últimas compras.

## Configuração

Todas as opções são variáveis de ambiente (valores padrão em `application.yml`):

| Variável | Padrão | Uso |
|---|---|---|
| `RABBITMQ_HOST` / `RABBITMQ_PORT` | `localhost` / `5672` | Broker |
| `RABBITMQ_USER` / `RABBITMQ_PASSWORD` | `guest` / `guest` | Credenciais |
| `RABBITMQ_VHOST` / `RABBITMQ_SSL` | `/` / `false` | Para CloudAMQP/LavinMQ use o vhost do serviço e `RABBITMQ_SSL=true` com porta `5671` |
| `MAIL_ENABLED` | `false` | `true` envia por SMTP; `false` só registra o e-mail no banco (`SIMULADO`) |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USER`, `MAIL_PASSWORD`, `MAIL_AUTH`, `MAIL_STARTTLS`, `MAIL_FROM` | Mailpit local | SMTP. Exemplo Gmail: `smtp.gmail.com`, `587`, `MAIL_AUTH=true`, `MAIL_STARTTLS=true` e **senha de app** |
| `BANCO_API_URL` / `BANCO_API_KEY` | API do próprio projeto / `banco-tranquilo-key` | Banco Tranquilo |
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | PostgreSQL local | Banco de dados (como na Parte 1) |

## Executando (passo a passo)

```bash
# 1) Infraestrutura: PostgreSQL + RabbitMQ + Mailpit (SMTP de teste)
docker compose up -d

# 2) Aplicação (use as variáveis abaixo para enviar e-mail ao Mailpit)
export DB_PASSWORD=postgres MAIL_ENABLED=true MAIL_HOST=localhost MAIL_PORT=1025
mvn spring-boot:run
```

- Aplicação: http://localhost:8082 (login `admin` / `admin123`, ou crie uma conta em **Criar conta**)
- **Dashboard do RabbitMQ:** http://localhost:15672 (`guest` / `guest`) → abas *Exchanges*, *Queues* e *Get messages*
- **Caixa de e-mail (Mailpit):** http://localhost:8025

## Roteiro de teste (e do vídeo)

1. Abra o **RabbitMQ Management** e mostre a exchange `mercado.pagamentos.exchange` e as quatro filas (duas principais e duas DLQs).
2. Entre como **USER**, adicione produtos ao carrinho em `/web/itens` e finalize em `/carrinho` com o cartão `4111 1111 1111 2222` → pedido `PENDENTE` e, em seguida, `APROVADO`.
3. Mostre no Mailpit (ou em `/admin/emails`) o e-mail de confirmação e o registro no banco.
4. Repita com final `0000` e `1111` → pedidos `RECUSADO`, estoque devolvido, **nenhum e-mail**.
5. **Teste interno do RabbitMQ:** em *Queues → pagamento.requisicao.queue → Publish message*, publique manualmente um JSON de requisição (exemplo abaixo) e veja a resposta chegar em `pagamento.resposta.queue` / o pedido mudar de status. Para ver as mensagens, desligue temporariamente os consumidores (`RABBITMQ_LISTENERS=false`) e use *Get messages*.
6. **Postman/Insomnia:** importe `docs/postman/Mercado-Express-CP5.postman_collection.json` (ou use `docs/postman/compra-exemplo.json`).
7. Abra `/admin/dashboard` como ADMIN e mostre vendas, lucro e estoque.

Mensagem de requisição para publicar manualmente (o `pedidoId`/`transactionId` precisam existir como pedido `PENDENTE`; faça uma compra com os consumidores desligados para obtê-los em `GET /api/compras/{id}`):

```json
{
  "transactionId": "COLE-O-TRANSACTION-ID-DO-PEDIDO",
  "pedidoId": 1,
  "clienteNome": "Cliente Teste",
  "clienteEmail": "cliente@example.com",
  "valor": 45.80,
  "numeroCartao": "4111111111112222",
  "nomeTitular": "Cliente Teste"
}
```

## Exemplo de requisição de compra (Postman / Insomnia)

`POST http://localhost:8082/api/compras` — Authorization: **Basic Auth** (`admin` / `admin123` ou um usuário cadastrado) — `Content-Type: application/json`

```json
{
  "itens": [
    { "itemId": 1, "quantidade": 2 },
    { "itemId": 3, "quantidade": 1 }
  ],
  "cartao": {
    "numero": "4111 1111 1111 2222",
    "titular": "Cliente Teste",
    "validade": "12/30"
  }
}
```

Resposta `202 Accepted` (o pagamento é processado de forma assíncrona):

```json
{
  "pedidoId": 1,
  "transactionId": "0b0c5f7e-....",
  "status": "PENDENTE",
  "total": 29.79,
  "itens": [ { "nome": "Sabão líquido", "quantidade": 2, "precoUnitario": 12.90, "subtotal": 25.80 }, ... ]
}
```

Depois consulte `GET /api/compras/1` e veja o `status` mudar para `APROVADO` (com `codigoAutorizacao`) ou `RECUSADO` (com `motivo`). Erros: `422` (carrinho vazio, estoque insuficiente ou broker indisponível), `400` (JSON inválido) e `404` (pedido inexistente).

## Informações de configuração do BD (para o professor)

```text
Banco ........: PostgreSQL (mesmo da Parte 1)
URL ..........: jdbc:postgresql://localhost:5432/mercadoexpress   (variável DB_URL)
Usuário/Senha : variáveis DB_USER / DB_PASSWORD
DDL ..........: Hibernate ddl-auto=update (tabelas criadas automaticamente)
Tabelas ......: TDS_Sec_MVC_TB_Mercado, TDS_Users_Mercado, TDS_Pedidos_Mercado,
                TDS_Itens_Pedido_Mercado, TDS_Email_Log_Mercado
Usuário ADMIN : criado na inicialização (ADMIN_USERNAME / ADMIN_PASSWORD, padrão admin / admin123)
Broker .......: RabbitMQ  (RABBITMQ_HOST, RABBITMQ_PORT, RABBITMQ_USER, RABBITMQ_PASSWORD, RABBITMQ_VHOST)
```

## Testes automatizados

```bash
mvn test
```

- `BancoTranquiloServiceTest` — regras de autorização do banco.
- `FluxoCompraTest` — criação do pedido, baixa de estoque, publicação na fila (RabbitTemplate simulado), resposta aprovada/recusada, devolução de estoque, idempotência e registro do e-mail (H2 em memória, sem precisar de RabbitMQ).

## Deploy da Parte 2

Além do PostgreSQL, o deploy precisa de um broker AMQP em nuvem (por exemplo **CloudAMQP** ou **LavinMQ**, que têm plano gratuito) e de um SMTP. No Render, defina as variáveis `RABBITMQ_*`, `RABBITMQ_SSL=true`, `MAIL_*` e `MAIL_ENABLED=true` além das variáveis do banco da Parte 1.

## Estrutura nova (Parte 2)

```text
src/main/java/com/java/cp04/Amazin
├── banco/        BancoTranquiloApiController · Client · Listener · Service · PagamentoApiRequest/Response
├── dashboard/    DashboardService · DashboardDados
├── messaging/    PagamentoPublisher · PagamentoRespostaListener · PagamentoRequisicao · PagamentoResposta
├── exception/    CompraException
├── config/       RabbitConfig (+ SecurityConfig atualizado)
├── controller/   CompraApiController · DashboardApiController
├── service/      CompraService · PedidoService · EmailService · CarrinhoService · CarrinhoSessao
├── model/        Pedido · ItemPedido · EmailLog
└── web/          CarrinhoViewController · PedidoViewController
```
