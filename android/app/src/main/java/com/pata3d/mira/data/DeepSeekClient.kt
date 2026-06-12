package com.pata3d.mira.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private const val BASE_URL = "https://api.deepseek.com/chat/completions"
private const val MODEL = "deepseek-chat"

private val SYSTEM_PROMPT = """
Voce e a IA revisora de um assistente operacional pessoal. Sua funcao e transformar entradas soltas em tarefas claras, priorizar com base em prazo, cliente, dinheiro, risco, esforco e dependencia, sugerir a proxima melhor acao e evitar listas longas.

O usuario trabalha com impressao 3D, atendimento, producao, modelagem, acabamento, manutencao de impressoras e criacao de produtos.

Priorize: tarefas de cliente, prazos proximos, producao bloqueada, orcamentos com chance de venda, acoes rapidas que destravam o dia, tarefas adiadas varias vezes.

Sempre que uma tarefa for vaga, reescreva de forma objetiva. Sempre que parecer maior que 60 min, divida.

Responda SOMENTE com JSON valido:
{
  "tarefas": [
    {
      "titulo": "string",
      "categoria": "string",
      "prioridade": "Alta|Media|Baixa",
      "tempo_estimado_minutos": 90,
      "motivo": "string",
      "proxima_acao": "string",
      "alerta_sugerido": "string"
    }
  ],
  "proxima_acao_recomendada": "string",
  "motivo_da_prioridade": "string",
  "observacao": "string"
}
""".trimIndent()

private val CHAT_SYSTEM_PROMPT = """
Voce e a Mira, parceira de planejamento de um usuario que trabalha com impressao 3D.

Tom: leve, direto, frases curtas, sem culpa. Uma ideia por vez.

Quando uma tarefa tiver varias partes dependentes, voce pode propor uma tarefa com etapas em ordem. Exemplo: ajustar -> imprimir -> pintar.

Datas podem vir em linguagem natural. Use "quando" como texto curto, por exemplo: "hoje", "amanha", "dia 16", "segunda".
Se houver horario, inclua junto em "quando", por exemplo: "hoje 18:00", "amanha 07:30", "dia 16 14:00".

Quando o usuario pedir para lembrar em certo horario, crie uma acao. Nao responda so em texto.

Responda SOMENTE com JSON valido:
{
  "resposta": "fala curta para o usuario",
  "acoes": [
    {
      "tipo": "criar_tarefa | editar_tarefa | agendar_tarefa | concluir_tarefa",
      "tarefa_id": "id curto da tarefa existente",
      "titulo": "string",
      "categoria": "cliente|orcamento|producao|impressao 3D|modelagem|manutencao|material|entrega|ideias",
      "prioridade": "Alta|Media|Baixa",
      "tempo_estimado_minutos": 120,
      "proxima_acao": "micro-passo concreto",
      "motivo": "por que importa",
      "quando": "hoje | amanha | dia 16 | hoje 18:00 | null",
      "etapas": [
        { "titulo": "Ajustar modelo", "tempo_estimado_minutos": 90 },
        { "titulo": "Imprimir peca", "tempo_estimado_minutos": 240 }
      ]
    }
  ]
}
Se nao houver acao, use "acoes": [].
""".trimIndent()

class DeepSeekClient(private val prefs: MiraPrefs) {

    private val http = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun revisar(entrada: String): RevisaoResult {
        val key = prefs.getDeepSeekKey()
        if (key.isBlank()) throw IllegalStateException("Chave DeepSeek nao configurada.")

        val body = JSONObject().apply {
            put("model", MODEL)
            put("messages", JSONArray().apply {
                put(JSONObject().apply { put("role", "system"); put("content", SYSTEM_PROMPT) })
                put(JSONObject().apply { put("role", "user"); put("content", entrada) })
            })
            put("temperature", 0.3)
            put("response_format", JSONObject().put("type", "json_object"))
        }.toString()

        val rawBody = executar(body, key)
        return parseResposta(rawBody)
    }

    suspend fun conversar(
        historico: List<ChatMessage>,
        snapshot: SnapshotEstado,
        novaMensagem: String,
    ): ChatResposta {
        val key = prefs.getDeepSeekKey()
        if (key.isBlank()) throw IllegalStateException("Chave DeepSeek nao configurada.")

        val messages = JSONArray().apply {
            put(JSONObject().apply { put("role", "system"); put("content", CHAT_SYSTEM_PROMPT) })
            put(JSONObject().apply { put("role", "system"); put("content", montarSnapshot(snapshot)) })
            historico.forEach { m ->
                put(JSONObject().apply {
                    put("role", if (m.role == "assistant") "assistant" else "user")
                    put("content", m.content)
                })
            }
            put(JSONObject().apply { put("role", "user"); put("content", novaMensagem) })
        }

        val body = JSONObject().apply {
            put("model", MODEL)
            put("messages", messages)
            put("temperature", 0.4)
            put("response_format", JSONObject().put("type", "json_object"))
        }.toString()

        val rawBody = executar(body, key)
        return parseChat(rawBody)
    }

    private suspend fun executar(body: String, key: String): String {
        val request = Request.Builder()
            .url(BASE_URL)
            .addHeader("Authorization", "Bearer $key")
            .addHeader("Content-Type", "application/json")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        val response = withContext(Dispatchers.IO) { http.newCall(request).execute() }
        val rawBody = response.body?.string() ?: throw Exception("Resposta vazia da API.")
        if (!response.isSuccessful) {
            val errMsg = runCatching { JSONObject(rawBody).optString("error", rawBody) }.getOrDefault(rawBody)
            throw Exception("Erro ${response.code}: $errMsg")
        }
        return rawBody
    }

    private fun montarSnapshot(s: SnapshotEstado): String {
        val sb = StringBuilder("ESTADO ATUAL DO USUARIO\n")
        sb.append("Contexto: ${s.contextoAtual}\n\n")
        sb.append("Tarefas abertas (id | titulo | prioridade | tempo | adiamentos):\n")
        if (s.tarefasAbertas.isEmpty()) sb.append("(nenhuma)\n")
        s.tarefasAbertas.take(30).forEach { t ->
            val curto = t.id.take(8)
            sb.append("- $curto | ${t.titulo} | ${t.prioridade} | ${t.tempoEstimadoMin ?: "?"}min | adiada ${t.adiamentos}x\n")
        }
        if (s.memorias.isNotEmpty()) {
            sb.append("\nMemorias sobre o usuario:\n")
            s.memorias.take(10).forEach { sb.append("- ${it.content}\n") }
        }
        return sb.toString()
    }

    private fun parseChat(raw: String): ChatResposta {
        val content = JSONObject(raw)
            .getJSONArray("choices").getJSONObject(0)
            .getJSONObject("message").getString("content")

        val json = safeJson(content) ?: return ChatResposta(
            resposta = "Recebi sua ideia, mas a resposta da IA veio quebrada. Tenta mandar de novo que eu reorganizo.",
            acoes = emptyList()
        )

        val acoes = json.optJSONArray("acoes")?.let { arr ->
            (0 until arr.length()).map { i ->
                val a = arr.getJSONObject(i)
                AcaoIA(
                    tipo = a.optString("tipo"),
                    tarefaId = a.optStringOrNull("tarefa_id"),
                    titulo = a.optStringOrNull("titulo"),
                    categoria = a.optStringOrNull("categoria"),
                    prioridade = a.optStringOrNull("prioridade"),
                    tempoEstimadoMin = if (a.has("tempo_estimado_minutos") && !a.isNull("tempo_estimado_minutos")) a.optInt("tempo_estimado_minutos") else null,
                    proximaAcao = a.optStringOrNull("proxima_acao"),
                    motivo = a.optStringOrNull("motivo"),
                    quando = a.optStringOrNull("quando"),
                    etapas = a.optJSONArray("etapas")?.toNovaEtapaList(),
                )
            }
        } ?: emptyList()

        return ChatResposta(
            resposta = json.optString("resposta").ifBlank { "Entendi. Vamos por partes." },
            acoes = acoes
        )
    }

    private fun JSONArray.toNovaEtapaList(): List<NovaEtapa> =
        (0 until length()).mapNotNull { i ->
            val etapa = optJSONObject(i) ?: return@mapNotNull null
            val titulo = etapa.optString("titulo").trim()
            if (titulo.isBlank()) return@mapNotNull null
            NovaEtapa(
                titulo = titulo,
                tempoEstimadoMin = if (etapa.has("tempo_estimado_minutos") && !etapa.isNull("tempo_estimado_minutos")) etapa.optInt("tempo_estimado_minutos") else null,
            )
        }

    private fun JSONObject.optStringOrNull(key: String): String? =
        if (has(key) && !isNull(key)) optString(key).takeIf { it.isNotBlank() } else null

    private fun parseResposta(raw: String): RevisaoResult {
        val root = JSONObject(raw)
        val content = root
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")

        val json = safeJson(content) ?: throw Exception("A IA devolveu uma resposta invalida na organizacao.")
        val tarefasJson = json.getJSONArray("tarefas")
        val tarefas = (0 until tarefasJson.length()).map { i ->
            val t = tarefasJson.getJSONObject(i)
            TarefaRevisada(
                titulo = t.optString("titulo"),
                categoria = t.optString("categoria", "Geral"),
                prioridade = t.optString("prioridade", "Media"),
                tempoEstimadoMinutos = if (t.isNull("tempo_estimado_minutos")) null else t.optInt("tempo_estimado_minutos"),
                motivo = t.optString("motivo"),
                proximaAcao = t.optString("proxima_acao"),
                alertaSugerido = t.optString("alerta_sugerido"),
            )
        }

        return RevisaoResult(
            tarefas = tarefas,
            proximaAcaoRecomendada = json.optString("proxima_acao_recomendada"),
            motivoDaPrioridade = json.optString("motivo_da_prioridade"),
            observacao = json.optString("observacao"),
        )
    }

    private fun safeJson(content: String): JSONObject? {
        return runCatching { JSONObject(content) }.getOrElse {
            val ini = content.indexOf('{')
            val fim = content.lastIndexOf('}')
            if (ini >= 0 && fim > ini) {
                runCatching { JSONObject(content.substring(ini, fim + 1)) }.getOrNull()
            } else null
        }
    }
}
