## MODIFIED Requirements

### Requirement: Modelo de domínio Despesa
O sistema SHALL representar internamente uma despesa como: `id (UUID)`, `descricao (String, não vazio, max 100)`, `valor (BigDecimal, positivo)`, `status (StatusDespesa)`, `competencia (YearMonth)`, `categoria (CategoriaDespesa)`, `contaRecorrenteId (UUID, opcional)`. O campo `contaRecorrenteId` SHALL ser nulo para despesas cadastradas manualmente e SHALL apontar para a conta recorrente de origem quando a despesa for criada pela geração de lançamentos recorrentes. O domínio SHALL ser livre de anotações de framework.

#### Scenario: Despesa criada com todos os campos
- **WHEN** um record `Despesa` é instanciado com todos os campos válidos
- **THEN** o objeto é imutável e os getters retornam os valores fornecidos

#### Scenario: Despesa cadastrada manualmente tem contaRecorrenteId nulo
- **WHEN** uma despesa é criada via `POST /api/v1/despesas`
- **THEN** o campo `contaRecorrenteId` é `null`

#### Scenario: Despesa gerada por conta recorrente referencia a origem
- **WHEN** uma despesa é criada pela geração de lançamentos recorrentes a partir de uma `ContaRecorrente` com `id X`
- **THEN** o campo `contaRecorrenteId` da despesa criada é igual a `X`
