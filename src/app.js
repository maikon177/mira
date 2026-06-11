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
} from "./db.js";
import { proximaAcao, ordenarPorPrioridade } from "./prioridade.js";

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
  if (tela === "caixa") renderCaixa();
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
}

// Início
renderAgora();
