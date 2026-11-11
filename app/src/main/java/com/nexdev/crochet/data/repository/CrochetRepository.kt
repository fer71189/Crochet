package com.nexdev.crochet.data.repository

import com.nexdev.crochet.data.local.dao.HistoryDao
import com.nexdev.crochet.data.local.dao.PatternDao
import com.nexdev.crochet.data.local.dao.ProgressDao
import com.nexdev.crochet.data.local.entity.ActiveProgressEntity
import com.nexdev.crochet.data.local.entity.CompletedProjectCount
import com.nexdev.crochet.data.local.entity.CompletedProjectEntity
import com.nexdev.crochet.data.local.entity.PartEntity
import com.nexdev.crochet.data.local.entity.PatternEntity
import com.nexdev.crochet.data.local.entity.PatternWithPartsAndRounds
import com.nexdev.crochet.data.local.entity.RoundEntity
import com.nexdev.crochet.api.RetrofitClient
import com.nexdev.crochet.api.model.Patron
import com.nexdev.crochet.api.model.Pieza
import com.nexdev.crochet.api.model.Vuelta
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow

class CrochetRepository(
    private val patternDao: PatternDao,
    private val progressDao: ProgressDao,
    private val historyDao: HistoryDao
) {

    // --- Catálogo de Patrones ---

    fun observeAllPatterns(): Flow<List<PatternWithPartsAndRounds>> =
        patternDao.observeAllPatternsWithDetails()

    fun observePatternById(patternId: String): Flow<PatternWithPartsAndRounds?> =
        patternDao.observePatternById(patternId)

    private fun mapWordToEmoji(word: String): String {
        val normalized = word.trim().lowercase()
        return when {
            normalized.contains("pulpo") -> "🐙"
            normalized.contains("angel") -> "👼"
            normalized.contains("rana") -> "🐸"
            normalized.contains("oso") -> "🐻"
            normalized.contains("gato") -> "🐱"
            normalized.contains("perro") -> "🐶"
            normalized.contains("conejo") -> "🐰"
            normalized.contains("unicornio") -> "🦄"
            normalized.contains("zorro") -> "🦊"
            normalized.contains("leon") || normalized.contains("león") -> "🦁"
            normalized.contains("vaca") -> "🐮"
            normalized.contains("cerdito") || normalized.contains("cerdo") -> "🐷"
            normalized.contains("mono") || normalized.contains("chango") -> "🐵"
            normalized.contains("corazon") || normalized.contains("corazón") -> "❤️"
            normalized.contains("estrella") -> "⭐"
            normalized.contains("flor") -> "🌸"
            normalized.contains("sol") -> "☀️"
            normalized.contains("luna") -> "🌙"
            normalized.contains("pajaro") || normalized.contains("pájaro") || normalized.contains("ave") -> "🐦"
            normalized.contains("patito") || normalized.contains("pato") -> "🦆"
            normalized.contains("abeja") -> "🐝"
            normalized.contains("mariposa") -> "🦋"
            normalized.contains("caracol") -> "🐌"
            normalized.contains("elefante") -> "🐘"
            normalized.contains("dinosaurio") -> "🦖"
            normalized.contains("jirafa") -> "🦒"
            normalized.contains("tortuga") -> "🐢"
            normalized.contains("ballena") -> "🐳"
            normalized.contains("pinguino") || normalized.contains("pingüino") -> "🐧"
            normalized.contains("sirena") -> "🧜‍♀️"
            normalized.contains("muneca") || normalized.contains("muñeca") -> "👧"
            normalized.contains("sonajero") -> "🪇"
            normalized.contains("manta") -> "🧶"
            normalized.contains("calcetin") || normalized.contains("calcetín") -> "🧦"
            normalized.contains("gorro") -> "🧢"
            normalized.length <= 2 -> word // Si es un emoji (1-2 caracteres de largo), lo devolvemos tal cual
            else -> "🧶" // Por defecto, una bolita de estambre
        }
    }

    // Función inversa para convertir el emoji de nuevo en texto antes de enviarlo al servidor
    private fun mapEmojiToWord(emoji: String): String {
        return when (emoji) {
            "🐙" -> "pulpo"
            "👼" -> "angel"
            "🐸" -> "rana"
            "🐻" -> "oso"
            "🐱" -> "gato"
            "🐶" -> "perro"
            "🐰" -> "conejo"
            "🦄" -> "unicornio"
            "🦊" -> "zorro"
            "🦁" -> "leon"
            "🐮" -> "vaca"
            "🐷" -> "cerdo"
            "🐵" -> "mono"
            "❤️" -> "corazon"
            "⭐" -> "estrella"
            "🌸" -> "flor"
            "☀️" -> "sol"
            "🌙" -> "luna"
            "🐦" -> "pajaro"
            "🦆" -> "pato"
            "🐝" -> "abeja"
            "🦋" -> "mariposa"
            "🐌" -> "caracol"
            "🐘" -> "elefante"
            "🦖" -> "dinosaurio"
            "🦒" -> "jirafa"
            "🐢" -> "tortuga"
            "🐳" -> "ballena"
            "🐧" -> "pinguino"
            "🧜‍♀️" -> "sirena"
            "👧" -> "muneca"
            "🪇" -> "sonajero"
            "🧦" -> "calcetin"
            "🧢" -> "gorro"
            "🧶" -> "manta"
            else -> "manta" // Equivalente a la bolita de estambre
        }
    }

    suspend fun syncWithApi(): Boolean {
        return try {
            android.util.Log.d("API_SYNC", "Iniciando descarga de patrones...")
            val response = RetrofitClient.instance.getCatalogo()
            android.util.Log.d("API_SYNC", "Patrones descargados: ${response.patrones.size}")
            
            val patternEntities = mutableListOf<PatternEntity>()
            val partEntities = mutableListOf<PartEntity>()
            val roundEntities = mutableListOf<RoundEntity>()
            val gson = Gson()

            response.patrones.forEach { patDto ->
                patternEntities.add(
                    PatternEntity(
                        id = patDto.id,
                        nombre = patDto.nombre,
                        icono = mapWordToEmoji(patDto.icono),
                        colorBg = patDto.colorBg,
                        categoria = patDto.categoria,
                        dificultad = patDto.dificultad,
                        tiempoEstimadoMin = patDto.tiempoEstimadoMin,
                        hiloRecomendado = patDto.hiloRecomendado,
                        agujaMm = patDto.agujaMm,
                        extrasJson = gson.toJson(patDto.extras),
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

            android.util.Log.d("API_SYNC", "Guardando en DB local...")
            patternDao.insertPatterns(patternEntities)
            patternDao.insertParts(partEntities)
            patternDao.insertRounds(roundEntities)
            android.util.Log.d("API_SYNC", "Guardado exitoso.")
            true
        } catch (e: Exception) {
            android.util.Log.e("API_SYNC", "Error al sincronizar con API", e)
            false
        }
    }

    suspend fun getPatternById(patternId: String): PatternWithPartsAndRounds? =
        patternDao.getPatternById(patternId)

    suspend fun saveCustomPattern(
        pattern: PatternEntity,
        parts: List<PartEntity>,
        rounds: List<RoundEntity>
    ) {
        patternDao.insertFullPattern(pattern, parts, rounds)
    }

    suspend fun updatePattern(
        pattern: PatternEntity,
        parts: List<PartEntity>,
        rounds: List<RoundEntity>
    ) {
        patternDao.updateFullPattern(pattern, parts, rounds)
    }

    suspend fun enviarPatronAAprobacion(patternId: String): Boolean {
        return try {
            val fullPattern = patternDao.getPatternById(patternId) ?: return false
            
            val gson = Gson()
            val type = object : TypeToken<List<String>>() {}.type
            val extrasList: List<String> = gson.fromJson(fullPattern.pattern.extrasJson, type) ?: emptyList()

            val patronParaApi = Patron(
                id = fullPattern.pattern.id,
                nombre = fullPattern.pattern.nombre,
                icono = mapEmojiToWord(fullPattern.pattern.icono),
                colorBg = fullPattern.pattern.colorBg,
                categoria = fullPattern.pattern.categoria,
                dificultad = fullPattern.pattern.dificultad,
                tiempoEstimadoMin = fullPattern.pattern.tiempoEstimadoMin,
                hiloRecomendado = fullPattern.pattern.hiloRecomendado,
                agujaMm = fullPattern.pattern.agujaMm,
                extras = extrasList,
                notasEnsamblaje = fullPattern.pattern.notasEnsamblaje,
                piezas = fullPattern.sortedParts.map { partWithRounds ->
                    Pieza(
                        idPieza = partWithRounds.part.idPieza,
                        nombre = partWithRounds.part.nombre,
                        cantidadRequerida = partWithRounds.part.cantidadRequerida,
                        notasPieza = partWithRounds.part.notasPieza,
                        vueltas = partWithRounds.sortedRounds.map { roundEntity ->
                            Vuelta(
                                numero = roundEntity.numero,
                                instruccion = roundEntity.instruccion,
                                puntosTotales = roundEntity.puntosTotales,
                                repeticiones = roundEntity.repeticiones
                            )
                        }
                    )
                }
            )

            RetrofitClient.instance.proponerPatron(patronParaApi)
            android.util.Log.d("API_SYNC", "Patrón propuesto correctamente: ${patronParaApi.nombre}")
            true
        } catch (e: Exception) {
            android.util.Log.e("API_SYNC", "Error al proponer patrón: ${e.message}", e)
            false
        }
    }

    // --- Progreso Activo (WIPs) ---

    fun observeLatestActiveProgress(): Flow<ActiveProgressEntity?> =
        progressDao.observeLatestActiveProgress()

    fun observeAllActiveProgress(): Flow<List<ActiveProgressEntity>> =
        progressDao.observeAllActiveProgress()

    fun observeProgressForPattern(patternId: String): Flow<ActiveProgressEntity?> =
        progressDao.observeProgressForPattern(patternId)

    suspend fun getProgressForPattern(patternId: String): ActiveProgressEntity? =
        progressDao.getProgressForPattern(patternId)

    suspend fun startOrResetProject(patternId: String): ActiveProgressEntity? {
        val fullPattern = patternDao.getPatternById(patternId) ?: return null
        val firstPart = fullPattern.sortedParts.firstOrNull()?.part ?: return null
        val firstRound = fullPattern.sortedParts.firstOrNull()
            ?.sortedRounds
            ?.firstOrNull()?.numero ?: 1

        val existing = progressDao.getProgressForPattern(patternId)
        val now = System.currentTimeMillis()
        val newProgress = ActiveProgressEntity(
            idSesion = existing?.idSesion ?: 0L,
            patternId = patternId,
            currentPartId = firstPart.idPieza,
            currentPartInstance = 1,
            currentRoundNumber = firstRound,
            currentRepetitionCount = 0,
            completedPartsMask = 0,
            startedAt = now,
            lastUpdatedTimestamp = now
        )
        progressDao.upsertProgress(newProgress)
        return progressDao.getProgressForPattern(patternId)
    }

    suspend fun ensureOrTouchProgress(patternId: String): ActiveProgressEntity? {
        val existing = progressDao.getProgressForPattern(patternId)
        return if (existing != null) {
            val touched = existing.copy(lastUpdatedTimestamp = System.currentTimeMillis())
            progressDao.upsertProgress(touched)
            touched
        } else {
            startOrResetProject(patternId)
        }
    }

    suspend fun updateProgress(progress: ActiveProgressEntity) {
        progressDao.upsertProgress(
            progress.copy(lastUpdatedTimestamp = System.currentTimeMillis())
        )
    }

    // --- Historial y Estadísticas ---

    fun observeCompletedProjects(): Flow<List<CompletedProjectEntity>> =
        historyDao.observeAllCompletedProjects()

    fun observeCompletedCountsByPattern(): Flow<List<CompletedProjectCount>> =
        historyDao.observeCompletedCountsByPattern()

    suspend fun completeProject(patternId: String, notasUsuario: String = ""): CompletedProjectEntity {
        val active = progressDao.getProgressForPattern(patternId)
        val now = System.currentTimeMillis()
        val started = active?.startedAt ?: now
        val historyEntry = CompletedProjectEntity(
            patternId = patternId,
            startedAt = started,
            completedAt = now,
            notasUsuario = notasUsuario
        )
        historyDao.insertCompletedProject(historyEntry)
        progressDao.deleteProgressByPatternId(patternId)
        return historyEntry
    }
}
