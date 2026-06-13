package com.pata3d.mira.data

import androidx.room.*

@Dao
interface SugestaoDao {
    @Insert suspend fun inserir(s: SugestaoDb): Long
    @Update suspend fun atualizar(s: SugestaoDb)
    @Query("SELECT * FROM suggestions WHERE status = 'pending' ORDER BY createdAt DESC LIMIT 10")
    suspend fun listarPendentes(): List<SugestaoDb>
    @Query("UPDATE suggestions SET status = :status WHERE id = :id")
    suspend fun atualizarStatus(id: Long, status: String)
    @Query("DELETE FROM suggestions WHERE createdAt < :antes")
    suspend fun limparAntigos(antes: Long)
}
