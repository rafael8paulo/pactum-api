## Context

A Pactum API está em seu estágio inicial. Nenhuma classe de negócio existe ainda — apenas o projeto Maven gerado. Toda feature das sprints seguintes depende que o banco de dados esteja acessível e que os endpoints possam ser inspecionados via Swagger. Este design cobre exclusivamente a camada de infraestrutura: datasource, docker-compose e documentação OpenAPI.

## Goals / Non-Goals

**Goals:**
- Configurar datasource PostgreSQL com profiles `default` (produção futura) e `dev` (desenvolvimento local)
- Prover `docker-compose.yml` com PostgreSQL 16 para zero-friction local setup
- Expor `/actuator/health` com detalhe do status do banco
- Disponibilizar Swagger UI em `/swagger-ui.html` e spec em `/v3/api-docs`
- Criar `GlobalExceptionHandler` com resposta de erro padronizada (`timestamp`, `status`, `message`, `path`)
- Criar `OpenApiConfig` com título, versão e descrição da API

**Non-Goals:**
- Migrations com Flyway/Liquibase (schema gerenciado por `ddl-auto` nesta fase)
- Autenticação ou segurança nos endpoints
- Qualquer lógica de negócio (despesas, receitas, patrimônio)
- Testes de integração contra banco real (ficam nas sprints de feature)

## Decisions

### D1 — Profile `dev` para separação de configuração local
**Decisão:** Usar `application-dev.properties` ativado por `spring.profiles.active=dev`.  
**Alternativa considerada:** Variáveis de ambiente em `application.properties` diretamente.  
**Rationale:** Profiles permitem que o CI/CD injete configuração sem modificar arquivos rastreados pelo Git. O profile `dev` com `ddl-auto=create-drop` facilita iterar o schema sem migrations ainda.

### D2 — `ddl-auto=validate` no profile padrão
**Decisão:** `spring.jpa.hibernate.ddl-auto=validate` no `application.properties` principal.  
**Alternativa considerada:** `update`.  
**Rationale:** `validate` falha rápido se o schema não bater com as entidades, evitando divergências silenciosas. `update` é perigoso em produção. Em `dev`, usa `create-drop` para simplicidade.

### D3 — SpringDoc OpenAPI (não springfox)
**Decisão:** `springdoc-openapi-starter-webmvc-ui` v2.x.  
**Alternativa considerada:** `springfox` 3.x.  
**Rationale:** SpringFox não é mantido e tem incompatibilidades conhecidas com Spring Boot 3.x. SpringDoc é o padrão para o ecossistema Spring Boot 3+.

### D4 — `GlobalExceptionHandler` em `adapter/in/web/`
**Decisão:** Criar `GlobalExceptionHandler` com `@ControllerAdvice` dentro de `adapter/in/web/`.  
**Rationale:** Exceções de domínio são mapeadas para HTTP aqui — é responsabilidade do adapter de entrada, não do domínio. Segue a regra de dependência hexagonal.

### D5 — Actuator expõe apenas `health`
**Decisão:** `management.endpoints.web.exposure.include=health`.  
**Rationale:** Minimizar superfície de ataque sem necessidade de autenticação. Outros endpoints (metrics, env) serão adicionados sob demanda.

## Risks / Trade-offs

- **[Risco] `ddl-auto=create-drop` em dev apaga dados ao reiniciar** → Mitigação: Aceitável nesta fase; quando as entidades estabilizarem, migrar para Flyway (backlog futuro).
- **[Risco] Credenciais `pactum/pactum` hardcoded no `application-dev.properties`** → Mitigação: Arquivo é para uso local apenas; adicionar ao `.gitignore` ou deixar explícito que é para dev, nunca usar em CI com secrets reais.
- **[Trade-off] Sem migration tool agora** → Simplifica o setup inicial mas exige cuidado ao mudar entidades JPA. Documentado como débito técnico.
