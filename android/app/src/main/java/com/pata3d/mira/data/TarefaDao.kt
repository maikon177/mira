package com.pata3d.mira.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TarefaDao {
    @Query("SELECT * FROM tarefas ORDER BY criadaEm DESC")
    fun observarTodas(): Flow<List<Tarefa>>

    @Query("SELECT * FROM tarefas ORDER BY criadaEm DESC")
    suspend fun listarTodas(): List<Tarefa>

    @Query("SELECT * FROM tarefas WHERE id = :id")
    suspend fun obter(id: String): Tarefa?

    @Query("SELECT * FROM tarefas WHERE status NOT IN ('concluida','cancelada')")
    suspend fun listarAbertas(): List<Tarefa>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(tarefa: Tarefa)

    @Update
    suspend fun atualizar(tarefa: Tarefa)

    @Query("DELETE FROM tarefas WHERE id = :id")
    suspend fun deletar(id: String)

    @Query("SELECT * FROM tarefas WHERE status = 'aberta' AND prazoEm IS NOT NULL ORDER BY prazoEm ASC")
    fun observarComPrazo(): Flow<List<Tarefa>>

    @Query("SELECT * FROM tarefas WHERE status = 'aberta' AND isTarefaMaquina = 1 ORDER BY prazoEm ASC")
    fun observarMaquina(): Flow<List<Tarefa>>

    @Query("UPDATE tarefas SET nivelRisco = :nivel, ultimaNotificacaoEm = :agora, atualizadaEm = :agora WHERE id = :id")
    suspend fun atualizarRisco(id: String, nivel: String, agora: Long)

    @Query("UPDATE tarefas SET iniciadaEm = :agora, atualizadaEm = :agora WHERE id = :id AND iniciadaEm IS NULL")
    suspend fun marcarIniciada(id: String, agora: Long)

    @Query("SELECT * FROM tarefas WHERE status = 'aberta' AND compromissoEm IS NOT NULL AND compromissoEm > :agora ORDER BY compromissoEm ASC")
    suspend fun listarCompromissosFuturos(agora: Long): List<Tarefa>

    @Query("SELECT * FROM tarefas WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: String): Tarefa?
}
