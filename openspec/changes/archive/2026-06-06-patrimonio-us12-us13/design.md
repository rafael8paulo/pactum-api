## Context

`Patrimonio` é o terceiro aggregate de dados do domínio (após `Despesa` e `Receita`). A estrutura é mais simples: sem status, sem categoria — apenas descricao, valor e competencia. A tabela `patrimonio` já está descrita no schema PostgreSQL do BACKLOG. O padrão de implementação segue exatamente o de `Receita` (cadastrar + consultar, sem editar/remover nesta sprint).

## Goals / Non-Goals

**Goals:**
- `POST /api/v1/patrimonio` com retorno `201 Created`
- `GET /api/v1/patrimonio?competencia=` com lista de itens e `totalPatrimonio`
- Seguir o mesmo padrão hexagonal de `Receita` e `Despesa`

**Non-Goals:**
- Editar ou remover itens de patrimônio (fora do escopo desta sprint)
- Histórico anual de patrimônio
- Comparação entre meses

## Decisions

**D1: `Patrimonio` sem enum de categoria ou status**
O modelo de domínio é `Patrimonio(UUID id, String descricao, BigDecimal valor, YearMonth competencia)`. Sem campos extras — YAGNI.

**D2: Dois use cases separados, um service único `PatrimonioService`**
`CadastrarPatrimonioUseCase` e `ConsultarPatrimonioUseCase` implementados por `PatrimonioService`. Mesmo padrão de `ReceitaService` e `ResumoService`.

**D3: `SalvarPatrimonioPort` e `BuscarPatrimonioPort` como ports de saída separados**
`SalvarPatrimonioPort.salvar(Patrimonio): Patrimonio` e `BuscarPatrimonioPort.buscarPorCompetencia(YearMonth): List<Patrimonio>`. Não há busca por ID nesta sprint (sem editar/remover), então `BuscarPatrimonioPort` é mais simples que `BuscarReceitasPort`.

**D4: `ListaPatrimonioResponse` com `itens` e `totalPatrimonio`**
Consistente com `ListaDespesasResponse` e `ListaReceitasResponse` — sempre retorna a lista e o total calculado no mapper.

**D5: Query JPQL simples por competencia**
`SELECT p FROM PatrimonioJpaEntity p WHERE p.competencia = :competencia` — sem filtros dinâmicos adicionais nesta sprint.

## Risks / Trade-offs

- [Risk] Sem editar/remover nesta sprint — usuário não pode corrigir lançamento errado → aceito, pode ser adicionado em sprint futura
- [Risk] `PatrimonioJpaEntity` armazena `competencia` como `LocalDate` (primeiro dia do mês) — mesma conversão `YearMonth ↔ LocalDate` já usada em `Despesa` e `Receita`, sem risco novo
