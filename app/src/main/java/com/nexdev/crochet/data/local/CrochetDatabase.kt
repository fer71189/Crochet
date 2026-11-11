package com.nexdev.crochet.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nexdev.crochet.data.local.dao.HistoryDao
import com.nexdev.crochet.data.local.dao.PatternDao
import com.nexdev.crochet.data.local.dao.ProgressDao
import com.nexdev.crochet.data.local.entity.ActiveProgressEntity
import com.nexdev.crochet.data.local.entity.CompletedProjectEntity
import com.nexdev.crochet.data.local.entity.PartEntity
import com.nexdev.crochet.data.local.entity.PatternEntity
import com.nexdev.crochet.data.local.entity.RoundEntity

@Database(
    entities = [
        PatternEntity::class,
        PartEntity::class,
        RoundEntity::class,
        ActiveProgressEntity::class,
        CompletedProjectEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CrochetDatabase : RoomDatabase() {

    abstract fun patternDao(): PatternDao
    abstract fun progressDao(): ProgressDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: CrochetDatabase? = null

        fun getDatabase(context: Context): CrochetDatabase {
            return INSTANCE ?: synchronized(this) {
                val appContext = context.applicationContext
                val instance = Room.databaseBuilder(
                    appContext,
                    CrochetDatabase::class.java,
                    "crochet_funny.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
