package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DopamineCrimson,
    onPrimary = Color.White,
    primaryContainer = DopamineCrimsonDark,
    onPrimaryContainer = Color.White,
    secondary = DopamineAmber,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF4A3000),
    onSecondaryContainer = DopamineAmber,
    tertiary = DopamineEmerald,
    onTertiary = Color.Black,
    background = DopamineDeepNight,
    onBackground = DopamineTextPrimary,
    surface = DopamineCard,
    onSurface = DopamineTextPrimary,
    surfaceVariant = DopamineCardElevated,
    onSurfaceVariant = DopamineTextSecondary,
    outline = DopamineOutline
)

@Composable
fun JapaneseDopamineTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
