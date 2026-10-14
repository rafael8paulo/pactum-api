## Context

O backend segue arquitetura hexagonal (`domain/` → `application/` → `adapter/`) e já expõe o cálculo de saldo mensal em `ResumoService` (receitas − despesas via `BuscarReceitasPort`/`BuscarDespesasPort`). Competências são persistidas como `DATE` (primeiro dia do mês) e todos os agregados são escopados por `usuario_id` via `UsuarioAutenticadoResolver`. O schema é gerido por Flyway (`V1`–`V3`) com `ddl-auto=validate`. Motivação: ver `proposal.md`.

## Goals / Non-Goals

**Goals:**
- Reaproveitar o cálculo de saldo existente como definição de "economizado", sem duplicar regra.
- Manter cálculo de projeção/status/marcos como lógica pura de domínio, testável sem Spring e com data controlada.
- Zero estado derivado persistido: só a meta (valor-alvo) é gravada.

**Non-Goals:**
- Job agendado de fechamento de mês, snapshots históricos, notificações, metas por categoria ou recorrência automática de metas.
- Rastrear transferências reais para poupança.

## Decisions

1. **"Economizado" = saldo do mês (receitas − despesas).** O sistema não tem conceito de poupança/transferência; o saldo é a única medida disponível e coerente com a tela de resumo. O `MetaEconomiaService` depende de `ConsultarResumoMensalUseCase` (port de entrada existente) em vez de recriar a soma — alternativa descartada: chamar os ports de saída diretamente, que duplicaria a regra. Saldo negativo é permitido em `valorEconomizado`; `percentual` é limitado inferiormente a 0.

2. **Persistir apenas a meta; histórico calculado sob demanda.** Tabela `metas_economia(id, usuario_id, competencia DATE, valor NUMERIC(10,2), created_at, updated_at)` com `UNIQUE (usuario_id, competencia)`. O histórico recalcula o saldo de cada mês com meta. Alternativa descartada: snapshot no fechamento do mês (exige scheduler e idempotência). Para mitigar o risco de o histórico "mudar" ao editar lançamentos antigos e de se reescrever o passado, metas de competências passadas são imutáveis (PUT/DELETE → 400); lançamentos antigos editados ainda alteram o `valorEconomizado` — aceito, pois reflete a realidade corrente dos dados.

3. **Projeção linear simples** (`economizado / diasDecorridos × diasNoMes`) e status por comparação com o ritmo linear esperado (limiares 110% / 90%). É compreensível ao usuário e determinística. Alternativas (média móvel, sazonalidade de salário no dia 5) rejeitadas por complexidade; limiares ficam como constantes nomeadas no domínio para ajuste fácil.

4. **`Clock` injetável.** Um bean `Clock` (`Clock.systemDefaultZone()`, ou fuso `America/Sao_Paulo` se já houver padrão no projeto) em `config/` é injetado no service; testes usam `Clock.fixed`. "Dias restantes" conta o dia corrente, o que reproduz o exemplo do briefing (R$ 400 / 10 dias = R$ 40).

5. **Marcos sem estado.** O backend devolve os marcos atingidos pelo percentual atual; a decisão de "celebrar apenas uma vez" é responsabilidade do cliente (localStorage). Evita tabela de eventos e lógica de idempotência para algo puramente de apresentação.

6. **Mensagens de feedback no frontend.** A API devolve `status` (enum estável); os textos contextualizados e i18n ficam no frontend.

7. **Estrutura hexagonal.** Model `MetaEconomia` (record), `ProgressoMeta` e `ResultadoMensalMeta` (records de domínio); ports in `DefinirMetaEconomiaUseCase`, `ConsultarProgressoMetaUseCase`, `RemoverMetaEconomiaUseCase`, `ConsultarHistoricoMetasUseCase`; ports out `BuscarMetaEconomiaPort`, `SalvarMetaEconomiaPort`, `RemoverMetaEconomiaPort`; exceção `MetaEconomiaNaoEncontradaException` (→404) e `CompetenciaEncerradaException` (→400) mapeadas em `GlobalExceptionHandler`. Upsert busca por (usuário, competência) e reaproveita o `id`.

## Risks / Trade-offs

- [Saldo ≠ poupança real: usuário que gasta o saldo em outra conta "perde" a economia] → documentado como não-objetivo; evolução futura pode introduzir lançamentos de categoria "Poupança".
- [Projeção distorcida no começo do mês (receita entra no dia 5, dia 1 = saldo 0)] → status `EM_RISCO` precoce; mitigação futura: suavizar nos primeiros dias. Documentado como limitação.
- [Histórico recalculado pode divergir se lançamentos antigos forem editados] → aceito (ver decisão 2).
- [N consultas ao calcular histórico anual (até 12 resumos)] → mesmo padrão já usado por `ResumoService.consultar(ano)`; volume pequeno por usuário.

## Migration Plan

1. Deploy da migration `V4__metas_economia.sql` (apenas cria tabela; sem backfill, aditiva).
2. Deploy do backend; frontend só depois (change irmão).
3. Rollback: remover endpoints; a tabela pode permanecer sem impacto (ou `DROP TABLE metas_economia`).
