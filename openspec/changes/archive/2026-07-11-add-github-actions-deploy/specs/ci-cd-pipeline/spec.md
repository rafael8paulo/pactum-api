## ADDED Requirements

### Requirement: Workflow disparado em push para main
O repositório SHALL conter um workflow GitHub Actions em `.github/workflows/deploy.yml`, disparado automaticamente em todo push para a branch `main`.

#### Scenario: Push em main dispara o workflow
- **WHEN** um commit é enviado (push) para a branch `main`
- **THEN** o workflow `deploy.yml` inicia uma nova execução

#### Scenario: Push em outra branch não dispara o workflow
- **WHEN** um commit é enviado para uma branch diferente de `main`
- **THEN** o workflow `deploy.yml` não é executado

### Requirement: Testes automatizados como gate de qualidade
O workflow SHALL executar `./mvnw test` como primeiro job. Nenhum job subsequente (build/push de imagem ou deploy) SHALL rodar se os testes falharem.

#### Scenario: Testes passam
- **WHEN** `./mvnw test` é executado no workflow e todos os testes unitários passam
- **THEN** os jobs de build/push da imagem e de deploy prosseguem

#### Scenario: Testes falham
- **WHEN** `./mvnw test` é executado no workflow e ao menos um teste falha
- **THEN** o workflow é interrompido com falha, e nenhuma imagem é publicada nem deploy é executado

### Requirement: Build e publicação da imagem Docker no GHCR
O workflow SHALL construir a imagem Docker a partir do `Dockerfile` existente na raiz do repositório e publicá-la no GitHub Container Registry (GHCR) com duas tags: `latest` e `${{ github.sha }}`. O job SHALL ter a permissão `packages: write` para autenticar no GHCR usando o `GITHUB_TOKEN` do próprio workflow.

#### Scenario: Imagem publicada com sucesso após testes passarem
- **WHEN** o job de testes é concluído com sucesso
- **THEN** a imagem Docker é construída a partir do `Dockerfile` e publicada no GHCR com as tags `latest` e o SHA do commit (`${{ github.sha }}`)

### Requirement: Deploy remoto via SSH
O workflow SHALL fazer deploy na VPS de produção via SSH (`appleboy/ssh-action`), executando `docker compose pull pactum-api && docker compose up -d --wait pactum-api` na VPS, autenticado com os secrets `SSH_PRIVATE_KEY`, `VPS_HOST`, `VPS_USER` e `VPS_SSH_PORT`. Este job SHALL rodar apenas após a publicação bem-sucedida da imagem no GHCR.

#### Scenario: Deploy executado após publicação da imagem
- **WHEN** a imagem é publicada com sucesso no GHCR
- **THEN** o workflow conecta via SSH na VPS usando os secrets configurados e executa `docker compose pull pactum-api && docker compose up -d --wait pactum-api`

#### Scenario: Deploy falha por secret ausente ou inválido
- **WHEN** um dos secrets `SSH_PRIVATE_KEY`, `VPS_HOST`, `VPS_USER` ou `VPS_SSH_PORT` está ausente ou inválido
- **THEN** o job de deploy falha de forma explícita, sem afetar a imagem já publicada no GHCR
