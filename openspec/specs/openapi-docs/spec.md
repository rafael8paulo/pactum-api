# Spec: openapi-docs

## Purpose

Configuração da documentação interativa da API via SpringDoc OpenAPI, incluindo Swagger UI, exposição da spec OpenAPI 3, anotações nos controllers e padronização das respostas de erro via GlobalExceptionHandler.

## Requirements

### Requirement: Swagger UI disponível em /swagger-ui.html
A aplicação SHALL disponibilizar a interface Swagger UI acessível via `GET /swagger-ui.html`, redirecionando para a UI interativa do SpringDoc OpenAPI.

#### Scenario: Swagger UI carrega com sucesso
- **WHEN** o desenvolvedor acessa `GET /swagger-ui.html` com a aplicação rodando
- **THEN** a resposta é `200 OK` com a interface Swagger UI renderizada no navegador

### Requirement: Spec OpenAPI 3 disponível em /v3/api-docs
A aplicação SHALL disponibilizar a especificação OpenAPI 3 em formato JSON via `GET /v3/api-docs`.

#### Scenario: Spec OpenAPI retornada com sucesso
- **WHEN** `GET /v3/api-docs` é chamado
- **THEN** a resposta é `200 OK` com JSON válido da especificação OpenAPI 3 contendo título "Pactum API", versão e descrição da API

### Requirement: Controllers anotados com @Tag
Todo `@RestController` da aplicação SHALL ser anotado com `@Tag(name = "...", description = "...")` para que os endpoints apareçam agrupados e descritos na documentação OpenAPI.

#### Scenario: Grupos de endpoints visíveis no Swagger UI
- **WHEN** o Swagger UI é acessado
- **THEN** os endpoints estão agrupados por tag com nome e descrição legíveis

### Requirement: Respostas documentadas com @ApiResponse
Cada endpoint de cada controller SHALL ter anotações `@ApiResponse` documentando ao menos os códigos de retorno de sucesso e erro esperados.

#### Scenario: Códigos de resposta visíveis na documentação
- **WHEN** um endpoint é expandido no Swagger UI
- **THEN** os possíveis códigos HTTP de resposta (ex: 200, 201, 404, 422) são listados com suas descrições

### Requirement: Resposta de erro padronizada via GlobalExceptionHandler
A aplicação SHALL retornar erros HTTP em formato padronizado com os campos `timestamp`, `status`, `message` e `path` para todas as exceções não tratadas e exceções de domínio conhecidas.

#### Scenario: Recurso não encontrado retorna 404 padronizado
- **WHEN** um endpoint é chamado com ID inexistente e lança exceção de domínio tipo `*NaoEncontradaException`
- **THEN** a resposta é `404 Not Found` com corpo `{ "timestamp": "...", "status": 404, "message": "...", "path": "..." }`

#### Scenario: Validação de input falha retorna 422 padronizado
- **WHEN** um endpoint recebe payload com campos inválidos (violação de `@Valid`)
- **THEN** a resposta é `422 Unprocessable Entity` com corpo padronizado listando os erros de validação
