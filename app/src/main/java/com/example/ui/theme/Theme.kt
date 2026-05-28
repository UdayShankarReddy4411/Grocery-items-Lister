package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SunlitColorScheme = lightColorScheme(
    primary = DeepBlack,
    onPrimary = Color.White,
    primaryContainer = SunYellow,
    onPrimaryContainer = DeepBlack,
    secondary = SoftBlack,
    onSecondary = Color.White,
    background = SunYellow,
    onBackground = DeepBlack,
    surface = WarmPaper,
    onSurface = DeepBlack,
    surfaceVariant = LightYellow,
    onSurfaceVariant = DeepBlack,
    error = SignalRed,
    onError = Color.White,
    outline = DeepBlack
)

private val MidnightSunColorScheme = darkColorScheme(
    primary = SunYellow,
    onPrimary = DeepBlack,
    primaryContainer = SoftBlack,
    onPrimaryContainer = SunYellow,
    secondary = SunYellowDark,
    onSecondary = DeepBlack,
    background = DeepBlack,
    onBackground = SunYellow,
    surface = SoftBlack,
    onSurface = Color.White,
    surfaceVariant = DeepBlack,
    onSurfaceVariant = SunYellow,
    error = SignalRed,
    onError = Color.White,
    outline = SunYellow
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable dynamic colors to enforce our sunlit notebook branding
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        MidnightSunColorScheme
    } else {
        SunlitColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
