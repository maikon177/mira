# AGENTS.md — Mira

**Antes de qualquer coisa, leia `HANDOFF.md` por inteiro.** Ele explica o projeto,
a arquitetura, o que já funciona e o que falta (passos 7 e 8).

## Regras essenciais (resumo — detalhes no HANDOFF.md)

- **Idioma**: pt-BR em tudo (código, UI, commits).
- **Sem build / sem dependências no front**: vanilla JS + ES modules. Não
  introduza React/bundler.
- **Local-first**: dados em IndexedDB (`src/db.js`). O app deve funcionar **sem IA**.
- **IA é camada revisora**: sugere e organiza; quem decide a prioridade é
  `src/prioridade.js` (sistema fixo).
- **Chave da DeepSeek**: só em `localStorage`, **nunca** no repo (é público).
- **Mudou schema do IndexedDB?** Suba `DB_VERSION` em `db.js` + migração
  (e mantenha em sincronia com `idbOpen` do `sw.js`).
- **Mudou arquivos do app?** Suba a versão do cache (`CACHE = "mira-vN"`) e
  atualize a lista `ASSETS` em `sw.js`.

## Rodar / testar / publicar

- `npm run serve` → http://localhost:5050 (PWA precisa de http, não file://).
- `npm run test:ia` → testa a DeepSeek via Node (usa `.env`).
- Deploy: `git push origin main` (GitHub Pages rebuilda sozinho).

## Próximo trabalho

Passo 7 (memória compactada) e passo 8 (laboratório de abordagens).
Veja a seção 6 do `HANDOFF.md` e os docs em `docs/`.
