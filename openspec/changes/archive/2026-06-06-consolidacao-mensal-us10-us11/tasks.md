## 1. Domínio — Model e Ports

- [x] 1.1 Criar record `ResumoMensal` em `domain/model/` com campos: `competencia (YearMonth)`, `totalReceitas (BigDecimal)`, `totalDespesas (BigDecimal)`, `saldo (BigDecimal)` — sem anotações de framework
- [x] 1.2 Criar interface `ConsultarResumoMensalUseCase` em `domain/port/in/` com método `consultar(YearMonth): ResumoMensal`
- [x] 1.3 Criar interface `ConsultarHistoricoAnualUseCase` em `domain/port/in/` com método `consultar(int ano): List<ResumoMensal>`

## 2. Domínio — Service

- [x] 2.1 Criar `ResumoService` em `domain/service/` anotado com `@UseCase` implementando `ConsultarResumoMensalUseCase` e `ConsultarHistoricoAnualUseCase`, injetando `BuscarReceitasPort` e `BuscarDespesasPort`
- [x] 2.2 Implementar `consultar(YearMonth)`: busca receitas via `BuscarReceitasPort.buscarPorFiltros(competencia, null)` e despesas via `BuscarDespesasPort.buscarPorFiltros(competencia, null, null)`, calcula totais e retorna `ResumoMensal`
- [x] 2.3 Implementar `consultar(int ano)`: itera os 12 meses do ano invocando `consultar(YearMonth)` para cada um, agrega em lista e retorna
- [x] 2.4 Criar `ResumoServiceTest` com `@ExtendWith(MockitoExtension.class)` cobrindo: resumo com receitas e despesas, resumo sem lançamentos (zeros), histórico anual com 12 meses, histórico anual zerado

## 3. Application — DTOs e Mapper

- [x] 3.1 Criar record `ResumoMensalResponse` em `application/dto/response/` com: `competencia (YearMonth)` com `@JsonFormat(shape=STRING, pattern="yyyy-MM")`, `totalReceitas (BigDecimal)`, `totalDespesas (BigDecimal)`, `saldo (BigDecimal)`
- [x] 3.2 Criar record `HistoricoAnualResponse` em `application/dto/response/` com: `ano (int)`, `meses (List<ResumoMensalResponse>)`, `totalAnualReceitas (BigDecimal)`, `totalAnualDespesas (BigDecimal)`, `saldoAnual (BigDecimal)`
- [x] 3.3 Criar classe `ResumoMapper` em `application/mapper/` com métodos estáticos: `toResponse(ResumoMensal): ResumoMensalResponse` e `toHistoricoResponse(int ano, List<ResumoMensal>): HistoricoAnualResponse`

## 4. Adapter In — Controller

- [x] 4.1 Criar `ResumoController` em `adapter/in/web/` com `@RestController`, `@RequestMapping("/api/v1/resumo")`, `@Tag(name="Resumo")`, injetando os 2 use cases via construtor
- [x] 4.2 Implementar `GET /` com `@RequestParam competencia (YearMonth)` obrigatório, retornando `200 OK` com `ResumoMensalResponse`, anotado com `@ApiResponse` para 200 e 400
- [x] 4.3 Implementar `GET /anual` com `@RequestParam int ano` obrigatório, retornando `200 OK` com `HistoricoAnualResponse`, anotado com `@ApiResponse` para 200 e 400

## 5. Testes

- [x] 5.1 Criar `ResumoControllerTest` com `@WebMvcTest(ResumoController.class)` cobrindo: GET resumo mensal 200, GET histórico anual 200
