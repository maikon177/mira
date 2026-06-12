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
import androidx.core.content.ContextCompat
import com.pata3d.mira.ui.MiraApp
import com.pata3d.mira.ui.theme.MiraTheme

class MainActivity : ComponentActivity() {

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
                pedirPermissaoNotif.launch(Manifest.permission.POST_NOTIFICATIONS)
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
            }
        }
    }
}
