package com.pata3d.mira.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pata3d.mira.data.EtapaTarefa
import com.pata3d.mira.data.NivelRisco
import com.pata3d.mira.data.ResumoEtapas
import com.pata3d.mira.data.Tarefa
import com.pata3d.mira.domain.ContextoAtual
import com.pata3d.mira.ui.theme.GlowGradient
import com.pata3d.mira.ui.theme.NeonPink
import com.pata3d.mira.ui.theme.NeonPinkSoft
import com.pata3d.mira.ui.theme.TextMuted
import com.pata3d.mira.ui.viewmodel.HojeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TelaHoje(
    vm: HojeViewModel,
    onAbrirConfig: () -> Unit,
    onAdicionar: () -> Unit,
    onEditar: (Tarefa) -> Unit = {},
) {
    val ui by vm.ui.collectAsState()

    Scaffold(containerColor = Color.Transparent) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(22.dp, 18.dp, 22.dp, 120.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                CabecalhoHoje(
                    totalAbertas = ui.totalAbertas,
                    contexto = ui.contexto,
                    onAbrirConfig = onAbrirConfig,
                    onAdicionar = onAdicionar,
                )
            }

            item {
                HeroAgoraOuPlaceholder(
                    tarefa = ui.hero,
                    resumoEtapas = ui.heroEtapas,
                    agoraMillis = ui.agoraMillis,
                    vm = vm,
                    onAdicionar = onAdicionar,
                    onEditar = onEditar,
                )
            }

            item { SecaoResto(ui.restoDoDia, onEditar = onEditar) }

            item {
                val hora = remember(ui.agoraMillis) {
                    java.util.Calendar.getInstance().also { it.timeInMillis = ui.agoraMillis }
                        .get(java.util.Calendar.HOUR_OF_DAY)
                }
                if (hora in 17..21) {
                    CartaoFechoDia(concluidasHoje = ui.concluidasHoje)
                }
            }
        }
    }
}

@Composable
private fun CabecalhoHoje(
    totalAbertas: Int,
    contexto: ContextoAtual?,
    onAbrirConfig: () -> Unit,
    onAdicionar: () -> Unit,
) {
    val dataHoje = remember {
        SimpleDateFormat("EEE, dd MMM.", Locale("pt", "BR")).format(Date())
            .replaceFirstChar { it.uppercase() }
    }

    Row(verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Sua próxima ação", style = MaterialTheme.typography.displaySmall)
            Text(dataHoje, style = MaterialTheme.typography.titleMedium, color = NeonPink)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickIconBox(icon = Icons.Outlined.Add, onClick = onAdicionar)
            QuickIconBox(icon = Icons.Outlined.Settings, onClick = onAbrirConfig)
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(Color(0x4421142D), CircleShape)
                    .padding(3.dp)
                    .background(Color(0x88511866), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("M", color = Color.White, style = MaterialTheme.typography.titleLarge)
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MiniStatPill(texto = "$totalAbertas abertas", destaque = true, modifier = Modifier.weight(0.9f))
        MiniStatPill(texto = contextoResumo(contexto), destaque = false, modifier = Modifier.weight(1.6f))
    }
}

@Composable
private fun MiniStatPill(texto: String, destaque: Boolean, modifier: Modifier = Modifier) {
    val bg = if (destaque) GlowGradient else androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(Color(0x221A0C29), Color(0x221A0C29)))
    Box(
        modifier = modifier
            .height(58.dp)
            .background(bg, RoundedCornerShape(22.dp))
            .padding(horizontal = if (destaque) 2.dp else 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (destaque) Color.Transparent else Color(0x66100718), RoundedCornerShape(22.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                texto,
                style = MaterialTheme.typography.titleMedium,
                color = if (destaque) Color.White else TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 18.dp)
            )
        }
    }
}

@Composable
private fun QuickIconBox(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(54.dp)
            .background(Color(0x33160723), RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = null, tint = NeonPinkSoft)
        }
    }
}

@Composable
private fun HeroAgoraOuPlaceholder(
    tarefa: Tarefa?,
    resumoEtapas: ResumoEtapas?,
    agoraMillis: Long,
    vm: HojeViewModel,
    onAdicionar: () -> Unit,
    onEditar: (Tarefa) -> Unit = {},
) {
    if (tarefa == null) {
        MiraGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(GlowGradient, RoundedCornerShape(100.dp)))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.MyLocation, contentDescription = null, tint = NeonPink, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("AGORA", style = MaterialTheme.typography.headlineSmall, color = NeonPink)
                    Spacer(Modifier.weight(1f))
                    ChipInfo("Sem tarefa ativa")
                }
                Text("Organizar a caixa de entrada e escolher a próxima ação.", style = MaterialTheme.typography.displaySmall)
                Text("Quando você adicionar tarefas, a Mira destaca a ação principal aqui.", style = MaterialTheme.typography.bodyLarge, color = TextMuted)
                GlowPrimaryButton(texto = "Nova tarefa", modifier = Modifier.fillMaxWidth().height(78.dp), onClick = onAdicionar)
            }
        }
        return
    }

    MiraGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(GlowGradient, RoundedCornerShape(100.dp)))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.MyLocation, contentDescription = null, tint = NeonPink, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("AGORA", style = MaterialTheme.typography.headlineSmall, color = NeonPink)
                Spacer(Modifier.weight(1f))
                ChipPrioridade(tarefa.prioridade)
                IconButton(onClick = { onEditar(tarefa) }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Editar", tint = TextMuted, modifier = Modifier.size(18.dp))
                }
            }

            Text(tarefa.titulo, style = MaterialTheme.typography.displaySmall, maxLines = 3, overflow = TextOverflow.Ellipsis)

            // Badge de risco (só se ATENCAO, APERTADO ou CRITICO)
            val nivelRisco = try { NivelRisco.valueOf(tarefa.nivelRisco) } catch (e: Exception) { NivelRisco.NENHUM }
            BadgeRisco(nivelRisco, modifier = Modifier.padding(top = 4.dp))

            // Micro-passo ou próxima ação como subtítulo
            val passoVisivel = tarefa.microPasso.ifBlank { tarefa.proximaAcao }
            if (passoVisivel.isNotBlank()) {
                Text(
                    text = passoVisivel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            } else {
                TextButton(
                    onClick = { onEditar(tarefa) },
                    modifier = Modifier.padding(top = 2.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                ) {
                    Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(4.dp))
                    Text("Definir primeiro passo", style = MaterialTheme.typography.bodySmall)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ChipInfo(tarefa.categoria)
                tarefa.tempoEstimadoMin?.let { ChipInfo("Total ${it} min") }
            }

            resumoEtapas?.let {
                PainelEtapas(it, agoraMillis, vm)
            } ?: run {
                PainelCronometroTarefa(tarefa, agoraMillis, vm)
            }

            GlowPrimaryButton(texto = "Concluir", modifier = Modifier.fillMaxWidth().height(78.dp), onClick = { vm.concluir(tarefa.id) })

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                SecundarioBotao("Iniciar", Icons.Outlined.PlayArrow, Modifier.weight(1f)) { vm.iniciar(tarefa.id) }
                SecundarioBotao("Adiar", Icons.Outlined.Schedule, Modifier.weight(1f)) { vm.adiar(tarefa.id, 60) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                SecundarioBotao("Cancelar", Icons.Outlined.Close, Modifier.weight(1f), true) { vm.cancelar(tarefa.id) }
                SecundarioBotao("Trocar", Icons.Outlined.MyLocation, Modifier.weight(1f)) { vm.trocarHero() }
            }
        }
    }
}

@Composable
private fun PainelEtapas(resumo: ResumoEtapas, agoraMillis: Long, vm: HojeViewModel) {
    val atual = resumo.etapaAtual ?: return
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x4412081D), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Etapa ${resumo.concluidas + 1} de ${resumo.total}", style = MaterialTheme.typography.titleMedium, color = NeonPink)
                Spacer(Modifier.weight(1f))
                ChipInfo("${resumo.concluidas}/${resumo.total}")
            }
            Text(atual.titulo, style = MaterialTheme.typography.headlineMedium)
            atual.tempoEstimadoMin?.let {
                Text("Estimado: $it min", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
            }
            Text("Tempo real: ${formatarCronometro(atual.tempoRealAcumuladoSeg, atual.cronometroIniciadoEm, agoraMillis)}", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                if (atual.cronometroIniciadoEm == null) {
                    SecundarioBotao("Cronometrar", Icons.Outlined.Timer, Modifier.weight(1f)) { vm.iniciarCronometroEtapa(atual.id) }
                } else {
                    SecundarioBotao("Pausar", Icons.Outlined.Pause, Modifier.weight(1f)) { vm.pausarCronometroEtapa(atual.id) }
                }
                SecundarioBotao("Fechar etapa", Icons.Filled.Check, Modifier.weight(1f)) { vm.concluirEtapa(atual.id) }
            }
            AnimatedVisibility(resumo.etapas.size > 1) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    resumo.etapas.forEachIndexed { index, etapa ->
                        LinhaEtapa(index = index + 1, etapa = etapa, atualId = atual.id)
                    }
                }
            }
        }
    }
}

@Composable
private fun LinhaEtapa(index: Int, etapa: EtapaTarefa, atualId: String) {
    val cor = when {
        etapa.status == "concluida" -> Color(0xFF45D49A)
        etapa.id == atualId -> NeonPink
        else -> TextMuted
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).background(cor, CircleShape))
        Spacer(Modifier.width(10.dp))
        Text("$index. ${etapa.titulo}", color = cor, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun PainelCronometroTarefa(tarefa: Tarefa, agoraMillis: Long, vm: HojeViewModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x4412081D), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Tempo real: ${formatarCronometro(tarefa.tempoRealAcumuladoSeg, tarefa.cronometroIniciadoEm, agoraMillis)}", style = MaterialTheme.typography.titleMedium)
            Text("Use o cronometro para registrar quanto essa tarefa realmente consome.", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                if (tarefa.cronometroIniciadoEm == null) {
                    SecundarioBotao("Cronometrar", Icons.Outlined.Timer, Modifier.weight(1f)) { vm.iniciarCronometroTarefa(tarefa.id) }
                } else {
                    SecundarioBotao("Pausar", Icons.Outlined.Pause, Modifier.weight(1f)) { vm.pausarCronometroTarefa(tarefa.id) }
                }
                tarefa.tempoEstimadoMin?.let { Text("Estimado: $it min", modifier = Modifier.align(Alignment.CenterVertically), color = TextMuted) }
            }
        }
    }
}

@Composable
private fun SecundarioBotao(
    texto: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier,
    apagado: Boolean = false,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(72.dp).alpha(if (apagado) 0.56f else 1f),
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonPinkSoft),
        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0x883F1A57)))
    ) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.size(10.dp))
        Text(texto, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun BadgeRisco(nivel: NivelRisco, modifier: Modifier = Modifier) {
    val (cor, texto) = when (nivel) {
        NivelRisco.CRITICO  -> Color(0xFFD32F2F) to "Prazo crítico"
        NivelRisco.APERTADO -> Color(0xFFF57C00) to "Prazo apertado"
        NivelRisco.ATENCAO  -> Color(0xFFF9A825) to "Atenção ao prazo"
        else -> return
    }
    Surface(
        color = cor.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier,
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelSmall,
            color = cor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

@Composable
private fun SecaoResto(tarefas: List<Tarefa>, onEditar: (Tarefa) -> Unit = {}) {
    var expandido by remember { mutableStateOf(true) }
    MiraGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Sem data (${tarefas.size})", style = MaterialTheme.typography.headlineSmall)
                    Text(if (tarefas.isEmpty()) "Nada parado por aqui" else "Tarefas sem data definida", style = MaterialTheme.typography.bodyLarge, color = TextMuted)
                }
                IconButton(onClick = { expandido = !expandido }) {
                    Icon(Icons.Outlined.Schedule, contentDescription = null, tint = NeonPink)
                }
            }
            AnimatedVisibility(expandido) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (tarefas.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().background(Color(0x66150721), RoundedCornerShape(18.dp)).padding(18.dp)) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Tudo organizado por enquanto.", style = MaterialTheme.typography.titleMedium)
                                Text("Adicione uma nova tarefa ou use a conversa para despejar ideias.", style = MaterialTheme.typography.bodyLarge, color = TextMuted)
                            }
                        }
                    } else {
                        tarefas.take(3).forEach { t ->
                            Box(modifier = Modifier.fillMaxWidth().background(Color(0x66150721), RoundedCornerShape(18.dp)).padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(22.dp).background(Color(0x33FF39C8), CircleShape))
                                    Spacer(Modifier.size(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(t.titulo, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(t.categoria, style = MaterialTheme.typography.bodyMedium, color = NeonPinkSoft)
                                    }
                                    IconButton(onClick = { onEditar(t) }, modifier = Modifier.size(36.dp)) {
                                        Icon(Icons.Outlined.Edit, contentDescription = "Editar", tint = TextMuted, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun contextoResumo(contexto: ContextoAtual?): String {
    if (contexto == null) return "Trabalho · So captura"
    val nivel = when (contexto.nivel) {
        "indisponivel" -> "So captura"
        "baixa" -> "Tarefas rapidas"
        "media" -> "Fluxo normal"
        "alta" -> "Foco alto"
        else -> "So captura"
    }
    return "${contexto.rotuloAmigavel} · $nivel"
}

private fun formatarCronometro(acumuladoSeg: Long, iniciadoEm: Long?, agoraMillis: Long): String {
    val extra = if (iniciadoEm != null) ((agoraMillis - iniciadoEm) / 1000L).coerceAtLeast(0) else 0
    val total = acumuladoSeg + extra
    val horas = total / 3600
    val minutos = (total % 3600) / 60
    val segundos = total % 60
    return if (horas > 0) "%02d:%02d:%02d".format(horas, minutos, segundos) else "%02d:%02d".format(minutos, segundos)
}

@Composable
private fun CartaoFechoDia(concluidasHoje: Int) {
    MiraGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Vamos fechar o dia?", style = MaterialTheme.typography.titleLarge, color = NeonPink)
            if (concluidasHoje > 0) {
                Text(
                    "Você concluiu $concluidasHoje tarefa${if (concluidasHoje > 1) "s" else ""} hoje.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    "Nenhuma tarefa concluída ainda — o dia ainda não acabou.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                "Anote o que ficou para amanhã e descanse.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
            )
        }
    }
}
