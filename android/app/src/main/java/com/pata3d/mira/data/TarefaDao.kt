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
}
