## Why

Hoje o deploy do Pactum API é manual: não há pipeline de CI/CD, então build, testes e publicação da imagem Docker dependem de passos executados à mão, sem garantia de que os testes passaram antes de ir para produção e sem rastreabilidade de qual commit está rodando na VPS. Automatizar isso via GitHub Actions elimina o risco de subir código não testado e torna o deploy reproduzível e auditável.

## What Changes

- Criar `.github/workflows/deploy.yml`, disparado em push para a branch `main`.
- O workflow roda `./mvnw test` (testes unitários) como gate antes de qualquer publicação ou deploy.
- Builda a imagem Docker a partir do `Dockerfile` já existente na raiz do repositório.
- Publica a imagem no GHCR (`ghcr.io/rafael8paulo/pactum-api`) com as tags `latest` e `${{ github.sha }}`.
- Faz deploy via SSH (`appleboy/ssh-action`) numa VPS já provisionada, executando `docker compose pull pactum-api && docker compose up -d --wait pactum-api` (o `docker-compose.yml` da VPS já define o serviço `pactum-api` apontando para a imagem do GHCR; esse arquivo não faz parte deste repositório e não é alterado por este change).
- Configura `permissions: packages: write` no workflow para autorizar o push ao GHCR.
- Requer os secrets do repositório: `SSH_PRIVATE_KEY`, `VPS_HOST`, `VPS_USER`, `VPS_SSH_PORT` (o token do GHCR usa o `GITHUB_TOKEN` automático, sem secret adicional).

## Capabilities

### New Capabilities
- `ci-cd-pipeline`: pipeline de integração e entrega contínua via GitHub Actions — testes automatizados, build/publicação de imagem Docker no GHCR e deploy remoto via SSH a cada push em `main`.

### Modified Capabilities
- Nenhuma. Não há spec existente de CI/CD ou deploy em `openspec/specs/`; esta é uma capability inteiramente nova e não altera requirements de nenhuma capability já documentada (ex.: `database-setup`, que trata apenas de conexão/health check, não de pipeline de deploy).

## Impact

- **Código**: novo arquivo `.github/workflows/deploy.yml`. Nenhuma alteração em código de aplicação, `Dockerfile` ou `docker-compose.yml` do repositório.
- **Infraestrutura GitHub**: novo pacote de imagem no GHCR do repositório (`ghcr.io/rafael8paulo/pactum-api`); permissão `packages: write` concedida ao `GITHUB_TOKEN` do workflow.
- **Secrets**: exige o cadastro prévio de `SSH_PRIVATE_KEY`, `VPS_HOST`, `VPS_USER`, `VPS_SSH_PORT` nos secrets do repositório GitHub — sem eles, o job de deploy falha.
- **VPS de produção**: pressupõe que a VPS já está provisionada com Docker, Docker Compose e um `docker-compose.yml` próprio contendo o serviço `pactum-api`, autenticado no GHCR (ou usando imagem pública) para conseguir dar `pull`. Nada nessa VPS é criado por este change.
- **Deploy**: a partir deste change, todo push em `main` publica automaticamente uma nova versão em produção — aumenta a frequência de deploy e exige que `main` seja tratada como branch protegida/estável.
