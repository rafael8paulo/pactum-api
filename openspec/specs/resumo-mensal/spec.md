## Purpose

Especificação da funcionalidade de resumo mensal financeiro. Define o endpoint `GET /api/v1/resumo`, o modelo de domínio `ResumoMensal` e o caso de uso de cálculo de saldo por competência.

---

## Requirements

### Requirement: Resumo mensal retorna totais e saldo de uma competência
A API SHALL aceitar `GET /api/v1/resumo?competencia=2025-07` e retornar `200 OK` com objeto contendo `competencia (String yyyy-MM)`, `totalReceitas (BigDecimal)`, `totalDespesas (BigDecimal)` e `saldo (BigDecimal)`. O `saldo` SHALL ser calculado como `totalReceitas - totalDespesas`. O parâmetro `competencia` SHALL ser obrigatório no formato `yyyy-MM`.

#### Scenario: Resumo com receitas e despesas existentes
- **WHEN** `GET /api/v1/resumo?competencia=2025-07` é chamado e existem receitas e despesas em julho/2025
- **THEN** a resposta é `200 OK` com `totalReceitas`, `totalDespesas` e `saldo` corretos

#### Scenario: Resumo de mês sem lançamentos retorna zeros
- **WHEN** `GET /api/v1/resumo?competencia=2025-01` é chamado e não há lançamentos em janeiro/2025
- **THEN** a resposta é `200 OK` com `totalReceitas: 0`, `totalDespesas: 0`, `saldo: 0`

#### Scenario: Competência ausente retorna 400
- **WHEN** `GET /api/v1/resumo` é chamado sem o parâmetro `competencia`
- **THEN** a resposta é `400 Bad Request`

### Requirement: Modelo de domínio ResumoMensal
O sistema SHALL representar internamente um resumo mensal como record puro: `competencia (YearMonth)`, `totalReceitas (BigDecimal)`, `totalDespesas (BigDecimal)`, `saldo (BigDecimal)`. O domínio SHALL ser livre de anotações de framework.

#### Scenario: ResumoMensal criado com todos os campos
- **WHEN** um record `ResumoMensal` é instanciado com `competencia`, `totalReceitas`, `totalDespesas` e `saldo`
- **THEN** o objeto é imutável e os getters retornam os valores fornecidos

### Requirement: Cálculo de saldo no use case
O `ConsultarResumoMensalUseCase` SHALL buscar as listas de receitas e despesas para a competência via ports de saída existentes (`BuscarReceitasPort` e `BuscarDespesasPort`) e calcular o saldo. Nenhum novo port de saída SHALL ser criado para esta funcionalidade.

#### Scenario: Saldo calculado corretamente
- **WHEN** `ConsultarResumoMensalUseCase.consultar(YearMonth)` é invocado
- **THEN** o service busca receitas e despesas via ports existentes e retorna `ResumoMensal` com `saldo = totalReceitas - totalDespesas`
