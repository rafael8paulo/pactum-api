## Why

A API cobre despesas, receitas e consolidação mensal, mas não rastreia o patrimônio guardado do usuário. Com a visibilidade de ativos como poupanças e carteira, o usuário tem uma fotografia financeira completa por mês.

## What Changes

- Novo endpoint `POST /api/v1/patrimonio` para cadastrar um item de patrimônio com `descricao`, `valor` e `competencia`
- Novo endpoint `GET /api/v1/patrimonio?competencia=2025-07` para consultar os itens de um mês e o total acumulado
- Novo aggregate `Patrimonio` no domínio com JPA entity, repositório e adapter de persistência
- Tabela `patrimonio` já prevista no schema PostgreSQL do projeto

## Capabilities

### New Capabilities

- `cadastrar-patrimonio`: Cadastro de item de patrimônio via POST com retorno 201
- `consultar-patrimonio`: Consulta de itens de patrimônio por competência com total agregado

### Modified Capabilities

## Impact

- Novos pacotes: `domain/model/Patrimonio`, `domain/port/in/CadastrarPatrimonioUseCase`, `domain/port/in/ConsultarPatrimonioUseCase`, `domain/port/out/SalvarPatrimonioPort`, `domain/port/out/BuscarPatrimonioPort`
- Novo `PatrimonioService` em `domain/service/`
- Novos DTOs `CadastrarPatrimonioRequest`, `PatrimonioResponse`, `ListaPatrimonioResponse`
- Novo `PatrimonioController` em `adapter/in/web/`
- Nova `PatrimonioJpaEntity` com `@Entity @Table(name="patrimonio")` e adapter de persistência correspondente
- Schema da tabela `patrimonio` já existe no BACKLOG — sem migration adicional
