package com.pata3d.mira.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pata3d.mira.data.BlocoDisponibilidade
import com.pata3d.mira.ui.viewmodel.CronogramaViewModel

private val DIAS = listOf("Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado", "Domingo")
private val TIPOS = listOf("trabalho", "pausa", "livre", "dormir")
private val NIVEIS = listOf("indisponivel", "baixa", "media", "alta")
private val CONTEXTOS = listOf("casa", "oficina", "fora", "qualquer")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaCronograma(
    vm: CronogramaViewModel,
    onVoltar: () -> Unit,
) {
    val blocos by vm.blocos.collectAsState()
    var editando by remember { mutableStateOf<BlocoDisponibilidade?>(null) }
    var adicionandoDia by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Cronograma semanal", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { adicionandoDia = 1 }) {
                Icon(Icons.Outlined.Add, contentDescription = "Adicionar bloco")
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            // Agrupar por dia da semana (1=Segunda...7=Domingo)
            items((1..7).toList()) { dia ->
                val blocosDia = blocos.filter { it.diaSemana == dia }.sortedBy { it.horaInicio }
                CartaoDia(
                    nomeDia = DIAS[dia - 1],
                    blocos = blocosDia,
                    onEditar = { editando = it },
                    onDeletar = { vm.deletar(it.id) },
                    onAdicionar = { adicionandoDia = dia },
                )
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    editando?.let { bloco ->
        DialogoBloco(
            bloco = bloco,
            titulo = "Editar bloco",
            onDismiss = { editando = null },
            onSalvar = { vm.salvar(it); editando = null },
        )
    }

    adicionandoDia?.let { dia ->
        DialogoBloco(
            bloco = BlocoDisponibilidade(diaSemana = dia, horaInicio = 480, horaFim = 600, tipo = "trabalho", nivel = "alta"),
            titulo = "Novo bloco",
            onDismiss = { adicionandoDia = null },
            onSalvar = { vm.salvar(it.copy(id = 0)); adicionandoDia = null },
        )
    }
}

@Composable
private fun CartaoDia(
    nomeDia: String,
    blocos: List<BlocoDisponibilidade>,
    onEditar: (BlocoDisponibilidade) -> Unit,
    onDeletar: (BlocoDisponibilidade) -> Unit,
    onAdicionar: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(nomeDia, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = onAdicionar, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.Add, contentDescription = "Adicionar", tint = MaterialTheme.colorScheme.primary)
                }
            }

            if (blocos.isEmpty()) {
                Text(
                    "Sem blocos configurados",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                blocos.forEach { bloco ->
                    LinhaBloco(bloco, onEditar, onDeletar)
                }
            }
        }
    }
}

@Composable
private fun LinhaBloco(
    bloco: BlocoDisponibilidade,
    onEditar: (BlocoDisponibilidade) -> Unit,
    onDeletar: (BlocoDisponibilidade) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            "${horaFormatada(bloco.horaInicio)} – ${horaFormatada(bloco.horaFim)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(108.dp),
        )
        Text(
            bloco.tipo,
            style = MaterialTheme.typography.bodySmall,
            color = corTipo(bloco.tipo),
            modifier = Modifier.weight(1f),
        )
        Text(
            bloco.nivel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        IconButton(onClick = { onEditar(bloco) }, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Outlined.Edit, contentDescription = "Editar", modifier = Modifier.size(16.dp))
        }
        IconButton(onClick = { onDeletar(bloco) }, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Outlined.Delete, contentDescription = "Deletar", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun DialogoBloco(
    bloco: BlocoDisponibilidade,
    titulo: String,
    onDismiss: () -> Unit,
    onSalvar: (BlocoDisponibilidade) -> Unit,
) {
    var diaSemana by remember { mutableStateOf(bloco.diaSemana) }
    var horaInicioTexto by remember { mutableStateOf(horaFormatada(bloco.horaInicio)) }
    var horaFimTexto by remember { mutableStateOf(horaFormatada(bloco.horaFim)) }
    var tipo by remember { mutableStateOf(bloco.tipo) }
    var nivel by remember { mutableStateOf(bloco.nivel) }
    var contexto by remember { mutableStateOf(bloco.contexto) }
    var erro by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo) },
        confirmButton = {
            TextButton(onClick = {
                val inicio = textoParaMinutos(horaInicioTexto)
                val fim = textoParaMinutos(horaFimTexto)
                if (inicio == null || fim == null) {
                    erro = "Horário inválido. Use HH:MM."
                    return@TextButton
                }
                if (fim <= inicio) {
                    erro = "Fim deve ser depois do início."
                    return@TextButton
                }
                onSalvar(bloco.copy(diaSemana = diaSemana, horaInicio = inicio, horaFim = fim, tipo = tipo, nivel = nivel, contexto = contexto))
            }) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Dia (só mostra se for novo bloco, id==0)
                if (bloco.id == 0L) {
                    Text("Dia da semana", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    ChipsSelector(DIAS, diaSemana - 1) { diaSemana = it + 1 }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = horaInicioTexto,
                        onValueChange = { horaInicioTexto = it },
                        label = { Text("Início") },
                        placeholder = { Text("08:00") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = dialogoCampoCores(),
                    )
                    OutlinedTextField(
                        value = horaFimTexto,
                        onValueChange = { horaFimTexto = it },
                        label = { Text("Fim") },
                        placeholder = { Text("12:00") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = dialogoCampoCores(),
                    )
                }

                Text("Tipo", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                ChipsSelector(TIPOS, TIPOS.indexOf(tipo).coerceAtLeast(0)) { tipo = TIPOS[it] }

                Text("Nível", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                ChipsSelector(NIVEIS, NIVEIS.indexOf(nivel).coerceAtLeast(0)) { nivel = NIVEIS[it] }

                Text("Contexto", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                ChipsSelector(CONTEXTOS, CONTEXTOS.indexOf(contexto).coerceAtLeast(0)) { contexto = CONTEXTOS[it] }

                if (erro.isNotBlank()) {
                    Text(erro, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        },
    )
}

@Composable
private fun ChipsSelector(opcoes: List<String>, selecionado: Int, onSelecionar: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        opcoes.forEachIndexed { i, op ->
            FilterChip(
                selected = i == selecionado,
                onClick = { onSelecionar(i) },
                label = { Text(op, style = MaterialTheme.typography.labelSmall) },
            )
        }
    }
}

@Composable
private fun dialogoCampoCores() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
)

private fun corTipo(tipo: String) = when (tipo) {
    "trabalho" -> androidx.compose.ui.graphics.Color(0xFF0EA5E9)
    "pausa" -> androidx.compose.ui.graphics.Color(0xFF22C55E)
    "livre" -> androidx.compose.ui.graphics.Color(0xFFB14CFF)
    "dormir" -> androidx.compose.ui.graphics.Color(0xFF64748B)
    else -> androidx.compose.ui.graphics.Color.Gray
}

private fun textoParaMinutos(texto: String): Int? {
    val partes = texto.trim().split(":", "h", "H")
    if (partes.size < 2) return null
    val h = partes[0].toIntOrNull() ?: return null
    val m = partes[1].toIntOrNull() ?: return null
    if (h !in 0..23 || m !in 0..59) return null
    return h * 60 + m
}
