## ADDED Requirements

### Requirement: Despesa editada completamente via PUT retorna 200
A API SHALL aceitar `PUT /api/v1/despesas/{id}` com um payload JSON completo (mesmos campos do cadastro) e retornar `200 OK` com o recurso atualizado. Todos os campos SHALL ser substituídos.

#### Scenario: Edição completa bem-sucedida
- **WHEN** `PUT /api/v1/despesas/{id}` é chamado com payload completo válido e a despesa existe
- **THEN** a resposta é `200 OK` com todos os campos da despesa refletindo os novos valores

#### Scenario: Edição de despesa inexistente retorna 404
- **WHEN** `PUT /api/v1/despesas/{id}` é chamado com UUID inexistente
- **THEN** a resposta é `404 Not Found` com body de erro padrão

#### Scenario: Payload inválido retorna 422
- **WHEN** `PUT /api/v1/despesas/{id}` é chamado com `"valor": 0`
- **THEN** a resposta é `422 Unprocessable Entity`

### Requirement: Despesa removida via DELETE retorna 204
A API SHALL aceitar `DELETE /api/v1/despesas/{id}` e, ao encontrar a despesa, removê-la permanentemente e retornar `204 No Content` sem body.

#### Scenario: Remoção bem-sucedida
- **WHEN** `DELETE /api/v1/despesas/{id}` é chamado com UUID de despesa existente
- **THEN** a resposta é `204 No Content` e a despesa não existe mais no banco

#### Scenario: Remoção de despesa inexistente retorna 404
- **WHEN** `DELETE /api/v1/despesas/{id}` é chamado com UUID inexistente
- **THEN** a resposta é `404 Not Found` com body de erro padrão
