package com.pata3d.mira.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_mensagens ORDER BY criadaEm ASC")
    fun observarTodas(): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chat_mensagens ORDER BY criadaEm DESC LIMIT :limite")
    suspend fun ultimas(limite: Int): List<ChatMessage>

    @Insert
    suspend fun inserir(msg: ChatMessage): Long

    @Query("DELETE FROM chat_mensagens")
    suspend fun limparTudo()
}
