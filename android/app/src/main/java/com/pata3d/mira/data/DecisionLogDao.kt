package com.pata3d.mira.data

import androidx.room.*

@Dao
interface DecisionLogDao {
    @Insert suspend fun inserir(d: DecisionLogDb): Long
    @Query("SELECT * FROM decision_logs ORDER BY createdAt DESC LIMIT 20")
    suspend fun listarRecentes(): List<DecisionLogDb>
    @Query("DELETE FROM decision_logs WHERE createdAt < :antes")
    suspend fun limparAntigos(antes: Long)
}
