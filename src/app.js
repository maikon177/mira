// Mira — lógica da interface (vanilla JS, sem build).
// Liga o armazenamento local (db.js) e o cálculo de prioridade (prioridade.js)
// às três telas: Agora, Hoje e Caixa de entrada.

import {
  criarTarefa,
  listarTarefas,
  concluirTarefa,
  adiarTarefa,
  iniciarTarefa,
  cancelarTarefa,
  deletarTarefa,
} from "./db.js";
import { proximaAcao, ordenarPorPrioridade } from "./prioridade.js";
import {
  reviewTaskInput,
  getApiKey,
  setApiKey,
  temApiKey,
} from "./ia/revisora-web.js";
import {
  suportaNotificacao,
  permissaoAtual,
  pedirPermissao,
  notificarProximaAcao,
  ativarModoFoco,
  desativarModoFoco,
  modoFocoAtivo,
} from "./notificacoes.js";

// Próxima ação atual (usada pelo modo foco e pela notificação)
async function obterProximaAcaoAtual() {
  return proximaAcao(await listarTarefas());
}

const $ = (sel) => document.querySelector(sel);
const $$ = (sel) => document.querySelectorAll(sel);

// ---------- Navegação entre telas ----------
function irPara(tela) {
  $$(".view").forEach((v) => v.removeAttribute("data-active"));
  $(`#view-${tela}`).setAttribute("data-active", "");
  $$(".tab").forEach((t) =>
    t.dataset.go === tela
      ? t.setAttribute("data-active", "")
      : t.removeAttribute("data-active")
  );
  if (tela === "agora") renderAgora();
  if (tela === "hoje") renderHoje();
  if (tela === "caixa") {
    atualizarConfigIA();
    renderCaixa();
  }
}

$$(".tab").forEach((t) =>
  t.addEventListener("click", () => irPara(t.dataset.go))
);

// ---------- Tela Agora ----------
async function renderAgora() {
  const tarefas = await listarTarefas();
  const alvo = proximaAcao(tarefas);
  const wrap = $("#agora-wrap");

  if (!alvo) {
    wrap.innerHTML = `
      <div class="vazio">
        <div class="big">◎</div>
        <p>Nada na mira agora.</p>
        <button class="btn btn-primary" id="ir-caixa">Adicionar uma tarefa</button>
      </div>`;
    $("#ir-caixa")?.addEventListener("click", () => irPara("caixa"));
    return;
  }

  wrap.innerHTML = `
    <div class="agora-card">
      <div class="agora-label">Agora</div>
      <h1 class="agora-titulo">${escapeHtml(alvo.titulo)}</h1>
      ${
        alvo.proximaAcao
          ? `<div class="agora-acao">→ ${escapeHtml(alvo.proximaAcao)}</div>`
          : ""
      }
      <div class="agora-meta">
        <span class="chip prio-${alvo.prioridade}">${alvo.prioridade}</span>
        ${
          alvo.tempoEstimadoMin
            ? `<span class="chip">${alvo.tempoEstimadoMin} min</span>`
            : ""
        }
        <span class="chip">${escapeHtml(alvo.categoria)}</span>
        ${
          alvo.adiamentos
            ? `<span class="chip">adiada ${alvo.adiamentos}x</span>`
            : ""
        }
      </div>
      ${alvo.motivo ? `<p class="agora-motivo">${escapeHtml(alvo.motivo)}</p>` : ""}

      <div class="acoes">
        <button class="btn btn-ok btn-full" data-acao="concluir">✓ Concluir</button>
        <button class="btn" data-acao="iniciar">▶ Iniciar</button>
        <button class="btn" data-acao="adiar15">⏱ +15 min</button>
        <button class="btn" data-acao="adiar60">⏱ +1 hora</button>
        <button class="btn btn-ghost" data-acao="cancelar">✕ Cancelar</button>
      </div>

      <div class="notif-row">
        <button class="btn btn-ghost" id="btn-lembrar">🔔 Me lembrar</button>
        <button class="btn btn-ghost" id="btn-foco">${
          modoFocoAtivo() ? "🎯 Foco ativo" : "🎯 Modo foco"
        }</button>
      </div>
    </div>`;

  wrap.querySelectorAll("[data-acao]").forEach((b) =>
    b.addEventListener("click", async () => {
      const a = b.dataset.acao;
      if (a === "concluir") await concluirTarefa(alvo.id);
      else if (a === "iniciar") await iniciarTarefa(alvo.id);
      else if (a === "adiar15") await adiarTarefa(alvo.id, 15);
      else if (a === "adiar60") await adiarTarefa(alvo.id, 60);
      else if (a === "cancelar") await cancelarTarefa(alvo.id);
      renderAgora();
    })
  );

  // 🔔 Me lembrar: dispara a notificação decisiva da próxima ação
  wrap.querySelector("#btn-lembrar")?.addEventListener("click", async () => {
    if (!suportaNotificacao()) return alert("Seu navegador não suporta notificações.");
    const p = await pedirPermissao();
    if (p !== "granted") return alert("Ative as notificações nas permissões do site.");
    const ok = await notificarProximaAcao(alvo);
    if (ok) flash("🔔 Notificação enviada — veja na barra de avisos.");
  });

  // 🎯 Modo foco: liga/desliga lembrete periódico
  wrap.querySelector("#btn-foco")?.addEventListener("click", async () => {
    if (modoFocoAtivo()) {
      desativarModoFoco();
      flash("Modo foco desligado.");
    } else {
      if (!suportaNotificacao()) return alert("Sem suporte a notificações.");
      const p = await pedirPermissao();
      if (p !== "granted") return alert("Ative as notificações primeiro.");
      ativarModoFoco(30, obterProximaAcaoAtual); // a cada 30 min enquanto aberto
      flash("🎯 Modo foco: vou lembrar a cada 30 min enquanto o app estiver aberto.");
    }
    renderAgora();
  });
}

// Mensagem rápida no rodapé
function flash(msg) {
  let el = document.querySelector("#flash");
  if (!el) {
    el = document.createElement("div");
    el.id = "flash";
    document.body.appendChild(el);
  }
  el.textContent = msg;
  el.classList.add("show");
  clearTimeout(el._t);
  el._t = setTimeout(() => el.classList.remove("show"), 3500);
}

// ---------- Tela Hoje ----------
async function renderHoje() {
  const tarefas = await listarTarefas();
  const abertas = tarefas.filter(
    (t) => !["concluida", "cancelada"].includes(t.status)
  );
  const top = ordenarPorPrioridade(abertas).slice(0, 5);
  const list = $("#hoje-list");

  if (top.length === 0) {
    list.innerHTML = `<div class="vazio"><div class="big">☰</div><p>Sem prioridades hoje.</p></div>`;
    return;
  }

  list.innerHTML = top
    .map(
      (t) => `
      <div class="item">
        <div class="item-top">
          <span class="item-titulo">${escapeHtml(t.titulo)}</span>
          <span class="chip prio-${t.prioridade}">${t.prioridade}</span>
        </div>
        <span class="item-sub">
          ${escapeHtml(t.categoria)}${
            t.tempoEstimadoMin ? ` · ${t.tempoEstimadoMin} min` : ""
          }${t.status === "fazendo" ? " · fazendo" : ""}
        </span>
      </div>`
    )
    .join("");
}

// ---------- Caixa de entrada ----------
async function adicionar() {
  const input = $("#caixa-input");
  const texto = input.value.trim();
  if (!texto) return;
  // Cada linha vira uma tarefa simples (a IA revisora melhora depois).
  const linhas = texto
    .split("\n")
    .map((l) => l.trim())
    .filter(Boolean);
  for (const linha of linhas) {
    await criarTarefa({ titulo: linha, categoria: "Geral", prioridade: "Média" });
  }
  input.value = "";
  renderCaixa();
}

$("#btn-add").addEventListener("click", adicionar);

// ---------- IA: configuração da chave ----------
function atualizarConfigIA() {
  const config = $("#ia-config");
  const status = $("#ia-status");
  if (temApiKey()) {
    config.removeAttribute("open");
    config.querySelector("summary").textContent = "🔑 IA conectada ✓ (trocar chave)";
  } else {
    config.setAttribute("open", "");
    config.querySelector("summary").textContent = "🔑 Configurar IA (cole sua chave)";
    status.textContent = "Cole sua chave DeepSeek acima para ativar a revisão por IA.";
  }
  $("#ia-key").value = getApiKey();
}

$("#btn-salvar-key").addEventListener("click", () => {
  setApiKey($("#ia-key").value);
  setStatusIA(temApiKey() ? "Chave salva ✓" : "Chave vazia.", temApiKey() ? "ok" : "erro");
  atualizarConfigIA();
});

function setStatusIA(msg, classe = "") {
  const el = $("#ia-status");
  el.textContent = msg;
  el.className = "hint" + (classe ? " " + classe : "");
}

// ---------- IA: revisar a caixa ----------
async function revisarComIA() {
  const btn = $("#btn-revisar");
  const tarefas = await listarTarefas();
  const naoRevisadas = tarefas.filter(
    (t) => !t.revisadaIA && !["concluida", "cancelada"].includes(t.status)
  );
  if (naoRevisadas.length === 0) {
    setStatusIA("Nada novo pra revisar — adicione tarefas primeiro.", "erro");
    return;
  }
  if (!temApiKey()) {
    setStatusIA("Configure sua chave da IA primeiro.", "erro");
    $("#ia-config").setAttribute("open", "");
    return;
  }

  // Estado: carregando
  btn.disabled = true;
  btn.querySelector(".ia-label").textContent = "Revisando...";
  btn.querySelector(".ia-spinner").hidden = false;
  setStatusIA("A IA está organizando e priorizando suas tarefas...");

  const entrada = naoRevisadas.map((t) => "- " + t.titulo).join("\n");

  try {
    const resultado = await reviewTaskInput(entrada);
    const novas = Array.isArray(resultado.tarefas) ? resultado.tarefas : [];
    if (novas.length === 0) throw new Error("A IA não retornou tarefas.");

    // Substitui as soltas pelas estruturadas
    for (const t of naoRevisadas) await deletarTarefa(t.id);
    for (const nt of novas) await criarTarefa({ ...nt, revisadaIA: true });

    setStatusIA(
      `✓ ${novas.length} tarefa(s) organizadas. Foco: ${
        resultado.proxima_acao_recomendada || "ver Tela Agora"
      }`,
      "ok"
    );
    renderCaixa();
  } catch (e) {
    setStatusIA(e.message, "erro");
  } finally {
    btn.disabled = false;
    btn.querySelector(".ia-label").textContent = "✨ Revisar com IA";
    btn.querySelector(".ia-spinner").hidden = true;
  }
}

$("#btn-revisar").addEventListener("click", revisarComIA);

async function renderCaixa() {
  const tarefas = await listarTarefas();
  const recentes = tarefas
    .filter((t) => !["concluida", "cancelada"].includes(t.status))
    .sort((a, b) => b.criadaEm - a.criadaEm);
  const list = $("#caixa-list");
  list.innerHTML = recentes
    .map(
      (t) => `
      <div class="item">
        <div class="item-top">
          <span class="item-titulo">${escapeHtml(t.titulo)}</span>
          <span class="chip prio-${t.prioridade}">${t.prioridade}</span>
        </div>
        <span class="item-sub">${escapeHtml(t.categoria)}</span>
      </div>`
    )
    .join("");
}

// ---------- util ----------
function escapeHtml(s) {
  return String(s).replace(
    /[&<>"']/g,
    (c) =>
      ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c])
  );
}

// ---------- Service worker (offline) ----------
if ("serviceWorker" in navigator) {
  window.addEventListener("load", () =>
    navigator.serviceWorker.register("./sw.js").catch(() => {})
  );
  // Quando o usuário decide pela notificação, o SW avisa para atualizar a tela
  navigator.serviceWorker.addEventListener("message", (e) => {
    if (e.data?.tipo === "decisao") {
      const ativa = document.querySelector(".view[data-active]")?.id;
      if (ativa === "view-agora") renderAgora();
      else if (ativa === "view-hoje") renderHoje();
    }
  });
}

// Início
renderAgora();
