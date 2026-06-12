package com.pata3d.mira.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pata3d.mira.ui.theme.NeonAmber
import com.pata3d.mira.ui.theme.NeonPink
import com.pata3d.mira.ui.theme.NeonPurple
import com.pata3d.mira.ui.theme.TextMuted
import com.pata3d.mira.ui.viewmodel.ProgressoViewModel

@Composable
fun TelaProgresso(vm: ProgressoViewModel) {
    val ui by vm.ui.collectAsState()

    Scaffold(containerColor = Color.Transparent) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp, 18.dp, 20.dp, 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Text("Progresso", style = MaterialTheme.typography.displayMedium) }
            item { Text("Seu historico de foco.", style = MaterialTheme.typography.headlineSmall, color = TextMuted) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Hoje", ui.concluidasHoje, NeonPink, Modifier.weight(1f))
                    StatCard("Semana", ui.concluidasSemana, NeonPurple, Modifier.weight(1f))
                    StatCard("Abertas", ui.abertas, NeonAmber, Modifier.weight(1f))
                }
            }
            item {
                MiraGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color(0x33FF9A1F), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.LocalFireDepartment,
                                contentDescription = null,
                                tint = NeonAmber,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(Modifier.size(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (ui.sequenciaDias > 0) "${ui.sequenciaDias} dias de sequencia" else "Comece sua sequencia",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                "Conclua tarefas todos os dias e crie ritmo.",
                                color = TextMuted,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Text("›", color = NeonPink, style = MaterialTheme.typography.displaySmall)
                    }
                }
            }
            item {
                MiraGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Ultimos 7 dias", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                            Box(
                                modifier = Modifier
                                    .background(Color(0x4412081B), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Text("Concluidas", style = MaterialTheme.typography.titleSmall)
                            }
                        }
                        SemanaGrafico(ui.porDiaSemana.map { it.second })
                        Text(
                            if (ui.porDiaSemana.sumOf { it.second } > 0) {
                                "Seu ritmo ja comecou a aparecer."
                            } else {
                                "Ainda sem tarefas concluidas. Cada avanco seu aparece aqui."
                            },
                            color = TextMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(rotulo: String, valor: Int, cor: Color, modifier: Modifier = Modifier) {
    MiraGlassCard(modifier = modifier.height(146.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.48f)
                    .height(5.dp)
                    .background(cor, RoundedCornerShape(100.dp))
            )
            Text("$valor", style = MaterialTheme.typography.displayMedium.copy(color = cor, fontWeight = FontWeight.Bold))
            Text(rotulo, style = MaterialTheme.typography.headlineSmall, color = TextMuted)
        }
    }
}

@Composable
private fun SemanaGrafico(valores: List<Int>) {
    val max = (valores.maxOrNull() ?: 0).coerceAtLeast(3)
    val nomes = listOf("Sab", "Dom", "Seg", "Ter", "Qua", "Qui", "Sex")

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(138.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            valores.forEachIndexed { idx, valor ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 38.dp, height = ((valor.coerceAtLeast(1) * 88) / max).dp)
                            .background(if (valor > 0) NeonPink else Color(0x33170A21), RoundedCornerShape(12.dp))
                    )
                    Text(nomes[idx], color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Text(
            "${valores.sum()} concluidas na semana",
            color = TextMuted,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
