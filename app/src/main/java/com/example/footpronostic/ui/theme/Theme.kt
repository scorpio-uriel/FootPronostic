package com.example.footpronostic.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PitchGreen,
    onPrimary = DeepStadium,
    secondary = AccentNeon,
    background = DeepStadium,
    surface = SurfaceGlass,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PitchGreen,
    onPrimary = DeepStadium,
    secondary = AccentNeon,
    background = Color(0xFFF8FAFC),
    surface = Color.White
)

@Composable
fun FootPronosticTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Désactivez le dynamicColor pour garder notre style
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Assurez-vous que Typography est défini dans Type.kt
        content = content
    )
}