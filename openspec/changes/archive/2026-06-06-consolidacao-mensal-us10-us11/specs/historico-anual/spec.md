## ADDED Requirements

### Requirement: Histórico anual retorna resumos dos 12 meses
A API SHALL aceitar `GET /api/v1/resumo/anual?ano=2025` e retornar `200 OK` com objeto contendo `ano (int)`, `meses (List<ResumoMensalResponse>)` com exatamente 12 entradas (janeiro a dezembro), `totalAnualReceitas (BigDecimal)`, `totalAnualDespesas (BigDecimal)` e `saldoAnual (BigDecimal)`. Meses sem lançamentos SHALL aparecer na lista com valores zerados.

#### Scenario: Histórico anual com lançamentos em alguns meses
- **WHEN** `GET /api/v1/resumo/anual?ano=2025` é chamado e existem lançamentos em apenas alguns meses de 2025
- **THEN** a resposta é `200 OK` com `meses` contendo 12 entradas; meses sem lançamentos têm valores zerados; totais anuais refletem a soma de todos os meses

#### Scenario: Histórico anual sem nenhum lançamento retorna zeros
- **WHEN** `GET /api/v1/resumo/anual?ano=2020` é chamado e não há lançamentos em 2020
- **THEN** a resposta é `200 OK` com `meses` contendo 12 entradas zeradas e `totalAnualReceitas: 0`, `totalAnualDespesas: 0`, `saldoAnual: 0`

#### Scenario: Parâmetro ano ausente retorna 400
- **WHEN** `GET /api/v1/resumo/anual` é chamado sem o parâmetro `ano`
- **THEN** a resposta é `400 Bad Request`

### Requirement: Totais anuais agregados
O `HistoricoAnualResponse` SHALL incluir `totalAnualReceitas` (soma das receitas dos 12 meses), `totalAnualDespesas` (soma das despesas dos 12 meses) e `saldoAnual` (soma dos saldos dos 12 meses).

#### Scenario: Totais anuais calculados corretamente
- **WHEN** `GET /api/v1/resumo/anual?ano=2025` é chamado com lançamentos em múltiplos meses
- **THEN** `totalAnualReceitas`, `totalAnualDespesas` e `saldoAnual` refletem a soma correta de todos os 12 meses
