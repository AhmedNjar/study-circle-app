package com.studycircle.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// The palette from the UI/UX discussion: emerald/teal primary (mosque
// architecture, the Prophetic green), warm gold accent (Quranic
// illumination), ivory rather than stark white. Dark mode keeps the same
// hues on a deep charcoal-green base instead of washing them out.
private val Emerald = Color(0xFF0F6E56)
private val EmeraldLight = Color(0xFF1D9E75)
private val Gold = Color(0xFFEF9F27)
private val Ivory = Color(0xFFFAF6EE)
private val Charcoal = Color(0xFF1C1C1C)
private val DeepCharcoalGreen = Color(0xFF0B1F1A)

private val LightColors = lightColorScheme(
    primary = Emerald,
    onPrimary = Color.White,
    secondary = Gold,
    onSecondary = Charcoal,
    background = Ivory,
    surface = Ivory,
    onBackground = Charcoal,
    onSurface = Charcoal
)

private val DarkColors = darkColorScheme(
    primary = EmeraldLight,
    onPrimary = Color.Black,
    secondary = Gold,
    onSecondary = Charcoal,
    background = DeepCharcoalGreen,
    surface = DeepCharcoalGreen,
    onBackground = Ivory,
    onSurface = Ivory
)

// TODO: swap the default Typography for a FontFamily built from Amiri
// (display/headers) and Cairo or Tajawal (body/UI) once the font files are
// added under androidApp/src/main/res/font/ — flagged in the UI/UX
// discussion as the thing this inline theme can't do that a real font can.
@Composable
fun StudyCircleTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
