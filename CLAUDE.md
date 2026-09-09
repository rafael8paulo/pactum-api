# CLAUDE.md — Pactum API

## 1. Visão Geral

**Pactum** é uma API REST de finanças pessoais construída com:

- **Java 17** + **Spring Boot 3.5.14** + **Maven**
- Empacotamento: Jar | Configuração: `application.properties`
- Dependências: Lombok, Spring Data JPA, PostgreSQL Driver, Spring Web, Validation, SpringDoc OpenAPI
- **Arquitetura Hexagonal (Ports & Adapters)**

A regra de dependência é **inviolável**: o domínio não conhece nada fora dele. Spring Boot é detalhe de infraestrutura.

---

## 2. Arquitetura Hexagonal — Estrutura de Pacotes

Pacote raiz: `br.com.rpx.pactumapi`

```
domain/
  model/          → entidades e value objects puros (sem anotações de framework)
  port/
    in/           → casos de uso (interfaces que a aplicação expõe)
    out/          → portas de saída (interfaces que a aplicação consome)
  exception/      → exceções de domínio puras
  service/        → implementações dos casos de uso (UseCaseImpl)

application/
  dto/request/    → records de entrada da API
  dto/response/   → records de saída da API
  mapper/         → conversão entre domain model e DTOs

adapter/
  in/
    web/          → @RestController (adapter de entrada HTTP)
  out/
    persistence/  → @Repository, @Entity JPA, implementações das portas de saída
      entity/     → entidades JPA (não são as domain model)
      mapper/     → conversão entre domain model e JPA entity
      repository/ → interfaces Spring Data JPA

config/           → beans Spring, configurações gerais
```

**Regra de dependência:**

- `domain/` não importa nada de `adapter/`, `application/` ou Spring
- `application/` conhece `domain/`, não conhece `adapter/`
- `adapter/` conhece `domain/` e `application/`, nunca o contrário
- Spring Boot é detalhe de infraestrutura — fica em `adapter/` e `config/`

---

## 3. Comandos Essenciais

```bash
# Build completo
./mvnw clean install

# Subir a aplicação
./mvnw spring-boot:run

# Rodar todos os testes
./mvnw test

# Rodar um teste de integração específico
./mvnw test -Dtest=NomeDaClasseIT

# Documentação interativa
# Swagger UI disponível em: /swagger-ui.html
```

---

## 4. Deploy

A aplicação roda em produção via imagem Docker multi-stage (`maven:3.9-eclipse-temurin-17` → `eclipse-temurin:17-jre-alpine`), publicada no GHCR. O schema do PostgreSQL é versionado via Flyway (`src/main/resources/db/migration`) — não há necessidade de rodar nada manualmente, o Flyway aplica as migrations pendentes no boot da API.

### Deploy automático via GitHub Actions

Todo push em `master` dispara `.github/workflows/deploy.yml`, em 3 jobs sequenciais (`needs`):

1. **test** — `./mvnw test` como gate; falha o pipeline (sem publicar imagem nem deployar) se algum teste quebrar.
2. **build-and-push** — builda a imagem a partir do `Dockerfile` e publica no GHCR (`ghcr.io/rafael8paulo/pactum-api`) com as tags `latest` e `${{ github.sha }}`.
3. **deploy** — dispara o deploy da aplicação no **Dokploy** (via API) e aguarda o deployment concluir; se o Dokploy reportar erro, o job falha e imprime os logs do deployment.

O Dokploy é o plano de controle de deploy na VPS: recebe o gatilho, puxa a imagem nova do GHCR, recria o container com health check e mantém histórico e rollback pela própria UI. Não há mais SSH nem `docker compose` manual no pipeline. O token do GHCR usa o `GITHUB_TOKEN` automático do workflow (`permissions: packages: write`).

**Testes de integração (`*IT.java`) não rodam neste pipeline** — dependem de Postgres/Testcontainers não provisionados em CI; ficam para uma iteração futura.

---

## 5. Nomenclatura por Camada

| Camada | Exemplo |
|---|---|
| `domain/port/in/` | `ManageTransactionUseCase`, `FindTransactionUseCase` |
| `domain/port/out/` | `LoadTransactionPort`, `SaveTransactionPort`, `DeleteTransactionPort` |
| `domain/service/` | `ManageTransactionService implements ManageTransactionUseCase` |
| `adapter/in/web/` | `TransactionController` |
| `adapter/out/persistence/` | `TransactionPersistenceAdapter implements LoadTransactionPort, SaveTransactionPort` |
| `adapter/out/persistence/repository/` | `TransactionJpaRepository` (interface Spring Data) |
| `adapter/out/persistence/entity/` | `TransactionJpaEntity` (`@Entity` JPA) |
| `application/dto/request/` | `CreateTransactionRequest` (record) |
| `application/dto/response/` | `TransactionResponse` (record) |

---

## 6. Padrões de Código Java 17

- Use **records** para DTOs e value objects imutáveis
- Use **`Optional<T>`** no retorno de ports de saída que buscam por ID
- Domain model **sem anotações de framework** — sem `@Entity`, sem `@Column` em `domain/`
- Campos sempre **`final`** nos domain models
- Prefira **streams e lambdas** a loops imperativos

---

## 7. Regras Clean Code

- Nomes expressivos e sem abreviações obscuras
- Métodos com responsabilidade única — guideline de até **20 linhas**
- Sem comentários que explicam o que o código já diz
- Sem magic numbers ou strings — use **constantes ou enums** no domínio
- **DRY**: nenhuma lógica de negócio duplicada entre use cases

---

## 8. SOLID na Arquitetura Hexagonal

- **SRP**: cada interface de use case tem um único propósito (`FindX`, `ManageX`, `DeleteX`)
- **OCP**: adicione comportamento criando novos ports/adapters, sem modificar os existentes
- **DIP**: `domain/` depende apenas de suas próprias abstrações (ports); Spring injeta os adapters
- **ISP**: ports coesos e pequenos — separe `LoadPort` de `SavePort` se os consumidores forem diferentes
- **LSP**: todo adapter deve respeitar o contrato do port sem efeitos colaterais inesperados

---

## 9. Padrões Spring Boot 3.x

- `@RestController` apenas em `adapter/in/web/` — **sem lógica de negócio**
- Receba DTOs de `application/`, converta para domain model antes de chamar o use case
- Use `@Valid` nos parâmetros de entrada do controller
- Exceções de domínio mapeadas para HTTP via `@ControllerAdvice` (em `config/` ou `adapter/in/web/`)
- Resposta padronizada de erro: `timestamp`, `status`, `message`, `path`
- `ResponseEntity` com status HTTP semântico: `201 Created`, `204 No Content`, `404 Not Found`, `422 Unprocessable Entity`
- `@ConfigurationProperties` para configurações externas

---

## 10. Regras para Testes

- **Domain puro** testado sem Spring, sem mocks de framework — apenas JUnit 5
- **Use cases** testados com `@ExtendWith(MockitoExtension.class)`, mockando os ports de saída
- **Web adapters** testados com `@WebMvcTest` + mock do use case
- **Persistence adapters** testados com `@DataJpaTest` ou Testcontainers (sufixo `IT`)
- **Nomenclatura**: `deve_[resultado]_quando_[condição]`
- **Cobertura mínima**: 80% em `domain/` e `domain/service/`

---

## 11. O Que Nunca Fazer

- Nunca importe classes de `adapter/` ou Spring dentro de `domain/`
- Nunca use `@Entity` ou `@Column` no domain model — isso é detalhe de persistência
- Nunca retorne `JpaEntity` diretamente no response — mapeie para DTO
- Nunca coloque lógica de negócio no Controller ou no PersistenceAdapter
- Nunca injete dependência por campo (`@Autowired` no atributo) — use **construtor**
- Nunca use `System.out.println` — use `@Slf4j` do Lombok
- Nunca commite secrets ou URLs hardcoded

---

## 12. Instruções para o Agente de IA

Antes de criar qualquer classe, identifique em qual camada ela pertence.

**Ordem obrigatória ao criar um novo recurso** (ex: `Category`):

1. `domain/model/` — entidade de domínio
2. `domain/port/in/` e `domain/port/out/` — interfaces dos casos de uso e portas de saída
3. `domain/service/` — implementação do caso de uso
4. `application/dto/` — records de request e response, mapper
5. `adapter/out/persistence/` — JpaEntity, JpaRepository, PersistenceAdapter e mapper de persistência
6. `adapter/in/web/` — Controller

**Regras adicionais:**

- Nunca pule etapas dessa ordem — o domínio sempre vem primeiro
- Ao criar um port de saída, crie o adapter correspondente na mesma entrega
- Ao criar qualquer lógica nova, crie o teste unitário junto
- Se a tarefa for ambígua ou cruzar múltiplos agregados, **pergunte antes de agir**
- Não refatore código fora do escopo da tarefa solicitada
- Use `@Slf4j` do Lombok para logging em adapters e services
