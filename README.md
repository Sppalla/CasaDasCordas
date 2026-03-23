# Project Fin — Loja Custos

Aplicação web em **Java (Spring Boot)** para custo do produto na loja + simulação de marketplaces.

Código principal: pasta [`loja-custos/`](./loja-custos/).

## GitHub

1. Crie um repositório **vazio** no GitHub (sem README, se quiser evitar conflito).
2. Na pasta deste projeto:

```powershell
cd "c:\Users\Pichau\Documents\Project Fin"
git remote add origin https://github.com/SEU_USUARIO/SEU_REPO.git
git branch -M main
git push -u origin main
```

## Render (depois do push)

1. No [Render](https://render.com): **New +** → **Blueprint** (se quiser usar o `render.yaml`) ou **Web Service**.
2. Conecte o repositório do GitHub.
3. Se for **Web Service** manual: **Docker**, raiz do contexto = `loja-custos`, Dockerfile = `loja-custos/Dockerfile`.
4. Variável de ambiente: `SPRING_PROFILES_ACTIVE` = `render`.
5. O Render define `PORT` automaticamente; o app já lê no perfil `render`.

**Obs.:** No plano gratuito os dados (H2 em memória) **resetam** quando o serviço reinicia. Para produção, use **PostgreSQL** no Render.

Mais detalhes em [`loja-custos/README.md`](./loja-custos/README.md).
