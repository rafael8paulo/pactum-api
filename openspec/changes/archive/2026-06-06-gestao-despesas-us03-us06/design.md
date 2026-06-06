## Context

A infraestrutura está pronta (US-01/US-02). Este design cobre o primeiro agregado de negócio: **Despesas**. A arquitetura hexagonal já está definida no CLAUDE.md — o desafio aqui é mapear corretamente os tipos de domínio (`YearMonth`, `BigDecimal`, enums) através de todas as camadas e tomar decisões consistentes sobre granularidade dos use cases.

## Goals / Non-Goals

**Goals:**
- CRUD completo de despesas (criar, listar, atualizar status, editar, remover)
- Filtragem de listagem por `competencia` (obrigatório), `categoria` e `status` (opcionais)
- Resposta de listagem com `total` agregado das despesas filtradas
- Exceção de domínio `DespesaNaoEncontradaException` propagada corretamente como `404`
- Testes unitários em todas as camadas (`domain`, `service`, `web`, `persistence`)

**Non-Goals:**
- Paginação (listagem retorna todos os resultados do mês filtrado)
- Soft delete (remoção é permanente)
- Auditoria ou histórico de alterações
- Bulk operations (uma despesa por vez)

## Decisions

### D1 — Um `DespesaService` implementando todos os use cases do agregado
**Decisão:** Criar `DespesaService` implementando `CadastrarDespesaUseCase`, `ListarDespesasUseCase`, `AtualizarStatusDespesaUseCase`, `EditarDespesaUseCase` e `RemoverDespesaUseCase`.  
**Alternativa considerada:** Uma classe `*UseCaseImpl` separada por use case.  
**Rationale:** O Spring já isola os beans por interface. Uma classe por aggregado é mais simples de navegar e testar — não há ganho real em fragmentar 5 classes para um único aggregado.

### D2 — `YearMonth` no domínio, `LocalDate` (primeiro dia do mês) na JPA entity
**Decisão:** `DespesaJpaEntity.competencia` é `LocalDate` armazenando sempre o primeiro dia do mês (ex: `2025-07-01`). O mapper converte para `YearMonth` no domínio.  
**Alternativa considerada:** Armazenar como `String` (`"2025-07"`).  
**Rationale:** `DATE` no PostgreSQL mapeia naturalmente para `LocalDate` em JPA. Guardar como `LocalDate` permite usar operadores de range nativos no banco. A convenção "primeiro do mês" é aplicada exclusivamente no mapper, centralizada em um único lugar.

### D3 — Filtros dinâmicos via `@Query` JPQL (não Specification)
**Decisão:** `DespesaJpaRepository` terá um método com `@Query` usando `COALESCE`/parâmetros nullable para filtros opcionais.  
**Alternativa considerada:** `JpaSpecificationExecutor` + `Specification`.  
**Rationale:** Temos apenas 3 dimensões de filtro e o caso de uso é fixo. A Specification adiciona complexidade de composição sem benefício aqui. Uma `@Query` JPQL com condições `(:param IS NULL OR campo = :param)` é mais legível e testável.

### D4 — Anotação `@UseCase` como estereótipo de `@Service`
**Decisão:** Criar `@UseCase` em `config/` como meta-anotação de `@Service` (Spring stereotype).  
**Rationale:** Documenta intenção arquitetural sem overhead. Classes de serviço do domínio ficam marcadas de forma semântica, distinguindo-as de `@Service` de infraestrutura.

### D5 — Serialização de `YearMonth` via `@JsonFormat` nos DTOs
**Decisão:** Usar `@JsonFormat(shape = STRING, pattern = "yyyy-MM")` nos campos `competencia` dos DTOs. Adicionar `spring.jackson.serialization.write-dates-as-timestamps=false` no `application.properties`.  
**Alternativa considerada:** `YearMonthSerializer`/`YearMonthDeserializer` registrados globalmente.  
**Rationale:** Configuração por campo é mais explícita e portável. O `JavaTimeModule` já está no classpath via `spring-boot-starter-web`.

### D6 — `SalvarDespesaPort` serve tanto para criar quanto para editar
**Decisão:** O port de saída `SalvarDespesaPort` tem um único método `salvar(Despesa): Despesa` que serve tanto para criação quanto para edição (o `JpaRepository.save()` faz upsert por ID).  
**Rationale:** Cria e atualiza têm o mesmo contrato de persistência. ISP não exige separação quando os consumidores são os mesmos.

## Risks / Trade-offs

- **[Risco] Query JPQL com parâmetros nullable pode ser ineficiente** → Mitigação: Para volumes do projeto (finanças pessoais), performance é irrelevante. Documentar como débito técnico para migração futura a Specification se necessário.
- **[Risco] `LocalDate` com primeiro do mês pode causar confusão** → Mitigação: Conversão centralizada no `DespesaPersistenceMapper`; domínio nunca vê `LocalDate`.
- **[Trade-off] Um `DespesaService` com múltiplas responsabilidades** → Com 5 métodos simples de CRUD, a classe não violará SRP na prática. Se crescer, dividir é trivial — as interfaces já isolam os contratos.
