## ADDED Requirements

### Requirement: Gerar despesas do mês a partir das contas recorrentes ativas
A API SHALL expor `POST /api/v1/contas-recorrentes/gerar?competencia=yyyy-MM`. Para cada `ContaRecorrente` do usuário autenticado com `status = ATIVA` cuja janela de vigência cobre a `competencia` informada (`competenciaInicio <= competencia` e, se `competenciaFim` não for nulo, `competencia <= competenciaFim`), o sistema SHALL criar uma `Despesa` com `descricao`, `categoria` e `valor = valorPadrao` copiados da conta recorrente, `status = PENDENTE`, `competencia` igual à informada e `contaRecorrenteId` apontando para a conta recorrente de origem. A resposta SHALL ser `200 OK` contendo a lista de despesas efetivamente criadas nessa chamada.

#### Scenario: Geração cria despesas para todas as contas ativas elegíveis
- **WHEN** o usuário autenticado tem 3 contas recorrentes `ATIVA` cuja vigência cobre `2026-08` e chama `POST /api/v1/contas-recorrentes/gerar?competencia=2026-08`
- **THEN** a resposta é `200 OK` com 3 despesas criadas, cada uma com `status: "PENDENTE"` e `contaRecorrenteId` correspondente à conta de origem

#### Scenario: Conta recorrente pausada não gera despesa
- **WHEN** o usuário autenticado tem uma conta recorrente com `status = PAUSADA` e chama `POST /api/v1/contas-recorrentes/gerar?competencia=2026-08`
- **THEN** nenhuma despesa é criada para essa conta

#### Scenario: Conta recorrente fora da janela de vigência não gera despesa
- **WHEN** uma conta recorrente tem `competenciaFim = "2026-06"` e o usuário chama `POST /api/v1/contas-recorrentes/gerar?competencia=2026-08`
- **THEN** nenhuma despesa é criada para essa conta

#### Scenario: Conta recorrente sem competenciaFim gera despesa indefinidamente
- **WHEN** uma conta recorrente `ATIVA` tem `competenciaInicio = "2026-01"` e `competenciaFim = null`, e o usuário chama `POST /api/v1/contas-recorrentes/gerar?competencia=2026-08`
- **THEN** uma despesa é criada normalmente para essa conta

### Requirement: Geração é idempotente por competência
Chamar `POST /api/v1/contas-recorrentes/gerar` mais de uma vez para a mesma `competencia` SHALL NOT criar despesas duplicadas para a mesma conta recorrente. Uma conta recorrente já processada para aquela competência SHALL ser ignorada em chamadas subsequentes, mesmo que a despesa gerada tenha sido editada ou tenha seu status alterado.

#### Scenario: Segunda chamada para a mesma competência não duplica
- **WHEN** `POST /api/v1/contas-recorrentes/gerar?competencia=2026-08` é chamado duas vezes seguidas para o mesmo usuário
- **THEN** a primeira chamada retorna as despesas criadas e a segunda chamada retorna uma lista vazia, sem novas despesas em `GET /api/v1/despesas?competencia=2026-08`

#### Scenario: Despesa gerada e depois editada não é regenerada
- **WHEN** uma despesa é gerada para uma conta recorrente na competência `2026-08`, o usuário edita o `valor` dessa despesa via `PUT /api/v1/despesas/{id}`, e então chama `POST /api/v1/contas-recorrentes/gerar?competencia=2026-08` novamente
- **THEN** nenhuma despesa nova é criada para aquela conta recorrente e a edição do usuário permanece intacta

### Requirement: Geração é isolada por usuário
A geração de lançamentos SHALL considerar apenas as contas recorrentes pertencentes ao usuário autenticado.

#### Scenario: Geração não afeta contas de outros usuários
- **WHEN** o usuário autenticado A chama `POST /api/v1/contas-recorrentes/gerar?competencia=2026-08`
- **THEN** nenhuma despesa é criada a partir de contas recorrentes pertencentes a outros usuários

### Requirement: Parâmetro competencia inválido retorna 400
A API SHALL validar o parâmetro `competencia` no formato `yyyy-MM`.

#### Scenario: Competência ausente retorna 400
- **WHEN** `POST /api/v1/contas-recorrentes/gerar` é chamado sem o parâmetro `competencia`
- **THEN** a resposta é `400 Bad Request`

#### Scenario: Formato de competência inválido retorna 400
- **WHEN** `POST /api/v1/contas-recorrentes/gerar?competencia=agosto-2026` é chamado
- **THEN** a resposta é `400 Bad Request`
