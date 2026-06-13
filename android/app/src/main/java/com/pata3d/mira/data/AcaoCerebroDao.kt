package com.pata3d.mira.data

import androidx.room.*

@Dao
interface AcaoCerebroDao {
    @Insert suspend fun inserir(a: AcaoCerebroDb): Long
    @Query("SELECT * FROM ai_actions WHERE status = 'pending' ORDER BY createdAt DESC")
    suspend fun listarPendentes(): List<AcaoCerebroDb>
    @Query("UPDATE ai_actions SET status = :status, executedAt = :at WHERE id = :id")
    suspend fun atualizarStatus(id: Long, status: String, at: Long)
}
