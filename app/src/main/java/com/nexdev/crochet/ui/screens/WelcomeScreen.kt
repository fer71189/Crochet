package com.nexdev.crochet.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexdev.crochet.ui.theme.AmberSecondary
import com.nexdev.crochet.ui.theme.EspressoDark
import com.nexdev.crochet.ui.theme.TerracottaLightBg
import com.nexdev.crochet.ui.theme.TerracottaPrimary
import com.nexdev.crochet.ui.theme.TextMutedBrown
import com.nexdev.crochet.ui.theme.TextSecondaryBrown
import com.nexdev.crochet.ui.theme.TextSubtleTaupe
import com.nexdev.crochet.ui.theme.WarmBorder
import com.nexdev.crochet.ui.theme.WelcomeGradientBottom
import com.nexdev.crochet.ui.theme.WelcomeGradientMid
import com.nexdev.crochet.ui.theme.WelcomeGradientTop
import com.nexdev.crochet.ui.theme.parseHexColor
import com.nexdev.crochet.ui.viewmodel.ActiveResumeSummary

@Composable
fun WelcomeScreen(
    resumeSummary: ActiveResumeSummary?,
    completedCount: Int,
    onExploreCatalog: () -> Unit,
    onResumeProject: (String) -> Unit,
    onOpenStats: () -> Unit,
    onOpenCreator: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        WelcomeGradientTop,
                        WelcomeGradientMid,
                        WelcomeGradientBottom
                    )
                )
            )
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header de Bienvenida
        Column(
            modifier = Modifier.padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, WarmBorder),
                shadowElevation = 2.dp
            ) {
                Text(
                    text = "✨ Tu asistente de amigurumis",
                    color = TerracottaPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .size(112.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(
                        width = 4.dp,
                        color = AmberSecondary.copy(alpha = 0.35f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = resumeSummary?.patternWithDetails?.pattern?.icono ?: "🐙",
                    fontSize = 54.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Crochet Funny",
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color = EspressoDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Teje sin perder la cuenta. Guardamos tu pieza y vuelta automáticamente en cada punto.",
                fontSize = 14.sp,
                color = TextMutedBrown,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        // Tarjeta Dinámica "Continuar Tejiendo" (Persistencia en Room)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = BorderStroke(1.dp, WarmBorder),
            shadowElevation = 4.dp
        ) {
            if (resumeSummary != null) {
                val pattern = resumeSummary.patternWithDetails.pattern
                val part = resumeSummary.currentPart.part
                val round = resumeSummary.currentRound
                val instance = resumeSummary.progress.currentPartInstance
                val percent = resumeSummary.metrics.percent

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = TerracottaLightBg
                        ) {
                            Text(
                                text = "📌 PROYECTO EN CURSO GUARDADO",
                                color = TerracottaPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = "$percent%",
                            color = TerracottaPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { percent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = TerracottaPrimary,
                        trackColor = WarmBorder.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(parseHexColor(pattern.colorBg)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = pattern.icono, fontSize = 26.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pattern.nombre,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = EspressoDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Pieza: ${part.nombre} ($instance/${part.cantidadRequerida})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondaryBrown
                            )
                            Text(
                                text = "Vuelta ${round.numero}: ${round.instruccion}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TerracottaPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { onResumeProject(pattern.id) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EspressoDark,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "▶ Continuar en Vuelta ${round.numero}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 28.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No tienes ningún tejido en pausa",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondaryBrown
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Explora el catálogo para iniciar tu próximo amigurumi.",
                        fontSize = 12.sp,
                        color = TextSubtleTaupe,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Acciones Inferiores
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onExploreCatalog,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TerracottaPrimary,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "Explorar Proyectos →",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenStats,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, WarmBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White.copy(alpha = 0.9f),
                        contentColor = TextSecondaryBrown
                    )
                ) {
                    Text(
                        text = "🏆 Finalizados ($completedCount)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                OutlinedButton(
                    onClick = onOpenCreator,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, WarmBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White.copy(alpha = 0.9f),
                        contentColor = TextSecondaryBrown
                    )
                ) {
                    Text(
                        text = "➕ Crear Patrón",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}
