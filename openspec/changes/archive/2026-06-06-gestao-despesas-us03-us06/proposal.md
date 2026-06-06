## Why

A Sprint 2 introduz o primeiro agregado de negócio da Pactum API: **Despesas**. Sem ele, a aplicação não tem valor funcional — nenhum dado financeiro pode ser registrado, consultado ou gerenciado.

## What Changes

- Novo domínio `Despesa` com enums `StatusDespesa` e `CategoriaDespesa`
- CRUD completo de despesas via API REST:
  - `POST /api/v1/despesas` — cadastrar
  - `GET /api/v1/despesas` — listar por mês com filtros opcionais de categoria e status
  - `PATCH /api/v1/despesas/{id}/status` — atualizar apenas o status
  - `PUT /api/v1/despesas/{id}` — editar completamente
  - `DELETE /api/v1/despesas/{id}` — remover
- Exceção de domínio `DespesaNaoEncontradaException` mapeada para `404`
- Handler no `GlobalExceptionHandler` existente para a nova exceção
- Tabela `despesas` no PostgreSQL (schema via `ddl-auto`)

## Capabilities

### New Capabilities

- `cadastrar-despesa`: Criação de uma despesa com validação de campos obrigatórios e retorno `201 Created`
- `listar-despesas`: Consulta paginada de despesas por `competencia` (YearMonth) com filtros opcionais de `categoria` e `status`; inclui `total` agregado
- `atualizar-status-despesa`: Atualização isolada do status de uma despesa (`PAGA`, `PENDENTE`, `AGENDADA`) retornando `200 OK`
- `editar-remover-despesa`: Edição completa via `PUT` e remoção via `DELETE` com `204 No Content`

### Modified Capabilities

- `openapi-docs`: Novos controllers anotados com `@Tag` e `@ApiResponse` conforme requisito existente

## Impact

- `domain/model/`: `Despesa` (record), `StatusDespesa` (enum), `CategoriaDespesa` (enum)
- `domain/port/in/`: `CadastrarDespesaUseCase`, `ListarDespesasUseCase`, `AtualizarStatusDespesaUseCase`, `EditarDespesaUseCase`, `RemoverDespesaUseCase`
- `domain/port/out/`: `SalvarDespesaPort`, `BuscarDespesasPort`, `RemoverDespesaPort`
- `domain/exception/`: `DespesaNaoEncontradaException`
- `domain/service/`: implementações dos use cases
- `application/dto/request/`: `CadastrarDespesaRequest`, `AtualizarStatusDespesaRequest`, `EditarDespesaRequest`
- `application/dto/response/`: `DespesaResponse`, `ListaDespesasResponse`
- `application/mapper/`: `DespesaMapper`
- `adapter/out/persistence/`: `DespesaJpaEntity`, `DespesaJpaRepository`, `DespesaPersistenceAdapter`, mapper de persistência
- `adapter/in/web/`: `DespesaController`, atualização do `GlobalExceptionHandler`
- Testes unitários para cada camada
