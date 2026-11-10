package com.nexdev.crochet.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CrochetLightColorScheme = lightColorScheme(
    primary = TerracottaPrimary,
    onPrimary = Color.White,
    primaryContainer = WarmAccentSurface,
    onPrimaryContainer = EspressoDark,
    secondary = AmberSecondary,
    onSecondary = Color.White,
    secondaryContainer = WarmChipBg,
    onSecondaryContainer = TextSecondaryBrown,
    tertiary = SageSuccess,
    onTertiary = Color.White,
    tertiaryContainer = SageSuccessBg,
    onTertiaryContainer = SageSuccessText,
    background = WarmCreamBg,
    onBackground = EspressoDark,
    surface = Color.White,
    onSurface = EspressoDark,
    surfaceVariant = WarmSurfaceSubtle,
    onSurfaceVariant = TextMutedBrown,
    outline = WarmBorder,
    outlineVariant = WarmBorderSubtle,
    error = DangerRed,
    onError = Color.White,
    errorContainer = DangerRedBg,
    onErrorContainer = DangerRed
)

@Composable
fun CrochetTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CrochetLightColorScheme,
        typography = Typography,
        content = content
    )
}
