# Spec: Gestão de Contas Recorrentes

## Purpose

Define o modelo de domínio `ContaRecorrente` e os endpoints REST para cadastrar, listar, editar, atualizar status e remover contas recorrentes (despesas fixas/recorrentes de um usuário), que servem de base para a geração automática de lançamentos de despesa.

---

## Requirements

### Requirement: Modelo de domínio ContaRecorrente
O sistema SHALL representar internamente uma conta recorrente como: `id (UUID)`, `descricao (String, não vazio, max 100)`, `valorPadrao (BigDecimal, positivo)`, `categoria (CategoriaDespesa)`, `diaVencimento (Integer, 1-31, opcional)`, `competenciaInicio (YearMonth)`, `competenciaFim (YearMonth, opcional)`, `status (StatusContaRecorrente)`, `usuarioId (UUID)`, `frequencia (FrequenciaCobranca)`, `formaPagamentoId (UUID, opcional)`, `dataBaseCobranca (LocalDate)`. O domínio SHALL ser livre de anotações de framework.

O domínio SHALL expor um método `proximaCobranca(LocalDate hoje)` que calcula a próxima data de cobrança avançando `dataBaseCobranca` em incrementos correspondentes à `frequencia` (7 dias para `SEMANAL`, 1 mês para `MENSAL`, 3 meses para `TRIMESTRAL`, 1 ano para `ANUAL`) até obter a primeira data maior ou igual a `hoje`. Contas com `status` diferente de `ATIVA` SHALL retornar `Optional.empty()` para `proximaCobranca`.

#### Scenario: ContaRecorrente criada com todos os campos
- **WHEN** um record `ContaRecorrente` é instanciado com todos os campos válidos
- **THEN** o objeto é imutável e os getters retornam os valores fornecidos

#### Scenario: ContaRecorrente sem competenciaFim é válida
- **WHEN** um record `ContaRecorrente` é instanciado com `competenciaFim = null`
- **THEN** o objeto é criado normalmente, representando uma recorrência sem data de término

#### Scenario: ContaRecorrente sem formaPagamentoId é válida
- **WHEN** um record `ContaRecorrente` é instanciado com `formaPagamentoId = null`
- **THEN** o objeto é criado normalmente

#### Scenario: proximaCobranca mensal avança em meses a partir da dataBaseCobranca
- **WHEN** uma `ContaRecorrente` `ATIVA` tem `frequencia = MENSAL`, `dataBaseCobranca = "2026-08-10"`, e `proximaCobranca` é calculada com `hoje = "2026-08-20"`
- **THEN** o resultado é `"2026-09-10"`

#### Scenario: proximaCobranca semanal avança em semanas
- **WHEN** uma `ContaRecorrente` `ATIVA` tem `frequencia = SEMANAL`, `dataBaseCobranca = "2026-08-04"` (uma terça-feira), e `proximaCobranca` é calculada com `hoje = "2026-08-10"`
- **THEN** o resultado é `"2026-08-11"`

#### Scenario: proximaCobranca trimestral avança em blocos de 3 meses
- **WHEN** uma `ContaRecorrente` `ATIVA` tem `frequencia = TRIMESTRAL`, `dataBaseCobranca = "2026-01-15"`, e `proximaCobranca` é calculada com `hoje = "2026-08-20"`
- **THEN** o resultado é `"2026-10-15"`

#### Scenario: proximaCobranca anual avança em anos
- **WHEN** uma `ContaRecorrente` `ATIVA` tem `frequencia = ANUAL`, `dataBaseCobranca = "2026-03-01"`, e `proximaCobranca` é calculada com `hoje = "2026-08-20"`
- **THEN** o resultado é `"2027-03-01"`

#### Scenario: Conta PAUSADA não tem próxima cobrança
- **WHEN** `proximaCobranca` é chamada em uma `ContaRecorrente` com `status = PAUSADA`
- **THEN** o resultado é vazio (`Optional.empty()`)

---

### Requirement: Enum StatusContaRecorrente
O sistema SHALL definir `StatusContaRecorrente` com valores `ATIVA`, `PAUSADA`, `ENCERRADA`.

#### Scenario: Status ATIVA é valor válido
- **WHEN** `StatusContaRecorrente.ATIVA` é referenciado
- **THEN** o valor existe e pode ser usado no domínio

---

### Requirement: Cadastrar conta recorrente
A API SHALL aceitar `POST /api/v1/contas-recorrentes` com payload contendo `descricao`, `valorPadrao`, `categoria`, `diaVencimento` (opcional), `competenciaInicio`, `competenciaFim` (opcional), `frequencia`, `dataBaseCobranca` e `formaPagamentoId` (opcional). Quando `frequencia` não for informada, o sistema SHALL assumir `MENSAL`. Ao persistir com sucesso, SHALL retornar `201 Created` com o recurso completo, incluindo `id` gerado e `status = ATIVA`. O cadastro SHALL também criar o primeiro registro de histórico de valores para a conta.

#### Scenario: Cadastro bem-sucedido de financiamento com data de término
- **WHEN** `POST /api/v1/contas-recorrentes` é chamado com `{"descricao":"Financiamento do Carro","valorPadrao":1335.50,"categoria":"FINANCIAMENTO","diaVencimento":10,"competenciaInicio":"2026-01","competenciaFim":"2029-12","frequencia":"MENSAL","dataBaseCobranca":"2026-01-10"}`
- **THEN** a resposta é `201 Created` com body contendo todos os campos, `id` (UUID) e `status: "ATIVA"`

#### Scenario: Cadastro bem-sucedido de assinatura trimestral vinculada a uma forma de pagamento
- **WHEN** `POST /api/v1/contas-recorrentes` é chamado com `{"descricao":"Streaming XPTO","valorPadrao":39.90,"categoria":"LAZER","competenciaInicio":"2026-08","frequencia":"TRIMESTRAL","dataBaseCobranca":"2026-08-05","formaPagamentoId":"<uuid de forma de pagamento existente>"}`
- **THEN** a resposta é `201 Created` com `competenciaFim: null` e `formaPagamentoId` igual ao informado

#### Scenario: Cadastro sem frequencia assume MENSAL
- **WHEN** `POST /api/v1/contas-recorrentes` é chamado sem o campo `frequencia`
- **THEN** a resposta é `201 Created` com `frequencia: "MENSAL"`

#### Scenario: Campo obrigatório ausente retorna 422
- **WHEN** `POST /api/v1/contas-recorrentes` é chamado sem o campo `valorPadrao`
- **THEN** a resposta é `422 Unprocessable Entity` com mensagem indicando o campo inválido

#### Scenario: valorPadrao negativo ou zero retorna 422
- **WHEN** `POST /api/v1/contas-recorrentes` é chamado com `"valorPadrao": 0`
- **THEN** a resposta é `422 Unprocessable Entity`

#### Scenario: competenciaFim anterior a competenciaInicio retorna 422
- **WHEN** `POST /api/v1/contas-recorrentes` é chamado com `"competenciaInicio":"2026-08"` e `"competenciaFim":"2026-01"`
- **THEN** a resposta é `422 Unprocessable Entity`

#### Scenario: dataBaseCobranca fora do mês de competenciaInicio retorna 422
- **WHEN** `POST /api/v1/contas-recorrentes` é chamado com `"competenciaInicio":"2026-08"` e `"dataBaseCobranca":"2026-09-05"`
- **THEN** a resposta é `422 Unprocessable Entity`

#### Scenario: formaPagamentoId inexistente ou de outro usuário retorna 422
- **WHEN** `POST /api/v1/contas-recorrentes` é chamado com `formaPagamentoId` que não existe ou pertence a outro usuário
- **THEN** a resposta é `422 Unprocessable Entity`

---

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

---

### Requirement: Editar conta recorrente
A API SHALL expor `PUT /api/v1/contas-recorrentes/{id}` para atualização completa dos campos editáveis (`descricao`, `valorPadrao`, `categoria`, `diaVencimento`, `competenciaInicio`, `competenciaFim`, `frequencia`, `dataBaseCobranca`, `formaPagamentoId`) de uma conta recorrente pertencente ao usuário autenticado. Quando `valorPadrao` for alterado em relação ao valor vigente, o sistema SHALL encerrar o registro de histórico vigente (`vigenteAte = hoje`) e abrir um novo registro com o novo valor, preservando o anterior.

#### Scenario: Edição bem-sucedida
- **WHEN** `PUT /api/v1/contas-recorrentes/{id}` é chamado com dados válidos para uma conta existente do usuário autenticado
- **THEN** a resposta é `200 OK` com o recurso atualizado

#### Scenario: Editar conta inexistente retorna 404
- **WHEN** `PUT /api/v1/contas-recorrentes/{id}` é chamado com um `id` que não existe
- **THEN** a resposta é `404 Not Found`

#### Scenario: Editar conta de outro usuário retorna 404
- **WHEN** o usuário autenticado A chama `PUT /api/v1/contas-recorrentes/{id}` para um `id` pertencente ao usuário B
- **THEN** a resposta é `404 Not Found`

#### Scenario: Reajuste de valor preserva o valor anterior no histórico
- **WHEN** uma `ContaRecorrente` com `valorPadrao = 39.90` recebe `PUT /api/v1/contas-recorrentes/{id}` com `"valorPadrao": 44.90`
- **THEN** a resposta é `200 OK` com `valorPadrao: 44.90` e `GET /api/v1/contas-recorrentes/{id}/historico-valores` passa a incluir um registro com `valor: 39.90` e `vigenteAte` preenchido, além de um novo registro vigente com `valor: 44.90`

#### Scenario: Edição sem alterar valorPadrao não cria novo registro de histórico
- **WHEN** `PUT /api/v1/contas-recorrentes/{id}` é chamado alterando apenas `descricao`, mantendo `valorPadrao` igual ao vigente
- **THEN** nenhum novo registro é criado em `GET /api/v1/contas-recorrentes/{id}/historico-valores`

---

### Requirement: Atualizar status da conta recorrente
A API SHALL expor `PATCH /api/v1/contas-recorrentes/{id}/status` para alterar o `status` entre `ATIVA`, `PAUSADA` e `ENCERRADA`, sem afetar despesas já geradas.

#### Scenario: Pausar conta recorrente
- **WHEN** `PATCH /api/v1/contas-recorrentes/{id}/status` é chamado com `{"status":"PAUSADA"}` para uma conta `ATIVA`
- **THEN** a resposta é `200 OK` com `status: "PAUSADA"` e as despesas já geradas anteriormente permanecem inalteradas

#### Scenario: Atualizar status de conta inexistente retorna 404
- **WHEN** `PATCH /api/v1/contas-recorrentes/{id}/status` é chamado com um `id` que não existe
- **THEN** a resposta é `404 Not Found`

---

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

---

### Requirement: Enum FrequenciaCobranca
O sistema SHALL definir `FrequenciaCobranca` com valores `SEMANAL`, `MENSAL`, `TRIMESTRAL`, `ANUAL`.

#### Scenario: Frequência MENSAL é valor válido
- **WHEN** `FrequenciaCobranca.MENSAL` é referenciado
- **THEN** o valor existe e pode ser usado no domínio

---

### Requirement: Consultar histórico de valores de uma conta recorrente
A API SHALL expor `GET /api/v1/contas-recorrentes/{id}/historico-valores` retornando, para uma conta recorrente do usuário autenticado, a lista de registros de valor (`valor`, `vigenteDesde`, `vigenteAte`) ordenada do mais recente para o mais antigo.

#### Scenario: Histórico reflete valor inicial e reajustes
- **WHEN** uma conta recorrente foi cadastrada com `valorPadrao = 39.90` e depois editada duas vezes (`44.90`, depois `49.90`), e o usuário chama `GET /api/v1/contas-recorrentes/{id}/historico-valores`
- **THEN** a resposta é `200 OK` com 3 registros: `49.90` (vigente, `vigenteAte: null`), `44.90` e `39.90`, cada um com `vigenteAte` igual à data em que deixou de valer

#### Scenario: Histórico de conta inexistente ou de outro usuário retorna 404
- **WHEN** `GET /api/v1/contas-recorrentes/{id}/historico-valores` é chamado com um `id` inexistente ou pertencente a outro usuário
- **THEN** a resposta é `404 Not Found`

---

### Requirement: Resumo mensal recorrente
A API SHALL expor `GET /api/v1/contas-recorrentes/resumo` retornando `200 OK` com o total mensal recorrente comprometido pelo usuário autenticado, considerando apenas contas com `status = ATIVA`, e a quebra desse total por forma de pagamento. Para cada conta, o custo mensal equivalente SHALL ser calculado a partir de `valorPadrao` e `frequencia`: `MENSAL` → `valorPadrao`; `TRIMESTRAL` → `valorPadrao / 3`; `ANUAL` → `valorPadrao / 12`; `SEMANAL` → `valorPadrao * 52 / 12`. Todo cálculo monetário SHALL usar arredondamento `HALF_UP` com 2 casas decimais.

#### Scenario: Resumo soma o custo mensal equivalente das contas ativas
- **WHEN** o usuário autenticado tem uma conta `MENSAL` de `50.00`, uma `ANUAL` de `1200.00` e uma `TRIMESTRAL` de `90.00`, todas `ATIVA`, e chama `GET /api/v1/contas-recorrentes/resumo`
- **THEN** a resposta é `200 OK` com `totalMensalRecorrente: 180.00` (50.00 + 100.00 + 30.00)

#### Scenario: Contas PAUSADA ou ENCERRADA não entram no total
- **WHEN** o usuário autenticado tem uma conta `ATIVA` de `50.00` mensal e uma conta `PAUSADA` de `100.00` mensal, e chama `GET /api/v1/contas-recorrentes/resumo`
- **THEN** a resposta é `200 OK` com `totalMensalRecorrente: 50.00`

#### Scenario: Resumo agrupa o total por forma de pagamento
- **WHEN** o usuário autenticado tem duas contas `ATIVA` mensais de `30.00` vinculadas à forma de pagamento X e uma conta `ATIVA` mensal de `20.00` vinculada à forma de pagamento Y, e chama `GET /api/v1/contas-recorrentes/resumo`
- **THEN** a resposta inclui `porFormaPagamento` com um item para X (`totalMensal: 60.00`) e um item para Y (`totalMensal: 20.00`)

#### Scenario: Contas sem forma de pagamento aparecem agrupadas como "sem forma de pagamento"
- **WHEN** o usuário autenticado tem uma conta `ATIVA` mensal de `10.00` sem `formaPagamentoId`, e chama `GET /api/v1/contas-recorrentes/resumo`
- **THEN** a resposta inclui em `porFormaPagamento` um item com `formaPagamentoId: null` e `totalMensal: 10.00`

#### Scenario: Resumo sem contas recorrentes retorna zero
- **WHEN** o usuário autenticado não tem nenhuma conta recorrente e chama `GET /api/v1/contas-recorrentes/resumo`
- **THEN** a resposta é `200 OK` com `totalMensalRecorrente: 0` e `porFormaPagamento: []`

---

### Requirement: Consultar próximas cobranças
A API SHALL expor `GET /api/v1/contas-recorrentes/proximas-cobrancas?dias={n}` retornando `200 OK` com a lista de contas recorrentes `ATIVA` do usuário autenticado cuja `proximaCobranca` cai entre hoje (inclusive) e hoje + `n` dias (inclusive), ordenada pela data mais próxima primeiro. O parâmetro `dias` SHALL ter valor padrão `7` quando omitido.

#### Scenario: Lista contas com cobrança dentro da janela informada
- **WHEN** o usuário autenticado tem uma conta `ATIVA` com `proximaCobranca` em 3 dias e outra com `proximaCobranca` em 20 dias, e chama `GET /api/v1/contas-recorrentes/proximas-cobrancas?dias=7`
- **THEN** a resposta é `200 OK` contendo apenas a conta com `proximaCobranca` em 3 dias

#### Scenario: Parâmetro dias omitido usa padrão de 7 dias
- **WHEN** `GET /api/v1/contas-recorrentes/proximas-cobrancas` é chamado sem o parâmetro `dias`
- **THEN** a resposta considera uma janela de 7 dias a partir de hoje

#### Scenario: Contas PAUSADA ou ENCERRADA não aparecem
- **WHEN** o usuário autenticado tem uma conta `PAUSADA` cuja última `proximaCobranca` calculada estaria dentro da janela, e chama `GET /api/v1/contas-recorrentes/proximas-cobrancas`
- **THEN** a resposta não inclui essa conta

#### Scenario: Nenhuma cobrança na janela retorna lista vazia
- **WHEN** o usuário autenticado não tem nenhuma conta `ATIVA` com `proximaCobranca` dentro da janela informada
- **THEN** a resposta é `200 OK` com lista vazia
