package com.nexdev.crochet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexdev.crochet.data.local.entity.ActiveProgressEntity
import com.nexdev.crochet.data.local.entity.PartWithRounds
import com.nexdev.crochet.data.local.entity.PatternWithPartsAndRounds
import com.nexdev.crochet.data.local.entity.RoundEntity
import com.nexdev.crochet.data.repository.CrochetRepository
import com.nexdev.crochet.ui.util.ProgressCalculator
import com.nexdev.crochet.ui.util.ProgressMetrics
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NextStepActionType {
    NEXT_ROUND,
    NEXT_PART_INSTANCE,
    NEXT_PART,
    FINISH_PROJECT
}

data class TrackerUiState(
    val isLoading: Boolean = true,
    val patternWithDetails: PatternWithPartsAndRounds? = null,
    val progress: ActiveProgressEntity? = null,
    val sortedParts: List<PartWithRounds> = emptyList(),
    val activePartIndex: Int = 0,
    val currentPart: PartWithRounds? = null,
    val currentPartInstance: Int = 1,
    val sortedRounds: List<RoundEntity> = emptyList(),
    val activeRoundIndex: Int = 0,
    val currentRound: RoundEntity? = null,
    val currentRepetitionCount: Int = 0,
    val metrics: ProgressMetrics = ProgressMetrics(),
    val canGoBack: Boolean = false,
    val nextActionType: NextStepActionType = NextStepActionType.NEXT_ROUND,
    val nextPartName: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class TrackerViewModel(
    private val repository: CrochetRepository
) : ViewModel() {

    private val activePatternIdFlow = MutableStateFlow<String?>(null)

    private val effectivePatternFlow = activePatternIdFlow.flatMapLatest { explicitId ->
        if (explicitId != null) {
            combine(
                repository.observePatternById(explicitId),
                repository.observeProgressForPattern(explicitId)
            ) { pat, prog -> pat to prog }
        } else {
            repository.observeLatestActiveProgress().flatMapLatest { latestProg ->
                if (latestProg == null) {
                    flowOf(null to null)
                } else {
                    repository.observePatternById(latestProg.patternId).combine(
                        flowOf(latestProg)
                    ) { pat, prog -> pat to prog }
                }
            }
        }
    }

    val uiState: StateFlow<TrackerUiState> = effectivePatternFlow.combine(
        activePatternIdFlow
    ) { (patternWithDetails, activeProgress), _ ->
        if (patternWithDetails == null) {
            return@combine TrackerUiState(isLoading = false)
        }

        val sortedParts = patternWithDetails.sortedParts
        if (sortedParts.isEmpty()) {
            return@combine TrackerUiState(
                isLoading = false,
                patternWithDetails = patternWithDetails
            )
        }

        val metrics = ProgressCalculator.calculate(patternWithDetails, activeProgress)
        val pIdx = metrics.activePartIndex.coerceIn(0, sortedParts.lastIndex)
        val currentPart = sortedParts[pIdx]
        val sortedRounds = currentPart.sortedRounds
        val rIdx = if (sortedRounds.isNotEmpty()) {
            metrics.activeRoundIndex.coerceIn(0, sortedRounds.lastIndex)
        } else {
            0
        }
        val currentRound = sortedRounds.getOrNull(rIdx)
        val instance = (activeProgress?.currentPartInstance ?: 1)
            .coerceIn(1, currentPart.part.cantidadRequerida)
        val repCount = (activeProgress?.currentRepetitionCount ?: 0)
            .coerceIn(0, currentRound?.repeticiones ?: 1)

        val isLastRoundOfPart = rIdx >= sortedRounds.lastIndex
        val isLastInstanceOfPart = instance >= currentPart.part.cantidadRequerida
        val isLastPartOfPattern = pIdx >= sortedParts.lastIndex

        val nextAction = when {
            isLastRoundOfPart && isLastInstanceOfPart && isLastPartOfPattern ->
                NextStepActionType.FINISH_PROJECT
            isLastRoundOfPart && !isLastInstanceOfPart ->
                NextStepActionType.NEXT_PART_INSTANCE
            isLastRoundOfPart && isLastInstanceOfPart && !isLastPartOfPattern ->
                NextStepActionType.NEXT_PART
            else -> NextStepActionType.NEXT_ROUND
        }

        val nextPartName = sortedParts.getOrNull(pIdx + 1)?.part?.nombre
        val canGoBack = rIdx > 0 || instance > 1 || pIdx > 0

        TrackerUiState(
            isLoading = false,
            patternWithDetails = patternWithDetails,
            progress = activeProgress,
            sortedParts = sortedParts,
            activePartIndex = pIdx,
            currentPart = currentPart,
            currentPartInstance = instance,
            sortedRounds = sortedRounds,
            activeRoundIndex = rIdx,
            currentRound = currentRound,
            currentRepetitionCount = repCount,
            metrics = metrics,
            canGoBack = canGoBack,
            nextActionType = nextAction,
            nextPartName = nextPartName
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TrackerUiState()
    )

    fun loadPatternForTracking(patternId: String?, startFresh: Boolean = false) {
        viewModelScope.launch {
            if (patternId != null) {
                activePatternIdFlow.value = patternId
                if (startFresh) {
                    repository.startOrResetProject(patternId)
                } else {
                    repository.ensureOrTouchProgress(patternId)
                }
            } else {
                activePatternIdFlow.value = null
            }
        }
    }

    fun changeRepetition(delta: Int) {
        val state = uiState.value
        val prog = state.progress ?: return
        val maxRep = state.currentRound?.repeticiones ?: 1
        val newRep = (state.currentRepetitionCount + delta).coerceIn(0, maxRep)
        if (newRep == state.currentRepetitionCount) return

        viewModelScope.launch {
            repository.updateProgress(
                prog.copy(currentRepetitionCount = newRep)
            )
        }
    }

    fun selectPart(partIndex: Int) {
        val state = uiState.value
        val prog = state.progress ?: return
        val targetPart = state.sortedParts.getOrNull(partIndex) ?: return
        val firstRoundNum = targetPart.sortedRounds.firstOrNull()?.numero ?: 1

        viewModelScope.launch {
            repository.updateProgress(
                prog.copy(
                    currentPartId = targetPart.part.idPieza,
                    currentPartInstance = 1,
                    currentRoundNumber = firstRoundNum,
                    currentRepetitionCount = 0
                )
            )
        }
    }

    fun jumpToRound(roundIndex: Int) {
        val state = uiState.value
        val prog = state.progress ?: return
        val targetRound = state.sortedRounds.getOrNull(roundIndex) ?: return

        viewModelScope.launch {
            repository.updateProgress(
                prog.copy(
                    currentRoundNumber = targetRound.numero,
                    currentRepetitionCount = 0
                )
            )
        }
    }

    fun resetCurrentPart() {
        val state = uiState.value
        val prog = state.progress ?: return
        val firstRoundNum = state.sortedRounds.firstOrNull()?.numero ?: 1

        viewModelScope.launch {
            repository.updateProgress(
                prog.copy(
                    currentRoundNumber = firstRoundNum,
                    currentRepetitionCount = 0
                )
            )
        }
    }

    fun previousRound() {
        val state = uiState.value
        val prog = state.progress ?: return
        val sortedParts = state.sortedParts
        if (sortedParts.isEmpty()) return

        val pIdx = state.activePartIndex
        val rIdx = state.activeRoundIndex
        val instance = state.currentPartInstance

        viewModelScope.launch {
            when {
                rIdx > 0 -> {
                    val prevRound = state.sortedRounds[rIdx - 1]
                    repository.updateProgress(
                        prog.copy(
                            currentRoundNumber = prevRound.numero,
                            currentRepetitionCount = 0
                        )
                    )
                }
                instance > 1 -> {
                    val lastRoundOfPart = state.sortedRounds.lastOrNull()?.numero ?: 1
                    repository.updateProgress(
                        prog.copy(
                            currentPartInstance = instance - 1,
                            currentRoundNumber = lastRoundOfPart,
                            currentRepetitionCount = 0
                        )
                    )
                }
                pIdx > 0 -> {
                    val prevPart = sortedParts[pIdx - 1]
                    val lastRoundOfPrevPart = prevPart.sortedRounds.lastOrNull()?.numero ?: 1
                    repository.updateProgress(
                        prog.copy(
                            currentPartId = prevPart.part.idPieza,
                            currentPartInstance = prevPart.part.cantidadRequerida,
                            currentRoundNumber = lastRoundOfPrevPart,
                            currentRepetitionCount = 0
                        )
                    )
                }
            }
        }
    }

    fun nextRoundOrFinish(
        onMessage: (String) -> Unit = {},
        onProjectFinished: (String) -> Unit = {}
    ) {
        val state = uiState.value
        val prog = state.progress ?: return
        val currentPart = state.currentPart ?: return
        val sortedParts = state.sortedParts
        val pIdx = state.activePartIndex
        val rIdx = state.activeRoundIndex
        val instance = state.currentPartInstance

        viewModelScope.launch {
            // 1. Siguiente vuelta en la misma pieza e instancia
            if (rIdx < state.sortedRounds.lastIndex) {
                val nextRound = state.sortedRounds[rIdx + 1]
                repository.updateProgress(
                    prog.copy(
                        currentRoundNumber = nextRound.numero,
                        currentRepetitionCount = 0
                    )
                )
                return@launch
            }

            // 2. Terminó última vuelta de la instancia y faltan más instancias (ej. Tentáculo 1 de 8 -> 2 de 8)
            if (instance < currentPart.part.cantidadRequerida) {
                val firstRoundNum = state.sortedRounds.firstOrNull()?.numero ?: 1
                val nextInstance = instance + 1
                val updatedMask = prog.completedPartsMask
                repository.updateProgress(
                    prog.copy(
                        currentPartInstance = nextInstance,
                        currentRoundNumber = firstRoundNum,
                        currentRepetitionCount = 0,
                        completedPartsMask = updatedMask
                    )
                )
                onMessage("${currentPart.part.nombre} $instance completado • Iniciando #$nextInstance")
                return@launch
            }

            // 3. Terminó todas las instancias de esta pieza y hay más piezas en el patrón
            if (pIdx < sortedParts.lastIndex) {
                val nextPart = sortedParts[pIdx + 1]
                val firstRoundNum = nextPart.sortedRounds.firstOrNull()?.numero ?: 1
                val newMask = prog.completedPartsMask or (1 shl pIdx)
                repository.updateProgress(
                    prog.copy(
                        currentPartId = nextPart.part.idPieza,
                        currentPartInstance = 1,
                        currentRoundNumber = firstRoundNum,
                        currentRepetitionCount = 0,
                        completedPartsMask = newMask
                    )
                )
                onMessage("¡Pieza terminada! Pasando a ${nextPart.part.nombre}")
                return@launch
            }

            // 4. ¡Proyecto Finalizado! Mover a CompletedProjectEntity y limpiar sesión activa
            val patternName = state.patternWithDetails?.pattern?.nombre ?: "Proyecto"
            val historyEntry = repository.completeProject(prog.patternId)
            val durationMillis = (historyEntry.completedAt - historyEntry.startedAt).coerceAtLeast(0)
            val durationText = ProgressCalculator.formatElapsedDuration(durationMillis)
            onProjectFinished("🎉 ¡Felicidades! Terminaste $patternName en $durationText")
        }
    }
}
