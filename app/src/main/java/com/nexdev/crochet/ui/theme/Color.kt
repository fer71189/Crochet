package com.nexdev.crochet.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta Cálida y Pastel de Crochet Funny
val WarmCreamBg = Color(0xFFFFFBF7)
val WarmSurfaceSubtle = Color(0xFFFAF5F0)
val WarmChipBg = Color(0xFFF5ECE4)
val WarmAccentSurface = Color(0xFFF4E6DD)
val WarmBorder = Color(0xFFEAD9CA)
val WarmBorderSubtle = Color(0xFFEFE2D6)

// Gradiente de Bienvenida
val WelcomeGradientTop = Color(0xFFFFFBF7)
val WelcomeGradientMid = Color(0xFFFDF4EC)
val WelcomeGradientBottom = Color(0xFFF9E5D8)

// Colores Principales
val TerracottaPrimary = Color(0xFFE07A5F)
val TerracottaDark = Color(0xFFD0694E)
val TerracottaLightBg = Color(0xFFFFF0E6)
val AmberSecondary = Color(0xFFF4A261)

// Textos y Contrastes
val EspressoDark = Color(0xFF3D312E)
val EspressoDeep = Color(0xFF2A211F)
val TextSecondaryBrown = Color(0xFF5E4E4A)
val TextMutedBrown = Color(0xFF7D6B65)
val TextSubtleTaupe = Color(0xFF8C7A75)
val TextUpperLabel = Color(0xFF9A8780)

// Semánticos (Éxito / Peligro)
val SageSuccess = Color(0xFF528265)
val SageSuccessText = Color(0xFF437A57)
val SageSuccessBg = Color(0xFFE3F2E9)

val DangerRed = Color(0xFFC53929)
val DangerRedBg = Color(0xFFFCE8E6)

fun parseHexColor(hexString: String, fallback: Color = WarmAccentSurface): Color {
    return runCatching {
        Color(android.graphics.Color.parseColor(hexString))
    }.getOrDefault(fallback)
}
