# 💰 Pactum API — Backlog

> **Stack:** Java 17 · Spring Boot 3.5.0 · PostgreSQL · Maven · Arquitetura Hexagonal (Ports & Adapters)
> **Pacote base:** `br.com.rpx.pactumapi`

---

## 🗂️ Épicos

| ID | Épico |
|----|-------|
| E1 | Infraestrutura & Setup |
| E2 | Gestão de Despesas |
| E3 | Gestão de Receitas |
| E4 | Consolidação Mensal |
| E5 | Patrimônio Guardado |

---

## 📐 Estrutura de pacotes (Hexagonal)

```
br.com.rpx.pactumapi
├── domain
│   ├── model          # Entidades e Value Objects
│   ├── port
│   │   ├── in         # Use case interfaces (driving ports)
│   │   └── out        # Repository interfaces (driven ports)
│   └── exception      # Exceções de domínio
├── application
│   └── usecase        # Implementações dos use cases
├── adapter
│   ├── in
│   │   └── web        # Controllers REST + DTOs de request/response
│   └── out
│       └── persistence # Entities JPA + Repositories Spring Data + Mappers
└── config             # Beans de configuração Spring
```

---

## 📋 User Stories

---

### E1 — Infraestrutura & Setup

#### US-01 · Configuração do banco de dados
**Como** desenvolvedor,
**quero** configurar a conexão com PostgreSQL e o schema inicial,
**para que** a aplicação possa persistir dados corretamente.

**Critérios de aceite:**
- [ ] `application.properties` configurado com datasource PostgreSQL
- [ ] `application-dev.properties` com dados locais (localhost:5432/pactum)
- [ ] `docker-compose.yml` com serviço PostgreSQL 16
- [ ] Schema criado via `spring.jpa.hibernate.ddl-auto=validate` em prod, `create-drop` em dev
- [ ] `GET /actuator/health` retorna `UP` com status do banco

**Configuração esperada (`application-dev.properties`):**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/pactum
spring.datasource.username=pactum
spring.datasource.password=pactum
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
```

---

#### US-02 · Configuração do SpringDoc OpenAPI
**Como** desenvolvedor,
**quero** ter a documentação Swagger disponível,
**para que** os endpoints possam ser testados e documentados.

**Critérios de aceite:**
- [ ] Swagger UI acessível em `GET /swagger-ui.html`
- [ ] OpenAPI spec em `GET /v3/api-docs`
- [ ] Todos os controllers anotados com `@Tag`
- [ ] Respostas documentadas com `@ApiResponse`

---

### E2 — Gestão de Despesas

> Categorias identificadas na planilha: `FINANCIAMENTO`, `CARTAO_CREDITO`, `EDUCACAO`, `SERVICOS`, `LAZER`, `IMPOSTO`, `OUTROS`
> Status observados: `PAGA`, `PENDENTE`

#### US-03 · Cadastrar despesa

**Domain:**
- [ ] Criar record `Despesa` em `domain.model` com: `id (UUID)`, `descricao`, `valor (BigDecimal)`, `status (StatusDespesa)`, `competencia (YearMonth)`, `categoria (CategoriaDespesa)`
- [ ] Criar enum `StatusDespesa`: `PAGA`, `PENDENTE`, `AGENDADA`
- [ ] Criar enum `CategoriaDespesa`: `FINANCIAMENTO`, `CARTAO_CREDITO`, `EDUCACAO`, `SERVICOS`, `LAZER`, `IMPOSTO`, `OUTROS`
- [ ] Criar port in `CadastrarDespesaUseCase` com método `cadastrar(Despesa): Despesa`

**Application:**
- [ ] Implementar `CadastrarDespesaUseCaseImpl` anotado com `@UseCase`

**Adapter In (Web):**
- [ ] Criar `DespesaController` com `POST /api/v1/despesas`
- [ ] Criar `CadastrarDespesaRequest` com validações (`@NotBlank`, `@NotNull`, `@Positive`)
- [ ] Criar `DespesaResponse`
- [ ] Retornar `201 Created` com o recurso criado

**Adapter Out (Persistence):**
- [ ] Criar `DespesaJpaEntity` com `@Entity @Table(name = "despesas")`
- [ ] Criar `DespesaJpaRepository` extendendo `JpaRepository`
- [ ] Criar `DespesaPersistenceAdapter` implementando port out `SalvarDespesaPort`
- [ ] Criar `DespesaMapper` (domínio ↔ JPA entity ↔ DTO)

**Modelo de request:**
```json
{
  "descricao": "Financiamento do Carro",
  "valor": 1335.50,
  "status": "PAGA",
  "competencia": "2025-07",
  "categoria": "FINANCIAMENTO"
}
```

---

#### US-04 · Listar despesas por mês

**Domain:**
- [ ] Criar port in `ListarDespesasUseCase` com método `listar(YearMonth, CategoriaDespesa, StatusDespesa): List<Despesa>`
- [ ] Criar port out `BuscarDespesasPort`

**Application:**
- [ ] Implementar `ListarDespesasUseCaseImpl`

**Adapter In (Web):**
- [ ] `GET /api/v1/despesas?competencia=2025-07&categoria=FINANCIAMENTO&status=PAGA`
- [ ] Criar `ListaDespesasResponse` com `despesas: List<DespesaResponse>` e `total: BigDecimal`

**Adapter Out (Persistence):**
- [ ] Query com filtros dinâmicos via `Specification` ou JPQL

---

#### US-05 · Atualizar status de despesa

**Domain:**
- [ ] Criar port in `AtualizarStatusDespesaUseCase` com método `atualizar(UUID id, StatusDespesa): Despesa`

**Application:**
- [ ] Implementar `AtualizarStatusDespesaUseCaseImpl`
- [ ] Lançar `DespesaNaoEncontradaException` para IDs inexistentes

**Adapter In (Web):**
- [ ] `PATCH /api/v1/despesas/{id}/status` com body `{ "status": "PAGA" }`
- [ ] Retornar `200 OK` com recurso atualizado

---

#### US-06 · Editar e remover despesa

**Domain:**
- [ ] Criar port in `EditarDespesaUseCase`
- [ ] Criar port in `RemoverDespesaUseCase`

**Adapter In (Web):**
- [ ] `PUT /api/v1/despesas/{id}` — atualização completa
- [ ] `DELETE /api/v1/despesas/{id}` — retorno `204 No Content`
- [ ] Retornar `404 Not Found` para IDs inexistentes

---

### E3 — Gestão de Receitas

> Receitas identificadas na planilha: `Salário`, `Ducz`, `1/3 férias`, `Guardado (Férias + salário)`
> Categorias sugeridas: `SALARIO`, `FREELANCE`, `BONUS`, `OUTROS`

#### US-07 · Cadastrar receita

**Domain:**
- [ ] Criar record `Receita` em `domain.model` com: `id (UUID)`, `descricao`, `valor (BigDecimal)`, `competencia (YearMonth)`, `categoria (CategoriaReceita)`
- [ ] Criar enum `CategoriaReceita`: `SALARIO`, `FREELANCE`, `BONUS`, `OUTROS`
- [ ] Criar port in `CadastrarReceitaUseCase`

**Application:**
- [ ] Implementar `CadastrarReceitaUseCaseImpl`

**Adapter In (Web):**
- [ ] `POST /api/v1/receitas`
- [ ] Criar `CadastrarReceitaRequest` com validações
- [ ] Criar `ReceitaResponse`
- [ ] Retornar `201 Created`

**Adapter Out (Persistence):**
- [ ] Criar `ReceitaJpaEntity` com `@Entity @Table(name = "receitas")`
- [ ] Criar `ReceitaJpaRepository`
- [ ] Criar `ReceitaPersistenceAdapter`
- [ ] Criar `ReceitaMapper`

**Modelo de request:**
```json
{
  "descricao": "Salário",
  "valor": 5136.38,
  "competencia": "2025-07",
  "categoria": "SALARIO"
}
```

---

#### US-08 · Listar receitas por mês

**Domain:**
- [ ] Criar port in `ListarReceitasUseCase`
- [ ] Criar port out `BuscarReceitasPort`

**Adapter In (Web):**
- [ ] `GET /api/v1/receitas?competencia=2025-07&categoria=SALARIO`
- [ ] Criar `ListaReceitasResponse` com `receitas: List<ReceitaResponse>` e `total: BigDecimal`

---

#### US-09 · Editar e remover receita

**Adapter In (Web):**
- [ ] `PUT /api/v1/receitas/{id}`
- [ ] `DELETE /api/v1/receitas/{id}` — retorno `204 No Content`
- [ ] `404` para IDs inexistentes

---

### E4 — Consolidação Mensal

#### US-10 · Consultar resumo mensal

**Domain:**
- [ ] Criar record `ResumoMensal` com: `competencia (YearMonth)`, `totalReceitas`, `totalDespesas`, `saldo (BigDecimal)`
- [ ] Criar port in `ConsultarResumoMensalUseCase`

**Application:**
- [ ] Implementar `ConsultarResumoMensalUseCaseImpl`
- [ ] Calcular `saldo = totalReceitas - totalDespesas`

**Adapter In (Web):**
- [ ] `GET /api/v1/resumo?competencia=2025-07`

**Modelo de resposta:**
```json
{
  "competencia": "2025-07",
  "totalReceitas": 5936.38,
  "totalDespesas": 4788.64,
  "saldo": 1147.74
}
```

---

#### US-11 · Histórico anual

**Domain:**
- [ ] Criar port in `ConsultarHistoricoAnualUseCase`

**Adapter In (Web):**
- [ ] `GET /api/v1/resumo/anual?ano=2025`
- [ ] Retornar lista de `ResumoMensal` para os 12 meses (zerado para meses sem lançamentos)
- [ ] Incluir `totalAnualReceitas`, `totalAnualDespesas`, `saldoAnual` no response

---

### E5 — Patrimônio Guardado

> Observado a partir de Abril na planilha: `Caixinha Turbo Nubank`, `Caixinha Nubank`, `Carteira`

#### US-12 · Cadastrar item de patrimônio

**Domain:**
- [ ] Criar record `Patrimonio` com: `id (UUID)`, `descricao`, `valor (BigDecimal)`, `competencia (YearMonth)`
- [ ] Criar port in `CadastrarPatrimonioUseCase`

**Adapter In (Web):**
- [ ] `POST /api/v1/patrimonio`
- [ ] Retorno `201 Created`

**Adapter Out (Persistence):**
- [ ] Criar `PatrimonioJpaEntity` com `@Entity @Table(name = "patrimonio")`
- [ ] Criar `PatrimonioJpaRepository`
- [ ] Criar `PatrimonioPersistenceAdapter`

**Modelo de request:**
```json
{
  "descricao": "Caixinha Turbo Nubank",
  "valor": 5069.02,
  "competencia": "2025-07"
}
```

---

#### US-13 · Consultar patrimônio por período

**Domain:**
- [ ] Criar port in `ConsultarPatrimonioUseCase`

**Adapter In (Web):**
- [ ] `GET /api/v1/patrimonio?competencia=2025-07`
- [ ] Retornar lista de itens e `totalPatrimonio: BigDecimal`

---

## 🗄️ Schema PostgreSQL

```sql
CREATE TABLE despesas (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    descricao   VARCHAR(100)   NOT NULL,
    valor       NUMERIC(10, 2) NOT NULL,
    status      VARCHAR(20)    NOT NULL,
    competencia DATE           NOT NULL,
    categoria   VARCHAR(50)    NOT NULL,
    created_at  TIMESTAMP      NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP      NOT NULL DEFAULT now()
);

CREATE TABLE receitas (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    descricao   VARCHAR(100)   NOT NULL,
    valor       NUMERIC(10, 2) NOT NULL,
    competencia DATE           NOT NULL,
    categoria   VARCHAR(50)    NOT NULL,
    created_at  TIMESTAMP      NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP      NOT NULL DEFAULT now()
);

CREATE TABLE patrimonio (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    descricao   VARCHAR(100)   NOT NULL,
    valor       NUMERIC(10, 2) NOT NULL,
    competencia DATE           NOT NULL,
    created_at  TIMESTAMP      NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP      NOT NULL DEFAULT now()
);
```

---

## 🔢 Priorização (MoSCoW)

| Story | Prioridade |
|-------|------------|
| US-01 Configuração banco | Must Have |
| US-02 SpringDoc OpenAPI | Must Have |
| US-03 Cadastrar despesa | Must Have |
| US-04 Listar despesas | Must Have |
| US-05 Atualizar status despesa | Must Have |
| US-07 Cadastrar receita | Must Have |
| US-08 Listar receitas | Must Have |
| US-10 Resumo mensal | Must Have |
| US-06 Editar/remover despesa | Should Have |
| US-09 Editar/remover receita | Should Have |
| US-11 Histórico anual | Should Have |
| US-12 Cadastrar patrimônio | Could Have |
| US-13 Consultar patrimônio | Could Have |

---

## 🚀 Sprints sugeridas

### Sprint 1 — Fundação
`US-01` `US-02`

### Sprint 2 — Despesas
`US-03` `US-04` `US-05` `US-06`

### Sprint 3 — Receitas
`US-07` `US-08` `US-09`

### Sprint 4 — Consolidação
`US-10` `US-11`

### Sprint 5 — Patrimônio
`US-12` `US-13`
