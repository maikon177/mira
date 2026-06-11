// Mira — IA Revisora (versão NAVEGADOR).
// Chama a DeepSeek direto do browser (CORS liberado), sem servidor.
// A chave fica no localStorage do aparelho — nunca no código/repo.

const LS_KEY = "mira_deepseek_key";
const LS_MODEL = "mira_deepseek_model";
const BASE_URL = "https://api.deepseek.com";
const MODEL_PADRAO = "deepseek-v4-flash";

export function getApiKey() {
  return localStorage.getItem(LS_KEY) || "";
}
export function setApiKey(k) {
  localStorage.setItem(LS_KEY, (k || "").trim());
}
export function temApiKey() {
  return !!getApiKey();
}
export function getModelo() {
  return localStorage.getItem(LS_MODEL) || MODEL_PADRAO;
}
export function setModelo(m) {
  localStorage.setItem(LS_MODEL, (m || MODEL_PADRAO).trim());
}

let _promptCache = null;
async function carregarPrompt() {
  if (_promptCache) return _promptCache;
  const res = await fetch("./src/ia/prompt_ia_revisora.md");
  _promptCache = await res.text();
  return _promptCache;
}

/**
 * Envia uma entrada bagunçada e devolve { tarefas[], proxima_acao_recomendada, ... }.
 * Lança erro com mensagem amigável se faltar chave ou a API falhar.
 */
export async function reviewTaskInput(input) {
  const apiKey = getApiKey();
  if (!apiKey) throw new Error("Sem chave da IA. Cole sua chave nas configurações.");
  if (!input || !input.trim()) throw new Error("Nada para revisar.");

  const systemPrompt = await carregarPrompt();

  const res = await fetch(`${BASE_URL}/chat/completions`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${apiKey}`,
    },
    body: JSON.stringify({
      model: getModelo(),
      messages: [
        { role: "system", content: systemPrompt },
        { role: "user", content: input },
      ],
      temperature: 0.2,
      response_format: { type: "json_object" },
    }),
  });

  if (!res.ok) {
    const txt = await res.text();
    if (res.status === 401)
      throw new Error("Chave da IA inválida. Confira nas configurações.");
    throw new Error(`Erro da IA (${res.status}): ${txt.slice(0, 160)}`);
  }

  const data = await res.json();
  const content = data?.choices?.[0]?.message?.content ?? "";
  try {
    return JSON.parse(content);
  } catch {
    throw new Error("A IA respondeu em formato inesperado. Tente de novo.");
  }
}
