## 1. Dependências e configuração do projeto

- [x] 1.1 Adicionar dependência `spring-boot-starter-actuator` ao `pom.xml`
- [x] 1.2 Adicionar dependência `springdoc-openapi-starter-webmvc-ui` ao `pom.xml`
- [x] 1.3 Configurar `application.properties` com datasource PostgreSQL, `ddl-auto=validate`, `show-sql=false`, e actuator health com `show-details=always`
- [x] 1.4 Criar `application-dev.properties` com `spring.datasource.url=jdbc:postgresql://localhost:5432/pactum`, usuário/senha `pactum`, `ddl-auto=create-drop`, `show-sql=true`

## 2. Docker Compose

- [x] 2.1 Criar `docker-compose.yml` na raiz do projeto com serviço `postgres` usando imagem `postgres:16`, variáveis `POSTGRES_DB=pactum`, `POSTGRES_USER=pactum`, `POSTGRES_PASSWORD=pactum`, porta `5432:5432` e volume nomeado para persistência

## 3. Configuração Spring — OpenAPI

- [x] 3.1 Criar classe `OpenApiConfig` em `config/` anotada com `@Configuration` e `@Bean` que retorna `OpenAPI` com título "Pactum API", versão "1.0.0" e descrição da API de finanças pessoais
- [x] 3.2 Configurar em `application.properties` as propriedades `springdoc.swagger-ui.path=/swagger-ui.html` e `springdoc.api-docs.path=/v3/api-docs`

## 4. Tratamento global de erros

- [x] 4.1 Criar record `ErrorResponse` em `application/dto/response/` com campos `timestamp (LocalDateTime)`, `status (int)`, `message (String)`, `path (String)`
- [x] 4.2 Criar `GlobalExceptionHandler` em `adapter/in/web/` anotado com `@ControllerAdvice` e `@Slf4j`
- [x] 4.3 Adicionar handler para `MethodArgumentNotValidException` retornando `422 Unprocessable Entity` com lista de erros de validação no campo `message`
- [x] 4.4 Adicionar handler para `Exception` genérica retornando `500 Internal Server Error`
- [x] 4.5 Adicionar handler para `NoResourceFoundException` / `NoHandlerFoundException` retornando `404 Not Found`

## 5. Testes

- [x] 5.1 Criar teste unitário `GlobalExceptionHandlerTest` com `@WebMvcTest` verificando resposta `422` para payload inválido
- [x] 5.2 Criar teste unitário `OpenApiConfigTest` verificando que o bean `OpenAPI` é criado com título e versão corretos
- [x] 5.3 Verificar manualmente que `GET /actuator/health` retorna `UP` com banco disponível via docker-compose
- [x] 5.4 Verificar manualmente que `GET /swagger-ui.html` carrega a UI e `GET /v3/api-docs` retorna JSON válido

