## Why

O usuário não tem visibilidade sobre gastos recorrentes (assinaturas como Claude, Netflix, Spotify, academia). O Pactum já modela recorrências mensais via `ContaRecorrente`, mas não sabe em qual cartão/conta cada cobrança cai, não suporta frequências diferentes de mensal, não guarda o histórico de reajustes de valor e não avisa o usuário antes de uma cobrança acontecer — o impacto financeiro só é percebido quando a fatura chega.

## What Changes

- `ContaRecorrente` passa a ter uma **frequência de cobrança** (`SEMANAL`, `MENSAL`, `TRIMESTRAL`, `ANUAL`), com `MENSAL` como padrão para contas já existentes.
- `ContaRecorrente` passa a referenciar uma **forma de pagamento** (`formaPagamentoId`), vinculando a cobrança a um cartão/conta específico.
- Nova entidade **`FormaPagamento`** (cartão/conta) com CRUD próprio, para o usuário cadastrar onde suas assinaturas são cobradas.
- Cálculo automático da **próxima data de cobrança** de cada `ContaRecorrente`, considerando frequência e dia de vencimento.
- Novo endpoint de **resumo de recorrências**: total mensal comprometido (normalizando frequências não mensais para custo mensal equivalente) e agrupamento desse total por forma de pagamento.
- Novo endpoint de **próximas cobranças**: lista as contas recorrentes com cobrança prevista dentro de uma janela de dias (aviso antecipado, consultado sob demanda pelo frontend — sem job agendado nem e-mail nesta iteração).
- **Histórico de valores**: ao editar `valorPadrao` de uma `ContaRecorrente`, o valor anterior é preservado com a data em que deixou de valer, em vez de sobrescrito.
- Reaproveita o ciclo de vida já existente (`ATIVA` / `PAUSADA` / `ENCERRADA`) — pausar, reativar e encerrar já são suportados por `PATCH /contas-recorrentes/{id}/status` e não mudam nesta iteração.

**Fora de escopo desta iteração:**
- Geração automática de despesas para frequência `SEMANAL` continua não suportada por `POST /contas-recorrentes/gerar` — essa frequência participa do cálculo de próxima cobrança e do custo mensal equivalente, mas a geração de `Despesa` continua alinhada ao grão de competência (mês), como hoje.
- Notificação por e-mail/push e jobs agendados (`@Scheduled`) — o aviso de cobrança próxima é uma consulta sob demanda, não um envio proativo.
- Importação de extrato/fatura, conciliação automática com operadoras de cartão, e assinatura compartilhada entre usuários.

## Capabilities

### New Capabilities
- `gestao-formas-pagamento`: cadastro, listagem, edição e remoção de formas de pagamento (cartão/conta) do usuário, usadas para vincular onde cada conta recorrente é cobrada.

### Modified Capabilities
- `gestao-contas-recorrentes`: adiciona `frequencia` e `formaPagamentoId` ao modelo `ContaRecorrente` e aos payloads de cadastro/edição; adiciona histórico de valores ao editar `valorPadrao`; adiciona endpoints de resumo mensal recorrente (com agrupamento por forma de pagamento) e de próximas cobranças.
- `gerar-lancamentos-recorrentes`: a geração de despesas passa a respeitar a frequência da conta recorrente (ex.: conta `TRIMESTRAL` só gera despesa nas competências correspondentes ao seu ciclo, não em todo mês coberto pela vigência).
- `gerar-lote-lancamentos-recorrentes`: mesma regra de respeito à frequência aplicada à geração em lote.

## Impact

- **Domínio**: novo agregado `FormaPagamento`; `ContaRecorrente` ganha `frequencia`, `formaPagamentoId` e um histórico de valores (nova entidade de suporte, ex. `HistoricoValorContaRecorrente`); `GerarLancamentosRecorrentesService` e a geração em lote passam a considerar a frequência ao decidir se uma competência é elegível.
- **API**: novos endpoints `POST/GET/PUT/DELETE /api/v1/formas-pagamento`, `GET /api/v1/contas-recorrentes/resumo` e `GET /api/v1/contas-recorrentes/proximas-cobrancas`; payloads de `POST`/`PUT /contas-recorrentes` ganham campos novos (compatibilidade: `formaPagamentoId` opcional para não quebrar clientes existentes; `frequencia` com default `MENSAL`).
- **Persistência**: nova tabela para `FormaPagamento`, nova coluna `forma_pagamento_id` (FK opcional) e `frequencia` em `conta_recorrente`, nova tabela para histórico de valores. Schema versionado via Flyway (`src/main/resources/db/migration`) — este change inclui as migrations correspondentes.
- **pactum-frontend**: fora deste change (ver change irmão no repositório `pactum-frontend`) — consumirá os novos endpoints para a tela de Assinaturas.
