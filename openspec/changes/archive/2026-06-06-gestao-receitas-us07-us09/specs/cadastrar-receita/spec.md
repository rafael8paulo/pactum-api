## ADDED Requirements

### Requirement: Receita cadastrada com campos válidos retorna 201
A API SHALL aceitar `POST /api/v1/receitas` com payload contendo `descricao`, `valor`, `competencia` e `categoria`. Ao persistir com sucesso, SHALL retornar `201 Created` com o recurso completo incluindo o `id` gerado (UUID).

#### Scenario: Cadastro bem-sucedido
- **WHEN** `POST /api/v1/receitas` é chamado com `{"descricao":"Salário","valor":5136.38,"competencia":"2025-07","categoria":"SALARIO"}`
- **THEN** a resposta é `201 Created` com body contendo todos os campos incluindo `id` (UUID)

#### Scenario: Campo obrigatório ausente retorna 422
- **WHEN** `POST /api/v1/receitas` é chamado sem o campo `valor`
- **THEN** a resposta é `422 Unprocessable Entity` com mensagem indicando o campo inválido

#### Scenario: Valor negativo retorna 422
- **WHEN** `POST /api/v1/receitas` é chamado com `"valor": -100`
- **THEN** a resposta é `422 Unprocessable Entity`

#### Scenario: Categoria inválida retorna 400
- **WHEN** `POST /api/v1/receitas` é chamado com `"categoria": "INVALIDA"`
- **THEN** a resposta é `400 Bad Request`

### Requirement: Modelo de domínio Receita
O sistema SHALL representar internamente uma receita como: `id (UUID)`, `descricao (String, não vazio, max 100)`, `valor (BigDecimal, positivo)`, `competencia (YearMonth)`, `categoria (CategoriaReceita)`. O domínio SHALL ser livre de anotações de framework. Receita não possui campo `status`.

#### Scenario: Receita criada com todos os campos
- **WHEN** um record `Receita` é instanciado com todos os campos válidos
- **THEN** o objeto é imutável e os getters retornam os valores fornecidos

### Requirement: Enum CategoriaReceita
O sistema SHALL definir `CategoriaReceita` com valores `SALARIO`, `FREELANCE`, `BONUS`, `OUTROS`.

#### Scenario: Categoria SALARIO é valor válido
- **WHEN** `CategoriaReceita.SALARIO` é referenciado
- **THEN** o valor existe e pode ser usado no domínio
