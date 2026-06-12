// Mira — notificações decisivas.
// Mostra a próxima ação com botões de decisão. O Service Worker (sw.js)
// trata os cliques dos botões direto no banco, mesmo com o app fechado.

import { registrarEvento } from "./db.js";
import {
  atualizarLaboratorioAbordagens,
  escolherEstrategiaNotificacao,
  gerarConteudoNotificacao,
  rotuloEstrategia,
} from "./laboratorio.js";

const RODANDO_NO_APK_ANDROID = new URLSearchParams(location.search).has("android");

export function suportaNotificacao() {
  return !RODANDO_NO_APK_ANDROID && "Notification" in window && "serviceWorker" in navigator;
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
export async function notificarProximaAcao(tarefa, estrategia = null) {
  if (!tarefa) return false;
  if (permissaoAtual() !== "granted") return false;

  const reg = await navigator.serviceWorker.ready;
  const estrategiaEscolhida = estrategia ?? (await escolherEstrategiaNotificacao(tarefa));
  const conteudo = gerarConteudoNotificacao(tarefa, estrategiaEscolhida);

  await reg.showNotification(conteudo.titulo, {
    body: conteudo.corpo || "Toque para abrir o Mira.",
    icon: "./assets/icon-192.png",
    badge: "./assets/icon-192.png",
    tag: "mira-proxima-acao", // substitui a anterior, não empilha
    renotify: true,
    requireInteraction: true, // não some sozinha — exige decisão
    data: {
      taskId: tarefa.id,
      estrategia: estrategiaEscolhida,
      categoria: tarefa.categoria || "Geral",
    },
    actions: [
      { action: "concluir", title: "✓ Concluir" },
      { action: "adiar15", title: "⏱ +15 min" },
      { action: "iniciar", title: "▶ Iniciar" },
    ],
  });

  await registrarEvento("notificacao_enviada", tarefa.id, {
    estrategia: estrategiaEscolhida,
    estrategiaRotulo: rotuloEstrategia(estrategiaEscolhida),
    categoria: tarefa.categoria || "Geral",
    titulo: tarefa.titulo,
  });
  await atualizarLaboratorioAbordagens();
  return { ok: true, estrategia: estrategiaEscolhida };
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
