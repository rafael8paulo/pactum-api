## ADDED Requirements

### Requirement: Receita editada completamente via PUT retorna 200
A API SHALL aceitar `PUT /api/v1/receitas/{id}` com payload completo (mesmos campos do cadastro) e retornar `200 OK` com o recurso atualizado. Todos os campos SHALL ser substituídos.

#### Scenario: Edição completa bem-sucedida
- **WHEN** `PUT /api/v1/receitas/{id}` é chamado com payload válido e a receita existe
- **THEN** a resposta é `200 OK` com todos os campos refletindo os novos valores

#### Scenario: Edição de receita inexistente retorna 404
- **WHEN** `PUT /api/v1/receitas/{id}` é chamado com UUID inexistente
- **THEN** a resposta é `404 Not Found` com body de erro padrão (`timestamp`, `status`, `message`, `path`)

#### Scenario: Payload inválido retorna 422
- **WHEN** `PUT /api/v1/receitas/{id}` é chamado com `"valor": 0`
- **THEN** a resposta é `422 Unprocessable Entity`

### Requirement: Receita removida via DELETE retorna 204
A API SHALL aceitar `DELETE /api/v1/receitas/{id}` e, ao encontrar a receita, removê-la permanentemente e retornar `204 No Content` sem body.

#### Scenario: Remoção bem-sucedida
- **WHEN** `DELETE /api/v1/receitas/{id}` é chamado com UUID de receita existente
- **THEN** a resposta é `204 No Content` e a receita não existe mais no banco

#### Scenario: Remoção de receita inexistente retorna 404
- **WHEN** `DELETE /api/v1/receitas/{id}` é chamado com UUID inexistente
- **THEN** a resposta é `404 Not Found` com body de erro padrão

### Requirement: ReceitaNaoEncontradaException mapeada para 404
O use case SHALL lançar `ReceitaNaoEncontradaException` quando uma operação busca receita por ID inexistente. O `GlobalExceptionHandler` SHALL mapear essa exceção para `404 Not Found`.

#### Scenario: Exception de domínio mapeada para 404
- **WHEN** `ReceitaNaoEncontradaException` é lançada pelo use case
- **THEN** o `GlobalExceptionHandler` retorna `404 Not Found` com body padrão de erro
