## Why

A geração de lançamentos recorrentes (`POST /api/v1/contas-recorrentes/gerar?competencia=yyyy-MM`) só cobre uma competência por chamada. Para cadastrar uma conta recorrente retroativa ou de longo prazo (ex: um financiamento de 48 parcelas iniciado meses atrás), o usuário precisa clicar "gerar" uma vez para cada mês — repetitivo e sujeito a esquecimento. Ele quer, para uma conta recorrente específica, gerar de uma vez todos os lançamentos entre a `competenciaInicio` e a `competenciaFim` já cadastradas nela, sem repetir a ação mês a mês.

## What Changes

- Novo endpoint `POST /api/v1/contas-recorrentes/{id}/gerar-todos`: gera, para uma única conta recorrente do usuário autenticado, todas as despesas de cada competência entre `competenciaInicio` e `competenciaFim` (inclusive). Para contas sem `competenciaFim` (indefinidas), o limite superior é o mês atual — não gera despesas de meses futuros ainda não alcançados.
- A geração reaproveita as mesmas regras já existentes da geração mensal: só conta `ATIVA` gera lançamentos, cada competência é verificada individualmente por idempotência (`contaRecorrenteId` + `competencia`), despesas já geradas ou editadas manualmente não são duplicadas nem sobrescritas.
- Resposta no mesmo formato de `POST /gerar` (`200 OK` com a lista de despesas efetivamente criadas nessa chamada).
- Refatoração interna: a lógica de "criar despesa a partir de uma conta recorrente, se ainda não existir para a competência" passa a ser compartilhada entre a geração mensal (`GerarLancamentosRecorrentesService`) e a nova geração em lote, evitando duplicar essa regra de negócio entre os dois use cases.

## Capabilities

### New Capabilities
- `gerar-lote-lancamentos-recorrentes`: endpoint que gera, de uma só vez, todos os lançamentos de uma conta recorrente específica entre sua competência de início e de fim (ou mês atual, se indefinida).

### Modified Capabilities
Nenhuma — a geração mensal existente (`gerar-lancamentos-recorrentes`) não muda de contrato; a nova geração em lote é aditiva.

## Impact

- **Domínio**: novo port in `GerarLoteLancamentosRecorrentesUseCase`; `GerarLancamentosRecorrentesService` e o novo `GerarLoteLancamentosRecorrentesService` passam a compartilhar a lógica de criação/idempotência de despesa a partir de uma `ContaRecorrente` (pequena extração interna, sem mudar contratos de port existentes).
- **Web**: novo endpoint no `ContaRecorrenteController` (`POST /{id}/gerar-todos`), reaproveitando `DespesaMapper.toListResponse` para a resposta.
- **Persistência**: nenhuma mudança de schema — reaproveita `BuscarContasRecorrentesPort.buscarPorId`, `BuscarDespesasPort.existePorContaRecorrenteECompetencia` e `SalvarDespesaPort.salvar` já existentes.
- **Testes**: novos testes unitários do novo service, e `@WebMvcTest` do novo endpoint no controller.
- Não afeta `Despesa`, `Receita`, `Patrimonio` nem o cálculo de `ResumoMensal`.
