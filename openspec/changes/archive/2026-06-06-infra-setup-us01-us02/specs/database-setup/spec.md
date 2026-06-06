## ADDED Requirements

### Requirement: Conexão com PostgreSQL configurada via profiles
A aplicação SHALL se conectar ao PostgreSQL usando configuração externalizada em profiles Spring. O profile `dev` SHALL apontar para `localhost:5432/pactum` com usuário `pactum` e senha `pactum`. O profile padrão SHALL utilizar `ddl-auto=validate` e o profile `dev` SHALL utilizar `ddl-auto=create-drop`.

#### Scenario: Aplicação sobe com profile dev e banco disponível
- **WHEN** a aplicação é iniciada com `spring.profiles.active=dev` e o PostgreSQL está acessível em `localhost:5432`
- **THEN** a aplicação conecta ao banco sem erros e o contexto Spring é carregado com sucesso

#### Scenario: Aplicação falha ao subir sem banco disponível
- **WHEN** a aplicação é iniciada e o PostgreSQL não está acessível
- **THEN** a aplicação falha no startup com erro de conexão descritivo (não sobe em estado degradado)

### Requirement: Docker Compose com PostgreSQL 16 para desenvolvimento local
O repositório SHALL conter um `docker-compose.yml` na raiz que provisione um contêiner PostgreSQL 16 com banco `pactum`, usuário `pactum` e senha `pactum` na porta `5432`.

#### Scenario: Ambiente local provisionado via docker-compose
- **WHEN** o desenvolvedor executa `docker compose up -d`
- **THEN** um contêiner PostgreSQL 16 é iniciado e acessível em `localhost:5432` com as credenciais do profile dev

### Requirement: Health check do banco via Actuator
A aplicação SHALL expor o endpoint `GET /actuator/health` que retorna status `UP` quando o banco está acessível. O endpoint SHALL incluir detalhes do componente `db`.

#### Scenario: Health check retorna UP com banco disponível
- **WHEN** `GET /actuator/health` é chamado e o banco está acessível
- **THEN** a resposta é `200 OK` com corpo `{ "status": "UP", "components": { "db": { "status": "UP" } } }`

#### Scenario: Health check retorna DOWN sem banco disponível
- **WHEN** `GET /actuator/health` é chamado e o banco não está acessível
- **THEN** a resposta é `503 Service Unavailable` com `"status": "DOWN"`
