package com.pata3d.mira.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Bloco do cronograma semanal. Define o estado/disponibilidade do usuário num intervalo.
 * diaSemana: 1=segunda ... 7=domingo (ISO).
 * horaInicio/horaFim: minutos desde a meia-noite (ex.: 07:00 = 420).
 * tipo: "trabalho" | "pausa" | "livre" | "dormir"
 * nivel: "indisponivel" | "baixa" | "media" | "alta"
 * contexto: "casa" | "oficina" | "fora" | "qualquer"
 */
@Entity(tableName = "blocos_disponibilidade")
data class BlocoDisponibilidade(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val diaSemana: Int,
    val horaInicio: Int,
    val horaFim: Int,
    val tipo: String,
    val nivel: String,
    val contexto: String = "qualquer",
)
