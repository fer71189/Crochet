package com.nexdev.crochet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexdev.crochet.data.local.entity.ActiveProgressEntity
import com.nexdev.crochet.data.local.entity.CompletedProjectEntity
import com.nexdev.crochet.data.local.entity.PartEntity
import com.nexdev.crochet.data.local.entity.PartWithRounds
import com.nexdev.crochet.data.local.entity.PatternEntity
import com.nexdev.crochet.data.local.entity.PatternWithPartsAndRounds
import com.nexdev.crochet.data.local.entity.RoundEntity
import com.nexdev.crochet.data.repository.CrochetRepository
import com.nexdev.crochet.ui.util.ProgressCalculator
import com.nexdev.crochet.ui.util.ProgressMetrics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class ActiveResumeSummary(
    val patternWithDetails: PatternWithPartsAndRounds,
    val progress: ActiveProgressEntity,
    val currentPart: PartWithRounds,
    val currentRound: RoundEntity,
    val metrics: ProgressMetrics
)

data class RankedPatternStat(
    val patternWithDetails: PatternWithPartsAndRounds,
    val count: Int
)

data class CompletedHistoryItem(
    val history: CompletedProjectEntity,
    val patternWithDetails: PatternWithPartsAndRounds
)

data class CatalogUiState(
    val patterns: List<PatternWithPartsAndRounds> = emptyList(),
    val filteredPatterns: List<PatternWithPartsAndRounds> = emptyList(),
    val categories: List<String> = listOf("Todos", "Apego", "Animales", "Personajes", "Recuerdos"),
    val selectedCategory: String = "Todos",
    val latestResumeSummary: ActiveResumeSummary? = null,
    val activeProgressByPatternId: Map<String, ActiveProgressEntity> = emptyMap(),
    val completedCountsByPatternId: Map<String, Int> = emptyMap(),
    val mostMadeRanking: List<RankedPatternStat> = emptyList(),
    val completedHistory: List<CompletedHistoryItem> = emptyList(),
    val totalCompletedCount: Int = 0
)

data class CustomRoundDraft(
    val numero: Int,
    val instruccion: String,
    val puntosTotales: Int,
    val repeticiones: Int = 1
)

data class CustomPartDraft(
    val nombre: String,
    val cantidadRequerida: Int,
    val notasPieza: String = "",
    val vueltas: List<CustomRoundDraft>
)

class CatalogViewModel(
    private val repository: CrochetRepository
) : ViewModel() {

    private val selectedCategoryFlow = MutableStateFlow("Todos")
    private val json = Json { ignoreUnknownKeys = true }

    val uiState: StateFlow<CatalogUiState> = combine(
        repository.observeAllPatterns(),
        repository.observeAllActiveProgress(),
        repository.observeCompletedProjects(),
        repository.observeCompletedCountsByPattern(),
        selectedCategoryFlow
    ) { patterns, activeProgressList, completedList, completedCounts, selectedCat ->

        val patternMap = patterns.associateBy { it.pattern.id }
        val activeMap = activeProgressList.associateBy { it.patternId }
        val countsMap = completedCounts.associate { it.patternId to it.count }

        val latestProgress = activeProgressList.maxByOrNull { it.lastUpdatedTimestamp }
        val latestResume = latestProgress?.let { prog ->
            val pat = patternMap[prog.patternId] ?: return@let null
            val sortedParts = pat.sortedParts
            if (sortedParts.isEmpty()) return@let null
            val part = sortedParts.find { it.part.idPieza == prog.currentPartId }
                ?: sortedParts.first()
            val round = part.sortedRounds.find { it.numero == prog.currentRoundNumber }
                ?: part.sortedRounds.firstOrNull()
                ?: return@let null
            val metrics = ProgressCalculator.calculate(pat, prog)
            ActiveResumeSummary(
                patternWithDetails = pat,
                progress = prog,
                currentPart = part,
                currentRound = round,
                metrics = metrics
            )
        }

        val filtered = if (selectedCat == "Todos") {
            patterns
        } else {
            patterns.filter { it.pattern.categoria.equals(selectedCat, ignoreCase = true) }
        }

        val ranking = completedCounts.mapNotNull { countItem ->
            val pat = patternMap[countItem.patternId] ?: return@mapNotNull null
            RankedPatternStat(patternWithDetails = pat, count = countItem.count)
        }.sortedByDescending { it.count }

        val historyItems = completedList.mapNotNull { hist ->
            val pat = patternMap[hist.patternId] ?: return@mapNotNull null
            CompletedHistoryItem(history = hist, patternWithDetails = pat)
        }

        val dynamicCategories = buildList {
            add("Todos")
            addAll(listOf("Apego", "Animales", "Personajes", "Recuerdos"))
            patterns.map { it.pattern.categoria }.forEach { cat ->
                if (none { it.equals(cat, ignoreCase = true) }) {
                    add(cat)
                }
            }
        }

        CatalogUiState(
            patterns = patterns,
            filteredPatterns = filtered,
            categories = dynamicCategories,
            selectedCategory = selectedCat,
            latestResumeSummary = latestResume,
            activeProgressByPatternId = activeMap,
            completedCountsByPatternId = countsMap,
            mostMadeRanking = ranking,
            completedHistory = historyItems,
            totalCompletedCount = completedList.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CatalogUiState()
    )

    fun selectCategory(category: String) {
        selectedCategoryFlow.value = category
    }

    fun startNewProject(patternId: String, onReady: () -> Unit = {}) {
        viewModelScope.launch {
            repository.startOrResetProject(patternId)
            onReady()
        }
    }

    fun resumeOrStartProject(patternId: String, onReady: () -> Unit = {}) {
        viewModelScope.launch {
            repository.ensureOrTouchProgress(patternId)
            onReady()
        }
    }

    fun createCustomPattern(
        nombre: String,
        categoria: String,
        dificultad: String = "Personalizado",
        hiloRecomendado: String,
        agujaMm: Double,
        icono: String = "🧶",
        colorBg: String = "#F4E6DD",
        extras: List<String> = listOf("Creado desde el móvil"),
        notasEnsamblaje: String = "",
        partsDraft: List<CustomPartDraft>,
        onCreated: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val patternId = "pat_custom_${System.currentTimeMillis()}"
            val totalRounds = partsDraft.sumOf { it.cantidadRequerida * it.vueltas.size }
            val estimatedMin = (totalRounds * 2).coerceAtLeast(10)
            val patternEntity = PatternEntity(
                id = patternId,
                nombre = nombre.trim(),
                icono = icono,
                colorBg = colorBg,
                categoria = categoria,
                dificultad = dificultad,
                tiempoEstimadoMin = estimatedMin,
                hiloRecomendado = hiloRecomendado.trim(),
                agujaMm = agujaMm,
                extrasJson = json.encodeToString(extras),
                notasEnsamblaje = notasEnsamblaje.trim(),
                esPersonalizado = true
            )

            val partEntities = mutableListOf<PartEntity>()
            val roundEntities = mutableListOf<RoundEntity>()

            partsDraft.forEachIndexed { pIndex, partDraft ->
                val partId = "${patternId}_p${pIndex + 1}"
                partEntities.add(
                    PartEntity(
                        idPieza = partId,
                        patternId = patternId,
                        orden = pIndex,
                        nombre = partDraft.nombre.trim(),
                        cantidadRequerida = partDraft.cantidadRequerida.coerceAtLeast(1),
                        notasPieza = partDraft.notasPieza.trim()
                    )
                )

                partDraft.vueltas.forEachIndexed { rIndex, roundDraft ->
                    roundEntities.add(
                        RoundEntity(
                            partId = partId,
                            numero = roundDraft.numero.takeIf { it > 0 } ?: (rIndex + 1),
                            instruccion = roundDraft.instruccion.trim(),
                            puntosTotales = roundDraft.puntosTotales.coerceAtLeast(1),
                            repeticiones = roundDraft.repeticiones.coerceAtLeast(1)
                        )
                    )
                }
            }

            repository.saveCustomPattern(patternEntity, partEntities, roundEntities)
            onCreated(patternId)
        }
    }

    fun updateExistingPattern(
        patternId: String,
        nombre: String,
        categoria: String,
        dificultad: String = "Personalizado",
        hiloRecomendado: String,
        agujaMm: Double,
        icono: String = "🧶",
        colorBg: String = "#F4E6DD",
        extras: List<String> = listOf("Materiales estándar"),
        notasEnsamblaje: String = "",
        partsDraft: List<CustomPartDraft>,
        onUpdated: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val totalRounds = partsDraft.sumOf { it.cantidadRequerida * it.vueltas.size }
            val estimatedMin = (totalRounds * 2).coerceAtLeast(10)
            val patternEntity = PatternEntity(
                id = patternId,
                nombre = nombre.trim(),
                icono = icono,
                colorBg = colorBg,
                categoria = categoria,
                dificultad = dificultad,
                tiempoEstimadoMin = estimatedMin,
                hiloRecomendado = hiloRecomendado.trim(),
                agujaMm = agujaMm,
                extrasJson = json.encodeToString(extras),
                notasEnsamblaje = notasEnsamblaje.trim(),
                esPersonalizado = true
            )

            val partEntities = mutableListOf<PartEntity>()
            val roundEntities = mutableListOf<RoundEntity>()

            partsDraft.forEachIndexed { pIndex, partDraft ->
                val partId = "${patternId}_p${pIndex + 1}"
                partEntities.add(
                    PartEntity(
                        idPieza = partId,
                        patternId = patternId,
                        orden = pIndex,
                        nombre = partDraft.nombre.trim(),
                        cantidadRequerida = partDraft.cantidadRequerida.coerceAtLeast(1),
                        notasPieza = partDraft.notasPieza.trim()
                    )
                )

                partDraft.vueltas.forEachIndexed { rIndex, roundDraft ->
                    roundEntities.add(
                        RoundEntity(
                            partId = partId,
                            numero = roundDraft.numero.takeIf { it > 0 } ?: (rIndex + 1),
                            instruccion = roundDraft.instruccion.trim(),
                            puntosTotales = roundDraft.puntosTotales.coerceAtLeast(1),
                            repeticiones = roundDraft.repeticiones.coerceAtLeast(1)
                        )
                    )
                }
            }

            repository.updatePattern(patternEntity, partEntities, roundEntities)
            onUpdated(patternId)
        }
    }
}
