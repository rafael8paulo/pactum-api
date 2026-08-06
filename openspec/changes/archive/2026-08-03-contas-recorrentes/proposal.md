## Why

Hoje toda despesa recorrente (financiamento, internet, cartão de crédito, streaming) precisa ser recadastrada manualmente todo mês via `POST /api/v1/despesas`. Isso é repetitivo, sujeito a esquecimento e não deixa rastro de que várias despesas pertencem à mesma obrigação recorrente. O usuário quer cadastrar a conta recorrente uma única vez e, a cada mês, gerar os lançamentos de despesa correspondentes com um único comando.

## What Changes

- Novo agregado `ContaRecorrente`: representa uma obrigação recorrente (descrição, valor de referência, categoria, dia de vencimento informativo, competência de início, competência de fim opcional, status).
- CRUD completo de contas recorrentes: cadastrar, listar (com filtro por status), editar, atualizar status (`ATIVA`, `PAUSADA`, `ENCERRADA`) e remover.
- Novo endpoint de geração manual `POST /api/v1/contas-recorrentes/gerar?competencia=yyyy-MM`: para cada conta recorrente `ATIVA` do usuário cuja janela de competência cobre o mês informado, cria uma `Despesa` com status `PENDENTE` e o `valorPadrao` da conta — a operação é idempotente (não duplica lançamento já gerado para a mesma conta+competência).
- `Despesa` ganha campo opcional `contaRecorrenteId` (nullable) para rastrear a origem do lançamento quando gerado a partir de uma conta recorrente. Lançamentos manuais continuam com esse campo nulo.
- Nova migration Flyway (`V2__`) criando a tabela `contas_recorrentes` e adicionando a coluna `conta_recorrente_id` em `despesas`.
- Sem job agendado (`@Scheduled`) nesta entrega — a geração é sempre disparada explicitamente pelo usuário (via botão no front-end ou chamada direta à API), evitando lançamentos "fantasmas" quando o app fica fora do ar na virada do mês.

## Capabilities

### New Capabilities
- `gestao-contas-recorrentes`: modelo de domínio `ContaRecorrente` e endpoints CRUD (`cadastrar`, `listar`, `editar`, `atualizar status`, `remover`) para contas recorrentes por usuário.
- `gerar-lancamentos-recorrentes`: endpoint que gera, de forma idempotente, as despesas de uma competência a partir das contas recorrentes ativas do usuário.

### Modified Capabilities
- `cadastrar-despesa`: o modelo de domínio `Despesa` ganha o campo opcional `contaRecorrenteId (UUID, nullable)` para rastrear a origem do lançamento; o payload de `POST /api/v1/despesas` não muda (o campo só é preenchido internamente pela geração automática).

## Impact

- **Domínio**: novo `domain/model/ContaRecorrente.java`, `domain/model/StatusContaRecorrente.java`; `domain/model/Despesa.java` ganha campo `contaRecorrenteId`.
- **Ports**: novos ports `in` (`Cadastrar/Listar/Editar/AtualizarStatus/RemoverContaRecorrenteUseCase`, `GerarLancamentosRecorrentesUseCase`) e `out` (`Salvar/Buscar/RemoverContaRecorrentePort`); `SalvarDespesaPort`/`BuscarDespesasPort` precisam suportar checagem de existência por `contaRecorrenteId` + `competencia`.
- **Persistência**: nova tabela `contas_recorrentes`, coluna `conta_recorrente_id` em `despesas`, nova migration Flyway `V2__`.
- **Web**: novo `ContaRecorrenteController` (`/api/v1/contas-recorrentes`), DTOs de request/response, mapper.
- **Testes**: unitários de domínio/serviço (JUnit 5 + Mockito), `@WebMvcTest` do novo controller, `@DataJpaTest`/`IT` do novo adapter de persistência.
- Sem impacto em `Receita`, `Patrimonio` ou no cálculo de `ResumoMensal` (que continua somando despesas por `competencia`, independente da origem).
