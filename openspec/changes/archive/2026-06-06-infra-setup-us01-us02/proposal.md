## Why

A aplicação Pactum API precisa de infraestrutura base funcional antes que qualquer feature de negócio possa ser desenvolvida: conexão com PostgreSQL configurada e documentação interativa via Swagger UI disponível para os demais endpoints.

## What Changes

- Configuração do datasource PostgreSQL com profiles `default` e `dev`
- `docker-compose.yml` com serviço PostgreSQL 16 para desenvolvimento local
- Integração do SpringDoc OpenAPI 3 com Swagger UI acessível em `/swagger-ui.html`
- Health check do banco via Spring Actuator em `/actuator/health`
- Configuração base de `GlobalExceptionHandler` para respostas de erro padronizadas

## Capabilities

### New Capabilities

- `database-setup`: Conexão com PostgreSQL, profile de desenvolvimento, docker-compose, validação de schema via DDL auto
- `openapi-docs`: Swagger UI e spec OpenAPI 3 disponíveis, controllers anotados com `@Tag` e respostas com `@ApiResponse`

### Modified Capabilities

<!-- Nenhuma — este é o setup inicial, não há specs existentes para modificar -->

## Impact

- `pom.xml`: adição de dependências `springdoc-openapi-starter-webmvc-ui` e `spring-boot-starter-actuator`
- `src/main/resources/application.properties`: configurações base do datasource e JPA
- `src/main/resources/application-dev.properties`: datasource local (localhost:5432/pactum)
- `docker-compose.yml`: novo arquivo na raiz do projeto
- `config/`: `OpenApiConfig` com metadados da API
- `adapter/in/web/`: `GlobalExceptionHandler` com `@ControllerAdvice`
