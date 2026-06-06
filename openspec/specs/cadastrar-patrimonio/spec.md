## Purpose

Spec para o cadastro de itens de patrimônio via `POST /api/v1/patrimonio`. Define o modelo de domínio `Patrimonio` e os contratos de resposta para criação bem-sucedida e validação de campos.

---

## Requirements

### Requirement: Item de patrimônio cadastrado com campos válidos retorna 201
A API SHALL aceitar `POST /api/v1/patrimonio` com payload contendo `descricao`, `valor` e `competencia`. Ao persistir com sucesso, SHALL retornar `201 Created` com o recurso completo incluindo o `id` gerado (UUID).

#### Scenario: Cadastro bem-sucedido
- **WHEN** `POST /api/v1/patrimonio` é chamado com `{"descricao":"Caixinha Turbo Nubank","valor":5069.02,"competencia":"2025-07"}`
- **THEN** a resposta é `201 Created` com body contendo todos os campos incluindo `id` (UUID)

#### Scenario: Campo obrigatório ausente retorna 422
- **WHEN** `POST /api/v1/patrimonio` é chamado sem o campo `valor`
- **THEN** a resposta é `422 Unprocessable Entity` com mensagem indicando o campo inválido

#### Scenario: Valor negativo retorna 422
- **WHEN** `POST /api/v1/patrimonio` é chamado com `"valor": -100`
- **THEN** a resposta é `422 Unprocessable Entity`

### Requirement: Modelo de domínio Patrimonio
O sistema SHALL representar internamente um item de patrimônio como: `id (UUID)`, `descricao (String, não vazio, max 100)`, `valor (BigDecimal, positivo)`, `competencia (YearMonth)`. O domínio SHALL ser livre de anotações de framework. Patrimônio não possui campo `categoria` nem `status`.

#### Scenario: Patrimonio criado com todos os campos
- **WHEN** um record `Patrimonio` é instanciado com todos os campos válidos
- **THEN** o objeto é imutável e os getters retornam os valores fornecidos
