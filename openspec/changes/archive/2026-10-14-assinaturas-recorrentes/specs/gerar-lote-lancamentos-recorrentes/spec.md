## MODIFIED Requirements

### Requirement: Gerar todos os lançamentos de uma conta recorrente de uma vez
A API SHALL expor `POST /api/v1/contas-recorrentes/{id}/gerar-todos`. Para a conta recorrente identificada por `{id}`, pertencente ao usuário autenticado, o sistema SHALL criar uma `Despesa` para cada competência elegível entre `competenciaInicio` e `competenciaFim` (inclusive), na mesma forma da geração mensal (`descricao`, `categoria` e `valor = valorPadrao` copiados da conta, `status = PENDENTE`, `contaRecorrenteId` preenchido). Uma competência é elegível seguindo a mesma regra de `frequencia` usada na geração individual (`MENSAL`: toda competência; `TRIMESTRAL`: a cada 3 meses desde `competenciaInicio`; `ANUAL`: a cada 12 meses desde `competenciaInicio`; `SEMANAL`: nenhuma competência é elegível). A resposta SHALL ser `200 OK` com a lista de despesas efetivamente criadas nessa chamada.

#### Scenario: Geração em lote cria uma despesa por competência elegível no intervalo
- **WHEN** uma conta recorrente `ATIVA` com `frequencia = MENSAL` tem `competenciaInicio = "2026-01"` e `competenciaFim = "2026-04"`, e o usuário chama `POST /api/v1/contas-recorrentes/{id}/gerar-todos`
- **THEN** a resposta é `200 OK` com 4 despesas criadas, uma para cada competência de `2026-01` a `2026-04`, todas com `status: "PENDENTE"` e `contaRecorrenteId` igual a `{id}`

#### Scenario: Geração em lote trimestral cria apenas despesas dos meses do ciclo
- **WHEN** uma conta recorrente `ATIVA` com `frequencia = TRIMESTRAL` tem `competenciaInicio = "2026-01"` e `competenciaFim = "2026-07"`, e o usuário chama `POST /api/v1/contas-recorrentes/{id}/gerar-todos`
- **THEN** a resposta é `200 OK` com despesas criadas apenas para `2026-01`, `2026-04` e `2026-07`, e nenhuma para os demais meses do intervalo

#### Scenario: Geração em lote de conta semanal não cria nenhuma despesa
- **WHEN** uma conta recorrente `ATIVA` com `frequencia = SEMANAL` tem `competenciaInicio = "2026-01"` e `competenciaFim = "2026-04"`, e o usuário chama `POST /api/v1/contas-recorrentes/{id}/gerar-todos`
- **THEN** a resposta é `200 OK` com uma lista vazia

---

### Requirement: Limite de geração para conta sem competenciaFim é o mês atual
Quando a conta recorrente não tiver `competenciaFim` definido, a geração em lote SHALL considerar como limite superior o mês corrente — competências futuras além do mês atual NÃO SHALL ser geradas. Esse limite se aplica antes do filtro de elegibilidade por `frequencia`.

#### Scenario: Conta indefinida gera até o mês atual
- **WHEN** uma conta recorrente `ATIVA` com `frequencia = MENSAL` tem `competenciaInicio = "2026-01"` e `competenciaFim = null`, e o mês atual do sistema é `2026-08`, e o usuário chama `POST /api/v1/contas-recorrentes/{id}/gerar-todos`
- **THEN** são criadas despesas para as competências de `2026-01` até `2026-08` (inclusive), e nenhuma despesa é criada para competências posteriores a `2026-08`
