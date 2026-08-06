## ADDED Requirements

### Requirement: Gerar todos os lançamentos de uma conta recorrente de uma vez
A API SHALL expor `POST /api/v1/contas-recorrentes/{id}/gerar-todos`. Para a conta recorrente identificada por `{id}`, pertencente ao usuário autenticado, o sistema SHALL criar uma `Despesa` para cada competência entre `competenciaInicio` e `competenciaFim` (inclusive), na mesma forma da geração mensal (`descricao`, `categoria` e `valor = valorPadrao` copiados da conta, `status = PENDENTE`, `contaRecorrenteId` preenchido). A resposta SHALL ser `200 OK` com a lista de despesas efetivamente criadas nessa chamada.

#### Scenario: Geração em lote cria uma despesa por competência no intervalo
- **WHEN** uma conta recorrente `ATIVA` tem `competenciaInicio = "2026-01"` e `competenciaFim = "2026-04"`, e o usuário chama `POST /api/v1/contas-recorrentes/{id}/gerar-todos`
- **THEN** a resposta é `200 OK` com 4 despesas criadas, uma para cada competência de `2026-01` a `2026-04`, todas com `status: "PENDENTE"` e `contaRecorrenteId` igual a `{id}`

### Requirement: Limite de geração para conta sem competenciaFim é o mês atual
Quando a conta recorrente não tiver `competenciaFim` definido, a geração em lote SHALL considerar como limite superior o mês corrente — competências futuras além do mês atual NÃO SHALL ser geradas.

#### Scenario: Conta indefinida gera até o mês atual
- **WHEN** uma conta recorrente `ATIVA` tem `competenciaInicio = "2026-01"` e `competenciaFim = null`, e o mês atual do sistema é `2026-08`, e o usuário chama `POST /api/v1/contas-recorrentes/{id}/gerar-todos`
- **THEN** são criadas despesas para as competências de `2026-01` até `2026-08` (inclusive), e nenhuma despesa é criada para competências posteriores a `2026-08`

### Requirement: Conta com competenciaFim gera até a data definida, mesmo no futuro
Quando a conta recorrente tiver `competenciaFim` definido, a geração em lote SHALL gerar despesas até essa competência, mesmo que seja posterior ao mês atual.

#### Scenario: Financiamento com término futuro gera todas as parcelas de uma vez
- **WHEN** uma conta recorrente `ATIVA` tem `competenciaInicio = "2026-01"` e `competenciaFim = "2029-12"`, e o mês atual do sistema é `2026-08`, e o usuário chama `POST /api/v1/contas-recorrentes/{id}/gerar-todos`
- **THEN** são criadas despesas para todas as competências de `2026-01` até `2029-12` (inclusive), incluindo competências futuras em relação ao mês atual

### Requirement: Geração em lote é idempotente por competência
Competências que já possuem despesa gerada para aquela conta recorrente SHALL ser puladas — a geração em lote não duplica nem sobrescreve despesas já existentes (geradas anteriormente ou editadas manualmente pelo usuário).

#### Scenario: Chamar gerar-todos duas vezes não duplica despesas já geradas
- **WHEN** o usuário chama `POST /api/v1/contas-recorrentes/{id}/gerar-todos` e, em seguida, chama o mesmo endpoint novamente para a mesma conta
- **THEN** a segunda chamada retorna uma lista vazia, sem novas despesas criadas

#### Scenario: Competência já gerada individualmente é preservada
- **WHEN** uma competência específica dentro do intervalo já teve despesa gerada via `POST /contas-recorrentes/gerar?competencia=...` e o usuário edita o valor dessa despesa, e então chama `POST /api/v1/contas-recorrentes/{id}/gerar-todos`
- **THEN** essa competência não é regenerada e a edição do usuário permanece intacta

### Requirement: Só conta ATIVA gera lançamentos em lote
Se a conta recorrente não estiver com `status = ATIVA` (estiver `PAUSADA` ou `ENCERRADA`), a geração em lote SHALL retornar uma lista vazia, sem criar nenhuma despesa e sem erro.

#### Scenario: Conta pausada não gera nenhuma despesa em lote
- **WHEN** uma conta recorrente com `status = PAUSADA` recebe `POST /api/v1/contas-recorrentes/{id}/gerar-todos`
- **THEN** a resposta é `200 OK` com uma lista vazia

### Requirement: Conta inexistente ou de outro usuário retorna 404
A API SHALL retornar `404 Not Found` se a conta recorrente não existir ou não pertencer ao usuário autenticado.

#### Scenario: Gerar em lote para conta inexistente
- **WHEN** `POST /api/v1/contas-recorrentes/{id}/gerar-todos` é chamado com um `id` que não existe
- **THEN** a resposta é `404 Not Found`

#### Scenario: Gerar em lote para conta de outro usuário
- **WHEN** o usuário autenticado A chama `POST /api/v1/contas-recorrentes/{id}/gerar-todos` para um `id` pertencente ao usuário B
- **THEN** a resposta é `404 Not Found`
