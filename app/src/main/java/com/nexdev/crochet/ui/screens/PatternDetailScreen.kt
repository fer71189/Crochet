package com.nexdev.crochet.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexdev.crochet.data.local.entity.PatternWithPartsAndRounds
import com.nexdev.crochet.ui.theme.DangerRed
import com.nexdev.crochet.ui.theme.EspressoDark
import com.nexdev.crochet.ui.theme.TerracottaPrimary
import com.nexdev.crochet.ui.theme.TextSecondaryBrown
import com.nexdev.crochet.ui.theme.TextSubtleTaupe
import com.nexdev.crochet.ui.theme.TextUpperLabel
import com.nexdev.crochet.ui.theme.WarmAccentSurface
import com.nexdev.crochet.ui.theme.WarmBorder
import com.nexdev.crochet.ui.theme.WarmBorderSubtle
import com.nexdev.crochet.ui.theme.WarmCreamBg
import com.nexdev.crochet.ui.theme.WarmSurfaceSubtle
import com.nexdev.crochet.ui.theme.parseHexColor
import com.nexdev.crochet.ui.util.ProgressCalculator

@Composable
fun PatternDetailScreen(
    patternWithDetails: PatternWithPartsAndRounds?,
    isAlreadyActive: Boolean,
    onBackToCatalog: () -> Unit,
    onEditPattern: (String) -> Unit,
    onSharePattern: (String) -> Unit,
    onStartNewProject: (String) -> Unit,
    onContinueProject: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (patternWithDetails == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(WarmCreamBg),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Cargando patrón...", color = TextSubtleTaupe)
        }
        return
    }

    val pattern = patternWithDetails.pattern
    val sortedParts = patternWithDetails.sortedParts
    val totalParts = sortedParts.sumOf { it.part.cantidadRequerida }
    val totalRounds = sortedParts.sumOf { it.rounds.size * it.part.cantidadRequerida }
    val extras = ProgressCalculator.parseExtras(pattern.extrasJson)
    val formattedTime = ProgressCalculator.formatDuration(pattern.tiempoEstimadoMin)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WarmCreamBg)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Barra Superior
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onBackToCatalog,
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, WarmBorder)
            ) {
                Text(
                    text = "← Volver",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondaryBrown,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (pattern.esPersonalizado) {
                    Surface(
                        onClick = { onEditPattern(pattern.id) },
                        shape = RoundedCornerShape(12.dp),
                        color = WarmAccentSurface
                    ) {
                        Text(
                            text = "✏️ Editar Patrón",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = WarmAccentSurface
                ) {
                    Text(
                        text = "${pattern.categoria} • ${pattern.dificultad}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TerracottaPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Cabecera y Especificaciones Técnicas (Grid 2x2)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = BorderStroke(1.dp, WarmBorder),
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(parseHexColor(pattern.colorBg)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = pattern.icono, fontSize = 44.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = pattern.nombre,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = EspressoDark,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Revisa los materiales y piezas antes de empezar",
                    fontSize = 12.sp,
                    color = TextSubtleTaupe,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Fila 1: Aguja y Tiempo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SpecBox(
                        label = "🪡 AGUJA SUGERIDA",
                        value = "${pattern.agujaMm} mm",
                        modifier = Modifier.weight(1f)
                    )
                    SpecBox(
                        label = "⏱️ TIEMPO APROX.",
                        value = formattedTime,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Hilo Recomendado (Ancho completo)
                SpecBox(
                    label = "🧶 HILO RECOMENDADO",
                    value = pattern.hiloRecomendado,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Fila 2: Total Piezas y Total Vueltas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SpecBox(
                        label = "🧩 TOTAL PIEZAS",
                        value = "$totalParts piezas",
                        modifier = Modifier.weight(1f)
                    )
                    SpecBox(
                        label = "🔄 TOTAL VUELTAS",
                        value = "$totalRounds vueltas",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Desglose de Piezas a Tejer
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = BorderStroke(1.dp, WarmBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "DESGLOSE DE PIEZAS A TEJER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextUpperLabel
                )

                sortedParts.forEach { partWithRounds ->
                    val part = partWithRounds.part
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = WarmSurfaceSubtle,
                        border = BorderStroke(1.dp, WarmBorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = part.nombre,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EspressoDark
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "x${part.cantidadRequerida}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TerracottaPrimary
                                    )
                                }
                                if (part.notasPieza.isNotBlank()) {
                                    Text(
                                        text = part.notasPieza,
                                        fontSize = 11.sp,
                                        color = TextSubtleTaupe,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, WarmBorder)
                            ) {
                                Text(
                                    text = "${partWithRounds.rounds.size} vueltas c/u",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondaryBrown,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Materiales Extra y Notas de Ensamblaje
        if (extras.isNotEmpty() || pattern.notasEnsamblaje.isNotBlank()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                border = BorderStroke(1.dp, WarmBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (extras.isNotEmpty()) {
                        Text(
                            text = "MATERIALES EXTRA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextUpperLabel
                        )
                        extras.forEach { extra ->
                            Text(
                                text = "• $extra",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondaryBrown
                            )
                        }
                    }

                    if (pattern.notasEnsamblaje.isNotBlank()) {
                        if (extras.isNotEmpty()) Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "NOTAS DE ENSAMBLAJE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextUpperLabel
                        )
                        Text(
                            text = pattern.notasEnsamblaje,
                            fontSize = 12.sp,
                            color = TextSubtleTaupe,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Botones de Acción Inferiores
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isAlreadyActive) {
                Button(
                    onClick = { onContinueProject(pattern.id) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EspressoDark,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "▶ Continuar donde me quedé",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                OutlinedButton(
                    onClick = { onStartNewProject(pattern.id) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, WarmBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = DangerRed
                    )
                ) {
                    Text(
                        text = "Empezar desde cero (Reiniciar avance)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            } else {
                Button(
                    onClick = { onStartNewProject(pattern.id) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TerracottaPrimary,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = "🧶 ¡Me convence! Empezar Proyecto",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            if (pattern.esPersonalizado) {
                OutlinedButton(
                    onClick = { onSharePattern(pattern.id) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, TerracottaPrimary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = TerracottaPrimary
                    )
                ) {
                    Text(
                        text = "📤 Compartir con la comunidad",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SpecBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = WarmSurfaceSubtle,
        border = BorderStroke(1.dp, WarmBorderSubtle)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextUpperLabel
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = EspressoDark
            )
        }
    }
}
