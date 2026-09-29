package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val RacingDarkColorScheme =
    darkColorScheme(
        primary = RacingRed,
        onPrimary = TextPrimary,
        secondary = CyberCyan,
        onSecondary = CarbonDark,
        tertiary = WarningYellow,
        background = CarbonDark,
        onBackground = TextPrimary,
        surface = CarbonSurface,
        onSurface = TextPrimary,
        surfaceVariant = CarbonCard,
        onSurfaceVariant = TextSecondary,
        error = RacingRedDark
    )

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = RacingDarkColorScheme,
        typography = Typography,
        content = content
    )
}
