package com.pata3d.mira.data

import androidx.room.*

@Dao
interface HistoricoDao {
    @Insert
    suspend fun inserir(historico: Historico)

    @Query("SELECT * FROM historico WHERE em >= :desde ORDER BY em DESC")
    suspend fun desde(desde: Long): List<Historico>
}
