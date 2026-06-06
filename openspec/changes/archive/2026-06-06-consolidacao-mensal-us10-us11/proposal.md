## Why

A API já gerencia receitas e despesas por mês, mas não oferece uma visão consolidada. O usuário precisa de um endpoint que agregue receitas e despesas de uma competência em um único resumo de saldo, e de um histórico anual que exiba os 12 meses de um ano.

## What Changes

- Novo endpoint `GET /api/v1/resumo?competencia=2025-07` que retorna `totalReceitas`, `totalDespesas` e `saldo` de um mês
- Novo endpoint `GET /api/v1/resumo/anual?ano=2025` que retorna lista de resumos mensais dos 12 meses do ano, com totais anuais agregados
- Novo domain model `ResumoMensal` (record puro) e use cases associados
- Meses sem lançamentos retornam valores zerados (sem erro)

## Capabilities

### New Capabilities

- `resumo-mensal`: Consulta de resumo financeiro de um mês — totalReceitas, totalDespesas, saldo calculado
- `historico-anual`: Consulta de histórico anual com lista de ResumoMensal para os 12 meses e totais anuais

### Modified Capabilities

## Impact

- Novos pacotes: `domain/model/ResumoMensal`, `domain/port/in/ConsultarResumoMensalUseCase`, `domain/port/in/ConsultarHistoricoAnualUseCase`
- Reutiliza `BuscarDespesasPort` e `BuscarReceitasPort` existentes — sem novos ports de saída
- Novo `ResumoController` em `adapter/in/web/`
- Novos DTOs `ResumoMensalResponse` e `HistoricoAnualResponse`
- Sem modificações de schema de banco — lê de `despesas` e `receitas` já existentes
