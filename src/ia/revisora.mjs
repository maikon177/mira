// Mira — IA Revisora (camada de IA sobre o sistema fixo de tarefas).
//
// reviewTaskInput(input) recebe uma entrada bagunçada do usuário e devolve
// tarefas estruturadas em JSON, prontas para o app salvar.
//
// Usa global fetch (disponível no Node 18+ e no navegador), então a mesma
// função serve para o teste em Node e, depois, para o PWA.

import { readFile } from "node:fs/promises";
import { fileURLToPath } from "node:url";
import { dirname, join } from "node:path";

const __dirname = dirname(fileURLToPath(import.meta.url));

/** Carrega o prompt-base da IA revisora a partir do arquivo .md. */
export async function loadSystemPrompt() {
  return readFile(join(__dirname, "prompt_ia_revisora.md"), "utf8");
}

/**
 * Envia uma entrada solta para a DeepSeek e devolve tarefas estruturadas.
 *
 * @param {string} input  Texto bagunçado do usuário (caixa de entrada).
 * @param {object} [opts]
 * @param {string} [opts.apiKey]       Default: process.env.DEEPSEEK_API_KEY
 * @param {string} [opts.baseUrl]      Default: process.env.DEEPSEEK_BASE_URL
 * @param {string} [opts.model]        Default: process.env.DEEPSEEK_MODEL
 * @param {string} [opts.systemPrompt] Prompt já carregado (evita ler o arquivo).
 * @returns {Promise<{parsed: object, raw: string, usage: object, model: string}>}
 */
export async function reviewTaskInput(input, opts = {}) {
  const apiKey = opts.apiKey ?? globalThis.process?.env?.DEEPSEEK_API_KEY;
  const baseUrl =
    opts.baseUrl ??
    globalThis.process?.env?.DEEPSEEK_BASE_URL ??
    "https://api.deepseek.com";
  const model =
    opts.model ?? globalThis.process?.env?.DEEPSEEK_MODEL ?? "deepseek-chat";

  if (!apiKey) {
    throw new Error("DEEPSEEK_API_KEY ausente. Configure o arquivo .env.");
  }
  if (!input || !input.trim()) {
    throw new Error("Entrada vazia: nada para revisar.");
  }

  const systemPrompt = opts.systemPrompt ?? (await loadSystemPrompt());

  const res = await fetch(`${baseUrl.replace(/\/$/, "")}/chat/completions`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${apiKey}`,
    },
    body: JSON.stringify({
      model,
      messages: [
        { role: "system", content: systemPrompt },
        { role: "user", content: input },
      ],
      temperature: 0.2,
      response_format: { type: "json_object" },
    }),
  });

  if (!res.ok) {
    const text = await res.text();
    throw new Error(`DeepSeek HTTP ${res.status}: ${text}`);
  }

  const data = await res.json();
  const content = data?.choices?.[0]?.message?.content ?? "";

  let parsed;
  try {
    parsed = JSON.parse(content);
  } catch {
    throw new Error(`Resposta não é JSON válido:\n${content}`);
  }

  return { parsed, raw: content, usage: data.usage, model: data.model };
}

/**
 * Validação leve do formato esperado. Devolve { ok, erros[] } sem lançar,
 * para o app decidir se confia na resposta da IA.
 */
export function validarRevisao(parsed) {
  const erros = [];
  const prioridadesValidas = new Set(["Alta", "Média", "Baixa"]);

  if (!parsed || typeof parsed !== "object") {
    return { ok: false, erros: ["Resposta não é um objeto."] };
  }
  if (!Array.isArray(parsed.tarefas) || parsed.tarefas.length === 0) {
    erros.push("Campo 'tarefas' ausente ou vazio.");
  } else {
    parsed.tarefas.forEach((t, i) => {
      const campos = [
        "titulo",
        "categoria",
        "prioridade",
        "tempo_estimado_minutos",
        "motivo",
        "proxima_acao",
        "alerta_sugerido",
      ];
      for (const c of campos) {
        if (t[c] === undefined || t[c] === null || t[c] === "") {
          erros.push(`tarefa[${i}]: campo '${c}' ausente.`);
        }
      }
      if (t.prioridade && !prioridadesValidas.has(t.prioridade)) {
        erros.push(
          `tarefa[${i}]: prioridade '${t.prioridade}' inválida (use Alta/Média/Baixa).`
        );
      }
      if (
        t.tempo_estimado_minutos !== undefined &&
        !Number.isInteger(t.tempo_estimado_minutos)
      ) {
        erros.push(`tarefa[${i}]: 'tempo_estimado_minutos' deve ser inteiro.`);
      }
    });
  }
  if (!parsed.proxima_acao_recomendada) {
    erros.push("Campo 'proxima_acao_recomendada' ausente.");
  }

  return { ok: erros.length === 0, erros };
}
