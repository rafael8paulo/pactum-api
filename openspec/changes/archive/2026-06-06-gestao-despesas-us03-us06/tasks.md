## 1. Configuração e infraestrutura

- [x] 1.1 Adicionar `spring.jackson.serialization.write-dates-as-timestamps=false` ao `application.properties`
- [x] 1.2 Criar anotação `@UseCase` em `config/` como meta-anotação de `@Service`

## 2. Domínio — Model e Enums

- [x] 2.1 Criar enum `StatusDespesa` em `domain/model/` com valores `PAGA`, `PENDENTE`, `AGENDADA`
- [x] 2.2 Criar enum `CategoriaDespesa` em `domain/model/` com valores `FINANCIAMENTO`, `CARTAO_CREDITO`, `EDUCACAO`, `SERVICOS`, `LAZER`, `IMPOSTO`, `OUTROS`
- [x] 2.3 Criar record `Despesa` em `domain/model/` com campos: `id (UUID)`, `descricao (String)`, `valor (BigDecimal)`, `status (StatusDespesa)`, `competencia (YearMonth)`, `categoria (CategoriaDespesa)` — sem anotações de framework

## 3. Domínio — Ports e Exception

- [x] 3.1 Criar interface `CadastrarDespesaUseCase` em `domain/port/in/` com método `cadastrar(Despesa): Despesa`
- [x] 3.2 Criar interface `ListarDespesasUseCase` em `domain/port/in/` com método `listar(YearMonth, CategoriaDespesa, StatusDespesa): List<Despesa>`
- [x] 3.3 Criar interface `AtualizarStatusDespesaUseCase` em `domain/port/in/` com método `atualizar(UUID, StatusDespesa): Despesa`
- [x] 3.4 Criar interface `EditarDespesaUseCase` em `domain/port/in/` com método `editar(UUID, Despesa): Despesa`
- [x] 3.5 Criar interface `RemoverDespesaUseCase` em `domain/port/in/` com método `remover(UUID): void`
- [x] 3.6 Criar interface `SalvarDespesaPort` em `domain/port/out/` com método `salvar(Despesa): Despesa`
- [x] 3.7 Criar interface `BuscarDespesasPort` em `domain/port/out/` com métodos `buscarPorId(UUID): Optional<Despesa>` e `buscarPorFiltros(YearMonth, CategoriaDespesa, StatusDespesa): List<Despesa>`
- [x] 3.8 Criar interface `RemoverDespesaPort` em `domain/port/out/` com método `remover(UUID): void`
- [x] 3.9 Criar `DespesaNaoEncontradaException` em `domain/exception/` estendendo `RuntimeException`

## 4. Domínio — Service

- [x] 4.1 Criar `DespesaService` em `domain/service/` anotado com `@UseCase` implementando todos os 5 use case interfaces
- [x] 4.2 Implementar `cadastrar`: delegar para `SalvarDespesaPort`
- [x] 4.3 Implementar `listar`: delegar para `BuscarDespesasPort.buscarPorFiltros`
- [x] 4.4 Implementar `atualizar`: buscar por ID (lançar `DespesaNaoEncontradaException` se não encontrado), criar nova instância com status atualizado, salvar
- [x] 4.5 Implementar `editar`: buscar por ID (lançar exception se não encontrado), salvar a versão editada com o ID original
- [x] 4.6 Implementar `remover`: buscar por ID (lançar exception se não encontrado), delegar para `RemoverDespesaPort`
- [x] 4.7 Criar `DespesaServiceTest` com `@ExtendWith(MockitoExtension.class)` cobrindo: cadastro, listagem, atualização de status com sucesso, atualização com ID inexistente (verifica exception), remoção com sucesso, remoção com ID inexistente

## 5. Application — DTOs e Mapper

- [x] 5.1 Criar record `CadastrarDespesaRequest` em `application/dto/request/` com validações: `@NotBlank descricao`, `@NotNull @Positive valor`, `@NotNull status (StatusDespesa)`, `@NotNull competencia (YearMonth)` com `@JsonFormat(shape=STRING, pattern="yyyy-MM")`, `@NotNull categoria (CategoriaDespesa)`
- [x] 5.2 Criar record `EditarDespesaRequest` com os mesmos campos e validações de `CadastrarDespesaRequest`
- [x] 5.3 Criar record `AtualizarStatusDespesaRequest` em `application/dto/request/` com campo `@NotNull status (StatusDespesa)`
- [x] 5.4 Criar record `DespesaResponse` em `application/dto/response/` com todos os campos de `Despesa`; `competencia` com `@JsonFormat(shape=STRING, pattern="yyyy-MM")`
- [x] 5.5 Criar record `ListaDespesasResponse` em `application/dto/response/` com `despesas: List<DespesaResponse>` e `total: BigDecimal`
- [x] 5.6 Criar classe `DespesaMapper` em `application/mapper/` com métodos estáticos: `toDomain(CadastrarDespesaRequest): Despesa`, `toResponse(Despesa): DespesaResponse`, `toListResponse(List<Despesa>): ListaDespesasResponse`

## 6. Adapter Out — Persistência

- [x] 6.1 Criar `DespesaJpaEntity` em `adapter/out/persistence/entity/` com `@Entity @Table(name="despesas")`, campos: `id (UUID, @Id)`, `descricao`, `valor`, `status (String)`, `competencia (LocalDate)`, `categoria (String)`, `createdAt`, `updatedAt` — usar `@Column` adequados
- [x] 6.2 Criar `DespesaJpaRepository` em `adapter/out/persistence/repository/` estendendo `JpaRepository<DespesaJpaEntity, UUID>` com `@Query` para busca por filtros dinâmicos (competencia obrigatório, categoria e status opcionais com `:param IS NULL OR`)
- [x] 6.3 Criar `DespesaPersistenceMapper` em `adapter/out/persistence/` com conversões `toDomain(DespesaJpaEntity): Despesa` e `toEntity(Despesa): DespesaJpaEntity` — converter `YearMonth ↔ LocalDate` (primeiro dia do mês)
- [x] 6.4 Criar `DespesaPersistenceAdapter` em `adapter/out/persistence/` com `@Repository` implementando `SalvarDespesaPort`, `BuscarDespesasPort`, `RemoverDespesaPort`

## 7. Adapter In — Controller e ExceptionHandler

- [x] 7.1 Atualizar `GlobalExceptionHandler` para tratar `DespesaNaoEncontradaException` retornando `404 Not Found`
- [x] 7.2 Criar `DespesaController` em `adapter/in/web/` com `@RestController`, `@RequestMapping("/api/v1/despesas")`, `@Tag(name="Despesas")`, injetando os 5 use cases via construtor
- [x] 7.3 Implementar `POST /` retornando `201 Created` com `DespesaResponse`
- [x] 7.4 Implementar `GET /` com `@RequestParam competencia` obrigatório e `categoria`, `status` opcionais, retornando `ListaDespesasResponse`
- [x] 7.5 Implementar `PATCH /{id}/status` retornando `200 OK` com `DespesaResponse`
- [x] 7.6 Implementar `PUT /{id}` retornando `200 OK` com `DespesaResponse`
- [x] 7.7 Implementar `DELETE /{id}` retornando `204 No Content`
- [x] 7.8 Anotar todos os endpoints com `@ApiResponse` documentando códigos 200/201/204, 404 e 422

## 8. Testes

- [x] 8.1 Criar `DespesaControllerTest` com `@WebMvcTest(DespesaController.class)` cobrindo: POST 201, POST 422 (payload inválido), GET 200, PATCH 200, PATCH 404 (mock lança exception), PUT 200, DELETE 204, DELETE 404
- [x] 8.2 Criar `DespesaPersistenceAdapterTest` com `@DataJpaTest` ou Testcontainers (sufixo IT) cobrindo: salvar, buscar por ID, buscar com filtros, remover
