package com.pata3d.mira.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Tarefa::class,
        EtapaTarefa::class,
        Historico::class,
        Memoria::class,
        ChatMessage::class,
        BlocoDisponibilidade::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class MiraDatabase : RoomDatabase() {
    abstract fun tarefaDao(): TarefaDao
    abstract fun etapaDao(): EtapaDao
    abstract fun historicoDao(): HistoricoDao
    abstract fun memoriaDao(): MemoriaDao
    abstract fun chatDao(): ChatDao
    abstract fun disponibilidadeDao(): DisponibilidadeDao

    companion object {
        @Volatile private var INSTANCE: MiraDatabase? = null

        fun get(context: Context): MiraDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                MiraDatabase::class.java,
                "mira.db",
            )
                .fallbackToDestructiveMigration()
                .build()
                .also { INSTANCE = it }
        }
    }
}
