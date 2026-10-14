## 1. Migration de banco

- [x] 1.1 Criar `src/main/resources/db/migration/V3__assinaturas_recorrentes.sql`: tabela `formas_pagamento`, colunas novas em `contas_recorrentes` (`frequencia` default `MENSAL`, `forma_pagamento_id` FK `ON DELETE SET NULL`, `data_base_cobranca`), tabela `historico_valor_conta_recorrente`
- [x] 1.2 Backfill de `data_base_cobranca` para linhas existentes (primeiro dia de `competencia_inicio`, ou `dia_vencimento` quando presente) e `SET NOT NULL`
- [x] 1.3 Backfill de `historico_valor_conta_recorrente` com um registro por `ContaRecorrente` existente (`valor = valor_padrao`, `vigente_desde = competencia_inicio`, `vigente_ate = NULL`)
- [x] 1.4 Rodar a aplicação localmente e confirmar que a migration aplica sem erro sobre o banco de dev com dados existentes

## 2. Domínio FormaPagamento

- [x] 2.1 `domain/model/FormaPagamento.java` (record) e `domain/model/TipoFormaPagamento.java` (enum)
- [x] 2.2 `domain/exception/FormaPagamentoNaoEncontradaException.java`
- [x] 2.3 `domain/port/in/`: `CadastrarFormaPagamentoUseCase`, `ListarFormasPagamentoUseCase`, `EditarFormaPagamentoUseCase`, `RemoverFormaPagamentoUseCase`
- [x] 2.4 `domain/port/out/`: `SalvarFormaPagamentoPort`, `BuscarFormasPagamentoPort`, `RemoverFormaPagamentoPort`
- [x] 2.5 `domain/service/FormaPagamentoService.java` implementando os 4 use cases, com validação de posse (`buscarPorIdEValidarDono`, mesmo padrão de `ContaRecorrenteService`)
- [x] 2.6 Testes unitários de `FormaPagamentoService` com `@ExtendWith(MockitoExtension.class)`, mockando os ports de saída

## 3. Adapters de FormaPagamento

- [x] 3.1 `adapter/out/persistence/entity/FormaPagamentoJpaEntity.java`, `repository/FormaPagamentoJpaRepository.java`
- [x] 3.2 `adapter/out/persistence/FormaPagamentoPersistenceMapper.java`, `FormaPagamentoPersistenceAdapter.java` implementando os ports de saída
- [x] 3.3 `application/dto/request/CadastrarFormaPagamentoRequest.java`, `EditarFormaPagamentoRequest.java`; `application/dto/response/FormaPagamentoResponse.java`; `application/mapper/FormaPagamentoMapper.java`
- [x] 3.4 `adapter/in/web/FormaPagamentoController.java`: `POST/GET/PUT/DELETE /api/v1/formas-pagamento`, usando `UsuarioAutenticadoResolver` como nos demais controllers
- [x] 3.5 Testes `@WebMvcTest(FormaPagamentoController.class)` cobrindo os cenários de `specs/gestao-formas-pagamento/spec.md`
- [x] 3.6 Teste de persistência (`@DataJpaTest` ou `IT`) para `FormaPagamentoPersistenceAdapter`

## 4. Estender domínio ContaRecorrente

- [x] 4.1 `domain/model/FrequenciaCobranca.java` (enum `SEMANAL, MENSAL, TRIMESTRAL, ANUAL`)
- [x] 4.2 Adicionar `frequencia`, `formaPagamentoId`, `dataBaseCobranca` ao record `ContaRecorrente`; validar no compact constructor que `dataBaseCobranca` cai dentro do mês de `competenciaInicio`
- [x] 4.3 Implementar `proximaCobranca(LocalDate hoje): Optional<LocalDate>` em `ContaRecorrente` (regra por `frequencia`, vazio quando `status != ATIVA`) — conforme `design.md` seção 3
- [x] 4.4 Implementar `custoMensal(): BigDecimal` em `ContaRecorrente` (normalização por `frequencia`, `HALF_UP`, scale 2) — conforme `design.md` seção 4
- [x] 4.5 Testes unitários de domínio puro (JUnit 5, sem Spring) para `proximaCobranca` e `custoMensal` cobrindo os cenários de `specs/gestao-contas-recorrentes/spec.md`

## 5. Histórico de valores

- [x] 5.1 `domain/model/HistoricoValorContaRecorrente.java` (record: `id, contaRecorrenteId, valor, vigenteDesde, vigenteAte`)
- [x] 5.2 `domain/port/out/`: `SalvarHistoricoValorPort`, `BuscarHistoricoValorPort`
- [x] 5.3 `domain/port/in/ConsultarHistoricoValorUseCase`
- [x] 5.4 `adapter/out/persistence/entity/HistoricoValorContaRecorrenteJpaEntity.java`, `repository/HistoricoValorContaRecorrenteJpaRepository.java`, mapper e adapter de persistência
- [x] 5.5 Em `ContaRecorrenteService.cadastrar`: criar o primeiro registro de histórico via `SalvarHistoricoValorPort`
- [x] 5.6 Em `ContaRecorrenteService.editar`: se `valorPadrao` mudou, fechar o registro vigente (`vigenteAte = hoje`) e abrir um novo — extrair para um colaborador de domínio dedicado (ex. `AtualizarHistoricoValorService`) para manter `editar` com responsabilidade única
- [x] 5.7 `application/dto/response/HistoricoValorResponse.java`; endpoint `GET /api/v1/contas-recorrentes/{id}/historico-valores` em `ContaRecorrenteController`
- [x] 5.8 Testes unitários do fluxo de histórico (cadastro cria 1 registro; edição com mudança de valor fecha e abre; edição sem mudança de valor não cria registro) e teste `@WebMvcTest` do novo endpoint

## 6. Vincular FormaPagamento e frequência ao fluxo existente de ContaRecorrente

- [x] 6.1 Atualizar `CadastrarContaRecorrenteRequest`/`EditarContaRecorrenteRequest`/`ContaRecorrenteResponse` e `ContaRecorrenteMapper` com `frequencia` (default `MENSAL` quando ausente), `formaPagamentoId` (opcional) e `dataBaseCobranca`
- [x] 6.2 Validar em `ContaRecorrenteService.cadastrar`/`editar` que `formaPagamentoId`, quando informado, existe e pertence ao usuário autenticado (reaproveitar `BuscarFormasPagamentoPort`) — 422 caso contrário
- [x] 6.3 Atualizar `ContaRecorrenteJpaEntity` e o mapper de persistência com as 3 colunas novas
- [x] 6.4 Atualizar os testes existentes de `ContaRecorrenteController`/`ContaRecorrenteService` que quebrarem com os novos campos obrigatórios do domínio

## 7. Geração de lançamentos sensível à frequência

- [x] 7.1 Extrair regra de elegibilidade de competência por frequência (ex. `ElegibilidadeCompetencia.eligivel(ContaRecorrente, YearMonth)`) reutilizável por `GerarLancamentosRecorrentesService` e pelo serviço de geração em lote
- [x] 7.2 Aplicar a regra em `GerarLancamentosRecorrentesService.gerar` (conforme `specs/gerar-lancamentos-recorrentes/spec.md`)
- [x] 7.3 Aplicar a mesma regra no serviço de geração em lote (`gerar-todos`) (conforme `specs/gerar-lote-lancamentos-recorrentes/spec.md`)
- [x] 7.4 Testes unitários cobrindo os novos cenários de elegibilidade (`MENSAL`, `TRIMESTRAL`, `ANUAL`, `SEMANAL`) em ambos os serviços

## 8. Resumo e próximas cobranças

- [x] 8.1 `domain/model/ResumoContasRecorrentes.java` (record: `totalMensalRecorrente`, `porFormaPagamento: List<TotalPorFormaPagamento>`)
- [x] 8.2 `domain/port/in/ConsultarResumoContasRecorrentesUseCase` + implementação em serviço dedicado (reaproveita `BuscarContasRecorrentesPort` filtrando `ATIVA`, sem novo port de saída — mesmo espírito de `ResumoService`)
- [x] 8.3 `domain/port/in/ConsultarProximasCobrancasUseCase` + implementação (filtra `ATIVA`, calcula `proximaCobranca`, filtra pela janela de dias, ordena)
- [x] 8.4 DTOs de resposta (`ResumoContasRecorrentesResponse`, `ProximaCobrancaResponse`) e endpoints `GET /api/v1/contas-recorrentes/resumo` e `GET /api/v1/contas-recorrentes/proximas-cobrancas?dias=` em `ContaRecorrenteController`
- [x] 8.5 Testes unitários dos dois use cases e testes `@WebMvcTest` dos dois endpoints, cobrindo os cenários das specs

## 9. Documentação e validação final

- [x] 9.1 Conferir que o Swagger/OpenAPI (`springdoc`) reflete os novos endpoints e campos sem anotações manuais adicionais além do padrão já usado no projeto
- [x] 9.2 Rodar `./mvnw test` e garantir 100% dos testes passando, incluindo cobertura mínima de 80% em `domain/` e `domain/service/`
- [x] 9.3 Rodar `./mvnw spring-boot:run` localmente e validar manualmente pelo Swagger UI: cadastrar forma de pagamento, cadastrar conta recorrente vinculada, editar valor e conferir histórico, consultar resumo e próximas cobranças
- [x] 9.4 Atualizar `CLAUDE.md` do `pactum-api` apenas se a convenção de nomenclatura/camadas tiver sido estendida de alguma forma não coberta pelo texto atual (não deve ser necessário)
