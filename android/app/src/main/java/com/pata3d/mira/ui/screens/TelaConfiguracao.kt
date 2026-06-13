package com.pata3d.mira.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.pata3d.mira.bubble.BolhaService
import com.pata3d.mira.ui.theme.*
import com.pata3d.mira.ui.viewmodel.ConfigViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaConfiguracao(
    vm: ConfigViewModel,
    onVoltar: () -> Unit,
    onToggleBolha: (Boolean) -> Unit = {},
    temPermissaoOverlay: () -> Boolean = { false },
    onAbrirCronograma: () -> Unit = {},
) {
    val ui by vm.ui.collectAsState()
    var mostrarChave by remember { mutableStateOf(false) }
    var bolhaAtiva by remember { mutableStateOf(BolhaService.isRunning) }
    var mostrarPickerLembrete by remember { mutableStateOf(false) }
    var mostrarPickerSilencio by remember { mutableStateOf(false) }
    var mostrarPickerTarde by remember { mutableStateOf(false) }
    var mostrarPickerNoite by remember { mutableStateOf(false) }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var temOverlay by remember { mutableStateOf(temPermissaoOverlay()) }
    DisposableEffect(lifecycle) {
        val obs = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                temOverlay = temPermissaoOverlay()
                bolhaAtiva = BolhaService.isRunning
            }
        }
        lifecycle.addObserver(obs)
        onDispose { lifecycle.removeObserver(obs) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Configurações", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Toggle básico/avançado
            Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Modo avançado", style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface)
                        Text("Mais controle sobre notificações e IA",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = ui.modoAvancado, onCheckedChange = vm::toggleModoAvancado)
                }
            }

            // ── BÁSICO ──
            SecaoCard("Chave DeepSeek", Icons.Outlined.Key) {
                Text("Fica salva somente neste aparelho.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = ui.chave,
                    onValueChange = vm::setChave,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("sk-...") },
                    singleLine = true,
                    visualTransformation = if (mostrarChave) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        IconButton(onClick = { mostrarChave = !mostrarChave }) {
                            Icon(if (mostrarChave) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = null)
                        }
                    },
                )
                Button(onClick = vm::salvarChave, enabled = ui.chave.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp)) {
                    Text("Salvar chave")
                }
                if (ui.chaveSalva) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = ActionGreen, modifier = Modifier.size(18.dp))
                        Text("Chave salva.", style = MaterialTheme.typography.bodySmall, color = ActionGreen)
                    }
                }
                if (ui.chave.isNotBlank()) {
                    var confirmarApagar by remember { mutableStateOf(false) }
                    OutlinedButton(
                        onClick = { confirmarApagar = true },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                    ) {
                        Icon(Icons.Outlined.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Apagar chave salva")
                    }
                    if (confirmarApagar) {
                        AlertDialog(
                            onDismissRequest = { confirmarApagar = false },
                            title = { Text("Apagar chave?") },
                            text = { Text("A chave DeepSeek será removida deste aparelho. As funções de IA vão parar de funcionar.") },
                            confirmButton = {
                                TextButton(onClick = { vm.apagarChave(); confirmarApagar = false }) {
                                    Text("Apagar", color = MaterialTheme.colorScheme.error)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { confirmarApagar = false }) { Text("Cancelar") }
                            },
                        )
                    }
                }
            }

            SecaoCard("Conversa com IA", Icons.Outlined.PsychologyAlt) {
                LinhaSwitch("Limpar histórico ao sair", ui.chatLimparAoSair, vm::toggleChatLimparAoSair)
            }

            SecaoCard("Notificações", Icons.Outlined.Notifications) {
                LinhaSwitch("Notificações ativas", ui.notificacoesAtivas, vm::toggleNotificacoes)
                LinhaEditavel("Lembrete do dia", horaFormatada(ui.lembreteManhaMin)) { mostrarPickerLembrete = true }
                LinhaEditavel("Silêncio", "${horaFormatada(ui.silencioInicioMin)} – ${horaFormatada(ui.silencioFimMin)}") { mostrarPickerSilencio = true }
            }

            if (mostrarPickerLembrete) {
                val state = rememberTimePickerState(
                    initialHour = ui.lembreteManhaMin / 60,
                    initialMinute = ui.lembreteManhaMin % 60,
                )
                AlertDialog(
                    onDismissRequest = { mostrarPickerLembrete = false },
                    confirmButton = {
                        TextButton(onClick = {
                            vm.setLembreteManha(state.hour * 60 + state.minute)
                            mostrarPickerLembrete = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrarPickerLembrete = false }) { Text("Cancelar") }
                    },
                    title = { Text("Lembrete do dia") },
                    text = { TimePicker(state = state) },
                )
            }

            if (mostrarPickerSilencio) {
                val stateInicio = rememberTimePickerState(
                    initialHour = ui.silencioInicioMin / 60,
                    initialMinute = ui.silencioInicioMin % 60,
                )
                AlertDialog(
                    onDismissRequest = { mostrarPickerSilencio = false },
                    confirmButton = {
                        TextButton(onClick = {
                            vm.setSilencio(stateInicio.hour * 60 + stateInicio.minute, ui.silencioFimMin)
                            mostrarPickerSilencio = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrarPickerSilencio = false }) { Text("Cancelar") }
                    },
                    title = { Text("Início do silêncio") },
                    text = { TimePicker(state = stateInicio) },
                )
            }

            SecaoCard("Alertas e Alarmes", Icons.Outlined.NotificationImportant) {
                Text("Controle quais tipos de alerta o Mira pode disparar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                LinhaSwitch("Alarmes de compromisso", ui.alertasCompromisso, vm::toggleAlertasCompromisso)
                LinhaSwitch("Alerta de prazo apertado", ui.alertasRiscoPrazo, vm::toggleAlertasRisco)
                LinhaSwitch("Checkin de máquina/impressão", ui.alertasMaquina, vm::toggleAlertasMaquina)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Text("Check-ins diários", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                LinhaEditavel("Manhã", horaFormatada(ui.lembreteManhaMin)) { mostrarPickerLembrete = true }
                LinhaEditavel("Tarde", horaFormatada(ui.checkinTardeMin)) { mostrarPickerTarde = true }
                LinhaEditavel("Noite", horaFormatada(ui.checkinNoiteMin)) { mostrarPickerNoite = true }
            }

            if (mostrarPickerTarde) {
                val state = rememberTimePickerState(
                    initialHour = ui.checkinTardeMin / 60,
                    initialMinute = ui.checkinTardeMin % 60,
                )
                AlertDialog(
                    onDismissRequest = { mostrarPickerTarde = false },
                    confirmButton = {
                        TextButton(onClick = {
                            vm.setCheckinTarde(state.hour * 60 + state.minute)
                            mostrarPickerTarde = false
                        }) { Text("OK") }
                    },
                    dismissButton = { TextButton(onClick = { mostrarPickerTarde = false }) { Text("Cancelar") } },
                    title = { Text("Check-in da tarde") },
                    text = { TimePicker(state = state) },
                )
            }
            if (mostrarPickerNoite) {
                val state = rememberTimePickerState(
                    initialHour = ui.checkinNoiteMin / 60,
                    initialMinute = ui.checkinNoiteMin % 60,
                )
                AlertDialog(
                    onDismissRequest = { mostrarPickerNoite = false },
                    confirmButton = {
                        TextButton(onClick = {
                            vm.setCheckinNoite(state.hour * 60 + state.minute)
                            mostrarPickerNoite = false
                        }) { Text("OK") }
                    },
                    dismissButton = { TextButton(onClick = { mostrarPickerNoite = false }) { Text("Cancelar") } },
                    title = { Text("Check-in da noite") },
                    text = { TimePicker(state = state) },
                )
            }

            SecaoCard("Cronograma semanal", Icons.Outlined.Schedule) {
                Text(
                    "Configure seus blocos de disponibilidade por dia e horário.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                LinhaEditavel("Editar cronograma", "Abrir") { onAbrirCronograma() }
            }

            SecaoCard("Bolha flutuante", Icons.Outlined.Adjust) {
                Text(
                    "Aparece sobre outros apps. Toque para ver as tarefas prioritárias sem abrir o Mira.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                LinhaSwitch(
                    label = if (temOverlay) "Bolha ativa" else "Ativar bolha (pede permissão)",
                    checked = bolhaAtiva && temOverlay,
                    onChange = { ligar ->
                        bolhaAtiva = ligar
                        onToggleBolha(ligar)
                    },
                )
            }

            SecaoCard("Tema", Icons.Outlined.Palette) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("escuro" to "Escuro", "claro" to "Claro", "sistema" to "Sistema").forEach { (v, label) ->
                        FilterChip(selected = ui.tema == v, onClick = { vm.setTema(v) },
                            label = { Text(label) })
                    }
                }
            }

            // ── AVANÇADO ──
            AnimatedVisibility(visible = ui.modoAvancado) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SecaoCard("Sugestões", Icons.Outlined.AutoAwesome) {
                        Text("Máximo de sugestões na tela Hoje: ${ui.maxSugestoes}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface)
                        Slider(
                            value = ui.maxSugestoes.toFloat(),
                            onValueChange = { vm.setMaxSugestoes(it.toInt()) },
                            valueRange = 0f..3f, steps = 2,
                        )
                        LinhaSwitch("Sugerir tarefas adiadas (destravar)",
                            ui.sugestaoDestravarAtiva, vm::toggleDestravar)
                    }
                    SecaoCard("IA", Icons.Outlined.Memory) {
                        OutlinedButton(onClick = vm::limparChat, modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)) {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Apagar histórico do chat")
                        }
                    }
                }
            }

            // Sobre
            SecaoCard("Sobre", Icons.Outlined.Info) {
                Text("Mira ${com.pata3d.mira.BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                Text("Tarefa certa, na hora certa, com o tamanho certo.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SecaoCard(titulo: String, icone: androidx.compose.ui.graphics.vector.ImageVector, conteudo: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icone, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text(titulo, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            }
            conteudo()
        }
    }
}

@Composable
private fun LinhaSwitch(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun LinhaInfo(label: String, valor: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Text(valor, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LinhaEditavel(label: String, valor: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(valor, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary)
            Icon(Icons.Outlined.Edit, contentDescription = null,
                modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
        }
    }
}
