## Context

`pactum-api` já gerencia `Despesa` e `Receita` por competência (`YearMonth`), seguindo arquitetura hexagonal estrita (ver `CLAUDE.md`): domínio puro em `domain/model`, casos de uso em `domain/port/in` + `domain/service`, portas de saída em `domain/port/out`, adapters web/persistência por fora. Não existe hoje nenhum mecanismo de recorrência, agendamento (`@Scheduled`) ou vínculo entre despesas — cada `Despesa` é um registro isolado. O schema é versionado via Flyway (`V1__baseline.sql` é a única migration existente).

O usuário confirmou dois pontos que restringem o design:
- Escopo desta mudança é só `pactum-api` (o front-end terá uma proposta própria em `pactum-frontend`, consumindo os mesmos endpoints).
- A geração dos lançamentos do mês é **só sob demanda** (endpoint manual), sem job `@Scheduled` — evita lançamentos gerados silenciosamente se o app cair na virada do mês, e mantém a primeira entrega simples.

## Goals / Non-Goals

**Goals:**
- Permitir cadastrar uma obrigação recorrente uma única vez (financiamento, internet, cartão, streaming) e reutilizá-la todo mês.
- Gerar despesas de um mês a partir das contas recorrentes ativas com uma única chamada, de forma idempotente (chamar duas vezes para a mesma competência não duplica lançamentos).
- Manter rastreabilidade: toda despesa gerada aponta para a conta recorrente que a originou.
- Preservar o fluxo existente de edição/status de despesa — uma despesa gerada é uma `Despesa` normal, editável e removível pelos endpoints já existentes, sem regras especiais.

**Non-Goals:**
- Job agendado / geração automática em background (fica para uma entrega futura, se necessário).
- Cálculo automático de parcela atual/restante de financiamento (ex: "12/48") — fora de escopo nesta entrega; `competenciaFim` só marca quando a recorrência para de gerar lançamentos.
- Notificações/lembretes de vencimento.
- Front-end (proposta separada em `pactum-frontend`).

## Decisions

### 1. Nova entidade `ContaRecorrente` em vez de flag em `Despesa`
Uma `Despesa` é um lançamento pontual de um mês; uma conta recorrente é um "molde" que produz despesas ao longo do tempo — são ciclos de vida diferentes (a conta recorrente sobrevive à edição/remoção de despesas individuais). Modelar como agregado próprio, com campo `contaRecorrenteId` opcional em `Despesa` para rastrear origem, segue o mesmo padrão hexagonal já usado para `Patrimonio`/`Receita` (nova entidade = novo conjunto model/port/service/adapter/controller, conforme `CLAUDE.md` seção 12).

**Alternativa considerada:** adicionar campos `recorrente: boolean` + `frequencia` direto em `Despesa`. Rejeitada porque misturaria o ciclo de vida do "lançamento do mês" com o da "obrigação recorrente" e dificultaria pausar/encerrar uma recorrência sem mexer em despesas já lançadas.

### 2. Janela de vigência via `competenciaInicio` / `competenciaFim` opcional
Contas com fim definido (financiamento com N parcelas) usam `competenciaFim`; contas indefinidas (internet, streaming) deixam `competenciaFim = null`. A geração só cria despesa se `competenciaInicio <= competencia <= competenciaFim` (ou `competenciaFim == null`). Isso cobre os exemplos citados (financiamento, internet, cartão, streaming) sem precisar modelar "número de parcelas" explicitamente.

### 3. Status da conta recorrente: `ATIVA`, `PAUSADA`, `ENCERRADA`
Permite pausar uma assinatura (ex: streaming cancelado temporariamente) sem apagar o histórico nem perder a configuração, e encerrar definitivamente (ex: financiamento quitado antecipadamente) sem depender só de `competenciaFim`. Só contas `ATIVA` entram na geração de lançamentos.

### 4. Idempotência via checagem de `(contaRecorrenteId, competencia)` antes de criar
`GerarLancamentosRecorrentesUseCase` consulta, para cada conta ativa elegível, se já existe uma `Despesa` com aquele `contaRecorrenteId` e `competencia`; se sim, pula (não duplica, não sobrescreve edições manuais já feitas). Isso exige um novo método no port de saída de despesas (`existePorContaRecorrenteECompetencia` ou equivalente) — mais barato e explícito que uma constraint `UNIQUE` no banco combinada com `INSERT ... ON CONFLICT`, e mantém a lógica de negócio no domínio, não no SQL.

**Alternativa considerada:** `UNIQUE (conta_recorrente_id, competencia)` no banco + upsert. Rejeitada como mecanismo primário porque queremos permitir múltiplas despesas manuais na mesma competência sem `contaRecorrenteId` (não pode haver `UNIQUE` normal em coluna nullable-com-repetição sem índice parcial); a checagem no domínio é suficiente para o requisito de idempotência da geração. Um índice não-único em `conta_recorrente_id` é adicionado só para performance de consulta.

### 5. Despesa gerada nasce com `status = PENDENTE`
Reaproveita o enum `StatusDespesa` já existente (`PAGA`, `PENDENTE`, `AGENDADA`) sem criar estado novo. `PENDENTE` reflete corretamente que a despesa foi lançada mas ainda não paga; o usuário confirma/edita valor e marca como `PAGA` pelo fluxo já existente (`PATCH /{id}/status`, `PUT /{id}`) — importante para categorias de valor variável como cartão de crédito, onde `valorPadrao` é só uma referência inicial.

### 6. Endpoint de geração retorna o resultado da operação, não só 204
`POST /api/v1/contas-recorrentes/gerar?competencia=yyyy-MM` responde `200 OK` com a lista de despesas efetivamente criadas nessa chamada (contas já geradas anteriormente para a competência não aparecem de novo). Isso dá feedback direto ao usuário/front-end sobre o que foi lançado, sem precisar de uma segunda chamada a `GET /despesas`.

## Risks / Trade-offs

- **[Risco]** Usuário esquece de chamar o endpoint de geração e a despesa do mês nunca é lançada → **Mitigação**: não coberto pelo backend nesta entrega (decisão consciente, ver Non-Goals); o front-end deve tornar a ação óbvia (ex: botão em destaque no dashboard/despesas). Job agendado fica como evolução futura natural sobre a mesma `GerarLancamentosRecorrentesUseCase`.
- **[Risco]** Alterar `valorPadrao` de uma conta recorrente não deve alterar despesas já geradas em meses anteriores → **Mitigação**: a geração copia o valor para a `Despesa` no momento da criação; não há vínculo "vivo" de valor entre conta recorrente e despesas já criadas, só o `contaRecorrenteId` para rastreabilidade.
- **[Risco]** Remover uma `ContaRecorrente` que já gerou despesas quebra a referência (`conta_recorrente_id` órfão) → **Mitigação**: `RemoverContaRecorrenteUseCase` não apaga despesas já geradas; a FK usa `ON DELETE SET NULL`, preservando o histórico de despesas mesmo se a conta recorrente for removida.
- **[Trade-off]** Sem job agendado, a "automação" prometida no nome da feature é, na prática, "geração em lote sob demanda" — aceito conforme decisão explícita do usuário nesta rodada.

## Migration Plan

1. `V2__contas_recorrentes.sql`: cria tabela `contas_recorrentes` (mesmo padrão de `despesas`/`patrimonio`: `id UUID PK`, `usuario_id`, `created_at`/`updated_at`) e adiciona `conta_recorrente_id UUID NULL REFERENCES contas_recorrentes(id) ON DELETE SET NULL` em `despesas`, com índice não-único em `conta_recorrente_id`.
2. Deploy segue o pipeline existente (`test` → `build-and-push` → `deploy`); Flyway aplica a migration no boot, sem passo manual.
3. Rollback: reverter para a imagem anterior não desfaz a migration automaticamente (Flyway não faz down-migration) — como a mudança é aditiva (nova tabela + coluna nullable), a aplicação anterior continua funcionando normalmente ignorando os novos recursos; não há necessidade de rollback de schema.

## Open Questions

- Nenhuma pendente para o backend. O contrato dos endpoints aqui definidos é a base para a proposta do `pactum-frontend`.
