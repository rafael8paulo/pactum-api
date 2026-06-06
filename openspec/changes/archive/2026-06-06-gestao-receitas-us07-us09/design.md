## Context

O agregado `Despesa` foi implementado na Sprint 2 com todos os padrões estabelecidos: `@UseCase`, `YearMonth→LocalDate` no mapper de persistência, filtros via `@Query` JPQL, `DespesaService` unificado. O agregado `Receita` segue **exatamente os mesmos padrões** — a principal diferença estrutural é que `Receita` não tem campo `status`, tornando o modelo ligeiramente mais simples.

## Goals / Non-Goals

**Goals:**
- CRUD de receitas (cadastrar, listar, editar, remover) seguindo os mesmos padrões de `Despesa`
- Listagem por `competencia` com filtro opcional de `categoria` e `total` agregado
- Exceção `ReceitaNaoEncontradaException` → `404` via `GlobalExceptionHandler`
- Testes unitários e de integração em todas as camadas

**Non-Goals:**
- Paginação
- Campo `status` em receitas (não existe nesse domínio)
- Qualquer forma de vinculação entre receita e despesa (feito no resumo mensal, Sprint 4)
- Bulk operations

## Decisions

### D1 — Mesmos padrões estruturais de Despesa sem desvios
**Decisão:** `ReceitaService`, `ReceitaJpaEntity`, `ReceitaPersistenceAdapter`, `ReceitaController` replicam exatamente a estrutura de seus equivalentes em Despesa.  
**Rationale:** Consistência arquitetural supera DRY neste nível — cada agregado é independente e pode evoluir separadamente. Compartilhar infraestrutura entre agregados criaria acoplamento desnecessário.

### D2 — `CategoriaReceita` como enum próprio (não reutilizar `CategoriaDespesa`)
**Decisão:** Criar `CategoriaReceita` com `SALARIO`, `FREELANCE`, `BONUS`, `OUTROS`.  
**Rationale:** As categorias de receita e despesa têm semânticas distintas e evoluem independentemente. Compartilhar um enum causaria acoplamento entre dois agregados não relacionados.

### D3 — `SalvarReceitaPort` serve criação e edição (mesmo padrão de `SalvarDespesaPort`)
**Decisão:** Um único método `salvar(Receita): Receita` para criação e atualização.  
**Rationale:** Mesma razão de `SalvarDespesaPort` — o `JpaRepository.save()` faz upsert por ID e os consumidores são os mesmos use cases.

### D4 — Quatro use cases em vez de cinco (sem `AtualizarStatusReceitaUseCase`)
**Decisão:** `Receita` expõe apenas `CadastrarReceitaUseCase`, `ListarReceitasUseCase`, `EditarReceitaUseCase`, `RemoverReceitaUseCase`.  
**Rationale:** `Receita` não tem campo `status`, então não há caso de uso de atualização de status isolado.

## Risks / Trade-offs

- **[Risco] Duplicação de código entre Despesa e Receita** → Mitigação: Aceita por design — os agregados são independentes. Se padrões comuns forem extraídos futuramente (ex: para Patrimônio), fazer naquele momento com evidência concreta de reuso.
- **[Trade-off] `GlobalExceptionHandler` cresce a cada agregado** → Mitigação: Cada handler é de uma linha. O padrão escala linearmente sem problema para o escopo deste projeto.
