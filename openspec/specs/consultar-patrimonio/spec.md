## Purpose

Spec para a consulta de itens de patrimônio por competência via `GET /api/v1/patrimonio?competencia=`. Define o contrato de resposta agregada (lista + total) e o comportamento de retornar lista vazia ao invés de 404 quando não há itens no mês.

---

## Requirements

### Requirement: Patrimônio consultado por competência retorna lista e total
A API SHALL aceitar `GET /api/v1/patrimonio?competencia=2025-07` e retornar `200 OK` com objeto contendo `itens: List<PatrimonioResponse>` e `totalPatrimonio: BigDecimal` (soma dos valores retornados). O parâmetro `competencia` SHALL ser obrigatório no formato `yyyy-MM`.

#### Scenario: Consulta com itens existentes no mês
- **WHEN** `GET /api/v1/patrimonio?competencia=2025-07` é chamado e existem itens em julho/2025
- **THEN** a resposta é `200 OK` com `itens` contendo os registros do mês e `totalPatrimonio` com a soma dos valores

#### Scenario: Consulta sem itens retorna lista vazia
- **WHEN** `GET /api/v1/patrimonio?competencia=2025-01` é chamado e não há itens em janeiro/2025
- **THEN** a resposta é `200 OK` com `itens: []` e `totalPatrimonio: 0`

#### Scenario: Competência ausente retorna 400
- **WHEN** `GET /api/v1/patrimonio` é chamado sem o parâmetro `competencia`
- **THEN** a resposta é `400 Bad Request`

### Requirement: PatrimonioNaoEncontradoException não existe — consulta nunca retorna 404
Diferente de despesas e receitas, a consulta de patrimônio por competência SHALL retornar sempre `200 OK` com lista vazia quando não há itens — nunca `404 Not Found`.

#### Scenario: Competência sem itens retorna 200 com lista vazia
- **WHEN** `GET /api/v1/patrimonio?competencia=2000-01` é chamado para um mês sem nenhum item cadastrado
- **THEN** a resposta é `200 OK` com `itens: []` e `totalPatrimonio: 0.00`
