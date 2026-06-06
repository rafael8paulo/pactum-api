## 1. Domínio — Model e Enum

- [x] 1.1 Criar enum `CategoriaReceita` em `domain/model/` com valores `SALARIO`, `FREELANCE`, `BONUS`, `OUTROS`
- [x] 1.2 Criar record `Receita` em `domain/model/` com campos: `id (UUID)`, `descricao (String)`, `valor (BigDecimal)`, `competencia (YearMonth)`, `categoria (CategoriaReceita)` — sem anotações de framework


## 2. Domínio — Ports e Exception

- [x] 2.1 Criar interface `CadastrarReceitaUseCase` em `domain/port/in/` com método `cadastrar(Receita): Receita`
- [x] 2.2 Criar interface `ListarReceitasUseCase` em `domain/port/in/` com método `listar(YearMonth, CategoriaReceita): List<Receita>`
- [x] 2.3 Criar interface `EditarReceitaUseCase` em `domain/port/in/` com método `editar(UUID, Receita): Receita`
- [x] 2.4 Criar interface `RemoverReceitaUseCase` em `domain/port/in/` com método `remover(UUID): void`
- [x] 2.5 Criar interface `SalvarReceitaPort` em `domain/port/out/` com método `salvar(Receita): Receita`
- [x] 2.6 Criar interface `BuscarReceitasPort` em `domain/port/out/` com métodos `buscarPorId(UUID): Optional<Receita>` e `buscarPorFiltros(YearMonth, CategoriaReceita): List<Receita>`
- [x] 2.7 Criar interface `RemoverReceitaPort` em `domain/port/out/` com método `remover(UUID): void`
- [x] 2.8 Criar `ReceitaNaoEncontradaException` em `domain/exception/` estendendo `RuntimeException`

## 3. Domínio — Service

- [x] 3.1 Criar `ReceitaService` em `domain/service/` anotado com `@UseCase` implementando os 4 use case interfaces
- [x] 3.2 Implementar `cadastrar`: delegar para `SalvarReceitaPort`
- [x] 3.3 Implementar `listar`: delegar para `BuscarReceitasPort.buscarPorFiltros`
- [x] 3.4 Implementar `editar`: buscar por ID (lançar `ReceitaNaoEncontradaException` se não encontrado), salvar com ID original
- [x] 3.5 Implementar `remover`: buscar por ID (lançar exception se não encontrado), delegar para `RemoverReceitaPort`
- [x] 3.6 Criar `ReceitaServiceTest` com `@ExtendWith(MockitoExtension.class)` cobrindo: cadastro, listagem, edição com sucesso, edição com ID inexistente, remoção com sucesso, remoção com ID inexistente

## 4. Application — DTOs e Mapper

- [x] 4.1 Criar record `CadastrarReceitaRequest` em `application/dto/request/` com validações: `@NotBlank @Size(max=100) descricao`, `@NotNull @Positive valor`, `@NotNull competencia (YearMonth)` com `@JsonFormat(shape=STRING, pattern="yyyy-MM")`, `@NotNull categoria (CategoriaReceita)`
- [x] 4.2 Criar record `EditarReceitaRequest` com os mesmos campos e validações de `CadastrarReceitaRequest`
- [x] 4.3 Criar record `ReceitaResponse` em `application/dto/response/` com todos os campos de `Receita`; `competencia` com `@JsonFormat(shape=STRING, pattern="yyyy-MM")`
- [x] 4.4 Criar record `ListaReceitasResponse` em `application/dto/response/` com `receitas: List<ReceitaResponse>` e `total: BigDecimal`
- [x] 4.5 Criar classe `ReceitaMapper` em `application/mapper/` com métodos estáticos: `toDomain(CadastrarReceitaRequest): Receita`, `toDomain(EditarReceitaRequest): Receita`, `toResponse(Receita): ReceitaResponse`, `toListResponse(List<Receita>): ListaReceitasResponse`

## 5. Adapter Out — Persistência

- [x] 5.1 Criar `ReceitaJpaEntity` em `adapter/out/persistence/entity/` com `@Entity @Table(name="receitas")`, campos: `id (UUID, @Id)`, `descricao`, `valor`, `competencia (LocalDate)`, `categoria (String)`, `createdAt`, `updatedAt` com `@PrePersist`/`@PreUpdate`
- [x] 5.2 Criar `ReceitaJpaRepository` em `adapter/out/persistence/repository/` estendendo `JpaRepository<ReceitaJpaEntity, UUID>` com `@Query` para filtros dinâmicos (competencia obrigatório, categoria opcional com `:param IS NULL OR`)
- [x] 5.3 Criar `ReceitaPersistenceMapper` em `adapter/out/persistence/` com conversões `toDomain(ReceitaJpaEntity): Receita` e `toEntity(Receita): ReceitaJpaEntity` — converter `YearMonth ↔ LocalDate` (primeiro dia do mês)
- [x] 5.4 Criar `ReceitaPersistenceAdapter` em `adapter/out/persistence/` com `@Repository` implementando `SalvarReceitaPort`, `BuscarReceitasPort`, `RemoverReceitaPort`

## 6. Adapter In — Controller e ExceptionHandler

- [x] 6.1 Atualizar `GlobalExceptionHandler` para tratar `ReceitaNaoEncontradaException` retornando `404 Not Found`
- [x] 6.2 Criar `ReceitaController` em `adapter/in/web/` com `@RestController`, `@RequestMapping("/api/v1/receitas")`, `@Tag(name="Receitas")`, injetando os 4 use cases via construtor
- [x] 6.3 Implementar `POST /` retornando `201 Created` com `ReceitaResponse`
- [x] 6.4 Implementar `GET /` com `@RequestParam competencia` obrigatório e `categoria` opcional, retornando `ListaReceitasResponse`
- [x] 6.5 Implementar `PUT /{id}` retornando `200 OK` com `ReceitaResponse`
- [x] 6.6 Implementar `DELETE /{id}` retornando `204 No Content`
- [x] 6.7 Anotar todos os endpoints com `@ApiResponse` documentando códigos 200/201/204, 404 e 422

## 7. Testes

- [x] 7.1 Criar `ReceitaControllerTest` com `@WebMvcTest(ReceitaController.class)` cobrindo: POST 201, POST 422 (payload inválido), GET 200, PUT 200, PUT 404 (mock lança exception), DELETE 204, DELETE 404
- [x] 7.2 Criar `ReceitaPersistenceAdapterIT` com `@DataJpaTest` cobrindo: salvar, buscar por ID, buscar com filtros, remover
