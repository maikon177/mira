// Mira — armazenamento local (IndexedDB).
// Local-first: tudo fica no próprio aparelho, funciona offline e sem login.
//
// Duas "tabelas": tarefas e historico (eventos).

const DB_NAME = "mira";
const DB_VERSION = 1;

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
  return todos.sort((a, b) => b.em - a.em).slice(0, limite);
}
