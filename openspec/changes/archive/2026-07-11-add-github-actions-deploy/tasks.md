## 1. Workflow scaffold

- [x] 1.1 Criar `.github/workflows/deploy.yml` com gatilho `on: push: branches: [main]`
- [x] 1.2 Definir `permissions: packages: write` (e `contents: read`) no nível do workflow

## 2. Job de testes

- [x] 2.1 Job `test`: checkout do código, setup do Java 17 (`actions/setup-java`, distribution `temurin`), cache do Maven
- [x] 2.2 Rodar `./mvnw test` e falhar o workflow se algum teste quebrar

## 3. Job de build e publicação da imagem

- [x] 3.1 Job `build-and-push`, com `needs: test`
- [x] 3.2 Login no GHCR usando `docker/login-action` com `github.actor` e `secrets.GITHUB_TOKEN`
- [x] 3.3 Build da imagem a partir do `Dockerfile` existente na raiz (`docker/build-push-action` ou `docker build`/`docker push`)
- [x] 3.4 Publicar a imagem com as tags `ghcr.io/rafael8paulo/pactum-api:latest` e `ghcr.io/rafael8paulo/pactum-api:${{ github.sha }}`

## 4. Job de deploy via SSH

- [x] 4.1 Job `deploy`, com `needs: build-and-push`
- [x] 4.2 Usar `appleboy/ssh-action` com `host: ${{ secrets.VPS_HOST }}`, `username: ${{ secrets.VPS_USER }}`, `key: ${{ secrets.SSH_PRIVATE_KEY }}`, `port: ${{ secrets.VPS_SSH_PORT }}`
- [x] 4.3 Executar na VPS: `docker compose pull pactum-api && docker compose up -d --wait pactum-api`

## 5. Validação

- [x] 5.1 Validar a sintaxe do workflow (`actionlint` local, se disponível, ou revisão manual do YAML) — rodado via `docker run rhysd/actionlint:latest`, sem findings
- [x] 5.2 Confirmar no README ou documentação interna quais secrets (`SSH_PRIVATE_KEY`, `VPS_HOST`, `VPS_USER`, `VPS_SSH_PORT`) precisam ser cadastrados no repositório GitHub antes do primeiro push em `main` — se não houver README, deixar como observação registrada neste change — não há `README.md` na raiz do projeto; observação registrada aqui: cadastrar os 4 secrets em Settings → Secrets and variables → Actions antes do primeiro push em `main`
- [x] 5.3 Validar a mudança com `openspec validate add-github-actions-deploy --strict`
