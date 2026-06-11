// Mira — notificações decisivas.
// Mostra a próxima ação com botões de decisão. O Service Worker (sw.js)
// trata os cliques dos botões direto no banco, mesmo com o app fechado.

import { registrarEvento } from "./db.js";

export function suportaNotificacao() {
  return "Notification" in window && "serviceWorker" in navigator;
}

export function permissaoAtual() {
  return suportaNotificacao() ? Notification.permission : "unsupported";
}

/** Pede permissão (precisa ser chamada a partir de um toque do usuário). */
export async function pedirPermissao() {
  if (!suportaNotificacao()) return "unsupported";
  if (Notification.permission === "granted") return "granted";
  return Notification.requestPermission();
}

/**
 * Dispara a notificação decisiva da próxima ação.
 * Monta texto no estilo do doc: ação + tempo + motivo + botões.
 */
export async function notificarProximaAcao(tarefa, estrategia = "financeiro") {
  if (!tarefa) return false;
  if (permissaoAtual() !== "granted") return false;

  const reg = await navigator.serviceWorker.ready;

  const tempo = tarefa.tempoEstimadoMin ? `Leva ${tarefa.tempoEstimadoMin} min. ` : "";
  const motivo = tarefa.motivo ? tarefa.motivo : "";
  const passo = tarefa.proximaAcao ? `1º passo: ${tarefa.proximaAcao}` : "";
  const corpo = [`${tempo}${motivo}`.trim(), passo].filter(Boolean).join("\n");

  await reg.showNotification(`Agora: ${tarefa.titulo}`, {
    body: corpo || "Toque para abrir o Mira.",
    icon: "./assets/icon-192.png",
    badge: "./assets/icon-192.png",
    tag: "mira-proxima-acao", // substitui a anterior, não empilha
    renotify: true,
    requireInteraction: true, // não some sozinha — exige decisão
    data: { taskId: tarefa.id },
    actions: [
      { action: "concluir", title: "✓ Concluir" },
      { action: "adiar15", title: "⏱ +15 min" },
      { action: "iniciar", title: "▶ Iniciar" },
    ],
  });

  await registrarEvento("notificacao_enviada", tarefa.id, { estrategia });
  return true;
}

// ---------- Modo foco (lembrete periódico enquanto o app está vivo) ----------
let _intervalo = null;

export function modoFocoAtivo() {
  return _intervalo !== null;
}

export function ativarModoFoco(minutos, obterProximaAcao) {
  desativarModoFoco();
  const ms = Math.max(1, minutos) * 60000;
  _intervalo = setInterval(async () => {
    const alvo = await obterProximaAcao();
    if (alvo) notificarProximaAcao(alvo, "passo_pequeno");
  }, ms);
  localStorage.setItem("mira_modo_foco_min", String(minutos));
}

export function desativarModoFoco() {
  if (_intervalo) clearInterval(_intervalo);
  _intervalo = null;
  localStorage.removeItem("mira_modo_foco_min");
}
