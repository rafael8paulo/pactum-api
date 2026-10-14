# Spec: Gestão de Formas de Pagamento

## Purpose

Define o modelo de domínio `FormaPagamento` e os endpoints REST para cadastrar, listar, editar e remover as formas de pagamento (cartão, PIX, boleto etc.) usadas pelas contas recorrentes de um usuário.

---

## Requirements

### Requirement: Modelo de domínio FormaPagamento
O sistema SHALL representar internamente uma forma de pagamento como: `id (UUID)`, `nome (String, não vazio, max 100)`, `tipo (TipoFormaPagamento)`, `diaFechamentoFatura (Integer, 1-31, opcional)`, `usuarioId (UUID)`. O domínio SHALL ser livre de anotações de framework.

#### Scenario: FormaPagamento criada com todos os campos
- **WHEN** um record `FormaPagamento` é instanciado com todos os campos válidos, incluindo `diaFechamentoFatura`
- **THEN** o objeto é imutável e os getters retornam os valores fornecidos

#### Scenario: FormaPagamento sem diaFechamentoFatura é válida
- **WHEN** um record `FormaPagamento` é instanciado com `diaFechamentoFatura = null`
- **THEN** o objeto é criado normalmente

---

---

### Requirement: Enum TipoFormaPagamento
O sistema SHALL definir `TipoFormaPagamento` com valores `CARTAO_CREDITO`, `CARTAO_DEBITO`, `CONTA_CORRENTE`, `PIX`, `OUTRO`.

#### Scenario: Tipo CARTAO_CREDITO é valor válido
- **WHEN** `TipoFormaPagamento.CARTAO_CREDITO` é referenciado
- **THEN** o valor existe e pode ser usado no domínio

---

---

### Requirement: Cadastrar forma de pagamento
A API SHALL aceitar `POST /api/v1/formas-pagamento` com payload contendo `nome`, `tipo` e `diaFechamentoFatura` (opcional). Ao persistir com sucesso, SHALL retornar `201 Created` com o recurso completo, incluindo `id` gerado (UUID).

#### Scenario: Cadastro bem-sucedido de cartão de crédito com dia de fechamento
- **WHEN** `POST /api/v1/formas-pagamento` é chamado com `{"nome":"Nubank","tipo":"CARTAO_CREDITO","diaFechamentoFatura":25}`
- **THEN** a resposta é `201 Created` com body contendo todos os campos e `id` (UUID)

#### Scenario: Cadastro bem-sucedido de conta corrente sem dia de fechamento
- **WHEN** `POST /api/v1/formas-pagamento` é chamado com `{"nome":"Itaú Conta Corrente","tipo":"CONTA_CORRENTE"}`
- **THEN** a resposta é `201 Created` com `diaFechamentoFatura: null`

#### Scenario: Campo obrigatório ausente retorna 422
- **WHEN** `POST /api/v1/formas-pagamento` é chamado sem o campo `nome`
- **THEN** a resposta é `422 Unprocessable Entity` com mensagem indicando o campo inválido

#### Scenario: diaFechamentoFatura fora do intervalo 1-31 retorna 422
- **WHEN** `POST /api/v1/formas-pagamento` é chamado com `"diaFechamentoFatura": 32`
- **THEN** a resposta é `422 Unprocessable Entity`

#### Scenario: tipo inválido retorna 400
- **WHEN** `POST /api/v1/formas-pagamento` é chamado com `"tipo": "INVALIDO"`
- **THEN** a resposta é `400 Bad Request`

---

---

### Requirement: Listar formas de pagamento do usuário
A API SHALL expor `GET /api/v1/formas-pagamento` retornando todas as formas de pagamento do usuário autenticado.

#### Scenario: Listagem retorna todas as formas do usuário
- **WHEN** `GET /api/v1/formas-pagamento` é chamado
- **THEN** a resposta é `200 OK` com a lista de todas as formas de pagamento do usuário autenticado

#### Scenario: Formas de pagamento de outro usuário não aparecem na listagem
- **WHEN** o usuário autenticado A chama `GET /api/v1/formas-pagamento` e existem formas de pagamento cadastradas pelo usuário B
- **THEN** a resposta não inclui nenhuma forma de pagamento pertencente ao usuário B

---

---

### Requirement: Editar forma de pagamento
A API SHALL expor `PUT /api/v1/formas-pagamento/{id}` para atualização completa dos campos editáveis (`nome`, `tipo`, `diaFechamentoFatura`) de uma forma de pagamento pertencente ao usuário autenticado.

#### Scenario: Edição bem-sucedida
- **WHEN** `PUT /api/v1/formas-pagamento/{id}` é chamado com dados válidos para uma forma existente do usuário autenticado
- **THEN** a resposta é `200 OK` com o recurso atualizado

#### Scenario: Editar forma inexistente retorna 404
- **WHEN** `PUT /api/v1/formas-pagamento/{id}` é chamado com um `id` que não existe
- **THEN** a resposta é `404 Not Found`

#### Scenario: Editar forma de outro usuário retorna 404
- **WHEN** o usuário autenticado A chama `PUT /api/v1/formas-pagamento/{id}` para um `id` pertencente ao usuário B
- **THEN** a resposta é `404 Not Found`

---

---

### Requirement: Remover forma de pagamento
A API SHALL expor `DELETE /api/v1/formas-pagamento/{id}` para remover uma forma de pagamento do usuário autenticado, retornando `204 No Content`. Contas recorrentes vinculadas a essa forma de pagamento SHALL permanecer, com o vínculo desfeito (`formaPagamentoId` passa a `null`).

#### Scenario: Remoção bem-sucedida
- **WHEN** `DELETE /api/v1/formas-pagamento/{id}` é chamado para uma forma existente do usuário autenticado
- **THEN** a resposta é `204 No Content` e a forma deixa de aparecer em `GET /api/v1/formas-pagamento`

#### Scenario: Contas recorrentes vinculadas sobrevivem à remoção da forma de pagamento
- **WHEN** uma `FormaPagamento` vinculada a contas recorrentes é removida via `DELETE /api/v1/formas-pagamento/{id}`
- **THEN** as contas recorrentes vinculadas continuam existindo em `GET /api/v1/contas-recorrentes`, com `formaPagamentoId` nulo

#### Scenario: Remover forma inexistente retorna 404
- **WHEN** `DELETE /api/v1/formas-pagamento/{id}` é chamado com um `id` que não existe
- **THEN** a resposta é `404 Not Found`
