## Context

A mudança anterior (`contas-recorrentes`, já arquivada) introduziu `ContaRecorrente` e a geração manual de despesas mês a mês via `POST /contas-recorrentes/gerar?competencia=yyyy-MM`, implementada em `GerarLancamentosRecorrentesService`: para uma competência informada, varre as contas `ATIVA` do usuário cuja janela (`competenciaInicio`/`competenciaFim`) cobre aquele mês, e cria uma `Despesa` (`status = PENDENTE`, `contaRecorrenteId` preenchido) para cada uma que ainda não tenha lançamento gerado naquela competência — checagem de idempotência via `BuscarDespesasPort.existePorContaRecorrenteECompetencia`.

O usuário confirmou que quer um botão por conta recorrente que gere de uma vez todas as competências entre as datas já cadastradas nela (não um seletor de período livre), e que contas sem `competenciaFim` (indefinidas) devem ser geradas até o mês atual, não além.

## Goals / Non-Goals

**Goals:**
- Gerar, para uma conta recorrente específica, todos os lançamentos de `competenciaInicio` até `competenciaFim` (ou até o mês atual, se `competenciaFim` for nulo) em uma única chamada.
- Reaproveitar as mesmas regras de elegibilidade e idempotência já usadas na geração mensal, sem duplicar a lógica de negócio entre os dois use cases.
- Manter o endpoint mensal existente inalterado (aditivo, não substitui).

**Non-Goals:**
- Seletor de período livre/arbitrário (decisão do usuário: só o intervalo já cadastrado na própria conta).
- Gerar despesas de competências futuras além de `competenciaFim` ou do mês atual.
- Job agendado ou qualquer automação em background — continua sendo uma ação explícita do usuário, mesmo em lote.
- Alterar o comportamento do endpoint `POST /gerar` (mensal) existente.

## Decisions

### 1. Endpoint por conta recorrente: `POST /contas-recorrentes/{id}/gerar-todos`
Como a decisão foi "por conta, usando as datas já cadastradas", o endpoint é escopado a uma `ContaRecorrente` específica (`{id}` no path), não a uma lista de contas ou um período arbitrário no query string. Retorna `404` se a conta não existir ou não pertencer ao usuário autenticado (mesmo padrão dos demais endpoints do recurso).

**Alternativa considerada:** endpoint global com `competenciaInicio`/`competenciaFim` no query string, aplicando a todas as contas ativas que se sobrepõem ao período. Rejeitada porque o usuário pediu explicitamente o comportamento por conta, usando as datas já cadastradas — um seletor de período livre adicionaria uma superfície de UI e de regras (ex: o que fazer se o período não bater com nenhuma conta) sem necessidade neste momento.

### 2. Limite superior para contas sem `competenciaFim`: mês atual
Para não gerar despesas de meses que ainda não aconteceram, quando `competenciaFim == null` o limite efetivo da geração é `YearMonth.now()`. Quando `competenciaFim` está preenchido (ex: financiamento com data de término), a geração vai até `competenciaFim` mesmo que seja no futuro — esse limite já foi definido explicitamente pelo usuário ao cadastrar a conta, então honrar esse limite integralmente é o comportamento esperado do "gerar tudo de uma vez" (ex: pré-carregar as 48 parcelas de um financiamento).

### 3. Reaproveitar elegibilidade e idempotência da geração mensal, extraindo a lógica compartilhada
Em vez de duplicar "criar despesa a partir de uma conta recorrente, se ainda não existir para a competência" entre `GerarLancamentosRecorrentesService` (mensal) e o novo `GerarLoteLancamentosRecorrentesService`, essa lógica é extraída para um método compartilhado (mesmo pacote `domain.service`), usado pelos dois. Ambos continuam expondo use cases (ports in) distintos — a geração mensal varre várias contas para uma competência; a geração em lote varre várias competências para uma conta — mas a regra de "quando gerar uma despesa" fica em um único lugar, seguindo a regra de DRY do `CLAUDE.md` (seção 7: "nenhuma lógica de negócio duplicada entre use cases").

### 4. Só contas `ATIVA` são elegíveis para geração em lote
Mesma regra da geração mensal: se a conta estiver `PAUSADA` ou `ENCERRADA`, a chamada retorna lista vazia (nenhuma despesa gerada), sem erro — mantém consistência de comportamento entre os dois endpoints em vez de criar uma exceção só para o caso em lote. O front-end deve desabilitar/ocultar o botão quando o status não for `ATIVA`, mas a API não depende disso para estar correta.

### 5. Sem limite artificial de quantidade de meses
Não há um teto rígido (ex: "no máximo 60 meses por chamada"). O intervalo já é naturalmente limitado pelos dados que o próprio usuário cadastrou na conta recorrente (competência de início/fim) ou pelo tempo decorrido desde a criação (para contas indefinidas, capadas no mês atual) — não há caso de uso real de um intervalo descontroladamente grande numa aplicação pessoal de finanças.

## Risks / Trade-offs

- **[Risco]** Usuário cadastra uma conta recorrente indefinida com `competenciaInicio` muito antiga por engano (ex: ano errado) e gera dezenas de despesas de uma vez → **Mitigação**: nenhuma automática no backend; o front-end deve deixar claro no botão quantos meses serão gerados antes do clique (ex: rótulo com o intervalo). Despesas geradas continuam editáveis/removíveis individualmente pelos endpoints já existentes.
- **[Trade-off]** Para contas com `competenciaFim` no futuro, a geração em lote cria despesas `PENDENTE` de meses que ainda não chegaram (ex: parcela de dezembro de 2029 já aparece em agosto de 2026) → aceito conforme decisão 2; é exatamente o comportamento que o usuário pediu ("gerar todas de uma vez, sem precisar mês por mês").

## Migration Plan

Sem mudança de schema. Endpoint puramente aditivo; nenhum impacto em dados ou contratos existentes.

## Open Questions

Nenhuma pendente.
