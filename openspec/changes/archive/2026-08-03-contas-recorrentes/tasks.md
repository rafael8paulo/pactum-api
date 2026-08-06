## 1. Migration

- [x] 1.1 Criar `src/main/resources/db/migration/V2__contas_recorrentes.sql` com a tabela `contas_recorrentes` (`id`, `descricao`, `valor_padrao`, `categoria`, `dia_vencimento` nullable, `competencia_inicio`, `competencia_fim` nullable, `status`, `usuario_id`, `created_at`, `updated_at`)
- [x] 1.2 Na mesma migration, adicionar coluna `conta_recorrente_id UUID NULL REFERENCES contas_recorrentes(id) ON DELETE SET NULL` na tabela `despesas` e um índice não-único em `conta_recorrente_id`

## 2. Domínio — modelo

- [x] 2.1 Criar `domain/model/StatusContaRecorrente.java` (`ATIVA`, `PAUSADA`, `ENCERRADA`)
- [x] 2.2 Criar record `domain/model/ContaRecorrente.java` com `id, descricao, valorPadrao, categoria, diaVencimento, competenciaInicio, competenciaFim, status, usuarioId`
- [x] 2.3 Adicionar campo `contaRecorrenteId (UUID, nullable)` ao record `domain/model/Despesa.java`
- [x] 2.4 Testes unitários (JUnit 5) garantindo imutabilidade dos novos records e validando os valores do enum `StatusContaRecorrente`

## 3. Domínio — ports e serviços do CRUD de ContaRecorrente

- [x] 3.1 Criar port in `CadastrarContaRecorrenteUseCase` e port out `SalvarContaRecorrentePort`
- [x] 3.2 Criar port in `ListarContasRecorrentesUseCase` e port out `BuscarContasRecorrentesPort` (filtro por `usuarioId` e `status` opcional)
- [x] 3.3 Criar port in `EditarContaRecorrenteUseCase`
- [x] 3.4 Criar port in `AtualizarStatusContaRecorrenteUseCase`
- [x] 3.5 Criar port in `RemoverContaRecorrenteUseCase` e port out `RemoverContaRecorrentePort`
- [x] 3.6 Criar exceção de domínio `ContaRecorrenteNaoEncontradaException`
- [x] 3.7 Implementar `ContaRecorrenteService` (`@UseCase`) com os cinco casos de uso acima, validando propriedade por `usuarioId` (404 se pertencer a outro usuário); `competenciaFim >= competenciaInicio` é validado no compact constructor do record `ContaRecorrente` (IllegalArgumentException → 422 via `GlobalExceptionHandler`), garantindo o invariante para qualquer caminho de construção, não só o service
- [x] 3.8 Testes unitários do `ContaRecorrenteService` (`@ExtendWith(MockitoExtension.class)`, mockando os ports de saída), nomenclatura `deve_[resultado]_quando_[condição]`, cobrindo os cenários dos specs `gestao-contas-recorrentes`

## 4. Domínio — geração de lançamentos recorrentes

- [x] 4.1 Adicionar ao port out de despesas o método necessário para checar existência de despesa por `contaRecorrenteId` + `competencia` (ex: `existePorContaRecorrenteECompetencia` em `BuscarDespesasPort`)
- [x] 4.2 Criar port in `GerarLancamentosRecorrentesUseCase` com método `gerar(YearMonth competencia, UUID usuarioId): List<Despesa>`
- [x] 4.3 Implementar `GerarLancamentosRecorrentesService`: busca contas `ATIVA` do usuário via `BuscarContasRecorrentesPort`, filtra por janela de vigência (`competenciaInicio`/`competenciaFim`), pula as que já têm despesa gerada para a competência, cria `Despesa` (`status = PENDENTE`, `contaRecorrenteId` preenchido) via `SalvarDespesaPort` para as demais, retorna apenas as recém-criadas
- [x] 4.4 Testes unitários cobrindo: geração para múltiplas contas elegíveis, conta pausada ignorada, conta fora da janela de vigência ignorada, idempotência em segunda chamada, isolamento por usuário (cenários do spec `gerar-lancamentos-recorrentes`)

## 5. Persistência — ContaRecorrente

- [x] 5.1 Criar `adapter/out/persistence/entity/ContaRecorrenteJpaEntity.java` (`@Entity @Table(name = "contas_recorrentes")`) com `@PrePersist`/`@PreUpdate` para `createdAt`/`updatedAt`, seguindo o padrão de `DespesaJpaEntity`
- [x] 5.2 Criar `adapter/out/persistence/repository/ContaRecorrenteJpaRepository.java` (Spring Data JPA) com queries por `usuarioId` (+ `status` opcional)
- [x] 5.3 Criar `adapter/out/persistence/ContaRecorrentePersistenceMapper.java` (domínio ↔ JPA entity) — mesmo pacote/padrão de `DespesaPersistenceMapper` (não `mapper/`, para seguir a convenção já usada no projeto)
- [x] 5.4 Criar `ContaRecorrentePersistenceAdapter` implementando `SalvarContaRecorrentePort`, `BuscarContasRecorrentesPort`, `RemoverContaRecorrentePort`
- [x] 5.5 Teste `ContaRecorrentePersistenceAdapterIT` (`@DataJpaTest`) cobrindo salvar, listar com/sem filtro de status, remover

## 6. Persistência — vínculo em Despesa

- [x] 6.1 Adicionar coluna `contaRecorrenteId` em `DespesaJpaEntity` (`@Column(name = "conta_recorrente_id")`, nullable)
- [x] 6.2 Atualizar `DespesaMapper` (persistência) para mapear `contaRecorrenteId` em ambas as direções
- [x] 6.3 Implementar a query de existência por `contaRecorrenteId` + `competencia` em `DespesaJpaRepository` e no `DespesaPersistenceAdapter`
- [x] 6.4 Atualizar/estender `DespesaPersistenceAdapterIT` para cobrir o novo campo e a nova query

## 7. Application — DTOs e mapper

- [x] 7.1 Criar `application/dto/request/CadastrarContaRecorrenteRequest.java` (record, com `@NotBlank`, `@NotNull`, `@Positive`, `@Min(1) @Max(31)` em `diaVencimento`)
- [x] 7.2 Criar `application/dto/request/EditarContaRecorrenteRequest.java`
- [x] 7.3 Criar `application/dto/request/AtualizarStatusContaRecorrenteRequest.java`
- [x] 7.4 Criar `application/dto/response/ContaRecorrenteResponse.java` e `ListaContasRecorrentesResponse.java`
- [x] 7.5 Criar `application/dto/response/DespesaResponse.java` — adicionar campo `contaRecorrenteId` (nullable) se ainda não presente (feito junto com a task 2.3)
- [x] 7.6 Criar `application/mapper/ContaRecorrenteMapper.java` (request/response ↔ domínio, distinto do mapper de persistência)

## 8. Web — ContaRecorrenteController

- [x] 8.1 Criar `adapter/in/web/ContaRecorrenteController.java` com `@RequestMapping("/api/v1/contas-recorrentes")`, `@Tag`, usando `UsuarioAutenticadoResolver` como nos demais controllers
- [x] 8.2 `POST /api/v1/contas-recorrentes` → `201 Created`
- [x] 8.3 `GET /api/v1/contas-recorrentes?status=` → `200 OK`
- [x] 8.4 `PUT /api/v1/contas-recorrentes/{id}` → `200 OK` / `404`
- [x] 8.5 `PATCH /api/v1/contas-recorrentes/{id}/status` → `200 OK` / `404`
- [x] 8.6 `DELETE /api/v1/contas-recorrentes/{id}` → `204 No Content` / `404`
- [x] 8.7 `POST /api/v1/contas-recorrentes/gerar?competencia=yyyy-MM` → `200 OK` com lista de despesas criadas / `400` para competência inválida ou ausente (adicionados handlers de `MissingServletRequestParameterException`/`MethodArgumentTypeMismatchException` → 400 no `GlobalExceptionHandler`, que antes caíam no handler genérico de 500)
- [x] 8.8 Teste `ContaRecorrenteControllerTest` (`@WebMvcTest` + `@MockitoBean` dos use cases) cobrindo todos os endpoints e códigos de status dos specs

## 9. Documentação e revisão final

- [x] 9.1 Conferir anotações `@Operation`/`@ApiResponses` no novo controller, coerentes com o padrão dos controllers existentes (Swagger)
- [x] 9.2 Rodar `./mvnw test` (87 testes) e `./mvnw test -Dtest=ContaRecorrentePersistenceAdapterIT,DespesaPersistenceAdapterIT` (11 testes de integração, não incluídos no `test` padrão) — todos passando
- [x] 9.3 Rodar `openspec validate contas-recorrentes --strict` — `Change 'contas-recorrentes' is valid`
