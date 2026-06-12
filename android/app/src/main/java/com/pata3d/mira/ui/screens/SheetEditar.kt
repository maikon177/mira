package com.pata3d.mira.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pata3d.mira.data.Tarefa

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
