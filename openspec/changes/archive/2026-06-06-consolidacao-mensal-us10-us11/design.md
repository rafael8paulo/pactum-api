## Context

A API já possui `BuscarReceitasPort` e `BuscarDespesasPort` com métodos `buscarPorFiltros(YearMonth, ...)`. O resumo mensal é uma operação de leitura pura que agrega dados de dois aggregates existentes. Não há nova tabela ou novo port de saída — apenas um serviço de domínio que orquestra buscas via ports já existentes.

## Goals / Non-Goals

**Goals:**
- Expor `GET /api/v1/resumo?competencia=` retornando totalReceitas, totalDespesas, saldo
- Expor `GET /api/v1/resumo/anual?ano=` retornando lista dos 12 meses + totais anuais
- Meses sem lançamentos retornam zeros (nunca 404)

**Non-Goals:**
- Persistência de resumos (sempre calculado on-the-fly)
- Filtros por categoria no resumo
- Comparação entre anos

## Decisions

**D1: Nenhum novo port de saída**
O `ResumoService` injeta `BuscarReceitasPort` e `BuscarDespesasPort` diretamente. Alternativa seria criar `BuscarResumoPort`, mas isso seria uma abstração sem implementação distinta — YAGNI.

**D2: `ResumoMensal` como record de domínio puro**
`ResumoMensal(YearMonth competencia, BigDecimal totalReceitas, BigDecimal totalDespesas, BigDecimal saldo)`. O saldo é pré-calculado no service, não computado lazy, para manter o record imutável e testável sem lógica.

**D3: Histórico anual calculado iterando 12 meses no service**
`ConsultarHistoricoAnualUseCase.consultar(int ano)` itera de `YearMonth.of(ano, 1)` a `YearMonth.of(ano, 12)`, reutilizando `ConsultarResumoMensalUseCase` para cada mês. Simples e sem N+1 relevante para 12 chamadas in-memory.

**D4: Dois use cases separados, um service único `ResumoService`**
`ConsultarResumoMensalUseCase` e `ConsultarHistoricoAnualUseCase` são interfaces distintas (SRP), implementadas pelo mesmo `ResumoService` (coesão). Mesmo padrão de `DespesaService` e `ReceitaService`.

**D5: `@RequestParam int ano` para o histórico anual**
Parâmetro simples inteiro, sem formatação especial. Validação implícita por tipo — valor inválido retorna 400.

## Risks / Trade-offs

- [Risk] 12 consultas separadas para histórico anual → banco local, aceitável; seria otimizável com query nativa se necessário → mitigação: aceito por ora, YAGNI
- [Risk] `BuscarDespesasPort.buscarPorFiltros` recebe `CategoriaDespesa` e `StatusDespesa` — chamar com `null, null` para somar todas as despesas → sem risco, comportamento já testado
