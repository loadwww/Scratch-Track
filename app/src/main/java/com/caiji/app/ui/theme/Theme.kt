package com.caiji.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 深红色主色调
private val DeepRed = Color(0xFFB71C1C)
private val DeepRedDark = Color(0xFF8B0000)
private val DeepRedLight = Color(0xFFE53935)
private val DeepRedContainer = Color(0xFFFFCDD2)
private val DeepRedOnContainer = Color(0xFF5D0A0A)

private val LightColors = lightColorScheme(
    primary = DeepRed,
    onPrimary = Color.White,
    primaryContainer = DeepRedContainer,
    onPrimaryContainer = DeepRedOnContainer,
    secondary = DeepRedDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFEbee),
    onSecondaryContainer = DeepRedOnContainer,
    tertiary = DeepRedLight,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = CardLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFFDE7E9),
    onSurfaceVariant = Color(0xFF5D4040),
    error = InvestRed,
    onError = Color.White,
    outline = DeepRed.copy(alpha = 0.5f)
)

private val DarkColors = darkColorScheme(
    primary = DeepRedLight,
    onPrimary = Color(0xFF5D0A0A),
    primaryContainer = DeepRedDark,
    onPrimaryContainer = DeepRedContainer,
    secondary = DeepRedLight,
    onSecondary = Color(0xFF5D0A0A),
    background = DarkBackground,
    onBackground = TextPrimaryDark,
    surface = CardDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0xFF3D2020),
    onSurfaceVariant = Color(0xFFE0B0B0),
    error = InvestRed,
    onError = Color.White,
    outline = DeepRedLight.copy(alpha = 0.5f)
)

@Composable
fun CaiJiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = CaiJiTypography,
        content = content
    )
}
