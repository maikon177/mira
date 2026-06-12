package com.pata3d.mira.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EtapaDao {
    @Query("SELECT * FROM etapas_tarefa ORDER BY tarefaId, ordem ASC")
    fun observarTodas(): Flow<List<EtapaTarefa>>

    @Query("SELECT * FROM etapas_tarefa WHERE tarefaId = :tarefaId ORDER BY ordem ASC")
    suspend fun listarDaTarefa(tarefaId: String): List<EtapaTarefa>

    @Query("SELECT * FROM etapas_tarefa WHERE id = :id")
    suspend fun obter(id: String): EtapaTarefa?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(etapa: EtapaTarefa)

    @Update
    suspend fun atualizar(etapa: EtapaTarefa)

    @Query("DELETE FROM etapas_tarefa WHERE tarefaId = :tarefaId")
    suspend fun deletarDaTarefa(tarefaId: String)
}
