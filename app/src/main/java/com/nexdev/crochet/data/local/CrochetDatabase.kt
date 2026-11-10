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
import com.nexdev.crochet.data.local.model.SeedCatalogDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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

        private val jsonParser = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

        fun getDatabase(context: Context): CrochetDatabase {
            return INSTANCE ?: synchronized(this) {
                val appContext = context.applicationContext
                val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
                val instance = Room.databaseBuilder(
                    appContext,
                    CrochetDatabase::class.java,
                    "crochet_funny.db"
                )
                    .addCallback(SeedDatabaseCallback(appContext, scope))
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                scope.launch {
                    seedIfNeeded(appContext, instance)
                }
                instance
            }
        }

        private class SeedDatabaseCallback(
            private val context: Context,
            private val scope: CoroutineScope
        ) : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch {
                        seedIfNeeded(context, database)
                    }
                }
            }
        }

        suspend fun seedIfNeeded(context: Context, database: CrochetDatabase) {
            val patternDao = database.patternDao()
            if (patternDao.getPatternCount() > 0) return

            runCatching {
                val jsonString = context.assets.open("patterns_seed.json")
                    .bufferedReader()
                    .use { it.readText() }

                val seedCatalog = jsonParser.decodeFromString<SeedCatalogDto>(jsonString)

                val patternEntities = mutableListOf<PatternEntity>()
                val partEntities = mutableListOf<PartEntity>()
                val roundEntities = mutableListOf<RoundEntity>()

                seedCatalog.patrones.forEach { patDto ->
                    patternEntities.add(
                        PatternEntity(
                            id = patDto.id,
                            nombre = patDto.nombre,
                            icono = patDto.icono,
                            colorBg = patDto.colorBg,
                            categoria = patDto.categoria,
                            dificultad = patDto.dificultad,
                            tiempoEstimadoMin = patDto.tiempoEstimadoMin,
                            hiloRecomendado = patDto.hiloRecomendado,
                            agujaMm = patDto.agujaMm,
                            extrasJson = jsonParser.encodeToString(patDto.extras),
                            notasEnsamblaje = patDto.notasEnsamblaje,
                            esPersonalizado = false
                        )
                    )

                    patDto.piezas.forEachIndexed { index, partDto ->
                        partEntities.add(
                            PartEntity(
                                idPieza = partDto.idPieza,
                                patternId = patDto.id,
                                orden = index,
                                nombre = partDto.nombre,
                                cantidadRequerida = partDto.cantidadRequerida,
                                notasPieza = partDto.notasPieza
                            )
                        )

                        partDto.vueltas.forEach { roundDto ->
                            roundEntities.add(
                                RoundEntity(
                                    partId = partDto.idPieza,
                                    numero = roundDto.numero,
                                    instruccion = roundDto.instruccion,
                                    puntosTotales = roundDto.puntosTotales,
                                    repeticiones = roundDto.repeticiones
                                )
                            )
                        }
                    }
                }

                patternDao.insertPatterns(patternEntities)
                patternDao.insertParts(partEntities)
                patternDao.insertRounds(roundEntities)
            }
        }
    }
}
