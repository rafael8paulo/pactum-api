## ADDED Requirements

### Requirement: Despesa cadastrada com campos válidos retorna 201
A API SHALL aceitar `POST /api/v1/despesas` com um payload JSON contendo `descricao`, `valor`, `status`, `competencia` e `categoria`. Ao persistir com sucesso, SHALL retornar `201 Created` com o recurso completo no body, incluindo o `id` gerado (UUID).

#### Scenario: Cadastro bem-sucedido
- **WHEN** `POST /api/v1/despesas` é chamado com `{"descricao":"Financiamento do Carro","valor":1335.50,"status":"PAGA","competencia":"2025-07","categoria":"FINANCIAMENTO"}`
- **THEN** a resposta é `201 Created` com body contendo todos os campos incluindo `id` (UUID)

#### Scenario: Campo obrigatório ausente retorna 422
- **WHEN** `POST /api/v1/despesas` é chamado sem o campo `valor`
- **THEN** a resposta é `422 Unprocessable Entity` com mensagem indicando o campo inválido

#### Scenario: Valor negativo ou zero retorna 422
- **WHEN** `POST /api/v1/despesas` é chamado com `"valor": -100`
- **THEN** a resposta é `422 Unprocessable Entity`

#### Scenario: Status inválido retorna 400
- **WHEN** `POST /api/v1/despesas` é chamado com `"status": "INVALIDO"`
- **THEN** a resposta é `400 Bad Request`

### Requirement: Modelo de domínio Despesa
O sistema SHALL representar internamente uma despesa como: `id (UUID)`, `descricao (String, não vazio, max 100)`, `valor (BigDecimal, positivo)`, `status (StatusDespesa)`, `competencia (YearMonth)`, `categoria (CategoriaDespesa)`. O domínio SHALL ser livre de anotações de framework.

#### Scenario: Despesa criada com todos os campos
- **WHEN** um record `Despesa` é instanciado com todos os campos válidos
- **THEN** o objeto é imutável e os getters retornam os valores fornecidos

### Requirement: Enums StatusDespesa e CategoriaDespesa
O sistema SHALL definir `StatusDespesa` com valores `PAGA`, `PENDENTE`, `AGENDADA` e `CategoriaDespesa` com valores `FINANCIAMENTO`, `CARTAO_CREDITO`, `EDUCACAO`, `SERVICOS`, `LAZER`, `IMPOSTO`, `OUTROS`.

#### Scenario: Status PAGA é valor válido
- **WHEN** `StatusDespesa.PAGA` é referenciado
- **THEN** o valor existe e pode ser usado no domínio

#### Scenario: Categoria FINANCIAMENTO é valor válido
- **WHEN** `CategoriaDespesa.FINANCIAMENTO` é referenciado
- **THEN** o valor existe e pode ser usado no domínio
