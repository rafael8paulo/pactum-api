## Purpose

Permite que o usuário defina uma meta de economia por mês, acompanhe o progresso, a projeção e o status de desempenho, receba marcos de incentivo e consulte o histórico de resultados mensais.

## ADDED Requirements

### Requirement: Definir ou ajustar a meta de uma competência
A API SHALL aceitar `PUT /api/v1/metas-economia/{competencia}` (`competencia` no formato `yyyy-MM`) com corpo `{ "valor": BigDecimal }` e SHALL criar a meta do usuário autenticado para a competência, ou substituir o valor se já existir (no máximo uma meta por usuário e competência). O `valor` SHALL ser maior que zero; valor inválido SHALL ser rejeitado com `422 Unprocessable Entity` (padrão de validação de corpo da API). A resposta SHALL ser `200 OK` com `competencia` e `valor`. A API SHALL rejeitar com `400 Bad Request` a criação ou alteração de meta de competência anterior ao mês corrente.

#### Scenario: Criar meta para o mês corrente
- **WHEN** `PUT /api/v1/metas-economia/2026-10` é chamado com `valor: 1000.00` e não há meta para 2026-10 (mês corrente)
- **THEN** a resposta é `200 OK` com `competencia: "2026-10"` e `valor: 1000.00`

#### Scenario: Ajustar meta existente
- **WHEN** `PUT /api/v1/metas-economia/2026-10` é chamado com `valor: 1500.00` e já existe meta de 1000.00 para a competência
- **THEN** a meta passa a ter `valor: 1500.00` e continua existindo uma única meta para a competência

#### Scenario: Valor não positivo é rejeitado
- **WHEN** `PUT /api/v1/metas-economia/2026-10` é chamado com `valor: 0` ou valor negativo
- **THEN** a resposta é `422 Unprocessable Entity`

#### Scenario: Competência passada é rejeitada
- **WHEN** `PUT /api/v1/metas-economia/2026-08` é chamado e o mês corrente é 2026-10
- **THEN** a resposta é `400 Bad Request` e nenhuma meta é criada ou alterada

#### Scenario: Metas são isoladas por usuário
- **WHEN** o usuário A define meta para 2026-10 e o usuário B consulta a meta de 2026-10
- **THEN** o usuário B não enxerga a meta do usuário A

### Requirement: Consultar progresso da meta
A API SHALL aceitar `GET /api/v1/metas-economia/{competencia}` e retornar `200 OK` com: `competencia`, `valorMeta`, `valorEconomizado`, `valorRestante`, `percentual`, `diasRestantes`, `valorDiarioNecessario`, `projecaoFechamento`, `status` e `marcosAtingidos`.

`valorEconomizado` SHALL ser `totalReceitas − totalDespesas` da competência (mesmo cálculo do resumo mensal) e MAY ser negativo. `valorRestante` SHALL ser `max(valorMeta − valorEconomizado, 0)`. `percentual` SHALL ser `max(valorEconomizado / valorMeta × 100, 0)` com 2 casas decimais, podendo exceder 100.

`diasRestantes` SHALL contar o dia corrente: no mês corrente é `diasNoMes − diaDoMês + 1`; em competência futura é `diasNoMes`; em competência passada é `0`. `valorDiarioNecessario` SHALL ser `valorRestante / diasRestantes` (2 casas, arredondamento `HALF_UP`) e `0` quando `diasRestantes = 0` ou a meta já foi atingida. `projecaoFechamento` SHALL ser a extrapolação linear `valorEconomizado / diasDecorridos × diasNoMes` no mês corrente (com `diasDecorridos = diaDoMês`), o próprio `valorEconomizado` em competência passada, e `valorEconomizado` em competência futura.

Se não houver meta para a competência, a resposta SHALL ser `404 Not Found`.

#### Scenario: Progresso do exemplo de referência
- **WHEN** existe meta de 1000.00 para a competência corrente, o `valorEconomizado` é 600.00 e restam 10 dias (contando hoje)
- **THEN** a resposta traz `percentual: 60.00`, `valorRestante: 400.00`, `diasRestantes: 10` e `valorDiarioNecessario: 40.00`

#### Scenario: Meta superada
- **WHEN** a meta é 1000.00 e o `valorEconomizado` é 1200.00
- **THEN** `percentual` é `120.00`, `valorRestante` é `0.00` e `valorDiarioNecessario` é `0.00`

#### Scenario: Saldo negativo
- **WHEN** a meta é 1000.00 e as despesas do mês superam as receitas (`valorEconomizado` negativo)
- **THEN** `percentual` é `0.00`, `valorRestante` é igual a `valorMeta` mais o valor absoluto do déficit

#### Scenario: Competência sem meta
- **WHEN** `GET /api/v1/metas-economia/2026-12` é chamado e não há meta para 2026-12
- **THEN** a resposta é `404 Not Found`

### Requirement: Status de desempenho
O campo `status` SHALL ser calculado no mês corrente comparando `valorEconomizado` com o esperado linear `valorMeta × diasDecorridos / diasNoMes`: `CONCLUIDA` quando `valorEconomizado >= valorMeta`; `ADIANTADA` quando `valorEconomizado >= 1,10 × esperado`; `NO_PLANEJADO` quando `valorEconomizado >= 0,90 × esperado`; `EM_RISCO` caso contrário. Em competência passada, `status` SHALL ser `CONCLUIDA` se a meta foi atingida e `EM_RISCO` caso contrário. Em competência futura, `status` SHALL ser `NO_PLANEJADO`.

#### Scenario: Usuário adiantado
- **WHEN** no dia 10 de um mês de 30 dias com meta 900.00 o `valorEconomizado` é 400.00 (esperado 300.00)
- **THEN** `status` é `ADIANTADA`

#### Scenario: Usuário dentro do planejado
- **WHEN** no dia 10 de um mês de 30 dias com meta 900.00 o `valorEconomizado` é 290.00
- **THEN** `status` é `NO_PLANEJADO`

#### Scenario: Usuário em risco
- **WHEN** no dia 20 de um mês de 30 dias com meta 900.00 o `valorEconomizado` é 300.00 (esperado 600.00)
- **THEN** `status` é `EM_RISCO`

#### Scenario: Meta concluída
- **WHEN** o `valorEconomizado` é maior ou igual ao `valorMeta`
- **THEN** `status` é `CONCLUIDA`

### Requirement: Marcos de incentivo
O campo `marcosAtingidos` SHALL ser a lista ordenada, em ordem crescente, dos marcos `25`, `50`, `75` e `100` cujo valor seja menor ou igual ao `percentual` atual. O cálculo SHALL ser sem estado persistido: a lista reflete apenas o percentual no momento da consulta.

#### Scenario: Marcos em 60%
- **WHEN** o `percentual` é `60.00`
- **THEN** `marcosAtingidos` é `[25, 50]`

#### Scenario: Nenhum marco
- **WHEN** o `percentual` é `24.99`
- **THEN** `marcosAtingidos` é uma lista vazia

#### Scenario: Todos os marcos
- **WHEN** o `percentual` é `100.00` ou mais
- **THEN** `marcosAtingidos` é `[25, 50, 75, 100]`

### Requirement: Remover meta de uma competência
A API SHALL aceitar `DELETE /api/v1/metas-economia/{competencia}` e responder `204 No Content` removendo a meta do usuário para a competência. Se não houver meta, a resposta SHALL ser `404 Not Found`. Competência anterior ao mês corrente SHALL ser rejeitada com `400 Bad Request`, preservando o histórico.

#### Scenario: Remover meta futura
- **WHEN** `DELETE /api/v1/metas-economia/2026-11` é chamado e existe meta para 2026-11
- **THEN** a resposta é `204 No Content` e um `GET` subsequente retorna `404`

#### Scenario: Remover meta de mês passado
- **WHEN** `DELETE` é chamado para uma competência anterior ao mês corrente
- **THEN** a resposta é `400 Bad Request` e a meta permanece

### Requirement: Histórico de desempenho
A API SHALL aceitar `GET /api/v1/metas-economia/historico?ano=2026` e retornar `200 OK` com a lista, em ordem cronológica, de todas as competências do ano que possuem meta definida e cujo mês já começou, cada item com `competencia`, `valorMeta`, `valorEconomizado`, `percentual` e `atingida` (`valorEconomizado >= valorMeta`). Meses sem meta SHALL ser omitidos. O resultado SHALL ser calculado a partir dos lançamentos no momento da consulta. O parâmetro `ano` SHALL ser obrigatório.

#### Scenario: Histórico com dois meses
- **WHEN** o usuário tem meta em 2026-08 (atingida) e 2026-09 (não atingida) e consulta `?ano=2026`
- **THEN** a resposta lista 2026-08 com `atingida: true` e 2026-09 com `atingida: false`, nessa ordem

#### Scenario: Ano sem metas
- **WHEN** o usuário consulta um ano sem nenhuma meta
- **THEN** a resposta é `200 OK` com lista vazia

#### Scenario: Parâmetro ano ausente
- **WHEN** `GET /api/v1/metas-economia/historico` é chamado sem `ano`
- **THEN** a resposta é `400 Bad Request`

### Requirement: Autenticação obrigatória
Todos os endpoints de `/api/v1/metas-economia` SHALL exigir usuário autenticado e SHALL operar apenas sobre dados do usuário autenticado.

#### Scenario: Requisição sem autenticação
- **WHEN** qualquer endpoint de metas é chamado sem token válido
- **THEN** a resposta é `401 Unauthorized`
