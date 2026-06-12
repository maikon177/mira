package com.pata3d.mira.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.PsychologyAlt
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pata3d.mira.data.AcaoIA
import com.pata3d.mira.data.ChatMessage
import com.pata3d.mira.ui.theme.GlowGradient
import com.pata3d.mira.ui.theme.NeonAmber
import com.pata3d.mira.ui.theme.NeonPink
import com.pata3d.mira.ui.theme.NeonPinkSoft
import com.pata3d.mira.ui.theme.TextMuted
import com.pata3d.mira.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@Composable
fun TelaChat(vm: ChatViewModel, onAbrirConfig: () -> Unit) {
    val mensagens by vm.mensagens.collectAsState()
    val enviando by vm.enviando.collectAsState()
    val erro by vm.erro.collectAsState()
    val acoes by vm.acoesPendentes.collectAsState()
    var texto by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(mensagens.size, acoes.size) {
        val total = mensagens.size + acoes.size
        if (total > 0) scope.launch { listState.animateScrollToItem(total) }
    }

    Scaffold(containerColor = Color.Transparent) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            CabecalhoChat()

            if (!vm.temChave()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp)
                        .clickable(onClick = onAbrirConfig)
                        .background(Color(0x331B0A18), RoundedCornerShape(22.dp))
                        .padding(18.dp)
                ) {
                    Text(
                        "Configure a chave DeepSeek para conversar com a Mira ->",
                        color = Color(0xFFFF7373),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            if (mensagens.isEmpty()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Sobre o que quer falar?", style = MaterialTheme.typography.headlineMedium)
                    Text("Escolha um tema ou comece do seu jeito.", style = MaterialTheme.typography.bodyLarge, color = TextMuted)
                    SuggestionRow("O que faco hoje?", Icons.Outlined.CalendarMonth) { texto = "O que faco hoje?" }
                    SuggestionRow("Quero organizar um projeto novo", Icons.Outlined.FolderOpen) { texto = "Quero organizar um projeto novo" }
                    SuggestionRow("Vou despejar tudo que ta na cabeca", Icons.Outlined.PsychologyAlt, destaque = true) {
                        texto = "Vou despejar tudo que ta na cabeca"
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(150.dp)
                                    .background(Color(0x221B0928), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(92.dp)
                                        .background(Color(0x66FF39C8), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                            Text(
                                "Fale naturalmente. A Mira te ajuda a organizar\nideias, planejar e decidir o proximo passo.",
                                color = TextMuted,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(top = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(mensagens, key = { it.id }) { Bolha(msg = it) }
                    items(acoes) { CardAcao(it, onConfirmar = { vm.confirmarAcao(it) }, onIgnorar = { vm.ignorarAcao(it) }) }
                    if (enviando) item { Text("Mira esta pensando...", color = TextMuted) }
                    erro?.let { item { Text(it, color = Color(0xFFFF8E8E)) } }
                }
            }

            BarraEntrada(
                texto = texto,
                onTexto = { texto = it },
                enviando = enviando,
                onEnviar = {
                    if (texto.isNotBlank()) {
                        vm.enviar(texto)
                        texto = ""
                    }
                }
            )
        }
    }
}

@Composable
private fun CabecalhoChat() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(74.dp)
                .background(Color(0x33140720), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = NeonPinkSoft, modifier = Modifier.size(34.dp))
        }
        Spacer(Modifier.size(16.dp))
        Column(Modifier.weight(1f)) {
            Text("Conversar", style = MaterialTheme.typography.displaySmall)
            Text("Assistente de tarefas", style = MaterialTheme.typography.headlineSmall, color = TextMuted)
        }
        Box(
            modifier = Modifier
                .size(58.dp)
                .background(Color(0x66180A26), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("M", color = Color.White, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun SuggestionRow(
    texto: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    destaque: Boolean = false,
    onClick: () -> Unit,
) {
    MiraGlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .background(Color(0x22180A22), RoundedCornerShape(28.dp))
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(if (destaque) Color(0x33FF9A1F) else Color(0x331D0A29), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = if (destaque) NeonAmber else NeonPink)
            }
            Spacer(Modifier.size(16.dp))
            Text(texto, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            Text("›", color = NeonPinkSoft, style = MaterialTheme.typography.displaySmall)
        }
    }
}

@Composable
private fun Bolha(msg: ChatMessage) {
    val ehUsuario = msg.role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (ehUsuario) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(if (ehUsuario) Color(0x33FF39C8) else Color(0x66140820), RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(msg.content, style = MaterialTheme.typography.bodyLarge, color = Color.White)
        }
    }
}

@Composable
private fun CardAcao(acao: AcaoIA, onConfirmar: () -> Unit, onIgnorar: () -> Unit) {
    MiraGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(acao.titulo ?: "Acao sugerida", style = MaterialTheme.typography.titleLarge)
            acao.proximaAcao?.let { Text("-> $it", color = TextMuted, style = MaterialTheme.typography.bodyMedium) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.material3.Button(
                    onClick = onConfirmar,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPink, contentColor = Color.White),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Confirmar") }
                androidx.compose.material3.OutlinedButton(onClick = onIgnorar, shape = RoundedCornerShape(14.dp)) {
                    Text("Ignorar")
                }
            }
        }
    }
}

@Composable
private fun BarraEntrada(texto: String, onTexto: (String) -> Unit, enviando: Boolean, onEnviar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp)
            .background(Color(0x4412081C), RoundedCornerShape(26.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value = texto,
            onValueChange = onTexto,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Fale com a Mira...", color = TextMuted) },
            shape = RoundedCornerShape(22.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonPink,
                unfocusedBorderColor = Color(0x885D1D6F),
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                cursorColor = NeonPink,
            ),
            maxLines = 4,
        )
        FilledIconButton(
            onClick = onEnviar,
            enabled = texto.isNotBlank() && !enviando,
            modifier = Modifier
                .size(58.dp)
                .background(GlowGradient, CircleShape),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White,
                disabledContainerColor = Color(0x332A152F),
                disabledContentColor = TextMuted,
            )
        ) {
            Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = null)
        }
    }
}
