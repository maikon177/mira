package com.pata3d.mira.data

import android.content.Context

class MiraPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("mira_prefs", Context.MODE_PRIVATE)

    // ── Chave DeepSeek ──
    fun getDeepSeekKey(): String = prefs.getString("deepseek_key", "") ?: ""
    fun setDeepSeekKey(key: String) = prefs.edit().putString("deepseek_key", key).apply()
    fun hasKey(): Boolean = getDeepSeekKey().isNotBlank()

    // ── Notificações ──
    var notificacoesAtivas: Boolean
        get() = prefs.getBoolean("notif_ativas", true)
        set(v) = prefs.edit().putBoolean("notif_ativas", v).apply()

    /** Hora do lembrete matinal em minutos desde meia-noite (padrão 07:30 = 450). */
    var lembreteManhaMin: Int
        get() = prefs.getInt("lembrete_manha", 450)
        set(v) = prefs.edit().putInt("lembrete_manha", v).apply()

    var silencioInicioMin: Int
        get() = prefs.getInt("silencio_inicio", 22 * 60)
        set(v) = prefs.edit().putInt("silencio_inicio", v).apply()

    var silencioFimMin: Int
        get() = prefs.getInt("silencio_fim", 7 * 60)
        set(v) = prefs.edit().putInt("silencio_fim", v).apply()

    // ── Tema ──
    /** "escuro" | "claro" | "sistema" */
    var tema: String
        get() = prefs.getString("tema", "escuro") ?: "escuro"
        set(v) = prefs.edit().putString("tema", v).apply()

    // ── Sugestões ──
    var maxSugestoes: Int
        get() = prefs.getInt("max_sugestoes", 3)
        set(v) = prefs.edit().putInt("max_sugestoes", v).apply()

    var sugestaoDestravarAtiva: Boolean
        get() = prefs.getBoolean("sug_destravar", true)
        set(v) = prefs.edit().putBoolean("sug_destravar", v).apply()

    // ── Modo avançado ──
    var modoAvancado: Boolean
        get() = prefs.getBoolean("modo_avancado", false)
        set(v) = prefs.edit().putBoolean("modo_avancado", v).apply()

    // ── Seed do cronograma padrão (uma vez) ──
    var cronogramaSemeado: Boolean
        get() = prefs.getBoolean("cronograma_seed", false)
        set(v) = prefs.edit().putBoolean("cronograma_seed", v).apply()

    // ── Bolha flutuante ──
    var bolhaAtiva: Boolean
        get() = prefs.getBoolean("bolha_ativa", false)
        set(v) = prefs.edit().putBoolean("bolha_ativa", v).apply()
}
