// Teste do passo 2 do TODO: validar a API DeepSeek de ponta a ponta.
//
// Rode com:  npm run test:ia
// (carrega o .env via `node --env-file=.env`)
//
// Envia uma entrada bagunçada real (contexto Pata 3D), recebe JSON,
// e valida se a resposta segue o formato que o app espera.

import { reviewTaskInput, validarRevisao } from "../src/ia/revisora.mjs";

const entradaBaguncada =
  "responder cliente João do orçamento da maquete, comprar filamento preto " +
  "que tá acabando, arrumar o bico entupido da Ender 3, terminar a modelagem " +
  "do chaveiro e mandar nota fiscal pro cliente Pedro";

console.log("📥 Entrada bagunçada:\n", entradaBaguncada, "\n");
console.log("⏳ Chamando DeepSeek (modelo:", process.env.DEEPSEEK_MODEL, ")...\n");

try {
  const t0 = Date.now();
  const { parsed, usage, model } = await reviewTaskInput(entradaBaguncada);
  const ms = Date.now() - t0;

  console.log("✅ Resposta recebida em", ms, "ms — modelo:", model);
  if (usage) console.log("🔢 Tokens:", JSON.stringify(usage), "\n");

  const { ok, erros } = validarRevisao(parsed);

  console.log("📋 Tarefas estruturadas:");
  parsed.tarefas?.forEach((t, i) => {
    console.log(
      `  ${i + 1}. [${t.prioridade}] ${t.titulo} ` +
        `(${t.tempo_estimado_minutos}min) — ${t.categoria}`
    );
    console.log(`     → próxima ação: ${t.proxima_acao}`);
  });

  console.log("\n🎯 Próxima ação recomendada:", parsed.proxima_acao_recomendada);
  console.log("💡 Motivo:", parsed.motivo_da_prioridade);
  if (parsed.observacao) console.log("📝 Observação:", parsed.observacao);

  console.log("\n──────────────────────────────────────");
  if (ok) {
    console.log("✅ JSON CONFIÁVEL: passou em todas as validações de formato.");
  } else {
    console.log("⚠️  JSON com problemas de formato:");
    erros.forEach((e) => console.log("   -", e));
  }
  console.log("──────────────────────────────────────");

  console.log("\n📄 JSON bruto:\n", JSON.stringify(parsed, null, 2));
} catch (err) {
  console.error("❌ Falha no teste:", err.message);
  process.exit(1);
}
