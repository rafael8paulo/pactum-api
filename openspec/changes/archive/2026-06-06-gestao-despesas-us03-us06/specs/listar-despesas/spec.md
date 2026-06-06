## ADDED Requirements

### Requirement: Despesas listadas por competência com total agregado
A API SHALL aceitar `GET /api/v1/despesas?competencia=2025-07` e retornar `200 OK` com um objeto contendo `despesas: List<DespesaResponse>` e `total: BigDecimal` (soma dos valores das despesas retornadas). O parâmetro `competencia` SHALL ser obrigatório no formato `yyyy-MM`.

#### Scenario: Listagem com competência retorna despesas do mês
- **WHEN** `GET /api/v1/despesas?competencia=2025-07` é chamado e existem despesas em julho/2025
- **THEN** a resposta é `200 OK` com `despesas` contendo apenas as despesas de julho/2025 e `total` com a soma dos valores

#### Scenario: Listagem sem despesas retorna lista vazia
- **WHEN** `GET /api/v1/despesas?competencia=2025-07` é chamado e não há despesas em julho/2025
- **THEN** a resposta é `200 OK` com `despesas: []` e `total: 0`

#### Scenario: Competência ausente retorna 400
- **WHEN** `GET /api/v1/despesas` é chamado sem o parâmetro `competencia`
- **THEN** a resposta é `400 Bad Request`

### Requirement: Filtros opcionais de categoria e status na listagem
A API SHALL aceitar os parâmetros opcionais `categoria` e `status` em `GET /api/v1/despesas`. Quando fornecidos, SHALL filtrar as despesas pelo valor exato do enum correspondente. Quando ausentes, SHALL retornar despesas de todas as categorias/status.

#### Scenario: Filtro por categoria aplicado
- **WHEN** `GET /api/v1/despesas?competencia=2025-07&categoria=FINANCIAMENTO` é chamado
- **THEN** a resposta contém apenas despesas da categoria `FINANCIAMENTO` no mês

#### Scenario: Filtro por status aplicado
- **WHEN** `GET /api/v1/despesas?competencia=2025-07&status=PAGA` é chamado
- **THEN** a resposta contém apenas despesas com status `PAGA` no mês

#### Scenario: Filtros combinados aplicados
- **WHEN** `GET /api/v1/despesas?competencia=2025-07&categoria=FINANCIAMENTO&status=PAGA` é chamado
- **THEN** a resposta contém apenas despesas que satisfazem ambos os filtros
