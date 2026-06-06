# Spec: Atualizar Status de Despesa

## Purpose

Define o contrato para a atualização parcial de status de uma despesa via `PATCH /api/v1/despesas/{id}/status`, incluindo o tratamento de exceção de domínio para IDs inexistentes.

---

## Requirements

### Requirement: Status de despesa atualizado via PATCH retorna 200
A API SHALL aceitar `PATCH /api/v1/despesas/{id}/status` com body `{"status": "<StatusDespesa>"}` e retornar `200 OK` com o recurso completo atualizado.

#### Scenario: Atualização de status bem-sucedida
- **WHEN** `PATCH /api/v1/despesas/{id}/status` é chamado com `{"status": "PAGA"}` e a despesa existe
- **THEN** a resposta é `200 OK` com a despesa atualizada contendo o novo status

#### Scenario: Despesa inexistente retorna 404
- **WHEN** `PATCH /api/v1/despesas/{id}/status` é chamado com UUID de despesa inexistente
- **THEN** a resposta é `404 Not Found` com mensagem descritiva no formato padrão de erro

#### Scenario: Status inválido retorna 400
- **WHEN** `PATCH /api/v1/despesas/{id}/status` é chamado com `{"status": "DESCONHECIDO"}`
- **THEN** a resposta é `400 Bad Request`

---

### Requirement: DespesaNaoEncontradaException lançada para IDs inexistentes
O use case SHALL lançar `DespesaNaoEncontradaException` (exceção de domínio) quando uma operação de busca por ID não encontrar resultado. O `GlobalExceptionHandler` SHALL mapear essa exceção para `404 Not Found`.

#### Scenario: Exception de domínio mapeada para 404
- **WHEN** `DespesaNaoEncontradaException` é lançada pelo use case
- **THEN** o `GlobalExceptionHandler` retorna `404 Not Found` com o body de erro padrão (`timestamp`, `status`, `message`, `path`)
