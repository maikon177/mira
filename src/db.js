// Mira — armazenamento local (IndexedDB).
// Local-first: tudo fica no próprio aparelho, funciona offline e sem login.
//
// "Tabelas": tarefas, historico (eventos) e memoria compactada.

const DB_NAME = "mira";
export const DB_VERSION = 2;

let _dbPromise = null;
let _usarFallbackLocal = false;
const LS_FALLBACK_KEY = "mira_db_fallback";
const RODANDO_NO_APK_ANDROID = new URLSearchParams(location.search).has("android");

function openDB() {
  if (!("indexedDB" in window)) {
    if (RODANDO_NO_APK_ANDROID) _usarFallbackLocal = true;
    return Promise.reject(new Error("IndexedDB indisponível"));
  }
  if (_dbPromise) return _dbPromise;
  const abertura = new Promise((resolve, reject) => {
    const req = indexedDB.open(DB_NAME, DB_VERSION);
    req.onupgradeneeded = () => {
      const db = req.result;
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
    req.onsuccess = () => resolve(req.result);
    req.onerror = () => reject(req.error);
  });
  _dbPromise = (RODANDO_NO_APK_ANDROID ? withTimeout(abertura, 2500) : abertura).catch((erro) => {
    if (RODANDO_NO_APK_ANDROID) {
      console.warn("IndexedDB indisponível, usando fallback localStorage.", erro);
      _usarFallbackLocal = true;
    }
    _dbPromise = null;
    throw erro;
  });
  return _dbPromise;
}

function tx(store, mode = "readonly") {
  return openDB().then((db) => db.transaction(store, mode).objectStore(store));
}

function withTimeout(promise, ms) {
  return new Promise((resolve, reject) => {
    const timer = setTimeout(() => reject(new Error("Tempo esgotado ao abrir IndexedDB")), ms);
    promise.then(
      (valor) => {
        clearTimeout(timer);
        resolve(valor);
      },
      (erro) => {
        clearTimeout(timer);
        reject(erro);
      }
    );
  });
}

function fallbackDB() {
  const vazio = { tarefas: [], historico: [], memoria: [], historicoSeq: 1 };
  try {
    return { ...vazio, ...(JSON.parse(localStorage.getItem(LS_FALLBACK_KEY)) || {}) };
  } catch {
    return vazio;
  }
}

function salvarFallback(db) {
  localStorage.setItem(LS_FALLBACK_KEY, JSON.stringify(db));
}

async function usarStore(nome, modo, operacao, fallback) {
  if (_usarFallbackLocal) return fallback();
  try {
    const store = await tx(nome, modo);
    return await operacao(store);
  } catch (erro) {
    if (!RODANDO_NO_APK_ANDROID) throw erro;
    console.warn(`Falha no IndexedDB (${nome}), usando fallback localStorage.`, erro);
    _usarFallbackLocal = true;
    return fallback();
  }
}

function reqAsPromise(request) {
  return new Promise((resolve, reject) => {
    request.onsuccess = () => resolve(request.result);
    request.onerror = () => reject(request.error);
  });
}

// ---------- Tarefas ----------

/** Cria uma tarefa. Aceita os campos vindos da IA ou um título solto. */
export async function criarTarefa(dados) {
  const agora = Date.now();
  const tarefa = {
    id: crypto.randomUUID(),
    titulo: dados.titulo ?? "(sem título)",
    categoria: dados.categoria ?? "Geral",
    prioridade: dados.prioridade ?? "Média",
    tempoEstimadoMin: dados.tempo_estimado_minutos ?? dados.tempoEstimadoMin ?? null,
    motivo: dados.motivo ?? "",
    proximaAcao: dados.proxima_acao ?? dados.proximaAcao ?? "",
    alertaSugerido: dados.alerta_sugerido ?? dados.alertaSugerido ?? null,
    status: dados.status ?? "aberta", // aberta | fazendo | concluida | adiada | cancelada
    revisadaIA: dados.revisadaIA ?? false,
    adiamentos: 0,
    criadaEm: agora,
    atualizadaEm: agora,
  };
  await usarStore(
    "tarefas",
    "readwrite",
    (store) => reqAsPromise(store.add(tarefa)),
    () => {
      const db = fallbackDB();
      db.tarefas.push(tarefa);
      salvarFallback(db);
    }
  );
  await registrarEvento("tarefa_criada", tarefa.id, { titulo: tarefa.titulo });
  return tarefa;
}

export async function listarTarefas(filtroStatus = null) {
  const todas = await usarStore(
    "tarefas",
    "readonly",
    (store) => reqAsPromise(store.getAll()),
    () => fallbackDB().tarefas
  );
  return filtroStatus ? todas.filter((t) => t.status === filtroStatus) : todas;
}

export async function obterTarefa(id) {
  return usarStore(
    "tarefas",
    "readonly",
    (store) => reqAsPromise(store.get(id)),
    () => fallbackDB().tarefas.find((t) => t.id === id)
  );
}

export async function atualizarTarefa(id, mudancas) {
  return usarStore(
    "tarefas",
    "readwrite",
    async (store) => {
      const atual = await reqAsPromise(store.get(id));
      if (!atual) throw new Error("Tarefa não encontrada: " + id);
      const nova = { ...atual, ...mudancas, atualizadaEm: Date.now() };
      await reqAsPromise(store.put(nova));
      return nova;
    },
    () => {
      const db = fallbackDB();
      const atual = db.tarefas.find((t) => t.id === id);
      if (!atual) throw new Error("Tarefa não encontrada: " + id);
      const nova = { ...atual, ...mudancas, atualizadaEm: Date.now() };
      db.tarefas = db.tarefas.map((t) => (t.id === id ? nova : t));
      salvarFallback(db);
      return nova;
    }
  );
}

// Ações de decisão usadas pela Tela Agora ---------------------------

export async function concluirTarefa(id) {
  const t = await atualizarTarefa(id, { status: "concluida" });
  await registrarEvento("tarefa_concluida", id);
  return t;
}

export async function adiarTarefa(id, minutos) {
  const atual = await obterTarefa(id);
  const t = await atualizarTarefa(id, {
    status: "adiada",
    adiarAte: Date.now() + minutos * 60_000,
    adiamentos: (atual?.adiamentos ?? 0) + 1,
  });
  await registrarEvento("tarefa_adiada", id, { minutos });
  return t;
}

export async function iniciarTarefa(id) {
  const t = await atualizarTarefa(id, { status: "fazendo" });
  await registrarEvento("tarefa_iniciada", id);
  return t;
}

export async function cancelarTarefa(id) {
  const t = await atualizarTarefa(id, { status: "cancelada" });
  await registrarEvento("tarefa_cancelada", id);
  return t;
}

export async function deletarTarefa(id) {
  await usarStore(
    "tarefas",
    "readwrite",
    (store) => reqAsPromise(store.delete(id)),
    () => {
      const db = fallbackDB();
      db.tarefas = db.tarefas.filter((t) => t.id !== id);
      salvarFallback(db);
    }
  );
}

// ---------- Histórico ----------

export async function registrarEvento(tipo, tarefaId = null, extra = {}) {
  await usarStore(
    "historico",
    "readwrite",
    (store) => reqAsPromise(store.add({ tipo, tarefaId, extra, em: Date.now() })),
    () => {
      const db = fallbackDB();
      db.historico.push({
        id: db.historicoSeq++,
        tipo,
        tarefaId,
        extra,
        em: Date.now(),
      });
      salvarFallback(db);
    }
  );
}

export async function listarHistorico(limite = 50) {
  const todos = await usarStore(
    "historico",
    "readonly",
    (store) => reqAsPromise(store.getAll()),
    () => fallbackDB().historico
  );
  const ordenados = todos.sort((a, b) => b.em - a.em);
  return limite ? ordenados.slice(0, limite) : ordenados;
}

// ---------- Memória compactada ----------

export async function criarMemoria(dados) {
  const agora = Date.now();
  const memoria = {
    id: dados.id ?? crypto.randomUUID(),
    memory_type: dados.memory_type ?? "behavior",
    content: dados.content ?? "",
    confidence: dados.confidence ?? "baixa",
    is_active: dados.is_active ?? true,
    origem: dados.origem ?? "manual",
    chave: dados.chave ?? null,
    evidencias: dados.evidencias ?? {},
    criadaEm: dados.criadaEm ?? agora,
    atualizadaEm: agora,
  };
  await usarStore(
    "memoria",
    "readwrite",
    (store) => reqAsPromise(store.add(memoria)),
    () => {
      const db = fallbackDB();
      db.memoria.push(memoria);
      salvarFallback(db);
    }
  );
  return memoria;
}

export async function listarMemorias(apenasAtivas = false) {
  const todas = await usarStore(
    "memoria",
    "readonly",
    (store) => reqAsPromise(store.getAll()),
    () => fallbackDB().memoria
  );
  const filtradas = apenasAtivas ? todas.filter((m) => m.is_active) : todas;
  return filtradas.sort((a, b) => (b.atualizadaEm ?? b.criadaEm) - (a.atualizadaEm ?? a.criadaEm));
}

export async function atualizarMemoria(id, mudancas) {
  return usarStore(
    "memoria",
    "readwrite",
    async (store) => {
      const atual = await reqAsPromise(store.get(id));
      if (!atual) throw new Error("Memória não encontrada: " + id);
      const nova = { ...atual, ...mudancas, atualizadaEm: Date.now() };
      await reqAsPromise(store.put(nova));
      return nova;
    },
    () => {
      const db = fallbackDB();
      const atual = db.memoria.find((m) => m.id === id);
      if (!atual) throw new Error("Memória não encontrada: " + id);
      const nova = { ...atual, ...mudancas, atualizadaEm: Date.now() };
      db.memoria = db.memoria.map((m) => (m.id === id ? nova : m));
      salvarFallback(db);
      return nova;
    }
  );
}

export async function deletarMemoria(id) {
  await usarStore(
    "memoria",
    "readwrite",
    (store) => reqAsPromise(store.delete(id)),
    () => {
      const db = fallbackDB();
      db.memoria = db.memoria.filter((m) => m.id !== id);
      salvarFallback(db);
    }
  );
}
