package com.nexdev.crochet.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.nexdev.crochet.ui.theme.EspressoDark
import com.nexdev.crochet.ui.theme.SageSuccessBg
import com.nexdev.crochet.ui.theme.SageSuccessText
import com.nexdev.crochet.ui.theme.TerracottaLightBg
import com.nexdev.crochet.ui.theme.TerracottaPrimary
import com.nexdev.crochet.ui.theme.TextSubtleTaupe
import com.nexdev.crochet.ui.theme.TextUpperLabel
import com.nexdev.crochet.ui.theme.WarmAccentSurface
import com.nexdev.crochet.ui.theme.WarmBorder
import com.nexdev.crochet.ui.theme.WarmCreamBg
import com.nexdev.crochet.ui.theme.parseHexColor
import com.nexdev.crochet.ui.viewmodel.CatalogUiState
import com.nexdev.crochet.ui.viewmodel.CompletedHistoryItem
import com.nexdev.crochet.ui.viewmodel.RankedPatternStat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatsScreen(
    uiState: CatalogUiState,
    onOpenPatternDetail: (String) -> Unit,
    onExploreCatalog: () -> Unit,
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
                    text = "Mis Logros 🏆",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = EspressoDark
                )
                Text(
                    text = "Proyectos terminados y ranking de favoritos",
                    fontSize = 12.sp,
                    color = TextSubtleTaupe
                )
            }

            Surface(
                shape = CircleShape,
                color = SageSuccessBg
            ) {
                Text(
                    text = "${uiState.totalCompletedCount} terminados",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SageSuccessText,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Sección 1: Ranking Más Realizados
            item(key = "header_most_made") {
                Text(
                    text = "🔥 MÁS REALIZADOS (TOP PATRONES)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextUpperLabel,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            if (uiState.mostMadeRanking.isEmpty()) {
                item(key = "empty_most_made") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, WarmBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Aún no hay proyectos finalizados.",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSubtleTaupe
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = onExploreCatalog,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = TerracottaPrimary,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = "Explorar Catálogo",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                itemsIndexed(
                    items = uiState.mostMadeRanking,
                    key = { _, item -> "rank_${item.patternWithDetails.pattern.id}" }
                ) { index, rankedItem ->
                    RankedPatternCard(
                        rank = index + 1,
                        item = rankedItem,
                        onClick = { onOpenPatternDetail(rankedItem.patternWithDetails.pattern.id) }
                    )
                }
            }

            // Sección 2: Historial Cronológico
            item(key = "header_history") {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "📅 HISTORIAL DE PROYECTOS FINALIZADOS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextUpperLabel,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            if (uiState.completedHistory.isEmpty()) {
                item(key = "empty_history") {
                    Text(
                        text = "Cuando termines la última vuelta de un amigurumi aparecerá registrado aquí con fecha y hora.",
                        fontSize = 12.sp,
                        color = TextSubtleTaupe,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(
                    items = uiState.completedHistory,
                    key = { "hist_${it.history.idHistorial}" }
                ) { historyItem ->
                    CompletedHistoryRow(
                        item = historyItem,
                        onKnitAgain = { onOpenPatternDetail(historyItem.patternWithDetails.pattern.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RankedPatternCard(
    rank: Int,
    item: RankedPatternStat,
    onClick: () -> Unit
) {
    val pattern = item.patternWithDetails.pattern
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, WarmBorder),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(WarmAccentSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#$rank",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TerracottaPrimary
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(parseHexColor(pattern.colorBg)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = pattern.icono, fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pattern.nombre,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = EspressoDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Categoría: ${pattern.categoria}",
                        fontSize = 11.sp,
                        color = TextSubtleTaupe
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = CircleShape,
                color = SageSuccessBg
            ) {
                Text(
                    text = "${item.count} ${if (item.count == 1) "vez" else "veces"}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SageSuccessText,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun CompletedHistoryRow(
    item: CompletedHistoryItem,
    onKnitAgain: () -> Unit
) {
    val pattern = item.patternWithDetails.pattern
    val formattedDate = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        .format(Date(item.history.completedAt))
    val durationMillis = (item.history.completedAt - item.history.startedAt).coerceAtLeast(0)
    val durationText = com.nexdev.crochet.ui.util.ProgressCalculator.formatElapsedDuration(durationMillis)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.9f),
        border = BorderStroke(1.dp, WarmBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = pattern.icono, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pattern.nombre,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = EspressoDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Finalizado el $formattedDate • ⏱️ Tomó: $durationText",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SageSuccessText
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                onClick = onKnitAgain,
                shape = RoundedCornerShape(10.dp),
                color = TerracottaLightBg
            ) {
                Text(
                    text = "Volver a tejer",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TerracottaPrimary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}
