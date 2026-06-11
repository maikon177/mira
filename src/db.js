// Mira — armazenamento local (IndexedDB).
// Local-first: tudo fica no próprio aparelho, funciona offline e sem login.
//
// "Tabelas": tarefas, historico (eventos) e memoria compactada.

const DB_NAME = "mira";
export const DB_VERSION = 2;

let _dbPromise = null;

function openDB() {
  if (_dbPromise) return _dbPromise;
  _dbPromise = new Promise((resolve, reject) => {
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
  return _dbPromise;
}

function tx(store, mode = "readonly") {
  return openDB().then((db) => db.transaction(store, mode).objectStore(store));
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
  const store = await tx("tarefas", "readwrite");
  await reqAsPromise(store.add(tarefa));
  await registrarEvento("tarefa_criada", tarefa.id, { titulo: tarefa.titulo });
  return tarefa;
}

export async function listarTarefas(filtroStatus = null) {
  const store = await tx("tarefas");
  const todas = await reqAsPromise(store.getAll());
  return filtroStatus ? todas.filter((t) => t.status === filtroStatus) : todas;
}

export async function obterTarefa(id) {
  const store = await tx("tarefas");
  return reqAsPromise(store.get(id));
}

export async function atualizarTarefa(id, mudancas) {
  const store = await tx("tarefas", "readwrite");
  const atual = await reqAsPromise(store.get(id));
  if (!atual) throw new Error("Tarefa não encontrada: " + id);
  const nova = { ...atual, ...mudancas, atualizadaEm: Date.now() };
  await reqAsPromise(store.put(nova));
  return nova;
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
  const store = await tx("tarefas", "readwrite");
  await reqAsPromise(store.delete(id));
}

// ---------- Histórico ----------

export async function registrarEvento(tipo, tarefaId = null, extra = {}) {
  const store = await tx("historico", "readwrite");
  await reqAsPromise(store.add({ tipo, tarefaId, extra, em: Date.now() }));
}

export async function listarHistorico(limite = 50) {
  const store = await tx("historico");
  const todos = await reqAsPromise(store.getAll());
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
  const store = await tx("memoria", "readwrite");
  await reqAsPromise(store.add(memoria));
  return memoria;
}

export async function listarMemorias(apenasAtivas = false) {
  const store = await tx("memoria");
  const todas = await reqAsPromise(store.getAll());
  const filtradas = apenasAtivas ? todas.filter((m) => m.is_active) : todas;
  return filtradas.sort((a, b) => (b.atualizadaEm ?? b.criadaEm) - (a.atualizadaEm ?? a.criadaEm));
}

export async function atualizarMemoria(id, mudancas) {
  const store = await tx("memoria", "readwrite");
  const atual = await reqAsPromise(store.get(id));
  if (!atual) throw new Error("Memória não encontrada: " + id);
  const nova = { ...atual, ...mudancas, atualizadaEm: Date.now() };
  await reqAsPromise(store.put(nova));
  return nova;
}

export async function deletarMemoria(id) {
  const store = await tx("memoria", "readwrite");
  await reqAsPromise(store.delete(id));
}
