## 1. Banco de dados

- [x] 1.1 Criar `V4__metas_economia.sql` com tabela `metas_economia` (id UUID PK, usuario_id UUID, competencia DATE, valor NUMERIC(10,2), created_at, updated_at, `UNIQUE (usuario_id, competencia)`) e verificar que a aplicação sobe com `ddl-auto=validate` contra um Postgres limpo

## 2. Domínio

- [x] 2.1 Criar records `MetaEconomia`, `ProgressoMeta`, `ResultadoMensalMeta` e enum `StatusMeta` (`CONCLUIDA`, `ADIANTADA`, `NO_PLANEJADO`, `EM_RISCO`) em `domain/model`, sem anotações de framework; verificar compilação
- [x] 2.2 Criar exceções `MetaEconomiaNaoEncontradaException` e `CompetenciaEncerradaException` em `domain/exception`
- [x] 2.3 Criar ports de entrada (`DefinirMetaEconomiaUseCase`, `ConsultarProgressoMetaUseCase`, `RemoverMetaEconomiaUseCase`, `ConsultarHistoricoMetasUseCase`) e de saída (`BuscarMetaEconomiaPort`, `SalvarMetaEconomiaPort`, `RemoverMetaEconomiaPort`)
- [x] 2.4 Implementar `MetaEconomiaService` (`@UseCase`) com cálculo de valor restante, percentual, dias restantes, valor diário, projeção, status e marcos, usando `ConsultarResumoMensalUseCase` e `Clock`; verificar com testes unitários (Clock fixo) cobrindo todos os cenários de cálculo, status, marcos e bloqueio de competência passada da spec, incluindo o exemplo 600/1000 com 10 dias → 40,00/dia

## 3. Persistência

- [x] 3.1 Criar `MetaEconomiaJpaEntity`, `MetaEconomiaJpaRepository`, `MetaEconomiaPersistenceMapper` e `MetaEconomiaPersistenceAdapter` implementando os ports de saída, sempre filtrando por `usuarioId`; verificar com teste de integração (upsert mantém única linha; isolamento entre usuários)
- [x] 3.2 Registrar bean `Clock` em `config/` e verificar que o contexto Spring sobe

## 4. API web

- [x] 4.1 Criar DTOs `DefinirMetaRequest` (`@NotNull @Positive valor`), `MetaEconomiaResponse`, `ProgressoMetaResponse`, `HistoricoMetaResponse` e `MetaEconomiaMapper` em `application/`
- [x] 4.2 Criar `MetaEconomiaController` em `/api/v1/metas-economia` (`PUT/GET/DELETE /{competencia}`, `GET /historico?ano=`) com anotações OpenAPI; garantir que `/historico` não conflite com `/{competencia}`; verificar com testes `@WebMvcTest`/MockMvc dos códigos 200/204/400/404/422
- [x] 4.3 Mapear `MetaEconomiaNaoEncontradaException` → 404 e `CompetenciaEncerradaException` → 400 em `GlobalExceptionHandler` e verificar nos testes do item 4.2

## 5. Verificação final

- [x] 5.1 Executar `./mvnw clean verify` e confirmar build verde; conferir manualmente no Swagger UI o fluxo definir meta → consultar progresso → histórico
