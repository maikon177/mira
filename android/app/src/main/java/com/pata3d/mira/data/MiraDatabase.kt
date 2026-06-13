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
        SugestaoDb::class,
        AcaoCerebroDb::class,
        DecisionLogDb::class,
    ],
    version = 5,
    exportSchema = false,
)
abstract class MiraDatabase : RoomDatabase() {
    abstract fun tarefaDao(): TarefaDao
    abstract fun etapaDao(): EtapaDao
    abstract fun historicoDao(): HistoricoDao
    abstract fun memoriaDao(): MemoriaDao
    abstract fun chatDao(): ChatDao
    abstract fun disponibilidadeDao(): DisponibilidadeDao
    abstract fun sugestaoDao(): SugestaoDao
    abstract fun acaoCerebroDao(): AcaoCerebroDao
    abstract fun decisionLogDao(): DecisionLogDao

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

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS suggestions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        type TEXT NOT NULL, title TEXT NOT NULL, message TEXT NOT NULL,
                        relatedTaskId TEXT, urgency INTEGER NOT NULL, usefulness INTEGER NOT NULL,
                        annoyanceRisk INTEGER NOT NULL, status TEXT NOT NULL DEFAULT 'pending',
                        createdAt INTEGER NOT NULL, expiresAt INTEGER NOT NULL )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS ai_actions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        intent TEXT NOT NULL, payloadJson TEXT NOT NULL DEFAULT '{}',
                        requiresConfirmation INTEGER NOT NULL, permissionResult TEXT NOT NULL,
                        status TEXT NOT NULL DEFAULT 'pending', createdAt INTEGER NOT NULL,
                        executedAt INTEGER )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS decision_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        selectedTaskId TEXT NOT NULL, reason TEXT NOT NULL,
                        scoreJson TEXT NOT NULL DEFAULT '{}', contextJson TEXT NOT NULL DEFAULT '{}',
                        createdAt INTEGER NOT NULL )
                """.trimIndent())
            }
        }

        fun get(context: Context): MiraDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                MiraDatabase::class.java,
                "mira.db",
            )
                .addMigrations(MIGRATION_3_4, MIGRATION_4_5)
                // SEM fallbackToDestructiveMigration: se faltar uma migração, o Room lança
                // IllegalStateException (crash visível) em vez de APAGAR os dados do usuário
                // em silêncio. Toda mudança de schema exige um novo MIGRATION_N_(N+1) acima.
                .build()
                .also { INSTANCE = it }
        }
    }
}
