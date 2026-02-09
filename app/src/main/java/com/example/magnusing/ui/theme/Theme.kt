package com.example.magnusing.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

/**
 * Retro black & white color scheme
 * Consistent across all devices (no dynamic colors)
 */
private val BlackWhiteColorScheme = darkColorScheme(
    background = Color.Black,
    onBackground = Color.White,

    surface = Color.Black,
    onSurface = Color.White,

    primary = Color.White,
    onPrimary = Color.Black,

    secondary = Color.White,
    onSecondary = Color.Black
)

/**
 * Retro typography (classic chess feel)
 */
private val RetroTypography = Typography(
    titleLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        letterSpacing = 2.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        letterSpacing = 2.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Serif
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        letterSpacing = 1.5.sp
    )
)

/**
 * App theme
 */
@Composable
fun MagnusingTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BlackWhiteColorScheme,
        typography = RetroTypography,
        content = content
    )
}
