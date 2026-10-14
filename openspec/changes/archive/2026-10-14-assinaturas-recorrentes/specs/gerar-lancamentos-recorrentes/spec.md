## MODIFIED Requirements

### Requirement: Gerar despesas do mês a partir das contas recorrentes ativas
A API SHALL expor `POST /api/v1/contas-recorrentes/gerar?competencia=yyyy-MM`. Para cada `ContaRecorrente` do usuário autenticado com `status = ATIVA` cuja janela de vigência cobre a `competencia` informada (`competenciaInicio <= competencia` e, se `competenciaFim` não for nulo, `competencia <= competenciaFim`) **e cuja `frequencia` torna a competência elegível**, o sistema SHALL criar uma `Despesa` com `descricao`, `categoria` e `valor = valorPadrao` copiados da conta recorrente, `status = PENDENTE`, `competencia` igual à informada e `contaRecorrenteId` apontando para a conta recorrente de origem. A resposta SHALL ser `200 OK` contendo a lista de despesas efetivamente criadas nessa chamada.

Uma competência é elegível conforme a `frequencia` da conta, contando os meses decorridos desde `competenciaInicio` (`mesesDesdeInicio`): `MENSAL` → toda competência coberta é elegível; `TRIMESTRAL` → elegível quando `mesesDesdeInicio % 3 == 0`; `ANUAL` → elegível quando `mesesDesdeInicio % 12 == 0`; `SEMANAL` → nenhuma competência é elegível (a conta nunca gera despesa por este endpoint).

#### Scenario: Geração cria despesas para todas as contas ativas elegíveis
- **WHEN** o usuário autenticado tem 3 contas recorrentes `ATIVA` com `frequencia = MENSAL` cuja vigência cobre `2026-08` e chama `POST /api/v1/contas-recorrentes/gerar?competencia=2026-08`
- **THEN** a resposta é `200 OK` com 3 despesas criadas, cada uma com `status: "PENDENTE"` e `contaRecorrenteId` correspondente à conta de origem

#### Scenario: Conta recorrente pausada não gera despesa
- **WHEN** o usuário autenticado tem uma conta recorrente com `status = PAUSADA` e chama `POST /api/v1/contas-recorrentes/gerar?competencia=2026-08`
- **THEN** nenhuma despesa é criada para essa conta

#### Scenario: Conta recorrente fora da janela de vigência não gera despesa
- **WHEN** uma conta recorrente tem `competenciaFim = "2026-06"` e o usuário chama `POST /api/v1/contas-recorrentes/gerar?competencia=2026-08`
- **THEN** nenhuma despesa é criada para essa conta

#### Scenario: Conta recorrente sem competenciaFim gera despesa indefinidamente
- **WHEN** uma conta recorrente `ATIVA` com `frequencia = MENSAL` tem `competenciaInicio = "2026-01"` e `competenciaFim = null`, e o usuário chama `POST /api/v1/contas-recorrentes/gerar?competencia=2026-08`
- **THEN** uma despesa é criada normalmente para essa conta

#### Scenario: Conta trimestral só gera despesa em competências múltiplas de 3 meses desde o início
- **WHEN** uma conta recorrente `ATIVA` com `frequencia = TRIMESTRAL` tem `competenciaInicio = "2026-01"`, e o usuário chama `POST /api/v1/contas-recorrentes/gerar?competencia=2026-03"`
- **THEN** nenhuma despesa é criada para essa conta, pois `2026-03` está 2 meses após o início (não é múltiplo de 3)

#### Scenario: Conta trimestral gera despesa na competência correspondente ao ciclo
- **WHEN** uma conta recorrente `ATIVA` com `frequencia = TRIMESTRAL` tem `competenciaInicio = "2026-01"`, e o usuário chama `POST /api/v1/contas-recorrentes/gerar?competencia=2026-04"`
- **THEN** uma despesa é criada para essa conta, pois `2026-04` está 3 meses após o início

#### Scenario: Conta semanal nunca gera despesa por este endpoint
- **WHEN** uma conta recorrente `ATIVA` com `frequencia = SEMANAL` tem vigência cobrindo `2026-08`, e o usuário chama `POST /api/v1/contas-recorrentes/gerar?competencia=2026-08`
- **THEN** nenhuma despesa é criada para essa conta, mesmo estando `ATIVA` e dentro da janela de vigência
