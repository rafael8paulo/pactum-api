## ADDED Requirements

### Requirement: Receitas listadas por competência com total agregado
A API SHALL aceitar `GET /api/v1/receitas?competencia=2025-07` e retornar `200 OK` com objeto contendo `receitas: List<ReceitaResponse>` e `total: BigDecimal` (soma dos valores retornados). O parâmetro `competencia` SHALL ser obrigatório no formato `yyyy-MM`.

#### Scenario: Listagem com competência retorna receitas do mês
- **WHEN** `GET /api/v1/receitas?competencia=2025-07` é chamado e existem receitas em julho/2025
- **THEN** a resposta é `200 OK` com `receitas` contendo apenas as receitas de julho/2025 e `total` com a soma dos valores

#### Scenario: Listagem sem receitas retorna lista vazia
- **WHEN** `GET /api/v1/receitas?competencia=2025-07` é chamado e não há receitas em julho/2025
- **THEN** a resposta é `200 OK` com `receitas: []` e `total: 0`

#### Scenario: Competência ausente retorna 400
- **WHEN** `GET /api/v1/receitas` é chamado sem o parâmetro `competencia`
- **THEN** a resposta é `400 Bad Request`

### Requirement: Filtro opcional de categoria na listagem de receitas
A API SHALL aceitar o parâmetro opcional `categoria` em `GET /api/v1/receitas`. Quando fornecido, SHALL filtrar as receitas pelo valor exato do enum. Quando ausente, SHALL retornar receitas de todas as categorias.

#### Scenario: Filtro por categoria aplicado
- **WHEN** `GET /api/v1/receitas?competencia=2025-07&categoria=SALARIO` é chamado
- **THEN** a resposta contém apenas receitas da categoria `SALARIO` no mês

#### Scenario: Listagem sem filtro retorna todas as categorias
- **WHEN** `GET /api/v1/receitas?competencia=2025-07` é chamado sem `categoria`
- **THEN** a resposta contém receitas de todas as categorias do mês
