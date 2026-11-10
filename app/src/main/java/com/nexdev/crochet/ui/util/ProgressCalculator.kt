package com.nexdev.crochet.ui.util

import com.nexdev.crochet.data.local.entity.ActiveProgressEntity
import com.nexdev.crochet.data.local.entity.PatternWithPartsAndRounds
import kotlinx.serialization.json.Json
import kotlin.math.roundToInt

data class ProgressMetrics(
    val percent: Int = 0,
    val completedRoundsSoFar: Int = 0,
    val totalRoundsAllInstances: Int = 1,
    val activePartIndex: Int = 0,
    val activeRoundIndex: Int = 0
)

object ProgressCalculator {

    private val json = Json { ignoreUnknownKeys = true }

    fun calculate(
        patternWithDetails: PatternWithPartsAndRounds?,
        activeProgress: ActiveProgressEntity?
    ): ProgressMetrics {
        if (patternWithDetails == null) return ProgressMetrics()
        val sortedParts = patternWithDetails.sortedParts
        if (sortedParts.isEmpty()) return ProgressMetrics()

        var totalRoundsAllInstances = 0
        sortedParts.forEach { partWithRounds ->
            totalRoundsAllInstances += partWithRounds.rounds.size * partWithRounds.part.cantidadRequerida
        }
        if (totalRoundsAllInstances <= 0) totalRoundsAllInstances = 1

        if (activeProgress == null || activeProgress.patternId != patternWithDetails.pattern.id) {
            return ProgressMetrics(
                percent = 0,
                completedRoundsSoFar = 0,
                totalRoundsAllInstances = totalRoundsAllInstances
            )
        }

        val activePartIdx = sortedParts.indexOfFirst {
            it.part.idPieza == activeProgress.currentPartId
        }.coerceAtLeast(0)

        var completedRoundsSoFar = 0
        var activeRoundIdx = 0

        sortedParts.forEachIndexed { pIdx, partWithRounds ->
            val roundsPerInstance = partWithRounds.sortedRounds.size
            val totalForThisPart = roundsPerInstance * partWithRounds.part.cantidadRequerida

            if (pIdx < activePartIdx) {
                completedRoundsSoFar += totalForThisPart
            } else if (pIdx == activePartIdx) {
                val sortedRounds = partWithRounds.sortedRounds
                val rIdx = sortedRounds.indexOfFirst {
                    it.numero == activeProgress.currentRoundNumber
                }.coerceAtLeast(0)
                activeRoundIdx = rIdx

                val finishedInstances = (activeProgress.currentPartInstance - 1)
                    .coerceAtLeast(0) * roundsPerInstance
                completedRoundsSoFar += finishedInstances + rIdx
            }
        }

        val percent = ((completedRoundsSoFar.toFloat() / totalRoundsAllInstances.toFloat()) * 100f)
            .roundToInt()
            .coerceIn(0, 100)

        return ProgressMetrics(
            percent = percent,
            completedRoundsSoFar = completedRoundsSoFar,
            totalRoundsAllInstances = totalRoundsAllInstances,
            activePartIndex = activePartIdx,
            activeRoundIndex = activeRoundIdx
        )
    }

    fun parseExtras(extrasJson: String): List<String> {
        return runCatching {
            json.decodeFromString<List<String>>(extrasJson)
        }.getOrDefault(emptyList())
    }

    fun formatDuration(minutes: Int): String {
        if (minutes <= 0) return "En primer tejido"
        if (minutes < 60) return "$minutes min"
        val hours = minutes / 60.0
        return if (minutes % 60 == 0) {
            "${minutes / 60} hrs"
        } else {
            String.format(java.util.Locale.US, "%.1f hrs", hours)
        }
    }

    fun formatElapsedDuration(millis: Long): String {
        val totalSeconds = (millis / 1000).coerceAtLeast(0)
        val totalMinutes = totalSeconds / 60
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        val seconds = totalSeconds % 60

        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            minutes > 0 -> "${minutes} min"
            else -> "${seconds} seg"
        }
    }
}
