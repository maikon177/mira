package com.pata3d.mira.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pata3d.mira.data.RevisaoResult
import com.pata3d.mira.data.TarefaRevisada
import com.pata3d.mira.data.TipoTarefa
import com.pata3d.mira.ui.theme.ActionGreen
import com.pata3d.mira.ui.theme.TextPrimary
import com.pata3d.mira.ui.viewmodel.EntradaViewModel
import com.pata3d.mira.ui.viewmodel.RevisaoState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetAdicionar(
    vm: EntradaViewModel,
    onFechar: () -> Unit,
    onAbrirConfig: () -> Unit,
) {
    val revisao by vm.revisao.collectAsState()
    val ui by vm.ui.collectAsState()
    var texto by remember { mutableStateOf("") }
    var tempoTotal by remember { mutableStateOf("") }
    var etapasTexto by remember { mutableStateOf("") }

    val ctx = LocalContext.current

    fun formatarDataHora(millis: Long): String =
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")).format(millis)

    fun pedirDataHora(onResultado: (Long) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(ctx, { _, ano, mes, dia ->
            TimePickerDialog(ctx, { _, hora, min ->
                val resultado = Calendar.getInstance().apply { set(ano, mes, dia, hora, min, 0) }
                onResultado(resultado.timeInMillis)
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    ModalBottomSheet(
        onDismissRequest = { vm.resetar(); onFechar() },
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (val r = revisao) {
                is RevisaoState.Sucesso -> PainelRevisao(
                    result = r.result,
                    onSalvar = { vm.salvarRevisao(r.result) { texto = ""; etapasTexto = ""; tempoTotal = ""; onFechar() } },
                    onVoltar = { vm.resetar() },
                )
                else -> {
                    Text("Descarregar tarefas", style = MaterialTheme.typography.headlineSmall)
                    Text("Se for uma tarefa grande, voce pode quebrar em etapas logo aqui.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    OutlinedTextField(
                        value = texto,
                        onValueChange = { texto = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Titulo da tarefa ou varias tarefas em linhas separadas") },
                        shape = RoundedCornerShape(14.dp),
                        minLines = 3,
                        maxLines = 8,
                        colors = campoCores(),
                    )

                    // Seletor de tipo
                    val tipos = listOf(
                        TipoTarefa.SIMPLES     to "Simples",
                        TipoTarefa.LEMBRETE    to "Lembrete",
                        TipoTarefa.COMPROMISSO to "Compromisso",
                        TipoTarefa.ENTREGA     to "Entrega",
                        TipoTarefa.PRODUCAO    to "Produção 3D",
                    )
                    Text("Tipo", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(tipos) { (tipo, label) ->
                            FilterChip(
                                selected = ui.tipoTarefa == tipo.name,
                                onClick  = { vm.setTipoTarefa(tipo.name) },
                                label    = { Text(label) },
                            )
                        }
                    }

                    // Campos condicionais por tipo
                    if (ui.tipoTarefa == TipoTarefa.LEMBRETE.name) {
                        OutlinedTextField(
                            value = ui.lembreteEm?.let { formatarDataHora(it) } ?: "",
                            onValueChange = {},
                            label = { Text("Data/hora do lembrete") },
                            modifier = Modifier.fillMaxWidth().clickable { pedirDataHora(vm::setLembreteEm) },
                            readOnly = true,
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = { Icon(Icons.Outlined.Schedule, null) },
                            colors = campoCores(),
                        )
                    }

                    if (ui.tipoTarefa == TipoTarefa.COMPROMISSO.name) {
                        OutlinedTextField(
                            value = ui.compromissoEm?.let { formatarDataHora(it) } ?: "",
                            onValueChange = {},
                            label = { Text("Data/hora do compromisso") },
                            modifier = Modifier.fillMaxWidth().clickable { pedirDataHora(vm::setCompromissoEm) },
                            readOnly = true,
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = { Icon(Icons.Outlined.Alarm, null) },
                            colors = campoCores(),
                        )
                    }

                    if (ui.tipoTarefa == TipoTarefa.ENTREGA.name || ui.tipoTarefa == TipoTarefa.PRODUCAO.name) {
                        OutlinedTextField(
                            value = ui.prazoEm?.let { formatarDataHora(it) } ?: "",
                            onValueChange = {},
                            label = { Text("Prazo de entrega") },
                            modifier = Modifier.fillMaxWidth().clickable { pedirDataHora(vm::setPrazoEm) },
                            readOnly = true,
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = { Icon(Icons.Outlined.Event, null) },
                            colors = campoCores(),
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Proteger prazo", style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
                            Switch(checked = ui.protegerPrazo, onCheckedChange = { vm.toggleProtegerPrazo() })
                        }
                    }

                    if (ui.tipoTarefa == TipoTarefa.PRODUCAO.name) {
                        Text("Tempo por fase (minutos)", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = ui.tempoPrepMin, onValueChange = vm::setTempoPrepMin,
                                label = { Text("Prep") }, modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                colors = campoCores())
                            OutlinedTextField(value = ui.tempoMaquinaMin, onValueChange = vm::setTempoMaquinaMin,
                                label = { Text("Máquina") }, modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                colors = campoCores())
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = ui.tempoSecagemMin, onValueChange = vm::setTempoSecagemMin,
                                label = { Text("Secagem") }, modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                colors = campoCores())
                            OutlinedTextField(value = ui.tempoFinalMin, onValueChange = vm::setTempoFinalMin,
                                label = { Text("Finaliz.") }, modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                colors = campoCores())
                        }
                    }

                    // Micro-passo
                    OutlinedTextField(
                        value = ui.microPasso,
                        onValueChange = vm::setMicroPasso,
                        label = { Text("Micro-passo (ação pequena agora)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        shape = RoundedCornerShape(12.dp),
                        colors = campoCores(),
                    )

                    OutlinedTextField(
                        value = tempoTotal,
                        onValueChange = { tempoTotal = it.filter(Char::isDigit) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Tempo total estimado em minutos (opcional)") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Timer, contentDescription = null)
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = campoCores(),
                    )

                    OutlinedTextField(
                        value = etapasTexto,
                        onValueChange = { etapasTexto = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Etapas, uma por linha. Ex.: Ajustar modelo | 90") },
                        shape = RoundedCornerShape(14.dp),
                        minLines = 3,
                        maxLines = 6,
                        colors = campoCores(),
                    )

                    if (etapasTexto.isNotBlank()) {
                        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp)) {
                            Text(
                                "Formato: titulo da etapa | minutos. A proxima etapa so libera quando a anterior concluir.",
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (texto.isNotBlank()) {
                                if (vm.temChave()) vm.revisarComIA(texto) else onAbrirConfig()
                            }
                        },
                        enabled = texto.isNotBlank() && revisao !is RevisaoState.Carregando,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    ) {
                        if (revisao is RevisaoState.Carregando) {
                            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onSecondary)
                            Spacer(Modifier.width(8.dp))
                            Text("Organizando...")
                        } else {
                            Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (vm.temChave()) "Organizar com IA" else "Configurar IA")
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            if (texto.isNotBlank()) {
                                vm.adicionarDireto(
                                    texto = texto,
                                    tempoTotalMin = tempoTotal.toIntOrNull(),
                                    etapasTexto = etapasTexto,
                                ) {
                                    texto = ""
                                    etapasTexto = ""
                                    tempoTotal = ""
                                    onFechar()
                                }
                            }
                        },
                        enabled = texto.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(if (etapasTexto.isBlank()) "Adicionar sem organizar" else "Salvar tarefa com etapas")
                    }

                    if (revisao is RevisaoState.Erro) {
                        Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(12.dp)) {
                            Text((revisao as RevisaoState.Erro).msg, Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PainelRevisao(result: RevisaoResult, onSalvar: () -> Unit, onVoltar: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Organizado", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
        TextButton(onClick = onVoltar) { Text("Voltar") }
    }

    if (result.proximaAcaoRecomendada.isNotBlank()) {
        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(12.dp)) {
                Text("Comece por", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(result.proximaAcaoRecomendada, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }

    Text("${result.tarefas.size} tarefa${if (result.tarefas.size != 1) "s" else ""}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    result.tarefas.forEach { CartaoRevisada(it) }

    if (result.observacao.isNotBlank()) {
        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp)) {
            Text(result.observacao, Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    Button(
        onClick = onSalvar,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = ActionGreen)
    ) {
        Text("Salvar tudo (${result.tarefas.size})", color = TextPrimary, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CartaoRevisada(t: TarefaRevisada) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ChipPrioridade(t.prioridade)
                ChipInfo(t.categoria)
                t.tempoEstimadoMinutos?.let { ChipInfo("$it min") }
            }
            Text(t.titulo, style = MaterialTheme.typography.bodyLarge)
            if (t.proximaAcao.isNotBlank()) {
                Text("-> ${t.proximaAcao}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun campoCores() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
)
