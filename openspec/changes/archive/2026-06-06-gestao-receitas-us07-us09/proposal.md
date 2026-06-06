## Why

Com as despesas implementadas na Sprint 2, a Sprint 3 introduz o segundo lado da equação financeira: **Receitas**. Sem receitas cadastradas, o resumo mensal (Sprint 4) não tem dados de entrada para calcular o saldo.

## What Changes

- Novo domínio `Receita` com enum `CategoriaReceita` — modelo mais simples que `Despesa` (sem `status`)
- CRUD completo de receitas via API REST:
  - `POST /api/v1/receitas` — cadastrar
  - `GET /api/v1/receitas` — listar por mês com filtro opcional de categoria
  - `PUT /api/v1/receitas/{id}` — editar completamente
  - `DELETE /api/v1/receitas/{id}` — remover
- Exceção de domínio `ReceitaNaoEncontradaException` mapeada para `404`
- Handler no `GlobalExceptionHandler` existente para a nova exceção
- Tabela `receitas` no PostgreSQL

## Capabilities

### New Capabilities

- `cadastrar-receita`: Criação de uma receita com validação de campos obrigatórios e retorno `201 Created`
- `listar-receitas`: Consulta de receitas por `competencia` (YearMonth obrigatório) com filtro opcional de `categoria`; inclui `total` agregado
- `editar-remover-receita`: Edição completa via `PUT` e remoção via `DELETE` com `204 No Content`

### Modified Capabilities

<!-- Nenhuma — Receita é um agregado novo, independente de Despesa -->

## Impact

- `domain/model/`: `Receita` (record), `CategoriaReceita` (enum)
- `domain/port/in/`: `CadastrarReceitaUseCase`, `ListarReceitasUseCase`, `EditarReceitaUseCase`, `RemoverReceitaUseCase`
- `domain/port/out/`: `SalvarReceitaPort`, `BuscarReceitasPort`, `RemoverReceitaPort`
- `domain/exception/`: `ReceitaNaoEncontradaException`
- `domain/service/`: `ReceitaService` implementando os 4 use cases
- `application/dto/request/`: `CadastrarReceitaRequest`, `EditarReceitaRequest`
- `application/dto/response/`: `ReceitaResponse`, `ListaReceitasResponse`
- `application/mapper/`: `ReceitaMapper`
- `adapter/out/persistence/`: `ReceitaJpaEntity`, `ReceitaJpaRepository`, `ReceitaPersistenceAdapter`, `ReceitaPersistenceMapper`
- `adapter/in/web/`: `ReceitaController`, atualização do `GlobalExceptionHandler`
- Testes unitários e de integração para cada camada
