package com.pata3d.mira.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pata3d.mira.bubble.BolhaService
import com.pata3d.mira.data.MiraRepository
import com.pata3d.mira.data.Tarefa
import com.pata3d.mira.ui.screens.*
import com.pata3d.mira.ui.theme.MiraBackground
import com.pata3d.mira.ui.theme.NeonPink
import com.pata3d.mira.ui.theme.TextMuted
import com.pata3d.mira.ui.viewmodel.*
import kotlinx.coroutines.launch

private data class Aba(val rota: String, val icone: ImageVector, val label: String)

private val ABAS = listOf(
    Aba("hoje",       Icons.Outlined.MyLocation,    "Hoje"),
    Aba("calendario", Icons.Outlined.CalendarMonth, "Calendário"),
    Aba("chat",       Icons.Outlined.AutoAwesome,   "Conversar"),
    Aba("progresso",  Icons.Outlined.TrendingUp,    "Progresso"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiraApp(
    repo: MiraRepository,
    onPedirOverlay: () -> Unit = {},
    temPermissaoOverlay: () -> Boolean = { false },
) {
    val nav = rememberNavController()
    val factory = remember { MiraViewModelFactory(repo) }

    val hojeVm: HojeViewModel             = viewModel(factory = factory)
    val calendarioVm: CalendarioViewModel = viewModel(factory = factory)
    val chatVm: ChatViewModel             = viewModel(factory = factory)
    val progressoVm: ProgressoViewModel   = viewModel(factory = factory)
    val entradaVm: EntradaViewModel       = viewModel(factory = factory)
    val configVm: ConfigViewModel         = viewModel(factory = factory)
    val cronogramaVm: CronogramaViewModel = viewModel(factory = factory)

    var mostrarSheet by remember { mutableStateOf(false) }
    var tarefaEditando by remember { mutableStateOf<Tarefa?>(null) }
    val scope = rememberCoroutineScope()

    val ctx = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            configVm.exportar { json ->
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    ctx.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            configVm.importar {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    ctx.contentResolver.openInputStream(uri)?.use { stream ->
                        stream.readBytes().toString(Charsets.UTF_8)
                    } ?: ""
                }
            }
        }
    }

    val backEntry by nav.currentBackStackEntryAsState()
    val rota = backEntry?.destination?.route ?: "hoje"
    val mostrarBarra = rota != "config" && rota != "cronograma"

    val mostrarFab = mostrarBarra && rota != "progresso"

    MiraBackground {
        Scaffold(
            containerColor = Color.Transparent,
            floatingActionButton = {
                if (mostrarFab) {
                    FloatingActionButton(
                        onClick = { mostrarSheet = true },
                        containerColor = Color(0xFFB01A7A),
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier
                            .padding(bottom = 90.dp)
                            .size(54.dp),
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = "Nova tarefa", modifier = Modifier.size(26.dp))
                    }
                }
            },
            bottomBar = {
                if (mostrarBarra) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .blur(22.dp)
                            .fillMaxWidth()
                            .height(82.dp)
                            .background(Color(0x22FF38C7), RoundedCornerShape(28.dp))
                    )
                    NavigationBar(
                        containerColor = Color(0xCC14071F),
                        tonalElevation = 0.dp,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .border(1.dp, Color(0xAA6E1A80), RoundedCornerShape(28.dp)),
                    ) {
                        ABAS.forEach { aba ->
                            val selecionado = rota == aba.rota
                            val cor by animateColorAsState(
                                targetValue = if (selecionado) NeonPink else TextMuted,
                                animationSpec = tween(220),
                                label = "tabColor"
                            )
                            NavigationBarItem(
                                selected = selecionado,
                                onClick = {
                                    nav.navigate(aba.rota) {
                                        popUpTo("hoje") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(aba.icone, contentDescription = aba.label, tint = cor)
                                        Spacer(Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(if (selecionado) 10.dp else 6.dp)
                                                .background(
                                                    if (selecionado) cor else Color.Transparent,
                                                    CircleShape
                                                )
                                        )
                                    }
                                },
                                label = { Text(aba.label, style = MaterialTheme.typography.labelMedium, color = cor) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = cor,
                                    selectedTextColor = cor,
                                    indicatorColor = Color.Transparent,
                                    unselectedIconColor = cor,
                                    unselectedTextColor = cor,
                                ),
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            NavHost(
                navController = nav,
                startDestination = "hoje",
                modifier = Modifier.padding(innerPadding),
                enterTransition = { fadeIn(tween(220)) },
                exitTransition = { fadeOut(tween(120)) },
                popEnterTransition = { fadeIn(tween(220)) },
                popExitTransition = { fadeOut(tween(120)) },
            ) {
                composable("hoje") {
                    TelaHoje(
                        hojeVm,
                        onAbrirConfig = { nav.navigate("config") },
                        onAdicionar = { mostrarSheet = true },
                        onEditar = { tarefaEditando = it },
                    )
                }
                composable("calendario") { TelaCalendario(calendarioVm) }
                composable("chat") {
                    val ctx = LocalContext.current
                    DisposableEffect(Unit) {
                        onDispose {
                            if ((ctx.applicationContext as com.pata3d.mira.MiraApplication).repository.prefsRef.chatLimparAoSair) {
                                chatVm.limpar()
                            }
                        }
                    }
                    TelaChat(chatVm, onAbrirConfig = { nav.navigate("config") })
                }
                composable("progresso") { TelaProgresso(progressoVm) }
                composable("config") {
                    val ctxConfig = LocalContext.current
                    TelaConfiguracao(
                        vm = configVm,
                        onVoltar = { nav.popBackStack() },
                        onToggleBolha = { ligar ->
                            if (ligar) {
                                if (temPermissaoOverlay()) {
                                    BolhaService.iniciar(ctxConfig)
                                    repo.prefsRef.bolhaAtiva = true
                                } else {
                                    onPedirOverlay()
                                }
                            } else {
                                BolhaService.parar(ctxConfig)
                                repo.prefsRef.bolhaAtiva = false
                            }
                        },
                        temPermissaoOverlay = temPermissaoOverlay,
                        onAbrirCronograma = { nav.navigate("cronograma") },
                        onExportar = {
                            val nome = "mira-backup-${java.text.SimpleDateFormat("yyyyMMdd-HHmm", java.util.Locale.getDefault()).format(java.util.Date())}.json"
                            exportLauncher.launch(nome)
                        },
                        onImportar = {
                            importLauncher.launch(arrayOf("application/json"))
                        },
                    )
                }
                composable("cronograma") {
                    TelaCronograma(vm = cronogramaVm, onVoltar = { nav.popBackStack() })
                }
            }
        }
    }

    if (mostrarSheet) {
        SheetAdicionar(
            vm = entradaVm,
            onFechar = { mostrarSheet = false },
            onAbrirConfig = { mostrarSheet = false; nav.navigate("config") },
        )
    }

    tarefaEditando?.let { tarefa ->
        SheetEditar(
            tarefa = tarefa,
            onFechar = { tarefaEditando = null },
            onSalvar = { atualizada ->
                scope.launch { repo.atualizarTarefa(atualizada) }
                tarefaEditando = null
            },
        )
    }
}
