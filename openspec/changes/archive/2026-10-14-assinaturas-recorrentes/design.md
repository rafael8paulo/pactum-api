## Context

`ContaRecorrente` já existe (arquitetura hexagonal, ver `CLAUDE.md`) e cobre cadastro, listagem, edição, ciclo de vida (`ATIVA`/`PAUSADA`/`ENCERRADA`) e geração manual de `Despesa` por competência (`YearMonth`). O modelo de tempo hoje é **mensal por natureza**: `competenciaInicio`/`competenciaFim` são `YearMonth`, e a geração de despesas (`gerar`, `gerar-todos`) itera competência a competência.

Este change introduz dois conceitos ausentes — **frequência de cobrança** (semanal/mensal/trimestral/anual) e **forma de pagamento** (cartão/conta) — e dois comportamentos novos — **próxima data de cobrança** e **histórico de valores** — sem reescrever o mecanismo de geração de despesas existente.

## Goals / Non-Goals

**Goals:**
- Vincular cada `ContaRecorrente` a uma `FormaPagamento` (novo agregado, com CRUD próprio).
- Suportar frequência `SEMANAL`, `MENSAL`, `TRIMESTRAL`, `ANUAL` no cadastro/edição.
- Calcular a próxima data de cobrança de forma consistente para as 4 frequências.
- Expor total mensal recorrente (normalizado) e sua quebra por forma de pagamento.
- Expor uma consulta de "próximas cobranças" (aviso antecipado sob demanda).
- Preservar o valor anterior ao reajustar `valorPadrao`.

**Non-Goals:**
- Gerar `Despesa` automaticamente para contas `SEMANAL` — a geração continua com grão de competência (mês); `SEMANAL` participa apenas do cálculo de próxima cobrança e do resumo mensal.
- Job agendado (`@Scheduled`) ou envio de e-mail/push — o aviso é uma consulta síncrona.
- CRUD de despesas ganhar `formaPagamentoId` — fica restrito a `ContaRecorrente` nesta iteração.
- Conciliação com extrato/fatura real do banco/operadora.

## Decisions

### 1. `FormaPagamento` como agregado próprio (não enum)
Modelo: `FormaPagamento(id, nome, tipo: TipoFormaPagamento, diaFechamentoFatura: Integer opcional, usuarioId)`, com `TipoFormaPagamento` = `CARTAO_CREDITO, CARTAO_DEBITO, CONTA_CORRENTE, PIX, OUTRO`. `diaFechamentoFatura` (1–31, opcional) existe para permitir, no futuro, alertar "antes da fatura fechar" — usado apenas como dado informativo nesta iteração, sem lógica de fatura.
- **Alternativa considerada**: campo texto livre em `ContaRecorrente`. Rejeitada — o usuário pediu explicitamente uma entidade com CRUD, e um agregado próprio permite reaproveitar a forma de pagamento em outras contas recorrentes e, futuramente, em despesas avulsas.
- Segue o mesmo padrão hexagonal de `ContaRecorrente`: `domain/model`, `port/in`+`out`, `service`, DTOs, `adapter/in/web` (`FormaPagamentoController`), `adapter/out/persistence`.

### 2. `ContaRecorrente.formaPagamentoId` é opcional (nullable)
Contas recorrentes existentes não têm forma de pagamento; o campo é adicionado como `UUID` nullable, sem quebrar o payload atual de `POST/PUT /contas-recorrentes` (campo novo e opcional). Remover uma `FormaPagamento` referenciada **não bloqueia** a remoção — `ON DELETE SET NULL`, mesmo padrão já usado em `despesas.conta_recorrente_id`.

### 3. Novo campo `dataBaseCobranca` (LocalDate) para ancorar a "próxima cobrança"
`competenciaInicio` (`YearMonth`) e `diaVencimento` (opcional) não têm granularidade suficiente para frequência `SEMANAL` (não expressam "toda terça-feira"). Em vez de sobrecarregar `diaVencimento`, adiciona-se `dataBaseCobranca (LocalDate, obrigatório)`: a data da primeira cobrança, usada como âncora para todas as frequências.

Algoritmo de `proximaCobranca(LocalDate hoje)` (método de domínio em `ContaRecorrente`):
- Avança `dataBaseCobranca` em incrementos da frequência (`+7 dias` semanal, `+1 mês` mensal, `+3 meses` trimestral, `+1 ano` anual) até obter a primeira data `>= hoje`.
- Contas `PAUSADA`/`ENCERRADA` não têm próxima cobrança (retornam vazio) — mantém o mesmo espírito de "pausada não gera lançamento" já usado na geração de despesas.

- **Alternativa considerada**: reaproveitar `competenciaInicio` + `diaVencimento` como âncora, tratando `diaVencimento` como "dia da semana" quando `frequencia = SEMANAL`. Rejeitada — mistura semânticas incompatíveis (dia do mês vs. dia da semana) no mesmo campo e complica validação.
- **Trade-off aceito**: `dataBaseCobranca` e `competenciaInicio` passam a coexistir descrevendo a mesma recorrência sob duas granularidades diferentes (uma para próxima cobrança, outra para geração de despesa). Documentado explicitamente nas specs para não confundir implementação futura.

### 4. Custo mensal equivalente
`custoMensal(ContaRecorrente)`:
- `MENSAL` → `valorPadrao`
- `TRIMESTRAL` → `valorPadrao / 3`
- `ANUAL` → `valorPadrao / 12`
- `SEMANAL` → `valorPadrao * 52 / 12`

Todas as divisões usam `BigDecimal` com `scale=2, RoundingMode.HALF_UP`. O total do resumo é a soma dos custos mensais de todas as contas `ATIVA` do usuário (contas `PAUSADA`/`ENCERRADA` não entram no total).

### 5. Geração de despesas passa a checar elegibilidade por frequência
`GerarLancamentosRecorrentesService` e a geração em lote (`gerar-todos`) hoje geram uma despesa para **toda** competência coberta pela vigência. Nova regra: uma competência só é elegível se for compatível com a frequência, contando meses desde `competenciaInicio`:
- `MENSAL` → toda competência coberta (comportamento atual, inalterado).
- `TRIMESTRAL` → competência elegível quando `mesesDesdeInicio % 3 == 0`.
- `ANUAL` → competência elegível quando `mesesDesdeInicio % 12 == 0`.
- `SEMANAL` → nenhuma competência é elegível (a conta nunca gera despesa via estes endpoints nesta iteração); retorna lista vazia, sem erro — mesmo tratamento hoje dado a contas `PAUSADA`.

### 6. Histórico de valores como entidade separada (não campo versionado embutido)
Nova entidade `HistoricoValorContaRecorrente(id, contaRecorrenteId, valor, vigenteDesde: LocalDate, vigenteAte: LocalDate opcional = vigente)`. Ao cadastrar uma `ContaRecorrente`, cria-se automaticamente o primeiro registro de histórico. Ao editar via `PUT /contas-recorrentes/{id}`, se `valorPadrao` mudou: fecha o registro vigente (`vigenteAte = hoje`) e abre um novo com o valor editado. Exposto via `GET /api/v1/contas-recorrentes/{id}/historico-valores` (somente leitura, ordenado por `vigenteDesde` desc).
- **Alternativa considerada**: guardar o histórico como JSON dentro da própria linha de `ContaRecorrente`. Rejeitada — dificulta consulta/ordenação e foge do padrão relacional já usado no resto do domínio.

## Risks / Trade-offs

- [`SEMANAL` não gera despesa automaticamente] → Documentado como não-objetivo explícito; usuário ainda vê a assinatura no resumo/próxima cobrança. Revisitar se o volume de assinaturas semanais crescer.
- [`dataBaseCobranca` pode divergir de `competenciaInicio`/`diaVencimento` se o usuário editar só um dos dois] → Validação no cadastro/edição garante `dataBaseCobranca` dentro do mês de `competenciaInicio`; regra documentada na spec de `gestao-contas-recorrentes`.
- [Remover `FormaPagamento` em uso não bloqueia] → Comportamento consistente com o resto do domínio (`ON DELETE SET NULL`), mas pode surpreender quem espera bloqueio; considerar aviso no frontend antes da remoção.
- [Arredondamento em `custoMensal` pode gerar diferença de centavos entre a soma dos custos individuais e o total exibido] → Mitigado usando `BigDecimal`/`HALF_UP` de forma consistente em todo o cálculo; diferença esperada é de no máximo alguns centavos e é aceitável para um resumo informativo.
- [Nova tabela de histórico cresce um registro por reajuste de preço] → Volume esperado é baixo (finanças pessoais); sem necessidade de purga nesta iteração.

## Migration Plan

1. Migration Flyway `V3__assinaturas_recorrentes.sql`:
   - `CREATE TABLE formas_pagamento (...)`.
   - `ALTER TABLE contas_recorrentes ADD COLUMN frequencia VARCHAR(20) NOT NULL DEFAULT 'MENSAL'` (contas existentes assumem `MENSAL`, consistente com o comportamento atual).
   - `ALTER TABLE contas_recorrentes ADD COLUMN forma_pagamento_id UUID`, com FK `ON DELETE SET NULL`.
   - `ALTER TABLE contas_recorrentes ADD COLUMN data_base_cobranca DATE` — backfill via `UPDATE` usando o primeiro dia de `competencia_inicio` (ou `dia_vencimento` quando presente) para linhas existentes, depois `SET NOT NULL`.
   - `CREATE TABLE historico_valor_conta_recorrente (...)` + backfill de um registro por `ContaRecorrente` existente (`valor = valor_padrao`, `vigente_desde = competencia_inicio`, `vigente_ate = NULL`).
2. Sem downtime esperado — todas as colunas novas são nullable ou têm default; backfill roda na mesma migration.
3. Rollback: reverter é destrutivo (perde histórico e vínculos de forma de pagamento criados após o deploy); em caso de problema, preferir corrigir para frente em vez de reverter a migration.

## Open Questions

- O frontend precisa que `formaPagamentoId` seja obrigatório no cadastro de uma nova conta recorrente, ou opcional (permitindo "assinatura sem forma de pagamento definida")? Este design assume **opcional**; confirmar com o change do `pactum-frontend`.
