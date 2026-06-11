# HANDOFF — Projeto Mira (para o Codex continuar)

> **Leia este documento inteiro antes de escrever qualquer código.**
> Ele explica o que é o projeto, como está construído, o que já funciona,
> o que falta, e as regras/armadilhas que você precisa respeitar.
> Data deste handoff: 11/06/2026.

---

## 1. O que é o Mira

**Mira** é um **assistente operacional pessoal** (estilo "Jarvis", mas prático).
Frase de produto: **"menos cobrança, mais direção"**.

O problema que ele resolve: o usuário (dono da **Pata 3D** — impressão 3D,
atendimento, produção, modelagem, manutenção de impressoras) se perde em listas
enormes de tarefas e ignora notificações comuns. O Mira existe para responder
**uma única pergunta**: *qual é a próxima melhor ação agora?*

### Princípio central (NÃO QUEBRAR)

> O app deve **funcionar mesmo sem IA**. A IA é uma **camada revisora** por cima
> de um sistema fixo de tarefas, status, histórico e regras. A IA **sugere e
> melhora**; o sistema fixo **decide**.

### Regra de ouro

Não transformar o MVP num projeto gigante. Validar sempre este fluxo primeiro:

```
entrada bagunçada → tarefa organizada → prioridade → próxima ação → decisão → histórico
```

Há documentação de conceito detalhada em `docs/` (arquitetura, mvp, notificações,
memória compactada, laboratório de abordagens, regras de prioridade, ux para TDAH,
identidade). **Consulte `docs/` antes de implementar cada passo novo.**

---

## 2. Stack e decisões de arquitetura

| Decisão | Escolha | Por quê |
|---|---|---|
| Plataforma | **PWA** (Progressive Web App) | Roda em celular E PC com um código só; instalável; offline |
| Build | **Nenhum** (vanilla JS, ES modules) | Sem bundler, sem npm install no front. Simplicidade. Carrega direto |
| Armazenamento | **IndexedDB** (local no aparelho) | Local-first: sem login, sem servidor, funciona offline |
| Hospedagem | **GitHub Pages** (branch `main`, raiz) | HTTPS grátis; instalável no celular |
| IA | **DeepSeek**, chamada **direto do navegador** | CORS liberado (testado) → **não precisa de servidor/proxy** |
| Idioma do código/UI | **pt-BR** | Nomes de variáveis, funções e UI em português |

**Repositório:** `https://github.com/maikon177/mira` (público)
**App no ar:** `https://maikon177.github.io/mira/`

---

## 3. Mapa de arquivos

```
mira/
├── index.html              # 3 telas (Agora / Hoje / Caixa) + nav inferior
├── styles.css              # tema escuro, mobile-first, vermelho da "mira"
├── sw.js                   # Service Worker: cache offline + notificationclick
├── manifest.webmanifest    # PWA instalável (nome, ícones, standalone)
├── assets/
│   ├── icon-192.png        # ícone PWA
│   └── icon-512.png
├── src/
│   ├── app.js              # ORQUESTRADOR: liga UI ↔ db ↔ prioridade ↔ IA ↔ notif
│   ├── db.js               # IndexedDB: CRUD de tarefas + histórico
│   ├── prioridade.js       # cálculo de prioridade FIXO (sem IA)
│   ├── notificacoes.js     # notificações decisivas + modo foco
│   └── ia/
│       ├── revisora-web.js        # IA no NAVEGADOR (usada pelo app)
│       ├── revisora.mjs           # IA no Node (só p/ teste via terminal)
│       └── prompt_ia_revisora.md  # system prompt da IA revisora
├── scripts/
│   ├── serve.mjs           # servidor estático local (npm run serve)
│   └── test-deepseek.mjs   # teste da IA via Node (npm run test:ia)
├── docs/                   # documentação de CONCEITO (ler antes de cada passo)
├── .env                    # chave da DeepSeek p/ o teste Node (GITIGNORED)
├── .env.example
├── package.json            # scripts: serve, test:ia (type: module)
└── HANDOFF.md              # este arquivo
```

> Observação: `src/ia/revisora.mjs` (Node) e `src/ia/revisora-web.js` (browser)
> são versões irmãs. O **app usa a `-web.js`**. A `.mjs` só serve para o teste
> `npm run test:ia` no terminal.

---

## 4. Modelo de dados (IndexedDB — banco `mira`, versão 1)

Duas object stores. Definição em `src/db.js` (`openDB` / `onupgradeneeded`).

### Store `tarefas` (keyPath: `id`)

```js
{
  id: "uuid",                 // crypto.randomUUID()
  titulo: "string",
  categoria: "string",        // ex: "Cliente/Orçamento", "Manutenção/Produção"
  prioridade: "Alta"|"Média"|"Baixa",
  tempoEstimadoMin: number|null,
  motivo: "string",           // por que importa
  proximaAcao: "string",      // 1º passo concreto
  alertaSugerido: string|null,
  status: "aberta"|"fazendo"|"concluida"|"adiada"|"cancelada",
  revisadaIA: boolean,        // true depois que a IA estruturou
  adiamentos: number,         // quantas vezes foi adiada
  adiarAte: number|undefined, // timestamp (ms) até quando fica adiada
  criadaEm: number,           // Date.now()
  atualizadaEm: number
}
```

Índices: `status`, `criadaEm`.

### Store `historico` (keyPath: `id` autoIncrement)

```js
{
  id: number,           // auto
  tipo: "string",       // ver eventos abaixo
  tarefaId: string|null,
  extra: object,        // metadados (ex: { via: "notificacao", acao: "concluir" })
  em: number            // Date.now()
}
```

**Eventos registrados hoje** (são a matéria-prima de memória + laboratório):
`tarefa_criada`, `tarefa_iniciada`, `tarefa_concluida`, `tarefa_adiada`,
`tarefa_cancelada`, `notificacao_enviada`, `notificacao_respondida`.

> Se você adicionar campos novos à tarefa, **suba a versão do IndexedDB** em
> `db.js` (`DB_VERSION`) e trate a migração em `onupgradeneeded`. O `sw.js`
> também abre o banco (em `idbOpen`) com a mesma versão — mantenha em sincronia.

---

## 5. Como cada parte funciona (o que JÁ está pronto)

### 5.1 Telas (`index.html` + `src/app.js`)

- **Agora** (`#view-agora`): mostra **UMA** próxima ação (a de maior score) num
  card, com botões de decisão (Concluir / Iniciar / +15min / +1h / Cancelar) e
  os botões de notificação (🔔 Me lembrar / 🎯 Modo foco).
- **Hoje** (`#view-hoje`): no máximo **5** prioridades.
- **Caixa** (`#view-caixa`): textarea (uma linha = uma tarefa), botão "Adicionar",
  botão "✨ Revisar com IA", e o `<details>` de configuração da chave da IA.
- Navegação por `irPara(tela)` que alterna `data-active` nas `.view` e `.tab`.

### 5.2 Prioridade FIXA (`src/prioridade.js`) — quem DECIDE

`scoreTarefa(t)` soma:
- peso da prioridade (Alta 100 / Média 50 / Baixa 20);
- bônus por categoria (regex): cliente/orçamento/venda/financeiro +40;
  manutenção/produção/impressora +30; suprimentos/filamento +20;
- tarefa curta (≤15 min) +15;
- cada adiamento +12 (o que você foge sobe, anti-procrastinação).

`proximaAcao(tarefas)` filtra disponíveis (ignora concluída/cancelada e adiada
que ainda não venceu) e retorna a de maior score. **A Tela Agora usa isto, não a
sugestão da IA** — alinhado ao princípio (IA sugere, sistema decide).

### 5.3 IA revisora (`src/ia/revisora-web.js`) — passo 5 ✅

- Chama `POST https://api.deepseek.com/chat/completions` direto do navegador
  (CORS liberado — confirmado no aparelho real).
- Modelo padrão: `deepseek-v4-flash` (configurável via localStorage).
- Usa `response_format: { type: "json_object" }` e o system prompt de
  `prompt_ia_revisora.md`.
- **Chave**: guardada em `localStorage` (`mira_deepseek_key`), digitada pelo
  usuário **uma vez** no app. **NUNCA** colocar a chave no código/repo (é público).
- Fluxo "Revisar com IA" (em `app.js > revisarComIA`): pega as tarefas não
  revisadas → manda pra IA → recebe lista estruturada → **deleta as soltas e
  recria** as estruturadas com `revisadaIA: true`.
- Latência ~15-20s (modelo com raciocínio). Há spinner e mensagens de erro.

### 5.4 Notificações decisivas (`src/notificacoes.js` + `sw.js`) — passo 6 ✅

- `notificarProximaAcao(tarefa)` monta a notificação no estilo do doc
  (`Agora: <titulo>` + tempo + motivo + 1º passo) com botões de ação.
- O **Service Worker** (`sw.js > notificationclick`) trata o clique do botão
  **mesmo com o app fechado**: `aplicarDecisao(taskId, acao)` atualiza o status
  da tarefa no IndexedDB, registra o evento, e faz `postMessage` para as abas
  abertas atualizarem a tela (`app.js` escuta `navigator.serviceWorker.message`).
- **Modo foco**: `ativarModoFoco(min, …)` dispara lembrete a cada N min
  **enquanto o app está aberto** (setInterval).
- Limite do Chrome: só **2 botões** de ação personalizados por notificação.

### 5.5 Offline (`sw.js`)

Cache-first. **Importante:** o array `ASSETS` lista os arquivos a cachear e a
constante `CACHE` tem uma versão (`mira-v3`). **Sempre que mudar arquivos do app,
suba a versão** (`mira-v4`, etc.) e adicione arquivos novos ao `ASSETS`, senão o
celular continua servindo a versão velha do cache.

---

## 6. O que FALTA (sua missão, Codex)

Os eventos do `historico` já são gravados — você tem a matéria-prima pronta.

### Passo 7 — Memória compactada  (ver `docs/memoria_compactada.md`)

Transformar o histórico em **aprendizados curtos, úteis e editáveis**. NÃO treinar
IA; é heurística + (opcionalmente) a IA resumindo padrões.

**Formato sugerido** (criar store `memoria` no IndexedDB):

```json
{
  "id": "uuid",
  "memory_type": "preference|routine|behavior|business|notification_strategy",
  "content": "O usuário conclui melhor tarefas de 10 a 30 minutos.",
  "confidence": "alta|media|baixa",
  "is_active": true,
  "origem": "auto|manual",
  "criadaEm": 0
}
```

**Implementar:**
1. Store `memoria` em `db.js` (subir `DB_VERSION` para 2 + migração).
2. Função `compactarMemoria()` que roda o histórico e deriva padrões, ex:
   - tempo médio das tarefas concluídas → "conclui melhor tarefas de X a Y min";
   - categorias com muitos adiamentos → "tende a adiar tarefas de <categoria>";
   - tarefas vagas (título curto/genérico) muito adiadas → "adia títulos vagos".
   Rodar nos momentos do doc: fim do dia, fim de semana, tarefa adiada várias
   vezes, padrão repetido. **Não** atualizar a cada clique.
3. Tela/aba de memória: **ver, editar, apagar, desativar** (controle do usuário —
   requisito do doc). Pode ser uma 4ª aba ou uma seção em Config.
4. **Usar a memória**: injetar os aprendizados ativos no system prompt da IA
   revisora (já existe `loadSystemPrompt`/`carregarPrompt` — concatenar a memória)
   e, quando fizer sentido, ajustar `scoreTarefa` com regras de negócio aprendidas.
   Respeitar a economia de API (doc `arquitetura.md`: não mandar histórico inteiro,
   só memória compactada + tarefas relevantes + contexto do dia).

### Passo 8 — Laboratório de abordagens  (ver `docs/laboratorio_abordagens.md`)

Testar **estratégias de notificação** e medir qual funciona por categoria.

As estratégias já existem como rótulo em `notificacoes.js` (parâmetro `estrategia`)
e há 6 abordagens no doc: direta, motivo financeiro, passo pequeno, urgência,
anti-procrastinação, checklist técnico.

**Implementar:**
1. Ao enviar notificação, escolher uma `estrategia` (rotacionar/explorar) e gravar
   em `notificacao_enviada.extra.estrategia` (já gravamos o campo — falta variar
   o TEXTO por estratégia: hoje o texto é fixo, criar um gerador por estratégia).
2. Medir resultado correlacionando `notificacao_enviada` → próximo evento da mesma
   `tarefaId` (iniciou/concluiu/adiou/ignorou). "Ignorou" = nenhuma ação em X tempo.
3. Calcular taxa de sucesso por (categoria × estratégia) e salvar a vencedora como
   memória `notification_strategy` (liga com o passo 7).
4. Usar a estratégia vencedora da categoria nas próximas notificações daquela
   categoria. Respeitar as regras do doc (não testar muitas por dia, sem tom
   agressivo, sempre registrar).
5. (Opcional) Usar a IA para **gerar novas mensagens** quando uma categoria tem
   baixa conclusão — ver "Exemplo de prompt interno" no doc.

### Backlog (depois dos passos 7 e 8)

- **Servidor de push (VAPID)** para notificações agendadas com o app TOTALMENTE
  fechado. Hoje só há sob-demanda + modo foco (enquanto aberto). Isso é a única
  forma de ter "me avise às 10h" de verdade num PWA. Provável: Web Push + um
  endpoint serverless (Cloudflare Worker / Supabase Edge Function) guardando as
  subscriptions e disparando por cron.
- **Sync entre dispositivos** (Supabase): hoje os dados são por aparelho.
- Banner de "foco/observação" da IA (`proxima_acao_recomendada` / `observacao`)
  na Tela Agora.
- Ações que faltam na UI/notificação: Dividir e Justificar (o doc as cita).

---

## 7. Como rodar, testar e publicar

### Rodar localmente (PC)

```bash
npm run serve         # servidor estático em http://localhost:5050
```

Abra `http://localhost:5050`. Como é PWA com Service Worker e ES modules, **tem
que ser via http** (não abrir o index.html como file://).

### Testar a IA via terminal (Node)

```bash
npm run test:ia       # usa .env (DEEPSEEK_API_KEY) e bate na DeepSeek
```

### Testar no celular real (ADB)

O aparelho de teste é um **Xiaomi Redmi Note 7, Android 10** (tela 1080x2340),
conectado por USB com depuração ativada. ADB vem junto do scrcpy.

- Abrir o app: `adb shell am start -a android.intent.action.VIEW -d "https://maikon177.github.io/mira/"`
- Screenshot **(NÃO usar `>` no PowerShell — corrompe o PNG em UTF-16)**:
  ```
  adb shell screencap -p /sdcard/_x.png
  adb pull /sdcard/_x.png _x.png
  adb shell rm /sdcard/_x.png
  ```
- Tocar: `adb shell input tap X Y` · Digitar: `adb shell input text "abc"`
  (espaços viram `%s`; **acentos não funcionam** via `input text`).
- Central de notificações: `adb shell cmd statusbar expand-notifications`.

### Publicar (deploy)

```bash
git add .
git commit -m "..."
git push origin main          # GitHub Pages rebuilda sozinho (~1-2 min)
```

O deploy é automático (Pages serve a raiz da `main`). Após o push, **suba a versão
do cache no `sw.js`** se mudou arquivos, e force reload no aparelho (o SW tem
`skipWaiting`/`clients.claim`, então 2 reloads pegam a versão nova).

---

## 8. Convenções e armadilhas (LEIA)

- **Idioma**: tudo em pt-BR (variáveis, funções, UI, commits). Mantenha o estilo.
- **Sem build / sem dependências no front**: vanilla JS + ES modules. Não
  introduza React/bundler sem necessidade real — quebra a simplicidade local-first.
- **Chave da IA**: só em `localStorage`, nunca no repo (repo é público).
- **IndexedDB versionado**: mudou schema → sobe `DB_VERSION` + migração. O `sw.js`
  abre o mesmo banco; mantenha a versão coerente.
- **Service Worker cache**: mudou arquivos → sobe `CACHE` (`mira-vN`) e atualiza
  `ASSETS`. Senão o celular não vê a mudança.
- **PowerShell (ambiente Windows)**:
  - here-string `@'...'@` para `git commit -m` deu problema; prefira `git commit -F arquivo`.
  - redirecionar binário com `>` corrompe (UTF-16). Use `adb pull`.
  - line endings: os commits usam `git -c core.autocrlf=false` para evitar ruído CRLF.
- **DeepSeek**: OpenAI-compatible. Latência alta (modelo com reasoning). Sempre
  validar o JSON de volta (a função já trata erro de parse).
- **Notificações**: Chrome limita a 2 botões de ação personalizados; o terceiro
  ("Cancelar inscrição") é do próprio Chrome.

---

## 9. Estado atual (checklist do TODO.md)

- [x] Passo 2 — Testar API DeepSeek
- [x] Passo 3 — Base de tarefas (modelo, status, categorias, histórico, prioridade)
- [x] Passo 4 — Fluxo principal (Caixa, Agora, Hoje, ações de decisão)
- [x] Passo 5 — IA revisora
- [x] Passo 6 — Notificações decisivas
- [ ] **Passo 7 — Memória compactada**  ← próximo
- [ ] **Passo 8 — Laboratório de abordagens**
- [ ] Backlog: push agendado, sync Supabase, dividir/justificar, banner da IA

**Comece pelo passo 7.** Leia `docs/memoria_compactada.md`, implemente a store
`memoria` + `compactarMemoria()` + tela de controle, e ligue a memória ao prompt
da IA. Mantenha o princípio: **a memória reduz decisão, não manipula o usuário.**

Bom trabalho. 🎯
