package com.nexdev.crochet.ui.screens

import android.app.Activity
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexdev.crochet.data.local.entity.RoundEntity
import com.nexdev.crochet.ui.theme.AmberSecondary
import com.nexdev.crochet.ui.theme.DangerRed
import com.nexdev.crochet.ui.theme.DangerRedBg
import com.nexdev.crochet.ui.theme.EspressoDark
import com.nexdev.crochet.ui.theme.SageSuccess
import com.nexdev.crochet.ui.theme.SageSuccessBg
import com.nexdev.crochet.ui.theme.SageSuccessText
import com.nexdev.crochet.ui.theme.TerracottaPrimary
import com.nexdev.crochet.ui.theme.TextMutedBrown
import com.nexdev.crochet.ui.theme.TextSecondaryBrown
import com.nexdev.crochet.ui.theme.TextSubtleTaupe
import com.nexdev.crochet.ui.theme.TextUpperLabel
import com.nexdev.crochet.ui.theme.WarmBorder
import com.nexdev.crochet.ui.theme.WarmChipBg
import com.nexdev.crochet.ui.theme.WarmCreamBg
import com.nexdev.crochet.ui.theme.WarmSurfaceSubtle
import com.nexdev.crochet.ui.viewmodel.NextStepActionType
import com.nexdev.crochet.ui.viewmodel.TrackerUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun KnittingTrackerScreen(
    uiState: TrackerUiState,
    onPauseAndExit: () -> Unit,
    onExploreCatalog: () -> Unit,
    onResetPart: () -> Unit,
    onSelectPart: (Int) -> Unit,
    onJumpToRound: (Int) -> Unit,
    onChangeRepetition: (Int) -> Unit,
    onPrevRound: () -> Unit,
    onNextRoundOrFinish: ((String) -> Unit, (String) -> Unit) -> Unit,
    onProjectCompletedNavigate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Regla 3: Exclusivamente en Modo Tejido, mantener la pantalla encendida (FLAG_KEEP_SCREEN_ON)
    DisposableEffect(context) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    val patternWithDetails = uiState.patternWithDetails
    val currentPart = uiState.currentPart

    if (patternWithDetails == null || currentPart == null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(WarmCreamBg)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "🧶", fontSize = 52.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No tienes ningún proyecto en curso",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = EspressoDark,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Selecciona un diseño desde el catálogo para comenzar el seguimiento paso a paso.",
                fontSize = 13.sp,
                color = TextSubtleTaupe,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onExploreCatalog,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TerracottaPrimary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Ir al Catálogo de Proyectos",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
        return
    }

    val pattern = patternWithDetails.pattern
    val listState = rememberLazyListState()
    var completionMessage by remember { mutableStateOf<String?>(null) }

    if (completionMessage != null) {
        AlertDialog(
            onDismissRequest = {
                completionMessage = null
                onProjectCompletedNavigate()
            },
            title = {
                Text(
                    text = "🎉 ¡Proyecto Finalizado!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = EspressoDark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = completionMessage ?: "",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = EspressoDark
                    )
                    Text(
                        text = "El tiempo exacto invertido y tu avance quedaron guardados en tu historial de logros.",
                        fontSize = 12.sp,
                        color = TextSecondaryBrown
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        completionMessage = null
                        onProjectCompletedNavigate()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SageSuccess,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Ver Mis Logros 🏆", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Auto-scroll hacia la vuelta activa cada vez que cambia de vuelta o pieza
    LaunchedEffect(uiState.activePartIndex, uiState.currentPartInstance, uiState.activeRoundIndex) {
        if (uiState.sortedRounds.isNotEmpty()) {
            val targetIndex = (uiState.activeRoundIndex - 1).coerceAtLeast(0)
            listState.animateScrollToItem(targetIndex)
        }
    }

    val savedTimeText = uiState.progress?.lastUpdatedTimestamp?.let { ts ->
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(ts))
    } ?: "Ahora"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WarmSurfaceSubtle)
    ) {
        // ==========================================
        // 1. CABECERA FIJA DE MODO TEJIDO
        // ==========================================
        Surface(
            color = Color.White,
            shadowElevation = 2.dp,
            border = BorderStroke(1.dp, WarmBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp)
            ) {
                // Botones Superiores: Pausar, Indicador Pantalla Activa, Reiniciar Pieza
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = onPauseAndExit,
                        shape = RoundedCornerShape(12.dp),
                        color = WarmChipBg
                    ) {
                        Text(
                            text = "← Pausar y Salir",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMutedBrown,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SageSuccessBg
                        ) {
                            Text(
                                text = "🔆 Pantalla Activa",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SageSuccessText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }

                        Surface(
                            onClick = {
                                onResetPart()
                                Toast.makeText(
                                    context,
                                    "Pieza reiniciada a la Vuelta 1",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = DangerRedBg
                        ) {
                            Text(
                                text = "↺ Reiniciar pieza",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = DangerRed,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Título del patrón y Porcentaje Global
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${pattern.icono} ${pattern.nombre}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = EspressoDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Guardado en Room: $savedTimeText",
                            fontSize = 11.sp,
                            color = TextSubtleTaupe
                        )
                    }

                    Text(
                        text = "${uiState.metrics.percent}%",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TerracottaPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Barra de Progreso General
                LinearProgressIndicator(
                    progress = { uiState.metrics.percent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = TerracottaPrimary,
                    trackColor = WarmChipBg
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Pestañas Horizontales de Piezas del Diseño
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(
                        items = uiState.sortedParts,
                        key = { _, partWithRounds -> partWithRounds.part.idPieza }
                    ) { index, partWithRounds ->
                        val isCurrent = index == uiState.activePartIndex
                        val isDone = index < uiState.activePartIndex
                        val bgColor = when {
                            isCurrent -> EspressoDark
                            isDone -> SageSuccessBg
                            else -> WarmChipBg
                        }
                        val textColor = when {
                            isCurrent -> Color.White
                            isDone -> SageSuccessText
                            else -> TextMutedBrown
                        }

                        Surface(
                            onClick = { onSelectPart(index) },
                            shape = RoundedCornerShape(12.dp),
                            color = bgColor
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (isDone) "✓ ${partWithRounds.part.nombre}" else "${index + 1}. ${partWithRounds.part.nombre}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                                Text(
                                    text = "(x${partWithRounds.part.cantidadRequerida})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textColor.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 2. SUB-BARRA DE INSTANCIA DE PIEZA
        // ==========================================
        Surface(
            color = WarmCreamBg,
            border = BorderStroke(0.5.dp, WarmBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = TextSecondaryBrown, fontWeight = FontWeight.Bold)) {
                            append("Tejiendo: ")
                        }
                        withStyle(SpanStyle(color = TerracottaPrimary, fontWeight = FontWeight.ExtraBold)) {
                            append("${currentPart.part.nombre} (Pieza ${uiState.currentPartInstance} de ${currentPart.part.cantidadRequerida})")
                        }
                    },
                    fontSize = 12.sp
                )

                Text(
                    text = "Vuelta ${uiState.activeRoundIndex + 1} de ${uiState.sortedRounds.size}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSubtleTaupe
                )
            }
        }

        // ==========================================
        // 3. LISTA DE VUELTAS CON AUTO-SCROLL
        // ==========================================
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Nota de la pieza si existe
            if (currentPart.part.notasPieza.isNotBlank()) {
                item(key = "part_note") {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, WarmBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "💡", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = currentPart.part.notasPieza,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondaryBrown
                            )
                        }
                    }
                }
            }

            itemsIndexed(
                items = uiState.sortedRounds,
                key = { _, round -> round.idVuelta.takeIf { it != 0L } ?: round.numero }
            ) { rIdx, round ->
                val isCompleted = rIdx < uiState.activeRoundIndex
                val isCurrent = rIdx == uiState.activeRoundIndex

                when {
                    isCurrent -> {
                        ActiveRoundCard(
                            round = round,
                            currentRepetitionCount = uiState.currentRepetitionCount,
                            onChangeRepetition = onChangeRepetition
                        )
                    }
                    isCompleted -> {
                        CompletedRoundCard(
                            round = round,
                            onClick = { onJumpToRound(rIdx) }
                        )
                    }
                    else -> {
                        UpcomingRoundCard(
                            round = round,
                            onClick = { onJumpToRound(rIdx) }
                        )
                    }
                }
            }
        }

        // ==========================================
        // 4. CONTROLES INFERIORES ERGONÓMICOS
        // ==========================================
        Surface(
            color = Color.White,
            shadowElevation = 10.dp,
            border = BorderStroke(1.dp, WarmBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onPrevRound,
                    enabled = uiState.canGoBack,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, WarmBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = TextSecondaryBrown,
                        disabledContentColor = TextSubtleTaupe.copy(alpha = 0.4f)
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = "- Vuelta",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                val nextBtnColor by animateColorAsState(
                    targetValue = when (uiState.nextActionType) {
                        NextStepActionType.FINISH_PROJECT -> SageSuccess
                        NextStepActionType.NEXT_PART -> EspressoDark
                        else -> TerracottaPrimary
                    },
                    label = "nextBtnColor"
                )

                val nextBtnLabel = when (uiState.nextActionType) {
                    NextStepActionType.FINISH_PROJECT -> "🎉 Finalizar Proyecto"
                    NextStepActionType.NEXT_PART_INSTANCE ->
                        "✓ Terminar ${currentPart.part.nombre} ${uiState.currentPartInstance}/${currentPart.part.cantidadRequerida} → Siguiente"
                    NextStepActionType.NEXT_PART ->
                        "Pasar a Pieza: ${uiState.nextPartName ?: "Siguiente"} →"
                    NextStepActionType.NEXT_ROUND -> {
                        val nextRoundNum = uiState.sortedRounds.getOrNull(uiState.activeRoundIndex + 1)?.numero
                            ?: (uiState.activeRoundIndex + 2)
                        "+ Siguiente Vuelta (V$nextRoundNum)"
                    }
                }

                Button(
                    onClick = {
                        onNextRoundOrFinish(
                            { msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            },
                            { finishMsg ->
                                completionMessage = finishMsg
                            }
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = nextBtnColor,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = nextBtnLabel,
                        fontSize = if (uiState.nextActionType == NextStepActionType.NEXT_ROUND ||
                            uiState.nextActionType == NextStepActionType.FINISH_PROJECT
                        ) 15.sp else 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveRoundCard(
    round: RoundEntity,
    currentRepetitionCount: Int,
    onChangeRepetition: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = BorderStroke(2.dp, TerracottaPrimary),
        shadowElevation = 6.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = TerracottaPrimary
                ) {
                    Text(
                        text = "👉 VUELTA ACTUAL ${round.numero}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = WarmChipBg
                ) {
                    Text(
                        text = "Total: ${round.puntosTotales} pts",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextSecondaryBrown,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = round.instruccion,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                color = EspressoDark,
                lineHeight = 25.sp
            )

            // Regla 5: Sub-contador interactivo de repeticiones dentro de la vuelta actual
            if (round.repeticiones > 1) {
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = WarmChipBg, thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = TextMutedBrown, fontWeight = FontWeight.Bold)) {
                                append("Contador de repeticiones: ")
                            }
                            withStyle(SpanStyle(color = EspressoDark, fontWeight = FontWeight.ExtraBold)) {
                                append("$currentRepetitionCount / ${round.repeticiones}")
                            }
                        },
                        fontSize = 12.sp
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            onClick = { onChangeRepetition(-1) },
                            shape = RoundedCornerShape(10.dp),
                            color = WarmChipBg
                        ) {
                            Text(
                                text = "-",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextSecondaryBrown,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        Surface(
                            onClick = { onChangeRepetition(1) },
                            shape = RoundedCornerShape(10.dp),
                            color = AmberSecondary
                        ) {
                            Text(
                                text = "+1 Rep",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompletedRoundCard(
    round: RoundEntity,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(0.75f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = WarmChipBg.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, WarmBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(SageSuccess),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Vuelta ${round.numero}: ${round.instruccion}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondaryBrown,
                    textDecoration = TextDecoration.LineThrough,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "${round.puntosTotales} pts",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSubtleTaupe
            )
        }
    }
}

@Composable
private fun UpcomingRoundCard(
    round: RoundEntity,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.8f),
        border = BorderStroke(1.dp, WarmBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(WarmChipBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${round.numero}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSubtleTaupe
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = round.instruccion,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondaryBrown,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "${round.puntosTotales} pts",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextUpperLabel
            )
        }
    }
}
