// Mira — cálculo simples de prioridade (camada FIXA, sem IA).
// A IA revisora pode sobrescrever depois, mas o app sabe priorizar sozinho.

const PESO_PRIORIDADE = { Alta: 100, Média: 50, Baixa: 20 };

// Categorias que destravam o dia / envolvem cliente ou dinheiro sobem.
const CATEGORIA_BONUS = [
  { re: /cliente|orçament|venda|financ|nota fiscal/i, bonus: 40 },
  { re: /manuten|bico|impressora|produç/i, bonus: 30 }, // produção bloqueada
  { re: /suprim|filament|estoque/i, bonus: 20 },
];

/**
 * Score maior = mais importante agora.
 * Considera: prioridade, categoria, esforço (tarefa rápida sobe um pouco)
 * e número de adiamentos (o que você foge sobe, pra não cair no esquecimento).
 */
export function scoreTarefa(t) {
  let score = PESO_PRIORIDADE[t.prioridade] ?? 50;

  const texto = `${t.categoria} ${t.titulo}`;
  for (const { re, bonus } of CATEGORIA_BONUS) {
    if (re.test(texto)) {
      score += bonus;
      break;
    }
  }

  // Tarefa curta (<= 15min) ganha um empurrão: destrava rápido.
  if (typeof t.tempoEstimadoMin === "number" && t.tempoEstimadoMin <= 15) {
    score += 15;
  }

  // Cada adiamento aumenta a urgência (evita procrastinação infinita).
  score += (t.adiamentos ?? 0) * 12;

  return score;
}

/** Ordena tarefas abertas da mais importante para a menos. */
export function ordenarPorPrioridade(tarefas) {
  return [...tarefas].sort((a, b) => scoreTarefa(b) - scoreTarefa(a));
}

/** A próxima melhor ação agora: a tarefa de maior score que está disponível. */
export function proximaAcao(tarefas) {
  const agora = Date.now();
  const disponiveis = tarefas.filter((t) => {
    if (["concluida", "cancelada"].includes(t.status)) return false;
    if (t.status === "adiada" && t.adiarAte && t.adiarAte > agora) return false;
    return true;
  });
  return ordenarPorPrioridade(disponiveis)[0] ?? null;
}
