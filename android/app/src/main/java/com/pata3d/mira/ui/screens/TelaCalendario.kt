package com.pata3d.mira.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pata3d.mira.data.Tarefa
import com.pata3d.mira.domain.Sugestoes
import com.pata3d.mira.ui.theme.GlowGradient
import com.pata3d.mira.ui.theme.NeonAmber
import com.pata3d.mira.ui.theme.NeonPink
import com.pata3d.mira.ui.theme.TextMuted
import com.pata3d.mira.ui.viewmodel.CalendarioViewModel
import java.util.Calendar

@Composable
fun TelaCalendario(vm: CalendarioViewModel) {
    val ui by vm.ui.collectAsState()

    Scaffold(containerColor = Color.Transparent) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp, 18.dp, 20.dp, 120.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item { CabecalhoCalendario(ui.diaSelecionado) }
            item { SemanaStrip(ui.diaSelecionado, ui.diasComTarefa, vm::selecionarDia) }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(rotuloDia(ui.diaSelecionado), style = MaterialTheme.typography.displaySmall)
                        Text("${ui.tarefasDoDia.size} tarefa${if (ui.tarefasDoDia.size == 1) "" else "s"} agendada${if (ui.tarefasDoDia.size == 1) "" else "s"}",
                            style = MaterialTheme.typography.bodyLarge, color = TextMuted)
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0x33140721), RoundedCornerShape(18.dp))
                            .padding(horizontal = 18.dp, vertical = 14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Ver dia completo", color = NeonPink, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.size(8.dp))
                            Icon(Icons.Outlined.KeyboardArrowRight, contentDescription = null, tint = NeonPink)
                        }
                    }
                }
            }
            item { HeroDiaOuPlaceholder(ui.tarefasDoDia.firstOrNull(), vm) }
            item { SecaoSemData(ui.semData, vm) }
        }
    }
}

@Composable
private fun CabecalhoCalendario(selecionado: Long) {
    Row(verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f)) {
            Text(nomeMes(selecionado), style = MaterialTheme.typography.displayMedium)
            Text("Suas tarefas no tempo", style = MaterialTheme.typography.headlineSmall, color = TextMuted)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TopIcon(Icons.Outlined.CalendarMonth)
            TopIcon(Icons.Outlined.MoreVert)
        }
    }
}

@Composable
private fun TopIcon(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(
        modifier = Modifier
            .size(58.dp)
            .background(Color(0x3312081B), RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = NeonPink)
    }
}

@Composable
private fun SemanaStrip(
    diaSelecionado: Long,
    diasComTarefa: Set<Long>,
    onSelecionar: (Long) -> Unit,
) {
    val dias = remember(diaSelecionado) { semanaDe(diaSelecionado) }
    val nomes = listOf("SEG", "TER", "QUA", "QUI", "SEX", "SÁB", "DOM")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        dias.forEachIndexed { idx, dia ->
            val numero = Calendar.getInstance().apply { timeInMillis = dia }.get(Calendar.DAY_OF_MONTH)
            val selecionado = Sugestoes.mesmoDia(dia, diaSelecionado)
            val temTarefa = diasComTarefa.contains(Sugestoes.inicioDoDia(dia))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onSelecionar(Sugestoes.inicioDoDia(dia)) }
            ) {
                Text(nomes[idx], color = if (selecionado) NeonPink else TextMuted, style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .background(if (selecionado) Color.Transparent else Color(0x22150A21), CircleShape)
                        .then(if (selecionado) Modifier.background(GlowGradient, CircleShape) else Modifier),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$numero", color = Color.White, style = MaterialTheme.typography.titleLarge)
                }
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .size(if (selecionado || temTarefa) 8.dp else 4.dp)
                        .background(if (selecionado) NeonPink else if (temTarefa) NeonAmber else Color.Transparent, CircleShape)
                )
            }
        }
    }
}

@Composable
private fun HeroDia(tarefa: Tarefa, vm: CalendarioViewModel) {
    MiraGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = NeonPink)
                Spacer(Modifier.size(8.dp))
                Text("AGORA", style = MaterialTheme.typography.headlineSmall, color = NeonPink)
                Spacer(Modifier.weight(1f))
                ChipPrioridade(tarefa.prioridade)
            }
            Text(tarefa.titulo, style = MaterialTheme.typography.displaySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ChipInfo(tarefa.categoria)
                ChipInfo("Geral")
            }
            Text("Sugerido: ${tarefa.tempoEstimadoMin ?: 30} min", color = TextMuted, style = MaterialTheme.typography.bodyLarge)
            GlowPrimaryButton("Concluir", modifier = Modifier.fillMaxWidth().height(76.dp))
            androidx.compose.material3.Button(
                onClick = { vm.concluir(tarefa.id) },
                modifier = Modifier.fillMaxWidth().height(76.dp).padding(0.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) { Text("", color = Color.Transparent) }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.weight(1f).background(Color(0x4412081D), RoundedCornerShape(20.dp)).padding(vertical = 22.dp),
                    contentAlignment = Alignment.Center
                ) { Text("Iniciar", color = NeonPink, style = MaterialTheme.typography.titleMedium) }
                Box(
                    modifier = Modifier.weight(1f).background(Color(0x4412081D), RoundedCornerShape(20.dp)).padding(vertical = 22.dp),
                    contentAlignment = Alignment.Center
                ) { Text("Adiar", color = NeonPink, style = MaterialTheme.typography.titleMedium) }
            }
        }
    }
}

@Composable
private fun HeroDiaOuPlaceholder(tarefa: Tarefa?, vm: CalendarioViewModel) {
    if (tarefa != null) {
        HeroDia(tarefa, vm)
        return
    }

    MiraGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = NeonPink)
                Spacer(Modifier.size(8.dp))
                Text("AGORA", style = MaterialTheme.typography.headlineSmall, color = NeonPink)
                Spacer(Modifier.weight(1f))
                ChipInfo("Dia livre")
            }
            Text("Nenhuma tarefa marcada para este dia.", style = MaterialTheme.typography.displaySmall)
            Text(
                "Use o calendário para espalhar suas próximas ações com mais clareza.",
                color = TextMuted,
                style = MaterialTheme.typography.bodyLarge
            )
            GlowPrimaryButton("Planejar este dia", modifier = Modifier.fillMaxWidth().height(76.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.weight(1f).background(Color(0x4412081D), RoundedCornerShape(20.dp)).padding(vertical = 22.dp),
                    contentAlignment = Alignment.Center
                ) { Text("Hoje", color = NeonPink, style = MaterialTheme.typography.titleMedium) }
                Box(
                    modifier = Modifier.weight(1f).background(Color(0x4412081D), RoundedCornerShape(20.dp)).padding(vertical = 22.dp),
                    contentAlignment = Alignment.Center
                ) { Text("Amanhã", color = NeonPink, style = MaterialTheme.typography.titleMedium) }
            }
        }
    }
}

@Composable
private fun SecaoSemData(tarefas: List<Tarefa>, vm: CalendarioViewModel) {
    var expandido by remember { mutableStateOf(true) }
    MiraGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Sem data (${tarefas.size})", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        if (tarefas.isEmpty()) "Nenhuma tarefa sem agenda"
                        else "Tarefas sem data definida",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                IconButton(onClick = { expandido = !expandido }) {
                    Icon(
                        if (expandido) Icons.Outlined.KeyboardArrowRight else Icons.Outlined.MoreVert,
                        contentDescription = null,
                        tint = NeonPink
                    )
                }
            }
            if (expandido) {
                if (tarefas.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x5513081E), RoundedCornerShape(18.dp))
                            .padding(18.dp)
                    ) {
                        Text(
                            "Quando surgirem tarefas soltas, elas aparecem aqui para você decidir depois.",
                            color = TextMuted,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    tarefas.take(3).forEach { t ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x5513081E), RoundedCornerShape(18.dp))
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(18.dp).background(Color.Transparent, CircleShape).background(Color(0x44FF39C8), CircleShape))
                                Spacer(Modifier.size(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(t.titulo, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(t.categoria, color = NeonPink, style = MaterialTheme.typography.bodyMedium)
                                }
                                Icon(Icons.Outlined.MoreVert, contentDescription = null, tint = TextMuted)
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Quer agendar alguma tarefa?", color = TextMuted, style = MaterialTheme.typography.bodyLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Agendar("Hoje")
                        Agendar("Amanhã")
                    }
                }
            }
        }
    }
}

@Composable
private fun Agendar(texto: String) {
    Box(
        modifier = Modifier
            .background(Color(0x3312091A), RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(texto, color = NeonPink, style = MaterialTheme.typography.titleSmall)
    }
}

private fun semanaDe(millis: Long): List<Long> {
    val cal = Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) cal.add(Calendar.DAY_OF_YEAR, -1)
    return (0..6).map { i ->
        (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, i) }.timeInMillis
    }
}
