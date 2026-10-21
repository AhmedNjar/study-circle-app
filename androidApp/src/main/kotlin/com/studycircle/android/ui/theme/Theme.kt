package com.studycircle.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

data class ThemeController(
    val isDark: Boolean?,
    val onToggle: () -> Unit
)

val LocalThemeController = compositionLocalOf<ThemeController> {
    ThemeController(isDark = null, onToggle = {})
}

private val Emerald = Color(0xFF0A4232)
private val EmeraldBright = Color(0xFF22A378)
private val Gold = Color(0xFFD4AF37)
private val Ivory = Color(0xFFFBF9F5)
private val Mint = Color(0xFFE8F3EE)
private val Charcoal = Color(0xFF1C1B1F)

private val DarkBackground = Color(0xFF0F1D19)
private val DarkSurface = Color(0xFF192C25)
private val DarkSurfaceVariant = Color(0xFF223830)
private val DarkBorder = Color(0xFF274237)

private val LightColors = lightColorScheme(
    primary = Emerald,
    onPrimary = Color.White,
    primaryContainer = Mint,
    onPrimaryContainer = Emerald,
    secondary = Gold,
    onSecondary = Charcoal,
    secondaryContainer = Color(0xFFFFF8E1),
    background = Ivory,
    surface = Color.White,
    surfaceVariant = Color(0xFFF4F2EB),
    onBackground = Charcoal,
    onSurface = Charcoal,
    onSurfaceVariant = Color(0xFF6B6050),
    outlineVariant = Color(0xFFE5E0D8)
)

private val DarkColors = darkColorScheme(
    primary = EmeraldBright,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1B4035),
    onPrimaryContainer = Ivory,
    secondary = Gold,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF38311B),
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = Ivory,
    onSurface = Ivory,
    onSurfaceVariant = Color(0xFFB5C9C1),
    outlineVariant = DarkBorder
)

@Composable
fun StudyCircleTheme(
    content: @Composable () -> Unit
) {
    val themeController = LocalThemeController.current
    val darkTheme = themeController.isDark ?: isSystemInDarkTheme()
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = StudyCircleTypography,
        content = content
    )
}
