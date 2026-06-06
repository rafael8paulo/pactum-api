## 1. Domínio — Model e Ports

- [x] 1.1 Criar record `Patrimonio` em `domain/model/` com campos: `id (UUID)`, `descricao (String)`, `valor (BigDecimal)`, `competencia (YearMonth)` — sem anotações de framework
- [x] 1.2 Criar interface `CadastrarPatrimonioUseCase` em `domain/port/in/` com método `cadastrar(Patrimonio): Patrimonio`
- [x] 1.3 Criar interface `ConsultarPatrimonioUseCase` em `domain/port/in/` com método `consultar(YearMonth): List<Patrimonio>`
- [x] 1.4 Criar interface `SalvarPatrimonioPort` em `domain/port/out/` com método `salvar(Patrimonio): Patrimonio`
- [x] 1.5 Criar interface `BuscarPatrimonioPort` em `domain/port/out/` com método `buscarPorCompetencia(YearMonth): List<Patrimonio>`

## 2. Domínio — Service

- [x] 2.1 Criar `PatrimonioService` em `domain/service/` anotado com `@UseCase` implementando `CadastrarPatrimonioUseCase` e `ConsultarPatrimonioUseCase`, injetando `SalvarPatrimonioPort` e `BuscarPatrimonioPort`
- [x] 2.2 Implementar `cadastrar`: delegar para `SalvarPatrimonioPort`
- [x] 2.3 Implementar `consultar`: delegar para `BuscarPatrimonioPort.buscarPorCompetencia`
- [x] 2.4 Criar `PatrimonioServiceTest` com `@ExtendWith(MockitoExtension.class)` cobrindo: cadastro bem-sucedido, consulta com itens, consulta sem itens (lista vazia)

## 3. Application — DTOs e Mapper

- [x] 3.1 Criar record `CadastrarPatrimonioRequest` em `application/dto/request/` com validações: `@NotBlank @Size(max=100) descricao`, `@NotNull @Positive valor`, `@NotNull competencia (YearMonth)` com `@JsonFormat(shape=STRING, pattern="yyyy-MM")`
- [x] 3.2 Criar record `PatrimonioResponse` em `application/dto/response/` com: `id (UUID)`, `descricao`, `valor`, `competencia (YearMonth)` com `@JsonFormat(shape=STRING, pattern="yyyy-MM")`
- [x] 3.3 Criar record `ListaPatrimonioResponse` em `application/dto/response/` com: `itens (List<PatrimonioResponse>)` e `totalPatrimonio (BigDecimal)`
- [x] 3.4 Criar classe `PatrimonioMapper` em `application/mapper/` com métodos estáticos: `toDomain(CadastrarPatrimonioRequest): Patrimonio`, `toResponse(Patrimonio): PatrimonioResponse`, `toListResponse(List<Patrimonio>): ListaPatrimonioResponse`

## 4. Adapter Out — Persistência

- [x] 4.1 Criar `PatrimonioJpaEntity` em `adapter/out/persistence/entity/` com `@Entity @Table(name="patrimonio")`, campos: `id (UUID, @Id)`, `descricao`, `valor`, `competencia (LocalDate)`, `createdAt`, `updatedAt` com `@PrePersist`/`@PreUpdate`
- [x] 4.2 Criar `PatrimonioJpaRepository` em `adapter/out/persistence/repository/` estendendo `JpaRepository<PatrimonioJpaEntity, UUID>` com método `findByCompetencia(LocalDate): List<PatrimonioJpaEntity>`
- [x] 4.3 Criar `PatrimonioPersistenceMapper` em `adapter/out/persistence/` com conversões `toDomain(PatrimonioJpaEntity): Patrimonio` e `toEntity(Patrimonio): PatrimonioJpaEntity` — converter `YearMonth ↔ LocalDate` (primeiro dia do mês)
- [x] 4.4 Criar `PatrimonioPersistenceAdapter` em `adapter/out/persistence/` com `@Repository` implementando `SalvarPatrimonioPort` e `BuscarPatrimonioPort`

## 5. Adapter In — Controller

- [x] 5.1 Criar `PatrimonioController` em `adapter/in/web/` com `@RestController`, `@RequestMapping("/api/v1/patrimonio")`, `@Tag(name="Patrimônio")`, injetando os 2 use cases via construtor
- [x] 5.2 Implementar `POST /` retornando `201 Created` com `PatrimonioResponse`, anotado com `@ApiResponse` para 201 e 422
- [x] 5.3 Implementar `GET /` com `@RequestParam competencia (YearMonth)` obrigatório, retornando `200 OK` com `ListaPatrimonioResponse`, anotado com `@ApiResponse` para 200 e 400

## 6. Testes

- [x] 6.1 Criar `PatrimonioControllerTest` com `@WebMvcTest(PatrimonioController.class)` cobrindo: POST 201, POST 422 (payload inválido), GET 200
- [x] 6.2 Criar `PatrimonioPersistenceAdapterIT` com `@DataJpaTest @Import(PatrimonioPersistenceAdapter.class)` cobrindo: salvar, buscar por competência, buscar competência vazia
