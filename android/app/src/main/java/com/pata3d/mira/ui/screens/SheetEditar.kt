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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pata3d.mira.data.Tarefa
import com.pata3d.mira.data.TipoAlerta
import com.pata3d.mira.data.TipoTarefa
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetEditar(
    tarefa: Tarefa,
    onFechar: () -> Unit,
    onSalvar: (Tarefa) -> Unit,
) {
    var titulo by remember { mutableStateOf(tarefa.titulo) }
    var prioridade by remember { mutableStateOf(tarefa.prioridade) }
    var categoria by remember { mutableStateOf(tarefa.categoria) }
    var tempo by remember { mutableStateOf(tarefa.tempoEstimadoMin?.toString() ?: "") }
    var proximaAcao by remember { mutableStateOf(tarefa.proximaAcao) }

    var tipoTarefa by remember { mutableStateOf(tarefa.tipoTarefa) }
    var prazoEm by remember { mutableStateOf(tarefa.prazoEm) }
    var compromissoEm by remember { mutableStateOf(tarefa.compromissoEm) }
    var lembreteEm by remember { mutableStateOf(tarefa.lembreteEm) }
    var microPasso by remember { mutableStateOf(tarefa.microPasso) }
    var protegerPrazo by remember { mutableStateOf(tarefa.protegerPrazo) }
    var tempoPrepMin by remember { mutableStateOf(tarefa.tempoPrepMin?.toString() ?: "") }
    var tempoMaquinaMin by remember { mutableStateOf(tarefa.tempoMaquinaMin?.toString() ?: "") }
    var tempoSecagemMin by remember { mutableStateOf(tarefa.tempoSecagemMin?.toString() ?: "") }
    var tempoFinalMin by remember { mutableStateOf(tarefa.tempoFinalMin?.toString() ?: "") }

    val ctxEditar = LocalContext.current

    fun formatarDataHoraEdit(millis: Long): String =
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")).format(millis)

    fun pedirDataHoraEdit(onResultado: (Long) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(ctxEditar, { _, ano, mes, dia ->
            TimePickerDialog(ctxEditar, { _, hora, min ->
                val res = Calendar.getInstance().apply { set(ano, mes, dia, hora, min, 0) }
                onResultado(res.timeInMillis)
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    ModalBottomSheet(
        onDismissRequest = onFechar,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Editar tarefa", style = MaterialTheme.typography.headlineSmall)

            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Título") },
                shape = RoundedCornerShape(14.dp),
                minLines = 2,
                maxLines = 4,
                colors = editarCampoCores(),
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
                        selected = tipoTarefa == tipo.name,
                        onClick  = { tipoTarefa = tipo.name },
                        label    = { Text(label) },
                    )
                }
            }

            // Campos condicionais por tipo
            if (tipoTarefa == TipoTarefa.LEMBRETE.name) {
                OutlinedTextField(
                    value = lembreteEm?.let { formatarDataHoraEdit(it) } ?: "",
                    onValueChange = {},
                    label = { Text("Data/hora do lembrete") },
                    modifier = Modifier.fillMaxWidth().clickable { pedirDataHoraEdit { lembreteEm = it } },
                    readOnly = true,
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = { Icon(Icons.Outlined.Schedule, null) },
                    colors = editarCampoCores(),
                )
            }

            if (tipoTarefa == TipoTarefa.COMPROMISSO.name) {
                OutlinedTextField(
                    value = compromissoEm?.let { formatarDataHoraEdit(it) } ?: "",
                    onValueChange = {},
                    label = { Text("Data/hora do compromisso") },
                    modifier = Modifier.fillMaxWidth().clickable { pedirDataHoraEdit { compromissoEm = it } },
                    readOnly = true,
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = { Icon(Icons.Outlined.Alarm, null) },
                    colors = editarCampoCores(),
                )
            }

            if (tipoTarefa == TipoTarefa.ENTREGA.name || tipoTarefa == TipoTarefa.PRODUCAO.name) {
                OutlinedTextField(
                    value = prazoEm?.let { formatarDataHoraEdit(it) } ?: "",
                    onValueChange = {},
                    label = { Text("Prazo de entrega") },
                    modifier = Modifier.fillMaxWidth().clickable { pedirDataHoraEdit { prazoEm = it } },
                    readOnly = true,
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = { Icon(Icons.Outlined.Event, null) },
                    colors = editarCampoCores(),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Proteger prazo", style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
                    Switch(checked = protegerPrazo, onCheckedChange = { protegerPrazo = it })
                }
            }

            if (tipoTarefa == TipoTarefa.PRODUCAO.name) {
                Text("Tempo por fase (minutos)", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = tempoPrepMin, onValueChange = { tempoPrepMin = it },
                        label = { Text("Prep") }, modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = editarCampoCores())
                    OutlinedTextField(value = tempoMaquinaMin, onValueChange = { tempoMaquinaMin = it },
                        label = { Text("Máquina") }, modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = editarCampoCores())
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = tempoSecagemMin, onValueChange = { tempoSecagemMin = it },
                        label = { Text("Secagem") }, modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = editarCampoCores())
                    OutlinedTextField(value = tempoFinalMin, onValueChange = { tempoFinalMin = it },
                        label = { Text("Finaliz.") }, modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = editarCampoCores())
                }
            }

            // Micro-passo
            OutlinedTextField(
                value = microPasso,
                onValueChange = { microPasso = it },
                label = { Text("Micro-passo") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                shape = RoundedCornerShape(12.dp),
                colors = editarCampoCores(),
            )

            Text(
                "Prioridade",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Alta", "Média", "Baixa").forEach { p ->
                    FilterChip(
                        selected = prioridade == p || (p == "Média" && prioridade == "Media"),
                        onClick = { prioridade = p },
                        label = { Text(p) },
                    )
                }
            }

            OutlinedTextField(
                value = categoria,
                onValueChange = { categoria = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Categoria") },
                shape = RoundedCornerShape(14.dp),
                colors = editarCampoCores(),
            )

            OutlinedTextField(
                value = tempo,
                onValueChange = { tempo = it.filter(Char::isDigit) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Tempo estimado (minutos)") },
                shape = RoundedCornerShape(14.dp),
                colors = editarCampoCores(),
            )

            OutlinedTextField(
                value = proximaAcao,
                onValueChange = { proximaAcao = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Próxima ação") },
                shape = RoundedCornerShape(14.dp),
                minLines = 2,
                maxLines = 4,
                colors = editarCampoCores(),
            )

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = {
                    onSalvar(
                        tarefa.copy(
                            titulo = titulo.trim().ifBlank { tarefa.titulo },
                            prioridade = prioridade,
                            categoria = categoria.trim().ifBlank { tarefa.categoria },
                            tempoEstimadoMin = tempo.toIntOrNull(),
                            proximaAcao = proximaAcao.trim(),
                            tipoTarefa = tipoTarefa,
                            prazoEm = prazoEm,
                            compromissoEm = compromissoEm,
                            lembreteEm = lembreteEm,
                            microPasso = microPasso,
                            protegerPrazo = protegerPrazo,
                            isTarefaMaquina = tipoTarefa == TipoTarefa.PRODUCAO.name,
                            tempoPrepMin = tempoPrepMin.toIntOrNull(),
                            tempoMaquinaMin = tempoMaquinaMin.toIntOrNull(),
                            tempoSecagemMin = tempoSecagemMin.toIntOrNull(),
                            tempoFinalMin = tempoFinalMin.toIntOrNull(),
                            tipoAlerta = when (tipoTarefa) {
                                TipoTarefa.LEMBRETE.name    -> TipoAlerta.LEMBRETE.name
                                TipoTarefa.COMPROMISSO.name -> TipoAlerta.ALARME_EXATO.name
                                TipoTarefa.ENTREGA.name     -> TipoAlerta.RISCO_PRAZO.name
                                TipoTarefa.PRODUCAO.name    -> TipoAlerta.RISCO_PRAZO.name
                                else                        -> TipoAlerta.NENHUM.name
                            },
                        )
                    )
                    onFechar()
                },
                enabled = titulo.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text("Salvar alterações")
            }
        }
    }
}

@Composable
private fun editarCampoCores() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
)
