## Why

Hoje o Pactum mostra apenas receitas, despesas e saldo do mês: o usuário vê o que sobrou, mas não tem um objetivo de economia nem sabe se o ritmo atual o levará até lá. Metas mensais com acompanhamento, projeção e marcos transformam o controle em hábito orientado a objetivo.

## What Changes

- Nova entidade **meta de economia mensal**: um valor-alvo por usuário e por competência (`yyyy-MM`), criável e ajustável (upsert) enquanto a competência não tiver terminado.
- Novo endpoint de **progresso** da meta: valor economizado (saldo do mês = receitas − despesas, reaproveitando o cálculo do resumo mensal), valor restante, percentual, dias restantes, valor diário necessário, projeção de fechamento do mês, status de desempenho (`CONCLUIDA`, `ADIANTADA`, `NO_PLANEJADO`, `EM_RISCO`) e marcos atingidos (25/50/75/100%).
- Novo endpoint de **histórico**: resultado de cada mês com meta definida (meta, economizado, percentual, atingida), consultável por ano, calculado sob demanda a partir dos lançamentos.
- Endpoint para **remover** a meta de uma competência (apenas competência atual/futura).
- Migration Flyway `V4` criando a tabela `metas_economia`.

Fora de escopo: notificações push/e-mail, metas por categoria, metas que se repetem automaticamente para os meses seguintes, snapshot/fechamento agendado do mês, conceito de "caixinha"/transferência explícita para poupança (o "economizado" é derivado do saldo do mês).

## Capabilities

### New Capabilities
- `metas-economia-mensal`: definição de meta mensal de economia, cálculo de progresso/projeção/status/marcos e histórico de desempenho por competência.

### Modified Capabilities
<!-- Nenhuma: resumo-mensal não muda; o novo use case apenas reutiliza seu cálculo de saldo. -->

## Impact

- **Código (pactum-api)**: novo model `MetaEconomia`, ports in/out, `MetaEconomiaService`, `MetaEconomiaController`, entity/adapter/mapper/repository JPA, DTOs e exceção de domínio com handler em `GlobalExceptionHandler`; bean `Clock` em `config/` para cálculo determinístico de dias restantes.
- **Banco**: nova tabela `metas_economia` (migration `V4__metas_economia.sql`), `ddl-auto=validate` exige entity alinhada.
- **API**: novos endpoints sob `/api/v1/metas-economia`; nenhum endpoint existente é alterado.
- **Change irmão**: `metas-economia-mensal-ui` em `pactum-frontend` consome estes endpoints.
