package com.pata3d.mira.data

import androidx.room.*

@Dao
interface MemoriaDao {
    @Query("SELECT * FROM memoria ORDER BY atualizadaEm DESC")
    suspend fun listarTodas(): List<Memoria>

    @Query("SELECT * FROM memoria WHERE isActive = 1 ORDER BY atualizadaEm DESC")
    suspend fun listarAtivas(): List<Memoria>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(memoria: Memoria)

    @Update
    suspend fun atualizar(memoria: Memoria)

    @Query("DELETE FROM memoria WHERE id = :id")
    suspend fun deletar(id: String)
}
