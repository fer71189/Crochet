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
