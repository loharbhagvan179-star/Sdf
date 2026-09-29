package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryIndigoLight,
    onPrimary = Color(0xFF0F172A),
    primaryContainer = PrimaryIndigoDark,
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = CoinGold,
    onSecondary = Color(0xFF1E1B4B),
    secondaryContainer = Color(0xFF78350F),
    onSecondaryContainer = CoinGoldLight,
    tertiary = EmeraldSuccess,
    onTertiary = Color.White,
    background = DarkBg,
    onBackground = TextDarkPrimary,
    surface = DarkSurface,
    onSurface = TextDarkPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextDarkSecondary,
    outline = DarkSurfaceBorder,
    error = CoralError,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryIndigo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEF2FF),
    onPrimaryContainer = PrimaryIndigoDark,
    secondary = CoinGoldDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF92400E),
    tertiary = EmeraldSuccess,
    onTertiary = Color.White,
    background = LightBg,
    onBackground = TextLightPrimary,
    surface = LightSurface,
    onSurface = TextLightPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextLightSecondary,
    outline = LightSurfaceBorder,
    error = CoralError,
    onError = Color.White
)

@Composable
fun QuizAndEarnTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
