// Mira — service worker simples para funcionar offline (local-first).
// Estratégia: cache-first para os arquivos do app.

const CACHE = "mira-v4";
const ASSETS = [
  "./",
  "./index.html",
  "./styles.css",
  "./manifest.webmanifest",
  "./src/app.js",
  "./src/db.js",
  "./src/memoria.js",
  "./src/prioridade.js",
  "./src/ia/revisora-web.js",
  "./src/ia/prompt_ia_revisora.md",
  "./src/notificacoes.js",
  "./assets/icon-192.png",
  "./assets/icon-512.png",
];

self.addEventListener("install", (e) => {
  e.waitUntil(caches.open(CACHE).then((c) => c.addAll(ASSETS)));
  self.skipWaiting();
});

self.addEventListener("activate", (e) => {
  e.waitUntil(
    caches
      .keys()
      .then((keys) =>
        Promise.all(keys.filter((k) => k !== CACHE).map((k) => caches.delete(k)))
      )
  );
  self.clients.claim();
});

self.addEventListener("fetch", (e) => {
  if (e.request.method !== "GET") return;
  e.respondWith(
    caches.match(e.request).then(
      (hit) =>
        hit ||
        fetch(e.request)
          .then((res) => {
            const copy = res.clone();
            caches.open(CACHE).then((c) => c.put(e.request, copy));
            return res;
          })
          .catch(() => hit)
    )
  );
});

// ===================== Notificações decisivas =====================
// O SW trata os botões da notificação direto no IndexedDB, mesmo com o
// app fechado: atualiza o status da tarefa e registra o evento (métricas).

function idbOpen() {
  return new Promise((resolve, reject) => {
    const r = indexedDB.open("mira", 2);
    r.onupgradeneeded = () => {
      const db = r.result;
      if (!db.objectStoreNames.contains("tarefas")) {
        const store = db.createObjectStore("tarefas", { keyPath: "id" });
        store.createIndex("status", "status", { unique: false });
        store.createIndex("criadaEm", "criadaEm", { unique: false });
      }
      if (!db.objectStoreNames.contains("historico")) {
        const h = db.createObjectStore("historico", {
          keyPath: "id",
          autoIncrement: true,
        });
        h.createIndex("em", "em", { unique: false });
      }
      if (!db.objectStoreNames.contains("memoria")) {
        const m = db.createObjectStore("memoria", { keyPath: "id" });
        m.createIndex("memory_type", "memory_type", { unique: false });
        m.createIndex("is_active", "is_active", { unique: false });
        m.createIndex("origem", "origem", { unique: false });
        m.createIndex("criadaEm", "criadaEm", { unique: false });
      }
    };
    r.onsuccess = () => resolve(r.result);
    r.onerror = () => reject(r.error);
  });
}
function idbReq(req) {
  return new Promise((res, rej) => {
    req.onsuccess = () => res(req.result);
    req.onerror = () => rej(req.error);
  });
}

async function aplicarDecisao(taskId, acao) {
  const db = await idbOpen();
  // Atualiza a tarefa
  const store = db.transaction("tarefas", "readwrite").objectStore("tarefas");
  const t = await idbReq(store.get(taskId));
  if (t) {
    const agora = Date.now();
    if (acao === "concluir") t.status = "concluida";
    else if (acao === "iniciar") t.status = "fazendo";
    else if (acao === "cancelar") t.status = "cancelada";
    else if (acao === "adiar15") {
      t.status = "adiada";
      t.adiarAte = agora + 15 * 60000;
      t.adiamentos = (t.adiamentos || 0) + 1;
    } else if (acao === "adiar60") {
      t.status = "adiada";
      t.adiarAte = agora + 60 * 60000;
      t.adiamentos = (t.adiamentos || 0) + 1;
    }
    t.atualizadaEm = agora;
    await idbReq(store.put(t));
  }
  // Registra evento (métrica do laboratório de abordagens)
  const mapa = {
    concluir: "tarefa_concluida",
    iniciar: "tarefa_iniciada",
    cancelar: "tarefa_cancelada",
    adiar15: "tarefa_adiada",
    adiar60: "tarefa_adiada",
  };
  const hist = db.transaction("historico", "readwrite").objectStore("historico");
  await idbReq(
    hist.add({
      tipo: mapa[acao] || "notificacao_respondida",
      tarefaId: taskId,
      extra: { via: "notificacao", acao },
      em: Date.now(),
    })
  );
}

self.addEventListener("notificationclick", (e) => {
  const acao = e.action; // "" quando toca no corpo
  const taskId = e.notification.data?.taskId;
  e.notification.close();

  e.waitUntil(
    (async () => {
      if (acao && acao !== "abrir" && taskId) {
        await aplicarDecisao(taskId, acao);
      }
      // Avisa as abas abertas pra atualizar a tela
      const clientsList = await self.clients.matchAll({
        type: "window",
        includeUncontrolled: true,
      });
      for (const c of clientsList) c.postMessage({ tipo: "decisao", acao, taskId });

      // Abre/foca o app se tocou no corpo ou em "Iniciar"
      if ((!acao || acao === "iniciar" || acao === "abrir") && clientsList[0]) {
        await clientsList[0].focus();
      } else if (!acao || acao === "iniciar" || acao === "abrir") {
        await self.clients.openWindow("./index.html");
      }
    })()
  );
});
