package com.abuhani.agri.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2D5E28),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB8E6B0),
    onPrimaryContainer = Color(0xFF1A3D16),
    secondary = Color(0xFF6B5B4E),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF0E0D0),
    onSecondaryContainer = Color(0xFF3D2E22),
    tertiary = Color(0xFF7B5E00),
    background = Color(0xFFFAF8F4),
    onBackground = Color(0xFF332820),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF332820),
    surfaceVariant = Color(0xFFF5F0E8),
    onSurfaceVariant = Color(0xFF6B5B4E),
    outline = Color(0xFFDDD4C0),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF5CAF56),
    onPrimary = Color(0xFF1A1612),
    primaryContainer = Color(0xFF1A3D16),
    onPrimaryContainer = Color(0xFFB8E6B0),
    secondary = Color(0xFFBFA898),
    onSecondary = Color(0xFF1A1612),
    secondaryContainer = Color(0xFF3D2E22),
    onSecondaryContainer = Color(0xFFF0E0D0),
    background = Color(0xFF1A1612),
    onBackground = Color(0xFFF0EDE8),
    surface = Color(0xFF2A231E),
    onSurface = Color(0xFFE5DDD4),
    surfaceVariant = Color(0xFF3A2E28),
    onSurfaceVariant = Color(0xFFBFA898),
    outline = Color(0xFF5C4E44),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
)

@Composable
fun AboHaniTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
