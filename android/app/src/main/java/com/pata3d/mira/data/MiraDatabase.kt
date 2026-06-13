package com.pata3d.mira.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Tarefa::class,
        EtapaTarefa::class,
        Historico::class,
        Memoria::class,
        ChatMessage::class,
        BlocoDisponibilidade::class,
    ],
    version = 4,
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

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Tipo e alerta
                db.execSQL("ALTER TABLE tarefas ADD COLUMN tipoTarefa TEXT NOT NULL DEFAULT 'SIMPLES'")
                db.execSQL("ALTER TABLE tarefas ADD COLUMN tipoAlerta TEXT NOT NULL DEFAULT 'NENHUM'")
                db.execSQL("ALTER TABLE tarefas ADD COLUMN nivelRisco TEXT NOT NULL DEFAULT 'NENHUM'")
                db.execSQL("ALTER TABLE tarefas ADD COLUMN contextoNecessario TEXT NOT NULL DEFAULT 'QUALQUER'")
                // Timestamps (nullable)
                db.execSQL("ALTER TABLE tarefas ADD COLUMN prazoEm INTEGER")
                db.execSQL("ALTER TABLE tarefas ADD COLUMN compromissoEm INTEGER")
                db.execSQL("ALTER TABLE tarefas ADD COLUMN lembreteEm INTEGER")
                db.execSQL("ALTER TABLE tarefas ADD COLUMN alarmeEm INTEGER")
                db.execSQL("ALTER TABLE tarefas ADD COLUMN inicioAgendadoEm INTEGER")
                // Proteção
                db.execSQL("ALTER TABLE tarefas ADD COLUMN protegerPrazo INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE tarefas ADD COLUMN microPasso TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE tarefas ADD COLUMN ultimaNotificacaoEm INTEGER")
                // Rastreamento
                db.execSQL("ALTER TABLE tarefas ADD COLUMN iniciadaEm INTEGER")
                db.execSQL("ALTER TABLE tarefas ADD COLUMN concluidaEm INTEGER")
                // Máquina/produção
                db.execSQL("ALTER TABLE tarefas ADD COLUMN isTarefaMaquina INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE tarefas ADD COLUMN tempoPrepMin INTEGER")
                db.execSQL("ALTER TABLE tarefas ADD COLUMN tempoMaquinaMin INTEGER")
                db.execSQL("ALTER TABLE tarefas ADD COLUMN tempoSecagemMin INTEGER")
                db.execSQL("ALTER TABLE tarefas ADD COLUMN tempoFinalMin INTEGER")
            }
        }

        fun get(context: Context): MiraDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                MiraDatabase::class.java,
                "mira.db",
            )
                .addMigrations(MIGRATION_3_4)
                .fallbackToDestructiveMigration()
                .build()
                .also { INSTANCE = it }
        }
    }
}
