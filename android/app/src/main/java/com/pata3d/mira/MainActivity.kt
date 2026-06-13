package com.pata3d.mira

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import com.pata3d.mira.ui.MiraApp
import com.pata3d.mira.ui.theme.MiraTheme

class MainActivity : ComponentActivity() {

    private val mostrarDialogNotif = mutableStateOf(false)

    private val pedirPermissaoNotif = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    private val pedirOverlay = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                mostrarDialogNotif.value = true
            }
        }

        val repo = (application as MiraApplication).repository
        setContent {
            MiraTheme {
                MiraApp(
                    repo = repo,
                    onPedirOverlay = {
                        if (!Settings.canDrawOverlays(this)) {
                            pedirOverlay.launch(
                                Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:$packageName")
                                )
                            )
                        }
                    },
                    temPermissaoOverlay = { Settings.canDrawOverlays(this) },
                )
                if (mostrarDialogNotif.value) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { mostrarDialogNotif.value = false },
                        title = { androidx.compose.material3.Text("Receber alertas?") },
                        text = {
                            androidx.compose.material3.Text(
                                "O Mira usa notificações para avisar quando um prazo está próximo, " +
                                "quando uma máquina precisa de atenção e para os check-ins do dia. " +
                                "Sem isso, os lembretes não chegam."
                            )
                        },
                        confirmButton = {
                            androidx.compose.material3.TextButton(onClick = {
                                mostrarDialogNotif.value = false
                                pedirPermissaoNotif.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }) { androidx.compose.material3.Text("Permitir") }
                        },
                        dismissButton = {
                            androidx.compose.material3.TextButton(onClick = { mostrarDialogNotif.value = false }) {
                                androidx.compose.material3.Text("Agora não")
                            }
                        },
                    )
                }
            }
        }
    }
}
