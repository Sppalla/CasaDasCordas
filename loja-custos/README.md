# Loja Custos (Java / Spring Boot)

Sistema web para **custo do produto para a loja** (compra + rateio de custos operacionais) e **simulação de preço sugerido** em **Mercado Livre**, **Shopee** e **TikTok Shop** usando **% de comissão + taxa fixa por venda** (valores editáveis).

## Requisitos

- **Java 17+**
- **Maven 3.9+** (ou rode pelo IntelliJ / Eclipse com import Maven)

## Como rodar (local)

```bash
cd loja-custos
mvn spring-boot:run
```

Abra `http://localhost:8080`.

## Deploy no Render (via GitHub)

O Render **não tem runtime Java nativo**; este projeto usa **Docker** (`Dockerfile`).

1. Envie o repositório para o GitHub (veja o `README.md` na raiz do **Project Fin**).
2. No Render: novo **Web Service** → escolha o repo → ambiente **Docker**.
3. **Root Directory** / contexto Docker: pasta `loja-custos` (ou use o **Blueprint** com o `render.yaml` na raiz do repo).
4. Variável: `SPRING_PROFILES_ACTIVE=render` (já sugerido no `render.yaml`).
5. Com o perfil `render`, o app usa **H2 em memória** e escuta na porta **`PORT`** do Render.

> Dados na nuvem **não ficam salvos** após reinício (H2 memória). Para persistir, o próximo passo é plugar **PostgreSQL** no Render.

Os dados ficam no banco **H2 em arquivo** em `./data/loja-custos.mv.db` (criado na primeira execução).

## Sobre as “taxas de marketplace” e atualização automática

- **Não existe uma forma confiável de “atualizar sozinha”** só com Java genérico: cada plataforma muda tabelas por **categoria**, **tipo de anúncio**, **CPF/CNPJ**, **frete grátis**, promoções etc.
- O que dá para fazer na prática:
  1. **Você ajusta manualmente** na tela (recomendado para trabalho acadêmico e uso real).
  2. **Integração oficial** (API do marketplace / parceiros) — bem mais complexo e costuma exigir conta de vendedor + tokens.
  3. **Valores padrão** vêm do `application.yml` (`loja-custos.marketplace-defaults`) **só na primeira carga** do banco vazio; depois o que vale é o que está salvo no H2.

> Use os percentuais iniciais como **ponto de partida** e substitua pelos números do **seu** painel de vendedor.

## Próximos passos (ideias)

- Exportar lista para **PDF A4** com marca d’água (OpenHTMLtoPDF / Flying Saucer).
- Login simples e multi-loja.
- Importar planilha CSV.
- Simular **frete** e **impostos** (Simples / Lucro Presumido).

## Estrutura

- `domain/` — entidades JPA
- `service/` — cálculos e orquestração do painel
- `web/` — controller e formulários
- `templates/dashboard.html` — interface (Thymeleaf + CSS moderno)
