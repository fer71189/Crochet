package com.nexdev.crochet.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexdev.crochet.data.local.entity.PatternWithPartsAndRounds
import com.nexdev.crochet.ui.theme.EspressoDark
import com.nexdev.crochet.ui.theme.SageSuccessBg
import com.nexdev.crochet.ui.theme.SageSuccessText
import com.nexdev.crochet.ui.theme.TerracottaLightBg
import com.nexdev.crochet.ui.theme.TerracottaPrimary
import com.nexdev.crochet.ui.theme.TextMutedBrown
import com.nexdev.crochet.ui.theme.TextSecondaryBrown
import com.nexdev.crochet.ui.theme.TextSubtleTaupe
import com.nexdev.crochet.ui.theme.TextUpperLabel
import com.nexdev.crochet.ui.theme.WarmAccentSurface
import com.nexdev.crochet.ui.theme.WarmBorder
import com.nexdev.crochet.ui.theme.WarmChipBg
import com.nexdev.crochet.ui.theme.WarmCreamBg
import com.nexdev.crochet.ui.theme.parseHexColor
import com.nexdev.crochet.ui.util.ProgressCalculator
import com.nexdev.crochet.ui.viewmodel.CatalogUiState

@Composable
fun CatalogScreen(
    uiState: CatalogUiState,
    onSelectCategory: (String) -> Unit,
    onOpenPatternDetail: (String) -> Unit,
    onResumeActiveProject: (String) -> Unit,
    onNavigateHome: () -> Unit,
    onOpenCreator: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WarmCreamBg)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Cabecera
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Proyectos",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = EspressoDark
                )
                Text(
                    text = "Escoge un diseño para empezar a tejer",
                    fontSize = 12.sp,
                    color = TextSubtleTaupe
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    onClick = onOpenCreator,
                    shape = CircleShape,
                    color = WarmAccentSurface
                ) {
                    Text(
                        text = "➕ Nuevo",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TerracottaPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }

                Surface(
                    onClick = onNavigateHome,
                    shape = CircleShape,
                    color = WarmAccentSurface,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "🏠", fontSize = 16.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Chips de Categorías
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(uiState.categories) { category ->
                val isSelected = uiState.selectedCategory.equals(category, ignoreCase = true)
                Surface(
                    onClick = { onSelectCategory(category) },
                    shape = CircleShape,
                    color = if (isSelected) EspressoDark else Color.White,
                    border = if (isSelected) null else BorderStroke(1.dp, WarmBorder),
                    shadowElevation = if (isSelected) 2.dp else 0.dp
                ) {
                    Text(
                        text = category,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else TextSecondaryBrown,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp, top = 4.dp)
        ) {
            // Banner rápido de Continuar si hay sesión activa
            uiState.latestResumeSummary?.let { resume ->
                item(key = "active_banner") {
                    val pat = resume.patternWithDetails.pattern
                    val part = resume.currentPart.part
                    Surface(
                        onClick = { onResumeActiveProject(pat.id) },
                        shape = RoundedCornerShape(20.dp),
                        color = EspressoDark,
                        shadowElevation = 3.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = pat.icono, fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "EN CURSO • ${resume.metrics.percent}%",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TerracottaPrimary
                                    )
                                    Text(
                                        text = "${pat.nombre} • ${part.nombre} (${resume.progress.currentPartInstance}/${part.cantidadRequerida}) • V${resume.currentRound.numero}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = TerracottaPrimary
                            ) {
                                Text(
                                    text = "Reanudar ▶",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Carrusel horizontal "Más Realizados" si existen estadísticas
            if (uiState.mostMadeRanking.isNotEmpty()) {
                item(key = "most_made_section") {
                    Column {
                        Text(
                            text = "🔥 MÁS REALIZADOS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextUpperLabel,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(uiState.mostMadeRanking, key = { it.patternWithDetails.pattern.id }) { ranked ->
                                val pat = ranked.patternWithDetails.pattern
                                Surface(
                                    onClick = { onOpenPatternDetail(pat.id) },
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, WarmBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = pat.icono, fontSize = 20.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = pat.nombre,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = EspressoDark
                                            )
                                            Text(
                                                text = "Tejido ${ranked.count}x",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = SageSuccessText
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Lista de Patrones
            items(
                items = uiState.filteredPatterns,
                key = { it.pattern.id }
            ) { item ->
                val patId = item.pattern.id
                val timesMade = uiState.completedCountsByPatternId[patId] ?: 0
                val isCurrentActive = uiState.activeProgressByPatternId.containsKey(patId)

                PatternCatalogCard(
                    patternWithDetails = item,
                    timesMade = timesMade,
                    isCurrentActive = isCurrentActive,
                    onClick = { onOpenPatternDetail(patId) }
                )
            }
        }
    }
}

@Composable
private fun PatternCatalogCard(
    patternWithDetails: PatternWithPartsAndRounds,
    timesMade: Int,
    isCurrentActive: Boolean,
    onClick: () -> Unit
) {
    val pattern = patternWithDetails.pattern
    val totalParts = patternWithDetails.parts.sumOf { it.part.cantidadRequerida }
    val totalRounds = patternWithDetails.parts.sumOf { it.rounds.size * it.part.cantidadRequerida }
    val formattedTime = ProgressCalculator.formatDuration(pattern.tiempoEstimadoMin)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = BorderStroke(
            width = if (isCurrentActive) 2.dp else 1.dp,
            color = if (isCurrentActive) TerracottaPrimary else WarmBorder
        ),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(parseHexColor(pattern.colorBg)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = pattern.icono, fontSize = 30.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = WarmChipBg
                    ) {
                        Text(
                            text = pattern.categoria.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextMutedBrown,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    if (timesMade > 0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SageSuccessBg
                        ) {
                            Text(
                                text = "✓ Tejido ${timesMade}x",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SageSuccessText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (isCurrentActive) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = TerracottaLightBg
                        ) {
                            Text(
                                text = "En curso",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TerracottaPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = pattern.nombre,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = EspressoDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "🧩 $totalParts piezas • 🔄 $totalRounds vueltas • ⏱️ $formattedTime",
                    fontSize = 12.sp,
                    color = TextSubtleTaupe
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "›",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextSubtleTaupe
            )
        }
    }
}
