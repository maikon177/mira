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

    @Query("SELECT COUNT(*) FROM chat_mensagens")
    suspend fun contar(): Int

    // Apaga as mensagens mais antigas além do limite
    @Query("DELETE FROM chat_mensagens WHERE id NOT IN (SELECT id FROM chat_mensagens ORDER BY criadaEm DESC LIMIT :manter)")
    suspend fun aparar(manter: Int)
}
