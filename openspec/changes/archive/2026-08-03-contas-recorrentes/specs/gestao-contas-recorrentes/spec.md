## ADDED Requirements

### Requirement: Modelo de domínio ContaRecorrente
O sistema SHALL representar internamente uma conta recorrente como: `id (UUID)`, `descricao (String, não vazio, max 100)`, `valorPadrao (BigDecimal, positivo)`, `categoria (CategoriaDespesa)`, `diaVencimento (Integer, 1-31, opcional)`, `competenciaInicio (YearMonth)`, `competenciaFim (YearMonth, opcional)`, `status (StatusContaRecorrente)`, `usuarioId (UUID)`. O domínio SHALL ser livre de anotações de framework.

#### Scenario: ContaRecorrente criada com todos os campos
- **WHEN** um record `ContaRecorrente` é instanciado com todos os campos válidos
- **THEN** o objeto é imutável e os getters retornam os valores fornecidos

#### Scenario: ContaRecorrente sem competenciaFim é válida
- **WHEN** um record `ContaRecorrente` é instanciado com `competenciaFim = null`
- **THEN** o objeto é criado normalmente, representando uma recorrência sem data de término

### Requirement: Enum StatusContaRecorrente
O sistema SHALL definir `StatusContaRecorrente` com valores `ATIVA`, `PAUSADA`, `ENCERRADA`.

#### Scenario: Status ATIVA é valor válido
- **WHEN** `StatusContaRecorrente.ATIVA` é referenciado
- **THEN** o valor existe e pode ser usado no domínio

### Requirement: Cadastrar conta recorrente
A API SHALL aceitar `POST /api/v1/contas-recorrentes` com payload contendo `descricao`, `valorPadrao`, `categoria`, `diaVencimento` (opcional), `competenciaInicio`, `competenciaFim` (opcional). Ao persistir com sucesso, SHALL retornar `201 Created` com o recurso completo, incluindo `id` gerado e `status = ATIVA`.

#### Scenario: Cadastro bem-sucedido de financiamento com data de término
- **WHEN** `POST /api/v1/contas-recorrentes` é chamado com `{"descricao":"Financiamento do Carro","valorPadrao":1335.50,"categoria":"FINANCIAMENTO","diaVencimento":10,"competenciaInicio":"2026-01","competenciaFim":"2029-12"}`
- **THEN** a resposta é `201 Created` com body contendo todos os campos, `id` (UUID) e `status: "ATIVA"`

#### Scenario: Cadastro bem-sucedido de assinatura sem data de término
- **WHEN** `POST /api/v1/contas-recorrentes` é chamado com `{"descricao":"Streaming XPTO","valorPadrao":39.90,"categoria":"LAZER","competenciaInicio":"2026-08"}` (sem `competenciaFim`)
- **THEN** a resposta é `201 Created` com `competenciaFim: null`

#### Scenario: Campo obrigatório ausente retorna 422
- **WHEN** `POST /api/v1/contas-recorrentes` é chamado sem o campo `valorPadrao`
- **THEN** a resposta é `422 Unprocessable Entity` com mensagem indicando o campo inválido

#### Scenario: valorPadrao negativo ou zero retorna 422
- **WHEN** `POST /api/v1/contas-recorrentes` é chamado com `"valorPadrao": 0`
- **THEN** a resposta é `422 Unprocessable Entity`

#### Scenario: competenciaFim anterior a competenciaInicio retorna 422
- **WHEN** `POST /api/v1/contas-recorrentes` é chamado com `"competenciaInicio":"2026-08"` e `"competenciaFim":"2026-01"`
- **THEN** a resposta é `422 Unprocessable Entity`

### Requirement: Listar contas recorrentes do usuário
A API SHALL expor `GET /api/v1/contas-recorrentes` retornando todas as contas recorrentes do usuário autenticado, com filtro opcional por `status`.

#### Scenario: Listagem sem filtro retorna todas as contas do usuário
- **WHEN** `GET /api/v1/contas-recorrentes` é chamado
- **THEN** a resposta é `200 OK` com a lista de todas as contas recorrentes do usuário autenticado, independente do status

#### Scenario: Listagem filtrada por status
- **WHEN** `GET /api/v1/contas-recorrentes?status=ATIVA` é chamado
- **THEN** a resposta é `200 OK` contendo apenas contas com `status = ATIVA`

#### Scenario: Contas de outro usuário não aparecem na listagem
- **WHEN** o usuário autenticado A chama `GET /api/v1/contas-recorrentes` e existem contas recorrentes cadastradas pelo usuário B
- **THEN** a resposta não inclui nenhuma conta recorrente pertencente ao usuário B

### Requirement: Editar conta recorrente
A API SHALL expor `PUT /api/v1/contas-recorrentes/{id}` para atualização completa dos campos editáveis (`descricao`, `valorPadrao`, `categoria`, `diaVencimento`, `competenciaInicio`, `competenciaFim`) de uma conta recorrente pertencente ao usuário autenticado.

#### Scenario: Edição bem-sucedida
- **WHEN** `PUT /api/v1/contas-recorrentes/{id}` é chamado com dados válidos para uma conta existente do usuário autenticado
- **THEN** a resposta é `200 OK` com o recurso atualizado

#### Scenario: Editar conta inexistente retorna 404
- **WHEN** `PUT /api/v1/contas-recorrentes/{id}` é chamado com um `id` que não existe
- **THEN** a resposta é `404 Not Found`

#### Scenario: Editar conta de outro usuário retorna 404
- **WHEN** o usuário autenticado A chama `PUT /api/v1/contas-recorrentes/{id}` para um `id` pertencente ao usuário B
- **THEN** a resposta é `404 Not Found`

### Requirement: Atualizar status da conta recorrente
A API SHALL expor `PATCH /api/v1/contas-recorrentes/{id}/status` para alterar o `status` entre `ATIVA`, `PAUSADA` e `ENCERRADA`, sem afetar despesas já geradas.

#### Scenario: Pausar conta recorrente
- **WHEN** `PATCH /api/v1/contas-recorrentes/{id}/status` é chamado com `{"status":"PAUSADA"}` para uma conta `ATIVA`
- **THEN** a resposta é `200 OK` com `status: "PAUSADA"` e as despesas já geradas anteriormente permanecem inalteradas

#### Scenario: Atualizar status de conta inexistente retorna 404
- **WHEN** `PATCH /api/v1/contas-recorrentes/{id}/status` é chamado com um `id` que não existe
- **THEN** a resposta é `404 Not Found`

### Requirement: Remover conta recorrente
A API SHALL expor `DELETE /api/v1/contas-recorrentes/{id}` para remover uma conta recorrente do usuário autenticado, retornando `204 No Content`. Despesas já geradas a partir dela SHALL permanecer no histórico, com o vínculo desfeito.

#### Scenario: Remoção bem-sucedida
- **WHEN** `DELETE /api/v1/contas-recorrentes/{id}` é chamado para uma conta existente do usuário autenticado
- **THEN** a resposta é `204 No Content` e a conta deixa de aparecer em `GET /api/v1/contas-recorrentes`

#### Scenario: Despesas geradas sobrevivem à remoção da conta recorrente
- **WHEN** uma `ContaRecorrente` que já gerou despesas é removida via `DELETE /api/v1/contas-recorrentes/{id}`
- **THEN** as despesas geradas continuam existindo em `GET /api/v1/despesas`, com `contaRecorrenteId` nulo

#### Scenario: Remover conta inexistente retorna 404
- **WHEN** `DELETE /api/v1/contas-recorrentes/{id}` é chamado com um `id` que não existe
- **THEN** a resposta é `404 Not Found`
