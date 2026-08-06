## 1. Domínio — extrair lógica compartilhada de geração

- [x] 1.1 Criar classe utilitária package-private `domain/service/GeradorDespesaRecorrente.java` com método estático `gerarSeNecessario(ContaRecorrente conta, YearMonth competencia, BuscarDespesasPort, SalvarDespesaPort): Optional<Despesa>` — checa `existePorContaRecorrenteECompetencia` e cria a despesa (`status = PENDENTE`, `contaRecorrenteId` preenchido) só se ainda não existir
- [x] 1.2 Refatorar `GerarLancamentosRecorrentesService.gerar(...)` para usar `GeradorDespesaRecorrente.gerarSeNecessario(...)` em vez de duplicar a lógica de criação de despesa
- [x] 1.3 Rodar os testes existentes de `GerarLancamentosRecorrentesServiceTest` para garantir que a refatoração não muda o comportamento da geração mensal

## 2. Domínio — port e service da geração em lote

- [x] 2.1 Criar port in `GerarLoteLancamentosRecorrentesUseCase` com método `gerarTodos(UUID contaRecorrenteId, UUID usuarioId): List<Despesa>`
- [x] 2.2 Implementar `GerarLoteLancamentosRecorrentesService` (`@UseCase`): busca a conta por id validando dono (`ContaRecorrenteNaoEncontradaException` se não existir/não pertencer ao usuário), retorna lista vazia se `status != ATIVA`, calcula o limite superior (`competenciaFim` ou `YearMonth.now()` se nulo), itera cada competência de `competenciaInicio` até o limite (inclusive) usando `GeradorDespesaRecorrente.gerarSeNecessario`
- [x] 2.3 Testes unitários (`@ExtendWith(MockitoExtension.class)`) cobrindo: geração para intervalo fechado, corte no mês atual para conta indefinida, geração até competenciaFim futuro, idempotência (segunda chamada não duplica), conta pausada/encerrada retorna lista vazia, conta inexistente ou de outro usuário lança `ContaRecorrenteNaoEncontradaException`

## 3. Web — endpoint

- [x] 3.1 Adicionar `POST /api/v1/contas-recorrentes/{id}/gerar-todos` em `ContaRecorrenteController`, injetando `GerarLoteLancamentosRecorrentesUseCase`, retornando `200 OK` com `ListaDespesasResponse` via `DespesaMapper.toListResponse` (mesmo padrão do endpoint `/gerar` existente)
- [x] 3.2 Anotar com `@Operation`/`@ApiResponses` (200, 404) seguindo o padrão dos demais endpoints do controller
- [x] 3.3 Testes em `ContaRecorrenteControllerTest` cobrindo `200` com despesas geradas, `200` com lista vazia (conta pausada) e `404` (conta inexistente)

## 4. Verificação final

- [x] 4.1 Rodar `./mvnw test` garantindo que toda a suíte (existente + nova) passa
- [x] 4.2 Rodar `openspec validate lote-lancamentos-recorrentes --strict` e corrigir eventuais pendências
