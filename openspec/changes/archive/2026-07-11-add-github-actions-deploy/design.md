## Context

O repositório não tem nenhum workflow em `.github/workflows/` hoje — todo build, teste e deploy é manual. Já existe um `Dockerfile` multi-stage funcional (build com Maven + runtime `eclipse-temurin:17-jre-alpine`, expõe porta 8080) e uma suíte de testes unitários (`*Test.java`/`*Tests.java`) que já roda com `./mvnw test` sem depender de banco (Testcontainers não está no classpath). Os testes de integração (`*IT.java`) não são cobertos pelo Surefire padrão e não fazem parte deste pipeline.

O repositório remoto tem tanto `main` quanto `master` como branches em `origin`; o gatilho do workflow deve ser especificamente push em `main`, conforme solicitado.

A VPS de destino já é gerenciada fora deste repositório: ela tem seu próprio `docker-compose.yml` com um serviço `pactum-api` apontando para a imagem do GHCR (decisão confirmada com o usuário). Este change não cria nem versiona esse arquivo.

## Goals / Non-Goals

**Goals:**
- Um único workflow (`deploy.yml`) que, a cada push em `main`: roda os testes unitários, builda e publica a imagem no GHCR, e dispara o deploy remoto via SSH.
- Falhar o pipeline (sem publicar imagem nem tocar na VPS) se os testes não passarem.
- Publicar a imagem com duas tags: `latest` (última versão estável) e `${{ github.sha }}` (rastreabilidade exata do commit em produção).
- Deploy remoto idempotente: `docker compose pull` + `up -d --wait`, que só recria o container se a imagem mudou e aguarda o healthcheck antes de considerar o deploy concluído.

**Non-Goals:**
- Não criar/gerenciar o `docker-compose.yml` da VPS, nem provisionar a VPS (Docker, rede, etc.) — pré-requisito assumido.
- Não rodar testes de integração (`*IT.java`) neste pipeline — eles dependem de infraestrutura (Postgres) não provisionada em CI nesta mudança.
- Não implementar rollback automático — rollback é operação manual (redeploy de uma tag `${{ github.sha }}` anterior).
- Não configurar ambientes de staging/homologação — apenas produção via push em `main`.

## Decisions

- **Um único job sequencial (`test` → `build-and-push` → `deploy`) usando `needs`, em vez de 3 workflows separados.**
  Alternativa considerada: workflows independentes disparados por eventos separados. Rejeitada porque quebraria a garantia de que o deploy só acontece após os testes passarem e a imagem certa ser publicada — `needs` no mesmo workflow run garante essa ordem e usa o mesmo `github.sha` em todos os jobs.

- **Autenticação no GHCR via `GITHUB_TOKEN` automático (`secrets.GITHUB_TOKEN`), não um PAT.**
  O `GITHUB_TOKEN` do próprio workflow run já tem escopo para publicar pacotes do mesmo repositório, bastando `permissions: packages: write` no workflow. Evita gerenciar mais um secret de longa duração.

- **Deploy via `appleboy/ssh-action`, autenticado com chave privada (`SSH_PRIVATE_KEY`) e host/usuário/porta configuráveis via secrets (`VPS_HOST`, `VPS_USER`, `VPS_SSH_PORT`).**
  Alternativa considerada: usar um runner self-hosted na própria VPS. Rejeitada por ser mudança de infraestrutura maior e não solicitada — SSH action é suficiente para o volume atual de deploys e não exige manter um runner.

- **Tags de imagem: `latest` + `${{ github.sha }}`, sem versionamento semântico.**
  Como o gatilho é todo push em `main` (não uma release manual), o SHA do commit já garante rastreabilidade única; `latest` é o que a VPS consome no `docker compose pull`. Versionamento semântico fica fora de escopo — não há tags Git de release no fluxo atual.

- **Job `deploy` roda `docker compose pull pactum-api && docker compose up -d --wait pactum-api` diretamente via SSH, contra um `docker-compose.yml` que já existe na VPS.**
  Não há alternativa avaliada aqui: essa é a interface exata pedida e já assume um compose file gerenciado fora deste repositório (confirmado com o usuário). O workflow não copia nem sincroniza esse arquivo.

## Risks / Trade-offs

- [Push direto em `main` sem PR/revisão dispara deploy automático em produção] → Mitigação: fora do escopo técnico deste change, mas a proteção de branch (exigir PR antes de merge em `main`) deve ser configurada separadamente no GitHub; o workflow em si não substitui essa prática.
- [Secrets ausentes ou incorretos (`SSH_PRIVATE_KEY`, `VPS_HOST`, `VPS_USER`, `VPS_SSH_PORT`) fazem o job de deploy falhar depois que a imagem já foi publicada no GHCR] → Mitigação: a imagem publicada com tag `${{ github.sha }}` permanece disponível para deploy manual/retry mesmo se o job de deploy falhar; nenhuma limpeza automática é necessária.
- [`docker compose up -d --wait` trava indefinidamente se o container nunca fica healthy] → Mitigação: usar timeout do próprio `--wait` do Docker Compose (baseado no `healthcheck` do serviço na VPS) e o timeout padrão do step do GitHub Actions como rede de segurança; investigação de causa raiz fica com quem opera a VPS.
- [Testes de integração (`*IT.java`) não são executados em CI, permitindo regressões de persistência não detectadas antes do deploy] → Trade-off aceito nesta mudança, já que essa suíte exigiria provisionar Postgres/Testcontainers em CI — fora do escopo pedido. Registrado aqui para uma iteração futura do pipeline.

## Migration Plan

1. Criar `.github/workflows/deploy.yml` com os três jobs (`test`, `build-and-push`, `deploy`).
2. Cadastrar os secrets `SSH_PRIVATE_KEY`, `VPS_HOST`, `VPS_USER`, `VPS_SSH_PORT` nas configurações do repositório GitHub (Settings → Secrets and variables → Actions) — pré-requisito manual antes do primeiro push em `main` após esta mudança.
3. Confirmar que a VPS já tem um `docker-compose.yml` com o serviço `pactum-api` referenciando `ghcr.io/rafael8paulo/pactum-api` e que a chave pública correspondente à `SSH_PRIVATE_KEY` está autorizada no usuário `VPS_USER`.
4. Primeiro push em `main` após o merge: acompanhar a execução do workflow na aba Actions para validar os três jobs de ponta a ponta.

## Open Questions

- Nenhuma pendência bloqueante — a estrutura da VPS foi confirmada como pré-existente e fora do escopo deste change.
