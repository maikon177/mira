package com.pata3d.mira.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_mensagens")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String,            // "user" | "assistant"
    val content: String,
    /** JSON das ações propostas pela IA (criar/editar/agendar/concluir). Null se não houver. */
    val acoesJson: String? = null,
    val criadaEm: Long = System.currentTimeMillis(),
)
