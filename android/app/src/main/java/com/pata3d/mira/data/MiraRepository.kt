package com.pata3d.mira.data

import com.pata3d.mira.data.NivelRisco
import com.pata3d.mira.domain.ContextoAtual
import com.pata3d.mira.domain.Disponibilidade
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.Locale
import java.util.UUID

data class ResumoEtapas(
    val etapas: List<EtapaTarefa>,
    val etapaAtual: EtapaTarefa?,
    val concluidas: Int,
    val total: Int,
)

class MiraRepository(
    db: MiraDatabase,
    private val prefs: MiraPrefs,
    private val deepSeekClient: DeepSeekClient,
) {
    private val tarefaDao = db.tarefaDao()
    private val etapaDao = db.etapaDao()
    private val historicoDao = db.historicoDao()
    private val memoriaDao = db.memoriaDao()
    private val chatDao = db.chatDao()
    private val dispDao = db.disponibilidadeDao()

    val prefsRef: MiraPrefs get() = prefs

    fun observarTarefas(): Flow<List<Tarefa>> = tarefaDao.observarTodas()
    fun observarEtapas(): Flow<List<EtapaTarefa>> = etapaDao.observarTodas()

    suspend fun listarTarefas(): List<Tarefa> = tarefaDao.listarTodas()
    suspend fun listarAbertas(): List<Tarefa> = tarefaDao.listarAbertas()

    fun observarTarefasComPrazo() = tarefaDao.observarComPrazo()
    fun observarTarefasMaquina() = tarefaDao.observarMaquina()

    suspend fun atualizarNivelRisco(id: String, nivel: NivelRisco) {
        tarefaDao.atualizarRisco(id, nivel.name, System.currentTimeMillis())
    }

    suspend fun marcarTarefaIniciada(id: String) {
        tarefaDao.marcarIniciada(id, System.currentTimeMillis())
    }

    suspend fun listarCompromissosFuturos(): List<Tarefa> =
        tarefaDao.listarCompromissosFuturos(System.currentTimeMillis())

    suspend fun buscarTarefa(id: String): Tarefa? = tarefaDao.buscarPorId(id)
    suspend fun observarEtapasSnapshot(): List<EtapaTarefa> = buildList {
        tarefaDao.listarTodas().forEach { addAll(etapaDao.listarDaTarefa(it.id)) }
    }
    suspend fun obterTarefa(id: String): Tarefa? = tarefaDao.obter(id)
    suspend fun listarEtapasDaTarefa(tarefaId: String): List<EtapaTarefa> = etapaDao.listarDaTarefa(tarefaId)

    suspend fun criarTarefa(
        titulo: String,
        categoria: String = "Geral",
        prioridade: String = "Media",
        tempoEstimadoMin: Int? = null,
        motivo: String = "",
        proximaAcao: String = "",
        revisadaIA: Boolean = false,
        dataAgendada: Long? = null,
        contexto: String? = null,
        etapas: List<NovaEtapa> = emptyList(),
        tipoTarefa: String = TipoTarefa.SIMPLES.name,
        prazoEm: Long? = null,
        compromissoEm: Long? = null,
        lembreteEm: Long? = null,
        microPasso: String = "",
        protegerPrazo: Boolean = false,
        isTarefaMaquina: Boolean = false,
        tempoPrepMin: Int? = null,
        tempoMaquinaMin: Int? = null,
        tempoSecagemMin: Int? = null,
        tempoFinalMin: Int? = null,
        tipoAlerta: String = TipoAlerta.NENHUM.name,
    ): Tarefa {
        val tarefa = Tarefa(
            id = UUID.randomUUID().toString(),
            titulo = titulo,
            categoria = categoria,
            prioridade = prioridade,
            tempoEstimadoMin = tempoEstimadoMin,
            motivo = motivo,
            proximaAcao = proximaAcao,
            revisadaIA = revisadaIA,
            dataAgendada = dataAgendada,
            contexto = contexto,
            tipoTarefa = tipoTarefa,
            prazoEm = prazoEm,
            compromissoEm = compromissoEm,
            lembreteEm = lembreteEm,
            microPasso = microPasso,
            protegerPrazo = protegerPrazo,
            isTarefaMaquina = isTarefaMaquina,
            tempoPrepMin = tempoPrepMin,
            tempoMaquinaMin = tempoMaquinaMin,
            tempoSecagemMin = tempoSecagemMin,
            tempoFinalMin = tempoFinalMin,
            tipoAlerta = tipoAlerta,
        )
        tarefaDao.inserir(tarefa)
        registrarHistorico("tarefa_criada", tarefaId = tarefa.id)
        if (etapas.isNotEmpty()) {
            salvarEtapas(tarefa.id, etapas)
        }
        return tarefa
    }

    suspend fun atualizarTarefa(t: Tarefa) {
        tarefaDao.atualizar(t.copy(atualizadaEm = System.currentTimeMillis()))
    }

    suspend fun concluirTarefa(id: String) {
        val t = tarefaDao.obter(id) ?: return
        val pausada = pausarCronometroTarefaInterno(t)
        tarefaDao.atualizar(pausada.copy(status = "concluida", atualizadaEm = System.currentTimeMillis()))
        registrarHistorico("tarefa_concluida", tarefaId = id)
    }

    suspend fun iniciarTarefa(id: String) {
        val t = tarefaDao.obter(id) ?: return
        tarefaDao.atualizar(t.copy(status = "fazendo", atualizadaEm = System.currentTimeMillis()))
        registrarHistorico("tarefa_iniciada", tarefaId = id)
    }

    suspend fun adiarTarefa(id: String, minutos: Int) {
        val t = tarefaDao.obter(id) ?: return
        val pausada = pausarCronometroTarefaInterno(t)
        tarefaDao.atualizar(
            pausada.copy(
                status = "adiada",
                adiarAte = System.currentTimeMillis() + minutos * 60_000L,
                adiamentos = t.adiamentos + 1,
                atualizadaEm = System.currentTimeMillis(),
            )
        )
        registrarHistorico("tarefa_adiada", tarefaId = id, detalhes = "${minutos}min")
    }

    suspend fun cancelarTarefa(id: String) {
        val t = tarefaDao.obter(id) ?: return
        val pausada = pausarCronometroTarefaInterno(t)
        tarefaDao.atualizar(pausada.copy(status = "cancelada", atualizadaEm = System.currentTimeMillis()))
        registrarHistorico("tarefa_cancelada", tarefaId = id)
    }

    suspend fun agendarTarefa(id: String, dataMillis: Long?) {
        val t = tarefaDao.obter(id) ?: return
        tarefaDao.atualizar(
            t.copy(
                dataAgendada = dataMillis,
                status = if (t.status == "adiada") "aberta" else t.status,
                atualizadaEm = System.currentTimeMillis(),
            )
        )
        registrarHistorico("tarefa_agendada", tarefaId = id, detalhes = dataMillis?.toString())
    }

    suspend fun iniciarCronometroTarefa(id: String) {
        val tarefa = tarefaDao.obter(id) ?: return
        if (listarEtapasDaTarefa(id).isNotEmpty()) return
        if (tarefa.cronometroIniciadoEm != null) return
        tarefaDao.atualizar(
            tarefa.copy(
                status = "fazendo",
                cronometroIniciadoEm = System.currentTimeMillis(),
                atualizadaEm = System.currentTimeMillis(),
            )
        )
        registrarHistorico("cronometro_tarefa_iniciado", tarefaId = id)
    }

    suspend fun pausarCronometroTarefa(id: String) {
        val tarefa = tarefaDao.obter(id) ?: return
        val pausada = pausarCronometroTarefaInterno(tarefa)
        tarefaDao.atualizar(pausada)
        registrarHistorico("cronometro_tarefa_pausado", tarefaId = id, detalhes = pausada.tempoRealAcumuladoSeg.toString())
    }

    suspend fun salvarEtapas(tarefaId: String, etapas: List<NovaEtapa>) {
        etapaDao.deletarDaTarefa(tarefaId)
        etapas.mapIndexed { index, etapa ->
            EtapaTarefa(
                id = UUID.randomUUID().toString(),
                tarefaId = tarefaId,
                titulo = etapa.titulo,
                ordem = index,
                tempoEstimadoMin = etapa.tempoEstimadoMin,
            )
        }.forEach { etapaDao.inserir(it) }
        sincronizarProximaAcaoComEtapa(tarefaId)
        registrarHistorico("etapas_salvas", tarefaId = tarefaId, detalhes = etapas.size.toString())
    }

    suspend fun obterResumoEtapas(tarefaId: String): ResumoEtapas {
        val etapas = etapaDao.listarDaTarefa(tarefaId)
        val etapaAtual = etapas.firstOrNull { it.status != "concluida" }
        return ResumoEtapas(
            etapas = etapas,
            etapaAtual = etapaAtual,
            concluidas = etapas.count { it.status == "concluida" },
            total = etapas.size,
        )
    }

    suspend fun iniciarCronometroEtapa(etapaId: String) {
        val etapa = etapaDao.obter(etapaId) ?: return
        val atual = etapaAtualDisponivel(etapa.tarefaId) ?: return
        if (atual.id != etapaId || etapa.cronometroIniciadoEm != null) return
        etapaDao.atualizar(
            etapa.copy(
                status = "fazendo",
                cronometroIniciadoEm = System.currentTimeMillis(),
                atualizadaEm = System.currentTimeMillis(),
            )
        )
        val tarefa = tarefaDao.obter(etapa.tarefaId)
        if (tarefa != null && tarefa.status != "fazendo") {
            tarefaDao.atualizar(tarefa.copy(status = "fazendo", atualizadaEm = System.currentTimeMillis()))
        }
        registrarHistorico("cronometro_etapa_iniciado", tarefaId = etapa.tarefaId, etapaId = etapaId)
    }

    suspend fun pausarCronometroEtapa(etapaId: String) {
        val etapa = etapaDao.obter(etapaId) ?: return
        val pausada = pausarCronometroEtapaInterno(etapa)
        etapaDao.atualizar(pausada)
        registrarHistorico("cronometro_etapa_pausado", tarefaId = etapa.tarefaId, etapaId = etapaId, detalhes = pausada.tempoRealAcumuladoSeg.toString())
    }

    suspend fun concluirEtapa(etapaId: String) {
        val etapa = etapaDao.obter(etapaId) ?: return
        val atual = etapaAtualDisponivel(etapa.tarefaId) ?: return
        if (atual.id != etapaId) return
        val pausada = pausarCronometroEtapaInterno(etapa)
        etapaDao.atualizar(
            pausada.copy(
                status = "concluida",
                cronometroIniciadoEm = null,
                atualizadaEm = System.currentTimeMillis(),
            )
        )
        registrarHistorico("etapa_concluida", tarefaId = etapa.tarefaId, etapaId = etapaId, detalhes = pausada.tempoRealAcumuladoSeg.toString())
        sincronizarProximaAcaoComEtapa(etapa.tarefaId)
        val restante = etapaAtualDisponivel(etapa.tarefaId)
        if (restante == null) {
            concluirTarefa(etapa.tarefaId)
        }
    }

    suspend fun listarHistorico(desde: Long): List<Historico> = historicoDao.desde(desde)

    suspend fun listarMemorias(apenasAtivas: Boolean = false): List<Memoria> =
        if (apenasAtivas) memoriaDao.listarAtivas() else memoriaDao.listarTodas()

    suspend fun revisarEntrada(texto: String): RevisaoResult = deepSeekClient.revisar(texto)

    suspend fun salvarTarefasRevisadas(tarefas: List<TarefaRevisada>) {
        tarefas.forEach { t ->
            criarTarefa(
                titulo = t.titulo,
                categoria = t.categoria,
                prioridade = t.prioridade,
                tempoEstimadoMin = t.tempoEstimadoMinutos,
                motivo = t.motivo,
                proximaAcao = t.proximaAcao,
                revisadaIA = true,
            )
        }
    }

    fun observarChat(): Flow<List<ChatMessage>> = chatDao.observarTodas()

    suspend fun enviarMensagemChat(texto: String): ChatResposta {
        chatDao.inserir(ChatMessage(role = "user", content = texto))
        val historico = chatDao.ultimas(12).reversed().dropLast(1)
        val snapshot = SnapshotEstado(
            tarefasAbertas = tarefaDao.listarAbertas(),
            memorias = memoriaDao.listarAtivas(),
            contextoAtual = descreverContexto(contextoAgora()),
        )
        val respostaIA = deepSeekClient.conversar(historico, snapshot, texto)
        val resposta = enriquecerRespostaChat(texto, respostaIA)
        chatDao.inserir(
            ChatMessage(
                role = "assistant",
                content = resposta.resposta,
                acoesJson = if (resposta.acoes.isEmpty()) null else "tem",
            )
        )
        // Manter no máximo 40 mensagens; apaga as mais antigas silenciosamente
        if (chatDao.contar() > 40) chatDao.aparar(40)
        return resposta
    }

    suspend fun aplicarAcaoIA(a: AcaoIA): String {
        return when (a.tipo) {
            "criar_tarefa" -> {
                criarTarefa(
                    titulo = a.titulo ?: "Tarefa",
                    categoria = a.categoria ?: "Geral",
                    prioridade = a.prioridade ?: "Media",
                    tempoEstimadoMin = a.tempoEstimadoMin,
                    motivo = a.motivo ?: "",
                    proximaAcao = a.proximaAcao ?: "",
                    revisadaIA = true,
                    dataAgendada = quandoParaData(a.quando),
                    etapas = a.etapas ?: emptyList(),
                )
                "Tarefa criada."
            }
            "agendar_tarefa" -> {
                val alvo = acharTarefaPorIdCurto(a.tarefaId)
                if (alvo != null) {
                    agendarTarefa(alvo.id, quandoParaData(a.quando))
                    "Tarefa agendada."
                } else "Tarefa nao encontrada."
            }
            "concluir_tarefa" -> {
                val alvo = acharTarefaPorIdCurto(a.tarefaId)
                if (alvo != null) {
                    concluirTarefa(alvo.id)
                    "Tarefa concluida."
                } else "Tarefa nao encontrada."
            }
            "editar_tarefa" -> {
                val alvo = acharTarefaPorIdCurto(a.tarefaId)
                if (alvo != null) {
                    atualizarTarefa(
                        alvo.copy(
                            titulo = a.titulo ?: alvo.titulo,
                            prioridade = a.prioridade ?: alvo.prioridade,
                            tempoEstimadoMin = a.tempoEstimadoMin ?: alvo.tempoEstimadoMin,
                            proximaAcao = a.proximaAcao ?: alvo.proximaAcao,
                        )
                    )
                    if (!a.etapas.isNullOrEmpty()) {
                        salvarEtapas(alvo.id, a.etapas)
                    }
                    "Tarefa atualizada."
                } else "Tarefa nao encontrada."
            }
            else -> "Acao desconhecida."
        }
    }

    suspend fun limparChat() = chatDao.limparTudo()

    private suspend fun acharTarefaPorIdCurto(idCurto: String?): Tarefa? {
        if (idCurto.isNullOrBlank()) return null
        return tarefaDao.listarAbertas().firstOrNull { it.id.startsWith(idCurto) || it.id.take(8) == idCurto }
    }

    private fun quandoParaData(quando: String?): Long? {
        if (quando.isNullOrBlank()) return null
        val texto = normalizar(quando)
        val horario = extrairHorario(texto)

        val base = when {
            "amanha" in texto -> inicioDoDia(1)
            "hoje" in texto -> inicioDoDia(0)
            else -> extrairDiaDoMes(texto)
        }

        if (base != null) return aplicarHorario(base, horario)
        if (horario != null) return proximoHorario(horario.first, horario.second)
        return null
    }

    private fun inicioDoDia(offsetDias: Int): Long = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, offsetDias)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    fun observarBlocos(): Flow<List<BlocoDisponibilidade>> = dispDao.observarTodos()
    suspend fun listarBlocos(): List<BlocoDisponibilidade> = dispDao.listarTodos()

    suspend fun salvarBloco(b: BlocoDisponibilidade) {
        if (b.id == 0L) dispDao.inserir(b) else dispDao.atualizar(b)
    }

    suspend fun deletarBloco(id: Long) = dispDao.deletar(id)

    suspend fun contextoAgora(): ContextoAtual = Disponibilidade.agora(dispDao.listarTodos())

    suspend fun semearCronogramaSeNecessario() {
        if (prefs.cronogramaSemeado || dispDao.contar() > 0) return
        Disponibilidade.cronogramaPadrao().forEach { dispDao.inserir(it) }
        prefs.cronogramaSemeado = true
    }

    private fun descreverContexto(c: ContextoAtual): String {
        val h = "%02d:%02d".format(c.horaMin / 60, c.horaMin % 60)
        return "${c.rotuloAmigavel}, $h, disponibilidade ${c.nivel}, local ${c.contexto}"
    }

    fun getDeepSeekKey(): String = prefs.getDeepSeekKey()
    fun setDeepSeekKey(key: String) = prefs.setDeepSeekKey(key)
    fun hasDeepSeekKey(): Boolean = prefs.hasKey()

    private suspend fun registrarHistorico(
        tipo: String,
        tarefaId: String? = null,
        etapaId: String? = null,
        detalhes: String? = null,
    ) {
        historicoDao.inserir(Historico(tipo = tipo, tarefaId = tarefaId, etapaId = etapaId, detalhes = detalhes))
    }

    private fun pausarCronometroTarefaInterno(tarefa: Tarefa): Tarefa {
        val inicio = tarefa.cronometroIniciadoEm ?: return tarefa
        val segundos = ((System.currentTimeMillis() - inicio) / 1000L).coerceAtLeast(0)
        return tarefa.copy(
            cronometroIniciadoEm = null,
            tempoRealAcumuladoSeg = tarefa.tempoRealAcumuladoSeg + segundos,
            atualizadaEm = System.currentTimeMillis(),
        )
    }

    private fun pausarCronometroEtapaInterno(etapa: EtapaTarefa): EtapaTarefa {
        val inicio = etapa.cronometroIniciadoEm ?: return etapa
        val segundos = ((System.currentTimeMillis() - inicio) / 1000L).coerceAtLeast(0)
        return etapa.copy(
            cronometroIniciadoEm = null,
            tempoRealAcumuladoSeg = etapa.tempoRealAcumuladoSeg + segundos,
            atualizadaEm = System.currentTimeMillis(),
        )
    }

    private suspend fun etapaAtualDisponivel(tarefaId: String): EtapaTarefa? =
        etapaDao.listarDaTarefa(tarefaId).firstOrNull { it.status != "concluida" }

    private suspend fun sincronizarProximaAcaoComEtapa(tarefaId: String) {
        val tarefa = tarefaDao.obter(tarefaId) ?: return
        val atual = etapaAtualDisponivel(tarefaId)
        tarefaDao.atualizar(
            tarefa.copy(
                proximaAcao = atual?.titulo ?: tarefa.proximaAcao,
                atualizadaEm = System.currentTimeMillis(),
            )
        )
    }

    private fun extrairDiaDoMes(quando: String?): Long? {
        if (quando.isNullOrBlank()) return null
        val match = Regex("""(\d{1,2})""").find(quando) ?: return null
        val dia = match.groupValues[1].toIntOrNull() ?: return null
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (dia < get(Calendar.DAY_OF_MONTH)) add(Calendar.MONTH, 1)
            set(Calendar.DAY_OF_MONTH, dia.coerceIn(1, getActualMaximum(Calendar.DAY_OF_MONTH)))
        }
        return cal.timeInMillis
    }

    private fun enriquecerRespostaChat(texto: String, resposta: ChatResposta): ChatResposta {
        if (resposta.acoes.isNotEmpty()) return resposta
        val acaoLocal = inferirLembrete(texto) ?: return resposta
        return ChatResposta(
            resposta = montarRespostaLembrete(acaoLocal),
            acoes = listOf(acaoLocal),
        )
    }

    private fun inferirLembrete(textoOriginal: String): AcaoIA? {
        val texto = normalizar(textoOriginal)
        val pediuLembrete = listOf("lembr", "avisa", "avisar", "avise").any { texto.contains(it) }
        if (!pediuLembrete) return null

        val horarios = Regex("""\b(\d{1,2})[:h](\d{2})\b""").findAll(texto).toList()
        val horarioLembrete = horarios.lastOrNull()?.let { match ->
            "%02d:%02d".format(
                match.groupValues[1].toIntOrNull() ?: return null,
                match.groupValues[2].toIntOrNull() ?: return null,
            )
        }

        val referenciaData = when {
            "amanha" in texto -> "amanha"
            "hoje" in texto -> "hoje"
            Regex("""\bdia\s+\d{1,2}\b""").containsMatchIn(texto) ->
                Regex("""\bdia\s+\d{1,2}\b""").find(texto)?.value
            else -> null
        }

        val quando = listOfNotNull(referenciaData, horarioLembrete).joinToString(" ").ifBlank { null }
        val titulo = inferirTituloLembrete(textoOriginal)
        if (titulo.isBlank()) return null

        val horarioEvento = if (horarios.size >= 2) {
            horarios.first().let { "${it.groupValues[1]}:${it.groupValues[2]}" }
        } else null

        val proximaAcao = buildString {
            append("Se preparar para ")
            append(titulo.lowercase(Locale("pt", "BR")))
            if (!horarioEvento.isNullOrBlank()) append(" as $horarioEvento")
        }

        return AcaoIA(
            tipo = "criar_tarefa",
            titulo = titulo,
            categoria = "pessoal",
            prioridade = if (referenciaData == "hoje") "Alta" else "Media",
            proximaAcao = proximaAcao,
            motivo = "Compromisso que voce pediu para nao esquecer.",
            quando = quando,
        )
    }

    private fun inferirTituloLembrete(textoOriginal: String): String {
        val textoSemLembrete = textoOriginal
            .replace(Regex("(?i)\\b(poderia|pode|consegue)?\\s*me\\s+lembrar.*$"), "")
            .replace(Regex("(?i)\\bme\\s+avisa(r|r\\s+de)?\\b.*$"), "")
            .replace(Regex("(?i)\\bme\\s+avise\\b.*$"), "")
            .trim()

        val semInicio = textoSemLembrete
            .replace(Regex("(?i)^\\s*(tenho que|tenho de|preciso|preciso de|vou|quero)\\s+"), "")

        return semInicio
            .replace(Regex("(?i)\\b(a|as|às)\\s*\\d{1,2}([:h]\\d{2})?\\b"), "")
            .replace(Regex("(?i)\\bde\\s+hoje\\b|\\bhoje\\b|\\bde\\s+amanh[ãa]\\b|\\bamanh[ãa]\\b|\\bdia\\s+\\d{1,2}\\b"), "")
            .replace(Regex("\\s+"), " ")
            .trim(' ', '.', ',', ';', ':')
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pt", "BR")) else it.toString() }
    }

    private fun montarRespostaLembrete(acao: AcaoIA): String {
        val horario = acao.quando
            ?.let { extrairHorario(normalizar(it)) }
            ?.let { "%02d:%02d".format(it.first, it.second) }
        return if (horario != null) {
            "Deixei isso montado como tarefa e lembrete para $horario. Confirma abaixo."
        } else {
            "Deixei isso montado como tarefa para voce confirmar abaixo."
        }
    }

    private fun extrairHorario(texto: String): Pair<Int, Int>? {
        val match = Regex("""\b(\d{1,2})[:h](\d{2})\b""").find(texto) ?: return null
        val hora = match.groupValues[1].toIntOrNull() ?: return null
        val minuto = match.groupValues[2].toIntOrNull() ?: return null
        if (hora !in 0..23 || minuto !in 0..59) return null
        return hora to minuto
    }

    private fun aplicarHorario(baseMillis: Long, horario: Pair<Int, Int>?): Long {
        if (horario == null) return baseMillis
        return Calendar.getInstance().apply {
            timeInMillis = baseMillis
            set(Calendar.HOUR_OF_DAY, horario.first)
            set(Calendar.MINUTE, horario.second)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun proximoHorario(hora: Int, minuto: Int): Long {
        val agora = Calendar.getInstance()
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, minuto)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= agora.timeInMillis) add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis
    }

    private fun normalizar(texto: String): String =
        texto.lowercase(Locale("pt", "BR"))
            .replace('á', 'a')
            .replace('à', 'a')
            .replace('â', 'a')
            .replace('ã', 'a')
            .replace('é', 'e')
            .replace('ê', 'e')
            .replace('í', 'i')
            .replace('ó', 'o')
            .replace('ô', 'o')
            .replace('õ', 'o')
            .replace('ú', 'u')
            .replace('ç', 'c')

    suspend fun concluidasHoje(): Int {
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return tarefaDao.contarConcluidasHoje(cal.timeInMillis)
    }
}
