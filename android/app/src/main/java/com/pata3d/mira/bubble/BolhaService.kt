package com.pata3d.mira.bubble

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.pata3d.mira.MainActivity
import com.pata3d.mira.MiraApplication
import com.pata3d.mira.R
import com.pata3d.mira.domain.scoreTarefa
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.math.abs

class BolhaService : Service() {

    private lateinit var wm: WindowManager
    private var bolhaView: View? = null
    private var painelView: View? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        iniciarForeground()
        mostrarBolha()
    }

    override fun onDestroy() {
        isRunning = false
        scope.cancel()
        runCatching { bolhaView?.let { wm.removeView(it) } }
        runCatching { painelView?.let { wm.removeView(it) } }
        super.onDestroy()
    }

    private fun iniciarForeground() {
        val canal = "mira_bolha"
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(canal, "Bolha Mira", NotificationManager.IMPORTANCE_MIN).apply {
                setShowBadge(false)
            }
        )
        val notif = NotificationCompat.Builder(this, canal)
            .setSmallIcon(R.drawable.ic_notif)
            .setContentTitle("Mira ativa")
            .setContentText("Toque na bolha para ver suas tarefas")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .build()
        startForeground(3, notif)
    }

    private fun mostrarBolha() {
        val bolha = criarBolha()
        val params = WindowManager.LayoutParams(
            dpToPx(56), dpToPx(56),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = dpToPx(16)
            y = dpToPx(200)
        }

        var inicioX = 0f; var inicioY = 0f
        var paramX = 0; var paramY = 0
        var moveu = false

        bolha.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    inicioX = event.rawX; inicioY = event.rawY
                    paramX = params.x; paramY = params.y
                    moveu = false; true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - inicioX
                    val dy = event.rawY - inicioY
                    if (abs(dx) > 8 || abs(dy) > 8) {
                        moveu = true
                        params.x = (paramX + dx).toInt()
                        params.y = (paramY + dy).toInt()
                        wm.updateViewLayout(bolha, params)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!moveu) togglePainel(bolha)
                    true
                }
                else -> false
            }
        }

        wm.addView(bolha, params)
        bolhaView = bolha
    }

    private fun criarBolha(): View {
        val view = FrameLayout(this)

        val bg = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor("#0B1221"))
            setStroke(dpToPx(2), Color.parseColor("#1E3A5F"))
        }
        view.background = bg
        view.clipToOutline = true

        val icon = ImageView(this).apply {
            setImageResource(R.mipmap.ic_launcher)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        view.addView(icon, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
        ))

        view.elevation = 8f
        return view
    }

    private fun togglePainel(ancora: View) {
        if (painelView != null) {
            fecharPainel()
            return
        }
        abrirPainel()
    }

    private fun abrirPainel() {
        val repo = (applicationContext as MiraApplication).repository
        val painel = criarPainel()

        val bParams = (bolhaView?.layoutParams as? WindowManager.LayoutParams)
        val px = bParams?.x ?: dpToPx(16)
        val py = bParams?.y ?: dpToPx(200)

        // Posiciona à direita da bolha; se não couber, vai para esquerda
        val largura = dpToPx(240)
        val telaLargura = wm.currentWindowMetrics.bounds.width()
        val xPainel = if (px + dpToPx(64) + largura < telaLargura) px + dpToPx(64) else px - largura - dpToPx(8)

        val params = WindowManager.LayoutParams(
            largura, WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = xPainel
            y = py
        }

        wm.addView(painel, params)
        painelView = painel

        scope.launch {
            val agora = System.currentTimeMillis()
            val tarefas = repo.listarAbertas()
                .filter { it.adiarAte == null || agora >= (it.adiarAte ?: 0L) }
                .sortedByDescending { scoreTarefa(it) }
                .take(5)

            val listaContainer = painel.findViewWithTag<android.widget.LinearLayout>("lista")
            val loading = painel.findViewWithTag<TextView>("loading")
            loading?.visibility = android.view.View.GONE

            if (tarefas.isEmpty()) {
                val tv = TextView(this@BolhaService).apply {
                    text = "Nenhuma tarefa para hoje 🎉"
                    textSize = 13f
                    setTextColor(Color.parseColor("#94A3B8"))
                    setPadding(0, dpToPx(4), 0, dpToPx(4))
                }
                listaContainer?.addView(tv)
            } else {
                tarefas.forEach { tarefa ->
                    val linha = android.widget.LinearLayout(this@BolhaService).apply {
                        orientation = android.widget.LinearLayout.HORIZONTAL
                        setPadding(0, dpToPx(6), 0, dpToPx(6))
                    }
                    val check = TextView(this@BolhaService).apply {
                        text = "✓"
                        textSize = 16f
                        setTextColor(Color.parseColor("#22C55E"))
                        setPadding(0, 0, dpToPx(10), 0)
                        setOnClickListener {
                            scope.launch {
                                repo.concluirTarefa(tarefa.id)
                                listaContainer?.removeView(linha)
                                com.pata3d.mira.notification.CheckInWorker.dispararAgora(applicationContext)
                            }
                        }
                    }
                    val titulo = TextView(this@BolhaService).apply {
                        text = tarefa.titulo
                        textSize = 13f
                        setTextColor(Color.WHITE)
                        maxLines = 2
                        ellipsize = android.text.TextUtils.TruncateAt.END
                        layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    }
                    linha.addView(check)
                    linha.addView(titulo)
                    listaContainer?.addView(linha)

                    // divisor fino
                    listaContainer?.addView(View(this@BolhaService).apply {
                        setBackgroundColor(Color.parseColor("#1E3A5F"))
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 1
                        )
                    })
                }
            }
        }
    }

    private fun fecharPainel() {
        painelView?.let { wm.removeView(it) }
        painelView = null
    }

    private fun criarPainel(): View {
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(dpToPx(14), dpToPx(12), dpToPx(14), dpToPx(12))
            elevation = 12f
        }
        layout.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dpToPx(18).toFloat()
            setColor(Color.parseColor("#0F172A"))
            setStroke(dpToPx(1), Color.parseColor("#1E3A5F"))
        }

        // Cabeçalho
        layout.addView(TextView(this).apply {
            text = "● HOJE"
            textSize = 10f
            setTextColor(Color.parseColor("#0EA5E9"))
            letterSpacing = 0.1f
            setPadding(0, 0, 0, dpToPx(8))
        })

        // Loading placeholder
        layout.addView(TextView(this).apply {
            tag = "loading"
            text = "Carregando..."
            textSize = 12f
            setTextColor(Color.parseColor("#94A3B8"))
        })

        // Container das tarefas (populado depois de forma assíncrona)
        layout.addView(android.widget.LinearLayout(this).apply {
            tag = "lista"
            orientation = android.widget.LinearLayout.VERTICAL
        })

        divider(layout)

        // Linha de ações rápidas: + Tarefa | 💬 IA
        val wc = android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        val acaoRow = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            weightSum = 2f
            setPadding(0, dpToPx(8), 0, dpToPx(4))
        }
        val btnTarefa = TextView(this).apply {
            tag = "btn_tarefa"
            text = "+ Tarefa"
            textSize = 12f
            setTextColor(Color.parseColor("#22C55E"))
            gravity = Gravity.CENTER
            setPadding(0, dpToPx(6), 0, dpToPx(6))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(8).toFloat()
                setStroke(dpToPx(1), Color.parseColor("#22C55E"))
            }
            setOnClickListener {
                fecharPainel()
                startActivity(Intent(this@BolhaService, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra("abrir_sheet", true)
                })
            }
        }
        val btnIA = TextView(this).apply {
            tag = "btn_ia"
            text = "💬 IA"
            textSize = 12f
            setTextColor(Color.parseColor("#0EA5E9"))
            gravity = Gravity.CENTER
            setPadding(0, dpToPx(6), 0, dpToPx(6))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(8).toFloat()
                setStroke(dpToPx(1), Color.parseColor("#0EA5E9"))
            }
            setOnClickListener {
                fecharPainel()
                startActivity(Intent(this@BolhaService, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra("abrir_chat", true)
                })
            }
        }
        acaoRow.addView(btnTarefa, android.widget.LinearLayout.LayoutParams(0, wc, 1f).apply { marginEnd = dpToPx(6) })
        acaoRow.addView(btnIA, android.widget.LinearLayout.LayoutParams(0, wc, 1f))
        layout.addView(acaoRow)

        divider(layout)

        // Abrir Mira →
        layout.addView(TextView(this).apply {
            text = "Abrir Mira →"
            textSize = 11f
            setTextColor(Color.parseColor("#0EA5E9"))
            gravity = Gravity.END
            setPadding(0, dpToPx(6), 0, 0)
            setOnClickListener {
                fecharPainel()
                startActivity(Intent(this@BolhaService, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                })
            }
        })

        return layout
    }

    private fun divider(parent: android.widget.LinearLayout) {
        parent.addView(View(this).apply {
            setBackgroundColor(Color.parseColor("#1E3A5F"))
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1)
            )
        })
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()

    companion object {
        @Volatile var isRunning = false

        fun iniciar(ctx: Context) {
            val intent = Intent(ctx, BolhaService::class.java)
            ctx.startForegroundService(intent)
        }

        fun parar(ctx: Context) {
            ctx.stopService(Intent(ctx, BolhaService::class.java))
        }
    }
}
