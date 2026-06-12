package com.pata3d.mira.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DisponibilidadeDao {
    @Query("SELECT * FROM blocos_disponibilidade ORDER BY diaSemana, horaInicio")
    fun observarTodos(): Flow<List<BlocoDisponibilidade>>

    @Query("SELECT * FROM blocos_disponibilidade ORDER BY diaSemana, horaInicio")
    suspend fun listarTodos(): List<BlocoDisponibilidade>

    @Query("SELECT * FROM blocos_disponibilidade WHERE diaSemana = :dia ORDER BY horaInicio")
    suspend fun doDia(dia: Int): List<BlocoDisponibilidade>

    @Insert
    suspend fun inserir(bloco: BlocoDisponibilidade): Long

    @Update
    suspend fun atualizar(bloco: BlocoDisponibilidade)

    @Query("DELETE FROM blocos_disponibilidade WHERE id = :id")
    suspend fun deletar(id: Long)

    @Query("SELECT COUNT(*) FROM blocos_disponibilidade")
    suspend fun contar(): Int
}
