// Mira — laboratório de abordagens.
// Escolhe, testa e mede estratégias de notificação sem depender da IA.

import {
  atualizarMemoria,
  criarMemoria,
  listarHistorico,
  listarMemorias,
  listarTarefas,
} from "./db.js";

const JANELA_RESPOSTA_MS = 2 * 60 * 60 * 1000;
const UM_DIA_MS = 24 * 60 * 60 * 1000;

export const ESTRATEGIAS_NOTIFICACAO = [
  { valor: "direta", rotulo: "Direta" },
  { valor: "financeiro", rotulo: "Motivo financeiro" },
  { valor: "passo_pequeno", rotulo: "Passo pequeno" },
  { valor: "urgencia", rotulo: "Urgência" },
  { valor: "anti_procrastinacao", rotulo: "Anti-procrastinação" },
  { valor: "checklist_tecnico", rotulo: "Checklist técnico" },
];

const EVENTOS_RESPOSTA = new Set([
  "notificacao_respondida",
  "tarefa_iniciada",
  "tarefa_concluida",
  "tarefa_adiada",
  "tarefa_cancelada",
]);

export function rotuloEstrategia(valor) {
  return ESTRATEGIAS_NOTIFICACAO.find((e) => e.valor === valor)?.rotulo ?? valor;
}

export async function escolherEstrategiaNotificacao(tarefa) {
  const [memorias, historico] = await Promise.all([
    listarMemorias(true),
    listarHistorico(null),
  ]);

  const vencedora = estrategiaVencedoraDaCategoria(tarefa, memorias);
  if (vencedora) return vencedora;

  const categoria = categoriaDaTarefa(tarefa);
  const agora = Date.now();
  const enviadas = historico.filter(
    (e) =>
      e.tipo === "notificacao_enviada" &&
      categoriaEvento(e) === categoria &&
      estrategiaValida(e.extra?.estrategia)
  );
  const enviadasHoje = enviadas.filter((e) => agora - e.em < UM_DIA_MS);

  // Evita testar várias abordagens para a mesma categoria no mesmo dia.
  if (enviadasHoje.length > 0) return enviadasHoje[0].extra.estrategia;

  if (enviadas.length > 0) {
    return ESTRATEGIAS_NOTIFICACAO[enviadas.length % ESTRATEGIAS_NOTIFICACAO.length].valor;
  }

  return estrategiaInicialPorTarefa(tarefa);
}

export function gerarConteudoNotificacao(tarefa, estrategia) {
  const titulo = tarefa.titulo || "tarefa";
  const tempo = tarefa.tempoEstimadoMin ? `Leva ${tarefa.tempoEstimadoMin} min.` : "";
  const motivo = tarefa.motivo || "";
  const passo = tarefa.proximaAcao || `começar por ${titulo}`;

  const textos = {
    direta: [`Agora faça: ${titulo}.`, tempo, motivo],
    financeiro: [
      `${titulo} pode proteger uma venda, prazo ou pagamento.`,
      tempo,
      motivo,
      tarefa.proximaAcao ? `Primeiro passo: ${passo}` : "",
    ],
    passo_pequeno: [`Primeiro passo: ${passo}.`, tempo, motivo],
    urgencia: [
      `Se isso está segurando entrega, cliente ou produção, resolva agora: ${titulo}.`,
      tempo,
      tarefa.proximaAcao ? `Comece por: ${passo}` : motivo,
    ],
    anti_procrastinacao: [
      "Faça só 5 minutos. Depois você decide se continua.",
      `Comece por: ${passo}.`,
      tempo,
    ],
    checklist_tecnico: [
      `Checklist técnico: ${passo}.`,
      motivo,
      tempo,
    ],
  };

  return {
    titulo: `Agora: ${titulo}`,
    corpo: (textos[estrategia] ?? textos.direta).filter(Boolean).join("\n"),
  };
}

export async function atualizarLaboratorioAbordagens() {
  const [historico, tarefas, memoriasExistentes] = await Promise.all([
    listarHistorico(null),
    listarTarefas(),
    listarMemorias(false),
  ]);

  const tarefasPorId = new Map(tarefas.map((t) => [t.id, t]));
  const avaliacoes = avaliarNotificacoes(historico, tarefasPorId);
  const vencedoras = calcularVencedoras(avaliacoes);

  let criadas = 0;
  let atualizadas = 0;
  let preservadas = 0;

  for (const vencedora of vencedoras) {
    const existente = memoriasExistentes.find((m) => m.chave === vencedora.chave);
    if (!existente) {
      await criarMemoria({ ...vencedora, origem: "auto", is_active: true });
      criadas += 1;
      continue;
    }
    if (existente.origem === "manual" || existente.editadaManualmente) {
      preservadas += 1;
      continue;
    }
    await atualizarMemoria(existente.id, {
      ...vencedora,
      origem: existente.origem ?? "auto",
      is_active: existente.is_active,
    });
    atualizadas += 1;
  }

  return {
    criadas,
    atualizadas,
    preservadas,
    avaliadas: avaliacoes.length,
    vencedoras: vencedoras.length,
  };
}

export function avaliarNotificacoes(historico, tarefasPorId = new Map(), agora = Date.now()) {
  const eventos = [...historico].sort((a, b) => a.em - b.em);
  const envios = eventos.filter((e) => e.tipo === "notificacao_enviada");

  return envios
    .map((envio) => {
      const estrategia = envio.extra?.estrategia;
      if (!estrategiaValida(estrategia)) return null;

      const proximoEnvio = eventos.find(
        (e) =>
          e.em > envio.em &&
          e.tarefaId === envio.tarefaId &&
          e.tipo === "notificacao_enviada"
      );
      const limiteResposta = Math.min(
        envio.em + JANELA_RESPOSTA_MS,
        proximoEnvio?.em ?? Infinity
      );
      const resposta = eventos.find(
        (e) =>
          e.em > envio.em &&
          e.tarefaId === envio.tarefaId &&
          EVENTOS_RESPOSTA.has(e.tipo) &&
          e.em <= limiteResposta
      );

      if (!resposta && !proximoEnvio && agora - envio.em < JANELA_RESPOSTA_MS) {
        return null;
      }

      const resultado = resposta ? resultadoDoEvento(resposta) : "ignorou";
      return {
        categoria:
          categoriaEvento(envio) ||
          tarefasPorId.get(envio.tarefaId)?.categoria ||
          "Geral",
        estrategia,
        resultado,
        sucesso: resultado === "iniciou" || resultado === "concluiu",
        envioEm: envio.em,
        respostaEm: resposta?.em ?? null,
      };
    })
    .filter(Boolean);
}

function calcularVencedoras(avaliacoes) {
  const porCategoria = new Map();
  for (const avaliacao of avaliacoes) {
    const categoria = avaliacao.categoria || "Geral";
    if (!porCategoria.has(categoria)) porCategoria.set(categoria, new Map());
    const porEstrategia = porCategoria.get(categoria);
    if (!porEstrategia.has(avaliacao.estrategia)) {
      porEstrategia.set(avaliacao.estrategia, {
        categoria,
        estrategia: avaliacao.estrategia,
        total: 0,
        sucessos: 0,
        resultados: {},
      });
    }
    const stats = porEstrategia.get(avaliacao.estrategia);
    stats.total += 1;
    if (avaliacao.sucesso) stats.sucessos += 1;
    stats.resultados[avaliacao.resultado] =
      (stats.resultados[avaliacao.resultado] ?? 0) + 1;
  }

  const vencedoras = [];
  for (const [categoria, porEstrategia] of porCategoria.entries()) {
    const stats = [...porEstrategia.values()];
    const totalCategoria = stats.reduce((total, item) => total + item.total, 0);
    if (totalCategoria < 4) continue;

    stats.sort((a, b) => taxaSucesso(b) - taxaSucesso(a) || b.total - a.total);
    const melhor = stats[0];
    if (melhor.total < 2) continue;

    const taxa = taxaSucesso(melhor);
    vencedoras.push({
      chave: `auto:notificacao:${normalizarChave(categoria)}`,
      memory_type: "notification_strategy",
      content: `Para tarefas de ${categoria}, notificações com abordagem ${rotuloEstrategia(
        melhor.estrategia
      )} funcionam melhor.`,
      confidence: totalCategoria >= 8 && taxa >= 0.6 ? "alta" : "media",
      evidencias: {
        categoria,
        estrategia: melhor.estrategia,
        taxaSucesso: Number(taxa.toFixed(2)),
        totalCategoria,
        totalEstrategia: melhor.total,
        sucessos: melhor.sucessos,
        resultados: melhor.resultados,
      },
    });
  }
  return vencedoras;
}

function estrategiaVencedoraDaCategoria(tarefa, memorias) {
  const categoria = normalizarChave(categoriaDaTarefa(tarefa));
  const memoria = memorias.find(
    (m) =>
      m.memory_type === "notification_strategy" &&
      estrategiaValida(m.evidencias?.estrategia) &&
      normalizarChave(m.evidencias?.categoria) === categoria
  );
  return memoria?.evidencias?.estrategia ?? null;
}

function estrategiaInicialPorTarefa(tarefa) {
  const texto = `${tarefa.categoria} ${tarefa.titulo}`.toLowerCase();
  if ((tarefa.adiamentos ?? 0) >= 2) return "anti_procrastinacao";
  if (/manuten|bico|impressora|t[eé]cnic/.test(texto)) return "checklist_tecnico";
  if (/cliente|orçament|venda|financeiro|nota fiscal|pagamento/.test(texto)) {
    return "financeiro";
  }
  if (/prazo|atras|entrega|urg/.test(texto)) return "urgencia";
  if (/modelagem|criativ|desenho|arte/.test(texto)) return "passo_pequeno";
  return "direta";
}

function resultadoDoEvento(evento) {
  if (evento.tipo === "tarefa_iniciada") return "iniciou";
  if (evento.tipo === "tarefa_concluida") return "concluiu";
  if (evento.tipo === "tarefa_adiada") return "adiou";
  if (evento.tipo === "tarefa_cancelada") return "cancelou";
  if (evento.tipo === "notificacao_respondida") return "abriu";
  return "ignorou";
}

function taxaSucesso(stats) {
  return stats.total ? stats.sucessos / stats.total : 0;
}

function categoriaEvento(evento) {
  return evento.extra?.categoria || "Geral";
}

function categoriaDaTarefa(tarefa) {
  return tarefa?.categoria || "Geral";
}

function estrategiaValida(valor) {
  return ESTRATEGIAS_NOTIFICACAO.some((e) => e.valor === valor);
}

function normalizarChave(valor) {
  return String(valor || "geral")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "_")
    .replace(/^_+|_+$/g, "");
}
