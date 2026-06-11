// Mira — memória compactada.
// Transforma histórico de uso em aprendizados curtos, editáveis e locais.

import {
  atualizarMemoria,
  criarMemoria,
  listarHistorico,
  listarMemorias,
  listarTarefas,
} from "./db.js";

export const TIPOS_MEMORIA = [
  { valor: "preference", rotulo: "Preferência" },
  { valor: "routine", rotulo: "Rotina" },
  { valor: "behavior", rotulo: "Comportamento" },
  { valor: "business", rotulo: "Negócio" },
  { valor: "notification_strategy", rotulo: "Notificação" },
];

export const CONFIANCAS_MEMORIA = [
  { valor: "baixa", rotulo: "Baixa" },
  { valor: "media", rotulo: "Média" },
  { valor: "alta", rotulo: "Alta" },
];

export function rotuloTipoMemoria(tipo) {
  return TIPOS_MEMORIA.find((t) => t.valor === tipo)?.rotulo ?? tipo;
}

export function rotuloConfiancaMemoria(confianca) {
  return CONFIANCAS_MEMORIA.find((c) => c.valor === confianca)?.rotulo ?? confianca;
}

export function formatarMemoriasParaPrompt(memorias) {
  const ativas = memorias
    .filter((m) => m.is_active && m.content?.trim())
    .slice(0, 12);
  if (ativas.length === 0) return "";

  const linhas = ativas.map(
    (m) =>
      `- [${rotuloTipoMemoria(m.memory_type)} | confiança: ${rotuloConfiancaMemoria(
        m.confidence
      )}] ${m.content.trim()}`
  );

  return [
    "",
    "",
    "## Memória compactada do usuário",
    "Use estes aprendizados como contexto leve. Não substitua as regras fixas de prioridade do app.",
    ...linhas,
  ].join("\n");
}

export async function compactarMemoria() {
  const [historico, tarefas, memoriasExistentes] = await Promise.all([
    listarHistorico(null),
    listarTarefas(),
    listarMemorias(false),
  ]);

  const tarefasPorId = new Map(tarefas.map((t) => [t.id, t]));
  const aprendizados = gerarAprendizados(historico, tarefasPorId);
  let criadas = 0;
  let atualizadas = 0;
  let preservadas = 0;

  for (const aprendizado of aprendizados) {
    const existente = memoriasExistentes.find((m) => m.chave === aprendizado.chave);
    if (!existente) {
      await criarMemoria({ ...aprendizado, origem: "auto", is_active: true });
      criadas += 1;
      continue;
    }

    if (existente.origem === "manual" || existente.editadaManualmente) {
      preservadas += 1;
      continue;
    }

    await atualizarMemoria(existente.id, {
      ...aprendizado,
      origem: existente.origem ?? "auto",
      is_active: existente.is_active,
    });
    atualizadas += 1;
  }

  return {
    criadas,
    atualizadas,
    preservadas,
    analisadas: historico.length,
    encontradas: aprendizados.length,
  };
}

function gerarAprendizados(historico, tarefasPorId) {
  const concluidas = eventosComTarefa(historico, tarefasPorId, "tarefa_concluida");
  const adiadas = eventosComTarefa(historico, tarefasPorId, "tarefa_adiada");
  return [
    aprendizadoTempoConclusao(concluidas),
    ...aprendizadosCategoriasAdiadas(adiadas),
    aprendizadoTitulosVagos(adiadas),
  ].filter(Boolean);
}

function eventosComTarefa(historico, tarefasPorId, tipo) {
  return historico
    .filter((e) => e.tipo === tipo && tarefasPorId.has(e.tarefaId))
    .map((e) => ({ evento: e, tarefa: tarefasPorId.get(e.tarefaId) }));
}

function aprendizadoTempoConclusao(concluidas) {
  const tempos = concluidas
    .map(({ tarefa }) => tarefa.tempoEstimadoMin)
    .filter((n) => Number.isFinite(n) && n > 0)
    .sort((a, b) => a - b);

  if (tempos.length < 3) return null;

  const de = tempos[Math.floor((tempos.length - 1) * 0.25)];
  const ate = tempos[Math.ceil((tempos.length - 1) * 0.75)];
  return {
    chave: "auto:tempo_conclusao",
    memory_type: "behavior",
    content: `O usuário costuma concluir melhor tarefas de ${de} a ${ate} minutos.`,
    confidence: tempos.length >= 8 ? "alta" : "media",
    evidencias: { tarefasConcluidasComTempo: tempos.length, faixaMin: de, faixaMax: ate },
  };
}

function aprendizadosCategoriasAdiadas(adiadas) {
  const porCategoria = new Map();
  for (const { tarefa } of adiadas) {
    const categoria = tarefa.categoria || "Geral";
    porCategoria.set(categoria, (porCategoria.get(categoria) ?? 0) + 1);
  }

  return [...porCategoria.entries()]
    .filter(([, total]) => total >= 3)
    .sort((a, b) => b[1] - a[1])
    .slice(0, 3)
    .map(([categoria, total]) => ({
      chave: `auto:categoria_adiada:${normalizarChave(categoria)}`,
      memory_type: "behavior",
      content: `O usuário tende a adiar tarefas de ${categoria}; transforme a próxima ação em um passo menor antes de cobrar.`,
      confidence: total >= 6 ? "alta" : "media",
      evidencias: { categoria, adiamentos: total },
    }));
}

function aprendizadoTitulosVagos(adiadas) {
  const tarefasVagas = new Set();
  for (const { tarefa } of adiadas) {
    if (tituloVago(tarefa.titulo)) tarefasVagas.add(tarefa.id);
  }

  if (tarefasVagas.size < 2) return null;

  return {
    chave: "auto:titulos_vagos_adiados",
    memory_type: "behavior",
    content:
      "O usuário tende a adiar tarefas com título vago; ao revisar, force uma próxima ação concreta.",
    confidence: tarefasVagas.size >= 4 ? "alta" : "media",
    evidencias: { tarefasVagasAdiadas: tarefasVagas.size },
  };
}

function tituloVago(titulo) {
  const palavras = String(titulo || "")
    .trim()
    .split(/\s+/)
    .filter(Boolean);
  if (palavras.length <= 2) return true;
  return /^(ver|resolver|mexer|olhar|arrumar|fazer|coisa|pend[eê]ncia)$/i.test(
    palavras[0]
  );
}

function normalizarChave(valor) {
  return String(valor || "geral")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "_")
    .replace(/^_+|_+$/g, "");
}
